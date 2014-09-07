package net.nvo;

import java.util.ArrayList;
import java.io.PrintStream;
import net.ivoa.util.Settings;

/** This class writes out a standard NVO Header.
*  Settings used:
*  <dl>
*    <dt>DOCBASE<dd>The root of the document area for this application.
*    <dt>CSSFILES<dd> A list of CSS files used in the application.
*    <dt>RightBoxText<dd> The text to be rendered in the right banner box.
*  </dl>
*/
public class Header {

   /** The list of CSS files to be inserted in the document header */
   private ArrayList<String> cssFiles = new ArrayList<String>();


   /** The list of JavaScript files to be inserted in the document header */
   private ArrayList<String> jsFiles  = new ArrayList<String>();

   /** The main text to be placed in the NVO banner. */
   private String bannerTitle = "National Virtual Observatory";

   /** The text to be presented in the box to the right of the banner. */
   private String rightBox    = "";

   /** The list of buttons to place under the banner */
   private ArrayList<String> bannerTokens = new ArrayList<String>();

   /** The HTML title of the page */
   private String pageTitle = "";

   /** The JavaScript method to call when loading is complete */
   private String onLoad = null;
    
   /** An application icon. */
   private String appIcon    = null;
   private String appAltText = null;

   /** Create a new header instance */
   public  Header() {

      String[] cssFiles    = Settings.getArray("CSSFiles");
      for (String file: cssFiles) {
         // If its not a URL prefix it with the document base.
         if (file.indexOf(":") < 0) {
            file = Settings.get("docbase", "")+file;
         }
         addCSSFile(file);
      }

      String rightBoxText    = Settings.get("RightBoxText","");
      String rightBoxImg     = Settings.get("RightBoxImage","");
      String link            = Settings.get("RightBoxLink","");
      setRightBox(rightBoxImg, rightBoxText, link);
      addToken(Settings.get("Portal"), "VAO Home");
   }
    
   /** Set the application icon and alternate text */
   public void setIcon(String url, String altText) {
       this.appIcon        = url;
       this.appAltText = altText;
   }

   /** Set the main title on the banner */
   public void setBannerTitle(String banner) {
      bannerTitle = banner;
   }

   /** Set the HTML title */
   public void setTitle(String title) {
      pageTitle = title;
   }

   /** Set the JavaScript method to be called when the page is loaded. */
   public void setOnLoad(String action) {
      onLoad = action;
   }

   /** Add a JavaScript file to be included in the HTML header */
   public void addJavaScript(String file) {
      jsFiles.add(file);
   }

   /** Add a CSS file to be included in the HTML header */
   public void addCSSFile(String file) {
      cssFiles.add(file);
   }

   /** Add a button to be added under the banner.
   *  @param token The text to be rendered in the token.
   */
   public void addToken(String token) {
      bannerTokens.add(token);
   }

   /** Add a button to be added under the banner. 
   *  @param token The text to be rendered in the token.
   *  @param url   The URL to which the token is to be a link.
   */
   public void addToken(String url, String token) {
      bannerTokens.add("<a href='"+url+"'>"+token+"</a>");
   }

   /** Add a button to be added under the banner.
   *  @param token The text to be rendered in the token.
   *  @param url   The URL to which the token is to be a link.
   *  @param title the tooltip/title text for the link.
   */
   public void addToken(String url, String token, String title) {
      bannerTokens.add("<a href='"+url+"' title='"+title+"'>"+token+"</a>");
   }

   /** Add a button to be added under the banner.
   *  @param token The text to be rendered in the token.
   *  @param url   The URL to which the token is to be a link.
   *  @param title the tooltip/title text for the link.
   *  @param onclick
   */
   public void addToken(String url, String token, String title, String onclick) {
      bannerTokens.add("<a href='"+url+"' title='"+title+"' onclick='"+onclick+"'>"+token+"</a>");
   }

   /** Set the text to be presented to the right of the banner */
   public void setRightBox(String input) {
      this.rightBox = input;
   }

   /** Set the right text to be presented to the right of the banner.
   *  @param img   Image to be shown.
   *  @param text  Text to be shown under the image.
   *  @param link  URL that image and text should link to.
   */
   public void setRightBox(String img, String text, String link) {

      if (img == null && text == null) {
         return;
      }
      String linkPrefix = "";
      String linkSuffix = "";
      if (link != null && link.length() > 0) {
         linkPrefix = "<a href='"+link+"'>";
         linkSuffix = "</a>";
      }

      String right = "";
      if (img != null) {
         right += linkPrefix + img + linkSuffix + "<br />";
      }
      if (text != null) {
         linkPrefix  = "<span class='nvolink'>"+linkPrefix;
         linkSuffix += "</span>";
         right += linkPrefix + text + linkSuffix;
      }
      setRightBox(right);
   }

   /** Print out the banner and associated boxes. */
   public void printBanner(PrintStream out) {
      addToken("http://us-vo.org/help/contact.cfm", "Contact Us");
      int nb = bannerTokens.size();
       
      String icon = "";
      if (appIcon != null) {
	  icon = "<TD width=50><img src='"+appIcon+"'";
	  if (appAltText != null) {
	      icon += " alttext='"+appAltText+"'";
	  }
	  icon += "></TD>";
      } else if (appAltText != null) {
	  icon = "<TD width=50>"+appAltText+"</TD>";
      }
       
      out.println(
         "<table id='header_main_table' width='100%' border='0' cellpadding='0' cellspacing='0'><tr>\n" +
         "<td width='112px' align='center' valign='top'>\n" +
         "<a href='http://www.us-vo.org' class='nvolink'>\n" +
         "<img src='http://www.usvao.org/mgmt/logos/VAO_logo_100.png' alt='VAO Logo' /></a>\n" +
         "<br />\n" +
         "<span class='nvolink'><a href='http://www.us-vo.org/'>Virtual Astronomical Observatory</a></span><br />\n" +
         "</td>\n" +
	 icon +
         "<td valign='top'><table id='header_center_table' width='100%'>\n"+
         "<tr><td colspan='"+nb+"' id='nvoapptitle' class='nvoapptitle'>" + bannerTitle + "</td></tr>\n" +
         "<tr id='navbar'>");
      String tdclass = "navlink";
      int token_counter = 0;
      int cell_width = ( nb > 0 ) ? 100 / nb : 100;   // if nb==0, it doesn't really matter what cell_width is
      for (String token: bannerTokens) {
         if ( ++token_counter == nb ) tdclass = "helplink"; // the "contact" link, which should be the last one, has a different style
         out.println("<td width='"+cell_width+"%' class='"+tdclass+"'>"+token+"</td>");
      }
      out.println(
         "</tr>\n"+
         "</table><!-- header_center_table --></td>\n"+
         "<td width='112px' align='center' valign='top'>"+rightBox +"</td>\n"+
         "</tr></table><!-- header_main_table -->");
   }

   /** Print the HTML Header element and content */
   public void printHTMLHeader(PrintStream out) {

      String title = pageTitle;
      if (title == null || title.length() == 0) {
         title = "DataScope page";
      }
      out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/2002/REC-xhtml1-20020801/DTD/xhtml1-transitional.dtd'>");
      out.println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
      out.println("<head>\n<title>"+title+"</title>");

      for(String sheet: cssFiles) {
         out.println("<link rel='stylesheet' href='"+sheet+"' type='text/css' />");
      }
      for (String script: jsFiles) {
         out.println("<script src='"+script+"' language='JavaScript' type='text/javascript'></script>");
      }

      String v = "";
      if (onLoad != null) {
         v = "onload=\""+onLoad+"\"";
      }
      out.println("</head><body "+v+">");
   }

   /** Print the HTTP header. */
   public static void printHTTP(PrintStream out) {
      out.println("Content-type: text/html\n");
   }

   /** Create a test HTML document */
   public static void main(String[] args) {
      Header hdr = new Header();
      Header.printHTTP(System.out);
      hdr.addToken("FirstToken");
      hdr.addToken("#", "SecondToken");
      hdr.addToken("#", "ThirdToken", "Title for third token");
      hdr.setBannerTitle("Banner Title");
      hdr.setTitle("Page Title");
      hdr.printHTMLHeader(System.out);
      hdr.printBanner(System.out);
      System.out.println("<h3> Test Page </h3>Test content");
      new Footer().print(System.out);
   }
}
