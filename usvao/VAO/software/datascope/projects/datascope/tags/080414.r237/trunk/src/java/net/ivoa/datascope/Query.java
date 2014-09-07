package net.ivoa.datascope;

import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;


public class Query {
    
    
    public static void main(String[] args) throws Exception {
	Header hdr = new Header();
	hdr.addCSSFile(DS.getURLBase()+"css/styles.css");
	hdr.setTitle("DataScope Query");
	hdr.setBannerTitle("VO DataScope Query");
	
	hdr.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
	hdr.printBanner(System.out);
	
	printFile("startFile.inc");
	
	System.out.println("<script language=JavaScript>\n"+
			   "function check(){\n"+
			   "   var size = document.getElementById('size').value\n "+
			   "   if (!size.match(/^\\s*[\\d\\.]+\\s*$/)) {\n" +
			   "      alert('The entered size does not seem to be a number:'+size)\n"+
			   "      return false\n"+
			   "   } else if (size > 2) {\n "+
			   "      alert('The size you have selected ('+size+')is too large, the maximum is 2 degrees')\n"+
			   "      return false\n"+
			   "   }\n"+
			   "   var pos = document.getElementById('position').value\n"+
			   "   if (pos == null || pos.length == 0) {\n"+
			   "      alert('Please enter a position')\n"+
			   "      return false\n"+
			   "   }\n"+
			   "   url = document.dsform.action+'?position='+encodeURIComponent(pos)+'&size='+size\n"+
			   "   if (document.getElementById('skipcache').checked) {\n"+
			   "       url += '&skipcache=on'\n"+
			   "   }\n"+
			   "   if (document.getElementById('skipregsave').checked) {\n"+
			   "       url += '&skipregsave=on'\n"+
			   "   }\n"+
			   "   if (document.getElementById('skiplog').checked) {\n"+
			   "       url += '&skiplog=on'\n"+
			   "   }\n"+
			   "   zopen(url)\n"+
			   "   return false\n"+
			   "}\n"+
			   "function zopen(url) {\n"+
			   "   var win = window.open(url, 'DSW', 'scrollbars,resizable,status,titlebar')\n"+
			   "   win.focus()\n"+
			   "   return false"+
			   "}\n"+
			   "</script>\n"+
			   "<noscript><p><div bgcolor='#ff0000'><hr><h3>**** JavaScript is not enabled. ****</h3>"+
			   " DataScope makes extensive use of JavaScript.  Please enable JavaScript and reload before proceeding.<p><hr></div>"+
			   "</noscript>");
			   
			   
			      
			       
	System.out.println("<p>What do we  know about a given point or region in the sky?<p>"+
			   "To find out, just enter a target or position. "+
			   "The NVO DataScope will show you the results from hundreds of resources.");
	
	System.out.println(
	  "<div bgcolor='#DDFFFF'>"+		   
	  "<form name=dsform id=dsform action="+DS.getCGIBase()+"jds.pl method=GET onsubmit='return check()'>"+
          "<table>"+
          "<tr><th class=right colspan=2><label for=position>Position:</label></th><td colspan=2><input size=40 id=position name=position value=''></td></tr>"+
	  "<tr><td colspan=2></td><td colspan=2><font size=-1> Use a target name (e.g., 3c273) or position (e.g., 10 10 10.1, 20 20 20.2)</font></td></tr>"+
          "<tr><th class=right colspan=2><label for=size> Size:</label></th><td colspan=2><input id=size size=10 name=size value=0.25 id=size>(in degrees, max is 2)</td></tr>"+
	  "<td class=right><label for=submit><b>Run query:</b></label></td><td> <input id=submit type=submit></td><td><input type=reset></td></tr>"+
	  "<tr><td colspan=4><label for=skipcache> <b>Skip cache?</b><input id=skipcache  name=skipcache type=checkbox>&nbsp;&nbsp;"+
          "<label for=skipregsave><b>Refresh registry?</b></label><input  id=skipregsave name=skipsave type=checkbox></td></tr>"+
          "<tr><td colspan=4><label for=skiplog>Do not add to list of recent queries?</label><input id=skiplog name=skiplog type=checkbox></td></tr>"+
	  "</table></form></div>");
	
	printLog(5);
	
	printFile("qryHelp.inc");
	
	new Footer().print(System.out);
    }
    
    static void printFile(String name) {
	File f = new File(DS.getURLHome()+name);
	if (!f.exists()) {
	    return;
	}
	BufferedReader bf = null;
	try {
	    bf = new BufferedReader(new FileReader(f));
	    String line;
	    while ( (line = bf.readLine()) != null) {
		System.out.println(line);
	    }
	} catch (Exception e) {
	} finally {
	    if (bf != null) {
		try {
		    bf.close();
		} catch (Exception e) {
		}
	    }
	}
    }
	      
						   
	
        
    static void printLog(int max) throws Exception {
	String stub = DS.getCGIBase()+"jds.pl?";
	
	String log = DS.getQueryLog();
	String result= null;
	if (new File(log).exists()) {
	    RandomAccessFile raf = null;
	    try {
	        raf = new RandomAccessFile(log, "r");
	        long size = raf.length();
	        if (size == 0) {
		    return;
	        }
		long read = size;
	        if (size > 1000) {
		    read = 1000;
	        }
	        if (size > read) {
		    raf.seek(size-read);
	        }
		byte[] arr = new byte[(int)read];
		raf.readFully(arr);
		result = new String(arr);
		
	    } finally {
		if (raf != null) {
		    raf.close();
		}
	    }
	    if (result != null  && result.indexOf('^') >= 0) {
		result = result.substring(result.indexOf('^'));
		String[] lines = result.split("\n");
		if (lines.length < max) {
		    max = lines.length;
		}
		
		System.out.println("Some recent queries:<br><dl>");
		for (int i=lines.length-max; i<lines.length; i += 1) {
		    String[] fields = lines[i].substring(1).split("\\|");
		    String url = stub + "position="+DS.encode(fields[0])+"&size="+fields[3]+"&errorcircle="+fields[4];
		    // These seem to get de-escaped once, so we
		    // do a double here
		    url = url.replace("%", "%25");
		    System.out.println("<dd><a href=\"javascript: void zopen('"+url+"')\">"+fields[0]+" ("+fields[3]+")</a>\n");
		}
		System.out.println("</dl>");
	    }
	}
    }
}
