package net.myportal;

import org.usvao.sso.openid.portal.spring.SSFUserDatabase;
import org.usvao.sso.openid.portal.SimpleFileUserDb;
import org.usvao.sso.openid.portal.UserDbAccessException;
import org.usvao.sso.openid.portal.UnrecognizedUserException;
import org.usvao.sso.openid.portal.PortalUser;

import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * an adapter to a local user database that can plug into spring security
 * via the PortalUserDetails class.  
 */
public class MyUserDatabase implements SSFUserDatabase, InitializingBean {

    SimpleFileUserDb userdb = null;

    /**
     * the default constructor used by the spring bean infrastructure
     */
    public MyUserDatabase() { }

    public MyUserDatabase(String dbfile) 
        throws FileNotFoundException, IOException
    {
        setUserDatabaseFile(dbfile);
    }

    public void setUserDatabaseFile(String dbfilename) {
        File dbfile = new File(dbfilename);
        if (! dbfile.exists())
            throw new IllegalArgumentException("Can't located User DB file: "+
                                               dbfile);
        try {
            userdb = new SimpleFileUserDb(dbfile);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkDbSet();
    }

    void checkDbSet() throws UserDbAccessException {
        if (userdb == null) 
            throw new UserDbAccessException("User database file not set");
    }

    /**
     * return the current registration status of a user
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    @Override 
    public int getUserStatus(String userid)
        throws UserDbAccessException
    {
        checkDbSet();
        return userdb.getUserStatus(userid);
    }

    /**
     * return true if the user exists in the user database.  
     */
    @Override 
    public boolean isRecognized(String userid)
        throws UserDbAccessException
    {
        checkDbSet();
        return userdb.isRecognized(userid);
    }


    /**
     * return true if the user status value indicates that the user 
     * it applies to exists in the user database.  This method can be 
     * necessary in generic code because the user database is free to 
     * use its own convention for status values.  
     */
    public boolean isRecognized(int status) {
        return (status >= SimpleFileUserDb.STATUS_REGISTERED);
    }

    /**
     * return the user attributes associated with a given user identifier
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    public Map<String, ? extends Object> getUserAttributes(String userid)
        throws UserDbAccessException, UnrecognizedUserException
    {
        checkDbSet();
        return userdb.getUserAttributes(userid);
    }

    /**
     * return the user authorizations as a list.  Each string in the
     * list is a name of some permission (or logical set of permissions)
     * afforded to the user that controls what the user has access to 
     * in the portal.  The supported names are implementation dependent.
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    public Collection<String> getUserAuthorizations(String userid)
        throws UserDbAccessException, UnrecognizedUserException
    {
        checkDbSet();
        Collection<String> out = userdb.getUserAuthorizations(userid);
        if (! out.contains(PortalUser.ROLE_REGISTERED_USER))
            out.add(PortalUser.ROLE_REGISTERED_USER);
        return out;
    }

    
    public boolean statusMeansAccountNonExpired(int status) {
        return (status > SimpleFileUserDb.STATUS_EXPIRED);
    }
    public boolean statusMeansAccountNonLocked(int status) {
        return (status > SimpleFileUserDb.STATUS_PENDING);
    }
    public boolean statusMeansEnabled(int status) {
        return (status >= SimpleFileUserDb.STATUS_ACTIVE);
    }

}
