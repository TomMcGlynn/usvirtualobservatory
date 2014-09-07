package org.nvo;
    import java.util.*;
    import org.globus.purse.exceptions.DatabaseAccessException;
    import org.globus.purse.registration.databaseAccess.UserDataHandler;

    
    public class NVOForm {
        /*  The properties */
        String name = "";
        String institution = "";
        String phone = "";
        String country = "";
        String email = "";
        String uname = "";
        String password = "";
        String cpassword = "";
    
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name.trim();
        }
    
        public String getInst() {
            return institution;
        }
        public void setInst(String institution) {
            this.institution = institution.trim();
        }
    
        public String getPhone() {
            return phone;
        }
        public void setPhone(String phone) {
            this.phone = phone.trim();
        }
    
        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country.trim();
        }
    
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email.trim();
        }
    
        public String getUname() {
            return uname;
        }
        public void setUname(String uname) {
            this.uname = uname.trim();
        }

        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password.trim();
        }

        public String getCPassword() {
            return cpassword;
        }
        public void setCPassword(String cpassword) {
            this.cpassword = cpassword.trim();
        }
    
        /* Errors */
        public static final Integer ERR_EMAIL_INVALID = new Integer(1);
        public static final Integer ERR_PASSWORD_MATCH = new Integer(2);
        public static final Integer ERR_PASSWORD_INVALID = new Integer(3);
        public static final Integer ERR_UNAME_INVALID = new Integer(4);
    
        // Holds error messages for the properties
        Map errorCodes = new HashMap();
    
        // Maps error codes to textual messages.
        // This map must be supplied by the object that instantiated this bean.

        Map msgMap;
        public void setErrorMessages(Map msgMap) {
            this.msgMap = msgMap;
        }
    
        public String getErrorMessage(String propName) {
            Integer code = (Integer)(errorCodes.get(propName));
            if (code == null) {
                return "";
            } else if (msgMap != null) {
                String msg = (String)msgMap.get(code);
                if (msg != null) {
                    return msg;
                }
            }
            return "Error";
        }
    
        /* Form validation and processing */
        public boolean isValid() {
            // Clear all errors
            errorCodes.clear();
    
         // Validate username
            try {
                if (UserDataHandler.userNameExists(uname)) {
                errorCodes.put("uname", ERR_UNAME_INVALID);

                }
            } catch (DatabaseAccessException exp) {
            }

            // Validate password
            if (!password .equals(cpassword)) {
                errorCodes.put("password", ERR_PASSWORD_MATCH);
            } else if (password.length() < 6) {
                errorCodes.put("password", ERR_PASSWORD_INVALID);
            }
    
            // If no errors, form is valid
            return errorCodes.size() == 0;
        }
    
        public boolean process() {
            if (!isValid()) {
                return false;
            }
    
            // Process form...
    
            // Clear the form
            name = "";
            institution = "";
            phone = "";
            country = "";
            email = "";
            errorCodes.clear();
            return true;
        }
    }
