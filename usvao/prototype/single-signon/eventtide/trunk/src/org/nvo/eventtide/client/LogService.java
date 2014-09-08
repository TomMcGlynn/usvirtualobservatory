package org.nvo.eventtide.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.core.client.GWT;
import org.nvo.eventtide.client.orm.Detail;

import java.util.*;

@RemoteServiceRelativePath("LogService")
public interface LogService extends RemoteService {
    void doSomething(String activity, String phase, ArrayList<Detail> details);

    /**
     * Utility/Convenience class.
     * Use LogService.App.getInstance() to access static instance of LogServiceAsync
     */
    public static class App {
        private static final LogServiceAsync ourInstance = (LogServiceAsync) GWT.create(LogService.class);

        public static LogServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
