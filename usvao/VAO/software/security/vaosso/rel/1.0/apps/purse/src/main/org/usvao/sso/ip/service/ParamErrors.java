package org.usvao.sso.ip.service;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * a container for error messages resulting from validating input parameters
 * to a service.  A message is stored and retrieved via the name of 
 * the parameter it describes.  General messages describing errors in all the 
 * parameters as a group should be attached with an empty string as its 
 * parameter name.  It is recommended that the most summarizing message appears
 * first in the list of messages.  
 * <p>
 * These messages are intended for display in a user interface
 * and be instructional to the user on how to correct the problems.  
 */
public class ParamErrors implements Iterable<String> {
    HashMap<String, List<String> > byname = 
        new HashMap<String, List<String> >();
    final String[] params;

    /**
     * create an empty set of error messages
     */
    ParamErrors() { params = new String[0]; }

    /**
     * initialize an empty set of error messages with a given set of 
     * names of the parameters they will describe.
     */
    ParamErrors(String[] parameters) { params = parameters; }

    public void addMessage(String paramName, String msg) {
        List<String> msgs = byname.get(paramName);
        if (msgs == null) {
            msgs = new ArrayList<String>();
            byname.put(paramName, msgs);
        }
        msgs.add(msg);
    }

    /**
     * return the error message for a given parameter or null if there 
     * are no messages registered.
     */
    public String[] getMessagesFor(String paramName) {
        List<String> msgs = byname.get(paramName);
        if (msgs == null || msgs.size() == 0) return null;

        return msgs.toArray(new String[msgs.size()]);
    }

    /**
     * return true if there messages registered for a given parameter 
     * name
     */
    public boolean hasMessagesFor(String paramName) {
        List<String> msgs = byname.get(paramName);
        return (msgs != null && msgs.size() > 0);
    }

    /**
     * return the number of parameters this container has messages for 
     */
    public int getParamCount() { return byname.size(); }

    /**
     * return true if this container contains errors registered 
     */
    public boolean hasMessages() { return getParamCount() > 0; }

    /**
     * return the total number of messages collected so far
     */
    public int getMessageCount() { 
        int n = 0;
        for (List<String> msgs : byname.values()) 
            n += msgs.size();
        return n;
    }

    public List<String> toList() {
        List<String> combined = new ArrayList(byname.size());
        List<String> msgs = byname.get("");
        if (msgs != null && msgs.size() > 0) {
            for (String msg : msgs) 
                combined.add(msg);
        }
        for(String param : params) {
            msgs = byname.get(param);
            if (msgs != null && msgs.size() > 0) {
                for (String msg : msgs) 
                    combined.add(msg);
            }
        }
        return combined;
    }

    public String[] toArray() {
        List<String> combined = toList();
        return combined.toArray(new String[combined.size()]);
    }

    /**
     * return a flat iteration through all registered messages.
     */
    public ListIterator<String> iterator() {
        List<String> combined = toList();
        return combined.listIterator();
    }

    /**
     * remove all error messages from the container
     */
    public void clear() {
        byname.clear();
    }
}
    
