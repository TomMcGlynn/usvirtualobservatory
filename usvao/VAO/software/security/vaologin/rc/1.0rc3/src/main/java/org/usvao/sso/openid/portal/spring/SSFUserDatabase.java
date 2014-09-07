package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.UserDatabase;

/**
 * an interface for connecting a UserDatabase into the Spring Security 
 * Framework.  Adding to the VAO Login's user database acces interface,
 * these methods interpret a user status value in the terms needed by 
 * the spring UserDetails interface.  
 * @see org.usvao.sso.openid.portal.UserDatabase
 * see org.springframework.security.core.userdetails.UserDetails
 */
public interface SSFUserDatabase extends UserDatabase {

    public boolean statusMeansAccountNonExpired(int status);
    public boolean statusMeansAccountNonLocked(int status);
    public boolean statusMeansEnabled(int status);
}