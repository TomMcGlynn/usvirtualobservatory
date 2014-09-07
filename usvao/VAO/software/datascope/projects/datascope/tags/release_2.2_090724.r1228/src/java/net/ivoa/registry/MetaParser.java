package net.ivoa.registry;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/** This class parses the simplified registry format and
 *  extracts the information we need from it.
 */
public class MetaParser {
    
    /** The metadata is a two level hashmap
     *    ivoid -> keyword -> values
     */
    private Map<String, Map<String, List<String>>> metadata;
    
    /** Append to existing metadata */
    public MetaParser(Map<String, Map<String, List<String>>> meta) {	
	metadata = meta;
    }
    
    /** Start fresh with no existing metadata */
    public MetaParser() {
	metadata = new HashMap<String, Map<String, List<String>>> ();
    }
    
    /** This class contains the SAX call backs that do the processing
     *  of the actual elements.  The syntax of the XML file is assumed
     *  to be of the form
     *  <pre>
     *    &lt;xxx&gt;
     *       &lt;Resource&gt;
     *           &lt;Field1&gt;value1&lt;/Field1&gt;
     *           &lt;Field2&gt;value2&lt;/Field2&gt;
     *       ...
     *       &lt;/Resource&gt;
     *       &lt;Resource&gt; ... &lt;/Resource&gt; ...
     *    &lt;/xxx&gt;
     *  </pre>
     *  where there is a unique 'Identifier' field that can be
     *  used to index the Resources.
     */
    private class DataCallBack extends DefaultHandler {
	
	boolean active    = false;
	String  ivoid     = null;
	Map<String, List<String>> currentMap;
	
	StringBuffer buf = null;
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    // At the beginning of qa new resource start with a new map for its values.
	    if (qName.equals("Resource")) {
		currentMap = new HashMap<String, List<String>>();
	    } else {
		
		// For other fields, we just want to get the values.
		active = true;
		buf    = new StringBuffer();
	    }
        }
    
	
        public void endElement(String uri, String localName, String qName) {
	    
	    // Save the accumulated information for this resource.
	    if (qName.equals("Resource")) {
		if (ivoid != null) {
		    metadata.put(ivoid, currentMap);
		}
		currentMap = null;
	    } else {
		
		// In case there are fields outside of Resources (e.g.,
		// the root node of the document)
		if (currentMap != null) {
		    
		    // Add the value for this field to the current metadata.
		    String val = new String(buf).trim();
		    if (val.length() > 0) {
		        List<String> vals;
			boolean add = false;
		        if (currentMap.containsKey(qName)) {
			    vals = currentMap.get(qName);
		        } else {
			    vals = new ArrayList<String>();
			    add  = true;
		        }
		        vals.add(val);
			if (add) {
			    currentMap.put(qName, vals);
			}
		        if (qName.equals("Identifier")) {
		            ivoid = val;
		        }
		    }
		}
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
    public Map<String, Map<String, List<String>>> 
      extract(InputStream is) throws Exception {
	SAXParser sp = SAXParserFactory.newInstance().newSAXParser(
									);
	BufferedReader bis = new BufferedReader(
			      new InputStreamReader(is, "ISO-8859-1"));
	
        sp.parse(new InputSource(bis), new DataCallBack());
	is.close();
        return metadata;
    }
    
    public static void main(String[] args) throws Exception {
	MetaParser mp = new MetaParser();
	mp.extract(new java.io.FileInputStream(args[0]));
    }
}
