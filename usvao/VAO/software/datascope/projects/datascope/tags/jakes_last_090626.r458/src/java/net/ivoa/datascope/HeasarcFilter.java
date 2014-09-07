package net.ivoa.datascope;

import java.util.HashMap;

public class HeasarcFilter extends Filter {
   
    private String urlString =  "http://heasarc.gsfc.nasa.gov/cgi-bin/vo/counter/browseCount.pl?";
    private HashMap<String, Integer> counts = new HashMap<String, Integer>();
    
    public int count(String id) {
	id = id.substring(id.lastIndexOf("/")+1);
	id = id.intern();
	Integer myInt = counts.get(id);
	if (counts.containsKey(id)) {
	    return counts.get(id);
	} else {
	    return 0;
	}
    }
    
    public void invoke(double ra, double dec, double size) {
	
	size             += DS.getFilterQueryFudge();
	String invokedURL = urlString + "ra="+ra+"&dec="+dec+"&size="+size;
	
	DS.log("Filter URL:"+invokedURL);

	String result = null;
	try {
	    result     = Service.get(invokedURL);
	} catch (Exception e) {
	    System.err.println("Error in HEASARC Filter"+e);
	    return;
	}
	if (result != null) {
	    String[] lines = result.split("\n");
	    for (int i=0; i<lines.length; i += 1) {
		String regex = "\\s+";
		String[] fields = lines[i].split(regex);
		if (fields.length != 3 || !fields[0].trim().equals(">")) {
		    continue;
		}
		try {
		    int count = Integer.parseInt(fields[2]);
		    fields[1] = fields[1].intern();
		    counts.put(fields[1], count);
		} catch (Exception e) {
		    // Just ignore this.
		}
	    }
	}
    }
    
    public double fudge() {
	return DS.getFilterQueryFudge();
    }
    
    public HashMap<String, Integer> getCounts() {
	return counts;
    }
}
