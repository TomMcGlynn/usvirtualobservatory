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

public partial class riws : System.Web.UI.Page
{
    protected static string _wsUrl = System.Configuration.ConfigurationSettings.AppSettings["baseURL"];

    protected void Page_Load(object sender, EventArgs e)
    {

        Page.Title = "NVO - Registry Webservices";
        //_wsUrl = "http://nvo.stsci.edu/vor10/";
        string lnkRIWS      = _wsUrl + "Service.asmx";
        string lnkRIWSWSDL  = lnkRIWS + "?WSDL";
//        string lnkFootprint         = _wsUrl + "HSTFootprint.asmx";
//        string lnkFootprintWSDL     = lnkFootprint  + "?WSDL";
    }
}
