
using System;
using System.IO;
using System.Xml;
using System.Data;
using System.Collections.Generic;

namespace VOTLib
{
	public class VOTParser
	{
		private XmlTextReader reader = null;
		private VOTReceiver receiver = null;

		public VOTParser (XmlTextReader iReader, VOTReceiver iReceiver)
		{
			reader = iReader;
			receiver = iReceiver;
		}

		public void Parse ()
		{
			if (reader.ReadToFollowing (Tags.VOTABLE)) {
				int votableCnt = 0;
				while (VOTABLE (buildTreeId (null, votableCnt))) {
					votableCnt++;
				}
				
			} else {
				Error ("No VOTABLE found.");
			}
		}

		////////////// Grammar methods /////////////

		bool VOTABLE (List<int> treeLocation)
		{
			bool found = false;
			if ((reader.NodeType == XmlNodeType.Element) && Tags.VOTABLE.Equals (reader.Name)) {
				PropertyCollection attributes = GetAttributes ();
				if (ReadToDescendant ()) {
					found = true;
					string description = DESCRIPTION ();
					receiver.VOTableBegin (treeLocation, attributes, description);
					
					DEFINITIONS (treeLocation);
					
					int infoCnt = InfoElements (treeLocation);
					
					// The current element should be a RESOURCE, and there should be at least 1.
					// Since the RESOURCES can (but are not likely too?) form a tree, we will assign 
					// them IDs that are Lists of ints, with the nth int in the List defining 
					// the RESOURCE's index at the nth level of the tree.  The root of the tree
					// is really the VOTABLE, and it's not including in the indexing.
					int resCnt = 0;
					while (RESOURCE (buildTreeId (treeLocation, resCnt))) {
						resCnt++;
					}
					if (resCnt < 1) {
						Error ("No resource found in VOTable.");
					}
					
					while (INFO (treeLocation, infoCnt++))
						;
					
				}
				reader.ReadToFollowing (Tags.VOTABLE);
				receiver.Debug ("At EOF: {0}", reader.EOF);
			}
			return found;
		}

		string DESCRIPTION ()
		{
			// If the reader position is on a non-empty element named DESCRIPTION,
			// this returns the string contents of that element, otherwise null.
			// If returning a string, the reader will be positioned after the end
			// of this element, otherwise it doesn't move.
			string description = null;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.DESCRIPTION.Equals (reader.Name)) {
				description = reader.ReadElementContentAsString ();
			}
			return description;
		}

		int InfoElements (List<int> treeLocation)
		{
			// Read any number of COOSYS, INFO, PARAM and GROUP elements.
			int coosysCnt = 0, infoCnt = 0, paramCnt = 0, groupCnt = 0, total = 0, prevTotal = -1;
			while (total > prevTotal) {
				prevTotal = total;
				if (COOSYS (treeLocation, coosysCnt))
					coosysCnt++;
				if (INFO (treeLocation, infoCnt))
					infoCnt++;
				if (PARAM (treeLocation, paramCnt))
					paramCnt++;
				if (GROUP (treeLocation, groupCnt))
					groupCnt++;
				total = coosysCnt + infoCnt + paramCnt + groupCnt;
			}
			return infoCnt;
		}

		bool DEFINITIONS (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.DEFINITIONS.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				PropertyCollection attributes = GetAttributes ();
				receiver.Definitions (treeLocation, attributes, reader.ReadOuterXml ());
			}
			return found;
		}

		bool COOSYS (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.COOSYS.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				PropertyCollection attributes = GetAttributes ();
				receiver.Coosys (treeLocation, index, attributes, reader.ReadOuterXml ());
			}
			return found;
		}

		bool INFO (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.INFO.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				PropertyCollection attributes = GetAttributes ();
				receiver.Info (treeLocation, index, attributes, reader.ReadOuterXml ());
			}
			return found;
		}

		bool PARAM (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.PARAM.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				PropertyCollection attributes = GetAttributes ();
				receiver.Param (treeLocation, index, attributes, reader.ReadOuterXml ());
			}
			return found;
		}

		bool GROUP (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.GROUP.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				PropertyCollection attributes = GetAttributes ();
				receiver.Group (treeLocation, index, attributes, reader.ReadOuterXml ());
			}
			return found;
		}

		bool LINK (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.LINK.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				PropertyCollection attributes = GetAttributes ();
				receiver.Link (treeLocation, index, attributes, reader.ReadOuterXml ());
			}
			return found;
		}

		bool RESOURCE (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.RESOURCE.Equals (reader.Name)) {
				int depth = reader.Depth;
				
				PropertyCollection attributes = GetAttributes ();
				if (ReadToDescendant ()) {
					found = true;
					string description = DESCRIPTION ();
					receiver.Resource (treeLocation, attributes, description);
					
					int infoCnt = InfoElements (treeLocation);
					
					int linkIndex = 0;
					while (LINK (treeLocation, linkIndex++))
						;
					
					// Resources can have tables or resources as children.
					int childIndex = 0;
					while (TABLE (buildTreeId (treeLocation, childIndex++)))
						;
					
					while (RESOURCE (buildTreeId (treeLocation, childIndex++)))
						;
					
					while (INFO (treeLocation, infoCnt++))
						;
					
					// Read to the next thing after this RESOURCE.
					ReadToSiblingAtDepth (depth);
					
				}
				
			}
			return found;
		}

		bool TABLE (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TABLE.Equals (reader.Name)) {
				int depth = reader.Depth;
				
				PropertyCollection attributes = GetAttributes ();
				if (ReadToDescendant ()) {
					found = true;
					
					string description = DESCRIPTION ();
					receiver.Table (treeLocation, attributes, description);
					
					// Get Metadata...
					int fieldCnt = TableMetadata (treeLocation);
					receiver.Debug ("Field count = {0}", fieldCnt);
					
					int linkIndex = 0;
					while (LINK (treeLocation, linkIndex++))
						;
					
					// Get Data...
					DATA (treeLocation);
					
					int infoCnt = 0;
					while (INFO (treeLocation, infoCnt++))
						;
					
					// Read to the next thing after this TABLE.
					ReadToSiblingAtDepth (depth);
					
				}
				
			}
			return found;
		}

		int TableMetadata (List<int> treeLocation)
		{
			// Read any number of FIELD, PARAM and GROUP elements.
			int fieldCnt = 0, paramCnt = 0, groupCnt = 0, total = 0, prevTotal = -1;
			while (total > prevTotal) {
				prevTotal = total;
				if (FIELD (treeLocation, fieldCnt))
					fieldCnt++;
				if (PARAM (treeLocation, paramCnt))
					paramCnt++;
				if (GROUP (treeLocation, groupCnt))
					groupCnt++;
				total = fieldCnt + paramCnt + groupCnt;
			}
			return fieldCnt;
		}

		bool FIELD (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.FIELD.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				
				PropertyCollection attributes = GetAttributes ();
				string description = null;
				string values = null;
				List<PropertyCollection> fieldLinks = null;
				PropertyCollection oneLink = null;
				if (ReadToDescendant ()) {
					
					description = DESCRIPTION ();
					
					values = VALUES ();
					
					while ((oneLink = FieldLink ()) != null) {
						if (fieldLinks == null)
							fieldLinks = new List<PropertyCollection> ();
						fieldLinks.Add (oneLink);
					}
					
				}
				// Read to the next thing after this FIELD.
				ReadToSiblingAtDepth (depth);
				
				receiver.Field (treeLocation, index, attributes, description, values, fieldLinks);
				
			}
			return found;
			
		}

		PropertyCollection FieldLink ()
		{
			reader.MoveToContent ();
			PropertyCollection attributes = null;
			if ((reader.NodeType == XmlNodeType.Element) && Tags.LINK.Equals (reader.Name)) {
				attributes = GetAttributes ();
			}
			return attributes;
		}

		string VALUES ()
		{
			string values = null;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.VALUES.Equals (reader.Name)) {
				values = reader.ReadOuterXml ();
			}
			return values;
		}

		bool DATA (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.DATA.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				
				if (ReadToDescendant ()) {
					found = TABLEDATA (treeLocation) || BINARY (treeLocation) || FITS (treeLocation);
					
					// Read to the next thing after this DATA.
					ReadToSiblingAtDepth (depth);
				}
			}
			return found;
		}

		bool TABLEDATA (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TABLEDATA.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				
				if (ReadToDescendant ()) {
					int rowCnt = 0;
					while (TR (treeLocation, rowCnt++))
						;
					
					// Read to the next thing after this TABLEDATA.
					ReadToSiblingAtDepth (depth);
				}
			}
			return found;
		}

		bool TR (List<int> treeLocation, int index)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TR.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				PropertyCollection attributes = GetAttributes ();
				List<string> dataValues = new List<string> ();
				if (ReadToDescendant ()) {
					string val = null;
					while ((val = TD ()) != null) {
						dataValues.Add (val);
					}
					
					// Read to the next thing after this TABLEDATA.
					ReadToSiblingAtDepth (depth);
				}
				receiver.Tr (treeLocation, index, attributes, dataValues);
			}
			return found;
		}

		string TD ()
		{
			// At this point, we ignore the possible "encoding" attribute for this element.
			string val = null;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TD.Equals (reader.Name)) {
				val = reader.ReadElementContentAsString ();
			}
			
			return val;
		}

		bool BINARY (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.BINARY.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				PropertyCollection fitsAttributes = GetAttributes ();
				PropertyCollection streamAttributes = null;
				string streamVal = null;
				if (ReadToDescendant ()) {
					streamVal = STREAM (out streamAttributes);
					
					// Read to the next thing after this TABLEDATA.
					ReadToSiblingAtDepth (depth);
				}
				receiver.Binary (treeLocation, fitsAttributes, streamAttributes, streamVal);
				
			}
			return found;
		}

		bool FITS (List<int> treeLocation)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.FITS.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				PropertyCollection fitsAttributes = GetAttributes ();
				PropertyCollection streamAttributes = null;
				string streamVal = null;
				if (ReadToDescendant ()) {
					streamVal = STREAM (out streamAttributes);
					
					// Read to the next thing after this TABLEDATA.
					ReadToSiblingAtDepth (depth);
				}
				receiver.Fits (treeLocation, fitsAttributes, streamAttributes, streamVal);
				
			}
			return found;
		}

		string STREAM (out PropertyCollection streamAttributes)
		{
			// At this point, we ignore the possible "encoding" attribute for this element.
			string val = null;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.STREAM.Equals (reader.Name)) {
				streamAttributes = GetAttributes ();
				val = reader.ReadInnerXml ();
			} else {
				streamAttributes = new PropertyCollection ();
			}
			
			return val;
			
		}



		////////////// Utility methods //////////////

		private void Error (string format, params Object[] args)
		{
			receiver.Error (format, args);
		}

		private PropertyCollection GetAttributes ()
		{
			PropertyCollection attributes = new PropertyCollection ();
			
			while (reader.MoveToNextAttribute ()) {
				// Read the attributes.
				attributes[reader.Name] = reader.Value;
			}
			// Return the read position to the element containing the attributes.
			reader.MoveToElement ();
			return attributes;
		}

		private bool ReadToDescendant ()
		{
			// Returns true if the first descendant element is found; otherwise false. 
			// If a child element is not found, the XmlReader is positioned on the end tag 
			// (NodeType is XmlNodeType.EndElement) of the starting element.
			// If the XmlReader is not positioned on an element when ReadToDescendant was called, 
			// this method returns false and the position of the XmlReader is not changed.
			bool foundDescendant = false;
			bool reachedEndElement = ((reader.NodeType != XmlNodeType.Element) || reader.IsEmptyElement);
			
			while (!foundDescendant && !reachedEndElement && reader.Read ()) {
				if (reader.NodeType == XmlNodeType.Element) {
					foundDescendant = true;
				} else if (reader.NodeType == XmlNodeType.EndElement) {
					reachedEndElement = true;
				}
			}
			return foundDescendant;
		}

		private bool ReadToSiblingAtDepth (int depth)
		{
			bool foundEnd = false;
			
			// Find the EndElement node at the desired depth.
			do {
				if ((reader.NodeType == XmlNodeType.EndElement) || ((reader.NodeType == XmlNodeType.Element) && reader.IsEmptyElement)) {
					if (reader.Depth == depth) {
						foundEnd = true;
					}
				}
			} while (!foundEnd && reader.Read ());
			
			// Move to the next content after that EndElement node.
			reader.Read ();
			reader.MoveToContent ();
			
			return foundEnd;
		}

		private List<int> buildTreeId (List<int> parentId, int index)
		{
			List<int> newId = null;
			if (parentId != null) {
				newId = new List<int> (parentId);
			} else {
				newId = new List<int> ();
			}
			newId.Add (index);
			return newId;
		}
		
	}
}

