using System;
using System.Data;
using System.IO;
using System.Threading;
using System.Text;
using System.Configuration;
using System.Collections;
using System.Collections.Generic;

using ExcelLibrary;
using ExcelLibrary.SpreadSheet;

namespace Mashup
{
	public class MashupResponseData
	{
		//
		// TODO: Refactor the MashupResponseData Class into an OO model based on Response Data Type
		// to avoid all of the ugly 'switch' statements below, based on FormatType
		//	
		public string ContentType="text/text";
		
		public string status="EXECUTING";
		public string msg="";
		public float percentComplete=float.NaN;
		
		// Input Data
		public Dictionary<string, object> data=null;
		
		// Input Data Set:
		public DataSet dsin = null;
		
		// Intermediate Sorted/Filtered Data Set:
		public DataSet dssort = null;
		
		// Intermediate Paged/Output Data Set:
		public DataSet dspage = null;
		
		// Output Products: string or workbook
		public StringBuilder ob = null;
		public Workbook wb = null;
        public Histogram histogram;
	
	    public MashupResponseData ()
		{
		}
		
		public string getOutBuffer(int length=200)
		{
			if (ob != null && ob.Length > 0)
			{
				return (ob.Length > length ? ob.ToString(0, length) : ob.ToString());
			}
			else
			{
				return "";
			}
		}
	}
}

