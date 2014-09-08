using System;
using System.IO;
using System.Data;
using System.Collections;
using System.Collections.Generic;
using System.Xml;

using Collections;
using VOTLib;

namespace VOTLib
{
	public abstract class ConsolidatorReceiver : VOTReceiver
	{

		protected IXmlLineInfo xmlLineInfo = null;

		public ConsolidatorReceiver ()
		{

		}

		#region Message Handlers
		public abstract void Debug (string format, params Object[] args);

		public abstract void Informational (string format, params Object[] args);

		public abstract void Warning (string format, params Object[] args);

		public abstract void Error (string format, params Object[] args);

		protected int LineNum() {
			int lineNum = -1;
			if (xmlLineInfo != null) {
				lineNum = xmlLineInfo.LineNumber;
			}
			return lineNum;
		}
		
		protected int LinePos() {
			int linePos = -1;
			if (xmlLineInfo != null) {
				linePos = xmlLineInfo.LinePosition;
			}
			return linePos;
		}

		#endregion

		#region Consolidated Methods
		
		abstract protected void NtBegin(string tag, int id, int parentId, OrderedDictionary<string, object> attributes);
		
		abstract protected void NtEnd(string tag, int id, int parentId);
		
		abstract protected void Terminal(string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content);

		abstract protected void Literal(string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content);

		abstract protected void Data (int trId, List<string> dataValues);

		#endregion

		#region Non-Terminals
		public virtual void VOTableBegin(int id, int parentId, OrderedDictionary<string, object> attributes, IXmlLineInfo iXmlLineInfo) {
			xmlLineInfo = iXmlLineInfo;  // Save the current line info object in case we need to report line numbers.

			NtBegin(Tags.VOTABLE, id, parentId, attributes);
		}

		public virtual void VOTableEnd(int id, int parentId) {
			NtEnd(Tags.VOTABLE, id, parentId);
		}

		public virtual void ResourceBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			NtBegin(Tags.RESOURCE, id, parentId, attributes);
		}

		public virtual void ResourceEnd(int id, int parentId) {
			NtEnd(Tags.RESOURCE, id, parentId);
		}
		
		public virtual void GroupBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			NtBegin(Tags.GROUP, id, parentId, attributes);
		}

		public virtual void GroupEnd(int id, int parentId) {
			NtEnd(Tags.GROUP, id, parentId);
		}
		
		public virtual void ParamBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			NtBegin(Tags.PARAM, id, parentId, attributes);
		}

		public virtual void ParamEnd(int id, int parentId) {
			NtEnd(Tags.PARAM, id, parentId);
		}
		
		public virtual void TableBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			NtBegin(Tags.TABLE, id, parentId, attributes);
		}

		public virtual void TableEnd(int id, int parentId) {
			NtEnd(Tags.TABLE, id, parentId);
		}
		
		public virtual void FieldBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			NtBegin(Tags.FIELD, id, parentId, attributes);
		}

		public virtual void FieldEnd(int id, int parentId) {
			NtEnd(Tags.FIELD, id, parentId);
		}
		
		public virtual void DataBegin(int id, int parentId, OrderedDictionary<string, object> attributes) {
			NtBegin(Tags.DATA, id, parentId, attributes);
		}

		public virtual void DataEnd(int id, int parentId) {
			NtEnd(Tags.DATA, id, parentId);
		}
		
		public virtual void ValuesBegin(int id, int parentId, OrderedDictionary<string, object> attributes)
		{
			NtBegin(Tags.VALUES, id, parentId, attributes);
		}

		public virtual void ValuesEnd(int id, int parentId) {
			NtEnd(Tags.VALUES, id, parentId);
		}
		
		public virtual void TableDataBegin(int id, int parentId, OrderedDictionary<string, object> attributes)
		{
			NtBegin(Tags.TABLEDATA, id, parentId, attributes);
		}

		public virtual void TableDataEnd(int id, int parentId) {
			NtEnd(Tags.TABLEDATA, id, parentId);
		}
		
		#endregion

		#region Terminals
		// The DEFINITIONS element was in v1.0, but deprecated in 1.1.
		public virtual void Definitions(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Literal(Tags.DEFINITIONS, id, parentId, attributes, content);
		}

		public virtual void Description(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.DESCRIPTION, id, parentId, attributes, content);
		}

		// The COOSYS element was in v1.1, but deprecated in 1.2.
		public virtual void Coosys(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.COOSYS, id, parentId, attributes, content);
		}

		public virtual void Info(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.INFO, id, parentId, attributes, content);
		}

		public virtual void Link(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.LINK, id, parentId, attributes, content);
		}

		public virtual void FieldRef(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.FIELDref, id, parentId, attributes, content);
		}

		public virtual void ParamRef(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.PARAMref, id, parentId, attributes, content);
		}

		public virtual void Min(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.MIN, id, parentId, attributes, content);
		}

		public virtual void Max(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.MAX, id, parentId, attributes, content);
		}

		public virtual void Option(int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			Terminal(Tags.OPTION, id, parentId, attributes, content);
		}

		
		public virtual void Tr(int id, int parentId, OrderedDictionary<string, object> attributes, List<string> dataValues) {
			NtBegin(Tags.TR, id, parentId, attributes);
			Data(id, dataValues);
			NtEnd(Tags.TR, id, parentId);
		}

		public virtual void Binary (int id, int parentId, OrderedDictionary<string, object> binaryAttributes, OrderedDictionary<string, object> streamAttributes, string streamVal) {
			NtBegin(Tags.BINARY, id, parentId, binaryAttributes);
			// What to do here?
			NtEnd(Tags.BINARY, id, parentId);
		}

		public virtual void Fits (int id, int parentId, OrderedDictionary<string, object> fitsAttributes, OrderedDictionary<string, object> streamAttributes, string streamVal) {
			NtBegin(Tags.FITS, id, parentId, fitsAttributes);
			// What to do here?
			NtEnd(Tags.FITS, id, parentId);
		}

		#endregion


	}
}

