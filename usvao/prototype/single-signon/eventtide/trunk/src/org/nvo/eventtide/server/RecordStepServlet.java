package org.nvo.eventtide.server;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nvo.eventtide.client.orm.Detail;
import org.nvo.eventtide.client.orm.Step;
import org.nvo.eventtide.server.util.DbKit;
import org.nvo.eventtide.server.util.PojoHtml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Record a single step at a time. */
public class RecordStepServlet extends HttpServlet {
    public static final String ACTIVITY = "activity", ACTION = "action", PHASE = "phase";
    public static final String GIVE_FEEDBACK = "meta_give_feedback";
    public static final Set<String> IGNORE_PARAMS;
    static {
        Set<String> ignoreParams = new HashSet<String>();
        ignoreParams.add(GIVE_FEEDBACK);
        IGNORE_PARAMS = Collections.unmodifiableSet(ignoreParams);
    }

    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serve(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serve(req, resp);
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    private void serve(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long start = System.currentTimeMillis();
        StepParser parser = new StepParser(request);

        if (!parser.errors.isEmpty()) {
            StringBuffer buf = new StringBuffer();
            for (String err : parser.errors) buf.append(err).append("\n");
            response.sendError(400, buf.toString());
        } else {
            Session session = null;
            try {
                session = DbKit.getSession();
                Transaction t;
                t = session.beginTransaction();
                session.save(parser.step);

                log.debug(parser.step.toString());
                for (Detail detail : parser.step.getDetails()) {
                    session.save(detail);
                    log.debug(detail.toString());
                }
                t.commit();

                response.setStatus(201); // "a new resource has been created"
                if (request.getParameter(GIVE_FEEDBACK) != null) {
                    // reload for debugging
                    session.evict(parser.step);
                    Step reloaded = (Step) session.get(Step.class, parser.step.getId());
                    response.setContentType("text/html");
                    Writer writer = response.getWriter();
                    writer.append("<h1>Created Log Entry</h1>")
                            .append("<p>" + new PojoHtml(reloaded).render() + "</p>");
                    writer.append("<p><a href=\"manual.jsp\">Create a log entry manually</a></p>");
                    writer.append("<p>served in " + (System.currentTimeMillis() - start) + " ms.</p>");
                }
                else {
                    response.setContentType("text/plain");
                    response.getWriter().append("saved step #" + parser.step.getId());
                }
            } finally {
                DbKit.close(session);
            }
        }
    }
}
