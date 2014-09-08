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

using Newtonsoft;

using Mashup.Adaptors;

namespace Mashup
{
    /// <summary>
    /// Summary description for Service1
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class Mashup : WebService 
    {
        //
        // ServiceRequest Async Stuff: 
		// NOTE: The Async code causes an Exception when loading the service in MonoDevelop:
		// System.InvalidOperationException: System.AsyncCallback cannot be serialized because it does not have a default public constructor
        //
		
		/* * * 
        public delegate void RequestMethodStub(string json);
        public class RequestMethodState
        {
            public object state;
            public RequestMethodStub stub;
            public RequestMethodState(object state, RequestMethodStub stub)
            {
                this.state = state;
                this.stub = stub;
            }
        }

        [WebMethod ]
        public IAsyncResult BeginRequestAsync(string json, AsyncCallback cb, object prevState)
        {
            Console.WriteLine("BeginRequestAsync(): called");
            RequestMethodStub stub = new RequestMethodStub(invoke);
            RequestMethodState state = new RequestMethodState(prevState, stub);
            return stub.BeginInvoke(json, cb, state);	// should call the stub here	
        }

        [WebMethod]
        public void EndRequestAync(IAsyncResult call)
        {
            Console.WriteLine("EndRequestAsync(): called");
            RequestMethodState state = (RequestMethodState)call.AsyncState;
            state.stub.EndInvoke(call);  // should end the stub here
        }
		* * */

		//private static string sRequestType = "{\"$type\":\"" + new ServiceRequest().GetType().AssemblyQualifiedName + "\",\n";
		
		//
		// invoke()
		//
        [WebMethod]
        public void invoke(string request)
        {
            Console.WriteLine("invoke(): request = " + request);
			ServiceRequest svcreq = null;
            try
            {
                //
                // Decode the incoming Json ServiceRequest into ServiceRequest Object
                //			
                Dictionary<string, object> requestDict = fastJSON.JSON.Instance.ToDictionary(request);
				svcreq = new ServiceRequest(Context, requestDict);	
				Console.WriteLine("service:" + svcreq.service);
				
                //
                // Extract Service Name from ServiceRequest 
                //
                if (svcreq.service != null && svcreq.service.Trim().Length > 0)
                {
					//
                    // Retreive Adaptor Definition (json) from Web.Config based on Service Name
					//
                    string sAdaptor = System.Configuration.ConfigurationManager.AppSettings.Get(svcreq.service.Trim());
                    Console.WriteLine("sAdaptor:" + sAdaptor);
                    if (sAdaptor != null && sAdaptor.Trim().Length > 0)
                    {
                        // Decode Adaptor Definition to Object
                        Object a = fastJSON.JSON.Instance.ToObject(sAdaptor) as Object;
                        if (a != null)
                        {
                            Console.WriteLine("Calling invoke() on type:" + a.GetType());
							if (a is ISyncAdaptor)
							{
								invokeSync(a as ISyncAdaptor, svcreq);
							}
							else if (a is IAsyncAdaptor)
							{
								invokeAsync(a as IAsyncAdaptor, svcreq);
							}	      
                        }
                        else
                        {
                            throw new Exception("Unable to deserialize Service Adaptor for service: \n" + svcreq.service);
                        }
                    }
                    else
                    {
                        throw new Exception("No Service Adaptor found for service: " + svcreq.service);
                    }
                }
				else
				{
					throw new Exception("'service' param is required for request: " + svcreq);
				}
            }
            catch (Exception ex)
            {
                // ToDo: Write Error into Response Object
                Console.WriteLine(ex);
                throw (new Exception("Exception caught invoking Mashup Adaptor: " + svcreq.service + "\n", ex));
            }
        }

		private void invokeSync(ISyncAdaptor a, ServiceRequest svcreq)
		{
			a.invoke(svcreq, this.Context.Response);
		}

		private void invokeAsync(IAsyncAdaptor a, ServiceRequest svcreq)
		{
			//
			// LOCK BEGIN: (ServiceResponseCache)
			//
			// The following secion of code allows 1 Thread to Execute at a time:
			// Retreive the ServiceResponse from Cache based on Request Key
			// If ServiceResponse does not exist, create a new one, and store it in the cache.
			//
			ServiceResponse svcresp = null;
			lock (typeof(ServiceResponseCache))
            {			
				// Extract ServiceResponse from CACHE
                svcresp = ServiceResponseCache.getServiceResponse(svcreq.key);
				
				// Clear CACHE first if specified AND the Response Thread is NOT Active
				if (svcresp != null && !svcresp.isActive && 
				    svcreq.clearcache != null && Convert.ToBoolean(svcreq.clearcache))
				{
					Console.WriteLine("Mashup: Removing Object from CACHE: " + svcreq.key);
					ServiceResponseCache.clearServiceResponse(svcreq.key);
					svcresp = null;
				}
				
				// If ServiceResponse not found in CACHE, create a new ServiceResponse and Invoke it's Adaptor 
				if (svcresp == null)
				{
					Console.WriteLine("Mashup: Saving Object to CACHE: " + svcreq.key);
					svcresp = new ServiceResponse();
					ServiceResponseCache.putServiceResponse(svcreq.key, svcresp);
					
					//
					// Invoke the Service Adaptor on a New Thread
					//
					Console.WriteLine("Mashup: Starting Thread a.invoke()...");
				    Thread t = new Thread(delegate()
			        {
			        	a.invoke(svcreq, svcresp);
			        });
					// NOTE: This is Critical to save the Thread on the Response Object.
					svcresp.thread = t;		// Used later in the wait() call down below.
				    t.Start();				// Start the Adaptor.invoke() 
				}
				else
				{
					Console.WriteLine("Mashup: FOUND Object in CACHE: " + svcreq.key);
				}
            } // LOCK END: (ServiceResponseCache)
			
			//
			// If Response Thread is still active, 
			// We will wait for the Response Data based on request timeout interval
			// then write back to the client the Data (if complete) or Status.
			//
			if (svcresp.isActive)
			{
				int timeout = (svcreq.timeout != null ? Convert.ToInt32(svcreq.timeout) : 20) * 1000;
				Console.WriteLine("Mashup: calling wait() timeout = " + timeout);
				svcresp.wait(timeout);
				Console.WriteLine("Mashup: back from wait() isActive = " + svcresp.isActive);
			}
			else // Thread is no longer Active : Sanity check that Response Status reflects this.
			{
				if (svcresp.status == "EXECUTING")
				{
					Console.WriteLine("Mashup: Response Thread is gone, but Response Status is EXECUTING: changing to COMPLETE.");
					svcresp.status = "COMPLETE";
				}
			}
			
			Console.WriteLine("Mashup: writing response...");
			svcresp.write(svcreq, Context.Response); 
			Console.WriteLine("Mashup: done.");
		}
		
		private class ServiceResponseCache
        {
			public static void clearServiceResponse(String key)
			{
				System.Web.HttpRuntime.Cache.Remove(key);
			}
			
            public static ServiceResponse getServiceResponse(String key)
            {
                ServiceResponse resp = (ServiceResponse)System.Web.HttpRuntime.Cache[key];
                return resp;
            }
			
			public static void putServiceResponse(String key, ServiceResponse resp)
			{
				//
                // Add ServiceResponse to the cache.  
				// Setup to expire 20 minutes from now.  
				// Do not use a sliding expiration which would bump up the 
				// timeout each time the cache data is used.  
				// We want an absolute timeout of 20 minutes from now.
                //
				System.Web.HttpRuntime.Cache.Insert(key, resp, null, GetNextMinutes(20), System.Web.Caching.Cache.NoSlidingExpiration);
			}

            private static DateTime GetNextMinutes(int minutes)
            {
                // Get the current time, add on the desired number of minutes, minimum is 1 minute.
                DateTime now = DateTime.Now.AddMinutes((double)(minutes <= 0 ? 1 : minutes));
                DateTime next = new DateTime(now.Year, now.Month, now.Day, now.Hour, now.Minute, 0, 0);
                return next;
            }
        }
		
        protected void writeResponse(string sResponse)
        {
            //
            // Write the formatted response, 
            // Final flush and close connection to client
            //
            try
            {
                Context.Response.Write(sResponse);
                Context.Response.Flush();
                Context.Response.Close();
            }
            catch (System.Threading.ThreadAbortException ex)
            {
                Console.WriteLine("Thread - caught ThreadAbortException." + ex);
            }
        }
		
		//
		// Test Driver Methods
		//

        [WebMethod]
        public void MastNameLookup(string input, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
			r.service = "Mast.Name.Lookup";
			r.clearcache = clearcache;
			r.paramss["input"] = input;
			
			invoke(r.ToJson());
        }

        [WebMethod]
        public void VoGalexCone(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
			r.service = "Vo.Caom.Tap";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoHesarcDatascope(string ra, string dec, string radius, string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
        public void MastGalexSql(string input, string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
			r.service = "Mast.Galex.Sql";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			r.paramss["input"] = input;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastHlspProject(string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
			r.service = "Mast.Hlsp.Project";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastHlspProducts(string id, string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
			ServiceRequest r = new ServiceRequest();
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
        public void CaomConeVotable(string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
			r.service = "Caom.Cone.Votable";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
		
		[WebMethod]
        public void GalexSiapVotable(string format, string timeout, string clearcache)
        {
			ServiceRequest r = new ServiceRequest();
			r.service = "Galex.Siap.Votable";
			r.format = format;
			r.timeout = timeout;
			r.clearcache = clearcache;
			
			invoke(r.ToJson());
        }
    }
}