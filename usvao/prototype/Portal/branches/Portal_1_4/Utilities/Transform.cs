using System;
using System.Data;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Xml.Xsl;

using ExcelLibrary;
using ExcelLibrary.SpreadSheet;

using log4net;
using JsonFx.Json;

using VOTLib;

namespace Utilities
{
	public class Transform
	{
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		// Do not Instantiate This Class Directly : It is just a collection of static functions.
		private Transform () 
		{
		}
		
		//
		// Transformation Methods
		//
		
		#region VoTableToDataSet
		public static DataSet VoTableToDataSet(XmlReader reader)
		{			
			DataSet ds = new DataSet("VoTable");
			VOTDataSetReceiver receiver = new VOTDataSetReceiver(reader, ds);
			
			VOTParser parser = new VOTParser(reader, receiver);			
			parser.Parse();
			
			return ds;
		}
		
		// NOTE: url must be to votable file on disk
		public static DataSet VoTableFileToDataSet(string url)
		{				
			XmlTextReader reader = new XmlTextReader(url);
			DataSet ds = new DataSet("VoTable");
			VOTDataSetReceiver receiver = new VOTDataSetReceiver(reader, ds);
						
			VOTParser parser = new VOTParser(reader, receiver);			
			parser.Parse();
			
			return ds;
		}
		#endregion VoTableToDataSet
		
		#region DataSetToCSV
		public static bool DataSetToCSVFile(DataSet ds, string filename)
		{
            DataTable dt = ds.Tables[0];
            return Transform.DataTableToCSVFile(dt, filename);
		}

        public static bool DataTableToCSVFile(DataTable dt, string filename)
        {
            try
            {
                // Create the CSV file to which grid data will be exported.
                StreamWriter sw = new StreamWriter(filename, false);
				StringBuilder sb = new StringBuilder();
				Transform.DataTableToCsv(dt, sb);
				sw.Write(sb.ToString());
                sw.Close();
                return true;
            }
            catch { return false; }
        }

		public static void DataSetToCsv(DataSet ds, StringBuilder sb)
		{
            DataTable dt = ds.Tables[0];
            Transform.DataTableToCsv(dt, sb);
		}
		
		public static void DataTableToCsv(DataTable dt, StringBuilder sb)
        {			
            // First we will write the headers as a single line.
            int iColCount = dt.Columns.Count;
            for (int i = 0; i < iColCount; i++)
            {
                sb.Append(dt.Columns[i].ColumnName);
                if (i < iColCount - 1)
                {
                    sb.Append(",");
                }
            }
            sb.Append("\n");
			
			// Loop through table and create a line for each row
		    foreach (DataRow dr in dt.Rows)
		    {
		        for (int i = 0; i < dt.Columns.Count; i++)
		        {
		            if (!Convert.IsDBNull(dr[i]))
		            {
		                string val = dr[i].ToString();
		 
						//
						// NOTE: Exporting to CSV must take into account special characters:
						//
						//    Embedded double quotes in fields: Replace double quote with 2 double quotes, quote the entire string.
						//    Embedded line-feeds in fields: quote the entire string.
						//    Embedded commas in fields: quote the entire string.
						//    
						if (val.Contains("\""))
						{
							val = val.Replace("\"", "\"\"");
							val = string.Concat("\"", val, "\"");
						}
						else if (val.Contains(","))
		                {
		                    val = string.Concat("\"", val, "\"");
		                }
						else if (val.Contains("\r"))
		                {
		                    val = string.Concat("\"", val, "\"");
		                }	
		                else if (val.Contains("\n"))
		                {
		                    val = string.Concat("\"", val, "\"");
		                }
		 
		                sb.Append(val);
		            }
		 
		            if (i < dt.Columns.Count - 1)
		            {
		                sb.Append(",");
		            }
		        }
		 
		        sb.AppendLine();
		    }
        }
		#endregion DataSetToCsv
		
		#region DataSetToXml
		public static void DataSetToXml(DataSet ds, StringBuilder sb)
		{
			XmlDocument doc = DataSetToXmlDocument(ds);
			StringWriter sw = new StringWriter();
			XmlTextWriter xw = new XmlTextWriter(sw);
			doc.WriteTo(xw);
			sb.Append(sw.ToString());
		}

        public static XmlDocument DataSetToXmlDocument(DataSet ds)
        {
            DataTable dt = ds.Tables[0];
            return DataTableToXmlDocument(dt);
        }
		
		public static void DataTableToXml(DataTable dt, StringBuilder sb)
		{
			XmlDocument doc = DataTableToXmlDocument(dt);
			StringWriter sw = new StringWriter();
			XmlTextWriter xw = new XmlTextWriter(sw);
			doc.WriteTo(xw);
			sb.Append(sw.ToString());
		}

        public static XmlDocument DataTableToXmlDocument(DataTable dt)
        {
            // 
            // Generate the XML to look like the following:
            //
            // <Tables>
            //    <Rows>
            //        <objid>123456789</objid>
            //        <ra>9.0</ra>
            //        <dec>-43.0</dec>
            //    </Rows>
            // </Tables>
            //
            if (dt.DataSet == null)
            {
                DataSet ds = new DataSet("Tables");
                ds.Tables.Add(dt);
                dt.TableName = "Rows";
            }
            else
            {
                dt.DataSet.DataSetName = "Tables";
                dt.TableName = "Rows";
            }

            System.IO.StringWriter sw = new System.IO.StringWriter();
            dt.WriteXml(sw);
            sw.Close();
            XmlDocument xd = new XmlDocument();
            xd.LoadXml(sw.ToString());
            return xd;
        }
		#endregion DataSetToXml
		
		#region DataSetToVoTable
		public static void DataSetToVoTable(DataSet ds, StringBuilder sb)
		{
			XmlDocument doc = DataSetToVoTable(ds);
			StringWriter sw = new StringWriter();
			XmlTextWriter xw = new XmlTextWriter(sw);
			doc.WriteTo(xw);
			sb.Append(sw.ToString());
		}

        public static XmlDocument DataSetToVoTable(DataSet ds)
        {
            return new VoTableDocument(ds, ds.DataSetName, "UNKNOWN", "1.2.3");
        }
		
		public static void DataTableToVoTable(DataTable dt, StringBuilder sb)
		{
			XmlDocument doc = DataTableToVoTable(dt);
			StringWriter sw = new StringWriter();
			XmlTextWriter xw = new XmlTextWriter(sw);
			doc.WriteTo(xw);
			sb.Append(sw.ToString());
		}

        public static XmlDocument DataTableToVoTable(DataTable dt)
        {
            return new VoTableDocument(dt, dt.TableName, "UNKNOWN", "1.2.3");
        }
		#endregion DataSetToVoTable
		
		#region DataSetToHtml
		public static void DataSetToHtml(DataSet ds, StringBuilder sb)
		{				
			// create a string writer
			using (StringWriter sw = new StringWriter())
			{
				using (HtmlTextWriter htw = new HtmlTextWriter(sw))
				{
					// instantiate a datagrid
					DataGrid dg = new DataGrid();
					dg.DataSource = ds.Tables[0];
					dg.DataBind();
					dg.RenderControl(htw);
				}
				sb.Append(sw.ToString());
			}
		}
		#endregion
		
		public static void DataSetToXls(DataSet ds, out Workbook wb)
		{
			wb = ExcelLibrary.DataSetHelper.CreateWorkbook(ds);
		}

        #region DataSetToJson
        public static void DataSetToJson(DataSet ds, StringBuilder sb)
        {
			sb.Append("{ ");              					// DataSet root
			
			if (ds != null && ds.Tables != null && ds.Tables.Count > 0)
			{
				// Optional DataSet Name
				if (ds.DataSetName != null && ds.DataSetName.Length > 0)
				{
					sb.Append("\"name\":" + "\"" + ds.DataSetName + "\",\n");
				}
				
				// Start appending the DataTable(s) in the DataSet
	            sb.Append("\"Tables\":[");   					// Tables []
				
				string tabsep = "";
	            foreach (DataTable dt in ds.Tables)
				{
					sb.Append(tabsep);tabsep="\n,\n";			// (table separator)
	            	DataTableToJson(dt, sb);					// Table[i]
				}
				
				sb.Append("]"); 								// Tables []
			}
			
			sb.Append("}");									// DataSet root
        }

        public static void DataTableToJson(DataTable dt, StringBuilder sb )
        {
			sb.Append("{ ");							// Table root

            if (dt != null && dt.Rows != null && dt.Rows.Count > 0)
            {			
				// Optional DataTable Name
				if (dt.TableName != null && dt.TableName.Length > 0)
				{
					sb.Append("\"name\":" + "\"" + dt.TableName + "\",");
				}
				
				// DataTable Extended Properties
								
				// DataTable Extended Properties
				sb.Append("\n");
				// Needed for the side-effect of ensuring that the page properties are in dt.ExtendedProperties
				DataTableExtendedProperties.getProperties(dt);  				
				
				StringBuilder jwsb = new StringBuilder("\"ExtendedProperties\":");
				JsonWriter jw = new JsonFx.Json.JsonWriter(jwsb);
				jw.Write(dt.ExtendedProperties);
				sb.Append(jwsb.ToString() + ",");
				
				sb.Append("\n");
                sb.Append("\"Rows\":[ \n");					// Rows[]
				
				string rowsep = "";
                foreach (DataRow row in dt.Rows)
                {
					sb.Append(rowsep); rowsep=",\n";
                    sb.Append("  {");
					string colsep = "";
                    foreach (DataColumn col in dt.Columns)
                    {
						if (!checkRemoveColumn(col))
						{
							sb.Append(colsep); colsep = ","; 						    // (column separtor)
							sb.Append("\"" + col.ColumnName.ToString() + "\" : ");   	// "name" :
							sb.Append(DataRowValueToJson(row[col], col.DataType));  	// "value"
						}
                    }
					sb.Append("}");
                }
				
				sb.Append("\n");
                sb.Append("]");								// Rows[]
            }
			
			sb.Append("}");								// Table root

        }
        #endregion  DataSetToJson
		
		#region DataSetToExtJs
		public static void DataSetToExtjs(DataSet ds, StringBuilder sb)
        {
			sb.Append("{ ");              					// DataSet root
			
			if (ds != null && ds.Tables != null && ds.Tables.Count > 0)
			{
				// Optional DataSet Name
				if (ds.DataSetName != null && ds.DataSetName.Length > 0)
				{
					sb.Append("\"name\":" + "\"" + ds.DataSetName + "\",\n");
				}
				
				// Start appending the DataTable(s) in the DataSet
	            sb.Append("\"Tables\":[");   					// Tables []
				
				string tabsep = "";
	            foreach (DataTable dt in ds.Tables)
				{
					sb.Append(tabsep);tabsep="\n,\n";			// (table separator)
	            	DataTableToExtjs(dt, sb);					// Table[i]
				}
				
				sb.Append("]"); 								// Tables []
			}
			
			sb.Append("}");									// DataSet root
		}
        		
        public static void DataTableToExtjs(DataTable dt, StringBuilder sb)
        {
			sb.Append("{ ");              				// Table root

            if (dt != null && dt.Columns != null && dt.Rows != null)
            {			
				// Optional DataTable Name
				if (dt.TableName != null && dt.TableName.Length > 0)
				{
					sb.Append("\"name\":" + "\"" + dt.TableName + "\",");
				}
				
				// DataTable Extended Properties
				sb.Append("\n");
				// Needed for the side-effect of ensuring that the page properties are in dt.ExtendedProperties
				DataTableExtendedProperties.getProperties(dt);  				
				
				StringBuilder jwsb = new StringBuilder("\"ExtendedProperties\":");
				JsonWriter jw = new JsonFx.Json.JsonWriter(jwsb);
				jw.Write(dt.ExtendedProperties);
				sb.Append(jwsb.ToString() + ",");
				
				//
				// Generate all the Fields to look like the following:
				//
				//	Columns: [
				//       {text: 'company',    dataIndex: 'company',    ExtendedProperties: {}},
				//       {text: 'price',      dataIndex: 'price',      ExtendedProperties: {}},
				//       {text: 'change',     dataIndex: 'change',     ExtendedProperties: {}},
				//       {text: 'pctChange',  dataIndex: 'pctChange',  ExtendedProperties: {}},
				//       {text: 'lastChange', dataIndex: 'lastChange', ExtendedProperties: {}}
				//	 ]
				//
				sb.Append("\n");
				sb.Append("\"Columns\":[ \n"); 				// Columns[]
				
				string colsep = "";
				foreach (DataColumn col in dt.Columns)
                {
					if (!checkRemoveColumn(col))
					{
						sb.Append(colsep); colsep = ",\n";		// (column separator)
	                    sb.Append("  {");            			// col[i]
						sb.Append("\"text\": \"" + col.ColumnName + "\", ");
						sb.Append("\"dataIndex\": \"" + makeLegalName(col.ColumnName) + "\"");
					
						//
						// Append Extended Properties (if present)
						//
						System.Data.PropertyCollection properties = col.ExtendedProperties;
						if (properties != null && properties.Count > 0)
						{
							sb.Append(",\n");

                            StringBuilder coljwsb = new StringBuilder("\"ExtendedProperties\":");
                            JsonWriter coljw = new JsonFx.Json.JsonWriter(coljwsb);
                            coljw.Write(properties);
                            sb.Append(coljwsb.ToString());
						}
						sb.Append("}");         				// col[i]
					}
				}
				
				sb.Append("],");  							// Columns[]
				
				//
				// Generate all the Fields to look like the following:
				//
				//	Fields: [
				//       {name: 'company'},
				//       {name: 'price',      type: 'float'},
				//       {name: 'change',     type: 'float'},
				//       {name: 'pctChange',  type: 'float'},
				//       {name: 'lastChange', type: 'date', dateFormat: 'n/j h:ia'}
				//	 ]
				//
				
				sb.Append("\n");
				sb.Append("\"Fields\":[ \n");				// Fields[]
				
				string fieldsep = "";
				foreach (DataColumn col in dt.Columns)
                {
					if (!checkRemoveColumn(col))
					{
						sb.Append(fieldsep); fieldsep=",\n";	// (field separator)
	                    sb.Append("  {");           			// field[i]
						sb.Append("\"name\": \"" + makeLegalName(col.ColumnName) + "\", ");
						string t = Transform.SytemToExtjsType(col.DataType, checkTreatNumeric(col));
						sb.Append("\"type\": \"" + t + "\"");

						// This is for round-tripping.  ExtJS does not have a "long" type, but we want to tell the server
						// that this should be a "long" if the data comes back, such as in an export.
						if (col.DataType.ToString().Equals("System.Int64") && t.Equals("int")) {
							t = "long";
							sb.Append(",\"serverType\": \"" + t + "\"");
						}

						sb.Append("}");            				// field[i]
					}
				}
				
				sb.Append("],");  							// Fields[]
				
				//
				// Generate all the Rows to look like the following:
				//
				//	 Rows: [
				//        ['3m Co',                               71.72, 0.02,  0.03,  '9/1 12:00am'],
				//        ['Alcoa Inc',                           29.01, 0.42,  1.47,  '9/1 12:00am'],
				//        ['Altria Group Inc',                    83.81, 0.28,  0.34,  '9/1 12:00am'],
				//        ['American Express Company',            52.55, 0.01,  0.02,  '9/1 12:00am'],
				//        ['American International Group, Inc.',  64.13, 0.31,  0.49,  '9/1 12:00am'],
				//        ['Wal-Mart Stores, Inc.',               45.45, 0.73,  1.63,  '9/1 12:00am']
				//    ]
				//
				
				sb.Append("\n");
                sb.Append("\"Rows\":[ \n");					// Rows []
				
				string rowsep = "";
                foreach (DataRow row in dt.Rows)
                {
					sb.Append(rowsep); rowsep = ",\n";		// (row separator)
                    sb.Append("  [");						// row[i]
					colsep = "";
                    foreach (DataColumn col in dt.Columns)
                    {
						if (!checkRemoveColumn(col))
						{
							sb.Append(colsep); colsep = ",";							// (column separator)
							// For now we treat int64s as strings unless explicitly told by columns config
							// to treat them numerically.  That is mostly because not all int64s can be
							// represented accurately in JavaScript, so round-tripping a table with such values
							// could change the table.
							Type t = col.DataType;
							if ((col.DataType.Name.ToString() == "Int64") && !checkTreatNumeric(col)) {
								t = typeof(String);
							}
							sb.Append(DataRowValueToJson(row[col], t));   	// column value
						}
                    }
					
					sb.Append("]"); 						// row[i]
                }

                sb.Append("]\n");  							// Rows []	
            }
			sb.Append("}");  							// Table root
        }
		#endregion  DataSetToExtJs
		
		#region ExtJsToDataSet
		public static DataSet ExtJsToDataSet(string json)
		{
			Dictionary<string, object> table = null;
			
			// Ensure the key values are set in the request
			if (json == null || json.Trim().Length == 0)
			{
				throw new Exception ("json table is empy.");
			}
			else 
			{
				// Decode the json data into a table object
				object d = new JsonFx.Json.JsonReader(json).Deserialize();
				
				// Validate the "table" object points to a valid Table
				if (d is Dictionary<string, object>)
				{
					table = d as Dictionary<string, object>;
				}
				else
				{
					throw new Exception ("json table is unexpected type = " + d.GetType());
				}
			}
			
			return ExtJsDictionaryToDataSet(table);
		}
					
		public static DataSet ExtJsDictionaryToDataSet(Dictionary<string, object> table)
		{
			Array rows = null;
			Array cols = null;
			Array fields = null;
				
			//
			// Step 1: Validate Input table
			//
			object orows = null, ocols = null, ofields = null;
			if (table.TryGetValue("Rows", out orows) && orows is Array)
			{
				rows = orows as Array;
			}
			else
			{
				throw new Exception ("Table Rows[] attribute is missing or unexpected data type(s): " + (orows != null ? orows.GetType().ToString() : " Missing"));
			}

			if (table.TryGetValue("Columns", out ocols) && ocols is Array)
			{
				cols = ocols as Array;
			}
			else
			{
				throw new Exception ("Table Columns[] attribute is missing or unexpected data type(s): " +  (ocols != null ? ocols.GetType().ToString() : " Missing"));
			}
			
			if (table.TryGetValue("Fields", out ofields) && ofields is Array)
			{
				fields = ofields as Array;
			}
			else
			{
				throw new Exception ("Table Fields[] attribute is missing are unexpected data type(s): " + (ofields != null ? ofields.GetType().ToString() : " Missing"));
			}
				
			//
			// Step 2: Create new DataSet/DataTable
			//
			DataSet ds = new DataSet("DataSet - Created " + DateTime.Now);
			
			string tname = (table["name"] != null ? table["name"].ToString() : "DataTable - Created : " + DateTime.Now);
			DataTable dt = ds.Tables.Add(tname);
			
			//
			// Step 3: Create DataTable Columns 
			//
			foreach (object f in fields)
			{
				if (f is Dictionary<string, object>)
				{
					Dictionary<string, object> fd = f as Dictionary<string, object>;
					object fname=null, ftype=null, fservtype=null;
					if (fd.TryGetValue("name", out fname) && fd.TryGetValue("type", out ftype))
					{
						if (fd.TryGetValue("serverType", out fservtype)) {
							ftype = fservtype;
						}
						Type type = Transform.ExtJsToSystemType(ftype.ToString());
						dt.Columns.Add(fname.ToString(), type);
					}
					else
					{
						log.Warn(tid + "Table Field Element is missing attributes: 'name' or 'type' : " + f.ToString());
					}
				}
			}
			
			//
			// Step 4: Append Column Extended Properties
			//
			foreach (object col in cols)
			{
				if (col is Dictionary<string, object>)
				{
					Dictionary<string, object> cd = col as Dictionary<string, object>;
					object o;
					if (cd.TryGetValue("ExtendedProperties", out o) && o != null && o is Dictionary<string, object>)
					{
						Dictionary<string, object> ep = o as Dictionary<string, object>;
						
						if (cd.TryGetValue("dataIndex", out o) && o != null && o is String)
						{
							string colname = o as String;
							if (dt.Columns.Contains(colname))
							{
								System.Data.DataColumn dtcol = dt.Columns[colname];
								Transform.AppendExtendedProperties(dtcol, ep, null);
							}
						}
					}
				}
			}
			
			//
			// Step 5: Create DataTable Rows
			//
			foreach (object row in rows)
			{
				if (row is object[])
				{
					object[] objectRow = (row as object[]);
					if (objectRow.Length == dt.Columns.Count)
					{
						Object[] convertedRow = new Object[dt.Columns.Count];
						for (int i=0; i<objectRow.Length; i++)
						{					
							Type colType = dt.Columns[i].DataType;
							if (objectRow[i] != null && objectRow[i].GetType() != colType)
							{
								try {
									convertedRow[i] = Convert.ChangeType(objectRow[i], colType);
								} catch (Exception) {
									log.WarnFormat (tid + "Could not convert column {0} <{1}> data item <{2}> to type <{3}>.  Data ignored.", i, dt.Columns[i].ColumnName, objectRow[i], colType);
									convertedRow[i] = null;
								}
							}
							else
							{
								convertedRow[i] = objectRow[i];
							}	
						}
						
						// Add the converted row to the DataTable
						dt.Rows.Add(convertedRow);
					}
					else
					{
						log.Warn(tid + "Row count does not match Column Count for row, skipping row : " + row.ToString());
					}
				}
				else
				{
					log.Warn(tid + "Row element is not array of values, skipping row : " + row.ToString());
				}
			}
			
			return ds;
		}
		#endregion
		
		//////////////////////////////////////////////////////////////////
		//
		// Helper Methods
		//
		//////////////////////////////////////////////////////////////////
		
		#region SytemToExtjsType
		// TODO: Refactor these 2 methods to reuse the same data structure linking names <-> types
		public static string SytemToExtjsType(System.Type type, bool treatNumeric)
		{
			//
			// Insert the Ext Type, which is one of:
			//
			//    auto (Default, implies no conversion)
			//    string
			//    int
			//    float
			//    boolean
			//    date
			//
			string extjsType="auto";
			
			switch (type.ToString())
			{
				case "System.String":
				{
					extjsType = "string"; break;
				}
				case "System.Byte":
				case "System.Int16":
				case "System.Int32":
				case "System.UInt16":
				case "System.UInt32":
				{
					extjsType = "int"; break;
				}				
				case "System.Int64":
				case "System.UInt64":
				{
					if (treatNumeric) {
						extjsType = "int";
					} else {
						extjsType = "string"; 
					}
					break;
				}
				case "System.Single":
				case "System.Double":
				case "System.Decimal":
				{
					extjsType = "float"; break;
				}
				case "System.Boolean":
				{
					extjsType = "boolean"; break;
				}
				case "System.DateTime":
				{
					extjsType = "date"; break;
				}
			 	default:
				{
					log.Warn(tid + "Unkown Datatype = " + type.ToString() + ". Using default extjs type of 'auto'"); 
					break;
				}
			}
			
			return extjsType;			
		}
		#endregion
		
		#region ExtJsToSystemType
		public static Type ExtJsToSystemType(string extjsType)
		{
			//
			// Insert the Ext Type, which is one of:
			//
			//    auto (Default, implies no conversion)
			//    string
			//    int
			//    float
			//    boolean
			//    date
			//
			Type type=typeof(System.String);
			
			switch (extjsType.Trim().ToString())
			{
				case "string":
				{
					type = typeof(System.String);
					break;
				}
				case "int":
				{
					type = typeof(System.Int32);
					break;
				}
				case "long":
				{
					type = typeof(System.Int64);
					break;
				}
				case "float":
				{
					type = typeof(System.Double);
					break;
				}
				case "boolean":
				{
					type = typeof(System.Boolean);
					break;
				}
				case "date":
				{
					type = typeof(System.String);
					break;
				}
			 	default:
				{
					log.Warn(tid + "Unkown extjs type = " + extjsType.ToString() + ". Using default type of String."); 
					break;
				}
			}
			
			return type;			
		}
        #endregion 
		
		#region DataRowValueToJson
		// http://msdn.microsoft.com/en-us/library/system.data.datacolumn.datatype(VS.80).aspx
	    private static Type[] numeric = new Type[] {typeof(byte), typeof(decimal), typeof(double), 
	                                     			typeof(Int16), typeof(Int32), typeof(Int64),
	                                     			typeof(UInt16), typeof(UInt32), typeof(UInt64),
													typeof(SByte), typeof(Single), typeof(Double)};
	
	    // I don't want to rebuild this value for every date cell in the table
	    private static long EpochTicks = new DateTime(1970, 1, 1).Ticks;
	
	    private static string DataRowValueToJson(object value, Type DataType)
	    {
	        // null
	        if (value == DBNull.Value) return "null";
	
	        // numeric
	        if (Array.IndexOf(numeric, DataType) > -1)
	            return value.ToString(); // TODO: eventually want to use a stricter format
	
	        // boolean
	        if (DataType == typeof(bool))
	            return ((bool)value) ? "true" : "false";
	
	        // date -- see http://weblogs.asp.net/bleroy/archive/2008/01/18/dates-and-json.aspx
            if (DataType == typeof(DateTime))
                return new TimeSpan(((DateTime)value).ToUniversalTime().Ticks - EpochTicks).TotalMilliseconds.ToString();
                //return "\"\\/Date(" + new TimeSpan(((DateTime)value).ToUniversalTime().Ticks - EpochTicks).TotalMilliseconds.ToString() + ")\\/\"";
	
	        // TODO: add Timespan support
	        // TODO: add Byte[] support
	
	        // string/char  
	        return "\"" + value.ToString().					// TODO: this would be _much_ faster with a state machine
					Replace(@"\", @"\\").
					Replace("\r\n", @"\n").
					Replace("\n", @"\n").
					Replace("\"", @"\""") + "\"";
	    }
		#endregion  DataRowValueToJson
		
		#region check extended properties
		public static Boolean checkRemoveColumn(DataColumn col)
		{
			Boolean bRemove = false;
			if (col != null && col.ExtendedProperties != null && col.ExtendedProperties.ContainsKey("cc.remove"))
			{
				Object o = col.ExtendedProperties["cc.remove"];
				bRemove = Convert.ToBoolean(o);	
			}
			return bRemove;
		}		

		public static Boolean checkTreatNumeric(DataColumn col)
		{
			Boolean bTreatNumeric = false;
			if (col != null && col.ExtendedProperties != null && col.ExtendedProperties.ContainsKey("cc.treatNumeric"))
			{
				Object o = col.ExtendedProperties["cc.treatNumeric"];
				bTreatNumeric = Convert.ToBoolean(o);	
			}
			return bTreatNumeric;
		}
		#endregion
		
		#region makeLegalName
		// This needed for the ExtJS export, because ExtJS has trouble with grid field names that contain
		// periods.
		public static string makeLegalName(string colName)
		{
			string legalName = colName.Replace('.', '_');
			return legalName;
		}
		#endregion  makeLegalName
		
		#region Append[Columns,Properties,ExtendedProperties]
		public static void AppendColumns(System.Data.DataSet ds, Dictionary<string, object>columnProperties)
		{
			foreach (string colname in columnProperties.Keys)
			{
				Dictionary<string, object> props = columnProperties[colname] as Dictionary<string, object>;
				Object oAdd;
				Object oType;
				if (props.TryGetValue("add", out oAdd))
				{
					if (Convert.ToBoolean(oAdd))
					{
						if (props.TryGetValue("type", out oType))
						{
							Type type = Transform.ExtJsToSystemType(Convert.ToString(oType));
							ds.Tables[0].Columns.Add(colname, type);
						}
						else
						{
							ds.Tables[0].Columns.Add(colname);
						}
					}
				}
			}
		}
		
		public static void AppendColumnProperties(System.Data.DataSet ds, Dictionary<string, object>columnProperties, string prefix)
		{
			foreach (DataTable dt in ds.Tables)
			{
				foreach (DataColumn col in dt.Columns)
				{
					string colname = col.ColumnName;
					Object o;
					if (columnProperties.TryGetValue(colname, out o) && o != null && o is Dictionary<string, object>)
					{
						Dictionary<string, object> ep = o as Dictionary<string, object>;
						AppendExtendedProperties(col, ep, prefix);
					}
				}
			}
		}
		
		public static void AppendExtendedProperties (System.Data.DataColumn col, Dictionary<string, object>extendedProperties, string prefix)
		{
			Dictionary<string, object> ep = extendedProperties;
			foreach (string key in ep.Keys)
			{
				if (!col.ExtendedProperties.ContainsKey(key))
				{
					string keyName = (prefix != null) ? prefix + "." + key : key;
					col.ExtendedProperties.Add(keyName, ep[key]);
				}
			}
		}
		#endregion 
	}
}

