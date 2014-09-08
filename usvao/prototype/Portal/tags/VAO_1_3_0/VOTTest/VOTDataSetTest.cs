using System;
using System.IO;
using System.Text;
using System.Xml;
using System.Data;
using VOTLib;

namespace VOTTest
{
	public class VOTDataSetTest
	{
		public static void Main (string[] args)
		{	
//			string fileName = "../../Resources/CaomConeVotable.xml";
//			string fileName = "../../Resources/GalexSiapVotable.xml";
//			string fileName = "../../Resources/GalexConeVotable.xml";
//			string fileName = "../../Resources/DataScopeTest.xml";
//			string fileName = "../../Resources/CADC_HST.90.xml";
//			string fileName = "../../Resources/vizier_cs-7.xml";
//			string fileName = "../../Resources/ISSA.30.xml";
//			string fileName = "../../Resources/2MASS_QL.29.xml";			
//			string fileName = "../../Resources/HST_STIS_Spectra.9738.xml";
			string fileName = "../../Resources/IUE.9748.xml";
			Console.WriteLine("Parsing Filename: " + fileName);

			Stream stream = new FileStream (fileName, FileMode.Open);
			XmlTextReader reader = new XmlTextReader (stream);
			DataSet dataSet = new DataSet ("VOTDataSet");
			
			// Create Receiver (3 types possible)
//			VOTTwoTableDataSetReceiver receiver = new VOTTwoTableDataSetReceiver (reader, dataSet);
//			VOTDataSetReceiver receiver = new VOTDataSetReceiver (reader, dataSet);
			DebugReceiver receiver = new DebugReceiver();
			
			// Parse the input stream, throwing output to the receiver
			VOTParser parser = new VOTParser (reader, receiver);	
			parser.Parse ();
			
			dataSet.WriteXml ("VOTDataSet.xml", XmlWriteMode.WriteSchema);
		}
	}
}

