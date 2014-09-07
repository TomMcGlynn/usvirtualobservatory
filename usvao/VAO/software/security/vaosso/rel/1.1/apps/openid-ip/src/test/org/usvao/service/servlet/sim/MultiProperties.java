package org.usvao.service.servlet.sim;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * a utility class for properties that can have a list of values but usually
 * only have one
 */
public class MultiProperties {
    Map<String, List<String> > data = new HashMap<String, List<String> >();
    public void set(String name, String value) {
        data.remove(name);
        add(name, value);
    }
    public void add(String name, String value) {
        List<String> vals = ensure(name);
        vals.add(value);
    }
    public void add(String name, String[] value) {
        List<String> vals = ensure(name);
        for(String v : value) 
            vals.add(v);
    }
    private List<String> ensure(String name) {
        List<String> vals = data.get(name);
        if (vals == null) {
            vals = new ArrayList<String>();
            data.put(name, vals);
        }
        return vals;
    }
    public void clear() { data.clear(); }
    public boolean hasName(String name) { return data.containsKey(name); }
    public Collection<String> get(String name) { return data.get(name); }
    public String[] getAll(String name) {
        List<String> out = data.get(name);
        if (out == null) return null;
        return out.toArray(new String[out.size()]);
    }
    public String getFirst(String name) { 
        List<String> out = data.get(name);
        if (out == null || out.size() == 0) return null;
        return out.get(0);
    }
    public Collection<String> getNames() { return data.keySet(); }
    public String[] getNameArray() { 
        return data.keySet().toArray(new String[data.size()]); 
    }
    public Enumeration<String> names() { 
        return new ListEnum(data.keySet().iterator()); 
    }
    public Enumeration<String> values(String name) { 
        List<String> vals = data.get(name);
        return new ListEnum((vals == null) ? null : vals.iterator()); 
    }
    public Map<String, String[]> map() {
        HashMap<String, String[]> out = new HashMap<String, String[]>();
        String name = null;
        for(Enumeration<String> e = names(); e.hasMoreElements();) {
            name = e.nextElement();
            out.put(name, getAll(name));
        }
        return out;
    }
    public int size() { return data.size(); }

    class ListEnum implements Enumeration<String> {
        private Iterator<String> iter = null;
        ListEnum(Iterator<String> i) { iter = i; }

        @Override
        public boolean hasMoreElements() { 
            return (iter == null) ? false : iter.hasNext(); 
        }

        @Override 
        public String nextElement() { 
            if (iter == null) 
                throw new NoSuchElementException();
            return iter.next(); 
        }
    }
}
    
