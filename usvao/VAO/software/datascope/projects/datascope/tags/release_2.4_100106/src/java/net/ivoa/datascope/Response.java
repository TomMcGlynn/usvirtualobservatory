package net.ivoa.datascope;

import net.ivoa.util.ShowError;
import net.ivoa.util.CGI;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;

import java.io.FileOutputStream;

import net.nvo.Header;
import net.nvo.Footer;

import skyview.request.SourceCoordinates;

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

	public static void main(String[] args) {
		Response resp = new Response();
		resp.respond();
	}

	public void respond() {
		parseInputs();
		printResponse();
		initializeCache();
	}

	private void initializeCache() {
		if (cf.existed()) {
			return;
		}
		try {
			scan = new Scanner(cf.getCacheHome());
			DS.setLogLocation(cf.getCacheHome());
			scan.setup(ra, dec, size);
		} catch (Exception e) {
			System.out.println("<pre>");
			e.printStackTrace(System.out);
			System.out.println("</pre>");
			error("Error initiating scan:" + e);
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

	private void parseInputs() {

		for (String key : cgi.keys()) {
			String val = cgi.value(key).trim();
			if (val != null && val.length() > 0) {
				// Need to know which resources to
				// check if result is cached, so we download
				// the resource list immediately.
				if (key.equalsIgnoreCase("resourcesURL")) {
					try {
						String resources = Service.get(val);
						key = "resources";
						val = resources;
					} catch (Exception e) {
						// Just ignore it, we'll go with everything.
						continue;
					}
				}
				Settings.put(key, val);
			}
		}

		target = cgi.value("position");

		if (target == null) {
			error("No target");
		}
		Settings.put("target", target);

		String ecirc = cgi.value("errorcircle");
		if (ecirc != null) {
			try {
				errorCircle = Double.parseDouble(ecirc);
			} catch (Exception e) {
				error("Invalid error circle radius:" + ecirc);
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
				error("Invalid size:" + size);
			}
		} else {
			size = DS.getDefaultSize();
		}

		Settings.put("size", "" + size);

		SourceCoordinates sc = new SourceCoordinates(target, DS
				.getCoordinates(), DS.getEquinox(), DS.getResolver());
		if (!sc.convertToCoords()) {
			error("Unable to parse/resolve coordinates:" + target);
		}
		try {
			double[] coords = sc.getPosition().getCoordinates();
			ra = coords[0];
			dec = coords[1];
			Settings.put("ra", "" + ra);
			Settings.put("dec", "" + dec);
		} catch (Exception e) {
			error("Error getting coordinates" + e);
		}
	}

	private void printResponse() {

		Header hdr = new Header();

		try {
			cf = new CacheFinder();
		} catch (Exception e) {
			error("Unable to find/create cache for request:" + e);
		}

		Settings.put("cache", cf.getCacheBase());
		hdr.addCSSFile(DS.getURLBase() + "/css/tp.css");
		hdr.addCSSFile(DS.getURLBase() + "/css/styles.css");
		hdr.addCSSFile(DS.getURLBase() + "css/net_nvo.css");
		hdr.addJavaScript(DS.getURLBase() + "/js/tp.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/parsenode.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/astro.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/datascope.js");
		hdr.addJavaScript(DS.getURLBase() + "/js/bar.js");
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
			SettingsFilter.filter(Settings.get("urlprefix")
					+ Settings.get("docBase") + "responsebody.html", true);
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

	static void error(String cause) {

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
		System.out.println("<h2> DataScope Error </h2><p>" + cause + "<p>");
		new Footer().print(System.out);
		System.exit(0);
	}

}
