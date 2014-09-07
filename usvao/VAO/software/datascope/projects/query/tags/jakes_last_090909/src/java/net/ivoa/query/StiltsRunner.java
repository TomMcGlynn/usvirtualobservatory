package net.ivoa.query;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import net.ivoa.util.CGI;
import net.ivoa.util.Settings;
import uk.ac.starlink.ttools.Stilts;

/** This class runs a STILTS command
*  on a VOTable and return a filtered VOTable.
*  This class using the VO Setting SaveDir which
*  specifies a directory into which external URLs
*  are to be read and saved.
* 
*  This program does not handle exceptions.
*  These are presumed to be handled in the calling program.
*/
public class StiltsRunner {

   public static void main(String[] args) throws Exception {
      new StiltsRunner().go();
   }

   void go() throws Exception {

      CGI params = new CGI();
      String url = params.value("url");

      String cmdString = getStiltsCmd(params);

      int hash = url.hashCode();
      String oldFile = Settings.get("savedir")+"/"+hash+".xml";


      if (!new File(oldFile).exists()) {
         prepareInput(url, oldFile);
         oldFile = "-";
      }

      String[] arguments = {
         "tpipe",
         "in="+oldFile,
         "ifmt=votable",
         "omode=cgi",
         "ofmt=votable",
         "cmd="+cmdString
      };

      Stilts.main(arguments);
   }

   String getStiltsCmd(CGI params) {

      String    min = params.value("minrow");
      String    max = params.value("maxrow");

      String    sort    = params.value("sortcolumn");
      String[]  filters = params.values("constraint");

      if (min == null) {
         min = "1";
      }
      String delta = "20";
      if (max == null) {
         max = Settings.get("RowBufferSize");
         if (max == null) {
            max = "20";
         }
         int start = Integer.parseInt(min);
         int end   = Integer.parseInt(max);
         if (end >= start) {
            delta = ""+(end-start+1);
         } else {
            if (Settings.has("RowBufferSize")) {
               delta = Settings.get("RowBufferSize");
            }
         }
      }

      // First apply the constraints, then sort, then
      // select rows.

      String cmd = "";
      String sep = "";

      if (filters != null && filters.length > 0) {

         String and = "";

         for (String filter: filters) {
            cmd += and + filter;
            and = " && ";
         }

         cmd = "select '"+cmd+"'";
         sep = ";";
      }

      if (sort != null) {
         String[] fields = sort.split("\\|");
         String desc = "";
         if (fields.length == 2 && fields[1].equals("desc") ) {
            desc = "-down";
         }
         cmd += sep + "sort "+desc+" "+fields[0];
         sep = ";";
      }

      cmd += sep + "head "+max+"; tail "+delta;

      return cmd;
   }


   void prepareInput(final String url, final String file) throws Exception {

      final PipedInputStream      pi = new PipedInputStream();
      final PipedOutputStream     po = new PipedOutputStream(pi);
      final File  old = new File(file+".x");
      final FileOutputStream      fo = new FileOutputStream(old);

      Runnable copier = new Runnable() {

         public void run() {
            try {
               // Just slurp the URL.  Write
               // it first to the standard output and
               // then to a file.
               URL u          = new URL(url);
               byte[] buffer  = new byte[32768];

               InputStream      is = u.openStream();
               int len;

               // Write to standard out and file.
               while ( (len=is.read(buffer)) > 0) {
                  po.write(buffer, 0, len);
                  fo.write(buffer, 0, len);
               }
               po.close();
               is.close();
               fo.close();

               File nw  = new File(file);
               old.renameTo(nw);

            } catch (Exception e) {
               System.err.println("StiltsRunner caught:"+e);
               e.printStackTrace(System.err);
            }
         }
      };

      System.setIn(pi);
      new Thread(copier).start();

   }
}
