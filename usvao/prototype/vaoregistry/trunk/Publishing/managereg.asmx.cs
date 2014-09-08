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
using oai;

namespace Publishing
{
    /// <summary>
    /// Registry support web service
    /// </summary>
    [System.Web.Services.WebService(Namespace = "http://www.stsci.edu/registrysupport", Name="RegistryManagement")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [ToolboxItem(false)]
    public class RegistryManagement : System.Web.Services.WebService
    {
        private static registry.logfile publishErrLog;
        public static string sConnect;

        private static string location = registry.Properties.Settings.Default.vdir; //tdower this may change to separate publishing dir?
        private static string dbAdmin = registry.Properties.Settings.Default.dbAdmin;
        private static string baseURL = registry.Properties.Settings.Default.baseURL;

        public static VOR_XML vorXML = new VOR_XML();

        public static ArrayList oaiListing = new ArrayList();

        public RegistryManagement()
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

        public static long GetAuthorityKey(string id, SqlConnection conn)
        {
            string select = "SELECT Authority.pkey FROM Authority, Resource WHERE( Authority.rkey = Resource.pkey and Resource.identifier LIKE '" + id + "')";
            SqlDataAdapter sqlDA = new SqlDataAdapter(select, conn);
            DataSet ds = new DataSet();
            sqlDA.Fill(ds);

            if (ds.Tables[0].Rows.Count > 0)
            {
                DataRow auth = ds.Tables[0].Rows[0];
                return (long)auth[0];
            }
            return 0;
        }


        //Returns list of Authority records as a ResourceList, sorted alphabetically.
        //We can get this from OAI if we really want to slog through the records, so no need
        //to keep state here.
        [WebMethod(Description=" Returns list of Authority records as a title/identifier list, sorted alphabetically, and with a possible error message. This is OAI-accessible data filtered for the XForms presentation layer.")]
        public ResourceList GetAuthorityList()
        {
            ResourceList list = new ResourceList();
            list.Response = new RegistryResponse(0, String.Empty);

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                list.Records = null;
                conn.Open();

                string getListing = "select rs.title, rs.identifier from authority auth, " +
                                    "resource rs where auth.rkey = rs.pkey and rs.[@status] = 1 and (harvestedFrom = '' or harvestedFrom like 'STScI%') and rs.xsi_type like '%Authority%'";

                SqlDataAdapter sqlDA = new SqlDataAdapter(getListing, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

				int ncount = ds.Tables[0].Rows.Count;
				list.Records = new ResourceInfo[ncount];
                String[] titles = new String[ncount];
                String[] keys = new String[ncount];

				for (int i=0;i<ncount;++i)
				{
					DataRow dr = ds.Tables[0].Rows[i];
                    titles[i] = (String)dr[0];
                    keys[i] = (String)dr[1];
                }
                Array.Sort(titles, keys);
                for(int i = 0; i < ncount;++i)
                {
                    list.Records[i] = new ResourceInfo(titles[i], keys[i]);
                }

            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting authority list: " + e.Message);

                list.Response.returncode = -1;
                list.Response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            return list;
        }


        [WebMethod(Description="Returns list of managing organisations as strings with a possible error message. This is OAI-accessible data filtered for the XForms presentation layer.")]
        public ManagingOrgList GetManagingOrgList()
        {
            ManagingOrgList list = new ManagingOrgList();
            list.Response = new RegistryResponse(0, String.Empty);

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                list.Records = null;
                conn.Open();

                string getListing = "select distinct auth.managingOrg from authority auth where auth.[@status] = 1 and (auth.harvestedFrom = '' or auth.harvestedFrom like 'STScI%')";
                SqlDataAdapter sqlDA = new SqlDataAdapter(getListing, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                list.Records = new string[ncount];
                for (int i = 0; i < ncount; ++i)
                {
                    DataRow dr = ds.Tables[0].Rows[i];
                    list.Records[i] = (string)dr[0];
                }
                Array.Sort(list.Records);

            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting managing organisation list: " + e.Message);

                list.Response.returncode = -1;
                list.Response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            return list;
        }

        private string GetAuthIdentifier(string userKey)
        {
            string identifier = string.Empty;
            string query = "select resource.identifier from users, authority, resource " +
                           "where users.authkey = authority.pkey and authority.rkey = resource.pkey " +
                           "and users.pkey = " + userKey;
            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();
                SqlDataAdapter sqlDA = new SqlDataAdapter(query, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                if (ncount > 0)
                {
                    DataRow dr = ds.Tables[0].Rows[0];
                    identifier = (String)dr[0];
                }
            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting authority identifier: " + e.Message);
                identifier = string.Empty;
            }
            finally
            {
                conn.Close();
            }
            return identifier;
        }

        //[WebMethod(EnableSession = true)]
        private RegistryResponse CanPublishNew()
        {
            // Check for things we know can go wrong -- not logged in, or
            // no authority record and thus an invalid user state.
            // (There may be no authority record for new users. This will cause us to
            //  prompt them for authority record creation.)
            RegistryResponse response = new RegistryResponse(0, String.Empty);
            String username = string.Empty;
            long authorityKey = 0;
            long userKey = 0;
            try
            {
                username = (String)Session["UserName"];
                authorityKey = (long)Session["Authority"];
                userKey = (long)Session["UserKey"];
            }
            catch (Exception)
            {
                response.returncode = 2;
                response.message = "Not Logged In. Your session may have expired. Please log back in and try again.";
                return response;
            }
            if (authorityKey == 0)
            {
                response.returncode = 1;
                response.message = "No authority record associated with this user.";
            }

            return response;
        }


        [WebMethod(EnableSession = true)]
        private string CreateIdentifier(string resourceKey)
        {
            if (resourceKey.StartsWith("ivo://"))
                return resourceKey;

            string identifier = string.Empty;
            RegistryResponse resp = CanPublishNew();
            if (resp.returncode == 0 )
            {
                long authorityKey = (long)Session["Authority"];
                string query = "select resource.identifier from authority, resource where authority.pkey = " +
                                authorityKey + " and authority.rkey = resource.pkey";
                SqlConnection conn = null;
                try
                {
                    conn = new SqlConnection(sConnect);
                    conn.Open();
                    SqlDataAdapter sqlDA = new SqlDataAdapter(query, conn);
                    DataSet ds = new DataSet();
                    sqlDA.Fill(ds);

                    int ncount = ds.Tables[0].Rows.Count;
                    if( ncount > 0 )
                    {
                        DataRow dr = ds.Tables[0].Rows[0];
                        string authName = (String)dr[0];

                        if( resourceKey.StartsWith("ivo://") )
                        {
                            if (resourceKey.StartsWith(authName + '/'))
                                identifier = resourceKey;
                            else
                            {
                                if (publishErrLog != null)
                                    publishErrLog.Log("Bad resource key entered: " + resourceKey);
                                identifier = string.Empty;
                            }
                        }
                        else
                            identifier = authName + '/' + resourceKey;
                    }
                }
                catch (Exception e)
                {
                    if (publishErrLog != null)
                        publishErrLog.Log("Exception getting authority identifier: " + e.Message);
                    identifier = string.Empty;
                }
                finally
                {
                    conn.Close();
                }
            }

            return identifier;
        }

        [WebMethod(EnableSession = true, Description = "Gets an editable list of pending resources for the session user. This will be changed with Single Sign-in and access rights management.")]
        public ResourceList GetMyPendingResources()
        {
            ResourceList list = new ResourceList();
            list.Response = CanPublishNew();
            if (list.Response.returncode == 0)
            {
                long userKey = (long)Session["UserKey"];
                DirectoryInfo di = new DirectoryInfo(location + "\\publish_temp");
                FileInfo[] rgFiles = di.GetFiles(userKey.ToString() + ",*.xml");
                if (rgFiles.Length > 0)
                {
                    list.Records = new ResourceInfo[rgFiles.Length];
                    for (int i = 0; i < rgFiles.Length; ++i)
                    {
                        String identifier = "ivo://" + rgFiles[i].Name.Substring(userKey.ToString().Length + 1).Replace(".xml", String.Empty).Replace(',', '/');
                        list.Records[i] = new ResourceInfo(String.Empty, identifier);
                        StreamReader reader = null;
                        try
                        {
                            reader = new StreamReader(location + "\\publish_temp\\" + rgFiles[i].Name, true);
                            XmlReader xr = new XmlTextReader(reader);
                            while (xr.Read())
                            {
                                string name = xr.LocalName.ToUpper();
                                if ((name == "TITLE") && (xr.NodeType != XmlNodeType.EndElement))
                                {
                                    xr.Read();
                                    list.Records[i].title = xr.Value.Trim();
                                    break; // done.
                                }
                            }
                            reader.Close();
                        }
                        catch (Exception)
                        {
                            list.Records[i].title = "[unavailable]";
                        }

                    }
                }

                if (list.Records.Length == 0)
                {
                    list.Response.returncode = 1;
                    list.Response.message = "No pending records associated with this user.";
                }
            }

            return list;
        }


        [WebMethod(EnableSession=true, Description="Gets an editable list of resources for the session user. This will be changed with Single Sign-in and access rights management.")]
        public ResourceList GetMyExistingResources()
        {
            // First check for things we know can go wrong -- not logged in, or
            // no authority record and thus an invalid user state.
            // (There may be no authority record for new users. This will cause us to
            //  prompt them for authority record creation.)
            ResourceList list = new ResourceList();
            list.Response = CanPublishNew();
            if (list.Response.returncode != 0)
                return list;

            long userKey = (long)Session["UserKey"];

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                list.Records = null;
                conn.Open();

                string getListing = "select rs.title, rs.identifier from " +
                                    "resource rs, users usr where rs.ukey = usr.pkey" +
                                    " and usr.pkey = " + userKey + " and (rs.[@status] = 1) order by identifier";

                SqlDataAdapter sqlDA = new SqlDataAdapter(getListing, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                list.Records = new ResourceInfo[ncount];
                for (int i = 0; i < ncount; ++i)
                {
                    DataRow dr = ds.Tables[0].Rows[i];
                    list.Records[i] = new ResourceInfo((String)dr[0], (String)dr[1]);
                }
            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting resource list: " + e.Message);

                list.Response.returncode = -1;
                list.Response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            if (list.Records.Length == 0)
            {
                list.Response.returncode = 1;
                list.Response.message = "No existing records associated with this user.";
            }

            return list;
        }

        [WebMethod(Description = "Gets a list of potential IVOIDs for drop-downs, services only.")]
        public ResourceList GetServiceList()
        {
            ResourceList list = new ResourceList();
            list.Response = new RegistryResponse(0, String.Empty);

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                list.Records = null;
                conn.Open();

                string getListing = "select rs.title, rs.identifier, rs.xsi_type from " +
                                    "resource rs where [@status]=1 and (harvestedFrom = '' or harvestedFrom like 'STScI%') and (xsi_type like '%Service')";

                SqlDataAdapter sqlDA = new SqlDataAdapter(getListing, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                list.Records = new ResourceInfo[ncount];
                String[] titles = new String[ncount];
                String[] keys = new String[ncount];

                string type = string.Empty;
                for (int i = 0; i < ncount; ++i)
                {
                    DataRow dr = ds.Tables[0].Rows[i];
                    titles[i] = (String)dr[0];
                    type = (String)dr[2];
                    titles[i] += " (" + type.Substring(type.IndexOf(':') + 1) + ")";
                    keys[i] = (String)dr[1];
                }
                Array.Sort(titles, keys);
                for (int i = 0; i < ncount; ++i)
                {
                    list.Records[i] = new ResourceInfo(titles[i], keys[i]);
                }
            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting resource list: " + e.Message);

                list.Response.returncode = -1;
                list.Response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            return list;
        }

        [WebMethod(Description = "Gets a list of potential IVOIDs for drop-downs.")]
        public ResourceList GetIVOIDOrgList()
        {
            ResourceList list = new ResourceList();
            list.Response = new RegistryResponse(0, String.Empty);

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                list.Records = null;
                conn.Open();

                string getListing = "select rs.title, rs.identifier, rs.xsi_type from " +
                                    "resource rs where [@status]=1 and (harvestedFrom = '' or harvestedFrom like 'STScI%') and (xsi_type like '%Resource' or xsi_type like '%Organisation')";

                SqlDataAdapter sqlDA = new SqlDataAdapter(getListing, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                list.Records = new ResourceInfo[ncount];
                String[] titles = new String[ncount];
                String[] keys = new String[ncount];

                for (int i = 0; i < ncount; ++i)
                {
                    DataRow dr = ds.Tables[0].Rows[i];
                    titles[i] = (String)dr[0];
                    if (((String)dr[2]).EndsWith("Organisation"))
                        titles[i] += " (Organisation)";
                    keys[i] = (String)dr[1];
                }
                Array.Sort(titles, keys);
                for (int i = 0; i < ncount; ++i)
                {
                    list.Records[i] = new ResourceInfo(titles[i], keys[i]);
                }
            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting resource list: " + e.Message);

                list.Response.returncode = -1;
                list.Response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            return list;
        }

        [WebMethod(Description = "Gets a list of potential IVOIDs for drop-downs.")]
        public ResourceList GetIVOIDPlainResourceList()
        {
            ResourceList list = new ResourceList();
            list.Response = new RegistryResponse(0, String.Empty);

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                list.Records = null;
                conn.Open();

                string getListing = "select rs.title, rs.identifier, rs.xsi_type from " +
                                    "resource rs where [@status]=1 and (xsi_type like '%Resource')";

                SqlDataAdapter sqlDA = new SqlDataAdapter(getListing, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);

                int ncount = ds.Tables[0].Rows.Count;
                list.Records = new ResourceInfo[ncount];
                String[] titles = new String[ncount];
                String[] keys = new String[ncount];

                for (int i = 0; i < ncount; ++i)
                {
                    DataRow dr = ds.Tables[0].Rows[i];
                    titles[i] = (String)dr[0];
                    if (((String)dr[2]).EndsWith("Organisation"))
                        titles[i] += " (Organisation)";
                    keys[i] = (String)dr[1];
                }
                Array.Sort(titles, keys);
                for (int i = 0; i < ncount; ++i)
                {
                    list.Records[i] = new ResourceInfo(titles[i], keys[i]);
                }
            }
            catch (Exception e)
            {
                if (publishErrLog != null)
                    publishErrLog.Log("Exception getting resource list: " + e.Message);

                list.Response.returncode = -1;
                list.Response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            return list;
        }

        [WebMethod(EnableSession = true, Description="Takes Authority Resource as XML. Posts to Registry and associates session user.")]
        public RegistryResponse PostAuthorityResourceAndAssociateUser()
        {
            RegistryResponse response = new RegistryResponse(0, String.Empty);

            String username = string.Empty;
            long authorityKey = 0;
            long userKey = 0;
            try
            {
                username = (String)Session["UserName"];
                userKey = (long)Session["UserKey"];
                authorityKey = (long)Session["Authority"];
            }
            catch (Exception e)
            {
                response.returncode = 2;
                response.message = "Not Logged In. Your session may have expired. Please log back in and try again.";
                return response;
            }

            if (authorityKey != 0)
            {
                response.returncode = 1;
                response.message = "Authority record already associated with this user.";
                return response;
            }

            String xml = String.Empty;
            int startID;
            string identifier;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                //Set managing org to Publisher text
                if (strmContents.Contains("<managingOrg/>"))
                {
                    int startPublisher = strmContents.IndexOf(">", strmContents.IndexOf("<publisher")) + 1;
                    int endPublisher = strmContents.IndexOf("</", startPublisher);
                    strmContents = strmContents.Replace("<managingOrg/>",
                        "<managingOrg>" + strmContents.Substring(startPublisher, endPublisher - startPublisher) + "</managingOrg>");
                }

                startID = strmContents.IndexOf("<identifier>") + 12;
                identifier = strmContents.Substring(startID, strmContents.IndexOf("</", startID) - startID);
                if( !(identifier.StartsWith("ivo://")))
                    strmContents = strmContents.Replace("<identifier>", "<identifier>ivo://");

                strmContents = SetupResourceNamespaces(strmContents, "vg:Authority");

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            startID = xml.IndexOf("<identifier>") + 12;
            identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);
            response = CreatePendingFile(identifier, xml, userKey);
            if (response.returncode == 0)
            {
                response = SavePendingResource(identifier, userKey);
                AssociateUserAuthority(identifier, userKey);
            }

            return response;
        }

        internal RegistryResponse AssociateUserAuthority(String authorityIdentifier, long userKey)
        {
            RegistryResponse response = new RegistryResponse(0, String.Empty);

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                long auth = RegistryManagement.GetAuthorityKey(authorityIdentifier, conn);
                if (auth == 0)
                {
                    response.returncode = 1;
                    response.message = "Cannot retrieve authority identifier to associate with user.";
                }
                else
                {
                    String updateCmd = "UPDATE USERS SET authkey = " +
                                auth +
                                " where( pkey = " + userKey + ")";
                    SqlCommand command = new SqlCommand(updateCmd, conn);
                    try
                    {
                        command.ExecuteNonQuery();
                        Session["Authority"] = auth;
                    }
                    catch (Exception)
                    {
                        response.returncode = 1;
                        response.message = "Default user authority not set.";
                    }

                    try
                    {
                        String insertCmd = "INSERT INTO USERAUTHORITIES(userkey, authkey) VALUES(" +
                                    userKey + ',' + auth + ")";
                        command = new SqlCommand(insertCmd, conn);
                        command.ExecuteNonQuery();
                    }
                    catch (Exception)
                    {
                        if (response.returncode == 1)
                            response.message = "Failure. Default user authority not set. Authority not added to list.";
                        else
                            response.message = "Partial Success: Default user authority set, but not added to list.";
                        response.returncode = 1;
                    }
                }
            }
            finally
            {
                conn.Close();
            }

            return response;
        }


        [WebMethod(EnableSession = true, Description = "Takes generic CatalogService Resource as XML. Posts to Registry.")]
        public RegistryResponse PostCatalogServiceResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"]; 
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                //todo - check for Catalog-specific data.


                //Set up the typing our deserialisation expects
                strmContents = SetupResourceNamespaces(strmContents, "vs:Catalog");

                //format this in place -- remove certain optional items which may be blank.
                String trimmedContents = strmContents.Replace("\n", String.Empty);
                xml = trimmedContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description="Takes Authority Resource as XML. Posts to Registry.")]
        public RegistryResponse PostAuthorityResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"]; 
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                //Set managing org to Publisher text
                if (strmContents.Contains("<managingOrg/>"))
                {
                    int startPublisher = strmContents.IndexOf(">", strmContents.IndexOf("<publisher")) + 1;
                    int endPublisher = strmContents.IndexOf("</", startPublisher);
                    strmContents = strmContents.Replace("<managingOrg/>",
                        "<managingOrg>" + strmContents.Substring(startPublisher, endPublisher - startPublisher) + "</managingOrg>");
                }

                //Set up the typing our deserialisation expects
                strmContents = strmContents.Replace("<Resource", "<ri:Resource");
                strmContents = strmContents.Replace("</Resource", "</ri:Resource");
                strmContents = strmContents.Replace("xmlns=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\"", "xmlns=\"\"");
                strmContents = strmContents.Replace("XMLSchema-instance\"", "XMLSchema-instance\" xsi:type=\"vg:Authority\" ");
                strmContents = strmContents.Replace("\n", String.Empty);

                //fix up waveband, rights, ContentLevels, ContentTypes
                strmContents = ExpandSelectAll(strmContents, "type");
                strmContents = ExpandSelectAll(strmContents, "contentLevel");
                int start = strmContents.IndexOf("<contentLevel>");
                int end = strmContents.LastIndexOf("</contentLevel>");
                if (strmContents.IndexOf('_', start) < end && strmContents.IndexOf('_', start) > -1)
                {
                    string spaces = strmContents.Substring(start, end - start).Replace('_', ' ');
                    strmContents = strmContents.Replace(strmContents.Substring(start, end - start), spaces);
                }

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        private string ExpandResourceXML(string strmContents)
        {
            string contents = strmContents;
            int start = 0;
            int end = 0;

            //fix up waveband, rights, ContentLevels, ContentTypes
            contents = ExpandSelectAll(contents, "waveband");
            contents = ExpandSelectAll(contents, "type");
            contents = ExpandSelectAll(contents, "contentLevel");
            contents = ExpandSelectAll(contents, "dataSource");
            contents = ExpandSelectAll(contents, "creationType");

            contents = ExpandSelectAll(contents, "contentLevel");
            start = contents.IndexOf("<contentLevel>");
            end = contents.LastIndexOf("</contentLevel>");
            if (start > -1 && contents.IndexOf('_', start) < end && contents.IndexOf('_', start) > -1)
            {
                string spaces = contents.Substring(start, end - start).Replace('_', ' ');
                contents = contents.Replace(contents.Substring(start, end - start), spaces);
            }

            return contents;
        }

        internal static bool CheckForUsersAuthority(string id, long userKey, SqlConnection conn)
        {
            //we have to let them post their first authority resource.
            //Without one, the xforms interface should disallow us from getting 
            //to this point from anywhere but the form for generation of first authority record.
            string select = "select * from userauthorities where userkey = " + userKey;
            DataSet ds = new DataSet();
            SqlDataAdapter sqlDA = new SqlDataAdapter(select, conn);
            sqlDA.Fill(ds);
            int count = ds.Tables[0].Rows.Count;
            if (count == 0)
                return true;


            select =
                "SELECT identifier FROM users left JOIN( SELECT userkey, authkey, auths.pkey, rkey, identifier " +
                "FROM userauthorities left join(SELECT distinct authority.pkey, rkey, identifier " +
                "FROM authority left join resource on resource.pkey = authority.rkey) auths " +
                "on auths.pkey = userauthorities.authkey) authorities on users.pkey = userkey " +
                "where users.pkey = " + userKey;

            sqlDA = new SqlDataAdapter(select, conn);
            ds = new DataSet();
            sqlDA.Fill(ds);

            count = ds.Tables[0].Rows.Count;
            for (int i = 0; i < count; ++i)
            {
                try
                {
                    string authid = (string)ds.Tables[0].Rows[i][0];
                    if (id.StartsWith(authid))
                        return true;
                }
                catch (Exception) { }
            }

            return false;
        }


        [WebMethod(EnableSession = true, Description = "Takes Authority Resource as XML. Posts to Registry.")]
        public RegistryResponse EditAuthorityResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();
                strmContents = SetupResourceNamespaces(strmContents, "vg:Authority");

                xml = ExpandResourceXML(strmContents);
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                if( newID != identifier )
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = new RegistryResponse(1, "Error editing pending file.");
            }
            return response;
        }

        private RegistryResponse DisallowFalseStandardRole(string contents)
        {
            //if we have a capability AND it has a non-standard "standard ID" AND it has a role="std", no good.

            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            int start = contents.IndexOf("<capability");
            while (start > -1)
            {
                int endcap = contents.IndexOf("</capability", start);
                int end = contents.IndexOf('>', start);
                
                start = contents.IndexOf("<genericinterface", start, endcap - start);
                while (start > -1)
                {
                    end = contents.IndexOf('>', start);
                    if (contents.IndexOf("role=\"std\"", start, end - start) != -1 ||
                        contents.IndexOf("role=\"STD\"", start, end - start) != -1)
                    {
                        resp.returncode = -1;
                        resp.message = "\'std\' role not allowed for non-standard capabilities.";
                        return resp;
                    }
                    start = contents.IndexOf("<genericinterface", end);
                }

                start = contents.IndexOf("<capability", endcap);
            }

            return resp;
        }

        [WebMethod(EnableSession = true, Description = "Takes Data Service Resource as XML. Posts to Registry.")]
        public RegistryResponse PostDataServiceResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"]; 
            String xml = String.Empty;
            int start = 0;
            int end = 0;
            try
            {
                String strmContents = CreateDates(GetRequestString());
                strmContents = SetupResourceNamespaces(strmContents, "vg:DataService");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");

                strmContents = ExpandResourceXML(strmContents);
                if( strmContents.IndexOf("<capability>" ) > -1)
                {
                    start = strmContents.IndexOf("<capability");
                    end = strmContents.IndexOf("</capability>") + 13;
                    strmContents = strmContents.Remove(start, end - start);
                }
                strmContents = strmContents.Replace("<rights/>", "");

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Data Service Resource as XML. Posts to Registry.")]
        public RegistryResponse EditDataServiceResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();
                strmContents = SetupResourceNamespaces(strmContents, "vg:DataService");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");

                strmContents = ExpandResourceXML(strmContents);
                if (strmContents.IndexOf("<capability>") > -1)
                {
                    int start = strmContents.IndexOf("<capability");
                    int end = strmContents.IndexOf("</capability>") + 13;
                    strmContents = strmContents.Remove(start, end - start);
                }
                strmContents = strmContents.Replace("<rights/>", "");

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }


        [WebMethod(EnableSession = true, Description = "Takes OpenSkyNode Service Resource as XML. Posts to Registry.")]
        public RegistryResponse PostOpenSkyNodeResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            int start = 0;
            int end = 0;
            try
            {
                String strmContents = CreateDates(GetRequestString());
                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"OpenSkyNode", "xsi:type=\"sn:OpenSkyNode");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");

                start = strmContents.IndexOf("<capability");
                end = strmContents.IndexOf("<longitude/>", start);
                if (end > -1)
                    strmContents = strmContents.Remove(strmContents.IndexOf("<longitude/>", start), 12);
                end = strmContents.IndexOf("<latitude/>", start);
                if( end > -1 )
                    strmContents = strmContents.Remove(strmContents.IndexOf("<latitude/>", start), 11);

                strmContents = ExpandResourceXML(strmContents);
                if (strmContents.IndexOf("<capability>") > -1)
                {
                    start = strmContents.IndexOf("<capability");
                    end = strmContents.IndexOf("</capability>") + 13;
                    strmContents = strmContents.Remove(start, end - start);
                }
                strmContents = strmContents.Replace("<rights/>", "");

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes OpenSkyNode Service Resource as XML. Posts to Registry.")]
        public RegistryResponse EditOpenSkyNodeResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();
                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"OpenSkyNode", "xsi:type=\"sn:OpenSkyNode");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");


                strmContents = ExpandResourceXML(strmContents);
                if (strmContents.IndexOf("<capability>") > -1)
                {
                    int start = strmContents.IndexOf("<capability");
                    int end = strmContents.IndexOf("</capability>") + 13;
                    strmContents = strmContents.Remove(start, end - start);
                }
                strmContents = strmContents.Replace("<rights/>", "");

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Data Collection Resource as XML. Posts to Registry.")]
        public RegistryResponse PostDataCollectionResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                strmContents = SetupResourceNamespaces(strmContents, "vs:DataCollection");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());
                strmContents = CleanUpCatalogTables(strmContents);

                xml = ExpandResourceXML(strmContents);
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Data Collection Resource as XML. Posts to Registry.")]
        public RegistryResponse EditDataCollectionResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();

                strmContents = SetupResourceNamespaces(strmContents, "vs:DataCollection");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());
                strmContents = CleanUpCatalogTables(strmContents);

                xml = ExpandResourceXML(strmContents);
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Basic Service Resource as XML. Posts to Registry.")]
        public RegistryResponse PostServiceResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());
                strmContents = SetupResourceNamespaces(strmContents, "vr:Service");

                strmContents = ExpandResourceXML(strmContents);
                if (strmContents.Contains("<interface version=\"1.0\">"))
                {
                    int start = strmContents.IndexOf("<capability>");
                    int end = strmContents.IndexOf("</capability>") + 13;
                    strmContents = strmContents.Remove(start, end - start);
                }

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true)]
        public RegistryResponse EditServiceResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();
                strmContents = SetupResourceNamespaces(strmContents, "vr:Service");

                strmContents = ExpandResourceXML(strmContents);
                if (strmContents.Contains("<interface version=\"1.0\" />"))
                {
                    int start = strmContents.IndexOf("<capability>");
                    int end = strmContents.IndexOf("</capability>") + 13;
                    strmContents = strmContents.Remove(start, end - start);
                }

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Cone Search Resource as XML. Posts to Registry.")]
        public RegistryResponse PostConeSearchResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"ConeSearch", "xsi:type=\"cs:ConeSearch");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");
                strmContents = ExpandResourceXML(strmContents);

                //removing some possibly empty optional parameters.
                strmContents = strmContents.Replace("<verb/>", "");
                strmContents = strmContents.Replace("<catalog/>", "");
                strmContents = strmContents.Replace("<extras/>", "");
                strmContents = strmContents.Replace("<resultType/>", "");
                strmContents = strmContents.Replace("<queryType/>", "");
                strmContents = strmContents.Replace("<rights/>", "");

                strmContents = strmContents.Replace("><", ">\n<");
                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true)]
        public RegistryResponse EditConeSearchResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();

                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"ConeSearch", "xsi:type=\"cs:ConeSearch");

                strmContents = CleanUpSTC(strmContents, userKey.ToString());

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");
                strmContents = ExpandResourceXML(strmContents);

                //removing some possibly empty optional parameters.
                strmContents = strmContents.Replace("<verb/>", "");
                strmContents = strmContents.Replace("<catalog/>", "");
                strmContents = strmContents.Replace("<extras/>", "");
                strmContents = strmContents.Replace("<resultType/>", "");
                strmContents = strmContents.Replace("<queryType/>", "");
                strmContents = strmContents.Replace("<rights/>", "");

                strmContents = strmContents.Replace("><", ">\n<");
                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        private string CleanUpSIATestQuery(string contents)
        {
            int startPos = contents.IndexOf("<pos>");
            int endPos = contents.IndexOf("</pos>", startPos) + 6;
            string temp = contents.Substring(startPos, endPos - startPos);
            if (temp.Contains("<long/>") && temp.Contains("<lat/>") )
            {
                contents = contents.Remove(startPos, endPos - startPos);
            }
            startPos = contents.IndexOf("<size>");
            endPos = contents.IndexOf("</size>", startPos) + 7;
            temp = contents.Substring(startPos, endPos - startPos);
            if (temp.Contains("<long/>") && temp.Contains("<lat/>"))
            {
                contents = contents.Remove(startPos, endPos - startPos);
            }

            contents = contents.Replace("<verb/>", "");
            contents = contents.Replace("<extras/>", "");
            contents = CleanUpXmlWhitespace(contents);
            contents = contents.Replace("<testQuery></testQuery>", "");

            return contents;
        }

        private string CleanUpSSATestQuery(string contents)
        {
            int startPos = contents.IndexOf("<pos>");
            int endPos = contents.IndexOf("</pos>", startPos) + 6;
            string temp = contents.Substring(startPos, endPos - startPos);
            if (temp.Contains("<long/>") && temp.Contains("<lat/>") && temp.Contains("<refframe/>"))
            {
                contents = contents.Remove(startPos, endPos - startPos);
            }

            contents = contents.Replace("<size/>", "");
            contents = contents.Replace("<queryDataCmd/>", "");
            contents = CleanUpXmlWhitespace(contents);
            contents = contents.Replace("<testQuery></testQuery>", "");

            return contents;
        }

        private string RemoveEmptyXMLElements(string contents)
        {
            int startTag = contents.IndexOf('<');
            int endTag = 0;
            while (startTag > -1 && endTag > -1)
            {
                endTag = contents.IndexOf(">", startTag);
                if ((endTag - 1) == contents.IndexOf("/>", startTag))
                {
                    contents = contents.Remove(startTag, endTag - startTag +1);
                    startTag = contents.IndexOf('<', startTag);
                }
                else
                    startTag = contents.IndexOf('<', startTag + 1);
            }
            return contents;
        }

        private string CleanUpXmlWhitespace(string contents)
        {
            int endTag = contents.IndexOf('>');
            int nextStartTag = 0;
            while (endTag > -1 && nextStartTag > -1)
            {
                nextStartTag = contents.IndexOf('<', endTag);
                if( nextStartTag > (endTag + 1 ) )
                {
                    string temp = contents.Substring(0, nextStartTag);
                    contents = contents.Replace(temp, temp.TrimEnd(null));
                    nextStartTag = contents.IndexOf('<', endTag);
                }
                if( nextStartTag > -1 )
                    endTag = contents.IndexOf('>', nextStartTag);
            }

            return contents;
        }

        private string CleanUpCatalogTables(string contents)
        {
            //todo = clean up whitespace in not entirely empty tables?
            int start = contents.IndexOf("<catalog>");
            int end = contents.IndexOf("</catalog>", start) + 10;
            string catalog = contents.Substring(start, end - start);

            string temp = RemoveEmptyXMLElements(catalog);
            temp = CleanUpXmlWhitespace(temp);

            temp = temp.Replace("<table></table>", "");
            temp = temp.Replace("<catalog></catalog>", "");

            return contents.Replace(catalog, temp);
        }

        [WebMethod(EnableSession = true, Description = "Takes Simple Image Access Resource as XML. Posts to Registry.")]
        public RegistryResponse PostSIAResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"SimpleImageAccess", "xsi:type=\"sia:SimpleImageAccess");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());
                strmContents = CleanUpSIATestQuery(strmContents);

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");

                strmContents = ExpandResourceXML(strmContents);

                //removing some possibly empty optional parameters.
                strmContents = strmContents.Replace("<resultType/>", "");
                strmContents = strmContents.Replace("<queryType/>", "");
                strmContents = strmContents.Replace("<rights/>", "");

                strmContents = strmContents.Replace("><", ">\n<");
                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Simple Image Access Resource as XML. Posts to Registry.")]
        public RegistryResponse EditSIAResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());

                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"SimpleImageAccess", "xsi:type=\"sia:SimpleImageAccess");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());
                strmContents = CleanUpSIATestQuery(strmContents);

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");
                strmContents = ExpandResourceXML(strmContents);

                //removing some possibly empty optional parameters.
                strmContents = strmContents.Replace("<resultType/>", "");
                strmContents = strmContents.Replace("<queryType/>", "");
                strmContents = strmContents.Replace("<rights/>", "");

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Simple Spectral Access Resource as XML. Posts to Registry.")]
        public RegistryResponse PostSSAResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();

                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = strmContents.Replace("type=\"SimpleSpectralAccess", "xsi:type=\"ssa:SimpleSpectralAccess");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());
                strmContents = CleanUpSSATestQuery(strmContents);

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");

                strmContents = ExpandResourceXML(strmContents);

                //removing some possibly empty optional parameters.
                strmContents = strmContents.Replace("<resultType/>", "");
                strmContents = strmContents.Replace("<queryType/>", "");
                strmContents = strmContents.Replace("<rights/>", "");

                strmContents = strmContents.Replace("><", ">\n<");
                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        [WebMethod(EnableSession = true, Description = "Takes Simple Spectral Access Resource as XML. Posts to Registry.")]
        public RegistryResponse EditSSAResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();
                strmContents = SetupResourceNamespaces(strmContents, "vs:CatalogService");
                strmContents = CleanUpSTC(strmContents, userKey.ToString());
                strmContents = CleanUpSSATestQuery(strmContents);

                response = DisallowFalseStandardRole(strmContents);
                if (response.returncode != 0)
                    return response;
                strmContents = strmContents.Replace("genericinterface", "interface");
                strmContents = ExpandResourceXML(strmContents);

                //removing some possibly empty optional parameters.
                strmContents = strmContents.Replace("<resultType/>", "");
                strmContents = strmContents.Replace("<queryType/>", "");
                strmContents = strmContents.Replace("<rights/>", "");

                strmContents = strmContents.Replace("><", ">\n<");
                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.Message;
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }
            return response;
        }

        //fix up waveband, rights, ContentLevels, ContentTypes, etc.
        private string ExpandSelectAll(string contents, string tag)
        {

            if (contents.Contains("<" + tag + ">"))
            {
                int start = contents.IndexOf("<" + tag + ">");
                int end = contents.IndexOf("</" + tag + ">", start) + 3 + tag.Length;
                string temp = contents.Substring(start, end - start);
                if (temp.IndexOf(' ') > -1)
                {
                    string insert = String.Empty;
                    temp = temp.Replace("<" + tag + ">", "");
                    temp = temp.Replace("</" + tag + ">", " .");

                    while( temp.IndexOf(' ') > -1 )
                    {
                        insert += "<" + tag + ">" + temp.Substring(0, temp.IndexOf(' ')) + "</" + tag + ">";
                        temp = temp.Substring(temp.IndexOf(' ') + 1);
                    }
                    contents = contents.Replace(contents.Substring(start, end - start), insert);
                }
            }
            return contents;
        }
        //fix up waveband, rights, ContentLevels, ContentTypes, etc. 
        //to go BACK to the XForms style in editing.
        private string CompressSelectAll(string contents, string tag)
        {
            if (contents.Contains("<" + tag + ">"))
            {
                int start = contents.IndexOf("<" + tag + ">");
                int end = contents.LastIndexOf("</" + tag + ">", contents.Length);
                string temp = contents.Substring(start, end - start);
                temp = temp.Replace(' ', '_');
                temp = temp.Replace("</" + tag + "><" + tag + ">", " ");
                contents = contents.Replace(contents.Substring(start, end - start), temp);
            }
            return contents;
        }

        private string FindTagNamespace(string contents, string tag)
        {
            Regex testMatch = new Regex("^.*<.*:" + tag, RegexOptions.Multiline);
            string withNamespace = testMatch.Match(contents).Value;
            if (withNamespace == null || withNamespace.Length == 0)
                return String.Empty;
            
            int end = withNamespace.IndexOf(":" + tag);
            int start = withNamespace.LastIndexOf('<', end) + 1;
            string ns = withNamespace.Substring(start, end - start);

            return ns;
        }

        private string MangleSTCToXFormsExample(string contents)
        {
            String stc = FindTagNamespace(contents, "STCResourceProfile");
            if( stc.Length > 0 )
            {
                contents = contents.Replace(stc + ":STCResourceProfile", "STCResourceProfile");
                contents = contents.Replace(" xmlns:" + stc + "=\"http://www.ivoa.net/xml/STC/stc-v1.30.xsd\"", "");
                contents = contents.Replace(stc + ":AstroCoordSystem", "AstroCoordSystem");
                contents = contents.Replace(stc + ":AstroCoords", "AstroCoords");
                contents = contents.Replace(stc + ":AstroCoordArea", "AstroCoordArea");
                contents = contents.Replace(stc + ":AllSky", "AllSky");
            }

           
            //start at the top -- add whatever's missing.
            int start = contents.IndexOf("<STCResourceProfile");
            int end = 0;
            if (start > -1)
            {
                if (contents.IndexOf("<STCResourceProfile xmlns") == start)
                {
                    end = contents.IndexOf('>', start) + 1;
                    contents = contents.Remove(start, end - start).Insert(start, "<STCResourceProfile>");
                }

                if (!contents.Contains("<AstroCoordSystem"))
                {
                    start += 20;
                    contents = contents.Insert(start, "<AstroCoordSystem type=\"simple\" href=\"\" id=\"ICRS\"></AstroCoordSystem>");
                }
                else
                {
                    start = contents.IndexOf("<AstroCoordSystem ");
                    end = contents.IndexOf('>', start) + 1;
                    contents = contents.Replace(contents.Substring(start, end - start), "<AstroCoordSystem type=\"simple\" href=\"\" id=\"ICRS\"></AstroCoordSystem>");
                    while (contents.IndexOf("AstroCoordArea coord_system_id=", start) > -1)
                    {
                        int tempstart = contents.IndexOf("coord_system_id=\"", start) + 17;
                        int tempend = contents.IndexOf('\"', tempstart);
                        contents = contents.Remove(tempstart, tempend - tempstart);
                        start = contents.IndexOf('>', tempstart);

                    }
                }

                #region astrocoords
                start = contents.IndexOf("<AstroCoords");
                if( start > -1)
                {
                    start = contents.IndexOf("<Position1D>", start);
                    if( start > -1 )
                    {
                        end = contents.IndexOf("</Position1D", start);
                        if (contents.IndexOf("<Resolution", start) == -1 || contents.IndexOf("<Resolution", start) > end)
                        {
                            contents = contents.Insert(end, "<Resolution pos_unit=\"deg\"></Resolution>");
                        }
                        end = contents.IndexOf("</Position1D", start);
                        if( contents.IndexOf("<Size", start) == -1 || contents.IndexOf("<Size", start) > end )
                        {
                            contents = contents.Insert(end, "<!--regionOfRegard--><Size pos_unit=\"arcsec\"></Size>");
                        }
                    }
                    else
                    {
                        start = contents.IndexOf('>', start) + 1;
                        contents = contents.Insert(start, "<Position1D>" +
                                        "<Resolution pos_unit=\"deg\"></Resolution><!--regionOfRegard--><Size pos_unit=\"arcsec\"></Size></Position1D>");
                    }
                    end = contents.IndexOf("</AstroCoords");
                    start = contents.IndexOf("<Spectral unit=\"m\">", start);
                    if( start == -1 && start > end )
                    {
                        contents = contents.Insert(end, "<Spectral unit=\"m\"><Resolution></Resolution></Spectral>");
                    }
                }
                else
                {
                    start = contents.IndexOf("</AstroCoordSystem>") + 19;
                    contents = contents.Insert(start, "<AstroCoords coord_system_id=\"\"> <Position1D><Resolution pos_unit=\"deg\"></Resolution>" +
                                        "<!--regionOfRegard--><Size pos_unit=\"arcsec\"></Size></Position1D><Spectral unit=\"m\"><Resolution></Resolution>" +
                                        "</Spectral></AstroCoords>");
                }
                #endregion
                start = contents.IndexOf("</AstroCoords>") + 14;
                #region allsky
                if( !contents.Contains("<AllSky/>") )
                {
                    contents = contents.Insert(start, "<AstroCoordArea coord_system_id=\"\"><AllSky/></AstroCoordArea>");
                }
                #endregion
                start = contents.IndexOf("</AstroCoordArea>", start) + 17;
                #region circle
                if( !contents.Contains("<Circle>") )
                {
                    contents = contents.Insert(start, "<AstroCoordArea coord_system_id=\"\">" +
                                        "<Circle><Center unit=\"deg\"><C1></C1><C2></C2></Center><Radius pos_unit=\"deg\"></Radius></Circle></AstroCoordArea>");
                }
                #endregion
                start = contents.IndexOf("</AstroCoordArea>", start) + 17;
                #region coordrange
                if (!contents.Contains("<LoLimit2Vec>"))
                {
                    contents = contents.Insert(start, "<AstroCoordArea coord_system_id=\"\"><Position2VecInterval unit=\"deg\"><LoLimit2Vec><C1></C1><C2></C2></LoLimit2Vec>" +
                                        "<HiLimit2Vec><C1></C1><C2></C2></HiLimit2Vec></Position2VecInterval></AstroCoordArea>");
                }
                #endregion
                start = contents.IndexOf("</AstroCoordArea>", start) + 17;
                #region time
                if( !contents.Contains("<StartTime>") )
                {
                    contents = contents.Insert(start, "<AstroCoordArea coord_system_id=\"\">" +
                                        "<TimeInterval><StartTime><ISOTime></ISOTime></StartTime><StopTime><ISOTime></ISOTime></StopTime></TimeInterval></AstroCoordArea>");
                }
                #endregion
                start = contents.IndexOf("</AstroCoordArea>", start) + 17;
                #region spectral range
                if( !contents.Contains("<SpectralInterval") )
                {
                    contents = contents.Insert(start, "<AstroCoordArea coord_system_id=\"\"><SpectralInterval unit=\"m\"><LoLimit></LoLimit><HiLimit></HiLimit></SpectralInterval>" +
                                        "</AstroCoordArea>");
                }
                #endregion
            }
            else
            {
                //while it's not STC, we need coverage to put it in.
                if (contents.IndexOf("<coverage>") == -1)
                {
                    start = contents.IndexOf("</content>") + 10;
                    contents = contents.Insert(start, "<coverage><footprint ivo-id=\"\"></footprint><waveband></waveband></coverage>");
                }
                start = contents.IndexOf("<coverage>") + 10;

                contents = contents.Insert(start, "<STCResourceProfile><AstroCoordSystem type=\"simple\" href=\"\" id=\"ICRS\"></AstroCoordSystem>" +
                                        "<AstroCoords coord_system_id=\"\"> <Position1D><Resolution pos_unit=\"deg\"></Resolution>" +
                                        "<!--regionOfRegard--><Size pos_unit=\"arcsec\"></Size></Position1D><Spectral unit=\"m\"><Resolution></Resolution>" +
                                        "</Spectral></AstroCoords><AstroCoordArea coord_system_id=\"\"><AllSky/></AstroCoordArea><AstroCoordArea coord_system_id=\"\">" +
                                        "<Circle><Center unit=\"deg\"><C1></C1><C2></C2></Center><Radius pos_unit=\"deg\"></Radius></Circle></AstroCoordArea>" +
                                        "<AstroCoordArea coord_system_id=\"\"><Position2VecInterval unit=\"deg\"><LoLimit2Vec><C1></C1><C2></C2></LoLimit2Vec>" +
                                        "<HiLimit2Vec><C1></C1><C2></C2></HiLimit2Vec></Position2VecInterval></AstroCoordArea><AstroCoordArea coord_system_id=\"\">" +
                                        "<TimeInterval><StartTime><ISOTime></ISOTime></StartTime><StopTime><ISOTime></ISOTime></StopTime></TimeInterval></AstroCoordArea>" +
                                        "<AstroCoordArea coord_system_id=\"\"><SpectralInterval unit=\"m\"><LoLimit></LoLimit><HiLimit></HiLimit></SpectralInterval>" +
                                        "</AstroCoordArea></STCResourceProfile>");
            }

            return contents;
        }

        private string CleanUpSTC(string contents, string user)
        {
            //make items in STCResourceProfile <STC:...
            contents = contents.Replace("<STCResourceProfile", "<STC:STCResourceProfile xmlns:STC=\"http://www.ivoa.net/xml/STC/stc-v1.30.xsd\"");
            contents = contents.Replace("</STCResourceProfile", "</STC:STCResourceProfile");
            contents = contents.Replace("AstroCoordSystem", "STC:AstroCoordSystem");
            contents = contents.Replace("AstroCoords", "STC:AstroCoords");
            contents = contents.Replace("AstroCoordArea", "STC:AstroCoordArea");
            contents = contents.Replace("AllSky", "STC:AllSky");

            //clean up  empty and redundant STC items (also AllSky, circle, etc)
            int start = 0;
            int end = 0;
            if (contents.Contains("<LoLimit/>") && contents.Contains("<HiLimit/>"))
            {
                //remove empty spectral interval.
                start = contents.IndexOf("<SpectralInterval");
                end = contents.IndexOf("</Spec", start) + 19;
                contents = contents.Remove(start, end - start);
            }
            
            start = contents.IndexOf("<StartTime>");
            end =  contents.IndexOf("</StartTime>", start) + 12;
            int time = contents.IndexOf("<ISOTime>", start ) + 9;
            DateTime startDate = DateTime.MinValue;
            DateTime endDate = DateTime.MinValue;
            if (time > 8 && time < end)
            {
                int endTime = contents.IndexOf("</ISO", time);
                string tempTime = contents.Substring(time, endTime - time);
                try
                {
                    int year = Convert.ToInt32(tempTime.Substring(0, 4));
                    int month = 1;
                    int day = 1;

                    if (tempTime.Length > 4)
                    {
                        month = Convert.ToInt32(tempTime.Substring(5, 2));
                        day = Convert.ToInt32(tempTime.Substring(8, 2));
                    }
                    if (tempTime.Length > 10)
                    {
                        int hour = Convert.ToInt32(tempTime.Substring(11, 2));
                        int min = Convert.ToInt32(tempTime.Substring(14, 2));
                        int sec = Convert.ToInt32(tempTime.Substring(17, 2));
                        startDate = new DateTime(year, month, day, hour, min, sec);
                        contents = contents.Remove(time, endTime - time).Insert(time, STOAI.GetOAIDatestamp(startDate, granularityType.YYYYMMDDThhmmssZ));
                    }
                    else
                    {
                        startDate = new DateTime(year, month, day);
                        contents = contents.Remove(time, endTime - time).Insert(time, STOAI.GetOAIDatestamp(startDate, granularityType.YYYYMMDD));
                    }
                }
                catch (Exception)
                {
                    throw new Exception("Submission Error. Start time must be in one of the following formats: YYYY or YYYY-MM-DD or YYYY-MM-DDThh:mm:ssZ.");
                }
            }
            else //empty
            {
                contents = contents.Remove(start, end - start);
            }

            start = contents.IndexOf("<StopTime>");
            end = contents.IndexOf("</StopTime>", start) + 11;
            time = contents.IndexOf("<ISOTime>", start) + 9;
            if (time  > 8 && time < end)
            {
                int endTime = contents.IndexOf("</ISO", time);
                string tempTime = contents.Substring(time, endTime - time);
                try
                {
                    int year = Convert.ToInt32(tempTime.Substring(0, 4));
                    int month = 1;
                    int day = 1;
                    if (tempTime.Length > 4)
                    {
                        month = Convert.ToInt32(tempTime.Substring(5, 2));
                        day = Convert.ToInt32(tempTime.Substring(8, 2));
                    }
                    if (tempTime.Length > 10)
                    {
                        int hour = Convert.ToInt32(tempTime.Substring(11, 2));
                        int min = Convert.ToInt32(tempTime.Substring(14, 2));
                        int sec = Convert.ToInt32(tempTime.Substring(17, 2));
                        endDate = new DateTime(year, month, day, hour, min, sec);
                        contents = contents.Remove(time, endTime - time).Insert(time, STOAI.GetOAIDatestamp(endDate, granularityType.YYYYMMDDThhmmssZ));
                    }
                    else
                    {
                        endDate = new DateTime(year, month, day);
                        contents = contents.Remove(time, endTime - time).Insert(time, STOAI.GetOAIDatestamp(endDate, granularityType.YYYYMMDD)); 
                    }
                }
                catch (Exception)
                {
                    throw new Exception("Submission Error. End time must be in one of the following formats: YYYY or YYYY-MM-DD or YYYY-MM-DDThh:mm:ssZ."); 
                }
            }
            else //empty
            {
                contents = contents.Remove(start, end - start);
            }
            if ( (startDate > DateTime.MinValue && endDate == DateTime.MinValue) ||
                 (startDate == DateTime.MinValue && endDate > DateTime.MinValue) )
            {
                throw new Exception("Submission Error. You must enter both start and stop time, or neither.");
            }
            if (startDate > endDate)
            {
                throw new Exception("Submission Error. If entered, start time must be before or the same as end time.");
            }


            contents = contents.Replace("<Radius pos_unit=\"deg\"/>", "");
            if (contents.Contains("<Size pos_unit=\"deg\"/>") || contents.Contains("<Size pos_unit=\"arcsec\"/>"))
            {
                contents = contents.Replace("<!--regionOfRegard-->", "");
                contents = contents.Replace("<Size pos_unit=\"arcsec\"/>", "");
                contents = contents.Replace("<Size pos_unit=\"deg\"/>", "");
            }
            contents = contents.Replace("<Resolution/>", "");
            contents = contents.Replace("<Resolution pos_unit=\"deg\"></Resolution>", "");
            contents = contents.Replace("<Resolution pos_unit=\"deg\"/>", "");

            //remove all empty areas.
            contents = CleanUpXmlWhitespace(contents);
            contents = contents.Replace("<LoLimit2Vec></LoLimit2Vec>", "");
            contents = contents.Replace("<LoLimit2Vec/>", "");
            contents = contents.Replace("<HiLimit2Vec></HiLimit2Vec>", "");
            contents = contents.Replace("<HiLimit2Vec/>", "");
            contents = contents.Replace("<Center unit=\"deg\"></Center>", "");
            contents = contents.Replace("<Center unit=\"deg\"/>", "");
            contents = contents.Replace("<Spectral unit=\"m\"></Spectral>", "");
            contents = contents.Replace("<TimeInterval></TimeInterval>", "");


            contents = contents.Replace("<Position2VecInterval unit=\"deg\"></Position2VecInterval>", "");
            contents = contents.Replace("<Circle></Circle>", "");
            contents = contents.Replace("<Position1D><!--regionOfRegard--></Position1D>", "");
            contents = contents.Replace("<Position1D></Position1D>", "");
            contents = contents.Replace("<STC:AstroCoordArea coord_system_id=\"\"></STC:AstroCoordArea>", "");
            contents = contents.Replace("<STC:AstroCoordArea coord_system_id=\"\"/>", "");
            contents = contents.Replace("<STC:AstroCoords coord_system_id=\"\"></STC:AstroCoords>", "");

            if( !contents.Contains("<STC:AstroCoords") && !contents.Contains("<STC:AstroCoordArea") )
                contents = contents.Replace("<STC:AstroCoordSystem type=\"simple\" href=\"\" id=\"ICRS\"/>", "");

            //remove entire STC profile if empty or consolidate non-empty areas.
            if (contents.Contains("<STC:AstroCoordArea") == false && contents.Contains("<STC:AstroCoords") == false)
            {
                start = contents.IndexOf("<STC:STCResourceProfile");
                end = contents.IndexOf("</STC:STCResourceProfile>") + 25;
                contents = contents.Remove(start, end - start);
            }
            else
            {
                contents = contents.Replace("type=\"simple\"", "xlink:type=\"simple\"");

                //generate coord system ID and apply to all remaining STC tags.
                start = contents.IndexOf("<identifier>") + 12;
                end = contents.IndexOf("</", start);
                string temp = GetAuthIdentifier(user) + "_" + contents.Substring(start, end - start);

                string smallid = "ICRS";
                start = contents.IndexOf("id=\"", contents.IndexOf("<STC:AstroCoordSystem")) + 4;
                end = contents.IndexOf("\"", start);
                if( end > (start) )
                    smallid = contents.Substring(start, end - start);

                string id = temp.Substring(6).Replace('/', '_') + "_UTC-" + smallid + "-TOPO";

                contents = contents.Remove(start, end - start).Insert(start, id);
                contents = contents.Replace("coord_system_id=\"\"", "coord_system_id=\"" + id + "\"");

                start = contents.IndexOf("href=\"\"", contents.IndexOf("<STC:AstroCoordSystem"));
                contents = contents.Remove(start, 7).Insert(start, "xlink:href=\"ivo://STClib/CoordSys#UTC-" + smallid + "-TOPO\"");
            }

            return contents;
        }

        private string CreateDates(string contents)
        {
            String strdate = STOAI.GetOAIDatestamp(DateTime.Now, granularityType.YYYYMMDDThhmmssZ);
            contents = contents.Replace("created=\"1970-01-01\"", "created=\"" + strdate + "\"");
            contents = contents.Replace("updated=\"1970-01-01\"", "updated=\"" + strdate + "\"");
            return contents;
        }

        private string SetupResourceNamespaces(string contents, string xsiType)
        {
            contents = contents.Replace("<Resource", "<ri:Resource");
            contents = contents.Replace("</Resource", "</ri:Resource");
            contents = contents.Replace("xmlns=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\"", "xmlns=\"\"");
            contents = contents.Replace("XMLSchema-instance\"", "XMLSchema-instance\" xsi:type=\"" + xsiType + "\" ");

            contents = contents.Replace("type=\"SimpleSpectralAccess", "xsi:type=\"ssa:SimpleSpectralAccess");
            contents = contents.Replace("type=\"ConeSearch", "xsi:type=\"cs:ConeSearch");

            //set interface type to xsi:type in xml, also vr:WebBrowser and WebService, vs:ParamHTTP
            if (contents.Contains("type=\"WebBrowser"))
                contents = contents.Replace("type=\"WebBrowser", "xsi:type=\"vr:WebBrowser");
            if (contents.Contains("type=\"WebService"))
                contents = contents.Replace("type=\"WebService", "xsi:type=\"vr:WebService");
            if (contents.Contains("type=\"ParamHTTP"))
                contents = contents.Replace("type=\"ParamHTTP", "xsi:type=\"vs:ParamHTTP");

            //make the header xml namespaces match what we use
            //if we were editing a resource not created by this interface, they might not
            contents = Regex.Replace(contents, "xmlns:..?.?=\"http://www.ivoa.net/xml/VOResource/v1.0\"", "xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v1.0\"");
            contents = Regex.Replace(contents, "xmlns:..?.?=\"http://www.ivoa.net/xml/VORegistry/v1.0\"", "xmlns:vg=\"http://www.ivoa.net/xml/VORegistry/v1.0\"");
            contents = Regex.Replace(contents, "xmlns:..?.?=\"http://www.ivoa.net/xml/VODataService/v1.0\"", "xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.0\"");
            contents = Regex.Replace(contents, "xmlns:..?.?=\"http://www.ivoa.net/xml/ConeSearch/v1.0\"", "xmlns:cs=\"http://www.ivoa.net/xml/ConeSearch/v1.0\"");
            contents = Regex.Replace(contents, "xmlns:..?.?=\"http://www.ivoa.net/xml/SIA/v1.0\"", "xmlns:sia=\"http://www.ivoa.net/xml/SIA/v1.0\"");
            contents = Regex.Replace(contents, "xmlns:..?.?=\"http://www.ivoa.net/xml/SSA/v1.0\"", "xmlns:sia=\"http://www.ivoa.net/xml/SSA/v1.0\"");

            return contents;
        }

        private RegistryResponse CreateEmptyForm(string xsi_type, string[] capabilityTypes)
        {
            RegistryResponse resp = new RegistryResponse();


            return resp;
        }

        [WebMethod(EnableSession = true)]
        //this is intended to be generic, based on the info in the start page's statedata.
        // it's a step in allowing multiple capabilities and interfaces in the publishing interface.
        public RegistryResponse CreateNewFormResource(string userKey, string dalTypes, string type)
        {
            RegistryResponse response = new RegistryResponse(0, string.Empty);

            //only allow local requests
            if (Context.Request.IsLocal == false)
                return new RegistryResponse(1, "Resource creation only permitted from the publishing interface.");

            try
            {
                //long userKey = (long)Session["UserKey"];
                //String statedata = GetRequestString();

                string tempResourceContents, tempFormContents = String.Empty;
                response = ReadTextFile("emptyResource.xml", out tempResourceContents);
                if (response.returncode != 0)
                    return response;
                else
                    AdjustSampleResource(dalTypes, type, ref tempResourceContents);


                response = ReadTextFile("createresourcetemplate.xhtml", out tempFormContents);
                if (response.returncode != 0)
                    return response;
                else
                    AdjustSampleForm(dalTypes, type, userKey + "_tempResource.xml", ref tempFormContents);

                response = CreateTempFile(userKey + "_tempResource.xml", tempResourceContents);
                if (response.returncode != 0)
                    return response;

                response = CreateTempFile(userKey + "_createResource.xhtml", tempFormContents);
                if( response.returncode == 0 )
                    response.message = userKey + "_createResource.xhtml";
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            return response;
        }


        //the point here being to add various capabilities and interfaces as needed, to both 
        //the example resource and the form.
        private void AdjustSampleResource(string dalTypes, string type, ref string resourceContents)
        {
            string strAdditionalElements;
            if (type == "vr:Organisation")
            {
                resourceContents = SetXmlValue("type", resourceContents, "Organisation");
            }
            else if (type.StartsWith("vg:Authority"))
            {
                resourceContents = SetXmlValue("type", resourceContents, "Organisation");
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), "<managingOrg></managingOrg>");
            }
            else if (type.Contains("DataCollection"))
            {
                resourceContents = SetXmlValue("type", resourceContents, "Other");
                ReadTextFile("emptyCoverage.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                ReadTextFile("emptyrightsetc.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                ReadTextFile("emptyCatalog.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
            }
            else if (type.Contains(":Service"))
            {
                resourceContents = SetXmlValue("type", resourceContents, "Other");
                ReadTextFile("emptyCapability.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                ReadTextFile("emptyrightsetc.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
            }
            else if (type.Contains("DataService"))
            {
                resourceContents = SetXmlValue("type", resourceContents, "Other");
                ReadTextFile("emptyCoverage.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                ReadTextFile("emptyrightsetc.xml", out strAdditionalElements);
                resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);

                //todo - properly allow more than one, and more than one interface per.
                if (dalTypes.Length == 0)
                {
                    ReadTextFile("emptyCapability.xml", out strAdditionalElements);
                    resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                    ReadTextFile("emptyrightsetc.xml", out strAdditionalElements);
                    resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                }
                else
                {
                    if (dalTypes.Contains("csc"))
                    {
                        ReadTextFile("emptyconesearch.xml", out strAdditionalElements);
                        resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                    }
                    if (dalTypes.Contains("sia"))
                    {
                        ReadTextFile("emptysia.xml", out strAdditionalElements);
                        resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                    }
                    if (dalTypes.Contains("ssa"))
                    {
                        ReadTextFile("emptyssa.xml", out strAdditionalElements);
                        resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                    }
                    if (dalTypes.Contains("OpenSkyNode"))
                    {
                        ReadTextFile("emptyskynode.xml", out strAdditionalElements);
                        resourceContents = resourceContents.Insert(resourceContents.IndexOf("</Resource>"), strAdditionalElements);
                    }
                }
            }
        }

        private string RemoveTab(string title, string form)
        {
            int index = form.IndexOf(title);
            while (index > -1)
            {
                index = form.LastIndexOf("<td", index);
                int end = form.IndexOf('>', form.IndexOf("</td", index)) + 1;

                //also get rid of extraneous spacers.
                if( form.LastIndexOf("<td width=\"3\"", index - 1) == form.LastIndexOf("<td", index - 1 ))
                    index = form.LastIndexOf("<td", index - 1);
                form = form.Remove(index, end - index);
                index = form.IndexOf(title);
            }
            return form;
        }

        private string AddFormCapability(string type, string form)
        {
             int index = form.IndexOf("<!--" + type);
             if (index != -1)
             {
                 index = form.IndexOf("<!--end", index);
                 string tempform = string.Empty;
                 ReadTextFile(type + "formcapability.xml", out tempform);
                 form = form.Insert(index, tempform);
             }

             return form;
        }
        private string AddGroupForCapability(string type, string form)
        {
            int index = form.IndexOf("<!--tab for " + type);
            if (index != -1)
            {
                index = form.IndexOf("<!--end", index);
                string tempform = string.Empty;
                ReadTextFile(type + "formtabcapability.xml", out tempform);
                form = form.Insert(index, tempform);
            }

            return form;
        }

        //intention is to work from createresource.xhtml 
        private void AdjustSampleForm(string dalTypes, string type, string emptyResourceFile, ref string formContents)
        {
            if (type == "vr:Organisation")
            {
                formContents = RemoveTab("Coverage", formContents);
                formContents = RemoveTab("Collection Table", formContents);
                formContents = RemoveTab("Capabilities", formContents);
                formContents = formContents.Replace("PostResource", "PostBaseResourceOrganisation");
            }
            else if (type.StartsWith("vg:Authority"))
            {
                formContents = RemoveTab("Coverage", formContents);
                formContents = RemoveTab("Collection Table", formContents);
                formContents = RemoveTab("Capabilities", formContents);
                formContents = formContents.Replace("PostResource", "PostAuthorityResource");
            }
            else if (type.Contains("DataCollection"))
            {
                formContents = RemoveTab("Capabilities", formContents);
                formContents = formContents.Replace("PostResource", "PostDataCollectionResource");
            }
            else if (type.Contains(":Service"))
            {
                formContents = RemoveTab("Coverage", formContents);
                formContents = RemoveTab("Collection Table", formContents);
                formContents = RemoveTab("Cone Search", formContents);
                formContents = RemoveTab("SIA Capabilities", formContents);
                formContents = RemoveTab("SSA Capabilities", formContents);
                formContents = RemoveTab("Sky Node Capabilities", formContents);
                formContents = AddFormCapability("generic", formContents);
                formContents = AddGroupForCapability("generic", formContents);
                formContents = formContents.Replace("PostResource", "PostServiceResource");
            }
            else if (type.Contains("DataService"))
            {
                //todo - we will be allowing > 1, and multiple interfaces
                formContents = RemoveTab("Collection Table", formContents);
                
                if (dalTypes.Length == 0)
                {
                    formContents = RemoveTab("Cone Search", formContents);
                    formContents = RemoveTab("SIA Capabilities", formContents);
                    formContents = RemoveTab("SSA Capabilities", formContents);
                    formContents = RemoveTab("Sky Node Capabilities", formContents);
                    formContents = formContents.Replace("PostResource", "PostDataServiceResource");
                    formContents = AddFormCapability("generic", formContents);
                    formContents = AddGroupForCapability("generic", formContents);
                }
                if (dalTypes.Contains("csc"))
                {
                    formContents = RemoveTab("Capabilities (Optional)", formContents);
                    formContents = RemoveTab("SIA Capabilities", formContents);
                    formContents = RemoveTab("SSA Capabilities", formContents);
                    formContents = RemoveTab("Sky Node Capabilities", formContents);
                    formContents = AddFormCapability("conesearch", formContents);
                    formContents = AddGroupForCapability("conesearch", formContents);

                    formContents = formContents.Replace("PostResource", "PostConeSearchResource");
                }
                if (dalTypes.Contains("sia"))
                {
                    formContents = RemoveTab("Capabilities (Optional)", formContents);
                    formContents = RemoveTab("Cone Search Capabilities", formContents);
                    formContents = RemoveTab("SSA Capabilities", formContents);
                    formContents = RemoveTab("Sky Node Capabilities", formContents);
                    formContents = AddFormCapability("sia", formContents);
                    formContents = AddGroupForCapability("sia", formContents);

                    formContents = formContents.Replace("PostResource", "PostSIAResource");
                }
                if (dalTypes.Contains("ssa"))
                {
                    formContents = RemoveTab("Capabilities (Optional)", formContents);
                    formContents = RemoveTab("Cone Search Capabilities", formContents);
                    formContents = RemoveTab("SIA Capabilities", formContents);
                    formContents = RemoveTab("Sky Node Capabilities", formContents);
                    formContents = AddFormCapability("ssa", formContents);
                    formContents = AddGroupForCapability("ssa", formContents);

                    formContents = formContents.Replace("PostResource", "PostSSAResource");
                }
                if (dalTypes.Contains("SkyNode"))
                {
                    formContents = RemoveTab("Capabilities (Optional)", formContents);
                    formContents = RemoveTab("Cone Search Capabilities", formContents);
                    formContents = RemoveTab("SIA Capabilities", formContents);
                    formContents = RemoveTab("SSA Capabilities", formContents);
                    formContents = AddFormCapability("openskynode", formContents);
                    formContents = AddGroupForCapability("openskynode", formContents);

                    formContents = formContents.Replace("PostResource", "PostOpenSkyNodeResource");
                }
            }

            formContents = formContents.Replace("example.xml", emptyResourceFile);
        }

        public static string GetXmlValue(string tag, string strmContents)
        {
            int start = strmContents.IndexOf('<' + tag + '>');
            if( start > 0 )
            {
                start = strmContents.IndexOf('>', start) + 1;
                int end = strmContents.IndexOf('<', start);

                return strmContents.Substring(start, end - start);
            }
            else
                return string.Empty;

            return string.Empty;
        }

        public static string SetXmlValue(string tag, string strmContents, string value)
        {
            int start = strmContents.IndexOf('<' + tag + '>');
            if (start > 0)
            {
                start = strmContents.IndexOf('>', start) + 1;
                int end = strmContents.IndexOf('<', start);

                if( end > (start + 1) )
                    strmContents = strmContents.Remove(start, end - start);
                strmContents = strmContents.Insert(start, value);
            }
            else
                return strmContents;

            return strmContents;
        }


        [WebMethod(EnableSession = true, Description = "Takes a basic Resource as XML. Posts to Registry.")]
        public RegistryResponse PostBaseResourceOrganisation()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"]; 
            String xml = String.Empty;
            try
            {
                String strmContents = CreateDates(GetRequestString());
                strmContents = SetupResourceNamespaces(strmContents, "vr:Organisation");

                strmContents = ExpandSelectAll(strmContents, "type");
                strmContents = ExpandSelectAll(strmContents, "contentLevel");
                int start = strmContents.IndexOf("<contentLevel>");
                int end = strmContents.LastIndexOf("</contentLevel>");
                if (strmContents.IndexOf('_', start) < end && strmContents.IndexOf('_', start) > -1)
                {
                    string spaces = strmContents.Substring(start, end - start).Replace('_', ' ');
                    strmContents = strmContents.Replace(strmContents.Substring(start, end - start), spaces);
                }


                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = CreatePendingFile(newID, xml, userKey);
            }
            else
            {
                response = CreateIdentifierError();
            }

            return response;
        }

        private RegistryResponse CreateIdentifierError()
        {
           RegistryResponse resp = new RegistryResponse(1, "Error creating ivo identifier from resource key. Please ensure that your resource" +
                       " does not already exist in the registry and try another resource key. If you have entered an entire" +
                       " ivo ID as your resource key, the ID must begin with the publishing authority with which you are " +
                       "currently associated.");
           return resp;
        }

        [WebMethod(EnableSession = true, Description = "Takes a basic Resource as XML. Posts to Registry.")]
        public RegistryResponse EditBaseResourceOrganisation()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                String strmContents = GetRequestString();

                strmContents = SetupResourceNamespaces(strmContents, "vr:Organisation");
                strmContents = strmContents.Replace("\n", String.Empty);

                strmContents = ExpandSelectAll(strmContents, "type");
                strmContents = ExpandSelectAll(strmContents, "contentLevel");
                int start = strmContents.IndexOf("<contentLevel>");
                int end = strmContents.LastIndexOf("</contentLevel>");
                if (start > -1 &&  strmContents.IndexOf('_', start) < end && strmContents.IndexOf('_', start) > -1)
                {
                    string spaces = strmContents.Substring(start, end - start).Replace('_', ' ');
                    strmContents = strmContents.Replace(strmContents.Substring(start, end - start), spaces);
                }

                xml = strmContents;
            }
            catch (Exception e)
            {
                response.returncode = -1;
                response.message = e.ToString();
                return response;
            }

            int startID = xml.IndexOf("<identifier>") + 12;
            String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

            string newID = CreateIdentifier(identifier);
            if (newID.Length > 0)
            {
                if( newID != identifier)
                xml = xml.Remove(startID, xml.IndexOf("</", startID) - startID).Insert(startID, newID);
                response = SaveResourceEdits(newID, xml, userKey);
            }
            else
            {
                response = new RegistryResponse(1, "Error editing pending resource file.");
            }

            return response;
        }


        private RegistryResponse XSLValidate(String xml)
        {
            RegistryResponse response = new RegistryResponse(0, String.Empty);
            try
            {
                //MSXML helpfully removes our "xlink" namespace under certain circumstances,
                //but we need it for the transform.
                string testXml = xml;
                if (xml.Contains("xmlns:xlink=\"http://www.w3.org/1999/xlink\"") == false)
                {
                    int insert = xml.IndexOf("xmlns:");
                    testXml = xml.Insert(insert, "xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
                }
                //tdower todo - regex to get namespaces for STC stuff.
                if (testXml.Contains("STCResourceProfile") == true &&
                    testXml.Contains("xmlns:stc=\"http://www.ivoa.net/xml/STC/stc-v1.30.xsd\"") == false)
                {
                    int insert = testXml.IndexOf("xmlns:");
                    testXml = testXml.Insert(insert, "xmlns:stc=\"http://www.ivoa.net/xml/STC/stc-v1.30.xsd\"\n");
                }

                StringReader reader = new StringReader(testXml);
                XmlTextReader xreader = new XmlTextReader(reader);
                XslCompiledTransform myXslTrans = new XslCompiledTransform();

                //load the Xsl
                StringWriter sw = new StringWriter();
                XmlTextWriter xwriter = new XmlTextWriter(sw);

                myXslTrans.Load(baseURL + "xsl/testsVOResource-v1_0.xsl");

                myXslTrans.Transform(xreader, xwriter);

                String test = sw.ToString().Trim();
                if (test.Length > 0)
                {
                    response.returncode = 1;
                    response.message = "Resource failed automatic validation.\n";
                    int startMessage = test.IndexOf('>') + 1;
                    int endMessage = 0;
                    while (startMessage > 0)
                    {
                        endMessage = test.IndexOf('<', startMessage);
                        if (endMessage > -1)
                            response.message += "    " + test.Substring(startMessage, endMessage - startMessage) + '\n';
                        startMessage = test.IndexOf('>', startMessage) + 1;
                    }
                }
            }
            catch( Exception e)
            {
                response.returncode = 1;
                response.message = "Error validating XML VOResource: " + e.Message;
            }

            return response;

        }


        private RegistryResponse CreatePendingFile(String fullID, String xml, long userKey)
        {
            return CreatePendingFile(fullID, xml, userKey, false);
        }

        private RegistryResponse CreatePendingFile(String fullID, String xml, long userKey, bool edit)
        {
            RegistryResponse response = new RegistryResponse(0, String.Empty);
            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                bool unique = CheckForUniqueIVOID(fullID, conn);
                if (edit == false && unique == false)
                {
                    response.returncode = 1;
                    response.message = "IVO Identifier already exists. Please ensure that your resource" +
                                       " does not already exist in the registry and try another resource key.";
                }
                else
                {
                    bool validauth = CheckForUsersAuthority(fullID, userKey, conn);
                    if (edit == false && validauth == false) //let them edit old ones they may have got anyway?
                    {
                        response.returncode = 1;
                        response.message = "IVO Identifier does not begin with an authority associated with this user. ";
                    }
                    else
                    {
                        response = XSLValidate(xml);
                        if (response.returncode != 0)
                            return response;

                        string idfile = GetIDFilename(fullID, userKey);
                        if (System.IO.File.Exists(idfile) && edit == false)
                        {
                            response.returncode = 1;
                            response.message = "IVO Identifier already exists as a pending resource. Please try another resource key.";
                        }
                        else
                        {
                            System.IO.StreamWriter sw = null;
                            try
                            {
                                sw = new System.IO.StreamWriter(idfile, false);
                                if (sw != null)
                                {
                                    sw.Write(xml);
                                    sw.Close();
                                }
                            }
                            catch (Exception e)
                            {
                                response.returncode = 1;
                                if (edit)
                                    response.message = "Error saving pending resource file. Log out and try again.";
                                else
                                    response.message = "IVO Identifier already exists as a pending resource. Please try another resource key.";
                            }
                            finally
                            {
                                sw = null;
                            }
                        }
                    }
                }
            }
            finally
            {
                conn.Close();
            }

            return response;
        }

        public static string GetIDFilename(String identifier, long userKey)
        {
            string idTag = identifier.Replace('/', ',');
            if( idTag.StartsWith("ivo:") )
                idTag = idTag.Substring(idTag.LastIndexOf(':') + 3);

           return location + 
               "\\publish_temp\\" + 
               userKey + "," +
               idTag + ".xml";
        }

        [WebMethod(EnableSession = true)]
        public RegistryResponse SubmitXMLEditedResource()
        {
           RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                 return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                xml = GetRequestString();
                xml = xml.Replace("&lt;", "<");
                xml = xml.Replace("&gt;", ">");

                if (xml.StartsWith("<?xml"))
                    xml = xml.Substring(xml.IndexOf('>') + 1).Trim();
                
                //remove dummy "resource" tag
                int tag = xml.IndexOf("<resource>");
                xml = xml.Remove(tag, 10);
                tag = xml.LastIndexOf("</resource>");
                xml = xml.Remove(tag, 11);

                int startID = xml.IndexOf("<identifier>") + 12;
                String identifier = xml.Substring(startID, xml.IndexOf("</", startID) - startID);

                //todo - check for changed identifier somehow?
                response = SaveResourceEdits(identifier, xml, userKey);
            }
            catch (Exception e)
            {
                response.message = e.Message;
                response.returncode = 1;
            }

            return response;
        }

        [WebMethod(EnableSession = true)]
        public RegistryResponse SubmitXMLResource()
        {
            RegistryResponse response = CanPublishNew();
            if (response.returncode != 0)
                 return response;

            long userKey = (long)Session["UserKey"];
            String xml = String.Empty;
            try
            {
                xml = GetRequestString();
                int start = xml.IndexOf(">", xml.IndexOf("<file")) + 1;
                int end = xml.IndexOf("</file>", start);
                string file = xml.Substring(start, end - start);

                byte[] byteFile = Convert.FromBase64String(file);
                string textfile = Encoding.UTF8.GetString(byteFile);
                textfile = textfile.Substring(textfile.IndexOf('<'));

                start = textfile.IndexOf("<identifier>") + 12;
                String identifier = textfile.Substring(start, textfile.IndexOf("</", start) - start);
                string auth = GetAuthIdentifier(userKey.ToString());
                if (!identifier.StartsWith(auth + '/'))
                {
                    response.returncode = 1;
                    response.message = "Error submitting resource: Identifier must begin with user authority id \"" + auth + "\"";
                    return response;
                }
                response = CreatePendingFile(identifier, textfile, userKey);
            }
            catch (Exception e)
            {
                response.message = e.Message;
                response.returncode = 1;
            }

            return response;
        }

        [WebMethod]
        public RegistryResponse DeleteResource(String identifier, String user)
        {
            //only allow local requests
            if (Context.Request.IsLocal == false)
                return new RegistryResponse(1, "Resource deletion only permitted from the publishing interface.");

            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            if (resp.returncode == 0)
            {
                long userKey = Convert.ToInt64(user);
                try
                {
                    String pendingFile = GetIDFilename(identifier, userKey);
                    if (File.Exists(pendingFile))
                    {
                        File.Delete(pendingFile);
                    }
                    else
                    {
                        if (GetAuthIdentifier(user) == identifier)
                        {
                            resp.returncode = 0;
                            resp.message = "Cannot delete your own authority record.";
                            return resp;
                        }

                        SqlConnection conn = null;
                        try
                        {
                           conn = new SqlConnection(sConnect);
                           conn.Open();

                           SqlCommand sc = SQLHelper.getGetDeleteResourceCmd(conn);

                           sc.Parameters["@Identifier"].Value = identifier;
                           sc.Parameters["@PassPhrase"].Value = dbAdmin;

                           int nobjs = sc.ExecuteNonQuery();
                           if (nobjs == 0)
                           {
                               resp.returncode = 1;
                               resp.message = "No resource exists with the identifier " + identifier;
                           }

                        }
                        finally
                        {
                            if (conn != null)
                                conn.Close();
                        }

                    }
                }
                catch (Exception e)
                {
                    resp.returncode = 1;
                    resp.message = e.Message;
                }
            }

            return resp;

        }

        private string MangleBackToXFormsExample(string contents, string user)
        {
            int start = 0;
            int end = 0;

            contents = contents.Remove(0, contents.IndexOf("?>") + 2);
            string riNamespace = FindTagNamespace(contents, "Resource");
            contents = contents.Replace("<" + riNamespace + ":Resource", "<Resource");
            contents = contents.Replace("</" + riNamespace + ":Resource", "</Resource");

            #region type-specific empty fields.
            Regex rxAnyService = new Regex("xsi:type=..?.?:.*Service");
            if( rxAnyService.Match(contents).Length > 0 )
            {
                if (contents.IndexOf("<rights") == -1)
                    contents = contents.Insert(contents.IndexOf("</Resource>"), "<rights></rights>");
                if (contents.IndexOf("<facility") == -1)
                    contents = contents.Insert(contents.IndexOf("</Resource>"), "<facility ivo-id=\"\"></facility>");
                contents = contents.Replace("<facility>", "<facility ivo-id=\"\">");
                if (contents.IndexOf("<instrument") == -1)
                    contents = contents.Insert(contents.IndexOf("</Resource>"), "<instrument ivo-id=\"\"></instrument>");
                contents = contents.Replace("<instrument>", "<instrument ivo-id=\"\">");
                contents = contents.Replace("<footprint/>", "<footprint ivo-id=\"\"></footprint>");

                int indexiface = contents.IndexOf("<interface");
                while(indexiface > -1)
                {
                    int endiface = contents.IndexOf(">", indexiface);
                    if (contents.IndexOf("version", indexiface) == -1 || contents.IndexOf("version", indexiface) > endiface)
                        contents = contents.Insert(endiface, " version=\"\" ");
                    indexiface = contents.IndexOf("<interface", indexiface + 1);
                }

                start = contents.IndexOf("<coverage");
                if (start > -1 && contents.IndexOf("<footprint", start) == -1)
                {
                    contents = contents.Insert(contents.IndexOf("</coverage>"), "<footprint ivo-id=\"\"></footprint>");
                }

                start = contents.IndexOf("<capability");
                int test;
                if (start == -1)
                {
                    contents = contents.Insert(contents.IndexOf("<rights"),
                        "<capability standardID=\"\">" +
                        "<interface version=\"1.0\" role=\"\" type=\"\"><accessURL use=\"base\"></accessURL></interface>" +
                        "<validationLevel validatedBy=\"ivo://archive.stsci.edu/nvoregistry\">1</validationLevel>" +
                        "<description></description></capability>");
                }
                else
                {
                    end = contents.IndexOf('>', start);
                    if (contents.IndexOf("standardID", start, end - start) == -1)
                        contents = contents.Insert(end, " standardID=\"\"");

                    end = contents.IndexOf("</capability", start);
                    if (contents.IndexOf("<description", start, end - start) == -1)
                    {
                        if (contents.IndexOf("</validationLevel>", start) > -1)
                            contents = contents.Insert(contents.IndexOf("</validationLevel>", start) + 18, "<description></description>");
                        else
                            contents = contents.Insert(contents.IndexOf('>', start) + 1, "<description></description>");
                    }
                    end = contents.IndexOf("</capability", start);

                    if (contents.IndexOf("<interface", start, end - start) > -1 &&
                        contents.IndexOf("role=", start, end - start) == -1)
                    {
                        end = contents.IndexOf('>', contents.IndexOf("<interface", start));
                        contents = contents.Insert(end, " role=\"\"");
                    }
                }

                //etc
                if (start > -1)
                {
                    if (contents.IndexOf("ConeSearch", start, end - start) > -1 ||
                        contents.IndexOf("SimpleImageAccess", start, end - start) > -1 ||
                        contents.IndexOf("SimpleSpectralAccess", start, end - start) > -1)
                    {
                        if (contents.IndexOf("<queryType") == -1)
                            contents = contents.Insert(contents.IndexOf("</interface>"), "<queryType></queryType>");
                        if (contents.IndexOf("<resultType") == -1)
                            contents = contents.Insert(contents.IndexOf("</interface>"), "<resultType></resultType>");
                    }

                    if (contents.IndexOf("SimpleSpectralAccess", start, end - start) > -1)
                    {
                        test = contents.IndexOf("<testQuery>");
                        if (test == -1)
                        {
                            contents = contents.Insert(contents.IndexOf("</capability>"), "<testQuery><pos><long></long><lat></lat><refframe></refframe></pos>" +
                                "<size></size><queryDataCmd></queryDataCmd></testQuery>");
                        }
                        else
                        {
                            if (contents.IndexOf("<queryDataCmd", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</testQuery>", test), "<queryDataCmd></queryDataCmd>");
                            if (contents.IndexOf("<size", test) == -1)
                                contents = contents.Insert(contents.IndexOf("<queryDataCmd>", test), "<size></size>");
                            if (contents.IndexOf("<pos", test) == -1)
                                contents = contents.Insert(contents.IndexOf("<size>", test), "<pos><long></long><lat></lat><refframe></refframe></pos>");
                            //todo - is this okay? i think so. otherwise, fill out pos in else.
                        }

                        contents = InsertVersionResultType(contents);
                        contents = MangleInterfacesBackToXForms("ssa", contents);
                    }
                    else if (contents.IndexOf("SimpleImageAccess", start, end - start) > -1)
                    {
                        test = contents.IndexOf("<testQuery>");
                        if (test == -1)
                        {
                            contents = contents.Insert(contents.IndexOf("</capability>"), "<testQuery><pos><long></long><lat></lat></pos>" +
                                 "<size><long></long><lat></lat></size><verb></verb><extras></extras></testQuery>");
                        }
                        else
                        {
                            if (contents.IndexOf("<pos", test) == -1)
                                contents = contents.Insert(contents.IndexOf("<testQuery>", test) + 11, "<pos><long></long><lat></lat></pos>");
                            if (contents.IndexOf("<size", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</pos>", test) + 6, "<size><long></long><lat></lat></size>");
                            if (contents.IndexOf("<verb", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</testQuery>", test), "<verb></verb>");
                            if (contents.IndexOf("<extras", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</testQuery>", test), "<extras></extras>");
                        }

                        contents = InsertVersionResultType(contents);

                        test = contents.IndexOf("<interface");
                        contents = MangleInterfacesBackToXForms("sia", contents);
                    }
                    else if (contents.IndexOf("ConeSearch", start, end - start) > -1)
                    {
                        test = contents.IndexOf("<testQuery>");
                        if (test > -1)
                        {
                            if (contents.IndexOf("<verb", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</testQuery>", test), "<verb></verb>");
                            if (contents.IndexOf("<catalog", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</testQuery>", test), "<catalog></catalog>");
                            if (contents.IndexOf("<extras", test) == -1)
                                contents = contents.Insert(contents.IndexOf("</testQuery>", test), "<extras></extras>");
                        }

                        contents = InsertVersionResultType(contents);
                        contents = MangleInterfacesBackToXForms("conesearch", contents);
                    }
                    else if (contents.IndexOf("OpenSkyNode", start, end - start) > -1)
                    {
                        test = contents.IndexOf("<longitude", contents.IndexOf("OpenSkyNode"));
                        if (test == -1)
                        {
                            contents = contents.Insert(contents.IndexOf("</capability", contents.IndexOf("OpenSkyNode")), "<longitude/>");
                        }
                        test = contents.IndexOf("<latitude", contents.IndexOf("OpenSkyNode"));
                        if (test == -1)
                        {
                            contents = contents.Insert(contents.IndexOf("</capability", contents.IndexOf("OpenSkyNode")), "<latitude/>");
                        }
                        contents = MangleInterfacesBackToXForms("skynode", contents);
                    }
                    else
                    {
                        string iface;
                        test = contents.IndexOf("<interface", start, end - start);
                        if (test == -1)
                            test = contents.IndexOf('<', start + 1);
                        GetEmptyInterfaceFromFile("emptycapability.xml", false, out iface);
                        contents = contents.Insert(test, iface);
                    }
                }
            }
            #endregion

            #region data collection - specific fields
            Regex rxDC = new Regex("xsi:type=..?.?:DataCollection");
            if (rxDC.Match(contents).Length > 0 )
            {
                if (contents.IndexOf("<rights") == -1)
                    contents = contents.Insert(contents.IndexOf("</Resource>"), "<rights></rights>");
                if (contents.IndexOf("<facility") == -1)
                    contents = contents.Insert(contents.IndexOf("</Resource>"), "<facility></facility>");
                contents = contents.Replace("<facility>", "<facility ivo-id=\"\">");
                if (contents.IndexOf("<instrument") == -1)
                    contents = contents.Insert(contents.IndexOf("</Resource>"), "<instrument></instrument>");
                contents = contents.Replace("<instrument>", "<instrument ivo-id=\"\">");

                contents = FillInDCTable(contents);
            }
            #endregion

            #region remove the resource xsi-type
            contents = contents.Replace("xmlns=\"\"", "xmlns=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\"");

            contents = Regex.Replace(contents, "xsi:type=\"..?.?:Service\"", "");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:DataService\"", "");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:CatalogService\"", "");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:DataCollection\"", "");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:Organisation\"", "");
            #endregion

            contents = contents.Replace("<publisher>", "<publisher ivo-id=\"\">");
            contents = contents.Replace("<source>", "<source format=\"\">").Replace("<source/>", "<source format=\"\"></source>");
            
            #region create empty first iterations for the xforms repeaters.
            start = contents.IndexOf("</publisher>") + 12;
            contents = contents.Insert(start, "<creator><name ivo-id=\"\"></name><logo></logo></creator>");

            if (contents.IndexOf("<version", start) == -1)
            {
                end = contents.IndexOf("<contact", start);
                contents = contents.Insert(start, "<version></version>");
            }

            start = contents.IndexOf("<contributor");
            if( start == -1 )
                start = contents.IndexOf("<version");
            contents = contents.Insert(start, "<contributor ivo-id=\"\"></contributor>");

            start = contents.IndexOf("<date");
            if (start == -1) 
                start = contents.IndexOf("<contact>");
            contents = contents.Insert(start, "<date role=\"representative\"></date>");

            start = contents.IndexOf("<relationship");
            if (start == -1)
                start = contents.IndexOf("</content>");
            contents = contents.Insert(start, "<relationship><relationshipType></relationshipType>" +
                                       "<relatedResource ivo-id=\"\"></relatedResource></relationship>");

            #endregion

            #region compress certain repeat fields for selectors type, contentLevel, waveband, creationType, dataSource
            contents = CompressSelectAll(contents, "type");
            contents = CompressSelectAll(contents, "contentLevel");
            contents = CompressSelectAll(contents, "dataSource");
            contents = CompressSelectAll(contents, "waveband");
            contents = CompressSelectAll(contents, "creationType");
            #endregion

            #region remove prefixes for namespaces.
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:WebBrowser\"", "type=\"WebBrowser\"");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:WebService\"", "type=\"WebService\"");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:ParamHTTP\"", "type=\"ParamHTTP\"");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:SimpleSpectralAccess\"", "type=\"SimpleSpectralAccess\"");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:SimpleImageAccess\"", "type=\"SimpleImageAccess\"");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:ConeSearch\"", "type=\"ConeSearch\"");
            contents = Regex.Replace(contents, "xsi:type=\"..?.?:OpenSkyNode\"", "type=\"OpenSkyNode\"");
            #endregion

            #region trim and clean up fields as necessary
            start = contents.IndexOf('>', contents.IndexOf("<publisher")) + 1;
            end = contents.IndexOf('<', start);
            string trim = contents.Substring(start, end - start).Trim();
            if( trim != contents.Substring(start, end - start) )
                contents = contents.Remove(start, end - start).Insert(start, trim);

            start = contents.IndexOf('>', contents.IndexOf("<description")) + 1;
            end = contents.IndexOf('<', start);
            trim = contents.Substring(start, end - start).Trim();
            trim = Regex.Replace(trim, @"\s+", " ", RegexOptions.Singleline);
            if (trim != contents.Substring(start, end - start))
                contents = contents.Remove(start, end - start).Insert(start, trim);
            #endregion

            #region fill in some optional parameters with empty fields.
            start = contents.IndexOf("<contact>");
            while (start > 0)
            {
                end = contents.IndexOf("</contact>", start);
                if (contents.IndexOf("<address", start, end - start) < 0)
                {
                    contents = contents.Insert(end, "<address></address>");
                    end = contents.IndexOf("</contact>", start);
                }
                if (contents.IndexOf("<email", start, end - start) < 0)
                {
                    contents = contents.Insert(end, "<email></email>");
                    end = contents.IndexOf("</contact>", start);
                }
                if (contents.IndexOf("<telephone", start, end - start) < 0)
                {
                    contents = contents.Insert(end, "<telephone></telephone>");
                    end = contents.IndexOf("</contact>", start);
                }
                start = contents.IndexOf("<contact>", end);
            }
            start = contents.IndexOf("<creator>");
            while (start > 0)
            {
                end = contents.IndexOf("</creator>", start);
                if (contents.IndexOf("<logo", start, end - start) < 0)
                {
                    contents = contents.Insert(end, "<logo></logo>");
                    end = contents.IndexOf("</creator>", start);
                }
                int name = contents.IndexOf("<name>", start, end - start);
                if ( name > 0 )
                {
                    contents = contents.Remove(name, 6).Insert(name, "<name ivo-id=\"\">");
                }
                start = contents.IndexOf("<creator>", end);
            }
            start = contents.IndexOf("<interface");
            if (start > 0)
            {
                end = contents.IndexOf('>', start);
                if (contents.IndexOf("version", start, end - start) < 0)
                    contents = contents.Insert(end, " version=\"\" ");
            }

            #endregion

            //identifier back to "resource key"
            //start = contents.IndexOf("<identifier>") + 12;

            //Regex rxAuth = new Regex("xsi:type=..?.?:Authority");
            //if (rxAuth.Match(contents).Length > 0)
            //{
            //    contents = contents.Remove(start, 6);
            //}
            //else
            //{
            //    string auth = GetAuthIdentifier(user);
            //    contents = contents.Remove(start, auth.Length + 1);
            //}

            contents = contents.Replace("xsi:type=\"vg:Authority\"", "");

            return contents;
        }

        private string FillInDCTable(string contents)
        {
            if (contents.IndexOf("<catalog") == -1)
            {
                contents = contents.Insert(contents.IndexOf("</Resource>"), "<catalog><table><name></name><description></description>" +
                  "<column><name></name><description></description><unit></unit><ucd></ucd></column><role></role></table>" +
                  "<table><name></name><description></description><column><name></name><description></description><unit></unit><ucd></ucd></column><role></role></table></catalog>");
            }
            else
            {
                int start = contents.IndexOf("<table>", contents.IndexOf("<catalog"));
                while (start != -1)
                {
                    int endTableInfo = contents.IndexOf("</table>", start);
                    int startColumn = contents.IndexOf("<column>", start, endTableInfo - start);
                    int indexEmpty = endTableInfo;
                    if (startColumn > -1)
                        indexEmpty = startColumn;

                    contents = contents.Insert(indexEmpty, "<column><name></name><description></description><unit></unit><ucd></ucd></column>");
                    startColumn = contents.IndexOf("<column>", start);

                    if (contents.IndexOf("<role>", start) == -1 || contents.IndexOf("<role>", start) > startColumn)
                    {
                        contents = contents.Insert(start + 7, "<role></role>");
                        startColumn = contents.IndexOf("<column>", start);
                    }
                    if (contents.IndexOf("<description>", start) == -1 || contents.IndexOf("<description>", start) > startColumn)
                    {
                        contents = contents.Insert(start + 7, "<description></description>");
                        startColumn = contents.IndexOf("<column>", start);
                    }
                    if (contents.IndexOf("<name>", start) == -1 || contents.IndexOf("<name>", start) > startColumn)
                    {
                        contents = contents.Insert(start + 7, "<name></name>");
                        startColumn = contents.IndexOf("<column>", start);
                    }

                    while (startColumn > -1)
                    {
                        int endColumn = contents.IndexOf("</column>", startColumn);
                        if (contents.IndexOf("<name>", startColumn) == -1 || contents.IndexOf("<name>", startColumn) > endColumn)
                        {
                            contents = contents.Insert(startColumn + 8, "<name></name>");
                            endColumn = contents.IndexOf("</column>", startColumn);
                        }
                        if (contents.IndexOf("<description>", startColumn) == -1 || contents.IndexOf("<description>", startColumn) > endColumn)
                        {
                            contents = contents.Insert(endColumn, "<description></description>");
                            endColumn = contents.IndexOf("</column>", startColumn);
                        }
                        if (contents.IndexOf("<unit>", startColumn) == -1 || contents.IndexOf("<unit>", startColumn) > endColumn)
                        {
                            contents = contents.Insert(endColumn, "<unit></unit>");
                            endColumn = contents.IndexOf("</column>", startColumn);
                        }
                        if (contents.IndexOf("<ucd>", startColumn) == -1 || contents.IndexOf("<ucd>", startColumn) > endColumn)
                        {
                            contents = contents.Insert(endColumn, "<ucd></ucd>");
                            endColumn = contents.IndexOf("</column>", startColumn);
                        }
                        startColumn = contents.IndexOf("<column>", endColumn);
                    }

                    start = contents.IndexOf("<table>", start + 1);
                }

                //add first empty whole table.
                contents = contents.Insert(contents.IndexOf("<catalog>") + 9, "<table><name></name><description></description>" +
                    "<column><name></name><description></description><unit></unit><ucd></ucd></column><role></role></table>");
            }

            return contents;
        }


        [WebMethod]
        public RegistryResponse EditResource(String identifier, String user)
        {
            //only allow local requests
            if (Context.Request.IsLocal == false)
                return new RegistryResponse(1, "Resource editing only permitted from the publishing interface.");

            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            String pendingFile = GetIDFilename(identifier, Convert.ToInt64(user));
            string strmContents = string.Empty;
            bool pending = false;

            try
            {

            if (System.IO.File.Exists(pendingFile))
            {
                    StreamReader reader = new StreamReader(pendingFile);
                    strmContents = reader.ReadToEnd();
                    reader.Close();

                    pending = true;
                }
                else //it's in the registry and we have to pull it out.
                {
                    // Query the resource from the registry 
                    registry.Registry reg = new registry.Registry();

                    XmlDocument[] docs = reg.QueryRIResourceXMLDoc("identifier = '" + identifier + "'");

                    if (docs.Length == 0)
                    {
                        resp.returncode = 1;
                        resp.message = "Unknown resource " + identifier;
                        return resp;
                    }

                    StringWriter sw = new StringWriter();
                    XmlTextWriter xw = new XmlTextWriter(sw);
                    docs[0].WriteTo(xw);
                    StringReader reader = new StringReader(sw.ToString());
                    strmContents = reader.ReadToEnd();
                    reader.Close();
                }

                string originalContents = strmContents;

                if (!strmContents.StartsWith("<?"))
                    strmContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + strmContents;

                //temporary test - no form edit for >1 capability or >1 interface
                int cap = strmContents.IndexOf("<capability");
                if (strmContents.IndexOf("<capability", cap + 1) > -1)
                {
                    resp.returncode = 1;
                    resp.message = "Form edit of resources with more than one capability is not supported at this time.";

                    return resp;
                }
/*                int iface = strmContents.IndexOf("<interface");
                if (strmContents.IndexOf("<interface", iface + 1) > -1)
                {
                    resp.returncode = 1;
                    resp.message = "Form edit of resources with more than one interface is not supported at this time.";

                    return resp;
                }
*/

                //determine type of resource, and use appropriate create/edit functionality.
                string xsi_type = string.Empty;
                string daltypes = String.Empty;
                string postFunction = string.Empty;
                int endResourceTag = strmContents.IndexOf(">", strmContents.IndexOf("Resource"));
                if (strmContents.IndexOf(":Organisation") > -1 && strmContents.IndexOf(":Organisation") < endResourceTag)
                {
                    xsi_type = "vr:Organisation";
                    postFunction = "managereg.asmx/PostBaseResourceOrganisation";
                }
                else if (strmContents.IndexOf(":Authority") > -1 && strmContents.IndexOf(":Authority") < endResourceTag)
                {
                    xsi_type = "vg:Authority";
                    postFunction = "managereg.asmx/PostAuthorityResource";
                }
                else if (strmContents.IndexOf(":DataService") > -1 && strmContents.IndexOf(":DataService") < endResourceTag)
                {
                    xsi_type = "DataService";
                    postFunction = "managereg.asmx/PostDataServiceResource";

                    strmContents = MangleSTCToXFormsExample(strmContents);
                }
                else if (strmContents.IndexOf(":DataCollection") > -1 && strmContents.IndexOf(":DataCollection") < endResourceTag)
                {
                    xsi_type = "DataCollection";
                    postFunction = "managereg.asmx/PostDataCollectionResource";

                    strmContents = MangleSTCToXFormsExample(strmContents);
                }
                else if (strmContents.IndexOf("CatalogService") > -1 && strmContents.IndexOf("CatalogService") < endResourceTag)
                {
                    xsi_type = "DataService";

                    if (strmContents.IndexOf("capability") > -1 &&
                        strmContents.Substring(strmContents.IndexOf("capability")).Contains(":ConeSearch") )
                    {
                        daltypes += "csc ";
                        postFunction = "managereg.asmx/PostConeSearchResource";
                    }
                    else if( strmContents.IndexOf("capability") > -1 &&
                             strmContents.Substring(strmContents.IndexOf("capability")).Contains(":SimpleSpectralAccess") )
                    {
                        daltypes += "ssa ";
                        postFunction = "managereg.asmx/PostSSAResource";
                    }
                    else if (strmContents.IndexOf("capability") > -1 &&
                             strmContents.Substring(strmContents.IndexOf("capability")).Contains(":SimpleImageAccess"))
                    {
                        daltypes += "sia ";
                        postFunction = "managereg.asmx/PostSIAResource";
                    }
                    else if (strmContents.IndexOf("capability") > -1 &&
                        strmContents.Substring(strmContents.IndexOf("capability")).Contains(":OpenSkyNode"))
                    {
                        daltypes += "SkyNode ";
                        postFunction = "managereg.asmx/PostOpenSkyNodeResource";
                    }
                    else //other service types.
                    {
                        resp.returncode = 1;
                        resp.message = "Unknown type for resource " + identifier + " or type unsupported for editing";
                        return resp;
                    }

                    strmContents = MangleSTCToXFormsExample(strmContents);
                }
                else if (strmContents.IndexOf("vr:Service") > -1 && strmContents.IndexOf("vr:Service") < endResourceTag)
                {
                    xsi_type = ":Service";
                    postFunction = "managereg.asmx/PostServiceResource";
                }
                else
                {
                    resp.returncode = 1;
                    resp.message = "Unknown type for resource " + identifier + " or type unsupported for editing";
                    return resp;
                }

                //mangle back to the form we're using for xforms.
                try
                {
                    strmContents = MangleBackToXFormsExample(strmContents, user);
                }
                catch (Exception ex)
                {
                    resp.returncode = 1;
                    resp.message = "Error loading form data from resource. " + ex.Message;
                    return resp;
                }


                resp = CreateTempFile(user + "_tempResource.xml", strmContents);
                if (resp.returncode != 0)
                    return resp;

                String strForm;
                RegistryResponse response = ReadTextFile("createresourcetemplate.xhtml", out strForm);
                if (response.returncode != 0)
                    return response;
                else
                    AdjustSampleForm(daltypes, xsi_type, user + "_tempResource.xml", ref strForm);

                //adjustments for edit rather than post.
                strForm = SetRepeatControllersFromExistingResource(strForm, originalContents);
                strForm = strForm.Replace(postFunction, postFunction.Replace("Post", "Edit"));
                strForm = strForm.Replace("Creation", "Modification");
                strForm = strForm.Replace("Resource created successfully!",
                                                    "Resource edited successfully!");

                if( pending == false )
                {
                    strForm = strForm.Replace("Note you <font color=\"firebrick\">MUST</font> finalize your resource before it appears in the registry.",
                        "Existing resource edited. Your changes have been applied to the registry.");
                    strForm = strForm.Replace("Review and Finalize", "Review Resources");
                }
                strForm = strForm.Replace("Resource Key:", "Resource Key: (fixed)");
                strForm = strForm.Replace("<xf:bind id=\"identifier\"", "<xf:bind id=\"identifier\" readonly=\"true()\"");
                strForm = strForm.Replace("recordstartpage.xhtml", "editfinalizeresources.xhtml");


                resp = CreateTempFile(user + "_editResource.xhtml", strForm);
                if (resp.returncode != 0)
                    return resp;
           }
           catch (Exception e)
           {
               resp.returncode = 1;
               resp.message = e.Message;
           }

           resp.message = user + "_editResource.xhtml";
           return resp;
        }
        
        //tdower todo - we could use this for ivoid repeat controllers and suchlike?
        private string SetRepeatControllersFromExistingResource(string form, string res)
        {
            string strmForm = form;

            if (res.Contains("AllSky/>"))
                strmForm = strmForm.Replace("<spatialcoveragetype></spatialcoveragetype>", "<spatialcoveragetype>All Sky</spatialcoveragetype>");
            else if (res.Contains("<Circle>"))
                strmForm = strmForm.Replace("<spatialcoveragetype></spatialcoveragetype>", "<spatialcoveragetype>Circle Region</spatialcoveragetype>");
            else if (res.Contains("<Position2VecInterval"))
                strmForm = strmForm.Replace("<spatialcoveragetype></spatialcoveragetype>", "<spatialcoveragetype>Coordinate Range</spatialcoveragetype>");

            if (strmForm.Contains("<togglecapability>0</togglecapability>") && res.Contains("<capability"))
                strmForm = strmForm.Replace("<togglecapability>0</togglecapability>", "<togglecapability>1</togglecapability>");

            return strmForm;
        }

        private RegistryResponse CreateTempFile(string filename, string contents)
        {
            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            StreamWriter xmlwrite;
            try
            {
                xmlwrite = new StreamWriter(location + "\\" + filename, false);
                xmlwrite.Write(contents);
                xmlwrite.Flush();
                xmlwrite.Close();
            }
            catch (Exception e)
            {
                resp.returncode = -1;
                resp.message = e.Message; 
            }

            return resp;
        }

        private string InsertVersionResultType(string contents) //for conesearch, sia, ssa
        {
            int start = contents.IndexOf("<interface");
            while (start > -1)
            {
                int endinterface = contents.IndexOf('>', start);
                int insert = contents.IndexOf("version", start);
                if (insert == -1 || insert > endinterface)
                {
                    contents = contents.Insert(endinterface, " version=\"\"");
                    endinterface = contents.IndexOf('>', start);
                }
                endinterface = contents.IndexOf("</interface", start);
                insert = contents.IndexOf("<result", start);
                if (insert == -1 || insert > endinterface)
                    contents = contents.Insert(endinterface, "<resultType></resultType>");

                start = contents.IndexOf("<interface", start + 1);
            }
            return contents;
        }

        private string MangleInterfacesBackToXForms(string type, string contents)
        {
            int test = contents.IndexOf("<interface");
            string iface;
            GetEmptyInterfaceFromFile("empty" + type + ".xml", false, out iface);
            contents = contents.Insert(test, iface);

            //insert one empty and label all generic interfaces
            GetEmptyInterfaceFromFile("empty" + type + ".xml", true, out iface);
            contents = LabelGenericInterfaces(contents);

            //add new generic interface at end of standard interfaces. 
            int endcap = contents.IndexOf("</capability");
            int lastiface = contents.IndexOf("<genericinterface");
            if (lastiface == -1)
                lastiface = contents.IndexOf('<', contents.LastIndexOf("</interface>", endcap) + 1);

            contents = contents.Insert(lastiface, iface);

            return contents;
        }

        private string LabelGenericInterfaces(string contents)
        {
            bool generic = false;
            int start = contents.IndexOf("<interface");
            while ( start > -1 )
            {
                //generic if there is no role, or role != "std"
                int endinterface = contents.IndexOf('>', start);
                int role = contents.IndexOf("role", start);
                if (role == -1 || role > endinterface)
                    generic = true;
                else
                {
                    int std = contents.IndexOf("std", role);
                    if (std == -1 || std > endinterface)
                       generic = true;
                }
                if (generic)
                {
                    contents = contents.Insert(start + 1, "generic");
                    contents = contents.Insert(contents.IndexOf("</interface", start) + 2, "generic");
                }

                start = contents.IndexOf("<interface", start + 1);
            }
            return contents;
        }

        private RegistryResponse GetEmptyInterfaceFromFile(string filename, bool generic, out string contents)
        {
            string gen = string.Empty;
            if (generic)
                gen = "generic";

            string wholeCap = String.Empty;
            contents = string.Empty;
            RegistryResponse resp = ReadTextFile(filename, out wholeCap);
            if (resp.returncode == 0)
            {
                int ifaceindex = wholeCap.IndexOf("<" + gen + "interface");
                if (ifaceindex >= 0)
                {
                    int ifaceend = wholeCap.IndexOf('>', wholeCap.IndexOf("</" + gen + "interface>", ifaceindex)) +1;
                    contents = wholeCap.Substring(ifaceindex, ifaceend - ifaceindex);
                }
            }
            return resp;
        }

        private RegistryResponse ReadTextFile(string filename, out string contents)
        {
            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            StreamReader sr;
            try
            {
                sr = new StreamReader(location + "\\" + filename);
                contents = sr.ReadToEnd();
                sr.Close();
            }
            catch (Exception e)
            {
                resp.returncode = -1;
                resp.message = e.Message;
                contents = string.Empty;
            }

            return resp;
        }

        [WebMethod]
        public RegistryResponse XMLEditResource(String identifier, String user)
        {
            //only allow local requests
            if (Context.Request.IsLocal == false)
                return new RegistryResponse(1, "Resource editing only permitted from the publishing interface.");

            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            String pendingFile = GetIDFilename(identifier, Convert.ToInt64(user));
            string strmContents = string.Empty;
            bool pending = false;

            try
            {
                if (System.IO.File.Exists(pendingFile))
                {
                    StreamReader reader = new StreamReader(pendingFile);
                    strmContents = reader.ReadToEnd();
                    reader.Close();

                    pending = true;
                }
                else //it's in the registry and we have to pull it out.
                {
                    // Query the resource from the registry 
                    registry.Registry reg = new registry.Registry();
                    XmlDocument[] docs = reg.QueryRIResourceXMLDoc("identifier = '" + identifier + "'");
                    if (docs.Length == 0)
                    {
                        resp.returncode = 1;
                        resp.message = "Unknown resource " + identifier;
                        return resp;
                    }

                    StringWriter sw = new StringWriter();
                    XmlTextWriter xw = new XmlTextWriter(sw);
                    docs[0].WriteTo(xw);
                    StringReader reader = new StringReader(sw.ToString());
                    strmContents = reader.ReadToEnd();
                    reader.Close();
                }


                if (!strmContents.StartsWith("<?"))
                    strmContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + strmContents;

                int start = strmContents.IndexOf('>') + 1;
                string resource = strmContents.Substring(start);
                resource = resource.Replace("<", "&lt;");
                resource = resource.Replace(">", "&gt;");

                strmContents = strmContents.Substring(0, start) + "<resource>" + resource + "</resource>";
                strmContents = strmContents.Replace(" /&gt;", "/&gt;");
                strmContents = strmContents.Replace("&gt;&lt;", "&gt;\n&lt;");

                resp = CreateTempFile(user + "_tempResource.xml", strmContents);
                if (resp.returncode != 0)
                    return resp;


                resp = ReadTextFile("XMLEdit.xhtml", out strmContents);
                if (resp.returncode != 0)
                    return resp;

                //Make template edit form use our temporary resource.
                strmContents = strmContents.Replace("example.xml", user + "_tempResource.xml");
                if (pending == false)
                {
                    strmContents = strmContents.Replace("Note you <font color=\"firebrick\">MUST</font> finalize your resource before it appears in the registry.",
                        "Existing resource edited. Your changes have been applied to the registry.");
                    strmContents = strmContents.Replace("Review and Finalize", "Review Resources");
                }

                resp = CreateTempFile(user + "_editResource.xhtml", strmContents);
                if (resp.returncode != 0)
                    return resp;

            }
            catch (Exception e)
            {
                resp.returncode = 1;
                resp.message = e.Message;
            }

            if (resp.returncode == 0)
            {
                resp.message = user + "_editResource.xhtml";
            }

            return resp;
        }


        [WebMethod]
        public RegistryResponse SubmitPendingResource(String identifier, String user)
        {
            //only allow local requests
            if (Context.Request.IsLocal == false)
                return new RegistryResponse(1, "Resource submission only permitted from the publishing interface.");

            RegistryResponse resp = new RegistryResponse(0, string.Empty);
            long userKey = Convert.ToInt64(user);

            resp = SavePendingResource(identifier, userKey);

            return resp;
        }

        //datetime in form provided by:  STOAI.GetOAIDatestamp(DateTime.Now, granularityType.YYYYMMDDThhmmssZ);
        private int SetXmlTime(string tag, string datetime, ref String xml)
        {
           int start = xml.IndexOf(tag);
           if (start < 0 )
               return -1;

           start = xml.IndexOf('"', start) + 1;
           int end = xml.IndexOf('"', start);

           xml = xml.Remove(start, end - start).Insert(start, datetime);
           return 0;
        }

        private RegistryResponse SaveResourceEdits(String fullID, String xml, long userKey)
        {
            SetXmlTime("updated", STOAI.GetOAIDatestamp(DateTime.Now, granularityType.YYYYMMDDThhmmssZ), ref xml);

            SqlConnection conn = new SqlConnection();
            RegistryResponse response = new RegistryResponse(0, String.Empty);
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                response = CreatePendingFile(fullID, xml, userKey, true);
                if (response.returncode != 1)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.Append("Creating resource: " + fullID + "\n");

                    int status = vorXML.LoadVORXML(xml, userKey, String.Empty, sb);
                    if (status != 0)
                    {
                        response.returncode = status;
                        response.message = sb.ToString();
                    }
                }
            }
            catch (Exception e) 
            {
                response.returncode = 1;
                response.message = e.ToString();
            }
            finally
            {
                conn.Close();
            }

            return response;
        }

        private RegistryResponse SavePendingResource(String identifier, long userKey)
        {
            RegistryResponse response = new RegistryResponse(0, String.Empty);
            String pendingFile = GetIDFilename(identifier, userKey);
            StreamReader reader = null;
            String xml = String.Empty;

            try
            {
                reader = new StreamReader(pendingFile, true);
                xml = reader.ReadToEnd();
                reader.Close();
                if (xml.Length == 0)
                {
                    response.returncode = 1;
                    response.message = "Bad pending file for identifier " + identifier;
                }
            }
            catch( Exception )
            {
                response.returncode = 1;
                response.message = "No pending file exists for identifier " + identifier;
            }
            if (response.returncode > 0)
                return response;

            SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                bool unique = CheckForUniqueIVOID(identifier, conn);
                bool validauth = CheckForUsersAuthority(identifier, userKey, conn);
                if (!unique)
                {
                    response.returncode = 1;
                    response.message = "IVO Identifier already exists. Please ensure that your resource" +
                                       " does not already exist in the registry and try another resource key.";
                }
                else if (!validauth)
                {
                    response.returncode = 1;
                    response.message = "IVO Identifier does not begin with an authority associated with this user. ";
                }
                else
                {
                    string now = STOAI.GetOAIDatestamp(DateTime.Now, granularityType.YYYYMMDDThhmmssZ);
                    SetXmlTime("created", now, ref xml);
                    SetXmlTime("updated", now, ref xml);

                    //MSXML helpfully removes our "xlink" namespace under certain circumstances
                    //we need it to load STC.
                    if (xml.Contains("xmlns:xlink=\"http://www.w3.org/1999/xlink\"") == false &&
                        xml.Contains("STCResourceProfile>") == true ) 
                    {
                        int insert = xml.IndexOf("xmlns:");
                        xml = xml.Insert(insert, "xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
                    }


                    StringBuilder sb = new StringBuilder();
                    sb.Append("Creating resource: " + identifier + "\n");

                    int status = vorXML.LoadVORXML(xml, userKey, String.Empty, sb);
                    if (status != 0)
                    {
                        response.returncode = status;
                        response.message = sb.ToString();
                    }
                }
            }
            finally
            {
                conn.Close();
            }

            //delete the temporary file if we have succeeded
            if (response.returncode == 0)
            {
                try
                {
                    System.IO.File.Delete(pendingFile);
                }
                catch (Exception e)
                {
                }

            }

            return response;
        }


        private static bool CheckForUniqueIVOID(string id, SqlConnection conn)
        {
             bool unique = false;
                 string select = "SELECT identifier FROM resource WHERE( identifier LIKE '" + id + "')";
                 SqlDataAdapter sqlDA = new SqlDataAdapter(select, conn);
                 DataSet ds = new DataSet();
                 sqlDA.Fill(ds);

                 if (ds.Tables[0].Rows.Count == 0)
                     unique = true;
             return unique;
        }

        public static XmlTextReader GetRequestXML(string root, Stream input)
        {
            XmlTextReader reader = null;
            try
            {
                String strmContents = GetRequestString(input);
                String user = strmContents.Substring(strmContents.IndexOf("<" + root));
                user = user.Remove(user.IndexOf(" xmlns"), user.IndexOf('>') - user.IndexOf(" xmlns"));

                MemoryStream memoryStream = new MemoryStream(StringToUTF8ByteArray(user));
                reader = new XmlTextReader(memoryStream);
                reader.MoveToContent();
            }
            catch (Exception) { return null; }

            return reader;
        }

        private string GetRequestString()
        {
            return GetRequestString(Context.Request.InputStream);
        }

        private static string GetRequestString(Stream str)
        {
            //Stream str = Context.Request.InputStream;
            string strmContents = String.Empty;
            Int32 strLen, strRead;
            // Find number of bytes in stream.
            strLen = Convert.ToInt32(str.Length);
            // Create a byte array.
            byte[] strArr = new byte[strLen];
            // Read stream into byte array.
            strRead = str.Read(strArr, 0, strLen);
            strmContents = System.Text.UTF8Encoding.UTF8.GetString(strArr);
            
            return strmContents;
        }

        private static Byte[] StringToUTF8ByteArray(String pXmlString)
        {
            System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
            Byte[] byteArray = encoding.GetBytes(pXmlString);
            return byteArray;
        }
    }
}
