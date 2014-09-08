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
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		private Web ()
		{
			// Not ment for instantiation - just a collection of Web Utility Methods
		}
		
		#region postWebRequest
		public static HttpWebResponse postWebResponse(string url, string urlEncodedParams)
        {
			HttpWebResponse resp = null;
			
			try
			{
	            ASCIIEncoding encoding = new ASCIIEncoding();
	            byte[] data = encoding.GetBytes(urlEncodedParams);
	
				log.Info(tid + "---> [WEB POST] " + url);
				log.Info(tid + "---> [URL PARAMS] " + urlEncodedParams);
				ServicePointManager.ServerCertificateValidationCallback = CertificateValidator;
	            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
	            req.Method = "POST";
	            req.ContentType = "application/x-www-form-urlencoded";
	            req.ContentLength = data.Length;
				
	            Stream reqStream = req.GetRequestStream();
	            reqStream.Write(data, 0, data.Length);
	            reqStream.Close();
				
				resp = (HttpWebResponse)req.GetResponse();
				log.Info(tid + "<--- [WEB POST] " + url + " status:" + resp.StatusCode + " content-length:" + resp.ContentLength);
			}
			catch (WebException wex)
            {
				if (wex.Status == WebExceptionStatus.TrustFailure)
				{
					// NOTE: If we catch a Trust Failure Exception, we log it and continue on.
					// This should not happen because our CertificatValidator returns true.
					log.Error(tid + "<--- [WEB POST] Caught Web Exception Trust Failure for url: " + url, wex);
				}
				else
				{
					log.Error(tid + "<--- [WEB POST] url: " + url);
					throw (wex);
				}
			}
            return resp;
        }
		#endregion
		
		#region GetWebResponseStream
		public static Stream postWebReponseStream(string url, string urlEncodedParams) 
        {
			HttpWebResponse resp = postWebResponse(url, urlEncodedParams);
			Stream stream = null;
            if (resp != null)
			{
                 stream = resp.GetResponseStream();  
            }
            return stream;
        }
		#endregion
		
		#region getWebResponseString
        public static String postWebResponseString(string url, string urlEncodedParams)
        {
            HttpWebResponse resp = postWebResponse(url, urlEncodedParams);
			var sResponse = "";
            if (resp != null)
            {
                StreamReader reader = new StreamReader(resp.GetResponseStream(), System.Text.Encoding.ASCII);
                sResponse = reader.ReadToEnd();
            }
            return sResponse;
        }
        #endregion
		
		#region CertificateValidator
		public static bool CertificateValidator (object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors)
		{
			log.Info(tid + "     [WEB GET] Certificate Validator: Returning true for url: " + (sender as HttpWebRequest).Address);
			return true;
		}
		#endregion
		
		#region getWebResponse
        public static HttpWebResponse getWebResponse(string url)
        {
            HttpWebResponse resp = null;

			try 
            {
				log.Info(tid + "---> [WEB GET] " + url);
				ServicePointManager.ServerCertificateValidationCallback = CertificateValidator;
				HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);    
                resp = (HttpWebResponse)req.GetResponse();
				log.Info(tid + "<--- [WEB GET] " + url + " status:" + resp.StatusCode + " content-length:" + resp.ContentLength);
            } 
            catch (WebException wex)
            {
				// NOTE: If we catch a Trust Failure Exception, we log it and continue on.
				// This should not happen because our CertificatValidator returns true.
				if (wex.Status == WebExceptionStatus.TrustFailure)
				{
					log.Error(tid + "<--- [WEB GET] Caught Web Exception Trust Failure for url: " + url, wex);
				}
				else
				{
					log.Error(tid + "<--- [WEB GET] " + url);
					throw (wex);
				}
			}
			
            return resp;
        }
        #endregion
		
		#region GetWebResponseStream
		public static Stream getWebReponseStream(string url) 
        {
			HttpWebResponse resp = getWebResponse(url);
			Stream stream = null;
            if (resp != null)
			{
                 stream = resp.GetResponseStream();  
            }
            return stream;
        }
		#endregion
		
		#region getWebResponseString
        public static String getWebResponseString(string url)
        {
			var sResponse = "";
	        HttpWebResponse resp = getWebResponse(url);
				
            if (resp != null)
            {
                StreamReader reader = new StreamReader(resp.GetResponseStream(), System.Text.Encoding.ASCII);
                sResponse = reader.ReadToEnd();
            }
			
            return sResponse;
        }
        #endregion
	}
}

