package org.usvao.sso.ip.register;

import org.usvao.sso.ip.User;
import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.pw.PasswordHasher;
import org.usvao.sso.ip.pw.Salted1Hasher;
import org.usvao.sso.ip.db.UserDatabase;
import org.usvao.sso.ip.db.UserDatabaseAccessException;
import org.usvao.sso.ip.db.DatabaseConfigException;

import java.util.Properties;
import java.util.HashMap;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * an application that loads users from a CSV file.
 *
 * The first line of the filegives the user attribute names for the 
 * attributes in the CSV file.
 */
public class LoadUsersFromCSVApp {
    public final static Log log = LogFactory.getLog("LoadusersFromCSVApp");

    private String usernameCol = "username";
    private String passwordCol = "pw";
    private String saltCol = "salt";
    private boolean pwhashed = false;
    private UserDatabase udb = null;
    private Properties defAttrs = new Properties();
    static Salted1Hasher hasher = new Salted1Hasher();

    /**
     * create the app
     * @param propfile   the app properties file
     */
    public LoadUsersFromCSVApp(File propfile) 
        throws IOException, DatabaseConfigException
    {
        this(loadprop(propfile));
    }

    /**
     * create the app
     * @param propfile   the app properties file
     */
    public LoadUsersFromCSVApp(String propfile) 
        throws IOException, DatabaseConfigException
    {
        this(new File(propfile));
    }
    static Properties loadprop(File filename) throws IOException {
        Properties out = new Properties();
        out.load(new FileInputStream(filename));
        return out;
    }

    /**
     * create the app
     * @param dbprops   the app properties 
     */
    public LoadUsersFromCSVApp(Properties dbprops) 
        throws DatabaseConfigException
    {
        udb = UserDatabase.connect(dbprops);
        defAttrs.setProperty("passwordMethod", Salted1Hasher.NAME);
    }

    /**
     * set the label that will identify the column containing the username
     */
    public void setUsernameColumn(String name) {
        if (name == null) throw new NullPointerException("name");
        usernameCol = name;
    }

    /**
     * set the label that will identify the column containing the password
     * (or password hash)
     */
    public void setPasswordColumn(String name) {
        if (name == null) throw new NullPointerException("name");
        passwordCol = name;
    }

    /**
     * set the label that will identify the column containing the password
     * (or password hash)
     */
    public void setSaltColumn(String name) {
        if (name == null) throw new NullPointerException("name");
        saltCol = name;
    }

    /**
     * return true if the in coming password should be assumed to be hashed
     * already.  Note that if true, input CSV files must include a salt
     * column with a name "salt" (or what was set with setSaltColumn()).
     */
    public boolean passwordIsHashed() { return pwhashed; }

    /**
     * set whether we should assume that the password is hashed already.
     * Note that if true, input CSV files must include a salt column with 
     * a name "salt" (or what was set with setSaltColumn()).
     */
    public void assumePasswordHashed(boolean yes) { pwhashed = yes; }

    /**
     * load the users listed in the given CSV file.  It is assumed that 
     * the first line read will be the CSV header.
     * @param csv     the CSV file stream
     * @param status  the status to assign the users
     * @returns int   the number of users added
     */
    public int loadusers(Reader csv, User.Status status) 
        throws IOException, SSOProviderSystemException 
    {
        return loadusers(csv, status, -1);
    }

    /**
     * load the users listed in the given CSV file.  It is assumed that 
     * the first line read will be the CSV header.
     * @param csv     the CSV file stream
     * @param status  the status to assign the users
     * @param maxrecs the maximum number of user records to add
     * @returns int   the number of users added
     */
    public int loadusers(Reader csv, User.Status status, int maxrecs) 
        throws IOException, SSOProviderSystemException
    {
        BufferedReader rdr = null;
        try { rdr = (BufferedReader) csv; }
        catch (ClassCastException ex) {
            rdr = new BufferedReader(csv);
        }

        String line = rdr.readLine();
        String[] colnames = line.split(",");
        HashMap<String, Integer> colidx = new HashMap<String, Integer>();
        for(int i=0; i < colnames.length; i++) 
            colidx.put(colnames[i], i);

        if (! colidx.containsKey(usernameCol)) {
            String err = "CSV does not contain a " + usernameCol + " column";
            log.error(err);
            throw new IllegalStateException(err);
        }
        if (passwordIsHashed() && ! colidx.containsKey(saltCol)) {
            String err = "CSV does not contain a " + saltCol + " column";
            log.error(err);
            throw new IllegalStateException(err);
        }
        if (! colidx.containsKey(passwordCol)) {
            String err = "CSV does not contain a " + passwordCol + " column";
            log.warn(err);
        }

        Properties attrs = new Properties(defAttrs);
        String[] cols = null;
        String username = null, pw = null;
        int nshort=0, nlong=0, count=0;
        while ((line = rdr.readLine()) != null) {
            cols = line.split(",");

            if (cols.length < colnames.length) {
                nshort++; nlong=0;
                log.error("Skipping record with one or more missing columns "+
                          "starting with '" + line.substring(0,25) + 
                          "...'");
                continue;
            }
            else if (cols.length > colnames.length) {
                nlong++; nshort=0;
                log.warn("Ignoring extra columns for record with "+
                         "username='" + cols[colidx.get(usernameCol)] + "'");
            } 
            else {
                if (nshort > 1)
                    log.warn("Repeated "+(nshort-1)+" additional times");
                else if (nlong > 1)
                    log.warn("Repeated "+(nlong-1)+" additional times");
                nshort = nlong = 0;
            }

            username = cols[colidx.get(usernameCol)];
            pw = "";
            if (colidx.containsKey(passwordCol))
                pw = cols[colidx.get(passwordCol)];
            if (colidx.containsKey(saltCol))
                attrs.setProperty("salt", cols[colidx.get(saltCol)]);

            for (String name : colidx.keySet()) {
                if (! name.equals(usernameCol) && ! name.equals(passwordCol) &&
                    ! name.equals(saltCol))
                  attrs.setProperty(name, cols[colidx.get(name)]);
            }

            try {
                if (! passwordIsHashed() && colidx.containsKey(passwordCol)) {
                    if (! attrs.containsKey("salt"))
                        attrs.setProperty("salt", hasher.newSalt());
                    pw = hasher.hash(pw, attrs.getProperty("salt"));
                }
                attrs.setProperty("passwordHash", pw);

                udb.addUser(username, attrs, status);
                count++;
                if (maxrecs > 0 && count >= maxrecs) break;
            }
            catch (UserDatabaseAccessException ex) {
                log.error("Failed to load record for username='"+username+
                          "': " + ex.getMessage());
            }
        }

        return count;
    }

    /**
     * load the users listed in the given CSV file.  It is assumed that 
     * the first line read will be the CSV header.
     * @param csv     the CSV file
     * @param status  the status to assign the users
     */
    public int loadusers(File csv, User.Status status) 
        throws IOException, SSOProviderSystemException 
    {
        return loadusers(new FileReader(csv), status);
    }

    /**
     * load the users listed in the given CSV file.  It is assumed that 
     * the first line read will be the CSV header.
     * @param csv     the CSV file
     * @param status  the status to assign the users
     */
    public int loadusers(File csv, User.Status status, int maxrecs) 
        throws IOException, SSOProviderSystemException 
    {
        return loadusers(new FileReader(csv), status, maxrecs);
    }    

    /**
     * execute the application on a list of files.  
     * 
     * Usage: LoadusersFromCSVApp propfile status csvfile [count] [csvfile [count] ...]
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            StringBuffer what =  new StringBuffer("Missing arguments:");
            if (args.length < 1) what.append(" propfile");
            if (args.length < 2) what.append(" status");
            what.append(" csvfile");
            LoadUsersFromCSVApp.log.fatal(what);
            System.exit(1);
        }

        String propfile = args[0];
        User.Status status = User.Status.PENDING;
        if ("accepted".equalsIgnoreCase(args[1])) status = User.Status.ACCEPTED;
        else if ("requested".equalsIgnoreCase(args[1])) 
            status = User.Status.REQUESTED;
        else if ("rejected".equalsIgnoreCase(args[1])) 
            status = User.Status.REJECTED;
        else if ("renewal".equalsIgnoreCase(args[1])) 
            status = User.Status.RENEWAL;

        LoadUsersFromCSVApp app = null;
        try {
            app = new LoadUsersFromCSVApp(propfile);
        } catch (IOException ex) {
            LoadUsersFromCSVApp.log.fatal("Failed to load system properties: "+
                                          ex.getMessage());
            System.exit(2);
        } catch (DatabaseConfigException ex) {
            LoadUsersFromCSVApp.log.fatal("Failed to configure database: "+
                                          ex.getMessage());
            System.exit(2);
        }
        int i = 2, nrecs=0, max=-1;
        String filename = null;

        for(i=2; i < args.length; i++) {
            filename = args[i];
            if (i+1 < args.length) {
                try {
                    max = Integer.parseInt(args[i+1]);
                    i++;
                } catch (NumberFormatException ex) {
                    max = -1;
                }
            }

            try {
                nrecs += app.loadusers(new File(filename), status, max);
            } catch (IOException ex) {
                app.log.error("Trouble loading users from "+filename+ 
                              " ("+ex.getMessage()+")");
            } catch (SSOProviderSystemException ex) {
                app.log.fatal(ex.getMessage());
                System.exit(3);
            } 
        }

        System.out.println("Added "+nrecs+" users");
    }
}
