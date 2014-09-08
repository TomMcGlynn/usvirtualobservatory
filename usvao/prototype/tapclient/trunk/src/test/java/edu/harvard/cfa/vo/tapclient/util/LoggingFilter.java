package edu.harvard.cfa.vo.tapclient.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class LoggingFilter extends Filter {
    private final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.util.LogginFilter");
    public static final String ATTRIBUTE_NAME = "application/x-www-form-urlencoded";
    public LoggingFilter() {
	super();
    }
    
    public String description() {
	return "LoggingFilter";
    }
    
    public void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "doFilter");

	Headers requestHeaders = exchange.getRequestHeaders();
	for (String name: requestHeaders.keySet()) {
	    for (String value: requestHeaders.get(name)) {
		if (logger.isLoggable(Level.INFO)) 
		    logger.log(Level.INFO, (name+" -> "+value));
	    }
	}

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

	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, writer.toString());

	chain.doFilter(exchange);
    }
}
