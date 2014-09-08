//using System;
//using System.Collections;
//using System.ComponentModel;
//using System.Diagnostics;
//using System.IO;
//using System.Net;
//using System.Net.Sockets;
//using System.Net.Security;
//using System.Reflection;
//using System.Web;
//using System.Web.UI.WebControls;
//using System.Xml;
//using System.Text; 
//using System.Threading;
//using System.Security.Cryptography.X509Certificates;
//using log4net;
//
//namespace Utilities
//{
//	public class AsyncWeb
//	{
//		//
//		// Log4Net Stuff
//		//
//		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
//		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
//
//		public AsyncWeb ()
//		{
//		}
//
//		public static void Main (string[] args)
//		{
//			WebResponse resp = null;
//			WebRequest req = null;
//			string url = "http://masttest.stsci.edu/robots.txt";
//			
//			try 
//			{
//				req = WebRequest.Create(url);  
//				AsyncCallback cb = new AsyncCallback(ResponseCallback);
//				RequestState state = new RequestState();
//				System.IAsyncResult asyncResult = req.BeginGetResponse(cb, state);
//			}
//			catch (WebException wex)
//			{
//				// NOTE: If we catch a Trust Failure Exception, we log it and continue on.
//				// This should not happen because our CertificatValidator returns true.
//				if (wex.Status == WebExceptionStatus.TrustFailure)
//				{
//					log.Error(tid + "<--- [WEB GET] Caught Web Exception Trust Failure for url: " + url, wex);
//				}
//				else
//				{
//					log.Error(tid + "<--- [WEB GET] " + url);
//					throw (wex);
//				}
//			}
//
//		}
//
//		static void ResponseCallback(IAsyncResult result) 
//		{
//
//		}
//	}
//
//	public class RequestState
//	{
//		// This class stores the State of the request.
//		const int BUFFER_SIZE = 1024;
//		public StringBuilder requestData;
//		public byte[] BufferRead;
//		public HttpWebRequest request;
//		public HttpWebResponse response;
//		public Stream streamResponse;
//		public RequestState()
//		{
//			BufferRead = new byte[BUFFER_SIZE];
//			requestData = new StringBuilder("");
//			request = null;
//			streamResponse = null;
//		}
//	}
//}
using System;
using System.Net; 
using System.IO;
using System.Text;
using System.Threading;
using System.Configuration;
using System.Collections;

using Utilities;
using log4net;

// Configure log4net using the .config file
[assembly: log4net.Config.XmlConfigurator(Watch=true)]
// This will cause log4net to look for a configuration file
// called ConsoleApp.exe.config in the application base
// directory (i.e. the directory containing ConsoleApp.exe)

namespace VOTTest
{
	class RequestState
	{
		// This class stores the State of the request.
		const int BUFFER_SIZE = 1024;
		public StringBuilder requestData;
		public byte[] BufferRead;
		public HttpWebRequest request;
		public HttpWebResponse response;
		public Stream streamResponse;
		
		public RequestState ()
		{
			BufferRead = new byte[BUFFER_SIZE];
			requestData = new StringBuilder ("");
			request = null;
			streamResponse = null;
		}
	}
	
	public class AsyncWebTest
	{
		//
		// Log4Net Stuff
		//
		public static readonly ILog log = LogManager.GetLogger (System.Reflection.MethodBase.GetCurrentMethod ().DeclaringType);
		
		public static string tid { get { return String.Format ("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] "; } }
		
		public static ManualResetEvent allDone = new ManualResetEvent (false);
		const int BUFFER_SIZE = 1024;
		const int DefaultTimeout = 2 * 60 * 1000; // 2 minutes timeout
		
		// Abort the request if the timer fires.
		private static void TimeoutCallback (object state, bool timedOut)
		{ 
			if (timedOut) {
				HttpWebRequest request = state as HttpWebRequest;
				if (request != null) {
					request.Abort ();
				}
			}
		}
		
		#region MyStuff
		
		const string URL_HLA_DELAY_5 = "http://mastdev.stsci.edu/portal/Mashup/Mashup.asmx/invoke?request={%22service%22%3A%22Hla.SIA.Votable%22%2C%22format%22%3A%22extjs%22%2C%22timeout%22%3A10000%2C%22sleep%22%3A5000}";
		
		static void Main ()
		{  
			
			try {
				AsyncRequest ar = new AsyncRequest ("Test Query", URL_HLA_DELAY_5, new AsyncCallback (queryCallback), 500);
				ar.start();

				log.Debug (tid + "---> [wait for allDone]");
				ar.allDone.WaitOne ();
				log.Debug (tid + "---> [end wait for allDone, exiting]");
			} catch (WebException e) {
				Console.WriteLine ("\nMain Exception raised!");
				Console.WriteLine ("\nMessage:{0}", e.Message);
				Console.WriteLine ("\nStatus:{0}", e.Status);
				Console.WriteLine ("Press any key to continue..........");
			} catch (Exception e) {
				Console.WriteLine ("\nMain Exception raised!");
				Console.WriteLine ("Source :{0} ", e.Source);
				Console.WriteLine ("Message :{0} ", e.Message);
				Console.WriteLine ("Press any key to continue..........");
				Console.Read ();
			}
		}
		
		private static void queryCallback (IAsyncResult asynchronousResult)
		{
			// The context has all the info we could ever want.  
			AsyncRequest ar = (AsyncRequest)asynchronousResult.AsyncState;

			if (ar.status.Equals (AsyncRequest.COMPLETE)) {

				// Put the whole response in a string.
				StreamReader reader = new StreamReader (ar.responseStream);
				string responseString = reader.ReadToEnd ();
				log.Debug (tid + "---> [user cb] Reponse is:" + responseString.Substring (0, System.Math.Min (50, responseString.Length)));
			
				// Clean up
				reader.Close ();
				ar.response.Close ();
			} else {
				log.Debug (tid + "---> [user cb] Timeout!!");
			}
			ar.allDone.Set ();
		}
		
		
#endregion
		
		static void MainOrig ()
		{  
			
			try {
				// Create a HttpWebrequest object to the desired URL. 
				HttpWebRequest myHttpWebRequest = (HttpWebRequest)WebRequest.Create ("http://masttest.stsci.edu/robots.txt");
				
				
				/**
			    * If you are behind a firewall and you do not have your browser proxy setup
			    * you need to use the following proxy creation code.

			      // Create a proxy object.
			      WebProxy myProxy = new WebProxy();

			      // Associate a new Uri object to the _wProxy object, using the proxy address
			      // selected by the user.
			      myProxy.Address = new Uri("http://myproxy");


			      // Finally, initialize the Web request object proxy property with the _wProxy
			      // object.
			      myHttpWebRequest.Proxy=myProxy;
			    ***/
				
				// Create an instance of the RequestState and assign the previous myHttpWebRequest
				// object to its request field.  
				RequestState myRequestState = new RequestState ();  
				myRequestState.request = myHttpWebRequest;
				
				
				// Start the asynchronous request.
				IAsyncResult result =
					(IAsyncResult)myHttpWebRequest.BeginGetResponse (new AsyncCallback (RespCallbackChunk), myRequestState);
				
				// this line implements the timeout, if there is a timeout, the callback fires and the request becomes aborted
				ThreadPool.RegisterWaitForSingleObject (result.AsyncWaitHandle, new WaitOrTimerCallback (TimeoutCallback), myHttpWebRequest, DefaultTimeout, true);
				
				// The response came in the allowed time. The work processing will happen in the 
				// callback function.
				allDone.WaitOne ();
				
				// Release the HttpWebResponse resource.
				myRequestState.response.Close ();
			} catch (WebException e) {
				Console.WriteLine ("\nMain Exception raised!");
				Console.WriteLine ("\nMessage:{0}", e.Message);
				Console.WriteLine ("\nStatus:{0}", e.Status);
				Console.WriteLine ("Press any key to continue..........");
			} catch (Exception e) {
				Console.WriteLine ("\nMain Exception raised!");
				Console.WriteLine ("Source :{0} ", e.Source);
				Console.WriteLine ("Message :{0} ", e.Message);
				Console.WriteLine ("Press any key to continue..........");
				Console.Read ();
			}
		}
		
		private static void RespCallback (IAsyncResult asynchronousResult)
		{  
			try {
				// State of request is asynchronous.
				RequestState myRequestState = (RequestState)asynchronousResult.AsyncState;
				HttpWebRequest myHttpWebRequest = myRequestState.request;
				myRequestState.response = (HttpWebResponse)myHttpWebRequest.EndGetResponse (asynchronousResult);
				
				// Read the response into a Stream object.
				Stream responseStream = myRequestState.response.GetResponseStream ();
				myRequestState.streamResponse = responseStream;
				
				// Begin the Reading of the contents of the HTML page and print it to the console.
				//IAsyncResult asynchronousInputRead = 
					responseStream.BeginRead (myRequestState.BufferRead, 0, BUFFER_SIZE, new AsyncCallback (ReadCallBack), myRequestState);
				return;
			} catch (WebException e) {
				Console.WriteLine ("\nRespCallback Exception raised!");
				Console.WriteLine ("\nMessage:{0}", e.Message);
				Console.WriteLine ("\nStatus:{0}", e.Status);
			}
			allDone.Set ();
		}
		
		private static void RespCallbackChunk (IAsyncResult asynchronousResult)
		{  
			try {
				// State of request is asynchronous.
				RequestState myRequestState = (RequestState)asynchronousResult.AsyncState;
				HttpWebRequest myHttpWebRequest = myRequestState.request;
				myRequestState.response = (HttpWebResponse)myHttpWebRequest.EndGetResponse (asynchronousResult);
				
				// Read the response into a Stream object.
				Stream responseStream = myRequestState.response.GetResponseStream ();
				myRequestState.streamResponse = responseStream;
				
				// Begin the Reading of the contents of the HTML page and print it to the console.
				//IAsyncResult asynchronousInputRead = 
					responseStream.BeginRead (myRequestState.BufferRead, 0, BUFFER_SIZE, new AsyncCallback (ReadCallBack), myRequestState);
				return;
			} catch (WebException e) {
				Console.WriteLine ("\nRespCallbackChunk Exception raised!");
				Console.WriteLine ("\nMessage:{0}", e.Message);
				Console.WriteLine ("\nStatus:{0}", e.Status);
			}
			allDone.Set ();
		}
		
		private static  void ReadCallBack (IAsyncResult asyncResult)
		{
			try {
				
				RequestState myRequestState = (RequestState)asyncResult.AsyncState;
				Stream responseStream = myRequestState.streamResponse;
				int read = responseStream.EndRead (asyncResult);
				// Read the HTML page and then print it to the console.
				if (read > 0) {
					myRequestState.requestData.Append (Encoding.ASCII.GetString (myRequestState.BufferRead, 0, read));
					//IAsyncResult asynchronousResult = 
						responseStream.BeginRead (myRequestState.BufferRead, 0, BUFFER_SIZE, new AsyncCallback (ReadCallBack), myRequestState);
					return;
				} else {
					Console.WriteLine ("\nThe contents of the Html page are : ");
					if (myRequestState.requestData.Length > 1) {
						string stringContent;
						stringContent = myRequestState.requestData.ToString ();
						Console.WriteLine (stringContent);
					}
					Console.WriteLine ("Press any key to continue..........");
					Console.ReadLine ();
					
					responseStream.Close ();
				}
				
			} catch (WebException e) {
				Console.WriteLine ("\nReadCallBack Exception raised!");
				Console.WriteLine ("\nMessage:{0}", e.Message);
				Console.WriteLine ("\nStatus:{0}", e.Status);
			}
			allDone.Set ();
			
		}
	}
}

