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
			object data = muRequest.data;
			if (data != null)
			{	
				if (data is Dictionary<string, object>)
				{
					Dictionary<string, object> dict = (data as Dictionary<string, object>);
					ds = Utilities.Transform.ExtJsDictionaryToDataSet(dict);	
				}
				else if (data is string)
				{
					string json = data as string;
					ds = Utilities.Transform.ExtJsToDataSet(json);
				}
				else
				{
					throw new Exception ("Mashup Table Exporter: request.data is unexpected data type = " + data.GetType());
				}
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
