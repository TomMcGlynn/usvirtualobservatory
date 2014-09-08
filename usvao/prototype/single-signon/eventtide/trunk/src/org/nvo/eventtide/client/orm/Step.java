package org.nvo.eventtide.client.orm;

import javax.persistence.*;
import java.util.*;

/** Not thread-safe. */
@SuppressWarnings({"UnusedDeclaration"})
@Entity
public class Step extends OrmBase {
    public static final String VAL_HTTP = "http remote host", VAL_CERT = "cert";

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private Long id = null;

    @Version
    @Column
    private int version = 0;

    @Column(updatable = false, nullable = false)
    private Date created = null;

    @OneToMany(/*targetEntity=org.nvo.eventtide.client.orm.Detail.class, */mappedBy = "step")
    private List<Detail> details;

    @Column
    private String activity = null;
    @Column
    private String phase = null;
    @Column
    private String source = null;
    @Column
    private String sourceValidation = null;

    public Step() {}

    public Step(String activity, String phase, String source, String sourceValidation) {
        this.activity = activity;
        this.phase = phase;
        this.source = source;
        this.sourceValidation = sourceValidation;
    }

    public Long getId() { return id; }
    public int getVersion() { return version; }

    public Date getCreated() { return created; }
    public void initCreated() {
        if (created == null) created = new Date();
        else throw new IllegalStateException("Already inited.");
    }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    /** How was source determined?  For example, {@link #VAL_CERT}, {@link #VAL_HTTP}. */
    public String getSourceValidation() { return sourceValidation; }
    public void setSourceValidation(String sourceValidation) { this.sourceValidation = sourceValidation; }

    /** Immutable snapshot.  Will never be null. */
    private transient List<Detail> detailsCache = null;
    public List<Detail> getDetails() {
        if (detailsCache != null) return detailsCache;
        else {
            List<Detail> result = (details == null ? Collections.unmodifiableList(new ArrayList<Detail>())
                    : Collections.unmodifiableList(details));
            detailsCache = result;
            return result;
        }
    }

    /** Save a copy of contents of <tt>details</tt>. */
    public void setDetails(List<Detail> details) {
        detailsCache = null;
        if (details == null) this.details = null;
        else {
            if (this.details == null) this.details = new ArrayList<Detail>();
            this.details.clear();
            this.details.addAll(details);
        }
    }
    public void addDetail(Detail detail) {
        if (detail.getStep() != null && !detail.getStep().isSimilar(this))
            throw new IllegalStateException("Detail (" + detail + ") already part of a different step (" + detail.getStep() + ").");
        detailsCache = null;
        if (detail.getStep() == null) detail.initStep(this);
        if (this.details == null) this.details = new ArrayList<Detail>();
        details.add(detail);
    }

    @Override
    public String toString() {
        return "Step{" +
                "id=" + id + ", version=" + version + ", created=" + created +
                ", activity='" + activity + '\'' + ", phase='" + phase + '\'' + ", source='" + source + '\'' +
                ", details=" + details +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Step)) return false; // accept subclass because we're using Hibernate

        Step step = (Step) o;

        if (version != step.version) return false;
        if (activity != null ? !activity.equals(step.activity) : step.activity != null) return false;
        if (created != null ? !created.equals(step.created) : step.created != null) return false;
        if (details != null ? !details.equals(step.details) : step.details != null) return false;
        if (id != null ? !id.equals(step.id) : step.id != null) return false;
        if (phase != null ? !phase.equals(step.phase) : step.phase != null) return false;
        //noinspection RedundantIfStatement
        if (source != null ? !source.equals(step.source) : step.source != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + version;
        return result;
    }
}
