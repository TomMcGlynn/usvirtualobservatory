
using System;
using System.IO;
using System.Xml;
using System.Data;
using System.Text.RegularExpressions;
using System.Collections.Generic;

using Collections;

namespace VOTLib
{
	public class VOTParser
	{		
		private XmlReader reader = null;
		private VOTReceiver[] recs = null;
		private int idCnt = 0;

		public VOTParser (XmlReader iReader, VOTReceiver[] iRecs)
		{
			reader = iReader;
			recs = iRecs;
		}

		public VOTParser (XmlReader iReader, VOTReceiver iReceiver)
		{
			reader = iReader;
			recs = new VOTReceiver[] {iReceiver};
		}

		public void Parse ()
		{
			//
			// IMPORTANT NOTE: 
			// We lock the Parse() method becase the instance methods of a XmlReader (reader) 
			// are NOT guranteed to be threadsafe:
			// Reference: http://msdn.microsoft.com/en-us/library/system.xml.xmltextreader%28v=VS.90%29.aspx
			//
			lock(reader)
			{
				if (reader.ReadToFollowing (Tags.VOTABLE)) {
					int votableCnt = 0;
					int topLevelId = AllocateId();
					while (VOTABLE (topLevelId)) {
						votableCnt++;
					}
					
				} else {
					Error ("No VOTABLE found.");
				}
			} // end lock(reader)
		}

		////////////// Grammar methods /////////////

		bool VOTABLE (int pid)
		{
			bool found = false;
			if ((reader.NodeType == XmlNodeType.Element) && Tags.VOTABLE.Equals (reader.Name)) {
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				if (ReadToDescendant ()) {
					found = true;
					IXmlLineInfo lineInfo = reader as IXmlLineInfo;
					for (int i=0; i<recs.Length; recs[i++].VOTableBegin (id, pid, attributes, lineInfo));
					DESCRIPTION (id);

					// DEFINITIONS is deprecated
					DEFINITIONS (id);
					
					InfoElements (id);
					
					// The current element should be a RESOURCE, and there should be at least 1.
					// Since the RESOURCES can (but are not likely too?) form a tree, we will assign 
					// them IDs that are Lists of ints, with the nth int in the List defining 
					// the RESOURCE's index at the nth level of the tree.  The root of the tree
					// is really the VOTABLE, and it's not including in the indexing.
					int resCnt = 0;
					while (RESOURCE (id)) {
						resCnt++;
					}
					if (resCnt < 1) {
						Error ("No resource found in VOTable.");
					}
					
					while (INFO (id))
						;

					for (int i=0; i<recs.Length; recs[i++].VOTableEnd(id, pid));
				}
				reader.ReadToFollowing (Tags.VOTABLE);
			}
			return found;
		}

		/**
		 * This special version of DESCRIPTION allows us to parse that element without 
		 * sending it to the receiver.  This is only needed because we once discovered a 
		 * resource that supplied a VO Table with a misplaced DESCRIPTION, and we want to ignore it.
		 */
		string DESCRIPTION (int pid, bool sendToReceiver)
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
			if ((description != null) && sendToReceiver) {
				int descId = AllocateId();
				OrderedDictionary<string, object> attr = new OrderedDictionary<string, object>();
				for (int i=0; i<recs.Length; recs[i++].Description(descId, pid, attr, description));
			}
			return description;
		}

		string DESCRIPTION (int pid)
		{
			return DESCRIPTION(pid, true);
		}

		int InfoElements (int pid)
		{
			// Read any number of COOSYS, INFO, PARAM and GROUP elements.
			int coosysCnt = 0, infoCnt = 0, paramCnt = 0, groupCnt = 0, descriptionCnt = 0, total = 0, prevTotal = -1;
			string description=null;
			while (total > prevTotal) {
				prevTotal = total;
				if (COOSYS (pid))
					coosysCnt++;
				if (INFO (pid))
					infoCnt++;
				if (PARAM (pid))
					paramCnt++;
				if (GROUP (pid))
					groupCnt++;
				if ((description = DESCRIPTION(pid, false)) != null)
				{
					for (int i=0; i<recs.Length; recs[i++].Warning ("Unexpected DESCRIPTION: {0}", description));
					descriptionCnt++;
				}
				total = coosysCnt + infoCnt + paramCnt + groupCnt + descriptionCnt;
			}
			return infoCnt;
		}

		/**
		 * DEFINITIONS is deprecated since version 1.0.  We won't try to parse its contents, but we won't choke on it either.
		 */
		bool DEFINITIONS (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.DEFINITIONS.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string s = reader.ReadInnerXml ();
				// The trouble is that ReadInnerXml includes an extra attribute that tells us the namespace.  
				// We'll try to remove that here.  (all for this stupid deprecated-since-1.0 element)
				Regex regex = new Regex("xmlns=\".*\" *");
				string sFixed = regex.Replace(s, "");

				for (int i=0; i<recs.Length; recs[i++].Definitions (id, pid, attributes, sFixed));
			}
			return found;
		}

		bool COOSYS (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.COOSYS.Equals (reader.Name)) {
				found = true;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].Coosys (id, pid, attributes, content));
			}
			return found;
		}

		/**
		 * Read sequence of DESCRIPTION, VALUES, then any number of LINK.
		 */
		void DescriptionValuesLink(int pid) {
			DESCRIPTION (pid);
			VALUES (pid);
			while (LINK (pid))
				;
		}

		bool INFO (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.INFO.Equals (reader.Name)) {
				found = true;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].Info (id, pid, attributes, content));
			}
			return found;
		}

		bool PARAM (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.PARAM.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].ParamBegin (id, pid, attributes));
				if (ReadToDescendant ()) {
					DescriptionValuesLink(id);
				}
				ReadToSiblingAtDepth(depth);
				for (int i=0; i<recs.Length; recs[i++].ParamEnd (id, pid));
			}
			return found;
		}

		bool GROUP (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.GROUP.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].GroupBegin (id, pid, attributes));

				
				if (ReadToDescendant ()) {
					DESCRIPTION (id);

					// Read any number of FIELDref, PARAM, PARAMref and GROUP elements.
					int fieldRefCnt = 0, paramCnt = 0, paramRefCnt = 0, groupCnt = 0, total = 0, prevTotal = -1;
					while (total > prevTotal) {
						prevTotal = total;
						if (FIELDref (pid))
							fieldRefCnt++;
						if (PARAM (pid))
							paramCnt++;
						if (PARAMref (pid))
							paramRefCnt++;
						if (GROUP (pid))
							groupCnt++;
						total = fieldRefCnt + paramCnt + paramRefCnt + groupCnt;
					}
				}
				ReadToSiblingAtDepth(depth);
				for (int i=0; i<recs.Length; recs[i++].GroupEnd (id, pid));
			}
			return found;
		}
		
		bool LINK (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.LINK.Equals (reader.Name)) {
				found = true;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].Link (id, pid, attributes, content));
			}
			return found;
		}
		
		bool FIELDref (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.FIELDref.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].FieldRef (id, pid, attributes, content));
			}
			return found;
		}

		bool PARAMref (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.PARAMref.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].ParamRef (id, pid, attributes, content));
			}
			return found;
		}

		bool RESOURCE (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.RESOURCE.Equals (reader.Name)) {
				int depth = reader.Depth;
				
				OrderedDictionary<string, object> attributes = GetAttributes ();
				found = true;
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].ResourceBegin (id, pid, attributes));
				if (ReadToDescendant ()) {

					DESCRIPTION (id);
					InfoElements (id);
					while (LINK (id))
						;
					// Resources can have tables or resources as children.
					while (TABLE (id))
						;
					while (RESOURCE (id))
						;
					while (INFO (id))
						;


				}
				// Read to the next thing after this RESOURCE.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].ResourceEnd (id, pid));
				
			}
			return found;
		}

		bool TABLE (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TABLE.Equals (reader.Name)) {
				int depth = reader.Depth;
				
				OrderedDictionary<string, object> attributes = GetAttributes ();
				found = true;
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].TableBegin (id, pid, attributes));
				if (ReadToDescendant ()) {

					DESCRIPTION (id);
					
					// Get Metadata...
					TableMetadata (id);
					while (LINK (id))
						;
					// Get Data...
					DATA (id);
					while (INFO (id))
						;
				}
				// Read to the next thing after this TABLE.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].TableEnd (id, pid));
				
			}
			return found;
		}

		int TableMetadata (int pid)
		{
			// Read any number of FIELD, PARAM and GROUP elements.
			int fieldCnt = 0, paramCnt = 0, groupCnt = 0, total = 0, prevTotal = -1;
			while (total > prevTotal) {
				prevTotal = total;
				if (FIELD (pid))
					fieldCnt++;
				if (PARAM (pid))
					paramCnt++;
				if (GROUP (pid))
					groupCnt++;
				total = fieldCnt + paramCnt + groupCnt;
			}
			return fieldCnt;
		}

		bool FIELD (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.FIELD.Equals (reader.Name)) {
				found = true;
				int id = AllocateId();
				OrderedDictionary<string, object> attributes = GetAttributes ();
				for (int i=0; i<recs.Length; recs[i++].FieldBegin (id, pid, attributes));
				int depth = reader.Depth;
				if (ReadToDescendant ()) {
					DescriptionValuesLink(id);
				}
				// Read to the next thing after this FIELD.
				ReadToSiblingAtDepth (depth);
				
				for (int i=0; i<recs.Length; recs[i++].FieldEnd (id, pid));
				
			}
			return found;
			
		}

		bool VALUES (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.VALUES.Equals (reader.Name)) {
				found = true;
				int depth = reader.Depth;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].ValuesBegin (id, pid, attributes));
				if (ReadToDescendant ()) {
					MIN(id);
					MAX(id);
					while (OPTION (id))
						;
				}
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].ValuesEnd (id, pid));
			}
			return found;
		}
		
		bool MIN (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.MIN.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].Min (id, pid, attributes, content));
			}
			return found;
		}
		
		bool MAX (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.MAX.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].Max (id, pid, attributes, content));
			}
			return found;
		}
		
		bool OPTION (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.OPTION.Equals (reader.Name)) {
				found = true;
				// For now, just return the attributes and the whole xml.
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				string content = reader.ReadElementContentAsString ();
				for (int i=0; i<recs.Length; recs[i++].Option (id, pid, attributes, content));
			}
			return found;
		}

		bool DATA (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.DATA.Equals (reader.Name)) {
				found = true;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].DataBegin (id, pid, attributes));
				int depth = reader.Depth;
				
				if (ReadToDescendant ()) {
					found = TABLEDATA (id) || BINARY (id) || FITS (id);
				}
				// Read to the next thing after this DATA.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].DataEnd (id, pid));
			}
			return found;
		}

		bool TABLEDATA (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TABLEDATA.Equals (reader.Name)) {
				found = true;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				for (int i=0; i<recs.Length; recs[i++].TableDataBegin (id, pid, attributes));
				int depth = reader.Depth;
				
				if (ReadToDescendant ()) {
					while (TR (id))
						;
				}
				// Read to the next thing after this TABLEDATA.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].TableDataEnd (id, pid));
			}
			return found;
		}

		bool TR (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.TR.Equals (reader.Name)) {
				found = true;
				OrderedDictionary<string, object> attributes = GetAttributes ();
				int id = AllocateId();
				int depth = reader.Depth;
				List<string> dataValues = new List<string> ();
				if (ReadToDescendant ()) {
					string val = null;
					while ((val = TD ()) != null) {
						dataValues.Add (val);
					}
				}
				// Read to the next thing after this TR.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].Tr (id, pid, attributes, dataValues));
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

		bool BINARY (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.BINARY.Equals (reader.Name)) {
				found = true;
				int id = AllocateId();
				int depth = reader.Depth;
				OrderedDictionary<string, object> binaryAttributes = GetAttributes ();
				OrderedDictionary<string, object> streamAttributes = null;
				string streamVal = null;
				if (ReadToDescendant ()) {
					streamVal = STREAM (out streamAttributes);
				}
				// Read to the next thing after this BINARY.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].Binary (id, pid, binaryAttributes, streamAttributes, streamVal));
				
			}
			return found;
		}

		bool FITS (int pid)
		{
			bool found = false;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.FITS.Equals (reader.Name)) {
				found = true;
				int id = AllocateId();
				int depth = reader.Depth;
				OrderedDictionary<string, object> fitsAttributes = GetAttributes ();
				OrderedDictionary<string, object> streamAttributes = null;
				string streamVal = null;
				if (ReadToDescendant ()) {
					streamVal = STREAM (out streamAttributes);
				}
				// Read to the next thing after this TABLEDATA.
				ReadToSiblingAtDepth (depth);
				for (int i=0; i<recs.Length; recs[i++].Fits (id, pid, fitsAttributes, streamAttributes, streamVal));
				
			}
			return found;
		}

		string STREAM (out OrderedDictionary<string, object> streamAttributes)
		{
			// At this point, we ignore the possible "encoding" attribute for this element.
			string val = null;
			reader.MoveToContent ();
			if ((reader.NodeType == XmlNodeType.Element) && Tags.STREAM.Equals (reader.Name)) {
				streamAttributes = GetAttributes ();
				val = reader.ReadInnerXml ();
			} else {
				streamAttributes = new OrderedDictionary<string, object> ();
			}
			
			return val;
			
		}



		////////////// Utility methods //////////////

		private void Error (string format, params Object[] args)
		{
			for (int i=0; i<recs.Length; recs[i++].Error (format, args));
		}

		private OrderedDictionary<string, object> GetAttributes ()
		{
			OrderedDictionary<string, object> attributes = new OrderedDictionary<string, object> ();
			
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

		private int AllocateId() {
			return idCnt++;
		}
		
		private int LineNum() {
			int lineNum = 0;
			IXmlLineInfo info = reader as IXmlLineInfo;
			if (info != null) {
				lineNum = info.LineNumber;
			}
			return lineNum;
		}
		
		private int LinePos() {
			int linePos = 0;
			IXmlLineInfo info = reader as IXmlLineInfo;
			if (info != null) {
				linePos = info.LinePosition;
			}
			return linePos;
		}

		
	}
}

