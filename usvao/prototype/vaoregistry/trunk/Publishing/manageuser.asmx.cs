using System;
using System.Data;
using System.Data.SqlClient;
using System.Web;
using System.Collections;
using System.Web.Services;
using System.Web.Services.Protocols;
using System.ComponentModel;
using System.IO;
using System.Xml;
using System.Xml.Xsl;
using System.Xml.Serialization;
using System.Text;
using System.Net;

using System.Text.RegularExpressions;

using ivoa.net.vr1_0;
using registry;

namespace Publishing
{
    /// <summary>
    /// Summary description for manageuserpermissions
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class ManageUser : System.Web.Services.WebService
    {
        private static registry.logfile publishErrLog;
        public static string sConnect;

        private static string location = registry.Properties.Settings.Default.vdir; //tdower this may change to separate publishing dir?
        private static string dbAdmin = registry.Properties.Settings.Default.dbAdmin;
        private static string baseURL = registry.Properties.Settings.Default.baseURL;

        public ManageUser()
        {
            publishErrLog = new registry.logfile("err_PublishingService.log");
            try
            {
                sConnect = registry.Properties.Settings.Default.SqlAdminConnection;
            }
            catch (Exception) { }

            if (sConnect == null)
                throw new Exception("Registry: SqlConnection.String not found in Web.config");
        }

        [WebMethod(EnableSession = true, Description = "This will be replaceable with Single Sign-in.")]
        public RegistryResponse Logout()
        {
            Session.Clear();
            RegistryResponse response = new RegistryResponse(0, String.Empty);

            return response;
        }

        [WebMethod(EnableSession = true, Description = "This will be replaceable with Single Sign-in.")]
        public RegistryResponse Login()
        {
            RegistryResponse response = new RegistryResponse(0, String.Empty);
            try
            {
                XmlTextReader reader = RegistryManagement.GetRequestXML("Login", Context.Request.InputStream);
                if (reader == null)
                {
                    response.returncode = 2;
                    response.message = "Invalid user data.";
                    return response;
                }

                string username = string.Empty;
                string password = string.Empty;

                while (reader.Read())
                {
                    if (reader.NodeType == XmlNodeType.Element)
                    {
                        if (reader.Name.ToString().ToUpper() == "USERNAME")
                            username = reader.ReadElementContentAsString();
                        else if (reader.Name.ToString().ToUpper() == "PASSWORD")
                            password = reader.ReadElementContentAsString();
                    }
                }

                SqlConnection conn = null;
                try
                {
                    conn = new SqlConnection(sConnect);
                    conn.Open();

                    string testUser = "SELECT username, pkey, authkey FROM USERS WHERE( username = '" + username + "'" +
                                          "AND PASSWORD = '" + password + "')";
                    SqlDataAdapter sqlDA = new SqlDataAdapter(testUser, conn);
                    DataSet ds = new DataSet();
                    sqlDA.Fill(ds);

                    Session.Clear();
                    if (ds.Tables[0].Rows.Count > 0)
                    {
                        DataRow user = ds.Tables[0].Rows[0];
                        Session["UserName"] = (string)user[0];
                        Session["UserKey"] = (long)user[1];
                        if (user[2] != null)
                            Session["Authority"] = (long)user[2];
                        else
                            Session["Authority"] = (long)0;
                    }
                    else
                    {
                        response.returncode = 2;
                        response.message = "Incorrect username or password.";
                    }
                }
                finally
                {
                    conn.Close();
                }
            }
            catch (Exception e)
            {
                Session.Clear();

                if (publishErrLog != null)
                    publishErrLog.Log("General error in login: " + e.Message);

                response.returncode = -1;
                response.message = e.ToString();
            }
            return response;
        }

    }
}
