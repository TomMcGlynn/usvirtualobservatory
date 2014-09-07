package net.ivoa.datascope;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;

public class VizierFilter extends Filter {

	private HashMap<String, Integer> counts = new HashMap<String, Integer>();
	private String urlString = "http://vizier.u-strasbg.fr/viz-bin/votable?-meta&";
	private String idBase = "ivo://CDS.VizieR/";

	private class VizierCallBack extends DefaultHandler {

		String name;
		int count;
		boolean countFound = false;
		boolean nameFound = false;

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {
			if (qName.equals("RESOURCE")) {
				countFound = false;
				nameFound = false;
				if (attrib.getValue("name") != null) {
					name = attrib.getValue("name");
					nameFound = true;
				}
			} else if (qName.equals("INFO")) {
				if (attrib.getValue("name") != null) {
					if (attrib.getValue("name").equals("-density")) {
						String dens = attrib.getValue("value");
						if (dens != null) {
							try {
								count = Integer.parseInt(dens);
								countFound = true;
							} catch (Exception e) {
								// Just ignore it...
							}
						}
					}
				}
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("RESOURCE")) {
				if (nameFound && countFound) {
					DS.log("Vizier filter found " + name + " " + count);
					counts.put(name, count);
				}
			}
		}
	}

	public void invoke(double ra, double dec, double size) {
		double modSize = ((size + 0.1) * 60);
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(10);
		
		urlString += "-c=" + DS.encode(formatter.format(ra) + " " + formatter.format(dec)) + "&-c.r="
				+ formatter.format(modSize);

		String results;
		try {
			results = Service.get(urlString);
		} catch (Exception e) {
			DS.log("Vizier filter exception: " + e);
			e.printStackTrace(DS.logWriter);
			return;
		}
		try {
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			StringReader is = new StringReader(results);
			sp.parse(new InputSource(is), new VizierCallBack());
			is.close();
		} catch (Exception e) {
			// Just give up on the filter
			DS.log("Error in Vizier Filter:" + e);
			System.err.println("Error in Vizier Filter:" + e);
		}
	}

	public int count(String id) {
		if (id.startsWith(idBase)) {
			id = id.substring(idBase.length());				
			if (counts.containsKey(id)) {
				return counts.get(id);
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}

	public double fudge() {
		return 0.1;
	}

	public HashMap<String, Integer> getCounts() {
		return counts;
	}
}
