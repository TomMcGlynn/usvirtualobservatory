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
using System.Collections.Generic;
using System.Text;
using System.Diagnostics;
using fastJSON;

namespace Mashup
{
    [Serializable]
    public class ServiceRequest : Dictionary<string,object>
    {
		public HttpContext context;
		
		//
		// Public Properties
		//
        public string service { 
			get { return getString("service"); } set { base["service"] = value;}
		}
		
		public Dictionary<string, object> paramss  { 
			get { return getDictionary("params"); } set { base["params"] = value;}
		}
				
		public string format { 
			get { return getString("format"); } set { base["format"] = value;}
		}
		
		public string timeout { 
			get { return getString("timeout"); } set { base["timeout"] = value;}
		}
		
		public string clearcache { 
			get { return getString("clearcache"); } set { base["clearcache"] = value;}
		}
		
		private string getString (string key)
		{
			object val;
			if (base.TryGetValue(key, out val))
			{
				return val.ToString().Trim();
			}
			else
			{
				return null;
			}
		}
		
		//
		// key:
		// Accessor to a unique key that defines this Request Object based on input params
		// The returned key is actually a JSON Object of name-value-pairs in the paramss dictionary
		// This key is used for caching Response Objects in Web Cache.
		//
		protected StringBuilder _key = null;
		public string key {
			get {
				if (_key == null)
				{
					_key = new StringBuilder();
					_key.Append("{");
					_key.Append("\"service\" : \"" + service + "\", ");
					_key.Append("\"params\" : ");
					appendJsonDictionary(_key, paramss);
					_key.Append("}");
				}
				return _key.ToString();
			}
		}
		
		private Dictionary<string, object> getDictionary(string key)
		{
			object val;
			if (base.TryGetValue(key, out val) && val is Dictionary<string, object>)
			{
				return val as Dictionary<string, object>;
			}
			else
			{
				return null;
			}
		}		
		
		//
		// Constructors
		//	
        public ServiceRequest() : this(null, new Dictionary<string, object>())
		{
        }
		
        public ServiceRequest(HttpContext context, Dictionary<string, object> dict) : base(dict)
		{
			this.context = context;
			if (paramss == null)
			{
				paramss = new Dictionary<string, object>();
			}
        }
		
        public override string ToString()
        {
            var sResult = "";
			foreach (string key in base.Keys)
			{
				sResult += "  " + key + ":  " + base[key];
			}
            return sResult;
        }
		
		public string ToJson()
        {
			StringBuilder sb = new StringBuilder();
			this.appendJsonDictionary(sb, this);		
			string json = sb.ToString();
			return json;
        }
		
		protected void appendJsonDictionary(StringBuilder sb, Dictionary<string, object> dict)
		{
			sb.Append("{");
			
			int count=0;
			foreach(string key in dict.Keys)
			{
				// Extract the value for each key
				object val = "";
				if (dict.TryGetValue(key, out val) && val.ToString().Length > 0)
				{
					// Append [optional] comma and new-line from previous record.
					if (count > 0)
					{
						sb.Append(",");	
					}	
					
					// Check if value is another dictionary to traverse down.
					if (val is Dictionary<string,object>)
					{
						sb.Append("\"" + key + "\"" + ":");
						Dictionary<string, object> dval = val as Dictionary<string, object>;
						appendJsonDictionary(sb, dval);
						count++;
					}
					else // Append key/value pair
					{
						sb.Append("\"" + key + "\"" + ":" + "\"" + val + "\"");
						count++;
					}
				}
			}
			sb.Append("}");
		}
    }
}
