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
	/// Summary description for ListColumns.
	///ID:		$Id: ListColumns.aspx.cs,v 1.1.1.1 2005/05/05 15:16:58 grgreene Exp $
	///Revision:	$Revision: 1.1.1.1 $
	///Date:	$Date: 2005/05/05 15:16:58 $
	/// </summary>
	public class ListColumns : System.Web.UI.Page
	{
		public string[] keys=null;
		private void Page_Load(object sender, System.EventArgs e)
		{
			if (null == keys) 
			{
				RegistryAdmin ra = new RegistryAdmin();
				keys = ra.ListDBColumns();
	
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
/* Log of changes
 * $Log: ListColumns.aspx.cs,v $
 * Revision 1.1.1.1  2005/05/05 15:16:58  grgreene
 * import
 *
 * Revision 1.2  2004/03/19 17:30:06  womullan
 * updated search page for and/or search and column listing
 *
 * */