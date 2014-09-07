package org.usvo.openid.orm;

import org.usvo.openid.util.Compare;

import javax.persistence.*;
import java.sql.Date;

/** A portal that uses VAO SSO. */
@Entity
public class Portal implements HasId {
    public static final String PROP_NAME = "name", PROP_DESCRIPTION = "description", PROP_URL = "url",
        PROP_APPROVER_ID = "approverId", PROP_ACTIVE = "active";

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false)
    private Long id = null;

    @Column private String name = null;
    @Column private String description = null;
    @Column private String url = null;
    @Column private Boolean active = null;

    @ManyToOne
    @JoinColumn(name=PROP_APPROVER_ID)
    private NvoUser approver;
    @Column(insertable=false, updatable=false)
    private Long approverId;
    @Column private Date dateApproved = null;

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    /** Is this portal currently considered active? Only for tracking purposes -- doesn't change behavior. */
    public boolean isActive() { return active == null ? false : active; }
    public void setActive(Boolean active) { this.active = active; }

    /** The administrator who approved this portal for SSO. */
    public NvoUser getApprover() { return approver; }
    public Long getApproverId() { return approverId; }
    public void setApprover(NvoUser approver) {
        if (approver != null && this.approver != null) throw new IllegalStateException
                ("Already approved by " + getApprover().getUserName() + " on " + getDateApproved() + ".");
        this.approver = approver;
        this.approverId = approver == null ? null : approver.getId();
        this.dateApproved = approver == null ? null : new Date(System.currentTimeMillis());
    }

    /** Has this portal been approved by an SSO administrator? */
    public boolean isApproved() { return getApproverId() != null; }

    /** When was this portal approved by an admin? Null if not approved. */
    public Date getDateApproved() { return dateApproved; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Portal)) return false;

        Portal that = (Portal) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) return false;
        if (isActive() != that.isActive()) return false;
        if (Compare.differ(getApproverId(), that.getApproverId())) return false;
        if (Compare.differ(getDateApproved(), that.getDateApproved())) return false;

        return true;
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "Portal{" +
                "id=" + id +
                (name == null ? "" : ", name='" + name + '\'') +
                (description == null ? "" : ", description='" + description + '\'') +
                (url == null ? "" : ", url='" + url + '\'') +
                '}';
    }
}
