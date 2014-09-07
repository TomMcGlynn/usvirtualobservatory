package net.ivoa.datascope;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.Date;

public abstract class Service {
    
    private String    baseURL;
    private boolean   status   = true;
    private String    message  = null;
    private double    ror      = 0;
    private String    sn;
    private int[]     hits     = null;
    private String    cache;
    private int       index;
    private String    ivoID;
    private static String requestID;
    
    public static void setServiceID(String id) {
	requestID = id;
    }
    
    
    public void initialize(String ivoID, String url, String cache, String sn, int index, double ror) {
	this.ivoID = ivoID;
	this.index = index;
	this.ror   = ror;
	this.sn    = sn;
	this.cache = cache;
	if (!url.endsWith("?") && !url.endsWith("&")) {
	    if (url.indexOf('?') < 0) {
		baseURL = url+"?";
	    } else {
		baseURL = url+"&";
	    }
	} else {
	    baseURL = url;
	}
    }
    
    public double getROR() {
	return ror;
    }
    
    public String getShortName() {
	return sn;
    }
    
    String getURL() {
	return baseURL;
    }
    
    protected void setURL(String url) {
	baseURL = url;
    }
    
    abstract void updateURL(double ra, double dec, double size);
    
    /** Get the result as a string, accommodating the character encoding
     *  used.  Default to ISO-8859-1 for anything
     */
    public static String get(String urlString) throws Exception {
	
	if (requestID != null) {
	    urlString += "&requestID="+requestID;
	}
	DS.log("Query URL is:"+urlString);
	
	URLConnection uc = new URL(urlString).openConnection();
	uc.setReadTimeout(DS.getTimeout());
	
	String charset = "ISO-8859-1";
	String type = uc.getContentType();
	boolean findXMLEncoding = false;
       
	// Look for the charset in the HTTP headers
        if (type != null) {
	    type = type.trim().toLowerCase();
	    if (type.startsWith("text") ) {
	        String[] fields = type.split(";");
	        if (fields[0].indexOf("xml") >= 0) {
		    findXMLEncoding = true;
		    charset        = "UTF-8";
	        }
	        for (int i=1; i<fields.length; i += 1) {
		    String fld = fields[i].trim();
		    if (fld.startsWith("charset=")) {
		        charset=fld.substring(8).trim();
		        findXMLEncoding = false;
		    }
	        }
	    } else {
		charset = "US-ASCII";  // This seems to just set the high order byte to 0 in chars.
	    }
	}
				       
	ByteArrayOutputStream bs = new ByteArrayOutputStream();
	InputStream is = uc.getInputStream();
	byte[] buf = new byte[16384];
	int len;
	while ( (len = is.read(buf)) > 0) {
	    bs.write(buf, 0, len);
	}
	bs.close();
	is.close();
	
	// Look for byte order marks that define UTF-16 encodings
	if (findXMLEncoding) {
	    byte[] ba =  bs.toByteArray();
	
	    if (ba.length > 2) {
	        if ( (ba[0]&0xff) == 0xff   && (ba[1]&0xff) == 0xfe) {
		    charset = "UTF-16LE";
		    findXMLEncoding = false;
	        }
	        else if ( (ba[0]&0xff) == 0xfe   && (ba[1]&0xff) == 0xff) {
		    charset = "UTF-16";
		    findXMLEncoding = false;
	        }
	    }
	}
	
	String result = bs.toString(charset);
	if (findXMLEncoding && result.length() > 10) {
	    int index = result.indexOf('\n');
	    String line1 = result.substring(0,index).toUpperCase();
	    String match="ENCODING=\"";
	    index = line1.indexOf(match);
	    if (index > 0) {
		line1= line1.substring(index+match.length());
		index = line1.indexOf("\"");
		if (index > 0) {
		    charset = line1.substring(0,index).trim();
		    // Get rid of the trailing quote
		    if (charset != "UTF-8") {
			try {
			    result = bs.toString(charset);
			} catch (Exception e) {
			    // Just log it.
			    DS.log("Encoding exception for "+charset);
			}
		    }
		}
	    }
	}
	return result;
    }
    
    protected abstract void analyze(String result) throws Exception;
    
    void invoke() {
	String message = "";
	String result = null;
	try {
	    result = get(getURL());
	    analyze(result);
	    
	    if(result != null) {
		save(result);
	    }
	    
	    if (hits != null && hits.length > 0 && hits[0] > 0) {
//		save(result);
		String sep = "";
		for (int i: hits) {
		    message += sep + i;
		    sep      = ",";
		}
		message += "|";
		setMessage(message);
	    } else {
		message = "|";
		setMessage(message);
	    }
	    
	} catch (Exception e) {
	    status = false;
	    hits   = null;
	    setMessage( "-1|"+e);
	    DS.log("Service:"+sn+"."+index+" got exception:"+e);
	    if (result != null) {
		if (result.length() < 50) {
		    DS.log("     Result:"+result);
		} else {
		    DS.log("     Result starts with:"+result.substring(0,50));
		}
	    }
	}
    }
    
    synchronized void setMessage(String message) {
	this.message = message;
    }
    
    static Service factory(String type) {
	
	String lowType = type.toLowerCase();
	if (lowType.startsWith("cone")){
	    return new Cone();
	} else if (lowType.startsWith("sia")) {
	    return new SIA();
	} else {
	    return null;
	}
    }
    
    protected void setHits(int[] newHits) {
	this.hits = newHits;
    }
    
    synchronized String getMessage() {
	return message;
    }
    
    private void save(String result) throws Exception {
	OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(cache+"/"+DS.validFileName(sn)+"."+index+".xml"), "ISO-8859-1");
	fw.write(result);
	fw.close();
    }
    
    String getID() {
	return ivoID;
    }
}
