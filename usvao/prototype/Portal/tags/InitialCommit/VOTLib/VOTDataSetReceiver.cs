using System;
using System.IO;
using System.Text;
using System.Xml;
using System.Data;
using System.Collections;
using System.Collections.Generic;

namespace VOTLib
{
	public class VOTDataSetReceiver : VOTReceiver
	{

		#region Members

		public readonly DataSet VOTDataSet = null;

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

		public VOTDataSetReceiver (XmlTextReader iReader, DataSet iDataSet)
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

		public void Definitions (List<int> treeLocation, PropertyCollection attributes, string content)
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
			VOTDataSet.Tables.Add (getIdString ("", treeLocation));
		}


		public void Field (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string description, string values, List<PropertyCollection> fieldLinks)
		{
			// A field was recognized.  Add an entry in the column table, and a column in the data table.
			
			// Add the column for this field to the data table.
			DataTable dataTable = VOTDataSet.Tables[getIdString ("", treeLocation)];
			if (dataTable != null) {
				
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
				while (dataTable.Columns[name] != null) {
					// We already have a row with that name.  Try modifying it.
					name = orig + "-" + repeater++;
				}
				
				// Create the new column.
				string datatype = (string)attributes["datatype"];
				string arraysize = (string)attributes["arraysize"];
				DataColumn newColumn = new DataColumn (name, getDataSetType (datatype, arraysize));
				
				// Add field attributes as extended properties.
				foreach (DictionaryEntry property in attributes) {
					newColumn.ExtendedProperties[property.Key] = property.Value;
				}
				
				dataTable.Columns.Add (newColumn);
			}
			
		}


		public void Tr (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, System.Collections.Generic.List<string> dataValues)
		{
			DataTable dataTable = VOTDataSet.Tables[getIdString ("", treeLocation)];
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
				
				// These two are not in the Vo Table spec, but are seen in the Galex cone search.
				typeMapping.Add ("int16", DS_INT16);
				typeMapping.Add ("int32", DS_INT32);

			}
		}
		
		#endregion
		
		#region Experiments
		
		#endregion
	}
}

