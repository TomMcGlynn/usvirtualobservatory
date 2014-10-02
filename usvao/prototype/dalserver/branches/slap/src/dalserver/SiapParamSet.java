/*
 * SiapParamSet.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;

/**
 * Construct an initial default parameter set containing the parameters
 * for a SIAP service.  Currently this paramset contains the combined
 * parameters for all SIAP operations.  A list of the currently defined
 * parameters, including their name, type, and description, can be found
 * in <a href="doc-files/siap-params.html">this table</a>.
 * 
 * @version	1.0, 27-Aug-2008
 * @author	Doug Tody
 */
public class SiapParamSet extends ParamSet implements Iterable {

    /** Create an initial default SIAP parameter set. */
    public SiapParamSet() throws DalServerException, InvalidDateException {
	// Shorthand for param type and level.
	final EnumSet<ParamType> STR = EnumSet.of(ParamType.STRING);
	final EnumSet<ParamType> BOO = EnumSet.of(ParamType.BOOLEAN);
	final EnumSet<ParamType> INT = EnumSet.of(ParamType.INTEGER);
	final EnumSet<ParamType> FLO = EnumSet.of(ParamType.FLOAT);
	final EnumSet<ParamType> ISO = EnumSet.of(ParamType.ISODATE);
	final EnumSet<ParamType> RIO = EnumSet.of(ParamType.INTEGER, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RIU = EnumSet.of(ParamType.INTEGER, ParamType.RANGELIST);
	final EnumSet<ParamType> RFO = EnumSet.of(ParamType.FLOAT, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RFU = EnumSet.of(ParamType.FLOAT, ParamType.RANGELIST);
	final EnumSet<ParamType> RSO = EnumSet.of(ParamType.STRING, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RSU = EnumSet.of(ParamType.STRING, ParamType.RANGELIST);
	final EnumSet<ParamType> RDO = EnumSet.of(ParamType.ISODATE, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RDU = EnumSet.of(ParamType.ISODATE, ParamType.RANGELIST);
	final EnumSet<ParamType> RLO = EnumSet.of(ParamType.RANGELIST, ParamType.ORDERED);

	// Define all core parameters defined by the SIAP standard.
	// This would be more flexibly done by reading an external
	// schema, but a wired in approach is simpler for now.

	// General protocol-level parameters.
	this.addParam(new Param("VERSION",     STR, "1.0", "SIAP protocol version"));
	this.addParam(new Param("REQUEST",     STR, "Operation to be performed"));

	// Parameters for the queryData operation.
	this.addParam(new Param("POS",         RFU, "Central coordinates of search region"));
	this.addParam(new Param("SIZE",        RFU, "Size (diameter) of the search region"));
	this.addParam(new Param("FORMAT",      RSU, "Allowable output data formats"));
	this.addParam(new Param("INTERSECT",   STR, "Specifies how image footprint may overlap ROI"));

	this.addParam(new Param("NAXIS",       RIU, "Size of the output image in pixels"));
	this.addParam(new Param("CFRAME",      STR, "Spatial reference frame of output image"));
	this.addParam(new Param("EQUINOX",     FLO, "Equinox of spatial coordinate frame"));
	this.addParam(new Param("CRPIX",       RFU, "Pixel coordinates of reference point"));
	this.addParam(new Param("CRVAL",       RFU, "World coordinates of reference point"));
	this.addParam(new Param("CDELT",       RFU, "Output image scale in degrees per pixel"));
	this.addParam(new Param("ROTANG",      FLO, "Rotation of output image relative to CFRAME"));
	this.addParam(new Param("PROJ",        STR, "Celestial projection of the output image"));

	// Define any service-defined extension parameters here.
	// Client-defined parameters can only be specified at runtime.

	this.addParam(new Param("Maxrec",      INT, "Maximum number of output records"));
	this.addParam(new Param("Compress",    BOO, "Allow dataset compression"));
	this.addParam(new Param("RunID",       STR, "Runtime job ID string"));

	// Mark these as service defined params as the are not in SIAP V1.0.
	this.getParam("Maxrec").setLevel(ParamLevel.SERVICE);
	this.getParam("Compress").setLevel(ParamLevel.SERVICE);
	this.getParam("RunID").setLevel(ParamLevel.SERVICE);
    }


    // Exercise the parameter mechanism.
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("test")) {
	    try {
		// Create a new, default SIAP queryData parameter set.
		SiapParamSet p = new SiapParamSet();

		// Set some typical parameter values.
		p.setValue("POS", "12.0,0.0");
		p.setValue("SIZE", "0.2");

		// Print out the edited SIAP parameter set.
		System.out.println ("SIAP: " + p);

	    } catch (DalServerException ex) {
		System.out.println ("DalServerException");
	    } catch (InvalidDateException ex) {
		System.out.println ("invalid date format");
	    }

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of a SIAP parameter set.
	    // Place the generated file into the dalserver/doc-files directory
	    // to have it included in the Javadoc.

	    String fname = "siap-params.html";
	    Object last = null;

	    try {
		// Create an SIAP parameter set.
		SiapParamSet siap = new SiapParamSet();

		// Output the parameter set in HTML format.
		PrintWriter out = new PrintWriter(new FileWriter(fname));
		out.println("<HTML><HEAD>");
		out.println("<TITLE>SIAP Parameters</TITLE>");
		out.println("</HEAD><BODY>");
		out.println("<TABLE width=700 align=center>");
		out.println("<TR><TD " +
		    "colspan=3 align=center bgcolor=\"LightGray\">" +
		    "SIAP Parameters" + "</TD></TR>");

		for (Iterator i = siap.iterator();  i.hasNext();  ) {
		    Map.Entry me = (Map.Entry) i.next();
		    Object obj = (Object) me.getValue();
		    if (obj == last)
			continue;
		    Param o = (Param)obj;

		    out.println("<TR><TD>" + o.getName() + 
			"</TD><TD>" + o.getType() + 
			"</TD><TD>" + o.getDescription() + 
			"</TD></TR>");

		    last = obj;
		}

		out.println("</TABLE>");
		out.println("</BODY></HTML>");
		out.close();

	    } catch (DalServerException ex) {
		System.out.println ("cannot create SIAP parameter set");
	    } catch (InvalidDateException ex) {
		System.out.println ("invalid date format");
	    } catch (IOException ex) {
		System.out.println ("cannot write file " + fname);
	    }
	}
    }
}
