
using System;
using System.Collections;
using System.Threading;
using System.Text;
using System.Net;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Data;

using Mashup;
using VOTLib;
using Utilities;

namespace UWS
{
    public class UWSPollingClient
    {
        UWSJob UWSJob;         // Job state and management information, shared between threads.
        string startParameters; //Job management doesn't need these.
        string runURL = string.Empty; // Datascope and STScI TAP currently differ on these. Todo: resolve

        static int iPollingSleep = 1000; // milliseconds between polling web server requests.
        static int iRunSleep = 500; // milliseconds between begin and run.

        Hashtable hashResultsByUrl = new Hashtable();

        ServiceRequest request;
		ServiceResponse response;

        public UWSPollingClient(UWSJob job, string startParameters, string runurl, ServiceRequest request, ServiceResponse response) : 
			this(job, startParameters, runurl)
        {
			this.request = request;
			this.response = response;
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
				//
				// (1) Intiate the Remote Job
				//
                bool success = SpawnJob(UWSJob);
                if (success)
                {					
					//
					// (2) Run the Remote Job, if not cached results
					//
                    string phase = UWSJob.GetPhase();
                    if (phase != "COMPLETED" && phase != "ERROR")
                    { 
						Thread.Sleep(iRunSleep);
                        success = RunJob(UWSJob);
                        phase = UWSJob.GetPhase();	
                    }
					
					//
					// (3) Poll the Remote Job at least once
					//
                    do 
                    {
                        Thread.Sleep(iPollingSleep);
                        success = PollJob(UWSJob);
                        phase = (success ? UWSJob.GetPhase() : phase);
                    } while (success && phase != "COMPLETED" && phase != "ERROR");
					
					//
					// (4) Retreive the Job Results
					//
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
									//
									// Retreive the 'hits.xml' URL and Transform the result VoTable into a DataSet
									//		
									Stream s =  Utilities.Web.getWebReponseStream(url);
									XmlTextReader reader = new XmlTextReader(s); 
									DataSet ds = Utilities.Transform.VoTableToDataSet(reader);
								
									//
									// Write the DataSet back to the client
									//
						            response.load(ds);
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
                HttpWebResponse resp = Web.postWebResponse(job.Url, this.startParameters);
                return SaveResponseToUWSJob(resp, ref job);
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
                    HttpWebResponse resp = Web.postWebResponse(url, "phase=run");
                    return SaveResponseToUWSJob(resp, ref job);
                }
                else //currently, datascope
                {
                    string url = job.Url + "/jobs/" + job.GetJobNumber() + runURL;
                    HttpWebResponse resp = Web.getWebResponse(url);
                    return SaveResponseToUWSJob(resp, ref job);
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
                HttpWebResponse resp = Web.getWebResponse(url);
                return SaveResponseToUWSJob(resp, ref job);
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
					HttpWebResponse resp = Web.getWebResponse(url);
                    filledOneResults = SaveResults(resp, ref results);
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

        private Stream FetchSingleResultsStream(string url) 
        {
            Stream stream = null;
            try
            {
				stream = Web.getWebReponseStream(url);
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
                    iCurrentRead = stream.Read(buffer, 0, buffer.Length);
					if (iCurrentRead > 0)
					{
						// Write response buffer to Client
						if (response != null && request != null)
						{
							//response.load(buffer, iCurrentRead, false);
						}
						else
						{
                    		Console.Write(buffer);
							Console.WriteLine();
						}
					}

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


/*
using System;
using System.Collections.Generic;
using System.Threading;
using System.Text;
using System.Net;
using System.Diagnostics;
using Mashup;

namespace UWS
{
    public class UWSPollingClient
    {
        UWSJob UWSJob;         // Job state and management information, shared between threads.
        string startParameters; //Job management doesn't need these.

        static int iPollingSleep = 1000; // milliseconds between polling web server requests.
        static int iRunSleep = 500; // milliseconds between begin and run.
        static int iResultsBufferSize = 1000; //test
		
		Request request;
		Response response;

        public UWSPollingClient(UWSJob job, string startParameters, Request request, Response response)
        {
            this.UWSJob = job;
            this.startParameters = startParameters;
			this.request = request;
			this.response = response;
        }
		
        public void ThreadRun()
        {
            if (UWSJob.Url != string.Empty)
            {
                bool success = SpawnJob(UWSJob);
                if (success)
                {
                    Thread.Sleep(iRunSleep);
                    success = RunJob(UWSJob);
                    string phase = UWSJob.GetPhase();
                    while (success && phase != "COMPLETED" && phase != "ERROR")
                    {
                        Thread.Sleep(iPollingSleep);
                        success = PollJob(UWSJob);
                        if (success)
						{
                            phase = UWSJob.GetPhase();
						}
                    }
                    if (success && phase == "COMPLETED")
                    {
                        success = FetchResults(UWSJob);
                    }
                }
                else
				{
                    UWSJob.SetErrorState("Error starting job. " + UWSJob.GetErrorStatus());
				}
            }            
        }

        private bool SpawnJob(UWSJob job)
        {
            try
            {
                HttpWebRequest req = SendPostForm(job.Url, this.startParameters);
				HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
                return SaveResponseToUWSJob(resp, ref job);
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
                string url = job.Url + '/' + job.GetJobNumber();
                HttpWebRequest req = SendPostForm(url, "phase=run");
				HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
                return SaveResponseToUWSJob(resp, ref job);
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
                string url = job.Url + '/' + job.GetJobNumber();
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
				HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
                return SaveResponseToUWSJob(resp, ref job);
            }
            catch (Exception)
            {
                return false;
            }
        }

        private bool FetchResults(UWSJob job)
        {
            try
            {
                string url = job.GetResultsURL();
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
				HttpWebResponse resp = (HttpWebResponse)req.GetResponse();
                return SaveResults(resp);
            }
            catch (Exception)
            {
                return false;
            }
        }

        private bool SaveResults(HttpWebResponse resp)
        {
			Console.WriteLine("UWSPollingClient: SaveResults()");
			
            try
            {
                System.IO.StreamReader stream = new System.IO.StreamReader(resp.GetResponseStream());

                int iCurrentRead = 0;
                int iTotal = 0;
                char[] buffer = new char[iResultsBufferSize];
                do
                {
                    iCurrentRead = stream.Read(buffer, 0, buffer.Length);
					if (iCurrentRead > 0)
					{
						// Write response buffer to Client
						if (response != null && request != null)
						{
							response.write(buffer, iCurrentRead, request, false);
						}
						else
						{
                    		Console.Write(buffer);
							Console.WriteLine();
						}
					}

                    iTotal += iCurrentRead;
                }
                while (iCurrentRead > 0);
				
				// Close Connection with Client
				if (response != null && request != null)
				{
					response.close(request);
				}
				
                Console.WriteLine("");
				
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
            System.IO.Stream newStream = req.GetRequestStream();
            // Send the data.
            newStream.Write(data, 0, data.Length);
            newStream.Close();

            return req;
        }

        private bool SaveResponseToUWSJob(HttpWebResponse resp, ref UWSJob job)
        {
            System.IO.StreamReader stream = new System.IO.StreamReader(resp.GetResponseStream());
            string output = stream.ReadToEnd();
            if (output != null)
			{
                job.WriteStateDataToUWSJob(output);
			}
            else
            {
                job.SetErrorState("Empty web response from " + resp.ResponseUri);
                return false;
            }
            return true;
        }
    }
}
*/

