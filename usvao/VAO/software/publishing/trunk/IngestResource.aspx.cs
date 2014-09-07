using System;
using System.Collections.Generic;
using System.Web;
using System.Web.SessionState;
using System.Data;
using System.Data.SqlClient;
using System.Xml;
using System.IO;

using registry;
using OperationsManagement;

namespace Publishing
{
    public partial class IngestResource : System.Web.UI.Page
    {
        private static string sConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"];
        private static string appDir = System.Web.Hosting.HostingEnvironment.ApplicationPhysicalPath;

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
                validationStatus vstatus = new validationStatus();
                if( userAuths.Length > 0) //one record, not first auth: test it now
                    vstatus = ResourceManagement.CleanupAndVerifyData(ref DOM, ref userAuths);

                #region check draft parameters.
                string url = Request.Url.ToString();
                bool isDraft = url.Contains("saveAsDraft");
                bool isPending = url.Contains("pending");
                #endregion

                if (vstatus.IsValid)
                {
                    if (isDraft)
                    {
                        try
                        {
                            WritePendingResource(ukey, DOM);
                        }
                        catch (Exception ex)
                        {
                            ReturnFailure(new string[1] { ex.Message });
                        }
                        ReturnSuccess();
                    }
                    else
                    {
                        System.Text.StringBuilder sb = new System.Text.StringBuilder();
                        int status = -1;

                        if (vstatus.IsValid && userAuths.Length == 0) //user is posting first org/authority record. split this.
                        {
                            string authDOM = string.Empty;
                            string orgDOM = string.Empty;
                            vstatus = SplitNewUserAuthRecords(DOM, ref authDOM, ref orgDOM);
                            if (vstatus.IsValid)
                            {
                                vstatus = ResourceManagement.CleanupAndVerifyData(ref authDOM, ref userAuths);
                                if (vstatus.IsValid)
                                {
                                    vstatus = ResourceManagement.CleanupAndVerifyData(ref orgDOM, ref userAuths);
                                    if (vstatus.IsValid)
                                    {
                                        status = vorXML.LoadVORXML(authDOM, ukey, String.Empty, String.Empty, sb);
                                        if (status == 0)
                                        {
                                            status = vorXML.LoadVORXML(orgDOM, ukey, String.Empty, String.Empty, sb);
                                            if (status == 0)
                                            {
                                                Session["userAuths"] = userAuths;
                                                UserManagement userManager = new UserManagement();
                                                UserManagement.AssociateDefaultUserAuthority((string)userAuths[0], ukey);
                                            }
                                            else
                                                vstatus.MarkInvalid(sb.ToString());
                                        }
                                        else
                                            vstatus.MarkInvalid(sb.ToString());
                                    }
                                }
                            }
                        }
                        else if (vstatus.IsValid)
                            status = vorXML.LoadVORXML(DOM, ukey, String.Empty, String.Empty, sb);

                        if (!vstatus.IsValid)
                            sb.Append(vstatus.GetConcatenatedErrors(", "));

                        if (status != 0)
                            ReturnFailure(new string[1] { "Error publishing resource: " + sb.ToString().Replace('\'', ' ') });
                        else
                        {
                            DeprecatePendingResource(ukey, DOM); //if there was one
                            ReturnSuccess();
                        }
                    }
                }
                else
                    ReturnFailure(vstatus.GetErrors());
                #endregion
            }
        }

        private validationStatus SplitNewUserAuthRecords(string DOM, ref string authDOM, ref string orgDOM)
        {
            validationStatus status = new validationStatus();
            try
            {
                XmlDocument doc = new XmlDocument();
                doc.LoadXml(DOM);
                string ivoid = doc.GetElementsByTagName("identifier")[0].InnerText.Trim();
                string idAuth = ivoid.Substring(0, ivoid.LastIndexOf('/'));

                XmlDocument auth = (XmlDocument)doc.CloneNode(true);
                XmlDocument org = (XmlDocument)doc.CloneNode(true);

                //change for auth record:
                auth.GetElementsByTagName("identifier")[0].InnerText = idAuth;
                XmlAttribute xsitype = auth.FirstChild.Attributes["xsi:type"];
                xsitype.Value = "vg:Authority";


                //change for org record:
                XmlNode managingOrg = org.GetElementsByTagName("managingOrg")[0];
                managingOrg.ParentNode.RemoveChild(managingOrg);
                xsitype = org.FirstChild.Attributes["xsi:type"];
                xsitype.Value = "vr:Organisation";

                System.IO.StringWriter sw = new System.IO.StringWriter();
                XmlTextWriter xw = new XmlTextWriter(sw);
                auth.WriteTo(xw);
                authDOM = sw.ToString();

                sw = new System.IO.StringWriter();
                xw = new XmlTextWriter(sw);
                org.WriteTo(xw);
                orgDOM = sw.ToString();
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.Message);
            }
            return status;
        }

        private void WritePendingResource(long ukey, string DOM)
        {
            XmlDocument doc = new XmlDocument();
            doc.LoadXml(DOM);
            string id = doc.GetElementsByTagName("identifier")[0].InnerText;

            const int maxntext = 1073741823; //2^30-1 is max.
            SqlConnection conn = new SqlConnection(sConnect);
            conn.Open();
            try
            {
                SqlCommand ins = conn.CreateCommand();

                ins.CommandText = "update [PendingResources] set rstat=2 where rstat = 1 and ivoid = '" + id + "' and ukey = " + ukey + ";";
                ins.ExecuteNonQuery();

                ins = conn.CreateCommand();
                ins.CommandText = "insert into [PendingResources](ukey, rstat, ivoid, xml) values(" + ukey + ",1,'" + id + "',@xml);";
                ins.Parameters.Add("@xml", SqlDbType.NText, maxntext);
                ins.Parameters[0].Value = DOM;
                ins.Prepare();
                ins.ExecuteNonQuery();
            }
            finally
            {
                conn.Close();
            }
        }

        private void DeprecatePendingResource(long ukey, string DOM)
        {
            XmlDocument doc = new XmlDocument();
            doc.LoadXml(DOM);
            string id = doc.GetElementsByTagName("identifier")[0].InnerText;

            SqlConnection conn = new SqlConnection(sConnect);
            conn.Open();
            try
            {
                SqlCommand ins = conn.CreateCommand();
                string updateCommand = "UPDATE PendingResources set rstat=0 where rstat = 1 and ivoid = '" + id + "' and ukey = " + ukey + ";";
                ins.CommandText = updateCommand;
                ins.Prepare();
                ins.ExecuteNonQuery();
            }
            finally
            {
                conn.Close();
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