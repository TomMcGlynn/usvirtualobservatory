using System;
namespace Mashup
{	
	public interface IAsyncAdaptor
	{
		void invoke(MashupRequest muRequest, MashupResponse muResponse);
	}
}

