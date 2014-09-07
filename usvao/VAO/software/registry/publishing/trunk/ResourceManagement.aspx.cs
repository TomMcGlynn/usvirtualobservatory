using System;
using System.Collections.Generic;
using System.Collections;
using System.Web;
using System.Data;
using System.Data.SqlClient;
using System.Web.Services;
using System.Xml;

using OperationsManagement;

namespace Publishing
{
    public partial class PublishingResourceManagement : System.Web.UI.Page
    {
        private static string appDir = System.Web.Hosting.HostingEnvironment.ApplicationPhysicalPath;
        public static string getResourceStatement = @"select top 1 xml from Resource where ivoid=@Identifier and ([rstat] = 1 or [rstat] = 0 or [rstat] = 3 ) and ukey=@ukey order by [updated] desc";
        private static string sConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlConnection"];
        private static string sAdminConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"];

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
                {
                    if (input["pending"] != null && input["pending"] == "true" )
                    {
                        status = MarkDeletedPendingResource(input["identifier"]);
                    }
                    else
                        status = SetResourceStatus(input["identifier"], "deleted");
                }
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

        private validationStatus SetResourceStatus(string identifier, string resourceStatus, bool isPending = false)
        {
            validationStatus status = new validationStatus();

            string username = string.Empty;
            long ukey = 0;
            string[] userAuths = new string[] {};
            if (Session["username"] != null)
                username = (string)Session["username"];
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            if (username != null && username != string.Empty && ukey > 0 && UserManagement.GetUserKey(username) == ukey)
            {
                try
                {
                    string xml = string.Empty;
                    if (isPending)
                    {
                        xml = GetResourceInfo.SelectPendingResource(ukey, identifier);
                    }
                    else
                    {
                        SqlConnection conn = new SqlConnection(sConnect);
                        conn.Open();

                        
                        //don't let user delete anyone's default auth
                        if (resourceStatus == "deleted" || resourceStatus == "inactive")
                        {
                            string checkDefaults = "select count(*) from users where defaultAuthorityID = '" + identifier + "'";
                            SqlDataAdapter sqlDA = new SqlDataAdapter(checkDefaults, conn);
                            DataSet ds = new DataSet();
                            sqlDA.Fill(ds);
                            if (ds.Tables[0].Rows.Count > 0 && (int)ds.Tables[0].Rows[0][0] > 0)
                            {
                                status.MarkInvalid("Resource " + identifier + " cannot be deleted because it is the default authority ID of at least one active user." +
                                    " Please contact helpdesk@usvao.org if you believe related user accounts should be purged or migrated to another authority.");
                            }
                        }

                        if (status.IsValid)
                        {
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
                                xml = (string)ds.Tables[0].Rows[0][0];
                            }
                        }
                    }

                    if (status.IsValid)
                    {
                        status = ResourceManagement.SetXmlResourceStatus(identifier, resourceStatus, ukey);
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

        private validationStatus MarkDeletedPendingResource(string identifier)
        {
            validationStatus status = new validationStatus();
            SqlConnection conn = new SqlConnection(sAdminConnect);
            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];
            else
                status.MarkInvalid("Not logged in.");

            if (status.IsValid)
            {
                try
                {
                    conn.Open();

                    SqlCommand ins = conn.CreateCommand();
                    string updateCommand = "UPDATE PendingResources set rstat=3 where rstat = 1 and ivoid = '" + identifier + "' and ukey = " + ukey + ";";
                    ins.CommandText = updateCommand;
                    ins.Prepare();
                    ins.ExecuteNonQuery();
                }
                catch (Exception ex)
                {
                    status.MarkInvalid(ex.Message);
                }
                finally
                {
                    conn.Close();
                }
            }
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


   