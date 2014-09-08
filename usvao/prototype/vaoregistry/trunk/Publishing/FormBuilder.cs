using System;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

namespace Publishing
{
    public class FormBuilder
    {
        public static DropDownList CreateValidationDropDown(string id, string currentValue)
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
    }
}