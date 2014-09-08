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
//using net.ivoa;
//using net.ivoa.vr0_10;

namespace registry
{
	/// <summary>
	/// Web Services for Administrating Registry
	///Current version
	///ID:		$Id: RegistryAdmin.asmx.cs,v 1.3 2005/12/19 18:08:57 grgreene Exp $
	///Revision:	$Revision: 1.3 $
	///Date:	$Date: 2005/12/19 18:08:57 $
	/// </summary>
	[WebService(Namespace="http://www.us-vo.org")]//

	//public class RegistryAdmin : Registry
    public class RegistryAdmin : System.Web.Services.WebService
	{
		public static string sConnect;
        internal static string PASS = Properties.Settings.Default.dbAdmin;


		public RegistryAdmin()
		{
			//CODEGEN: This call is required by the ASP.NET Web Services Designer
			InitializeComponent();
            try
            {
                if (Properties.Settings.Default.SqlAdminConnection != null)
                    sConnect = Properties.Settings.Default.SqlAdminConnection;
                else
                    sConnect = Properties.Settings.Default.SqlConnection; //try and do what we can without write access, to log issues

            }
            catch (Exception) { }

			if (sConnect == null)
				throw new Exception ("Registry: SqlConnection not found in configuration settings");		
		}

		public RegistryAdmin(string connectionString)
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
			string cmd = SQLHelper.createResourceSelect( predicate );
            cmd = EnsureSafeSQLStatement(cmd);
			return DSQuery(cmd, PASS);
		}

        /*[WebMethod(Description = "Input identifier to retrieve an inactive resource. This queries the Resource table only.")]
        public DataSet DSQueryInactiveResource(string identifier)
        {
            string cmd = SQLHelper.createInactiveResourceSelect(identifier);
            cmd = EnsureSafeSQLStatement(cmd);
            return DSQuery(cmd, PASS);
        }*/

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

		[WebMethod (Description="Harvest OAI repository given URL from the given date. Records already existent in the registry will be updated. Requires passphrase.")]
		public string HarvestOAI(string url,DateTime from, bool managed_only, string passphrase)
		{
			if (passphrase != PASS) return "You need the correct password";
			Harvester h = new Harvester();
			string ret = "";
			string pars = null;		
            bool ivo_managed = true;
            if (managed_only == false)
                ivo_managed = false;
				
			string upUrl = url.ToUpper();
			pars = "verb=ListRecords";
            if( ivo_managed )
                pars += "&set=ivo_managed";
            pars += "&metadataPrefix=ivo_vor&from=";
			//	oai dates .. 2004-04-22T21:09:50Z
            oai.granularityType gran = h.GetTimeGranularity(url);
            pars += STOAI.GetOAIDatestamp(from, gran);
			
			ret += h.harvest(url,pars);		
			
			return ret;
		}

		[WebMethod (Description="Harvest Single OAI Record from a given URL, also provided the ivo identifier. Requires passphrase.")]
		public string HarvestRecord(string url, string IVOA_id, string passphrase)
		{
			if (passphrase != PASS) return "You need the correct password";
			Harvester h = new Harvester();
			string ret = "HARVEST RECORD: " + IVOA_id;
			try 
			{
                if (!url.EndsWith("?"))
                    url += '?';
                url = url + "verb=GetRecord&identifier=" + IVOA_id;
                ret += " HARVEST " + h.harvest(url,
                   "&metadataPrefix=ivo_vor");
			}				
			catch (Exception ce) 
			{
				ret+= ce;
			}

			return ret;
		}

        [WebMethod(Description = "Harvest OAI Records (comma-delimited) from a given URL, also provided the ivo identifier. Requires passphrase.")]
        public string HarvestRecords(string url, string IVOA_ids, string passphrase)
        {
            string ret = String.Empty;

            if (passphrase != PASS) return "You need the correct password";
            Harvester h = new Harvester();

            string[] ids = IVOA_ids.Split(new char[] { ',' });
            foreach (string id in ids)
            {
                ret += " HARVEST RECORD: " + id + "\n";

                try
                {
                    string baseUrl = url;
                    if (!baseUrl.EndsWith("?"))
                        baseUrl += '?';
                    baseUrl = baseUrl + "verb=GetRecord&identifier=" + id;
                    ret += " HARVEST " + h.harvest(baseUrl,
                       "&metadataPrefix=ivo_vor");
                }
                catch (Exception ce)
                {
                    ret += ce + "\n";
                }
            }

            return ret;
        }

		[WebMethod (Description="Delete an individual record - returns the number of rows deleted. Requires passphrase.")]
		public string DeleteEntry(string identifier, string passPhrase)
		{
			SqlConnection conn = null;
			try
			{	
				conn = new SqlConnection (sConnect);
				conn.Open();

				SqlCommand sc = SQLHelper.getGetDeleteResourceCmd(conn);
				// add in the parameters for the prepared statement

				sc.Parameters["@Identifier"].Value	= identifier;
				sc.Parameters["@PassPhrase"].Value	= passPhrase;

				int nobjs = sc.ExecuteNonQuery();
				return "deleted "+nobjs;
			}
			catch (Exception e) 
			{
				return e.Message;
			}

			finally 
			{
				conn.Close();
			}
			
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

		/// <summary>
		/// Revision from CVS
		/// </summary>
		public  static string Revision
		{
			get
			{
				return "$Revision: 1.4 $";
			}
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

/** log of changes
 * 
 *  $Log: RegistryAdmin.asmx.cs,v $
 *  Revision 1.3  2005/12/19 18:08:57  grgreene
 *  validationLEvel can edit now
 *
 *  Revision 1.2  2005/05/27 18:53:47  grgreene
 *  fixed identifier trim
 *
 *  Revision 1.1.1.1  2005/05/05 15:17:02  grgreene
 *  import
 *
 *  Revision 1.31  2005/05/05 14:59:01  womullan
 *  adding oai files
 *
 *  Revision 1.30  2005/03/22 20:11:00  womullan
 *  update to parser for descrip + OAI fixes
 *
 *  Revision 1.29  2004/11/29 18:26:12  womullan
 *  ssap accessurl with SIAP
 *
 *  Revision 1.28  2004/11/24 19:36:09  womullan
 *   mouse over more space in table, pull down working
 *
 *  Revision 1.27  2004/11/24 16:25:14  womullan
 *  adv search front page
 *
 *  Revision 1.26  2004/11/12 01:42:40  womullan
 *   vizier works
 *
 *  Revision 1.25  2004/11/12 00:57:28  womullan
 *   small change to admin
 *
 *  Revision 1.24  2004/11/11 19:40:20  womullan
 *  minor updates
 *
 *  Revision 1.23  2004/11/10 19:10:48  womullan
 *  interfaces
 *
 *  Revision 1.22  2004/11/09 21:11:01  womullan
 *  added relation get
 *
 *  Revision 1.21  2004/11/08 20:20:35  womullan
 *  updated relationship insert
 *
 *  Revision 1.20  2004/11/05 18:45:28  womullan
 *  relations added
 *
 *  Revision 1.19  2004/11/02 20:12:53  womullan
 *  date fields fixed
 *
 *  Revision 1.18  2004/11/01 18:30:16  womullan
 *  v0.10 upgrade
 *
 *  Revision 1.17  2004/08/12 17:23:14  womullan
 *   added new cls for SkyNode PrimaryTabel PrimaryKey max records, fixed it on the forms also fixed repluicator to deal with new fiedls
 *
 *  Revision 1.16  2004/07/08 18:08:38  womullan
 *  skynode lat/lon added
 *
 *  Revision 1.15  2004/06/03 15:03:06  womullan
 *  fixed vizier harvest
 *
 *  Revision 1.14  2004/05/06 20:04:32  womullan
 *   ensure status set for failed vizier harvest - use in front page
 *
 *  Revision 1.13  2004/05/06 16:15:41  womullan
 *   some mods to main page - replication fixed
 *
 *  Revision 1.12  2004/04/23 01:37:32  womullan
 *   harvest dates on front page
 *
 *  Revision 1.11  2004/04/22 21:42:00  womullan
 *   from dates fixed  harvest works
 *
 *  Revision 1.10  2004/04/22 17:45:53  womullan
 *  harvesting added to replicate
 *
 *  Revision 1.9  2004/04/20 16:24:16  womullan
 *   added query merthod with pass to admin
 *
 *  Revision 1.8  2004/04/01 20:21:56  womullan
 *   footprint added
 *
 *  Revision 1.7  2004/04/01 18:29:28  womullan
 *   harvest ncsa
 *
 *  Revision 1.6  2004/04/01 17:24:29  womullan
 *   insert/update fixed
 *
 *  Revision 1.5  2004/03/31 17:28:26  womullan
 *  changes for new schema
 *
 *  Revision 1.4  2004/03/25 16:29:21  womullan
 *   contains for inverted index
 *
 *  Revision 1.3  2004/03/19 17:25:20  womullan
 *  updated search page for and/or search and column listing
 *
 *  Revision 1.2  2004/03/12 19:15:49  womullan
 *  added keyword search to form
 *
 *  Revision 1.1  2004/02/26 20:18:22  womullan
 *  adding registryadmin files
 *
 */

