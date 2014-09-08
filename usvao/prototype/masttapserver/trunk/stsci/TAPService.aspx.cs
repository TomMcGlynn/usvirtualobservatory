
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

using net.ivoa.VOTable;
using TAPService;

using tapLib;

namespace ivoa.net.TAP
{
	public class TAPPage : System.Web.UI.Page
	{
		private void Page_Load(object sender, System.EventArgs e)
        {
            System.Collections.Specialized.NameValueCollection input = Request.QueryString;
            if (Request.RequestType == "POST")
                input = Request.Form;
            string restPath = Request.PathInfo;
            if (restPath.Length > 1) 
                restPath = restPath.Substring(1);

            Response.Clear();
            Response.ClearHeaders();
            Response.ClearContent();
            Response.ContentType = "text/xml";

            Object response = HandleRequest(input, restPath, Request.RequestType);

            try
            {
                XmlSerializer serializer = new XmlSerializer(response.GetType());
                XmlTextWriter xw = new XmlTextWriter(Response.OutputStream, System.Text.Encoding.UTF8);

                //because .NET 'helpfully' adds nonsense for some standard namespaces if given a chance.
                XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
                ns.Add("xlink", "http://www.w3.org/1999/xlink");
                ns.Add("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                serializer.Serialize(xw, response, ns);
                xw.Close();
            }
            catch (Exception ex)
            {
                Response.ContentType = "text/plain";
                Response.Output.WriteLine("Error Handling Request: " + ex.Message);
            }

            Response.End();
        }

        public Object HandleRequest(System.Collections.Specialized.NameValueCollection input, string restPath, string requestType)
        {
            Object results = new VOTABLE();
            string errorString = string.Empty;
            string redirect = string.Empty;

            try
            {
                if (restPath.ToUpper().StartsWith("SYNC"))
                {
                    tapLib.Exec.RequestHandler handler = new tapLib.Exec.RequestHandler();
                    results = handler.doSync(input);
                }
                else if (restPath.ToUpper().StartsWith("ASYNC"))
                {
                    UWSLib.UWSHandler handler = new UWSLib.UWSHandler();
                    results = handler.doAsync(input, restPath, ref redirect, requestType);
                    if (redirect != string.Empty)
                    {
                        Response.StatusCode = 303;
                        Response.AddHeader("Location", redirect);
                    }
                }
                else
                {
                    errorString = "Missing Request Parameter";
                    results = VOTableUtil.CreateErrorVOTable(errorString);
                }
            }
            catch (Exception e)
            {
                results = VOTableUtil.CreateErrorVOTable(errorString + " " + e.Message);
            }

            return results;
        }

        #region Web Form Designer generated code
        override protected void OnInit(EventArgs e)
        {
            //
            // CODEGEN: This call is required by the ASP.NET Web Form Designer.
            //
            InitializeComponent();
            base.OnInit(e);
        }

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.Load += new System.EventHandler(this.Page_Load);
        }
        #endregion
    }
}
