
using System.IO;
using System.Xml;
using VOTLib;

namespace VOTTest
{
	class MainClass
	{
		public static void Main (string[] args)
		{
			
			string fileName = "../../Resources/testfile.xml";
			Stream stream = new FileStream(fileName, FileMode.Open);
			XmlTextReader reader = new XmlTextReader(stream);
			
			DebugReceiver receiver = new DebugReceiver();
			
			VOTParser parser = new VOTParser(reader, receiver);
			
			parser.Parse();
		}
	}
}

