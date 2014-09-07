package net.ivoa.datascope;


import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.HashMap;

public class VizierFilter extends Filter {
    
    private HashMap<String, Integer> counts = new HashMap<String, Integer>();
    private String urlString =  "http://vizier.u-strasbg.fr/viz-bin/votable?-meta&";
    private String idBase    =  "http://vizier.u-strasbg.fr/";
    
    private class VizierCallBack extends DefaultHandler {
	
	String       name;
	int          count;
	boolean      countFound = false;
	boolean      nameFound  = false;
    
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    if (qName.equals("RESOURCE")) {
		countFound = false;
		nameFound  = false;
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
		    counts.put(name,count);
		}
	    }
	}
    }
    
    public void invoke(double ra, double dec, double size) {
        urlString +=  "-c="+DS.encode(ra+" "+dec) +
                     "&-c.r="+((size+0.1)*60);
	
	String results;
        try {
	    results = Service.get(urlString);
	} catch (Exception e) {
	    return;
	}
	try {
	    SAXParser sp   = SAXParserFactory.newInstance().newSAXParser();
	    StringReader is = new StringReader(results);
            sp.parse(new InputSource(is), new VizierCallBack());
	    is.close();
	} catch (Exception e) {
	    // Just give up on the filter
	    System.err.println("Error in Vizier Filter:"+e);
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
