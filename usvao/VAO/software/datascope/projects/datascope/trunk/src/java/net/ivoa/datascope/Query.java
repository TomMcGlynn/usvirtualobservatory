package net.ivoa.datascope;

import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import net.nvo.Header;
import net.nvo.Footer;
import net.ivoa.util.CGI;

public class Query {

	public static void main(String[] args) throws Exception {
		Header hdr = new Header();
		CGI cgi = new CGI();

		hdr.addCSSFile(DS.getURLBase() + "css/styles.css");
		hdr.addCSSFile(DS.getURLBase() + "css/net_nvo.css");
		hdr.addToken(DS.getURLBase() + "helpInc.html", "Help",
				"Help for DataScope");

		hdr.setIcon(DS.getURLBase() + "images/datascope50.png",
				"DataScope Icon");

		hdr.setTitle("DataScope Query");

		hdr.setBannerTitle("VAO Data Discovery: DataScope");

		Header.printHTTP(System.out);
		hdr.printHTMLHeader(System.out);
		hdr.printBanner(System.out);

		String value = "";
		if (cgi.count("position") != 0) {
			value = cgi.value("position");
			value = value.replaceAll("'", "\\'");
		}

		printFile("startFile.inc");

		System.out
				.println("<script language=JavaScript>\n"
						+ "function check(){\n"
						+ "   var size = document.getElementById('size').value\n "
						+ "   if (!size.match(/^\\s*[\\d\\.]+\\s*$/)) {\n"
						+ "      alert('The entered size does not seem to be a number:'+size)\n"
						+ "      return false\n"
						+ "   } else if (size > 2) {\n "
						+ "      alert('The size you have selected ('+size+')is too large, the maximum is 2 degrees')\n"
						+ "      return false\n"
						+ "   }\n"
						+ "   var pos = document.getElementById('position').value\n"
						+ "   if (pos == null || pos.length == 0) {\n"
						+ "      alert('Please enter a position')\n"
						+ "      return false\n"
						+ "   }\n"
						+ "   url = document.dsform.action+'?position='+encodeURIComponent(pos)+'&size='+size\n"
						+ "   if (document.getElementById('skipcache').checked) {\n"
						+ "       url += '&skipcache=on'\n"
						+ "   }\n"
						+ "   if (document.getElementById('skiplog').checked) {\n"
						+ "       url += '&skiplog=on'\n"
						+ "   }\n"
						+ "   zopen(url)\n"
						+ "   return false\n"
						+ "}\n"
						+ "function zopen(url) {\n"
						+ "   var win = window.open(url, 'DSW', 'scrollbars,resizable,status,titlebar')\n"
						+ "   win.focus()\n"
						+ "   return false"
						+ "}\n"
						+ "</script>\n"
						+ "<noscript><p><div bgcolor='#ff0000'><hr><h3>**** JavaScript is not enabled. ****</h3>"
						+ " DataScope makes extensive use of JavaScript.  Please enable JavaScript and reload before proceeding.<p><hr></div>"
						+ "</noscript>");

		System.out
				.println("<TABLE><TR><TD width=15%></TD><TD width=70%>\n"
						+ "<div bgcolor='#DDFFFF'>\n"
						+ "<form name=dsform id=dsform action="
						+ DS.getCGIBase()
						+ "jds.pl method=GET onsubmit='return check()'>"
						+ "<table>\n"
						+ "<tr><th class=right colspan=2><label for=position>Position:</label></th><td colspan=2><input size=40 id=position name=position value='"
						+ value
						+ "'></td></tr>\n"
						+ "<tr><td colspan=2></td><td colspan=2><font size=-1> Use a target name (e.g., 3c273) or position (e.g., 10 10 10.1, 20 20 20.2)</font></td></tr>\n"
						+ "<tr><th class=right colspan=2><label for=size> Size:</label></th><td colspan=2><input id=size size=10 name=size value=0.25 id=size>(in degrees, max is 2)</td></tr>\n"
						+ "<tr><td class=right colspan=2><label for=submit><b>Run query:</b></label></td><td> <input id=submit type=submit></td><td><input type=reset></td></tr>\n"
						+ "<tr><td colspan=3><label for=skipcache> <b>Skip cache?</b><input id=skipcache  name=skipcache type=checkbox>&nbsp;&nbsp;"
						+ "<td colspan=4><label for=skiplog>Do not add to list of recent queries?</label><input id=skiplog name=skiplog type=checkbox></td></tr>\n"
						+ "</table></form></div>\n");

		printFile("qryHelp.inc");

		printLog(5);

		System.out.println("</TD><TD width=15%></TD></TR></TABLE>");

		System.out.println("<p>" + DS.getVersionMessage());

		new Footer().print(System.out);
	}

	static void printFile(String name) {
		File f = new File(DS.getURLHome() + name);
		if (!f.exists()) {
			return;
		}
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(f));
			String line;
			while ((line = bf.readLine()) != null) {
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
		String stub = DS.getCGIBase() + "jds.pl?";

		String log = DS.getQueryLog();
		String result = null;
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
					raf.seek(size - read);
				}
				byte[] arr = new byte[(int) read];
				raf.readFully(arr);
				result = new String(arr);

			} finally {
				if (raf != null) {
					raf.close();
				}
			}
			if (result != null && result.indexOf('^') >= 0) {
				result = result.substring(result.indexOf('^'));
				String[] lines = result.split("\n");
				if (lines.length < max) {
					max = lines.length;
				}

				System.out.println("Some recent queries:<br><dl>");
				for (int i = lines.length - max; i < lines.length; i += 1) {
					String[] fields = lines[i].substring(1).split("\\|");
					String url = stub + "position=" + DS.encode(fields[0])
							+ "&size=" + fields[3] + "&errorcircle="
							+ fields[4];
					// These seem to get de-escaped once, so we
					// do a double here
					url = url.replace("%", "%25");
					System.out.println("<dd><a href=\"javascript: void zopen('"
							+ url + "')\">" + fields[0] + " (" + fields[3]
							+ ")</a>\n");
				}
				System.out.println("</dl>");
			}
		}
	}
}
