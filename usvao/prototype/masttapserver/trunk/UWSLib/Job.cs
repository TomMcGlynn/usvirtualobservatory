using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Xml;
using System.Xml.Serialization;

using net.ivoa.VOTable;


namespace UWSLib
{

    class Job
    {
        internal static int UWSMaxLifeHours = Convert.ToInt32((string)System.Configuration.ConfigurationSettings.AppSettings["UWSMaxLifeHours"]);
        internal static int UWSDefaultQuoteMinutes = Convert.ToInt32((string)System.Configuration.ConfigurationSettings.AppSettings["UWSDefaultQuoteMinutes"]);
        public static string jobsDir = (string)System.Configuration.ConfigurationSettings.AppSettings["jobsDir"];
        private static string baseURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseURL"];

        static object currentJobLocker = new object(); //thread safety for unique job Id

        private JobSummary js;
        public JobSummary JS { get { return js; } }

        public Job( RequestDefinition def)
        {
            DateTime now = DateTime.Now;

            js = new JobSummary();
            js.runId = string.Empty;

            if (UWSDefaultQuoteMinutes > 0)
            {
                js.quote = now.AddMinutes(UWSDefaultQuoteMinutes);
                js.quoteSpecified = true;
                js.executionDuration = UWSDefaultQuoteMinutes;
            }

            js.destruction = now.AddHours(UWSMaxLifeHours);
            lock (currentJobLocker)
            {
                js.jobId = now.Ticks.ToString();
            }

            if (def.InputParams.Count > 0)
            {
                js.parameters = new Parameter[def.InputParams.Count];
                for( int i = 0; i < def.InputParams.Count; ++i )
                {
                    js.parameters[i] = new Parameter();
                    js.parameters[i].id = def.InputParams.Keys[i];
                    js.parameters[i].Text = def.InputParams.GetValues(i);
                }
            }
        }

        ~Job()
        {
            DeleteResults();
        }

        System.Collections.Specialized.NameValueCollection GetParamsAsNVC()
        {
            System.Collections.Specialized.NameValueCollection input = new System.Collections.Specialized.NameValueCollection();
            foreach (Parameter param in js.parameters)
            {
                foreach (string text in param.Text)
                {
                    input.Add(param.id, text);
                }
            }
            return input;
        }


        private Parameter GetParam(string ID)
        {
            foreach (Parameter param in js.parameters)
            {
                if(param.id.ToUpper() == ID )
                    return param;
            }
            return null;
        }

        public void Run() //based off of dosync.
        {
            VOTABLE results = null;
            string errorString = string.Empty;
            bool successful = false;
            js.startTime = DateTime.Now;

            try
            {
                Parameter req = GetParam("REQUEST");
                if( req == null || req.Text.Length == 0 )
                {
                    errorString = "Missing Request Parameter";
                }
                else
                {
                    if (req.Text[0].ToUpper() == "DOQUERY")
                    {
                        Parameter lang = GetParam("LANG");
                        if (lang == null || lang.Text.Length == 0)
                        {
                            errorString = "Missing Language Parameter";
                        }
                        else
                        {
                            if (lang.Text[0].ToUpper() == "ADQL")
                            {
                                tapLib.Exec.RequestHandler handler = new tapLib.Exec.RequestHandler();
                                results = handler.doADQL(GetParamsAsNVC());
                                successful = ! VOTableUtil.IsErrorVOTable(results);
                            }
                            else if (lang.Text[0].ToUpper() == "PQL")
                            {
                                tapLib.Exec.RequestHandler handler = new tapLib.Exec.RequestHandler();
                                results = handler.doPQL(GetParamsAsNVC());
                                successful = ! VOTableUtil.IsErrorVOTable(results);
                            }
                            else
                            {
                                errorString = "Unsupported Value for Language Parameter";
                            }
                        }
                    }
                }

                if (successful == false && results == null)
                {
                    SetError(errorString, ErrorType.transient, false);
                }
                else if (successful == false) //but we have a results table
                {
                    SetError("Error executing underlying query", ErrorType.fatal, true);
                }
            }
            catch (Exception e)
            {
                results = VOTableUtil.CreateErrorVOTable(errorString + " " + e.Message);
                SetError(e.Message, ErrorType.fatal, true);

            }
            if (results != null)
            {
                js.results = new ResultReference[1];
                js.results[0] = new ResultReference();
                js.results[0].href = baseURL + "/async/" + js.jobId + "/results/result";

                WriteResults(results);
            }
            js.endTime = DateTime.Now;

            //not completed until results are available
            if( successful )
                js.phase = ExecutionPhase.COMPLETED;

        }

        private void WriteResults(VOTABLE results)
        {
            string filename = jobsDir + '/' + js.jobId;
            try
            {
                FileStream file = null;
                file = System.IO.File.Open(filename, FileMode.CreateNew);

                XmlSerializer ser = new XmlSerializer(typeof(VOTABLE));
                StringBuilder sb = new StringBuilder();
                StringWriter sw = new StringWriter(sb);

                ser.Serialize(sw, results);
                sw.Close();

                StreamWriter fw = new StreamWriter(file);
                fw.Write(sw.ToString());
                fw.Close();
                file.Close();
            }
            catch (Exception e)
            {
                SetError(e.Message, ErrorType.transient, false);
            }
        }
        internal Object ReadResults()
        {
            string filename = jobsDir + '/' + js.jobId;
            Object results = null;
            try
            {
                FileStream file = System.IO.File.Open(filename, FileMode.Open, FileAccess.Read);
                TextReader tr = new StreamReader(file); 
                XmlSerializer ser = new XmlSerializer(typeof(VOTABLE));
                results = (VOTABLE)ser.Deserialize(tr);

                tr.Close();
            }
            catch (Exception e)
            {
                SetError(e.Message, ErrorType.transient, false);
                results = null;
            }

            return results;
        }

        internal void DeleteResults()
        {
            string filename = jobsDir + '/' + js.jobId;
            try
            {
                if( System.IO.File.Exists(filename))
                    System.IO.File.Delete(filename);
            }
            catch (Exception) { }
        }

        private void SetError(string message, ErrorType type, bool detail)
        {
            js.phase = ExecutionPhase.ERROR;
            js.errorSummary = new ErrorSummary();
            js.errorSummary.message = message;
            js.errorSummary.type = type;
            js.errorSummary.hasDetail = detail;
        }

        internal Object GetErrorDoc()
        {
            VOTABLE results = null;
            if (js.phase == ExecutionPhase.ERROR)
            {
                if (js.errorSummary.hasDetail)
                    return ReadResults();
                else
                    return VOTableUtil.CreateErrorVOTable(js.errorSummary.message);
            }

            return results;
        }

    }
}
