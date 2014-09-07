package org.usvo.openid.util;

import java.io.IOException;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.*;

public class ParseKit {
    public static String getText(String filename) throws IOException {
        FileReader reader = new FileReader(filename);
        StringWriter writer = new StringWriter();
        char[] chars = new char[4000];
        while (true) {
            int n = reader.read(chars);
            if (n < 0) break;
            writer.write(chars, 0, n);
        }
        return writer.toString();
    }

    public static List<String> readLines(String path) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(path));
        List<String> lines = new ArrayList<String>();
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            lines.add(line);
        }
        return lines;
    }

    public static String printLines(List<String> lines, String separator) {
        Comma sep = new Comma("", separator);
        StringBuilder b = new StringBuilder();
        for (String line : lines) b.append(sep).append(line);
        return b.toString();
    }

    public static String printLines(List<String> lines) {
        return printLines(lines, 0, lines.size());
    }

    public static String appendLine(String lines, String line) {
        return lines + printLines(Arrays.asList(line));
    }

    public static String printLines(List<String> lines, int begin, int end) {
        StringWriter s = new StringWriter();
        PrintWriter writer = new PrintWriter(s);
        for (int i = begin; i < end; ++i) writer.println(lines.get(i));
        return s.toString();
    }

    /** Trim a URL.
     *  @param removeProtocol if true, remove http:// or https://
     *  @param removeStandardPort if true, remove :80 or :443
     * @param removePath if true, remove trailing path elements
     * @param removeParams if true, remove ?a=b&c=d etc. */
    public static String trimUrl(String url,
         boolean removeProtocol, boolean removeStandardPort, boolean removePath, boolean removeParams)
    {
        // start with http://example.com:80/some/path?etc.&etc.
        String result = url == null ? "" : url.trim();
        // first, trim off up to double (or more) slashes
        int protocolEnd = result.indexOf("://");
        String protocol = "";
        if (protocolEnd >= 0) {
            protocol = result.substring(0, protocolEnd + 3);
            result = result.substring(protocolEnd + 3);
            while (result.indexOf("/") == 0)
                result = result.substring(1); // remove additional slashes
        }
        // now we're down to example.com:80/some/path?etc.&etc.
        // trim off parameters & hash
        String params = "";
        int paramStart = result.indexOf("?"), hashStart = result.indexOf("#");
        if (hashStart > 0 && hashStart < paramStart) paramStart = hashStart;
        if (paramStart > 0) {
            params = result.substring(paramStart);
            result = result.substring(0, paramStart);
        }
        // trim off path
        String path = "";
        int pathStart = result.indexOf("/");
        if (pathStart > 0) {
            path = result.substring(pathStart);
            result = result.substring(0, pathStart);
        }
        // now we're down to example.com:80
        // now trim off :80 or :443 (leave non-standard ports)
        String port = "";
        if ((protocol.equalsIgnoreCase("http://") && result.endsWith(":80"))
                || (protocol.equalsIgnoreCase("https://") && result.endsWith(":443")))
        {
            port = result.substring(result.lastIndexOf(':'));
            result = result.substring(0, result.lastIndexOf(':'));
        }

        return (removeProtocol ? "" : protocol)
                + result
                + (removeStandardPort ? "" : port)
                + (removePath ? "" : path)
                + (removeParams ? "" : params);
    }

    public static String generateRandomBase64String(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < buf.length; ++i)
            buf[i] = BASE64_CHARS.charAt(r.nextInt(BASE64_CHARS.length()));
        return new String(buf);
    }

    // 64 characters
    private static final String BASE64_CHARS
            = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";

    private static final Random r = new Random();
}
