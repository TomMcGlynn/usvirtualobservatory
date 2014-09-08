using System;
using System.Data;
using System.Configuration;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Xml.Linq;
using System.Diagnostics;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Text;
using System.Threading;

using log4net;

namespace Mashup.Adaptors
{	
    [Serializable]
    public class HttpProxy : ISyncAdaptor
    {
		//
		// Logger Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		const int BLOCK_SIZE = 100*1024;		// 100K chunks.

        public String url {get; set;}
		public String encode {get; set;}

        public HttpProxy()
        {
            url = "";
			encode = "false";
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(MashupRequest muRequest, HttpResponse httpResponse)
        {		
			//
			// Replace every [PARAM] in the URL with it's equivalent muRequest param value
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, muRequest.paramss, Boolean.Parse(encode));
			
			//
			// Invoke the new URL 
			//		
			System.Net.WebResponse webResponse = Utilities.Web.getWebResponse(sUrl);
			
			// Ensure that the output content type will match the content type we're reading.
			httpResponse.ContentType = webResponse.ContentType;
			
			// Relay the content one block at a time back to the client.
			int totalLength = 0;
			BinaryReader reader = new BinaryReader(webResponse.GetResponseStream(), System.Text.Encoding.ASCII);
			byte[] block = new byte[BLOCK_SIZE];
			int bytesRead = 0;
			while ((bytesRead = reader.Read(block, 0, BLOCK_SIZE)) > 0) 
			{
				httpResponse.OutputStream.Write(block, 0, bytesRead);	
				totalLength += bytesRead;
				httpResponse.Flush();
			}
			
			log.Info(tid + "<=== " + "[HTTP_PROXY] Complete. url: " + sUrl + " length: " + totalLength);
        }
    }
}
