using System;
using System.Data;
using System.Configuration;
using System.Linq;
using System.Web;
using System.IO;
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
		public static int DEBUG_LENGTH = 300;
		
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
		
		public Boolean formatIsSpecified { 
			get {
				return (this.format != null && format.Trim().Length > 0);
			}
		}
		
		public object data { 
			get { return getObject("data"); } set { base["data"] = value;}
		}
		
		public string filename { 
			get { return getString("filename"); } set { base["filename"] = value;}
		}
		
		public Boolean filenameIsSpecified {
			get {
				return (this.filename != null && filename.Trim().Length > 0);
			}
		}
		
		public string filetype { 
			get { return getString("filetype"); } set { base["filetype"] = value;}
		}
		
		public Boolean filetypeIsSpecified {
			get {
				return (this.filetype != null && filetype.Trim().Length > 0);
			}
		}
		
		public string attachment { 
			get { return getString("attachment"); } set { base["attachment"] = value;}
		}
		
		public Boolean attachmentIsSpecified {
			get {
				return (this.attachment != null && attachment.Trim().Length > 0);
			}
		}

		public bool attachmentAsBoolean() {
			bool b_attachment = true;
			if (attachmentIsSpecified) {
				b_attachment = Convert.ToBoolean(attachment);
			}
			return b_attachment;
		}
		
		//
		// NOTE: formatType is derived from 3 inputs based on the following priortity:
		//
		// (1) 'filetype'
		// (2) 'format'
		// (3) 'filename' suffix.
		//
		// We try 3 ways to determine the resulting formatType of the output DataSet
		//
		// (1) If 'filename' and 'filetype' are specified, then use 'filetype'
		// (2) Else if 'format' is specified, use that
		// (3) Else, if the filename is specified, try to extract its suffix
		//
		public string formatType {
			get {
				string type = "extjs";
				if (filetypeIsSpecified)
				{
					type = filetype;
				}
				else if (formatIsSpecified)
				{
					type = format;
				}
				else if (filenameIsSpecified)
				{
					type = Path.GetExtension(filename).Replace(".", "");
				}
				return type;
			}
		}
					
		public string timeout { 
			get { return getString("timeout"); } set { base["timeout"] = value;}
		}
		
		public static int DEFAULT_TIMEOUT_SECS = 20;
		
		public int timeoutAsMsecs {
			 get { return (this.timeout != null && this.timeout.Trim().Length > 0 ? 
					Convert.ToInt32(this.timeout) : 
					DEFAULT_TIMEOUT_SECS) * 1000; }
		}
		
		public int timeoutAsSecs {
			 get { return (this.timeout != null && this.timeout.Trim().Length > 0 ? 
					Convert.ToInt32(this.timeout) : 
					DEFAULT_TIMEOUT_SECS); }
		}
		
		public string clearcache { 
			get { return getString("clearcache"); } set { base["clearcache"] = value;}
		}
		
		public Boolean clearcacheIsSpecified {
			get {
				return (this.clearcache != null && this.clearcache.Trim().Length > 0 && Convert.ToBoolean(this.clearcache));
			}
		}
		
		public string page { 
			get { return getString("page"); } set { base["page"] = value;}
		}
		
		public Boolean pageIsSpecified {
			get {
				return (this.page != null && this.page.Trim().Length > 0);
			}
		}
		
		public int pageAsInt {
			 get { return (pageIsSpecified ? Convert.ToInt32(this.page) : 1); }
		}
		
		public static int DEFAULT_PAGESIZE = 1000;
		
		public string pagesize { 
			get { return getString("pagesize"); } set { base["pagesize"] = value;}
		}
		
		public Boolean pagesizeIsSpecified {
			get {
				return (this.pagesize != null && this.pagesize.Trim().Length > 0);
			}
		}
		
		public int pagesizeAsInt {
			 get { return (pagesizeIsSpecified ? Convert.ToInt32(pagesize) : DEFAULT_PAGESIZE); }
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
		
		private object getObject (string key)
		{
			object val;
			if (base.TryGetValue(key, out val))
			{
				return val;
			}
			else
			{
				return null;
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
		
		public MashupRequest(string json) : base((Dictionary<string, object>)new JsonReader(json).Deserialize())
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
				
		//
		// Key:
		// Accessor to a unique key that defines this Request Object based on input params
		// The returned key is actually a JSON Object of name-value-pairs of 'this' dictionary
		// The 'data' param is replaced by a HashCode representation of the data becuase it can be quite large.
		// This key is used for caching Response Objects in Web Cache.
		//
		protected string _key = null;
		
		public string Key
        {	
			//
			// VERY IMPORTANT NOTE:
			// The Request Key is a Json Representation of the 'service' and 'params' attributes only.
			// 'service' and 'params' are the only Request Attributes that define a unique Response DataSet.
			// All other Request Attributes define modifcations to be applied to the Response DataSet.
			// 
			get
			{
				if (_key == null)
				{
		            Dictionary<string, object> key = new Dictionary<string, object>();
					key.Add("service", service);
					key.Add("params", paramss);
					
					StringBuilder sb = new StringBuilder();
					new JsonFx.Json.JsonWriter(sb).Write(key);
					_key = sb.ToString();
				}
				return _key;
			}
        }
		
		public string Debug()
		{
			return "[REQUEST] : " + ToJson(DEBUG_LENGTH);
		}
							
		public string ToJson()
		{
			return ToJson(-1);
		}
		
		public string ToJson(int length)
        {				
			StringBuilder sb = new StringBuilder();
			new JsonFx.Json.JsonWriter(sb).Write(this);
			
			// Optionally truncate the json string based on input length 
			string json = ((length > 0 && sb.Length > length) ? 
				           sb.ToString(0, length) + "...[" + sb.Length +"]" 
				           : sb.ToString());
			return json;
        }	
		
		public static string requestInfo(string request)
		{
			// Optionally truncate the request string based on DEBUG_LENGTH 
			return "[REQUEST] " + (request.Length > DEBUG_LENGTH ? 
								   request.Substring(0, DEBUG_LENGTH) + "...[" + request.Length + "]" 
								   : request);
		}
		
		/*
		static Int64 GetInt64HashCode(string s)
		{
		    Int64 hashCode = 0;
		    if (!string.IsNullOrEmpty(s))
		    {
		        //Unicode Encode Covering all characterset
		        byte[] byteContents = Encoding.Unicode.GetBytes(s);
		        System.Security.Cryptography.SHA256 hash = new System.Security.Cryptography.SHA256CryptoServiceProvider();
		        byte[] hashText = hash.ComputeHash(byteContents);
		        //32Byte hashText separate
		        //hashCodeStart = 0~7  8Byte
		        //hashCodeMedium = 8~23  8Byte
		        //hashCodeEnd = 24~31  8Byte
		        //and Fold
		        Int64 hashCodeStart = BitConverter.ToInt64(hashText, 0);
		        Int64 hashCodeMedium = BitConverter.ToInt64(hashText, 8);
		        Int64 hashCodeEnd = BitConverter.ToInt64(hashText, 24);
		        hashCode = hashCodeStart ^ hashCodeMedium ^ hashCodeEnd;
		    }
		    return (hashCode);
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
		*/
    }
}
