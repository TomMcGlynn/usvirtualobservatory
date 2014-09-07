package org.usvao.sso.ip.service;

import org.usvao.sso.ip.SSOProviderServiceException;
import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.db.UserDatabaseAccessException;
import org.usvao.sso.ip.db.AuthenticationException;
import org.usvao.sso.ip.db.NoSuchUserException;
import org.usvao.sso.ip.pw.PasswordChecker;
import org.usvao.sso.ip.pw.VAOPasswordChecker;
import org.usvao.sso.ip.pw.WeakPasswordException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.UserData;
import org.globus.purse.registration.RegisterUtil;

/**
 * a service bean for handling user inputs for updating a password.  
 * <p>
 * This service takes a username, old password, and a new password entered
 * twice.  The service will not update the password unless the two ones 
 * entered match exactly.  Before updating the password, the user is 
 * authenticated using the old password.  The {@link execute()} function 
 * validates the inputs and executes the request.  
 */
public class ChangePasswordRequest extends ServiceRequestBean {

    static Log logger =
	LogFactory.getLog(ChangePasswordRequest.class.getName());
    
    // bean properties/parameters
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String NEWPW1   = "newpw1";
    public static final String NEWPW2   = "newpw2";
    public static final String TOKEN    = "token";
    public static final String[] parameters = {
        USERNAME, PASSWORD, NEWPW1, NEWPW2, TOKEN
    };

    public String getUserName() { return getParameter(USERNAME); }
    public String getPassword() { return getParameter(PASSWORD); }
    public String getNewpw1()   { return getParameter(NEWPW1);   }
    public String getNewpw2()   { return getParameter(NEWPW2);   }
    public String getToken()    { return getParameter(TOKEN);    }

    public void setUserName(String username) { setParameter(USERNAME, username);}
    public void setPassword(String password) { setParameter(PASSWORD, password);}
    public void setNewpw1(String newpw1)     { setParameter(NEWPW1, newpw1);    }
    public void setNewpw2(String newpw2)     { setParameter(NEWPW2, newpw2);    }
    public void setToken(String token)       { setParameter(TOKEN, token);      }

    /**
     * create an empty service bean
     */
    public ChangePasswordRequest() {  super(parameters);  }

    /**
     * create the service bean with inputs
     */
    public ChangePasswordRequest(String username, String password, 
                                 String newpw1, String newpw2)
    {
        this();
        setUserName(username);
        setPassword(password);
        setNewpw1(newpw1);
        setNewpw2(newpw2);
    }

    /**
     * create the service bean with inputs
     */
    public ChangePasswordRequest(String username, String password, String token,
                                 String newpw1, String newpw2)
    {
        this(username, password, newpw1, newpw2);
        setToken(token);
    }

    /**
     * return true if the input paramters are valid for processing by the 
     * service.  If they are not, register errors internally.  This does not
     * test if the username exists, the current password is correct, or if 
     * the new password is strong enough; this is done in execute();
     */
    @Override
    public boolean validate() {
        _errors.clear();

        boolean missingInputs = false;
        boolean newPwsDontMatch = false;
        if (getUserName().length() == 0) {
            addErrorMsg(USERNAME, "Please enter your login name");
            missingInputs = true;
        }
        if (getPassword().length() == 0 && getToken().length() == 0) {
            System.err.println("token: " + getToken());
            addErrorMsg(PASSWORD, "Please enter your current password");
            missingInputs = true;
        }
        if (getNewpw1().length() == 0 || getNewpw2().length() == 0) {
            addErrorMsg(NEWPW1, "Please enter the new password twice.");
            missingInputs = true;
        }
        else if (! getNewpw1().equals(getNewpw2())) {
            addErrorMsg(NEWPW1, "Please re-enter new password twice");
            newPwsDontMatch = true;
            if (! missingInputs) 
                addErrorMsg("", "New passwords do not match; please re-enter");
        }

        if (missingInputs) 
            addErrorMsg("", 
               "To change your password, please fill in all input fields");

        return ! (missingInputs || newPwsDontMatch);
    }

    /**
     * commit the requested password change.  This calls validate().  It 
     * will also test to see if the passwords pass local policies.
     * @throws InvalidInputsException  if errors were encountered during 
     *            input parameter validation.  Errors will be registered 
     *            explaining the problem.  
     * @throws WeakPasswordException  if the new password fails meet local
     *            password policies; note that reasons will be registered 
     *            as errors.  
     * @throws NoSuchUserException  if the user does not exist in the database
     * @throws AuthenticationException  if the old password is not correct
     * @throws SSOProviderServiceException  thrown only in the form of one of 
     *            the above exceptions.
     * @throws SSOProviderSystemException  if a internal system error is 
     *            detected (most likely as a UserDatabaseAccessException).  
     */
    public void execute() 
        throws SSOProviderServiceException, SSOProviderSystemException
    {
        if (! validate())
            throw new InvalidInputsException(_errors);

        UserData user = getUserData(getUserName(), getPassword(), getToken());
        if (! checkPassword(getNewpw1(), user))
            throw new WeakPasswordException("New password is too weak");

        try {
            UserDataHandler.setUserPassword(getUserName(), getNewpw1());
        }
        catch (DatabaseAccessException e) {
            logger.error("DB failure while attempting password change: " + e);
            throw new UserDatabaseAccessException("Password change error: " +
                                                  e.getMessage(), e);
        }
        catch (RuntimeException e) {
            logger.error("Unexpected exception while attempting password change: " + e);
            throw new SSOProviderSystemException("Password change sys error: " +
                                                 e.getMessage(), e);
        }
    }


    /**
     * return the user data for a username.  If the password is not valid
     * for the given username or if the username does not exist in the 
     * user database, an error message will be registered and null will 
     * be returned.
     * @param username   the login name for the user changine their password
     * @param password   that user's current (old) password.
     * @throws NoSuchUserException  if the username does not exist in 
     *                   the database.
     * @throws AuthenticationException  if either the password is incorrect 
     *                   for this user or the user's status is something other 
     *                   than "accepted".
     * @throws UserDatabaseAccessException  if a system failure occurs while
     *                   accessing the database.  
     */
    protected UserData getUserData(String username, String password, 
                                   String token) 
        throws NoSuchUserException, AuthenticationException, 
               UserDatabaseAccessException, SSOProviderSystemException
    {
        if (username == null || username.length() == 0) {
            addErrorMsg(USERNAME, "Please provide your username");
            return null;
        }

        if ((password == null || password.length() == 0) &&
            (token == null || token.length() == 0)) 
        {
            addErrorMsg(PASSWORD, "Please provide your current password");
            return null;
        }

        int acceptedStatus = -1;
        try {
            acceptedStatus = 
                StatusDataHandler.getId(RegisterUtil.getAcceptedStatus());
        } catch (RegistrationException ex) {
            // not found error: should not happen
            throw new SSOProviderSystemException("Failed to load accepted " +
                                                 "status id: " + ex, ex);
        }

        try {

            UserData data = UserDataHandler.getDataForUsername(username);
            if (data == null) 
                throw new NoSuchUserException(username);

            if (password != null && password.length() > 0) {
                if (! UserDataHandler.passwordMatches(data, password) || 
                    data.getStatus() != acceptedStatus) 
                  throw new AuthenticationException(username, 
                                                    "Incorrect password");
            }
            else if (token != null && token.length() > 0) {
                if (! token.equals(data.getToken())) 
                    throw new AuthenticationException(username, 
                                                      "Incorrect token");
            }
            else {
                // shouldn't happen
                throw new AuthenticationException(username, "Missing password");
            }

            return data;
        } 
        catch (DatabaseAccessException e) {
            logger.error(e);
            throw new UserDatabaseAccessException(e.getMessage(), e);
        }
    }
    
    /**
     * check that the password complies with policies on passwords.  
     * This assumes the current values of the user data.
     */
    public boolean checkPassword(String pw, UserData user) {
        PasswordChecker chkr = 
            new VAOPasswordChecker(user.getFirstName(), user.getLastName(),
                                   user.getUserName(), user.getInstitution(), 
                                   user.getEmailAddress());

        String[] why = chkr.explainNoncompliance(pw);
        if (why.length == 0) return true;

        for(String reason : why) 
            addErrorMsg(NEWPW1, reason);

        return false;
    }

}