package org.usvao.sso.ip.service;

import org.usvao.sso.ip.SSOProviderServiceException;
import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.db.UserDatabaseAccessException;
import org.usvao.sso.ip.db.NoSuchUserException;
import org.usvao.sso.ip.db.AuthenticationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.MailAccessException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.mailProcessing.MailOptions;
import org.globus.purse.registration.mailProcessing.MailManager;
import org.globus.purse.registration.UserData;
import org.globus.purse.registration.RegisterUtil;

import java.util.Properties;
import java.net.URLEncoder;
import java.text.MessageFormat;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


/**
 * a service bean for requesting to reset a forgotten password.
 * <p>
 * This class provides the business logic behind the JSP page for resetting
 * passwords.  The starts by submitting their username.  To authenticate 
 * the user, we email a token by email.  The user must either pass back
 * the username and token in the JSP page or do the same via an email.  
 * The user is then presented with the form for resetting passwords.
 * <p>
 * We note that it is possible that the account has not been activated yet
 * as we have been still awaiting confirmation.  Completing this process will 
 * serve as that confirmation and the will automatically activate the account.
 */
public class ResetPasswordRequest extends ServiceRequestBean {

    static Log logger =
	LogFactory.getLog(ResetPasswordRequest.class.getName());

    // bean properties/parameters
    public final static String ACTION = "action";
    public final static String MYACTION = "reset";
    public final static String USERNAME = "userName";
    public final static String TOKEN = "token"; 
    public static final String[] parameters = { ACTION, USERNAME, TOKEN };

    public String getUserName() { return getParameter(USERNAME); }
    public String getToken()    { return getParameter(TOKEN);    }
    public String getAction()   { return getParameter(ACTION);  }

    public void setUserName(String userName) { setParameter(USERNAME, userName);}
    public void setToken(String token)       { setParameter(TOKEN, token);      }

    static int _acceptedStatus = -1,
               _pendingStatus = -1;
    static {
        // cache some status values.
        try {
            acceptedStatus();
            pendingStatus();
        } catch (Exception ex) { 
            // we'll try again later
        }
    }

    /**
     * create an empty service request bean
     */
    public ResetPasswordRequest() {
        super(parameters); 
        setParameter(ACTION, MYACTION);
    }

    /**
     * create a fully-configured request
     */
    public ResetPasswordRequest(String userName, String token) {
        this();
        setUserName(userName);
        setToken(token);
    }

    /**
     * return true if the input paramters are valid for processing by the 
     * service.  If they are not, register errors internally.  
     * <p>
     * This implentation not only checks to see if a user name is provided 
     * (as the token is optional; without it, a token is sent to the user).
     * It does not check to see if the user exists.
     */
    @Override
    public boolean validate() { 
        _errors.clear();
        if (getUserName().length() == 0) {
            addErrorMsg(USERNAME, "Please enter your login name");
            return false;
        }
        
        return true;
    }

    /**
     * execute the request: either send the user a token or a new password.
     * <p>
     * This function has four different possible successful outcomes.  To 
     * indicate which occurred, one of the follow three key strings are set as 
     * the only error message associated with the empty string ("") label:
     * <ul>
     *   <li> <code>SentConfirm</code> -- the account is still in a pending
     *        state, awaiting confirmation; the confirmation email was
     *        resent. </li>
     *   <li> <code>SentToken</code> -- since no token was provided, one
     *        one was emailed to the user. </li>
     *   <li> <code>Confirmed</code> -- the account was still in a pending
     *        state, but since they sent us a token, we've activated it and
     *        will allow setting a new password. </li>
     *   <li> <code>Reset</code> -- The user is allowed to change his/her 
     *        password.  </li>
     * </ul>
     * The JSP page will take responsibility for translating these into 
     * user messages.  
     */
    public void execute() 
        throws SSOProviderServiceException, SSOProviderSystemException
    {
        if (! validate())
            throw new InvalidInputsException(_errors);

        try {
            UserData data = UserDataHandler.getDataForUsername(getUserName());
            if (data == null || 
                (data.getStatus() != acceptedStatus() && 
                 data.getStatus() != pendingStatus()    ) )
              throw new NoSuchUserException(getUserName());

            if (getToken().length() == 0) {
                sendPwTokenEmail(data);

                if (data.getStatus() == pendingStatus()) 
                    // we will need to confirm upon receipt of token
                    addErrorMsg("", "ConfirmedSent");
                else 
                    // send the token to the user
                    addErrorMsg("", "Sent");
            }
            else {
                if (! data.getToken().equals(getToken()))
                    throw new AuthenticationException(getUserName(),
                                                      "Incorrect token");

                if (data.getStatus() == pendingStatus()) {
                    // still unconfirmed; confirm it now
                    confirmAccount(data);
                    addErrorMsg("", "ConfirmedReset");
                }
                else 
                    addErrorMsg("", "Reset");

                // reset the password and send it.
                // resetPassword(data);
            }
        }
        catch (DatabaseAccessException e) {
            logger.error("DB error while handling password reset: " + e);
            throw new UserDatabaseAccessException(e.getMessage(), e);
        }
        catch (MailAccessException e) {
            logger.error("Mail error while handling password reset: " + e);
            throw new SSOProviderSystemException(e.getMessage(), e);
        }
        catch (RegistrationException e) {
            // should not happen
            String msg = "failed to find status ids in DB: " + e;
            logger.error(msg);
            throw new SSOProviderSystemException(msg, e);
        }
    }

    boolean sendPwTokenEmail(UserData user) 
        throws DatabaseAccessException, RegistrationException
    {
        MailOptions mailOptions = MailManager.getOptions();
        if (mailOptions == null) 
            throw new MailAccessException("MailOptions not initialized!");

        Properties msgData = new Properties();
        RegisterUtil.loadUserTags(msgData);

        // fullname
        String fullname = user.getLastName();
        String first = user.getFirstName();
        if (first != null && first.length() > 0)
            fullname = first+" "+fullname;
        msgData.setProperty("fullname", fullname);

        // username, token
        msgData.setProperty("username", user.getUserName());
        msgData.setProperty("token", user.getToken());

        // email for getting help
        String prop = mailOptions.getUserAccountAddr();
        try {  prop = (new InternetAddress(prop)).getAddress(); }
        catch (AddressException ex) {
            logger.error("Configuration has bad address in userAcctAddress:" +
                         prop);
            prop = "vaosso@ncsa.illinois.org";
        }
        msgData.setProperty("helpemail", prop);

        // password services URL
        prop = mailOptions.getCABaseUrl();
        String passServURL = prop+"/password.jsp";
        msgData.setProperty("passservices", passServURL);

        // the url
        prop = "?action=reset&userName={0}&token={1}";
        prop = MessageFormat.format(prop, getUserName(), user.getToken());
        msgData.setProperty("url", passServURL+prop);

        // send message
        MailManager.sendPasswordReminderMail(user.getEmailAddress(), msgData);
        return true;
    }

    void confirmAccount(UserData user) throws RegistrationException {
        if (user.getStatus() != pendingStatus()) return;

        RegisterUtil.setUserStatusAsAccepted(user.getToken());

        // Get ready to send mail to user
        Properties messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                               user.getFirstName() + ' ' + user.getLastName());
        
        // Send mailto user that all is set.
        MailManager.sendProxyUploadMail(user.getEmailAddress(), messageData);
    }

    static int acceptedStatus() throws RegistrationException {
        if (_acceptedStatus < 0) 
            _acceptedStatus = 
                StatusDataHandler.getId(RegisterUtil.getAcceptedStatus());
        return _acceptedStatus;
    }
    static int pendingStatus() throws RegistrationException {
        if (_pendingStatus < 0) 
            _pendingStatus = 
                StatusDataHandler.getId(RegisterUtil.getPendingStatus());
        return _pendingStatus;
    }

}
