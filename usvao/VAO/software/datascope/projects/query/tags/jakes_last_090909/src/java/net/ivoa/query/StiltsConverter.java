package net.ivoa.query;

import uk.ac.starlink.ttools.Stilts;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** This class ensures that a VOTable is in the TABLEDATA format.
 */
public class StiltsConverter implements Runnable {
    
    InputStream  is = null;
    PrintStream  os = null;
    
    String[] args = {"votcopy",  "in=-", "out=-", "format=TABLEDATA"};
    
    public StiltsConverter(InputStream in, PrintStream out) {
	
	is = in;
	os = out;
	
    }
    
    public void run() {
	
        System.setIn(is);
	System.setOut(os);
	
	try {
	    Stilts.main(args);
	} catch (Exception e) {
	    System.err.println("Got STILTS exception:"+e);
	    throw new Error("Stilts error", e);
	}
    }
    
    public static void main(String[] args)  throws Exception {
	
	InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
        PrintStream os = new PrintStream(new FileOutputStream(args[1]));
	
	Thread th  = new Thread(new StiltsConverter(is,os));
	th.start();
    }
}
