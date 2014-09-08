
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Net.Security;
using System.Web;
using System.Text;
using System.Threading;
using System.Collections.Generic;
using System.Security.Cryptography.X509Certificates;

using log4net;

namespace Utilities
{
	public class AsyncRequest : Dictionary<string,object>
	{

		// Log4Net Stuff
		public static readonly ILog log = LogManager.GetLogger (System.Reflection.MethodBase.GetCurrentMethod ().DeclaringType);

		public static string tid { get { return String.Format ("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] "; } }
		
		private HttpWebRequest request = null;
		public HttpWebResponse response = null;
		public Stream responseStream = null;
		public ManualResetEvent allDone = new ManualResetEvent (false);
		
		// Use this for storing the user specified data
		public String name { get; set; }   // Not functionally important, but useful for logging and debugging
		public String requestUrl { get; set; }

		public AsyncCallback userCallback { get; set; }

		public int timeoutMs { get; set; }  // value < 0 ==> no timeout
		
		// status
		public String status = PENDING;
		public bool timedOut = false;
		public const string PENDING = "PENDING";
		public const string EXECUTING = "EXECUTING";
		public const string COMPLETE = "COMPLETE";
		public const string FAILED = "FAILED";
		public const string ERROR = "ERROR";
		
		// These would only be used for aynsynchronous reading of the response stream:
		const int BUFFER_SIZE = 1024;
		public byte[] BufferRead = null;
		public StringBuilder requestData = null;
		
		#region CertificateValidator
		public static bool CertificateValidator (object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
		{
			log.Info (tid + "     [WEB GET] Certificate Validator: Returning true for url: " + (sender as HttpWebRequest).Address);
			return true;
		}
		#endregion

		// Constructor
		public AsyncRequest (String iName, String iRequestUrl, AsyncCallback iUserCallback, int iTimeoutMs) : base()
		{
			name = iName;
			requestUrl = iRequestUrl;
			userCallback = iUserCallback;
			timeoutMs = iTimeoutMs;

			// Create a HttpWebrequest object to the desired URL. 
			ServicePointManager.ServerCertificateValidationCallback = CertificateValidator;  // Don't know if we need this
			request = (HttpWebRequest)WebRequest.Create (requestUrl);		
		}

		#region PublicMethods
		public void start ()
		{

			log.Info (tid + "---> [ASYNC REQUEST START] " + requestUrl);

			try {			// Start the asynchronous request.
				status = EXECUTING;
				IAsyncResult result = request.BeginGetResponse (new AsyncCallback (callbackProxy), this);
			
				// this line implements the timeout (if any). If a timeout occurs, the callback fires and the request becomes aborted.
				if (timeoutMs > -1) {
					ThreadPool.RegisterWaitForSingleObject (result.AsyncWaitHandle, new WaitOrTimerCallback (timeoutCallback), request, timeoutMs, true);
				}

			} catch (Exception e) {
				log.Error ("Exception in AsyncRequest.start(), url = " + requestUrl + "\n" + e.Message);
			}

		}

		public Object getValue (string key)
		{
			object val;
			if (base.TryGetValue (key, out val)) {
				return val;
			} else {
				return null;
			}
		}
		#endregion

		#region PrivateMethods

		private void callbackProxy (IAsyncResult asynchronousResult)
		{
			log.Debug (tid + "---> [ASYNC REQUEST (enter cb proxy)] " + requestUrl);

			if (!timedOut) {
				status = COMPLETE;
			
				response = (HttpWebResponse)request.EndGetResponse (asynchronousResult);
				responseStream = response.GetResponseStream ();
			}

			// Call the user's callback.
			// (We could help out here by allowing simpler callbacks that only get the 
			// stream or full text of the response, but let's wait to see if that would 
			// actually be useful.)
			userCallback.Invoke (asynchronousResult);

			log.Debug (tid + "---> [ASYNC REQUEST (exit cb proxy)] " + requestUrl);
		}
		
		// Abort the request if the timer fires.
		private void timeoutCallback (object state, bool iTimedOut)
		{ 
			log.Debug (tid + "---> [ASYNC REQUEST (timeout cb)] " + iTimedOut + ": " + requestUrl);
			// Figure out how this affects the real callback.  How do we mark the status?
			if (iTimedOut) {
				timedOut = true;
				status = FAILED;
				request.Abort ();
			}
		}
		#endregion

	}
}

