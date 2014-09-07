package net.nvo;

import net.ivoa.util.SettingsFilter;

import net.ivoa.util.Settings;

/** Generate a Footer for a web page */
public class Footer {

   private String elementSeparator = "<hr noshade='noshade' />\n";

   public void setSeparator(String separator) {
      elementSeparator = separator;
   }

   public void print(java.io.PrintStream out) {
      print(out,"footer");// footer is default footer id (has some style)
   }

   public void print(java.io.PrintStream out, String footerid) {
      String[] footerFiles = Settings.getArray("FooterFiles");
      System.out.println("<div id='"+footerid+"'>");
      try {
         for (String file: footerFiles) {
            out.println(elementSeparator);
            SettingsFilter.filter(file, true);
         }
      } catch (Exception e) {
         e.printStackTrace(System.out);
         // Carry on
      }
      System.out.println("</div><!-- id='"+footerid+"' -->");
      System.out.println("</body>\n</html>");
   }
}
