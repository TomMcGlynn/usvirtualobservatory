package org.usvo.openid.orm;

import org.usvo.openid.util.Compare;

import javax.persistence.*;
import javax.servlet.http.Cookie;
import java.util.*;

/** An authenticated user's login session. */
@Entity
public class UserSession implements HasId {
    public static final String PROP_TOKEN = "token";

    /** How many seconds of slop do we allow between servers
     *  -- how forgiving are we of createTime and expireTime?
     *  That is, how many seconds in the future can createTime be and not raise any flags? */
    public static final int SLOP_SECONDS = 300;

    /** Default lifetime is two weeks. */
    public static final long DEFAULT_LIFETIME_SECONDS = 3600 * 24 * 7 * 2;

    /** The browser cookie associated with this session (not persisted). */
    private transient Cookie cookie;

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false)
    private Long id = null;

    @ManyToOne
    @JoinColumn(name="userTableId")
    private NvoUser user;

    @Column
    private String token;

    @Column
    private Date createTime, expireTime;

    @Column
    private String hostAddress;

    /** @deprecated For serialization only. */
    public UserSession() {}

    /** By default, create time is now and expire time is two weeks hence. */
    public UserSession(NvoUser user, String token) {
        this.user = user;
        this.token = token;
        createTime = new Date();
        expireTime = new Date(createTime.getTime() + 1000 * DEFAULT_LIFETIME_SECONDS);
    }

    public NvoUser getUser() { return user; }

    public Long getId() { return id; }

    public String getToken() { return token; }

    public Date getCreateTime() { return createTime; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }

    public Cookie getCookie() { return cookie; }
    public void setCookie(Cookie cookie) { this.cookie = cookie; }

    /** Expire this session and its cookie.  Doesn't save the session, however. */
    public void expire() {
        cookie.setMaxAge(0);
        if (isValid()) // expire 2 seconds ago, to ensure isValid() is false
            setExpireTime(new Date(System.currentTimeMillis() - 2000));
    }

    /** The address of the user's machine when the session was created (may change over time,
     *  for example as a laptop moves from network to network. */
    public String getHostAddress() { return hostAddress; } 
    public void setHostAddress(String hostAddress) { this.hostAddress = hostAddress; }

    /** Is this session currently valid? */
    public boolean isValid() { return isValid(SLOP_SECONDS, 0); }

    /** Is this session currently valid?
     *  @param futureCreateForgive allow the session to be created this many seconds in the future
     *  but still be valid (consider the case of redundancy, where the multiple servers are not in perfect time sync).
     *  @param futureExpireStrict will the session still be valid this many seconds in the future? */
    public boolean isValid(int futureCreateForgive, int futureExpireStrict) {
        long now = System.currentTimeMillis();
        return getCreateTime().getTime() -  futureCreateForgive*1000 < now // not created in the future
                && getExpireTime().getTime() + futureExpireStrict*1000 > now; // hasn't expired yet
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSession)) return false;

        UserSession that = (UserSession) o;

        return Compare.equal(id, that.id)
                && Compare.equal(createTime, that.createTime)
                && Compare.equal(expireTime, that.expireTime);
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", user=" + getUser().getUserName() +
                ", token='" + token + '\'' +
                ", created " + createTime +
                ", expires " + expireTime +
                '}';
    }
}
