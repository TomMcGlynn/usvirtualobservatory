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
	/// Summary description for QueryRegistry.
	/// </summary>
	public class SearchRegistry : System.Web.UI.Page
	{
		
		protected System.Web.UI.WebControls.Button Button1;
		protected System.Web.UI.HtmlControls.HtmlTextArea sql;
		protected System.Web.UI.WebControls.DropDownList ddSQLList;
		protected System.Web.UI.WebControls.RadioButtonList rblANDOR;

		protected bool advanced=false;
		public string keywords = "";
		public int endRes = 10;
		public int startRes = 0;
		public int prevRes= 0;
		public int maxLen = 20;
		public int totalRes = 0;
		public string predicate = null;
		public string ddSQLSelectVal
		{
			get 
			{
				return ddSQLList.SelectedItem.Value.ToString();
			}
		}


		protected DataSet ds;
		protected DataSet groupDs;

        protected string keywordCmd = null;
        protected string URLkeywordResults = "http://localhost/VOR/registry.asmx?op=QueryVORVot";

		private void Page_Load(object sender, System.EventArgs e)
		{
			string advancedstr = Request.Params["advanced"];
			advanced = advancedstr!=null;

			string startResStr = Request.Params["startRes"];
			if (null != startResStr)
			{
				try
				{
					startRes = Convert.ToInt32(startResStr);
				}
				catch(Exception ee)
				{
					ee=ee;
					startRes=-1;
				}


			}
			if (startRes >= 0)
			{
				ds=(DataSet)Session["ds"];
				groupDs=(DataSet)Session["groupDs"];
				SetPage();	
			}
			else
			{
				keywords = Request.Params["keywords"];
				

				if ((keywords!=null) && (keywords!=""))
				{
					keywordSearch();
				}

				string grpFlag = Request.Params["groupFlag"];
				string sqlstr = Request.Params["sql"];
				
				if ( (sqlstr != null) && (grpFlag==null) )
				{
					if ( (sqlstr.IndexOf(",") >=0 ) && (sqlstr.IndexOf("contains")< 0) ) 
					{
						String[] sqlstrs = sqlstr.Split(',');
						sqlstr = sqlstrs[sqlstrs.Length-1];
					}
					predicate = sqlstr;	
					sql.Value = sqlstr;
					doQuery();
					
				}
		

			}			
		}

		private void SetPage()
		{
			if (null != ds) 
			{
				Session["ds"]=ds;
				Session["groupDs"]=groupDs;

				totalRes = ds.Tables[0].Rows.Count;
				if ((startRes + maxLen) <= totalRes)
				{
					endRes = startRes + maxLen;
				}
				else 
				{
					endRes = totalRes; 					
				}
				if (startRes - maxLen < 0) 
				{
					prevRes=0;
				}
				else
				{
					prevRes = startRes -maxLen;
				}

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
			this.Button1.Click += new System.EventHandler(this.Button1_Click);
			this.ddSQLList.SelectedIndexChanged += new System.EventHandler(this.ddSQLList_SelectedIndexChanged);

			this.Load += new System.EventHandler(this.Page_Load);

		}
		#endregion


		private void Button1_Click(object sender, System.EventArgs e)
		{
			if (sender !=null || predicate==null) 	predicate = sql.Value;
			doQuery();
		
		}


		private void doQuery()
		{
			RegistryAdmin reg = new RegistryAdmin();
			ds = reg.DSQueryRegistry(predicate);
			groupDs = doGroups(reg,predicate);
			SetPage();
			startRes=0;
		}


		private void keywordSearch()
		{
			RegistryAdmin reg = new RegistryAdmin();
			bool andKeys = rblANDOR.SelectedIndex ==0; // boolean test
			predicate = SQLHelper.createKeyWordStatement(keywords,andKeys);
            keywordCmd = reg.XMLQueryRegistry(predicate);

//			ds = reg.DSKeywordSearch(keywords, andKeys);
//            string resultsXML = reg.QueryXMLVOResource(predicate);
// NEED to take these resultsXML,  put through registry stylesheet to convert to VOTable

//			groupDs = doGroups(reg,predicate);
//			startRes=0;
//			SetPage();
		}
         
		private void ddSQLList_SelectedIndexChanged(object sender, System.EventArgs e)
		{
			if (ddSQLList.SelectedIndex > 0) 
			{
				sql.Value = ddSQLList.SelectedItem.Value;
				predicate = ddSQLList.SelectedItem.Value;
				doQuery();
//				Response.Redirect(Request.Url+"&sql="+predicate);
			}
		}

		protected DataSet doGroups(RegistryAdmin reg, string predicate) 
		{
			string mpredicate = predicate.ToUpper().Replace("RESOURCETYPE"," r.resourceType");
			mpredicate = mpredicate.Replace("CONTAINS (*,","CONTAINS (r.*,");
			return  reg.DSQuery("select rt.resourcetype, count(r.identifier), rt.description from resource r, resourcetype rt where r.status=1 and r.resourcetype=rt.resourcetype  and "+mpredicate+" group by rt.resourcetype, rt.description order by rt.resourcetype", RegistryAdmin.PASS);
		}
	

	}
}
