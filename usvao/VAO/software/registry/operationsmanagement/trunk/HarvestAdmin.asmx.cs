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
using OperationsManagement;

namespace Replicate
{
	/// <summary>
	/// Web Services for Administrating Registry
	///Current version
	///ID:		$Id: HarvestAdmin.asmx.cs,v 1.3 2005/12/19 18:08:57 grgreene Exp $
	///Revision:	$Revision: 1.3 $
	///Date:	$Date: 2005/12/19 18:08:57 $
	/// </summary>
	[WebService(Namespace="http://www.us-vo.org")]//

	//public class HarvestAdmin : Registry
    public class HarvestAdmin : System.Web.Services.WebService
	{
		public static string sConnect;
        internal static string PASS = (string)System.Configuration.ConfigurationManager.AppSettings["dbAdmin"];


		public HarvestAdmin()
		{
			//CODEGEN: This call is required by the ASP.NET Web Services Designer
			InitializeComponent();
            try
            {
                if ((string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"] != null)
                    sConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"];
                else
                    sConnect = registry.Properties.Settings.Default.SqlAdminConnection;

                if (System.Configuration.ConfigurationManager.AppSettings["dbAdmin"] == null)
                    PASS = registry.Properties.Settings.Default.dbAdmin;
            }
            catch (Exception) { }

			if (sConnect == null)
				throw new Exception ("Registry: SqlConnection not found in configuration settings");		
		}

		public HarvestAdmin(string connectionString)
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


        [WebMethod(Description = "Find the 'home'registry for resources (comma-delimited) by identifier.")]
        public DataSet FindHomeRegistry(string IVOA_ids)
        {
            DataSet ds = new DataSet();
            string[] ids = IVOA_ids.Split(',');
            string cmd = SQLHelper.createFindHomeRegistrySelect(ids);
            SqlConnection conn = new SqlConnection(sConnect);
            conn.Open();
            SqlDataAdapter sqlDA = new SqlDataAdapter(cmd, conn);
            sqlDA.Fill(ds);
            conn.Close();

            return ds;
        }

        [WebMethod(Description = "Find all of the active and deleted identifiers from a home RofR resource by its OAI endpoint. Use an empty string for new STScI-based resources or 'STScI/Migrate/VOR0.1toVOR1.0' for ones imported from pre- version 1.0 registries")]
        public DataSet FindResourcesByHomeRegistry(string OAI)
        {
            DataSet ds = new DataSet();
            string cmd = SQLHelper.createFindResourcesByHomeRegistrySelect(OAI);
            SqlConnection conn = new SqlConnection(sConnect);
            conn.Open();
            SqlDataAdapter sqlDA = new SqlDataAdapter(cmd, conn);
            sqlDA.Fill(ds);
            conn.Close();

            return ds;
        }

		[WebMethod (Description="Harvest OAI repository given URL from the given date. Records already existent in the registry will be updated. Requires passphrase.")]
		public string HarvestOAI(string url, string registryID, DateTime from, bool managed_only, string passphrase)
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
			
			ret += h.harvest(url, registryID, pars);		
			
			return ret;
		}

		[WebMethod (Description="Harvest Single OAI Record from a given URL, also provided the ivo identifier. Requires passphrase.")]
		public string HarvestRecord(string url, string registryID, string IVOA_id, string passphrase)
		{
			if (passphrase != PASS) return "You need the correct password";
			Harvester h = new Harvester();
			string ret = "HARVEST RECORD: " + IVOA_id;
			try 
			{
                if (!url.EndsWith("?"))
                    url += '?';
                url = url + "verb=GetRecord&identifier=" + IVOA_id;
                ret += " HARVEST " + h.harvest(url, registryID,
                   "&metadataPrefix=ivo_vor");
			}				
			catch (Exception ce) 
			{
				ret+= ce;
			}

			return ret;
		}

        [WebMethod(Description = "Harvest OAI Records (comma-delimited) from a given URL, also provided the ivo identifier. Requires passphrase.")]
        public string HarvestRecords(string url, string registryID, string IVOA_ids, string passphrase)
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
                    ret += " HARVEST " + h.harvest(baseUrl, registryID,
                       "&metadataPrefix=ivo_vor");
                }
                catch (Exception ce)
                {
                    ret += ce + "\n";
                }
            }

            return ret;
        }


        [WebMethod(Description = "Change record status (Some OAI interfaces do not have full support for status reporting, including deletion.) Allowed values for status are 'active', 'inactive', and 'deleted'. Requires passphrase.")]
        public string SetRecordStatus(string IVOA_id, string status, string passphrase)
        {
            if (passphrase != PASS) return "You need the correct password";

            validationStatus vstatus = ResourceManagement.SetXmlResourceStatus(IVOA_id, status.ToLower().Trim());
            if (vstatus.IsValid)
                return "Resource " + IVOA_id + " successfully marked '" + status + "'.";
            else
                return "Error(s) chaing resource " + IVOA_id + " status to '" + status +  "': " + vstatus.GetConcatenatedErrors(", ");
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
