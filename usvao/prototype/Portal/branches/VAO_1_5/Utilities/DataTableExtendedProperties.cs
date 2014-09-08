using System;
using System.Collections.Generic;
using System.Data;
using System.Text;

using JsonFx.Json;

namespace Utilities
{
	public class DataTableExtendedProperties
	{
		public const string KEY = "Paging";

		public int page=0;				// page number requested by client (default (0) is used when only pagesize is specified)
		public int pageSize=0;			// pagesize requested by client (default (1000) used when only page is specified)
		public int pagesFiltered=0;		// number of pages available on server, after filtering and paging
		
		public int rows=0;				// rows returned for active query
		public int rowsFiltered=0;		// rows available after filter(s) are applied
		public int rowsTotal=0;			// total rows available (unfiltered)
	
		public DataTableExtendedProperties ()
		{
		}

		public DataTableExtendedProperties(IDictionary<string, object> dict) {
			page = GetIntVal(dict, "page", 0);
			pageSize = GetIntVal(dict, "pageSize", 0);
			pagesFiltered = GetIntVal(dict, "pagesFiltered", 0);
			rows = GetIntVal(dict, "rows", 0);
			rowsFiltered = GetIntVal(dict, "rowsFiltered", 0);
			rowsTotal = GetIntVal(dict, "rowsTotal", 0);
		}
		
//		public string ToJson()
//		{
//			StringBuilder sb = new StringBuilder("\"" + KEY +"\":");
//			JsonWriter jw = new JsonFx.Json.JsonWriter(sb);
//			jw.Write(this);
//			return sb.ToString();
//		}
		
		public static DataTableExtendedProperties getProperties(DataTable dt)
		{
			DataTableExtendedProperties props = null;
			if (dt.ExtendedProperties.ContainsKey(KEY))
			{
				props = dt.ExtendedProperties[KEY] as DataTableExtendedProperties;
			}
			else
			{
				props = new DataTableExtendedProperties();
				dt.ExtendedProperties[KEY] = props;
			}
			return props;
		}

		private int GetIntVal(IDictionary<string, object> dict, string key, int defaultVal) {
			object o = null;
			int result = defaultVal;
			if (dict.TryGetValue(key, out o)) {
				result = (int)o;
			}
			return result;
		}
		
	}
}

