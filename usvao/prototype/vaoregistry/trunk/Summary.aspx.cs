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

namespace registry
{
	/// <summary>
	/// Summary description for Summary.
	/// </summary>
	public class Summary : System.Web.UI.Page
	{

		protected System.Web.UI.WebControls.DataGrid DataGrid1;

		protected DataSet ds;
		protected DataSet ds2;

		private void Page_Load(object sender, System.EventArgs e)
		{

			RegistryAdmin reg = new RegistryAdmin();

			ds = reg.DSQuery("select rt.resourcetype, count(*), rt.description from resource r, resourcetype rt where r.status=1 and r.resourcetype=rt.resourcetype group by rt.resourcetype, rt.description order by rt.resourcetype",
				RegistryAdmin.PASS);

			ds2 = reg.DSQuery("select count(*) from resource where status=1",RegistryAdmin.PASS);

//			DataGrid1.DataSource = ds.Tables[0];
//			DataGrid1.DataBind();

			// Put user code to initialize the page here
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
			this.Load += new System.EventHandler(this.Page_Load);
		}
		#endregion
	}
}
