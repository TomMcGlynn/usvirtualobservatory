package org.nvo.eventtide.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.nvo.eventtide.client.orm.Detail;

import java.util.*;

public interface LogServiceAsync {
    void doSomething(String activity, String phase, ArrayList<Detail> details, AsyncCallback<Void> async);
}
