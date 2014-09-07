/*
 * Adapted from org.apache.catalina.core.DummyResponse.java:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usvao.service.servlet.sim;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Locale;
import java.util.Collection;
import java.net.URLEncoder;

import java.io.BufferedReader;
import java.io.FileReader;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * An HTTPServletRequest implementation used for unit testing
 *
 * Based on org.apache.catalina.core.DummyResponse.java by 
 * @author Remy Maucherat
 */
@SuppressWarnings("deprecation")
public class TestResponse implements HttpServletResponse {

    TestResponse() {
    }

    /**
     * create the response, specifying a file to capture the response
     * stream.  
     */
    public TestResponse(String destFile) throws IOException {
        this(new File(destFile));
    }

    /**
     * create the response, specifying a file to capture the response
     * stream.  
     */
    public TestResponse(File destFile) throws IOException {
        outf = destFile;
        if (outf.exists()) outf.delete();
        header.add("Server", "servlet-tester/1.1");
        header.add("Date", "Mon, 23 May 2005 22:38:34 GMT");
    }

    protected File outf = null;
    protected MultiProperties header = new MultiProperties();
    MyWriter out = null;
    protected int sc = SC_OK;
    protected String msg = "OK";

    public PrintWriter getWriter() throws IOException { 
        PrintWriter pw = null;
        if (out == null) {
            out = new MyWriter();
            pw = new PrintWriter(out);
            writeHeader(pw, sc, msg);
        } else {
            pw = new PrintWriter(out);
        }            
        return pw;
    }
    public void flushBuffer() throws IOException {
        if (out != null) out.flush();
    }
    public boolean isCommitted() { return outf.exists(); }
    protected void writeHeader(PrintWriter w, int code, String message) {
        w.append("HTTP/1.1 ").append(Integer.toString(code));
        if (message == null) 
            message = (code == 200) ? "OK" : "Not OK";
        w.append(" ").println(message);

        Collection<String> vals = null;
        for(String name : getHeaderNames()) {
            vals = getHeaders(name);
            for(String val : vals) 
                w.append(name).append(": ").println(val);
        }
        w.println();
    }
    public String getHeader(String name) { return header.getFirst(name); }
    public Collection<String> getHeaderNames() { return header.getNames();  }
    public Collection<String> getHeaders(String name) { 
        return header.get(name); 
    }
    public String[] getHeaderValues(String name) { return header.getAll(name); }
    public void setHeader(String name, String value) { header.set(name, value); }
    public void setIntHeader(String name, int value) {
        header.set(name, Integer.toString(value));
    }
    public void addHeader(String name, String value) { header.add(name, value); }
    public void addIntHeader(String name, int value) {
        header.add(name, Integer.toString(value));
    }
    public boolean containsHeader(String name) { return header.hasName(name); }
    public void addCookie(Cookie cookie) {
        String v = null;
        StringBuffer sb = new StringBuffer();
        sb.append(cookie.getName()).append("=").append(cookie.getValue());
        v = cookie.getComment();
        if (v != null) sb.append("; ").append("Comment=").append(v);
        v = cookie.getDomain();
        if (v != null) sb.append("; ").append("Domain=").append(v);
        v = cookie.getPath();
        if (v != null) sb.append("; ").append("Path=").append(v);
        int ver = cookie.getVersion();
        if (ver > 1) sb.append("; ").append("Version=")
                       .append(Integer.toString(ver));
        // if (cookie.isHttpOnly()) sb.append("; ").append("HttpOnly");

        header.add("Set-Cookie", sb.toString());
    }

    public void setStatus(int status) { 
        setStatus(status, null); 
    }
    public void setStatus(int status, String message) { 
        sc = status; 
        if (message == null) message = (sc == 200) ? "OK" : "Not OK";
        msg = message;
    }

    public void setContentLength(int length) {
        setIntHeader("Content-Length", length);
    }
    public void setContentType(String type) {
        setHeader("Content-Type", type);
    }
    public OutputStream getStream() { 
        throw new UnsupportedOperationException("TestResponse.getStream()");
    }
    public ServletResponse getResponse() { return this; }
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }
    public int getStatus() { return sc; }
    public int getContentLength() { 
        String v = getHeader("Content-Length");
        if (v == null) return -1;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ex) { }
        return -1; 
    }
    public String getContentType() { return header.getFirst("Content-Type"); }
    public Cookie[] getCookies() { return null; }
    public String encodeRedirectURL(String url) { 
        return URLEncoder.encode(url); 
    }

    public void close() throws IOException {
        PrintWriter rstrm = getWriter();
        if (rstrm != null) {
            rstrm.flush();
            rstrm.close();
        }
    }
    public void sendError(int status) throws IOException {
        sendError(status, null);
    }
    public void sendError(int status, String message) throws IOException {
        if (isCommitted())
            throw new IllegalStateException("Previous output already committed");
        if (out != null) out.clearBuffer();
        out = null;
        setStatus(status, message);
        PrintWriter err = getWriter();
        err.flush();
        err.close();
    }
    public void sendRedirect(String location) throws IOException {
        if (isCommitted())
            throw new IllegalStateException("Previous output already committed");

        setHeader("Location", location);

        if (out != null) out.clearBuffer();
        out = null;
        setStatus(SC_FOUND, "Redirecting");
        PrintWriter err = getWriter();
        err.flush();
        err.close();
    }

    // rest are null stubs
    /*
    public void setAppCommitted(boolean appCommitted) {}
    public boolean isAppCommitted() { return false; }
    public int getContentCount() { return -1; }
    public boolean getIncluded() { return false; }
    public void setIncluded(boolean included) {}
    public String getInfo() { return null; }
    public void setStream(OutputStream stream) {}
    public void setSuspended(boolean suspended) {}
    public boolean isSuspended() { return false; }
    public void setError() {}
    public ServletOutputStream createOutputStream() throws IOException {
        return null;
    }
    public void finishResponse() throws IOException {}
    public PrintWriter getReporter() { return null; }
    public void recycle() {}
    public void write(int b) throws IOException {}
    public void write(byte b[]) throws IOException {}
    public void write(byte b[], int off, int len) throws IOException {}
    */

    public String getCharacterEncoding() { return null; }
    public void setCharacterEncoding(String charEncoding) {}
    public int getBufferSize() { return -1; }
    public void reset() {}
    public void resetBuffer() {}
    public void setBufferSize(int size) {}
    public void setLocale(Locale locale) {}
    public void addDateHeader(String name, long value) {}
    public void setDateHeader(String name, long value) {}
    public Locale getLocale() { return null; }
    public String getMessage() { return null; }
    public void reset(int status, String message) {}
    public String encodeRedirectUrl(String url) { return null; }
    public String encodeURL(String url) { return null; }
    public String encodeUrl(String url) { return null; }
    public void sendAcknowledgement() throws IOException {}

    class MyWriter extends StringWriter {
        public void flush() {
            super.flush();
            StringBuffer sb = getBuffer();
            try {
                FileWriter w = new FileWriter(outf, true);
                w.write(sb.toString());
                w.close();
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            clearBuffer(sb);
        }
        public void clearBuffer() { clearBuffer(null); }
        public void clearBuffer(StringBuffer sb) {
            if (sb == null) sb = getBuffer();
            sb.delete(0,sb.length());
        }
    }

    public static void main(String[] args) {
        try {
            TestResponse tr = new TestResponse(args[0]);

            Cookie cook = new Cookie("session", "123456789abcdef");
            cook.setDomain("example.net");
            cook.setPath("/request");

            tr.addCookie(cook);
            // System.out.println(tr.getHeader("Set-Cookie"));

            PrintWriter pw = tr.getWriter();
            pw.flush();
            pw.close();

            BufferedReader rdr = new BufferedReader(new FileReader(args[0]));
            String line = null;
            while((line = rdr.readLine()) != null) 
                System.out.println(line);
        } catch (IOException ex) { 
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
