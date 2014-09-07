package net.ivoa.query;

import uk.ac.starlink.ttools.Stilts;
import net.ivoa.util.CGI;
import net.ivoa.util.Settings;

import java.io.File;

public class GetRowCount {
    
    public static void main(String[] args) throws Exception {
	
	CGI params     = new CGI();
	String url     = params.value("url");
	int    maxWait = Integer.parseInt(Settings.get("maxWait", "10"));
	String file    = Settings.get("savedir")+"/"+url.hashCode()+".xml";
	
	File want  = new File(file);
	File trans = new File(file+".x");
	
	int wait = 0;
	
	while (wait < maxWait) {
	    if (want.exists()) {
		process(file);
	    } else if (trans.exists()) {
		Thread.sleep(1000);
		wait += 1;
	    }
	}
	System.out.println("Content-type: text/plain\n\nNodata");
    }
    
    static void process(String file) {
	String[] args = {
	    "tpipe",
	    "in="+file,
	    "ifmt=votable",
	    "omode=count"
	};
	System.out.println("Content-type: text/plain\n");
	Stilts.main(args);
	System.exit(1);
    }
}
			     
	
    
    
