package net.ivoa.skynode;

import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamSource;

import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;

/** This class translates from the XML structured representation
 *  of a query to a scalar string similar to what most relational
 *  databases use.
 */
public class XSLTransform {
    
    /** This function translates an input file into an output file.
     *  @param xslFile   The name of the file defining the transformation.
     *  @param input     A file to be transformed.
     *  @return A string giving results of the transformation.
     */
    public static String translate(String xslFile, File input) throws Exception {
	StringBuffer   inp = new StringBuffer();
	BufferedReader bf  = new BufferedReader(new FileReader(input));
	String line;
	while ((line = bf.readLine()) != null) {
	    inp.append(line);
	}
	String inps = new String(inp);
	
	return translate(xslFile, inps);
    }
    
    /** This function translates an XML string into some output string defined
     *  byt the XSL file.
     *  into scalar SQL string.
     *  @param xslFile   The name of the file defining the transformation.
     *  @param xml       The input XML.
     *  @return          A string giving results of the transformation.
     */
    public static String translate(String xslFile, String xml) throws Exception {
	xml = xml.replaceAll("xsi:type=\"\\w+:", "xsi:type=\"");
	Source src = new StreamSource(new StringReader(xml));
	return translate(xslFile, src);
    }
    
    /** Translate a parsed XML structure.
     *  @param xslFile   The name of the file defining the transformation.
     *  @param top       The top of the XML tree to be transformed.
     *  @return A string giving results of the transformation.
     */
    public static String translate(String xslFile, Node top) throws Exception {
	Source src = new DOMSource(top);
	return translate(xslFile, src);
    }
    
    /** Translate XML given an XML source.
     *  @param xslFile    The name of the file defining the transformation
     *  @param src        The XML source
     *  @return           A string giving the results of the transformation.
     */
    static String translate(String xslFile, Source src) throws Exception {

	StringWriter writer          = new StringWriter(); 
	TransformerFactory tFactory  = TransformerFactory.newInstance();
        Transformer        transformer = tFactory.newTransformer(new StreamSource(xslFile));
	transformer.transform(src, new StreamResult(writer));
        return  writer.toString();
    }
    
    /** Test the translator:
     *    Usage: java net.ivoa.skynode.XSLTransform xmlfile xslFile
     *  Note that the order of arguments to the command is reversed
     *  from the order in most of the translate functions.
     */
    public static void main(String[] args) throws Exception {
	File   f   = new File(args[0]);
	String res = translate(args[1], f);
	System.out.println("Length of result is:"+res.length());
	System.out.println(res);
    }
}
