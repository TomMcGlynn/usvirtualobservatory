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
using JsonFx.Json;

namespace Mashup
{
    [Serializable]
    public class MashupRequest : Dictionary<string,object>
    {		
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
		
		public string data { 
			get { return getString("data"); } set { base["data"] = value;}
		}
		
		public string filename { 
			get { return getString("filename"); } set { base["filename"] = value;}
		}
		
		public Boolean filenameSet {
			get {
				return (this.filename != null && filename.Trim().Length > 0);
			}
		}
		
		public string timeout { 
			get { return getString("timeout"); } set { base["timeout"] = value;}
		}
		
		public static int DEFAULT_TIMEOUT_SECS = 20;
		
		public int timeoutMsecs {
			 get { return (this.timeout != null && this.timeout.Trim().Length > 0 ? 
					Convert.ToInt32(this.timeout) : 
					DEFAULT_TIMEOUT_SECS) * 1000; }
		}
		
		public int timeouSecs {
			 get { return (this.timeout != null && this.timeout.Trim().Length > 0 ? 
					Convert.ToInt32(this.timeout) : 
					DEFAULT_TIMEOUT_SECS); }
		}
		
		public string clearcache { 
			get { return getString("clearcache"); } set { base["clearcache"] = value;}
		}
		
		public Boolean clearcacheIsTrue {
			get {
				return (this.clearcache != null && Convert.ToBoolean(this.clearcache));
			}
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
		// The returned key is actually a JSON Object of name-value-pairs in the paramz dictionary
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
        public MashupRequest() : this(new Dictionary<string, object>())
		{
			// Invokes the Mashup(Dictionary) Constructor below
        }
		
		public MashupRequest(string request) : base((Dictionary<string, object>)new JsonReader(request).Deserialize())
		{
			// Invokes the Mashup(Dictionary) Constructor below
        }
		                
        public MashupRequest(Dictionary<string, object> dict) : base(dict)
		{
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
			new JsonFx.Json.JsonWriter(sb).Write(this);
			return sb.ToString();
        }
		
		protected void appendJsonDictionary(StringBuilder sb, Dictionary<string, object> dict)
		{
			sb.Append("{");
			
			if (dict != null && dict.Count > 0)
			{
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
			}
			
			sb.Append("}");
		}
    }
}
