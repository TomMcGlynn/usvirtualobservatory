using System;
using System.Collections.Generic;
using System.Text;

namespace Utilities
{
	public class ParamString
	{
		private ParamString ()
		{
		}
		
		public static string replaceAllParams(string paramString, Dictionary<string, object>dict)
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
						string token = param.Split(']')[0];
						string key = token.Trim().ToLower();
						string defaultval = "";
						
						if (token.IndexOf(":") > 0)
						{
							string[]keyval = token.Split(':');
							key = keyval[0].Trim().ToLower();
							defaultval = keyval[1].Trim();
						}
						
						object val="";
						if (dict.TryGetValue(key, out val) && val.ToString().Trim().Length > 0)
						{
							sb.Replace("[" + token + "]", val.ToString());
						} 
						else if (defaultval.Length > 0)
						{
							sb.Replace("[" + token + "]", defaultval);
						}
						else
						{
							throw new Exception("Request Object is Missing Required Parameter : " + key);
						}
					}
				}
			}
							
			return sb.ToString();
		}
	}
}

