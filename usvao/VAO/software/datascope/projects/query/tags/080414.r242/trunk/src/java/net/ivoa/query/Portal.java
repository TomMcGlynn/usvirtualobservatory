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
	
	h.addCSSFile(Settings.get("DocBase")+"/css/styles.css");
	
	h.addToken("http://us-vo.org", "NVO home page");
	h.addToken("http://nvo.stsci.edu/voregistry/index.aspx", "Registry", "Search for resources using keywords");
	h.addToken("http://irsa.ipac.caltech.edu/applications/QuickStats/", "Inventory", "Search for resources that have coverage for a list of positions");
	h.addToken("http://heasarc.gsfc.nasa.gov/cgi-bin/vo/datascope/init.pl", "DataScope", "Search for all data on a given position/object");
	h.addToken("http://nesssi.cacr.caltech.edu/Vim/", "VIM",  "Search and combine data from selected resources and targets");
      	h.addToken(Settings.get("DocBase")+"/scripting.html", "Scripting", "How to run retrieval scripts on your machine");
      	h.addToken(Settings.get("DocBase")+"/Portal.html", "Help", "Help for the NVO Portal");
	h.addToken("http://us-vo.org/feedback/index.cfm", "NVO Feedback");
	
	h.printHTTP(System.out);
	h.printHTMLHeader(System.out);
	h.printBanner(System.out);
	
        SettingsFilter.filter("portal.html");
	
	new Footer().print(System.out);
    }
    
}
