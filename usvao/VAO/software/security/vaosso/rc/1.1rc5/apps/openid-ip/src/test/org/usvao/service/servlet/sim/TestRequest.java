/*
 * Adapted from org.apache.catalina.core.DummyRequest.java:
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * An HTTPServletRequest implementation used for unit testing
 *
 * Based on org.apache.catalina.core.DummyRequest.java by 
 * @author Remy Maucherat
 */
@SuppressWarnings("deprecation")
public class TestRequest implements HttpServletRequest {

    TestSession sess = new TestSession();
    public static String defaultAppURL = "http://example.net/app";
    public static String defaultReferer = "http://example.org/portal";

    TestRequest() {
    }

    /**
     * Initialize a GET request to a particular servlet URL and HTTP session.
     * This sets a default base URL for the web app as its value usually does 
     * not matter for testing purposes.
     * @param servletPathInfo  the path that invokes the servlet.  The first
     *                           path item (i.e. before the first /) is taken
     *                           to be the path to the servlet and the rest
     *                           is that pathInfo passed to the servlet.  
     * @param queryString      the query string passed to the servlet (i.e. 
     *                           the string after "?")
     * @param session          the session object to use for this request
     */
    public TestRequest(String servletPathInfo, String queryString,
                       HttpSession session) 
    {
        this(TestRequest.defaultAppURL, servletPathInfo, "GET", 
             queryString, session);
    }

    /**
     * Initialize the request to a particular servlet URL and HTTP method.
     * This sets a default base URL for the web app as its value usually does 
     * not matter for testing purposes.
     * @param servletPathInfo  the path that invokes the servlet.  The first
     *                           path item (i.e. before the first /) is taken
     *                           to be the path to the servlet and the rest
     *                           is that pathInfo passed to the servlet.  
     * @param httpmethod       the HTTP method name (typically either "GET" 
     *                           or "POST")
     * @param queryString      the query string passed to the servlet (i.e. 
     *                           the string after "?")
     */
    public TestRequest(String servletPathInfo, String httpmethod, 
                       String queryString) 
    {
        this(TestRequest.defaultAppURL, servletPathInfo, httpmethod, 
             queryString, null);
    }

    /**
     * Initialize the request to a particular servlet URL and HTTP method
     * @param appURL           the URL (i.e. starting with "http:") of the 
     *                           web app containing the servlet being tested.
     * @param servletPathInfo  the path that invokes the servlet.  The first
     *                           path item (i.e. before the first /) is taken
     *                           to be the path to the servlet and the rest
     *                           is that pathInfo passed to the servlet.  
     * @param httpmethod       the HTTP method name (typically either "GET" 
     *                           or "POST")
     * @param queryString      the query string passed to the servlet (i.e. 
     *                           the string after "?")
     */
    public TestRequest(String appURL, String servletPathInfo,
                       String httpmethod, String queryString) 
    {
        this(appURL, servletPathInfo, httpmethod, queryString, null);
    }

    /**
     * Initialize the request to a particular servlet URL and HTTP method.
     * Use this to constructor to initiate with an existing session instance.
     * @param appURL           the URL (i.e. starting with "http:") of the 
     *                           web app containing the servlet being tested.
     * @param servletPathInfo  the path that invokes the servlet.  The first
     *                           path item (i.e. before the first /) is taken
     *                           to be the path to the servlet and the rest
     *                           is that pathInfo passed to the servlet.  
     * @param httpmethod       the HTTP method name (typically either "GET" 
     *                           or "POST")
     * @param queryString      the query string passed to the servlet (i.e. 
     *                           the string after "?")
     * @param session          the session object to use for this request
     */
    public TestRequest(String appURL, String servletPathInfo,
                       String httpmethod, String queryString,
                       HttpSession session) 
    {
        _meth = httpmethod;
        _sess = session;
        try {
            URL url = new URL(appURL);
            _schm = url.getProtocol();
            port = url.getPort();
            if (port < 0) port = url.getDefaultPort();
            _host = url.getHost();
            _cntxPath = url.getPath();
        }
        catch (MalformedURLException ex) {
            throw new IllegalArgumentException("bad context URL: "+appURL);
        }
        while (servletPathInfo.length() > 0 && servletPathInfo.charAt(0) == '/')
            servletPathInfo = servletPathInfo.substring(1);
        String[] parts = servletPathInfo.split("/", 2);
        _servletPath = "/"+parts[0];
        _pathInfo = (parts.length > 1) ? "/"+parts[1] : "";
        setQueryString(queryString);

        header.add("Referer", defaultReferer);
        header.add("Host", _host);
    }

    protected String _schm, _host, _cntxPath, _servletPath, _pathInfo, _meth, 
        _qs;
    protected int port = 443;
    MultiProperties header = new MultiProperties();
    MultiProperties params = new MultiProperties();
    HttpSession _sess = null;

    protected FilterChain filterChain = null;
    
    public String getContextPath() {
        return _cntxPath;
    }
    public void setContextPath(String path) { _cntxPath = path; }

    public ServletRequest getRequest() {
        return (this);
    }

    public FilterChain getFilterChain() {
        return (this.filterChain);
    }

    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

    public String getQueryString() {
        return _qs;
    }

    public void setQueryString(String query) {
        if (query == null) query = "";
        _qs = query;

        params.clear();
        StringTokenizer tok = new StringTokenizer(_qs, "&");
        String val = null;
        String[] pair = null;
        while(tok.hasMoreTokens()) {
            val = tok.nextToken();
            pair = val.split("=", 2);
            if (pair.length == 1) 
                params.add(pair[0], "");
            else 
                params.add(pair[0], pair[1]);
        }
    }

    public String getParameter(String name) { 
        return params.getFirst(name);
    }
    public Map getParameterMap() { 
        return params.map();
    }
    public Enumeration getParameterNames() { return params.names(); }
    public String[] getParameterValues(String name) { 
        return params.getAll(name); 
    }
    public void addParameter(String name, String values[]) {
        params.add(name, values);
    }


    public String getPathInfo() {
        return _pathInfo;
    }

    public void setPathInfo(String path) {
        _pathInfo = (path == null) ? "" : path;
    }

    public String getServletPath() {
        return _servletPath;
    }

    public void setServletPath(String path) {
        _servletPath = path;
    }

    public void setScheme(String scheme) { _schm = scheme; }
    public String getScheme() { return _schm; }
    public void setServerName(String name) { _host = name; }
    public String getServerName() { return _host; }
    public void setServerPort(int port) { this.port = port; }
    public int getServerPort() { return port; }
    public void setMethod(String method) { _meth = method; }
    public String getMethod() { return _meth; }

    public HttpSession getSession() { return getSession(false); }
    public HttpSession getSession(boolean create) { 
        if (create || _sess == null) _sess = new TestSession();
        return _sess;
    }

    public String getHeader(String name) { return header.getFirst(name); }
    public Enumeration getHeaders(String name) { return header.values(name); }
    public Enumeration getHeaderNames() { return header.names(); }
    public void addHeader(String name, String val) { header.add(name, val); }
    public void setHeader(String name, String val) { header.set(name, val); }

    public Cookie[] getCookies() { 
        String[] cstrs = header.getAll("Cookie");
        if (cstrs == null) return new Cookie[0];
        Cookie[] out = new Cookie[cstrs.length];
        HttpCookie parsed = null;
        for(int i=0; i < cstrs.length; i++) {
            parsed = HttpCookie.parse(cstrs[i]).get(0);
            out[i] = new Cookie(parsed.getName(), parsed.getValue());
            // out[i].setMaxAge(parsed.getMaxAge());
            if (parsed.getPath() != null) out[i].setPath(parsed.getPath());
            if (parsed.getDomain() != null) 
                out[i].setDomain(parsed.getDomain());
        }

        return out;
    }

    //  Rest are null stubs

    public String getDecodedRequestURI() { return null; }
    public String getAuthorization() { return null; }
    public void setAuthorization(String authorization) {}
    public String getInfo() { return null; }
    public Socket getSocket() { return null; }
    public void setSocket(Socket socket) {}
    public InputStream getStream() { return null; }
    public void setStream(InputStream input) {}
    public void addLocale(Locale locale) {}
    public ServletInputStream createInputStream() throws IOException {
        return null;
    }
    public void finishRequest() throws IOException {}
    public Object getNote(String name) { return null; }
    public Iterator getNoteNames() { return null; }
    public void removeNote(String name) {}
    public void setContentType(String type) {}
    public void setNote(String name, Object value) {}
    public void setProtocol(String protocol) {}
    public void setRemoteAddr(String remoteAddr) {}
    public void setRemoteHost(String remoteHost) {}
    public Object getAttribute(String name) { return null; }
    public Enumeration getAttributeNames() { return null; }
    public String getCharacterEncoding() { return null; }
    public int getContentLength() { return -1; }
    public void setContentLength(int length) {}
    public String getContentType() { return null; }
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }
    public Locale getLocale() { return null; }
    public Enumeration getLocales() { return null; }
    public String getProtocol() { return "HTTP/1.1"; }
    public BufferedReader getReader() throws IOException { return null; }
    public String getRealPath(String path) { return null; }
    public String getRemoteAddr() { return "127.0.0.1"; }
    public String getRemoteHost() { return "localhost"; }
    public boolean isSecure() { return false; }
    public void removeAttribute(String name) {}
    public void setAttribute(String name, Object value) {}
    public void setCharacterEncoding(String enc)
        throws UnsupportedEncodingException {}
    public void addCookie(Cookie cookie) {}
    public void clearCookies() {}
    public void clearHeaders() {}
    public void clearLocales() {}
    public void clearParameters() {}
    public void recycle() {}
    public void setAuthType(String authType) {}
    public void setRequestedSessionCookie(boolean flag) {}
    public void setRequestedSessionId(String id) {}
    public void setRequestedSessionURL(boolean flag) {}
    public void setRequestURI(String uri) {}
    public void setSecure(boolean secure) {}
    public void setUserPrincipal(Principal principal) {}
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }
    public String getAuthType() { return null; }
    public long getDateHeader(String name) { return -1; }
    public int getIntHeader(String name) { return -1; }
    public String getPathTranslated() { return null; }
    public String getRemoteUser() { return null; }
    public String getRequestedSessionId() { return null; }
    public boolean isRequestedSessionIdFromCookie() { return false; }
    public boolean isRequestedSessionIdFromURL() { return false; }
    public boolean isRequestedSessionIdFromUrl() { return false; }
    public boolean isRequestedSessionIdValid() { return false; }
    public boolean isUserInRole(String role) { return false; }
    public Principal getUserPrincipal() { return null; }
    public String getLocalAddr() { return null; }    
    public String getLocalName() { return null; }
    public int getLocalPort() { return -1; }
    public int getRemotePort() { return -1; }

    StringBuffer getServerURL() {
        StringBuffer out = new StringBuffer(this.getScheme());
        out.append("://");
        String s = this.getServerName();
        if (s != null) {
            out.append(s);
        } else {
            out.append(this.getServerName());
            int p = this.getServerPort();
            if (p != 80 && p != 443) 
                out.append(":").append(Integer.toString(p));
        }
        return out;
    }

    StringBuffer getContextURL() {
        return getServerURL().append(getContextPath());
    }
    StringBuffer getServletURL() {
        return getContextURL().append(getServletPath());
    }
    public StringBuffer getRequestURL() {
        return getServletURL().append(getPathInfo());
    }
    public String getRequestURI() {
        return getRequestURL().substring(getServerURL().length());
    }

}

