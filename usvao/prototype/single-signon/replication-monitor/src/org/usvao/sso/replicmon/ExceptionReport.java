package org.usvao.sso.replicmon;

/** A report that an exception occurred. */
public class ExceptionReport {
    private Throwable throwable;
    private String explanation;

    public ExceptionReport(Throwable throwable, String explanation) {
        this.throwable = throwable;
        this.explanation = explanation;
    }

    public ExceptionReport(Throwable throwable) { this.throwable = throwable; }

    public Throwable getThrowable() { return throwable; }
    public String getExplanation() {
        return explanation == null
            ? (throwable == null ? "<no explanation>" : throwable.getMessage())
            : explanation;
    }
}
