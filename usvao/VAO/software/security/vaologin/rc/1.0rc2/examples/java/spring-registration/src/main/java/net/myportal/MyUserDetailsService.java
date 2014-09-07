package net.myportal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.usvao.sso.openid.portal.SimpleFileUserDb;
import org.usvao.sso.openid.portal.UserDbAccessException;
import org.usvao.sso.openid.portal.UserDbAccessException;
import org.usvao.sso.openid.portal.UnrecognizedUserException;

import java.io.IOException;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * An example UserDetailsService that connects to a local user database
 */
public class MyUserDetailsService implements UserDetailsService {

    SimpleFileUserDb userdb = null;

    public MyUserDetailsService() {
        try {
            userdb = new SimpleFileUserDb("myuserdb.txt");
        } catch (IOException ex) {
            // we won't bother with a user db if it can't be found/opened
            // Spring can add one later.
        }
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

    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException
    {
        Collection<String> roles = null;
        if (userdb != null) {
            try {
                roles = userdb.getUserAuthorizations(username);
            }
            catch (Exception ex) {
                // aw, forget it.
            }
        }

        if (roles == null) {
            // user not found in db; set some default authorizations
            roles = new HashSet<String>(2);
            roles.add("ROLE_OPENID_USER");
            if (username.endsWith("@usvao"))
                roles.add("ROLE_VAO_USER");
        }

        HashSet<SimpleGrantedAuthority> authz = 
            new HashSet<SimpleGrantedAuthority>(roles.size());
        for(String role : roles) 
            authz.add(new SimpleGrantedAuthority(role));

        return new User(username, "", authz);
    }

}