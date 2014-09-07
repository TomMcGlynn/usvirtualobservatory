package org.usvao.sso.ip.service;

import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.text.MessageFormat;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

import org.usvao.sso.ip.SSOProviderServiceException;
import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.db.UserDatabaseAccessException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.MailAccessException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.mailProcessing.MailManager;
import org.globus.purse.registration.mailProcessing.MailOptions;
import org.globus.purse.registration.UserData;
import org.globus.purse.registration.RegisterUtil;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


/**
 * a service bean for reminding users of their username.
 */
public class RemindUserNameRequest extends ServiceRequestBean {

    static Log logger =
	LogFactory.getLog(RemindUserNameRequest.class.getName());

    // bean properties/parameters
    public final static String ACTION = "action";
    public final static String MYACTION = "remind";
    public final static String EMAILS = "emails";
    public final static String[] parameters = { ACTION, EMAILS };

    public String getEmails() { return getParameter(EMAILS); }
    public void setEmails(String emails) { 
        parsed = null;
        setParameter(EMAILS, emails); 
    }
    public String getAction() { return getParameter(ACTION); }

    // private data
    private Vector<Object[]> parsed = null;

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
     * create an empty service bean.
     */
    public RemindUserNameRequest() { 
        super(parameters); 
        setParameter(ACTION, MYACTION);
    }

    /**
     * connect to this service with the given input parameters
     */
    public RemindUserNameRequest(String emails) {
        this();
        setEmails(emails);
    }

    /**
     * add an address to the list of addresses check for
     * @throws AddressException   if the address is syntactically incorrect.
     */
    public synchronized void addAddress(String emailAddress) 
        throws AddressException 
    {
        InternetAddress ia = new InternetAddress(emailAddress);
        ia.validate();
        StringBuilder sb = new StringBuilder();
        if (getEmails().length() > 0) sb.append("\n");
        sb.append(ia.toString());
        parsed = null;
    }

    /**
     * return true if the input paramters are valid for processing by the 
     * service.  If they are not, register errors internally.  
     * <p>
     * This implentation not only checks to see if email addresses were 
     * provided but also checks at least one provide is syntactically 
     * correct.  
     */
    @Override
    public boolean validate() throws SSOProviderSystemException { 
        _errors.clear();
        if (getEmails().length() == 0) {
            addErrorMsg(EMAILS, "Please enter one or more email addresses");
            return false; 
        }

        if (! parseAddresses()) {
            addErrorMsg(EMAILS, "Please enter at least one valid email address");
            addErrorMsg("", "Input contains no valid addresses; no emails sent");
            return false;
        }
        else {
            for (Object[] addr : parsed) {
                if (addr[1] != null) continue;
                addErrorMsg(EMAILS, "Could not parse '" + addr[0] + 
                                    "; no email sent there");
            }
        }

        return true;
    }

    /**
     * execute the request 
     */
    public void execute() 
        throws SSOProviderServiceException, SSOProviderSystemException
    {
        if (! validate())
            throw new InvalidInputsException(_errors);

        try {
            Vector<String> sent = new Vector<String>(parsed.size());
            Vector<String> failed = new Vector<String>();
            Vector<String> pend = new Vector<String>();
            HashMap<String, UserData> pendudata = 
                new HashMap<String, UserData>();

            InternetAddress ia = null;
            String address = null;
            UserData[] users = null;
            Vector<String> usernames = null;
            HashMap<String, UserData> udata = null;
            String uname = null;
            for(Object [] addr : parsed) {
                ia = (InternetAddress) addr[1];
                if (ia == null) 
                    // this one failed to parse; skip it
                    continue;

                users = UserDataHandler.getAllDataForEmail(ia.getAddress());
                if (users == null || users.length == 0) {
                    // not found.  
                    failed.add((String) addr[0]);
                }
                else {
                    usernames = new Vector<String>(users.length);
                    udata = new HashMap<String, UserData>();
                    for(UserData user : users) {
                        uname = user.getUserName();
                        if (user.getStatus() == acceptedStatus()) {
                            usernames.add(uname); 
                            udata.put(uname, user);
                        }
                        else if (user.getStatus() == pendingStatus()) {
                            uname += "*";
                            usernames.add(uname); 
                            udata.put(uname, user);
                            pend.add(user.getUserName());
                            pendudata.put(user.getUserName(), user);
                        }
                    }
                    if (usernames.size() == 0) 
                        failed.add((String) addr[0]);
                    else {
                        sendReminder(ia.getAddress(), usernames, udata);
                        sent.add((String) addr[0]);
                    }
                }
            }

            if (sent.size() > 0) {
                addErrorMsg("", "Successfully sent messages to the following addresses:");
                for (String email : sent)
                    addErrorMsg("", email);
            }
            else {
                addErrorMsg("", "No messages sent.");
            }
            if (failed.size() > 0) {
                addErrorMsg("", "No usernames (pending or active) were found associated with:");
                for (String email : sent)
                    addErrorMsg("", email);
            }

            if (pend.size() > 0) {
                for (String user : pend) 
                    sendConfirmEmail(pendudata.get(user));
            }
        }
        catch (DatabaseAccessException e) {
            logger.error("DB error while reminding of usernames: " + e);
            throw new UserDatabaseAccessException(e.getMessage(), e);
        }
        catch (RegistrationException e) {
            // should not happen
            String msg = "failed to find status ids in DB: " + e;
            logger.error(msg);
            throw new SSOProviderSystemException(msg, e);
        }
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

    void sendReminder(String email, List<String> usernames, 
                      Map<String, UserData> uname2data) 
        throws MailAccessException
    {
        Properties data = new Properties();
        String prop = null;
        MailOptions mailOptions = MailManager.getOptions();
        if (mailOptions == null) 
            throw new MailAccessException("MailOptions not initialized!");

        // salutation: pick the first non-pending name
        for (String u : usernames) {
            if (! u.endsWith("*")) {
                prop = u;
                break;
            }
        }
        if (prop == null) prop = usernames.get(0);
        data.setProperty("fullname", prop);

        // a formatted list of usernames
        StringBuilder sb = new StringBuilder();
        for(String user : usernames) 
            sb.append("\n    ").append(user);
        data.setProperty("usernames", sb.toString());

        // email for getting help
        prop = mailOptions.getUserAccountAddr();
        try {  prop = (new InternetAddress(prop)).getAddress(); }
        catch (AddressException ex) {
            logger.error("Configuration has bad address in userAcctAddress:" +
                         prop);
            prop = "vaosso@ncsa.illinois.org";
        }
        data.setProperty("helpemail", prop);

        // password services URL
        prop = mailOptions.getCABaseUrl();
        String passServURL = prop+"/password.jsp";
        data.setProperty("passservices", passServURL);

        // formatted list of password reset URLs
        String format = passServURL+"?action=reset&userName={0}&token={1}";
        sb = new StringBuilder();
        for(String u : usernames) {
            if (u.endsWith("*")) continue;
            prop = uname2data.get(u).getToken();
            sb.append("\n  Username: ").append(u)
              .append("\n  Token: ").append(prop)
              .append("\n  URL: ")
              .append(MessageFormat.format(format, u, prop)).append('\n');
        }
        data.setProperty("resets", sb.toString());

        MailManager.sendUsernameReminderMail(email, data);
    }

    boolean sendConfirmEmail(UserData user) 
        throws DatabaseAccessException, MailAccessException, 
               RegistrationException
    {
        if (user.getStatus() != pendingStatus())
            return false;

        Properties msgData = new Properties();
        RegisterUtil.loadUserTags(msgData);

        // fullname
        String fullname = user.getLastName();
        String first = user.getFirstName();
        if (first != null && first.length() > 0)
            fullname = first+" "+fullname;
        msgData.setProperty("fullname", fullname);

        // token
        msgData.setProperty("secret", user.getToken());

        // send message
        MailManager.sendTokenMail(user.getEmailAddress(), msgData);
        return true;
    }

    /**
     * parse the addresses and count the number of addresses (good or bad)
     * that were found.
     */
    public synchronized int countAddresses() {
        try {
            if (parsed == null) parseAddresses();
        } catch (SSOProviderSystemException ex) {
            // shouldn't happen
            System.err.println("sys error during parsing: " + ex.getCause());
            logger.error("sys error during parsing: " + ex.getCause());
            parsed = null;
        }
        if (parsed == null) return 0;
        return parsed.size();
    }

    /**
     * parse the addresses and count the number of syntactically good 
     * addresses that were found.
     */
    public synchronized int countGoodAddresses() {
        try {
            if (parsed == null) parseAddresses();
        } catch (SSOProviderSystemException ex) {
            // shouldn't happen
            logger.error("sys error during parsing: " + ex.getCause());
            parsed = null;
        }
        if (parsed == null) return 0;

        int c=0;
        for(Object[] addr : parsed) {
            if (addr[1] != null) c++;
        }

        return c;        
    }

    /**
     * parse the addresses and return addresses (good or bad)
     */
    public synchronized String[] getAddresses() {
        try {
            if (parsed == null) parseAddresses();
        } catch (SSOProviderSystemException ex) {
            // shouldn't happen
            logger.error("sys error during parsing: " + ex.getCause());
            parsed = null;
        }
        if (parsed == null) return new String[0];

        String[] out = new String[parsed.size()];
        int i=0;
        for(Object[] addr : parsed) 
            out[i++] = (String) addr[0];

        return out;
    }

    /**
     * parse the addresses and return addresses (good or bad)
     */
    public synchronized String[] getGoodAddresses() {
        try {
            if (parsed == null) parseAddresses();
        } catch (SSOProviderSystemException ex) {
            // shouldn't happen
            logger.error("sys error during parsing: " + ex.getCause());
            parsed = null;
        }
        if (parsed == null) return new String[0];

        Vector<String> out = new Vector<String>(parsed.size());
        for(Object[] addr : parsed) {
            if (addr[1] != null) out.add((String) addr[0]);
        }

        return out.toArray(new String[out.size()]);
    }

    /**
     * parse and save the addresses internally.
     * @return boolean   true if at least one syntactically good address was 
     *                      found; false if none were found.
     */
    protected synchronized boolean parseAddresses() 
        throws SSOProviderSystemException 
    {
        boolean goodfound = false;
        parsed = new Vector<Object[]>();  // where we'll store the 
                                          // parsed addresses

        try {
            BufferedReader rdr = new BufferedReader(new StringReader(getEmails()));

            String line = null;
            String email = null;
            InternetAddress ia = null;
            int b = -1;
            while ((line = rdr.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;

                /*
                 * there may be more than one address here; don't throw 
                 * the whole thing out
                 *
                if (line.indexOf("@") < 1) {
                    // no @; format looks garbled
                    parsed.add(new Object[] { line, null });
                    continue;
                }
                */

                // despite instructions, allow multiple addresses on one line.
                while (line.length() > 0) {
                    b = line.indexOf(">");
                    if (b > 3) {
                        // looks like: "John A. Doe" <jdoe@sample.org>
                        email = line.substring(0,b+1);
                        line = line.substring(b+1).trim();
                        try { 
                            ia = new InternetAddress(email);
                            ia.validate(); 
                            parsed.add(new Object[] { email, ia });
                            goodfound = true;
                        } catch (AddressException e) { 
                            // illegal format
                            parsed.add(new Object[] { email, null });
                            continue;
                        }
                    }
                    else {
                        // assume next word is a simple (jdoe@sample.org) address
                        String[] words = line.split("\\s+", 2);
                        email = words[0];
                        line = (words.length > 1) ? words[1] : "";
                        try { 
                            ia = new InternetAddress(email);
                            ia.validate(); 
                            parsed.add(new Object[] { email, ia });
                            goodfound = true;
                        } catch (AddressException e) { 
                            // illegal format
                            parsed.add(new Object[] { email, null });
                        }
                    }
                }
            }
        }
        catch (IOException ex) {
            // should not happen
            throw new SSOProviderSystemException("Error while parsing addresses: " + ex, ex);
        }

        return goodfound;
    }

    public String toString() {
        String sep = " : ", delim = ", ";
        
        try {
            if (parsed == null) parseAddresses();
        } catch (SSOProviderSystemException ex) {
            // shouldn't happen
            parsed = null;
            return "[<unprintable RemindUserNameRequest>]";
        }
        if (parsed == null) return "[]";

        StringBuilder sb = new StringBuilder("[");
        for (Object[] a : parsed) 
            sb.append(a[0]).append(sep).append(delim);
        sb.delete(sb.length()-delim.length(), sb.length());
        sb.append(']');
        return sb.toString();
    }
}