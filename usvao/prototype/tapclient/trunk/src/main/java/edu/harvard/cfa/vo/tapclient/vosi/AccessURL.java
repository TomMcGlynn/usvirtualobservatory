package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;

/**
 * The URL, and its context within a service, that a client uses to access a particular Interface of a service. 
 * @see Interface
 * @see <a href="http://www.ivoa.net/Documents/REC/ReR/VOResource-20080222.html#s:AccessURLInterface">VOResource: an XML Encoding Schema for Resource Metadata</a>
 */
public class AccessURL {
    private String value;
    private String use;

    protected AccessURL() {
	this.value = null;
	this.use = null;
    }

    AccessURL(net.ivoa.xml.voResource.v10.AccessURL xaccessURL) {
	this.value = xaccessURL.getStringValue();
	net.ivoa.xml.voResource.v10.AccessURL.Use xuse = xaccessURL.xgetUse();
	this.use = xuse != null ? xuse.getStringValue() : null;
    }

    /**
     * Returns the url for this AccessURL object.
     * @return the url
     */
    public String getValue() { 
	return value; 
    }

    /**
     * Returns a string indicating the general way the URL is used.  The <a href="">VOResource</a> recommendation specifies the following allowed values:
     * <ul>
     * <li>full a full URL is one that can be invoked directly without alteration.</li>
     * <li>base a base URL is one requiring an extra portion to be appended before being invoked</li>
     * <li>post assume the URL is a service endpoint that requires input sent via the HTTP POST mechanism.</li>
     * <li>dir assume URL points to a directory that will return a listing of files.</li>
     * </ul>
     * @return a string indicating the general way the URL is used.  Though the values listed above are specified in the VOResource Recommendation, this object does not enforce the restriction.
     */
    public String getUse() { 
	return use; 
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String use = getUse();
	String value = getValue();

	if (use != null)
	    output.println(indent+"Use: "+use);
	if (value != null) 
	    output.println(indent+"Value: "+value);
    }
}
