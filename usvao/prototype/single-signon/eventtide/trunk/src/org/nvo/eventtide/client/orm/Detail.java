package org.nvo.eventtide.client.orm;

import javax.persistence.*;

@Entity
public class Detail extends OrmBase {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private Long id = null;

    @Version
    @Column(name = "version")
    private int version = 0;

    @Column
    private String name = null;
    @Column
    private String value = null;

    @ManyToOne
    private Step step;

    public Detail() { }

    public Detail(String name, String value) { this.name = name; this.value = value; }

    /** This detail's type -- "portal", "username", "serial number", "email", etc. */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** This detail's value -- "nvo.noao.edu", "someuser", "12345", etc. */
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Long getId() { return id; }
    public int getVersion() { return version; }

    public Step getStep() { return step; }

    public void initStep(Step step) {
        if (this.step == null) this.step = step;
        else throw new IllegalStateException("Already initialized.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Detail)) return false;

        Detail detail = (Detail) o;

        if (version != detail.version) return false;
        if (id != null ? !id.equals(detail.id) : detail.id != null) return false;
        if (name != null ? !name.equals(detail.name) : detail.name != null) return false;
        if (step != null ? !step.equals(detail.step) : detail.step != null) return false;
        //noinspection RedundantIfStatement
        if (value != null ? !value.equals(detail.value) : detail.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + version;
        return result;
    }

    @Override
    public String toString() {
        return "Detail{" +
                "id=" + id + ", version=" + version +
                ", name='" + name + '\'' + ", value='" + value + '\'' +
                ", step=" + step.getId() + // prevent recursion
                '}';
    }
}
