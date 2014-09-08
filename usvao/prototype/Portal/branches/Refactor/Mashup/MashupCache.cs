using System;

using log4net;

using Utilities;

namespace Mashup
{
	public class MashupCache
	{
		//
		// Logger Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		//
		// This class is a collection of static methods and NOT intended to be Instantiated.
		//
		private MashupCache ()
		{
		}
			
		public static readonly int DEFAULT_CACHE_TIMEOUT_MINUTES = 20;
		static readonly string CACHE_TIMEOUT_MINUTES_KEY = "CacheTimeoutMinutes";
		static string sCacheTimeoutMinutes = System.Configuration.ConfigurationManager.AppSettings.Get(CACHE_TIMEOUT_MINUTES_KEY);
		
		public static void removeMashupResponse(String key)
		{
			lock (typeof(MashupCache))
        	{
				System.Web.HttpRuntime.Cache.Remove(key);
				Info("REMOVE", key);
			} // lock()
		}
		
        public static MashupResponse getMashupResponse(String key)
        {
			MashupResponse muResponse;
			lock (typeof(MashupCache))
        	{
                muResponse = (MashupResponse)System.Web.HttpRuntime.Cache[key];
				if (muResponse != null) 
				{
					Info("HIT " + (muResponse.isActive ? "[Active]" : "[Inactive]"), key);
				}
			} // lock()

            return muResponse;
        }
		
		public static void insertMashupResponse(String key, MashupResponse muResponse)
		{
			lock (typeof(MashupCache))
        	{
				MashupResponse response = (MashupResponse)System.Web.HttpRuntime.Cache[key];
				if (response == null)
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
														MashupCache.onExpiredCallback);
					
					Info("INSERT", key);
				}
				else
				{
					string msg = "     [CACHE] Response Object is already in Mashup Cache." + response.Debug();
					Mashup.log.Error(tid + msg);
					Mail.sendError("[CACHE] Response Object is already in Mashup Cache.", DateTime.Now, msg, false);
				}
			} // lock()
		}
		
		public static void onExpiredCallback(String key, object response, System.Web.Caching.CacheItemRemovedReason reason)
	    {
			lock (typeof(MashupCache))
        	{
				Info("EXPIRED", key);

				if (response is MashupResponse)
				{
					MashupResponse muResponse = response as MashupResponse;
					if (muResponse.isActive)
					{
						string msg = "     [CACHE] Expired Response Object is still Active. Terminating Thread. Response = " + muResponse.Debug();
						Mashup.log.Error(tid + msg);
						Mail.sendError("[CACHE] Expired Response Object Thread is Still Active.", DateTime.Now, msg, false);
						muResponse.abort();
					}
				}
			} // lock()
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
			log.Debug(tid + "     [CACHE] " + state + " count = " + System.Web.HttpRuntime.Cache.Count + ", key = " + key);
		}
		
		public static void Info(string state, string key)
		{
			log.Info(tid + "     [CACHE] " + state + " count = " + System.Web.HttpRuntime.Cache.Count + ", key = " + key );
		} 
	}
}