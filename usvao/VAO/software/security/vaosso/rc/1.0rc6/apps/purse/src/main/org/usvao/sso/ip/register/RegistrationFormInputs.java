package org.usvao.sso.ip.register;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.usvao.sso.ip.pw.PasswordChecker;
import org.usvao.sso.ip.pw.VAOPasswordChecker;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.util.Comma;
import org.globus.purse.util.HtmlEncode;

/**
 * a bean for collecting and validating the inputs from a user registration
 * form.
 */
public class RegistrationFormInputs {

    // bean properties
    private String _fname = "";
    private String _lname = "";
    private String _email1 = "";
    private String _email2 = "";
    private String _inst = "";
    private String _phone = "";
    private String _username = "";
    private String _password1 = "";
    private String _password2 = "";
    private String _returnURL = "";
    private String _portalName = "";
    private String _country = "";

    private ParamErrors _errors = new ParamErrors();

    public String getLastName() { return _lname; }
    public String getFirstName() { return _fname; }
    public String getInst() { return _inst; }
    public String getPhone() { return _phone; }
    public String getCountry() { return _country; }
    public String getEmail() { return _email1; }
    public String getEmail2() { return _email2; }
    public String getPortalName() { return _portalName; }
    public String getReturnURL() { return _returnURL; }
    public String getUserName() { return _username; }
    public String getPassword1() { return _password1; }
    public String getPassword2() { return _password2; }

    public void setLastName(String name) { _lname = clean(name); }
    public void setFirstName(String name) { _fname = clean(name); }
    public void setInst(String inst) { _inst = clean(inst); }
    public void setPhone(String phone) { _phone = clean(phone); }
    public void setCountry(String country) { _country = clean(country); }
    public void setEmail(String email) { _email1 = clean(email); }
    public void setEmail2(String email) { _email2 = clean(email); }
    public void setUserName(String username) { _username = clean(username); }
    public void setReturnURL(String returnUrl) { _returnURL = clean(returnUrl);}
    public void setPassword1(String password1) { _password1 = clean(password1);}
    public void setPassword2(String password2) { _password2 = clean(password2);}
    public void setPortalName(String portalName) { 
        _portalName = clean(portalName); 
    }

    /**
     * add an error message associated with the given parameter name
     */
    public void addErrorMsg(String paramName, String msg) {
        _errors.addMessage(paramName, msg);
    }

    /**
     * return the error message for a given parameter or null if there are 
     * no messages registered.
     */
    public String[] getErrorMsgsFor(String paramName) {
        return _errors.getMessagesFor(paramName);
    }

    /**
     * return true if there are error messages registered for the given 
     * parameter.
     */
    public boolean errorsFoundFor(String paramName) {
        return _errors.hasMessagesFor(paramName);
    }

    /**
     * return true if any errors were register as a result of running validate().
     */
    public boolean errorsFound() {
        return (_errors.getParamCount() > 0);
    }

    /** 
     * return the error message set for external storage
     */
    public ParamErrors exportErrors() { return _errors; }

    /**
     * load errors from external storage
     */
    public void loadErrors(ParamErrors errs) {
        if (errs != null) _errors = errs;
    }


    // trim and decode the string
    private String clean(String s) { 
        if (s == null) return "";
        try {
            return URLDecoder.decode(s.trim(), "UTC-16"); 
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError("prog. error in clean():"+ex.getMessage());
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * review all the inputs, registering any detecting problems as 
     * error messages that can be retrieved via getErrorMsgsFor().
     * @return boolean   true if no problems were found.
     */
    public boolean validate() {
        boolean out = true;
        if (! validateFirstName(_fname)) out = false;
        if (! validateLastName(_lname))   out = false;
        if (! validateInstitution(_inst))    out = false;
        if (! validatePhone(_phone))         out = false;

        if (! validateEmail(_email1, _email2)) {
            out = false;
            _email2 = "";
        }

        if (! validateUserName(_username))   
            out = false;
        else if (! checkUserName(_username))
            out = false;

        if (! validatePassword(_password1, _password2)) {
            out = false;
        }
        else if (! checkPassword(_password1)) {
            out = false;
        }

        return out;
    }

    /**
     * ensure that a value is not blank
     * @param value      the value provided in the form
     * @param paramName  the name of the parameter that the value was 
     *                       provided for
     * @param errorMsg   the error message to register for paramName if the 
     *                       value is blank
     */
    protected boolean validateNotBlank(String value, String paramName, 
                                       String errorMsg)
    {
        if (! isBlank(value)) return true;
        addErrorMsg(paramName, errorMsg);
        return false;
    }

    /**
     * validate the user's last name
     */
    public boolean validateLastName(String value) {
        return validateNotBlank(value, LASTNAME, "Please enter your last name");
    }

    /**
     * validate the user's last name
     */
    public boolean validateFirstName(String value) {
        return validateNotBlank(value, FIRSTNAME, 
                                "Please enter your first name");
    }

    /**
     * validate the user's phone number
     */
    public boolean validatePhone(String value) {
        return validateNotBlank(value, PHONE, 
                                "Please enter your phone number");
    }

    /**
     * validate the user's institution
     */
    public boolean validateInstitution(String value) {
        return true;
        /*
        return validateNotBlank(value, INSTITUTION, 
                                "Please enter an institution of affiliation");
        */
    }

    /**
     * validate the user's home country
     */
    public boolean validateCountry(String value) {
        if (! isBlank(value) && ! value.equalsIgnoreCase("Select Country")) 
            return true;
        addErrorMsg(COUNTRY, "Please choose your country of residence");
        return false;
    }
    
    /**
     * validate the user's email address
     */
    public boolean validateEmail(String val1, String val2) {
        boolean out = true;
        if (! validateNotBlank(val1, EMAIL, 
                               "Please enter your email address"))
            out = false;
        else if (val1.indexOf('@') == -1) {
            out = false;
            addErrorMsg(EMAIL, 
                        "Please enter a valid email address (missing @)");
        }

        if (! validateNotBlank(val2, EMAIL2, "Please enter your email again"))
            out = false;
        else if (! val1.equals(val2)) {
            out = false;
            addErrorMsg(EMAIL2, "Your email addresses must match");
        }

        return out;
    }

    static Pattern _usernamere = Pattern.compile("[\\p{Alnum}_\\.\\-]+");

    /**
     * validate the form of the username.  This does not check to see if 
     * the username is already taken; see checkUsername();
     */ 
    public boolean validateUserName(String value) {
        if (! validateNotBlank(value, USERNAME, 
                               "Please enter your desired username"))
            return false;

        boolean out = true;
        if (value.length() < 2) {
            addErrorMsg(USERNAME, "Please choose a username that is at least " +
                        "2 characters long");
            out = false;
        }

        // look for disallowed characters
        if (! _usernamere.matcher(value).matches()) {
            addErrorMsg(USERNAME, "Please choose a username composed only of "+
                        "letters, numbers, dots (.), dashes (-), and " +
                        "underscores (_).");
            out = false;
        }

        return out;
    }

    /**
     * validate the password.  This does not include checking that the 
     * password meets content pollcies; see checkPassword().
     */
    public boolean validatePassword(String val1, String val2) {
        boolean out = true;
        if (! validateNotBlank(val1, PASSWORD1, "Please enter a password"))
            out = false;
        else if (! validateNotBlank(val2, PASSWORD2, 
                               "Please enter the password again"))
            out = false;
        else if (! val1.equals(val2)) {
            out = false;
            addErrorMsg(PASSWORD1, 
                        "Passwords did not match; please enter again");
        }

        return out;
    }

    /**
     * check that the chosen username is available
     */
    public boolean checkUserName(String val) {
        if (userNameTaken(val)) {
            addErrorMsg(USERNAME, "The username \"" + val + 
                        "\" is already in use; please choose a different one");
            return false;
        }
        return true;
    }

    /**
     * return true if the given username already exists in the user database.
     */
    public static boolean userNameTaken(String username) {
        try {
            return UserDataHandler.userNameExists(username);
        }
        catch (DatabaseAccessException ex) {
            throw new IllegalStateException("Database Access Failure: " + 
                                            ex.getMessage());
        }
    }

    /**
     * check that the password complies with policies on passwords.  
     * This assumes the current values of the user data.
     */
    public boolean checkPassword(String pw) {
        PasswordChecker chkr = new VAOPasswordChecker(_fname, _lname,
                                                      _username, _inst, _email1);
        String[] why = chkr.explainNoncompliance(pw);
        if (why.length == 0) return true;

        for(String reason : why) 
            addErrorMsg(PASSWORD1, reason);

        return false;
    }

    class ParamFormatter {
        String _pre = "";
        String _post = "";
        String _delim = "";
        Comma _sep = new Comma("", "");
        boolean _encode = false;
        StringBuilder buf = new StringBuilder();

        ParamFormatter(String pre, String delim, String post, String sep,
                       boolean htmlEncode)
        {
            if (pre != null) _pre = pre;
            if (post != null) _post = post;
            if (delim != null) _delim = delim;
            if (sep != null) _sep = new Comma("", sep);
            _encode = htmlEncode;
        }

        public void appendParam(String name, String val) {
            if (_encode) val = HtmlEncode.encode(val);
            buf.append(_sep).append(_pre).append(name).append(_delim)
               .append(val).append(_post);
        }

        public void formatParams() {
          if (_username.length() > 0)   appendParam(USERNAME, _username);
          if (_fname.length() > 0)      appendParam(FIRSTNAME, _fname);
          if (_lname.length() > 0)      appendParam(LASTNAME, _lname);
          if (_inst.length() > 0)       appendParam(INSTITUTION, _inst);
          if (_email1.length() > 0)     appendParam(EMAIL, _email1);
          if (_email2.length() > 0)     appendParam(EMAIL2, _email2);
          if (_phone.length() > 0)      appendParam(PHONE, _phone);
          if (_country.length() > 0)    appendParam(COUNTRY, _country);
          if (_portalName.length() > 0) appendParam("portalName=", _portalName);
          if (_returnURL.length() > 0)  appendParam("returnURL=", _returnURL);
        }

        public String toString() { return buf.toString(); }
    }

    /**
     * create URL-GET arguments from these inputs
     */
    public String toURLArgs() {
        ParamFormatter pf = new ParamFormatter("", "=", "", "&", false);
        pf.formatParams();
        return pf.toString();
    }

    /**
     * create a list of hidden fields setting the paramters with their 
     * current values.
     */
    public String toHiddenInputs() {
        ParamFormatter pf = new ParamFormatter("<input type=\"hidden\" name=\"",
                                             "\" value=\"", "\" />", "\n", true);
        pf.formatParams();
        return pf.toString();
    }

    public static final String USERNAME    = "userName";
    public static final String LASTNAME    = "lastName";
    public static final String FIRSTNAME   = "firstName";
    public static final String INSTITUTION = "inst";
    public static final String PHONE       = "phone";
    public static final String COUNTRY     = "country";
    public static final String EMAIL       = "email";
    public static final String EMAIL2      = "email2";
    public static final String PASSWORD1   = "password1";
    public static final String PASSWORD2   = "password2";
    public static final String PORTALNAME  = "portalName";
    public static final String RETURNURL   = "returnURL";
    public static final String[] parameters = {
        FIRSTNAME, LASTNAME, INSTITUTION, PHONE, COUNTRY, EMAIL, 
        EMAIL2, USERNAME, PASSWORD1, PASSWORD2, PORTALNAME, RETURNURL
    };

    public class ParamErrors implements Iterable<String> {
        HashMap<String, List<String> > byname = 
            new HashMap<String, List<String> >();

        ParamErrors() {  }

        public void addMessage(String paramName, String msg) {
            List<String> msgs = byname.get(paramName);
            if (msgs == null) {
                msgs = new ArrayList<String>();
                byname.put(paramName, msgs);
            }
            msgs.add(msg);
        }

        /**
         * return the error message for a given parameter or null if there 
         * are no messages registered.
         */
        public String[] getMessagesFor(String paramName) {
            List<String> msgs = byname.get(paramName);
            if (msgs == null || msgs.size() == 0) return null;

            return msgs.toArray(new String[msgs.size()]);
        }

        /**
         * return true if there messages registered for a given parameter 
         * name
         */
        public boolean hasMessagesFor(String paramName) {
            List<String> msgs = byname.get(paramName);
            return (msgs != null && msgs.size() > 0);
        }

        /**
         * return the number of parameters this container has messages for 
         */
        public int getParamCount() { return byname.size(); }

        /**
         * return the total number of messages collected so far
         */
        public int getMessageCount() { 
            int n = 0;
            for (List<String> msgs : byname.values()) 
                n += msgs.size();
            return n;
        }

        public List<String> toList() {
            List<String> combined = new ArrayList(byname.size());
            for(String param : parameters) {
                List<String> msgs = byname.get(param);
                if (msgs != null && msgs.size() > 0) {
                    for (String msg : msgs) 
                        combined.add(msg);
                }
            }
            return combined;
        }

        public String[] toArray() {
            List<String> combined = toList();
            return combined.toArray(new String[combined.size()]);
        }

        /**
         * return a flat iteration through all registered messages.
         */
        public ListIterator<String> iterator() {
            List<String> combined = toList();
            return combined.listIterator();
        }
    }
    
}