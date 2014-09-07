package org.usvao.sso.openid.portal;

import java.security.BasicPermission;

/**
 * a portal role-base authorization expressed as a java Permission subclass.
 * <p>
 * Permissions are provided by the {@link PortalUser} class as a hook for 
 * providing more complex authorizations/permissions beyond named roles.  
 * This class integrates named roles into the Java Permissions security 
 * framework.
 * <p>
 */
public class RolePermission extends BasicPermission {

    /**
     * a permission corresponding to the PortalUser.ROLE_OPENID_USER.  The
     * name is "user.openid.any".
     */
    public final static RolePermission OPENID_USER = 
        new RolePermission("user.openid.any");

    /**
     * a permission corresponding to the PortalUser.ROLE_OPENID_USER.  The
     * name is "user.openid.vao"
     */
    public final static RolePermission VAO_USER = 
        new RolePermission("user.openid.vao");

    /**
     * a permission corresponding to the PortalUser.ROLE_REGISTERED_USER.
     * The name is "user.registered".
     */
    public final static RolePermission REGISTERED_USER = 
        new RolePermission("user.registered");

    public RolePermission(String name) {
        super(name);
    }
}

