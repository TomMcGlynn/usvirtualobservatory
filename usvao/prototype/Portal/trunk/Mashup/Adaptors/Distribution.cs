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
            public string format = "bundle";
		}
	
		private class DistributionRequestClass
		{
			public ResourcesClass Resources = new ResourcesClass();
			public string BundleFileName = "download";
			public string Compression = "gzip";
			public PackagingClass Packaging = new PackagingClass();
			public DeliveryClass Delivery = new DeliveryClass();
		}

        private class PackagingClass
        {
            public bool includeTopLevelDir = false;
            public string packageType = "tar";
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
			//	        "Compression": "gzip",
			//	        "Packaging": "tar",
			//	        "Delivery": {
			//	            "@mode": "async|wget|curl"
            //              "@format": "bundle|wget|curl"
			//	        }
			//	    }
			//	}
			//
			//
			// Where:
			// Compression : none, zip, gzip
            // Packaging : ":{"@includeTopLevelDir":"true|false", "$":"tar|zip|none"}
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
			// Extract the Params from the incoming Mashup Request
			//
			string urlList = "",
                   pathList = "",
                   extension = "";
			
			if (muRequest.paramss.ContainsKey("filename") && muRequest.paramss["filename"].ToString().Trim().Length > 0)
			{
				request.DistributionRequest.BundleFileName = muRequest.paramss["filename"].ToString().Trim();
			}	
			
			if (muRequest.paramss.ContainsKey("urlList") && muRequest.paramss["urlList"].ToString().Trim().Length > 0)
			{
				urlList = muRequest.paramss["urlList"].ToString().Trim();
			}

            if (muRequest.paramss.ContainsKey("pathList") && muRequest.paramss["pathList"].ToString().Trim().Length > 0)
            {
                pathList = muRequest.paramss["pathList"].ToString().Trim();
            }

            if (muRequest.paramss.ContainsKey("extension") && muRequest.paramss["extension"].ToString().Trim().Length > 0)
            {
                extension = muRequest.paramss["extension"].ToString().Trim();
                if ((extension == "wget") || (extension == "curl"))
                {
                    request.DistributionRequest.Compression = "none";
                    request.DistributionRequest.Packaging.packageType = "none";
                    request.DistributionRequest.Delivery.format = extension;
                }
                else if (extension == "zip")
                {
                    request.DistributionRequest.Compression = "zip";
                    request.DistributionRequest.Packaging.packageType = "zip";
                }
                else if (extension == "tar")
                {
                    request.DistributionRequest.Compression = "gzip";
                    request.DistributionRequest.Packaging.packageType = "tar";
                }
            }

			// Add each url/filename pair to the Distribution Request Object
			string[] urls = urlList.Split (',');
            string [] paths = pathList.Split(',');
            int i = 0;
			foreach (string urli in urls)
			{
				string path = (i < paths.Length ? paths[i++] : "");
				string fullpathname = path;

				// If pathname is a directory only (does not contain a '.') we then attempt to derive a the filename from the URL
				if (!Path.GetFileName (fullpathname).Contains (".")) {
					if (fullpathname.Trim().Length > 0 && !fullpathname.EndsWith("/")) fullpathname += "/";
					fullpathname += getFileNameFromUrl(urli);
				}

				ResourceClass resource = new ResourceClass(urli, fullpathname);  // This lets us build a hierarchy in the bundle file
				request.DistributionRequest.Resources.Resource.Add(resource);
			}
			
			// Convert Distribution Request to Json
			StringBuilder sb = new StringBuilder();
			new JsonFx.Json.JsonWriter(sb).Write(request);
			
			// Use goofy naming convention required by Distribution Service for attributes (blech!)
			sb.Replace("\"id\":", "\"@id\":");
			sb.Replace("\"mode\":", "\"@mode\":");
            sb.Replace("\"includeTopLevelDir\":", "\"@includeTopLevelDir\":");
            sb.Replace("\"format\":", "\"@format\":");
            sb.Replace("\"packageType\":", "\"$\":");
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
            string size = "0", progress = "0.0", bytes = "0";
			int npolltime = Convert.ToInt32(polltime) * 1000;
			int n404Errors = 0;

			while (status == "PROCESSING")
			{
				// Sleep second iteration on...
				log.Debug(tid + "Distribution: Sleeping for " + npolltime + " millisecs.");
				if (npolltime > 0) Thread.Sleep(npolltime); 
				
				// Build Poll String and Invoke It 
				// We also allow for 3 HTTP 404 Errors from Distribution (blech!)
                try
                {
                    response = Utilities.Web.getWebResponseString(sPoll);
					if (!parseResponse(response, out status, out message, out size, out progress, out bytes))
					{
						throw new Exception("Distribution: Unable to parse Poll Response = " + response);
					} 
                }
                catch (Exception ex)
                {
					if (ex.Message.Contains("404"))
					{
						if (++n404Errors >= 3)
						{
							throw ex;
						}
					}
					else
					{
						throw ex;
					}
                }
 
                updateProgress(muResponse, progress, bytes);
			}
			
			//////////////////////////////////////////////////
			// (5) Load the Results, if the phase is COMPLETED
			//////////////////////////////////////////////////
			if (status == "COMPLETE")
			{
				if (Uri.IsWellFormedUriString(message, UriKind.RelativeOrAbsolute))
				{
                    Dictionary<string, string> statusList = gatherUrls(response);
					loadResults(muResponse, status, message, statusList);
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
                            int pos = filename.LastIndexOf('/');
                            filename = filename.Substring(pos + 1);
                            pos = filename.IndexOf('.');
                            if (pos > 0)
                            {
                                filename = filename.Substring(0, pos);
                            }
                        }
                        else if (param.ContainsKey("specobjid")) // GALEX
                        {
                            filename = param["specobjid"] + "";
                        }
                        else if (param.ContainsKey("hstonline_mark")) // HST online
                        {
                            filename = param["hstonline_mark"] + "";
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
                else
                {   // cut the leaf name off the URI
                    filename = urlid.Substring(urlid.LastIndexOf('/') + 1);
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
		protected bool parseResponse(string response, out string status, out string message, out string size, out string progress, out string bytes)
		{	
			status = "";
			message = "";
            size = "0";
            progress = "0.0";
            bytes = "0";
            int nBytes = 0;
            string[] records = null;
			
			// Look for first record of multi-line response
			if (response.IndexOf('\n') >= 0)
			{
				records = response.Split('\n');
				response = records[0];
			}
			
			// Now look for the 2 fields "status|message"
			if (response.IndexOf("|") >= 0)
			{
				string[] items = response.Split('|');
				status = items[0].Trim();
				message = items[1].Trim();
                size = items[2].Trim();

                int len = records.Length, completed = 0, realLen = 0;
                for (int i = 1; i < len; i++)
                {
                    string[] urls = records[i].Split('|');
                    if (urls.Length > 1) {
                        realLen++;
                        if (urls[1] == "COMPLETE") completed++;
                    }
                    if (urls.Length > 2)
                    {
                        nBytes += System.Convert.ToInt32(urls[2]);
                    }
                }
                double percent = (System.Convert.ToDouble(completed) / (realLen));
                progress = percent.ToString();
                bytes = nBytes.ToString();
				return true;
			}
			return false;
		}

        protected Dictionary<string, string> gatherUrls(string response)
        {
            Dictionary<string, string> retval = new Dictionary<string, string>();

            string[] lines = response.Split('\n');
            int len = lines.Length;
            for (int i = 1; i < len; i++) 
            {
                string line = lines[i].Trim();
                if (line == "") continue;
                string[] s = line.Split('|');
                string url = s[0],
                       status = s[1];
                retval[url] = status;
            }

            return retval;
        }

		///////////////////
		// loadResults()
		///////////////////
        protected void loadResults(MashupResponse muResponse, string status, string message, Dictionary<string, string> statusList)
        {
            // Load the muResponse Data
            //muResponse.load(ds, complete);
            Dictionary<string, object> data = new Dictionary<string, object>();
            data.Add("url", message);
            data.Add("statusList", statusList);
            muResponse.load(data, true);
        }
		
		///////////////////
		// updateProgress()
		///////////////////
		protected void updateProgress(MashupResponse muResponse, string percent, string size)
		{	
			// Load the muResponse Data
			//muResponse.load(ds, complete);
			Dictionary<string, object>data = new Dictionary<string, object>();
			data.Add("progress", percent);
            data.Add("bytesStreamed", size);
			muResponse.load(data, false);
		}
	}
}

