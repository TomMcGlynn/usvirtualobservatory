package org.usvao.sso.ip.db;

import org.usvao.sso.ip.User;

import java.util.Properties;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * a class for interacting with the user database.  This class is abstract,
 * allowing for different implementations, but comes with its own factory 
 * method, {@link connect(Properties)}.
 */
public abstract class UserDatabase {

    /**
     * the database configuration properties.  The property names expected
     * is dependent on the UserDatabase implementation.
     */
    protected Properties config = null;

    static String purseClass = "org.usvao.sso.ip.db.PurseUserDatabase";

    /**
     * connect to the UserDatabase via the implementation appropriate
     * for the given database configuration properties.
     */
    public static UserDatabase connect(Properties props) 
        throws DatabaseConfigException
    {
        String clsname = props.getProperty("userdb.class");
        if (clsname == null) {
            if (props.getProperty("dbPropFile") != null) 
                clsname = purseClass;
        }
        if (clsname == null) 
            throw new DatabaseConfigException("Unable to determine UserDatabase"
                                              + " implementation class");

        Class udclass = null;
        try {
            udclass = Class.forName(clsname);

            Constructor ctor = udclass.getConstructor(Properties.class);
            return (UserDatabase) ctor.newInstance(props);
        }
        catch (ClassNotFoundException ex) {
            // Class matching configured name not found
            throw new DatabaseConfigException("UserDatabase class not found: "+
                                              udclass, ex);
        }
        catch (NoSuchMethodException ex) {
            // named UserDatabase class does not have a T(Properties) ctor
            throw new DatabaseConfigException("Not a compliant UserDatabase " +
                                              "class: "+ udclass, ex);
        }
        catch (InstantiationException ex) {
            // matching constructor not callable for some reason 
            // (shouldn't happen) 
            throw new DatabaseConfigException("Not a compliant UserDatabase " +
                                              "class: "+ udclass, ex);
        }
        catch (IllegalAccessException ex) {
            // matching constructor not public (shouldn't happen) 
            throw new DatabaseConfigException("Not a compliant UserDatabase " +
                                              "class: "+ udclass, ex);
        }
        catch (InvocationTargetException ex) {
            // constructor threw an exception
            throw new DatabaseConfigException("Trouble instantiating " +
                                              "UserDatabase class: " + udclass, 
                                              ex.getCause());
        }
        catch (ClassCastException ex) {
            // configured class is not a subclass of UserDatabase
            throw new DatabaseConfigException("Configured class not a "+
                                              "UserDatabase: " + udclass, ex);
        }
    }

    /**
     * initialize the configuration properties
     */
    protected UserDatabase(Properties configProps) {
        config = configProps;
    }

    /**
     * return true if a user with a given username exists
     */
    public abstract boolean exists(String username) 
        throws UserDatabaseAccessException;

    /**
     * return true if a user with a given username exists and has an
     * accepted status
     */
    public abstract boolean isAccepted(String username) 
        throws UserDatabaseAccessException;

    /**
     * add a user to the database with a given username and status.
     * The username must not already be in use.
     * @param username   the user's username
     * @param userdata   the user's attributes
     * @param status     the user's initial status. 
     */
    public abstract void addUser(String username, Properties userdata, 
                                 User.Status status)
        throws UserDatabaseAccessException;

    /**
     * return a User from the database with a given username.  Null is 
     * returned if the username does not exist in the database.
     */
    public abstract User getUser(String username)
        throws UserDatabaseAccessException;
}