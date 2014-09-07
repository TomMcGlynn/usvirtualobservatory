package net.ivoa.datascope;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import net.ivoa.util.CGI;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class SimpleFormatter {
    
    /** The class outputs skeleton information for the VOTable
     */
    private class VOTCallBack extends DefaultHandler {
	
	boolean active    = false;
	
	public ArrayList<String> names = new ArrayList<String>();
	public ArrayList<String> ucds  = new ArrayList<String>();
	public ArrayList<String> types = new ArrayList<String>();
	int tab = 0;
	int row = 0;
	int col = 0;
	int ncol = 0;
	StringBuffer buf = null;
	String sepChar = "";
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    if (qName.equals("RESOURCE")) {
		names.clear();
		ucds.clear();
		types.clear();
		row = 0;
		ncol = 0;
	    } else if (qName.equals("TR")) {
		sepChar = "";
		col  = 0;
		row += 1;
		if (row == 1) {
		    tab += 1;
		    printPrefix();
		}
		col  = 0;
		printRowStart();
	    } else if (qName.equals("TD")) {
		active = true;
		col += 1;
		buf  = new StringBuffer();
	    } else if (qName.equals("FIELD")) {
		addField(attrib);
	    }
        }
    
        public void endElement(String uri, String localName, String qName) {
	    if (qName.equals("TR")) {
		printRowEnd();
		
	    } else if (qName.equals("TD")) {
		active = false;
		String s = new String(buf).trim();
		printElement(s);
	    } else if (qName.equals("RESOURCE")) {
		printSuffix();
	    }
	}
	private void addField(Attributes attrib) {
	    String ucd = attrib.getValue("ucd");
	    if (ucd == null) {
		ucd = attrib.getValue("ucd");
	    }
	    if (ucd == null) {
		ucd = "";
	    }
	    ucds.add(ucd);
	    String name = attrib.getValue("name");
	    if (name == null) {
		name = attrib.getValue("ID");
	    }
	    if (name == null) {
		name = "";
	    }
	    names.add(name);
	    String type = attrib.getValue("datatype");
	    if (type == null) {
		type = "";
	    }
	    types.add(type);
	    ncol += 1;
	}
	private void printPrefix() {
	    String sep = "";
	    for (int i=0; i<ncol; i += 1) {
		System.out.print( sep+names.get(i));
		sep = "|";
	    }
	    System.out.println();
	    System.out.println("---");
	}
	
	private void printSuffix() {
	}
	
	private void printElement(String s) {
	    System.out.print(sepChar + s);
	    sepChar = "|";
	}
	
	private void printRowStart() {
	}
	
	private void printRowEnd() {
	    System.out.println();
	}
        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
    }
    public static void main(String[] args) throws Exception {
	
	SimpleFormatter sf = new SimpleFormatter();
	if (args.length == 0) {
	    CGI inp = new CGI();
	
	    String sn    = inp.value("sn");
	    String id    = inp.value("id");
	    String cache = inp.value("cache");
	
	    String file = DS.baseToHome(cache)+DS.validFileName(sn)+"."+id+".xml";
	    System.out.println("Content-type: text/plain\n");
	    sf.format(file);
	} else {
	    sf.format(args[0]);
	}
    }
	
    public void format(String filename) throws Exception {
	File f = new File(filename);
	if (!f.exists()) {
	    throw new Exception("File "+filename + " not found.");
	}
	SAXParser      sp = SAXParserFactory.newInstance().newSAXParser();
	BufferedReader is = new BufferedReader(
			      new InputStreamReader(
				new FileInputStream(f), "ISO-8859-1"));
	sp.parse(new InputSource(is), new VOTCallBack());
	is.close();
    }
}
