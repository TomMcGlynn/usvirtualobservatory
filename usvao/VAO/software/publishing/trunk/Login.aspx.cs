using System;
using System.Collections.Generic;
using System.Web;
using System.Web.SessionState;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Collections;
using DotNetOpenAuth.OpenId.RelyingParty;
using DotNetOpenAuth.OpenId.Extensions.AttributeExchange;

namespace Publishing
{
    public partial class login : System.Web.UI.Page
    {
        private static UserManagement userManager = new UserManagement();
        private static string VAOLoginLogout = (string)System.Configuration.ConfigurationManager.AppSettings["VAOLoginLogout"];
        
        protected void Page_Load(object sender, EventArgs e)
        {
  
            try
            {
                System.Collections.Specialized.NameValueCollection input = Request.QueryString;
                #region Get/check Parameters
                 if (Request.RequestType == "POST")
                    input = new System.Collections.Specialized.NameValueCollection(Request.Form);
                 bool isValid = CleanInput(ref input);
                #endregion       

                 #region stop now if given bad/faked/malicious form input
                 string[] errors = {};
                 if (!isValid)
                 {
                     errors = new string[] { "Bad form input." };
                     ReturnFailure(errors);
                     return;
                 }
                 #endregion

                 #region assuming good input, see if we want to log out user or look up their auth info
                 if (input["action"] == "logout")
                 {
                     SessionLogout();
                     string redirect = VAOLoginLogout + "?returnURL=" +
                         Request.Url.GetLeftPart(System.UriPartial.Authority) + Request.ApplicationPath +
                         "/VAOlogin.aspx";
                     Response.Redirect(redirect);

                     ReturnSuccess();
                     return;
                 }
                 else if (input["action"] == "isloggedin")
                 {
                     if (IsLoggedIn())
                         ReturnSuccess((string)Session["username"]);
                     else
                         ReturnFailure(new string[] { "Not Logged In." });
                     return;
                 }
                 else if (input["action"] == "getauthinfo")
                 {
                     if (IsLoggedIn())
                     {
                         string[] userAuths = (string[])Session["userAuths"];
                         if (userAuths.Length > 0)
                         {
                             string defaultAuth = userAuths[0];
                             if (defaultAuth.Length > 0)
                             {
                                 bool bHasOrg = userManager.CheckAuthForOrg(defaultAuth);
                                 if (bHasOrg)
                                     ReturnAuthSuccess(defaultAuth);
                                 else
                                     ReturnSuccess(defaultAuth, "Organisation record required.");
                             }
                             else
                                 ReturnFailure(new string[] { "Invalid default authority resource for user." });
                         }
                         else
                             ReturnSuccess(string.Empty);
                     }
                     else
                     {
                         ReturnFailure(new string[] { "" });
                     }
                     return;
                 }
                 #endregion

                 #region VAO login / registration
                 Response.Redirect("VAOLogin.aspx");

                //if( errors.Length == 0 )
                //    ReturnSuccess();
                //else
                //    ReturnFailure(errors);
            }
            catch( Exception ex)
            {
                ReturnFailure(new string[] { "Error reading login form input.  (" + ex.ToString() + ")" });
            }
             #endregion
        }


        internal bool IsLoggedIn()
        {
            try
            {
                if (Session["username"] != null && (string)Session["username"] != string.Empty &&
                    Session["ukey"] != null && (long)Session["ukey"] != 0)
                    return true;

            }
            catch (Exception) { }

            return false;
        }

        internal void SessionLogout()
        {
            //Session["username"] = string.Empty;
            //Session["ukey"] = (long)0;
            //Session["userAuths"] = new string[] { };
            Session.Clear();
        }

        internal void LocalLogin(string username, long ukey, string[] userAuths)
        {
            Session["username"] = username;
            Session["ukey"] = ukey;
            Session["userAuths"] = userAuths;
            Session.Timeout = 180;
        }

        //todo: more SQL validation here.
        private bool CleanInput(ref System.Collections.Specialized.NameValueCollection input)
        {
            bool isValid = true;
            System.Collections.Specialized.NameValueCollection output = new System.Collections.Specialized.NameValueCollection();
            foreach (string key in input)
            {
                string value = Server.UrlDecode(input[key].Trim());
                string uppercase = value.ToUpper();
                if (value.IndexOf(';') > -1 || uppercase.Contains(" DELETE ") || uppercase.Contains(" INSERT ") || uppercase.Contains(" UPDATE ") || uppercase.Contains(" DROP "))
                {
                    isValid = false;
                    break;
                }
                output[key] = value;
            }
            if (isValid) 
                output = input;
            return isValid;
        }

        private void ReturnSuccess()
        {
            Response.Write("{ 'success': true}");
            //Response.Flush();
        }

        private void ReturnSuccess(string details)
        {
            Response.Write("{ 'success': true, 'details': '" + details + "'}");
            //Response.Flush();
        }

        private void ReturnAuthSuccess(string defaultauth)
        {
            Response.Write("{ 'success': true, 'defaultauth': '" + defaultauth + "'}");
        }

        private void ReturnSuccess(string details, string message)
        {
            Response.Write("{ 'success': true, 'details': '" + details + "', message: '" + message + "'}");
            //Response.Flush();
        }

        //remove JSON-incompatible characters here.
        private void ReturnFailure(string[] errors)
        {
            if (errors != null && errors.Length > 0)
            {
                Response.Write("{ 'success': false, 'errors': { 'reason': '");
                foreach (string error in errors)
                {
                    Response.Write(error.Replace('\'', '`').Replace("\r\n", " ") + ' '); 
                }
                Response.Write("' }}");
             }
            else
                 Response.Write("{ 'success': false, 'errors': { 'reason': 'Login failed. Try again.' }}");
            //Response.Flush();
        }
    }
}