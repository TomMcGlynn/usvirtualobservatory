package net.ivoa.datascope;

import java.util.HashMap;
import net.ivoa.util.CGI;

import net.nvo.Header;
import net.nvo.Footer;

import java.io.ObjectInputStream;
import java.io.FileInputStream;

public class MetaDisplay {
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi       = new CGI();
	String id     = cgi.value("id");
	String cache  = cgi.value("cache");
	String status = cgi.value("status");
	String[] flds;
	if (status != null) {
	    flds = status.split(",");
	} else {
	    flds = new String[0];
	}
	
	try {
	    ObjectInputStream is = new ObjectInputStream(new FileInputStream(DS.baseToHome(cache) + DS.getMetadataFile()));
	    HashMap<String, HashMap<String, String[]>> meta = (HashMap<String, HashMap<String, String[]>>)is.readObject();
	
            HashMap<String, String[]> result = meta.get(id);
	    if (result == null) {
	        Response.error("Unable to find id:"+id+" in saved metadata");
	    }
	
	    Header hdr = new Header();
	    hdr.addCSSFile(DS.getURLBase()+"css/styles.css");
	    hdr.printHTTP(System.out);
	    hdr.printHTMLHeader(System.out);
	    
	    System.out.println("<h3> Metadata and Status for \'"+result.get("ShortName")[0]+"\'</h3>");
	    
	    if (flds.length > 2) {
		int stat = -1;
		try {
		    stat = Integer.parseInt(flds[2]);
		} catch (Exception e) {
		    // Just go with default which will do nothing.
		}
		if (stat == 0) {
		    System.out.println("<b>Resource status:</b>No results were found in the requested region.<p>");
		} else if (stat == 2) {
		    System.out.println("<b>Resource status:</b>An error was encountered querying this resource.  See the 'Errors' pane for details<p>");
		} else if (stat == 3) {
		    System.out.println("<b>Resource status:</b>This resource has not yet returned any information.<p>");
		} else if (stat == 1) {
		    System.out.println("<hr>");
		    System.out.println("<b>Resource status:</b<br>");
		    String type = result.get("ServiceType")[0];
		    boolean hasData = type.indexOf("SIA") >= 0;
		    if (hasData) {
			System.out.println("&nbsp;&nbsp;This resource returns a table file that can index FITS and non-FITS data. If you view "+
					   "the resource in the resource pane, you can immediately download any of these files, or you can add FITS "+
					   "files to a shopping cart to be sent to analysis tools or downloaded in a tar file.<p>");
					   
					   
			if (flds.length > 5) {
			    System.out.println("&nbsp;&nbsp;Number of FITS files available:"+flds[4]+" <br>");
			    System.out.println("&nbsp;&nbsp;Number of FITS files selected to shopping cart:"+flds[1]+"<br>");
			    System.out.println("&nbsp;&nbsp;Number of non FITS files (GIF, JPEG, HTML, ...) available:"+flds[5]+"<p>");
			}
		    } else {
			System.out.println("&nbsp;&nbsp;This resource provides only catalog data.<br>");
			if (flds.length > 4) {
			    System.out.print("&nbsp;&nbsp;The number of rows returned is:");
			    String sep = "";
			    for (int i=3; i<flds.length-1; i += 1) {
			        System.out.print(sep+flds[i]);
			        sep = ", ";
			    }
			    System.out.println("<br>\n");
			    if (flds.length != 5) {
			        System.out.println("&nbsp;&nbsp;Multiple tables were found.<br>");
				System.out.println("&nbsp;&nbsp;Total count:"+sep+flds[flds.length-1]+"<br>");
			    }
			}
		    }
		    if (flds[0].equals("1") || flds[0].equals("true")) {
			System.out.println("&nbsp;&nbsp;You have selected this table in your shopping cart<br>");
		    } else {
			System.out.println("&nbsp;&nbsp;You have not selected this table in your shopping cart<br>");
			System.out.println("&nbsp;&nbsp;To select the table click on its checkbox in the 'Resources' pane<br>");
		    }
		    if (hasData) {
			System.out.println("&nbsp;&nbsp;Note that the table and any FITS files it indexes are selectable separately.<br>");
		    }
		    System.out.println("<hr>");
		}
	    }
		
	    System.out.println("<p>");
	    System.out.println("<table>");
	    
	    printLine("Title", result);
	    printLine("ShortName", result);
	    printLine("Description", result);
	    String[] keys = result.keySet().toArray(new String[0]);
	    java.util.Arrays.sort(keys);
	    
	    for (String key: keys) {
		if (!key.equals("Title") && !key.equals("ShortName") && !key.equals("Description")) {
		    printLine(key, result);
		}
	    }
	    System.out.println("</table><p><hr><p>");
	    new Footer().print(System.out);
	} catch (Exception e) {
	    Response.error("Error in metadata display:"+e);
	}
    }
    
    private static void printLine(String key, HashMap<String, String[]> resource) {
	
	String[] values = resource.get(key);
	if (values == null || values.length == 0) {
	    return;
	}
	String value = "";
	String sep   = "";
	for (String curr: values) {
	    value += sep+curr;
	    sep    = ", ";
	}
	if (value.toLowerCase().equals("not provided")) {
	    return;
	}
	System.out.println("<tr><td align=right><b>"+key+"</b></td><td>&nbsp;&nbsp;</td><td>"+value+"</td></tr>");
    }
}
