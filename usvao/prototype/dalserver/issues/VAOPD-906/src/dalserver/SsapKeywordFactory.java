/*
 * SsapKeywordFactory.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;

/**
 * SsapKeywordFactory implements a factory class for well-known SSAP keywords,
 * including Groups, Params and Fields.  The use of a factory class frees
 * the client from having to know all the detailed metadata associated
 * with each type of keyword.  Keywords are indexed by both their ID and
 * UTYPE tags.  In general UTYPE is required to ensure uniqueness, but
 * within a limited scope, the ID tag may be sufficient to uniquely identify
 * a keyword without having to know the full UTYPE.
 *
 * <p>A list of the major SSAP keywords including the defined ID and UTYPE
 * keys is shown in <a href="doc-files/ssap-metadata.html">this table</a>.
 * All non-data keywords from the full
 * <a href="doc-files/spectrum-metadata.html">Spectrum data model</a>
 * are also available, but only those most commonly used in the SSAP query
 * response are shown by default in the SSAP keyword dictionary and
 * code.  For each group (component data model), the ID, UTYPE, and
 * DESCRIPTION fields for the GROUP element are given first, followed by the
 * parameters belonging to that group.  The datatype and unit are also
 * specified for parameter elements.
 *
 * @version	1.0, 13-Dec-2006
 * @author	Doug Tody
 */
public class SsapKeywordFactory extends KeywordFactory {

    /** Null constructor to generate a new SSAP keyword factory. */
    public SsapKeywordFactory() {
	super("Spectrum.");

	String gid=null, key=null; 

	for (String line : SsapKeywordData.data) {
	    String id=null, utype=null, ucd=null, descr=null, fits=null;
	    String csv=null; String dataType=null, arraySize=null, unit=null,
	    ssapUnit=null; String hint=null, defval=null;

	    // Crude parser which assumes that the CSV has the right columns.
	    // (This is only run for code maintenance.)

	    String tok[] = line.split(",", 12+1);
	    if (tok.length < 6)
		continue;

	    id = tok[0].length() == 0 ? null : tok[0].trim();
	    utype = tok[1].length() == 0 ? null : tok[1].trim();
	    ucd = tok[2].length() == 0 ? null : tok[2].trim();
	    descr = tok[3].length() == 0 ? null : tok[3].trim();
	    fits = tok[4].length() == 0 ? null : tok[4].trim();
	    csv = tok[5].length() == 0 ? null : tok[5].trim();

	    if (tok.length > 6)
		dataType = tok[6].length() == 0 ? null : tok[6].trim();
	    if (tok.length > 7)
		arraySize = tok[7].length() == 0 ? null : tok[7].trim();
	    if (tok.length > 8)
		unit = tok[8].length() == 0 ? null : tok[8].trim();
	    if (tok.length > 9)
		ssapUnit = tok[9].length() == 0 ? null : tok[9].trim();
	    if (tok.length > 10)
		hint = tok[10].length() == 0 ? null : tok[10].trim();
	    if (tok.length > 11)
		defval = tok[11].length() == 0 ? null : tok[11].trim();

	    // Skip header line and blank lines.
	    if (utype != null && utype.equals("UTYPE"))
		continue;
	    if (utype == null)
		continue;

	    // For the SSAP keywords, exclude all data-related elements.
	    if (utype.startsWith("Data.") || utype.startsWith("Spectrum.Data."))
		continue;

	    // Process a GROUP followed by a set of PARAMs which belong to
	    // that group.

	    if (id == null) {
		// For the SSAP query, upcast "Spectrum" to the more generic
		// "Dataset".
		if (utype.equals("Spectrum"))
		    utype = "Dataset";

		// Start a new GROUP.
		this.addGroup(id=utype, id, gid=id, key=utype,
		    null, descr, hint);

	    } else {
		// For use within SSAP remove the "Spectrum." prefix.
		if (utype.startsWith("Spectrum."))
		    utype = utype.substring(9);

		// For the Dataset group add a "Dataset." prefix.
		if (key != null && key.equals("Dataset"))
		    utype = key + "." + utype;

		// Add a PARAM to the group.
		this.addParam(id, defval, id, gid, dataType, arraySize,
		    ssapUnit, utype, ucd, descr, fits, csv, hint);
	    }
	}
    }

    /**
     * Create a new SSAP keyword factory and initialize an associated
     * request response to process SSAP keywords.  This is not required,
     * but allows automated initialization of related context such as the      
     * keyword name space.  
     *
     * @param response	RequestResponse object to be linked to the SSAP
     *			keyword factory.
     */
    public SsapKeywordFactory(RequestResponse response)
	throws DalServerException {

	// Create the keyword factory.
	this();

	// Set the response XML namespace for SSAP metadata.
	TableParam xmlnsPar = this.newParam("XmlnsSsap", null);
	response.setXmlns(xmlnsPar.getUtype(), xmlnsPar.getValue());
    }

    /**
     * SSAP keyword-related utilities.
     *
     * <pre>
     *   ingest [csv-file]	Turn a CSV version of the SSAP data model
     *				into a SsapData class which contains raw data
     *				defining the data model.
     *
     *   doc [type]		Generate an HTML version of the SSAP keyword
     * 				dictionary.
     *
     *   table [type]		Generate Java code to create the indicated
     *				keywords in a RequestResponse object.
     * </pre>
     */
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("ingest")) {
	    // Read a CSV version of the SSAP/Spectrum data models, and use
	    // this to generate code for a compiled SsapData class which
	    // encodes the raw keyword data.

	    String inFile = (args.length > 1) ?
		args[1] : "lib/ssap-keywords.csv";
	    String outFile = (args.length > 2) ?
		args[2] : "src/dalserver/SsapKeywordData.java";

	    BufferedReader in = null;
	    PrintWriter out = null;

	    try {
		in = new BufferedReader(new FileReader(inFile));
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot open file " + "["+inFile+"]");
	    }

	    try {
		out = new PrintWriter(outFile);
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot open file " + "["+outFile+"]");
		System.exit(1);
	    }

	    try {
		out.println("package dalserver;");
		out.println("/**");
		out.println(" * Raw data for the SSA and Spectrum " +
		    "data models (this class is autogenerated).");
		out.println(" * See {@link dalserver.SsapKeywordFactory}.");
		out.println(" */");

		out.println("public class SsapKeywordData {");
		out.println("  /** CSV data for the SSAP and Spectrum " +
		    "data models. */");
		out.println("  public static final String[] data = {");
		for (String line;  (line = in.readLine()) != null;  ) {
		    out.println("  \"" + line + "\",");
		}
		out.println("  };");
		out.println("}");

		out.close();
		in.close();

	    } catch (IOException ex) {
		System.out.println(ex.getMessage());
	    }

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of the SSAP keyword dictionary.
	    // Place the generated file in the dalserver/doc-files directory
	    // to have it included in the javadoc.

	    SsapKeywordFactory keywords = new SsapKeywordFactory();
	    keywords.printDoc("ssap-metadata.html", "SSAP Keywords", "Qq");

	} else if (args[0].equals("table")) {
	    // Generate Java code for the SSAP keywords.  This is included
	    // in files like SsapService to generate RequestResponse objects

	    SsapKeywordFactory keywords = new SsapKeywordFactory();
	    keywords.printCode("ssap-table.txt", "ssap", "Qq");
	}
    }
}
