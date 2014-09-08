using System;
using System.Data;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Text;
using System.Collections;
using System.Collections.Generic;

using JsonFx.Json;

using VOTLib;

namespace Utilities
{
	public class Transform
	{
		// Do not Instantiate This Class Directly : It is just a collection of static functions.
		private Transform () 
		{
		}
		
		#region VoTableToDataSet
		public static DataSet VoTableToDataSet(XmlTextReader reader)
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
            try
            {
                // Create the CSV string buffer to which grid data will be exported.
				
                // First we will write the headers.
                int iColCount = dt.Columns.Count;
                for (int i = 0; i < iColCount; i++)
                {
                    sb.Append(dt.Columns[i]);
                    if (i < iColCount - 1)
                    {
                        sb.Append(",");
                    }
                }
                sb.Append("\n");
				
                // Now write all the Rows.
                foreach (DataRow dr in dt.Rows)
                {
                    for (int i = 0; i < iColCount; i++)
                    {
                        if (!Convert.IsDBNull(dr[i]))
                        {
                            sb.Append(dr[i].ToString());
                        }
                        if (i < iColCount - 1)
                        {
                            sb.Append(",");
                        }
                    }
                   sb.Append("\n");
                }
            }
            catch { }
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
						if (!removeColumn(col))
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
		
		#region ExtJsToDataSet
		public static DataSet JsonToDataSet(string json)
		{
			Dictionary<string, object> table = null;
			Array rows = null;
			Array cols = null;
			Array fields = null;
			
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
					
					// Now, validate both Rows and Columns exist
					object orows = null, ocols = null, ofields = null;
					if (table.TryGetValue("Rows", out orows) && orows is Array)
					{
						rows = orows as Array;
					}
					else
					{
						throw new Exception ("Mashup Table Importer: request.params.data table Rows[] attribute is missing or unexpected data type(s): " + (orows != null ? orows.GetType().ToString() : " Missing"));
					}
	
					if (table.TryGetValue("Columns", out ocols) && ocols is Array)
					{
						cols = ocols as Array;
					}
					else
					{
						throw new Exception ("Mashup Table Importer: request.params.data table Columns[] attribute is missing unexpected data type(s): " +  (ocols != null ? ocols.GetType().ToString() : " Missing"));
					}
					
					if (table.TryGetValue("Fields", out ofields) && ofields is Array)
					{
						fields = ofields as Array;
					}
					else
					{
						throw new Exception ("Mashup Table Importer: request.params.data table Files[] attribute is missing are unexpected data type(s): " + (ofields != null ? ofields.GetType().ToString() : " Missing"));
					}
				}
				else
				{
					throw new Exception ("Mashup Table Importer: request.params.data decoded type is unknown: " + d.GetType() + ".  Expecting type Dictionary.");
				}
			}
			
			//
			// If we got here then table Dictionary contains some Rows and Columns
			// So let's start importing them into a new DataSet.
			//
			DataSet ds = new DataSet("DataSet - Imported " + DateTime.Now);
			
			string name = (table["name"] != null ? table["name"].ToString() : "DataTable - Imported " + DateTime.Now);
			DataTable dt = ds.Tables.Add(name);
			
			foreach (object f in fields)
			{
				if (f is Dictionary<string, object>)
				{
					dt.Columns.Add();
				}
			}
			
			foreach (object r in rows)
			{
				if (r is object[])
				{
					// r is another dict
				}
			}
			
			return ds;
		}
		#endregion
		
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
	            	DataTableToExtjs(dt, sb);			// Table[i]
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
					if (!removeColumn(col))
					{
						sb.Append(colsep); colsep = ",\n";		// (column separator)
	                    sb.Append("  {");            			// col[i]
						sb.Append("\"text\": \"" + col.ColumnName + "\", ");
						sb.Append("\"dataIndex\": \"" + makeLegalName(col.ColumnName) + "\"");
					
						//
						// Append Extended Properties (if present)
						//
						System.Data.PropertyCollection props = col.ExtendedProperties;
						if (props != null && props.Count > 0)
						{
							sb.Append(",\n");
							sb.Append("    \"ExtendedProperties\" : ");
							sb.Append("{");
							
							string keysep = "";
							foreach (string key in props.Keys)
							{
								sb.Append(keysep); keysep = ",";	// (key seperator)
								sb.Append("\"" + key + "\": ");		// name :
								sb.Append(DataRowValueToJson(props[key], props[key].GetType()));  	// value
							}
							sb.Append("}");
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
					if (!removeColumn(col))
					{
						sb.Append(fieldsep); fieldsep=",\n";	// (field separator)
	                    sb.Append("  {");           			// field[i]
						sb.Append("\"name\": \"" + makeLegalName(col.ColumnName) + "\", ");
						sb.Append("\"type\": \"" + Transform.getExtjsFieldType(col.DataType) + "\"");
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
						if (!removeColumn(col))
						{
							sb.Append(colsep); colsep = ",";							// (column separator)
							sb.Append(DataRowValueToJson(row[col], col.DataType));   	// column value
						}
                    }
					
					sb.Append("]"); 						// row[i]
                }

                sb.Append("]\n");  							// Rows []	
            }
			sb.Append("}");  							// Table root
        }
		
		public static string getExtjsFieldType(System.Type t)
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
			string type="auto";
			
			switch (t.ToString())
			{
				case "System.String":
				{
					type = "string"; break;
				}
				case "System.Int16":
				case "System.Int32":
				case "System.Int64":
				case "System.UInt16":
				case "System.UInt32":
				case "System.UInt64":
				{
					type = "int"; break;
				}
				case "System.Single":
				case "System.Double":
				{
					type = "float"; break;
				}
				case "System.Boolean":
				{
					type = "boolean"; break;
				}
				case "System.DateTime":
				{
					type = "date"; break;
				}
			 	default:
				{
					Console.WriteLine("getExtjsFieldType() Unkown Datatype = " + t.ToString()); break;
				}
			}
			
			return type;			
		}
        #endregion  DataSetToExtJs
		
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
	            return "\"\\/Date(" + new TimeSpan(((DateTime)value).ToUniversalTime().Ticks - EpochTicks).TotalMilliseconds.ToString() + ")\\/\"";
	
	        // TODO: add Timespan support
	        // TODO: add Byte[] support
	
	        // string/char  
	        return "\"" + value.ToString().					// TODO: this would be _much_ faster with a state machine
					Replace(@"\", @"\\").
					Replace(Environment.NewLine, @"\n").
					Replace("\"", @"\""") + "\"";
	    }
		#endregion  DataRowValueToJson
		
		#region removeColumn
		public static Boolean removeColumn(DataColumn col)
		{
			Boolean bRemove = false;
			if (col != null && col.ExtendedProperties != null && col.ExtendedProperties.ContainsKey("cc.remove"))
			{
				Object o = col.ExtendedProperties["cc.remove"];
				bRemove = Convert.ToBoolean(o);	
			}
			return bRemove;
		}
		#endregion  removeColumn
		
		#region makeLegalName
		// This needed for the ExtJS export, because ExtJS has trouble with grid field names that contain
		// periods.
		public static string makeLegalName(string colName)
		{
			string legalName = colName.Replace('.', '_');
			return legalName;
		}
		#endregion  makeLegalName
		
		#region AppendProperties
		public static void AppendProperties(System.Data.DataSet ds, Dictionary<string, object>properties, string prefix)
		{
			foreach (DataTable dt in ds.Tables)
			{
				foreach (DataColumn col in dt.Columns)
				{
					string name = col.ColumnName;
					Object o;
					if (properties.TryGetValue(name, out o) && o is Dictionary<string, object>)
					{
						Dictionary<string, object> d = o as Dictionary<string, object>;
						foreach (string key in d.Keys)
						{
							if (!col.ExtendedProperties.ContainsKey(key))
							{
								string keyName = (prefix != null) ? prefix + "." + key : key;
								col.ExtendedProperties.Add(keyName, d[key]);
							}
						}
					}
				}
			}
		}
		#endregion  AppendProperties
	}
}

