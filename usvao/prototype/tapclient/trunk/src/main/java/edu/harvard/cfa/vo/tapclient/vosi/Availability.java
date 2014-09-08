package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;

import net.ivoa.xml.vosiAvailability.v10.AvailabilityDocument;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;

/**
 * The operability and reliability of a service for extended and scheduled requests.
 <pre>
 <code>
    edu.harvard.cfa.vo.tapclient.tap.TapService tapService = new edu.harvard.cfa.vo.tapclient.tap.TapService(baseURL);
    Availability availability = tapService.getAvailability();
    if (! availability.isAvailable()) {
	System.out.println("Service is unavailable.");
	Calendar backAt = availability.getBackAt();
	if (availability.getBackAt() != null) {
	    Calendar now = Calendar.getInstance();
	    if (backAt.compareTo(now) >= 0) {
		System.out.println("Sleeping...");
		long delay = (backAt.getTimeInMillis()-now.getTimeInMillis())+5000;
		Thread.sleep(delay);
	    }
	    
	    // Update the availability
	    availability.update();
	    
	    System.out.println("Service is "+(availability.isAvailable() ? "available." : " not available."));
	} else {
	    System.out.println("Service did not specify when it would be back.");
	}
    } else {
	System.out.println("Service is available");
    }

    for (String note: availability.getNotes()) {
	System.out.println("TAP service note: "+note);
    }
 </code>
 </pre>
 */
public class Availability {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.vosi.Availability");

    private String fullURL;
    private boolean available;
    private Calendar upSince;
    private Calendar downAt;
    private Calendar backAt;
    private List<String> noteList;

    /**
     * Construct an Availability for the given Vosi.
     * @param fullURL the full URL associated with this availability
     */
    public Availability(String fullURL) {
	this.fullURL = fullURL;
	this.available = false;
	this.upSince = null;
	this.downAt = null;
	this.backAt = null;
	this.noteList = new ArrayList<String>();
    }

    /**
     * Returns true if the service is accepting requests.  The value returned is current 
     * as of the last call to <code>update</code>.
     * @return true if the service is accepting requests, false otherwise.
     * @see #update
     */
    public boolean isAvailable() {
	return available;
    }

    /**
     * Returns the instant since which the service has been continuously available.  The value returned is current as of the last call to update.
     * @return the instant in time the service has been continuously available or null if the value was not provided by the service.
     * @see #update
     */
    public Calendar getUpSince() {
	return upSince;
    }

    /**
     * Returns the instant at which the service is scheduled to be unavailable.  The value returned is current as of the last call to update.
     * @return the instant at which the service is scheduled to be unavailable or null if the value was not provided by the service.
     * @see #update
     */
    public Calendar getDownAt() {
	return downAt;
    }

    /**
     * Returns the instant at which the service is scheduled to become available.  The value returned is current as of the last call to update.
     * @return the instant at which the service is scheduled to become available or null if the value was not provided by the service.
     * @see #update
     */
    public Calendar getBackAt() {
	return backAt;
    }

    /**
     * Returns a list of notes, for example explaining the reason for unavailability.  The value returned is current as of the last call to update.
     * @return a list of notes or null if the value was not provided by the service.
     * @see #update
     */
    public List<String> getNotes() {
	return Collections.unmodifiableList(noteList);
    }

    /**
     * Updates this Availability object with the latest response from the service.  An HTTP request is made to the service associated with this Availability object.
     * @throws HttpException if the service responses to the VOSI Availability request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Availability document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public void update() throws HttpException, ResponseFormatException, IOException {
	if (logger.isLoggable(Level.FINE)) 
	    logger.log(Level.FINE, fullURL);

        InputStream inputStream = null;
	try {
	    inputStream = HttpClient.get(fullURL);

	    // Availability
	    XmlOptions availabilityOptions = new XmlOptions();
	    //   Namespace substitution
	    Map<String,String> availabilityNamespaces = new HashMap<String,String>();
	    availabilityNamespaces.put("http://www.ivoa.net/xml/Availability/v0.4", "http://www.ivoa.net/xml/VOSIAvailability/v1.0");
	    availabilityOptions.setLoadSubstituteNamespaces(availabilityNamespaces);
	    //   Document element replacement
	    QName availabilityDocumentElement = new QName("http://www.ivoa.net/xml/VOSIAvailability/v1.0", "availability", "vosi");
	    availabilityOptions.setLoadReplaceDocumentElement(availabilityDocumentElement);
	    AvailabilityDocument xdocument = AvailabilityDocument.Factory.parse(inputStream, availabilityOptions);

	    net.ivoa.xml.vosiAvailability.v10.Availability xavailability = xdocument.getAvailability();

	    available = false;
	    upSince = null;
	    downAt = null;
	    backAt = null;
	    noteList.clear();
	    if (xavailability != null) {
		try {
		    available = xavailability.getAvailable();
		    try {
			upSince = xavailability.getUpSince();
		    } catch (IllegalArgumentException ex) {
			if (logger.isLoggable(Level.INFO))
			    logger.log(Level.INFO, "upSince value is not ISO 8601 compliant");
			
			upSince = parseDateTime(xavailability.xgetUpSince().toString()).getCalendarValue();
		    }
		    try{
			downAt = xavailability.getDownAt();
		    } catch (IllegalArgumentException ex) {
			if (logger.isLoggable(Level.INFO))
			    logger.log(Level.INFO, "downAt value is not ISO 8601 compliant");
			
			downAt = parseDateTime(xavailability.xgetDownAt().toString()).getCalendarValue();
		    }
		    try{
			backAt = xavailability.getBackAt();
		    } catch (IllegalArgumentException ex) {
			if (logger.isLoggable(Level.INFO))
			    logger.log(Level.INFO, "backAt value is not ISO 8601 compliant");
			
			backAt = parseDateTime(xavailability.xgetBackAt().toString()).getCalendarValue();
		    }
		    noteList.addAll(xavailability.getNoteList());
		} catch (RuntimeException ex) {
		    throw new ResponseFormatException("error parsing VOSI Availability response: "+ex.getMessage(), ex);
		}
	    }
	} catch (XmlException ex) {
	    throw new ResponseFormatException("error parsing VOSI Availability response: "+ex.getMessage(), ex);
	} catch (HttpException ex) {
	    throw new HttpException("error getting VOSI Availablity response: "+ex.getMessage(), ex);
	} catch (ResponseFormatException ex) {
	    throw ex;
	} catch (IOException ex) {
	    throw new IOException("error reading VOSI Availability response: "+ex.getMessage(), ex);
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (Exception ex) {
		    if (logger.isLoggable(Level.WARNING))
			logger.log(Level.WARNING, "error closing input stream", ex);
		}
	    }
	} 
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	boolean available = isAvailable();
	Calendar upSince = getUpSince();
	Calendar downAt = getDownAt();
	Calendar backAt = getBackAt();
	List<String> notes = getNotes();

	output.println(indent+"Available: "+available);

	if (upSince != null) 
	    output.println(indent+"Up since: "+DatatypeConverter.printDateTime(upSince));
	if (downAt != null) 
	    output.println(indent+"Down at: "+DatatypeConverter.printDateTime(downAt));
	if (backAt != null) 
	    output.println(indent+"Back at: "+DatatypeConverter.printDateTime(backAt));

	if (notes != null) 
	    for (String note: notes) 
		output.println(indent+"Note: "+note);
    }

    private XmlDateTime parseDateTime(String value) throws XmlException {
	XmlString xstring = XmlString.Factory.parse(value);
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
	ParsePosition parsePosition = new ParsePosition(0);
	Date date = dateFormat.parse(xstring.getStringValue(), parsePosition);
		
	XmlDateTime xdateTime = XmlDateTime.Factory.newInstance();
	xdateTime.setDateValue(date);
	return xdateTime;
    }
}
