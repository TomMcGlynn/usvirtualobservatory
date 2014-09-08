using System;
namespace Mashup
{	
	public interface IAsyncAdaptor
	{
		void invoke(ServiceRequest request, ServiceResponse response);
	}
}

