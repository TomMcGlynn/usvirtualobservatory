using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Net;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Xml;
using System.Xml.Xsl;
using System.Xml.XPath;
using System.Xml.Serialization;
using System.Text;
using System.IO;


using registry;
using ivoa.net.ri1_0.server;
using ivoa.altVOTable;


public partial class getRecord : System.Web.UI.Page
{
    private static string baseURL = registry.Properties.Settings.Default.baseURL;
    private static string vdir = registry.Properties.Settings.Default.vdir;

    protected void Page_Load(object sender, EventArgs e)
    {
        try
        {
            if (!baseURL.EndsWith("/")) baseURL += '/';
            if (!vdir.EndsWith("\\")) vdir += "\\";

            string id = Request.Params["id"];

            string wholeParam = Request.Params.ToString();
            wholeParam = Uri.UnescapeDataString(wholeParam.Substring(wholeParam.IndexOf('=', wholeParam.IndexOf("id=")) + 1));
            if (wholeParam.Contains("&"))
                wholeParam = wholeParam.Substring(0, wholeParam.IndexOf('&'));
            if (wholeParam.IndexOf('+') >= 0)
                id = id.Remove(wholeParam.IndexOf('+'), 1).Insert(wholeParam.IndexOf('+'), "+");

            string sformat = Request.Params["format"];

            // Query the resource from the registry 
            registry.Registry reg = new registry.Registry();

            XmlDocument[] docs = reg.QueryRIResourceXMLDocAllResources("identifier like \'" + id + "'", false, true);
            if (docs.Length > 0) //we want the last, most recent one if there are multiple active/inactive/deleted versions
            {
                try
                {
                    StringWriter sw = new StringWriter();
                    XmlTextWriter xw = new XmlTextWriter(sw);
                    docs[docs.Length - 1].WriteTo(xw);
 
                    StringReader reader = new StringReader(sw.ToString());
                    XPathDocument myXPathDoc = new XPathDocument(reader);
                    XslCompiledTransform myXslTrans = new XslCompiledTransform();

                    XmlUrlResolver resolver = new XmlUrlResolver();
                    resolver.Credentials = CredentialCache.DefaultCredentials;

                    //load the Xsl
                    StringBuilder sbVOT = new StringBuilder();
                    StringWriter swVOT = new StringWriter(sbVOT);
                    myXslTrans.Load(baseURL + "xsl/nvobrowse.xsl", XsltSettings.TrustedXslt, resolver);
                    //Response.Write("loaded");
 
                    myXslTrans.Transform(myXPathDoc, null, swVOT);
                    //Response.Write("transformed");

                    if (sformat != null && sformat == "xml")
                    {
                        Response.ContentType = "text/xml";
                        Response.AppendHeader("content-disposition", "attachment; filename=\"RegistryMetadata.xml\"");
                        string response = sw.ToString().Replace("utf-16", "utf-8").Replace("><", ">\n<");
                        Response.Write(response);
                        //Response.End();
                    }
                    else
                    {
                        Response.Write(swVOT.ToString());
                        //Response.End();
                    }
                }
                catch (Exception ex)
                {
                    logfile errlog = new logfile("errXslt.log");
                    errlog.Log(ex.ToString());

                    //Response.Write(ex.ToString());
                    //Response.End();
                }
            }
            else
            {
                Response.Write("Cannot find resource '" + id + "' in registry");
                Response.End();
            }
        }
        catch (Exception ex)
        {
            ex.ToString();
        }
    }
}
