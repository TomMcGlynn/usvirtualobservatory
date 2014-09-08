using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Text;
using System.Net;
using net.ivoa.VOTable;

using tapLib.Args;
using tapLib.Args.ParamQuery;
using tapLib.Config;
using tapLib.Db.ParamQuery;
using tapLib.Exec;
using tapLib.ServiceSupport;
using tapLib.Stsci;

namespace tapLib.Exec
{
    public class RequestHandler {

        public static string baseURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseURL"];
        public static string baseVOSIURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseVOSIURL"];
        public static string adqlURL = (string)System.Configuration.ConfigurationSettings.AppSettings["adqlURL"];
        public static string vDir = (string)System.Configuration.ConfigurationSettings.AppSettings["vDir"];
        internal static string fileName = (string)System.Configuration.ConfigurationSettings.AppSettings["tableConfigFile"];

        private static readonly ISqlGeneratorFactory _generatorFactory;
        //internal static TableServiceConfiguration config = new TableServiceConfiguration();

        static RequestHandler() {
            // Set up the path to the test file
            TapConfiguration.setConfigFilePath(vDir + "\\" + fileName);

            // Setup the generator factory
            _generatorFactory = new SqlGeneratorFactory();
            //_generatorFactory.publish("hlascience", typeof(HlaScienceGenerator).FullName);
            //_generatorFactory.publish("PhotoPrimary", typeof(PhotoPrimaryGenerator).FullName);
            //_generatorFactory.publish("resource", typeof(RegistryGenerator).FullName);

            _generatorFactory.publish("obscore", typeof(ObsCoreGenerator).FullName);
        }

        //This has been split out into its own function for top-level error handling.
        private bool FillPosSize(string pos, string size, ref string error, out TapPosArg pa, out TapSizeArg sa)
        {
            error = string.Empty;
            pa = TapPosArg.Empty;
            if (pos != null && pos.Length > 0)
            {
                pa = new TapPosArg(Uri.UnescapeDataString(pos));
                foreach ( TapPos onepos in pa.posList )
                    error += onepos.problem;
            }

            sa = TapSizeArg.Empty;
            if (size != null && size.Length > 0)
            {
                sa = new TapSizeArg(Convert.ToDouble(Uri.UnescapeDataString(size)));
                error += sa.problem;
            }
            else if (pos != null) //use default
            {
                sa = TapSizeArg.DEFAULT;
            }

            if (error.Length > 0)
                return false;

            return true;
        }

        public VOTABLE doSync(System.Collections.Specialized.NameValueCollection input)
        {
            VOTABLE results = new VOTABLE();
            string errorString = string.Empty;
            bool successful = false;

            try
            {
                if (input["REQUEST"] == null)
                {
                    errorString = "Missing Request Parameter";
                }
                else
                {
                    string req = input["REQUEST"].ToUpper();
                    if (req == "DOQUERY")
                    {
                        if (input["LANG"] == null)
                        {
                            errorString = "Missing Language Parameter";
                        }
                        else
                        {
                            string lang = input["LANG"].ToUpper();
                            if (lang == "ADQL")
                            {
                                results = doADQL(input);
                                successful = true;
                            }
                            else if (lang == "PQL")
                            {
                                results = doPQL(input);
                                successful = true;
                            }
                            else
                            {
                                errorString = "Unsupported Value for Language Parameter";
                            }
                        }
                    }
                }

                if (successful == false)
                    results = VOTableUtil.CreateErrorVOTable(errorString);
            }
            catch (Exception e)
            {
                results = VOTableUtil.CreateErrorVOTable(errorString + " " + e.Message);
            }

            return results;
        }

        public VOTABLE doADQL(System.Collections.Specialized.NameValueCollection input)
        {
            VOTABLE results = new VOTABLE();
            bool successful = false;
            string errorString = string.Empty;

            string adql = GetParsedADQL(input["QUERY"]);
            if (adql.Length > 0)
            {
                if( adql.ToUpper().StartsWith("<ERROR>"))
                {
                    //remove <ERROR> tags.
                    adql = adql.Substring(adql.IndexOf('>') + 1);
                    adql = adql.Substring(0, adql.IndexOf('<')).Trim();

                    errorString = adql;
                }
                else if (adql.ToUpper().StartsWith("<SQL>"))
                {
                    //not error, remove <sql> tags.
                    adql = adql.Substring(adql.IndexOf('>') + 1);
                    adql = adql.Substring(0, adql.IndexOf('<')).Trim();
                }
                else
                {
                    string tblname = GetFrom(adql);

                    QueryArg qa = new QueryArg(tblname, true); //tdower - temp code. parse this out of adql
                    TapQueryArgs tqa = new TapQueryArgs(adql, qa);

                    try
                    {
                        ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, tqa, _generatorFactory);
                        successful = ex.Execute();
                        if (successful)
                        {
                            //_printResults(ex.results);
                            results = VOTableUtil.DataSet2VOTable(ex.results);
                        }
                        else
                        {
                            foreach (string problem in qa.problems)
                                errorString += problem + " ";
                        }
                    }
                    catch (Exception ex)
                    {
                        errorString = ex.Message;
                    }
                }
            }

            if (successful == false)
                results = VOTableUtil.CreateErrorVOTable(errorString);

            return results;
       }

        private string GetFrom(string adql)
        {
            try
            {
                int startcol = adql.IndexOf(' ', adql.ToUpper().IndexOf("FROM"));
                int endcol = adql.IndexOf(' ', startcol + 1);
                if (endcol == -1)
                    endcol = adql.Length;
                string col = adql.Substring(startcol, endcol - startcol).Trim();

                return col;
            }
            catch(Exception)
            {
                return string.Empty;
            }
        }

        //  TDower todo: redo ADQL parsing entirely, to use the new SQL server geospatial functions
        //  being written by Brian McClean
        private string GetParsedADQL(string query)
        {
            /*string url = adqlURL + System.Web.HttpUtility.UrlEncode(query);

            HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(url);
            // Sends the HttpWebRequest and waits for the response. 
            HttpWebResponse resp = null;
            try
            {
                resp = (HttpWebResponse)wr.GetResponse();
                StreamReader reader = new StreamReader(resp.GetResponseStream());

                // Read the whole contents and return as a string  
                string result = reader.ReadToEnd();

                //remove <?xml> tag
                if (result.StartsWith("<?x"))
                    result = result.Substring(result.IndexOf('<', 1));

                return result;
            }
            catch (Exception ex)
            {
                return "<ERROR>" + "Error Parsing ADQL: " + ex.Message + "</ERROR>";
            }     */

            return query;
        }

        public VOTABLE doPQL(System.Collections.Specialized.NameValueCollection input)
        {
            VOTABLE results = new VOTABLE();
            bool successful = false;
            string errorString = string.Empty;

            QueryArg qa = new QueryArg(input);
            if( qa.isValid == false )
                qa = new QueryArg(Uri.UnescapeDataString(input.ToString()));

            if (qa.isValid)
            {
                if (qa.from.ToUpper().Contains("TAP_SCHEMA"))
                {
                    //todo error handling.
                    results = VOTableUtil.DataSet2VOTable(TapConfiguration.Instance.ExecuteConfigQuery(qa));
                    if (qa.problems.Count == 0)
                        successful = true;
                    else
                    {
                        foreach (string problem in qa.problems)
                            errorString += problem + " ";
                    }
                }
                else
                {
                    TapPosArg pa;
                    TapSizeArg sa;
                    successful = FillPosSize((string)input["POS"], (string)input["SIZE"], ref errorString, out pa, out sa);
                    if (successful)
                    {
                        TapQueryArgs tqa = new TapQueryArgs(pa, sa, qa);
                        try
                        {
                            ThreadedQueryStyle ex = new ThreadedQueryStyle(TapConfiguration.Instance, tqa, _generatorFactory);
                            successful = ex.Execute();
                            if (successful)
                            {
                                //_printResults(ex.results);
                                results = VOTableUtil.DataSet2VOTable(ex.results);
                            }
                            else
                            {
                                foreach (string problem in qa.problems)
                                    errorString += problem + " ";
                            }
                        }
                        catch (Exception ex)
                        {
                            errorString = ex.Message;
                        }
                    }
                }
            }
            else
            {
                errorString = "Invalid Query Arguments";
                if (qa != null)
                {
                    errorString += ": ";
                    foreach (string prob in qa.problems)
                        errorString += prob + ' ';
                }
            }
            

            if (successful == false)
                results = VOTableUtil.CreateErrorVOTable(errorString);

            return results;
        }

        private static string RemoveRequestFromInput(string input)
        {
            string output = String.Empty;
            int index = input.IndexOf("REQUEST");
            if (index >= 0)
            {
                int endIndex = input.IndexOf("&", index);
                if (endIndex > index)
                    output = input.Remove(index, endIndex - index + 1);
                else
                    output = input.Remove(index);
            }

            return output;
        }

        private static void _printResults(DataSet results) {
            foreach (DataTable each in results.Tables) {
                Console.WriteLine("Id is: " + each.TableName);
                _printResult(each);
            }
        }

        private static void _printResult(DataTable r) {
            List<String> columnNames = new List<String>();
            int columnCount = r.Columns.Count;
            for (int i = 0; i < columnCount; i++) {
                columnNames.Add(r.Columns[i].ColumnName);
            }
            foreach (String each in columnNames) {
                Console.Write(String.Format("{0,-25}", each));
            }
            Console.WriteLine();
            foreach (DataRow each in r.Rows) {
                foreach (String columnName in columnNames) {
                    Console.Write(String.Format("{0,-25}", each[columnName]));
                }
                Console.Write("\n");
            }
            Console.WriteLine("DONE");
        }

    }
}