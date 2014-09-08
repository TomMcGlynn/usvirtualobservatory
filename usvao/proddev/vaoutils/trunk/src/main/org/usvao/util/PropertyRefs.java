/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.util;

import java.util.Properties;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * a class for supporting within a properties file recursive references 
 * to other properties.
 * <p>
 * Note that this class was originally written to support configuration 
 * files that suportted internal references; however, this class is deprecated
 * for that purpose in favor of {@link StdConfiguration}.  
 */
public class PropertyRefs {

    private Properties props = null;
    private Pattern prefpat = Pattern.compile("(?<!\\\\)\\$\\{([^}]+)\\}");
    private Set<String> resolved = new HashSet<String>(4);

    /**
     * initialize this resolver with a Properties instance to operate on
     */
    public PropertyRefs(Properties p) {
        props = p;
    }

    /**
     * resolve the ${...} in a string with values from a Properties list
     */
    public String resolve(String val) {
        HashSet<String> seen = new HashSet<String>(4);
        return resolve(val, seen);
    }

    String resolve(String val, Set<String> seen) {
        Matcher m = prefpat.matcher(val);
        String refname = null, refval;
        while (m.find()) {
            refname = m.group(1).trim();
            if (seen.contains(refname)) continue;  // circular refs
            seen.add(refname);

            refval = props.getProperty(refname);
            if (! resolved.contains(refname)) {
                if (refval == null) continue;
                refval = resolve(refval, seen);
                props.setProperty(refname, refval);
                resolved.add(refname);
            }

            // this is necessary in case of circular or broken references;
            // refval may contain a ${...} that would break replaceAll().
            Matcher rf = prefpat.matcher(refval);
            if (rf.find()) {
                String repl = "\\\\\\${$1}";
                refval = rf.replaceAll(repl);
            }

            val = Pattern.compile("\\$\\{"+m.group(1)+"\\}")
                .matcher(val).replaceAll(refval);
            m = prefpat.matcher(val);
        }

        return val;
    }

    /**
     * resolve ${...} references in a Properties instance
     */
    public void resolve() {
        String name;
        Enumeration e=props.propertyNames();
        while(e.hasMoreElements()) {
            name = (String) e.nextElement();
            if (resolved.contains(name)) continue;
            props.setProperty(name, resolve(props.getProperty(name)));
        }
    }

    /**
     * resolve ${...} references in a Properties instance
     */
    public static void resolve(Properties props) {
        PropertyRefs resolver = new PropertyRefs(props);
        resolver.resolve();
    }

    /**
     * resolve ${...} references in a template string with values 
     * given in a Properties set.  The Properties values can be 
     * self-referential; however, its values will not be altered.
     */
    public static String format(String template, Properties props) {
        PropertyRefs resolver = new PropertyRefs(new Properties(props));
        return resolver.resolve(template);
    }

    /**
     * return the wrapped Properties.  The values will have been updated
     * if resolve() was called.  
     */
    public Properties getProperties() { return props; }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home", "${msg}");
        props.setProperty("home.build", "${home}/build");
        props.setProperty("home.classes", "${home.build}/classes");
        props.setProperty("msg", "${home.classes} is tops");
        PropertyRefs pr = new PropertyRefs(props);
        System.out.println("Before resolve:");
        props.list(System.out);
        pr.resolve();
        System.out.println("After resolve:");
        props.list(System.out);
    }
}