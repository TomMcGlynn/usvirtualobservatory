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

namespace registry
{

    public partial class riws : System.Web.UI.Page
    {
        protected string _wsUrl = Properties.Settings.Default.baseURL;

        protected void Page_Load(object sender, EventArgs e)
        {
            if (!_wsUrl.EndsWith("/")) _wsUrl += '/';

            Page.Title = "NVO - Registry Webservices";
            string lnkRIWS = _wsUrl + "RIWebService.asmx";
            string lnkRIWSWSDL = lnkRIWS + "?WSDL";
            string lnkRegOAIWS = _wsUrl + "STOAI.asmx";
            string lnkRegOAIWSDL = lnkRegOAIWS + "?WSDL";
        }
    }
}
