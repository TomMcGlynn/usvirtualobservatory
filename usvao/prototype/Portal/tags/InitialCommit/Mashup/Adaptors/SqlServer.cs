using System;
using System.Data;
using System.Web;
using System.Xml;
using System.Text;
using System.Threading;

namespace Mashup.Adaptors
{
	[Serializable]
	public class SqlServer : IAsyncAdaptor
	{
	    public String db {get; set;}
		public String sql {get; set;}
		public String email {get;set;}
		public String direct {get; set;}
		
	    public SqlServer() 
	    {
	        db = "";
			sql = "";
			email = "false";
			direct = "false";
	    }
	
		//
		// IAdaptor::invoke()
		//
	    public void invoke(ServiceRequest request, ServiceResponse response)
	    {		
			//Console.WriteLine("SqlServer.invoke() Sleeping start (20 seconds)...");
			//Thread.Sleep(20*1000);
			//Console.WriteLine("SqlServer.invoke() Sleep() end.");
			
			//
			// Replace every [PARAM] in the QUERY string with it's equivalent ServiceRequest Param
			//
			string sSql = Utilities.ParamString.replaceAllParams(sql, request.paramss);		
			
			//
			// Run the New Query on the Database to extract resulting DataSet
			//		
			DataSet ds = new Utilities.Database(db, 
			                                    sSql, 
			                                    Convert.ToBoolean(email), 
			                                    Convert.ToBoolean(direct)).getDataSet();
			//
			// Load the response data
			//
			response.load(ds);
		}
	}
}