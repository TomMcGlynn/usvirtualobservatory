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
	public class SummaryWrapper : IAsyncAdaptor
	{
		// Log4Net Stuff
		public static readonly ILog log = LogManager.GetLogger (System.Reflection.MethodBase.GetCurrentMethod ().DeclaringType);
		public static string tid { get { return String.Format ("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] "; } }
		
		public String ra {get; set;}
		public String dec {get; set;}
		public String radius {get; set;}

		public SummaryWrapper() 
		{
			ra = "";
			dec = "";
			radius = "";
		}
		
		//
		// IAdaptor::invoke()
		//
		public void invoke(MashupRequest iMuRequest, MashupResponse iMuResponse)
		{		
			string sRa = Utilities.ParamString.replaceAllParams(ra, iMuRequest.paramss);
			string sDec = Utilities.ParamString.replaceAllParams(dec, iMuRequest.paramss);
			string sRadius = Utilities.ParamString.replaceAllParams(radius, iMuRequest.paramss);

			Summary s = new Summary(sRa, sDec, sRadius);
			s.invoke (iMuRequest, iMuResponse);
		}
	}
}

