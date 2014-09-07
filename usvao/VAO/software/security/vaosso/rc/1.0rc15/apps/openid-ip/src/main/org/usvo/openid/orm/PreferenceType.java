package org.usvo.openid.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/** A type of preference -- for example, "share email address" or "allow SSO". */
@Entity
public class PreferenceType implements HasId {
    public static final String PROP_NAME = "name", PROP_DESCRIP = "description";

    public static final String NAME_SSO_ENABLED = "Enable Single Signon";

    public static final String NAME_EMAIL_SHARED = "Share email address", NAME_NAME_SHARED = "Share name",
        NAME_USERNAME_SHARED = "Share username", NAME_PHONE_SHARED = "Share phone number",
        NAME_INSTITUTION_SHARED = "Share institution name",
        NAME_COUNTRY_SHARED = "Share country name",
        NAME_CREDENTIAL_DELEGATED = "Delegate credential";

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private Long id = null;

    @Column private String name = null;
    @Column private String description = null;

    public PreferenceType() {}
    public PreferenceType(String name) { setName(name); }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreferenceType)) return false;

        PreferenceType that = (PreferenceType) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "PreferenceType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
