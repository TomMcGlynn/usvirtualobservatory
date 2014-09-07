using System;
using System.Collections.Generic;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

using System.Xml;

namespace OperationsManagement
{

    public partial class ValidationManagement : System.Web.UI.Page
    {
        string strQueryIdentifier = string.Empty;
        string strViewResourceURL = "http://vao.stsci.edu/directory/getRecord.aspx?id=";

        private static DropDownList CreateStatusDropDown(string id, string currentValue)
        {
            DropDownList list = new DropDownList();
            list.ID = id;

            list.Items.Add(new ListItem("active", "active"));
            list.Items.Add(new ListItem("inactive", "inactive"));
            list.Items.Add(new ListItem("deleted", "deleted"));

            if (list.Items.FindByValue(currentValue) != null) list.SelectedValue = currentValue;

            return list;
        }

        private static DropDownList CreateValidationDropDown(string id, string currentValue)
        {
            DropDownList list = new DropDownList();
            list.ID = id;

            list.Items.Add(new ListItem("0: noncompliant", "0"));
            list.Items.Add(new ListItem("1: syntactically compliant, not functional.", "1"));
            list.Items.Add(new ListItem("2: syntactically compliant, functional. STANDARD.", "2"));
            list.Items.Add(new ListItem("3: compliant, functional, semantically compliant, contains important metadata.", "3"));
            list.Items.Add(new ListItem("4: compliant, functional, human-judged excellent description.", "4"));

            if (list.Items.FindByValue(currentValue) != null) list.SelectedValue = currentValue;

            return list;
        }

        protected void Page_Load(object sender, EventArgs e)
        {
            validationStatus status = ValidateAndSetQueryParameters();
            if (status.IsValid)
            {
                if (!Page.IsPostBack)
                {
                    Session["docResource"] = new XmlDocument();
                    status += LoadExistingResourceXML();
                    if( status.IsValid )
                        status += LoadStaticFormData();
                }
                if (status.IsValid)
                    status += GenerateDynamicFormData();
            }

            if (!status.IsValid)
            {
                LabelErrorMessage.Text = status.GetConcatenatedErrors("<br/>");
                ToggleControls(false);
            }
        }

        private validationStatus ValidateAndSetQueryParameters()
        {
            System.Collections.Specialized.NameValueCollection input = Request.QueryString;

            if (input["identifier"] != null)
            {
                strQueryIdentifier = input["identifier"];
                return new validationStatus();
            }
            else
                return new validationStatus("No identifier specified in request URL");
        }

        private void ToggleControls(bool toggle)
        {
            foreach (Control control in this.validationform.Controls)
            {
                if (control is WebControl && control != LabelErrorMessage)
                {
                    ((WebControl)control).Visible = toggle;
                }
            }
        }

        private validationStatus LoadExistingResourceXML()
        {
            ToggleControls(false);
            validationStatus status = new validationStatus();

            if (strQueryIdentifier == string.Empty)
                status.MarkInvalid("Validation page cannot be loaded without a specified 'identifier' in the Request URL.");
            else
            {
                try
                {
                    XmlDocument docResource = (XmlDocument)Session["docResource"];
                    status += ResourceManagement.GetExistingResource(strQueryIdentifier, ref docResource);
                    if (status.IsValid)
                    {
                        //repair after load to catch automatic repair changes on save, instead of noting "no changes to save"
                        Session["docResource"] = docResource;
                        Session["docAsLoaded"] = docResource.InnerXml;
                        status += ResourceManagement.RepairExistingResource(ref docResource);                          
                    }
                }
                catch (Exception ex)
                {
                    status.MarkInvalid("Error loading existing resource for edit: " + ex.Message);
                    LabelErrorMessage.Text = "Error loading existing resource for edit: " + status.GetConcatenatedErrors("<br/>");
                }
                if (status.IsValid)
                    ToggleControls(true);
            }
            return status;
        }

        private validationStatus LoadStaticFormData()
        {
            validationStatus status = new validationStatus();
            try
            {
                LabelIdentifier.Text = "Identifier: " + strQueryIdentifier;
                HyperlinkViewResource.NavigateUrl = strViewResourceURL + strQueryIdentifier;
            }
            catch (Exception ex)
            {
                status.MarkInvalid(ex.Message);
                ToggleControls(false);
            }
            return status;
        }

        private validationStatus GenerateDynamicFormData()
        {
            validationStatus status = new validationStatus();
            try
            {
                XmlNodeList list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("ri:Resource");
                if (list.Count == 0)
                    list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("Resource");
                PanelMain.Controls.Add(CreateStatusDropDown("DropDownStatus", list[0].Attributes["status"].Value));

                list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("validationLevel");
                PanelMain.Controls.Add(CreateValidationDropDown("DropDown_validationLevel", list[0].InnerText));

                CreateCapabilityList();
            }
            catch (Exception ex)
            {
                status.MarkInvalid("Error generating dynamic form data. " + ex.Message);
            }
            return status;
        }

        private void CreateCapabilityList()
        {
            XmlNodeList list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("capability");
            for( int i = 0; i < list.Count; ++i)
            {
                XmlNode node = list[i];
                AddBr(); AddBr();

                string labeltext = "Capability: <br>";
                foreach (XmlAttribute attribute in node.Attributes)
                {
                    if (attribute.Name.Contains("type"))
                        labeltext += " type: " + attribute.Value + "<br/>";
                }
                foreach (XmlNode child in node.ChildNodes)
                {
                    if (child.Attributes["version"] != null)
                        labeltext += " version: " + child.Attributes["version"].Value + "<br/>";
                    if (child.Name.Contains("interface"))
                    {
                        foreach (XmlNode ifacedetails in child.ChildNodes)
                        {
                            if (ifacedetails.Name == "accessURL")
                                labeltext += " accessURL: " + ifacedetails.InnerText;
                        }
                    }
                }

                Label nodeLabel = new Label();
                nodeLabel.Text = labeltext;
                PanelMain.Controls.Add(nodeLabel);
                AddBr();

                foreach (XmlNode child in node.ChildNodes)
                {
                    if (child.Name.ToLower() == "validationlevel")
                    {
                        Label label = new Label();
                        label.Text = "Capability-specific Validation: ";
                        PanelMain.Controls.Add(label);

                        string status = child.InnerText;
                        PanelMain.Controls.Add(CreateValidationDropDown("capability" + i, status));
                    }
                }
            }
            if (list.Count == 0)
            {
                AddBr(); AddBr();
                Label label = new Label();
                label.Text = "There are no capabilities in this record to validate.";
                PanelMain.Controls.Add(label);
            }
        }

        private void AddBr()
        {
            WebControl br = new WebControl(HtmlTextWriterTag.Br);
            PanelMain.Controls.Add(br);
        }

        private validationStatus SaveDocToRegistry()
        {
            validationStatus status = new validationStatus();

            try
            {
                SaveValidationInformation();
                string docAsSubmitted = ((XmlDocument)Session["docResource"]).InnerXml;
                if (docAsSubmitted == (string)Session["docAsLoaded"])
                    status.MarkInvalid("No changes to save.");
                else
                {
                    status += ResourceManagement.IngestXmlResource((XmlDocument)Session["docResource"], false, strQueryIdentifier);
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
            if (status.IsValid)
            {
                LabelErrorMessage.Text = "Resource saved successfully!";
            }
            else
            {
                LabelErrorMessage.Text = "Error(s) saving resource data: " + status.GetConcatenatedErrors("<br/>");
            }
        }

        //protected void DropDownStatus_SelectedIndexChanged(object sender, EventArgs e)
        //{
            //DropDownList dlist = (DropDownList)PanelMain.FindControl("DropDownStatus");
            //string status = dlist.SelectedValue;
            //XmlNodeList list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("ri:Resource");
            //if (list.Count == 0)
            //    list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("Resource");
 
            //list[0].Attributes["status"].Value = status;
        //}

        //todo: DB and code support for multiple validationLevels from various providers.
        public void SaveValidationInformation()
        {
            DropDownList dlist = (DropDownList)PanelMain.FindControl("DropDownStatus");
            string status = dlist.SelectedValue;
            XmlNodeList list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("ri:Resource");
            if (list.Count == 0)
                list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("Resource");

            list[0].Attributes["status"].Value = status;


            dlist = (DropDownList)PanelMain.FindControl("DropDown_validationLevel");
            ((XmlDocument)Session["docResource"]).GetElementsByTagName("validationLevel")[0].InnerXml =  dlist.SelectedValue;

             list = ((XmlDocument)Session["docResource"]).GetElementsByTagName("capability");
             for (int i = 0; i < list.Count; ++i)
             {
                 dlist = (DropDownList)PanelMain.FindControl("capability" + i);
                 XmlNode node = list[i];
                 foreach (XmlNode child in node.ChildNodes)
                 {
                     if (child.Name == "validationLevel")
                     {
                         child.Attributes.GetNamedItem("validatedBy").Value = ResourceManagement.strValidatedBy;
                         child.InnerXml = dlist.SelectedValue;
                         break;
                     }
                 }
             }
        }
    }
}