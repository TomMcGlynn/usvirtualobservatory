using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Web;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Net;
using System.Text;
using System.Data.SqlClient;
using oai;
using oai_dc;
using net.ivoa;
using ivoa.net.ri1_0.server;
using registry;


namespace IntegrityReport
{
    class IntegrityReport
    {
        protected static string sConnect = Properties.Settings.Default.SqlConnection;
        protected static string baseUrl = Properties.Settings.Default.baseURL;
        protected static string adminPWD = Properties.Settings.Default.dbAdmin;
        protected static string oaiUrl = Properties.Settings.Default.oaiURL;

        private static bool argShowAll = false;
        private static bool argShowDeleted = false;
        //private static bool argShowInactive = false;
        private static bool argFix = false;
        private static bool argUseALL = false;
        private static bool argUseURL = false;
        private static string argSpecifiedURL = string.Empty;
        private static string argSince = string.Empty;

        private static Hashtable recordsToFix = null;
        private static int totalLocalOAI = 0;
        private static int totalRemoteOAI = 0;
        private static int totalMissingOAI = 0;
        private static int totalMissingCache = 0;
        private static int totalDeleted = 0;
        private static int totalFixed = 0;

        static void Main(string[] args)
        {
            if (!baseUrl.EndsWith("/")) baseUrl += '/';

            ParseArguments(args);
            if (TestValidArguments(args.Length) == false)
                return;

            SqlConnection conn = null;
            string[] harvestURLs = null;
            int[] areHarvesting = null;
            try
            {
                conn = new SqlConnection(sConnect);

                ArrayList localOAIRecords = new ArrayList();
                bool gotLocalOAI = GetOAIRecords(baseUrl + oaiUrl, false, ref localOAIRecords);
                if (gotLocalOAI == false)
                {
                    Console.WriteLine("Could not retrieve local OAI records to compare.");
                    return;
                }
                totalLocalOAI = localOAIRecords.Count;

                //If a URL is specified, force a check against it, and ignore all others in the RofR harvester DB table.
                if (argUseURL == true) 
                {
                    harvestURLs = new string[1];
                    harvestURLs[0] = argSpecifiedURL;
                    areHarvesting = new int[1];
                    areHarvesting[0] = 1;
                }
                else
                    GetHarvesterInfo(conn, ref harvestURLs, ref areHarvesting);

                int numURLS = harvestURLs.Length;
                for (int i = 0; i < numURLS; ++i)
                {
                    //can't compare our own interface against itself....
                    if (harvestURLs[i].StartsWith(baseUrl))
                        continue;

                    if (areHarvesting[i] == 1)
                    {
                        Console.WriteLine("Registry at " + harvestURLs[i]);
                        ListMissingRecords(harvestURLs[i], ref localOAIRecords);
                    }
                    else
                    {
                        Console.WriteLine("Registry at " + harvestURLs[i] +
                            " . Not Currently Harvesting");
                        if (argShowAll == true)
                        {
                            ListMissingRecords(harvestURLs[i], ref localOAIRecords);
                        }
                    }
                    Console.WriteLine();

                    if (argFix == true)
                    {
                        FixRecords(harvestURLs[i]);
                        Console.WriteLine();
                    }

                }

                //if (argFix == true)
                //{
                //    FixRecords();
                //}
                ReportTotals();
             }
            catch (Exception e)
            {
                Console.WriteLine("Error: " + e);
            }
        }

        #region Argument Handling
        public static void ParseArguments(string[] args)
        {
            foreach (string str in args) {
                string arg = str.ToLower();
                if (arg == "showinactiveregistries")
                    argShowAll = true;
                else if (arg == "showdeleted")
                    argShowDeleted = true;
                //else if (arg == "showinactiverecords")
                //    argShowInactive = true;
                else if (arg == "fix")
                    argFix = true;
                else if (arg == "all")
                    argUseALL = true;
                else if (arg.StartsWith("url:"))
                {
                    argUseURL = true;
                    if (arg.Length > 4)
                        argSpecifiedURL = str.Substring(4).Trim();
                }
                else if (arg.StartsWith("since:"))
                {
                    if( arg.Length > 6 )
                        argSince = arg.Substring(6).Trim();
                }
            }
        }

        public static bool TestValidArguments(int count)
        {
            if (count == 0)
            {
                Console.WriteLine("Command line arguments are as follows:");
                Console.WriteLine("   \"all\" or \"url:[specified OAI Interface]\" - test all registries in RofR, or a specified url. One or the other of these is mandatory.");
                Console.WriteLine("   \"showinactiveregistries\" - test registries in the RofR not currently being harvested, if \"all\" option used. Optional, default off.");
                Console.WriteLine("   \"showdeleted\" - Show records marked 'deleted' in OAI interface headers to check for local OAI or Search interface consistency. Optional, default off.");
                Console.WriteLine("   \"fix\" - attempt to re-harvest all resources determined to be missing from local OAI or Search interfaces. Optional, default off.");
            }

            if (argUseURL == true && argSpecifiedURL == string.Empty)
            {
                Console.WriteLine("url: argument specified, but no URL given");
                return false;
            }
            else if (argUseURL == false && argUseALL == false)
            {
                Console.WriteLine("Command line must specify whether to test all resources in the RofR " +
                    "with argument \"all\" or a specific OAI interface wuth argument \"url:[OAI interface url]\"");
                return false;
            }
            if (argFix == true)
            {
                recordsToFix = new Hashtable();
            }
            return true;
        }
        #endregion

        public static void ReportTotals()
        {
            Console.WriteLine();
            Console.WriteLine("Total Active Records in Local OAI Interface: " + totalLocalOAI);
            Console.WriteLine("Total Active Remote Records in OAI ListIdentifiers: " + totalRemoteOAI);
            Console.WriteLine("Total Active Remote OAI Records Found Missing: " + totalMissingOAI);
            Console.WriteLine("Total Active Remote OAI Records Not In Search Cache: " + totalMissingCache);
            Console.WriteLine("Total Remote OAI Records Deleted and Correctly Missing: " + totalDeleted);
            Console.WriteLine("Total Records Re-Harvested: " + totalFixed);
            Console.WriteLine();

        }

        public static void GetHarvesterInfo(SqlConnection conn, ref string[] harvestURLs, ref int[] areHarvesting)
        {
            string sGetEntry = "select distinct serviceurl, harvest from harvester";

            SqlDataAdapter sqlDA = new SqlDataAdapter(sGetEntry, conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);

            harvestURLs = new string[ds.Tables[0].Rows.Count];
            areHarvesting = new int[ds.Tables[0].Rows.Count];
            for (int i = 0; i < ds.Tables[0].Rows.Count; ++i)
            {
                DataRow row = ds.Tables[0].Rows[i];
                harvestURLs[i] = (string)row[0];
                if ((byte)row[1] > 0)
                    areHarvesting[i] = 1;
                else
                    areHarvesting[i] = 0;
            }

            //hack to force vizier last. It takes hours longer than any of the other registries.
            int maxindex = harvestURLs.Length - 1;
            if (maxindex > 0)
            {
                for (int i = 0; i < maxindex; ++i) //don't bother switching if it's already last
                {
                    /*if (harvestURLs[i].ToUpper().Contains("VIZIER"))
                    {
                        string tempURL = harvestURLs[maxindex];
                        int tempHarvest = areHarvesting[maxindex];
                        harvestURLs[maxindex] = harvestURLs[i];
                        areHarvesting[maxindex] = areHarvesting[i];
                        harvestURLs[i] = tempURL;
                        areHarvesting[i] = tempHarvest;

                        break;
                    }*/
                }
            }
        }

        public static bool GetOAIRecords(string url, bool managed_only, ref ArrayList records)
        {
            records = new ArrayList();

            bool nextToken = true;
			string rt = null;
            string useURL;

            while (nextToken)
            {
                if (rt == null)
                {
                    if (url.EndsWith("?"))
                        useURL = url + "verb=ListIdentifiers&metadataPrefix=ivo_vor";
                    else
                        useURL = url + "?verb=ListIdentifiers&metadataPrefix=ivo_vor";
                    if (managed_only)
                        useURL = useURL + "&set=ivo_managed";
                    if (argSince != string.Empty)
                        useURL = useURL + "&from=" + argSince;
                }
                else //we have resumption tokens
                {
                    if (url.EndsWith("?"))
                        useURL = url + "verb=ListIdentifiers&resumptionToken=" + rt;
                    else
                        useURL = url + "?verb=ListIdentifiers&resumptionToken=" + rt;
                }
                HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(useURL);
                // Sends the HttpWebRequest and waits for the response. 
                HttpWebResponse resp = null;
                try
                {
                    resp = (HttpWebResponse)wr.GetResponse();
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error getting response from url " + useURL + ". " + e.Message);
                    return false;
                }

                if (resp == null)
                {
                    Console.WriteLine("No response from url " + useURL);
                    return false;
                }


                // Gets the stream associated with the response.
                Stream receiveStream = resp.GetResponseStream();
                Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
                // Pipes the stream to a higher level stream reader with the required encoding format. 
                StreamReader stream = new StreamReader(receiveStream, encode);

                // it is OAI we presume so make a serializer for it
                // of course, several registries are giving back noncompliant errors on "no records"
                // so we're going out on a limb here and guessing that's what deserialisation errors are.
                OAIPMHtype oai = null;
                try
                {
                    XmlSerializer ser = new XmlSerializer(typeof(OAIPMHtype));
                    oai = (OAIPMHtype)ser.Deserialize(stream);

                    if (oai.Items[0] is oai.OAIPMHerrorType)
                    {
                        OAIPMHerrorType err = oai.Items[0] as OAIPMHerrorType;
                        if (err.code == OAIPMHerrorcodeType.noRecordsMatch)
                        {
                            Console.WriteLine(" No records found for url " + useURL + ". " + ((OAIPMHerrorType)oai.Items[0]).Value);
                            return false;
                        }
                    }
                }
                catch (System.NullReferenceException)
                {
                    Console.WriteLine(" No records found for url " + useURL + ". ");
                    return false;
                }

                try
                {
                    if (oai.Items[0].GetType() == typeof(ListIdentifiersType))
                    {
                        if (((ListIdentifiersType)oai.Items[0]).resumptionToken == null)
                            rt = null;
                        else
                            rt = ((ListIdentifiersType)oai.Items[0]).resumptionToken.Value;
                    }
                }
                catch (Exception se)
                {
                    Console.WriteLine(se + " : Problem Resumption Token" + se.Message);
                    return false;
                }

                nextToken = rt != null && rt.Length > 0;


                headerType[] recs = null;

                try
                {
                    if (oai.Items[0].GetType() == typeof(ListIdentifiersType))
                    {
                        recs = ((ListIdentifiersType)oai.Items[0]).header;
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("No records. " + e.Message);
                    return true;
                }
                if (recs == null)
                {
                    Console.WriteLine("No records.");
                    return false;
                }

                for (int r = 0; r < recs.Length; r++)
                {
                    if (recs[r].status == statusType.deleted)
                    {
                        if (argShowDeleted) //tdower test. this should catch some heasarc records, or they are oai non-compliant
                            Console.WriteLine("Record marked deleted in remote OAI: " + recs[r].identifier);
                    }
                    else
                    {
                        string id = recs[r].identifier.Trim();
                        records.Add(id);
                    }
                }
            }
            return true;
        }

        public static bool recordIsDeleted(string url, string id)
        {
            if (url.EndsWith("?"))
                url = url + "verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + id;
            else
                url = url + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + id;

            HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(url);
            // Sends the HttpWebRequest and waits for the response. 
            HttpWebResponse resp = null;
            try
            {
                resp = (HttpWebResponse)wr.GetResponse();
            }
            catch (Exception e)
            {
                Console.WriteLine("Trying to determine record deletion state: Error getting response from url " + url + ". " + e.Message);
                return false;
            }

            if (resp == null)
            {
                Console.WriteLine("Trying to determine record deletion state: No response from url " + url);
                return false;
            }

            // Gets the stream associated with the response.
            Stream receiveStream = resp.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
            // Pipes the stream to a higher level stream reader with the required encoding format. 
            StreamReader stream = new StreamReader(receiveStream, encode);

            // it is OAI we presume so make a serializer for it
            // of course, several registries are giving back noncompliant errors on "no records"
            // so we're going out on a limb here and guessing that's what deserialisation errors are.
            OAIPMHtype oai = null;
            try
            {
                XmlSerializer ser = new XmlSerializer(typeof(OAIPMHtype));
                oai = (OAIPMHtype)ser.Deserialize(stream);

                if (oai.Items[0] is oai.OAIPMHerrorType)
                {
                    OAIPMHerrorType err = oai.Items[0] as OAIPMHerrorType;
                    if (err.code == OAIPMHerrorcodeType.idDoesNotExist)
                    {
                        return true;
                    }
                }
                else if (oai.Items[0] is oai.GetRecordType)
                {
                    GetRecordType rec = oai.Items[0] as GetRecordType;
                    if (rec.record != null && rec.record.metadata != null &&
                        rec.record.metadata.Attributes["status"] != null && rec.record.metadata.Attributes["status"].Value.ToString().ToLower() == "deleted")
                    {
                        Console.WriteLine("Deleted record missing OAI header status from remote OAI. " + id);
                        return true;
                    }
                }
            }
            catch (System.NullReferenceException)
            {
                Console.WriteLine(" Error discovering possibly-deleted record " + id);
                return false;
            }

            return false;
        }

        public static bool recordInLocalSearch(string id)
        {
            string url = baseUrl + "NVORegInt.asmx/VOTKeyword?andKeys=true&keywords=" + id;

            HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(url);
            // Sends the HttpWebRequest and waits for the response. 
            HttpWebResponse resp = null;
            try
            {
                resp = (HttpWebResponse)wr.GetResponse();
            }
            catch (Exception e)
            {
                Console.WriteLine("Trying to determine record search state: Error getting response from url " + url + ". " + e.Message);
                return false;
            }

            if (resp == null)
            {
                Console.WriteLine("Trying to determine search deletion state: No response from url " + url);
                return false;
            }

            Stream receiveStream = resp.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
            StreamReader stream = new StreamReader(receiveStream, encode);
            string Response = stream.ReadToEnd();

            if( Response.Contains("<TABLEDATA>") )
                return true;

            return false;
        }

        public static bool recordIsMarkedInactive(string id)
        {

            string testURL = baseUrl + "registryadmin.asmx/DSQuery?sqlStmnt=" +
                "select top 1 [@status] from resource where identifier = '" + id +
                "' order by harvestedFromDate desc";

            testURL += "&password=" + adminPWD;

            HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(testURL);
            HttpWebResponse resp = null;
            try
            {
                resp = (HttpWebResponse)wr.GetResponse();
            }
            catch (Exception e)
            {
                Console.WriteLine("Error getting response from local query URL" + e.Message);
                return false;
            }

            if (resp == null)
            {
                Console.WriteLine("No response from local query URL");
                return false;
            }

            Stream receiveStream = resp.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
            StreamReader stream = new StreamReader(receiveStream, encode);
            string textresponse = stream.ReadToEnd();

            int iTable = textresponse.IndexOf("<Table");
            if (iTable > -1)
            {
                textresponse = textresponse.Substring(iTable);
                int iStatus = textresponse.IndexOf("status");
                if (iStatus > -1)
                {
                    iStatus = textresponse.IndexOf('>', iStatus) + 1;
                    try
                    {
                        int iEndStatus = textresponse.IndexOf("</", iStatus);
                        if (iEndStatus > iStatus && Convert.ToInt16(textresponse.Substring(iStatus, iEndStatus - iStatus)) == 0)
                            return true;
                    }
                    catch (Exception)
                    {
                        return false;
                    }

                }
            }
            return false;
        }

        //tdower todo - look for "status=\"deleted\"" || "status=\'deleted\'" || "status=\'inactive\'" records in remote OAI
        public static void ListMissingRecords(string url, ref ArrayList localOAIRecords)
        {
            ArrayList remoteOAIRecords = new ArrayList();
            bool gotRecords = GetOAIRecords(url, true, ref remoteOAIRecords);
            if (gotRecords == false)
                return;

            ArrayList missingOAIRecords = new ArrayList();
            ArrayList missingSearchRecords = new ArrayList();
            ArrayList missingInactiveRecords = new ArrayList();
            ArrayList deletedRecords = new ArrayList();

            totalRemoteOAI += remoteOAIRecords.Count;
            if( argSince != string.Empty)
                Console.WriteLine("Total active records in OAI since " + argSince + " : " + remoteOAIRecords.Count);
            else
                Console.WriteLine("Total active records in OAI: " + remoteOAIRecords.Count);
            for (int i = 0; i < remoteOAIRecords.Count; ++i)
            {
                string currentRemote = (string)remoteOAIRecords[i];

                try
                {
                    if (localOAIRecords.Contains(currentRemote))
                    {
                        if (!recordInLocalSearch(currentRemote))
                            missingSearchRecords.Add(currentRemote);
                    }
                    else
                    {
                        if (recordIsMarkedInactive(currentRemote))
                            missingInactiveRecords.Add(currentRemote);
                        else if (recordIsDeleted(url, currentRemote))
                        {
                            if (argShowDeleted == true)
                                deletedRecords.Add(currentRemote);
                        }
                        else
                            missingOAIRecords.Add(currentRemote);
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error processing individual record: " + currentRemote + " from " + url + " . " + e);
                }
            }

            if (missingInactiveRecords.Count > 0)
            {
                Console.WriteLine("Records marked inactive in registry that will not be shown in OAI or search results: "
                    + missingInactiveRecords.Count);
                for (int i = 0; i < missingInactiveRecords.Count; ++i)
                {
                    Console.WriteLine((string)missingInactiveRecords[i]);
                }
                Console.WriteLine();
            }

            if( missingOAIRecords.Count > 0 )
            {
                try
                {
                    if (argFix == true)
                        recordsToFix.Add(url, missingOAIRecords);

                    Console.WriteLine("Records missing from local OAI Interface: " + missingOAIRecords.Count);
                    for (int i = 0; i < missingOAIRecords.Count; ++i)
                    {
                        Console.WriteLine((string)missingOAIRecords[i]);
                    }
                    Console.WriteLine();
                }
                catch (ArgumentException ex)
                {
                    Console.WriteLine();
                    Console.WriteLine("Error adding missing " + url + " OAI records to list. " + ex.Message);
                    foreach (string str in missingOAIRecords)
                        Console.WriteLine("    " + str);
                    Console.WriteLine();
                }

            }
            if (missingSearchRecords.Count > 0)
            {
                try
                {
                    if (argFix == true) //if they're in our OAI interface but missing from search, try re-caching locally.
                        recordsToFix.Add(baseUrl + oaiUrl, missingSearchRecords);

                    Console.WriteLine("Records in local OAI interface but missing from local search Interface: "
                        + missingSearchRecords.Count);
                    for (int i = 0; i < missingSearchRecords.Count; ++i)
                    {
                        Console.WriteLine((string)missingSearchRecords[i]);
                    }
                    Console.WriteLine();
                }
                catch (ArgumentException ex)
                {
                    Console.WriteLine();
                    Console.WriteLine("Error adding missing " + url + " search cache records to list: " + ex.Message);
                    foreach (string str in missingSearchRecords)
                        Console.WriteLine("    " + str);
                    Console.WriteLine();
                }

            }
            if (argShowDeleted == true && deletedRecords.Count > 0)
            {
                Console.WriteLine("Records shown deleted by remote OAI interface. " + 
                    "These should be missing from local search." + deletedRecords.Count);
                for (int i = 0; i < deletedRecords.Count; ++i)
                {
                    Console.WriteLine((string)deletedRecords[i]);
                }
                Console.WriteLine();
            }

            totalMissingOAI += missingOAIRecords.Count;
            totalDeleted += deletedRecords.Count;
            totalMissingCache += missingSearchRecords.Count;
        }

        #region Re-harvesting missing records (if "fix" argument is used)
        public static void FixRecords(string thisURL = null)
        {
            if( argFix == true )
            {
                bool recordSuccess = false;
                string errMsg = string.Empty;

                Console.WriteLine("Harvesting missing records.");
                foreach (string strURLKey in recordsToFix.Keys)
                {
                    if (thisURL == null || thisURL == strURLKey)
                    {
                        ArrayList records = ((ArrayList)recordsToFix[strURLKey]);
                        Console.WriteLine("Harvesting records from url: " + strURLKey);
                        foreach (string id in records)
                        {
                            recordSuccess = HarvestOneRecord(strURLKey, id, ref errMsg);
                            if (recordSuccess)
                            {
                                Console.WriteLine("SUCCESS: Record " + id);
                                ++totalFixed;
                            }
                            else
                                Console.WriteLine("FAILURE: Record " + id + ". Error: " + errMsg);
                        }
                    }
                }
            }
        }

        public static bool HarvestOneRecord(string url, string id, ref string error)
        {
            bool success = false;
            if (argFix)
            {
                string harvestURL = baseUrl + "registryadmin.asmx/HarvestRecord?IVOA_id=" + id;
                harvestURL += "&passphrase=" + adminPWD + "&url=" + url;

                HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(harvestURL);
                // Sends the HttpWebRequest and waits for the response. 
                HttpWebResponse resp = null;
                try
                {
                    resp = (HttpWebResponse)wr.GetResponse();
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error getting response from local harvest URL" + e.Message);
                    return false;
                }

                if (resp == null)
                {
                    Console.WriteLine("No response from local harvest URL");
                    return false;
                }

                Stream receiveStream = resp.GetResponseStream();
                Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
                StreamReader stream = new StreamReader(receiveStream, encode);
                string textresponse = stream.ReadToEnd();

                int iLoaded = textresponse.IndexOf("Loaded");
                if (iLoaded > -1)
                {
                    string[] parsedResponse = textresponse.Substring(iLoaded).Split(new char[] { ' ' });
                    if (parsedResponse.Length > 1 && Convert.ToInt16(parsedResponse[1]) > 0)
                        success = true;
                    else
                        error = textresponse.Substring(iLoaded).Replace("</string>", "");
                }
                else if (textresponse.IndexOf("HARVEST RECORD") > -1)
                    error = textresponse.Substring(textresponse.IndexOf("HARVEST RECORD")).Replace("</string>", "");
                else
                    error = "Malformed response from local harvest.";

            }
            return success;
        }
        #endregion

    }
}
