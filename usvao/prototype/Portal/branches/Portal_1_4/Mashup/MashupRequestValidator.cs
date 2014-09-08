using System;
using System.Web;
using System.Web.Util;

namespace Mashup
{
	public class MashupRequestValidator : RequestValidator
	{
		public MashupRequestValidator ()
		{
		}
		
		protected override bool IsValidRequestString( HttpContext context, 
		                                             string value, 
		                                             RequestValidationSource requestValidationSource, 
		                                             string collectionKey,
        											 out int validationFailureIndex)
        {
			validationFailureIndex = -1;
			return true;
    	}
	}
}

