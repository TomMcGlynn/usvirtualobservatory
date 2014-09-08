package org.nvo.eventtide.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.nvo.eventtide.client.LogService;
import org.nvo.eventtide.client.orm.Detail;
import org.nvo.eventtide.client.orm.Step;
import org.nvo.eventtide.server.util.DbKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LogServiceImpl extends RemoteServiceServlet implements LogService {
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    @Override
    public void doSomething(String activity, String phase, ArrayList<Detail> details) {
        try {
            Session session = DbKit.getSession();
            Transaction t;

            t = session.beginTransaction();
            Step step = new Step();
            step.setActivity(activity);
            step.setPhase(phase);
            step.setSource("GWT client");
            step.initCreated();
            session.save(step);
            log.debug(step.toString());
            for (Detail detail : details) {
                detail.initStep(step);
                session.save(detail);
                log.debug(detail.toString());
            }
            t.commit();
            session.clear();

            log.debug("All Steps:");
            for (Iterator i = session.createQuery("from Step").iterate(); i.hasNext(); ) {
                Step d = (Step) i.next();
                log.debug(" * " + d);
            }
            log.debug("All Details:");
            for (Iterator i = session.createQuery("from Detail").iterate(); i.hasNext(); ) {
                Detail detail = (Detail) i.next();
                log.debug(" * " + detail);
            }
        }
        catch(Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}