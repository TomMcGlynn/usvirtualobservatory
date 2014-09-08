using System;
using System.Data;
using System.IO;
using System.Threading;
using System.Text;

namespace Mashup
{	
	public class ServiceResponse
	{		
		//
		// Acccessors 
		//
		public Thread thread;
		
		public Boolean isActive{
		get {
			return (this.thread != null && thread.IsAlive);
			}	
		}
		
		public void wait(int timeout)
		{
			if (isActive)
			{
				thread.Join(timeout);
			}
		}
		
		public string msg 
		{ 
			get { return this.respenv.msg; } 
			set { this.respenv.msg = value;}
		}
		
		public string status 
		{ 
			get { return this.respenv.status; } 
			set { this.respenv.status = value;}
		}
			
		//
		// ResponseData Class
		//
		protected ResponseEnvelope respenv = new ResponseEnvelope();

		protected class ResponseEnvelope
		{	
			public string ContentType="text/text";
			
			public string status="EXECUTING";
			public string msg="";
			public string data = null;
			
			// Input data set:
			public DataSet ds;
			
			// Output data:
			public StringBuilder sb = new StringBuilder();
			
			public void clear()
			{
				sb = new StringBuilder();
			}
			
			public void jsonHeader()
			{
				sb.Append("{\n");
				sb.Append("  \"status\" : \"" + status +"\",\n"); 
				sb.Append("  \"msg\" : \"" + msg + "\",\n"); 
				sb.Append("  \"data\" : \n  ");
			}
			
			public void jsonTrailer()
			{
				sb.Append("}");
			}
			
			public void format(string fmt)
			{
				switch (fmt)
				{
					case "json":
						ContentType = "text/json";
						clear();
						jsonHeader();
						Utilities.Transform.DataSetToJson(ds, sb); 
					    jsonTrailer();
						break;
					case "extjs":
						ContentType = "text/json";
						clear();
						jsonHeader();
						Utilities.Transform.DataSetToExtjs(ds, sb); 
						jsonTrailer();
						break;
					case "csv":
						ContentType = "text/csv";
					    clear();
						Utilities.Transform.DataSetToCsv(ds, sb); 
						break;					
					case "xml":
						ContentType = "text/xml";
					    clear();
						Utilities.Transform.DataSetToXml(ds, sb); 
						break;
					default:
						ContentType = "text/xml";
					    clear();
						Utilities.Transform.DataSetToXml(ds, sb); 
						break;
				}
			}
				
		}
		
		//
		// Constructor
		//
		public ServiceResponse ()
		{	
		}
				
		public void load(DataSet ds)
		{
			load(null, ds, true);
		}
		
		public void load(DataSet ds, Boolean complete)
		{
			load(null, ds, complete);
		}
		
		protected void load(String s, DataSet ds, Boolean complete)
		{
			respenv.ds = ds;
			respenv.status = (complete ? "COMPLETE" : "EXECUTING");
		}
		
		//
		// Write the ResponseData Object 
		//		
		public void write(ServiceRequest request, System.Web.HttpResponse httpResponse)
		{
			//
			// BEGIN LOCK (respenv):
			//
			// This is to ensure that 1 Thread at a time is accessing/manipulating the Response Envelope Object
			// This Object is saved/loaded into web cache and can have multiple request threads accessing it simultaneously,
			// We need to ensure only one thread modifies the envelope at a time.
			//
			lock(respenv)
			{	
				// TODO: Filter DataSet:
				// respenv.filter(request.filters)
				
				// TODO: Sort DataSet
				// respenv.sort(request.sort)
				
				// TODO: Page DataSet
				// respenv.slicedice(request.page)
								
				// Format DataSet: 
				// to 'csv', 'json', 'xml', 'extjs'
				respenv.format(request.format);
				
				//////////////////////////////////////////////////////
				// Write the ResponseEnvelope object back out to the client
				//////////////////////////////////////////////////////
				if (request != null && httpResponse != null)
				{
					if (respenv.sb != null && respenv.sb.Length > 0)	    
					{
						httpResponse.ContentType = respenv.ContentType;
						httpResponse.Write(respenv.sb.ToString());	
					}
					
		            httpResponse.Flush();
		            httpResponse.Close();
				}	
			} // END LOCK (respenv)	
			
			Console.WriteLine("write: done.");

		}
	}
}

