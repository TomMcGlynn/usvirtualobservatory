using System;
using System.Net;
using System.IO;
using System.Xml.Serialization;
using oai;
using System.Text;
using System.Xml;
using registry;
using System.Configuration;
using System.Collections;

namespace registry
{
	/// <summary>
	/// Summary description for Class1.
	///Current version
	///ID:		$Id: Harvester.cs,v 1.2 2006/04/19 15:39:58 grgreene Exp $
	///Revision:	$Revision: 1.2 $
	///Date:	$Date: 2006/04/19 15:39:58 $
	/// </summary>
	public class Harvester
	{

		public static VOResourceParser vop = new VOResourceParser();

        public static VOR_XML vorXML = new VOR_XML();

		public static string XMLHEADER = @"<?xml version=""1.0"" encoding=""UTF-8""?>";

        private static string dbAdmin = Properties.Settings.Default.dbAdmin;
        private static string log_location = Properties.Settings.Default.log_location;

		public static StringBuilder sb = new StringBuilder();

        private logfile errLog;

        private ArrayList knownbad = new ArrayList();

        public Harvester()
        {
            errLog = new logfile("err_HarvesterService.log");

            //These are known bad records to be managed by hand.
            //Note we can *delete* them easily enough, if they were already imported
            //somehow. If they're in this list, they've repeatedly failed to import properly
            //and been caught in the logs by a real person.
            try
            {
                string file = "known_bad_records.txt";
                string dir = Properties.Settings.Default.vdir;
                if (dir != null)
                {
                    if (!dir.EndsWith("\\")) dir += "\\";
                    file = dir + file;
                }
                using (System.IO.StreamReader sr = new System.IO.StreamReader(file))
                {
                    string line;
                    while( (line = sr.ReadLine()) != null)
                        knownbad.Add(line);
                }
            }
            catch (Exception) { }

        }

        public oai.granularityType GetTimeGranularity(string baseurl)
        {
            string url = baseurl;
            if (!baseurl.EndsWith("?"))
                url += "?";

            url += "verb=Identify";

            HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(url);
            // Sends the HttpWebRequest and waits for the response. 
            HttpWebResponse resp = null;
            try
            {
                resp = (HttpWebResponse)wr.GetResponse();
            }
            catch (Exception e)
            {
                sb.Append(" Harvester: " + e.Message);
            }

            if (resp == null)
            {
                sb.Append("\nError: No time granularity can be determined.");
                throw new Exception(sb.ToString());
            }

            // Gets the stream associated with the response.
            Stream receiveStream = resp.GetResponseStream();
            Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
            // Pipes the stream to a higher level stream reader with the required encoding format. 
            StreamReader stream = new StreamReader(receiveStream, encode);

            // it is OAI we presume so make a serializer for it
            OAIPMHtype oai = null;
            XmlSerializer ser = new XmlSerializer(typeof(OAIPMHtype));
            oai = (OAIPMHtype)ser.Deserialize(stream);

            if (oai.Items[0] is oai.OAIPMHerrorType)
            {
                OAIPMHerrorType err = oai.Items[0] as OAIPMHerrorType;
                sb.Append("\nOAI Error :");
                sb.Append(((OAIPMHerrorType)oai.Items[0]).Value);

                throw new Exception(sb.ToString());
            }
            else if (oai.Items[0] is oai.IdentifyType)
            {
                granularityType gran = ((oai.IdentifyType)(oai.Items[0])).granularity;
                return gran;
            }
            else
            {
                sb.Append("\nError: No time granularity can be determined.");
                throw new Exception(sb.ToString());
            }
            //return granularityType.YYYYMMDD;
        }

		public string harvest(string baseurl, string extraParams) 
		{
            string connect = Properties.Settings.Default.SqlAdminConnection;
            if (null == connect)
                connect = Properties.Settings.Default.SqlConnection;
			if (null == connect) connect = "bad connection string";
			RegistryAdmin reg = new RegistryAdmin(connect);
			string url= "";

			if ( baseurl.Contains("?"))
				url = baseurl + extraParams;
            else
				url = baseurl + "?" + extraParams;

			bool nextToken = true;
			string rt = null;
			StringBuilder sb = new StringBuilder();

            int recFailures = 0;
            int recSkipped = 0;
			while (nextToken)
			{
                try
                {
                    if (rt != null)
                    {
                        if (baseurl.EndsWith("?"))
                            url = baseurl + "verb=ListRecords&resumptionToken=" + rt;
                        else
                            url = baseurl + "?verb=ListRecords&resumptionToken=" + rt;
                    }
                    sb.Append(DateTime.Now + " Harvesting " + url);
                    Console.Out.WriteLine(DateTime.Now + " Harvesting " + url);
                    HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(url);
                    // Sends the HttpWebRequest and waits for the response. 
                    HttpWebResponse resp = null;
                    try
                    {
                        resp = (HttpWebResponse)wr.GetResponse();
                    }
                    catch (Exception e)
                    {
                        sb.Append(" Harvester: " + e.Message + " : " + e.StackTrace);
                        return sb.ToString();
                    }

                    if (resp == null) return sb.ToString();

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
                                return "No Records to Harvest";
                            else if (err.code == OAIPMHerrorcodeType.idDoesNotExist)
                                return "Individual record does not exist";

                            sb.Append("\nOAI Error :");
                            sb.Append(((OAIPMHerrorType)oai.Items[0]).Value);

                            throw new Exception(sb.ToString());
                        }
                    }
                    catch (System.NullReferenceException)
                    {
                        OAIPMHerrorType err = new OAIPMHerrorType();
                        err.code = OAIPMHerrorcodeType.noRecordsMatch;
                        return "No Records to Harvest";
                    }
                    catch (Exception ex)
                    {
                        return "Error harvesting records: " + ex.Message;
                    }
 
                    try
                    {
                        if (oai.Items[0].GetType() == typeof(ListRecordsType))
                        {
                            if (((ListRecordsType)oai.Items[0]).resumptionToken == null)
                                rt = null;
                            else
                                rt = ((ListRecordsType)oai.Items[0]).resumptionToken.Value;
                        }
                    }
                    catch (Exception se)
                    {
                        sb.Append(se + " : Problem Resumption Token" + se.Message);
                    }

                    nextToken = rt != null && rt.Length > 0;


                    recordType[] recs = null;

                    try
                    {
                        if (oai.Items[0].GetType() == typeof(ListRecordsType))
                        {
                            recs = ((ListRecordsType)oai.Items[0]).record;
                        }
                        else
                        {
                            recs = new recordType[1];
                            recs[0] = ((GetRecordType)oai.Items[0]).record;
                        }
                    }
                    catch
                    {
                        sb.Append("\nGot no records :" + oai.Items[0] + "\n");
                        return sb.ToString();
                    }
                    if (recs == null)
                    {
                        return "No Records to Harvest";
                    }

                    sb.Append(" \nGot " + recs.Length + " recs\n");
                    Console.Out.WriteLine(" \nGot " + recs.Length + " recs\n");

                    ArrayList res = new ArrayList();
                    DateTime now = DateTime.Now;
                    string theXML = null;
                    for (int r = 0; r < recs.Length; r++)
                    {
                        string id = recs[r].header.identifier.Trim();

                        //Console.Out.WriteLine(r+"  resources "+recs[r].metadata);
                        if (recs[r].header.status == statusType.deleted)
                        {
                            string retdel = reg.DeleteEntry(id, dbAdmin);
                            sb.Append("\nDelete id: " + id + " " + retdel + "\n");
                        }
                        else
                        {
                            //These are known bad records to be managed by hand.
                            //Note we can *delete* them easily enough, if they were already imported
                            //somehow. If they're in this list, they've repeatedly failed to import properly
                            //and been caught in the logs by a real person.
                            if (knownbad.Contains(id))
                            {
                                ++recSkipped;
                                continue;
                            }

                            try
                            {
                                //                          Do not store the XML Header for instances of selecting
                                //                          and concatenating xml resources.
                                //							theXML = XMLHEADER+recs[r].metadata.OuterXml;
                                theXML = recs[r].metadata.OuterXml;
                            }
                            catch
                            {
                                sb.Append(" \nAt rec=" + r + " " + recs[r].metadata);
                            }
                            // Load single resource in the Registry
                            try
                            {
                                string harvestURL = baseurl;
                                if (harvestURL.Contains("?"))
                                    harvestURL = harvestURL.Substring(0, harvestURL.IndexOf('?'));

                                //misc cleanup of records from other publishers.
                                theXML = theXML.Replace(" xmlns=\"http://www.openarchives.org/OAI/2.0/\"", "");
                                if (!theXML.Contains("http://www.w3.org/2001/XMLSchema-instance"))
                                {
                                    int index = theXML.IndexOf("xmlns=");
                                    theXML = theXML.Insert(index, "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
                                }

                                int status = vorXML.LoadVORXML(theXML, 0, harvestURL, sb);
                                try
                                {
                                    if (status != 0)
                                    {
                                        ++recFailures;
                                        Console.Out.WriteLine("Failed to harvest resource. dumping fragment to bad" + r + "\n");
                                        StreamWriter sr = new StreamWriter(log_location + "\\bad" + r + ".xml");
                                        sr.Write(theXML);
                                        sr.Flush();
                                        sr.Close();

                                        errLog.Log("Failed to harvest resource. Dumped to bad" + r + ".xml\n" +
                                                   "Err is " + sb.ToString());
                                    }
                                }
                                catch (System.IO.IOException)
                                {
                                    status = -1;
                                    //if we can't write to the logfile there's no 
                                    //point in trying to log that....
                                }
                            }

                            catch (Exception se)
                            {
                                try
                                {
                                    Console.Out.WriteLine(se + ":" + se.StackTrace);
                                    sb.Append(se + ": " + se.StackTrace + " : dumping fragment to bad" + r + "\n");
                                    StreamWriter sr = new StreamWriter(log_location + "\\bad" + r + ".xml");
                                    sr.Write(theXML);
                                    sr.Flush();
                                    sr.Close();

                                    try
                                    {
                                        errLog.Log("Exception harvesting resource. Dumped to bad" + r + ".xml\n" +
                                        "Message is " + se.Message);
                                    }
                                    catch (System.IO.IOException) { }

                                }
                                catch (System.IO.IOException)
                                {
                                    //if we can't write to the logfile there's no 
                                    //point in trying to log that....
                                }
                            }
                        }
                    }

                    sb.Append("Loaded " + (recs.Length - recFailures - recSkipped) + " RESOURCES. ");
                    if (recSkipped > 0)
                        sb.Append("Skipped " + recSkipped + " RESOURCES from known bad list. ");
                    if (recFailures > 0)
                        sb.Append("Failed to load " + recFailures + " RESOURCES. ");
                    //sb.Append(DateTime.Now+" "+result);
                }
                catch (Exception ex)
                {
                    sb.Append("Uncaught Exception in harvesting " + baseurl + " : " + ex);
                }
			}

			return sb.ToString();
		}

	}
}

/* Log of changes
 * $Log: Harvester.cs,v $
 * Revision 1.2  2006/04/19 15:39:58  grgreene
 * updating OAI delete
 *
 * Revision 1.1.1.1  2005/05/05 15:16:58  grgreene
 * import
 *
 * Revision 1.31  2005/03/22 20:11:00  womullan
 * update to parser for descrip + OAI fixes
 *
 * Revision 1.30  2004/12/07 15:45:21  womullan
 *  downfile
 *
 * Revision 1.29  2004/11/30 20:26:42  womullan
 * replicate fix
 *
 * Revision 1.28  2004/11/30 17:23:03  womullan
 * replicate fix
 *
 * Revision 1.27  2004/11/30 16:48:43  womullan
 * keywords/replicate fix
 *
 * Revision 1.26  2004/11/29 21:51:02  womullan
 *  harvest working for Vizier
 *
 * Revision 1.25  2004/11/15 16:23:21  womullan
 * fixed stringbuffer
 *
 * Revision 1.24  2004/11/13 01:20:34  womullan
 * added verb to resumptiontoken
 *
 * Revision 1.23  2004/11/11 19:40:20  womullan
 * minor updates
 *
 * Revision 1.22  2004/11/08 20:20:35  womullan
 * updated relationship insert
 *
 * Revision 1.21  2004/11/02 20:12:53  womullan
 * date fields fixed
 *
 * Revision 1.20  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.19  2004/09/30 07:17:13  womullan
 *  better reporting in replicate - voform added
 *
 * Revision 1.18  2004/05/20 17:31:53  womullan
 * fixed the oai error check
 *
 * Revision 1.17  2004/05/06 20:04:32  womullan
 *  ensure status set for failed vizier harvest - use in front page
 *
 * Revision 1.16  2004/05/06 16:15:41  womullan
 *  some mods to main page - replication fixed
 *
 * Revision 1.15  2004/04/23 01:37:32  womullan
 *  harvest dates on front page
 *
 * Revision 1.14  2004/04/22 21:42:00  womullan
 *  from dates fixed  harvest works
 *
 * Revision 1.13  2004/04/22 20:59:02  womullan
 *  small cosmetic changes - some fromn Alex
 *
 * Revision 1.12  2004/04/22 16:06:31  womullan
 * delete harvest entry added
 *
 * Revision 1.11  2004/04/01 18:29:28  womullan
 *  harvest ncsa
 *
 * Revision 1.10  2004/02/13 19:07:30  womullan
 * update voresource and ws layout
 *
 * Revision 1.9  2004/02/05 18:48:47  womullan
 * added sqlquery and harvestedfromDate
 *
 * Revision 1.8  2003/12/18 19:45:17  womullan
 * updated harvester
 *
 * Revision 1.7  2003/12/16 21:17:50  womullan
 * now returning voresource
 *
 * Revision 1.6  2003/12/15 21:00:39  womullan
 * relations and Harvested from added
 *
 * Revision 1.5  2003/12/08 22:35:37  womullan
 * Parser fixed
 *
 * Revision 1.4  2003/12/08 17:32:39  womullan
 * parser almost working
 *
 * Revision 1.3  2003/12/05 13:41:41  womullan
 *  cone siap skynode insert working
 *
 * Revision 1.2  2003/12/03 23:00:15  womullan
 *  many mods to get SQL working
 *
 * Revision 1.1  2003/05/09 19:18:40  womullan
 * moved harvester into registry service
 *
 * Revision 1.1  2003/05/08 21:19:51  womullan
 *  harvester now working - new update service
 *
 * 
 * */
