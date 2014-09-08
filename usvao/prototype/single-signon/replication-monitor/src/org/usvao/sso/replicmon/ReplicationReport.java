package org.usvao.sso.replicmon;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ReplicationReport {
    private ReplicmonConfig config;
    private boolean allTestsPassed;
    private List<ExceptionReport> exceptions;

    /** Even if tests passed, only succeed if no exceptions. */
    public ReplicationReport(ReplicmonConfig config, boolean allTestsPassed, List<ExceptionReport> exceptions) {
        this.config = config;
        this.allTestsPassed = allTestsPassed;
        this.exceptions = exceptions;
    }

    public void report(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        boolean detailed = (path.startsWith("/detail"));
        boolean success = exceptions.isEmpty() && allTestsPassed;

        // head
        response.setContentType("text/html");
        if (success) {
            response.setStatus(200);
            response.getWriter().println
                    ("<html><head><title>Replication Monitor - All Tests Passed</title></head>");
            response.getWriter().println("<body>");
        } else {
            response.setStatus(500);
            response.getWriter().println
                    ("<html><head><title>Replication Monitor - Failed</title></head>");
            response.getWriter().println("<body style='background:#fcc'>");
        }

        // body: header
        String summary;
        if (!exceptions.isEmpty())
            summary = (allTestsPassed
                    ? "At least some replication checks passed, but exceptions occurred."
                    : "Some replication tests failed, and exceptions occurred.");
        else
            summary = success ? "All replication tests passed." : "Some replication tests failed.";
        response.getWriter().println("<h1>" + (success ? "Passed" : "Failed") + "</h1>");
        response.getWriter().println("<h3>" + summary + "</h3>");

        // body: detailed report
        if (detailed) {
            response.getWriter().println("<h3>Details:</h3>");
            for (String s : config.stringPropertyNames()) {
                if (s.toLowerCase().contains("password")) continue;
                response.getWriter().println("<p>" + s + " = " + config.get(s) + "</p>");
            }

            String noDetails = request.getRequestURL().toString();
            int detailPos = noDetails.indexOf("/detail");
            if (detailPos > 0) {
                noDetails = noDetails.substring(0, detailPos);
                response.getWriter().println("<p><a href=\"" + noDetails + "\">hide details</a></p>");
            }
        }
        else {
            String detailPath = request.getContextPath() + request.getServletPath() + "/detail";
            response.getWriter().println("<p><a href=\"" + detailPath + "\">show details</a></p>");
        }

        // body: exceptions
        if (!exceptions.isEmpty()) {
            // 1. summaries & links
            response.getWriter().println("<h3>Exceptions:</h3>");
            response.getWriter().println("<ul>");
            for (int i = 0; i < exceptions.size(); i++) {
                ExceptionReport report = exceptions.get(i);
                response.getWriter().println
                        ("<li><a href='#" + exceptionLink(i) + "'>" + report.getExplanation() + "</a></li>");
            }
            response.getWriter().println("</ul>");

            // 2. stack traces
            for (int i = 0; i < exceptions.size(); i++) {
                ExceptionReport report = exceptions.get(i);
                response.getWriter().println
                        ("<p><a name='" + exceptionLink(i) + "'>" + report.getExplanation() + "</a></p>");
                //noinspection ThrowableResultOfMethodCallIgnored
                if (report.getThrowable() != null) {
                    response.getWriter().println("<pre>");
                    //noinspection ThrowableResultOfMethodCallIgnored
                    report.getThrowable().printStackTrace(response.getWriter());
                    response.getWriter().println("</pre>");
                }
            }
        }


        // footer
        response.getWriter().println("</body></html>");
    }

    private String exceptionLink(int index) { return "ex" + index; }
}
