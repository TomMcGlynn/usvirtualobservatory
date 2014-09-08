using System;
using System.IO;
using System.Text;
using System.Xml;
using System.Data;
using System.Collections;
using System.Collections.Generic;

using Collections;
using VOTLib;
using JsonFx.Json;
using Utilities;
using Mashup;

namespace VOTTest
{
	public class VOTTest
	{
		static string[] SIMPLE = {
			"../../Resources/VAOPD-784/input/m101-galex.xml",
			"../../Resources/VAOPD-784/input/m101-dss.xml",
			"../../Resources/VAOPD-784/input/m101-hla.xml",
			"../../Resources/VAOPD-784/input/3c066a.xml",
			"../../Resources/VAOPD-784/input/asdc-small.xml",
			"../../Resources/VAOPD-784/input/vizier_votable.xml",
			"../../Resources/VAOPD-784/input/m101-caom-0.xml",
			"../../Resources/VAOPD-784/input/CSC-SCS.xml",
			"../../Resources/VAOPD-784/input/CSC2.xml",
			//"../../Resources/VAOPD-784/input/CSC2-2.xml",
			"../../Resources/VAOPD-784/input/CXC-SIAP.xml",
			"../../Resources/VAOPD-784/input/IRAM-precision-test.xml",
			"../../Resources/VAOPD-784/input/SDSS-null-large.xml",
			"../../Resources/VAOPD-784/input/boolean.xml"
		};
		static string[] SDSS = {
			"../../Resources/VAOPD-784/input/SDSS-null-large.xml"
		};
		static string[] NULLS = {
			"../../Resources/VAOPD-784/input/intNulls.xml",
			"../../Resources/VAOPD-784/input/floatNulls.xml"
		};

		public static void Main (string[] args)
		{	
			RoundTrip(NULLS, true);
			RoundTrip(SIMPLE, true);
			//conversionTest();
			// RunLoggingTest(SIMPLE);
		}

		public static void RunLoggingTest(string[] files) {
			for (int i=0; i<files.Length; i++) {
				DateTime start = DateTime.Now;

				string input = files[i];
				string output = Path.GetDirectoryName(input) + "/../outLog/" + Path.GetFileName(input);
				string outputArray = Path.GetDirectoryName(input) + "/../outArray/" + Path.GetFileName(input);
				Console.WriteLine("Parsing  " + input);

				Stream inStream = new FileStream (input, FileMode.Open);

				using (XmlReader reader = XmlReader.Create(inStream, new XmlReaderSettings {DtdProcessing = DtdProcessing.Ignore}))
				{
					// Create logging receiver
					StreamWriter outStream = new StreamWriter(output);
					LoggingReceiver loggingReceiver = new LoggingReceiver(outStream, 10000, false);

					// Create and ArrayList and DataSet receivers
					ArrayListReceiver arrayListReceiver = new ArrayListReceiver();
					VOTDataSetReceiver dsReceiver = new VOTDataSetReceiver();
					VOTReceiver[] receivers = new VOTReceiver[] {loggingReceiver, arrayListReceiver, dsReceiver};

					VOTParser parser = new VOTParser (reader, receivers);
					parser.Parse ();

					// Write out VOT table from ArrayList.
					Console.WriteLine("Writing ArrayList");
					StreamWriter outArrayStream = new StreamWriter(outputArray);
					VOTWriter arrayWriter = new VOTWriter(outArrayStream);
					arrayWriter.Formatting = System.Xml.Formatting.Indented;
					string invalidReason = null;
					if (arrayListReceiver.Results.Count == 1) {
						arrayWriter.WriteVoTable((ArrayList)arrayListReceiver.Results[0], out invalidReason);
					} else {
						Console.WriteLine("Array List Count != 1");
					}
					Console.WriteLine("Array result:  " + invalidReason);

					inStream.Close();
					reader.Close();
					outStream.Close ();
					outArrayStream.Close ();
					Console.WriteLine ("Output:  " + output);
				}
				LogTimeSince(start, "to read file and write all outputs");
			}
			
			Console.WriteLine ("Done");
		}		
		
		public static void RoundTrip(string[] files, bool shouldAppendHistogram) {
			for (int i=0; i<files.Length; i++) {

				string input = files[i];
				string path = Path.GetDirectoryName(input);
				string filename = Path.GetFileName(input);
				string outDs2Vot =  path + "/../outDs2Vot/" + filename;
				string outDs2Json =  path + "/../outDs2Json/" + filename + ".json";
				string outDs2Json2Vot =  path + "/../outDs2Json2Vot/" + filename;
				
				Stream inStream = new FileStream (input, FileMode.Open);
				
				using (XmlReader reader = XmlReader.Create(inStream, new XmlReaderSettings {DtdProcessing = DtdProcessing.Ignore}))
				{
					DataSet ds = null;
					{
						DateTime start = DateTime.Now;
						ds = Transform.VoTableToDataSet(reader);
						LogTimeSince(start, "Parsed the VOT.");

						if (shouldAppendHistogram) {
							appendHistogram(ds, ds);
							LogTimeSince(start, "Appended the histogram.");
						}
						
						inStream.Close ();
						reader.Close();
					}
					
					{
						DateTime start = DateTime.Now;
						StreamWriter outStream = new StreamWriter(outDs2Vot);
						VOTWriter w = new VOTWriter(outStream, 100000, false);
						w.Formatting = System.Xml.Formatting.Indented;
						string invalidReason = null;
						w.WriteVoTable(ds, out invalidReason);
						LogTimeSince(start, "Wrote the VOT from the DataSet." + ((invalidReason != null) ? ("  invalidReason = " + invalidReason) : ""));
						
						w.Close();
						outStream.Close ();
					}
					
					{
						DateTime start = DateTime.Now;
						StreamWriter outStreamJson = new StreamWriter(outDs2Json);
						StringBuilder jsonString = new StringBuilder();
						Transform.DataSetToExtjs(ds, jsonString, true);
						outStreamJson.WriteLine(jsonString);
						LogTimeSince(start, "Wrote the ExtJS from the DataSet to " + outDs2Json);
					
						outStreamJson.Close();
					}	

					DataSet dsFromJson = null;
					{
						DateTime start = DateTime.Now;
						StreamReader jsonReader = new StreamReader(outDs2Json);
						string jsonReadString = jsonReader.ReadToEnd();
						dsFromJson = Transform.ExtJsToDataSet(jsonReadString);
						LogTimeSince(start, "Read the ExtJS from the file from " + outDs2Json);
						
						jsonReader.Close();
					}

					{
						DateTime start = DateTime.Now;
						StreamWriter outStreamRt = new StreamWriter(outDs2Json2Vot);
						VOTWriter w = new VOTWriter(outStreamRt, 100000, false);
						w.Formatting = System.Xml.Formatting.Indented;
						string invalidReason = null;
						w.WriteVoTable(dsFromJson, out invalidReason);
						LogTimeSince(start, "Wrote the Round Trip VOT from the DataSet to " + outDs2Json2Vot);
						
						w.Close ();
						outStreamRt.Close ();
					}
					
				}
				Console.WriteLine ("Done processing: " + input);
			}
			Console.WriteLine ("Done Round Trip Test: ");
		}
		
		public static void LogTimeSince(DateTime since, string msg) {
			TimeSpan span = DateTime.Now.Subtract(since);
			Console.WriteLine(span + " : " + msg);
		}

		// The input and output can be the same, but don't run them through here twice.
		private static void appendHistogram(DataSet dsin, DataSet dsout)
		{
			Histogram h = new Histogram(dsin.Tables[0]);
			
			// Append the Histogram to the Output DataSet Columns
			Dictionary<string, Object> dict = h.getHistogram();
			DataTable outDt = dsout.Tables[0];
			foreach (DataColumn column in outDt.Columns)
			{
				string name = column.ColumnName;
				if (!dict.ContainsKey(name)) continue;
				var histObj = dict[name];
				column.ExtendedProperties.Add("histObj", histObj);
			}
		}

		private static void conversionTest() {
			// Floats
//			convert ("1.0", VOTType.DS_SINGLE, null);
//			convert ("NaN", VOTType.DS_SINGLE, null);
//			convert ("Inf", VOTType.DS_SINGLE, null);
//			convert ("Infinity", VOTType.DS_SINGLE, null);
//			convert ("-Inf", VOTType.DS_SINGLE, null);
//			convert ("-Infinity", VOTType.DS_SINGLE, null);
//			convert ("-9999", VOTType.DS_SINGLE, -9999);
//			convert ("", VOTType.DS_SINGLE, null);

			// Ints
			convert ("0x01", VOTType.DS_INT32, null);

			// booleans
			convert ("True", VOTType.DS_BOOLEAN, null);
			convert ("tRue", VOTType.DS_BOOLEAN, null);
			convert ("TRUE", VOTType.DS_BOOLEAN, null);
			convert ("False", VOTType.DS_BOOLEAN, null);
			convert ("fAlse", VOTType.DS_BOOLEAN, null);
			convert ("FALSE", VOTType.DS_BOOLEAN, null);
			convert ("T", VOTType.DS_BOOLEAN, null);
			convert ("t", VOTType.DS_BOOLEAN, null);
			convert ("F", VOTType.DS_BOOLEAN, null);
			convert ("f", VOTType.DS_BOOLEAN, null);
			convert ("1", VOTType.DS_BOOLEAN, null);
			convert ("0", VOTType.DS_BOOLEAN, null);
		}

		private static void convert(string input, Type targetType, object nullVal) {
			object o = null;
			string message = null;
			Console.Write("Type: " + targetType + ", Input/Output:   " + input);
			try {
				o = VOTDataSetReceiver.Convert(input, targetType, nullVal);
			} catch (Exception e) {
				message = e.Message;
				o = null;
			}
			Console.WriteLine(" / " + ((o == null) ? "null" : o) + ((message != null) ? ("           ---> Exception: " + message) : ""));
		}

	}

}

