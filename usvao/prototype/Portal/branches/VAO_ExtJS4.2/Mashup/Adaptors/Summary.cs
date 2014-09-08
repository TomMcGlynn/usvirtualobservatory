using System;
using System.Data;
using System.Configuration;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Xml.Linq;
using System.Diagnostics;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Text;
using System.Threading;

using Mashup.Config;
using log4net;
using Utilities;
using JsonFx.Json;

namespace Mashup.Adaptors
{
    [Serializable]
    public class Summary : IAsyncAdaptor
    {
		// Log4Net Stuff
		public static readonly ILog log = LogManager.GetLogger (System.Reflection.MethodBase.GetCurrentMethod ().DeclaringType);
		public static string tid { get { return String.Format ("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] "; } }

		public String ra {get; set;}
		public String dec {get; set;}
		public String radius {get; set;}

		// Private members
		private DataSet resultSet = new DataSet("SummaryResultSet");
		private DataTable resultTable = new DataTable("SummaryTable");
		private List<AsyncRequest> searches = new List<AsyncRequest>();
		private Dictionary<string,object>[] resourceData = null;
		private WaitHandle[] allDoneFlags = null;
		private MashupResponse muResponse = null;
		private MashupRequest muRequest = null;

		private const int REQUEST_TIMEOUT = 60000;
		private const int TOTAL_TIMEOUT = 75000;

        public Summary(string iRa, string iDec, string iRadius) 
        {
			ra = iRa;
			dec = iDec;
			radius = iRadius;
        }
		
		//
		// IAdaptor::invoke()
		//
        public void invoke(MashupRequest iMuRequest, MashupResponse iMuResponse)
        {		
			muRequest = iMuRequest;
			muResponse = iMuResponse;

			// Assign the the resource data to an instance variable for easier access.
			resourceData = (Dictionary<string,object>[])muRequest.data;

			initializeResultTable();
			
			createSearches();

			respond ();
			
			startSearches();
			
			waitForCompletion();

			//			fakeResponses(muResponse);

        }

		private void initializeResultTable() {
			resultSet = new DataSet("SummaryResultSet");
			resultTable = new DataTable("SummaryTable");
			resultSet.Tables.Add (resultTable);

			// Columns
			resultTable.Columns.Add(new DataColumn("recordNumber", Type.GetType ("System.Int32")));
			resultTable.Columns.Add(new DataColumn("Status", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("Short Name", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("Title", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("Publisher", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("Description", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("Records Found", Type.GetType ("System.Int32")));
			resultTable.Columns.Add(new DataColumn("serviceId", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("capabilityClass", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("invokeBaseUrl", Type.GetType ("System.String")));
			resultTable.Columns.Add(new DataColumn("requestJson", Type.GetType ("System.String")));

			// Columns Config
			// Retreive Column Definitions for the Service and append them to the DataSet Column 'Extended Properties'
			Dictionary<string, object> props = ColumnsConfig.Instance.getColumnProperties(muRequest);
			if (props != null && props.Count > 0)
			{
				Utilities.Transform.AppendColumns(resultSet, props);
				Utilities.Transform.AppendColumnProperties(resultSet, props, ColumnsConfig.CC_PREFIX);
			}
		}

		private void createSearches() {
			string requestBaseUrl = muRequest.requestBaseUrl;
			for (int i=0; i<resourceData.Length; i++) {
				int recordNumber = (int)resourceData[i]["recordNumber"];
				string shortName = (string)resourceData[i]["shortName"];
				string title = (string)resourceData[i]["title"];
				string publisher = (string)resourceData[i]["publisher"];
				string desc = (string)resourceData[i]["description"];
				string serviceId = (string)resourceData[i]["serviceId"];
				string capabilityClass = (string)resourceData[i]["capabilityClass"];
				Dictionary<string, object> resRequest = (Dictionary<string, object>)resourceData[i]["request"];
				MashupRequest resMuRequest = new MashupRequest(resRequest);

				// Use this mashup server unless the client specified another one.
				string invokeBaseUrl = requestBaseUrl;
				string resourceBaseUrl = (string)resourceData[i]["invokeBaseUrl"];
				if (resourceBaseUrl != null) {
					invokeBaseUrl = resourceBaseUrl;
				}

				AsyncRequest search = createOneSearch(recordNumber, shortName, title, publisher, desc, serviceId, capabilityClass, invokeBaseUrl, resMuRequest, REQUEST_TIMEOUT);
				searches.Add(search);
			}

			// Create the array of allDone flags so that we can wait for them later.
			allDoneFlags = new WaitHandle[searches.Count];
			for (int i=0; i<searches.Count; i++) {
				allDoneFlags[i] = searches[i].allDone;
			}
		}

		private void startSearches() {
			for (int i=0; i<searches.Count; i++) {
				log.Info(tid + "Starting search for " + i + ", " + searches[i].name);
				searches[i].start();
			}
		}

		private void waitForCompletion() {
			// Note:  The total timeout should never be reached since it's greater than the individual request timeouts.
			bool allCompleted = WaitHandle.WaitAll(allDoneFlags, TOTAL_TIMEOUT);

			if (!allCompleted) {
				// Figure out which searches didn't complete, and make sure they're marked failed.
				for (int i=0; i<searches.Count; i++) {
					if (searches[i].status.Equals(AsyncRequest.PENDING) || searches[i].status.Equals(AsyncRequest.EXECUTING)) {
						searches[i].status = AsyncRequest.FAILED;
					}
				}

				// Respond that we're complete.
				respond();
			}
		}

		private AsyncRequest createOneSearch(int recordNumber, string iShortName, string iTitle, string iPublisher, string iDesc, string iServiceId, 
		                                     string iCapabilityClass, string iInvokeBaseUrl, 
		                                     MashupRequest iSearchMuRequest, int iTimeoutMs) {
			log.Info(tid + "Creating search for " + iTitle);

			string requestJson = iSearchMuRequest.ToJson();  // Save the client's request JSON before overriding the timeout, which is done for server purposes.

			iSearchMuRequest.timeout = ((2 * REQUEST_TIMEOUT) / 1000).ToString(); // (seconds) Make sure the mashup server doesn't timeout before our async request.
			string requestUrl = iSearchMuRequest.createRequestUrl(iInvokeBaseUrl);
			AsyncRequest request = new AsyncRequest(iTitle, requestUrl, new AsyncCallback (requestCallback), iTimeoutMs);

			Object[] rowData = {recordNumber, request.status, iShortName, iTitle, iPublisher, iDesc, 0, iServiceId, iCapabilityClass, iInvokeBaseUrl, requestJson};
			DataRow newRow = resultTable.Rows.Add (rowData);

			// Hang properties on the request that we might need later.
			request.Add("DataRow", newRow);

			return request;
		}

		private void requestCallback(IAsyncResult asynchronousResult)
		{
			AsyncRequest request = (AsyncRequest)asynchronousResult.AsyncState;
			if (request.status.Equals(AsyncRequest.COMPLETE)) {

				// Put the whole response in a string.
				StreamReader reader = new StreamReader (request.responseStream);
				string responseString = reader.ReadToEnd ();
				log.Debug (tid + "---> [SUMMARY cb] Reponse is:" + responseString.Substring (0, System.Math.Min (50, responseString.Length)));

				// Parse the response.
				ExtjsReponse respObject = new ExtjsReponse(responseString);

				// Put the repsonse values in the result set.
				DataRow requestRow = (DataRow)request["DataRow"];
				requestRow["Status"] = request.status;
				if (respObject.fullyParsed) {
					requestRow["Records Found"] = respObject.rowsTotal;
				} else {
					requestRow["Records Found"] = 0;
				}
				
				// Clean up
				reader.Close ();
				request.response.Close ();
			}

			// Whether we completed successfully or not, respond and mark us complete.
			respond();
			request.allDone.Set();
		}

		private bool isComplete() {
			Boolean complete = true;
			foreach(AsyncRequest request in searches) {
				if (request.status.Equals(AsyncRequest.PENDING) || request.status.Equals(AsyncRequest.EXECUTING)) {
					complete = false;
				}
			}
			return complete;
		}

		private void respond() {
			bool complete = isComplete ();
			muResponse.load(resultSet, complete);
		}
		#region fakeStuff
		//  Fake stuff below....

		private void fakeResponses(MashupResponse muResponse) {
			Object[][] rows = null;			// Initial Response

			rows = new Object[][] {
				new Object[] {"Hubble Legacy Archive", 0, "EXECUTING", "{\"url\":\"http://google.com\"}"},
				new Object[] {"Common Archive Observatory Model", 0, "EXECUTING", "{\"url\":\"http://google.com\"}"},
				new Object[] {"GSC2", 0, "EXECUTING", "{\"url\":\"http://google.com\"}"},
				new Object[] {"Galex (failed)", 0, "EXECUTING", "{\"url\":\"http://google.com\"}"}
			};
			respond (rows, muResponse);
			
			// Interim response after sleep
			Thread.Sleep(5000);
			rows[0][1] = 420;  rows[0][2] = "COMPLETE";  rows[0][3] = "{\"url\":\"http://hla.stsci.edu\"}";
			respond (rows, muResponse);
			
			// Interim response after sleep
			Thread.Sleep(5000);
			rows[2][1] = 612;  rows[2][2] = "COMPLETE";  rows[2][3] = "{\"url\":\"http://archive.stsci.edu\"}";
			respond(rows, muResponse);
			
			// Interim response after sleep
			Thread.Sleep(5000);
			rows[3][1] = 0; rows[3][2] = "Failed"; rows[3][3] = "{\"url\":\"http://galex.stsci.edu\"}";
			respond(rows, muResponse);
			
			// Final response after sleep
			Thread.Sleep(5000);
			rows[1][1] = 321;  rows[1][2] = "COMPLETE";  rows[1][3] = "{\"url\":\"http://archive.stsci.edu\"}";
			respond (rows, muResponse);	

		}

		private void respond(Object[][] rows, MashupResponse muResponse) {
			Boolean complete = true;
			for (int i=0; i<rows.Length; i++) {
				if (rows[i][2].Equals("EXECUTING")) {
					complete = false;
				}
			}

			DataSet ds = createDummyResultSet(rows);
			muResponse.load(ds, complete);
		}

		public DataSet createDummyResultSet(Object[][] rows) {
			resultSet = new DataSet("SummaryResultSet");
			resultTable = new DataTable("SummaryTable");

			DataColumn col1 = new DataColumn("Title", Type.GetType ("System.String"));
			DataColumn col2 = new DataColumn("Count", Type.GetType ("System.Int32"));
			DataColumn col3 = new DataColumn("status", Type.GetType ("System.String"));
			DataColumn col4 = new DataColumn("access", Type.GetType ("System.String"));

			
			resultSet.Tables.Add (resultTable);
			resultTable.Columns.Add(col1);
			resultTable.Columns.Add(col2);
			resultTable.Columns.Add(col3);
			resultTable.Columns.Add(col4);

			// Add data rows
			for (int i=0; i<rows.Length; i++) {
				resultTable.Rows.Add (rows[i]);
			}

			return resultSet;
		}
		#endregion
    }

	class ExtjsReponse {
		public bool fullyParsed = false;
		public string status = null;
		public int rowCount = 0;
		public int rowsTotal = 0;
		public Dictionary<string, object> pagingInfo = null;

		public ExtjsReponse(string iResponseString) {
			Dictionary<string, object> respDict = (Dictionary<string, object>) new JsonReader(iResponseString).Deserialize();
//			foreach (string k in respDict.Keys) {
//				Console.WriteLine("respDict[" + k + "] = " + respDict[k]);
//			}
			status = (string)respDict["status"];
			Object o = null;
			if (respDict.TryGetValue("data", out o) && o is Dictionary<string, object>) {
				Dictionary<string, object> respData = (Dictionary<string, object>)o;
				if (respData.TryGetValue("Tables", out o)) {
					Dictionary<string, object>[] tables = (Dictionary<string, object>[])o;
					if (tables.Length > 0) {
						// Don't need these lines because rowCount is pulled from extended properties below, but this shows how
						// to get at the rows themselves.
						// object[][]rows = (object[][])tables[0]["Rows"];
						// rowCount = rows.Length;

						if (tables[0].TryGetValue("ExtendedProperties", out o)) {
							Dictionary<string, object> ep = (Dictionary<string, object>)o;
							if (ep.TryGetValue("Paging", out o)) {
								pagingInfo = (Dictionary<string, object>)o;
								rowCount = (int)pagingInfo["rows"];
								rowsTotal = (int)pagingInfo["rowsTotal"];
								fullyParsed = true;
							}
						}
					}
				}
			}
		}
	}
}
