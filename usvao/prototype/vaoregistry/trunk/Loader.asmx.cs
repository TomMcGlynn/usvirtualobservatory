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

namespace registry
{
	/// <summary>
	/// Summary description for Loader.
	/// </summary>

	public class Loader : System.Web.Services.WebService
	{
		public string sConnect;


		public static string insertStatement = @"INSERT INTO Resource VALUES(
													@Title,
													@Publisher,
													@Creator,
													@Subject,
													@Description,
													@Contributor,
												    @Date,
													@Version,
													@Identifier,
													@ResourceURL,
													@ServiceURL,
													@ContactName,
													@ContactEmail,
													@Type,
													@Coverage,
													@ContentLevel,
													@Facility,
													@Instrument,
													@Format,
													@ServiceType)";
		

		public Loader()
		{
			//CODEGEN: This call is required by the ASP.NET Web Services Designer
			InitializeComponent();
			sConnect = ConfigurationSettings.AppSettings["SqlConnection.String"];
			if (sConnect == null)
				throw new Exception ("Loader: SqlConnection.String not found in Web.config");		
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

		
		[WebMethod]
		public string Load(SimpleResource[] resources)
		{
			SqlConnection conn = null;
			StringBuilder sb = new StringBuilder();
			try
			{	
				conn = new SqlConnection (sConnect);
				conn.Open();
				SqlCommand sc = conn.CreateCommand();
				
				sc.CommandText = insertStatement;

				addParameters(sc);

				sc.Prepare() ;  // Calling Prepare after having setup commandtext and params.

				// now go througt the resources and add them 

				for (int r=0 ; r < resources.Length; r++) 
				{
					try 
					{
						// add in the parameters for the prepared statement
						grabParameters(sc.Parameters,resources[r]);		
										
						sc.ExecuteNonQuery();
					} 
					catch (Exception e) 
					{
						sb.Append(" Rsource["+r+"]: "+e.Message);
					}
				}

				
			}
			finally 
			{
				conn.Close();
			}

			return "Hello World";
		}

		public void addParametersWithVals(SqlCommand sc, SimpleResource res) 
		{
			// add parameters to bind the inputs 
			sc.Parameters.Add ("@Title",SqlDbType.VarChar, 80).Value = res.Title;
			sc.Parameters.Add ("@Publisher",SqlDbType.VarChar, 80).Value = res.Publisher;
			sc.Parameters.Add ("@Creator",SqlDbType.VarChar, 80).Value = res.Creator;
			sc.Parameters.Add ("@Subject",SqlDbType.VarChar, 80).Value = res.Subject;
			sc.Parameters.Add ("@Description",SqlDbType.VarChar, 80).Value = res.Description;
			sc.Parameters.Add ("@Contributor",SqlDbType.VarChar, 80).Value = res.Contributor;
			sc.Parameters.Add ("@Date",SqlDbType.VarChar, 80).Value = res.Date;
			sc.Parameters.Add ("@Version",SqlDbType.VarChar, 80).Value = res.Version;
			sc.Parameters.Add ("@Identifier",SqlDbType.VarChar, 80).Value = res.Identifier;
			sc.Parameters.Add ("@ResourceURL",SqlDbType.VarChar, 80).Value = res.ReferenceURL;
			sc.Parameters.Add ("@ServiceURL",SqlDbType.VarChar, 80).Value = res.ServiceURL;
			sc.Parameters.Add ("@ContactName",SqlDbType.VarChar, 80).Value = res.Name;
			sc.Parameters.Add ("@ContactEmail",SqlDbType.VarChar, 80).Value = res.Email;
			sc.Parameters.Add ("@Type",SqlDbType.VarChar, 80).Value = res.ResourceType;
			sc.Parameters.Add ("@Coverage",SqlDbType.VarChar, 80).Value = res.Coverage;
			sc.Parameters.Add ("@ContentLevel",SqlDbType.VarChar, 80).Value = res.ContentLevel;
			sc.Parameters.Add ("@Facility",SqlDbType.VarChar, 80).Value = res.Facility;
			sc.Parameters.Add ("@Instrument",SqlDbType.VarChar, 80).Value = res.Instrument;
			sc.Parameters.Add ("@Format",SqlDbType.VarChar, 80).Value = res.Format;
			sc.Parameters.Add ("@ServiceType",SqlDbType.VarChar, 80).Value = res.ServiceType;
		}

		public void addParameters(SqlCommand sc) 
		{
			// add parameters to bind the inputs 
			sc.Parameters.Add ("@Title",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Publisher",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Creator",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Subject",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Description",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Contributor",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Date",SqlDbType.DateTime);
			sc.Parameters.Add ("@Version",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Identifier",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@ResourceURL",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@ServiceURL",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@ContactName",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@ContactEmail",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Type",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Coverage",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@ContentLevel",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Facility",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Instrument",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@Format",SqlDbType.VarChar, 80);
			sc.Parameters.Add ("@ServiceType",SqlDbType.VarChar, 80);
		}
		public void grabParameters(SqlParameterCollection pars, SimpleResource res) 
		{
			int ind = 0;
			pars[ind++].Value = res.Title;//	@Title,
			pars[ind++].Value = res.Publisher;//	@Publisher,
			pars[ind++].Value = res.Creator;//		@Creator,
			pars[ind++].Value = String.Join(",",res.Subject);//		@Subject,
			pars[ind++].Value = res.Description;//		@Description,
			pars[ind++].Value = res.Contributor;//		@Contributor,
			pars[ind++].Value = res.Date;//		@Date,
			pars[ind++].Value = res.Version;//		@Version,
			pars[ind++].Value = res.Identifier;//		@Identifier,
			pars[ind++].Value = res.ReferenceURL;//		@ResourceURL,
			pars[ind++].Value = res.ServiceURL;//		@ServiceURL,
			pars[ind++].Value = res.Name;//		@ContactName,
			pars[ind++].Value = res.Email;//		@ContactEmail,
			pars[ind++].Value = res.ResourceType;//		@Type,
			pars[ind++].Value = res.Coverage;//		@Coverage,
			pars[ind++].Value = res.ContentLevel;//		@ContentLevel,
			pars[ind++].Value = res.Facility;//		@Facility,
			pars[ind++].Value = res.Instrument;//		@Instrument,
			pars[ind++].Value = res.Format;//		@Format,
			pars[ind++].Value = res.ServiceType;//		@ServiceType


		}

		/// <summary>
		/// horible method with all parts exposed
		/// </summary>
		[WebMethod]
		public string LoadSimple(string Title ,
			string Publisher,
			string Creator,
			string Subject,
			string Description,
			string Contributor,
			string  Version,
			string Identifier,
			string ReferenceURL,
			string ServiceURL,
			string Name,
			string Email,
			string ResourceType,
			string Coverage,
			string ContentLevel,
			string Facility,
			string Instrument,
			string Format,
			string ServiceType)
						
		{
			SimpleResource res = new SimpleResource();
			res.Title = 		Title; 
			res.Publisher	=	Publisher;
			res.Creator		=	Creator;
			res.Subject		= new string[1];
			res.Subject[0]	=	Subject;
			res.Description	=	Description;
			res.Contributor	=	Contributor;
			res.Date		=	DateTime.Now;
			res.Version		=	Version;
			res.Identifier	=	Identifier;
			res.ReferenceURL=	ReferenceURL;
			res.ServiceURL	=	ServiceURL;
			res.Name		=	Name;
			res.Email		=	Email;
			res.ResourceType=	ResourceType;
			res.Coverage	=	Coverage;
			res.ContentLevel=	ContentLevel;
			res.Facility	=	Facility;
			res.Instrument	=	Instrument;
			res.Format		=	Format;
			res.ServiceType	=	ServiceType;

			SqlConnection conn = null;
			StringBuilder sb = new StringBuilder();
			int nrows = 0;
			try
			{	
				conn = new SqlConnection (sConnect);
				conn.Open();
				SqlCommand sc = conn.CreateCommand();
				
				sc.CommandText = insertStatement;
				// add in the parameters for the prepared statement
				addParameters(sc);

				sc.Prepare() ;  // Calling Prepare after having setup commandtext and params.

				// now go througt the resources and add them 

				grabParameters(sc.Parameters,res);
				nrows = sc.ExecuteNonQuery();
				
			}
			catch (Exception e) 
			{
				return e.Message+ " "+ e.StackTrace;
			}
			finally 
			{
				conn.Close();
			}

			return nrows + " rows insterted";
		}

		/// <summary>
		/// just try to get this working 
		/// </summary>
		[WebMethod]
		public string LoadDummy(string Title)
		{

			SqlConnection conn = null;
			StringBuilder sb = new StringBuilder();
			int nrows = 0;
			try
			{	
				conn = new SqlConnection (sConnect);
				conn.Open();
				SqlCommand sc = conn.CreateCommand();
				
		//		sc.CommandText = @"INSERT INTO Resource (Title) VALUES (@Title)";

				sc.CommandText = insertStatement;
												
				// add in the parameters for the prepared statement
				//sc.Parameters.Add("@Title",SqlDbType.VarChar,40);

				addParameters(sc);

				sc.Prepare() ;  // Calling Prepare after having setup commandtext and params.

				sc.Parameters["@Title"].Value = Title;
				SqlParameterCollection pars = sc.Parameters;
				int ind = 1;
				pars[ind++].Value = "Pub";//	@Publisher,
				pars[ind++].Value = "Creator";//		@Creator,
				pars[ind++].Value = "Subject";//		@Subject,
				pars[ind++].Value = "Description";//		@Description,
				pars[ind++].Value = "Contributor";//		@Contributor,
				pars[ind++].Value = DateTime.Now;//		@Date,
				pars[ind++].Value = "Version";//		@Version,
				pars[ind++].Value = "Identifier";//		@Identifier,
				pars[ind++].Value = "ReferenceURL";//		@ResourceURL,
				pars[ind++].Value = "ServiceURL";//		@ServiceURL,
				pars[ind++].Value = "Name";//		@ContactName,
				pars[ind++].Value = "Email";//		@ContactEmail,
				pars[ind++].Value = "ResourceType";//		@Type,
				pars[ind++].Value = "Coverage";//		@Coverage,
				pars[ind++].Value = "ContentLevel";//		@ContentLevel,
				pars[ind++].Value = "Facility";//		@Facility,
				pars[ind++].Value = "Instrument";//		@Instrument,
				pars[ind++].Value = "Format";//		@Format,
				pars[ind++].Value = "Documentation";//		@ServiceType

				nrows = sc.ExecuteNonQuery();
				
			}
			catch (Exception e) 
			{
				return e.Message+ " "+ e.StackTrace;
			}
			finally 
			{
				conn.Close();
			}

			return nrows + " rows insterted";
		}
	}
}
