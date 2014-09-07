package org.usvao.sso.ip.db;

/**
 * an exception indicating an error in the database configuration is 
 * preventing access to the user database.
 */
public class DatabaseConfigException extends UserDatabaseAccessException {

    /**
     * create the exception with a message.
     * @param msg    the message summarizing the problem
     */
    public DatabaseConfigException(String msg) {  super(msg);  }

    /**
     * wrap another exception that represents the underlying cause of 
     * the problem.  
     * @param msg    the message summarizing the problem
     * @param cause  a caught exception that represents the underlying 
     *                 cause of the problem.  
     */
    public DatabaseConfigException(String msg, Throwable cause) {  
        super(msg, cause);  
    }

}