package skyview.request;

import net.ivoa.util.CGI;
import skyview.executive.Settings;
import skyview.executive.Imager;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import java.util.HashMap;

/** This class initiates a SkyView request from CGI.
 */
public class CGIInitiator {
    
    
    public static void main(String[] args) {
	
	System.err.println("\n--- Starting CGIInitiator ---");
	
	boolean wroteHeader = false;
	
	// Add any settings files now.
	for (int i=0; i<args.length; i += 1) {
	    Settings.updateFromFile(args[i]);
	}
	
	try {
	    CGI params    = new CGI();
	    String[] keys = params.keys();
	    HashMap<String,String> newArgs = new HashMap<String,String>();
	
            //--- query timestamp for log
            long time = System.currentTimeMillis();
            Calendar calendar = 
		Calendar.getInstance(TimeZone.getTimeZone("EST"));
            String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            java.text.SimpleDateFormat sdf = 
            	new java.text.SimpleDateFormat(DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("EST"));
            System.err.print("[" + sdf.format(calendar.getTime()) + "]:");


	    // To convert CGI parameters to settings we
	    // need to combine multiple entries.
	    // We also handle the case where the user may
	    // have specified the same key with different cases
	    // e.g., survey=XX&Survey=YY
            String delim="";
            String projwordkey = "maproj";
            String projwordval = "Gnonomic";

	    for (String key: keys) {
	        String[] values = params.values(key);
		boolean first = true;

	        for (String val: values) {
                   //--- Kluge for queries from commercial software that 
                   //--- is misspelling projection name  (uses MAPROJ)
                   if (Pattern.compile(projwordkey, 
		      Pattern.CASE_INSENSITIVE).matcher(key).find() &&
                      Pattern.compile(projwordval, 
		      Pattern.CASE_INSENSITIVE).matcher(val).find()) {
                         val="Gnomonic";
                    }
                    System.err.print(delim +key+"="+val);
		    if (first) {
			Settings.put(key, val);
			first = false;
                        delim=",";
		    } else {
		        Settings.add(key, val);
		    }
	        }
	    }
            System.err.println();
	
	    // Note that on current (2006) systems we seem to
	    // have a microsecond clock, so we truncate
	    // the last three digits of the time.  We do not
	    // handle collisions at the microsecond level but
	    // we could try to get the process number (perhaps
	    // using a -DprocessID=$$ in the command line if
	    // this is needed.
	
	    String id = "";
	
	    if (Settings.has("outputRoot")) {
	        id += Settings.get("outputRoot");
		if (!id.endsWith("/")) {
		    id += "/";
		}
 	    }
	    id += "skv"+Math.abs(System.nanoTime()/1000);
		
            boolean html     = true;
	    String  retVal   = null;
	    // Did the user ask for something other than an HTML output?
	    if (Settings.has("return")) {
		retVal = Settings.get("return").toLowerCase();
		if (!retVal.equals("simple")) {
		    html = false;
		}
		
	        if (retVal.equals("gif") || retVal.equals("jpeg") ||
		    retVal.equals("png") || retVal.equals("jpg")  ||
		    retVal.equals("tiff") || retVal.equals("bmp")) {
		    Settings.put("quicklook", retVal);
	        }
				
		if (retVal.equals("filename")) {
		    Settings.put("quicklook", "gif");
		}
		if (retVal.equals("compfits")) {
		    Settings.put("compressed", "1");
		}
		// Use 4 byte reals.
		Settings.suggest("float", "");
	    }
	
	    HTMLWriter writer = null;
	    if (html) {
	        // Add the HTML writer postprocessor.
	        String htmlWriter;
	        if (Settings.has("rgb")) {
	            htmlWriter = Settings.get("RGBWriter");
	        } else {
	            htmlWriter = Settings.get("HTMLWriter");
	        }
	        if (htmlWriter == null) {
	            htmlWriter = "skyview.request.HTMLWriter";
	        }
		Settings.add("PostProcessor", "skyview.ij.IJProcessor");
	        Settings.add("PostProcessor", htmlWriter);
                if (!Settings.has("Quicklook")) {
	            Settings.put("Quicklook", "JPG");
	        } 
		
	        writer = (HTMLWriter) Class.forName(htmlWriter).newInstance();
	        // This is an object that we use to write out the HTML for
	        // a request.  It's also used as a post-processor.
	        writer.writeHeader();
		wroteHeader  = true;
	    }
	
	    if (Settings.has("catlog") || Settings.has("catalog")) {
	        Settings.put("catalogFile", id+".cat");
	    }
	
	    Settings.put("output", id);
	    Settings.put("NOEXIT", "");
	
	    Imager.main(new String[]{"Dummy"});
	    

	    if (html) {
	        writer.writeFooter();
	    } else {
	        copyFile(id, retVal);
	    }
	} catch (Throwable e) {
	    if (!wroteHeader) {
	        System.out.print("Content-type: text/html\n\n");
	        System.out.println("<html><head>SkyView Java Exception</head><body>");
	    }
	    System.out.println("<h2> Exception encountered</h2>\n");
	    System.out.println("An irrecoverable error terminated the request.<p>");
	    System.out.println("Reason: "+e.getMessage()+"<p>");
	    System.out.println("<br><input type=button onclick='document.getElementById(\"traceback\").style.visibility=\"visible\"' value='Show Traceback'>");
	    System.out.println("<div id=traceback style='visibility:hidden'><pre>");
	    System.out.println("Java Traceback<br>");
	    e.printStackTrace(System.out);
	    System.out.println("</pre></div></body></html>");
	} finally {
	    System.exit(0);
	}
    }
    
    static void copyFile(String id, String type) throws Exception {
	if (type.equals("gif") ) {
	    id += ".gif";
	    copyToOutput(id, "gif");
	} else if (type.equals("jpeg") || type.equals("jpg")) {
	    id += ".jpg";
	    copyToOutput(id, "jpeg");
	} else if (type.equals("png")) {
	    id += ".png";
	    copyToOutput(id, "png");
	} else if (type.equals("tiff")) {
	    id += ".tiff";
	    copyToOutput(id, "tiff");
	} else if (type.equals("bmp")) {
	    id += ".bmp";
	    copyToOutput(id, "bmp");
	    
	} else if (type.equals("compfits") || type.equals("fits") || type.equals("batch")) {
	    id += ".fits";
	    if (Settings.has("compressed")) {
		id += ".gz";
	    }
	    copyToOutput(id, "fits");
	    
	} else if (type.equals("filename")) {
	    if (id.indexOf("/") >= 0) {
		id = id.substring(id.lastIndexOf("/")+1);
	    }
	    System.out.println("Content-type: text/plain\n\n"+id);
	}
    }
    
    static void copyToOutput(String file, String type) throws Exception {
	String dispo = "inline";
	if (type.indexOf("fits") >= 0) {
	    dispo = "attachment";
	}
	String last = file.substring(file.lastIndexOf("/")+1);
	
	java.io.File f = new java.io.File(file);
	if (!f.exists()) {
	    file = Settings.get("NullImageDir")+"/nodata."+type;
	}
	System.out.println("Content-disposition: "+dispo+"; filename="+last);
	
	// Note that we need exactly two new-lines.
	System.out.print("Content-type: image/"+type+"\n\n");
	
	java.io.FileInputStream ifs = new java.io.FileInputStream(file);
	byte[] block = new byte[32768];
	int len;
	while ( (len=ifs.read(block, 0, block.length)) > 0) {
	    System.out.write(block, 0, len);
	}
	System.out.close();
    }
    
    private static String join(String[] input, String sep) {	
	String out = "";
	String xsep = "";
	for (int i=0; i<input.length; i += 1) {
	    out += xsep + input[i];
	    xsep = sep;
	}
	return out;
    }
}
