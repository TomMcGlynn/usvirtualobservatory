using System;
using System.Configuration;
using System.ComponentModel;
using System.Globalization;
using System.Collections;
using System.Collections.Specialized;
using System.Data;
using System.Data.SqlClient;
using System.Text;
using System.IO;
using System.Web;
using System.Text.RegularExpressions;

using log4net;

namespace Utilities
{
	public class Database
	{
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }
		
		//
		// constructor so unique instance is created
		//
		private string dbString;
		private string sqlString;
		
		private bool direct = false;
		private int  maxrows = 10000;
		
		private DataSet ds = null;
		
		public Database(string dbString, string sqlString, bool direct) 
		{
			this.dbString = dbString;
			this.sqlString = sqlString;
			this.direct = direct;
		}

        #region getDataSet 		
        public DataSet getDataSet()
        {
            // Connect to the database, dispose() is called automatically below
            using (SqlConnection dbConn = new SqlConnection(dbString))
            {
                dbConn.Open();
				
                string sSqlCommand = null;
                //
                // Check if this is a direct query.  
                // If not, then wrap the sql inside spExecuteSQL() 
                //
                if (direct)
                {
                    sSqlCommand = sqlString;
                }
                else // Excapsulate query string in spExecuteSQL (for 'select' statements) or spExecuteSP (for stored procedures)
                {
					sSqlCommand = "spExecuteSQL '" + sqlString + "','" + maxrows + "'";
                }
				
				log.Info(tid + "     [SQL] " + sSqlCommand);
				
                // Issuse Query
                SqlCommand sqlcmd = new SqlCommand(sSqlCommand, dbConn);
                SqlDataAdapter sqlda = new SqlDataAdapter(sqlcmd);
				
                // Set Query Timeout
                sqlcmd.CommandTimeout = 900;
				
                // Return the result of a query as a dataset, i.e. ds
                ds = new DataSet();
				
                // Fill the dataset
                sqlda.Fill(ds);
				
            }// dbConn.dispose() called automatically

            return ds; // Return Dataset
        }
        #endregion
	}
}

