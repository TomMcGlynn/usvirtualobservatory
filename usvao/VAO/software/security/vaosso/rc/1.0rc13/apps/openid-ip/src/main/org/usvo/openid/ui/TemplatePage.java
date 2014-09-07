package org.usvo.openid.ui;

import org.usvo.openid.Conf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/** A web page that is based on a template, with some values substituted into it.
 *
 *  <p>Reads a template out of the WEB-INF/templates directory.
 *  See {@link #substitute(String, Map<String,String>)} for main usage.</p> */
public class TemplatePage {
    private String name;
    private String template;
    private String path;

    /** Render a page to the user.
     *  @param pageName the name of the template to use
     *  @param map substitutions to make in the template before sending */
    public static void display
            (HttpServletRequest request, HttpServletResponse response,
             String pageName, Map<String, String> map)
            throws IOException
    {
        if (map == null)
            map = new HashMap<String, String>();

        if (!map.containsKey(TemplateTags.TAG_BASE_URL))
            map.put(TemplateTags.TAG_BASE_URL, Conf.get().getBaseUrl()+"/");

        new TemplatePage(pageName).handle(request, response, map);
    }

    /** Render a page as a String.
     *  @param pageName the name of the template to use
     *  @param map substitutions to make in the template before sending */
    public static String substitute(String pageName, Map<String, String> map)
            throws IOException
    {
        return new TemplatePage(pageName).substitute(map);
    }

    /** Render a page with a single key-value substitution. */
    public static String substitute(String pageName, String key, String value) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return substitute(pageName,  map);
    }

    /** Load a page without doing any substitutions. */
    public static String load(String pageName) throws IOException {
        return new TemplatePage(pageName).getTemplate();
    }

    public TemplatePage(String name) throws IOException {
        this(Conf.get().getContext().getRealPath("WEB-INF" + File.separator + "templates"), name);
    }

    public TemplatePage(String path, String name) throws IOException {
        this.path = path;
        this.name = name;
        read();
    }

    /** The raw, un-substituted form of this page. */
    public String getTemplate() { return template; }

    /** For each key and value in <tt>map</tt>, go through the template and substitute
     *  each instance of "<tt>[key]</tt>" with "<tt>value</tt>", and return the result. */
    public String substitute(Map<String, String> map) {
        String result = template;
        if (map != null)
            for (String key : map.keySet()) {
                String value = map.get(key);
                if (value == null) value = "";
                result = result.replaceAll("<!--" + key + "-->", value);
            }
        return result;
    }

    /** Append <tt>value</tt> to <tt>key</tt>'s existing value, if it exists, or add it to the map if it doesn't.
     *  @param separator if a value already exists, separate it from the new value with <tt>separator</tt> */
    public static void append(Map<String, String> map, String key, String value, String separator) {
        String before = map.get(key);
        if (before == null)
            map.put(key, value);
        else
            map.put(key, before + separator + value);
    }
    /** Append <tt>value</tt> to <tt>key</tt>'s existing value, if it exists, or add it to the map if it doesn't. */
    public static void append(Map<String, String> map, String key, String value) { append(map, key, value, ""); }

    private void read() throws IOException {
        File file = new File(path + File.separator + name);
        if (!file.exists()) throw new IOException("File not found: " + file.getPath());
        FileReader reader = new FileReader(file);
        LineNumberReader lineReader = new LineNumberReader(reader);
        StringBuilder builder = new StringBuilder();
        String line;
        do {
            line = lineReader.readLine();
            if (line != null) builder.append(line).append("\n");
        } while (line != null);
        template = builder.toString();

        resolveIncludes();
    }

    private static final String INCLUDE_START = "<!--include:";
    private void resolveIncludes() throws IOException {
        int includeLocation = template.indexOf(INCLUDE_START);
        while (includeLocation >= 0) {
            if (includeLocation >= 0) {
                int start = includeLocation + INCLUDE_START.length();
                int end = template.indexOf("-->", start);
                String includeName = template.substring(start, end);
                TemplatePage includePage = new TemplatePage(path, includeName);
                template = template.replaceAll(INCLUDE_START + includeName + "-->", includePage.template);
            }
            // go on to the next include: directive
            includeLocation = template.indexOf(INCLUDE_START, includeLocation+1);
        }
    }

    public void handle(HttpServletResponse response, Map<String, String> map) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().print(substitute(map));
    }

    private Map<String, String> addRootPath(HttpServletRequest request, Map<String, String> map) {
        if (map == null) map = new HashMap<String, String>();
        if (!map.containsKey(TemplateTags.TAG_ROOT_PATH)) {
            map = new HashMap<String, String>(map);
            map.put(TemplateTags.TAG_ROOT_PATH, request.getContextPath() + "/");
        }
        return map;
    }

    /** Add root path, based on <tt>request</tt>. */
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, String> map) throws IOException {
        map = addRootPath(request, map);
        if (!map.containsKey(TemplateTags.TAG_TITLE))
            map.put(TemplateTags.TAG_TITLE, "");
        handle(response, map);
    }
}
