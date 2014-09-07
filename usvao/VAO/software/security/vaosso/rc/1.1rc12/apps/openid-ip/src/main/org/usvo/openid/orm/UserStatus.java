package org.usvo.openid.orm;

import org.usvo.openid.util.Compare;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name="statusTable")
public class UserStatus implements HasId {
    /** Currently only requested and accepted are used, since we don't have RAs. */
    public static final String STATUS_REQUESTED = "requested", STATUS_PENDING = "pending",
        STATUS_ACCEPTED = "accepted", STATUS_REJECTED = "rejected", STATUS_RENEWAL = "renewal";

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false, name="statusId")
    private Long id;

    @Column(name="statusName") private String name;
    @Column(name="statusDescription") private String description;

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserStatus)) return false;

        UserStatus that = (UserStatus) o;

        return Compare.equal(id, that.id)
                && Compare.equal(description, that.description)
                && Compare.equal(name, that.name);
    }

    @Override public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "UserStatus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
