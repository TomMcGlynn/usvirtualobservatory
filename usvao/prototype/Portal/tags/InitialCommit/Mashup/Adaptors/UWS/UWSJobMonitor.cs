using System;
using System.Collections.Generic;
using System.Threading;
using System.Text;

namespace UWS
{
    public class UWSJobMonitor
    {
        UWSJob UWSJob;         // Job management information shared between threads.

        public UWSJobMonitor(UWSJob job)
        {
            UWSJob = job;         
        }
        public void ThreadRun()
        {
            while (UWSJob.GetPhase() != "ERROR" && UWSJob.GetPhase() != "COMPLETED")
            {
                UWSJob.HandleWebResponsesFromUWSJob();
                Thread.Sleep(100);
            }
       }
    }
}
