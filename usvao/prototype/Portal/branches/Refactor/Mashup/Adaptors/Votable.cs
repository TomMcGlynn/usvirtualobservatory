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

using Mashup.Config;

namespace Mashup.Adaptors
{
    [Serializable]
    public class Votable : IAsyncAdaptor
    {
        public String url {get; set;}
		public String encode {get; set;}

        public Votable() 
        {
            url = "";
			encode = "false";
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(MashupRequest muRequest, MashupResponse muResponse)
        {			
			//
			// Replace every [PARAM] in the URL with it's equivalent muRequest param value
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, muRequest.paramss, Boolean.Parse(encode));
			
			//
			// Invoke the new URL and Transform the result VoTable into a DataSet
			//
			Stream s =  Utilities.Web.getWebReponseStream(sUrl);
			
			//
			// IMPORTANT NOTE: 
			// We explicitly set the Decoder to use the DecoderReplacementFallback character '#'
			// Under .NET an undecodable character would throw an Exception.
			//
			Encoding encoding = Encoding.GetEncoding("us-ascii",
              new EncoderReplacementFallback(), 
              new DecoderReplacementFallback("#"));

			StreamReader sr = new StreamReader(s, encoding);
			XmlTextReader reader = new XmlTextReader(sr);

			DataSet ds = Utilities.Transform.VoTableToDataSet(reader);
			
			//
			// Retreive Column Definitions for the Service and append them to the DataSet Column 'Extended Properties'
			//
			Dictionary<string, object> props = ColumnsConfig.Instance.getColumnProperties(muRequest);
			if (props != null && props.Count > 0)
			{
				Utilities.Transform.AppendColumnProperties(ds, props, ColumnsConfig.CC_PREFIX);
			}
		
			//
			// Load the muResponse data
			//
            muResponse.load(ds);
        }
    }
}
