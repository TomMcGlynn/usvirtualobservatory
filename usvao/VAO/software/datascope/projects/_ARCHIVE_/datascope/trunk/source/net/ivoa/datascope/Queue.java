package net.ivoa.datascope;

import java.util.ArrayList;

/** This class implements a list of services to be
 *  run one at a time.  It is assumed that the
 *  queue services only one host id.
 */
public class Queue implements Runnable {
    
    ArrayList<Service> services = new ArrayList<Service>();
    String             host;
    Filter             hostFilter;
    double ra, dec, size;
    
    public Queue(String host, double ra, double dec, double size) {
	this.host  = host;
	hostFilter = Filter.factory(host);
	this.ra    = ra;
	this.dec   = dec;
	this.size  = size;
    }
    
    public void addService(Service s) {
	services.add(s);
    }
    
    public void run() {
	DS.log("Normal start for queue "+host+" with "+services.size()+" requests.");
	double fudge      = 0;
	String filterName = null;
	
	if (hostFilter != null) {
	    DS.log("Invoking host filter:"+hostFilter);
	    hostFilter.invoke(ra, dec, size);
	    fudge      = hostFilter.fudge();
	    filterName = hostFilter.getClass().getName();
	}
	
	for (int i=0; i<services.size(); i += 1) {
	    
	    Service s = services.get(i);
	    
	    if ( (hostFilter != null) && 
		 (s.getROR() < fudge)  ) {
		
		int cnt = hostFilter.count(s.getID());
		if (cnt == 0) {
		    s.setMessage("|Filtered by "+filterName);
		    continue;
		}
	    }
	    s.updateURL(ra, dec, size);
	    s.invoke();
	}
	DS.log("Normal exit for queue:"+host);
    }
}
