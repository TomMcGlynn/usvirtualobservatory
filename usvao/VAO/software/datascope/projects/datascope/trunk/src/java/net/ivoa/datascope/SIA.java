package net.ivoa.datascope;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.text.DecimalFormat;

public class SIA extends Service {

	private int fits;
	private int nonFits;

	/**
	 * The class counts the number of rows as FITS or non-FITS.
	 */
	private class SIACallBack extends DefaultHandler {

		boolean fitsFound = false;
		boolean active = false;
		StringBuffer buf;

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {
			if (qName.equals("TR")) {
				fitsFound = false;
			}
			if (qName.equals("TD")) {
				active = true;
				buf = new StringBuffer();
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("TR")) {
				if (fitsFound) {
					fits += 1;
				} else {
					nonFits += 1;
				}
			} else if (qName.equals("TD")) {
				active = false;
				String s = new String(buf).trim().toLowerCase();
				if ((s.startsWith("application") || s.startsWith("image"))
						&& s.endsWith("fits")) {
					fitsFound = true;
				}
			}
		}

		public void characters(char[] arr, int start, int len) {
			if (active) {
				buf.append(arr, start, len);
			}
		}
	}

	public void updateURL(double ra, double dec, double size) {
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(10);
		setURL( getURL() + "POS=" + formatter.format(ra) + "," + formatter.format(dec) + "&SIZE="
				+ formatter.format(size + getROR()) );
	}

	protected void analyze(String result) throws Exception {
		SAXParserFactory sp_fac = SAXParserFactory.newInstance();
		sp_fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		SAXParser sp = sp_fac.newSAXParser();

//		DS.log("Is SIA sax parser validating: " + sp.isValidating() + " " + sp.getSchema());
		
		StringReader is = new StringReader(result);
		sp.parse(new InputSource(is), new SIACallBack());
		is.close();
		if (fits == 0 && nonFits == 0) {
			setHits(new int[0]);
		} else {
			setHits(new int[] { fits + nonFits, fits, nonFits });
		}
	}
}
