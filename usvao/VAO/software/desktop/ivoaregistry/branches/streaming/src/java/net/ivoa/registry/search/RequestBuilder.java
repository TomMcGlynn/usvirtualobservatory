/*
 * Created by Ray Plante for the National Virtual Observatory
 * c. 2009
 */
package net.ivoa.registry.search;

import net.ivoa.registry.RegistryServiceException;
import net.ivoa.registry.RegistryFormatException;
import net.ivoa.registry.RegistryAccessException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.Name;
import javax.xml.soap.MimeHeaders;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;

/**
 * a message builder that will create SOAP request messages to be sent
 * to registry search interface.
 * <p>
 * Note that this class can support a deprecated form of the keywordSearch
 * operation that uses a <code>to</code> input parameter instead of 
 * <code>max</code>, depending on the strictness setting (set via 
 * {@link #setCompliance} (see {@link #getKeywordSearchVariant()} for more 
 * discussion).  
 */
public class RequestBuilder {

    protected MessageFactory soapfactory = null;

    // this is used for backward compatibility with earlier working draft
    // of the standard interface
    protected int kwsVariant = AssessWSDL.VARIANT_UNRECOGNIZED;

    boolean qualified = false; 

    public static final String WSDL_NS = 
        "http://www.ivoa.net/wsdl/RegistrySearch/v1.0";

    public static final String WSDL_PREFIX = "rs";


    /**
     * create a RequestBuilder
     */
    public RequestBuilder() {
        try {
            soapfactory = MessageFactory.newInstance();
        }
        catch (SOAPException ex) {
            throw new InternalError("installation/config error: " + 
                                    ex.getMessage());
        }
    }

    /**
     * return the variant of the keywordSearch interface being assumed
     * for the service.  This method will return an integer representing
     * a bit mask of the variants that are supported, where the bits include
     * {@link net.ivoa.registry.search.AssessWSDL#VARIANT_ACCEPTS_MAX AssessWSDL.VARIANT_ACCEPTS_MAX} 
     * (the "max" input parameter is supported) and 
     * {@link net.ivoa.registry.search.AssessWSDL#VARIANT_ACCEPTS_TO AssessWSDL.VARIANT_ACCEPTS_TO} 
     * (the "to" input parameter is supported).  This determination involves
     * accessing the remote WSDL and examining the interface description.  
     * If the value is 
     * {@link net.ivoa.registry.search.AssessWSDL#VARIANT_UNRECOGNIZED AssessWSDL.VARIANT_UNRECOGNIZED}
     * (the default), this builder will assume 
     * {@link net.ivoa.registry.search.AssessWSDL#VARIANT_ACCEPTS_MAX AssessWSDL.VARIANT_ACCEPTS_MAX}.
     * <p>
     * This attribute became necessary due to an error in the official (working 
     * draft) standard WSDL published on the IVOA web site, against several 
     * registries were built.  To provide backward compatibility with these
     * pre-Recommendation implementations, this class can adjust its keyword
     * search message accordingly.  
     * @see net.ivoa.registry.search.AssessWSDL 
     */
    public int getKeywordSearchVariant() {
        return kwsVariant;
    }

    /**
     * set the variant of the keywordSearch interface to assume.  
     * @see #getKeywordSearchVariant()
     */
    public void setKeywordSearchVariant(int var) {
        kwsVariant = var;
    }

    boolean useKwsTo() {
        int var = getKeywordSearchVariant();
        return (var > 0 && (var & AssessWSDL.VARIANT_ACCEPTS_MAX) == 0);
    }

    /**
     * return true if the operation arguments are qualified in the request
     * SOAP request messages that are produced.
     */
    public boolean argsAreQualified() { return qualified; }

    /**
     * set whether operation arguments will be qualified in the request
     * SOAP request messages that are produced.
     */
    public void qualifyArgs(boolean yesno) { qualified = yesno; }

    /**
     * submit a keyword search
     * @param keywords   space-delimited words to search on
     * @param orThem     if true, return results that contain any of the 
     *                      keywords
     * @param from       the position of the first match to return
     * @param max        the maximum number of matches to return.
     * @param identifiersOnly  if true, return only identifiers; otherwise,
     *                   return the entire VOResource record for each match.
     * @exception RegistryServiceException  if the service encounters an error 
     *                           (i.e. on the server side).
     * @exception SOAPException if an error is encountered while creating or
     *                           submitting the SOAP request or while processing
     *                           the SOAP response.  
     */
    public SOAPMessage keywordSearch(String keywords, boolean orThem, 
                                     int from, int max, 
                                     boolean identifiersOnly) 
         throws RegistryServiceException, SOAPException
    {
        SOAPMessage msg = makeSOAPMessage();
        SOAPBody body = msg.getSOAPBody();
        SOAPElement sel = body.addBodyElement(makeRSName(msg, "KeywordSearch"));

        // add the keywords argument
        SOAPElement arg = sel.addChildElement(makeArgName(msg, "keywords"));
        arg.addTextNode(keywords.trim());

        // add the orValues argument
        arg = sel.addChildElement(makeArgName(msg, "orValues"));
        arg.addTextNode(Boolean.toString(orThem));

        // add the from argument, if necessary
        if (from > 0) {
            arg = sel.addChildElement(makeArgName(msg, "from"));
            arg.addTextNode(Integer.toString(from));
        }

        // add the max argument, if necessary.  Use "to" instead if we 
        // are playing loose and it is supported by the service.
        if (max > 0) {
            if (useKwsTo()) {
                // backward compatability
                arg = sel.addChildElement(makeArgName(msg, "to"));
                max += from;
            }
            else {
                arg = sel.addChildElement(makeArgName(msg, "max"));
            }
            arg.addTextNode(Integer.toString(max));
        }
        arg = sel.addChildElement(makeArgName(msg, "identifiersOnly"));
        arg.addTextNode(Boolean.toString(identifiersOnly));

        return msg;
    }

    /**
     * submit a constraint-based search
     * @param adqlWhere  the search constraints in the form of a ADQL Where 
     *                      clause.  The element's name should be "Where", and
     *                      its contents should comply with the ADQL schema's
     *                      "WhereType".  
     * @param from       the position of the first match to return
     * @param max        the maximum number of matches to return.
     * @param identifiersOnly  if true, return only identifiers; otherwise,
     *                   return the entire VOResource record for each match.
     * @exception DOMException if the adqlWhere Element object does not allow
     *                           itself to be imported or otherwise its 
     *                           implementation is defective.
     * @exception RegistryServiceException  if the service encounters an error 
     *                           (i.e. on the server side).
     * @exception SOAPException if an error is encountered while creating or
     *                           submitting the SOAP request or while processing
     *                           the SOAP response.  
     */
    public SOAPMessage search(Element adqlWhere, int from, int max, 
                              boolean identifiersOnly) 
         throws RegistryServiceException, SOAPException, DOMException
    {
        SearchQuery query = new SearchQuery();
        Element wparent = query.getWhereParent();

        query.setWhere(adqlWhere);
        query.setFrom(from);
        query.setMax(max);
        query.setIdentifiersOnly(identifiersOnly);

        return query.getSearchSOAPMessage();
    }

    /**
     * return a SearchQuery object that can be used to attach an ADQL 
     * query to.
     * @exception SOAPException if an error is encountered while creating 
     *                           the SOAP request.  
     */
    public SearchQuery createSearchQuery() throws SOAPException {
        return new SearchQuery(); 
    }

    /**
     * return the Registry description
     * @exception RegistryServiceException  if the service encounters an error 
     *                           (i.e. on the server side).
     * @exception SOAPException if an error is encountered while creating or
     *                           submitting the SOAP request or while processing
     *                           the SOAP response.  
     */
    public SOAPMessage getIdentity() 
         throws RegistryServiceException, SOAPException
    {
        SOAPMessage msg = makeSOAPMessage();

        SOAPBody body = msg.getSOAPBody();
        SOAPElement sel = body.addBodyElement(makeRSName(msg, "GetIdentity"));
        return msg;
    }

    /**
     * return the Resource description for a given identifier
     * @param ivoid   the IVOA Identifier to resolve
     * @param query   the search constraints as a SearchQuery object.
     * @exception IDNotFoundException  if the service cannot match the given
     *                           ID to a description
     * @exception RegistryServiceException  if the service encounters an error 
     *                           (i.e. on the server side).
     * @exception SOAPException if an error is encountered while creating or
     *                           submitting the SOAP request or while processing
     *                           the SOAP response.  
     */
    public SOAPMessage getResource(String ivoid) 
         throws RegistryServiceException, IDNotFoundException, SOAPException
    {
        SOAPMessage msg = makeSOAPMessage();

        SOAPBody body = msg.getSOAPBody();
        SOAPElement sel = body.addBodyElement(makeRSName(msg, "GetResource"));
        SOAPElement id = sel.addChildElement(makeArgName(msg, "identifier"));
        id.addTextNode(ivoid.trim());

        return msg;
    }

    protected Name makeRSName(SOAPMessage msg, String elname) 
         throws SOAPException
    {
        return msg.getSOAPPart().getEnvelope().createName(elname, WSDL_PREFIX,
                                                          WSDL_NS);
    }

    protected Name makeArgName(SOAPMessage msg, String elname) 
         throws SOAPException
    {
        if (qualified) 
            return makeRSName(msg, elname);
        else
            return msg.getSOAPPart().getEnvelope().createName(elname);
    }

    /**
     * return the result of an XQuery search
     * @exception UnsupportedOperationException  if the service does not support
     *                          an XQuery-based search
     * @exception RegistryServiceException  if the service encounters an error 
     *                           (i.e. on the server side).
     * @exception SOAPException if an error is encountered while creating or
     *                           submitting the SOAP request or while processing
     *                           the SOAP response.  
     */
    public SOAPMessage xquerySearch(String xquery) 
         throws RegistryServiceException, UnsupportedOperationException, 
                SOAPException
    {
        SOAPMessage msg = makeSOAPMessage();
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();

        SOAPBody body = msg.getSOAPBody();
        SOAPElement sel = body.addBodyElement(makeRSName(msg, "XQuerySearch"));
        SOAPElement id = sel.addChildElement(makeArgName(msg, "xquery"));
        id.addTextNode(xquery.trim());

        return msg;
    }
     
    /**
     * create an empty SOAP message.  This can provide a DOM Document with which
     * an element containing an ADQL query can be created and inserted directly.
     * @exception SOAPException if an error is encountered while creating 
     *                           the SOAP request.  
     */
    public SOAPMessage makeSOAPMessage() throws SOAPException {
        return soapfactory.createMessage();
    }

    /**
     * an updatable search query
     */
    public class SearchQuery {

        SOAPMessage msg = null;
        int from=0, max=-1;
        boolean identifiersOnly = false;
        SOAPElement whereParent = null;

        SearchQuery() throws SOAPException {
            msg = makeSOAPMessage();
            SOAPBody body = msg.getSOAPBody();

            // this ensures that the owner document is set.  
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();

            whereParent = body.addBodyElement(makeRSName(msg, "Search"));
            whereParent.setAttribute("xmlns", WSDL_NS);
        }

        public void setWhere(Element where) throws SOAPException {
            SOAPElement wparent = getWhereParent();
            where = (Element)wparent.getOwnerDocument().importNode(where, true);

            if (WSDL_NS.equals(where.getNamespaceURI())) {
                wparent.appendChild(where);
            }
            else {
                // need to change the namespace
                SOAPElement newwhere = 
                    wparent.addChildElement(makeRSName(msg, "Where"));
                
                // copy over all the attributes
                Attr attr = null;
                NamedNodeMap attrs = where.getAttributes();
                while (attrs.getLength() > 0) {
                    attr = (Attr) attrs.item(0);
                    if (attr == null) break;
                    // where.removeAttributeNode(attr);
                    newwhere.setAttributeNode(attr);
                }

                // copy over all children
                Node node = where.getFirstChild();
                while (node != null) {
                    // where.removeChild(node);
                    newwhere.appendChild(node);
                    node = where.getFirstChild();
                }
            }
        }

        /**
         * return a SOAP Message that is ready for submission
         */
        public SOAPMessage getSearchSOAPMessage() throws SOAPException {
            SOAPElement child = null;

            // this ensures that the owner document is set.  
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();

            // check that we have an ADQL
//             if (! whereParent.getChildElements("Where").hasNext())
//                 throw new IllegalStateException("Missing ADQL Where clause");

            if (from > 0) {
                child = whereParent.addChildElement(makeArgName(msg, "from"));
                child.addTextNode(Integer.toString(from));
            }
            if (max > 0) {
                child = whereParent.addChildElement(makeArgName(msg, "max"));
                child.addTextNode(Integer.toString(max));
            }
            child = whereParent.addChildElement(
                                    makeArgName(msg, "identifiersOnly"));
            child.addTextNode(Boolean.toString(identifiersOnly));

            return msg;
        }

        /**
         * return a parent element that the Where clause can be appended to.
         */
        public SOAPElement getWhereParent() {
            whereParent.removeContents();
            return whereParent;
        }

        /**
         * return the position of the first record to return
         */
        public int getFrom() { return from; } 

        /**
         * set the position of the first record to return
         */
        public void setFrom(int pos) { from = pos; }

        /**
         * return the maximum number of records to return
         */
        public int getMax() { return max; }

        /**
         * set the maximum number of records to return
         */
        public void setMax(int count) { max = count; }

        /**
         * return whether idenitifiers only should be returned
         */
        public boolean isIdentifiersOnly() { return identifiersOnly; }

        /**
         * set whether idenitifiers only should be returned.  The 
         * default value is false. 
         */
        public void setIdentifiersOnly(boolean yes) { identifiersOnly = yes; }

    }

    public static void main(String[] args) {
        RequestBuilder rb = new RequestBuilder();
        SOAPMessage msg = null;
        try {
            if (args.length <= 0 || args[0].equals("getIdentity"))
                msg = rb.getIdentity();
            else if (args[0].equals("getResource"))
                msg = rb.getResource("ivo://ivoa.net");
            else if (args[0].equals("keywordSearch")) 
                msg = rb.keywordSearch("quasars [black hole]", true, 0, 0, false);

            msg.writeTo(System.err);
            System.err.println("");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

}