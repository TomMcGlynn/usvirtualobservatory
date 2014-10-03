package net.ivoa.registry.harvest;

import net.ivoa.registry.std.RIStandard;
import net.ivoa.registry.std.RIProperties;

import ncsa.xml.extractor.ExtractingParser;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.NoSuchElementException;

import java.io.Writer;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

/**
 *  a class for extracting VOResource records out of an OAI-PMH ListRecords
 *  response.  
 *  <p>
 *  To get the records, the client uses the {@link DocumentIterator} 
 *  interface, {@link #nextReader()} or {@link #nextDocument()},
 *  to step through the available records.  When one of these methods return 
 *  null, no more records are available.  
 *  <p>
 *  As a side effect, this class will also look for a resumption
 *  token in the response and makes it available via 
 *  {@link #getResumptionToken()}.  Note, however, that because the token 
 *  usually appears at the end of the input ListRecords response, the token 
 *  is typically not available until {@link #nextReader()} returns null.
 */
public class VOResourceExtractor extends DocumentIteratorBase 
    implements RIProperties 
{

    ExtractingParser parser = null;
    ResumptionToken resume = null;
    String oains = null;

    /**
     * create an instance that will extract records from the given stream
     * @param is     the stream providing the ListRecords response
     */
    public VOResourceExtractor(InputStream is) {
        this(is, null);
    }

    /**
     * create an instance that will extract records from the given stream
     * @param is     the stream providing the ListRecords response
     */
    public VOResourceExtractor(Reader is) {
        this(is, null);
    }

    /**
     * create an instance that will extract records from the given stream
     * @param is     the stream providing the ListRecords response
     * @param riStd  the properties containing definitions pertaining to the 
     *                 IVOA Registry Interfaces standard.  This must include
     *                 values for OAI_NAMESPACE, REGISTRY_INTERFACE_NAMESPACE,
     *                 and RESOURCE_ELEMENT.
     */
    public VOResourceExtractor(InputStream is, Properties riStd) {
        this(new InputStreamReader(is), riStd);
    }

    /**
     * create an instance that will extract records from the given stream
     * @param is     the stream providing the ListRecords response
     * @param riStd  the properties containing definitions pertaining to the 
     *                 IVOA Registry Interfaces standard.  This must include
     *                 values for OAI_NAMESPACE, REGISTRY_INTERFACE_NAMESPACE,
     *                 and RESOURCE_ELEMENT.
     */
    public VOResourceExtractor(Reader is, Properties riStd) {
        if (riStd == null) riStd = RIStandard.getDefaultDefinitions();
        oains = riStd.getProperty(OAI_NAMESPACE);

        parser = new ExtractingParser(is);
        parser.setContentHandler(new ResumptionFinder());
        parser.ignoreNamespace(riStd.getProperty(OAI_NAMESPACE));
        parser.ignoreNamespace(riStd.getProperty(OAI_DC_NAMESPACE));
        parser.extractElement(riStd.getProperty(REGISTRY_INTERFACE_NAMESPACE), 
                              riStd.getProperty(RESOURCE_ELEMENT));
    }

    /**
     * return the next document in the set as a Reader object.
     * @exception IOException   if an error occurs while creating Reader to data
     */
    public Reader nextReader() throws IOException {
        return parser.nextNode();
    }

    /**
     * return the resumption token encoded in the response.  Null is 
     * returned if no resumption token element was specified.  Because
     * the resumptionToken element is found at the end of the ListRecords
     * response, this should normally should only be called after all 
     * documents have been read and {@link #nextReader()} returns null.  
     */
    public ResumptionToken getResumptionToken() { return resume; }

    /**
     * return true if a resumption token element was specific and includes 
     * an actual token.
     */
    public boolean shouldResume() { 
        return (resume != null && resume.moreRecords()); 
    }

    class ResumptionFinder extends DefaultHandler {
        ResumptionToken tok = null;
        final String oaiuri = oains;
        final String resumption = "resumptionToken";

        public void startDocument() {
            tok = null;
        }

        public void startElement(String uri, String localname, String qName,
                                 Attributes atts) 
            throws SAXException
        {
            if (localname.equals(resumption) && oaiuri.equals(uri)) {
                tok = new ResumptionToken("", atts.getValue("expirationDate"), 
                                          -1, -1);
                String val = null;
                try {
                    val = atts.getValue("completeSize");
                    if (val != null) tok.size = Integer.parseInt(val);
                }
                catch (NumberFormatException ex) { /* be tolerant */ }
                try {
                    val = atts.getValue("cursor");
                    if (val != null) tok.curs = Integer.parseInt(val);
                }
                catch (NumberFormatException ex) { /* be tolerant */ }
            }
        }

        public void characters(char[] text, int start, int length) {
            if (tok != null) {
                tok.tok += text;
            }
        }

        public void endElement(String uri, String localname, String qName)
            throws SAXException
        {
            if (tok != null && 
                localname.equals(resumption) && oaiuri.equals(uri)) 
            {
                resume = tok;
                tok = null;
            }
        }
    }

    public static void main(String[] args) {
        try {
            File f = new File(args[0]);
            VOResourceExtractor ext = 
                new VOResourceExtractor(new FileReader(f));

            Reader vor = null;
            char[] buf = new char[16*1024];
            int n;
            Writer out = new OutputStreamWriter(System.out);
            while ((vor = ext.nextReader()) != null) {
                while ((n = vor.read(buf)) >= 0) {
                    out.write(buf, 0, n);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//     /**
//      * create an extractor that understands with version 1.0 of the Registry 
//      * Interface
//      */
//     public VOResourceExtractor extractorFor10(Reader is) {
//         Properties p = new Properties();
//         RIStandard.loadProperties(p, "1.0");
//         return new VOResourceExtractor(is, p);
//     }

//     /**
//      * create an extractor that understands with version 1.0 of the Registry 
//      * Interface
//      */
//     public VOResourceExtractor extractorFor10(InputStream is) {
//         return extractorFor10(new InputStreamReader());
//     }
}
