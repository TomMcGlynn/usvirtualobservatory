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
	/// Summary description for Publish.
	/// </summary>
	public class ShowParams : System.Web.UI.Page
	{

		protected DataSet ds;
		protected int interfaceNum=0;
		public DBResource res = null;
		public InterfaceParam ip = null;

		private void Page_Load(object sender, System.EventArgs e)
		{
			RegistryAdmin reg = new RegistryAdmin();
			
			res = (DBResource)Session["res"];

			string str = Request.Params["interfaceNum"];
			if (str != null) 
			{
				interfaceNum = Convert.ToInt32(str);
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
			this.Load += new System.EventHandler(this.Page_Load);
		}
		#endregion
	}
}
