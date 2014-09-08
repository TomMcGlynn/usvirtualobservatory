using System;
using System.Collections;

namespace UWSLib
{
    public class UWSWorker
    {
        private static string baseURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseURL"];
        private static volatile Hashtable CurrentJobs = new Hashtable();
        static TimeSpan waitTime = new TimeSpan(0, 0, 1);

        internal static bool HasJob(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if( CurrentJobs.ContainsKey(ID))
                    return true;
                else
                    return false;
            }
        }

        internal static JobSummary GetJobSummary(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                return ((Job)CurrentJobs[ID]).JS;
            }
        }

        internal static Object StartJob(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                    ((Job)CurrentJobs[ID]).JS.phase = ExecutionPhase.QUEUED;
            }

            return GetJobSummary(ID);
        }

        internal static Object GetPhase(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                    return ((Job)CurrentJobs[ID]).JS.phase;
                else
                    return null;
            }
        }

        internal static Object GetQuote(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                    return ((Job)CurrentJobs[ID]).JS.quote;
                else
                    return null;
            }
        }

        internal static Object GetDestructionTime(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                    return ((Job)CurrentJobs[ID]).JS.destruction;
                else
                    return null;
            }
        }

        internal static Object GetErrorDoc(long ID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                    return ((Job)CurrentJobs[ID]).GetErrorDoc();
                else
                    return null;
            }
        }

        internal static Object GetParameters( long ID)
        {
            lock(CurrentJobs.SyncRoot)
            {
                if(CurrentJobs.ContainsKey(ID))
                {
                    parameters parcontainer = new parameters();
                    parcontainer.parameter = ((Job)CurrentJobs[ID]).JS.parameters;
                    return parcontainer;
                }
                else
                    return null;
            }
        }

        internal static Object GetResultsList(long ID)
        {
            results res = new results();
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                {
                    res.result = new ResultReference[1];
                    res.result[0] = new ResultReference();
                    res.result[0].id = "result";
                    res.result[0].href = baseURL + "/async/" + ((Job)CurrentJobs[ID]).JS.jobId + "/results/result";
                }
            }
                return res;
        }

        internal static Object GetJobResults(long ID)
        {
            Job currJob = null;
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(ID))
                    currJob = (Job)CurrentJobs[ID];
            }
            if (currJob != null)
            {
                return currJob.ReadResults();
            }
            else
                return null;
        }

        internal static void SaveJobChange(Job job)
        {
            lock (CurrentJobs.SyncRoot)
            {
                long jobID = Convert.ToInt64(job.JS.jobId);
                if (CurrentJobs.ContainsKey(jobID))
                    CurrentJobs[jobID] = job;
            }
        }

        internal static JobSummary AddJob(RequestDefinition def)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (!CurrentJobs.ContainsKey(def.JobNumber))
                {
                    Job newJob = new Job(def);

                    //tdower todo - file, persistence if UWS restarted?
                    CurrentJobs.Add(Convert.ToInt64(newJob.JS.jobId), newJob);
                    return newJob.JS;
                }
                return null;
            }
        }

        internal static void DeleteJob(long jobID)
        {
            lock (CurrentJobs.SyncRoot)
            {
                if (CurrentJobs.ContainsKey(jobID))
                {
                    Job delJob = (Job)CurrentJobs[jobID];
                    CurrentJobs.Remove(jobID);
                    delJob.DeleteResults();
                }
            }
        }

        internal static Object GetShortJobList()
        {
            int count = 0;
            jobs joblist = new jobs();
            lock (CurrentJobs.SyncRoot)
            {
                count = CurrentJobs.Count;
                joblist.jobref = new ShortJobDescription[count];
                int i = 0;
                foreach (DictionaryEntry de in CurrentJobs)
                {
                    joblist.jobref[i] = new ShortJobDescription();
                    joblist.jobref[i].id = ((Job)de.Value).JS.jobId;
                    joblist.jobref[i].href = baseURL + "/async/" + ((Job)de.Value).JS.jobId;
                    joblist.jobref[i].phase = ((Job)de.Value).JS.phase;
                    ++i;
                }
            }
            return joblist;
        }

        //tdower todo this is what goes while'd in a separate thread.
        public static void MonitorJobs()
        {
            while (CurrentJobs.Count > 0)
            {
                Job delJob = null;
                Job currJob = null;
                lock (CurrentJobs.SyncRoot)
                {
                    foreach (DictionaryEntry de in CurrentJobs)
                    {
                        currJob = (Job)de.Value;
                        if (currJob.JS.destruction <= DateTime.Now)
                        {
                            delJob = currJob;
                            break;
                        }
                    }
                    if( delJob != null )
                        DeleteJob(Convert.ToInt64(delJob.JS.jobId));
                }

                Job runJob = null;
                lock (CurrentJobs.SyncRoot)
                {
                    long jobID = 0;
                    currJob = null;
                    foreach (DictionaryEntry de in CurrentJobs)
                    {
                        currJob = (Job)de.Value;
                        if (currJob.JS.phase == ExecutionPhase.QUEUED)
                        {
                            runJob = currJob;
                            jobID = Convert.ToInt64(currJob.JS.jobId);
                            ((Job)CurrentJobs[jobID]).JS.phase = ExecutionPhase.EXECUTING;
                            break;
                        }
                    }
                }
                if (runJob != null) //Do this outside of the lock if we can, to allow greater access.
                {
                    runJob.Run();
                    SaveJobChange(runJob); //also locks the list
                }

                System.Threading.Thread.Sleep(waitTime);
            }
        }
    }
}