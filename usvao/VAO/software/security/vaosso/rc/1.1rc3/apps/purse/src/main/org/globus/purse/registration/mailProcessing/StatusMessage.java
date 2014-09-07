package org.globus.purse.registration.mailProcessing;

/** A description of the outcome of an attempted action.  Immutable. */
public class StatusMessage {
    private String description;
    private boolean successful;
    private Throwable throwable;

    /** Construct a new status message. */
    public StatusMessage(String description, boolean successful, Throwable throwable) {
        this.description = description;
        this.successful = successful;
        this.throwable = throwable;
    }

    /** No exception. */
    public StatusMessage(String description, boolean successful) { this(description, successful, null); }

    /** A user-readable description of the outcome. */
    public String getDescription() { return description; }

    /** Was the attempt successful? */
    public boolean isSuccessful() { return successful; }

    /** An exception that was thrown during the attempt. */
    public Throwable getThrowable() { return throwable; }
}
