package net.ivoa.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Date;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import net.ivoa.util.DynamicInvoker;


public class RegistrySearch {
    
    DynamicInvoker di;
    String         wsdl;

    /** 
     * Connect to the STScI NVO registry.
     */
    public RegistrySearch(String wsdlNode)  throws Exception {
	wsdl = wsdlNode;
    }
    
    /** Submit a query to the registry.
     *  Returns a hash of hashes using the IDs to identify the
     *  nodes and then for each ID using the elements of the returned
     *  query and the values.  The results are represented as arrays of strings.
     */
    public HashMap<String, HashMap<String, String[]>> query(String queryString) throws Exception {
	
	if (di == null) {
	    di   = new DynamicInvoker(wsdl);
	}
	
	// First two values in 
        HashMap map = di.invokeMethod("QueryRegistry", null, new String[]{queryString});
	
        NodeList nl = null;
	
        for (Object okey: map.keySet()) {
	    String key = (String) okey;
            Object value = map.get(key);
            if (value instanceof Element) {
		Element el = (Element) value;
	        nl = el.getChildNodes();
		break;
            }
        }
	
	if (nl == null) {
	    System.err.println("No results returned from registry query.");
	    return null;
	}
	
	HashMap<String, HashMap<String, String[]>> res = new HashMap<String, HashMap<String, String[]>>();
	for(int i=0; i<nl.getLength(); i += 1) {
	    Node n = nl.item(i);
	    HashMap<String, String[]> curr = new HashMap<String, String[]>();
	    
	    NodeList ml  = n.getChildNodes();
	    String   id = null;
	    for (int j=0; j<ml.getLength(); j += 1) {
		Node m = ml.item(j);
		
		String key = m.getNodeName();
		if (key.indexOf(':') > 0) {
		    key = key.substring(key.indexOf(':')+1);
		}
		
		NodeList kl = m.getChildNodes();
		
		if (key.equals("Identifier")) {
		    try {
		        id = m.getFirstChild().getNodeValue();
		    } catch (Exception e) {
			System.err.println("No ID for entry:"+i);
			break;
		    }
		}
		
		String[] vals = new String[kl.getLength()];
		for (int k=0; k<kl.getLength(); k += 1) {
		    
		    Node kk = kl.item(k);
		    if (kk.getNodeType() == Node.TEXT_NODE) {
			vals[k] = kk.getNodeValue().trim();
		    } else {
			NodeList ll = kk.getChildNodes();
			if (ll.getLength() > 0) {
			    if (ll.getLength() > 1) {
				System.err.println("Unexpected multiplicity at key:"+key);
			    }
			    Node l = ll.item(0);
			    vals[k] = l.getNodeValue().trim();
			}
		    }
		}
		
		curr.put(key, vals);
	    }
	    if (id != null) {
		res.put(id, curr);
	    }
	}
	return res;
    }
    
    public static void main(String[] args) throws Exception {
	
	RegistrySearch rs = new RegistrySearch("http://nvo.stsci.edu/VORegistry/registry.asmx?WSDL");
	
	for (String query: args) {
	    HashMap<String, HashMap<String, String[]>> hm = rs.query(query);
	    for (String id: hm.keySet()) {
		HashMap<String, String[]> obj = hm.get(id);
		System.out.println("Processing ID: "+id);
		String[] sn  =  obj.get("ShortName");
		String[] url =  obj.get("ServiceURL");
		if (sn == null) {
		    System.out.println("  No short name found");
		} else {
		    System.out.println("  ShortName="+sn[0]);
		}
		if (url == null  || url.length == 0) {
		    System.out.println("  No URL found");
		} else {
		    System.out.println("  URL="+url[0]);
		}
		String subj = "";
		String div  = "";
		String[] sarr = obj.get("Subject");
		if (sarr != null) {
		    for (String join: sarr) {
		        subj += div + join;
		        div = ",";
		    }
		}
		System.out.println("  Subject="+subj);
	    }
	}
    }
}
