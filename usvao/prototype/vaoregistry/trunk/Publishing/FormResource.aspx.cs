using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using System.Xml;

namespace Publishing
{
    public partial class FormResource : System.Web.UI.Page
    {
        bool isNewRecord = true;
        string strQueryIdentifier = string.Empty;

        protected void Page_Load(object sender, EventArgs e)
        {
            ToggleControls(false);
            validationStatus status = ValidateAndSetQueryParameters();
            if (status.IsValid)
            {
                if (!Page.IsPostBack)
                {
                    Session["docResource"] = new XmlDocument();
                    Session["docAsLoaded"] = string.Empty;
                    if (isNewRecord)
                        status += LoadNewResourceXML();
                    else
                        status += LoadExistingResourceXML();
                    if (status.IsValid)
                        status += LoadStaticFormData();
                }
                if (status.IsValid)
                    status += GenerateDynamicFormData();
            }

            if (status.IsValid)
                ToggleControls(true);
            else
            {
                ToggleControls(false); //catch any that hadn't yet been generated when we toggled everything off at first
                LabelErrorMessage.Text = "Error: " + status.GetConcatenatedErrors("<br/>");
            }
        }

        private validationStatus ValidateAndSetQueryParameters()
        {
            validationStatus status = new validationStatus();
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
            else if (isNewRecord == false)
                status.MarkInvalid("Edit mode cannot be loaded without a given identifier.");

            return status;
        }

        private void ToggleControls(bool toggle)
        {
            foreach( Control control in form.Controls )
            {
                if( control is WebControl && control != LabelErrorMessage && control != LabelCreateEdit)
                     ((WebControl)control).Visible = toggle;
            }
        }

        private validationStatus LoadNewResourceXML()
        {
            LabelCreateEdit.Text = "Resource Management: Publish New Resource";
            validationStatus status = new validationStatus();
            try
            {
                XmlDocument docResource = (XmlDocument)Session["docResource"];
                status += ResourceManagement.GetEmptyResource(strQueryIdentifier, ref docResource);
                if (status.IsValid)
                {
                    Session["docResource"] = docResource;
                }
            }
            catch (Exception ex)
            {
                status.MarkInvalid("Server error loading example resource. " + ex.Message);
            }

            return status;
        }

        private validationStatus LoadExistingResourceXML()
        {
            validationStatus status = new validationStatus();
            LabelCreateEdit.Text = "Resource Management: Edit Existing Resource";
 
            if (strQueryIdentifier == string.Empty)
                status.MarkInvalid("Edit mode cannot be loaded without a specified identifier in the Request URL parameters.");
            else
            {
                try
                {
                    XmlDocument docResource = (XmlDocument)Session["docResource"];
                    status = ResourceManagement.GetExistingResource(strQueryIdentifier, ref docResource);
                    if (status.IsValid)
                    {
                        Session["docResource"] = docResource;
                        Session["docAsLoaded"] = docResource.InnerXml;
                     }
                }
                catch( Exception ex )
                {
                    status.MarkInvalid("Error loading existing resource for edit: " + ex.Message);
                }
            }
            return status;
        }

        private validationStatus LoadStaticFormData()
        {
            validationStatus status = new validationStatus();
            try
            {
                SetupIdentifier();
                SelectTab(form.FindControl("Tab_PanelMain"), new EventArgs());
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.Message);
            }
            return status;
        }

        private validationStatus GenerateDynamicFormData()
        {
            validationStatus status = new validationStatus();

            if (!status.IsValid)
                LabelErrorMessage.Text = status.GetConcatenatedErrors("<br/>");

            return status;
        }

        private void SetupIdentifier()
        {
            if (strQueryIdentifier == string.Empty)
            {
                TextBox_identifier.Text = "ivo://";
            }
            else
            {
                TextBox_identifier.Text = strQueryIdentifier;
                //if (!isNewRecord)
                    TextBox_identifier.Enabled = false;
            }
        }

        private validationStatus SaveDocToRegistry()
        {
            validationStatus status = new validationStatus();

            try
            {
                string docAsSubmitted = ((XmlDocument)Session["docResource"]).InnerXml;
                if (docAsSubmitted == (string)Session["docAsLoaded"])
                    status.MarkInvalid("No changes to save.");
                else
                {
                    status += ResourceManagement.PublishXmlResource(docAsSubmitted, isNewRecord, strQueryIdentifier);
                }
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.Message);
            }

            return status;
        }

        protected void ButtonCancel_Click(object sender, EventArgs e)
        {

        }

        protected void ButtonSubmit_Click(object sender, EventArgs e)
        {
            validationStatus status = SaveDocToRegistry();
            if (!status.IsValid)
            {
                LabelErrorMessage.Text = "Error(s) saving resource data: " + status.GetConcatenatedErrors("<br/>");
            }
        }

        //todo: temp object, only sync these up as necessary.
        protected void TextBoxUniqueElement_TextChanged(object sender, EventArgs e)
        {
            string element = ((WebControl)sender).ID.Replace("TextBox_", "");
            XmlNodeList idlist = ((XmlDocument)Session["docResource"]).GetElementsByTagName(element);
            if (idlist != null && idlist.Count == 1)
                ((XmlDocument)Session["docResource"]).GetElementsByTagName(element)[0].InnerXml = ((TextBox)sender).Text;
        }

        protected void SelectTab(object sender, EventArgs e)
        {
            string name = ((WebControl)sender).ID.Replace("Tab_", "");
            if (name == "PanelMain")
            {
                PanelMain.Visible = true;
                PanelCuration.Visible = false;
            }

        }
    }
}