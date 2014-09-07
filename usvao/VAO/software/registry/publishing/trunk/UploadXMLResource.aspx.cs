using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Data;
using System.Data.SqlClient;
using System.Web.Services;
using System.Xml;

using registry;
using OperationsManagement;

namespace Publishing
{
    public partial class UploadXMLResource : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];
            else
            {
                ReturnFailure();
                return;
            }
            string[] userAuths = null;
            if (Session["userAuths"] != null)
                userAuths = (string[])Session["userAuths"];

            try
            {
                HttpFileCollection uploadedFiles = Request.Files;
                for (int i = 0; i < uploadedFiles.Count; i++)
                {
                    HttpPostedFile userPostedFile = uploadedFiles[i];
                    try
                    {
                        if (userPostedFile.ContentLength > 0)
                        {
                            System.IO.StreamReader streamReader = new System.IO.StreamReader(userPostedFile.InputStream);
                            string strResource = streamReader.ReadToEnd();
                            streamReader.Close();
                            strResource = Uri.UnescapeDataString(strResource);

                            string ivoid = FindIdentifier(strResource);
                            if (ivoid == string.Empty)
                            {
                                ReturnFailure("File does not contain a valid identifier record.");
                            }
                            else
                            {
                                try
                                {
                                    XmlDocument doc = new XmlDocument();
                                    doc.LoadXml(strResource);
                                    validationStatus status = ResourceManagement.IngestXmlResource(doc, true, ivoid, ukey);
                                    if (status.IsValid)
                                        ReturnSuccess("Publishing resource " + ivoid + " successful.");
                                    else
                                        ReturnFailure(status.GetConcatenatedErrors(", "));
                                }
                                catch (Exception ex)
                                {
                                    ReturnFailure(ex.Message);
                                }
                            }
                         }
                        else
                            ReturnFailure("File is empty or did not upload.");
                    }
                    catch (System.ArgumentOutOfRangeException)
                    {
                        ReturnFailure("File is not a valid XML Resource beginning with an ri:resource tag.");
                    }
                    catch (Exception Ex)
                    {
                        ReturnFailure("General failure reading file: " + Ex.Message);
                    }
                }
             }
            catch (Exception ex)
            {
                Response.Write("Error uploading XML resource: " + Server.UrlEncode(ex.Message));
            }
            Response.Flush();
        }

        private validationStatus PublishResource(HttpContext context)
        {
            long ukey = 0;
            if (Session["ukey"] != null)
                ukey = (long)Session["ukey"];

            validationStatus status = new validationStatus();
            try
            {
                HttpPostedFile fileupload = context.Request.Files["file"];

                // process your fileupload...
            }
            catch (Exception ex)
            {
                status.MarkInvalid("Server error loading example resource. " + ex.Message);
            }

            return status;
        }

        internal static string FindIdentifier(string strResource)
        {
            string id = string.Empty;
            System.IO.StringReader rd = new System.IO.StringReader(strResource);
            XmlReader xr = new XmlTextReader(rd);
            while (xr.Read())
            {
                string name = xr.LocalName.ToUpper();
                if ((name == "IDENTIFIER") && (xr.NodeType != XmlNodeType.EndElement))
                {
                    xr.Read();
                    id = xr.Value.Trim();
                    break;
                }
            }
            return id;
        }

        private void ReturnSuccess(string details)
        {
            Response.Write("{ 'success': true, 'details': '" + details + "'}");
            //Response.Flush();
        }

        //remove JSON-incompatible characters here.
        private void ReturnFailure(string error = null)
        {
            if (error != null)
            {
               string formattederror = error.Replace('\'', '`').Replace("\r\n", " ");
               Response.Write("{ 'success': false, 'details': '" + formattederror + "' }");
            }
            else
                Response.Write("{ 'success': false, 'details': 'Invalid login data. Session may have timed out.'}");
            //Response.Flush();
        }
    }
}