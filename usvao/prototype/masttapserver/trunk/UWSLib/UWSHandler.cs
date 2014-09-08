using System;
using System.Collections;
using System.Data;
using System.IO;
using System.Text;
using System.Net;
using net.ivoa.VOTable;

namespace UWSLib
{
    public class UWSHandler
    {
        private static string baseURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseURL"];

        //tdower todo - should we only have a single instance of this class, and some threading protection?
        private Object HandleRequest(System.Collections.Specialized.NameValueCollection input, string restPath, ref string redirect, string requestType)
        {
            Object results = new VOTABLE();
            string errorString = string.Empty;

            try
            {
                RequestDefinition def = RequestDefinition.parseRequest(input, restPath);
                if (def.ArgName == Args.Names.jobs)
                {
                    results = UWSWorker.GetShortJobList();
                }
                else if (def.JobNumber == -1)
                {
                    if (requestType == "POST")
                    {
                        if (UWSWorker.HasJob(def.JobNumber))
                            results = UWSWorker.GetJobSummary(def.JobNumber);
                        else
                            results = UWSWorker.AddJob(def);
                        if (results == null)
                            results = VOTableUtil.CreateErrorVOTable("Job " + def.JobNumber + " already exists or could not be created. ");
                        else if (results.GetType() == typeof(JobSummary))
                        {
                            redirect = baseURL + "/async/" + ((JobSummary)results).jobId;
                        }
                    }
                    else
                    {
                        results = UWSWorker.GetShortJobList();
                    }
                }
                else
                {
                    if (!UWSWorker.HasJob(def.JobNumber))
                    {
                        results = VOTableUtil.CreateErrorVOTable("Unable to complete your request. Job number " + def.JobNumber + " does not exist.");
                    }
                    else
                    {
                        switch (def.ArgName) //tdower note we know request type post/get here now.
                        {
                            case Args.Names.phase:
                                if (requestType == "POST")
                                {
                                    results = VOTableUtil.CreateErrorVOTable("Phase change not yet supported.");
                                    //once implemented, set redirect, will 303 to the job
                                }
                                else
                                    results = UWSWorker.GetPhase(def.JobNumber);
                                break;

                            case Args.Names.quote:
                                results = UWSWorker.GetQuote(def.JobNumber);
                                break;

                            case Args.Names.executionduration:
                                //this is a 'may' in UWS doc
                                results = VOTableUtil.CreateErrorVOTable("Execution duration change not yet supported.");
                                break;

                            case Args.Names.destruction:
                                if (requestType == "POST")
                                {   //this is a 'may' in UWS doc
                                    results = VOTableUtil.CreateErrorVOTable("Destruction time change not yet supported.");
                                    //when implemented, set redirect, will 303 to the job
                                }
                                else
                                    results = UWSWorker.GetDestructionTime(def.JobNumber);
                                break;

                            case Args.Names.error:
                                results = UWSWorker.GetErrorDoc(def.JobNumber);
                                if (results == null)
                                    results = VOTableUtil.CreateErrorVOTable("Job does not exist or is not in an error state.");
                                break;

                            case Args.Names.parameters:
                                results = UWSWorker.GetParameters(def.JobNumber);
                                if (results == null)
                                    results = VOTableUtil.CreateErrorVOTable("Job does not exist or has invalid parameters.");
                                break;

                            //Note that for non-TAP implementations where there is a results list
                            //with more than one identified value, this case will require modification
                            case Args.Names.results:
                                if (def.SubArg == string.Empty)
                                {
                                    results = UWSWorker.GetResultsList(def.JobNumber);
                                    if (results == null)
                                        results = VOTableUtil.CreateErrorVOTable("Job does not exist or does not yet have results.");
                                }
                                else if (def.SubArg == "result")
                                {
                                    results = UWSWorker.GetJobResults(def.JobNumber);
                                    if (results == null)
                                        results = VOTableUtil.CreateErrorVOTable("Job does not exist or does not yet have results.");
                                }
                                else
                                    results = VOTableUtil.CreateErrorVOTable("Invalid argument for asynchronous query.");
                                break;

                            case Args.Names.INVALID_ARG:
                                results = VOTableUtil.CreateErrorVOTable("Invalid argument for asynchronous query.");
                                break;

                            case Args.Names.NO_ARG: //job info or control.
                                if (requestType == "POST")
                                {
                                    if (def.InputParams["ACTION"] != null && def.InputParams["ACTION"].ToUpper() == "DELETE")
                                    {
                                        UWSWorker.DeleteJob(def.JobNumber);
                                        redirect = baseURL + "/async";       //job list       
                                    }
                                    else if (def.InputParams["PHASE"] != null && def.InputParams["PHASE"].ToUpper() == "RUN")
                                    {
                                        results = UWSWorker.StartJob(def.JobNumber);
                                    }
                                    else
                                    {
                                        results = UWSWorker.GetJobSummary(def.JobNumber);
                                    }
                                    //tdower todo - abort jobs
                                }
                                else
                                {
                                    results = UWSWorker.GetJobSummary(def.JobNumber);
                                }
                                break;

                        };
                        if (results == null)
                            results = VOTableUtil.CreateErrorVOTable("Job " + def.JobNumber + " does not exist. ");
                    }
                }

                //UWSWorker.MonitorJobs();
                System.Threading.Thread workerThread = new System.Threading.Thread(new System.Threading.ThreadStart(UWSWorker.MonitorJobs));
                workerThread.Start();

                return results;
            }
            catch (Exception e)
            {
                results = VOTableUtil.CreateErrorVOTable(errorString + " " + e.Message);
            }

            return results;
        }

        public Object doAsync(System.Collections.Specialized.NameValueCollection input, string restPath, ref string redirect, string requestType)
        {
            return HandleRequest(input, restPath, ref redirect, requestType);
        }
    }
}