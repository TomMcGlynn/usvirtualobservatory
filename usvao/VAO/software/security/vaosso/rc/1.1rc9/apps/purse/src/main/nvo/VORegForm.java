package nvo;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.util.Comma;
import org.globus.purse.util.HtmlEncode;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;

public class VORegForm {
    private String fname = "";
    private String lname = "";
    private String email = "";
    private String inst = "";
    private String phone = "";
    private String userName = "";
    private String password1 = "";
    private String password2 = "";
    private String returnURL = "";
    private String portalName = "";
    private String country = "";
    private Properties errors = new Properties();

    public static final String DEFAULT_RETURN_URL = "http://www.us-vo.org/";

    public boolean validate() {
        boolean result = true;
        if (isBlank(lname)) {
            errors.put("lastName", "Please enter your last name");
            result = false;
        }

        if (isBlank(fname)) {
            errors.put("firstName", "Please enter a first name");
            result = false;
        }

        if (isBlank(phone)) {
            errors.put("phone", "Please enter your phone number.");
            result = false;
        }

        if (isBlank(country)) {
            errors.put("country", "Please select a country name.");
            result = false;
        }
        if (isBlank(email) || (email.indexOf('@') == -1)) {
            this.errors.put("email", "Please enter a valid email address.");
            result = false;
        }

        boolean usernameValid = true;
        if (isBlank(userName) || userName.length() < 2) {
            errors.put("userName", "Please choose a username with at least two characters.");
            result = false;
            usernameValid = false;
        }
        if (usernameValid && userName.indexOf(' ') >= 0) {
            errors.put("userName", "Please choose a username with no spaces. You may use _, -, or . instead.");
            usernameValid = false;
            result = false;
        }
        if (usernameValid && !userName.matches("[[0-9][a-z][A-Z][_\\.-]]+")) {
            errors.put("userName", "Please choose a username with no special characters -- use only letters, numbers, dot, dash, and underline.");
            usernameValid = false;
            result = false;
        }
        if (usernameValid && (userNameTaken(this.userName))) {
            errors.put("userName", "The username \"" + userName + "\" is already in use; please try a different one.");
            result = false;
        }

        if (isBlank(password1) || password1.length() < 6) {
            errors.put("password1", "Please choose a password with at least 6 characters.");

            password1 = "";
            password2 = "";
            result = false;
        } else if (!password1.equals(password2)) {
            this.errors.put("password2", "Passwords did not match; please re-enter passwords.");

            this.password1 = "";
            this.password2 = "";
            result = false;
        }

        return result;
    }

    public static boolean userNameTaken(String paramString) {
        try {
            return UserDataHandler.userNameExists(paramString);
        }
        catch (DatabaseAccessException localDatabaseAccessException) {
            throw new IllegalStateException("Database Access Failure: " + localDatabaseAccessException.getMessage());
        }
    }

    public String getErrorMsg(String paramString) {
        String str = (String) this.errors.get(paramString.trim());
        return ((str == null) ? "" : str);
    }

    public String getLastName() { return lname; }
    public String getFirstName() { return fname; }
    public String getInst() { return inst; }
    public String getPhone() { return phone; }
    public String getCountry() { return country; }
    public String getEmail() { return email; }
    public String getPortalName() { return portalName; }
    public boolean isReturnURLBlank() { return isBlank(returnURL); }
    public String getReturnURL() { return returnURL; }
    public String getReturnURL(boolean defaultIfBlank) {
        return isReturnURLBlank() && defaultIfBlank ? DEFAULT_RETURN_URL : returnURL;
    }
    public String getUserName() { return userName; }
    public String getPassword1() { return password1; }
    public String getPassword2() { return password2; }

    public void setLastName(String name) { this.lname = clean(name); }
    public void setFirstName(String name) { this.fname = clean(name); }
    public void setInst(String inst) { this.inst = clean(inst); }
    public void setPhone(String phone) { this.phone = clean(phone); }
    public void setCountry(String country) { this.country = clean(country); }
    public void setEmail(String email) { this.email = clean(email); }
    public void setPortalName(String portalName) { this.portalName = clean(portalName); }
    public void setReturnURL(String returnUrl) { this.returnURL = clean(returnUrl); }
    public void setUserName(String username) { this.userName = clean(username); }
    public void setPassword1(String password1) { this.password1 = clean(password1); }
    public void setPassword2(String password2) { this.password2 = clean(password2); }
    public void setErrorMsg(String name, String msg) { errors.put(name, msg); }

    public void loadErrors(Properties errors) {
        if (errors == null) errors = new Properties();
        this.errors = errors;
    }
    public Properties getErrors() { return errors; }

    public void printErrors(OutputStream paramOutputStream) {
        printErrors(new OutputStreamWriter(paramOutputStream));
    }

    public void printErrors(Writer paramWriter) {
        PrintWriter localPrintWriter = new PrintWriter(paramWriter);
        Properties localProperties = getErrors();
        localPrintWriter.println("Found " + localProperties.size() + " errors:");
        for (Enumeration localEnumeration = localProperties.elements(); localEnumeration.hasMoreElements();)
            localPrintWriter.println("  " + localEnumeration.nextElement());
    }

    private static boolean isBlank(String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }

    private String clean(String s) { return s == null ? "" : s.trim(); }

    public String makeArgs() { return makeArgs("", "=", "", "&", false); }

    public String makeHiddenFields() {
        return makeArgs("<input type=\"hidden\" name=\"", "\" value=\"", "\"/>", "\n", true);
    }

    public String emptyProperties() {
        StringBuffer localStringBuffer = new StringBuffer();
        if (this.lname.equals("")) localStringBuffer.append("&lname=");
        if (this.fname.equals("")) localStringBuffer.append("&fname=");
        if (this.inst.equals("")) localStringBuffer.append("&inst=");
        if (this.email.equals("")) localStringBuffer.append("&email=");
        if (this.country.equals("")) localStringBuffer.append("&country=");
        return localStringBuffer.toString();
    }

    public String makeArgs(String prefix, String infix, String suffix, String separator, boolean form) {
        StringBuffer result = new StringBuffer();
        Comma sep = new Comma("", separator);
        if (!isBlank(userName))
            result.append(sep).append(makeArg("userName", userName, prefix, infix, suffix, form));
        if (!isBlank(lname))
            result.append(sep).append(makeArg("name", lname, prefix, infix, suffix, form));
        if (!isBlank(fname))
            result.append(sep).append(makeArg("name", fname, prefix, infix, suffix, form));
        if (!isBlank(inst))
            result.append(sep).append(makeArg("inst", inst, prefix, infix, suffix, form));
        if (!isBlank(email))
            result.append(sep).append(makeArg("email", email, prefix, infix, suffix, form));
        if (!isBlank(phone))
            result.append(sep).append(makeArg("phone", phone, prefix, infix, suffix, form));
        if (!isBlank(country))
            result.append(sep).append(makeArg("country", country, prefix, infix, suffix, form));
        if (!isBlank(returnURL))
            result.append(sep).append(makeArg("returnURL", returnURL, prefix, infix, suffix, form));
        if (!isBlank(portalName))
            result.append(sep).append(makeArg("portalName", portalName, prefix, infix, suffix, form));
        return result.toString();
    }

    private String makeArg(String name, String value, String prefix, String infix, String suffix, boolean form) {
        return prefix + name + infix + (form ? HtmlEncode.encode(value) : HtmlEncode.encodeHREFParam(value))+ suffix;
    }
}
