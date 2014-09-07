package net.ivoa.datascope;

import net.ivoa.util.ShowError;
import net.ivoa.util.CGI;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.nvo.Header;
import net.nvo.Footer;

import skyview.request.SourceCoordinates;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

/**
 * Provide a response to a request. This is the main class for the response to
 * the DataScope request.
 */
public class Response {

	private CGI cgi = new CGI();
	private String target;

	private double ra;
	private double dec;
	private double size;
	private double errorCircle = -1;
	private boolean skipDataCache = false;
	private boolean skipLog = false;

	private CacheFinder cf = null;
	private Scanner scan = null;
	private String jobDir;
	
	private Map<String, String> jobParams = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {
		Response resp = new Response();
		if (args.length > 0) {
			resp.respondService(args);
		} else {
			resp.respondWebpage();
		}
		System.exit(0);
	}

	public void respondService(String[] args) throws Exception {
		jobDir = args[0];
		parseParameters();
		
		try {
			cf = new CacheFinder();
		} catch (Exception e) {
			throw new Exception("Unable to find/create cache for request", e);
		}
		
		try {
			File cacheDirFile = new File(jobDir + "/cachedir.txt");
			PrintWriter out =
			    new PrintWriter(
			        new BufferedWriter(
			            new FileWriter(cacheDirFile) ) );
			out.println(cf.getFile().getName());
			out.close();
						
			if (cf.existed()) {
				return;
			}

			setPhase("EXECUTING");
			setTimeStamp("start");
					
			runScanner();
			
			setPhase("COMPLETED");
			setTimeStamp("end");
		} catch (Exception e) {
			File errorFile = new File(cf.getCacheHome() + "/error.txt");
			try {
				PrintWriter out =
				    new PrintWriter(
				        new BufferedWriter(
				            new FileWriter(errorFile) ) );
				e.printStackTrace(out);
				out.close();
				setPhase("COMPLETED");
				setTimeStamp("end");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
    }
	
	private void parseParameters() throws Exception {
		// parse the xml parameter list
		String paramFileName = jobDir + "/parameters.xml";
		File paramFile = new File(paramFileName);
		if (!paramFile.exists()) {
			throw new Exception("File " + paramFileName + " not found.");
		}

		SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
		BufferedReader is = new BufferedReader(
				new InputStreamReader(new FileInputStream(paramFile), "ISO-8859-1"));
		sp.parse(new InputSource(is), new paramParseCallBack());
		is.close();	
		
		ra = Double.parseDouble(jobParams.get("RA"));
		dec = Double.parseDouble(jobParams.get("DEC"));
		size = Double.parseDouble(jobParams.get("RADIUS"));

		Settings.put("ra", "" + ra);
		Settings.put("dec", "" + dec);
		Settings.put("size", "" + size);
		
		if( jobParams.containsKey("SERVICES") ){
			Settings.put("SERVICES", jobParams.get("SERVICES"));
		}
		
		String skipCache = jobParams.get("SKIPCACHE");
		if( skipCache != null && skipCache.matches("(?i)(yes|true)") ){
			skipDataCache = true;
		}
		Settings.put("skipDataCache", "" + skipDataCache);
		Settings.put("skipLog", "" + skipLog);
	}
	
	private void setPhase(String phase) throws Exception {
		File phaseFile = new File(cf.getCacheHome() + "/phase.txt");
		PrintWriter out = new PrintWriter(
							new BufferedWriter(
								new FileWriter(phaseFile) ) );
		out.print(phase);
		out.close();	
	}
	
	private void setTimeStamp(String type) throws Exception {
		File timeFile = new File(cf.getCacheHome() + "/timeline.xml");

		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		if( type.equals("start") ){
			PrintWriter out = new PrintWriter(
								new BufferedWriter(
										new FileWriter(timeFile, false) ) );
			out.println("<uws:startTime>" + format.format(now) + "</uws:startTime>");
			out.close();
		}else if( type.equals("end") ){
			PrintWriter out =
				new PrintWriter(
						new BufferedWriter(
				            new FileWriter(timeFile, true) ) );

			out.println("<uws:endTime>" + format.format(now) + "</uws:endTime>");
			out.close();
		}
	}

	public void respondWebpage() {
		try {
			parseInputs();
			
			cf = new CacheFinder();

			printResponse();

			if (cf.existed()) {
				return;
			}

			setPhase("EXECUTING");
			setTimeStamp("start");

			runScanner();
			
			setPhase("COMPLETED");
			setTimeStamp("end");		
		} catch (Exception e) {
			Header hdr = new Header();
			hdr.setTitle("DataScope Error");
			hdr.setBannerTitle("DataScope Error");
			hdr.setOnLoad("window.focus()");

			Header.printHTTP(System.out);
			hdr.printHTMLHeader(System.out);
			hdr.printBanner(System.out);
			System.out.println("<script language=JavaScript>");
			// System.out.println("alert('Window is:'+window)");
			System.out.println("window.focus()");
			System.out.println("</script>");
			
			System.out.println("<h2> DataScope Error </h2><pre>");
			e.printStackTrace(System.out);
			System.out.println("</pre>");
			
			new Footer().print(System.out);
			System.exit(0);

			File errorFile = new File(cf.getCacheHome() + "/error.txt");
			try {
				PrintWriter out =
				    new PrintWriter(
				        new BufferedWriter(
				            new FileWriter(errorFile) ) );
				e.printStackTrace(out);
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void runScanner() throws Exception {
		// This will kill everything after a time...
		Runnable killer = new Runnable() {
			public void run() {
				try {
					Thread.sleep(1200000);
				} catch (InterruptedException e) {
				}
				if(scan != null){
					scan.cleanupErrors("Datascope timed out.");
				}
				try {
					setPhase("COMPLETED");
					setTimeStamp("end");
				} catch (Exception e) {
				}
				System.exit(0);
			}
		};
		new Thread(killer).start();

		try {
			scan = new Scanner(cf.getCacheHome());
			DS.setLogLocation(cf.getCacheHome());
			scan.setup(ra, dec, size);
		} catch (Exception e) {
			throw new Exception("Error initiating scan", e);
		}
		try {
			System.in.close();
		} catch (Exception e) {
		}
		System.out.close();
		System.err.close();
		try {
			logQuery();
		} catch (Exception e) {
			DS.log("Query log error:" + e);
		}

		try {
			scan.scan();
		} catch (Exception e) {
			DS.log("Scanner error: " + e);
		}
	}

	private void parseInputs() throws Exception {
		for (String key : cgi.keys()) {
			String val = cgi.value(key).trim();
			if (val != null && val.length() > 0) {
				// Need to know which resources to
				// check if result is cached, so we download
				// the resource list immediately.
				//if (key.equalsIgnoreCase("services")) {
				//	try {
				//		String resources = Service.get(val);
				//		key = "resources";
				//		val = resources;
				//	} catch (Exception e) {
				//		// Just ignore it, we'll go with everything.
				//		continue;
				//	}
				//}
				Settings.put(key, val);
			}
		}

		target = cgi.value("position");

		if (target == null) {
			throw new Exception("No target");
		}
		Settings.put("target", target);

		String ecirc = cgi.value("errorcircle");
		if (ecirc != null) {
			try {
				errorCircle = Double.parseDouble(ecirc);
			} catch (Exception e) {
				throw new Exception("Invalid error circle radius:" + ecirc);
			}
		} else {
			Settings.put("ErrorCircle", "" + -1);
		}

		skipDataCache = cgi.count("skipcache") > 0;
		skipLog = cgi.count("skiplog") > 0;

		Settings.put("skipDataCache", "" + skipDataCache);
		Settings.put("skipLog", "" + skipLog);

		String sizeStr = cgi.value("size");
		if (sizeStr != null) {
			try {
				size = Double.parseDouble(sizeStr);
			} catch (Exception e) {
				throw new Exception("Invalid size:"+size, e);
			}
		} else {
			size = DS.getDefaultSize();
		}

		Settings.put("size", "" + size);

		SourceCoordinates sc = new SourceCoordinates(target,
				DS.getCoordinates(), DS.getEquinox(), DS.getResolver());
		if (!sc.convertToCoords()) {
			throw new Exception("Unable to parse/resolve coordinates:" + target);
		} 
		try {
			double[] coords = sc.getPosition().getCoordinates();
			ra = coords[0];
			dec = coords[1];
			Settings.put("ra", "" + ra);
			Settings.put("dec", "" + dec);
		} catch (Exception e) {
			throw new Exception("Error getting coordinates", e);
		}
	}

	private void printResponse() throws Exception {
		Header hdr = new Header();

		Settings.put("cache", cf.getCacheBase());
		hdr.addCSSFile(DS.getURLBase() + "/css/tp.css");
		hdr.addCSSFile(DS.getURLBase() + "/css/styles.css");
		hdr.addCSSFile(DS.getURLBase() + "css/net_nvo.css");
		hdr.addCSSFile(DS.getURLBase() + "css/voview.css");

		hdr.addJavaScript(DS.getURLBase() + "/js/tp.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/parsenode.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/astro.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/datascope.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/bar.js");

		hdr.addJavaScript(DS.getURLBase() + "/js/cookie.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/sarissa.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/statemanager.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/query.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/filter.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/fsm.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/tablednd.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/voview.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/jquery.js");

		hdr.setOnLoad("initialize()");
		hdr.setTitle("DataScope query:" + target);

		hdr.setBannerTitle("NVO Portal: DataScope Response");

		hdr.addToken("init.pl", "New Query", "Start a new query");
		hdr.addToken("/vo/datascope/helpInc.html", "Help", "Help on DataScope");
		hdr.setIcon(DS.getURLBase() + "images/datascope50.png",
				"DataScope Icon");
		// hdr.addToken("http://us-vo.org/feedback/", "Contact Us",
		// "Send mail to the NVO team");
		Header.printHTTP(System.out);
		hdr.printHTMLHeader(System.out);
		hdr.printBanner(System.out);

		printBody();

		Footer ftr = new Footer();
		ftr.print(System.out);
	}

	private void printBody() {
		try {
			SettingsFilter.filter(
					Settings.get("urlprefix") + Settings.get("docBase")
							+ "responsebody.html", true);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			new ShowError("DataScope").fail(
					"Missing/Invalid response template", e);
		}
	}

	private void logQuery() throws Exception {
		if (skipLog) {
			return;
		}

		java.nio.channels.FileLock fl = null;
		FileOutputStream fo = null;

		try {
			String filename = DS.getQueryLog();
			fo = new FileOutputStream(filename, true);
			fl = fo.getChannel().lock();
			String out = "^" + target + "|" + ra + "|" + dec + "|" + size + "|"
					+ errorCircle + "\n";
			fo.write(out.getBytes());
			fl.release();
			fl = null;
			fo.close();
		} finally {
			if (fl != null) {
				fl.release();
			}
			if (fo != null) {
				fo.close();
			}
		}
	}

	private class paramParseCallBack extends DefaultHandler {
		StringBuffer buf = null;
		boolean active = false;
		String id;

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {
			if (qName.equals("uws:parameter")) {
				id = attrib.getValue("id");
				active = true;
				buf = new StringBuffer();
			}

		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("uws:parameter")) {
				jobParams.put(id, new String(buf).trim());
			}
		}

		public void characters(char[] arr, int start, int len) {
			if (active) {
				buf.append(arr, start, len);
			}
		}
	}
}
