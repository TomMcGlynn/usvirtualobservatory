using System;
using System.IO;
using System.Data;
using System.Collections;
using System.Collections.Generic;

using Collections;
using VOTLib;

namespace VOTLib
{
	public class LoggingReceiver : ConsolidatorReceiver
	{
		public bool LoggingEnabled = true;

		private bool IncludeIds = true;

		int fDataOutputLimit = 0;
		VOTWriter w = null;

		public LoggingReceiver (TextWriter iWriter, int iOutputDataLimit) : this(iWriter, iOutputDataLimit, false)
		{
		}

		public LoggingReceiver (TextWriter iWriter, int iOutputDataLimit, bool iIncludeIds)
		{
			fDataOutputLimit = iOutputDataLimit;
			IncludeIds = iIncludeIds;
			w = new VOTWriter(iWriter, fDataOutputLimit, IncludeIds);
			w.Formatting = System.Xml.Formatting.Indented;
		}

		#region Message Handlers
		public override void Debug (string format, params Object[] args)
		{
			string s = "Parser Debug: " + String.Format(format, args) + " at line " + LineNum() + ", position " + LinePos();	
			w.WriteComment(s);
		}

		public override void Informational (string format, params Object[] args)
		{
			string s = "Parser Informational: " + String.Format(format, args) + " at line " + LineNum() + ", position " + LinePos();
			w.WriteComment(s);
		}

		public override void Warning (string format, params Object[] args)
		{
			string s = "Parser Warning: " + String.Format(format, args) + " at line " + LineNum() + ", position " + LinePos();
			w.WriteComment(s);
		}

		public override void Error (string format, params Object[] args)
		{
			string s = "Parser Error: " + String.Format(format, args) + " at line " + LineNum() + ", position " + LinePos();
			w.WriteComment(s);
		}
		
		#endregion


		#region Writers

		void WriteIds(int id, int parentId) {			
			if (IncludeIds) {
				w.WriteAttributeString("_id", id.ToString());
				w.WriteAttributeString("_parentId", parentId.ToString());
			}
		}

		void WriteAttributes(OrderedDictionary<string, object> attributes) {
			foreach (KeyValuePair<string, object> e in attributes) {
				w.WriteAttributeString(e.Key.ToString(), e.Value.ToString());
			}
		}

		#endregion

		#region Reporting Methods
		
		protected override void NtBegin(string tag, int id, int parentId, OrderedDictionary<string, object> attributes) {
			w.WriteNtBegin(tag, id, parentId, attributes);
		}
		
		protected override void NtEnd(string tag, int id, int parentId) {
			w.WriteNtEnd(tag, id, parentId);
		}
		
		protected override void Terminal(string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			w.WriteTerminal(tag, id, parentId, attributes, content, false);
		}

		protected override void Literal(string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			w.WriteTerminal(tag, id, parentId, attributes, content, true);
		}

		protected override void Data (int trId, List<string> dataValues)
		{
			w.WriteData(trId, dataValues);
		}

		#endregion

	}
}

