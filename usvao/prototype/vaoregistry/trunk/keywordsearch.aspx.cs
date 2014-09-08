using System;
using System.Data;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.IO;
using System.Text;

using System.Xml.XPath;
using System.Xml.Xsl;


    public partial class keywordsearch : System.Web.UI.Page
    {
        private static string baseURL = registry.Properties.Settings.Default.baseURL;

        protected void Page_Load(object sender, EventArgs e)
        {
            try
            {
                string method = Request.HttpMethod;

                string sources = String.Empty;
                string sourcesURL = String.Empty;
                string RunID = String.Empty;
                string referralURL = String.Empty;
                string benchID = String.Empty;

                if (method == "GET")
                {
                    sources = Request.QueryString["sources"];
                    sourcesURL = Request.QueryString["sourcesURL"];
                    RunID = Request.QueryString["RunID"];
                    referralURL = Request.QueryString["referralURL"];
                    benchID = Request.QueryString["benchID"];
                }
                else if (method == "POST")
                {
                    sources = Request.Form["sources"];
                    sourcesURL = Request.Form["sourcesURL"];
                    RunID = Request.Form["RunID"];
                    referralURL = Request.Form["referralURL"];
                    benchID = Request.Form["benchID"];
                }

                //Fill in the interop form with these values that will have otherwise disappeared by the time 
                //client-side javascript exists
                //if (!this.IsStartupScriptRegistered("Startup"))
                ClientScriptManager cm = Page.ClientScript;
                if( ! cm.IsStartupScriptRegistered("startupScript"))
                {
                    String scriptString = "<script language=\"JavaScript\"> ";


                    if (sources != null)
                        scriptString += "document.getElementById(\"sources\").value = \"" + sources + "\";";
                    if (sourcesURL != null)
                        scriptString += "document.getElementById(\"sourcesURL\").value = \"" + sourcesURL + "\";";
                    if (benchID != null)
                        scriptString += "document.getElementById(\"benchID\").value = \"" + benchID + "\";";

                    if (referralURL != null)
                        scriptString += "document.getElementById(\"referralURL\").value = \"" + referralURL + "\";";
                    else
                        scriptString += "document.getElementById(\"referralURL\").value = \"" + baseURL + "\";";

                    if (RunID != null)
                        scriptString += "document.getElementById(\"RunID\").value = \"" + RunID + "\";";
                    else
                        scriptString += "document.getElementById(\"RunID\").value = \"" + "STScIRegistry" + DateTime.Now.ToFileTimeUtc() + "\";";

                    scriptString += "</script>";
                    //this.RegisterStartupScript("Startup", scriptString);
                    cm.RegisterStartupScript(this.GetType(), "startupScript", scriptString); 
                }
            }
            catch (Exception ex)
            {
                ex.ToString();
            }
        }
    }
