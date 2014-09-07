/*
 * vaologin.js -- useful fuctions for portals using VAO logins,
 *                including the ability to query and display a user's
 *                login status.  
 */

var vaologin = {

    "conf": {

        // These values can be overridden to configure vaologin functions.
        // See below after vaologin definition for semantic documentation.

        "vaossoServerUrl":   null,
        "loginStatusUrl":    null,
        "portalLoginUrl":    null,
        "portalLogoutUrl":   null,
        "portalRegisterUrl": null
    },

    "loginStatus": null,

    /**
     * query the server for the user's login status and send the response to 
     * a handler function.  The function should take a status object as its
     * only input.  A null input to that function indicates that an error 
     * occured.
     *
     * The status object contains the following fields:
     */
    "updateLoginStatus": function(handler) {
        var use = (function(status) {
            vaologin.loginStatus = status;
            handler(status);
        });
        jQuery.getJSON(vaologin.loginStatusUrl, use);
    },

    /**
     * create an HTML rendering of a login status object. 
     * @param status      the status object
     * @param loginURL    the URL for logging in to the portal; if null,
     *                        no Login link will be included (def: null).
     * @param logoutURL   the URL for logging out of the portal; if null,
     *                        no Logout link will be included (def: null).
     * @param registerURL the URL for registering to use the portal; if null,
     *                        Register link will be included (def: null).
     */
    "statusToHTML": function(status, loginURL, registerURL, logoutURL) {
        var user = status.username;
        if (user != null) user = user.replace(/@usvao.org$/, '');

        var htmltxt = "";
        if (status.state == "in") {
            htmltxt += "Logged in as ";
            htmltxt += user;
            htmltxt += " <br /> Time left: ";
            htmltxt += status.dispLeft;
            if (logoutURL) {
                htmltxt += ' &nbsp;&nbsp;<a href="';
                htmltxt += logoutURL;
                htmltxt += '">Logout</a> ';
            }
        }
        else if (status.state == "ex") {
            htmltxt += "Expired session as ";
            htmltxt += status.username;
            if (loginURL) {
                htmltxt += '&nbsp;&nbsp;<a href="';
                htmltxt += loginURL;
                htmltxt += '">Login</a>';
            }
        }
        else {
            htmltxt += 'Logged out';
            if (loginURL) {
                htmltxt += ' &nbsp;&nbsp; <a href="';
                htmltxt += loginURL;
                htmltxt += '">Login</a>';
            }
            if (registerURL) {
                htmltxt += '&nbsp;&nbsp;<a href="';
                htmltxt += registerURL;
                htmltxt += '">Register</a>';
            }
        }
        return htmltxt;
    },

    /**
     *  display the login status at a given location (via the statusToHTML() 
     *  function).  
     *  @param status     a login status object
     *  @param idwhere    the id of the element that will enclose the 
     *                       HTML-formatted login status information.  Any
     *                       content in that element will be replaced.
     * @param loginURL    the URL for logging in to the portal; if null,
     *                        the value of the global var portalLoginUrl will 
     *                        be used.  If that value is null,
     *                        no Login link will be included.
     * @param registerURL the URL for registering to use the portal; if null,
     *                        the value of the global var portalRegisterUrl will 
     *                        be used.  If that value is null,
     *                        Register link will be included (def: null).
     * @param logoutURL   the URL for logging out of the portal; if null,
     *                        the value of the global var portalLogoutUrl will 
     *                        be used.  If that value is null,
     *                        no Logout link will be included (def: null).
     */
    "displayStatus": function(status, idwhere, loginURL, registerURL, logoutURL) {
        if (! loginURL)    loginURL    = vaologin.portalLoginUrl;
        if (! logoutURL)   logoutURL   = vaologin.portalLogoutUrl;
        if (! registerURL) registerURL = vaologin.portalRegisterUrl;

        jQuery("#"+idwhere)[0].innerHTML = 
            vaologin.statusToHTML(status, loginURL, registerURL, logoutURL);
    },

    /**
     *  update the display of the login status using the configuration.
     *  call this function with in a "on-load" function that requires 
     *  the login status information to be available.  
     *  @param idwhere  the id of the element that will enclose the 
     *                     HTML-formatted login status information.  Any
     *                     content in that element will be replaced.
     */
    "updateStatusDisplay": function(idwhere) {
        var dispfunc = (function(status) {
                vaologin.displayStatus(status, idwhere);
            });
        vaologin.updateLoginStatus(dispfunc);
    }, 

    /**
     *  when the page has finished loading fetch and display the login status
     *  (via the displayStatus() function).  This assumes that the global 
     *  variables portalLoginURL, portalRegisterURL, and portalLogoutURL 
     *  have been set as desired.  
     *  @param idwhere  the id of the element that will enclose the 
     *                     HTML-formatted login status information.  Any
     *                     content in that element will be replaced.
     */
    "displayStatusOnLoad": function(idwhere) {
        var updfunc = (function() {
                vaologin.updateStatusDisplay(idwhere);
            });

        jQuery(document).ready(updfunc);
    }   

};

/**
 *  these values can be overridden to configure vaologin functions
 */

//** the Login server to use
vaologin.vaossoServerUrl = "https://sso.usvao.org";

//** the local URL for the loginstatus service
vaologin.loginStatusUrl = "/cgi-bin/loginstatus";

//** the local URL to use for logging in.  
vaologin.portalLoginUrl = "/cgi-bin/portal/login";

//** the URL to use for logging out; this can either be a local URL
//   for ending the local portal session or a URL at the VAOSSO server
//   for logging out of all of the VO.
vaologin.portalLogoutUrl = null;

//** the URL to use for registering with this portal.  This can be 
//   either
//    1. the plain VAOSSO register page,
//    2. the VAOSSO register page with a return URL appended as an 
//       argument, or 
//    3. a local registration page.
vaologin.portalRegisterUrl = 
    vaologin.vaossoServerUrl + "/register";  // this is 1. above
