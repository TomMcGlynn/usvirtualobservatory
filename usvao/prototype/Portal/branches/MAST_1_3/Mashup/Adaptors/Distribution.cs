using System;
using System.Web;
using System.Net;
using System.Xml;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Collections;
using System.Threading;
using System.IO;
using System.Data;
using System.Text;

using log4net;

using Mashup.Config;
using Utilities;

namespace Mashup.Adaptors
{
	[Serializable]
	public class Distribution : IAsyncAdaptor
	{
		private class ResourceClass
		{
			public string id = "";	
			public string FileName = "";
			
			public ResourceClass(string id, string filename="") 
			{
				this.id = id;
				this.FileName = filename;
			}
		}
		
		private class ResourceClassRepository : ResourceClass
		{
			public string Repository = "";
			
			public ResourceClassRepository(string id, string repository, string filename) : base(id, filename)
			{
				this.Repository = repository;
			}
		}
	
		private class ResourcesClass
		{
			public ArrayList Resource = new ArrayList();
		}
	
		private class DeliveryClass
		{
			public string mode = "async";
		}
	
		private class DistributionRequestClass
		{
			public ResourcesClass Resources = new ResourcesClass();
			public string BundleFileName = "download";
			public string Compression = "zip";
			public string Packaging = "zip";
			public DeliveryClass Delivery = new DeliveryClass();
		}
		
		private class RequestClass
        {
            public DistributionRequestClass DistributionRequest = new DistributionRequestClass();
        }
		
		//
		// Log4Net Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		//
		// Class Members
		//
		public String url {get; set;}
		public String poll {get; set;}
		public String polltime {get; set;}
		
		//
		// Default Constructor
		//
		public Distribution ()
		{
			url = "";
			poll = "";
			polltime = "3";
		}
			
		//
		// IAdaptor::invoke()
		//
	    public void invoke(MashupRequest muRequest, MashupResponse muResponse)
	    {	
			//
			// ====================
			// Distribution Request
			// ====================
			// Post {request} to http://dmslab1.stsci.edu:8080/Distribution/Request/
			// Returns <requestID> as plain text
			//
			//	{
			//	    "DistributionRequest": {
			//	        "Resources": {
			//	            "Resource": [
			//	                {
			//	                    "@id": "test/proj/j8d601010_asn.fits",
			//	                    "Repository": "STScI.MAST.iRODS",
			//	                    "FileName": "README.j8d601011_asn.fits"
			//	                },
			//	                {
			//	                    "@id": "http://galex.stsci.edu/data/GR6/pipe/01-vsn/03000-MISDR1_24278_0266/d/00-visits/0001-img/07-try/MISDR1_24278_0266_0001-asp.fits.gz",
			//	                    "FileName": "galex.gz"
			//	                },
			//	                {
			//	                    "@id": "http://galex.stsci.edu/data/GR6/pipe/01-vsn/03000-MISDR1_24278_0266/d/00-visits/0001-img/07-try/MISDR1_24278_0266_0001-aspraw.fits.gz"
			//	                },
			//	                {
			//	                    "@id": "http://galex.stsci.edu/data/GR6/pipe/01-vsn/03000-MISDR1_24278_0266/d/00-visits/0001-img/07-try/MISDR1_24278_0266_0001-asprta.fits.gz"
			//	                }
			//	            ]
			//	        },
			//	        "BundleFileName": "download",
			//	        "Compression": "zip",
			//	        "Packaging": "zip",
			//	        "Delivery": {
			//	            "@mode": "async"
			//	        }
			//	    }
			//	}
			//
			//
			// Where:
			// Compression : none, zip, gzip
			// Packaging : none, zip, tar
			//
			// ==============
			// Status Request
			// ==============
			// Status Request: http://dmslab1.stsci.edu:8080/Distribution/Async/Status/<requestID>
			// Returns: <status>|<message>
			//
			// Where <status> : PROCESSING, COMPLETE, or ERROR.  
			// When <status> == COMPLETE, <message> will be the direct URL for download.  
			// When <status> == ERROR, <message> will describe the error that occurred during processing.
			//
			
			////////////////////////////////////////////
			//
			//  Distribution State Transition Diagram
			// 
			//             (start) ---> PROCESSING
			//                |             |
			//                |             |
			//                + <-----------+
			//              /  \
			//             /    \
			//       COMPLETE  ERROR
			// 
			////////////////////////////////////////////
			
			//
			// Extract the Submit URL (url) 
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, muRequest.paramss);
					
			///////////////////////////////
			// (1) Build the Request Object 
			///////////////////////////////			
			RequestClass request = new RequestClass();
			
			//
			// Add the requested files from the incoming Mashup Request
			//
			string filelist = "";
			
			if (muRequest.paramss.ContainsKey("filename") && muRequest.paramss["filename"].ToString().Trim().Length > 0)
			{
				request.DistributionRequest.BundleFileName = muRequest.paramss["filename"].ToString().Trim();
			}	
			
			if (muRequest.paramss.ContainsKey("filelist") && muRequest.paramss["filelist"].ToString().Trim().Length > 0)
			{
				filelist = muRequest.paramss["filelist"].ToString().Trim();
			}
			
			string []files = filelist.Split(',');
			foreach (string urlid in files)
			{
				string filename = getFileNameFromUrl(urlid);
				ResourceClass resource = new ResourceClass(urlid, filename);
				request.DistributionRequest.Resources.Resource.Add(resource);
			}
			
			// Convert Distribution Request to Json
			StringBuilder sb = new StringBuilder();
			new JsonFx.Json.JsonWriter(sb).Write(request);
			
			// Use goofy naming convention required by Distribution Service for attributes (blech!)
			sb.Replace("\"id\":", "\"@id\":");
			sb.Replace("\"mode\":", "\"@mode\":");
			string json = sb.ToString();
			
			///////////////////////////////
			// (2) Post the Request Object 
			///////////////////////////////
			string requestID = Utilities.Web.postWebResponseString(sUrl, json, "application/json");	
					
			///////////////////////////////////////////////
			// (3) Build the Poll Url using <requestID>
			///////////////////////////////////////////////
			Dictionary<string, object> dict = new Dictionary<string, object>();
			dict["REQUEST_ID"] = requestID;
			string sPoll = Utilities.ParamString.replaceAllParams(poll, dict);
			
			///////////////////////////////////////////////
			// (4) Poll the Job, while phase is PROCESSING
			///////////////////////////////////////////////					
			string status="PROCESSING", response="",message="";
			int npolltime = Convert.ToInt32(polltime) * 1000;
			while (status == "PROCESSING")
			{
				// Sleep second iteration on...
				log.Debug(tid + "Distribution: Sleeping for " + npolltime + " millisecs.");
				if (npolltime > 0) Thread.Sleep(npolltime); 
				
				// Build Poll String and Invoke It
				response = Utilities.Web.getWebResponseString(sPoll);	
				if (!parseResponse(response, out status, out message))
				{
					throw new Exception("Distribution: Unable to parse Poll Response = " + response);
				}
			}
			
			//////////////////////////////////////////////////
			// (5) Load the Results, if the phase is COMPLETED
			//////////////////////////////////////////////////
			if (status == "COMPLETE")
			{
				if (Uri.IsWellFormedUriString(message, UriKind.RelativeOrAbsolute))
				{
					loadResults(muResponse, status, message);
				}
				else
				{
					throw new Exception ("Distribution: Invalid URI : " + message + "\n Response = " + response);
				}
			}
			else // PollResponse is no Good
			{
				throw new Exception ("Distribution: Unexpected status: " + status + "\n Response = " + response);
			}
		}
		
		protected string getFileNameFromUrl(string urlid)
		{
			// Default (empty string) is to use filename specified in URL 
			string filename = ""; 
			
			try
			{
				Uri uri = new Uri(urlid);
				
				// Check if URL is just query params.  If so, we must derive a filename from them.
				if (uri.Query.Length > 0)
				{
					// put query params into a Hashtable for use later on
					string query = uri.Query.Replace("?", "");
					Hashtable param = getQueryParams(query);
					
					// Use filename param, if specified
					if (param.ContainsKey("filename"))
					{
						filename = param["filename"] + "";
					}
					else // Derive filename based on query params
					{
						if (param.ContainsKey("dataset"))		// HLA
						{
							filename = param["dataset"] + "";
						}
						else if (param.ContainsKey("red"))		// HLA
						{
							filename = param["red"] + "";
						}
						else if (param.ContainsKey("specobjid")) // GALEX
						{
							filename = param["specobjid"] + "";
						}
					
						// Derive the filename suffix
						if (filename != "")
						{
							if (param.ContainsKey("format"))	// HLA
								filename += "." + param["format"];
							else if (param.ContainsKey("output_size") && param["output_size"].ToString() == "256")	// HLA
								filename += ".jpeg";
							else
								filename += ".fits";
						}
					}
				}
			} 
			// Catch any processing Exceptions and return blank filename, to use default
			catch (Exception ex) 
			{
				string msg = "Exception caught processing url: " + urlid;
				log.Error(msg, ex);
				Mail.sendException(ref ex, msg);
				filename = "";
			}
			
			return filename;
		}
		
		protected Hashtable getQueryParams(string query)
		{
			Hashtable param = new Hashtable();
			
			// nvp := Name Value Pair
			string []nvps = query.Split('&');
			foreach (string nvp in nvps)
			{
				string []nv = nvp.Split('=');
				if (nv != null)
				{
					string name = (nv.Length > 0 ? nv[0] : "");
					string val = (nv.Length > 1 ? nv[1] : "");
					if (name != "" && val != "")
					{
						param[name] = val;
					}
				}
			}
			return param;
		}
		
		////////////////////
		// parseResponse()
		////////////////////
		protected bool parseResponse(string response, out string status, out string message)
		{	
			status = "";
			message = "";
			
			// Look for first record of multi-line response
			if (response.IndexOf('\n') >= 0)
			{
				string[]records = response.Split('\n');
				response = records[0];
			}
			
			// Now look for the 2 fields "status|message"
			if (response.IndexOf("|") >= 0)
			{
				string[] items = response.Split('|');
				status = items[0].Trim();
				message = items[1].Trim();
				return true;
			}
			return false;
		}	
		
		///////////////////
		// loadResults()
		///////////////////
		protected void loadResults(MashupResponse muResponse, string status, string message)
		{	
			// Load the muResponse Data
			//muResponse.load(ds, complete);
			Dictionary<string, object>data = new Dictionary<string, object>();
			data.Add("url", message);
			muResponse.load(data, true);
		}
	}
}

