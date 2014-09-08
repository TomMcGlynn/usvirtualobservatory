using System;
using System.Data;
using System.Text;

using JsonFx.Json;

namespace Utilities
{
	public class DataTableExtendedProperties
	{
		public const string KEY = "ExtendedProperties";

		public int page=0;				// page number requested by client (default (0) is used when only pagesize is specified)
		public int pageSize=0;			// pagesize requested by client (default (1000) used when only page is specified)
		public int pagesFiltered=0;		// number of pages available on server, after filtering and paging
		
		public int rows=0;				// rows returned for active query
		public int rowsFiltered=0;		// rows available after filter(s) are applied
		public int rowsTotal=0;			// total rows available (unfiltered)
	
		public DataTableExtendedProperties ()
		{
		}
		
		public string ToJson()
		{
			StringBuilder sb = new StringBuilder("\"" + KEY +"\":");
			JsonWriter jw = new JsonFx.Json.JsonWriter(sb);
			jw.Write(this);
			return sb.ToString();
		}
		
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
	}
}

