using System;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Web;
using System.Web.UI.WebControls;
using System.Xml;
using System.Text; 
using System.Threading;

namespace Utilities
{
	public class Web
	{
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
	
				Console.WriteLine("[WEB POST] " + url);
				Console.WriteLine("[URL PARAMS] " + urlEncodedParams);
	            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);
	            req.Method = "POST";
	            req.ContentType = "application/x-www-form-urlencoded";
	            req.ContentLength = data.Length;
				
	            Stream reqStream = req.GetRequestStream();
	            reqStream.Write(data, 0, data.Length);
	            reqStream.Close();
				
				resp = (HttpWebResponse)req.GetResponse();
			}
			catch (WebException wex)
            {
                //
                // Extract the Response Html and check if the Exception was thrown due failed lookup
                //
                if (wex.Response != null && wex.Response.ContentLength > 0)
                {
                    resp = (HttpWebResponse)wex.Response;
                    StreamReader reader = new StreamReader(resp.GetResponseStream(), System.Text.Encoding.ASCII);
                    string sResponse = reader.ReadToEnd();

                    // We only send out an email notification if exception is NOT due to a failed lookup
                    if (sResponse.IndexOf("NoSearchMatchException") < 0)
                    {
                        Exception ex = wex as Exception;
                        Utilities.Mail.sendException(ref ex, sResponse);
                    }
                }
                else
                {
                    Exception ex = wex as Exception;
                    Utilities.Mail.sendException(ref ex);
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
		
		#region getWebResponse
        public static HttpWebResponse getWebResponse(string url)
        {
			int attempts=0;
			int timeout=1;
            HttpWebResponse resp = null;
			
			//
			// Try 3 times to create a valid Web Response from Input URL
			//
			do
			{
	            try 
	            {
					Thread.Sleep(timeout); timeout=1000;
					Console.WriteLine("[WEB GET] " + url);
					HttpWebRequest req = (HttpWebRequest)WebRequest.Create(url);             
	                resp = (HttpWebResponse)req.GetResponse();
	            } 
	            catch (WebException wex)
	            {
	                //
	                // Extract the Response Html and check if the Exception was thrown due failed lookup
	                //
	                if (wex.Response != null && wex.Response.ContentLength > 0)
	                {
	                    resp = (HttpWebResponse)wex.Response;
	                    StreamReader reader = new StreamReader(resp.GetResponseStream(), System.Text.Encoding.ASCII);
	                    string sResponse = reader.ReadToEnd();
	
	                    // We only send out an email notification if exception is NOT due to a failed lookup
	                    if (sResponse.IndexOf("NoSearchMatchException") < 0)
	                    {
	                        Exception ex = wex as Exception;
	                        Utilities.Mail.sendException(ref ex, sResponse);
	                    }
	                }
	                else
	                {
	                    Exception ex = wex as Exception;
	                    Utilities.Mail.sendException(ref ex);
	                }
				}
			} while (resp == null && attempts++ < 3);
			
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

