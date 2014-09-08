using System;
using System.Data;
using System.IO;
using System.Xml;
using VOTLib;

namespace VOTTest
{
	public class TestDS
	{
		public static void Main (string[] args)
		{
			
			string fileName = "../../Resources/testfile.xml";
			Stream stream = new FileStream(fileName, FileMode.Open);
			XmlTextReader reader = new XmlTextReader(stream);
			DataSet ds = new DataSet("TestDS");
			VOTDataSetReceiver receiver = new VOTDataSetReceiver(reader, ds);
			
			//receiver.CreateRowsWithItemArray();
			//receiver.TestDictionary();
			
//			VOTParser parser = new VOTParser(reader, receiver);
//			
//			parser.Parse();
		}

		public TestDS ()
		{
			
		}
	}
}

