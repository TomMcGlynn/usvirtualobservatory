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

namespace Mashup.Adaptors
{	
    [Serializable]
    public class HttpProxy : ISyncAdaptor
    {
		const int BLOCK_SIZE = 8192;

        public String url {get; set;}

        public HttpProxy()
        {
            url = "";
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(ServiceRequest request, HttpResponse httpResponse)
        {		
			//
			// Replace every [PARAM] in the URL with it's equivalent request param value
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, request.paramss);
			
			//
			// Invoke the new URL 
			//		
			System.Net.HttpWebResponse webResponse = Utilities.Web.getWebResponse(sUrl);
			
			//
			// Write the Response Directly back to the client
			//
			
			// Ensure that the output content type will match the content type we're reading.
			httpResponse.ContentType = webResponse.ContentType;
			
			// Relay the content one block at a time.
			StreamReader reader = new StreamReader(webResponse.GetResponseStream(), System.Text.Encoding.ASCII);
			char[] block = new char[BLOCK_SIZE];
			while (!reader.EndOfStream) 
			{
				int bytesRead = reader.ReadBlock(block, 0, BLOCK_SIZE);
				httpResponse.Write(block, 0, bytesRead);	
		        httpResponse.Flush();     
			}
			httpResponse.Close();
        }
    }
}
