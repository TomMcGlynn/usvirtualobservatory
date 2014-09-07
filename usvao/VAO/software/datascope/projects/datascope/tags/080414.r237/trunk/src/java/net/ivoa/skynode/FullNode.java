package net.ivoa.skynode;

/** This class implments the capabilities of a full SkyNode.
 *  While this supports a SOAP like interface it directly
 *  parses the SOAP inputs and outputs so that it does not
 *  depend on AXIS or any other Servlet container.
 *  It expects to be installed as a CGI Script.
 */


import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class FullNode {
    
    static String method                  = System.getenv("REQUEST_METHOD");
    static String qs                      = System.getenv("QUERY_STRING");
    static long   startTime;
    
    
    /** Run the request.
     *  There are no arguments.  Data is passed in via standard input for
     *  SOAP and POST queries and via an environment variable for GET.
     *  Only SOAP inputs are currently supported.
     */
    public static void main(String[] args)  {
	startTime = new java.util.Date().getTime();
	log("Request Initiated:"+startTime);
	
	SkyNode.update();
	try {
	    if (!doWSDL()) {
	        doSOAP();
	    }
	} catch (Exception e) {
	    log("Request caught exception: "+e);
	    e.printStackTrace(System.err);
	    throw new Error("Exception in processing");
	}
	log("Request Completed: start"+startTime+" Duration:"+(new java.util.Date().getTime()-startTime)/1000.);
    }
    
    /**  Send back a copy of the WSDL describing this service.
     */
    public static boolean doWSDL() throws Exception  {
	
	if (method != null && qs != null &&
	    method.toUpperCase().equals("GET") &&
	    qs.toUpperCase().equals("WSDL")) {
	
	    System.out.println("Content-type: text/xml\n");
	    BufferedReader br = new BufferedReader(new FileReader(new File(SkyNode.WSDLFile)));
	    String line;
	    
	    while ( (line=br.readLine()) != null) {
		System.out.print(line);
	    }
	    return true;
	}
	return false;
    }
    
    /** Log a message.  Currently this uses the file /tmp/sn.log. */
    public static void log(String data) {
	try {
            java.io.PrintStream log = new java.io.PrintStream(new java.io.FileOutputStream("/tmp/sn.log", true));
	    log.println(new java.util.Date()+":"+data);
	    log.close();
	} catch (Exception e) {
	    System.err.println("Logging error:"+e);
	}
    }
    
    
    /** Handle a SOAP request */
    public static void doSOAP() throws Exception {
	SoapParser sp = new SoapParser();
	sp.run();
    }
    
    /** Print out a file to standard output */
    public static void printFile(String name) {
	
	File f = new File(name);
	
	if (!f.exists()) {
	    return;
	}
	BufferedReader bf = null;
	try {
	    bf = new BufferedReader(new FileReader(f));
	    String line;
	    while ( (line = bf.readLine()) != null) {
		System.out.println(line);
	    }
	} catch (Exception e) {
	} finally {
	    if (bf != null) {
		try {
		    bf.close();
		} catch (Exception e) {
		}
	    }
	}
    }
}
