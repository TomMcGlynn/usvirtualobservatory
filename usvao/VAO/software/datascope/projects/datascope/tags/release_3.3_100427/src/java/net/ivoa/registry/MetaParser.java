package net.ivoa.registry;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class parses the simplified registry format and extracts the information
 * we need from it.
 */
public class MetaParser {

	/**
	 * The metadata is a two level hashmap ivoid -> keyword -> values
	 */
	private Map<String, Map<String, List<String>>> metadata;

	/** Append to existing metadata */
	public MetaParser(Map<String, Map<String, List<String>>> meta) {
		metadata = meta;
	}

	/** Start fresh with no existing metadata */
	public MetaParser() {
		metadata = new HashMap<String, Map<String, List<String>>>();
	}

	/**
	 * This class contains the SAX call backs that do the processing of the
	 * actual elements. 
	 */
	private class DataCallBack extends DefaultHandler {

		boolean active = false;
		String ivoid = null;
		int colCounter;
		Map<String, List<String>> currentMap;
		ArrayList<String> fields = new ArrayList<String>();
		LinkedList<String> ancestors = new LinkedList<String>();

		StringBuffer buf = null;

		public void startElement(String uri, String localName, String qName,
				Attributes attrib) {

			if (qName.equalsIgnoreCase("FIELD")) {
				fields.add(attrib.getValue("ID"));
			} else if (qName.equalsIgnoreCase("TR")
					&& ancestors.get(0).equalsIgnoreCase("TABLEDATA")) {
				// At the beginning of qa new resource start with a new map for
				// its
				// values.
				currentMap = new HashMap<String, List<String>>();
				colCounter = 0;
			} else if (qName.equalsIgnoreCase("TD")
					&& ancestors.get(1).equalsIgnoreCase("TABLEDATA")) {

				// Here we just want to get the values.
				active = true;
				buf = new StringBuffer();
			}

			ancestors.push(qName);
		}

		public void endElement(String uri, String localName, String qName) {
			ancestors.pop();

			// Save the accumulated information for this resource.
			if (qName.equalsIgnoreCase("TR")
					&& ancestors.get(0).equalsIgnoreCase("TABLEDATA")) {
				if (ivoid != null) {
					metadata.put(ivoid, currentMap);
				}
				currentMap = null;
			} else if (qName.equals("TD")
					&& ancestors.get(1).equalsIgnoreCase("TABLEDATA")) {
				// Add the value for this field to the current metadata.
				String val = new String(buf).trim();
				if (val.length() > 0) {
					List<String> vals;
					boolean add = false;
					String key = fields.get(colCounter);
					if (currentMap.containsKey(key)) {
						vals = currentMap.get(key);
					} else {
						vals = new ArrayList<String>();
						add = true;
					}
					for (String split_val : val.split("#")) {
						if( split_val.length() > 0 ){
							vals.add(split_val);
						}
					}
					if (add) {
						currentMap.put(key, vals);
					}
					if (key.equalsIgnoreCase("Identifier")) {
						ivoid = val;
					}

				}
				colCounter++;
			}
			active = false;
		}

		/** Accumulate the character values. */
		public void characters(char[] arr, int start, int len) {
			if (active) {
				buf.append(arr, start, len);
			}
		}

	}

	/** Generate the metadata hash from the XML input */
	public Map<String, Map<String, List<String>>> extract(InputStream is)
			throws Exception {
		SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
		BufferedReader bis = new BufferedReader(new InputStreamReader(is,
				"ISO-8859-1"));

		sp.parse(new InputSource(bis), new DataCallBack());
		is.close();
		return metadata;
	}

	public static void main(String[] args) throws Exception {
		MetaParser mp = new MetaParser();
		mp.extract(new java.io.FileInputStream(args[0]));
	}
}
