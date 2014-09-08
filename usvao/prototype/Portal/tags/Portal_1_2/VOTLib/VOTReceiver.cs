using System;
using System.Data;
using System.Collections.Generic;

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
		void VOTableBegin(List<int> treeLocation, PropertyCollection attributes, string description);
		
		// The DEFINITIONS element was in v1.0, but deprecated in 1.1.
		void Definitions(List<int> treeLocation, PropertyCollection attributes, string content);
		
		// The COOSYS element was in v1.1, but deprecated in 1.2.
		void Coosys(List<int> treeLocation, int index, PropertyCollection attributes, string content);
		void Info(List<int> treeLocation, int index, PropertyCollection attributes, string content);
		void Param(List<int> treeLocation, int index, PropertyCollection attributes, string content);
		void Group(List<int> treeLocation, int index, PropertyCollection attributes, string content);
		void Link(List<int> treeLocation, int index, PropertyCollection attributes, string content);
		void Resource(List<int> treeLocation, PropertyCollection attributes, string description);
		
		void Table(List<int> treeLocation, PropertyCollection attributes, string description);
		void Field(List<int> treeLocation, int index, PropertyCollection attributes, string description, string values, List<PropertyCollection> fieldLinks);
		void Tr(List<int> treeLocation, int index, PropertyCollection attributes, List<string> dataValues);
		void Binary(List<int> treeLocation, PropertyCollection fitsAttributes, PropertyCollection streamAttributes, string streamVal);
		void Fits(List<int> treeLocation, PropertyCollection fitsAttributes, PropertyCollection streamAttributes, string streamVal);
		#endregion
		
	}
}

