package org.usvao.sso.openid.portal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * a simple user database implementation that stores the user data in a 
 * flat file.  This class is intended mainly for testing purposes and a 
 * small number of users.
 * <p>
 * The user data file has a simple format...
 *
 * @see WriteableFileUserDb
 */
public class SimpleFileUserDb implements UserDatabase, CommonUserStatus {

    protected URL userdb = null;

    /**
     * create an unitialized database.  The database file must be set 
     * via {@link #setDbFile(String) setDbFile()} before this file can
     * be used. 
     */
    public SimpleFileUserDb() { }

    /**
     * initialize this database to use the given file as its data source.
     * See {@link #setDbFile(String) setDbFile()} for details on interpreting
     * the path.
     */
    public SimpleFileUserDb(String dbFile) throws IOException { 
        setDbFile(dbFile);
    }

    /**
     * initialize this database to use the given file as its data source.
     */
    public SimpleFileUserDb(File dbFile) throws IOException { 
        this(dbFile.getAbsolutePath());
    }

    /**
     * set the file containing the user data.  (See main documentation for 
     * an explanation of its format.)  This method will actually check for 
     * the existance of the file and ensure that it can be read.  
     * <p>
     * @param dbFile   the path to the user data file.  If the given path is 
     *                 relative it will be interpreted as a resource from the 
     *                 classpath (relative to the root of the package 
     *                 hierarchy).  An absolute path will be treated as an
     *                 arbitrary file in the filesystem.
     * @throws FileNotFoundException    if the file cannot be found.
     * @throws IOException              if the file cannot be read.  
     */
    public void setDbFile(String dbFile) throws IOException {

        URL in = null;
        File file = new File(dbFile);
        if (file.isAbsolute()) {
            // find it in an arbitrary location on disk
            if (! file.exists()) 
                throw new FileNotFoundException(file.toString());
            try {
                in = new URL("file://" + file.toString());
            }
            catch (MalformedURLException ex) {
                throw new IllegalArgumentException("setDbFile: bad file path "
                                                   + "format: " + dbFile);
            }
        }
        else {
            // it's resource found relative to the classpath root.
            in = getClass().getClassLoader().getResource(dbFile);
            if (in == null) throw new FileNotFoundException(dbFile);
        }

        checkReadable(in);
        userdb = in;
    }

    /**
     * check to see if the given URL can be read.
     * @param dbFile   the user database file as a URL
     * @throws IOException   if there is a problem reading or parsing the URL.
     */
    protected void checkReadable(URL dbFile) throws IOException {
        BufferedReader in = openURL(dbFile);
        try { in.readLine(); }
        finally { try { in.close(); } catch (IOException ex) { } }
    }

    /**
     * check to see if the internal user database file can be read.
     */
    public void checkReadable() throws IOException {
        checkDbFileSet();
        checkReadable(userdb);
    }

    /**
     * check to makc sre that the user database file has been set yet.  
     * The file is set either at construction time or via 
     * {@link #setDbFile(String) setDbFile()}.  
     * @throws IllegalStateException   if the file has not been set.
     */
    public void checkDbFileSet() {
        if (userdb == null) 
            throw new IllegalStateException("User database file not yet set"); 
    }

    protected BufferedReader openURL(URL src) throws IOException {
        return new BufferedReader(new InputStreamReader(src.openStream()));
    }

    static String defFieldSep = ":";
    protected String fieldSep = defFieldSep;
    protected Pattern fieldSepP = Pattern.compile("\\s*"+fieldSep+"\\s*");
    static String defRoleSep = " ";
    protected String roleSep = defRoleSep;
    protected Pattern roleSepP = Pattern.compile("\\s+");
    static String defAttrSep = ",";
    protected String attrSep = defAttrSep;
    protected Pattern attrSepP = Pattern.compile("\\s*"+attrSep+"\\s*");
    static String defCommentChar = "#";
    protected String commentChar = defCommentChar;
    protected Pattern commentPart = Pattern.compile(commentChar+".*$");
    static String defMetaUsername = "_meta_";
    protected String metaUsername = defMetaUsername;

    /**
     * a simple container for a User found in the user database file
     */
    public class User implements CommonUserStatus {
        String name = null;
        int status = STATUS_UNKNOWN;
        HashSet<String> roles = new HashSet<String>();
        HashMap<String, String> attrs = null;
        String[] attvals = null;

        public User(String[] flds) {
            this(flds[0], (flds.length > 1) ? flds[1] : null);
            if (flds.length > 2 && flds[2].length() > 0) 
                attvals = attrSepP.split(flds[2]);
            else
                attvals = new String[0];
        }
        public User(String username, String auths) {
            name = username;
            status = STATUS_DISABLED;
            if (auths != null) {
                for (String role : roleSepP.split(auths)) {
                    if (role.length() == 0) continue;
                    roles.add(role);
                }
                status = STATUS_ACTIVE;
            }
        }
        public User(String[] flds, String[] attrNames) {
            this(flds);
            if (attrNames != null) {
                attrs = new HashMap<String, String>(attrNames.length);
                for(int i=0; i < attrNames.length; i++) {
                    if (attvals != null && i < attvals.length)
                        attrs.put(attrNames[i], attvals[i]);
                    else
                        attrs.put(attrNames[i], "");
                }
            }
        }

        public String getName() { return name; }
        public int getStatus() {  return status; }
        public String[] getAttributeList() { return attvals; }
        public Map<String, String> getAttributes() {
            if (attrs != null) return attrs;
            return new HashMap<String, String>();
        }
        public Set<String> getAuthorizations() {  return roles; }
    }

    /**
     * extract a user record from the file.  Null is returned if the 
     * user is not found.  
     */
    public synchronized User findUser(String username) throws IOException {
        checkDbFileSet();

        String[] flds = null;
        String[] attrnames = null;

        BufferedReader rdr = openURL(userdb);
        try {
            for(String line = rdr.readLine(); 
                line != null; line = rdr.readLine())
            {
                line = commentPart.matcher(line).replaceAll("").trim();
                if (line.length() == 0) continue;
                flds = fieldSepP.split(line, 3);

                if (flds.length > 0) {
                    if (flds[0].equals(username)) 
                        return new User(flds, attrnames);

                    if (flds[0].equals(metaUsername)) {
                        if (flds.length > 2)
                            attrnames = attrSepP.split(flds[2]);
                        else
                            attrnames = new String[0];
                    }
                }
            }
        }
        finally {
            rdr.close();
        }

        return null;
    }

    public String[] getAttributeNames() throws IOException {
        User meta = findUser(metaUsername);
        if (meta == null) return new String[0];
        return meta.getAttributeList();
    }

    /**
     * return the current registration status of a user
     */
    @Override
    public int getUserStatus(String userid) throws UserDbAccessException {
        try {
            User user = findUser(userid);
            if (user == null) return STATUS_UNRECOGNIZED;
            return user.getStatus();
        }
        catch (IOException ex) {
            throw new UserDbAccessException("Failed to read user database " +
                                            "file: " + ex.getMessage(), ex);
        }
    }

    /**
     * return true if the user exists in the user database.  
     */
    public boolean isRecognized(String userid)
        throws UserDbAccessException
    {
        return isRecognized(getUserStatus(userid));
    }

    /**
     * return true if the user status value indicates that the user 
     * it applies to exists in the user database.  This returns true if
     * the status value is greater than or equal to STATUS_REGISTERED.  
     */
    public boolean isRecognized(int status) {
        return (status >= STATUS_REGISTERED);
    }

    /**
     * return the user attributes associated with a given user identifier
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    @Override
    public Map<String, ? extends Object> getUserAttributes(String userid)
        throws UserDbAccessException, UnrecognizedUserException 
    {
        try {
            User user = findUser(userid);
            if (user == null) 
                throw new UnrecognizedUserException(userid);
            return user.getAttributes();
        }
        catch (IOException ex) {
            throw new UserDbAccessException("Failed to read user database " +
                                            "file: " + ex.getMessage(), ex);
        }
    }

    /**
     * return the user authorizations as a list.  Each string in the
     * list is a name of some permission (or logical set of permissions)
     * afforded to the user that controls what the user has access to 
     * in the portal.  The supported names are implementation dependent.
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    @Override
    public Collection<String> getUserAuthorizations(String userid)
        throws UserDbAccessException, UnrecognizedUserException
    {
        try {
            User user = findUser(userid);
            if (user == null) 
                throw new UnrecognizedUserException(userid);
            return user.getAuthorizations();
        }
        catch (IOException ex) {
            throw new UserDbAccessException("Failed to read user database " +
                                            "file: " + ex.getMessage(), ex);
        }
    }

    /**
     * create a user database file in the format supported by this 
     * class and initialize it with a given set of user attributes.  
     * This function will not overwrite an existing file.  
     * @param dbFile    the user database file to create
     * @param attnames  a list of the attribute names
     * @param auths     a list of the possible authorization roles, can be null.
     */
    public static void createDB(File dbFile, List<String> attnames, 
                                Collection<String> auths) 
        throws IOException
    {
        if (dbFile.exists())
            throw new IOException("Database file already exists: " + dbFile);

        PrintWriter w = new PrintWriter(new FileWriter(dbFile));
        try {
            w.println("# User database written by SimpleFileUserDb");
            w.print(defMetaUsername);
            w.print(' ');
            w.print(defFieldSep);
            w.print(' ');

            StringBuilder s = null;
            if (auths != null && auths.size() > 0) {
                s = new StringBuilder();
                for (String name : auths)
                    s.append(name).append(defRoleSep);
                s.delete(s.length()-defRoleSep.length(), s.length());
                w.print(s.toString());
            }

            w.print(' ');
            w.print(defFieldSep);
            w.print(' ');

            if (attnames != null && attnames.size() > 0) {
                s = new StringBuilder();
                for (String name : attnames)
                    s.append(name).append(defAttrSep).append(' ');
                s.delete(s.length()-defAttrSep.length()-1, s.length());
                w.println(s.toString());
            }
        }
        finally {
            w.close();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Missing db filename argument");
            System.exit(1);
        }

        File dbfile = new File(args[0]);
        if (! dbfile.exists()) {
            ArrayList<String> attnames = new ArrayList<String>(args.length-1);
            for(int i=1; i < args.length; i++) 
                attnames.add(args[i]);
            SimpleFileUserDb.createDB(dbfile, attnames, null);
            System.out.println("Database " + dbfile + " created.");
            System.exit(0);
        }

        SimpleFileUserDb db = new SimpleFileUserDb(dbfile);
        if (args.length < 2) {
            System.out.println("Found readable database: " + dbfile);
            String[] attnames = db.getAttributeNames();
            System.out.print("User attributes available:");
            for(String name : attnames) {
                System.out.print(' ');
                System.out.print(name);
            }
            System.out.println();
            System.exit(0);
        }

        SimpleFileUserDb.User user = null;
        String[] atts = null;
        for(int i=1; i < args.length; i++) {
            user = db.findUser(args[i]);
            if (user == null) {
                System.out.print(args[i]);
                System.out.println(": not found");
            }
            else if (user.getStatus() < user.STATUS_ACTIVE) {
                System.out.print(user.getName());
                System.out.println(": disabled");
            }
            else {
                System.out.print(user.getName());
                System.out.print(": ");
                atts = user.getAttributeList();
                if (atts == null || atts.length < 1) {
                    System.out.println("(no attributes available)");
                }
                else {
                    System.out.println(atts[0]);
                }
            }
        }

        System.exit(0);
    }
}