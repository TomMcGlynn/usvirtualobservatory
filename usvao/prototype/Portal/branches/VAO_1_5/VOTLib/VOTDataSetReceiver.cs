using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Xml;

using Collections;

namespace VOTLib
{
	public class VOTDataSetReceiver : ArrayListReceiver
	{
		public readonly List<DataSet> DsResults = new List<DataSet>(1);  // We aren't likely to have more than 1 VO Table, and therefor 1 DataSet.

		private int votCnt = 0;  // zero-based index pointing the VOT currently being received.
		private int tableCnt = 0;  // zero-based index pointing to the TABLE (withing the VOT) being received.

		// While we're within a FieldBegin and FieldEnd sequence, keep track of the field's ID and working DataColumn so that
		// child elements can attach the info appropriately.
		private int currFieldId = 0;  
		private DataColumn currFieldColumn = null;

		// For keeping track of columns that have warnings about bad data so that we don't repeat the messages over and over.
		private bool[] columnWarned = null;

		public VOTDataSetReceiver ()
		{
		}

		#region Overridden Receiver Methods

		public override void VOTableBegin(int id, int parentId, OrderedDictionary<string, object> attributes, IXmlLineInfo lineInfo) {
			base.VOTableBegin(id, parentId, attributes, lineInfo);

			DataSet ds = new DataSet("VOTable_" + votCnt);
			DsResults.Add(ds);

			// Add the ArrayList storage of VOT metadata
			ArrayList votMetadata = parents.Peek();  // The current parent will be the ArrayList for the VOTABLE element.
			PropertyCollection ep = ds.ExtendedProperties;
			ep[Tags.VOT_METADATA] = votMetadata;
		}
		
		public override void VOTableEnd(int id, int parentId) {
			base.VOTableEnd(id, parentId);

			votCnt++;  // Increment the index for the next VO Table (actually unlikely we'll get more than one).
			tableCnt = 0;  // Tables are counted within the VO Table, so the count gets reset
		}

		/*
		 * For PARAM and LINK, we're trying to get the sort of structure in the ExtendedProperties
		 * that would result in this sort of JSON:
		 * "Tables":[{ "name":"0.0.0",
		 * "ExtendedProperties":{"Paging":{"page":1,"pageSize":1000000,"pagesFiltered":1,
		 * "rows":83,"rowsFiltered":83,"rowsTotal":83},"vot":{"PARAMs":[{"value":"0.058354277","datatype":"float","name":"percentComplete"}]}},
		 * 
		 */

		public override void ParamBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			base.ParamBegin(id, parentId, attributes);

			// These are used only for DataScope progress percentage.
			DataTable dataTable = DsResults[votCnt].Tables[tableCnt.ToString()];
			if (dataTable != null) {
				// We found the TABLE that contains this PARAM.  Note that this will only match PARAMs that are immediate children 
				// of the TABLE.
				
				PropertyCollection ep = dataTable.ExtendedProperties;
				PropertyCollection vot = ensurePropertyCollection(ep, "vot");
				ArrayList allParams = ensureArrayList(vot, "PARAMs");
				PropertyCollection thisParam = new PropertyCollection();
				allParams.Add(thisParam);
				
				// Add PARAM attributes to the Data Table as extended properties.
				foreach (KeyValuePair<string, object> property in attributes) {
					thisParam[property.Key] = property.Value;
				}
			}
		}
		
		public override void ParamEnd(int id, int parentId) {
			base.ParamEnd(id, parentId);
		}
		
		public override void TableBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			base.TableBegin(id, parentId, attributes);

			DataTable dt = DsResults[votCnt].Tables.Add (tableCnt.ToString());
			
			// Add the parsing id for future retrieval.
			PropertyCollection ep = dt.ExtendedProperties;
			ep[Tags.ID_ATTR] = id;
		}
		
		public override void TableEnd(int id, int parentId) {
			base.TableEnd(id, parentId);

			tableCnt++;  // Increment the table count in case we get another table in this VO Table.
		}
		
		public override void FieldBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			base.FieldBegin(id, parentId, attributes);

			// A field was recognized.  Add an entry in the column table, and a column in the data table.
			
			// Add the column for this field to the data table.
			DataTable dataTable = DsResults[votCnt].Tables[tableCnt.ToString()];
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
				foreach (KeyValuePair<string, object> property in attributes) {
					string prefixedName = "vot." + property.Key;
					newColumn.ExtendedProperties[prefixedName] = property.Value;
				}
				newColumn.ExtendedProperties[Tags.ID_ATTR] = id;
				
				dataTable.Columns.Add (newColumn);

				currFieldId = id;
				currFieldColumn = newColumn;
			}

		}
		
		public override void FieldEnd(int id, int parentId) {
			base.FieldEnd(id, parentId);

			currFieldId = 0;
			currFieldColumn = null;
		}

		public override void Description(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			base.Description(id, parentId, attributes, content);
			
			if ((parentId == currFieldId) && (currFieldColumn != null)) {
				// Add description as an extended property to the current DataColumn (field) we're working on.
				currFieldColumn.ExtendedProperties["vot.description"] = content;
			}
		}
		
		public override void ValuesBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			base.ValuesBegin(id, parentId, attributes);
			
			if ((parentId == currFieldId) && (currFieldColumn != null)) {
				// Extract the null value specification and attach it to the column ExtendedProperties as cc.ignoreValue, which is how it
				// will be represented in the DataSet and JSON clients.  
				//
				// Note that the nullVal will be stored in Extended Properties as a number object.  This means that in a 
				// roundtrip to/from DataSet native xml (or probably to/from a database), the value will be lost.
				// Revisit that as needed.
				// 
				// Note also that we will not support null values for the VO logical data type, so we will ignore
				// all null values specified for something other than a number (DS_INT16, DS_INT32, DS_INT64, DS_SINGLE and DS_DOUBLE).
				string nullString = (string)attributes["null"];
				if (nullString != null) {
					object nullVal = null;
					Type type = currFieldColumn.DataType;
					if (VOTType.DS_INT16.Equals(type) ||
					    VOTType.DS_INT32.Equals(type) ||
					    VOTType.DS_INT64.Equals(type) ||
					    VOTType.DS_SINGLE.Equals(type) ||
					    VOTType.DS_DOUBLE.Equals(type)) {
						try {
							nullVal = Convert(nullString, type, null);
						} catch (Exception) {
							Warning ("Could not interpret VALUES null attribute <{0}> on FIELD <{1}> as a {2}.  Attrtibute ignored.", nullString, currFieldColumn.ColumnName, type);
						}
						if (nullVal != null) {
							currFieldColumn.ExtendedProperties["cc.ignoreValue"] = nullVal;
						}
					}
				}
			}
		}

		public override void Link(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			base.Link(id, parentId, attributes, content);
			
			// These are used only for discovering preview templates.
			DataTable dataTable = DsResults[votCnt].Tables[tableCnt.ToString()];
			if (dataTable != null) {
				// We found the TABLE that contains this LINK.  Note that this will only match LINKs that are immediate children 
				// of the TABLE.
				
				PropertyCollection ep = dataTable.ExtendedProperties;
				PropertyCollection vot = ensurePropertyCollection(ep, "vot");
				ArrayList allParams = ensureArrayList(vot, "LINKs");
				PropertyCollection thisParam = new PropertyCollection();
				allParams.Add(thisParam);
				
				// Add LINK attributes to the Data Table as extended properties.
				foreach (KeyValuePair<string, object> property in attributes) {
					thisParam[property.Key] = property.Value;
				}
			}
		}

		public override void Binary (int id, int parentId, OrderedDictionary<string, object> binaryAttributes, OrderedDictionary<string, object> streamAttributes, string streamVal) {
			Error ("BINARY data format not supported.");
		}

		public override void Fits (int id, int parentId, OrderedDictionary<string, object> fitsAttributes, OrderedDictionary<string, object> streamAttributes, string streamVal) {
			Error ("FITS data format not supported.");
		}

		#endregion

		#region Overridden Consolidator Methods

		protected override void Data (int trId, List<string> dataValues)
		{
			// We will not call the overridden method so the we don't duplicate the storage of this data in an ArrayList... base.Data(trId, dataValues);

			DataTable dataTable = DsResults[votCnt].Tables[tableCnt.ToString()];
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
						DataColumn col = (DataColumn)columnEnum.Current;
						Type targetType = col.DataType;
						string fieldName = col.ColumnName;
						object nullVal = col.ExtendedProperties["cc.ignoreValue"];
						try {
							convertedData[i] = Convert (dataVal, targetType, nullVal);
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

		#endregion

		#region Utility Methods

		/*
		 * This could throw an exception if the input cannot be parsed into the specified type.
		 * The caller should handle that possibility.
		 * 
		 * The null value is a numeric quantity that the VO Table specified as equivalent to null for 
		 * the FIELD.  If the input value, when parsed into the specified type, is equal to nullVal, then 
		 * this method will return null instead of the parsed value.
		 */
		public static object Convert (object input, Type targetType, object nullVal)
		{
			object target = null;
			
			if (targetType.Equals (VOTType.DS_STRING)) {
				target = input.ToString();
			} else if ("".Equals (input)) {
				// "" means null for VO Table values.
				target = null;
			} else {
				if ((targetType.Equals(VOTType.DS_SINGLE) || targetType.Equals(VOTType.DS_DOUBLE)) &&
				    (input.Equals("Inf") || input.Equals("-Inf"))) {
					// Look for the VO format for +/-Infinity and convert it to one C# understands.
					input = input + "inity";
				} else if ((input is String) && 
				           (targetType.Equals(VOTType.DS_INT16) || targetType.Equals(VOTType.DS_INT32) || targetType.Equals(VOTType.DS_INT64)) &&
				           ((string)input).StartsWith("0x")) {
					// Look for hex format integers, which don't automatically convert.
					input = ((string)input).Replace("0x", "");
					if (targetType.Equals(VOTType.DS_INT16)) {
						target = Int16.Parse((string)input, System.Globalization.NumberStyles.HexNumber);
					} else if (targetType.Equals(VOTType.DS_INT32)) {
						target = Int32.Parse((string)input, System.Globalization.NumberStyles.HexNumber);
					} else if (targetType.Equals(VOTType.DS_INT64)) {
						target = Int64.Parse((string)input, System.Globalization.NumberStyles.HexNumber);
					}
				} else if (targetType.Equals(VOTType.DS_BOOLEAN)) {
					// Accept other boolean formats:  T, t, 1, F, f, 0
					if ("T".Equals(input) || "t".Equals(input) || "1".Equals(input)) {
						target = true;
					} if ("F".Equals(input) || "f".Equals(input) || "0".Equals(input)) {
						target = false;
					} 
				}

				// If we haven't already figured out the target value, use the system conversion mechanisms to read the string into the specified type.
				if (target == null) {
					target = System.Convert.ChangeType (input, targetType);
				}
			}

			// We've extracted the best value we can out of the input string.  If a null value was specified,
			// check to see if our extracted value matches that null value.  If it does, then just change our
			// extracted value to null.
			if ((nullVal != null) && nullVal.Equals(target)) {
				target = null;
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
		

	}


}

