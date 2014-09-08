package org.usvao.sso.replicmon;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/** The basic Replication Monitor servlet.  Checks replication and prints status. */
public class ReplicMonServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(ReplicMonServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        go(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        go(request, response);
    }

    private void go(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean checksPassed = false;
        List<ExceptionReport> exceptions = new LinkedList<ExceptionReport>();

        ReplicmonConfig config = new ReplicmonConfig(getServletContext());
        {
            SystemChecker checker = null;
            try {
                checker = new SystemChecker(config);
                exceptions.addAll(checker.getSetupExceptions());
                // TODO break out results by pair & display timing for each
                checker.checkAll();
                checksPassed = true;
            } catch (SQLException e) {
                String msg = "Exception while checking replication.";
                exceptions.add(0, new ExceptionReport(e, msg)); // push setup exceptions to back
                log.warn(msg, e);
            } catch(ReplicationFailedException e) {
                exceptions.add(0, new ExceptionReport(e)); // push setup exceptions to back
            } finally {
                if (checker != null)
                    checker.close();
            }
        }

        ReplicationReport report = new ReplicationReport(config, checksPassed, exceptions);
        report.report(request, response);
    }
}
