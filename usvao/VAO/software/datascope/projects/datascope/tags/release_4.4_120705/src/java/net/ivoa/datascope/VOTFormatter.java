package net.ivoa.datascope;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import net.ivoa.util.CGI;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class VOTFormatter {

	/**
	 * The class outputs skeleton information for the VOTable
	 */
	private class VOTCallBack extends DefaultHandler {

		boolean active = false;

		public ArrayList<String> names = new ArrayList<String>();
		public ArrayList<String> ucds = new ArrayList<String>();
		public ArrayList<String> types = new ArrayList<String>();
		int tab = 0;
		int row = 0;
		int col = 0;
		int ncol = 0;
		StringBuffer buf = null;
		String tname = null;
		String tdesc = null;
		boolean lookForDesc = false;

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {
			if (qName.equals("RESOURCE")) {
				names.clear();
				ucds.clear();
				types.clear();
				row = 0;
				ncol = 0;
				tname = null;
				lookForDesc = true;

			} else if (qName.equals("TABLE")) {
				lookForDesc = false;
				tname = attrib.getValue("name");

			} else if (qName.equals("DESCRIPTION") && lookForDesc) {
				active = true;
				buf = new StringBuffer();

			} else if (qName.equals("TR")) {
				col = 0;
				row += 1;
				if (row == 1) {
					tab += 1;
					printPrefix();
				}
				col = 0;
				printRowStart();
			} else if (qName.equals("TD")) {
				active = true;
				col += 1;
				buf = new StringBuffer();
			} else if (qName.equals("FIELD")) {
				addField(attrib);
				lookForDesc = false;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("TR")) {
				printRowEnd();
			} else if (qName.equals("TD")) {
				active = false;
				String s = new String(buf).trim();
				printElement(s);
			} else if (qName.equals("RESOURCE")) {
				if (row > 0) {
					printSuffix();
				}
			} else if (lookForDesc && qName.equals("DESCRIPTION")) {
				lookForDesc = false;
				tdesc = new String(buf).trim();
			}
		}

		private void addField(Attributes attrib) {
			String ucd = attrib.getValue("ucd");
			if (ucd == null) {
				ucd = attrib.getValue("UCD");
			}
			if (ucd == null) {
				ucd = "";
			}
			ucds.add(ucd);
			String name = attrib.getValue("name");
			if (name == null) {
				name = attrib.getValue("ID");
			}
			if (name == null) {
				name = "";
			}
			names.add(name);
			String type = attrib.getValue("datatype");
			if (type == null) {
				type = "";
			}
			types.add(type);
			ncol += 1;
		}

		private void printPrefix() {
			String tns;
			String tnd = "";
			if (tdesc != null) {
				String[] lines = tdesc.split("\n");
				tdesc = lines[0];
				tdesc = tdesc.replace("[\"'><&]", "_");
				tnd = "Ttdesc" + tab + "='" + tdesc + "'\n";
			}
			if (tname == null) {
				tns = "Ttname" + tab + "=null\n";
			} else {
				tns = "Ttname" + tab + "='" + tname + "'\n";
			}

			System.out.println("<SCRIPT language='JavaScript'>\n" + "Tncol"
					+ tab + "=" + ncol + "\n" + "Tucd" + tab + "='"
					+ join(ucds, ",", ncol) + "'\n" + "Tname" + tab + "='"
					+ join(names, ",", ncol) + "'\n" + "Ttype" + tab + "='"
					+ join(types, ",", ncol) + "'\n" + tns + tnd + "Tntab="
					+ tab + "\n" + "</SCRIPT>\n"
					+ "<table border><tr bgcolor='#FFFFCC' id=tr" + tab + "-0>"
					+ "<td id=t" + tab + "-0-0-0></td>\n");
			for (int i = 0; i < ncol; i += 1) {
				System.out.println("<th id=th" + tab + "-" + (i + 1) + ">"
						+ names.get(i) + "</th>");
			}
			System.out.println("<tr id=trx" + tab + "></tr>");
		}

		private void printSuffix() {
			System.out.println("</TABLE>");
		}

		private void printElement(String s) {
			System.out.print("<td id=t" + tab + "-" + row + "-" + col + ">");
			try {
				System.out.write(s.getBytes("ISO-8859-1"));
			} catch (Exception e) {
				System.out.print("???");
			}
			System.out.print("</td>");
		}

		private String join(ArrayList<String> arr, String sep, int ncol) {
			StringBuffer buf = new StringBuffer();
			String xsep = "";
			for (int i = 0; i < ncol; i += 1) {
				String inp = arr.get(i);
				inp = inp.replace("'", "\\'");
				buf.append(xsep);
				if (inp != null) {
					buf.append(inp);
				}
				xsep = sep;
			}
			return new String(buf);
		}

		private void printRowStart() {

			if (row > 1 && (row - 1) % 25 == 0) {
				System.out
						.print("</TABLE><TABLE border><tr bgcolor='#FFFFCC'><td id=t"
								+ tab + "-" + row / 25 + "-0-0></td>");
				for (int i = 0; i < ncol; i += 1) {
					System.out.println("<th>" + names.get(i) + "</th>");
				}
			}
			String color = "'#DDDDDD'";
			if (row % 2 == 0) {
				color = "'#FFFFFF'";
			}
			System.out.print("<tr bgcolor=" + color + "><td id=t" + tab + "-"
					+ row + "-0></td>");
		}

		private void printRowEnd() {
			System.out.println("</tr>\n");
		}

		public void characters(char[] arr, int start, int len) {
			if (active) {
				buf.append(arr, start, len);
			}
		}
	}

	public static void main(String[] args) throws Exception {

		VOTFormatter vf = new VOTFormatter();
		if (args.length == 0) {
			CGI inp = new CGI();

			String sn = inp.value("sn");
			String id = inp.value("id");
			String cache = inp.value("cache");

			String file = DS.baseToHome(cache) + DS.validFileName(sn) + "."
					+ id + ".xml";
			System.out.println("Content-type: text/html\n");
			vf.format(file);
		} else {
			vf.format(args[0]);
		}
	}

	public void format(String filename) throws Exception {
		File f = new File(filename);
		if (!f.exists()) {
			throw new Exception("File " + filename + " not found.");
		}
		SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
		BufferedReader is = new BufferedReader(new InputStreamReader(
				new FileInputStream(f), "ISO-8859-1"));
		sp.parse(new InputSource(is), new VOTCallBack());
		is.close();
	}
}
