package net.myportal;

import org.usvao.sso.openid.portal.PortalUser;
import org.usvao.sso.openid.portal.WriteableFileUserDb;
import org.usvao.sso.openid.portal.RegistrationException;
import org.usvao.sso.openid.portal.UserDbAccessException;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;

/**
 * An example of a class for registering users.  The user database is a
 * just a flat file managed via the WriteableFileUserDb class.  
 */
public class MyUserRegistration {

    WriteableFileUserDb userdb = null;

    static Set<String> defaultAuths = null;
    static {
        defaultAuths = new HashSet<String>();
        defaultAuths.add(PortalUser.ROLE_OPENID_USER);
        defaultAuths.add(PortalUser.ROLE_VAO_USER);
    }

    public MyUserRegistration(File dbfile) 
        throws FileNotFoundException, IOException
    {
        userdb = new WriteableFileUserDb(dbfile);
    }

    /**
     * create the registration servce, pulling initialization parameters
     * from the servlet context.  
     * <p>
     * When instantiating from a JSP page, this would be the 
     * <code>application</code> implicit object, as in:
     * <pre>
     *    regservice = MyUserRegistration(application);
     * <pre>
     * for this to work, the full path to a writable database file must
     * be specified as a context parameter called "userDatabaseFile".  This
     * is done via the <code>&lt;context-param&gt;</code> section within the 
     * servlet's <code>web.xml</code> file, e.g:
     * <pre>
     * &lt;context-param&gt;
     *    &lt;param-name&gt;userDatabaseFile&lt;/param-name&gt;
     *    &lt;param-value&gt;/etc/myportal/conf/myuserdb.txt&lt;/param-value&gt;
     * &lt;/context-param&gt;
     * <pre>
     *
     */
    public MyUserRegistration(ServletContext context) 
        throws FileNotFoundException, IOException, ServletException
    {
        this(dbFileFromContext(context));
    }

    public static File dbFileFromContext(ServletContext context) 
        throws ServletException
    {
        String filename = context.getInitParameter("userDatabaseFile");
        if (filename == null) 
            throw new ServletException("userDatabaseFile context parameter " + 
                                       "is not set");
        return new File(filename);
    }

    public boolean isRegistered(String username) throws UserDbAccessException {
        return (userdb.getUserStatus(username) >= userdb.STATUS_REGISTERED);
    }

    public void registerUser(String username, Properties attributes) 
        throws RegistrationException
    {
        try {
            if (isRegistered(username)) 
                throw new RegistrationException("User is already registered",
                                                username);
        }
        catch (UserDbAccessException ex) {
            throw new RegistrationException("Failure accessing user database: "+
                                            ex.getMessage(), ex);
        }
        userdb.registerUser(username, userdb.STATUS_ACTIVE, defaultAuths,
                            attributes);
    }
}

