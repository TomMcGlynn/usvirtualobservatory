//Tdower: This uses MSDN monitor sample code.

using System;
using System.Threading;
using System.Collections;
using System.Collections.Generic;

using UWSClient;

public class UWSThreadedClient
{
    public static void Main(String[] args)
    {
        //string argURL = "http://nvodev.stsci.edu/obstap/tapservice.aspx/async";
        //string startParameters = "REQUEST=doQuery&LANG=PQL&SELECT=s_ra,s_dec&FROM=obscore&MAXREC=500";
        //string runURL = string.Empty;

        string argURL = "http://heasarc.gsfc.nasa.gov/cgi-bin/vo/dscope_dev";
        string startParameters = "ra=187.277916&dec=2.052381&radius=0.2";
        string runURL = "/phase?phase=RUN";

        int result = 0;   // Threading result initialized to say there is no error


        UWSJob UWSJob = new UWSJob(argURL);
        UWSPollingClient prod = new UWSPollingClient(UWSJob, startParameters, runURL);  // Use UWSJob for storage 
        UWSJobMonitor cons = new UWSJobMonitor(UWSJob);  // Use UWSJob for storage


        Thread producer = new Thread(new ThreadStart(prod.ThreadRun));
        Thread Monitor = new Thread(new ThreadStart(cons.ThreadRun));
        // Threads producer and Monitor have been created, 
        // but not started at this point.

        try
        {
            producer.Start();
            Monitor.Start();

            producer.Join();   // Join both threads with no timeout
            // Run both until done.
            Monitor.Join();
            // threads producer and Monitor have finished at this point.
        }
        catch (ThreadStateException e)
        {
            Console.WriteLine(e);  // Display text of exception
            result = 1;            // Result says there was an error
        }
        catch (ThreadInterruptedException e)
        {
            Console.WriteLine(e);  // This exception means that the thread
            // was interrupted during a Wait
            result = 1;            // Result says there was an error
        }
        // Even though Main returns void, this provides a return code to 
        // the parent process.
        Environment.ExitCode = result;
    }

 }




