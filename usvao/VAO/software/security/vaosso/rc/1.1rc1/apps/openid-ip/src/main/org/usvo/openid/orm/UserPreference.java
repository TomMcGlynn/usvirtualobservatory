package org.usvo.openid.orm;

import org.usvo.openid.util.Compare;

import javax.persistence.*;

/** A single preference value for a user. */
@Entity
public class UserPreference implements HasId {
    public static final String PROP_PREF_TYPE_ID = "preferenceTypeId",
                               PROP_PORTAL_ID    = "portalId", 
                               PROP_USER_ID      = "userTableId";

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false)
    private Long id = null;

    @ManyToOne
    @JoinColumn(name=PROP_PREF_TYPE_ID)
    private PreferenceType type;

    @Column(name=PROP_PREF_TYPE_ID, insertable=false, updatable=false)
    private Long preferenceTypeId;

    @ManyToOne
    @JoinColumn(name=PROP_PORTAL_ID)
    private Portal portal;

    @Column(name=PROP_PORTAL_ID, insertable=false, updatable=false)
    private Long portalId;

    @ManyToOne
    @JoinColumn(name=PROP_USER_ID)
    private NvoUser user;

    @Column(name=PROP_USER_ID, insertable=false, updatable=false)
    private Long userTableId;

    @Column
    private String value;

    /** @deprecated For serialization only. */
    public UserPreference() {}

    public UserPreference(NvoUser user, Portal portal, PreferenceType type) {
        setUser(user);
        setPortal(portal);
        setType(type);
    }

    public Long getId() { return id; }

    public NvoUser getUser() { return user; }
    public Long getUserTableId() { return userTableId; }
    private void setUser(NvoUser user) {
        this.user = user;
        this.userTableId = user == null ? null : user.getId();
    }

    public PreferenceType getType() { return type; }
    public Long getTypeId() { return preferenceTypeId; }
    public void setType(PreferenceType type) {
        this.type = type;
        this.preferenceTypeId = type == null ? null : type.getId();
    }

    public Portal getPortal() { return portal; }
    public Long getPortalId() { return portalId; }
    public void setPortal(Portal portal) {
        this.portal = portal;
        this.portalId = portal == null ? null : portal.getId();
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public void setValue(boolean value) { this.value = value ? "true" : "false"; }

    /** Convenience method for boolean values. */
    public void setBooleanValue(String value) { setValue(isTrue(value)); }
    /** Convenience method for boolean values: Is this preference's value "true" or "yes"? */
    public boolean isTrue() { return isTrue(value); }
    public static boolean isTrue(String value) {
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPreference)) return false;

        UserPreference that = (UserPreference) o;

        return Compare.equal(id, that.id)
                && Compare.equal(portal, that.portal)
                && Compare.equal(type, that.type)
                && Compare.equal(value, that.value);
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "UserPreference{" +
                "id=" + id +
                ", type=" + (getType() == null ? "null" : "\"" + getType().getName() + "\"") +
                ", portal=" + (getPortal() == null ? "null" : "\"" + describePortal(getPortal()) + "\"") +
                ", value=" + (value == null ? "null" : "\"" + value + "\"") +
                '}';
    }

    private static String describePortal(Portal p) {
        return Compare.isBlank(p.getName())
                ? (Compare.isBlank(p.getDescription()) ? p.getUrl() : p.getDescription())
                : p.getName();
    }
}
