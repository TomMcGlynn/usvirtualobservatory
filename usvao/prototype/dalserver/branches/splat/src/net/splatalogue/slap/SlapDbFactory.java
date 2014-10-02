package net.splatalogue.slap;

import dalserver.DalServerException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * a factory for creating a query interface into the Splatalogue Database
 */
public class SlapDbFactory {

    String baseUrl = null;
    String user = null;
    String pass = null;
    String defDb = null;

    /**
     * create a database connection factory.
     * @param jdbcDriver  the fully qualified name of the JDBC driver class 
     *                       to use to connect to one's database
     * @param dbBaseUrl   the base JDBC URL that should be used to connect to
     *                       the database.  The value must be terminated by 
     *                       necessary delimiter characters such that a full
     *                       JDBC URL can be obtained by simply concatonating
     *                       the database name.
     * @param defaultDb   the name of the default database to connect to.  If 
     *                       null, there is no default database, so the 
     *                       database name must be passed to the connect() 
     *                       function.  
     * @param username    User name to use to login to the database.  If null,
     *                       user authentication is not necessary
     * @param password    password to use to login to the database.  If null,
     *                       no password is required to authenticate
     */
    public SlapDbFactory(String jdbcDriver, String dbBaseUrl, String defaultDb, 
                         String username, String password)
        throws ClassNotFoundException
    {
        // this loads the JDBC Driver
        Class.forName(jdbcDriver);

        baseUrl = dbBaseUrl;
        defDb = defaultDb;
        user = username;
        pass = password;
    }

    /**
     * create a database connection factory.
     * @param jdbcDriver  the fully qualified name of the JDBC driver class 
     *                       to use to connect to one's database
     * @param dbBaseUrl   the base JDBC URL that should be used to connect to
     *                       the database.  The value must be terminated by 
     *                       necessary delimiter characters such that a full
     *                       JDBC URL can be obtained by simply concatonating
     *                       the database name.
     * @param username    User name to use to login to the database.  If null,
     *                       user authentication is not necessary
     * @param password    password to use to login to the database.  If null,
     *                       no password is required to authenticate
     */
    public SlapDbFactory(String jdbcDriver, String dbBaseUrl, 
                                   String username, String password)
        throws ClassNotFoundException
    {
        this(jdbcDriver, dbBaseUrl, null, username, password);
    }

    /**
     * create a database connection factory.
     * @param jdbcDriver  the fully qualified name of the JDBC driver class 
     *                       to use to connect to one's database
     * @param dbBaseUrl   the base JDBC URL that should be used to connect to
     *                       the database.  The value must be terminated by 
     *                       necessary delimiter characters such that a full
     *                       JDBC URL can be obtained by simply concatonating
     *                       the database name.
     */
    public SlapDbFactory(String jdbcDriver, String dbBaseUrl)
        throws ClassNotFoundException
    {
        this(jdbcDriver, dbBaseUrl, null, null, null);
    }

    /**
     * create a database connection factory.
     * @param jdbcDriver  the fully qualified name of the JDBC driver class 
     *                       to use to connect to one's database
     * @param url         JDBC URL of the database
     * @param defautlDb   the name of the default database to connect to.  If 
     *                       null, there is no default database, so the 
     *                       database name must be passed to the connect() 
     *                       function.  
     */
    public SlapDbFactory(String jdbcDriver, String dbBaseUrl, String defaultDb)
        throws ClassNotFoundException
    {
        this(jdbcDriver, dbBaseUrl, defaultDb, null, null);
    }

    /**
     * return a connection to the database that SLAP queries can be sent to.
     * @param database   the name of the database to connect to.  If null,
     *                      a default name is assumed.
     * @throws DalServerException     if a connection cannot be established.
     * @throws IllegalStateException  if database is null and this class was 
     *            not configured with a default database 
     */
    public SlapDb connect(String database) throws DalServerException {

        if (database == null) {
            if (defDb == null)
                throw new IllegalStateException("No default database configured");
            database = defDb;
        }

        try {
            Connection conn = DriverManager.getConnection(baseUrl+database,
                                                          user, pass);
            return new SlapDb(conn);
        } catch (SQLException ex) {
            throw new DalServerException(ex.getMessage());
        }
    }

    /**
     * return a connection to the default database that SLAP queries can be 
     * sent to.  
     * @throws DalServerException     if a connection cannot be established.
     * @throws IllegalStateException  if this class was not configured with 
     *            a default database
     */
    public SlapDb connect() throws DalServerException {
        return connect(null);
    }

}
