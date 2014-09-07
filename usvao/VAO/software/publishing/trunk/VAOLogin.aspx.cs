using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using DotNetOpenAuth;
using DotNetOpenAuth.OpenId;
using DotNetOpenAuth.OpenId.RelyingParty;
using DotNetOpenAuth.OpenId.Extensions.SimpleRegistration;
using DotNetOpenAuth.OpenId.Extensions.AttributeExchange;
using DotNetOpenAuth.Messaging;

namespace Publishing
{
    public partial class VAOLogin : System.Web.UI.Page
    {
        private static UserManagement userManager = new UserManagement();
        private static string redirect = "resourcemanagement.html?debug";
        private static string VAOLoginEndpoint = (string)System.Configuration.ConfigurationManager.AppSettings["VAOLoginEndpoint"];
        private static string VAOLoginRegistrationEndpoint = (string)System.Configuration.ConfigurationManager.AppSettings["VAOLoginRegistrationEndpoint"];
        private static string VAOLoginRealm = (string)System.Configuration.ConfigurationManager.AppSettings["VAOLoginRealm"];
 
        protected void Page_Load(object sender, EventArgs e)
        {
            loginButton.Focus();

            OpenIdRelyingParty openid = new OpenIdRelyingParty();
            var response = openid.GetResponse();
            if (response != null)
            {
                switch (response.Status)
                {
                    case AuthenticationStatus.Authenticated:
                        this.loginFailedLabel.Visible = false;
  
                        FetchResponse fetch = response.GetExtension<FetchResponse>(); //This is null for VAO, but not for Google or Yahoo 
                        string email = fetch.GetAttributeValue(WellKnownAttributes.Contact.Email);
                        string fullname = fetch.GetAttributeValue(WellKnownAttributes.Name.FullName);
                        string claimedID = response.ClaimedIdentifier.ToString();
                        string username = claimedID.Substring(claimedID.LastIndexOf('/')+1);

                        string[] userAuths = new string[] {};
                        long ukey = UserManagement.GetUserKey(username);
                        string[] errors = userManager.GetUserAuths(ukey, ref userAuths);
                        if( ukey == 0 && errors.Length == 0 )
                        {
                            errors = userManager.RegisterNewUser(username, email, fullname, ref ukey);
                        }
                        if (errors.Length == 0)
                        {
                            LocalLogin(username, ukey, userAuths);
                            Response.Redirect(redirect);
                        }
                         break;
                    case AuthenticationStatus.Canceled:
                        this.loginCanceledLabel.Visible = true;
                        break;
                    case AuthenticationStatus.Failed:
                        this.loginFailedLabel.Visible = true;
                        break;
                }
            }
        }

        protected void openidValidator_ServerValidate(object source, ServerValidateEventArgs args)
        {
            // This catches common typos that result in an invalid OpenID Identifier.
            args.IsValid = Identifier.IsValid(args.Value);
        }

        protected void loginButton_Click(object sender, EventArgs e)
        {
            try
            {
                using (OpenIdRelyingParty openid = new OpenIdRelyingParty())
                {
                    DotNetOpenAuth.OpenId.Identifier discovery = new Uri(VAOLoginEndpoint);
                    DotNetOpenAuth.OpenId.Realm realm = new DotNetOpenAuth.OpenId.Realm(VAOLoginRealm);
                    string returnUri = System.Web.HttpContext.Current.Request.Url.ToString();
                    IAuthenticationRequest request = openid.CreateRequest(discovery, realm, new Uri(returnUri));

                    FetchRequest fetch = new FetchRequest();
                    fetch.Attributes.Add(new AttributeRequest(WellKnownAttributes.Contact.Email, true));
                    fetch.Attributes.Add(new AttributeRequest(WellKnownAttributes.Name.FullName, true));
                    request.AddExtension(fetch);

                    // Send your visitor to their Provider for authentication.
                    request.RedirectToProvider();
                }
            }
            catch (ProtocolException ex)
            {
                // The user probably entered an Identifier that
                // was not a valid OpenID endpoint.
                this.loginFailedLabel.Text = ex.Message;
                this.loginFailedLabel.Visible = true;
            }
        }


        protected void registerButton_Click(object sender, EventArgs e)
        {
           string returnUri = System.Web.HttpContext.Current.Request.Url.ToString();
           //#region try direct openid
           // try
           // {
           //     using (OpenIdRelyingParty openid = new OpenIdRelyingParty())
           //     {
           //         DotNetOpenAuth.OpenId.Identifier discovery = new Uri(VAOLoginRegistrationEndpoint);
           //         DotNetOpenAuth.OpenId.Realm realm = new DotNetOpenAuth.OpenId.Realm(VAOLoginRealm);
           //         IAuthenticationRequest request = openid.CreateRequest(discovery, realm, new Uri(returnUri));

           //         FetchRequest fetch = new FetchRequest();
           //         fetch.Attributes.Add(new AttributeRequest(WellKnownAttributes.Contact.Email, true));
           //         fetch.Attributes.Add(new AttributeRequest(WellKnownAttributes.Name.FullName, true));
           //         request.AddExtension(fetch);

           //         // Send your visitor to their Provider for authentication.
           //         request.RedirectToProvider();
           //     }
           // }
           // catch (ProtocolException ex)
           // {
           //     // The user probably entered an Identifier that
           //     // was not a valid OpenID endpoint.
           //     this.loginFailedLabel.Text = ex.Message;
           //     this.loginFailedLabel.Visible = true;
           // }
           // #endregion

            var redirect = VAOLoginRegistrationEndpoint + "?returnURL=" + returnUri;
            Response.Redirect(redirect);
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

  

    }
}