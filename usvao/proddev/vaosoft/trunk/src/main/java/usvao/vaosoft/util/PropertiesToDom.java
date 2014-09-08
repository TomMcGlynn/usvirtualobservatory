package usvao.vaosoft.util;

import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class applies a convention for converting a set of properties 
 * into an XML DOM.
 * <p>
 * This class uses the hierarchy implied by the <it>name.name</it> key 
 * structure to define the XML hierarchy.  If one of the fields is a number
 * it is taken as a repeatable instance of the name one level up from the 
 * number.  
 */
public class PropertiesToDom {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    String rootName = "properties";
        
    /**
     * create the converter
     */
    public PropertiesToDom() { this(null); }

    /**
     * create the converter
     */
    public PropertiesToDom(String root) { 
        super();
        if (root != null) rootName = root; 
    }

    public Document convert(Properties data) 
        throws ParserConfigurationException 
    {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document out = builder.newDocument();
        out.setXmlStandalone(true);
        out.setXmlVersion("1.0");
        Element root = out.createElement(rootName);
        out.appendChild(root);

        addElements(root, data);

        return out;
    }

    void addElements(Element root, Properties data) {
        Document doc = root.getOwnerDocument();
        Set<String> names = new TreeSet<String>();
        for(Enumeration e = data.propertyNames(); e.hasMoreElements();) 
            names.add((String) e.nextElement());

        HashMap<String, Element> children = new HashMap<String, Element>();
        HashMap<String, Properties> descdata = new HashMap<String, Properties>();

        String path = null;
        String[] parts = null, headtail = null;
        Element elem = null;
        Properties descs = null;
        Integer occur = null;
        Iterator<String> it = null;
        for(it = names.iterator(); it.hasNext();) {
            path = it.next();
            headtail = path.split("\\.", 2);
            occur = null;

            // see if the next field is a number
            parts = (headtail.length > 1) ? headtail[1].split("\\.", 2) : new String[0];
            if (parts.length > 0) {
                try {
                    occur = new Integer(parts[0]);
                } catch (NumberFormatException ex) { 
                    occur = null;
                }
            }

            if (headtail.length == 1 || (parts.length == 1 && occur != null)) {
                // a leaf node
                elem = doc.createElement(headtail[0]);
                if (occur != null) headtail[0] = path;
                elem.appendChild(doc.createTextNode(data.getProperty(headtail[0])));
                root.appendChild(elem);
            }
            else {
                // a branch node
                if (! children.containsKey(headtail[0]) && occur == null) {
                    elem = doc.createElement(headtail[0]);
                    root.appendChild(elem);
                    children.put(headtail[0], elem);
                    descdata.put(headtail[0], new Properties());
                }
                else if (occur != null) {
                    String newhead = headtail[0] + occur.toString();
                    if (! children.containsKey(newhead)) {
                        elem = doc.createElement(headtail[0]);
                        root.appendChild(elem);
                        children.put(newhead, elem);
                        descdata.put(newhead, new Properties());
                    }
                    headtail[0] = newhead;
                    headtail[1] = parts[1];
                }
                descdata.get(headtail[0]).setProperty(headtail[1], 
                                                      data.getProperty(path));
            }
        }

        for(it = descdata.keySet().iterator(); it.hasNext();) {
            path = it.next();
            descs = descdata.get(path);
            addElements(children.get(path), descs);
        }

        return;
    }

    public static Properties sampleProperties() {
        Properties out = new Properties();
        out.setProperty("name", "foo");
        out.setProperty("version", "3.2.2");
        out.setProperty("build.type", "make");
        out.setProperty("build.install.evidence", "some");
        out.setProperty("build.install.evidence.env", "MY_HOME");
        out.setProperty("build.install.evidence.dirfile.1", "/opt/sw lib/libsw.a");
        out.setProperty("build.install.evidence.dirfile.2", "/usr/local lib/libsw.a");
        out.setProperty("build.install.evidence.dirfile.3.dir", "/usr/local");
        out.setProperty("build.install.evidence.dirfile.3.file", "lib/libsw.a");
        out.setProperty("build.install.evidence.stddirfile.1", "lib/libsw.a");
        out.setProperty("build.install.evidence.stddirfile.2", "lib/libr.a");
        out.setProperty("build.install.evidence.swdir", "/opt/sw");
        return out;
    }

    public static void main(String[] args) {
        try {
            PropertiesToDom p2d = new PropertiesToDom();
            Document doc = p2d.convert(PropertiesToDom.sampleProperties());
            TransformerFactory tFactory = TransformerFactory.newInstance();
            tFactory.setAttribute("indent-number", 2);
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", 
                                          "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result); 
            System.out.println();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}