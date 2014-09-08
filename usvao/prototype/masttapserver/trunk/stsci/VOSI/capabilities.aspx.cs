
using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Web;
using System.Web.SessionState;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.HtmlControls;
using System.Text;
using System.IO;
using System.Net;
using System.Xml;
using System.Xml.Serialization;

namespace TAPService.VOSI
{

    public partial class capabilities : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            VOSIRequestHandler handler = new VOSIRequestHandler();
            Object response = handler.HandleRequest(typeof(capabilities));

            Response.Clear();
            Response.ClearHeaders();
            Response.ClearContent();
            Response.ContentType = "text/xml";

            try
            {
                Response.Write(response);
            }
            catch (Exception ex)
            {
                Response.ContentType = "text/plain";
                Response.Output.WriteLine("Error Handling Request: " + ex.Message);
            }

            Response.End();
        }
    }
}
