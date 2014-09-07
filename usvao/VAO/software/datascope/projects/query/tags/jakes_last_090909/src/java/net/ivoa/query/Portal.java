package net.ivoa.query;

import net.nvo.Header;
import net.nvo.Footer;
import net.ivoa.util.CGI;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;

public class Portal {

   public static void main(String[] args) throws Exception {

      CGI params = new CGI();

      Header h = new Header();
      h.setBannerTitle("NVO Data Portal");
      h.setTitle("NVO Data Portal");

      h.addCSSFile(Settings.get("docbase","")+Settings.get("CSS_PATH","css/")+"styles.css");
      h.addToken("http://us-vo.org", "NVO home page");
      h.addToken(Settings.get("Registry"), "Directory", "Search for resources using keywords");
      h.addToken(Settings.get("Inventory"), "Inventory", "Search for resources that have coverage for a list of positions");
      h.addToken(Settings.get("DataScope"), "DataScope", "Search for all data on a given position/object");
      h.addToken(Settings.get("VIM"), "VIM",  "Search and combine data from selected resources and targets");

      h.addToken(Settings.get("docbase","")+"scripting.html", "Scripting", "How to run retrieval scripts on your machine");
      h.addToken(Settings.get("docbase","")+"Portal.html", "Help", "Help for the NVO Portal");
      h.addToken("http://us-vo.org/feedback/index.cfm", "NVO Feedback");

      h.printHTTP(System.out);
      h.printHTMLHeader(System.out);
      h.printBanner(System.out);

      SettingsFilter.filter("portal.html");
      new Footer().print(System.out);
   }
}
