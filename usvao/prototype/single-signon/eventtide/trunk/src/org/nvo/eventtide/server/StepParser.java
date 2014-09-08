package org.nvo.eventtide.server;

import org.nvo.eventtide.client.orm.Detail;
import org.nvo.eventtide.client.orm.Step;
import org.nvo.eventtide.client.util.Compare;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class StepParser {
    public Collection<String> errors = new LinkedHashSet<String>();
    public Step step = new Step();
    public Map<String, String> details = new HashMap<String, String>();
    private HttpServletRequest request;

    public StepParser(HttpServletRequest request) {
        this.request = request;
        extract();
    }

    private void extract() {
        step.initCreated();
        System.out.println("Request parameters = " + request.getParameterMap());
        // we check all parameters, looking for matches to activity and phase
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            if (RecordStepServlet.IGNORE_PARAMS.contains(name)) continue;
            if (paramNameMatches(name, RecordStepServlet.ACTIVITY) || paramNameMatches(name, RecordStepServlet.ACTION)) {
                for (String action : request.getParameterValues(name)) {
                    if (step.getActivity() == null) step.setActivity(action);
                    else errors.add("Activity already set to " + step.getActivity()
                            + "; new value " + name + "=" + action + " ignored.");
                }
            }
            else if (paramNameMatches(name, RecordStepServlet.PHASE)) {
                for (String phase : request.getParameterValues(name)) {
                    if (step.getPhase() == null) step.setPhase(phase);
                    else errors.add("Phase already set to " + step.getPhase()
                            + "; new value " + name + "=" + phase + " ignored.");
                }
            }
            else {
                // only store non-blank values
                for (String value : request.getParameterValues(name)) {
                    Detail detail = new Detail(name, value);
                    step.addDetail(detail);
                    if (!Compare.isBlank(value)) details.put(name, value);
                }
            }
        }

        if (Compare.isBlank(step.getActivity())) errors.add("No activity specified.");
        if (Compare.isBlank(step.getPhase())) errors.add("No phase specified.");

        try {
            InetAddress remoteHost = InetAddress.getByName(request.getRemoteHost());
            step.setSource(remoteHost.getCanonicalHostName());
            step.setSourceValidation(Step.VAL_HTTP);
        } catch(UnknownHostException e) {
            step.setSource(request.getRemoteHost());
            step.setSourceValidation(Step.VAL_HTTP);
        }
    }

    /** Does HttpRequest parameter named <tt>paramName</tt> match <tt>propertyName</tt>?
     *  True if <tt>paramName.toLowerCase()</tt> equals <tt>paramName</tt>. */
    private boolean paramNameMatches(String paramName, String propertyName) {
        return paramName.toLowerCase().equals(propertyName);
    }
}
