using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;
using System.Data.SqlClient;
using System.Web.Services;


namespace Publishing
{
    public partial class GetResourceInfo : System.Web.UI.Page
    {
        private static string sConnect = registry.Properties.Settings.Default.SqlConnection;
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
           else if (action == "GETRESOURCE")
           {
               if (input["identifier"] != null)
                   GetResource(input["identifier"]);
           }
           else if (action == "MYLIST")
           {
               GetMyResourcesList();
           }
        }

        private void GetAuthorityList()
        {
            string getListing = "select distinct rs.title, rs.identifier from authority auth, " +
                                  "resource rs where auth.rkey = rs.pkey and rs.[@status] = 1 and (harvestedFrom = '' or harvestedFrom like 'STScI%') and rs.xsi_type like '%Authority%'";
            GetIdentifierList(getListing, "AuthorityInfo", true);

        }

        private void GetPublisherList()
        {
            string getListing = "select rs.title, rs.identifier, rs.xsi_type from " +
                     "resource rs where [@status]=1 and (xsi_type like '%Resource' or xsi_type like '%Organisation')";
            GetIdentifierList(getListing, "PublisherInfo");
        }

        //tdower todo: allow to work with deleted resources.
        private void GetMyResourcesList()
        {
            long ukey = 0;
            if( Session["ukey"] != null )
                ukey = (long)Session["ukey"];

            string getList = "select rs.title, rs.shortName, rs.identifier, rs.[@status], rs.[@updated], cap.xsi_type, iface.xsi_type from resource rs left outer join capability cap on rs.pkey = cap.rkey " +
                    "left outer join interface iface on cap.pkey = iface.container_key " +
                    "where ([@status] = 1 or [@status] = 0) and ukey = " + ukey;

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

                    System.Collections.Hashtable collectedRows = new System.Collections.Hashtable();
                    for (int i = 0; i < ncount; ++i)
                    {
                        DataRow dr = ds.Tables[0].Rows[i];
                        string title = ((String)dr[0]).Replace("\"", "").Replace("\'", "");
                        string shortName = ((String)dr[1]).Replace("\"", "").Replace("\'", "");
                        string status = sActive;
                        if ((int)dr[3] == 3)
                            status = sDeleted;
                        else if ((int)dr[3] == 0)
                            status = sInactive;
                        DateTime dt = ((DateTime)dr[4]);
                        string updated = String.Format("{0:u}", dt);

                        string type = string.Empty;
                        if (dr[5] != null && dr[5] != DBNull.Value )
                        {
                            string typetext = ((String)dr[5]);
                            if (typetext.Length == 0)
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
                            else if (typetext.IndexOf(':') > -1)
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
                            else if (existing.IndexOf(sWebPage) > -1 )
                                existing = existing.Insert(existing.IndexOf(sWebPage), type + ", ");
                            else
                                existing = existing.Insert(existing.IndexOf("\"}"), ", " + type);
                            collectedRows[id] = existing;
                        }
                        else //then add new resource to the list
                        {
                            collectedRows.Add(id, ("{\"title\":\"" + title + "\",\"shortName\":\"" + shortName + "\",\"identifier\":\"" + id + "\",\"status\":\"" + status + "\",\"updated\":\"" + updated + "\",\"type\":\"" + type + "\"}"));
                        }
                    }

                    ncount = 0;
                    Response.Write("{\"ResourceInfo\":[");
                    foreach (System.Collections.DictionaryEntry de in collectedRows)
                    {
                        Response.Write(de.Value);
                        if (++ncount < collectedRows.Count) Response.Write(',');
                    }
                    Response.Write("]}");
                }
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

        private void GetResource(string identifier)
        {
            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            if (ukey > 0)
            {
                string sql = "select xml from resource where identifier = '" + identifier + "' and [@status] = 1 and ukey = " + ukey;
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
                        Response.ContentType = "text/xml";
                        Response.Write((string)ds.Tables[0].Rows[0][0]);
                    }
                    else
                    {
                        Response.Write("Resource " + identifier + " does not exist or does not belong to the current user. Login may have timed out.");
                    }
                    conn.Close();
                }
                finally
                {
                    Response.Flush();
                }
            }
        }
    }
}