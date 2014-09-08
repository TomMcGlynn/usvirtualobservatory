using System;
using System.IO;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Web;

using JsonFx.Json;
using Mashup.Adaptors;

namespace Mashup.Config
{
	
	public class ColumnsConfig
	{
		public static readonly string COLUMNS_CONFIG_KEY = "ColumnsConfig";
		
		// Prefix to use when including column config attribute in the column extended properties
		public static readonly string CC_PREFIX = "cc";
		
		private Dictionary<string, object> dict = null;
		
		//
		// Singleton Instance (Thread Safe with padlock)
		//
		private static readonly object padlock = new object();
	    private static ColumnsConfig instance=null;
	
		static ColumnsConfig ()
		{
			lock (padlock)
			{
				instance = new ColumnsConfig();
			}
		}
		
	    public static ColumnsConfig Instance
	    {
	        get
	        {
	            lock (padlock)
	            {
					return instance;
	            }
	        }
	    }	
		
		private ColumnsConfig ()
		{
			string filename = System.Configuration.ConfigurationManager.AppSettings.Get(COLUMNS_CONFIG_KEY);
			if (filename != null)
			{
				string fullpathname = System.Web.HttpContext.Current.Server.MapPath(filename);
				loadFile(fullpathname);
			}
			else
			{
				throw new Exception("Key missing from Web.Config <appSettings> Section: " + COLUMNS_CONFIG_KEY);
			}
			
			// Set the static instance variable
			instance = this;
		}
		
		private void loadFile(string fullpathname)
		{	
			if (fullpathname != null)
			{
				//
				// Read the File and Decode the JSON Contents
				//
		        if (File.Exists(fullpathname))
		        {
					string json = File.ReadAllText(fullpathname);
					dict = (Dictionary<string, object>)new JsonReader(json).Deserialize();
					if (dict == null)
					{
						throw new Exception("Unable to Parse Json File: " + fullpathname);
					}
				}
				else
				{
					throw new Exception("Unable to Open Json Config File: " + fullpathname);
				}
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////
		// getColumnProperties(MashupRequest)
		//
		// Try 3 ways to retrieve the Column Config Properties for the incoming mashup request:
		//
		// (1) Look for Column Properties specified directly within the incoming Mashup Request 
		//     via the 'columnsconfig' property
		//     
		// (2) Lookup the Column Properties using the 'columnsconfigid' request property 
		//     as the key into the ColumnsConfig.json file.  
		//
		// (3) Finally, lookup the Columns Properties using the incoming 'service' request property
		//     as the key into the ColumnsConfig.json file.
		//
		/////////////////////////////////////////////////////////////////////////////////////////

		public Dictionary<string, object> getColumnProperties(MashupRequest muRequest)
		{
			object d;
			if (dict != null)
			{
				// (1) See if Column Config Properties were passed along directly within the Mashup Request
				if (muRequest.columnsconfig != null && 
					muRequest.columnsconfig is Dictionary<string, object> &&
					muRequest.columnsconfig.Count > 0)
				{
					return muRequest.columnsconfig as Dictionary<string, object>;
				}
				
				// (2) Use the 'columnsconfigid' property as a key to retreive the Columns Config Properties
				if (muRequest.columnsconfigidIsSpecified)
				{
					if (dict.TryGetValue(muRequest.columnsconfigid, out d) && d is Dictionary<string, object>)
					{
						return d as Dictionary<string, object>;
					}
				}
				
				// (3) Try to use the 'service' property as a key to retrieve the Columns Config Properties
				if (dict.TryGetValue(muRequest.service, out d) && d is Dictionary<string, object>)
				{
					return d as Dictionary<string, object>;
				}
			}
			return null;
		}
	}
}

