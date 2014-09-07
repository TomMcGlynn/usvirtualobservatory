package net.ivoa.query;

import net.ivoa.util.CGI;
import net.ivoa.util.ShowError;
import net.ivoa.util.Settings;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.StringReader;

/** This class takes the parameters given and
 *  invokes the VOClient tools to generate a
 *  single VOTable.
 */

public class VOCli {
    
    public static void main(String[] args) throws Exception {
	try {
	    new VOCli(args).run();
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	}
    }
    
    public VOCli(String[] args) {}
    
    public void run() throws Exception {
    
	CGI cgi = new CGI();
	
	
	String position=cgi.value("POSITION");
	if (position != null  && position.length() > 0) {
	    processSingle(cgi);
	} else {
	    processMulti(cgi);
	}
	
    }
    
    void processSingle(CGI cgi) throws Exception {
	
	String position = cgi.value("POSITION");
	String radius   = cgi.value("RADIUS");
	if (radius == null  || radius.length() == 0) {
	    radius = Settings.get("DefaultRadius");
	}
	String id       = cgi.value("IVOID");
	
	doSingle(position, radius, id);
    }
    
    void doSingle(String position, String radius, String id) throws Exception {
	
	String cmd = Settings.get("SingleTargetCmd");
	
	cmd += " "+id+" "+position+" "+radius;
	
	System.err.println("CMD is:"+cmd);
	ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
	addPath(pb);
	pb.directory(new File("/tmp"));
	
	System.out.println("Content-type: text/xml\n");
	
	Process proc = pb.start();
	
	InputStream inp = proc.getInputStream();
	byte[]      buf = new byte[32768];
	int len;
	while ( (len = inp.read(buf)) > 0) {
	    System.out.write(buf, 0, len);
	}
    }
    void processMulti(CGI cgi) throws Exception {
	
	StreamSource input = null;
	String sources = cgi.value("sources");
	String sourcesURL = cgi.value("sourcesURL");
	
	// Note bug that requires us to check for length > 1
	if (sources != null  && sources.length() > 1) {
	    input = new StreamSource(new StringReader(sources));
	} else if (sourcesURL != null && sourcesURL.length() > 0) {
	    input = new StreamSource(sourcesURL);
	} else {
	    new ShowError("query").fail("No position inputs");
	    System.exit(-1);
	}
	String id     = cgi.value("IVOID");
	String radius = cgi.value("RADIUS");
	
	
	if (radius == null || radius.length() == 0) {
	    radius = Settings.get("DefaultRadius");
	}
	doMulti(input, radius, id);
    }
    
    private void addPath(ProcessBuilder pb) {
	String cliDir = Settings.get("VOClientDir");
	if (cliDir != null && cliDir.length() > 0) {
	    String path   = pb.environment().get("PATH");
	    path = cliDir+":"+path;
	    pb.environment().put("PATH", path);
	}
    }
    
    void doMulti(StreamSource input, String radius, String id) throws Exception {
	
	String sources = getSources(input);
	
	String cmd = Settings.get("MultiTargetCmd");
	
	cmd += " -rd "+radius +" "+id;
	
	System.out.println("Content-type: text/xml\n");
	
	ProcessBuilder pb      = new ProcessBuilder(cmd.split(" "));
	addPath(pb);
	pb.directory(new File("/tmp"));
	
	Process      proc = pb.start();
	
        OutputStream os = proc.getOutputStream();
	os.write(sources.getBytes());
	os.close();
	
	// Now transform the output...
	// 
	input = new StreamSource(proc.getInputStream());
	
	StreamSource xsl   = XSLTrans.getSource(Settings.get("combinerXsl"));
	StreamResult out   = new StreamResult(System.out);
	
	new XSLTrans().transform(xsl, input, out);
	System.out.close();
    }
    
    String getSources(StreamSource input) throws Exception {
	
	String xslFile = Settings.get("positionsXsl");
	StreamSource xsl  = XSLTrans.getSource(xslFile);
	StringWriter sw   = new StringWriter();
	StreamResult out  = new StreamResult(sw);
	
	new XSLTrans().transform(xsl, input, out);
	sw.close();
	return sw.toString();
    }
					   
}
