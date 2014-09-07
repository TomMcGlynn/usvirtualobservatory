using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Web;
using System.Web.Services;
using System.Data.SqlClient;
using System.Text;
using System.Configuration;

using registry;


namespace registry
{
	[WebService(Namespace="http://www.us-vo.org")]//

    public class RegistryDBQuery : System.Web.Services.WebService
	{
		public static string sConnect;
        internal static string PASS = (string)System.Configuration.ConfigurationManager.AppSettings["dbAdmin"];


		public RegistryDBQuery()
		{
			//CODEGEN: This call is required by the ASP.NET Web Services Designer
			InitializeComponent();
            try
            {
                if ((string)Properties.Settings.Default.SqlAdminConnection != null)
                    sConnect = (string)Properties.Settings.Default.SqlAdminConnection;
                else
                    sConnect = Properties.Settings.Default.SqlConnection;

                if (System.Configuration.ConfigurationManager.AppSettings["dbAdmin"] == null)
                    PASS = registry.Properties.Settings.Default.dbAdmin;

                Console.Out.WriteLine(" Database is "  + sConnect.Substring(0,sConnect.IndexOf("User")) + "\n");
            }
            catch (Exception) { }

			if (sConnect == null)
				throw new Exception ("Registry: SqlConnection not found in configuration settings");		
		}

		public RegistryDBQuery(string connectionString)
		{
			//CODEGEN: This call is required by the ASP.NET Web Services Designer
			InitializeComponent();
            try
            {
                sConnect = connectionString;
            }
            catch (Exception) { }

			if (sConnect == null)
				sConnect = connectionString;		
		}

		#region Component Designer generated code
		
		//Required by the Web Services Designer 
		private IContainer components = null;
				
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if(disposing && components != null)
			{
				components.Dispose();
			}
			base.Dispose(disposing);		
		}
		
		#endregion

		[WebMethod (Description="Input WHERE predicate for SQL Query. This queries the Resource table only.")]
		public DataSet DSQueryRegistry(string predicate)
		{
            string cmd = SQLHelper.createBasicResourceSelect(predicate);
            cmd = EnsureSafeSQLStatement(cmd);
            return DSQuery(cmd, PASS);
		}

		[WebMethod (Description="Input SQL Query. This can query the entire registry database. Requires passphrase.")]
		public DataSet DSQuery(string sqlStmnt, string password)
		{
			if (password.CompareTo(PASS) !=0) 
				throw new Exception("Invalid password"); 
			SqlConnection conn = null;
			DataSet ds = null;
			try
			{	
				conn = new SqlConnection (sConnect);
				conn.Open();
                sqlStmnt = SQLHelper.TranslateOldSchemaQuery(sqlStmnt);
				SqlDataAdapter sqlDA = new SqlDataAdapter(sqlStmnt,conn);
				ds= new DataSet();
				sqlDA.Fill(ds);
			}
			catch (Exception) 
			{
				throw new Exception("SQL is :"+sqlStmnt);
			}

			finally 
			{
				conn.Close();
			}

            return ds;
		}

        [WebMethod(Description = "retrieve capability and interface information for a given ACTIVE resource. Returns dataset custom-serialized")]
        public string DSQueryInterfaces(string identifier)
        {
            string cmd = SQLHelper.createInterfacesSelect(identifier);
            cmd = EnsureSafeSQLStatement(cmd);
            DataSet ds = DSQuery(cmd, PASS);

            System.IO.StringWriter sw = new System.IO.StringWriter();
            System.Xml.XmlTextWriter xw = new System.Xml.XmlTextWriter(sw);
            ds.WriteXml(xw, XmlWriteMode.IgnoreSchema);

            return sw.ToString();
        }

		/// <summary>
		/// (Description="searches registry for keyword")
		/// </summary>
		[WebMethod (Description="Searches registry for keyword.  To AND keywords, set andKeys to 'true', otherwise keywords will be OR'd.")]
		public DataSet DSKeywordSearch(string keywords, bool andKeys)
		{
			string q = SQLHelper.createKeyWordStatement(keywords,andKeys);
            q = EnsureSafeSQLStatement(q);
			return DSQueryRegistry(q);
		}

        //Clears the query string if it matches any common forms of malicious SQL.
        //I'm sure this could use improvement.
        private string EnsureSafeSQLStatement(string original)
        {
            string up = original.ToUpper();
            if (up.Contains("UPDATE ") || up.Contains("DELETE ") || up.Contains("EXEC("))
                return string.Empty;

            if (System.Text.RegularExpressions.Regex.IsMatch(up, "DROP\\s+TABLE"))
                return string.Empty;

            return original;
        }
	}
}

