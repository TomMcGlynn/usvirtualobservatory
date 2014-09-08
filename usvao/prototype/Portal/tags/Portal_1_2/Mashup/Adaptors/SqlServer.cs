using System;
using System.Data;
using System.Web;
using System.Xml;
using System.Text;
using System.Threading;
using System.Collections;
using System.Collections.Generic;

using Mashup.Config;
using Utilities;

namespace Mashup.Adaptors
{
	[Serializable]
	public class SqlServer : IAsyncAdaptor
	{
	    public String db {get; set;}
		public String sql {get; set;}
		public String direct {get; set;}
		
	    public SqlServer() 
	    {
	        db = "";
			sql = "";
			direct = "false";
	    }
	
		//
		// IAdaptor::invoke()
		//
	    public void invoke(MashupRequest muRequest, MashupResponse muResponse)
	    {					
			//
			// Replace every [PARAM] in the QUERY string with it's equivalent ServiceRequest Param
			//
			string sSql = Utilities.ParamString.replaceAllParams(sql, muRequest.paramss);		
			
			//
			// Run the New Query on the Database to extract resulting DataSet
			//		
			DataSet ds = new Utilities.Database(db, 
			                                    sSql, 
			                                    Convert.ToBoolean(direct)).getDataSet();
			
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