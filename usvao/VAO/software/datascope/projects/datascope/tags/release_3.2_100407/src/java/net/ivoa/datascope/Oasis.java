package net.ivoa.datascope;

import net.ivoa.util.CGI;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import net.nvo.Header;
import net.nvo.Footer;

import java.io.FileWriter;

public class Oasis {

	private static String cache;

	private static class OasisCallBack extends DefaultHandler {

		boolean active = false;

		int tab = 0;
		int row = 0;
		int col = 0;
		int ncol = 0;

		StringBuffer buf = null;
		int raCol = -1;
		int decCol = -1;

		double raVal;
		double decVal;

		FileWriter output;

		OasisCallBack(FileWriter fw) {
			this.output = fw;
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {
			if (qName.equals("RESOURCE")) {
				row = 0;
				ncol = 0;
			} else if (qName.equals("TR")) {
				col = 0;
				row += 1;
				if (row == 1) {
					if (tab == 0) {
						printPrefix();
					}
					tab += 1;
				}
				col = 0;

			} else if (qName.equals("TD")) {
				if (col == raCol || col == decCol) {
					active = true;
					buf = new StringBuffer();
				}
			} else if (qName.equals("FIELD")) {
				checkField(attrib);
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("TR")) {
				printRow();
			} else if (qName.equals("TD")) {
				if (active) {
					if (col == raCol) {
						try {
							String s = new String(buf).trim();
							raVal = Double.parseDouble(s);
						} catch (Exception e) {
							raVal = Double.NaN;
						}
					} else if (col == decCol) {
						try {
							String s = new String(buf).trim();
							decVal = Double.parseDouble(s);
						} catch (Exception e) {
							decVal = Double.NaN;
						}
					}
					active = false;
				}
				col += 1;
			}
		}

		private void checkField(Attributes attrib) {
			String ucd = attrib.getValue("ucd");

			if (ucd != null
					&& (ucd.equals("POS_EQ_RA_MAIN") || ucd
							.equals("pos.eq.ra;meta.main"))) {
				raCol = ncol;
			} else if (ucd != null
					&& (ucd.equals("POS_EQ_DEC_MAIN") || ucd
							.equals("pos.eq.dec;meta.main"))) {
				decCol = ncol;
			}
			ncol += 1;
		}

		public void characters(char[] arr, int start, int len) {
			if (active) {
				buf.append(arr, start, len);
			}
		}

		private void printPrefix() {
			try {
				output
						.write("|RA                       |Dec                      \n");
			} catch (Exception e) {
			}
		}

		private void printRow() {
			String r = "" + raVal;
			r += "                         ".substring(r.length()) + "|";
			String d = "" + decVal;
			d += "                         ".substring(d.length()) + "|";
			try {
				output.write(" " + r + d + "\n");
			} catch (Exception e) {
			}
		}
	}

	public static void main(String[] args) throws Exception {

		CGI cgi = new CGI();
		Header hdr = new Header();
		hdr.setBannerTitle("OASIS Launcher Page");
		hdr.addCSSFile(DS.getURLBase() + "css/styles.cs");

		Header.printHTTP(System.out);
		hdr.printHTMLHeader(System.out);
		hdr.printBanner(System.out);

		String selectString = cgi.value("selections");
		String[] selections = selectString.split(";");
		cache = cgi.value("cache");

		int max = selections.length;
		if (max > 10) {
			System.out
					.println("<hr><b>Note:</b> More than 10 items were selected but only 10 can "
							+ " be sent to OASIS<hr>");
			max = 10;
		}

		System.out
				.print("<h2> OASIS Launcher </h2>"
						+ "If this is the first time you have run OASIS there will be "
						+ "a short dialog to install OASIS.  You will be requested "
						+ "for permission to install OASIS as a trusted applet on "
						+ "your machine.<br>Then just click "
						+ "on the button to start your OASIS session.<p> "
						+ "The OASIS applet requires the Java plugin with Java 1.2 or greater.<br>"
						+ "<OBJECT classid='clsid:8AD9C840-044E-11D1-B3E9-00805F499D93' "
						+ "   WIDTH=120 "
						+ "   HEIGHT=40 "
						+ "   codebase='http://java.sun.com/products/plugin/1.3/jinstall-13-win32.cab#Version=1,3,0,0'> "
						+ "   <PARAM NAME = CODE            VALUE = 'irsa.oasis.display.OasisApplet'>"
						+ "   <PARAM NAME = CODEBASE        VALUE = 'http://irsa.ipac.caltech.edu/applications/Oasis/applet/'>"
						+ "   <PARAM NAME = 'type'          VALUE = 'application/x-java-applet;version=1.3'>"
						+ "   <PARAM NAME = 'mayscript'     VALUE = 'true'>"
						+ "   <PARAM NAME = 'scriptable'    VALUE = 'false'>"
						+ "   <PARAM NAME = 'cache_option'  VALUE = 'Plugin'>"
						+ "   <PARAM NAME = 'cache_archive' VALUE = 'Oasis.jar'>");

		String[] urls = new String[max];
		for (int i = 0; i < max; i += 1) {
			urls[i] = makeURL(selections[i]);
		}

		int ocount = 0;
		for (int i = 0; i < max; i += 1) {
			if (urls[i] != null) {
				System.out.println("<PARAM NAME = 'dataurl" + ocount
						+ " VALUE='" + urls[i] + "'>");
				ocount += 1;
			}
		}

		System.out
				.println("<EMBED type          = 'application/x-java-applet;version=1.3' "
						+ "     CODE          = 'irsa.oasis.display.OasisApplet' "
						+ "     CODEBASE      = 'http://irsa.ipac.caltech.edu/applications/Oasis/applet/' "
						+ "     WIDTH         =  120 "
						+ "     HEIGHT        =  40 "
						+ "     mayscript     =  true "
						+ "     scriptable    =  false "
						+ "     pluginspage   = 'http://java.sun.com/products/plugin/1.3/plugin-install.html' "
						+ "     cache_option  = 'Plugin' "
						+ "     cache_archive = 'Oasis.jar' ");
		ocount = 0;
		for (int i = 0; i < max; i += 1) {
			if (urls[i] != null) {
				System.out.println("    dataurl" + ocount + "='" + urls[i]
						+ "'");
				ocount += 1;
			}
		}
		System.out.println("><NOEMBED></NOEMBED></OBJECT>");

		new Footer().print(System.out);
	}

	private static String makeURL(String selection) throws Exception {
		String[] flds = selection.split(",");
		String url;
		if (flds.length == 2) {
			String file = DS.validFileName(flds[0]) + "." + flds[1] + ".xml";
			return "http://" + DS.getHost() + cache + file;
		} else {
			String[] inp = flds[1].split("-");
			String file = DS.baseToHome(cache) + DS.validFileName(flds[0])
					+ "." + flds[1] + "." + inp[0] + "." + inp[1] + ".fits";
			file = DS.checkFile(file);

			if (file != null) {
				url = "http://" + DS.getHost() + DS.homeToBase(file);
			} else {
				url = "http://" + DS.getHost() + DS.getCGIBase() + "rf.pl?"
						+ "sn=" + DS.encode(flds[0]) + "&id=" + inp[0]
						+ "&index=" + inp[1] + "&col=" + flds[2] + "&cache="
						+ DS.encode(cache);
			}
		}
		return url;
	}
}
