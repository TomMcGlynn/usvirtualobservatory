package org.usvo.openid.orm;

import javax.persistence.*;
import java.util.*;

/** The old purse user.  Would be nice to replace this someday. */
@Entity(name="userTable")
public class NvoUser implements HasId {
    public static final String PROP_NAME = "name", PROP_EMAIL = "email", PROP_PHONE = "phone",
        PROP_USER_NAME = "userName", PROP_STATUS_ID = "statusId";

    @Id
    @GeneratedValue
    @Column(updatable=false, nullable=false, name="userId")
    private Long id = null;

    @Column private String lastName = null;
    @Column private String firstName = null;
    @Column private String email = null;
    @Column private String phone = null;
    @Column private String userName = null;
    @Column private String institution = null;
    @Column private String country = null;
    @Column private Date creationTime = null;
    @Column private Boolean isAdmin = null;

    @ManyToOne
    @JoinColumn(name=PROP_STATUS_ID)
    private UserStatus status;

    @OneToMany
    @JoinColumn(name="userTableId")
    private List<UserPreference> preferences;

    public Long getId() { return id; }

    public String getLastName() { return lastName; }
    public void setLastName(String name) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String name) { this.firstName = firstName; }

    public String getName() { return firstName + " " + lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    /** When this user's record was first created. */
    public Date getCreationTime() { return creationTime; }

    /** Registration status */
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public void setAdmin(boolean admin) { isAdmin = admin; }
    public boolean isAdmin() {
        // noinspection UnnecessaryUnboxing
        return isAdmin != null && isAdmin.booleanValue();
    }

    public List<UserPreference> getPreferences() { return preferences; }

    @Override
    public String toString() {
        String name = getName();
        return "NVO User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                (name == null ? "" : ", name='" + name + '\'') +
                ", status=" + (status == null ? "null" : status.getName() + " (" + status.getId() + ")") +
                (email == null ? "" : ", email='" + email + '\'') +
                (phone == null ? "" : ", phone='" + phone + '\'') +
                (institution == null ? "" : ", institution='" + institution + '\'') +
                (country == null ? "" : ", country='" + country + '\'') +
                '}';
    }
}
