using System;
using System.Xml;
using System.IO;
using System.Collections;
using registry;
using System.Text;
using System.Data;
using System.Data.SqlClient;
using System.Xml.XPath;
using System.Xml.Xsl;
using System.Net;


namespace registry
{


    /// <summary>
    /// Summary description for Class1
    /// </summary>
    public class VOR_XML
    {
        private static string sConnect = Properties.Settings.Default.SqlAdminConnection;
        private static string baseurl = Properties.Settings.Default.baseURL;
        public static string strValidatedBy = "ivo://archive.stsci.edu";

        public VOR_XML()
		{
			//
			// TODO: Add constructor logic here
			//
		
		}

        private static string appendUrl(string urlbase, string last)
        {
            if (urlbase.EndsWith("/"))
                return urlbase + last;
            else
                return urlbase + '/' + last;
        }

        public int LoadVORXML(string theXML, StringBuilder sbOut)
        {
            return LoadVORXML(theXML, 0, String.Empty, sbOut);
        }

        public int LoadVORXML(string theXML, long userKey, string harvestUrl, StringBuilder sbOut)
        {
            int iReturn = 0;
            SqlConnection conn = null; 

            // First Read theXML and select the IVOID for loading in the full XML
            // from the IDENTIFIER element tag
            
            string ivoid = "";
            long resourceKey = 0; // note keys can not be 0,  so this is for unset key
            StringReader rd = new StringReader(theXML);
            StringBuilder sbSQL = new StringBuilder();
            SqlCommand cachecmd = new SqlCommand();

            XmlReader xr = new XmlTextReader(rd);
            while (xr.Read())
			{
                string name = xr.LocalName.ToUpper();
                if ( (name == "IDENTIFIER") && ( xr.NodeType != XmlNodeType.EndElement) ) 
                {
                    xr.Read(); 
                    ivoid = xr.Value.Trim();
                    break; // to avoid selecting identifier tags within subelements of resource
                }
            }

            // Lookup Record to Check if Exists
            try
            {
                conn = new SqlConnection(sConnect);
                conn.Open();

                try
                {
                    SqlCommand lookupRkey = SQLHelper.getRkeyLookupCmd(conn);
                    lookupRkey.Parameters[0].Value = ivoid;

                    using (SqlDataReader rdr = lookupRkey.ExecuteReader(CommandBehavior.SingleResult))
                    {
                        while (rdr.Read())
                        {
                            resourceKey = rdr.GetInt64(0);
                        }
                    }

                }
                catch (Exception e) {sbOut.Append("Lookup Exist Record Error:" + e.Message + ":" + e.StackTrace); };

                // (1) Load in the VOR DB Schema Table Fields

                try
                {
                    // convert XML resource into db sql insert commands via XSL

                    //first handle validationLevels of incoming resources
                    if (harvestUrl != null && harvestUrl != string.Empty && harvestUrl.IndexOf("stsci") == -1)
                    {
                        theXML = SetValidationLevels(theXML);
                    }


                    string sres = theXML;

                    StringReader reader = new StringReader(sres);
                    XPathDocument myXPathDoc = new XPathDocument(reader);
                    XslCompiledTransform myXslTrans = new XslCompiledTransform();
                    XmlUrlResolver resolver = new XmlUrlResolver();
                    resolver.Credentials = CredentialCache.DefaultCredentials;


                    //load the Xsl 
                    StringWriter sw = new StringWriter(sbSQL);
                    myXslTrans.Load(appendUrl(baseurl,"xsl/insert.xsl"), XsltSettings.TrustedXslt, resolver);

                    // Load the Xsl Parameters
                    XsltArgumentList argList = new XsltArgumentList();
                    //DateTime harvestDate = DateTime.Now;
                    String harvestDate = DateTime.Now.ToString();

                    argList.AddParam("harvestedFrom", "", harvestUrl);
                    argList.AddParam("harvestedFromDate", "", harvestDate);
                    if (resourceKey != 0) argList.AddParam("existingrkey", "", resourceKey);


                    XmlTextWriter writer = new XmlTextWriter(sw);
                    myXslTrans.Transform(myXPathDoc, argList, writer);

                    string cacherow = GetVOTableCacheRow(theXML, sbOut);
                    string interfaces = GetVOTableCacheInterfaceRows(theXML, sbOut);
                    if (cacherow.Length > 0)
                    {
                        cachecmd = SQLHelper.GetInsertVOTableCmd("@rkey", cacherow, interfaces, conn);
                    }
                    else
                    {
                        sbOut.Append(" No error given, but failed to create cache SQL. " + "Resource: " + theXML);
                        iReturn = -1;
                        return iReturn;
                    }
                }
                catch (Exception e)
                {
                    conn.Close();
                    sbOut.Append("Xsl Load Exception using baseurl " + baseurl + " . " + e.Message + ":" + e.StackTrace);
                    iReturn = -1;
                    return iReturn;
                };


                SqlCommand command = conn.CreateCommand();
                SqlTransaction transaction;

                // Start a local transaction.
                transaction = conn.BeginTransaction("SampleTransaction");

                // Must assign both transaction object and connection
                // to Command object for a pending local transaction
                command.Connection = conn;
                command.Transaction = transaction;
                cachecmd.Transaction = transaction;

                int rows = 0;
                int cacherows = 0;
                try
                {
                    command.CommandText = sbSQL.ToString();
                    rows = command.ExecuteNonQuery();

                    cacherows = cachecmd.ExecuteNonQuery();

                    // Attempt to commit the transaction.
                    transaction.Commit();

                    if (rows <= 0 || cacherows <= 0)
                    {
                        sbOut.Append(" No error given, but failed to write resource to DB. " + "Resource: " + theXML);
                        iReturn = -1;
                    }
                }
                catch (Exception ex)
                {
                    sbOut.Append("Commit Exception Type: {0}" + ex.GetType());
                    sbOut.Append("  Message: {0}" + ex.Message);

                    // Attempt to roll back the transaction.
                    try
                    {
                        transaction.Rollback();
                    }
                    catch (Exception ex2)
                    {
                        // This catch block will handle any errors that may have occurred
                        // on the server that would cause the rollback to fail, such as
                        // a closed connection.
                        sbOut.Append("Rollback Exception Type: {0}" + ex2.GetType());
                        sbOut.Append("  Message: {0}" + ex2.Message);
                    }
                    iReturn = -1;
                    conn.Close();
                }

                if (iReturn == 0)
                {

                    // (2) 
                    // Load in the full XML field in the Resource Table where the Identifier 
                    // matches the resource 
                    try
                    {
                        const int maxntext = 1073741823; //2^30-1 is max.

                        SqlCommand ins = conn.CreateCommand();
                        StringBuilder sb = new StringBuilder();

                        string updateCommand = "UPDATE Resource set xml = @xml";
                        sb.Append(updateCommand);
                        //sb.Append(" where identifier = '" + ivoid + "' and [@status] = 1");
                        sb.Append(" where pkey in ( select top 1 pkey from Resource where identifier= '" + ivoid + "' and [@status] != 2 order by [@updated], harvestedFromDate desc)");
                        ins.CommandText = sb.ToString();
                        ins.Parameters.Add("@xml", SqlDbType.NText, maxntext);
                        ins.Parameters[0].Value = theXML.ToString();
                        ins.Prepare();
                        ins.ExecuteNonQuery();

                    }
                    catch (Exception e)
                    {
                        sbOut.Append("Failed to Load Full XML for identifier: " + ivoid + ". " + e.Message);
                        iReturn = -1;
                    }

                    if (iReturn == 0)
                    {
                        // (3)
                        //  Associate new resource with a user record if one was specified.
                        try
                        {
                            if (userKey > 0)
                            {
                                SqlCommand ins = conn.CreateCommand();

                                ins.CommandText = "UPDATE Resource set ukey = " + userKey +
                                                        " where identifier = " + "'" + ivoid + "'";
                                ins.ExecuteNonQuery();
                            }
                        }
                        catch (Exception e)
                        {
                            sbOut.Append("Failed to associate User with Resource: " + ivoid + ". " + e.Message);
                            iReturn = -1;
                        }
                    }
                }
            }
            finally
            {
                conn.Close();
            }

            return iReturn;
        }

        private string SetValidationLevels(string theXML)
        {
            XmlDocument doc = new XmlDocument();
            doc.LoadXml(theXML);

            XmlNodeList list = doc.FirstChild.ChildNodes;
            foreach (XmlNode node in list)
            {
                if ((node.Name.ToLower() == "validationlevel") && !(node.ParentNode.Name == "capability") && node.InnerXml != "2")
                {
                    if (node.Attributes["validatedBy"] != null)
                        node.Attributes["validatedBy"].Value = strValidatedBy;
                    node.InnerXml = "2";
                    break;
                }
            }

            list = doc.GetElementsByTagName("capability");
            foreach (XmlNode node in list)
            {
                foreach (XmlNode child in node.ChildNodes)
                {
                    if (child.Name.ToLower() == "validationlevel" && node.InnerXml != "2")
                    {
                        if (node.Attributes["validatedBy"] != null)
                            node.Attributes["validatedBy"].Value = strValidatedBy;
                        child.InnerXml = "2";
                        break;
                    }
                }
            }
            System.Text.StringBuilder sb = new System.Text.StringBuilder();
            System.IO.StringWriter sw = new System.IO.StringWriter();
            XmlTextWriter xw = new XmlTextWriter(sw);
            doc.WriteTo(xw);
            return sw.ToString();
        }

        public int LoadEntireVOTableCache(string theXML, StringBuilder sbOut)
        {
            int iReturn = 0;
            SqlConnection conn = null;

            try
            {
                conn = new SqlConnection(sConnect);

                if (sConnect == null)
                    throw new Exception("Loader: SqlConnection.String not found in Web.config");

                // First Read theXML and select the IVOID for loading in the full XML
                // from the IDENTIFIER element tag

                string ivoid = "";
                long resourceKey = 0; // note keys can not be 0,  so this is for unset key
                StringReader rd = new StringReader(theXML);
                StringBuilder sbSQL = new StringBuilder();

                XmlReader xr = new XmlTextReader(rd);
                while (xr.Read())
                {
                    string name = xr.LocalName.ToUpper();
                    if ((name == "IDENTIFIER") && (xr.NodeType != XmlNodeType.EndElement))
                    {
                        xr.Read();
                        ivoid = xr.Value.Trim();
                        break; // to avoid selecting identifier tags within subelements of resource
                    }
                }

                // Lookup Record to Check if Exists
                try
                {
                    conn.Open();

                    SqlCommand lookupRkey = SQLHelper.getRkeyLookupCmd(conn);
                    lookupRkey.Parameters[0].Value = ivoid;

                    using (SqlDataReader rdr = lookupRkey.ExecuteReader(CommandBehavior.SingleResult))
                    {
                        while (rdr.Read())
                        {
                            resourceKey = rdr.GetInt64(0);
                        }
                    }

                }
                catch (Exception e)
                {
                    sbOut.Append("Lookup Exist Record Error:" + e.Message + ":" + e.StackTrace);
                    resourceKey = 0;
                };
                if (resourceKey == 0)
                {
                    sbOut.Append("Resource does not exist in database. Cannot save VOTable version.");
                    return -1;
                }

                string resource = GetVOTableCacheRow(theXML, sbOut);
                string interfaces = GetVOTableCacheInterfaceRows(theXML, sbOut);
                SqlCommand command = SQLHelper.GetInsertVOTableCmd(resourceKey.ToString(), resource, interfaces, conn);

                // Make the DB Connection/Transaction for Loading in XML
                //conn.Open();

                SqlTransaction transaction;

                // Start a local transaction.
                transaction = conn.BeginTransaction("SampleTransaction");

                // Must assign both transaction object and connection
                // to Command object for a pending local transaction
                command.Connection = conn;
                command.Transaction = transaction;

                int rows = 0;
                try
                {
                    rows = command.ExecuteNonQuery();

                    // Attempt to commit the transaction.
                    transaction.Commit();

                    if (rows <= 0)
                    {
                        //sbOut.Append(" No error given, but failed to write resource to DB.");
                        iReturn = -1;
                    }
                }
                catch (Exception ex)
                {
                    sbOut.Append("Commit Exception Type: {0}" + ex.GetType());
                    sbOut.Append("  Message: {0}" + ex.Message);

                    // Attempt to roll back the transaction.
                    try
                    {
                        transaction.Rollback();
                    }
                    catch (Exception ex2)
                    {
                        // This catch block will handle any errors that may have occurred
                        // on the server that would cause the rollback to fail, such as
                        // a closed connection.
                        sbOut.Append("Rollback Exception Type: {0}" + ex2.GetType());
                        sbOut.Append("  Message: {0}" + ex2.Message);
                    }
                    iReturn = -1;
                }
            }
            finally
            {
                conn.Close();
            } 


            return iReturn;
        }

        private string GetVOTableCacheInterfaceRows(string theXML, StringBuilder sbOut)
        {
            string ifaces = string.Empty;

            try
            {
                string sres = theXML;
                StringReader reader = new StringReader(sres);
                XPathDocument myXPathDoc = new XPathDocument(reader);
                XslCompiledTransform myXslTrans = new XslCompiledTransform();

                //load the Xsl 
                StringBuilder sbXSL = new StringBuilder();
                StringWriter sw = new StringWriter(sbXSL);
                myXslTrans.Load(appendUrl(baseurl,"xsl/InterfaceViewResults_one_resource.xsl"));

                XmlTextWriter writer = new XmlTextWriter(sw);
                myXslTrans.Transform(myXPathDoc, writer);

                string rawIfaces = sbXSL.ToString();

                int iTR = rawIfaces.IndexOf("<TR>");
                while (iTR > -1)
                {
                    ifaces += rawIfaces.Substring(iTR, rawIfaces.IndexOf("</TR>", iTR) - iTR + 5);
                    iTR = rawIfaces.IndexOf("<TR>", iTR + 1);
                }

            }
            catch (Exception e)
            {
                sbOut.Append("Xsl Load Exception" + e.Message + ":" + e.StackTrace);
                return string.Empty;
            };

            return ifaces;
        }

        private string GetVOTableCacheRow(string theXML, StringBuilder sbOut)
        {
            string resource = string.Empty;
            try
            {
                string sres = theXML;
                StringReader reader = new StringReader(sres);
                XPathDocument myXPathDoc = new XPathDocument(reader);
                XslCompiledTransform myXslTrans = new XslCompiledTransform();

                //load the Xsl 
                StringBuilder sbSQL = new StringBuilder();
                StringWriter sw = new StringWriter(sbSQL);
                myXslTrans.Load(appendUrl(baseurl, "xsl/RegistryResults_vot_one_resource.xsl"));

                XmlTextWriter writer = new XmlTextWriter(sw);
                myXslTrans.Transform(myXPathDoc, writer);

                resource = sbSQL.ToString();
                resource = resource.Substring(resource.IndexOf("<TR>"));
                resource = resource.Substring(0, resource.IndexOf("</TR>") + 5);
            }
            catch (Exception e)
            {
                sbOut.Append("Xsl Load Exception" + e.Message + ":" + e.StackTrace);
                return string.Empty;
            };

            return resource;
        }
    }
}
