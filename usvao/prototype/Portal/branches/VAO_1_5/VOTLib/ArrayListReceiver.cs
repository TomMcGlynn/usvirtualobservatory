using System;
using System.IO;
using System.Data;
using System.Collections;
using System.Collections.Generic;

using log4net;

using Collections;
using VOTLib;

namespace VOTLib
{
	public class ArrayListReceiver : ConsolidatorReceiver
	{
		public readonly ArrayList Results = new ArrayList(1);

		protected Stack<ArrayList> parents = new Stack<ArrayList>();

		//
		// Logger Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }

		public ArrayListReceiver ()
		{
			parents.Push(Results);
		}

		#region Message Handlers
		public override void Debug (string format, params Object[] args)
		{
			if (log.IsDebugEnabled) {
				log.DebugFormat(tid + format, args);
				log.DebugFormat(tid + "   at line {0}, position {1}", LineNum(), LinePos());
			} else {
				Console.WriteLine("Debug: " + format, args);
				Console.WriteLine ("   at line {0}, position {1}", LineNum(), LinePos());
			}
		}

		public override void Informational (string format, params Object[] args)
		{
			if (log.IsInfoEnabled) {
				log.InfoFormat(tid + format, args);
				log.InfoFormat(tid + "   at line {0}, position {1}", LineNum(), LinePos());
			} else {
				Console.WriteLine("Info: " + format, args);
				Console.WriteLine ("   at line {0}, position {1}", LineNum(), LinePos());
			}
		}

		public override void Warning (string format, params Object[] args)
		{
			if (log.IsWarnEnabled) 
			{
				log.WarnFormat(tid + format, args);
				log.WarnFormat(tid + "   at line {0}, position {1}", LineNum(), LinePos());
			}
			else
			{
				Console.WriteLine ("Warning: " + format, args);
				Console.WriteLine ("   at line {0}, position {1}", LineNum(), LinePos());
			}
		}

		public override void Error (string format, params Object[] args)
		{
			if (log.IsErrorEnabled) 
			{
				log.ErrorFormat(tid + format, args);
				log.ErrorFormat(tid + "   at line {0}, position {1}", LineNum(), LinePos());
			}
			else
			{		
				Console.WriteLine ("Error: " + format, args);
				Console.WriteLine ("   at line {0}, position {1}", LineNum(), LinePos());		
			}
			throw new Exception("Error parsing VO Table");
		}
		
		#endregion

		#region Utility Methods

		public static ArrayList CreateAndAddElement(ArrayList parentElt, string tag, int id, int parentId, OrderedDictionary<string, object> attributes) {
			// Create the new xml element, represented by an ArrayList.
			ArrayList elt = new ArrayList();
			
			// The first ArrayList item will be a dictionary with the new element's metadata.
			OrderedDictionary<string, object> metadata = new OrderedDictionary<string, object>(3);
			metadata.Add(Tags.TAG_ATTR, tag);
			metadata.Add(Tags.ID_ATTR, id);
			metadata.Add(Tags.PARENT_ID_ATTR, parentId);
			elt.Add(metadata);

			// The second ArrayList item will be the actual attributes of the element.
			elt.Add(attributes);

			// Add the element to the "document".
			parentElt.Add(elt);

			return elt;
		}

		public static void CreateAndAddTerminalElement(ArrayList parentElt, string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content, bool isLiteral) {
			// Create the new xml element and add it to the current parent.
			ArrayList elt = CreateAndAddElement(parentElt, tag, id, parentId, attributes);
			
			// Add content as a dictionary attribute.  This allows us to distinguish regular content from literal content
			// which will not be escaped on writing back to a VO Table.
			OrderedDictionary<string, object> contentDict = new OrderedDictionary<string, object>(1);
			contentDict.Add(isLiteral ? Tags.LITERAL_CONTENT_ATTR : Tags.CONTENT_ATTR, content);
			elt.Add(contentDict);
		}

		#endregion

		#region Reporting Methods
		
		protected override void NtBegin(string tag, int id, int parentId, OrderedDictionary<string, object> attributes) {
			// Create the new xml element and add it to the current parent.
			ArrayList elt = CreateAndAddElement(parents.Peek(), tag, id, parentId, attributes);

			// Switch the current parent to this element.
			parents.Push(elt);
		}
		
		protected override void NtEnd(string tag, int id, int parentId) {
			// We're done with the current element, so pop up to the previous parent.
			parents.Pop();
		}
		
		protected override void Terminal(string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			CreateAndAddTerminalElement(parents.Peek(), tag, id, parentId, attributes, content, false);
		}

		protected override void Literal(string tag, int id, int parentId, OrderedDictionary<string, object> attributes, string content) {
			CreateAndAddTerminalElement(parents.Peek(), tag, id, parentId, attributes, content, true);
		}

		protected override void Data (int trId, List<string> dataValues)
		{
			// Add the Data row as a list to the current parent which is a TR.
			parents.Peek().Add(dataValues);
		}
		#endregion

	}
}

