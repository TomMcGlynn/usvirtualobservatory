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
			public string _id = "";	
			
			public ResourceClass(string id) 
			{
				this._id = id;
			}
		}
		
		private class ResourceClassRepository : ResourceClass
		{
			public string Repository = "";
			public string FileName = "";
			
			public ResourceClassRepository(string id, string repository, string filename) : base(id)
			{
				this.Repository = repository;
				this.FileName = filename;
			}
		}
	
		private class ResourcesClass
		{
			public ArrayList Resource = new ArrayList();
		}
	
		private class DeliveryClass
		{
			public string _mode = "async";
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
			foreach (string file in files)
			{
				ResourceClass resource = new ResourceClass(file);
				request.DistributionRequest.Resources.Resource.Add(resource);
			}
			
			// Convert Distribution Request to Json
			StringBuilder sb = new StringBuilder();
			new JsonFx.Json.JsonWriter(sb).Write(request);
			
			// Replace goofy naming convention used by Distribution Service for attributes
			sb.Replace("_id", "@id");
			sb.Replace("_mode", "@mode");
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
		
		////////////////////
		// parseResponse()
		////////////////////
		protected bool parseResponse(string response, out string status, out string message)
		{	
			status = "";
			message = "";
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

