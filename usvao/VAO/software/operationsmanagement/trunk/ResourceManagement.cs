using System;
using System.Collections.Generic;
using System.Collections;
using System.Xml;
using System.Xml.XPath;

//for faster direct query and small modification access
using System.Data;
using System.Data.SqlClient;

//in order to use the same ingest scheme as harvesting.
using System.Net;

namespace OperationsManagement
{
     
    public class ResourceManagement
    {
        private static string sqlConnection = (string)System.Configuration.ConfigurationManager.AppSettings["SqlConnection"];
        private static string sqlAdminConnection = (string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"];

        private static string appDir = System.Web.Hosting.HostingEnvironment.ApplicationPhysicalPath;
        public static string strValidatedBy = "ivo://archive.stsci.edu/nvoregistry";

        private static string strCheckIdentifier = "select [rstat] from resource where ivoid = '$1'";
        private static string strCheckAuthorities = "select [rstat] from resource where res_type like '%Authority' and ivoid like '$1%'";
        private static string strGetActiveXmlResource = "select xml from resource where ivoid = '$1' and [rstat] = 1";
        private static string strGetXmlResource = "select top 1 xml from resource where ivoid = '$1' and ([rstat] > 0) order by [harvestedfromdate] desc";
 
        public static validationStatus CheckForCapabilities(ref XmlDocument doc)
        {
            validationStatus status = new validationStatus();
            string xsitype = doc.DocumentElement.Attributes["xsi:type"].InnerText;

            if (xsitype.ToLower().Contains("service"))
            {
                XmlNodeList capabilities = doc.GetElementsByTagName("capability");
                if (capabilities.Count < 1)
                    status = new validationStatus("Services must contain at least one capability.");
            }
            return status;
        }

        public static validationStatus CheckValidIdentifier(ref XmlDocument doc, ref string[] userAuthorities)
        {
            XmlNodeList list = doc.GetElementsByTagName("identifier");
            if (list.Count != 1)
                return new validationStatus("Resources must contain a single identifier tag.");

            string id = list[0].InnerText;
            bool bFoundValidAuth = false;

            if (id.Contains("#"))
                return new validationStatus("Resource identifier may not contain hash tags.");
            if (id == "ivo://")
                return new validationStatus("Resource identifier must have additional information added after the base value for your archive.");

            if (userAuthorities.Length == 0)
            {
                validationStatus status = TestNewAuthority(doc, id);
                if (status.IsValid)
                {
                    userAuthorities = new string[] { id };
                }
                return status;
            }
            else
            {
                foreach (string auth in userAuthorities)
                {
                    if (id.StartsWith(auth))
                    {
                        if (id == auth + '/' ||  (id == auth && doc.FirstChild.Attributes["xsi:type"].Value != "vg:Authority"))
                            return new validationStatus("Resource identifier must have additional information added after the base value for your archive");
                        else if ((id == auth) || id.Length > auth.Length && id[auth.Length] == '/') //edit or first publish, with split.
                        {
                            bFoundValidAuth = true;
                            break;
                        }
                    }
                }
                if (!bFoundValidAuth)
                {
                    string errorText = "The authority portion of your identifier must be an archive with which you are associated. These are: ";
                    for (int i = 0; i < userAuthorities.Length; ++i)
                    {
                        errorText += userAuthorities[i];
                        if (i < userAuthorities.Length - 1)
                            errorText += ", ";
                    }
                    return new validationStatus(errorText);
                }
            }

            return new validationStatus();
        }

        public static validationStatus CleanupAndVerifyData(ref string strDoc, ref string[] userAuths)
        {
            validationStatus vstatus = new validationStatus();

            vstatus += CleanupEmptyIDReferences(ref strDoc);

            //easier to work with as a document here.
            XmlDocument doc = new XmlDocument();
            doc.LoadXml(strDoc);

            vstatus += CleanupUnusedResourceElements(ref doc);
            vstatus += CleanupAccessURLs(ref doc);
            vstatus += CheckForCapabilities(ref doc);
            vstatus += CheckValidIdentifier(ref doc, ref userAuths);
 
            System.IO.StringWriter sw = new System.IO.StringWriter();
            XmlTextWriter xw = new XmlTextWriter(sw);
            doc.WriteTo(xw);
            strDoc = sw.ToString();

            return vstatus;
        }

        private static validationStatus CleanupEmptyIDReferences(ref string strDoc)
        {
            validationStatus status = new validationStatus();

            strDoc.Replace("ivo-id=\"\"", "");
            strDoc.Replace("version=\"\"", "");

            return status;
        }

        private static validationStatus CleanupAccessURLs(ref XmlDocument doc)
        {
            validationStatus status = new validationStatus();

            XmlNodeList capabilities = doc.GetElementsByTagName("capability");
            foreach (XmlNode capability in capabilities)
            {
                if (capability.Attributes["standardID"] != null)
                {
                    foreach (XmlNode child in capability.ChildNodes)
                    {
                        if (child.Name == "interface")
                        {
                            foreach (XmlNode ifacechild in child.ChildNodes)
                            {
                                if (ifacechild.Name == "accessURL")
                                {
                                    string text = ifacechild.InnerText;
                                    if (!(text.EndsWith("?") || text.EndsWith("&")))
                                    {
                                        ifacechild.InnerText = ifacechild.InnerText.Trim();
                                        if (text.IndexOf('?') > -1)
                                            ifacechild.InnerText += '&';
                                        else
                                            ifacechild.InnerText += '?';
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return status;
        }

        private static validationStatus CleanupUnusedResourceElements(ref XmlDocument doc)
        {
            validationStatus status = new validationStatus();

            //?RemoveEmptyNodes(ref doc, "name");
            //?RemoveEmptyNodes(ref doc, "logo");
            RemoveEmptyNodes(ref doc, "creator");
            RemoveEmptyNodes(ref doc, "contributor");
            RemoveEmptyNodes(ref doc, "version");
            RemoveEmptyNodes(ref doc, "date");
            RemoveEmptyNodes(ref doc, "contact");
            RemoveEmptyNodes(ref doc, "subject");
            RemoveEmptyNodes(ref doc, "source");
            RemoveEmptyNodes(ref doc, "type");
            RemoveEmptyNodes(ref doc, "contentLevel");
            RemoveEmptyNodes(ref doc, "relationship");
            RemoveEmptyNodes(ref doc, "coverage");
            RemoveEmptyNodes(ref doc, "waveband");
            //RemoveEmptyNodes(ref doc, "publisher");

            RemoveEmptyIdentifiers(ref doc, "publisher");

            RemoveEmptyNodes(ref doc, "capability");
            RemoveEmptyNodes(ref doc, "maxRecords");
            RemoveEmptyNodes(ref doc, "maxFileSize");
            RemoveEmptyNodes(ref doc, "queryType");
            RemoveEmptyNodes(ref doc, "resultType");

            //Authority specific
            RemoveEmptyNodes(ref doc, "managingOrg");

            //cone search specific
            RemoveEmptyNodes(ref doc, "maxSR");
            RemoveEmptyNodes(ref doc, "ra");
            RemoveEmptyNodes(ref doc, "dec");
            RemoveEmptyNodes(ref doc, "sr");
            RemoveEmptyNodes(ref doc, "testQuery");
            RemoveEmptyNodes(ref doc, "verbosity");

            //SIA specific
            RemoveEmptyNodes(ref doc, "long");
            RemoveEmptyNodes(ref doc, "lat");
            RemoveEmptyNodes(ref doc, "maxImageExtent");
            RemoveEmptyNodes(ref doc, "maxImageSize");
            RemoveEmptyNodes(ref doc, "imageServiceType");

            //SSA specific
            RemoveEmptyNodes(ref doc, "creationType");
            RemoveEmptyNodes(ref doc, "defaultMaxRecords");
            RemoveEmptyNodes(ref doc, "maxAperture");
            //RemoveEmptyNodes(ref doc, "complianceLevel");
            //RemoveEmptyNodes(ref doc, "dataSource");

            //remove empty interfaces in otherwise non-empty capabilities
            XmlNodeList list = doc.GetElementsByTagName("capability");
            foreach (XmlNode node in list)
            {
                XmlNodeList children = node.ChildNodes;
                ArrayList emptyInterfaces = new ArrayList();
                bool bNonEmptyInterfaces = false;
                foreach (XmlNode child in children)
                {
                    if (child.Name.ToLower() == "interface")
                    {
                        if (child.InnerText == string.Empty)
                            emptyInterfaces.Add(child);
                        else
                            bNonEmptyInterfaces = true;
                    }
                }
                if (bNonEmptyInterfaces)
                {
                    foreach (XmlNode child in emptyInterfaces)
                        child.ParentNode.RemoveChild(child);
                }
                else
                    status.MarkInvalid("Capabilities must contain at least one specified interface with a valid accessURL, or be entirely empty");
            }

            //table-specific
            list = doc.GetElementsByTagName("table");
            if (list != null && list.Count > 0)
            {
                XmlNode node = list[0];
                ArrayList emptyElements = new ArrayList();
                foreach (XmlNode child in node.ChildNodes)
                {
                    if (child.Name.ToLower() == "column" && child.InnerText == string.Empty)
                        emptyElements.Add(child);
                    else if (child.Name.ToLower() == "name" && child.InnerText == string.Empty)
                        emptyElements.Add(child);
                    else if (child.Name.ToLower() == "description" && child.InnerText == string.Empty)
                        emptyElements.Add(child);
                    //note does not remove empty column-level fields if any column-level data is present at all: only going down one level.
                }
                foreach (XmlNode child in emptyElements)
                    child.ParentNode.RemoveChild(child);
                RemoveEmptyNodes(ref doc, "table");
            }

            return status;
        }

        private static void RemoveEmptyIdentifiers(ref XmlDocument doc, string tag)
        {
            XmlNodeList tagged = doc.GetElementsByTagName(tag);
            foreach (XmlNode node in tagged)
            {
                XmlAttribute id = node.Attributes["ivo-id"];
                if (id != null && id.Value == string.Empty)
                    node.Attributes.Remove(id);
            }
        }

        private static void RemoveEmptyNodes(ref XmlDocument doc, string tag)
        {
            XmlNodeList tagged = doc.GetElementsByTagName(tag);
            ArrayList nodesToRemove = new ArrayList();
            foreach (XmlNode node in tagged)
                if (node.InnerText == string.Empty) //todo check for state of being empty
                    nodesToRemove.Add(node);
            foreach (XmlNode node in nodesToRemove)
                node.ParentNode.RemoveChild(node);
        }

        public static validationStatus IngestXmlResource(XmlDocument doc, bool isNewResource, string specifiedIdentifier, long ukey = 0)
        {
            validationStatus status = TestIdentifier(doc, isNewResource, specifiedIdentifier);
            if (status.IsValid)
                status = UpdateTimeInformation(ref doc, isNewResource);
            if (status.IsValid)
            {
                registry.VOR_XML loader = new registry.VOR_XML();
                System.Text.StringBuilder sb = new System.Text.StringBuilder();
                System.IO.StringWriter sw = new System.IO.StringWriter();
	            XmlTextWriter xw = new XmlTextWriter(sw);
	            doc.WriteTo(xw);
                int iStatus = loader.LoadVORXML(sw.ToString(), ukey, string.Empty, string.Empty, sb);
                if (iStatus != 0) { status.MarkInvalid(sb.ToString()); }
            }
            return status;
        }

        public static validationStatus SetXmlResourceStatus(string specifiedIdentifier, string resourceStatus, long ukey = 0)
        {
            if (resourceStatus != null)
            {
                resourceStatus = resourceStatus.ToLower().Trim();
                if (resourceStatus != "active" && resourceStatus != "inactive" && resourceStatus != "deleted")
                {
                    return new validationStatus("invalid resource status");
                }
            }

            XmlDocument doc = new XmlDocument();
            validationStatus status = GetExistingResource(specifiedIdentifier, ref doc, false);
            if (status.IsValid)
                status = UpdateTimeInformation(ref doc, false);
            if (status.IsValid)
                status = UpdateStatusInformation(ref doc, resourceStatus);

            if (status.IsValid)
            {
                registry.VOR_XML loader = new registry.VOR_XML();
                System.Text.StringBuilder sb = new System.Text.StringBuilder();
                System.IO.StringWriter sw = new System.IO.StringWriter();
                XmlTextWriter xw = new XmlTextWriter(sw);
                doc.WriteTo(xw);
                int iStatus = loader.LoadVORXML(sw.ToString(), ukey, string.Empty, string.Empty, sb);
                if (iStatus != 0) { status.MarkInvalid(sb.ToString()); }
            }
            return status;
        }

        private static validationStatus TestNewAuthority(XmlDocument doc, string id)
        {
            validationStatus status = TestIdentifier(doc, true, id);
            if (!status.IsValid)
                return status;

            //test for authorities starting with full ID, or with ID subset?
            DataSet ds = new DataSet();
            try
            {
                ds = QueryRegistry(strCheckAuthorities.Replace("$1", id));
                if (ds.Tables[0].Rows.Count > 0)
                    status.MarkInvalid("Identifier " + id + " matches an Authority record already known to the IVOA registry network. Please try another.");
            }
            catch (Exception ex)
            {
                status.MarkInvalid("Issue querying registry for identifier " + id + ". " + ex.Message);
                return status;
            }
            return status;
        }

        private static validationStatus TestIdentifier(XmlDocument doc, bool isNewRecord, string specifiedIdentifier)
        {
            validationStatus status = new validationStatus();
            XmlNodeList list = doc.GetElementsByTagName("identifier");
            if (list.Count == 1)
            {
                string id = list[0].InnerText;
                if (specifiedIdentifier != string.Empty && specifiedIdentifier != id)
                    status.MarkInvalid("Specified identifier does not match identifier element in resource XML");

                return status + IsValidIdentifier(id, isNewRecord);
            }

            return new validationStatus("Resource XML document does not contain an identifier element");
        }

        public static validationStatus IsValidIdentifier(string id, bool isNew)
        {
            validationStatus status = new validationStatus();
            DataSet ds = null;
            if (!id.StartsWith("ivo://"))
            {
                status.MarkInvalid("Identifier must begin with 'ivo://'");
                return status;
            }
            else
            {
                try
                {
                    ds = QueryRegistry(strCheckIdentifier.Replace("$1", id));
                }
                catch (Exception ex)
                {
                    status.MarkInvalid("Issue querying registry for identifier " + id + ". " + ex.Message);
                    return status;
                }

                if (isNew) //looking for NO records.
                {
                    if (ds.Tables[0].Rows.Count > 0)
                        status.MarkInvalid("Resource identifier " + id + " already exists in the registry.");
                }
                else //looking for *our* records.
                {
                    if (ds.Tables[0].Rows.Count == 0)
                        status.MarkInvalid("Resource identifier " + id + " does not exist in the registry and cannot be edited.");
                }
 
            }
            return status;
        }

        //update resource @updated attribute to now. If is a new record, also set @created.
        public static validationStatus UpdateTimeInformation(ref XmlDocument doc, bool isNew)
        {
            validationStatus status = new validationStatus();
            try
            {
                DateTime now = DateTime.UtcNow;
                XmlNodeList list = doc.GetElementsByTagName("ri:Resource");
                if (list.Count == 0)
                    list = doc.GetElementsByTagName("Resource");
                list[0].Attributes["updated"].Value = String.Format("{0:s}", now);
                if (isNew)
                {
                    list = doc.GetElementsByTagName("ri:Resource");
                    if (list.Count == 0)
                        list = doc.GetElementsByTagName("Resource");
                    list[0].Attributes["created"].Value = String.Format("{0:s}", now);
                }
            }
            catch( Exception ex )
            {
                status.MarkInvalid(ex.Message);
            }
            return status;
        }

        //update resource @status.
        public static validationStatus UpdateStatusInformation(ref XmlDocument doc, string newStatus)
        {
            validationStatus status = new validationStatus();
            try
            {
                DateTime now = DateTime.UtcNow;
                XmlNodeList list = doc.GetElementsByTagName("ri:Resource");
                if (list.Count == 0)
                    list = doc.GetElementsByTagName("Resource");
                list[0].Attributes["status"].Value = newStatus;
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.Message);
            }
            return status;
        }

        public static validationStatus GetExistingResource(string identifier, ref string text, bool bOnlyActive = true)
        {
            validationStatus status = new validationStatus();
            try
            {
                if (bOnlyActive)
                {
                    DataSet ds = QueryRegistry(strGetActiveXmlResource.Replace("$1", identifier));
                    if (ds.Tables[0].Rows.Count == 0)
                        status.MarkInvalid("No active resources found in registry with identifier " + identifier);
                    else if (ds.Tables[0].Rows.Count > 1)
                        status.MarkInvalid("Database error: more than one active resource found in registry with identifier " + identifier);
                    else
                        text = (string)ds.Tables[0].Rows[0][0];
                }
                else
                {
                    DataSet ds = QueryRegistry(strGetXmlResource.Replace("$1", identifier));
                    if (ds.Tables[0].Rows.Count == 0)
                        status.MarkInvalid("No resources found in registry with identifier " + identifier);
                    else
                        text = (string)ds.Tables[0].Rows[0][0];
                }
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.Message);
            }

            return status;
        }

        public static validationStatus GetExistingResource(string identifier, ref XmlDocument doc, bool bOnlyActive = true)
        {
            validationStatus status = new validationStatus();
            try
            {
                string text = string.Empty;
                status += GetExistingResource(identifier, ref text, bOnlyActive);
                doc.LoadXml(text);
            }
            catch (Exception ex)
            {
                status.MarkInvalid("Error validating XML while loading resource " + identifier + ": " + ex.Message);
            }
            return status;
        }

        public static validationStatus GetEmptyResource(string identifier, ref XmlDocument doc)
        {
            validationStatus status = new validationStatus();
            try
            {
                System.IO.StreamReader myFile = new System.IO.StreamReader(appDir + "EmptyVOResource.xml");
                string myString = myFile.ReadToEnd();
                myFile.Close();
                doc.LoadXml(myString);            
            }
            catch (Exception ex)
            {
                status.MarkInvalid("Error generating empty resource file: " + ex.Message);
            }

            return status;
        }

        //this will become a laundry-list of known issues with various resources throughout the IVOA RofR
        public static validationStatus RepairExistingResource(ref XmlDocument doc)
        {
            validationStatus status = new validationStatus();
            XmlNodeList list = doc.FirstChild.ChildNodes;
            if (list.Count == 0) //some top-level xml shenanigans
                list = doc.FirstChild.NextSibling.ChildNodes;
            bool foundTopLevelValidationLevel = false;
            foreach( XmlNode node in list)
            {
                if ((node.Name.ToLower() == "validationlevel") && ! (node.ParentNode.Name == "capability"))
                {
                    foundTopLevelValidationLevel = true;
                    break;
                }
            }
            if( !foundTopLevelValidationLevel)
            {
                XmlElement newNode = doc.CreateElement("validationLevel");
                XmlAttribute newAttribute = doc.CreateAttribute("validatedBy");
                newAttribute.Value = strValidatedBy;
                newNode.Attributes.Append(newAttribute);
                newNode.InnerXml = "2";
                doc.GetElementsByTagName("ri:Resource")[0].InsertBefore(newNode, doc.GetElementsByTagName("ri:Resource")[0].FirstChild);
            }

            list = doc.GetElementsByTagName("capability");
            foreach (XmlNode node in list)
            {
                if (!node.InnerXml.Contains("<validationLevel"))
                {
                    XmlElement newNode = doc.CreateElement("validationLevel");
                    XmlAttribute newAttribute = doc.CreateAttribute("validatedBy");
                    newAttribute.Value = strValidatedBy;
                    newNode.Attributes.Append(newAttribute);
                    newNode.InnerXml = "2";
                    node.InsertBefore(newNode, node.FirstChild);
                }
            }
            return status;
        }

        private static DataSet QueryRegistry(string cmd)
        {
            SqlConnection conn = new SqlConnection();
            try
            {
                conn = new SqlConnection(sqlConnection);
                conn.Open();

                string sQuery = cmd;
                SqlDataAdapter sqlDA = new SqlDataAdapter(sQuery, conn);
                DataSet ds = new DataSet();
                sqlDA.Fill(ds);
                return ds;
            }
            finally
            {
                conn.Close();
            }
        }

        public static string SetAllValidationLevels(int capacity)
        {
            validationStatus status = new validationStatus();
            try
            {
                DataSet ds = QueryRegistry("select top " + capacity + " ivoid from resource where [rstat] = 1 and validationLevel = 1");
                int rows = 0;
                foreach (DataRow row in ds.Tables[0].Rows)
                {
                    status += SetValidationLevels((string)row[0]);
                    if (!status.IsValid)
                        break;
                    ++rows;
                }
                if( status.IsValid )
                    return rows + " rows changed.";
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.ToString());
            }
            return "error(s): " + status.GetConcatenatedErrors(". ");
        }

        public static validationStatus SetValidationLevels(string identifier)
        {
            validationStatus status = new validationStatus();
            XmlDocument doc = new XmlDocument();
            status += ResourceManagement.GetExistingResource(identifier, ref doc);
            bool bChanged = false;
            if (status.IsValid)
            {
                try
                {
                    XmlNodeList list = doc.FirstChild.ChildNodes;
                    foreach (XmlNode node in list)
                    {
                        if ((node.Name.ToLower() == "validationlevel") && !(node.ParentNode.Name == "capability") && node.InnerXml != "2")
                        {
                            if (node.Attributes["validatedBy"] != null)
                                node.Attributes["validatedBy"].Value = strValidatedBy;
                            node.InnerXml = "2";
                            bChanged = true;
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
                                bChanged = true;
                                break;
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    status.MarkInvalid(ex.ToString());
                }
            }
            if (status.IsValid && bChanged)
            {
                IngestXmlResource(doc, false, identifier);
            }

            return status;
        }
    }
}