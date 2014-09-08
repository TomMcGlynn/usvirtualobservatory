package edu.harvard.cfa.vo.tapclient.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class ParametersFilter extends Filter {
    private final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.util.ParametersFilter");
    public static final String ATTRIBUTE_NAME = "ParametersFilter.ATTRIBUTE_NAME";

    public ParametersFilter() {
	super();
    }
    
    public String description() {
	return "ParametersFilter";
    }
    
    public void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "doFilter");

	Headers parameters = new Headers();
	exchange.setAttribute(ATTRIBUTE_NAME, parameters);

	try {
	    if (exchange != null) {
		parseURIQuery(exchange, parameters);
		parseForm(exchange, parameters);
	    }
	} catch (Throwable ex) {
	    if (logger.isLoggable(Level.WARNING))
		logger.log(Level.WARNING, "ParametersFilter failed: "+exchange.getRequestURI(), ex);
	    
	} finally {
	    chain.doFilter(exchange);

	}
    }

    protected void parseURIQuery(HttpExchange exchange, Headers parameters) throws IOException {
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "parseURIQuery");

	URI requestURI = exchange.getRequestURI();
	if (requestURI != null) {
	    String requestRawQuery = requestURI.getRawQuery();
	    if (requestRawQuery != null && ! requestRawQuery.isEmpty()) {
		String[] pairs = requestRawQuery.split("&");
		for (String pair: pairs) {
		    String[] keywordValue = pair.split("=");
		    if (keywordValue.length != 2) {
			throw new IllegalArgumentException();
		    }
		    
		    parameters.add(keywordValue[0], keywordValue[1]);
		}
	    }
	}
    }

    protected void parseForm(HttpExchange exchange, Headers parameters) throws UnsupportedEncodingException, IOException {
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "parseForm");

	Headers requestHeaders = exchange.getRequestHeaders();
	if (requestHeaders != null) {
	    String requestContentType = requestHeaders.getFirst("Content-Type");
	    if (requestContentType != null && "application/x-www-form-urlencoded".equals(requestContentType)) {
		Reader requestBody = null;
		StringWriter writer = new StringWriter();
		try {
		    requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
		    
		    int c = -1;
		    while ((c = requestBody.read()) != -1) {
			writer.write(c);
		    }
		} finally {
		    if (requestBody != null) {
			requestBody.close();
		    }
		    
		    exchange.setStreams(new ByteArrayInputStream(writer.toString().getBytes()), exchange.getResponseBody());
		}
		
		if (! writer.toString().isEmpty()) {
		    String[] pairs = writer.toString().split("&");
		    for (String pair: pairs) {
			String[] keywordValue = pair.split("=");
			if (keywordValue.length != 2) {
			    throw new IllegalArgumentException();
			}
			
			parameters.add(keywordValue[0], keywordValue[1]);
		    }
		}
	    }
	}
    }
}
