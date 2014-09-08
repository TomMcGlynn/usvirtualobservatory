package org.nvo.eventtide.server.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nvo.eventtide.client.orm.*;

public class DbKit {
    private static final SessionFactory sessionFactory;

    static {
        try {
            AnnotationConfiguration config = (AnnotationConfiguration) new AnnotationConfiguration().
                    configure("hibernate.cfg.xml");
            config.setNamingStrategy(ImprovedNamingStrategy.INSTANCE); // lower_case_table_names
            config.addAnnotatedClass(Step.class);
            config.addAnnotatedClass(Auth.class);
            config.addAnnotatedClass(Correlate.class);
            config.addAnnotatedClass(Detail.class);
            config.addAnnotatedClass(Sequence.class);
            sessionFactory = config.buildSessionFactory();
        }
        catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException { return sessionFactory.openSession(); }

    public static SessionFactory getSessionFactory() { return sessionFactory; }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void shutdown() { getSessionFactory().close(); }

    public static void close(Session session) { if (session != null) session.close(); }
}
