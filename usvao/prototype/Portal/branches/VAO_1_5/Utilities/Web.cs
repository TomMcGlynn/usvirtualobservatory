using System;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Net.Security;
using System.Reflection;
using System.Web;
using System.Web.UI.WebControls;
using System.Xml;
using System.Text; 
using System.Threading;
using System.Security.Cryptography.X509Certificates;

using log4net;

namespace Utilities
{
	public class Web
	{
		//
		// Log4Net Stuff
		//
		public static readonly ILog log = LogManager.GetLogger (System.Reflection.MethodBase.GetCurrentMethod ().DeclaringType);
		public static string tid { get { return String.Format ("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] "; } }

		const int DefaultTimeout = 2 * 60 * 1000; // 2 minutes timeout

		private Web ()
		{
			// Not meant for instantiation - just a collection of Web Utility Methods
		}
		
		#region postWebRequest
		public static HttpWebResponse postWebResponse (string url, string content, string contentType)
		{
			HttpWebResponse resp = null;
			
			try {
				ASCIIEncoding encoding = new ASCIIEncoding ();
				byte[] data = encoding.GetBytes (content);
	
				log.Info (tid + "---> [WEB POST] " + url + " [CONTENT] " + content);
				ServicePointManager.ServerCertificateValidationCallback = CertificateValidator;
				HttpWebRequest req = (HttpWebRequest)WebRequest.Create (url);
				req.Method = "POST";
				req.ContentType = contentType; // ( i.e: "application/json", "application/x-www-form-urlencoded")
				req.ContentLength = data.Length;

				Stream reqStream = req.GetRequestStream ();
				reqStream.Write (data, 0, data.Length);
				reqStream.Close ();
				
				resp = (HttpWebResponse)req.GetResponse ();
				log.Info (tid + "<--- [WEB POST] " + url + " status:" + resp.StatusCode + " content-length:" + resp.ContentLength);
			} catch (WebException wex) {
				if (wex.Status == WebExceptionStatus.TrustFailure) {
					// NOTE: If we catch a Trust Failure Exception, we log it and continue on.
					// This should not happen because our CertificatValidator returns true.
					log.Error (tid + "<--- [WEB POST] Caught Web Exception Trust Failure for url: " + url, wex);
				} else {
					log.Error (tid + "<--- [WEB POST] url: " + url);
					throw (wex);
				}
			}
			return resp;
		}
		#endregion
		
		#region GetWebResponseStream
		public static Stream postWebReponseStream (string url, string content, string contentType)
		{
			HttpWebResponse resp = postWebResponse (url, content, contentType);
			Stream stream = null;
			if (resp != null) {
				stream = resp.GetResponseStream ();  
			}
			return stream;
		}
		#endregion
		
		#region postWebResponseString
		public static String postWebResponseString (string url, string content, string contentType)
		{
			HttpWebResponse resp = postWebResponse (url, content, contentType);
			var sResponse = "";
			if (resp != null) {
				StreamReader reader = new StreamReader (resp.GetResponseStream (), System.Text.Encoding.ASCII);
				sResponse = reader.ReadToEnd ();
			}
			return sResponse;
		}
        #endregion
		
		#region CertificateValidator
		public static bool CertificateValidator (object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
		{
			log.Info (tid + "     [WEB GET] Certificate Validator: Returning true for url: " + (sender as HttpWebRequest).Address);
			return true;
		}
		#endregion
		
		#region getWebResponse
		public static WebResponse getWebResponse (string url)
		{
			WebResponse resp = null;

			try {
				log.Info (tid + "---> [WEB GET] " + url);
				ServicePointManager.ServerCertificateValidationCallback = CertificateValidator;
				// Handle direct file accesses
				if (url.StartsWith ("file://")) {
					String stripped = url.Replace ("file://", "");
					if (!url.StartsWith ("/")) {
						// Path was relative (meaning it's within the deployed directory tree), so give it an absolute path.
						url = System.IO.Path.GetFullPath (stripped);
					}

				}
				WebRequest req = (WebRequest)WebRequest.Create (url);    
				resp = req.GetResponse ();
				log.Info (tid + "<--- [WEB GET] " + url + " status:" + ((resp is HttpWebResponse) ? ((HttpWebResponse)resp).StatusCode : 0) + " content-length:" + resp.ContentLength);
			} catch (WebException wex) {
				// NOTE: If we catch a Trust Failure Exception, we log it and continue on.
				// This should not happen because our CertificatValidator returns true.
				if (wex.Status == WebExceptionStatus.TrustFailure) {
					log.Error (tid + "<--- [WEB GET] Caught Web Exception Trust Failure for url: " + url, wex);
				} else {
					log.Error (tid + "<--- [WEB GET] " + url);
					throw (wex);
				}
			}
			
			return resp;
		}
        #endregion
		
		#region GetWebResponseStream
		public static Stream getWebReponseStream (string url)
		{
			WebResponse resp = getWebResponse (url);
			Stream stream = null;
			if (resp != null) {
				stream = resp.GetResponseStream ();  
			}
			return stream;
		}
		#endregion
		
		#region getWebResponseString
		public static String getWebResponseString (string url)
		{
			var sResponse = "";
			WebResponse resp = getWebResponse (url);
				
			if (resp != null) {
				StreamReader reader = new StreamReader (resp.GetResponseStream (), System.Text.Encoding.ASCII);
				sResponse = reader.ReadToEnd ();
			}
			
			return sResponse;
		}
        #endregion

		#region async

		// iTimeoutMs of -1 means wait indefinitely.
		public static AsyncContext AsyncWebRequest (string iUrl, object iUserData, AsyncCallback iCallback, int iTimeoutMs)
		{
			log.Info (tid + "---> [WEB GET (async)] " + iUrl);
			ServicePointManager.ServerCertificateValidationCallback = CertificateValidator;

			// Build the context object that will be used to access all the info about this 
			// request throughout the request.
			AsyncContext context = new AsyncContext (iUrl, iUserData, iCallback, iTimeoutMs);

			// Create a HttpWebrequest object to the desired URL. 
			context.request = (HttpWebRequest)WebRequest.Create (context.url);

			// Start the asynchronous request.
			context.status = AsyncContext.EXECUTING;
			IAsyncResult result = context.request.BeginGetResponse (new AsyncCallback (asyncCbProxy), context);

			// this line implements the timeout (if any). If a timeout occurs, the callback fires and the request becomes aborted.
			if (iTimeoutMs > -1) {
				ThreadPool.RegisterWaitForSingleObject (result.AsyncWaitHandle, new WaitOrTimerCallback (asyncTimeoutCallback), context.request, context.timeoutMs, true);
			}
//			// The response came in the allowed time. The work processing will happen in the 
//			// callback function.
//			// DO THIS SOMEWHERE ELSE
//			allDone.WaitOne ();
//			
//			// Release the HttpWebResponse resource.
//			// DO THIS SOMEWHERE ELSE
//			myRequestState.response.Close ();


			return context;
		}

		private static void asyncCbProxy(IAsyncResult asynchronousResult)
		{
			AsyncContext context = (AsyncContext)asynchronousResult.AsyncState;
			context.status = AsyncContext.COMPLETE;
			log.Debug (tid + "---> [WEB GET (enter cb proxy)] " + context.url);

			context.response = (HttpWebResponse)context.request.EndGetResponse (asynchronousResult);
			context.responseStream = context.response.GetResponseStream ();

			// Call the user's callback.
			// (We could help out here by allowing simpler callbacks that only get the 
			// stream or full text of the response, but let's wait to see if that would 
			// actually be useful.)
			context.callback.Invoke(asynchronousResult);

			log.Debug (tid + "---> [WEB GET (exit cb proxy)] " + context.url);
		}

		// Abort the request if the timer fires.
		private static void asyncTimeoutCallback (object state, bool timedOut)
		{ 
			// Figure out how this affects the real callback.  How do we mark the status?
			if (timedOut) {
				HttpWebRequest request = state as HttpWebRequest;
				if (request != null) {
					request.Abort ();
				}
			}
		}

		#endregion
	}
	
}

