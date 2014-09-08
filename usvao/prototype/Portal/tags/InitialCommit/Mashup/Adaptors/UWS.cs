using System;
using UWS;

namespace Mashup.Adaptors
{
	[Serializable]
	public class UWS : IAsyncAdaptor
	{
		public String url {get; set;}
		public String query {get; set;}
		public String run {get; set;}
		
		public UWS ()
		{
			url = "";
			query = "";
			run = "";
		}
			
		//
		// IAdaptor::invoke()
		//
	    public void invoke(ServiceRequest request, ServiceResponse response)
	    {
			string sUrl = Utilities.ParamString.replaceAllParams(url, request.paramss);
			string sQuery = Utilities.ParamString.replaceAllParams(query, request.paramss);
			string sRun = Utilities.ParamString.replaceAllParams(run, request.paramss);
			
			// Pass in the Response Object so that UWS can load response data
			UWSClient client = new UWSClient(sUrl, sQuery, sRun, request, response);
			client.start();
		}
	}
}

