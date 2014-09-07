package skyview.survey;

import skyview.executive.Settings;
import skyview.survey.Image;

import java.io.BufferedInputStream;
import java.net.URL;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

/** This class gets a set of candidates from a SIAP request */
public class SIAPGenerator implements ImageGenerator {
    
    /** The descriptions of the images we are interested in */
    java.util.ArrayList<String> spells = new java.util.ArrayList<String>();

    /** Find the base URL for this SIAP service */
    protected String getBaseURL() {
	return Settings.get("SiapURL");
    }
    
    /** Get images from a SIAP service */
    public void getImages(double ra, double dec, double size, java.util.ArrayList<String> spells)  {
	
	String urlString = getBaseURL();
	
	urlString       += "&POS="+ra+","+dec+"&SIZE="+(1.4*size);
	System.err.println("  SIAP request URL:"+urlString);
	
      try {
        java.io.BufferedInputStream   bi = new java.io.BufferedInputStream(
					     new URL(urlString).openStream());
	java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(32768);
	
	byte[] buf = new byte[32768];
	int len;
	while ( (len=bi.read(buf)) > 0) {
	    bo.write(buf, 0, len);
	}
	bi.close();
	bo.close();
	
        String response = bo.toString();
	response = response.replaceAll("<!DOCTYPE.*", "");
	    
	java.io.ByteArrayInputStream byi = new java.io.ByteArrayInputStream(response.getBytes());
	try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
	    // This should fill images with the strings for any images we want.
            sp.parse(byi, new SIAPGenerator.SIAPParserCallBack(spells));
        } catch(Exception e) {
	    throw new Error("Error parsing SIAP:"+e);
        }
      } catch (Exception e) {
	  throw new Error("Unable to do IO in SIAP processing:"+e);
      }
    }

    private class SIAPParserCallBack extends DefaultHandler {
	
	
	/** Buffer to accumulate text into */
	private StringBuffer buf;
	
	/** Are we in an active element? */
	private boolean active = false;
	
	private int fieldCount = 0;
	
	private java.util.HashMap<String, Integer> fields = new java.util.HashMap<String, Integer>();
	private java.util.ArrayList<String> values = new java.util.ArrayList<String>();
	
	private String proj    = Settings.get("SIAPProjection");
	private String csys    = Settings.get("SIAPCoordinates");
	private String naxis   = Settings.get("SIAPNaxis");
	private String scaling = Settings.get("SIAPScaling");
	private String maxImageString = Settings.get("SIAPMaxImages");
	private int maxImages;
	private int imageCount = 0;
	
	
	java.util.ArrayList<String> spells;
	
	SIAPParserCallBack(java.util.ArrayList<String> spells) {
	    this.spells = spells;
	    if (maxImageString != null) {
		maxImages = Integer.parseInt(maxImageString);
	    }
	}
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    
	    if (qName.equals("FIELD")) {
		String ucd = attrib.getValue("ucd");
		if(ucd != null && ucd.length() > 1) {
		    fields.put(ucd, fieldCount);
		}
		fieldCount += 1;
		
	    } else if (qName.equals("TR") ) {
		values.clear();

		
	    } else if (qName.equals("TD")) {
	        active = true;
		buf    = new StringBuffer();
	    }
		
        }
    
	private String getUCD(String ucd) {
	    
	    if (fields.containsKey(ucd)) {
		int i   = fields.get(ucd);
		return values.get(i);
	    } else {
		return null;
	    }
	}
	
        public void endElement(String uri, String localName, String qName) {
	    
	    
	    // This means we finished a setting.
	    if (active) {
		
	        active = false;
		String s = new String(buf).trim();
		
		if(qName.equals("TD")) {
		    values.add(s);
		    
		}
	    } else if (qName.equals("TR")) {
		
		if (maxImageString != null) {
		    if (imageCount >= maxImages) {
			return;
		    }
		}
		/** Heres where all the work goes... */
		String spell = "";
		String url    = getUCD("VOX:Image_AccessReference");
		
		String file = getUCD("VOX:File_Name");
		
		if (file == null) {
		    file = url.substring(url.lastIndexOf('/')+1);
		}
		
		String ra      = getUCD("POS_EQ_RA_MAIN");
		String dec     = getUCD("POS_EQ_DEC_MAIN");
		boolean invert = false;
		String projstr = getUCD("VOX:WCS_CoordProjection");
		if (projstr != null) {
		    // If Dec comes before RA we need to flip the order
		    // of axes.
		    invert = proj.matches(".*\\-(DEC|LAT).*\\-(RA|LON).*");
		}
		
		String crval   = mashVal(getUCD("VOX:WCS_CoordRefValue"), invert);
		
		// The following may be set generally.  If so
		// don't query.
		if (scaling == null) {
		    scaling = mashVal(getUCD("VOX:WCS_CDMatrix"), invert);
		}
		if (scaling == null) {
		    scaling = mashVal(getUCD("VOX:Image_Scale"), invert);
		}
		
		if (naxis == null) {
		    naxis  = mashVal(getUCD("VOX:Image_Naxis"), invert);
		}
		
		if (crval == null) {
		    crval = ra + "," +dec;
		}
		
		spell = url + "," + file + "," + crval + "," + proj+","+csys+","+naxis + ","+scaling;
		spells.add(spell);
		imageCount += 1;
	    }
	}
	
	/** Take the input string, split by spaces or commas
	 *  invert array if needed and join with spaces.
	 */
	private String mashVal(String input, boolean invert) {
	    if (input == null) {
		return null;
	    }
	    input = input.trim();
	    String[] tokens = input.split(" ");
	    if (tokens.length == 1) {
		tokens = input.split(",");
	    }
	    String output = "";
	    
	    int curr = 0;
	    int delta = 1;
	    
	    if (invert) {
		curr = tokens.length-1;
		delta = -1;
	    }
	    
	    String sep = "";
	    for (int i=0; i<tokens.length; i += 1) {
		output += sep + tokens[curr];
		curr   += delta;
		sep    = ",";
	    }
	    return output;
	}
		    
        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
	
    }
}
