using System;
using System.Collections;
using System.Threading;
using System.Text;
using System.Net;
using System.IO;
using System.Xml;
using System.Xml.Serialization;

using VOTLib;

namespace UWSClient
{
    public class UWSPollingClient
    {
        UWSJob UWSJob;         // Job state and management information, shared between threads.
        string startParameters; //Job management doesn't need these.
        string runURL = string.Empty; // Datascope and STScI TAP currently differ on these. Todo: resolve

        static int iPollingSleep = 1000; // milliseconds between polling web server requests.
        static int iRunSleep = 500; // milliseconds between begin and run.

        Hashtable hashResultsByUrl = new Hashtable();

        public UWSPollingClient(UWSJob job, string startParameters)
        {
            this.UWSJob = job;
            this.startParameters = startParameters;
        }

        public UWSPollingClient(UWSJob job, string startParameters, string runURL)
        {
            this.UWSJob = job;
            this.startParameters = startParameters;
            this.runURL = runURL;
        }

        public void ThreadRun()
        {
            if (UWSJob.Url != string.Empty)
            {
                bool success = SpawnJob(UWSJob);
                if (success)
                {
                    //due to cacheing, we could already have the results:
                    string phase = UWSJob.GetPhase();
                    if (phase != "COMPLETED" && phase != "ERROR")
                    {
                        Thread.Sleep(iRunSleep);
                        success = RunJob(UWSJob);
                        phase = UWSJob.GetPhase();
                    }
                    while (success && phase != "COMPLETED" && phase != "ERROR")
                    {
                        Thread.Sleep(iPollingSleep);
                        success = PollJob(UWSJob);
                        if (success)
                            phase = UWSJob.GetPhase();
                    }
                    if (success && phase == "COMPLETED")
                    {
                        try
                        {
                            //    success = success && FetchAllResultsToTable(UWSJob, url);

                            //get the smallest file once for a test.
                            string[] urls = UWSJob.GetResultsURLs();
                            foreach (string url in urls)
                            {
                                if (url.Contains("hits.xml"))
                                {
                                    Stream stream = FetchSingleResultsStream(url);
                                    if (stream != null)
                                    {
                                        XmlTextReader reader = new XmlTextReader(stream);
                                        System.Data.DataSet ds = new System.Data.DataSet("hits");
                                        VOTDataSetReceiver receiver = new VOTDataSetReceiver(reader, ds);
                                        VOTParser parser = new VOTParser(reader, receiver);
                                        parser.Parse();
 
                                        if (hashResultsByUrl.ContainsKey(url))
                                            hashResultsByUrl[url] = ds;
                                        else
                                            hashResultsByUrl.Add(url, ds);
                                    }
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine(ex.Message);
                        }
                    }
               }
                else
                    UWSJob.SetErrorState("Error starting job. " + UWSJob.GetErrorStatus());
            }            
        }

        private bool SpawnJob(UWSJob job)
        {
            try
            {
                HttpWebRequest req = SendPostForm(job.Url, this.startParameters);
                return SaveResponseToUWSJob( (HttpWebResponse)req.GetResponse(), ref job);
            }
            catch (Exception)
            {
                return false;
            }
        }

        private bool RunJob(UWSJob job)
        {
            try
            {
                if (runURL == string.Empty) //standard(???) UWS processing
                {
                    string url = job.Url + '/' + job.GetJobNumber();
                    HttpWebRequest req = SendPostForm(url, "phase=run");
                    return SaveResponseToUWSJob((HttpWebResponse)req.GetResponse(), ref job);
                }
                else //currently, datascope
                {
                    string url = job.Url + "/jobs/" + job.GetJobNumber() + runURL;
                    HttpWebRequest req =(HttpWebRequest)WebRequest.Create(url);
                    return SaveResponseToUWSJob((HttpWebResponse)req.GetResponse(), ref job);
                }
             }
            catch (Exception)
            {
                return false;
            }
        }

        private bool PollJob(UWSJob job) 
        {
            try
            {   //polling is a GET, rather than a POST
                string url = job.Url + "/jobs/" + job.GetJobNumber();
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
                return SaveResponseToUWSJob((HttpWebResponse)req.GetResponse(), ref job);
            }
            catch (Exception)
            {
                return false;
            }
        }

        //only returns true if we got all of the results files: check the individual hash elements.
        private bool FetchAllResultsToTable(UWSJob job) 
        {
            bool success = true;
            bool filledOneResults = false;
            try
            {
                string[] urls = job.GetResultsURLs();

                foreach (string url in urls)
                {
                    char[] results = { };
                    HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
                    filledOneResults = SaveResults((HttpWebResponse)req.GetResponse(), ref results);
                    if (!filledOneResults) results = null;

                    if (hashResultsByUrl.ContainsKey(url))
                        hashResultsByUrl[url] = results;
                    else
                        hashResultsByUrl.Add(url, results);

                    success = success && filledOneResults;
                }
                return true;
            }
            catch (Exception)
            {
                return false;
            }
        }

        private Stream FetchSingleResultsStream(string fileURL) 
        {
            Stream stream = null;
            try
            {
                 HttpWebRequest req = (HttpWebRequest)WebRequest.Create(fileURL);
                 stream = ((HttpWebResponse)req.GetResponse()).GetResponseStream();
                 return stream;
            }
            catch (Exception)
            {
                return null;
            }
        }

        //This is the final results-wrangling function. Edit it to change behaviour of what we *do* with the final result file.
        private bool SaveResults(HttpWebResponse resp, ref char[] results)
        {
            try
            {
                StreamReader stream = new StreamReader(resp.GetResponseStream());

                int iCurrentRead = 0;
                int iTotal = 0;
                char[] buffer = new char[resp.ContentLength];
                do
                {
                    iCurrentRead = stream.Read(buffer, iTotal, buffer.Length);
                    //Console.Write(buffer, iTotal, iCurrentRead);

                    iTotal += iCurrentRead;
                }
                while (iCurrentRead > 0);
                results = buffer;

                Console.WriteLine();
                return true;
            }
            catch (Exception)
            {
                return false;
            }
        }

        private HttpWebRequest SendPostForm(string url, string parameters)
        {
            ASCIIEncoding encoding = new ASCIIEncoding();
            byte[] data = encoding.GetBytes(parameters);

            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
            req.Method = "POST";
            req.ContentType = "application/x-www-form-urlencoded";
            req.ContentLength = data.Length;
            Stream newStream = req.GetRequestStream();
            // Send the data.
            newStream.Write(data, 0, data.Length);
            newStream.Close();

            return req;
        }

        private bool SaveResponseToUWSJob(HttpWebResponse resp, ref UWSJob job)
        {
            StreamReader stream = new StreamReader(resp.GetResponseStream());
            string output = stream.ReadToEnd();
            if (output != null)
                job.WriteStateDataToUWSJob(output);
            else
            {
                job.SetErrorState("Empty web response from " + resp.ResponseUri);
                return false;
            }
            return true;
        }
    }
}
