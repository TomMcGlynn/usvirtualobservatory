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

using JsonFx.Json;

using Mashup.Config;

namespace Mashup.Adaptors
{
    [Serializable]
    public class Exporter : IAsyncAdaptor
    {
		public String exportdir {get; set;}
        public String exporturl {get; set;}
		public String filename {get; set;}
        public String input {get; set;}
		public String format {get; set;}
		
        public Exporter() 
        {
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(MashupRequest muRequest, MashupResponse muResponse)
        {	
			DataSet ds = null;
			
			// Ensure the key data values are set correctly in the request
			if (muRequest.data != null && muRequest.data.Length > 0)
			{	
				ds = Utilities.Transform.JsonToDataSet(muRequest.data);	
			}
			else
			{				
				throw new Exception ("Mashup Table Exporter: request.data is empty.");
			}
			
			// Check for empty DataSet
			if (ds == null || ds.Tables == null || ds.Tables.Count == 0 || ds.Tables[0].Columns.Count == 0)
			{
				throw new Exception ("Mashup Table Exporter: Export from table Failed.");
			}
			
			//
			// Load the DataSet into the Response and let the Mashup Reponse Save it to disk
			//		
			muResponse.load(ds);			
        }
    }
}
