using System;
using System.Collections.Generic;
using System.Threading;
using System.Text;
using System.Xml;

namespace UWS
{
    public class UWSJob
    {
        private long jobNumber = 0;

        private readonly string url;
        public string Url { get { return url; } }

        private string phase = string.Empty;
        private string errorStatus = string.Empty;
        private string[] resultsURLs = { };

        public UWSJob(string url)
        {
            this.url = url;
        }

        bool processingflag = false;  

        public long GetJobNumber()
        {
            long job = 0;
            lock (this)   // Enter synchronization block
            {
                job = jobNumber;
            }
            return job;
        }

        public string GetPhase()
        {
            string phase = string.Empty;
            lock (this)   // Enter synchronization block
            {
                phase = this.phase;
            }
            return phase;
        }

        public string GetErrorStatus()
        {
            string error = string.Empty;
            lock (this)   // Enter synchronization block
            {
                error = this.errorStatus;
            }
            return error;
        }

        public string[] GetResultsURLs()
        {
            string[] results = { };
            lock (this)   // Enter synchronization block
            {
                results = new string[resultsURLs.Length];
                resultsURLs.CopyTo(results, 0);
            }
            return results;
        }

        public long HandleWebResponsesFromUWSJob()
        {
            lock (this)   // Enter synchronization block
            {
                if (!processingflag)
                {            // Wait until UWSJob.WriteToUWSJob is done producing
                    try
                    {
                        // Waits for the Monitor.Pulse in WriteToUWSJob
                        Monitor.Wait(this);
                    }
                    catch (SynchronizationLockException e)
                    {
                        Console.WriteLine(e);
                    }
                    catch (ThreadInterruptedException e)
                    {
                        Console.WriteLine(e);
                    }
                }

                /*****begin processing block*****/
                Console.WriteLine("Web Response read for job: {0}, phase {1} {2}", jobNumber, phase, errorStatus);

                /*****end processing block*****/

                processingflag = false;    
                // is done.
                Monitor.Pulse(this);   // Pulse tells UWSJob.WriteToUWSJob that
                // UWSJob.ReadFromUWSJob is done.
            }   // Exit synchronization block
            return jobNumber;
        }

        //there may be threading with pulse if this gets used. TEST.
        public void SetErrorState(string error)
        {
            lock (this)  // Enter synchronization block
            {
                if (processingflag)
                {      // Wait until UWSJob.ReadFromUWSJob is done consuming.
                    try
                    {
                        Monitor.Wait(this);   // Wait for the Monitor.Pulse in
                        // ReadFromUWSJob
                    }
                    catch (SynchronizationLockException e)
                    {
                        Console.WriteLine(e);
                    }
                    catch (ThreadInterruptedException e)
                    {
                        Console.WriteLine(e);
                    }
                }
                try
                {
                    phase = "ERROR";
                    errorStatus = error;
                }
                catch (Exception ex)
                {
                    errorStatus = ex.Message;
                    Console.WriteLine("Error setting job error status " + error + " : " + ex.Message);
                }
                Console.WriteLine("Job error set: " + error);

                processingflag = true;  
                // is done
                Monitor.Pulse(this);  // Pulse tells UWSJob.ReadFromUWSJob that 
                // UWSJob.WriteToUWSJob is done.
            }   // Exit synchronization block
        }

        public void WriteStateDataToUWSJob(string UWSResponse)
        {
            lock (this)  // Enter synchronization block
            {
                if (processingflag)
                {      // Wait until UWSJob.ReadFromUWSJob is done consuming.
                    try
                    {
                        Monitor.Wait(this);   // Wait for the Monitor.Pulse in
                        // ReadFromUWSJob
                    }
                    catch (SynchronizationLockException e)
                    {
                        Console.WriteLine(e);
                    }
                    catch (ThreadInterruptedException e)
                    {
                        Console.WriteLine(e);
                    }
                }
                try
                {
                    /*****begin processing block*****/
                    #region document -> job processing

                    //strip namespace information: .NET xml document processing doesn't like the datascope namespace format
                    //and the STScI TAP service doesn't include it.
                    UWSResponse = UWSResponse.Replace("uws:", "");

                    XmlDocument doc = new XmlDocument();
                    doc.LoadXml(UWSResponse);
                    XmlNode node = doc.GetElementsByTagName("jobId")[0];
                    jobNumber = Convert.ToInt64(node.InnerXml);

                    node = doc.GetElementsByTagName("phase")[0];
                    phase = node.InnerXml;

                    if (phase == "ERROR")
                    {
                        node = doc.GetElementsByTagName("errorSummary")[0];
                        if (node != null && node.InnerText != string.Empty)
                            errorStatus = node.InnerText;
                    }
                    // if (phase == "EXECUTING") //for datascope, at least, we may be able to get partial results.
                    // {
                    // }
                    else if (phase == "COMPLETED" )
                    {
                        XmlNodeList list = doc.GetElementsByTagName("result");
                        if (list.Count == 0) list = doc.GetElementsByTagName("results");

                        resultsURLs = new string[list.Count];
                        for (int i = 0; i < list.Count; ++i)
                        {
                            if( list[i].Attributes["href"] != null )
                                resultsURLs[i] = list[i].Attributes.GetNamedItem("href").Value;
                            else
                                resultsURLs[i] = list[i].Attributes.GetNamedItem("xlink:href").Value;
                        }
                    }

                    #endregion
                    /*****end processing block*****/
                }
                catch (Exception ex)
                {
                    errorStatus = ex.Message;
                    Console.WriteLine("Error reading job status: " + ex.Message);
                }
                Console.WriteLine("Web response saved for job number: {0}, phase {1} {2}", jobNumber, phase, errorStatus);

                processingflag = true;    
                Monitor.Pulse(this);  // Pulse tells UWSJob.ReadFromUWSJob that 
                // UWSJob.WriteToUWSJob is done.
            }   // Exit synchronization block
        }
    }
}
