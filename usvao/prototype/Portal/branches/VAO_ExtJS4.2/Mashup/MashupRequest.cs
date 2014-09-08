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

using Collections;

namespace Mashup
{
    [Serializable]
    public class MashupRequest
    {	
		private IDictionary<string, object> fDict = null;

		/////////////////////////////////////////////////////////////////////////////////////////
		//
		// Public Properties
		//
		/////////////////////////////////////////////////////////////////////////////////////////

		//
		// service
		//
        public string service { 
			get { return getString("service"); } set { fDict["service"] = value;}
		}	
		
		//
		// paramss
		//
		public IDictionary<string, object> paramss  { 
			get { return getDictionary("params"); } set { fDict["params"] = value;}
		}
		
		//
		// format
		//			
		public string format { 
			get { return getString("format"); } set { fDict["format"] = value;}
		}

		//
		// requestBaseUrl
		//			
		public string requestBaseUrl { 
			get { return getString("requestBaseUrl"); } set { fDict["requestBaseUrl"] = value;}
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
			get { return getObject("data"); } set { fDict["data"] = value;}
		}
		
		//
		// filename
		//
		public string filename { 
			get { return getString("filename"); } set { fDict["filename"] = value;}
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
			get { return getString("filetype"); } set { fDict["filetype"] = value;}
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
			get { return getString("attachment"); } set { fDict["attachment"] = value;}
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
			get { return getString("timeout"); } set { fDict["timeout"] = value;}
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
			get { return getString("sleep"); } set { fDict["sleep"] = value;}
		}	

		public int sleepAsMsecs {
			get { return (this.sleep != null && this.sleep.Trim().Length > 0 ? 
				              Convert.ToInt32(this.sleep) : 0); }
		}
		
		//
		// clearcache
		//
		public string clearcache { 
			get { return getString("clearcache"); } set { fDict["clearcache"] = value;}
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
			get { return getString("page"); } set { fDict["page"] = value;}
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
			get { return getString("pagesize"); } set { fDict["pagesize"] = value;}
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
			get { return getString("columnsconfigid"); } set { fDict["columnsconfigid"] = value;}
		}
		
		public Boolean columnsconfigidIsSpecified { 
			get {
				return (this.columnsconfigid != null && columnsconfigid.Trim().Length > 0);
			}
		}
		
		public IDictionary<string, object> columnsconfig  { 
			get { return getDictionary("columnsconfig"); } set { fDict["columnsconfig"] = value;}
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
			if (fDict.TryGetValue(key, out val) && (val != null))
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
			if (fDict.TryGetValue(key, out val))
			{
				return val;
			}
			else
			{
				return null;
			}
		}
		
		private IDictionary<string, object> getDictionary(string key)
		{
			object val;
			if (fDict.TryGetValue(key, out val) && val is IDictionary<string, object>)
			{
				return val as IDictionary<string, object>;
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
        public MashupRequest() : this(new OrderedDictionary<string, object>())
		{
			// Invokes the Mashup(Dictionary) Constructor below
        }

		private static int dumpCnt = 0;
		public MashupRequest(string json) : 
			this((IDictionary<string, object>) new JsonFx.Json.JsonReader(json, new JsonReaderSettings(typeof(Dictionary<String, Object>))).Deserialize())
		{
			// Invokes the Mashup(IDictionary) Constructor below with the json string deserialized into an OrderedDictionary

			// For debugging purposes, this makes it easy to see the whole JSON request.
			bool dumpJson = false;
			if (dumpJson) {
				// This will be written with no formatting, but will be the exact string we got.
				StreamWriter w = new StreamWriter("DebugOutput/mashupRequestUnformatted-" + dumpCnt + ".json");
				w.WriteLine(json);
				w.Close();

				// This will be formatted, but relies on the JSON being deserialized, then reserialized, so it's not good if you're debugging
				// the JSON serializer/deserializer.
				StreamWriter sw = new StreamWriter("DebugOutput/mashupRequest-" + dumpCnt + ".json");
				JsonWriter jw = new JsonWriter(sw, JsonDataWriter.CreateSettings(true));
				jw.Write(fDict);
				sw.Close();

				// Increment dumpCnt.
				dumpCnt = (dumpCnt + 1) % 10;
			}

        }
		                
        public MashupRequest(IDictionary<string, object> dict)
		{
			fDict = dict;
			if (paramss == null)
			{
				paramss = new OrderedDictionary<string, object>();
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
		            OrderedDictionary<string, object> key = new OrderedDictionary<string, object>();
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
			new JsonFx.Json.JsonWriter(sb).Write(fDict);
			
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
