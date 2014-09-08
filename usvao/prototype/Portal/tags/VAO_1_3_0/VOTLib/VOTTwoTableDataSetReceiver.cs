using System;
using System.IO;
using System.Text;
using System.Xml;
using System.Data;
using System.Collections;
using System.Collections.Generic;

namespace VOTLib
{
	public class VOTTwoTableDataSetReceiver : VOTReceiver
	{

		#region Members

		public readonly DataSet VOTDataSet = null;

		const string COLUMN_TABLE_PREFIX = "Columns-";
		const string DATA_TABLE_PREFIX = "Rows-";

		// Types
		static readonly Type DS_STRING = Type.GetType ("System.String");
		static readonly Type DS_BOOLEAN = Type.GetType ("System.Boolean");
		static readonly Type DS_INT16 = Type.GetType ("System.Int16");
		static readonly Type DS_INT32 = Type.GetType ("System.Int32");
		static readonly Type DS_INT64 = Type.GetType ("System.Int64");
		static readonly Type DS_SINGLE = Type.GetType ("System.Single");
		static readonly Type DS_DOUBLE = Type.GetType ("System.Double");

		static Dictionary<string, Type> typeMapping = null;

		XmlTextReader reader = null;

		#endregion

		public VOTTwoTableDataSetReceiver (XmlTextReader iReader, DataSet iDataSet)
		{
			reader = iReader;
			VOTDataSet = iDataSet;
			
			initializeTypeMapping ();
		}

		#region VOTReceiver implementation

		#region Message-handling Methods
		public void Debug (string format, params Object[] args)
		{
			Console.Write ("Debug: " + format, args);
		}

		public void Informational (string format, params Object[] args)
		{
			Console.Write ("Info: " + format, args);
		}

		public void Warning (string format, params Object[] args)
		{
			Console.Write ("Warning: " + format, args);
			Console.Write ("   at line {0}, position {1}", reader.LineNumber, reader.LinePosition);
		}

		public void Error (string format, params Object[] args)
		{
			Console.Write ("Error: " + format, args);
			Console.Write ("   at line {0}, position {1}", reader.LineNumber, reader.LinePosition);
		}

		#endregion

		public void VOTableBegin (System.Collections.Generic.List<int> treeLocation, PropertyCollection attributes, string description)
		{
			
		}

		public void Definitions(List<int> treeLocation, PropertyCollection attributes, string content)
		{
			
		}

		public void Coosys (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			
		}


		public void Info (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			
		}


		public void Param (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			
		}


		public void Group (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			
		}


		public void Link (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			
		}


		public void Resource (System.Collections.Generic.List<int> treeLocation, PropertyCollection attributes, string description)
		{
			
		}


		public void Table (System.Collections.Generic.List<int> treeLocation, PropertyCollection attributes, string description)
		{
			// For this table, create two DataTables, one for the columns, one for the actual data.
			DataTable columnTable = new DataTable (columnTableName (treeLocation));
			DataColumn nameColumn = new DataColumn ("name");
			columnTable.Columns.Add (nameColumn);
			columnTable.PrimaryKey = new DataColumn[] { nameColumn };
			VOTDataSet.Tables.Add (columnTable);
			
			VOTDataSet.Tables.Add (dataTableName (treeLocation));
		}


		public void Field (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string description, string values, List<PropertyCollection> fieldLinks)
		{
			// A field was recognized.  Add an entry in the column table, and a column in the data table.
			
			// Column table
			DataTable columnTable = VOTDataSet.Tables[columnTableName (treeLocation)];
			if (columnTable != null) {
				
				// Find the name of the field.
				string name = "";
				if ((name = (string)attributes["name"]) == null) {
					// Although name is a required attribute for the field we didn't find one,
					// so we'll make one up.  Maybe try to find an id first?
					if ((name = (string)attributes["ID"]) == null) {
						// Now something bogus.
						name = "UnnamedField";
					}
				}
				
				// Make sure the name is unique among fields so far.
				int repeater = 1;
				string orig = name;
				while (columnTable.Rows.Find (name) != null) {
					// We already have a row with that name.  Try modifying it.
					name = orig + "-" + repeater++;
				}
				
				// Make sure we have all the necessary columns.
				foreach (DictionaryEntry kv in attributes) {
					if (!columnTable.Columns.Contains ((string)kv.Key)) {
						// The column didn't exist so add it.
						columnTable.Columns.Add ((string)kv.Key);
					}
				}
				
				// Add the row for the new field.
				DataRow newRow = columnTable.NewRow ();
				foreach (DictionaryEntry kv in attributes) {
					if (kv.Key.Equals ("name")) {
						// We might have modified the name to ensure uniqueness, so use the modified verion.
						newRow["name"] = name;
					} else {
						newRow[(string)kv.Key] = kv.Value;
					}
				}
				columnTable.Rows.Add (newRow);
				
				// Add the column for this field to the data table.
				DataTable dataTable = VOTDataSet.Tables[dataTableName (treeLocation)];
				if (dataTable != null) {
					string datatype = (string)attributes["datatype"];
					string arraysize = (string)attributes["arraysize"];
					dataTable.Columns.Add (name, getDataSetType (datatype, arraysize));
				}
				
			}
			
		}


		public void Tr (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, System.Collections.Generic.List<string> dataValues)
		{
			DataTable dataTable = VOTDataSet.Tables[dataTableName (treeLocation)];
			if (dataTable != null) {
				int numColumns = dataTable.Columns.Count;
				if (numColumns == dataValues.Count) {
					// The incoming data has the same number of values as the table has columns.  That's good.  :)
					
					// Convert the string data values to their appropriate types.
					Object[] convertedData = new Object[numColumns];
					int i = 0;
					IEnumerator columnEnum = dataTable.Columns.GetEnumerator ();
					foreach (string dataVal in dataValues) {
						columnEnum.MoveNext ();
						Type targetType = ((DataColumn)columnEnum.Current).DataType;
						try {
							convertedData[i] = convert (dataVal, targetType);
						} catch (Exception e) {
							Warning ("Exception {0}\nCould not convert column {1} data <{2}> to type <{3}>.  Data ignored.", e, i, dataVal, targetType);
							convertedData[i] = null;
						} finally {
							i++;
						}
					}
					
					// Add the converted data array all at once as the whole row of data.					
					dataTable.Rows.Add (convertedData);
				} else {
					Error ("Data row has {0} values where {1} are expected.", numColumns, dataValues.Count);
				}
			}
		}


		public void Binary (System.Collections.Generic.List<int> treeLocation, PropertyCollection fitsAttributes, PropertyCollection streamAttributes, string streamVal)
		{
			Error ("BINARY tag processing not implemented.  BINARY data ignored.");
		}


		public void Fits (System.Collections.Generic.List<int> treeLocation, PropertyCollection fitsAttributes, PropertyCollection streamAttributes, string streamVal)
		{
			Error ("BINARY tag processing not implemented.  BINARY data ignored.");
			;
		}

		#endregion

		#region Helper Methods

		private string getIdString (string prefix, List<int> treeLocation)
		{
			StringBuilder sb = new StringBuilder (prefix);
			bool first = true;
			foreach (int index in treeLocation) {
				if (!first) {
					sb.Append ('.');
				}
				first = false;
				sb.Append (index);
			}
			
			string id = sb.ToString ();
			return id;
		}

		private string columnTableName (List<int> treeLocation)
		{
			return getIdString (COLUMN_TABLE_PREFIX, treeLocation);
		}

		private string dataTableName (List<int> treeLocation)
		{
			return getIdString (DATA_TABLE_PREFIX, treeLocation);
		}

		private Object convert (string input, Type targetType)
		{
			Object target = null;
			
			if (targetType.Equals (DS_STRING)) {
				// The input and target are both strings, so just pass the value through. 
				target = input;
			} else if ("".Equals (input)) {
				// "" means null for VO Table values.
				target = null;
			} else {
				target = Convert.ChangeType (input, targetType);
			}
			
			return target;
		}

		private Type getDataSetType (string votDataType, string votArraySize)
		{
			if ((votArraySize != null) && !(votArraySize.Equals ("*") || votArraySize.Equals (""))) {
				Warning ("VOT arraysize {0} ignored.", votArraySize);
			}
			
			Type dataSetType = null;
			if (!typeMapping.TryGetValue (votDataType, out dataSetType)) {
				Warning ("VOT datatype {0} not recognized, will be stored as String.", votDataType);
				dataSetType = Type.GetType ("System.String");
			}
			
			return dataSetType;
		}

		private void initializeTypeMapping ()
		{
			if (typeMapping == null) {
				typeMapping = new Dictionary<string, Type> ();
				typeMapping.Add ("char", DS_STRING);
				typeMapping.Add ("boolean", DS_BOOLEAN);
				typeMapping.Add ("short", DS_INT16);
				typeMapping.Add ("int", DS_INT32);
				typeMapping.Add ("long", DS_INT64);
				typeMapping.Add ("float", DS_SINGLE);
				typeMapping.Add ("double", DS_DOUBLE);
			}
		}

		#endregion

		#region Experiments

		#endregion
	}
}

