package org.nvo.eventtide.server.util;

import org.nvo.eventtide.client.util.Compare;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/** Render a Java object in HTML.  Formats beans, maps, collections, exceptions. */
public class PojoHtml {
    private static final String TABLE_OPEN = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\">";
    private static final String TABLE_CLOSE = "</table>";

    private Set<Object> dun = new HashSet<Object>();
    private String result = null;
    private final Object o;

    public PojoHtml(Object o) {
        this.o = o;
    }

    public synchronized String render() {
        if (result == null) {
            StringWriter result = new StringWriter();
            PrintWriter out = new PrintWriter(result);
            Deque<Object> parents = new ArrayDeque<Object>();
            // wrap so that class name is visible in output
            Collection<Object> wrapper = new ArrayList<Object>();
            wrapper.add(o);
            render(wrapper, out, parents);
            this.result = result.toString();
        }
        return result;
    }

    private void render(Object o, PrintWriter out, Deque<Object> parents) {
        if (isSimple(o)) out.print(o);
        else if (similar(parents, o)) out.println("<i>[ancestor]</i>");
        else if (dun.contains(o)) out.println("<i>[already displayed elsewhere]</i>");
        else {
            dun.add(o);
            if (o instanceof Collection) renderCollection((Collection) o, out, parents);
            else if (o instanceof Map) renderMap((Map) o, out, parents);
            else if (o instanceof Throwable) renderException((Throwable) o, out);
            else renderBean(o, out, parents);
        }
    }

    /** Is <tt>o</tt> {@link org.nvo.eventtide.client.util.Similar} to any elements in <tt>collection</tt>? */
    private boolean similar(Collection<Object> collection, Object o) {
        for (Object x : collection)
            if (Compare.isSimilar(x, o)) return true;
        return false;
    }

    private void renderCollection(Collection c, PrintWriter out, Deque<Object> parents) {
        out.println(TABLE_OPEN);
        parents.push(c);
        for (Object x : c) {
            Class cls = getClass(x);
            out.println("<tr>");
            out.println("<td title=\"" + cls.getCanonicalName() + "\">" + cls.getSimpleName() + "</td>");
            out.println("<td>");
            render(x, out, parents);
            out.println("</td>");
            out.println("</tr>");
        }
        parents.pop();
        out.println(TABLE_CLOSE);
    }

    private void renderMap(Map m, PrintWriter out, Deque<Object> parents) {
        out.println(TABLE_OPEN);
        parents.push(m);
        for (Object key : m.keySet()) {
            Object value = m.get(key);
            Class vCls = getClass(value);
            out.println("<tr>");
            out.println("<td title=\"" + vCls.getCanonicalName() + "\">");
            render(key, out, parents);
            out.println("</td>");
            out.println("<td>");
            render(value, out, parents);
            out.println("</td>");
            out.println("</tr>");
        }
        parents.pop();
        out.println(TABLE_CLOSE);
    }

    /** Crude rendering of a JavaBean in HTML. */
    private void renderBean(Object bean, PrintWriter out, Deque<Object> parents) {
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            out.println(TABLE_OPEN);
            for (PropertyDescriptor prop : props) {
                if (shouldSkip(prop)) continue;
                out.println("<tr>");
                out.println("<td title=\"" + prop.getPropertyType().getCanonicalName() + "\">" + prop.getName() + "</td>");
                out.println("<td>");
                try {
                    Object value = prop.getReadMethod().invoke(bean);
                    parents.push(bean);
                    render(value, out, parents);
                    parents.pop();
                } catch (Exception e) {
                    renderException(e, out);
                }
                out.println("</td>");
                out.println("</tr>");
            }
            out.println(TABLE_CLOSE);
        } catch (IntrospectionException e) {
            renderException(e, out);
        }
    }

    private void renderException(Throwable t, PrintWriter out) {
        out.println("<pre>");
        t.printStackTrace(out);
        out.println("</pre>");
    }

    private static boolean shouldSkip(PropertyDescriptor prop) { return prop.getName().equals("class"); }
    private static Class getClass(Object o) { return o == null ? Void.class : o.getClass(); }
    private static boolean isSimple(Object x) { return x == null || x instanceof String || x instanceof Number || x instanceof Boolean || x instanceof Date; }
}
