package net.ivoa.query;

import net.ivoa.util.CGI;
import net.ivoa.util.ShowError;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;
import net.nvo.Header;
import net.nvo.Footer;
import static java.net.URLEncoder.encode;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.File;

public class Querier {   

   public static void main(String[] args) {
      CGI cgi = new CGI();
      String message[] = new String[1];
      if (!validQuery(cgi,message)) {
         BaseQuery.run(cgi, message[0]);
      } else {
         run(cgi);
      }
   }

   static void run(CGI cgi) {
      for (String key: cgi.keys()) {
         String val = cgi.value(key).trim();
         if (val != null && val.length() > 0) {
            Settings.put(key, val);
         }
      }

      if ( Settings.get("position","").contains("http") ) {
         Settings.put("sourcesURL", Settings.get("position") );
         Settings.remove("position");
      }

      // All-sky overrides radius if no position is
      // specified, but is overriden by it if a position
      // is specified.
      if (Settings.get("allsky", "").equals("checked")) {
         if (!Settings.has("position")) {
            Settings.put("position", "0. 0.");
            Settings.put("radius", "180");
         } else {
            if (!Settings.has("radius")) {
               Settings.put("radius", "180");
            }
         }
      }

      if (!Settings.has("Radius")) {
         Settings.put("Radius", "0.25");
      } 

      Querier q = new Querier();

      int i = 0;
      if (Settings.has("position") && Settings.get("position").length()>0) { i++; }
      if (Settings.has("sources") && Settings.get("sources").length()>0) { i++; }
      if (Settings.has("sourcesURL") && Settings.get("sourcesURL").length()>0) { i++; }
      if (Settings.has("viewLocal") && Settings.get("viewLocal").length()>0) { i++; }
      if (Settings.has("viewURL") && Settings.get("viewURL").length()>0) { i++; }

      try {
         if (Settings.has("position")) { 
            q.run("singleStart.js", "singleTemplate", cgi);
         } else {
            if ( Settings.has("viewLocal") ) {
              Settings.put("viewLocal", cgi.value("viewLocal").replace("'","\\\\'").replace("\n","") );
            }
            q.run("multiStart.js", "multiTemplate", cgi);
         }
      } catch (Exception e) {
         BaseQuery.run(cgi,"Error in query:"+e);
      }
   }

   void run(String startupJS, String templateSetting, CGI cgi) throws Exception {

      String params="IVOID=" + encode(Settings.get("IVOID",""), "UTF-8");

      if (Settings.has("Position")) {
         params += "&POSITION=" + encode(Settings.get("position"), "UTF-8");
         params += "&RADIUS="   + encode(Settings.get("radius"), "UTF-8");
         params += "&units="    + encode(Settings.get("units",""), "UTF-8");
      }

      if (Settings.has("Verbosity")) {
         params += "&VERBOSITY=yes";
      }

      Settings.put("CGIParams", params);

      Header hdr = new Header();
      String title = "Query Results";
      if (Settings.has("view")) {
	  title = "Table Results";
      }
      String sname = Settings.get("ShortName");
      if (sname != null) {
         title += ": "+sname;
      }
      hdr.setBannerTitle(title);
      hdr.setTitle(title);


      // Non-cgi need to be full paths as may be accessed through /cgi-bin/
      hdr.addCSSFile(Settings.get("docbase","")+Settings.get("CSS_PATH","css/")+"style.css");
      hdr.addCSSFile(Settings.get("docbase","")+Settings.get("CSS_PATH","css/")+"voview.css");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"sarissa.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"cookie.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+startupJS);
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"jquery.pack.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"jquery.vo.convert.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"jquery.simplemodal.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"vo_graph.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"statemanager.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"query.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"filter.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"fsm.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"voview.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"tablednd.js");
//      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"voformatter.js");
      hdr.addJavaScript(Settings.get("docbase","")+Settings.get("JS_PATH","js/")+"export.js");

      if (cgi.value("viewform") == null) {
          hdr.addToken("javascript:void(0)", "Modify&nbsp;Query", "Modify Current Query", "document.getElementById(\"requeryform\").submit()");
          hdr.addToken(Settings.get("docbase",""), "New&nbsp;Query", "Start New Query");
      } else {
          hdr.addToken("javascript:void(0)", "New Table", "View another table", "document.getElementById(\"requeryform\").submit()");
      }
      hdr.addToken(Settings.get("docbase","")+"scripting.html", "Scripting", "How to run retrieval scripts on your machine");
      hdr.addToken(Settings.get("docbase","")+"helpInc.html", "Help", "Help for the SimpleQuery service");
      hdr.printHTTP(System.out);
      hdr.printHTMLHeader(System.out);

      //	This is an AJAX call to update the page title to include
      // the short name of the catalog.
      System.out.println("<script>");
      System.out.println("$(function() {");
      System.out.println("  $.get(\""+Settings.get("cgibase","")+"getIVOnames.pl\", { IVOID:\""+Settings.get("IVOID","")+"\"},");
       
      /*
         ivo://nasa.heasarc/a1       A1
         ivo://nasa.heasarc/a1point  A1POINT
         ivo://nasa.heasarc/ascalss  ASCA LSS
         Note that the ShortName can be more than one "word"!
      */
       
      System.out.println("    function(data){");
      System.out.println("      if(data){");
      System.out.println("        names = data.split('\\n');");
      System.out.println("        ivo = names[0].split(/\\s+/);");
      System.out.println("        ivo.shift();");
      System.out.println("        $('#nvoapptitle').html('Query Results:&nbsp;'+ivo.join(' ')); ");
      System.out.println("      }");
      System.out.println("    }");
      System.out.println("  );");
      System.out.println("  if (! PreferralURL) $('#gobackto').hide();");
      System.out.println("});");
      System.out.println("</script>");

      hdr.printBanner(System.out);

      // This is where data/TableResults.html is actually printed
      if ( Settings.has("sources") )
         Settings.put("sources",Settings.get("sources").replaceAll("'","&apos;"));
      SettingsFilter.filter(Settings.get(templateSetting), true); // 'true' deletes lines where the matching variable value is ""

      // the outputform should contain any and all passed parameters, 
      // so we need to do this dynamically here, rather than in a 
      // template as previously done.  Since 'sources' and 'sourcesURL'
      // are input, they shouldn't be included in the output form.  A
      // placeholder is used for 'sources' as it will be our output.
      // I don't know what the _spec1 and _spec2 are for.  I think that
      // VIM or Inventory needs one of them, at least.
      System.out.println( "<div id='outputformdiv' style='display:none;'>" );
      System.out.println( "<form id='outputform' action='"+Settings.get("cgibase","")+"viewresults.pl' method='post'>" );
      for (String key: cgi.keys()) {
         if ( !( key.equals("sources") 
            || key.equals("sourcesURL") 
            || key.equals("viewLocal") 
            || key.equals("viewURL") 
            || key.equals("limit") 
            || key.equals("RADIUS") 
            || key.equals("POSITION") 
            || key.equals("units") 
            || key.equals("autosubmit") 
            ) ) {
            String val = cgi.value(key).trim();
            if (val != null && val.length() > 0) {
               System.out.println( "<input type='hidden' name='" + key + "' value='" + val + "' />" );
      
	    }
	 }	
      }
      System.out.println( "<input type='hidden' name='SourcesProvenance' value='TableViewer' >");			    
      System.out.println( "<input type='hidden' name='findResources' value='1' />" );  // for Inventory
//      System.out.println( "<input type='hidden' name='radius'        value='10' />" );        // for Inventory
//      System.out.println( "<input type='hidden' name='units'         value='arcsec' />" );     // for Inventory
      System.out.println( "<input type='hidden' name='toolName'      value='sources' />" ); // for VIM
      System.out.println( "<input type='hidden' name='sources' />" );  // placeholder
      System.out.println( "<input type='hidden' name='referralURL'   value='http://heasarc.gsfc.nasa.gov"+Settings.get("cgibase","")+"/query.sh'/>" );
      System.out.println( "</form>" );

      System.out.println( "<form id='requeryform' action='"+Settings.get("cgibase","")+"' method='post'>" );
      for (String key: cgi.keys()) {
         if ( !( key.equals("autosubmit") 
            ) ) {
            String val = cgi.value(key).trim();
            if (val != null && val.length() > 0) {
               System.out.println( "<input type='hidden' name='" + key + "' value='" + val + "' />" );
      }	}	}
      System.out.println( "<input type='hidden' name='autosubmit' value='false' />" );// stop an auto-submit
      System.out.println( "</form> </div>" );

      new Footer().print(System.out,"myfooter");
   }

   static boolean validQuery(CGI cgi) {
      String ignore[] = new String[1];
      return validQuery(cgi, ignore);
   }

   static boolean validQuery(CGI cgi, String[] errorMessage) {
      // using String[] errorMessage array so that I can return a value
      // as Java apparently has no easier way to pass-by-reference
      // We need some validation in Java, as well as JavaScript,
      // since we may be passed lots of parameters.
      // The JavaScript validation, is quicker for interactive usage.
      int position_fields = 0;
      int radius_fields = 0;
      String cgiValues = "";
      if (cgi.count("POSITION")   > 0 && cgi.value("POSITION").length() > 1) {
         cgiValues += "POSITION:"+cgi.value("POSITION")+":<br/>";
         position_fields++; 
      }
      if (cgi.count("sourcesURL") > 0 && cgi.value("sourcesURL").length() > 1) {
         cgiValues += "sourcesURL:"+cgi.value("sourcesURL")+":<br/>";
         position_fields++; 
      }
      if (cgi.count("sources")    > 0 && cgi.value("sources").length()  > 1)     { 
         cgiValues += "sources:NOT SHOWING VALUE:<br/>";
         position_fields++;
      }
      if (cgi.count("viewLocal")  > 0 && cgi.value("viewLocal").length() > 1) { 
         cgiValues += "viewLocal:NOT SHOWING VALUE:<br/>";
         position_fields++;
      }
      if (cgi.count("viewURL")    > 0 && cgi.value("viewURL").length() > 1) { 
         cgiValues += "viewURL:"+cgi.value("viewURL")+":<br/>";
         position_fields++;
      }

      /* DON'T include the all-sky checkbox in this check */
      if ( position_fields >= 2 ) {
         errorMessage[0] = "Java Error: More than one of position, sources, sourcesURL or viewLocal specified.<br/>"+cgiValues;
         return false;
      }
      
      if (cgi.count("allsky")    == 1 && cgi.value("allsky").equals("checked")    )   { 
         cgiValues += "allsky:"+cgi.value("allsky")+":<br/>";
         position_fields++;
         radius_fields++;
      }
      /* DO include the all-sky checkbox in this check */
      if ( position_fields <= 0 ) {
         errorMessage[0] = "Java Error: No position, list of positions or all-sky specified.<br/>"+cgiValues;
         return false;
      }


      if (cgi.count("RADIUS")    > 0 && cgi.value("RADIUS").length() > 0) { 
         cgiValues += "RADIUS:"+cgi.value("RADIUS")+":<br/>";
         radius_fields++;
      }
      // if ( radius_fields == 0 )
      //    we'll use a default
      //
      if ( radius_fields > 1 ) {
         errorMessage[0] = "Java Error: More than one of radius or all-sky given.<br/>"+cgiValues;
         return false;
      }


      if ( !( cgi.count("viewLocal") > 0 && cgi.value("viewLocal").length() > 1 ) 
         && !( cgi.count("viewURL") > 0 && cgi.value("viewURL").length() > 0 ) 
         && !( cgi.count("IVOID") > 0 && cgi.value("IVOID").length() > 0 ) ){
         // need to have an IVOID with Query
         errorMessage[0] = "Java Error: IVOID required for queries.<br/>"+cgiValues;
         return false;
      }
      return true;
   }
}
