package net.ivoa.datascope;

import java.text.DecimalFormat;
import java.util.ArrayList;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

public class SSA extends Service {

	ArrayList<Integer> counts = new ArrayList<Integer>();
	int cTab = 0;

	/**
	 * The class counts the number of rows in each table in the VOTable. It just
	 * looks for
	 * <TR>'s and saves the result when it comes to the end of the <RESOURCE>.
	 * Tables without rows are ignored.
	 */
	private class SpectralCallBack extends DefaultHandler {

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {

			if (qName.equals("TR")) {
				cTab += 1;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("RESOURCE") && cTab > 0) {
				counts.add(cTab);
				cTab = 0;
			}
		}
	}

	public void updateURL(double ra, double dec, double size) {
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(10);
		setURL(getURL() + "POS=" + formatter.format(ra) + "," + formatter.format(dec) + "&SIZE="
				+ formatter.format(size + getROR()));
	}

	protected void analyze(String result) throws Exception {
		SAXParserFactory sp_fac = SAXParserFactory.newInstance();
		sp_fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		SAXParser sp = sp_fac.newSAXParser();

		StringReader is = new StringReader(result);
		sp.parse(new InputSource(is), new SpectralCallBack());
		is.close();
		if (counts.size() == 0) {
			setHits(new int[0]);
		} else {
			int[] z = new int[counts.size() + 1];
			int total = 0;
			for (int i = 0; i < counts.size(); i += 1) {
				z[i + 1] = counts.get(i);
				total += z[i + 1];
			}
			z[0] = total;
			setHits(z);
		}
	}
}
