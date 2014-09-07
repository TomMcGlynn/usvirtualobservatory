package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.nvo.Header;
import net.nvo.Footer;

public class FitsImageWrapper {

   public static void main(String[] args) {

      CGI  cgi = new CGI();

      String sn      = cgi.value("sn");
      String index   = cgi.value("index");
      Header hdr     = new Header();
      hdr.setBannerTitle("Quicklook View for image "+index+" of "+sn);

      Header.printHTTP(System.out);
      hdr.printHTMLHeader(System.out);
      hdr.printBanner(System.out);

      System.out.println("<p>This page renders the FITS file using a simple logarithmic image scaling.</p>");
// the following generates code which makes validator.w3.org sad.
//      System.out.println("<p><img alt='fv.pl image' src='fv.pl?"+System.getenv("QUERY_STRING")+"'/></p>");
// replacing all the ampersands makes it happy!
      System.out.println("<p><img alt='fv.pl image' src='fv.pl?"+System.getenv("QUERY_STRING").replaceAll("&","&amp;")+"'/></p>");
      new Footer().print(System.out);
   }
}
