package org.usvao.sso.client;

import java.io.*;
import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.openid.*;

public class VAOUserDetailsService implements UserDetailsService {
    private String acceptableOpenIDProviders;
    private String[] openIDProviders;
    private String roleFile;

    private static final String VAO_OP = "https://testsso.ncsa.illinois.edu/openid/id/";

    public void setAcceptableOpenIDProviders(String acceptableOpenIDProviders) {
        this.acceptableOpenIDProviders = acceptableOpenIDProviders;
        this.openIDProviders = acceptableOpenIDProviders.split(" ");
    }

    public void setRoleFile(String roleFile) {
        this.roleFile = roleFile;
    }

    public String rolesFromFile(String username) {
        if (roleFile == null)
            return null;

        String roles = null;

        try {
            Scanner in = new Scanner(new File(roleFile));
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.indexOf(username) == 0) {
                    // Strip leading and ending whitespace
                    roles = line.substring(username.length()).trim();
                    if (roles.indexOf("ROLE_") == 0)
                        break;
                    else
                        roles = null;
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            /* ignore */
        } catch (Exception e) {
            e.printStackTrace();
        }

        return roles;
    }

    public UserDetails loadUserByUsername(String username) {

System.out.println("IN USERDETAILS");
        boolean opAcceptable = false;
        for (String op: openIDProviders)
        {
            if (op.equalsIgnoreCase("ANY") ||
                (op.equalsIgnoreCase("VAO") && username.startsWith(VAO_OP)) ||
                username.startsWith(op))
            {
                opAcceptable = true;
                break;
            }
        }

        if (opAcceptable == true)
        {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

            authorities.add(new GrantedAuthorityImpl("ROLE_OPENID_USER"));
            if (username.startsWith(VAO_OP))
                authorities.add(new GrantedAuthorityImpl("ROLE_VAO_USER"));

            String roles = rolesFromFile(username);
            if (roles != null)
            {
System.out.println("ROLES FROM FILE: " + roles);
                String[] roleArr = roles.split(",");
                for (String i:roleArr)
                    authorities.add(new GrantedAuthorityImpl(i));
            }

            User user = new User(username, "[PROTECTED]", true, true, true, true, authorities);

            return user;
        } else
            throw new UsernameNotFoundException(username +
                    " is not from an acceptable OpenID Provider");
    }
}
