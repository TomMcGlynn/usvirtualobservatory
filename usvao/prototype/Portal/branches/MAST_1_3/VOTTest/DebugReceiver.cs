using System;
using System.IO;
using System.Data;
using System.Collections.Generic;
using VOTLib;

namespace VOTTest
{
	public class DebugReceiver : VOTReceiver
	{
		public DebugReceiver ()
		{
		}

		public void Debug (string format, params Object[] args)
		{
			Console.Write (format, args);
		}

		public void Informational (string format, params Object[] args)
		{
			Console.Write (format, args);
		}

		public void Warning (string format, params Object[] args)
		{
			Console.Write ("Rec. Warning:  " + String.Format(format, args));
		}

		public void Error (string format, params Object[] args)
		{
			Console.Write ("Rec. Error:  " + String.Format(format, args));
		}

		public void VOTableBegin (List<int> treeLocation, PropertyCollection attributes, string description)
		{
			reportJunk (Tags.VOTABLE, treeLocation, 0, attributes, description, null);
		}

		public void Definitions (List<int> treeLocation, PropertyCollection attributes, string content)
		{
			reportJunk (Tags.DEFINITIONS, treeLocation, 0, attributes, null, content);
		}

		public void Coosys (List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			reportJunk (Tags.COOSYS, treeLocation, index, attributes, null, content);
		}

		public void Info (List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			reportJunk (Tags.INFO, treeLocation, index, attributes, null, content);
		}

		public void Param (List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			reportJunk (Tags.PARAM, treeLocation, index, attributes, null, content);
		}

		public void Group (List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			reportJunk (Tags.GROUP, treeLocation, index, attributes, null, content);
		}

		public void Link (List<int> treeLocation, int index, PropertyCollection attributes, string content)
		{
			reportJunk (Tags.LINK, treeLocation, index, attributes, null, content);
		}

		public void Resource (List<int> treeLocation, PropertyCollection attributes, string description)
		{
			reportJunk (Tags.RESOURCE, treeLocation, 0, attributes, description, null);
		}

		public void Table (List<int> treeLocation, PropertyCollection attributes, string description)
		{
			reportJunk (Tags.TABLE, treeLocation, 0, attributes, description, null);
		}

		public void Field (List<int> treeLocation, int index, PropertyCollection attributes, string description, string values, List<PropertyCollection> fieldLinks)
		{
			reportJunk (Tags.FIELD, treeLocation, index, attributes, description, null);
		}

		public void Tr (List<int> treeLocation, int index, PropertyCollection attributes, List<string> dataValues)
		{
			reportJunk (Tags.TR, treeLocation, index, attributes, null, null);
			reportData (dataValues, 4);
		}

		public void Binary (List<int> treeLocation, PropertyCollection fitsAttributes, PropertyCollection streamAttributes, string streamVal)
		{
			reportJunk (Tags.BINARY, treeLocation, 0, streamAttributes, null, null);
		}

		public void Fits (List<int> treeLocation, PropertyCollection fitsAttributes, PropertyCollection streamAttributes, string streamVal)
		{
			reportJunk (Tags.FITS, treeLocation, 0, streamAttributes, null, null);
		}

		private void reportJunk (string tag, List<int> treeLocation, int index, PropertyCollection attributes, string description, string content)
		{
			Console.Write ("<{0}", tag);
			Console.Write (" treeLoc=\"");
			foreach (int i in treeLocation) {
				Console.Write ("{0}.", i);
			}
			Console.Write ("-{0}", index);
			Console.Write ("\"");
			
			foreach (string key in attributes.Keys)
			{
				if (key != null)
				{
					string val = attributes[key].ToString();
					Console.Write (" {0}=\"{1}\"", key, val);
				}
			}
			
			Console.WriteLine (">");
			
			if (description != null) {
				Console.WriteLine ("<" + Tags.DESCRIPTION + ">");
				Console.WriteLine (description);
				Console.WriteLine ("</" + Tags.DESCRIPTION + ">");
			}
			
			if (content != null) {
				Console.WriteLine (content);
			}
		}

		private void reportData (List<string> dataValues, int limit)
		{
			List<string> subList = dataValues;
			if ((limit > 0) && (limit <= dataValues.Count)) {
				subList = dataValues.GetRange (0, limit);
			}
			foreach (string s in subList) {
				Console.Write ("<TD>{0}</TD>", s);
			}
			Console.WriteLine ("...");
		}
		
		
		
	}
}

