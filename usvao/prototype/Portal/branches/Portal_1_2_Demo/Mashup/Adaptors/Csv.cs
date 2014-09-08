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
using LumenWorks.Framework.IO.Csv;

using Mashup.Config;

namespace Mashup.Adaptors
{
    [Serializable]
    public class Csv : IAsyncAdaptor
    {
        public String url {get; set;}

        public Csv() 
        {
            url = "";
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(MashupRequest muRequest, MashupResponse muResponse)
        {			
			//
			// Replace every [PARAM] in the URL with it's equivalent muRequest param value
			//
			string sUrl = Utilities.ParamString.replaceAllParams(url, muRequest.paramss);
			
			//
			// Invoke the new URL and Transform the result VoTable into a DataSet
			//		
			Stream s =  Utilities.Web.getWebReponseStream(sUrl);
			StreamReader streamReader = new StreamReader(s);
			CsvReader csvReader = new CsvReader(streamReader, true, ',');
			DataTable dt = new DataTable("CSVImportTable");
			dt.Load(csvReader);
			DataSet ds = new DataSet("CSVImportSet");
			ds.Tables.Add(dt);
			
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

