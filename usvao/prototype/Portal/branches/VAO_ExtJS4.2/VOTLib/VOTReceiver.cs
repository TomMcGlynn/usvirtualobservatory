using System;
using System.Data;
using System.Collections.Generic;
using System.Xml;

using Collections;

namespace VOTLib
{
	public interface VOTReceiver
	{

		#region Messages
		void Debug (string format, params Object[] args);
		void Informational (string format, params Object[] args);
		void Warning(string format, params Object[] args);
		void Error(string format, params Object[] args);
		#endregion
		
		#region Tag Receivers

		#region Non-Terminals (branches)
		void VOTableBegin(int id, int parentId, OrderedDictionary<string, object> attributes, IXmlLineInfo lineInfo);
		void VOTableEnd(int id, int parentId);
		
		void ResourceBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void ResourceEnd(int id, int parentId);
		
		void GroupBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void GroupEnd(int id, int parentId);
		
		void ParamBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void ParamEnd(int id, int parentId);
		
		void TableBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void TableEnd(int id, int parentId);
		
		void FieldBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void FieldEnd(int id, int parentId);
		
		void DataBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void DataEnd(int id, int parentId);
		
		void ValuesBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void ValuesEnd(int id, int parentId);
		
		void TableDataBegin(int id, int parentId, OrderedDictionary<string, object> attributes);
		void TableDataEnd(int id, int parentId);
		#endregion

		#region Terminals (leaves)
		// The DEFINITIONS element was in v1.0, but deprecated in 1.1.
		void Definitions(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void Description(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		// The COOSYS element was in v1.1, but deprecated in 1.2.
		void Coosys(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void Info(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void Link(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void FieldRef(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void ParamRef(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void Min(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void Max(int id, int parentId, OrderedDictionary<string, object> attributes, string content);
		void Option(int id, int parentId, OrderedDictionary<string, object> attributes, string content);

		// Pseudo-terminals because I don't want to change the existing code yet to break out 
		// the data elements.
		void Tr(int id, int parentId, OrderedDictionary<string, object> attributes, List<string> dataValues);
		void Binary (int id, int parentId, OrderedDictionary<string, object> binaryAttributes, OrderedDictionary<string, object> streamAttributes, string streamVal);
		void Fits (int id, int parentId, OrderedDictionary<string, object> fitsAttributes, OrderedDictionary<string, object> streamAttributes, string streamVal);
		#endregion

		#endregion
		
	}
}

