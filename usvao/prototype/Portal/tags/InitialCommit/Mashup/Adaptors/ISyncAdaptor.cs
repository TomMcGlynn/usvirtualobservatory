using System;
using System.Web;

namespace Mashup
{	
	public interface ISyncAdaptor
	{
		void invoke(ServiceRequest request, HttpResponse httpResponse);
	}
}

