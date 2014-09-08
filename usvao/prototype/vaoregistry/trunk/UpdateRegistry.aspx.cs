using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Web;
using System.Web.SessionState;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.HtmlControls;
using registry;


namespace registry
{
	/// <summary>
	/// Summary description for UpdateRegistry.
	/// </summary>
	public class UpdateRegistry : System.Web.UI.Page
	{
		protected System.Web.UI.WebControls.TextBox SearchIdentifier;
		protected System.Web.UI.WebControls.Button btnSubmit;
	
		protected DBResource sres;
		protected string ro = "";
		protected bool InsertMode = false;
        

		private void Page_Load(object sender, System.EventArgs e)
		{
			// Put user code to initialize the page here
			string id = Request.Params["SearchIdentifier"];
			if (Request.Params["ro"] != null) ro = " readonly ";
			if (Request.Params["InsertMode"] != null) InsertMode = true;
			if (InsertMode) 
			{
				String resourceType = Request.Params["ResourceType"];
				switch (resourceType) 
				{
					case "CONE": sres = new ServiceCone(); break;
					case "SKYNODE": sres = new ServiceSkyNode(); break;
					case "SIAP": sres = new ServiceSimpleImageAccess(); break;
				}
				if (sres==null) sres = new DBResource();
                
				//sres.ResourceType=resourceType;
			}
			if (id != null) 
			{
				SearchIdentifier.Text=id;
				btnSubmit_Click(null,null);
			}
		}

		#region Web Form Designer generated code
		override protected void OnInit(EventArgs e)
		{
			//
			// CODEGEN: This call is required by the ASP.NET Web Form Designer.
			//
			InitializeComponent();
			base.OnInit(e);
		}
		
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{    
			this.btnSubmit.Click += new System.EventHandler(this.btnSubmit_Click);
			this.Load += new System.EventHandler(this.Page_Load);

		}
		#endregion

		private void btnSubmit_Click(object sender, System.EventArgs e)
		{
			Registry reg = new Registry();
			DBResource[] sra = reg.QueryResource("Identifier = '" + SearchIdentifier.Text + "'");
			
			if (sra == null || sra.Length==0) 
			{
				Response.Write("<p class=\"Warn\"> RESOURCE NOT FOUND!" + SearchIdentifier.Text+ "</p>");
			}
			else 
			{
				sres = sra[0];
			}
		}
	}
}
