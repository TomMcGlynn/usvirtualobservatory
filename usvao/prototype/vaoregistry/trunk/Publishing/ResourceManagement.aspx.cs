using System;
using System.Collections.Generic;
using System.Collections;
using System.Web;
using System.Data;
using System.Data.SqlClient;
using System.Web.Services;
using System.Xml;

namespace Publishing
{
    public partial class PublishingResourceManagement : System.Web.UI.Page
    {

        public static string getResourceStatement = @"select top 1 xml from Resource where Identifier=@Identifier and ([@status] = 1 or [@status] = 0 or [@status] = 3 ) and ukey=@ukey order by [@updated] desc";
        private static string sConnect = registry.Properties.Settings.Default.SqlConnection;
 
        protected void Page_Load(object sender, EventArgs e)
        {
            #region Get Parameters
            System.Collections.Specialized.NameValueCollection input = Request.QueryString;
            if (Request.RequestType == "POST")
                input = Request.Form;
            #endregion

            string action = input["action"];
            if (action == null || action == string.Empty)
                return;

            action = action.ToUpper();
            validationStatus status = null;
            if (action == "DELETERESOURCE")
            {
                if (input["identifier"] != null)
                    status = SetResourceStatus(input["identifier"], "deleted");
            }
            else if (action == "DEACTIVATERESOURCE")
            {
                if (input["identifier"] != null)
                    status = SetResourceStatus(input["identifier"], "inactive");
            }
            else if (action == "ACTIVATERESOURCE")
            {
                if (input["identifier"] != null)
                    status = SetResourceStatus(input["identifier"], "active");
            }
            else
            {
                ReturnFailure(new string[] { "Bad argument." });
                return;
            }

            if (status != null && status.IsValid)
                ReturnSuccess();
            else if (status != null)
                ReturnFailure(status.GetErrors());
            else
                ReturnFailure(new string[] { "Bad identifier argument." });
        }

        private validationStatus SetResourceStatus(string identifier, string resourceStatus)
        {
            validationStatus status = new validationStatus();

            string username = string.Empty;
            long ukey = 0;
            if (Session["username"] != null)
                username = (string)Session["username"];
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            if (username != null && username != string.Empty && ukey > 0 && UserManagement.UserExists(username))
            {
                try
                {
                    SqlConnection conn = new SqlConnection(sConnect);
                    conn.Open();
                    SqlCommand cmd = conn.CreateCommand();
                    cmd.CommandText = getResourceStatement;
                    cmd.Parameters.Add("@Identifier", SqlDbType.VarChar, 500);
                    cmd.Parameters.Add("@ukey", SqlDbType.BigInt);                 
                    cmd.Prepare();  // Calling Prepare after having setup commandtext and params.
                    cmd.Parameters["@Identifier"].Value = identifier;
                    cmd.Parameters["@ukey"].Value = ukey;

                    SqlDataAdapter sqlDA = new SqlDataAdapter(cmd);
                    DataSet ds = new DataSet();
                    sqlDA.Fill(ds);
                    int ncount = ds.Tables[0].Rows.Count;
                    if (ncount < 1)
                    {
                        status.MarkInvalid("Could not find resource " + identifier + " belonging to current user. Login may have timed out.");
                    }
                    else
                    {
                        string xml = (string)ds.Tables[0].Rows[0][0];
                        XmlDocument doc = new XmlDocument();
                        doc.LoadXml(xml);
                        XmlNodeList elemList = doc.GetElementsByTagName("ri:Resource");
                        if (elemList.Count != 1)
                        {
                            status.MarkInvalid("Errors in resource " + identifier + " belonging to current user. Cannot edit xml.");
                        }
                        else
                        {
                            elemList[0].Attributes["status"].InnerText = resourceStatus;
                            status = ResourceManagement.PublishXmlResource(doc, false, identifier, ukey);
                        }  
                    }                   
                }
                catch (Exception e)
                {
                    status.MarkInvalid("Error changing status of resource " + identifier + " to " + resourceStatus + ": " + e.Message);
                }
            }
            else
                status.MarkInvalid("Invalid login data. Session may have timed out.");

            return status;
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


   