package net.ivoa.query;


import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileOutputStream;
import java.io.File;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

/** This class runs an XSLT transformation on 
 *  an input XML file.
 */
public class XSLTrans {
    
    
    public static void main(String[] args) throws Exception {
	
	String xsl = args[0];
        String source = null;
	String result = null;
	
	if (args.length > 1) {
	    source = args[1];
	}
	if (args.length > 2) {
	    result = args[2];
	}
	new XSLTrans().transform(xsl, source, result);
    }
    
    public static StreamSource getSource(String name) {
	String lc = name.toLowerCase();
	if (lc.startsWith("http:") || lc.startsWith("ftp:") ||
	    lc.startsWith("https:")) {
	    return new StreamSource(name);
	} else {
	    return new StreamSource(new File(name));
	}
    }
    
    public void transform(String xsltFile, String source, String result) throws Exception {
	
	StreamSource src;
	StreamResult rslt;
	
	if (source != null) {
	    src = getSource(source);
	} else {
	    src = new StreamSource(System.in);
	}
	
	StreamSource xsl = getSource(xsltFile);
	
	
	if (result != null) {
	    rslt = new StreamResult(new FileOutputStream(result));
	} else {
	    rslt = new StreamResult(System.out);
	}
	
	transform(xsl, src, rslt);
    }
    
    public void transform(StreamSource xsl, StreamSource src, StreamResult rslt) throws Exception {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer transformer = tfactory.newTransformer(xsl);
        transformer.transform(src, rslt);
    }
}
