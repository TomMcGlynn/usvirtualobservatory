using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using System.Xml;

namespace Publishing
{
    public partial class XmlResource : System.Web.UI.Page
    {
        bool isNewRecord = true;
        string strQueryIdentifier = string.Empty;

        protected void Page_Load(object sender, EventArgs e)
        {
            ValidateAndSetQueryParameters();
            if (! Page.IsPostBack)
            {
                if (isNewRecord)
                    ShowNewResource();
                else
                    ShowExistingResource();
            }
         }

        private void ShowNewResource()
        {
            LabelCreateEdit.Text = "Publish New Resource: Raw XML Format";
            TextBoxResource.Enabled = true;
        }

        private void ShowExistingResource()
        {
            LabelCreateEdit.Text = "Edit Existing Resource: Raw XML Format";
            TextBoxResource.Enabled = false;

            try
            {
                if (strQueryIdentifier == string.Empty)
                    LabelErrorMessage.Text = "Edit mode cannot be loaded without a specified identifier in the Request URL parameters.";
                else
                {
                    string text = string.Empty;
                    LabelErrorMessage.Text = strQueryIdentifier;
                    validationStatus status = ResourceManagement.GetExistingResource(strQueryIdentifier, ref text);
                    if (status.IsValid)
                    {
                        TextBoxResource.Enabled = true;
                        TextBoxResource.Text = text;
                    }
                    else
                    {
                        LabelErrorMessage.Text = "Error loading existing resource for edit: " + status.GetConcatenatedErrors("<br/>");
                    }
                }
            }
            catch (Exception ex)
            {
                LabelErrorMessage.Text = "Error loading existing resource for edit: " + ex.Message;
            }
        }

        private void ValidateAndSetQueryParameters()
        {
            System.Collections.Specialized.NameValueCollection input = Request.QueryString;
 
            if (input["mode"] != null)
            {
                if (input["mode"].ToLower() == "edit")
                    isNewRecord = false;
                else if (input["mode"].ToLower() == "new")
                    isNewRecord = true;
                else
                    Response.Redirect(Request.Url.GetLeftPart(UriPartial.Path));
            }
            if (input["identifier"] != null)
            {
                strQueryIdentifier = input["identifier"];
            }
        }

        protected void TextBoxResource_TextChanged(object sender, EventArgs e)
        {
        }

        protected void ButtonSubmit_Click(object sender, EventArgs e)
        {
            LabelErrorMessage.Text = string.Empty;
            validationStatus status = new validationStatus();
            try
            {
                XmlDocument doc = new XmlDocument();
                doc.LoadXml(TextBoxResource.Text);
                status = ResourceManagement.PublishXmlResource(doc, isNewRecord, strQueryIdentifier);
                if (status.IsValid)
                {
                    if( isNewRecord )
                        LabelErrorMessage.Text = "Resource published successfully!";
                    else 
                        LabelErrorMessage.Text = "Resource saved successfully!";
                }
                else
                    LabelErrorMessage.Text = "Error(s) publishing resource: " + status.GetConcatenatedErrors("<br/>");
            }
            catch (XmlException x)
            {
                LabelErrorMessage.Text = "Invalid XML in resource data: " + x.Message;
            }
            catch (Exception ex)
            {
                if (isNewRecord)
                    LabelErrorMessage.Text = "Error(s) creating new resource: " + ex.Message;
                else
                    LabelErrorMessage.Text = "Error(s) editing existing resource: " + ex.Message;
            }
        }

        protected void ButtonCancel_Click(object sender, EventArgs e)
        {

        }
    }
}