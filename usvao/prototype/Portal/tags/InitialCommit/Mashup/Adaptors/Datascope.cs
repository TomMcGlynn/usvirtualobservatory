using System;
using System.Web;
using System.Net;
using System.Xml;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Threading;
using System.IO;
using System.Data;

namespace Mashup.Adaptors
{
	[Serializable]
	public class Datascope : IAsyncAdaptor
	{
		public String url {get; set;}
		public String run {get; set;}
		public String poll {get; set;}
		public String polltime {get; set;}
		
		//
		// Default Constructor
		//
		public Datascope ()
		{
			url = "";
			run = "";
			poll = "";
			polltime = "3";
		}
			
		//
		// IAdaptor::invoke()
		//
	    public void invoke(ServiceRequest request, ServiceResponse response)
	    {	
			////////////////////////////////////////////
			//
			//  Datascope State Transition Diagram
			// 
			//          (start)
			//             |
			//          PENDING ------\
			//         /      \        \
			//    QUEUED --> EXECUTING  \
			//                    \      \
			//                     COMPLETED
			//
			////////////////////////////////////////////
			
			//
			// Extract the Submit URL (url) and the Poll URL (poll)
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, request.paramss);
			
			///////////////////////////////
			// (1) Submit the Initial Job 
			///////////////////////////////
			
			string phase, error;
			long job; 
			int numberOfHits;
			string []resultUrls;
			
			string sResponse = Utilities.Web.getWebResponseString(sUrl);	
			if (!parseResponse(sResponse, out phase, out job, out numberOfHits, out resultUrls, out error))
			{
				throw new Exception("Datascope: Unable to parse UWS Response = " + sResponse);
			}
			
			// We need to embed the [JOB] number in the subsequent Run and Poll URLs
			Dictionary<string, object> dict = new Dictionary<string, object>();
			dict["job"] = job;
						
			////////////////////////////////////////
			// (2) Run the Job, if phase is PENDING
			////////////////////////////////////////
			if (phase == "PENDING")
			{
				string sRun = Utilities.ParamString.replaceAllParams(run, dict);
				string sRunResponse = Utilities.Web.getWebResponseString(sRun);	
				if (!parseResponse(sRunResponse, out phase, out job, out numberOfHits, out resultUrls, out error))
				{
					throw new Exception("Datascope: Unable to parse UWS Response = " + sResponse);
				}
			}
			else 
			{
				throw new Exception ("Datascope: Unexpected UWS phase: " + phase + " Expected PENDING.  Response = " + sResponse);
			}
			
			/////////////////////////////////////////////////////
			// (3) Poll the Job, if phase is QUEUED or EXECUTING
			/////////////////////////////////////////////////////
			string sPoll = Utilities.ParamString.replaceAllParams(poll, dict);
			int npolltime = 0;
			int lastNumberOfHits = 0;
			if (phase == "QUEUED" || phase == "EXECUTING")
			{	
				while (phase == "QUEUED" || phase == "EXECUTING")
				{
					Console.WriteLine("Datascope: Sleeping for " + npolltime + " millisecs.");
					Thread.Sleep(npolltime); npolltime = Convert.ToInt32(polltime) * 1000;
					string sPollResponse = Utilities.Web.getWebResponseString(sPoll);	
					
					// Parse the Response XML
					if (parseResponse(sPollResponse, out phase, out job, out numberOfHits, out resultUrls, out error))
					{
						// Only Load the Results if the NumberOfHits is different from last request (or not found)
						if (numberOfHits != lastNumberOfHits || numberOfHits < 0)
						{
							loadResults(resultUrls, false, response);
						}
						else
						{
							Console.WriteLine("Datascope: Not loading results: NumberOfHits = " + numberOfHits + " LastNumberOfHits = " + lastNumberOfHits);
						}
						lastNumberOfHits = numberOfHits;
					}
					else
					{
						throw new Exception("Datascope: Unable to parse UWS Response = " + sResponse);
					}
					
				}
			}
			else if (phase != "COMPLETED")
			{
				throw new Exception ("Datascope: Unexpected UWS phase: " + phase + "\n Expecting QUEUED or EXECUTING.  Response = " + sResponse);
			}
			
			//////////////////////////////////////////////////
			// (4) Get the Results, if the phase is COMPLETED
			//////////////////////////////////////////////////
			if (phase == "COMPLETED")	
			{
				if (resultUrls != null && resultUrls.Length > 0)
				{
					loadResults(resultUrls, true, response);
				}
				else
				{
					throw new Exception ("Datascope: Unable to extract Results Returned from COMPLETED Status.\n Response = " + sResponse);
				}
			}
			else
			{
				throw new Exception ("Datascope: Unexpected UWS phase: " + phase + "\n Expecting COMPLETED.  Response = " + sResponse);
			}
		}
		
		////////////////////
		// parseResponse()
		////////////////////
		protected bool parseResponse(string sResponse, out string phase, out long job, out int numberOfHits, out string[] resultsURLs, out string error)
		{				
			//
			// Initialize return variables
			//
			phase = "";
			job = -1;
			numberOfHits = -1;
			resultsURLs = null;
			error = "";
			
			// Ensure Response String contains data before continuing to parse it
			if (sResponse == null || sResponse.Trim().Length == 0)
			{
				return false;
			}

			bool valid=true;
			
			//
			// Strip namespace information: 
			// .NET xml document processing doesn't like the datascope namespace format
            // and the STScI TAP service doesn't include it.
			//
            sResponse = sResponse.Replace("uws:", "");

            XmlDocument doc = new XmlDocument();
            doc.LoadXml(sResponse);
			
			// Extract job number
            XmlNode node = doc.GetElementsByTagName("jobId")[0];
            job = Convert.ToInt64(node.InnerXml);
			
			// Extrace phase
            node = doc.GetElementsByTagName("phase")[0];
            phase = node.InnerXml.ToUpper().Trim();
			
			Console.WriteLine("Datascope: job = " + job + " phase = " + phase);

			switch (phase)
			{
				case "ERROR":	// The job failed to complete. No further work will be done nor Results produced.  
	                node = doc.GetElementsByTagName("errorSummary")[0];
	                if (node != null && node.InnerText != string.Empty)
					{
	                    error = node.InnerText;
					}
					break;
				
				case "PENDING": 	// The job is accepted by the service but not yet committed for execution by the client. In this state, the job quote can be read and evaluated. This is the state into which a job enters when it is first created.
				case "QUEUED": 		// The job is committed for execution by the client but the service has not yet assigned it to a processor. No Results are produced in this phase.
				case "ABORTED": 	// The job has been manually aborted by the user, or the system has aborted the job due to lack of or overuse of resources. 
				case "UNKNOWN":		// The job is in an unknown state. 
				case "HELD": 		// The job is HELD pending execution and will NOT automatically be executed (cf,PENDING) 
				case "SUSPENDED": 	// The job has been suspended by the system during execution. This might bebecause of temporary lack of resource. The UWS WILL automatically resume the job into the EXECUTING 
					break;
				
				case "EXECUTING": 	// The job has been assigned to ap rocessor. Results may be produced at any time during this phase.
				case "COMPLETED":	// The execution of the job is over. The Results may be collected. 
				{    
				    //
				    // Parse the results shown here:
				    //
				    // <uws:results xsi:schemaLocation="http://www.ivoa.net/xml/UWS/v1.0/UWS.xsd">
					//    <uws:result id="results.xml" xlink:href="http://heasarc.gsfc.nasa.gov/vo/testcache/187.277916_2.052381_0.2/results.xml"/>
					//    <uws:result id="hits.xml" xlink:href="http://heasarc.gsfc.nasa.gov/vo/testcache/187.277916_2.052381_0.2/hits.xml"/>
					//    <uws:result id="status.xml" xlink:href="http://heasarc.gsfc.nasa.gov/vo/testcache/187.277916_2.052381_0.2/status.xml"/>
					//    <uws:result id="numberOfHits">453</uws:result>
					// </uws:results>
					//
					XmlNodeList xmllist = doc.GetElementsByTagName("result");
				
					List<String> urls = new List<String>();
					if (xmllist.Count > 0)
					{
		                foreach (XmlNode xnode in xmllist)
		                {
							if (xnode.Attributes["id"] != null && xnode.Attributes["id"].Value != null)
							{
								switch (xnode.Attributes["id"].Value.ToLower())
								{
									case "numberofhits":
										Console.WriteLine("Datascope: NumberOfHits: " + xnode.InnerText);
								        numberOfHits = Convert.ToInt32(xnode.InnerText);
										break;
										
									case "results.xml":
									case "hits.xml":
									case "status.xml":
										if( xnode.Attributes["href"] != null && 
								            xnode.Attributes["href"].Value != null)
										{
											urls.Add(xnode.Attributes["href"].Value);
										}
										else if (xnode.Attributes["xlink:href"] != null && 
								                 xnode.Attributes["xlink:href"].Value != null)
										{
											urls.Add(xnode.Attributes["xlink:href"].Value);
										}
										break;
								} // end switch on ["id"]
							
							} // end if ["id"] attribute exists
						
						} // end for each xml node
					
						// If we found some result Urls, save them to the 'out' resultsUrl Array.
						if (urls.Count > 0)
						{
							resultsURLs = new string[urls.Count];
							urls.CopyTo(resultsURLs);
						}
					}
					break;
				}	
				default: // Unknown phase encounterred
					Console.WriteLine("DataScope: Unknown phase = " + phase);
				    valid = false;
					break;
			}
			
			return valid;
		}	
		
		///////////////////
		// loadResults()
		///////////////////
		protected void loadResults(string []resultUrls, bool complete, ServiceResponse response)
		{
			if (resultUrls != null && resultUrls.Length > 0)
			{
				foreach (string resultUrl in resultUrls)
				{
					if (resultUrl != null && resultUrl.Contains("hits.xml"))
					{
						// Invoke the result URL and Transform the result VoTable into a DataSet	
						Stream s =  Utilities.Web.getWebReponseStream(resultUrl);
						XmlTextReader reader = new XmlTextReader(s); 
						DataSet ds = Utilities.Transform.VoTableToDataSet(reader);
					
						// Load the Response Data
			            response.load(ds, complete);
					}
				}
			}
		}
	}
}

