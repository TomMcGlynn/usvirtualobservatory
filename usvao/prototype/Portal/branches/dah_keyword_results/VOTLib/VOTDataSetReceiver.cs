using System;
using System.IO;
using System.Text;
using System.Xml;
using System.Data;
using System.Collections;
using System.Collections.Generic;

using log4net;

namespace VOTLib
{
	public class VOTDataSetReceiver : VOTReceiver
	{
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }		

		#region Members

		public readonly DataSet VOTDataSet = null;

		
		

		XmlReader reader = null;
		bool[] columnWarned = null;

		#endregion

		public VOTDataSetReceiver (XmlReader iReader, DataSet iDataSet)
		{
			reader = iReader;
			VOTDataSet = iDataSet;
		}

		#region VOTReceiver implementation

		#region Message-handling Methods
		public void Debug (string format, params Object[] args)
		{
			if (log.IsFatalEnabled) 
				log.DebugFormat(tid + format, args);
			else 
				Console.WriteLine("Debug: " + format, args);
		}

		public void Informational (string format, params Object[] args)
		{
			if (log.IsFatalEnabled)   
				log.InfoFormat(tid + format, args);
			else 
				Console.WriteLine ("Info: " + format, args);
		}

		public void Warning (string format, params Object[] args)
		{
			IXmlLineInfo info = reader as IXmlLineInfo;
			if (log.IsFatalEnabled) 
			{
				log.WarnFormat(tid + format, args);
				log.WarnFormat(tid + "   at line {0}, position {1}", info.LineNumber, info.LinePosition);
			}
			else
			{
				Console.WriteLine ("Warning: " + format, args);
				Console.WriteLine ("   at line {0}, position {1}", info.LineNumber, info.LinePosition);
			}
		}

		public void Error (string format, params Object[] args)
		{
			IXmlLineInfo info = reader as IXmlLineInfo;
			if (log.IsFatalEnabled) 
			{
				log.ErrorFormat(tid + format, args);
				log.ErrorFormat(tid + "   at line {0}, position {1}", info.LineNumber, info.LinePosition);
			}
			else
			{		
				Console.WriteLine ("Error: " + format, args);
				Console.WriteLine ("   at line {0}, position {1}", info.LineNumber, info.LinePosition);		
			}
			throw new Exception("Error parsing VO Table");
			                    
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
			DataTable dataTable = VOTDataSet.Tables[getIdString ("", treeLocation)];
			if (dataTable != null) {
				// We found the TABLE that contains this PARAM.  Note that this will only match PARAMs that are immediate children 
				// of the TABLE.

				PropertyCollection ep = dataTable.ExtendedProperties;
				PropertyCollection vot = ensurePropertyCollection(ep, "vot");
				ArrayList allParams = ensureArrayList(vot, "PARAMs");
				PropertyCollection thisParam = new PropertyCollection();
				allParams.Add(thisParam);
				
				// Add PARAM attributes to the Data Table as extended properties.
				foreach (DictionaryEntry property in attributes) {
					thisParam[property.Key] = property.Value;
				}
			}
		}


		public void Group (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			
		}


		public void Link (System.Collections.Generic.List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			DataTable dataTable = VOTDataSet.Tables[getIdString ("", treeLocation)];
			if (dataTable != null) {
				// We found the TABLE that contains this LINK.  Note that this will only match LINKs that are immediate children 
				// of the TABLE.

				PropertyCollection ep = dataTable.ExtendedProperties;
				PropertyCollection vot = ensurePropertyCollection(ep, "vot");
				ArrayList allParams = ensureArrayList(vot, "LINKs");
				PropertyCollection thisParam = new PropertyCollection();
				allParams.Add(thisParam);
				
				// Add LINK attributes to the Data Table as extended properties.
				foreach (DictionaryEntry property in attributes) {
					thisParam[property.Key] = property.Value;
				}
			}
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
				DataColumn newColumn = new DataColumn (name, getDataSetType (name, datatype, arraysize));
				
				// Add field attributes as extended properties.
				foreach (DictionaryEntry property in attributes) {
					string prefixedName = "vot." + property.Key;
					newColumn.ExtendedProperties[prefixedName] = property.Value;
				}
				// Add description as an extended property
				newColumn.ExtendedProperties["vot.description"] = description;
				
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
					
					// Initialize columnWarned if necessary.
					if (columnWarned == null) {
						columnWarned = new bool[numColumns];
						for (int j=0; j<columnWarned.Length; j++) {
							columnWarned[j] = false;
						}
					}
					// Convert the string data values to their appropriate types.
					Object[] convertedData = new Object[numColumns];
					int i = 0;
					IEnumerator columnEnum = dataTable.Columns.GetEnumerator ();
					foreach (string dataVal in dataValues) {
						columnEnum.MoveNext ();
						Type targetType = ((DataColumn)columnEnum.Current).DataType;
						string fieldName = ((DataColumn)columnEnum.Current).ColumnName;
						try {
							convertedData[i] = convert (dataVal, targetType);
						} catch (Exception) {
							if (!columnWarned[i]) {
								Warning ("Could not convert column {0} <{1}> data <{2}> to type <{3}>.  Data ignored.", i, fieldName, dataVal, targetType);
								columnWarned[i] = true;
							}
							convertedData[i] = null;
						} finally {
							i++;
						}
					}
					
					// Add the converted data array all at once as the whole row of data.					
					dataTable.Rows.Add (convertedData);
				} else {
					Error ("Data row has {0} values where {1} are expected.", dataValues.Count, numColumns);
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
			
			if (targetType.Equals (VOTType.DS_STRING)) {
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

		private Type getDataSetType (string fieldName, string votDataType, string votArraySize)
		{
			
			Type dataSetType = null;
			
			if (votArraySize != null) {
				// Use string since we're not ready to handle any arrays other than char[*] (string).
				dataSetType = Type.GetType ("System.String"); 
				
				if (!"char".Equals(votDataType)) {
					// For arrays of chars, a string type is correct, for others, we should mention that we're not trying to parse
					// the value as an array.
					Informational ("Field <{1}>:  arraysize <{0}> ignored, values will be treated as strings", votArraySize, fieldName);
				}
				
			} else if ((votDataType == null) || !VOTType.SystemTypeMapping.TryGetValue (votDataType, out dataSetType)) {
				Warning ("Field <{1}>:  datatype <{0}> not recognized, values will be stored as String.", votDataType, fieldName);
				dataSetType = Type.GetType ("System.String");  // Use string since we can't interpret a better correct value
			}
			
			return dataSetType;
		}	
		
		private PropertyCollection ensurePropertyCollection(PropertyCollection containerCollection, string collectionName) {
			PropertyCollection ensuredCollection = (PropertyCollection)containerCollection[collectionName];
			if (ensuredCollection == null) {
				ensuredCollection = new PropertyCollection();
				containerCollection[collectionName] = ensuredCollection;
			}
			return ensuredCollection;
		}
		
		private ArrayList ensureArrayList(PropertyCollection containerCollection, string collectionName) {
			ArrayList ensuredCollection = (ArrayList)containerCollection[collectionName];
			if (ensuredCollection == null) {
				ensuredCollection = new ArrayList();
				containerCollection[collectionName] = ensuredCollection;
			}
			return ensuredCollection;
		}
		
		#endregion
		
		#region Experiments
		
		#endregion
	}
}

