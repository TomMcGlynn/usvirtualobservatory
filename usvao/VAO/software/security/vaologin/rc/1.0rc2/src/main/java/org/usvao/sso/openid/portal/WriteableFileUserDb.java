package org.usvao.sso.openid.portal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.util.Properties;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * a SimpleFileUserDb that allows new users to be added or existing users
 * to be updated.  
 */
public class WriteableFileUserDb extends SimpleFileUserDb {

    ArrayList<String> attnames = null;
    File dbfile = null;

    public WriteableFileUserDb(File dbFile) throws IOException {
        super(dbFile);
        dbfile = dbFile;

        // make sure we can write to the file
        if (! dbFile.canWrite())
            throw new IOException("user db file is not writable: " + dbFile);

        // load the attribute names we're expecting
        User user = findUser(metaUsername);
        if (user != null) {
            String[] attrs = user.getAttributeList();
            attnames = new ArrayList<String>(attrs.length);
            for(String name : attrs) 
                attnames.add(name);
        }
    }

    protected PrintWriter openToRewrite() throws IOException {
        return new PrintWriter(new FileWriter(dbfile, false));
    }

    protected PrintWriter openToAppend() throws IOException {
        return new PrintWriter(new FileWriter(dbfile, true));
    }

    protected String formatRecord(String user, Collection<String> roles, 
                                  List<String> attributes, 
                                  boolean disabled) 
    {
        if (user == null) 
            throw new IllegalArgumentException("formatRecord: need a usernaem");
        StringBuilder out = new StringBuilder(user);
        out.append(' ');

        if (disabled) out.append(commentChar);
        out.append(fieldSep).append(' ');

        // roles
        if (roles != null && roles.size() > 0) {
            for(String role : roles) 
                out.append(role).append(roleSep);
            out.delete(out.length()-roleSep.length(), out.length());
        }
        out.append(' ').append(fieldSep).append(' ');

        // attributes
        int i = 0;
        if (attributes != null) {
            for(; i < attributes.size() && i < attnames.size(); i++)
                out.append(attributes.get(i)).append(attrSep).append(' ');
            if (i > 0) {
                out.delete(out.length()-attrSep.length()-1, out.length());
                i--;
            }
        }
        for(i++; i < attnames.size(); i++)
            out.append(attrSep);

        return out.toString();
    }

    void cacheDatabase(List<String> lineContainer) throws IOException {
        if (lineContainer == null) 
            throw new NullPointerException("cacheDatabase: lineContainer");
        BufferedReader rdr = openURL(userdb);
        try {
            String line = rdr.readLine();
            while (line != null) {
                lineContainer.add(line);
                line = rdr.readLine();
            }
        }
        finally {
            rdr.close();
        }
    }

    boolean updateRecord(List<String> db, String user, String record) {
        String line = null;
        String[] flds = null;
        for(int i=0; i < db.size(); i++) {
            line = db.get(i);
            line = commentPart.matcher(line).replaceAll("").trim();
            if (line.length() == 0) continue;
            flds = fieldSepP.split(line, 3);

            if (flds.length > 0 && user.equals(flds[0])) {
                db.set(i, record);
                return false;
            }
        }

        db.add(record);
        return true;
    }

    void dumpDatabase(List<String> lineContainer) throws IOException {
        PrintWriter wtr = openToRewrite();
        for(String line : lineContainer) 
            wtr.println(line);
        wtr.close();
    }

    /**
     * register a user.  If the user has already been registered, the
     * user data will be updated with this new information; otherwise,
     * the user will just be added.  
     * @param userid         the username to register
     * @param status         the integer that represents the user's new 
     *                          status.  This implementation only supports 
     *                          two values:  STATUS_ACTIVE and STATUS_DISABLED.
     * @param authorizations the set of strings that represents the 
     *                          authorization roles granted to the user.
     * @param attributes     the set of attributes to associate with the 
     *                          user.  The implementation is allowed to 
     *                          ignore any attributes whose names it does 
     *                          not recognize.  
     * @return boolean  true if this userid was not previously registered
     */
    public synchronized boolean registerUser(String userid, int status, 
                                             Collection<String> authorizations,
                                             Properties attributes)
        throws RegistrationException
    {
        int initsz = (attributes == null) ? 0 : attributes.size();
        ArrayList<String> atts = new ArrayList<String>(initsz);

        if (attributes != null && attnames != null) {
            for(String name : attnames) 
                atts.add(attributes.getProperty(name, ""));
        }
        String record = formatRecord(userid, authorizations, atts, 
                                     status != STATUS_ACTIVE);

        try {
            User user = findUser(userid);
            if (user == null) {
                PrintWriter pw = openToAppend();
                pw.println(record);
                pw.close();
                return true;
            }
            else {
                // user already exists
                List<String> db = new LinkedList<String>();
                cacheDatabase(db);
                boolean added = updateRecord(db, userid, record);
                dumpDatabase(db);
                return added;
            }
        }
        catch (IOException ex) {
            throw new RegistrationException("Error updating database: " + 
                                            ex.getMessage(), ex);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Missing db filename argument");
            System.exit(1);
        }
        
        WriteableFileUserDb db = new WriteableFileUserDb(new File(args[0]));
        if (args.length < 2) {
            System.out.println("Found database: " + args[0]);
            String[] attnames = db.getAttributeNames();
            System.out.print("User attributes available:");
            for(String name : attnames) {
                System.out.print(' ');
                System.out.print(name);
            }
            System.out.println();
            System.exit(0);
        }

        if (args[1].equals(":")) {
            System.err.println("Missing user name before colon (:)");
            System.exit(1);
        }
        String userid = args[1];

        boolean disabled = false;
        HashSet<String> roles = new HashSet<String>();
        ArrayList<String> atts = new ArrayList<String>();
        int i = 2;

        if (args.length > i && args[i].equals("#")) {
            disabled = true;
            i++;
        }

        if (args.length > i) {
            if (args[i].equals(":")) i++;
            while (i < args.length && ! args[i].equals(":")) 
                roles.add(args[i++]);
        }
        if (args.length > i) {
            if (args[i].equals(":")) i++;
            while (i < args.length) 
                atts.add(args[i++]);
        }

        Properties attp = new Properties();
        if (atts.size() > 0) {
            String[] names = db.getAttributeNames();
            for(int j=0; j < names.length && j < atts.size(); j++) 
                attp.setProperty(names[j], atts.get(j));
        }

        try {
            boolean added = db.registerUser(userid, 
                                            (disabled) ? STATUS_DISABLED 
                                                       : STATUS_ACTIVE,
                                            roles, attp);
 
            System.out.print((added) ? "Added" : "Updated");
            System.out.print(" user ");
            System.out.println(userid);
            System.exit(0);
        }
        catch (RegistrationException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}