package org.usvo.openid.serve;

import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * a collection of a user attributes, either requested or ready to ship
 */
public class Attributes implements Iterable<Attribute> {

    private Map<UserAttributes.LocalType, Attribute> atts = 
        new TreeMap<UserAttributes.LocalType, Attribute>();

    /**
     * create an empty set of Attributes
     */
    public Attributes() {  }

    /**
     * add an Attribute to this collection
     */
    public void add(Attribute att) {
        atts.put(att.getType(), att);
    }

    /**
     * return a requested attribute or null if it is not in the collection
     */
    public Attribute getAttribute(UserAttributes.LocalType type) {
        return atts.get(type);
    }

    /**
     * return true if the Attribute list contains the given type
     */
    public boolean includes(UserAttributes.LocalType type) {
        return atts.containsKey(type);
    }

    /**
     * return an iterator for accessing the individusl attributes
     */
    public Iterator<Attribute> iterator() {
        return atts.values().iterator();
    }

    /**
     * create and fill a name-value look-up of attributes that have been 
     * okayed for sharing
     */
    public Map<String, List<String> > export() {
        Map<String, List<String> > out = 
            new HashMap<String, List<String> >(atts.size());
        List<String> values = null;
        for (Attribute att : atts.values()) {
            if (att.sharingAllowed()) {
                values = new Vector<String>(att.values);
                out.put(att.getAlias(), values);
            }
        }

        return out;
    }

    /**
     * return true if one of the attributes included is a user credential
     * but that credential has not be created yet.
     */
    public boolean needsCredential() {
        if (! atts.containsKey(UserAttributes.LocalType.CREDENTIAL)) 
            return false;

        return atts.get(UserAttributes.LocalType.CREDENTIAL)
            .getLastValue() == "";
    }

    /**
     * set a credential for this set
     */
    public void setCredential(String cred) {
        Attribute att = atts.get(UserAttributes.LocalType.CREDENTIAL);
        att.setValue(cred);
    }

    /**
     * return the number of attributes in this set
     */
    public int size() { return atts.size(); }

    /**
     * return true if this list includes required attributes
     */
    public boolean includesRequired() {
        for (Attribute att : atts.values()) {
            if (att.isRequired()) return true;
        }
        return false;
    }

    /**
     * return true if this list includes non-required attributes
     */
    public boolean includesOptional() {
        for (Attribute att : atts.values()) {
            if (! att.isRequired()) return true;
        }
        return false;
    }
}