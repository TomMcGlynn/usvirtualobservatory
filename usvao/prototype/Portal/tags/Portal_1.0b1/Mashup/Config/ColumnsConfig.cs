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
		public static readonly string EP_PREFIX = "cc";
		
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
		
		public Dictionary<string, object> getColumnDictionary(string service)
		{
			if (dict != null && dict[service] != null && dict[service] is Dictionary<string, object>)
			{
				return dict[service] as Dictionary<string, object>;
			}
			return null;
		}
	}
}

