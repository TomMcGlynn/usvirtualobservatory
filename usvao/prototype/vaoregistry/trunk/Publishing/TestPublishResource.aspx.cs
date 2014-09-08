using System;
using System.Collections.Generic;
using System.Web;
using System.Web.SessionState;
using System.Data;
using System.Data.SqlClient;
using registry;

namespace Publishing
{
    public partial class TestPublishResource : System.Web.UI.Page
    {
        private static string sConnect = registry.Properties.Settings.Default.SqlAdminConnection;
        public static VOR_XML vorXML = new VOR_XML();

        //todo: integrate this with validationLevel, xmlform, all other ways to ingest a resource.
        protected void Page_Load(object sender, EventArgs e)
        {

            #region get login information?
            string username = string.Empty;
            if (Session["username"] != null)
                username = (string)Session["username"];
            long ukey = 0;
            if( Session["ukey"] != null )
                ukey = (long)Session["ukey"];
            string[] userAuths = null;
            if (Session["userAuths"] != null)
                userAuths = (string[])Session["userAuths"];
            #endregion

            if (username == string.Empty || ukey <= 0)
                ReturnFailure(new string[] { "Invalid login information. Session may have timed out."});
            else
            {

                #region read the DOM resource sent back
                System.IO.StreamReader reader = new System.IO.StreamReader(Request.InputStream);
                string text = reader.ReadToEnd();
                string DOM = Uri.UnescapeDataString(text);
                DOM = DOM.Substring(DOM.IndexOf('<')).Substring(0, DOM.LastIndexOf("esource>") + 4); //resource or Resource, with or without ri:
                #endregion

                #region clean up empty sections, etc, and publish as if locally harvested
                validationStatus vstatus = ResourceManagement.CleanupAndVerifyData(ref DOM, ref userAuths); 

                if (vstatus.IsValid)
                {
                    System.Text.StringBuilder sb = new System.Text.StringBuilder();
                    int status = vorXML.LoadVORXML(DOM, ukey, String.Empty, sb);
                    if (status != 0)
                        ReturnFailure(new string[1] { "Error publishing resource: " + sb.ToString().Replace('\'', ' ') });
                    else
                        ReturnSuccess();
                }
                else
                    ReturnFailure(vstatus.GetErrors());
                #endregion
            }
        }

        private void ReturnSuccess()
        {
            Response.Write("{ 'success': true}");
            Response.Flush();
        }

        private void ReturnFailure(string[] errors)
        {
            if (errors != null && errors.Length > 0)
            {
                Response.Write("{ 'success': false, 'errors': { 'reason': '");
                foreach (string error in errors)
                {
                    Response.Write(error + ' ');
                }
                Response.Write("' }}");
            }
            else
                Response.Write("{ 'success': false, 'errors': { 'reason': 'Login failed. Try again.' }}");
            Response.Flush();
        }
     }
}