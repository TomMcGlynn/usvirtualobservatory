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

namespace Mashup.Adaptors
{
    [Serializable]
    public class Votable : IAsyncAdaptor
    {
        public String url {get; set;}

        public Votable() 
        {
            url = "";
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(ServiceRequest request, ServiceResponse response)
        {			
			//
			// Replace every [PARAM] in the URL with it's equivalent request param value
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, request.paramss);
			
			//
			// Invoke the new URL and Transform the result VoTable into a DataSet
			//		
			Stream s =  Utilities.Web.getWebReponseStream(sUrl);
			XmlTextReader reader = new XmlTextReader(s); 
			DataSet ds = Utilities.Transform.VoTableToDataSet(reader);
		
			//
			// Load the response data
			//
            response.load(ds);
        }
    }
}
