package org.usvo.openid.orm;

import org.usvo.openid.util.Compare;

import javax.persistence.*;
import java.sql.Date;

/** 
 *  A persistable representation of a portal visited by VAOSSO users.
 *  Some portals will be recognized with a special name.  
 */
@Entity
public class Portal implements HasId {
    public static final String PROP_NAME = "name", 
                               PROP_DESCRIPTION = "description", 
                               PROP_URL = "url",
                               PROP_CURATOR_ID = "curatorId", 
                               PROP_STATUS = "status";

    public static final int STATUS_UNKNOWN = 0,
                            STATUS_APPROVED = 1,
                            STATUS_UNAPPROVED = 2,
                            STATUS_UNSUPPORTED = 3;

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false)
    private Long id = null;

    /*
     * the short, canonical name for the portal.  This will be simply a hostname
     * for "unrecognized" portals.
     */
    @Column private String name = null;
    @Column private String description = null;
    @Column private String url = null;
    @Column private int status = 0;

    @ManyToOne
    @JoinColumn(name=PROP_CURATOR_ID)
    private NvoUser curator;
    @Column(insertable=false, updatable=false)
    private Long curatorId;

    public Portal() { }
    public Portal(String name) { setName(name); }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }


    public NvoUser getCurator() { return curator; }
    public Long getCuratorId() { return curatorId; }
    public void setCurator(NvoUser curator) {
        this.curator = curator;
        this.curatorId = curator == null ? null : curator.getId();
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    /** 
     * return true if the portal is not recognized by the SSO curators as a 
     * collaborating portal.  
     */
    public boolean isUnknown() { return getStatus() == STATUS_UNKNOWN; }

    /** 
     * return true if the portal is recognized by the SSO curators as a 
     * collaborating portal.  
     */
    public boolean isApproved() { return getStatus() == STATUS_APPROVED; }

    /**
     * return true if this portal is recognized as explicitly unsupported.
     * An portal may be declared unsupported if it is identified as a 
     * malicious or otherwise harmful.
     */
    public boolean isUnsupported() { return getStatus() == STATUS_UNSUPPORTED; }

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
        if (getStatus() != that.getStatus()) return false;
        if (Compare.differ(getCuratorId(), that.getCuratorId())) return false;

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
