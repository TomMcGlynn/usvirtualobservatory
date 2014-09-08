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
	public class AdaptorsConfig
	{
		public static readonly string ADAPTORS_CONFIG_KEY = "AdaptorsConfig";
		
		private Dictionary<string, object> dict = null;
		
		//
		// Singleton Instance
		//
		private static readonly object padlock = new object();
	    private static AdaptorsConfig instance=null;
	
		static AdaptorsConfig ()
		{
			lock (padlock)
			{
				instance = new AdaptorsConfig();
			}
		}
		
	    public static AdaptorsConfig Instance
	    {
	        get
	        {
	            lock (padlock)
	            {
					return instance;
	            }
	        }
	    }	
		
		private AdaptorsConfig ()
		{
			string filename = System.Configuration.ConfigurationManager.AppSettings.Get(ADAPTORS_CONFIG_KEY);
			if (filename != null)
			{
				string fullpathname = System.Web.HttpContext.Current.Server.MapPath(filename);
				loadFile(fullpathname);
			}
			else
			{
				throw new Exception("Key missing from Web.Config <appSettings> Section: " + ADAPTORS_CONFIG_KEY);
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
				
				// NOTE: We try both relative path name and full path to the file
		        if (File.Exists(fullpathname))
		        {
					//
					// IMPORTANT NOTE:
					// We must set the TypeHintName in the Settings below to "$type"
					// because this is the property that defines the Adaptor Class in the Adaptors Config File.
					// This is very subtle and *very* important so that the correct instance object is created
					// by the JsonReader.
					//
					// Example: 
					//
					// "Mast.Name.Lookup": {
        			//    "$type": "Mashup.Adaptors.HttpProxy, Mashup",
        			//    "url": "http://galex.stsci.edu/gxws/NameResolver/SantaResolver.asmx/query?name=[INPUT]"
    				//  }
					//
					string sJson = File.ReadAllText(fullpathname);
					JsonReaderSettings settings = new JsonReaderSettings();
					settings.TypeHintName = "$type";
					JsonReader jr = new JsonReader(sJson, settings);
					Object o = jr.Deserialize();
					
					if (o!= null && o is Dictionary<string, object>)
					{
						dict = o as Dictionary<string, object>;
					    Object a = dict["Mast.Name.Lookup"];
						Console.WriteLine ("a.GetType() " + a.GetType());
					}
					else
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
		
		public Object getAdaptor(string service)
		{
			Object a = null;
			if (dict != null && dict.TryGetValue(service, out a) && a != null)
			{
				return a;
			}
			throw new Exception("Unable to Locate Adaptor for service: " + service);
		}
	}
}

