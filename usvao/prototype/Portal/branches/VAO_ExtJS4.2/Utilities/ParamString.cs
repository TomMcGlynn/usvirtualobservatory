using System;
using System.Collections.Generic;
using System.Text;
using System.Web;

namespace Utilities
{
	public class ParamString
	{
		private ParamString ()
		{
		}
		
		public static string replaceAllParams(string paramString, IDictionary<string, object>dict, Boolean encodeParam=false)
		{
			//
			// Replace every [PARAM] in input ParamString with it's equivalent request param value
			//
			StringBuilder sb = new StringBuilder(paramString);

			if (paramString.IndexOf("[") >= 0)
			{
				//
				// Extract [PARAM]s from input String
				//
				string[] paramStrings = paramString.Split('[');
				
				//
				// Extract each [PARAM] from the Input String and do the following:
				//
				// (1) Check if it has a default value [PARAM:DEFAULT]
				//     If so, extract and separate the PARAM Name and DEFAULT value.
				//
				// (2) Check if the input Request Object contains the PARAM extracted above. 
				//     If so, extract the Request Object value and insert it in the Input String
				//
				// (3) If not, use the DEFAULT value extracted above
				//
				// (4) If there is not a Request Object param and no DEFAULT value has been specified - 
				//     Throw exception to indicate required PARAM is missing from the input string
				//
				foreach (string param in paramStrings)
				{
					if (param.IndexOf("]") > 0)
					{
						//
						// Extract each Parameter String (ps) Name:Value Pair
						// Then extract the Parameter String (ps) Name and the Parameter String (ps)Value
						//
						string psNameVal = param.Split(']')[0];
						string psName = psNameVal.Trim().ToLower();
						string psVal = "";
						
						if (psNameVal.IndexOf(":") > 0)
						{
							int i = psNameVal.IndexOf(':');
							psName = psNameVal.Substring(0, i).ToLower();
							psVal = psNameVal.Substring(i+1);
						}
						
						//
						// If the parameter was specified in the dict request 
						// Then insert its value into the Parameter String
						//
						object val="";
						string replacement = null;
						if (dict != null && dict.TryGetValue(psName, out val) && val.ToString().Trim().Length > 0)
						{
							replacement = val.ToString();
						} 
						else if (dict != null && dict.TryGetValue(psName.ToUpper(), out val) && val.ToString().Trim().Length > 0)
						{
							replacement = val.ToString();
						} 
						else if (psVal.Length > 0)
						{
							replacement = psVal;
						}
						else
						{
							throw new Exception("Request Object is Missing Required Parameter : " + psName);
						}
						
						if (replacement != null) {
							if (encodeParam) replacement = Uri.EscapeDataString(replacement);
							sb.Replace("[" + psNameVal + "]", replacement);
						}
					}
				}
			}
							
			return sb.ToString();
		}
	}
}

