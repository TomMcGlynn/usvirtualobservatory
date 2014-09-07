package org.usvao.sso.ip.service;

import org.usvao.sso.ip.SSOProviderServiceException;
import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.db.UserDatabaseAccessException;

/**
 * a service bean that encapsulates several services for managing passwords
 * served from one page.  Which specific service is run depends on the value
 * of the action property.
 *
 * This is intended for use with the password.jsp page which contains a 
 * seperate form for each of the services it provides an interface for.  
 * The submit button provides the value of the "action" parameter to 
 * indicate which service is being invoked.  
 */
public class PasswordServices extends ServiceRequestBean {

    // combined bean properties/service parameters
    public final static String ACTION = "action";
    public final static String EMAILS = RemindUserNameRequest.EMAILS;
    public final static String USERNAME = ChangePasswordRequest.USERNAME;
    public final static String PASSWORD = ChangePasswordRequest.PASSWORD;
    public final static String NEWPW1 = ChangePasswordRequest.NEWPW1;
    public final static String NEWPW2 = ChangePasswordRequest.NEWPW2;
    public final static String TOKEN = ResetPasswordRequest.TOKEN;

    public static final String[] parameters = {
        ACTION, EMAILS, USERNAME, PASSWORD, NEWPW1, NEWPW2, TOKEN
    };

    public String getAction()   { return getParameter(ACTION); }
    public String getEmails()   { return getParameter(EMAILS); }
    public String getUserName() { return getParameter(USERNAME); }
    public String getPassword() { return getParameter(PASSWORD); }
    public String getNewpw1()   { return getParameter(NEWPW1); }
    public String getNewpw2()   { return getParameter(NEWPW2); }
    public String getToken()    { return getParameter(TOKEN); }

    public void setAction(String action)     { setParameter(ACTION, action);    }
    public void setEmails(String emails)     { setParameter(EMAILS, emails);    }
    public void setUserName(String userName) { setParameter(USERNAME, userName);}
    public void setPassword(String password) { setParameter(PASSWORD, password);}
    public void setNewpw1(String newpw1)     { setParameter(NEWPW1, newpw1);    }
    public void setNewpw2(String newpw2)     { setParameter(NEWPW2, newpw2);    }
    public void setToken(String token)       { setParameter(TOKEN, token);      }

    public static final String ACTION_CHANGE = "change";
    public static final String ACTION_RESET  = "reset";
    public static final String ACTION_REMIND = "remind";

    private ServiceRequestBean _selected = null;

    public PasswordServices() {  super(parameters);  }

    /**
     * create a RemindUserNameRequest service bean from the input parameters.  
     * The value of action is ignored; thus, the resulting request may not 
     * have the inputs it needs.  One should use getActionRequest() instead.
     */
    public RemindUserNameRequest makeRemindUserNameRequest() {
        return new RemindUserNameRequest(getEmails());
    }

    /**
     * create a ChangePasswordRequest service bean from the input parameters.  
     * The value of action is ignored; thus, the resulting request may not 
     * have the inputs it needs.  One should use getActionRequest() instead.
     */
    public ChangePasswordRequest makeChangePasswordRequest() {
        return new ChangePasswordRequest(getUserName(), getPassword(),getToken(),
                                         getNewpw1(), getNewpw2());
    }

    /**
     * create a PasswordResetRequest service bean from the input parameters
     * The value of action is ignored; thus, the resulting request may not 
     * have the inputs it needs.  One should use getActionRequest() instead.
     */
    public ResetPasswordRequest makePasswordResetRequest() {
        return new ResetPasswordRequest(getUserName(), getToken());
    }

    /**
     * create the service bean corresponding the current value of the 
     * action parameter.  Null is returned if the action value is not 
     * recognized.  
     */
    public ServiceRequestBean getActionRequest() {
        String action = getAction();
        if (remindRequest())
            return makeRemindUserNameRequest();
        else if (resetRequest()) 
            return makePasswordResetRequest();
        else if (changeRequest()) 
            return makeChangePasswordRequest();
        else 
            return null;
    }

    /**
     * return true if a password change was requested (via the action 
     * parameter)
     */
    public boolean changeRequest() { return ACTION_CHANGE.equals(getAction()); }

    /**
     * return true if a password change was requested (via the action 
     * parameter)
     */
    public boolean resetRequest() { return ACTION_RESET.equals(getAction()); }

    /**
     * return true if a password change was requested (via the action 
     * parameter)
     */
    public boolean remindRequest() { return ACTION_REMIND.equals(getAction()); }

    /**
     * validate the inputs based on the value of the action parameter
     */
    @Override
    public boolean validate() throws SSOProviderSystemException {
        _errors = new ParamErrors(parameters);
        _selected = getActionRequest();
        if (_selected == null) {
            addErrorMsg(ACTION, "illegal value of action parameter; please use the web page form");
            return false;
        }

        loadErrors(_selected.exportErrors());
        return _selected.validate();
    }

    /**
     * execute the request
     */
    @Override
    public void execute() 
        throws SSOProviderServiceException, SSOProviderSystemException 
    {
        if (! validate())
            throw new InvalidInputsException(_errors);
        _selected.execute();
    }
}