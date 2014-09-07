package net.ivoa.query;

import skyview.request.SourceCoordinates;
import net.ivoa.util.CGI;
import net.ivoa.util.ShowError;
import net.ivoa.util.Settings;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.InputStreamReader;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.io.BufferedReader;
import java.net.URL;

/** This class takes the parameters given and
*  invokes the VOClient tools to generate a
*  single VOTable.
*/

public class VOCli {

   /** The CGI fields set for this request */
   private CGI cgi;
    
   /** Does the XML prefix need to be written? */
   int prefix = 0;
   
   /** The input... */
   static CopyingInputStream cis;

   public static void main(String[] args) throws Exception {
      VOCli task = null;
      try {
	  task =  new VOCli(args);
	  task.run();
      } catch (Exception e) {
	 //  There is an anticipation that a VOTable will be
	 //  returned, so we'll create a VOTable with an
	 //  Info that gives the error.
	 printErrorVOTable(e, task.prefix);
	 System.err.println("Error in VOCli:");
	 e.printStackTrace(System.err);
	 System.err.println("Content was:\n---");
	 System.err.println(new String(cis.getCopy()));
	 System.err.println("---");
      }
   }
    
    static private void printErrorVOTable (Exception e, int prefix) {
	if (prefix < 1) {
            System.out.print(
              "Content-type: text/xml\n\n"
	    );
	} 
	
	if (prefix < 2) {
	    System.out.print(
              "<?xml version=\"1.0\"?>\n"
			     );
	}
	
	String sample = "";
	if (cis != null) {
	    String copy = new String(cis.getCopy());
	    if (copy.length() > 0) {
		sample = copy;
		if (copy.length() > 20) {
		    sample = copy.substring(0,20);
		}
	        sample = sample.replaceAll("&", "&amp;");
	        sample = sample.replaceAll(">", "&gt;");
	        sample = sample.replaceAll("<", "&lt;");
		sample = sample.replaceAll("\n", "");
	    }
	    sample = "  Remote response begins:"+sample;
	}
	
        System.out.print(
          "<!DOCTYPE VOTABLE SYSTEM \"http://us-vo.org/xml/VOTable.dtd\">\n"+
          "<VOTABLE version=\"1.0\">\n"+
          "<INFO name=\"Error\" value=\"Processing error:");
	String err = e.toString();
	if (sample.length() > 0) {
	    err += sample;
	}
	err = err.replace("\"", "'");
        System.out.print(
	  err+"\" />"+    
	  "</VOTABLE>\n");
    }
   public VOCli(String[] args) {}

   public void run() throws Exception {
      cgi = new CGI();
      String position= cgi.value("POSITION");
      String vLocal  = cgi.value("viewLocal");
      String vURL    = cgi.value("viewURL");
      if ( vLocal != null && vLocal.length() > 0 ) {
         System.out.println("Content-type: text/xml\n");
	 
	 // We encoded apostrophe's so that we could include
	 // the text in JavaScript variables.
         System.out.println(cgi.value("viewLocal").replaceAll("\\&apos", "'"));
         System.out.close();
      } else if (vURL != null  && vURL.length() > 0) {
         System.out.println("Content-type: text/xml\n");
         URL votable = new URL(vURL);
         BufferedReader in = new BufferedReader(
            new InputStreamReader(
               votable.openStream()));
         String inputLine;
         while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
	 }
	     
         in.close();
         System.out.close();
         //		} else if (position != null  && position.length() > 0 ) {
      } else if (position != null  && position.length() > 0 && position.indexOf(';')<0 ) {
         processSingle();
      } else {
         processMulti();
      }
   }

   void processSingle() throws Exception {
      String position = correctPosition( cgi.value("POSITION") );
      String id       = cgi.value("IVOID");
      String radius   = radius2degree( cgi.value("RADIUS"), cgi.value("units") );
      doSingle(position, radius, id);
   }

   /*
      This version of doSingle is a modified copy of doMulti as it 
      will generate a valid empty votable if there are no results
      Unfortunately, it also creates a "rec_id" column which some may find annoying,
      so I've created a SinglerXSL to strip off this column.
    
      We add in the filtering by STILTS to ensure that we get
      something that can be parsed on the far end.
    * 
    * We have the following sequence
    *    ResourceURL -> VOCli -> SinglerTransformation -> STILTS transformation -> STDOUT
   */
   void doSingle(String position, String radius, String id) throws Exception {
       
      String cmd = Settings.get("MultiTargetCmd");
      cmd += verbosity()+" "+"-rd "+radius +" "+id;
      System.err.println("VOCli: voclient command is:"+cmd);

      //	Start the vodata process ...
      ProcessBuilder pb      = new ProcessBuilder(cmd.split(" "));
      java.util.Map<String,String> env = pb.environment();
      env.put("LD_LIBRARY_PATH", "/www/server/vo/080821_src/curl/lib");
//      System.err.println("LD PATH is now:"+env.get("LD_LIBRARY_PATH"));
      addPath(pb);
      pb.directory(new File("/tmp"));
      Process proc = null;
       
       
      PipedOutputStream po = new PipedOutputStream();
      PipedInputStream  pi = new PipedInputStream();
      StiltsConverter   sc = new StiltsConverter(pi, System.out);
      
      po.connect(pi);
      
      try {
          proc = pb.start();

          //	Send the sources to the vodata command ...
          OutputStream os = proc.getOutputStream();
          os.write(position.getBytes());
          os.flush();
          os.close();
	  

          // Now transform the output...
          System.out.println("Content-type: text/xml\n");
	  prefix = 1;
	  cis = new CopyingInputStream(proc.getInputStream());
          StreamSource input = new StreamSource(cis);
          StreamSource xsl   = XSLTrans.getSource(Settings.get("SinglerXSL"));
	  CopyingOutputStream cs   = new CopyingOutputStream(po);
          StreamResult out   = new StreamResult(cs);
	  prefix = 2;
	  
	  // Start the StiltsConverter
	  new Thread(sc).start();
	  
	  // Start the XSLTransformation.
          new XSLTrans().transform(xsl, input, out);
	  po.close();
	  
	  System.out.flush();
	  
	  String output = new String(cs.getCopy());
	  if (output.length() < 100) {
	      throw new Exception("No valid data found in resource at remote host");
	  }
	      
      } finally {
	  if (proc != null) {
              proc.destroy();	// added these because I noticed many vodata processes on heasarcdev
	  }
      }
   }

   void processMulti() throws Exception {
      StreamSource input = null;
      String sources = cgi.value("sources");
      String sourcesURL = cgi.value("sourcesURL");
      String position = cgi.value("POSITION");
      int limit = 5;
      String cgilimit = cgi.value("limit");
      if ( cgilimit != null ){
         limit = new Integer(cgi.value("limit"));
      }
      String positions = "";
      // Note bug that requires us to check for length > 1
      if (sources != null  && sources.length() > 1) {
         positions = getSources( new StreamSource(new StringReader(sources)), limit);
      } else if (sourcesURL != null && sourcesURL.length() > 0) {
         positions = getSources( new StreamSource(sourcesURL), limit );
      } else if (position != null && position.length() > 0) {
//         positions = position.replaceAll("\\s*;\\s*","\n");

         String [] positions_array = position.split("\\s*;\\s*");
         for ( int i=0; i<positions_array.length; i++ ) {
            positions += correctPosition( positions_array[i] );
         }





      } else {
         new ShowError("query").fail("No position inputs"+position);
         System.exit(-1);
      }
      String id     = cgi.value("IVOID");
      String radius = radius2degree( cgi.value("RADIUS"), cgi.value("units") );

      doMulti(positions, radius, id);
   }

   private void addPath(ProcessBuilder pb) {
      String cliDir = Settings.get("VOClientDir");
      if (cliDir != null && cliDir.length() > 0) {
         String path   = pb.environment().get("PATH");
         path = cliDir+":"+path;
         pb.environment().put("PATH", path);
      }
   }

   void doMulti(String sources, String radius, String id) throws Exception {
      String cmd = Settings.get("MultiTargetCmd");
      cmd += verbosity()+" "+"-rd "+radius +" "+id;

      //	Start the vodata process ...
      ProcessBuilder pb      = new ProcessBuilder(cmd.split(" "));
      addPath(pb);
      pb.directory(new File("/tmp"));
      
      Process      proc = null;
      
      try {
	  proc = pb.start();

          //	Send the sources to the vodata command ...
          OutputStream os = proc.getOutputStream();
          os.write(sources.getBytes());
          os.flush();
          os.close();

          System.out.println("Content-type: text/xml\n");

          // Now transform the output...
          StreamSource input = new StreamSource(proc.getInputStream());
          StreamSource xsl  = XSLTrans.getSource(Settings.get("CombinerXSL"));

          StreamResult out   = new StreamResult(System.out);
          new XSLTrans().transform(xsl, input, out);
          System.out.close();
	  
       } finally {
	  if (proc != null) {
              proc.destroy();	// added these because I noticed many vodata processes on heasarcdev
	  }
       }
   }

   String verbosity() {
      if (cgi.value("VERBOSITY") != null) {
         return " -vv";
      } else {
         return "";
      }
   }

   String getSources(StreamSource input, int count) throws Exception {
      StreamSource xsl  = XSLTrans.getSource(Settings.get("PositionsXSL"));
      StringWriter sw   = new StringWriter();
      StreamResult out  = new StreamResult(sw);
      new XSLTrans().transform(xsl, input, out);
      sw.close();
      //		return sw.toString();


      // only allow a limited number of sources
      // for the time being as this could take
      // quite a while.
      String[] all = sw.toString().split("\n");
      int i = 0;
      String selected = "";
      while ( i < count && i < all.length ) {
         selected += all[i++]+"\n";
      }
      return selected;
   }

   String correctPosition( String position ) throws Exception {
       
      double[] pos = null;
       
      try {
	  
          SourceCoordinates sc = new SourceCoordinates(position, "J2000", 2000, null);
          sc.convertToCoords();
          pos = sc.getPosition().getCoordinates();
	  
      } catch (Exception e) {
	  
	  throw new Exception("Uable to parse coordinates:"+position);
	  
      }
       
      if (pos == null) {
         throw new Exception("Unable to parse input coordinates: position");
      }
       
      return pos[0]+" "+pos[1]+"\n";
   }

   String radius2degree( String radius, String units ) {
      if (radius == null || radius.length() == 0) {
         radius = Settings.get("DefaultRadius");
      }
      if ( units != null && units.length() > 0 ) {
         Float r = new Float(radius);
         if ( units.equals("arcmin") ) {
            r /= 60;
         } else if ( units.equals("arcsec") ) {
            r /= 3600;
         } 
         radius = r.toString();
      }
      return radius;
   }
}
