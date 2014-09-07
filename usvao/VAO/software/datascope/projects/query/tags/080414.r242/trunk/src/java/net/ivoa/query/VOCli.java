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

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;


/** This class takes the parameters given and
*  invokes the VOClient tools to generate a
*  single VOTable.
*/

public class VOCli {

	/** The CGI fields set for this request */
	private CGI cgi;
    
	public static void main(String[] args) throws Exception {
		try {
			new VOCli(args).run();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public VOCli(String[] args) {}

	public void run() throws Exception {
		cgi = new CGI();
		String position=cgi.value("POSITION");
		String vLocal  = cgi.value("viewLocal");
		String vURL    = cgi.value("viewURL");
		if ( vLocal != null && vLocal.length() > 0 ) {
			System.out.println("Content-type: text/xml\n");
			System.out.println(cgi.value("viewLocal").trim());
//			System.out.println(addValue( new StreamSource(new StringReader(cgi.value("viewLocal").trim())) ) );
			System.out.close();
		} else if (vURL != null  && vURL.length() > 0) {
			System.out.println("Content-type: text/xml\n");
			URL votable = new URL(vURL);
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
					votable.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();
			System.out.close();
//		} else if (position != null  && position.length() > 0 ) {
		} else if (position != null  && position.length() > 0 && position.indexOf(',')<0 ) {
			processSingle();
		} else {
			processMulti();
		}
	}

	void processSingle() throws Exception {
		String position = cgi.value("POSITION");
		String radius   = cgi.value("RADIUS");
		if (radius == null  || radius.length() == 0) {
			radius = Settings.get("DefaultRadius");
		}
		String units    = cgi.value("units");
		if ( units != null && units.length() > 0 ) {
			Float r = new Float(radius);
			if ( units.equals("arcmin") ) {
				r /= 60;
			} else if ( units.equals("arcsec") ) {
				r /= 3600;
			} 
			radius = r.toString();
		}
		String id       = cgi.value("IVOID");
		doSingle(position, radius, id);
	}

/*
	This version of doSingle is a modified copy of doMulti as it 
	will generate a valid empty votable if there are no results
	Unfortunately, it also creates a "rec_id" column which some may find annoying,
	so I've created a SinglerXSL to strip off this column.
*/
	void doSingle(String position, String radius, String id) throws Exception {
		String cmd = Settings.get("MultiTargetCmd");
		cmd += verbosity()+" "+"-rd "+radius +" "+id;

		//	Start the vodata process ...
		ProcessBuilder pb      = new ProcessBuilder(cmd.split(" "));
		addPath(pb);
		pb.directory(new File("/tmp"));
		Process      proc = pb.start();

		//	Send the sources to the vodata command ...
		OutputStream os = proc.getOutputStream();
		os.write(position.getBytes());
		os.flush();
		os.close();

		// Now transform the output...
		System.out.println("Content-type: text/xml\n");
		StreamSource input = new StreamSource(proc.getInputStream());
		StreamSource xsl  = XSLTrans.getSource(Settings.get("SinglerXSL"));
		StreamResult out   = new StreamResult(System.out);
		new XSLTrans().transform(xsl, input, out);
		System.out.close();
		proc.destroy();	// added these because I noticed many vodata processes on heasarcdev
	}





	void processMulti() throws Exception {
		StreamSource input = null;
		String sources = cgi.value("sources");
		String sourcesURL = cgi.value("sourcesURL");
		String position = cgi.value("POSITION");
		int limit = 5;
		String cgilimit = cgi.value("limit");
		if ( cgilimit != null ){
			limit = new Integer(cgi.value("limit"));
		}
		String positions = "";
		// Note bug that requires us to check for length > 1
		if (sources != null  && sources.length() > 1) {
//			input = new StreamSource(new StringReader(sources));
			positions = getSources( new StreamSource(new StringReader(sources)), limit);
		} else if (sourcesURL != null && sourcesURL.length() > 0) {
//			input = new StreamSource(sourcesURL);
			positions = getSources( new StreamSource(sourcesURL), limit );
		} else if (position != null && position.length() > 0) {
			positions = position.replaceAll("\\s*,\\s*","\n");
		} else {
			new ShowError("query").fail("No position inputs"+position);
			System.exit(-1);
		}
		String id     = cgi.value("IVOID");
		String radius = cgi.value("RADIUS");
		if (radius == null || radius.length() == 0) {
			radius = Settings.get("DefaultRadius");
		}
		doMulti(positions, radius, id);
	}

	private void addPath(ProcessBuilder pb) {
		String cliDir = Settings.get("VOClientDir");
		if (cliDir != null && cliDir.length() > 0) {
			String path   = pb.environment().get("PATH");
			path = cliDir+":"+path;
			pb.environment().put("PATH", path);
		}
	}

	void doMulti(String sources, String radius, String id) throws Exception {
		String cmd = Settings.get("MultiTargetCmd");
		cmd += verbosity()+" "+"-rd "+radius +" "+id;

		//	Start the vodata process ...
		ProcessBuilder pb      = new ProcessBuilder(cmd.split(" "));
		addPath(pb);
		pb.directory(new File("/tmp"));
		Process      proc = pb.start();

		//	Send the sources to the vodata command ...
		OutputStream os = proc.getOutputStream();
		os.write(sources.getBytes());
		os.flush();
		os.close();

		System.out.println("Content-type: text/xml\n");

		// Now transform the output...
		StreamSource input = new StreamSource(proc.getInputStream());
		StreamSource xsl  = XSLTrans.getSource(Settings.get("CombinerXSL"));

		StreamResult out   = new StreamResult(System.out);
		new XSLTrans().transform(xsl, input, out);
/*
		StringWriter sw   = new StringWriter();
		StreamResult out  = new StreamResult(sw);
		try {
			new XSLTrans().transform(xsl, input, out);
		} catch (Exception e) { 
			//	Initially assuming that input is empty
			//	Allow to be empty and force addValue to deal with it
		}
		sw.close();
		System.out.println(addValue(new StreamSource( new StringReader(sw.toString()) ) ) );
*/

		System.out.close();
		proc.destroy();	// added these because I noticed many vodata processes on heasarcdev
	}
    
	String verbosity() {
		if (cgi.value("VERBOSITY") != null) {
			return " -vv";
		} else {
			return "";
		}
	}

	String getSources(StreamSource input, int count) throws Exception {
		StreamSource xsl  = XSLTrans.getSource(Settings.get("PositionsXSL"));
		StringWriter sw   = new StringWriter();
		StreamResult out  = new StreamResult(sw);
		new XSLTrans().transform(xsl, input, out);
		sw.close();
//		return sw.toString();


		// only allow a limited number of sources
		// for the time being as this could take
		// quite a while.
		String[] all = sw.toString().split("\n");
		int i = 0;
		String selected = "";
		while ( i < count && i < all.length ) {
			selected += all[i++]+"\n";
		}
		return selected;
	}
}
/*
Old stuff

	String addValue(StreamSource input) throws Exception {
			StringWriter sw    = new StringWriter();
			StreamResult out   = new StreamResult(sw);
			StreamSource xsl   = XSLTrans.getSource(Settings.get("ValueXSL"));
			try {
				new XSLTrans().transform(xsl, input, out);
			} catch (Exception e) { 
				//	Initially assuming that input is empty
				// Create an empty VOTABLE for graceful exit
				//	Not the best way, but it works.
				sw.write("<?xml version='1.0' encoding='UTF-8'?>"+
					"<VOTABLE xmlns:vo='http://www.ivoa.net/xml/VOTable/v1.1'>"+
					"<RESOURCE>"+
					"<TABLE>"+
					"<FIELD name='No_data' type='char'>"+
					"<DESCRIPTION></DESCRIPTION>"+
					"</FIELD>"+
					"<DATA>"+
					"<TABLEDATA/>"+
					"</DATA>"+
					"</TABLE>"+
					"</RESOURCE>"+
					"</VOTABLE>"+
				"");
			}
			sw.close();
			return sw.toString();
	}


	void ORIGINALdoSingle(String position, String radius, String id) throws Exception {
		String cmd = Settings.get("SingleTargetCmd");
		cmd += verbosity()+" "+id+" "+position+" "+radius;
		ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
		pb.redirectErrorStream(true);
		addPath(pb);
		pb.directory(new File("/tmp"));
		System.out.println("Content-type: text/xml\n");
		Process proc = pb.start();

// How to determine if output is empty?
// If it is, what to do?
		InputStream inp = proc.getInputStream();
		byte[]      buf = new byte[32768];
		int len;
		while ( (len = inp.read(buf)) > 0) {
			System.out.write(buf, 0, len);
		}
		proc.destroy();	// added these because I noticed many vodata processes on heasarcdev
	}






*/
