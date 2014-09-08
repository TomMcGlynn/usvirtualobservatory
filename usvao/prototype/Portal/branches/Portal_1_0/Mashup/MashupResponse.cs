using System;
using System.Data;
using System.IO;
using System.Threading;
using System.Text;
using System.Net;
using System.Configuration;
using System.Collections.Generic;

using JsonFx.Json;
using log4net;

namespace Mashup
{	
	public class MashupResponse
	{	
		// Mashup.txt Logging
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		//
		// Acccessors 
		//
		public Thread thread;
		
		public Boolean isActive{
		get {
			return (this.thread != null && this.thread.IsAlive);
			}	
		}
		
		public int threadID
		{
			get {return (thread != null ? thread.ManagedThreadId : -1);}
		}
		
		public void wait(int timeout)
		{
			if (isActive)
			{
				thread.Join(timeout);
			}
		}
		
		public void abort()
		{
			if (isActive)
			{
				thread.Abort();
			}
		}
		
		public string msg 
		{ 
			get { return ((this.rd != null && this.rd.msg != null) ? this.rd.msg : ""); } 
			set { this.rd.msg = value;}
		}
		
		public string status 
		{ 
			get { return ((this.rd != null && this.rd.status != null) ? this.rd.status : ""); } 
			set { this.rd.status = value;}
		}
		
		public int length
		{
			get { return ((this.rd != null && this.rd.sb != null) ? this.rd.sb.Length : 0); }
		}
		
		Dictionary<string, object> debug = new Dictionary<string, object>();
		public string Debug()
		{		
			StringBuilder sb = new StringBuilder();
			debug["status"] = status;
			debug["threadID"] = threadID;
			debug["isActive"] = isActive;
			debug["length"] = length;
			debug["msg"] = msg;
			
			new JsonFx.Json.JsonWriter(sb).Write(debug); 
			return sb.ToString();
		}
			
		//
		// ResponseData Class
		//
		protected ResponseData rd = new ResponseData();
		
		//
		// Constructor
		//
		public MashupResponse ()
		{	
		}
		
		//
		// Load Methods are called by the Adaptor.invoke() Thread
		//
		public void load(DataSet ds)
		{
			loadResponse(null, ds, true);
		}
		
		public void load(DataSet ds, Boolean complete)
		{
			loadResponse(null, ds, complete);
		}
		
		protected void loadResponse(String s, DataSet ds, Boolean complete)
		{
			rd.ds = ds;
			rd.status = (complete ? "COMPLETE" : "EXECUTING");
		}
		
		//
		// Write Method is called by the Web Service Request Thread
		//		
		public void write(MashupRequest muRequest, System.Web.HttpResponse httpResponse)
		{
			//
			// BEGIN LOCK (rd):
			//
			// This is to ensure that 1 Thread at a time is accessing/manipulating the rd Container sbject
			// This sbject is saved/loaded into web cache and can have multiple muRequest threads accessing it simultaneously,
			// We need to ensure only one thread modifies the envelope at a time.
			//
			lock(rd)
			{	
				if (muRequest != null)
				{
					// TODO: Filter DataSet:
					// rd.filter(muRequest.filters)
					
					// TODO: Sort DataSet
					// rd.sort(muRequest.sort)
					
					// TODO: Page DataSet
					// rd.slicedice(muRequest.page)
									
					// Format DataSet: 
					// to 'csv', 'json', 'xml', 'extjs'
					rd.format(muRequest);
					
					// [OPTIONAL] Save Response To File
					if (muRequest.filenameSet)
					{
						rd.saveToFile(muRequest);
					}			
				}
				
				//////////////////////////////////////////////////////////////
				// Write the rd Container sbject back out to the client
				//////////////////////////////////////////////////////////////
				if (httpResponse != null)
				{		
					// Write back Data rd if it exits
					if (rd.hasOutputData)	    
					{
						httpResponse.ContentType = rd.ContentType;
						httpResponse.Write(rd.toOutputString());	
					}
					else if (status == "ERROR")
					{
						httpResponse.TrySkipIisCustomErrors = true;
						httpResponse.StatusCode = (int)HttpStatusCode.InternalServerError;
						httpResponse.ContentType = "text/plain";
						httpResponse.Write(msg);
					}
					
		            httpResponse.Flush();
					
					// Log Outgoing Response
					log.Info(tid + "<=== " + Debug() + " request: " + muRequest.ToJson());
				}	
			} // END LOCK (rd)			
		}
		
		#region ResponseData
		//
		// TODO: Refactor the ResponseData Class into an OO model based on Response Data Type
		// to avoid this big ugly 'if/switch' statments below
		//
		protected class ResponseData
		{	
			public string ContentType="text/text";
			
			public string status="EXECUTING";
			public string msg="";
			public string data = null;
			
			// Input data set:
			public DataSet ds;
			
			// Output data:
			public StringBuilder sb = new StringBuilder();
			
			public void clearOutput()
			{
				sb = new StringBuilder();
			}
			
			public void appendJsonHeader()
			{
				JsonWriter jw = new JsonFx.Json.JsonWriter(sb); 
				sb.Append("{\n");
				sb.Append("  \"status\" : "); jw.Write(status); sb.Append(",\n"); 
				sb.Append("  \"msg\" : "); jw.Write(msg); sb.Append(",\n"); 
				sb.Append("  \"data\" : ");
			}
			
			public void appendJsonTrailer()
			{
				sb.Append("}");
			}
			
			public string toOutputString()
			{
				return (hasOutputData ? sb.ToString() : "");
			}
			
			public Boolean hasDataSet
			{
				get { return (ds != null && ds.Tables != null && ds.Tables.Count > 0); }
			}
			
			public Boolean hasOutputData
			{
				get { return (sb != null && sb.Length > 0); }
			}		

			public void format(MashupRequest muRequest)
			{
				switch (muRequest.format)
				{
					case "json":
						ContentType = "text/javascript";
						clearOutput();
						if (!muRequest.filenameSet) 
							appendJsonHeader();
						if (hasDataSet)
						{
							sb.Append("\n  ");
							Utilities.Transform.DataSetToJson(ds, sb); 
						}
						else 
						{
							sb.Append("{}");
						}
					    if (!muRequest.filenameSet) 
							appendJsonTrailer();
						break;
					
					case "extjs":
						ContentType = "text/javascript";
						clearOutput();
						if (!muRequest.filenameSet) 
							appendJsonHeader();
						if (hasDataSet) 
						{
							sb.Append("\n  ");
							Utilities.Transform.DataSetToExtjs(ds, sb); 
						}
						else
						{
							sb.Append("{}");
						}
						if (!muRequest.filenameSet) 
							appendJsonTrailer();
						break;
					
					case "csv":
						ContentType = "text/csv";
					    clearOutput();
						if (hasDataSet) Utilities.Transform.DataSetToCsv(ds, sb); 
						break;	
					
					case "xml":
						ContentType = "text/xml";
					    clearOutput();
						if (hasDataSet) Utilities.Transform.DataSetToXml(ds, sb); 
						break;
					
					default:
						ContentType = "text/xml";
					    clearOutput();
						if (hasDataSet) Utilities.Transform.DataSetToXml(ds, sb); 
						break;
				}
			}
			
			private static object SaveLock = new object();
			
			public void saveToFile(MashupRequest muRequest)
			{
				if (muRequest.filenameSet)
				{
					// Get the Temp File Directory from the Web.Config
					
			        string internalTempDir = ConfigurationManager.AppSettings["internalTempDir"];
			        string externalTempDir = ConfigurationManager.AppSettings["externalTempDir"];
					
					if (internalTempDir == null || externalTempDir == null)
					{
						status = "ERROR";
						msg = "internalTempDir/externalTempDir not Specified in the Web.Config.";
						throw new Exception("Unable to save Response To File: internalTempDir/externalTempDir not Specified in the Web.Config.");
					}
			
			        //
			        // Ensure that only one thread at a time is determining a unique filename
			        // First, remove any stupid characters (like '+') from the filename, which can causes headache(s) down the road
			        string filename = muRequest.filename.Replace('+', '-');
			        string filenamePath = Path.GetFileName(filename);
			        string filenamePathTemp = internalTempDir + filenamePath;
					
					string file = filenamePathTemp; // specified below
			        string url = ""; // specified below
			
			        lock (SaveLock)
			        {
			            int i = 0;
			            while (File.Exists(file))
			            {
			                file = internalTempDir + 
								   Path.GetFileNameWithoutExtension(filenamePathTemp) + 
								   "_" + (i++) + 
								   Path.GetExtension(filenamePathTemp);
			            }
			
			            // set the new URL
			            url = externalTempDir + Path.GetFileName(file);
			        }  // lock(SaveLock)
			
			        //
					// Save The File to the internal file name
					//
					StreamWriter sw = new StreamWriter(file);
			        sw.Write(sb.ToString());
					sw.Close();
					
					//
					// Return the url to the saved file
					//
					clearOutput();
					status = "COMPLETE";
					appendJsonHeader();
					sb.Append("{ \"url\": \"" + url + "\"}");
					appendJsonTrailer();
				}
				else
				{
					status = "ERROR";
					msg = "Unable to save Response To File: Filename is not specified.";
					throw new Exception("Unable to save Response To File: Filename is not specified. ");
				}
			}
		}
		#endregion
	}
}

