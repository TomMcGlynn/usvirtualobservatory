package net.ivoa.skynode;

/** This class writes the text that needs to be
 *  at the beginning and end of each SOAP message,
 *  include the appropriate HTTP header.
 *  It assumes this is a SkyNode message, hence the
 *  default name specifed specified in the innermost element.
 */
class SoapWrapper {
    
    private String element;
    private String innerElem;
    
    /** The opening an closing strings
     *  for wrapping a SkyNode SOAP response.
     *  This assumes that all responses are of the form:
     *   <Envelope><Body><xxxResponses<xxxResult>...</xxxResult>...
     */
    SoapWrapper (String element) {
	this(element, null);
    }
    
    SoapWrapper (String element, String innerElement) {
	this.element   = element;
	this.innerElem = innerElement;
    }
    
    String prefix() {
	
	String prefix = "Content-Type: text/xml; charset=utf-8\n"+
                        "Status-code:  200\n"+
	                "Status-text:  OK\n\n" +
	                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"                                   +
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                        " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "                               +
	                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"                    +
	                "<soapenv:Body>"                                                                 +
	                "<"+element+"Response xmlns=\"SkyNode.ivoa.net\">";
	prefix += "<"+element+"Result";
	if (innerElem != null) {
	    prefix += " xsi:type=\""+innerElem+"\"";
	}
	prefix += ">";
        return prefix;
    }
    String suffix() {
	String suffix = "</"+element+"Result>";
	suffix += "</"+element+"Response></soapenv:Body></soapenv:Envelope>";
	return suffix;
    }
}
