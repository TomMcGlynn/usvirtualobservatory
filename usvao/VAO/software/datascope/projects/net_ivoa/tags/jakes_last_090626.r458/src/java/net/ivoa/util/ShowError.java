package net.ivoa.util;

/** This class is used to render error pages.
*/
public class ShowError {

   private String origin = "request";

   public ShowError(){};

   public ShowError(String or) {
      origin = or;
   }

   void writeHeader() {

      System.out.print("Content-type: text/html\n\n"+
         "<HTML></HEAD>\n"+
         "<TITLE>Error in "+origin+"</TITLE>"+
         "<script language='JavaScript'>\n"+
         "var vis='visible'\n"+
         "function flipVisible(elem) {\n"+
         "var x = document.getElementById(elem)\n"+
         "if (x == null) {\n"+
         "return\n"+
         "}\n"+
         "var vis = x.style.visibility\n"+
         "if (vis == 'visible') x.style.visibility = 'hidden'\n"+
         "else x.style.visibility ='visible'\n"+
         "return false\n"+
         "}\n"+
         "</script>\n"+
         "</HEAD><BODY>\n"+
         "<h2> Error in "+origin+"</h2>");
   }

   void writeFooter() {
      System.out.print("</BODY></HTML>\n");
   }

   public void fail(String message) {
      writeHeader();
      writeMessage(message);
      writeFooter();
   }

   public void fail(String message, Throwable t) {
      writeHeader();
      writeMessage(message);
      writeTraceback(t);
      writeFooter();
   }

   void writeMessage(String message) {
      System.out.println("<H2> Query error </H2>"+message);
   }

   void writeTraceback(Throwable t) {
      System.out.println("<br><input type=button name='See traceback details' value='See details' onclick='flipVisible(\"traceback\")'>");
      System.out.println("<div id=traceback style='visibility:hidden'>");
      System.out.println("<h3> Java Traceback on Error </h3>");
      System.out.println("<pre>");
      t.printStackTrace(System.out);
      System.out.println("</pre></div>");
   }
}
