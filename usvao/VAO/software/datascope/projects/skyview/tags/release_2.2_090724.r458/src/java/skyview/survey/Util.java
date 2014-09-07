package skyview.survey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Util {
    
     public static String replace(String original, String match, String replace, boolean all) {
	
	  Pattern p = Pattern.compile(match);
	  Matcher m = p.matcher(original);
	  StringBuffer sb = new StringBuffer();
	  if (all) {
	      while (m.find()) {
	          m.appendReplacement(sb, replace);
	      }
	  } else {
	      if (m.find()) {
		  m.appendReplacement(sb, replace);
	      }
	  }
	  m.appendTail(sb);
	 return sb.toString();
     }
    
    /** Copy a URL to a local file */
    public static void getURL(String url, String file) throws Exception {
	
	BufferedInputStream bi = new BufferedInputStream(
							 new java.net.URL(url).openStream());
	BufferedOutputStream bo = new BufferedOutputStream(new java.io.FileOutputStream(file));
	
	byte[] buffer = new byte[32768];
	
	int len;
	while ( (len=bi.read(buffer)) > 0) {
	    bo.write(buffer, 0, len);
	}
	bi.close();
	bo.close();
    }
    
    /** Open a stream from a resource or a file.
     *  If a file with the given name exists then return
     *  a stream from that file, otherwise try to open it as a system resource.
     *
     *  @param name  The name of the resource or file.
     */
    public static InputStream getResourceOrFile(String name) throws IOException {
	
	
	if (new java.io.File(name).exists()) {
            return new java.io.FileInputStream(name);
	} else {
	    return ClassLoader.getSystemClassLoader().getResourceAsStream(name);
	}
    }
    /** Replace one prefix string with another.
     *  This is used, e.g.,  to replace URLs with local file access.
     *  @param value    The string to be modifed.
     *  @param prefixes A 2 element array where we will look for the
     *                  first prefix at the beginning of value and if
     *                  found replace it with the second prefix.
     *  @return   The string with the prefix replaced if found.
     */
    public static String replacePrefix(String value, String[] prefixes) {
	if (prefixes.length < 2 || value == null) {
	    return value;
	}
        if (value.startsWith(prefixes[0])) {
	    // Replace the beginning of the URL with the local file specification
	    return prefixes[1]+value.substring(prefixes[0].length());
	} else {
	    return value;
	}
    }
}
