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


public partial class querytest : System.Web.UI.Page
{
    private static string baseURL = registry.Properties.Settings.Default.baseURL;
    private static string vdir = registry.Properties.Settings.Default.vdir;

    protected void Page_Load(object sender, EventArgs e)
    {
        try
        {
            StreamReader reader = new StreamReader(HttpContext.Current.Request.InputStream);
            String data = reader.ReadToEnd();
        }
        catch (Exception ex)
        {
            Response.Write(ex.Message);
        }
    }
}
