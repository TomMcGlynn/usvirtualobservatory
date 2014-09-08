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
		/////////////////////////////////////////////////////////////////////////////////////////
		//
		// Public Properties
		//
		/////////////////////////////////////////////////////////////////////////////////////////
		
		//
		// service
		//
        public string service { 
			get { return getString("service"); } set { base["service"] = value;}
		}	
		
		//
		// paramss
		//
		public Dictionary<string, object> paramss  { 
			get { return getDictionary("params"); } set { base["params"] = value;}
		}
		
		//
		// format
		//			
		public string format { 
			get { return getString("format"); } set { base["format"] = value;}
		}
		
		public Boolean formatIsSpecified { 
			get {
				return (this.format != null && format.Trim().Length > 0);
			}
		}
		
		//
		// data
		//
		public object data { 
			get { return getObject("data"); } set { base["data"] = value;}
		}
		
		//
		// filename
		//
		public string filename { 
			get { return getString("filename"); } set { base["filename"] = value;}
		}
		
		public Boolean filenameIsSpecified {
			get {
				return (this.filename != null && filename.Trim().Length > 0);
			}
		}
		
		//
		// filetype
		//
		public string filetype { 
			get { return getString("filetype"); } set { base["filetype"] = value;}
		}
		
		public Boolean filetypeIsSpecified {
			get {
				return (this.filetype != null && filetype.Trim().Length > 0);
			}
		}
		
		//
		// attachment
		//
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
		// timeout
		//
		public static int DEFAULT_TIMEOUT_SECS = 20;
		
		public string timeout { 
			get { return getString("timeout"); } set { base["timeout"] = value;}
		}	
		
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

		//
		// sleep - this is for testing/debugging.  It will introduce a sleep of this many milliseconds prior 
		//         to dispatching to the adapter.
		//
		
		public string sleep { 
			get { return getString("sleep"); } set { base["sleep"] = value;}
		}	

		public int sleepAsMsecs {
			get { return (this.sleep != null && this.sleep.Trim().Length > 0 ? 
				              Convert.ToInt32(this.sleep) : 0); }
		}
		
		//
		// clearcache
		//
		public string clearcache { 
			get { return getString("clearcache"); } set { base["clearcache"] = value;}
		}
		
		public Boolean clearcacheIsSpecified {
			get {
				return (this.clearcache != null && this.clearcache.Trim().Length > 0 && Convert.ToBoolean(this.clearcache));
			}
		}
		
		//
		// page
		//
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
		
		//
		// pagesize
		//
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
		
		//
		// columnsconfigid : columnsconfig
		//
		public string columnsconfigid { 
			get { return getString("columnsconfigid"); } set { base["columnsconfigid"] = value;}
		}
		
		public Boolean columnsconfigidIsSpecified { 
			get {
				return (this.columnsconfigid != null && columnsconfigid.Trim().Length > 0);
			}
		}
		
		public Dictionary<string, object> columnsconfig  { 
			get { return getDictionary("columnsconfig"); } set { base["columnsconfig"] = value;}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////
		// formatType: 
		//
		// Public accessor to determine the response dataset format based on 
		//                 'filetype', 'format' and 'filename' properties
		// 
		// The formatType is derived from 3 properties based on the following priortity:
		//
		// (1) If 'filename' and 'filetype' are specified, then use 'filetype'
		// (2) Else if 'format' is specified, use 'format'
		// (3) Else, if the filename is specified, determine the filetype from the suffix.
		//
		/////////////////////////////////////////////////////////////////////////////////////////
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
		
		/////////////////////////////////////////////////////////////////////////////////////////
		//
		// Internal Utility Functions (private)
		//
		/////////////////////////////////////////////////////////////////////////////////////////
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
		
		/////////////////////////////////////////////////////////////////////////////////////////
		//
		// Constructors
		//	
		/////////////////////////////////////////////////////////////////////////////////////////
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
				
		/////////////////////////////////////////////////////////////////////////////////////////
		//
		// Key:
		//
		// Accessor to generate/retrieve a unique key that defines this Mashup Request Object based 
		// on 2 properties:
		//
		//    (1) service
		//    (2) input params
		//
		// The returned key is actually a JSON Object containing (1) and (2) above.
		// This key is extremely important because it uniquely indentifies a retreived DataSet 
		// that is stored/retreived in Web Cache using this Key. 
		//
		/////////////////////////////////////////////////////////////////////////////////////////
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
		
		/////////////////////////////////////////////////////////////////////////////////////////
		//
		// Public Utility Methods
		//
		/////////////////////////////////////////////////////////////////////////////////////////
		public static int DEBUG_LENGTH = 300;

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

		public string createRequestUrl(string muInvokeUrl) {
			string json = ToJson();
			string encodedJson = Uri.EscapeDataString(json);
			string requestUrl = muInvokeUrl + "?request=" + encodedJson;

			return requestUrl;
		}
		
		public static string requestInfo(string request)
		{
			// Optionally truncate the request string based on DEBUG_LENGTH 
			return "[REQUEST] " + (request.Length > DEBUG_LENGTH ? 
								   request.Substring(0, DEBUG_LENGTH) + "...[" + request.Length + "]" 
								   : request);
		}
    }
}
