using System;
using System.IO;
using System.Net;
using System.Web;
using System.Text;
using System.Threading;

using log4net;

namespace Utilities
{
	public class AsyncContext
	{
		// This class stores the State of an async request.
		
		//
		// Log4Net Stuff
		//
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }

		public HttpWebRequest request = null;
		public HttpWebResponse response = null;
		public Stream responseStream = null;
		public ManualResetEvent allDone = new ManualResetEvent (false);
		
		// Use this for storing the user specified data
		public string url;
		public object userData;
		public AsyncCallback callback;
		public int timeoutMs;

		// status
		public String status = PENDING;
		
		// Constants
		public const string PENDING = "PENDING";
		public const string EXECUTING = "EXECUTING";
		public const string COMPLETE = "COMPLETE";
		public const string FAILED = "FAILED";
		public const string ERROR = "ERROR";

		// These would only be used for aynsynchronous reading of the response stream:
		const int BUFFER_SIZE = 1024;
		public byte[] BufferRead = null;
		public StringBuilder requestData = null;
		
		public AsyncContext (string iUrl, object iUserData, AsyncCallback iCallback, Int32 iTimeoutMs)
		{
			url = iUrl;
			userData = iUserData;
			callback = iCallback;
			timeoutMs = iTimeoutMs;

			// Let's wait and see if we need this...
//			BufferRead = new byte[BUFFER_SIZE];
//			requestData = new StringBuilder ("");
		}
	}
}

