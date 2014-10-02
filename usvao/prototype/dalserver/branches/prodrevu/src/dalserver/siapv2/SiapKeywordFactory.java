/*
 * SiapKeywordFactory.java
 * $ID*
 */

package dalserver.siapv2;

import dalserver.conf.XMLConfiguredKeywordFactory;
import dalserver.conf.KeywordConfig;
import dalserver.RequestResponse;
import dalserver.KeywordFactory;
import dalserver.TableParam;
import dalserver.DalServerException;

import java.io.*;
import java.util.*;

/**
 * SiapKeywordFactory implements a factory class for well-known SIAP keywords,
 * including Groups, Params and Fields.  The use of a factory class frees
 * the client from having to know all the detailed metadata associated
 * with each type of keyword.  Keywords are indexed by both their ID and
 * UTYPE tags.  In general UTYPE is required to ensure uniqueness, but
 * within a limited scope, the ID tag may be sufficient to uniquely identify
 * a keyword without having to know the full UTYPE.
 *
 * <p>A list of the major SIAP keywords including the defined ID and UTYPE
 * keys is shown in <a href="doc-files/siapv2-metadata.html">this table</a>.
 *
 * @version	2.0, 19-Aug-2013
 * @author	Doug Tody
 */
public class SiapKeywordFactory extends KeywordFactory {

    /**
     * Constructor to generate a new SIAP keyword factory.  To generate a
     * keyword factory for only the Data element use model="data".  To
     * return the main model (both Access and Core), use model="main".
     *
     * @param	model	Data model to be used ("main" or "data").
     *
     */
    public SiapKeywordFactory(String model) {
	super("Image.");  // Can probably remove this for the ImageDM

	String gid=null, key=null; 
	boolean dataElement = model.equalsIgnoreCase("data");
	boolean inDataElement = false;

	for (String line : SiapKeywordData.data) {
	    String id=null, type=null, utype=null, ucd=null, descr=null, fits=null;
	    String csv=null; String dataType=null, arraySize=null, unit=null,
	    siapUnit=null; String hint=null, defval=null;

	    // Skip forward if we are building a factory for the Data element,
	    // otherwise stop when we reach the Data element.

	    if (dataElement) {
		if (!inDataElement) {
		    if (line.startsWith("## DATA"))
			inDataElement = true;
		    continue;
		}
	    } else if (line.startsWith("## DATA"))
		break;

	    // Crude parser which assumes that the CSV has the right columns.
	    // This is only run for code maintenance to update the ImageDM.
	    // Note embedded commas are not permitted in fields (could be fixed,
	    // but it is easy to avoid embedded commas in the DM spreadsheet).

	    String tok[] = line.split(",", 13+1);
	    if (tok.length < 7)
		continue;

	    id = tok[0].length() == 0 ? null : tok[0].trim();
	    type = tok[1].length() == 0 ? null : tok[1].trim();
	    utype = tok[2].length() == 0 ? null : tok[2].trim();
	    ucd = tok[3].length() == 0 ? null : tok[3].trim();
	    descr = tok[4].length() == 0 ? null : tok[4].trim();
	    fits = tok[5].length() == 0 ? null : tok[5].trim();
	    csv = tok[6].length() == 0 ? null : tok[6].trim();

	    if (tok.length > 7)
		dataType = tok[7].length() == 0 ? null : tok[7].trim();
	    if (tok.length > 8)
		arraySize = tok[8].length() == 0 ? null : tok[8].trim();
	    if (tok.length > 9)
		unit = tok[9].length() == 0 ? null : tok[9].trim();
	    if (tok.length > 10)
		siapUnit = tok[10].length() == 0 ? null : tok[10].trim();
	    if (tok.length > 11)
		hint = tok[11].length() == 0 ? null : tok[11].trim();
	    if (tok.length > 12)
		defval = tok[12].length() == 0 ? null : tok[12].trim();

	    // Skip header line and blank and comment lines.
	    if (utype != null && utype.equals("UTYPE"))
		continue;
	    if (utype == null)
		continue;
	    if (id != null && id.startsWith("#"))
		continue;

	    // For the SIAP keywords, exclude all data-related elements. (Not needed for SIAV2)
	    // if (utype.startsWith("Data.") || utype.startsWith("Image.Data."))
	    //     continue;

	    // Process a GROUP followed by a set of PARAMs which belong to
	    // that group.

	    if (id == null) {
		// For the SIAP query, upcast "Image" to the more generic
		// "Dataset". (Not needed for SIAV2)
		// if (utype.equals("Image"))
		//     utype = "Dataset";

		// Start a new GROUP.
		this.addGroup(id=utype, id, gid=id, key=utype,
		    null, descr, hint);

	    } else {
		// For use within SIAP remove the "Image." prefix.
		if (utype.startsWith("Image."))
		    utype = utype.substring(6);

		// For the Dataset group add a "Dataset." prefix. (Not needed for SIAV2)
		// if (key != null && key.equals("Dataset"))
		//     utype = key + "." + utype;

		// Add a PARAM to the group.
		this.addParam(id, defval, id, gid, dataType, arraySize,
		    siapUnit, utype, ucd, descr, fits, csv, hint);
	    }
	}
    }


    /**
     * Generate a new SIAP keyword factory for the main part of the ImageDM
     * (excluding the Data element).
     */
    public SiapKeywordFactory() {
	this("main");
    }

    /**
     * Create a new SIAP keyword factory and initialize an associated
     * request response to process SIAP keywords.  This is not required,
     * but allows automated initialization of related context such as the      
     * keyword name space.  
     *
     * @param response	RequestResponse object to be linked to the SIAP
     *			keyword factory.
     */
    public SiapKeywordFactory(RequestResponse response)
	throws DalServerException {

	// Create the keyword factory.
	this();

	// Set the response Utype namespace for SIAP metadata.
	TableParam DMprefix = this.newParam("DataModelPrefix", null);
	TableParam DMurl = this.newParam("DataModelURL", null);
	response.setXmlns(DMprefix.getValue(), DMurl.getValue());
    }

    /**
     * SIAP keyword-related utilities.
     *
     * <pre>
     *   ingest [csv-file]	Turn a CSV version of the SIAP data model
     *				into a SiapData class which contains raw data
     *				defining the data model.
     *
     *   doc [type]		Generate an HTML version of the SIAP keyword
     * 				dictionary.
     *
     *   table [type]		Generate Java code to create the indicated
     *				keywords in a RequestResponse object.
     * </pre>
     */
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("ingest")) {
	    // Read a CSV version of the SIAP/Image data models, and use
	    // this to generate code for a compiled SiapData class which
	    // encodes the raw keyword data.

	    String inFile = (args.length > 1) ?
		args[1] : "lib/siapv2-keywords.csv";
	    String outFile = (args.length > 2) ?
		args[2] : "src/dalserver/siapv2/SiapKeywordData.java";

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
		out.println("package dalserver.siapv2;");
		out.println("/**");
		out.println(" * Raw data for the SIAP and Image " +
		    "data models (this class is autogenerated).");
		out.println(" * See {@link dalserver.siapv2.SiapKeywordFactory}.");
		out.println(" */");

		out.println("public class SiapKeywordData {");
		out.println("  /** CSV data for the SIAP and Image " +
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
	    // Generate an HTML version of the SIAP keyword dictionary.
	    // Place the generated file in the dalserver/doc-files directory
	    // to have it included in the javadoc.

	    SiapKeywordFactory mainKeywords = new SiapKeywordFactory();
	    mainKeywords.printDoc("siapv2-core-metadata.html", "SIAV2 Main Model Keywords", "");
	    SiapKeywordFactory dataKeywords = new SiapKeywordFactory("data");
	    dataKeywords.printDoc("siapv2-data-metadata.html", "SIAV2 Data Element Keywords", "");

	} else if (args[0].equals("table")) {
	    // Generate Java code for the SIAP keywords.  This is included
	    // in files like SiapService to generate RequestResponse objects

	    SiapKeywordFactory keywords = new SiapKeywordFactory();
	    keywords.printCode("siapv2-table.txt", "siap", "Qqm");
	}
    }
}
