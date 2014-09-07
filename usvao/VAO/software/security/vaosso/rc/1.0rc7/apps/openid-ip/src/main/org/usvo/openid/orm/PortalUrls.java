package org.usvo.openid.orm;

import org.usvo.openid.util.Compare;
import javax.persistence.*;
import java.sql.Date;
import java.net.URL;

/**
 * A persistable representation of a set of URL bases that are recognized as 
 * part of a portal.
 */
@Entity
public class PortalUrls implements HasId {

    public final static String PROP_HOSTNAME  = "hostname",
                               PROP_PATH      = "path",
                               PROP_PORTAL_ID = "portalId";

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false)
    private Long id = null;

    @ManyToOne
    @JoinColumn(name=PROP_PORTAL_ID)
    private Portal portal = null;
    @Column(insertable=false, updatable=false) private Long portalId = null;
    @Column private String hostname = null;
    @Column private String path = null;

    public PortalUrls() { }
    public PortalUrls(Portal portal, URL access) {
        setPortal(portal);
        hostname = access.getHost();
        path = access.getPath();
    }

    public Long getId() { return id; }

    public String getHostname() { return hostname; }
    public void setHostname(String name) { this.hostname = name; }

    public String getPath() { return path; }
    public void setPath(String name) { this.path = path; }

    public Long getPortalId() { return portalId; }
    public void setPortalId(Long id) { portalId = id; }
    public void setPortal(Portal p) {
        portal = p;
        portalId = (portal == null) ? null : portal.getId();
    }
    public Portal getPortal() { return portal; }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof PortalUrls)) return false;

        PortalUrls that = (PortalUrls) o;
        return Compare.equal(id, that.id) &&
               Compare.equal(hostname, that.hostname) && 
               Compare.equal(path, that.path);
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : 0; }
}
