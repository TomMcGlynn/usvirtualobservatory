using System;
using System.Web;
using System.Net;
using System.Xml;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Threading;
using System.IO;
using System.Data;

using log4net;

using Mashup.Config;
using Utilities;

namespace Mashup.Adaptors
{
	[Serializable]
	public class Datascope : IAsyncAdaptor
	{
		//
		// Log4Net Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		//
		// Class Members
		//
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
	    public void invoke(MashupRequest muRequest, MashupResponse muResponse)
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
			string sUrl = Utilities.ParamString.replaceAllParams(url, muRequest.paramss);
			
			///////////////////////////////
			// (1) Submit the Initial Job 
			///////////////////////////////
			
			string phase, error;
			long job; 
			int numberOfHits;
			Dictionary<string, string> resultUrls;
			int npolltime;
			
			string sSubmitResponse = Utilities.Web.getWebResponseString(sUrl);	
			if (!parseResponse(sSubmitResponse, out phase, out job, out numberOfHits, out resultUrls, out error))
			{
				throw new Exception("Datascope: Unable to parse UWS. Submit Response = " + sSubmitResponse);
			}
			
			// We need to embed the [JOB] number in the subsequent Run and Poll URLs
			Dictionary<string, object> dict = new Dictionary<string, object>();
			dict["job"] = job;
						
			////////////////////////////////////////
			// (2) Run the Job, while phase is PENDING
			////////////////////////////////////////
			string sRunResponse = "";
			if (phase == "PENDING")
			{
				npolltime = 0;
				while (phase == "PENDING")
				{
					// Sleep for second iteration on...
					log.Debug(tid + "Datascope: Sleeping for " + npolltime + " millisecs.");
					if (npolltime > 0) Thread.Sleep(npolltime); npolltime = Convert.ToInt32(polltime) * 1000;
					
					// Build Run String and Invoke It
					string sRun = Utilities.ParamString.replaceAllParams(run, dict);
					sRunResponse = Utilities.Web.getWebResponseString(sRun);	
					if (!parseResponse(sRunResponse, out phase, out job, out numberOfHits, out resultUrls, out error))
					{
						throw new Exception("Datascope: Unable to parse UWS. Run Response = " + sRunResponse);
					}
				}
			}
			else // SubmitReponse is No Good
			{
				throw new Exception ("Datascope: Unexpected UWS phase: " + phase + " Expected PENDING. Submit Response = " + sSubmitResponse);
			}
			
			/////////////////////////////////////////////////////
			// (3) Poll the Job, if phase is QUEUED or EXECUTING
			/////////////////////////////////////////////////////
			string sPoll = Utilities.ParamString.replaceAllParams(poll, dict);
			npolltime = 0;
			int lastNumberOfHits = 0;
			string sPollResponse = "";
			if (phase == "QUEUED" || phase == "EXECUTING")
			{	
				int parsingExceptions = 0;	// See IMPORTANT NOTE Below
				while (phase == "QUEUED" || phase == "EXECUTING")
				{
					// Sleep for second iteration on...
					log.Debug(tid + "Datascope: Sleeping for " + npolltime + " millisecs.");
					if (npolltime > 0) Thread.Sleep(npolltime); npolltime = Convert.ToInt32(polltime) * 1000;
					
					// Get the Poll Response
					sPollResponse = Utilities.Web.getWebResponseString(sPoll);	
					
					// Parse the muResponse XML
					if (parseResponse(sPollResponse, out phase, out job, out numberOfHits, out resultUrls, out error))
					{
						// Only Load the Results if the NumberOfHits is different from last muRequest (or not found)
						if (numberOfHits != lastNumberOfHits || numberOfHits < 0)
						{
							/////////////////////////////////////////////////////////////////////////////////
							// IMPORTANT NOTE: 
							// The DataScope Service randomly returns incomplete files (blech). 
							// Which causes a VoTable parsing exception on our end. 
							// So we catch the parsing exception and try to retreive the file again.
							// After 3 attempts we abandon all hope and push the exception up the call stack 
							// and abort this DataScope Query entirely.
							/////////////////////////////////////////////////////////////////////////////////							
							try {
								loadResults(resultUrls, false, muRequest, muResponse);
								parsingExceptions=0;
							} 
							catch (Exception ex)
							{
								parsingExceptions++;
								string msg = "WARNING: Datascope: Parsing Exception caught [" + parsingExceptions + "] while parsing VotTable. request = " + muRequest.ToJson();
								log.Warn(tid + msg, ex);
								Mail.sendException(ref ex, msg);
								//
								// If we got 3 parsing exceptions in a row, we abandon all hope on this DataScope Request
								//
								if (parsingExceptions > 2) 
								{
									throw (new Exception(msg, ex));
								}
							}
						}
						else
						{
							log.Debug(tid + "Datascope: Not loading results: NumberOfHits = " + numberOfHits + " LastNumberOfHits = " + lastNumberOfHits);
						}
						lastNumberOfHits = numberOfHits;
					}
					else
					{
						throw new Exception("Datascope: Unable to parse UWS. Poll Response = " + sPollResponse);
					}
				}
			}
			else if (phase != "COMPLETED") // RunResponse is No good
			{
				throw new Exception ("Datascope: Unexpected UWS phase: " + phase + "\n Expecting QUEUED or EXECUTING. Run Response = " + sRunResponse);
			}
			
			//////////////////////////////////////////////////
			// (4) Get the Results, if the phase is COMPLETED
			//////////////////////////////////////////////////
			if (phase == "COMPLETED")	
			{
				if (resultUrls != null && resultUrls.Count > 0)
				{
					loadResults(resultUrls, true, muRequest, muResponse);
				}
				else
				{
					throw new Exception ("Datascope: Unable to extract Results Returned from COMPLETED Status.\n resultUrls[] = " + resultUrls);
				}
			}
			else // PollResponse is no Good
			{
				throw new Exception ("Datascope: Unexpected UWS phase: " + phase + "\n Expecting COMPLETED. Poll Response = " + sPollResponse);
			}
		}
		
		////////////////////
		// parseResponse()
		////////////////////
		protected bool parseResponse(string sResponse, out string phase, out long job, out int numberOfHits, out Dictionary<string, string> resultsURLs, out string error)
		{				
			//
			// Initialize return variables
			//
			phase = "";
			job = -1;
			numberOfHits = -1;
			resultsURLs = null;
			error = "";
			
			// Ensure muResponse String contains data before continuing to parse it
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
			
			log.Debug(tid + "Datascope: job = " + job + " phase = " + phase);

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
				
					//List<String> urls = new List<String>();
					Dictionary<string, string> urls = new Dictionary<string, string>();
					if (xmllist.Count > 0)
					{
		                foreach (XmlNode xnode in xmllist)
		                {
							if (xnode.Attributes["id"] != null && xnode.Attributes["id"].Value != null)
							{
								string attribute = xnode.Attributes["id"].Value.ToLower();
								switch (attribute)
								{
									case "numberofhits":
										log.Debug(tid + "Datascope: NumberOfHits: " + xnode.InnerText);
								        numberOfHits = Convert.ToInt32(xnode.InnerText);
										break;
										
									case "results.xml":
									case "hits.xml":
									case "status.xml":
										if( xnode.Attributes["href"] != null && 
								            xnode.Attributes["href"].Value != null)
										{
											urls.Add(attribute, xnode.Attributes["href"].Value);
										}
										else if (xnode.Attributes["xlink:href"] != null && 
								                 xnode.Attributes["xlink:href"].Value != null)
										{
											urls.Add(attribute, xnode.Attributes["xlink:href"].Value);
										}
										break;
								} // end switch on ["id"]
							
							} // end if ["id"] attribute exists
						
						} // end for each xml node
					
						// If we found some result Urls, save them to the 'out' resultsUrl Array.
						if (urls.Count > 0)
						{
							//resultsURLs = new string[urls.Count];
							//urls.CopyTo(resultsURLs);
							resultsURLs = urls;
						}
					}
					break;
				}	
				default: // Unknown phase encounterred
					log.Debug(tid + "DataScope: Unknown phase = " + phase);
				    valid = false;
					break;
			}
			
			return valid;
		}	
		
		///////////////////
		// loadResults()
		///////////////////
		protected void loadResults(Dictionary<string, string> resultUrls, bool complete, MashupRequest muRequest, MashupResponse muResponse)
		{
			if (resultUrls != null && resultUrls.Count > 0)
			{
				foreach (KeyValuePair<string, string> resultKeyVal in resultUrls)
				{
					if (resultKeyVal.Key != null && resultKeyVal.Key.Equals("hits.xml"))
					{
						// Invoke the result URL and Transform the result VoTable into a DataSet	
						Stream s =  Utilities.Web.getWebReponseStream(resultKeyVal.Value);
						XmlTextReader reader = new XmlTextReader(s); 
						DataSet ds = Utilities.Transform.VoTableToDataSet(reader);
						
						//
						// Retreive Column Definitions for the Service and append them to the DataSet Column 'Extended Properties'
						//
						Dictionary<string, object> props = ColumnsConfig.Instance.getColumnProperties(muRequest.service);
						if (props != null && props.Count > 0)
						{
							Utilities.Transform.AppendColumnProperties(ds, props, ColumnsConfig.CC_PREFIX);
						}
					
						// Load the muResponse Data
			            muResponse.load(ds, complete);
					}
				}
			}
		}
	}
}

