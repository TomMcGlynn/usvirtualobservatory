//Tdower: This uses MSDN monitor sample code.

using System;
using System.Threading;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using Mashup;

namespace UWS
{
	public class UWSClient
	{
		protected string url;
		protected string query;
		protected string run;
		
		protected ServiceRequest request;
		protected ServiceResponse response;
		
		public UWSClient(string url, string query, string run)
		{
			this.url = url;
			this.query = query;
			this.run = run;
		}
		
		public UWSClient(string url, string query, string run, ServiceRequest request, ServiceResponse response) : 
			this(url, query, run)
		{
			this.request = request;
			this.response = response;
		}
		
		public void start()
		{
	        UWSJob job = new UWSJob(url);
	        UWSPollingClient client = new UWSPollingClient(job, query, run, request, response);  // Use UWSJob for storage 
	        UWSJobMonitor monitor = new UWSJobMonitor(job);  // Use UWSJob for storage
	
			// Ceate Threads Client and Monitor Threads
	        Thread ClientThread = new Thread(new ThreadStart(client.ThreadRun));
	        Thread MonitorThread = new Thread(new ThreadStart(monitor.ThreadRun));
			
	        // Sstartup the Client and Monitor Threads
	        try
	        {
	            ClientThread.Start();
	            MonitorThread.Start();
	
	            ClientThread.Join();   // Join both threads with no timeout, Run both until done.
	            MonitorThread.Join();
				
				//
	            // Client and Monitor threads have finished at this point.
				//
				Console.WriteLine("UWSClient: Client and Monitor Threads Finished.");
	        }
	        catch (Exception ex)
	        {
	            Console.WriteLine(ex);  // Display text of exception
				throw new Exception("UWSClient: Thread Exception Caught inside UWS Adaptor", ex);
	        }
		}
		
		//
		// Main - Used to Test Drive the UWSClient() class from the Console.
		//
	    public static void Main(String[] args)
	    {
			//
			// MAST Tap Service Example
			//
	        string argURL = "http://nvodev.stsci.edu/obstap/tapservice.aspx";
	        string argQuery = "SELECT=s_ra,s_dec&FROM=obscore&MAXREC=500"; //SELECT=$STD returns a very large number of standard columns.
			string argRun = "";
			
			//
			// DataScope Example
			//
			
	        int result = 0;   // Threading result initialized to say there is no error
	
	        //TAP-specific for testing.
	        string startParameters = "REQUEST=doQuery&LANG=PQL&";
	
	        ParseArguments(args, ref argURL, ref argQuery);
	        if (argQuery == string.Empty || argURL == string.Empty)
	        {
	            System.Console.WriteLine("arguments url:{url} and query:{PQL query parameters} are required.");
	            return;
	        }
	
			UWSClient client = new UWSClient(argURL, startParameters + argQuery, argRun);
			client.start();
			
	        // Even though Main returns void, this provides a return code to 
	        // the parent process.
	        Environment.ExitCode = result;
	    }
	
	    private static void ParseArguments(String[] args, ref string argURL, ref string argQuery)
	    {
	        foreach (string str in args)
	        {
	            string arg = str.ToLower();
	
	            if (arg.StartsWith("url:"))
	            {
	                if (arg.Length > 4)
	                   argURL = str.Substring(4).Trim();
	            }
	            if (arg.StartsWith("query:"))
	            {
	                if (arg.Length > 6)
	                    argQuery = str.Substring(6).Trim();
	            }
	        }
	    }
	}
}