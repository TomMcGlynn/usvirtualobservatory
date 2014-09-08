using System;
using System.Collections.Generic;
using System.Web;
using System.Web.SessionState;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Collections;

namespace Publishing
{
    public partial class login : System.Web.UI.Page
    {
        private static UserManagement userManager = new UserManagement();
        
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
                 string[] errors;
                 if (!isValid)
                 {
                     errors = new string[] { "Bad form input." };
                     ReturnFailure(errors);
                     return;
                 }
                 #endregion

                 #region assuming good input, see if we want to log out user
                 if (input["action"] == "logout")
                 {
                     SessionLogout();
                     ReturnSuccess();
                     return;
                 }
                 else if (input["action"] == "isloggedin")
                 {
                     if (IsLoggedIn())
                         ReturnSuccess((string)Session["username"]);
                     else
                         ReturnFailure( new string[] {"Not Logged In."});
                     return;
                 }
                 else if (input["action"] == "getauthID")
                 {
                     if (IsLoggedIn())
                         ReturnSuccess(((string[])Session["userAuths"])[0]);
                     else
                         ReturnFailure(new string[] { "" });
                     return;
                 }
                 #endregion

                 #region otherwise assuming good input, try to log in or create new user + log them in
                 long ukey = 0;
                 if (input["authorityInfo"] == null)
                 {
                     errors = userManager.CheckLoginCredentials(input["loginUsername"], input["loginPassword"], ref ukey);
                 }
                 else
                     errors = RegisterNewUser(input["loginUsername"], input["loginPassword"], input["confirmLoginPassword"], input["authorityInfo"], input["email"], input["name"], ref ukey);
                 if (errors.Length == 0)
                 {
                     string[] userAuths = new string[] {};
                     errors = userManager.GetUserAuths(ukey, ref userAuths);
                     SessionLogin(input["loginUsername"], ukey, userAuths);
                 }

                if( errors.Length == 0 )
                    ReturnSuccess();
                else
                    ReturnFailure(errors);
            }
            catch( Exception ex)
            {
                ReturnFailure(new string[] { "Error reading login form input.  (" + ex.ToString() + ")" });
            }
             #endregion
        }

        private string[] RegisterNewUser(string username, string password, string confirm, string authorityID, string email, string name, ref long ukey)
        {
            ArrayList errors = new ArrayList();
            if (confirm != password)
                errors.Add("Password and Password Confirmation do not match.");
            else if (username.Length < 1 || password.Length < 1 || email.Length < 1 || name.Length < 1)
                errors.Add("No blank fields allowed.");
            else if (password.Length < 8)
                errors.Add("Password must be at least 8 characters");
            else
            {

                //make sure user does not already exist
                if (UserManagement.UserExists(username))
                    errors.Add("Username already exists.");
                else
                {
                    string[] errorsReg = userManager.RegisterNewUser(username, password, authorityID, email, name, ref ukey);
                    errors.AddRange(errorsReg);
                    string[] userAuths = new string[] { };
                    errorsReg = userManager.GetUserAuths(ukey, ref userAuths);
                    errors.AddRange(errorsReg);

                    if (errors.Count == 0)
                        SessionLogin(username, ukey, userAuths);
                }
            }

            return (string[])errors.ToArray(typeof(string));
        }

        private bool IsLoggedIn()
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

        private void SessionLogout()
        {
            Session["username"] = string.Empty;
            Session["ukey"] = (long)0;
            Session["userAuths"] = null;
        }

        private void SessionLogin(string username, long ukey, string[] userAuths)
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