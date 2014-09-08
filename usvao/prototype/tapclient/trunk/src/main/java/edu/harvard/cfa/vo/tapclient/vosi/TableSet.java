package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;

/**
 * A set of tables that are part of a single resource.
 * <p/>
 * <pre>
 * <code>
  void showTableset(TableSet tableset) {
 
    edu.harvard.cfa.vo.tapclient.tap.TapService tapService = new edu.harvard.cfa.vo.tapclient.tap.TapService(baseURL);

    TableSet tableset = tapService.getTableSet();
    for (Schema schema: tableset.getSchemas()) {
	System.out.println(schema+"\t[Schema]");
		
	for (Table table: schema.getTables()) {
	    System.out.println("\t"+table+"\t[Table]");
		    
	    for (Column column: table.getIndexedColumns()) {
		System.out.println("\t\t"+column+"\t[Indexed column]");
	    }
		    
	    for (ForeignKey foreignKey: table.getForeignKeys()) {
		System.out.println("\t\t"+foreignKey+"\t[Foreign key]");
	    } 
	    System.out.println("");
	}
	System.out.println("");
    }
  }
 * </code>
 * </pre>
 */
public class TableSet {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.vosi.TableSet");

    private static final String DEFAULT_TRANSFORM_RESOURCE = "Tables2VOSITables.xsl"; 
    private URL transformURL;

    protected String fullURL;
    protected Map<String,Schema> schemas;

    /**
     * Constructs a TableSet object from the given service.
     * @param fullURL the full URL associated with this TableSet object
     */
    public TableSet(String fullURL) {
	this.fullURL = fullURL;
	this.schemas = new HashMap<String,Schema>();
	this.transformURL = getClass().getResource(DEFAULT_TRANSFORM_RESOURCE);
    }

    /**
     * Returns the schema list of the TAP service associate with this TableSet object.  The list returned is current as of the last call to update.
     * @return a list of Schema objects.
     * @see #update
     * @see Schema
     */
    public List<Schema> getSchemas() { 
	final Schema[] array = schemas.values().toArray(new Schema[schemas.size()]);
	return new AbstractList<Schema>() {
	    public Schema get(int index) { return array[index]; }
	    public int size() { return array.length; }
	};
    }

    public void add(Schema newValue) {
	schemas.put(newValue.getName(), newValue);
    }
    
    /**
     * Updates this TableSet object with the VOSI tables response from the TAP service.  A request is made to the TAP service associated with this TableSet object.
     * @throws Exception if an error occurs connecting to the TAP service or parsing the response
     * @throws HttpException if the service responses to the VOSI Tables request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Tables document.
     * @throws IOException if an error occurs creating an input stream.
     */   
    public void update() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    inputStream = HttpClient.get(fullURL);

	    //////
	    // Astrogrid DSA/Catalogue Tables.xsd transform
	    Source source = new DOMSource(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream));
	    DOMResult result = new DOMResult();

	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = (transformURL != null ? transformerFactory.newTransformer(new StreamSource(transformURL.openStream())) : transformerFactory.newTransformer());
	    
	    transformer.transform(source, result);
	    // Astrogrid DSA/Catalogue Tables.xsd transform
	    //////

	    // Tables 
	    //   Namespace substitution 
	    XmlOptions tablesOptions = new XmlOptions();
	    Map<String,String> tablesNamespaces = new HashMap<String,String>();
	    tablesNamespaces.put("http://www.ivoa.net/xml/VODataSource/v1.0", "");
	    tablesOptions.setLoadSubstituteNamespaces(tablesNamespaces);
	    //   Document element replacement
	    QName tablesDocumentElement = new QName("http://www.ivoa.net/xml/VOSITables/v1.0", "tableset", "vosi");
	    tablesOptions.setLoadReplaceDocumentElement(tablesDocumentElement);
	    net.ivoa.xml.vosiTables.v10.TablesetDocument xdocument = net.ivoa.xml.vosiTables.v10.TablesetDocument.Factory.parse(result.getNode()/*inputStream*/, tablesOptions);

	    net.ivoa.xml.voDataService.v11.TableSet xtableSet = xdocument.getTableset();

	    schemas.clear();
	    if (xtableSet != null) {
		List<net.ivoa.xml.voDataService.v11.TableSchema> xschemaList = xtableSet.getSchemaList();
		for (net.ivoa.xml.voDataService.v11.TableSchema xschema: xschemaList) {
		    if (logger.isLoggable(Level.FINER)) 
			logger.log(Level.FINER, "schema name: "+xschema.getName());
		    schemas.put(xschema.getName(), new Schema(xschema));
		}
	    }
	} catch (TransformerConfigurationException ex) {
	    throw new ResponseFormatException("error parsing VOSI Tables response: "+ex.getMessage(), ex);

	} catch (ParserConfigurationException ex) {
	    throw new ResponseFormatException("error parsing VOSI Tables response: "+ex.getMessage(), ex);

	} catch (SAXException ex) {
	    throw new ResponseFormatException("error parsing VOSI Tables response: "+ex.getMessage(), ex);

	} catch (TransformerException ex) {
	    throw new ResponseFormatException("error parsing VOSI Tables response: "+ex.getMessage(), ex);

	} catch (XmlException ex) {
	    throw new ResponseFormatException("error parsing VOSI Tables response: "+ex.getMessage(), ex);

	} catch (HttpException ex) {
	    throw new HttpException("error getting VOSI Tables response: "+ex.getMessage(), ex);

	} catch (IOException ex) {
	    throw new IOException("error reading VOSI Tables response: "+ex.getMessage(), ex);

	}
    }

    /**
     * 
     */
    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	for (Schema schema: getSchemas()) {
	    output.println(indent+"Schema: ");
	    schema.list(output, indent+"  ");
	}
    }

    /**
     * Returns a URL pointing to the XSL transform to apply to the XML documents recieved from the service.
     * @return The URL of the transform to apply to XML documents recieved from the service.  Null impliesthe identity transformation will be applied.
     */
    public URL getTransformURL() {
	return transformURL;
    }

    /**
     * Set the XSL transform to apply to the XML documents recieved from the service.
     * @param newValue The URL of the transform to apply to XML documents recieved from the service.  Null impliesthe identity transformation will be applied.
     */
    public void setTransformURL(URL newValue) {

	transformURL = newValue;    
    }
}
