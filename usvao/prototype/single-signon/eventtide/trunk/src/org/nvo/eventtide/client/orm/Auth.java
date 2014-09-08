package org.nvo.eventtide.client.orm;

import javax.persistence.*;

@Entity
public class Auth extends OrmBase {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private Long id = null;

    @Version
    @Column(name = "version")
    private int version = 0;

    public Long getId() { return id; }
    public int getVersion() { return version; }
}
