using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.IO;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

using registry;
using ivoa.net.ri1_0.server;
using ivoa.altVOTable;

using System.Xml.XPath;
using System.Xml.Xsl;


public partial class savexml : System.Web.UI.Page
{
    protected static string _wsdir = registry.Properties.Settings.Default.vdir;
    protected static string baseURL = registry.Properties.Settings.Default.baseURL;

    protected void Page_Load(object sender, EventArgs e)
    {
        try
        {
            if (!baseURL.EndsWith("/")) baseURL += '/';
            if (!_wsdir.EndsWith("\\")) _wsdir += "\\";

            string resourceList = Request.Form["resourceList"];
            if (resourceList != null)
            {
                FileStream file = null;
                string resourceListFilename = "resourcelists\\" + System.Guid.NewGuid().ToString("N") + ".xml";

                //we have a list of identifiers. make a VOTable out of them.
                registry.Registry reg = new registry.Registry();
                string identifiers = "('ivo://" + resourceList.Replace("|", "', 'ivo://") + "')";
                try
                {
                    ivoa.net.ri1_0.server.Resource[] reses = reg.QueryFullVOR10Resource("identifier in " + identifiers);
                    if (reses.Length > 0)
                    {
                        VOResources vres = new VOResources();
                        vres.Items = reses;

                        XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.ri1_0.server.VOResources));
                        StringBuilder sb = new StringBuilder();
                        StringWriter sw = new StringWriter(sb);

                        XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
                        ns.Add("", "http://www.ivoa.net/xml/RegistryInterface/v1.0");

                        ser.Serialize(sw, vres, ns);
                        sw.Close();

                        // Create reader for the doc.
                        StringReader reader = new StringReader(sb.ToString());
                        XPathDocument myXPathDoc = new XPathDocument(reader);
                        XslCompiledTransform myXslTrans = new XslCompiledTransform();

                        //load the Xsl and transform to string VOTABLE
                        StringBuilder sbVOT = new StringBuilder();
                        StringWriter swVOT = new StringWriter(sbVOT);
                        myXslTrans.Load(baseURL + "xsl/RegistryResults_vot3.xsl");
                        XmlTextWriter writer = new XmlTextWriter(swVOT);
                        myXslTrans.Transform(myXPathDoc, null, writer);

                        StringReader srVOT = new StringReader(sbVOT.ToString());
                        identifiers = srVOT.ReadToEnd();

                        file = System.IO.File.Open(_wsdir + resourceListFilename, FileMode.Append);
                    }
                }
                catch (Exception /*ex*/)
                {
                    /*Response.ContentType = "text/plain";
                    Response.Write(ex.Message);
                    Response.End();*/
                }

                if (file != null)
                {
                    StreamWriter sw = new StreamWriter(file);
                    sw.Write(identifiers);
                    sw.Close();
                    file.Close();

                    Response.ContentType = "text/plain";
                    string url = baseURL + resourceListFilename;
                    url = url.Replace("\\", "/");
                    Response.Write(url);
                    Response.End();
                }
                else
                {
                    Response.ContentType = "text/plain";
                    Response.Write(resourceListFilename);
                    Response.End();
                }


                //cleanup old files while we're here.
                DirectoryInfo di = new DirectoryInfo(_wsdir + "resourceLists");
                FileInfo[] rgFiles = di.GetFiles("*.xml");
                foreach (FileInfo fi in rgFiles)
                {
                    if (fi.CreationTime.AddDays(3) < DateTime.Now)
                    {
                        string filename = _wsdir + "resourceLists\\" + fi.Name;
                        System.IO.File.Delete(filename);
                    }
                }

            }
            else
            {
                string format = Request.Form["format"];
                string str = Request.Form["save"];
                if (format == null || str == null)
                    return;

                if (format.CompareTo("csv") == 0)
                {
                    Response.ContentType = "text/plain";
                    Response.AppendHeader("content-disposition", "attachment; filename=\"RegistryMetadata.csv\"");
                }
                else if (format.CompareTo("xml") == 0)
                {
                    Response.ContentType = "text/xml";
                    Response.AppendHeader("content-disposition", "attachment; filename=\"RegistryMetadata.xml\"");
                }
                Response.Write(str);
                Response.End();
            }

        }
        catch (Exception ex)
        {
            ex.ToString();
        }
    }
}
