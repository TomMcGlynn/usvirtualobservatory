using System;
using System.Web;

namespace Mashup
{	
	public interface ISyncAdaptor
	{
		void invoke(MashupRequest muRequest, HttpResponse httpResponse);
	}
}

