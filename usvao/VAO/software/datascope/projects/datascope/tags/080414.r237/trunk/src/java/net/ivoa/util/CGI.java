package net.ivoa.util;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import static java.net.URLDecoder.decode;


/** Simple utility for parsing CGI parameters */
public class CGI {
    
    private HashMap<String, String[]> params = new HashMap<String, String[]>();
    public CGI() {
	parseParams();
    }
    
    public  HashMap<String, String[]> getParams() {
	return params;
    }
    
    protected void parseParams() {
	
	String type        = System.getenv("REQUEST_METHOD").toUpperCase();
	String queryString = null;
	if (type.equals("GET")) {
	    queryString = System.getenv("QUERY_STRING");
	} else {
	    try {
	        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
	        queryString = bf.readLine();
	    } catch (Exception e) {
		System.err.println("Unable to read standard input on POST");
	    }
	}
	
	if (queryString != null) {
	    String[] fields = queryString.split("\\&");
	    for (String field: fields) {
		decodeField(field);
	    }
	}
    }
    
    protected void decodeField(String field) {
	String[] elems = field.split("=", 2);
        try {
	    if (elems.length == 2) {
	        String key = decode(elems[0], "UTF-8");
	        String val = decode(elems[1], "UTF-8");
	        addField(key, val);
	    }
        } catch (Exception e) {	       
	    // This should be an unsupported encoding exception!
 	    throw new Error("Unexpected error:"+e);
        }
    }
    
    protected void addField(String key, String val) {
	if (params.containsKey(key)) {
	    String[] old = params.get(key);
	    String[] nw  = new String[old.length+1];
	    System.arraycopy(old, 0, nw, 0, old.length);
	    nw[old.length] = val;
	    params.put(key, nw);
	} else {
	    params.put(key, new String[]{val});
	}
    }
    
    public String value(String key) {
	if (params.containsKey(key)) {
	    return params.get(key)[0];
	} else {
	    return null;
	}
    }
    
    public String[] values(String key) {
	return params.get(key);
    }
    
    public int count(String key) {
	if (params.containsKey(key)) {
	    return params.get(key).length;
	} else {
	    return 0;
	}
    }
    
    public String[] keys() {
	return params.keySet().toArray(new String[0]);
    }
}
