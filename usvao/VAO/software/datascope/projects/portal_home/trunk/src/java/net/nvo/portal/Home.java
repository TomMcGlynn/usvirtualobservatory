package net.nvo.portal;

import net.nvo.Header;
import net.nvo.Footer;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;
import net.ivoa.util.CGI;

public class Home {
	public static void main(String[] args) { 
      CGI params = new CGI();
      show(params); 
   }

	static void show(CGI cgi) {
		Header h = new Header();
		h.setBannerTitle("Data Discovery with the NVO");
		h.setTitle("Data Discovery with the NVO");
		h.addCSSFile(Settings.get("CSS_PATH","css/")+"style.css");
		h.addJavaScript(Settings.get("JS_PATH","js/")+"parser.js");
		h.addJavaScript(Settings.get("JS_PATH","js/")+"jquery.pack.js");
		h.addToken("?page=tutorial", "Help");
		h.printHTTP(System.out);
		h.printHTMLHeader(System.out);
		h.printBanner(System.out);
		System.out.println("<div id='content' class='content'>");
      String page = cgi.value("page");
      if ( page != null && page.length() > 0) {
         if ( !page.contains(".html") ) {
            page += ".html";
         }
      } else {
         page = "home.html";
      }
      try {
         SettingsFilter.filter(page, true);// true deletes rows with matching filters == null
      } catch (Exception e){}
      
		System.out.println("</div><!-- id='content' -->\n<br/><br/><br/>");
		new Footer().print(System.out,"footer");
	}
}
