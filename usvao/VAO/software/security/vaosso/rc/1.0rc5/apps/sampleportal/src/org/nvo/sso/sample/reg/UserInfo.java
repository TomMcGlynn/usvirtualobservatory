package org.nvo.sso.sample.reg;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String NAME="name", EMAIL="email", INST = "inst", PHONE="phone",
            COLOR = "color", UNIQUE_ID = "uniqueId", CREATED="created", REGISTERED="registered";

    private String name, email, inst, phone, color;
    private String nvoUsername;
    private long uniqueId;
    private Date created, registered;

    public UserInfo() {
        uniqueId = Math.abs(new Random().nextLong());
        created = new Date();
    }

    public long getUniqueId() { return uniqueId; }
    public Date getCreated() { return created; }

    public void register(String nvoUsername) {
        registered = new Date();
        this.nvoUsername = nvoUsername;
    }
    public Date getRegistered() { return registered; }
    public boolean isRegistered() { return registered != null; }

    public String getNvoUsername() { return nvoUsername; }

    public String getRegistrationUrl(String urlBase, String returnBase, boolean includeReturnUrl) {
        String result = urlBase;
        Comma qm = new Comma("?", "&");
        if (name != null) result += qm + NAME + "=" + StringKit.urlEscape(name);
        if (email != null) result += qm + EMAIL + "=" + StringKit.urlEscape(email);
        if (inst != null) result += qm + INST + "=" + StringKit.urlEscape(inst);
        if (phone != null) result += qm + PHONE + "=" + StringKit.urlEscape(phone);
        if (includeReturnUrl)
            result += qm + "returnURL=" + StringKit.urlEscape(returnBase + "?" + UNIQUE_ID + "=" + uniqueId)
                    + "&portalName=Sample+Registration";
        return result;
    }

    public String getRegistrationUrl(ServletConfig config, HttpServletRequest request, boolean includeReturnUrl) {
        String returnBase = request.getRequestURL().toString();
        returnBase = returnBase.substring(0, returnBase.lastIndexOf("/")) + "/return.jsp";
        return getRegistrationUrl(Config.getRegUrl(config), returnBase, includeReturnUrl);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getInst() { return inst; }
    public void setInst(String inst) { this.inst = inst; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String toString() {
        Comma comma = new Comma(" - ");
        String result = "";
        if (name != null) result += comma + name;
        if (email != null) result += comma + email;
        if (inst != null) result += comma + inst;
        if (phone != null) result += comma + phone;
        if (color != null) result += comma + "favorite color: " + color;
        return result;
    }
}
