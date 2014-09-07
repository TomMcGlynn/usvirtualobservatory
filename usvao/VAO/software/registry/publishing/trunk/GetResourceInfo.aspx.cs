using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;
using System.Data.SqlClient;
using System.Web.Services;
using System.Xml;
using System.Xml.XPath;

namespace Publishing
{
    public partial class GetResourceInfo : System.Web.UI.Page
    {
        private static string sConnect = (string)System.Configuration.ConfigurationManager.AppSettings["SqlConnection"];
        private static string appDir = System.Web.Hosting.HostingEnvironment.ApplicationPhysicalPath;

        private static string sActive = "active";
        private static string sInactive = "inactive";
        private static string sDeleted = "deleted";

        private static string sNonStandard = "Non-standard";
        private static string sSSA = "Spectra";
        private static string sSIA = "Image";
        private static string sConeSearch = "ConeSearch";
        private static string sWebPage = "Web Page";
        private static string sParamHTTP = "HTTP Request";

        protected void Page_Load(object sender, EventArgs e)
        {
            #region Get Parameters
            System.Collections.Specialized.NameValueCollection input = Request.QueryString;
            if (Request.RequestType == "POST")
                input = Request.Form;
            #endregion

           string action = input["action"];
            if( action == null || action == string.Empty)
                return;

           action = action.ToUpper();
           if (action == "PUBLISHERLIST")
               GetPublisherList();
           else if (action == "AUTHORITYLIST")
               GetAuthorityList();
           else if (action == "GETRESOURCE" && input["identifier"] != null)
                GetResource(input["identifier"], input["pending"]);
           else if (action == "MYLIST")
           {
               GetMyResourcesList();
           }
           else if (action == "MYAUTHORITYLIST")
           {
               GetMyAuthoritiesList();
           }
        }

        private void GetMyAuthoritiesList()
        {
            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            string getListing = "select distinct rs.res_title, rs.ivoid from " +
                                " resource rs, userAuthorities ua where rstat = 1 and ua.authorityID = rs.ivoid and ua.ukey = " + ukey;
            GetIdentifierList(getListing, "AuthorityInfo", true);
        }

        private void GetAuthorityList()
        {
            string getListing = "select distinct rs.res_title, rs.ivoid from authority auth, " +
                                 "resource rs where auth.rkey = rs.pkey and rs.[rstat] = 1 and (harvestedFromID = '' or harvestedFromID like '%STScI%') and rs.res_type like '%Authority%'";
            GetIdentifierList(getListing, "AuthorityInfo", true);

        }

        private void GetPublisherList()
        {
            string getListing = "select rs.res_title, rs.ivoid, rs.res_type from " +
                     "resource rs where [rstat]=1 and (res_type like '%Resource' or res_type like '%Organisation')";
            GetIdentifierList(getListing, "PublisherInfo");
        }

        //todo: read, list info from pending resources
        private System.Collections.Hashtable GetPendingResources(ref System.Collections.Hashtable publishedResources)
        {
            System.Collections.Hashtable table = new System.Collections.Hashtable();

            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            string[] resources = SelectPendingResources(ukey);
            foreach (string str in resources)
            {
                XmlDocument doc = new XmlDocument();
                doc.LoadXml(str);

                string id = doc.GetElementsByTagName("identifier")[0].InnerText;
                string title = doc.GetElementsByTagName("title")[0].InnerText;
                string shortName = string.Empty;
                if (doc.GetElementsByTagName("shortName").Count > 0)
                    shortName = doc.GetElementsByTagName("shortName")[0].InnerText;
                string updated = string.Empty;

                if (!table.ContainsKey(id) && !publishedResources.ContainsKey(id))
                    table.Add(id, ("{\"title\":\"" + title + "\",\"shortName\":\"" + shortName + "\",\"identifier\":\"" + id + "\",\"status\":\"" + "PENDING" + "\",\"updated\":\"" + updated + "\",\"type\":\"" + ' ' + "\"}"));              
            }

            return table;
        }

        private string[] SelectPendingResources(long ukey)
        {
            string[] results = null;
            SqlConnection conn = null;
            try
            {
                string getList = "select xml from PendingResources where rstat = 1 and ukey = " + ukey + ";";

                conn = new SqlConnection(sConnect);
                conn.Open();

                SqlDataAdapter sqlDA = new SqlDataAdapter(getList, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                results = new string[ncount];
                if (ncount > 0)
                {
                    for (int i = 0; i < ncount; ++i)
                    {
                        DataRow dr = ds.Tables[0].Rows[i];
                        results[i] = (string)dr[0];
                    }
                }
            }
            catch (Exception ex)
            {
                string message = ex.Message;
            }
            finally
            {
                conn.Close();
            }
            return results;
        }

        //tdower todo: allow to work with deleted resources.
        private void GetMyResourcesList()
        {
            long ukey = 0;
            if( Session["ukey"] != null )
                ukey = (long)Session["ukey"];

            string getList = "select rs.res_title, rs.short_name, rs.ivoid, rs.[rstat], rs.[updated], cap.cap_type, iface.intf_type from resource rs left outer join capability cap on rs.pkey = cap.rkey " +
                    "left outer join interface iface on cap.pkey = iface.ckey " +
                    "where ([rstat] > 0 and [rstat] != 3) and ukey = " + ukey;

            System.Collections.Hashtable collectedRows = new System.Collections.Hashtable();
            #region query and translate to JSON
            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                SqlDataAdapter sqlDA = new SqlDataAdapter(getList, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                if (ncount > 0)
                {
                    for (int i = 0; i < ncount; ++i)
                    {
                        DataRow dr = ds.Tables[0].Rows[i];
                        string title = ((String)dr[0]).Replace("\"", "").Replace("\'", "");
                        string shortName = ((String)dr[1]).Replace("\"", "").Replace("\'", "");
                        string status = sActive;
                        if ((int)dr[3] == 3)
                            status = sDeleted;
                        else if ((int)dr[3] == 2)
                            status = sInactive;
                        DateTime dt = ((DateTime)dr[4]);
                        string updated = String.Format("{0:u}", dt);

                        string type = string.Empty;
                        if (dr[5] != null && dr[5] != DBNull.Value)
                        {
                            string typetext = ((String)dr[5]);
                            if (typetext.Length == 0 || typetext.IndexOf("Capability") > -1 )
                            {
                                if (dr[6] != null && dr[6] != DBNull.Value)
                                    typetext = ((String)dr[6]);
                                if (typetext.IndexOf("Param") > -1)
                                    type = sParamHTTP;
                                else if (typetext.IndexOf("Web") > -1)
                                    type = sWebPage;
                                else
                                    type = sNonStandard;
                            }
                            else
                            {
                                type = typetext.Substring(typetext.IndexOf(':') + 1);
                                if (type.IndexOf("Spectral") > -1)
                                    type = sSSA;
                                else if (type.IndexOf("Image") > -1)
                                    type = sSIA;
                                else if (type.IndexOf("Cone") > -1)
                                    type = sConeSearch;
                            }
                        }
                        string id = (string)dr[2];
                        if (collectedRows.ContainsKey(id))
                        {
                            String existing = (String)collectedRows[id];
                            if (existing.IndexOf(sNonStandard) > -1)
                                existing = existing.Insert(existing.IndexOf(sNonStandard), type + ", ");
                            else if (existing.IndexOf(sParamHTTP) > -1)
                                existing = existing.Insert(existing.IndexOf(sParamHTTP), type + ", ");
                            else if (existing.IndexOf(sWebPage) > -1)
                                existing = existing.Insert(existing.IndexOf(sWebPage), type + ", ");
                            else
                                existing = existing.Insert(existing.IndexOf("\"}"), ", " + type);
                            collectedRows[id] = existing;
                        }
                        else if(!collectedRows.ContainsKey(id)) //then add new resource to the list
                        {
                            collectedRows.Add(id, ("{\"title\":\"" + title + "\",\"shortName\":\"" + shortName + "\",\"identifier\":\"" + id + "\",\"status\":\"" + status + "\",\"updated\":\"" + updated + "\",\"type\":\"" + type + "\"}"));
                        }
                    }
                }

                //now get 'pending' resources.
                System.Collections.Hashtable pendingRows = GetPendingResources(ref collectedRows);
                if (collectedRows.Count > 0 || pendingRows.Count > 0)
                {
                    ncount = 0;
                    Response.Write("{\"ResourceInfo\":[");
                    foreach (System.Collections.DictionaryEntry de in collectedRows)
                    {
                        Response.Write(de.Value);
                        if (++ncount < (collectedRows.Count + pendingRows.Count)) Response.Write(',');
                    }
                    foreach (System.Collections.DictionaryEntry de in pendingRows)
                    {
                        Response.Write(de.Value);
                        if (++ncount < (collectedRows.Count + pendingRows.Count)) Response.Write(',');
                    }
                    Response.Write("]}");
                }
            }
            catch (Exception ex)
            {
                string message = ex.Message;
            }
            finally
            {
                conn.Close();
                Response.Flush();
            }
            #endregion

        }

        private void GetIdentifierList(string sql, string jsonTitle, bool concat = false)
        {
            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

 
                SqlDataAdapter sqlDA = new SqlDataAdapter(sql, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                String[] titles = new String[ncount];
                String[] keys = new String[ncount];

                for (int i = 0; i < ncount; ++i)
                {
                    DataRow dr = ds.Tables[0].Rows[i];
                    titles[i] = ((String)dr[0]).Replace("\"", "").Replace("\'", "");
                    keys[i] = (String)dr[1];
                    if (concat)
                        titles[i] += " (" + keys[i] + ")";
                }
                Array.Sort(titles, keys);

                Response.Write("{\"" + jsonTitle + "\":[");
                for (int i = 0; i < ncount; ++i)
                {
                    Response.Write("{\"title\":\"" + titles[i] + "\",\"identifier\":\"" + keys[i] + "\"}");
                    if (i < ncount - 1) Response.Write(',');
                }
                Response.Write("]}");
            }
            finally
            {
                conn.Close();
                Response.Flush();
            }
        }

        public static string SelectPendingResource(long ukey, string identifier)
        {
            string response = null;
            string sql = "select top 1 xml from PendingResources where rstat = 1 and ivoid = '" + identifier + "' and ukey = " + ukey;
            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                SqlDataAdapter sqlDA = new SqlDataAdapter(sql, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                if (ncount > 0)
                {
                    response = (string)(ds.Tables[0].Rows[0][0]);
                }
                conn.Close();
            }
            finally
            {
            }
            return response;
        }

        public static string SelectResource(long ukey, string identifier)
        {
            string response = null;
            if (ukey > 0)
            {
                string sql = "select xml from resource where ivoid = '" + identifier + "' and [rstat] = 1 and ukey = " + ukey;
                SqlConnection conn = null;
                try
                {
                    conn = new SqlConnection(sConnect);
                    conn.Open();

                    SqlDataAdapter sqlDA = new SqlDataAdapter(sql, conn);
                    DataSet ds = new DataSet();
                    sqlDA.Fill(ds);

                    int ncount = ds.Tables[0].Rows.Count;
                    if (ncount > 0)
                    {
                        response = (string)(ds.Tables[0].Rows[0][0]);
                    }
                    conn.Close();
                }
                finally
                {
                }
            }
            return response;
        }

        private void GetResource(string identifier, string pending = null)
        {
            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            if (ukey > 0)
            {
                try
                {
                    string response = null;
                    if (pending == null)
                        response = SelectResource(ukey, identifier);
                    else
                        response = SelectPendingResource(ukey, identifier);

                    if (response != null)
                    {
                        Response.ContentType = "text/xml";
                        Response.Write(response);
                    }
                    else
                    {
                        Response.Write("Resource " + identifier + " does not exist or does not belong to the current user. Login may have timed out.");
                    }
                }
                catch (Exception ex)
                {
                    Response.Write(ex.Message);
                }
                finally
                {
                    Response.Flush();
                }
            }
        }
    }
}