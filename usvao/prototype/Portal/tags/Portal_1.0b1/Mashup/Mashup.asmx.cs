using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Web.Services.Protocols;
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
		
		#region invoke()
		//
		// invoke()
		//
        [WebMethod]
        public void invoke(string request)
        {		
            try
            {	
				log.Info(tid + "===> [INVOKE] " + request);
				log.Info(tid + "     [STATS] " + MashupInfo());
								
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
					throw new Exception("Mashup: 'service' param is missing from request: " + request);
				}
            }
            catch (Exception ex)
            {
				cleanUp(request, ex);
            }
        }
		#endregion
		
		#region cleanUp()
		private void cleanUp(string request, Exception ex)
		{		
			//
			// STEP 1: Notify the pending Client of the failure:
			// That way if Exceptions are thrown below during cleanUp, the client is not left hanging.
			//
			try
			{
				if (muResponse == null)
				{
					muResponse = new MashupResponse();
				}
							
				muResponse.status = "ERROR";
				muResponse.msg = ex.Message;
				muResponse.write(muRequest, Context.Response); 
			} catch (Exception ex2)
			{
				string msg = "SECONDARY Exception caught while Writing Error Response to Client. request: " + request;
				log.Error(tid + msg, ex2);
				Mail.sendException(ref ex2, ex2.Message);
			}
			
			//
			// STEP 2: Log Original Exception Error Message and Send Email Notification to Developers
			//
			try
			{
				string msg = "Mashup: Invoke Exception caught. request: " + request;
	        	log.Error(tid + msg, ex);
				Mail.sendException(ref ex, msg); 
			} catch (Exception ex2) {
				string msg = "SECONDARY Exception caught while Logging Original Exception. request = " + request;
				Console.WriteLine(msg + "\n" + ex2.ToString());
			}
			
			//
			// STEP 3: Terminate the Response Thread if it's Still Active
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
				string msg = "SECONDARY Exception caught while Terminating Active Thread. request = " + request;
				log.Error(tid + msg, ex2);
				Mail.sendException(ref ex2, ex2.Message);
			}
			
			//
			// STEP 4: Remove the Error Response Object From Cache
			//
			try
			{
				if (muRequest != null)
				{
					ResponseCache.clearMashupResponse(muRequest.key);
				}
			} 
			catch (Exception ex2) 
			{
				string msg = "SECONDARY Exception caught while Clearing Error Response from Cache.  request = " + request;
				log.Error(tid + msg, ex2);
				Mail.sendException(ref ex2, ex2.Message);
			}
		}
		#endregion 
		
		#region invokeSyncAdaptor()
		private void invokeSyncAdaptor(ISyncAdaptor a)
		{
			log.Debug(tid + "called.");
			a.invoke(muRequest, this.Context.Response);
			log.Info(tid + "<=== " + muRequest.ToJson());
		}
		#endregion 
		
		#region invokeAsyncAdaptor()
		private void invokeAsyncAdaptor(IAsyncAdaptor a)
		{
			//
			// LOCK BEGIN: (ResponseCache)
			//
			// The following secion of code allows 1 Thread to Execute at a time:
			// Retreive the Mashup Response from Cache based on Request Key
			// If Mashup Response does not exist, create a new one, invoke() it's adaptor and store it in cache.
			//
			log.Debug(tid + "[THREAD] called");

			lock (typeof(ResponseCache))
            {			
				// Extract Mashup Response from Response Cache
                muResponse = ResponseCache.getMashupResponse(muRequest.key);
				
				//
				// Checks for Clearing Response Object From Cache:
				//
				// (1) Response Thread MUST no longer be active 
				// (2) AND clearcache was specified in the Request
				// (3) OR prior Response was an ERROR
				//
				// Otherwise, we reuse the Response
				//
				if (muResponse != null)
				{
					if (!muResponse.isActive)			
					{
						if (muRequest.clearcacheIsTrue)
						{
							ResponseCache.clearMashupResponse(muRequest.key);
							muResponse = null;
						}
						else if (muResponse.status == "ERROR")
						{
							ResponseCache.clearMashupResponse(muRequest.key);
							muResponse = null;
						}
						else
						{
							ResponseCache.Debug("Reuse (inactive)" , muRequest.key);
						}
					}
					else
					{
						ResponseCache.Debug("Reuse (active)", muRequest.key);
					}
				}
				
				// If Mashup Response not found in CACHE (or we just cleared it from Cache), 
				// create a new Mashup Response and Invoke it's Adaptor on a Separate Thread
				if (muResponse == null)
				{
					muResponse = new MashupResponse();
					ResponseCache.putMashupResponse(muRequest.key, muResponse);
					startAsyncAdaptorThread(a);
				}

            } // LOCK END: (ResponseCache)
			
			//
			// If Response Thread is still active, 
			// We will wait for the Response Data based on request timeout interval
			// then write back to the client the Data (if complete) or Status.
			//
			if (muResponse.isActive)
			{
				log.Debug(tid + "[THREAD] Waiting for [" + muRequest.timeouSecs + "] secs on " + muResponse.Debug());
				muResponse.wait(muRequest.timeoutMsecs);
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
				muResponse.write(muRequest, Context.Response); 
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
			
			t.Name = muRequest.key;
			
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
				string msg = "[MASHUP] AsyncAdaptorThread Exception. request: " + muRequest.ToJson();
                log.Error(tid + msg, ex);
				Mail.sendException(ref ex, msg);
				
				// Cleanup the Response Message in Cache so Client Request Thread will notify client
				muResponse.status = "ERROR";
				muResponse.msg = ex.Message;
			}
			
			log.Debug(tid + "[THREAD] exit");
		}
		#endregion 
		
		#region Response Cache
		//
		// ResponseCache Class
		//

		private class ResponseCache
        {				
			public static readonly int DEFAULT_CACHE_TIMEOUT_MINUTES = 20;
			static readonly string CACHE_TIMEOUT_MINUTES_KEY = "CacheTimeoutMinutes";
			static string sCacheTimeoutMinutes = System.Configuration.ConfigurationManager.AppSettings.Get(CACHE_TIMEOUT_MINUTES_KEY);
			
			public static void clearMashupResponse(String key)
			{
				System.Web.HttpRuntime.Cache.Remove(key);
				Debug("Remove", key);
			}
			
            public static MashupResponse getMashupResponse(String key)
            {
                MashupResponse muResponse = (MashupResponse)System.Web.HttpRuntime.Cache[key];
				if (muResponse != null) Debug("Get", key);
                return muResponse;
            }
			
			public static void putMashupResponse(String key, MashupResponse muResponse)
			{
				//
                // Add Mashup Response Object to the cache.  
				// Setup to expire 20 minutes from now (default) or the key ("CacheTimeoutMinutes") from the Web.Config.  
				// We use a sliding expiration which would bump up the timeout each time the cache data is accessed.  
                //
				int minutes = (sCacheTimeoutMinutes != null ? Convert.ToInt32(sCacheTimeoutMinutes) : DEFAULT_CACHE_TIMEOUT_MINUTES);
				System.Web.HttpRuntime.Cache.Insert(key, muResponse, null, 
													System.Web.Caching.Cache.NoAbsoluteExpiration, 
					     							new TimeSpan(0, minutes, 0), 
													System.Web.Caching.CacheItemPriority.Normal, 
													ResponseCache.onRemoveCallback);
				Debug("Insert", key);
			}
			
			public static void onRemoveCallback(String key, object response, System.Web.Caching.CacheItemRemovedReason reason)
		    {
				Debug("Expired", key);

				if (response is MashupResponse)
				{
					MashupResponse muResponse = response as MashupResponse;
					if (muResponse.isActive)
					{
						string msg = "Response Object is still Active. Terminating Thread. Response = " + muResponse.Debug();
						Mashup.log.Error(msg);
						Mail.sendError("Response Object is Still Active", DateTime.Now, msg, false);
						muResponse.abort();
					}
				}
		    }

            private static DateTime GetNextMinutes(int minutes)
            {
                // Get the current time, add on the desired number of minutes, minimum is 1 minute.
                DateTime now = DateTime.Now.AddMinutes((double)(minutes <= 0 ? 1 : minutes));
                DateTime next = new DateTime(now.Year, now.Month, now.Day, now.Hour, now.Minute, 0, 0);
                return next;
            }
			
			public static void Debug(string state, string key)
			{
				log.Debug(tid + "[CACHE]: Count = " + System.Web.HttpRuntime.Cache.Count + ", " + state + " = " + key);
			}
        }
		#endregion  
		
		#region MashupInfo
		[WebMethod]
		public string MashupInfo()
		{
			int work_avail, comp_avail;  ThreadPool.GetAvailableThreads(out work_avail, out comp_avail);	
			int work_min, comp_min;  ThreadPool.GetMinThreads(out work_min, out comp_min);		
			int work_max, comp_max;  ThreadPool.GetMaxThreads(out work_max, out comp_max);		
			return "Worker Threads: avail = " + work_avail + ", " +
				                   " min =  " + work_min + ", " +
					               " max =  " + work_max + ", " + 
					               " Cache Count = " + System.Web.HttpRuntime.Cache.Count;
		}
		#endregion 
		
		#region Test Driver Methods
		//
		// Test Driver Methods
		//

        [WebMethod]
        public void MastNameLookup(string input)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Name.Lookup";
			r.paramss["input"] = input;
			
			invoke(r.ToJson());
        }

        [WebMethod]
        public void VoGalexCone(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Galex.Cone";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			invoke(r.ToJson());
        }
		
        [WebMethod]
        public void VoGenericTable(string url, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Generic.Table";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["url"] = url;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoInventoryCone(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Inventory.Cone";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoHlaCone(string ra, string dec, string radius, string catalog, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Hla.Cone";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoCaomTap(string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Caom.Tap";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoHesarcDatascope(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Hesarc.Datascope";			
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;

			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastCaomCone(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Caom.Cone";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			invoke(r.ToJson());
        }

        [WebMethod]
        public void VoGalexSiap(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Galex.Siap";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastHlspProject(string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Hlsp.Project";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastHlspProducts(string id, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Hlsp.Products";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["id"] = id;

			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastGalexTile(string ra, string dec, string radius, string catalog, string maxrecords, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Galex.Tile";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			r.paramss["maxrecords"] = maxrecords;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastGalexCatalog(string ra, string dec, string radius, string catalog, string maxrecords, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Galex.Catalog";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			r.paramss["maxrecords"] = maxrecords;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void CaomConeVotable(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Caom.Cone.Votable";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void GalexSiapVotable(string format, string timeout, string clearcache)
        {
			MashupRequest r = new MashupRequest();
			r.service = "Galex.Siap.Votable";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MashupTestHttpProxy(string url)
        {
			MashupRequest r = new MashupRequest();
			r.paramss["url"] = url;
			r.service = "Mashup.Test.HttpProxy";
			
			invoke(r.ToJson());
        }

		[WebMethod]
        public void MashupTableExporter(string data, string filename, string format)
        {			
			MashupRequest r = new MashupRequest();
			r.filename = (filename != null && filename.Trim().Length > 0 ? filename : "output.csv");
			r.data = (data != null && data.Trim().Length > 0 ? data : jsonTable);
			r.format = (format != null && format.Trim().Length > 0 ? format : "csv");
			r.clearcache = "true";
			r.service = "Mashup.Table.Exporter";
						
			invoke(r.ToJson());
        }
		#endregion 
		    
		private static string jsonTable = 
		"{\"name\":\"0.0.0\"," + 
			"\"Columns\":[ " + 
			"  {\"text\": \"objid\", \"dataIndex\": \"objid\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"objid\",\"vot.ucd\": \"ID_MAIN\",\"vot.datatype\": \"char\",\"vot.name\": \"objid\"}}," + 
			"  {\"text\": \"iauname\", \"dataIndex\": \"iauname\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"iauname\",\"vot.ucd\": \"?\",\"vot.datatype\": \"char\",\"vot.name\": \"iauname\"}}," + 
			"  {\"text\": \"ra\", \"dataIndex\": \"ra\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"ra\",\"vot.ucd\": \"?\",\"vot.datatype\": \"double\",\"vot.name\": \"ra\"}}," + 
			"  {\"text\": \"dec\", \"dataIndex\": \"dec\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"dec\",\"vot.ucd\": \"?\",\"vot.datatype\": \"double\",\"vot.name\": \"dec\"}}," + 
			"  {\"text\": \"e_bv\", \"dataIndex\": \"e_bv\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"e_bv\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"e_bv\"}}," + 
			"  {\"text\": \"nuv_artifact\", \"dataIndex\": \"nuv_artifact\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_artifact\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int32\",\"vot.name\": \"nuv_artifact\"}}," + 
			"  {\"text\": \"fuv_artifact\", \"dataIndex\": \"fuv_artifact\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_artifact\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int32\",\"vot.name\": \"fuv_artifact\"}}," + 
			"  {\"text\": \"nuv_flags\", \"dataIndex\": \"nuv_flags\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_flags\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int16\",\"vot.name\": \"nuv_flags\"}}," + 
			"  {\"text\": \"fuv_flags\", \"dataIndex\": \"fuv_flags\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_flags\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int16\",\"vot.name\": \"fuv_flags\"}}," + 
			"  {\"text\": \"nuv_flux\", \"dataIndex\": \"nuv_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_flux\"}}," + 
			"  {\"text\": \"fuv_flux\", \"dataIndex\": \"fuv_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_flux\"}}," + 
			"  {\"text\": \"nuv_fluxerr\", \"dataIndex\": \"nuv_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fluxerr\"}}," + 
			"  {\"text\": \"fuv_fluxerr\", \"dataIndex\": \"fuv_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_fluxerr\"}}," + 
			"  {\"text\": \"nuv_fwhm_world\", \"dataIndex\": \"nuv_fwhm_world\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fwhm_world\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fwhm_world\"}}," + 
			"  {\"text\": \"fuv_fwhm_world\", \"dataIndex\": \"fuv_fwhm_world\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_fwhm_world\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_fwhm_world\"}}," + 
			"  {\"text\": \"nuv_mag\", \"dataIndex\": \"nuv_mag\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_mag\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_mag\"}}," + 
			"  {\"text\": \"fuv_mag\", \"dataIndex\": \"fuv_mag\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_mag\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_mag\"}}," + 
			"  {\"text\": \"nuv_magerr\", \"dataIndex\": \"nuv_magerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_magerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_magerr\"}}," + 
			"  {\"text\": \"fuv_magerr\", \"dataIndex\": \"fuv_magerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_magerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_magerr\"}}," + 
			"  {\"text\": \"nuv_fcat_flux\", \"dataIndex\": \"nuv_fcat_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fcat_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fcat_flux\"}}," + 
			"  {\"text\": \"fuv_ncat_flux\", \"dataIndex\": \"fuv_ncat_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_ncat_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_ncat_flux\"}}," + 
			"  {\"text\": \"nuv_fcat_fluxerr\", \"dataIndex\": \"nuv_fcat_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fcat_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fcat_fluxerr\"}}," + 
			"  {\"text\": \"fuv_ncat_fluxerr\", \"dataIndex\": \"fuv_ncat_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_ncat_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_ncat_fluxerr\"}}," + 
			"  {\"text\": \"nuv_weight\", \"dataIndex\": \"nuv_weight\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_weight\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_weight\"}}," + 
			"  {\"text\": \"fuv_weight\", \"dataIndex\": \"fuv_weight\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_weight\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_weight\"}}," + 
			"  {\"text\": \"survey\", \"dataIndex\": \"survey\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"survey\",\"vot.ucd\": \"?\",\"vot.datatype\": \"char\",\"vot.name\": \"survey\"}}" +
			"]," + 
			"\"Fields\":[ " + 
			"  {\"name\": \"objid\", \"type\": \"string\"}," + 
			"  {\"name\": \"iauname\", \"type\": \"string\"}," + 
			"  {\"name\": \"ra\", \"type\": \"float\"}," + 
			"  {\"name\": \"dec\", \"type\": \"float\"}," + 
			"  {\"name\": \"e_bv\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_artifact\", \"type\": \"int\"}," + 
			"  {\"name\": \"fuv_artifact\", \"type\": \"int\"}," + 
			"  {\"name\": \"nuv_flags\", \"type\": \"int\"}," + 
			"  {\"name\": \"fuv_flags\", \"type\": \"int\"}," + 
			"  {\"name\": \"nuv_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fwhm_world\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_fwhm_world\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_mag\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_mag\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_magerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_magerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fcat_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_ncat_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fcat_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_ncat_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_weight\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_weight\", \"type\": \"float\"}," + 
			"  {\"name\": \"survey\", \"type\": \"string\"}" +
			"]," + 
			"\"Rows\":[ " + 
			"  [\"2532925283203297679\",\"GALEX J003559.7-425953\",8.99888621477736,-42.9983250726353,0.0068157,0,256,3,3,2.097009,1.593237,0.1955768,0.1512506,0.002704791,0.002033239,23.096,23.3943,0.1012854,0.1030972,2.152891,1.772928,0.1719466,0.1665996,25424,28596,\"DIS\"]," + 
			"  [\"2532925283203297707\",\"GALEX J003558.6-425954\",8.99432154559548,-42.9985192834603,0.0068157,0,256,3,3,2.297442,2.090112,0.1489189,0.1758012,0.001840609,0.002210505,22.99689,23.09958,0.07039393,0.09134442,3.682231,1.465541,0.201472,0.1294275,25456,28600,\"DIS\"]" +
			"]" + 
		"}";	
	}
}