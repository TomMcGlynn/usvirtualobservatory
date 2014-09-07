package org.usvao.sso.ip.db;

import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.databaseAccess.DatabaseConstants;
import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.databaseAccess.DatabaseManager;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.UserRegistrationException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * convert NVO style accounts to vaosso accounts by adding salt
 */
public class SaltNVOAccountsApp {

    Properties dbprops = new Properties();
    String dburl = null;
    Log log = LogFactory.getLog(getClass());

    public SaltNVOAccountsApp(File propsFile, String database) 
        throws ConfigException 
    {
        loadProps(propsFile);
        setDBURL(database);

        String dbpropfile = getProp("dbPropFile");

        if (dbpropfile.contains("${purse.dir}")) {
            String purseDir = getProp("purse.dir");
            dbpropfile = dbpropfile.replaceAll("\\$\\{purse.dir\\}", purseDir);
        }
        try {
            int hi =  Integer.parseInt(getProp("hashIterations"));
            DatabaseOptions dbopts = 
                new DatabaseOptions(getProp("dbDriver"), dburl, 
                                    getProp("dbUsername"), 
                                    getProp("dbPassword"), dbpropfile, hi);
            DatabaseManager.initialize(dbopts);
        }
        catch (DatabaseAccessException ex) {
            throw new ConfigException(ex.getMessage(), ex);
        }
        catch (UserRegistrationException ex) {
            throw new ConfigException(ex.getMessage(), ex);
        }
        log.info("Database connection configured");
    }

    public static class ConfigException extends Exception {
        public ConfigException(String msg) { super(msg); }
        public ConfigException(String msg, Throwable t) { super(msg, t); }
        public ConfigException(Throwable t) { super(t); }
    }
    public static class DBException extends Exception {
        public DBException(String msg) { super(msg); }
        public DBException(String msg, Throwable t) { super(msg, t); }
        public DBException(Throwable t) { super(t); }
    }

    String getProp(String propname) throws ConfigException {
        String out = System.getProperty(propname,dbprops.getProperty(propname));
        if (out == null) 
            throw new ConfigException(propname + " property not set");
        return out;
    }

    void setDBURL(String database) throws ConfigException {
        dburl = getProp("db.connection.url");
        if (database != null) {
            int p = dburl.lastIndexOf("/");
            if (p < 11)
                throw new ConfigException("db.connection.url property looks bad: "+dburl);
            dburl = dburl.substring(0,p+1) + database;
        }
        log.info("Will update database via " + dburl);
    }

    void loadProps(File propsFile) throws ConfigException {
        try {
            dbprops.load(new FileReader(propsFile));
        } catch (IOException ex) {
            throw new ConfigException(propsFile.toString() + ": " + 
                                      ex.getMessage(), ex);
        }
    }

    /**
     * submit the query that selects the records needing salt
     * and count the number of results.
     */
    public int countOldRecords() throws DBException {
        try {
            ResultSet resultSet = submit(makeQuery());
            if (resultSet == null) 
                throw new DBException("Null result returned from submit()");

            int n = 0;
            while (resultSet.next()) 
                n++;

            return n;
        } catch (SQLException ex) {
            throw new DBException(ex);
        } catch (DatabaseAccessException ex) {
            throw new DBException(ex);
        }
    }

    public int convertPasswords() 
        throws DBException, DatabaseAccessException, SQLException 
    {
        ResultSet resultSet = submit(makeQuery());
        if (resultSet == null) 
          throw new IllegalStateException("Null result returned from submit()");

        int n = 0;
        String userName = null, oldPwSha = null, passwordMethod = null;
        while (resultSet.next()) {
            userName =
                resultSet.getString(DatabaseConstants.USER_COL_USER_NAME);
            oldPwSha =
                resultSet.getString(DatabaseConstants.USER_COL_PASSWORD_SHA);
            passwordMethod  =
                resultSet.getString(DatabaseConstants.USER_COL_PASSWORD_METHOD);

            if (passwordMethod == null || passwordMethod.equals("")) {
                UserDataHandler.updateUserPassword(userName, oldPwSha);
                n++;
            }
        }

        return n;
    }

    /**
     * Test the conversion for a username and password.  The username
     * must be one that has not yet been converted.  The database will
     * not be updated.  
     */
    public boolean testConversionFor(String userName, String passwd) 
        throws DBException, DatabaseAccessException, SQLException
    {
        String[] hashNSalt1 = getUpdatedHashAndSalt(userName);
        String[] hashNSalt2 =UserDataHandler.passwordSha(passwd, hashNSalt1[1]);
        return hashNSalt1[0].equals(hashNSalt2[0]);
    }

    String[] getUpdatedHashAndSalt(String userName) 
        throws DBException, DatabaseAccessException, SQLException 
    {
        StringBuilder query = new StringBuilder(makeQuery());
        query.append(" and ");
        query.append(DatabaseConstants.USER_COL_USER_NAME).append("='");
        query.append(userName).append("'");

        ResultSet resultSet = submit(query.toString());
        if (resultSet == null) 
          throw new DBException("Null result returned from submit()");

        if (! resultSet.next())
            throw new DBException("Old style password not found for user "+
                                  userName);

        String oldPwSha = null, passwordMethod = null;
        oldPwSha =
            resultSet.getString(DatabaseConstants.USER_COL_PASSWORD_SHA);
        passwordMethod  =
            resultSet.getString(DatabaseConstants.USER_COL_PASSWORD_METHOD);

        if (passwordMethod != null && ! passwordMethod.equals("")) 
            throw new DBException("User's record state does not match query");

        try {
            byte[] decodedSha = Hex.decodeHex(oldPwSha.toCharArray());
            return UserDataHandler.passwordSha(decodedSha);
        } catch (DecoderException ex) {
            throw new DBException("Hash decoder exception for user " +
                                  userName + " (" + ex.getMessage() + ")", ex);
        }
    }

    /**
     * create the SQL query that selects the records needing salt.
     */
    public String makeQuery() {
        StringBuilder query = new StringBuilder("select ");
        query.append(DatabaseConstants.USER_COL_USER_NAME).append(", ");
        query.append(DatabaseConstants.USER_COL_PASSWORD_SHA).append(", ");
        query.append(DatabaseConstants.USER_COL_PASSWORD_METHOD);
        query.append(" from ").append(DatabaseConstants.USER_TABLE_NAME);
        query.append(" where ");
        query.append(DatabaseConstants.USER_COL_PASSWORD_METHOD);
        query.append(" is null or ");
        query.append(DatabaseConstants.USER_COL_PASSWORD_METHOD);
        query.append("=''");
        return query.toString();
    }

    ResultSet submit(String sql) throws DatabaseAccessException, SQLException {
        Connection connection = DatabaseManager.getDBConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    static void fail(int code, String msg) {
        System.err.println(msg);
        System.exit(code);
    }

    static void usageFailure(String msg) {
        String usage = "\nArguments: propfile database count|convert|test [username]";
        fail(1, msg+usage);
    }

    public static void main(String[] args) {
        if (args.length < 3) usageFailure("Missing argument(s)");

        File propfile = new File(args[0]);
        if (! propfile.exists())
            fail(1, "Properties file not found: "+args[0]);
        String database = args[1];
        String action = args[2];

        try {
            SaltNVOAccountsApp app = new SaltNVOAccountsApp(propfile, database);

            if ("count".equals(action)) {
                int nr = app.countOldRecords();
                System.out.println("Found " + Integer.toString(nr) + 
                                   " records needing salt.");
            }
            else if ("convert".equals(action)) {
                app.convertPasswords();
            }
            else if ("test".equals(action)) {
                if (args.length < 4) usageFailure("Missing username argument");
                String user = args[3];
                String password = null;
                if (args.length > 4) {
                    password = args[4];
                } else {
                    byte[] input = new byte[80];
                
                    System.out.print("Password: ");
                    System.out.flush();
                    int nb = System.in.read(input);
                    System.out.println();
                    password = (new String(input, 0, nb)).trim();
                }

                if (! app.testConversionFor(user, password)) 
                    fail(2, "Conversion failed.");
                System.out.println("Conversion validated");
            }

        }
        catch (RuntimeException ex) {
            ex.printStackTrace();
            fail(3, ex.getMessage());
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            fail(4, ex.getMessage());
        }
        catch (DatabaseAccessException ex) {
            ex.printStackTrace();
            fail(4, ex.getMessage());
        }
        catch (Exception ex) {
            fail(2, ex.getMessage());
        }

    }
}
