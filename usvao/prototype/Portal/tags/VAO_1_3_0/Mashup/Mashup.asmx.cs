using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Web.Services.Protocols;
using System.Net;
using System.Xml;
using System.Xml.Linq;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.IO;
using System.Text;
using System.Diagnostics;
using System.Configuration;
using System.Threading;

using log4net;
using JsonFx.Json;

using Mashup.Adaptors;
using Mashup.Config;
using Utilities;

namespace Mashup
{	
    /// <summary>
    /// Summary description for Mashup
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class Mashup : WebService 
    {	
		//
		// Initialize Config Files to ensure they load correctly!
		//
		public static ColumnsConfig cc = ColumnsConfig.Instance;
		public static AdaptorsConfig ac = AdaptorsConfig.Instance;
		
		//
		// Logger Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		private MashupRequest muRequest = null;
		private MashupResponse muResponse = null;
		
		#region invoke
		//
		// invoke()
		//
        [WebMethod]
        public void invoke(string request)
        {	
			string requestInfo = MashupRequest.requestInfo(request);		
            try
            {	
				log.Info(tid + "===> " + requestInfo);
								
                //
                // Decode the incoming Json Request into Mashup Request Object
                //			
				muRequest = new MashupRequest(request);	
				log.Debug(tid + "Created Mashup Request Object for service: " + muRequest.service);
				
                //
                // Extract Service Name from Mashup Request then retreive it's Service Adaptor from the Adaptors.Config File
                //
                if (muRequest.service != null && muRequest.service.Trim().Length > 0)
                {
                    Object adaptor = AdaptorsConfig.Instance.getAdaptor(muRequest.service);
                    if (adaptor != null)
                    {
						log.Debug(tid + "Created Service Adaptor of Type: " + adaptor.GetType());
						if (adaptor is ISyncAdaptor)
						{
							invokeSyncAdaptor(adaptor as ISyncAdaptor);
						}
						else if (adaptor is IAsyncAdaptor)
						{
							invokeAsyncAdaptor(adaptor as IAsyncAdaptor);
						}	      
                    }
                    else
                    {
                        throw new Exception("Mashup: Unable to Find Service Adaptor for service: \n" + muRequest.service);
                    }
                }
				else
				{
					throw new Exception("Mashup: 'service' param is missing. " + requestInfo);
				}
            }
            catch (Exception ex)
            {
				cleanUpException(requestInfo, ex);
            }
        }
		#endregion
		
		#region cleanUpException()
		private void cleanUpException(string requestInfo, Exception ex)
		{	
			//
			// List of Non Fatal Exceptions : These Exceptions are Logged Only
			//
			string[] nonFatalExceptions = new string[] { 
				"remote host closed the connection",
				"peer unexpectedly closed the connection"};
			
			//
			// STEP 1: Log the Exception
			//
			string msg = "Mashup: Exception caught. " + requestInfo;
			try
			{
	        	log.Error(tid + msg, ex);
			} catch (Exception ex2) {
				msg = "[CLEANUP] Exception caught while trying to Log the Original Exception. " + requestInfo;
				Console.WriteLine(msg + "\n" + ex2.ToString());
			}
			
			//
			// If Non-Fatal Exception, No Further Cleanup Required
			//
			foreach (string message in nonFatalExceptions)
			{
				if (ex.Message != null && ex.Message.ToLower().Contains(message))
				{
					return;
				}
			}
			
			//
			// STEP 2: Email Exception 
			//
			try
			{
				Mail.sendException(ref ex, msg); 
			} catch (Exception ex2) {
				msg = "[CLEANUP] Exception caught while trying to Email the Original Exception. " + requestInfo;
				Console.WriteLine(msg + "\n" + ex2.ToString());
			}
			
			//
			// STEP 3: Write Error Response To Client 
			// That way if Exceptions are thrown below during cleanUpException, the client is not left hanging.
			//
			try
			{
				if (muResponse == null)
				{
					muResponse = new MashupResponse();
				}
							
				muResponse.status = "ERROR";
				muResponse.msg = ex.Message;
				muResponse.writeCleanupResponse(muRequest, Context.Response); 
			} catch (Exception ex2)
			{
				msg = "[CLEANUP] Exception caught while Writing Error Response to Client. " + requestInfo;
				log.Error(tid + msg, ex2);
			}
			
			//
			// STEP 4: Terminate the Response Thread
			// NOTE: Many other Request Threads could be sitting and waiting for this Response Thread to Finish.
			// That is why we set the status to ERROR *before* terminating the Response Thread.
			//
			try
			{
				if (muResponse != null && muResponse.isActive)
				{			
					muResponse.thread.Abort();
				}
			} catch (Exception ex2)
			{
				msg = "[CLEANUP] Exception caught while Terminating Active Response Thread. " + requestInfo;
				log.Error(tid + msg, ex2);
			}
			
			//
			// STEP 5: Remove the Response Object From Cache
			//
			try
			{
				if (muRequest != null)
				{
					MashupCache.removeMashupResponse(muRequest.Key);
				}
			} 
			catch (Exception ex2) 
			{
				msg = "[CLEANUP] Exception caught while Removing Error Response from Cache. " + requestInfo;
				log.Error(tid + msg, ex2);
			}
		}
		#endregion 
		
		#region invokeSyncAdaptor()
		private void invokeSyncAdaptor(ISyncAdaptor a)
		{
			log.Debug(tid + "called.");
			a.invoke(muRequest, this.Context.Response);
		}
		#endregion 
		
		#region invokeAsyncAdaptor()
		private void invokeAsyncAdaptor(IAsyncAdaptor a)
		{
			log.Debug(tid + "[THREAD] called");
			
			//
			// Retreive the Mashup Response from Cache based on Request Key
			// If Mashup Response does not exist, create a new one, invoke() it's adaptor and store it in cache.
			//
            muResponse = MashupCache.getMashupResponse(muRequest.Key);
			
			//
			// Clear Response Object From Cache IF:
			//
			// Response Thread is no longer active AND
			// (1) clearcache was specified in the Request OR
			// (2) previous Response was an ERROR
			//
			// Otherwise, we reuse the Response
			//
			if (muResponse != null)
			{
				if (!muResponse.isActive)			
				{
					if (muRequest.clearcacheIsSpecified)
					{
						MashupCache.removeMashupResponse(muRequest.Key);
						muResponse = null;
					}
					else if (muResponse.status == "ERROR")
					{
						MashupCache.removeMashupResponse(muRequest.Key);
						muResponse = null;
					}
				}
			}
			
			//
			// If Mashup Response NOT found in CACHE (or we just cleared it from Cache), 
			// create a new Mashup Response and Invoke it's Adaptor on a Separate Thread
			//
			if (muResponse == null)
			{
				muResponse = new MashupResponse();
				if (muRequest.data == null)
				{
					MashupCache.insertMashupResponse(muRequest.Key, muResponse);
				}
				startAsyncAdaptorThread(a);
			}
			
			//
			// If Response Thread is still active, 
			// wait for the Response Data based on request timeout interval
			// then write back to the client the Data (if complete) or Status.
			//
			if (muResponse.isActive)
			{
				log.Debug(tid + "[THREAD] Waiting for [" + muRequest.timeoutAsSecs + "] secs on " + muResponse.Debug());
				muResponse.wait(muRequest.timeoutAsMsecs);
				log.Debug(tid + "[THREAD] Back from Wait on " + muResponse.Debug());
			}
			else // Thread is no longer Active : Sanity check that Response Status reflects this.
			{
				log.Debug(tid + "[THREAD] No Wait for " + muResponse.Debug());

				if (muResponse.status == "EXECUTING")
				{
					muResponse.status = "ERROR";
					muResponse.msg = "[MASHUP] Invoke Thread exited without specifying the response status.  Results may be incomplete.";
					log.Error(tid + muResponse.msg);
				}
			}
			
			// If the Client is still connected, write the formatted response
			if (Context.Response.IsClientConnected)
			{
				muResponse.writeMashupResponse(muRequest, Context.Response); 
			}
			else
			{
				log.Warn(tid + "[WARNING] Client Unexpectedly Disconnected, NOT writing response.");			
			}		
		}
		
		public void startAsyncAdaptorThread(IAsyncAdaptor a)
		{
			//
			// Invoke the Adaptor on a New Thread
			//
		    Thread t = new Thread(delegate()
	        {
				this.AsyncAdaptorThread(a);
	        });
			
			t.Name = muRequest.Key;
			
			// NOTE: This is Critical to save the Thread ID on the Response Object.
			// Becaue it is used later on in the wait() call down below and on Subsequent Mashup Requests.
			muResponse.thread = t;
			log.Debug(tid + "[THREAD] Starting AsyncAdaptorThread [" + t.ManagedThreadId + "]");

		    t.Start();				// Start the AsyncAdaptorThread() Delegate
		}
		
		public void AsyncAdaptorThread(IAsyncAdaptor a)
		{
			log.Debug(tid + "[THREAD] entry");

			try
			{
				a.invoke(muRequest, muResponse);
			}
			catch (Exception ex)
			{
				// Log Error and Send Email Notification to Developers, then cleanup the response
				string msg = "[MASHUP] AsyncAdaptorThread Exception. request: " + muRequest.Debug();
                log.Error(tid + msg, ex);
				Mail.sendException(ref ex, msg);
				
				// Cleanup the Response Message in Cache so Client Request Thread will notify client
				muResponse.status = "ERROR";
				muResponse.msg = ex.Message;
			}
			
			log.Debug(tid + "[THREAD] exit");
		}
		#endregion 
	

		
		#region upload
		[WebMethod]
		public void upload()
        {
			log.Info(tid + "===> " + "[UPLOAD] Files Count:" + Context.Request.Files.Count);

            // Response type is void because we write back a custom result
			// First off, we have to deal with a POST and extract any uploaded files:	
			UploadResponse response=null;
			try
			{
				// Verify the contents contain a file
		        if (Context.Request.Files.Count == 0)
		        {
		            throw new Exception("No File(s) in Request.");
		        }
		
		        foreach (string fileKey in Context.Request.Files)
		        {
		            // Save each file out to disk
		            HttpPostedFile httpFile = Context.Request.Files[fileKey];
					if (httpFile.FileName == null || httpFile.FileName.Trim().Length == 0)
					{
						throw new Exception("FileName not Specified");
					}
		            response = savePostedFile(httpFile);
		        }

			} 
			catch (Exception ex)
			{
				response = new UploadResponse(false, "File Upload Failed", ex.Message, "");
				log.Error(tid + "     " + "[UPLOAD] File Upload Failed", ex);
				Mail.sendException(ref ex, "Exception Caught Uploading File.");
			}
			
			writeResponse(response);
			log.Info(tid + "<=== " + "[UPLOAD] " + response.ToJson());
        }	
		
		protected void writeResponse(UploadResponse response)
		{
			//////////////////////////////////////////////////////////////
			// Write the Upload Response as Json back out to the client
			//////////////////////////////////////////////////////////////
			Context.Response.ClearHeaders();
			Context.Response.ClearContent();
			Context.Response.ContentType = "text/html";
            Context.Response.Write(response.ToJson());
	        Context.Response.Flush();
		}	
	
	    protected UploadResponse savePostedFile(HttpPostedFile httpFile)
	    {
	        FileDefinition fd = getUniqueFileDefintion(httpFile.FileName);
			
            httpFile.SaveAs(fd.filename);
			
			// Return successful response attributes
			UploadResponse response = new UploadResponse(true, "", "Upload Complete for file = " + httpFile.FileName, fd.url);
			return response;
	    }
				
		//
		// Need a static lock here to control access to Unique Filename Searcher
		//
		static object UploaderLock = new object();
		
		protected FileDefinition getUniqueFileDefintion(string filename)
		{
			// Get the Output Directory from the Web.Config
            string TempDir = ConfigurationManager.AppSettings["TempDir"];

            if (TempDir == null)
            {
                throw new Exception("Unable to save File: 'TempDir' not specified in the Web.Config.");
            }

            // Convert TempDir From Local Relative Directory To Full Local Path 
			string tempDir = TempDir.Trim();
            if (!tempDir.EndsWith("/")) tempDir += "/";
			
			string fullPath = "";
			if (tempDir.Contains(":"))	// Assume full path is specified in Web.Config
			{
				fullPath = tempDir + filename;
			}
			else 						// Assume relative path is specified in Web.Config
			{
            	fullPath = System.Web.HttpContext.Current.Server.MapPath(tempDir + filename);
			}
			
	        // Remove '+' character which causes headache(s) for the SQL
	        fullPath = fullPath.Replace('+', '-');
	
	        //
	        // Ensure that only one thread at a time is determining a unique filename
	        //
	        string url = ""; // Url pointing to Unique Filename on Disk
			
	        lock (UploaderLock)
	        {
	            int i = 0;
				while (File.Exists(fullPath))
	            {
	                fullPath = Path.GetDirectoryName(fullPath) + '/' + 
						   	   Path.GetFileNameWithoutExtension(filename) + 
						   	   "_" + (i++) + 
						   	   Path.GetExtension(filename);
	            }

	        }  // lock()	
				
	        // Determine the file URL
	        url = getUrl(tempDir, fullPath);
		
			FileDefinition ufile = new FileDefinition(fullPath, url);
			return ufile;
	    }
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		// Determine the URL that points to the Saved File 
		//
		// IMPORTANT NOTE: 
		//
		// We backup ONE '/' from the right side of the oringinal URL in order to determine the root URL.
		// The BIG ASSUMPTION is the the initial request came in on a URL similar to this:
		//
		// http://127.0.0.1:8080/Uplaoder.asmx/invoke
		//
		// so backing up ONE '/' from the right gives us a root url of:
		//
	    // http://127.0.0.1:8080/Uplaoder.asmx
		//
		// backing up TWO '/' from the right gives a root url of:
		//
		// http://127.0.0.1:8080
		//
		// Next Append the 'TempDir' and filename:
		//
		// http://127.0.0.1:8080/Temp/filename.txt
		//
		//////////////////////////////////////////////////////////////////////////////////////////////
		protected string getUrl(string tempDir, string fullPath) 
		{
			string uploadUrl = System.Web.HttpContext.Current.Request.Url.GetLeftPart(UriPartial.Path);
			int length = uploadUrl.LastIndexOf("/");
			uploadUrl = (length > 0 ? uploadUrl.Substring(0, length) : uploadUrl);
			
			length = uploadUrl.LastIndexOf("/");
			uploadUrl = (length > 0 ? uploadUrl.Substring(0, length) : uploadUrl);
			if (!uploadUrl.EndsWith("/")) uploadUrl += "/";
			
			// Uri.EscapeDataString()
            uploadUrl += tempDir + Path.GetFileName(fullPath);	
			return uploadUrl;
		}
		
		protected class UploadResponse
		{
			public bool   success = true;
			public string errors = "";
			public string msg = "";
			public string data = "";
			
			public UploadResponse (bool success, string errors, string msg, string data)
			{
				this.success = success;
				this.errors = errors;
				this.msg = msg;
				this.data = data;
			}
										
			public string ToJson()
			{
				StringBuilder sb = new StringBuilder();
				JsonWriter jw = new JsonFx.Json.JsonWriter(sb);
				jw.Write(this);
				return sb.ToString();
			}
		}
		
		protected class FileDefinition 
		{
			public string filename = "";
			public string url = "";
						
			public FileDefinition (string filename, string url) 
			{
				this.filename = filename;
				this.url = url;
			}
		}
		#endregion 
		
		#region stats	
		[WebMethod]
		public void stats()
		{
			StatsResponse response = getStatsResponse();
			
			//////////////////////////////////////////////////////////////
			// Write the Info Response as Json back out to the client
			//////////////////////////////////////////////////////////////
			Context.Response.ClearHeaders();
			Context.Response.ClearContent();
			Context.Response.ContentType = "text/javascript";
            Context.Response.Write(response.ToJson());
	        Context.Response.Flush();
			
			log.Info(tid + "<=== " + "[STATS] : " + response.ToJson());
		}
		
		protected StatsResponse getStatsResponse()
		{
			int work_avail, comp_avail;  ThreadPool.GetAvailableThreads(out work_avail, out comp_avail);	
			int work_min, comp_min;  ThreadPool.GetMinThreads(out work_min, out comp_min);		
			int work_max, comp_max;  ThreadPool.GetMaxThreads(out work_max, out comp_max);	
			
			StatsResponse response = new StatsResponse(work_avail, work_min, work_max, System.Web.HttpRuntime.Cache.Count);
			return response;
		}
		
		protected class StatsResponse
		{
			public int threads_avail;
			public int threads_min;
			public int threads_max;
			public int cache_count;
			
			public StatsResponse (int threads_avail, int threads_min, int threads_max, int cache_count)
			{
				this.threads_avail = threads_avail;
				this.threads_min = threads_min;
				this.threads_max = threads_max;
				this.cache_count = cache_count;
			}
										
			public string ToJson()
			{
				StringBuilder sb = new StringBuilder();
				JsonWriter jw = new JsonFx.Json.JsonWriter(sb);
				jw.Write(this);
				return sb.ToString();
			}
		}
		#endregion 
	}
}