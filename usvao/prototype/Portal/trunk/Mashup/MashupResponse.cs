using System;
using System.Data;
using System.IO;
using System.Threading;
using System.Text;
using System.Net;
using System.Configuration;
using System.Collections;
using System.Collections.Generic;
using System.Text.RegularExpressions;

using Utilities;
using ExcelLibrary;
using ExcelLibrary.SpreadSheet;
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
		// MashupResponseData Class: 
		// This class is a dumb pallette, used to hold each of the intermediate steps of the output processing pipeline.
		// 
		protected MashupResponseData mrd = new MashupResponseData();
		
		// Methods to access information in the MashupResponseData 
		public string status 
		{ 
			get { return ((this.mrd != null && this.mrd.status != null) ? this.mrd.status : ""); } 
			set { this.mrd.status = value;}
		}
		
		public float percentComplete
		{ 
			get { return (this.mrd != null ? this.mrd.percentComplete : float.NaN); } 
			set { this.mrd.percentComplete = value;}
		}
		
		public string msg 
		{ 
			get { return ((this.mrd != null && this.mrd.msg != null) ? this.mrd.msg : ""); } 
			set { this.mrd.msg = value;}
		}
		
		public int length
		{
			get { return ((this.mrd != null && this.mrd.ob != null) ? this.mrd.ob.Length : 0); }
		}
			
		//
		// Response Thread Stuff
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
				
		//
		// Constructor
		//
		public MashupResponse ()
		{	
		}
		
		public string Debug()
		{		
			StringBuilder sb = new StringBuilder();
			sb.Append("[RESPONSE] : ");
			sb.Append("[LENGTH:" + length + "] ");
            string replaced = Regex.Replace(mrd.getOutBuffer(), @"\n\r|\n|\r", " ");
			sb.Append(replaced); 

			Dictionary<string, object> dict = new Dictionary<string, object>();
			dict["id"] = threadID;
			dict["active"] = isActive;
			sb.Append(" [THREAD] : ");
			new JsonFx.Json.JsonWriter(sb).Write(dict); 

			return sb.ToString();
		}
		
		//
		// Load Methods are called by the Adaptor Threads to load the initial DataSet onto the Pallette
		//
		public void load(Dictionary<string, object> data, Boolean complete)
		{
			mrd.data = data;
			mrd.status = (complete ? "COMPLETE" : "EXECUTING");
		}
		
		public void load(DataSet ds, Boolean complete)
		{
			mrd.dsin = ds;
			mrd.histogram = null;	// Histogram is now stale and must be recomputed with new DataSet 
			mrd.status = (complete ? "COMPLETE" : "EXECUTING");
		}
		
		public void load(DataSet ds, Boolean complete, float percentComplete)
		{
			mrd.dsin = ds;
			mrd.histogram = null;	// Histogram is now stale and must be recomputed with new DataSet 
			mrd.status = (complete ? "COMPLETE" : "EXECUTING");
			mrd.percentComplete = percentComplete;
		}
		
		//
		// WriteMashupResponse() Method : called by the Mashup Main Request Thread
		//		
		public void writeMashupResponse(MashupRequest muRequest, System.Web.HttpResponse httpResponse)
		{
			//
			// BEGIN LOCK (mrd):
			//
			// This is to ensure that 1 Thread at a time is accessing/manipulating the mrd Container sbject
			// This object is saved/loaded into web cache and can have multiple muRequest threads accessing it simultaneously,
			// We need to ensure only one thread modifies the MashupResponseData (mrd) at a time.
			//
			lock(mrd)
			{	
				//
				// STEP 0: Create new Output Buffer (ob and wb)
				//
				mrd.ob = new StringBuilder();
				mrd.wb = null;
					
				// If Input DataSet contains a DataTable, run it through: STEPS 1 - 3
				if (mrd.dsin != null && mrd.dsin.Tables.Count > 0)
				{
					//
					// STEP 1: Filter, sort rows and columns in DataSet (dsin) ===> (dssort)
					//
					filterSortDataSet(muRequest, mrd.dsin, out mrd.dssort);
					
					//
					// STEP 2: Paginate DataSet (dssort) ===> (dspage)
					//
					pageDataSet(muRequest, mrd.dssort, out mrd.dspage);
					
					//
					// STEP 3: Append Histogram for page 1 only: (dsin) ===> (dspage) 
					//
                    // if the response is an attachment it is being downloaded as a file, so the histogram doesn't need to be calculated
					bool pageOne = false;  // Keep track of whether this is the first page, so we know whether to include votMetadata in a JSON response.
					if ((muRequest.page == "1") && !muRequest.filenameIsSpecified)
                    {
						pageOne = true;
						try{
							appendHistogram(mrd.dsin, mrd.dspage);
						} catch (Exception ex)
						{  
							msg = "[RESPONSE] Exception caught while Creating Histogram: " + ex.Message;
							log.Error(tid + msg, ex);
						}
					}

					//
					// STEP 4: Format Output DataSet to Output Products (dspage) ===> (ob or wb)
					//
					formatDataSet(muRequest, mrd.dspage, mrd.ob, out mrd.wb, pageOne);
				}
				else if (mrd.data != null)
				{
					mrd.ob = new StringBuilder();
					new JsonFx.Json.JsonWriter(mrd.ob).Write(mrd.data);
				}
				else
				{
					mrd.ob = new StringBuilder("{}");
				}
				
				//
				// STEP 5: (Optional) Save Output Products To File (ob or wb) ===> (file)
				//
				if (muRequest.filenameIsSpecified)
				{
					// Save the file out to local disk
					string url = saveToFile(muRequest, mrd.ob, mrd.wb);
					
					// Set Output Buffer to the Saved File Url
					mrd.ob = new StringBuilder("{ \"url\": \"" + url + "\"}");
                }
				
				//
				// STEP 6: Insert the Mashup Header/Trailer in the Output Buffer (ob)
				//
				if (muRequest.formatIsSpecified)
				{
					mrd.ob.Insert(0, getMashupHeader(muRequest));
                    mrd.ob.Append(getMashupTrailer(muRequest));
				}
				
				//
				// STEP 7: Write Output Buffer Response to the client (ob) ===> (client)
				//
				writeMashupResponseData(mrd, httpResponse);
				
				//
				// STEP 8: Log Outgoing Response
				//
				log.Info(tid + "<=== " + this.Debug());			
				
				//
				// STEP 9: Clear up Output Objects to reduce memory footprint
				//
				mrd.ob = null;
				mrd.wb = null;
				mrd.dspage = null;
				
			} // END LOCK (mrd)			
		}	
		
		protected void appendHistogram(DataSet dsin, DataSet dsout)
		{
			// Create Histogram from Input DataSet
			DataColumnCollection inColumns = dsin.Tables[0].Columns;
			if (mrd.histogram == null)
			{
				mrd.histogram = new Histogram(dsin.Tables[0]);
			}
			
			// Append the Histogram to the Output DataSet Columns
			Dictionary<string, Object> dict = mrd.histogram.getHistogram();
			DataTable outDt = dsout.Tables[0];
		    foreach (DataColumn column in outDt.Columns)
		    {
		        string name = column.ColumnName;
				
				// If the histogram generator imposed an ignore value, copy that here.
				// Note that this has to happen before we check for the histogram dictionary below,
				// because the histogrammer may have given up on a histgram (e.g., too many nulls), 
				// but still assigned an ignore value.
				DataColumn inCol = inColumns[name];
				if (inCol != null) {
					object ignoreVal = inCol.ExtendedProperties["cc.ignoreValue"];
					if (ignoreVal != null) {
						column.ExtendedProperties["cc.ignoreValue"] = ignoreVal;
					}
				}

		        if (!dict.ContainsKey(name)) continue;
		        var histObj = dict[name];
		        column.ExtendedProperties.Add("histObj", histObj);

		    }
		}
		
		public void writeCleanupResponse(MashupRequest muRequest, System.Web.HttpResponse httpResponse)
		{
			//
			// writeCleanup() Method is called when an Exception in thrown and the caught by the cleanUp() method of Mashup.asmx.cs
			// IMPORTANT NOTE: The muRequest could be null here
			//
			
			//
			// BEGIN LOCK (mrd):
			//
			// This is to ensure that 1 Thread at a time is accessing/manipulating the mrd Container sbject
			// This object is saved/loaded into web cache and can have multiple muRequest threads accessing it simultaneously,
			// We need to ensure only one thread modifies the MashupResponseData (mrd) at a time.
			//
			lock(mrd)
			{
				//
				// STEP 0: Create new Output Buffer
				//
				mrd.ob = new StringBuilder();
				
				//
				// STEP 1: Create the Mashup Response Message Header and Trailer
				//
				if (muRequest != null && muRequest.formatIsSpecified)
				{
					mrd.ob.Insert(0, getMashupHeader(muRequest));
					mrd.ob.Append(getMashupTrailer(muRequest));
				}
				
				//
				// STEP 2: Write Mashup Response
				//
				writeMashupResponseData(mrd, httpResponse);
				
				//
				// STEP 3: Log Outgoing Response
				//
				log.Info(tid + "<=== " + this.Debug() + " " + muRequest.Debug());
				
				//
				// STEP 4: Clear up Output Objects to reduce memory footprint
				//
				mrd.ob = null;
				
			} // END LOCK (mrd)			
		}		
		
		protected void filterSortDataSet(MashupRequest muRequest, DataSet dsin, out DataSet dsout)
		{
			//
			// Create new DataSet that is Sorted and Filtered based on the input DataSet
			//
			DataTable dtout = null;
			if (dsin != null && dsin.Tables != null && dsin.Tables.Count > 0)
			{
				string rowFilter = "";	
				DataTable dtin = dsin.Tables[0];
				
				// Get the columns that we'll actually use, in the correct order
				SortedDictionary<int, DataColumn> orderedColumns = getOrderedColumns(muRequest, dtin);
				
				// Create the Ordered Columns string array
				string[] columnNames = new string[orderedColumns.Values.Count];
				int i = 0;
				foreach (DataColumn col in orderedColumns.Values) {
					columnNames[i++] = col.ColumnName;
				}
				
				// Create the Column Sort String (i.e: 'Author ASC, Date DESC')
				string colSortString = getColumnSortString(orderedColumns);
				
				// Create Sorted, Filtered DataSet
				DataView dv = new DataView(dtin, rowFilter, colSortString, DataViewRowState.CurrentRows);				
				dtout = dv.ToTable(dtin.TableName, false, columnNames);
				copyDataTableExtendedProperties(dtin, dtout);
				
				// Update DataTable Properties with Rows Info
				DataTableExtendedProperties dtep = getDataTableExtendedProperties(dtout);
				dtep.rowsTotal = dtin.Rows.Count;
				dtep.rowsFiltered = dtout.Rows.Count;
			}
				
			// Create Ouput DataSet
			dsout = new DataSet(dsin.DataSetName);
			copyExtendedProperties(dsin.ExtendedProperties, dsout.ExtendedProperties);
			dsout.Tables.Add(dtout);
		}
		
		protected DataTableExtendedProperties getDataTableExtendedProperties(DataTable dtin)
		{
			DataTableExtendedProperties dtep = null;
			if (dtin.ExtendedProperties.ContainsKey(DataTableExtendedProperties.KEY))
			{
				dtep = dtin.ExtendedProperties[DataTableExtendedProperties.KEY] as DataTableExtendedProperties;
			}
			else
			{
				dtep = new DataTableExtendedProperties();
				dtin.ExtendedProperties[DataTableExtendedProperties.KEY] = dtep;
			}
			return dtep;
		} 	
		
		protected void pageDataSet(MashupRequest muRequest, DataSet dsin, out DataSet dsout)
		{
			//
			// Validate Request Page Params
			//
			if (muRequest.pageIsSpecified && muRequest.pageAsInt <= 0) 
				throw new Exception("page parameter must be > 0");
			if (muRequest.pagesizeIsSpecified && muRequest.pagesizeAsInt <= 0) 
				throw new Exception("pagesize parameter must be > 0");
			
			// Extract first DataTable
			DataTable dtin = dsin.Tables[0];
			
			// Set default start, end, pagesize values.
			int start = 0;
			int end = dtin.Rows.Count;
			int pagesize = end;
			
			// If 'page' is specified, determine start record AND end record using specified pagesize (or default)
			if (muRequest.pageIsSpecified)
			{
				start = (muRequest.pageAsInt-1) * muRequest.pagesizeAsInt;	
				end = start + muRequest.pagesizeAsInt;
				pagesize = muRequest.pagesizeAsInt;
			}
			else if (muRequest.pagesizeIsSpecified)
			{
				// If 'pagesize' is specified, start = 0, determine end row
				end = start + muRequest.pagesizeAsInt;
				pagesize = muRequest.pagesizeAsInt;
			}
			
			//
			// Check if we should just return Existing Table containing all rows
			//
			DataTableExtendedProperties ep;
			if (start == 0 && end >= dtin.Rows.Count)
			{
				if (dtin.Rows.Count > 0)
				{
					// Update DataTable Extended Properties 
					ep = getDataTableExtendedProperties(dtin);
					ep.page = 1;
					ep.pageSize = pagesize;
					ep.pagesFiltered = 1;
					ep.rows = dtin.Rows.Count;
				}
				dsout = dsin;
				return;
			}
			
			//
			// We need to create a new DataTable containing the requested Page
            // So we clone the Schema from the original DataTable and load records from start - end
            //
            DataTable dtout = dtin.Clone();
			copyColumnExtendedProperties(dtin, dtout);
			
            // Import the Rows for the specified page range
            for (int i = start; i < end; i++)
            {
				if (i < dtin.Rows.Count)
				{
                	DataRow row = dtin.Rows[i];
                	dtout.ImportRow(row);
                	dtout.AcceptChanges();
				}
				else
				{
					break;
				}
            }
			
			// Update DataTable-Page Extended Properties 
			ep = getDataTableExtendedProperties(dtout);
			ep.rows = dtout.Rows.Count;
			ep.page = muRequest.pageAsInt;
			ep.pageSize = muRequest.pagesizeAsInt; 
			ep.pagesFiltered = dtin.Rows.Count/muRequest.pagesizeAsInt;
			if ((dtin.Rows.Count % muRequest.pagesizeAsInt) > 0) ep.pagesFiltered++;
			
			// Create New DataSet for the DataTable-Page
			dsout = new DataSet(dsin.DataSetName);
			copyExtendedProperties(dsin.ExtendedProperties, dsout.ExtendedProperties);
			dsout.Tables.Add(dtout);
		}
		
		protected void copyDataTableExtendedProperties(DataTable dtin, DataTable dtout) 
		{
			PropertyCollection epout = dtout.ExtendedProperties;
			PropertyCollection epin = dtin.ExtendedProperties;
			
			epout.Clear();
			foreach (string key in epin.Keys)
			{
				epout.Add(key, epin[key]);
			}	
			
			// Copy the Extended Properties on each Column
			copyColumnExtendedProperties(dtin, dtout);
		}
		
		protected void copyColumnExtendedProperties(DataTable dtin, DataTable dtout)
		{
			//
			// NOTE: 
			// The Extended Properties of the new DataTable are turned into DataCollection Objects
			// This appears to be a bug in Microsoft or Mono.  Not sure.
			// So, we clear it the Extended Properties on the new column and copy them over from the origninal table
			//
			foreach (DataColumn colout in dtout.Columns)
			{
				PropertyCollection epout = colout.ExtendedProperties;
				PropertyCollection epin = dtin.Columns[colout.ColumnName].ExtendedProperties;
				
				epout.Clear();
				foreach (string key in epin.Keys)
				{
					epout.Add(key, epin[key]);
				}
			}
		}

		protected void copyExtendedProperties(PropertyCollection epIn, PropertyCollection epOut) {
			foreach (string key in epIn.Keys)
			{
				epOut.Add(key, epIn[key]);
			}
		}
		
		protected SortedDictionary<int, DataColumn> getOrderedColumns(MashupRequest muRequest, DataTable dtin)
		{
			SortedDictionary<int, DataColumn> sortedDict = new SortedDictionary<int, DataColumn>();
			ArrayList otherList = new ArrayList();
			
			//
			// Insert the orderedList columns first at their specified location
			//
			foreach (DataColumn col in dtin.Columns)
			{
				if (!isColumnRemoved(col))
				{
					int order = getColumnOrder(col);
					if (order >= 0)
					{
						// Try to access index.  This may throw exception because index may be larger than array.
						DataColumn sortedColumn;
						if (sortedDict.TryGetValue(order, out sortedColumn))
						{
							log.Warn("Duplicate Column Order [" + order + "] for Service Adaptor [" + muRequest.service + 
								     "] column names [" + sortedColumn.ColumnName + ", " + col.ColumnName + "]. Please correct the ColumnsConfig File.");
						}
						sortedDict[order] = col;	
					}
					else
					{
						otherList.Add(col);
					}
				}		
			}
			
			//
			// Insert the remaining otherList column names into the orderedList
			//
			int i=0;
			foreach (DataColumn col in otherList)
			{
				// Insert column name into any gaps in the orderedList
				DataColumn sortedColumn;
				while (sortedDict.TryGetValue(i, out sortedColumn)) i++;
				sortedDict[i] = col;
			}

			return sortedDict;
		}
		
		protected string getColumnSortString(SortedDictionary<int, DataColumn> orderedColumns) 
		{
			string colSort = "";
			string sep = "";
			foreach (DataColumn col in orderedColumns.Values) 
			{
				string sort = getColumnSortString(col);
				if (sort != null) {
					colSort += (sep + col.ColumnName + " " + sort);
					sep = ",";
				}
			}
			return colSort;
		}
		
		protected Boolean isColumnVisible(DataColumn col)
		{
			bool isVisible = true;
			
			if (col != null && 
				col.ExtendedProperties != null &&
				col.ExtendedProperties.ContainsKey("cc.visible"))
			{
				Object o = col.ExtendedProperties["cc.visible"];
				isVisible = Convert.ToBoolean(o);
			}	
			return isVisible;
		}
		
		protected Boolean isColumnRemoved(DataColumn col)
		{
			bool isRemoved = false;
			
			if (col != null && 
				col.ExtendedProperties != null &&
				col.ExtendedProperties.ContainsKey("cc.remove"))
			{
				Object o = col.ExtendedProperties["cc.remove"];
				isRemoved = Convert.ToBoolean(o);
			}	
			return isRemoved;
		}
		
		protected int getColumnOrder(DataColumn col)
		{
			int order = -1;
			if (col != null && 
				col.ExtendedProperties != null && 
				col.ExtendedProperties.ContainsKey("cc.order"))
			{
				Object o = col.ExtendedProperties["cc.order"];
				order = Convert.ToInt32(o);	
			}
			return order;
		}
		
		protected string getColumnSortString(DataColumn col)
		{
			string sort = null;
			if (col != null && 
				col.ExtendedProperties != null && 
				col.ExtendedProperties.ContainsKey("cc.sort"))
			{
				Object o = col.ExtendedProperties["cc.sort"];
				sort = (string)o;
			}
			return sort;
		}

		protected void formatDataSet(MashupRequest muRequest, DataSet dsin, StringBuilder sb, out Workbook wb, bool includeVotMetadata)
		{	
			wb = null;
			
			// Transform it to appropriate Output Data Structure
			if (dsin != null && dsin.Tables.Count > 0)
			{
				switch (muRequest.formatType)
				{
					case "json":
					case "extjs":
						mrd.ContentType = "text/javascript";
					Utilities.Transform.DataSetToExtjs(dsin, sb, includeVotMetadata); 
						break;
					
					case "csv":
						mrd.ContentType = "text/csv";
						Utilities.Transform.DataSetToCsv(dsin, sb); 
						break;	
					
					case "xml":
						mrd.ContentType = "text/xml";
						Utilities.Transform.DataSetToXml(dsin, sb); 
						break;
					
					case "html":
						mrd.ContentType = "text/html";
						Utilities.Transform.DataSetToHtml(dsin, sb); 
						break;
					
					case "votable":
					case "vot":
						mrd.ContentType = "text/xml";
						Utilities.Transform.DataSetToVoTable(dsin, sb); 
						break;
										
					case "xls":
						mrd.ContentType = "application/x-excel";
						Utilities.Transform.DataSetToXls(dsin, out wb); 
						break;
					
					default:
						throw new Exception("Unknown  Format Type specified : " + muRequest.formatType);
				}
			}
		}
		
		// Static Object used to control Thread Access to Save File Directory
		private static object SaveLock = new object();
		
		protected string saveToFile(MashupRequest muRequest, StringBuilder ob, Workbook wb)
		{
			//
			// Get the Temp File Directory from the Web.Config	
			// and convert TempDir From Local Relative Directory To Full Local Path 
			//
	        string tempDir = ConfigurationManager.AppSettings["TempDir"];
			
			if (tempDir == null)
			{
				status = "ERROR";
				msg = "TempDir not Specified in the Web.Config.";
				throw new Exception("Unable to save Response To File: 'TempDir' not Specified in the Web.Config.");
			}
			
			if (!tempDir.EndsWith("/")) tempDir += "/";
			string tempDirPath = System.Web.HttpContext.Current.Server.MapPath(tempDir);
			if (!Directory.Exists(tempDirPath))
			{
				throw new Exception("Unable to save Response To File: 'TempDir' Directory Does not Exist: " + tempDirPath);
			}
	
	        //
	        // Ensure that only one thread at a time is determining a unique filename
	        // First, remove any stupid characters (like '+') from the filename, which can causes headache(s) down the road
			//
	        string filename = muRequest.filename.Replace('+', '-');
	        filename = Path.GetFileName(filename);
			string filenamePath = tempDirPath + filename;
									
			string filenameWrite = filenamePath; 	// unique filename determined below
	
	        lock (SaveLock)
	        {
				// Find a Unique Filename for Exporting the Results
	            int i = 0;
	            while (File.Exists(filenameWrite))
	            {
	                filenameWrite = Path.GetDirectoryName(filenamePath) + '/' + 
						   			Path.GetFileNameWithoutExtension(filenamePath) + 
						   			"_" + (i++) + 
						   			Path.GetExtension(filenamePath);
	            }
	        }  // lock(SaveLock)
	
	        //
			// Save Response Data Output products to the Unique file name
			//
			if (wb != null)
			{
				wb.Save(filenameWrite);
			}
			else if (ob != null)
			{	
				StreamWriter sw = new StreamWriter(filenameWrite);
	        	sw.Write(ob.ToString());
				sw.Close();	
			}
				
			//
			// Create the URL to download the output file 
			//
			string url = getDownloadFileUrl(muRequest, tempDir, filenameWrite);
		
			return url;
		}
		
		protected String getDownloadFileUrl(MashupRequest muRequest, string tempDir, string file) 
		{
			//
			// Determine the URL that points to the Saved File 
			//
			// IMPORTANT NOTE: 
			//
			// We backup ONE '/' from the right side of the oringinal URL in order to determine the root URL.
			// The BIG ASSUMPTION is the the initial request came in on a URL similar to this:
			//
			// http://127.0.0.1:8080/Mashup.asmx/invoke?request={}
			//
			// so backing up ONE '/' from the right gives us a root url of:
			//
		    // http://127.0.0.1:8080/Mashup.asmx
			//
			// Next Append the 'download' [Method] Invocation and arguments:
			//
			// http://127.0.0.1:8080/Mashup.asmx/download?file=&filename=
			//
			string reqUrl = System.Web.HttpContext.Current.Request.Url.GetLeftPart(UriPartial.Path);
			int length = reqUrl.LastIndexOf("/");
			string mashupUrl = (length > 0 ? reqUrl.Substring(0, length) : reqUrl);
			if (!mashupUrl.EndsWith("/")) mashupUrl += "/";
			
			//
			// Build new Mashup Download Request and return as embedded URL for the Client
			//
			MashupRequest request = new MashupRequest();
			request.service = "Mashup.File.Download";
			request.paramss["file"] = tempDir + Path.GetFileName(file);
			request.paramss["filename"] = muRequest.filename;
			request.paramss["attachment"] = muRequest.attachmentAsBoolean().ToString();
							
			string url = mashupUrl + "invoke?request=" + Uri.EscapeDataString(request.ToJson());
			
			return url;
		}
		
		protected String getMashupHeader(MashupRequest muRequest)
		{
			StringBuilder sb = new StringBuilder("");

			switch (muRequest.format)
			{
				case "extjs":
					JsonWriter jw = new JsonFx.Json.JsonWriter(sb); 
					sb.Append("{\n");
					sb.Append("  \"status\" : "); jw.Write(status); sb.Append(",\n"); 
					if (!float.IsNaN(percentComplete))
					{
						sb.Append("  \"percentComplete\" : "); jw.Write(percentComplete); sb.Append(",\n"); 
					}
					sb.Append("  \"msg\" : "); jw.Write(msg); sb.Append(",\n"); 
					sb.Append("  \"data\" : ");
					break;
			}
			return sb.ToString();
		}
		
		protected String getMashupTrailer(MashupRequest muRequest)
		{
			string trailer="";
			switch (muRequest.format)
			{
				case "extjs":
					trailer = "}";
					break;
			}
			return trailer;
		}	
		
		protected void writeMashupResponseData(MashupResponseData mrd, System.Web.HttpResponse httpResponse)
		{
			//////////////////////////////////////////////////////////////
			// Write the mrd Container sbject back out to the client
			//////////////////////////////////////////////////////////////
			if (httpResponse != null)
			{		
				// Write back Data mrd if it exits
				if (mrd.ob != null && mrd.ob.Length > 0)	    
				{
					httpResponse.ContentType = mrd.ContentType;
					httpResponse.Write(mrd.ob.ToString());	
				}
				else if (mrd.status == "ERROR")
				{
					httpResponse.TrySkipIisCustomErrors = true;
					httpResponse.StatusCode = (int)HttpStatusCode.InternalServerError;
					httpResponse.ContentType = "text/plain";
					httpResponse.Write(mrd.msg);
				}
				
	            httpResponse.Flush();
			}
		}	
	}
}