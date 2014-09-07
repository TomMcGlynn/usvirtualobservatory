package net.ivoa.skynode;

import java.sql.ResultSet;
import org.w3c.dom.Node;


/** Handle the query of the local database.
 *  This class converts the VO style SQL to
 *  SQL that the local database can manage.
 */
public class Query {
    
    private String    origSQL;
    private String    currSQL;
    
    private String    alias;  
    private String    upload;
    
    private DBQuery   myDB;
    private Node      plan;
    private Node      element;
    private String[]  xmtabs;
    
    /** Create a query without a query plan.
     *  This is used for cost estimates and the basic SkyNode.
     */
    public Query(DBQuery dbq, String sql, String tableAlias, String uploadAlias) {
	this(dbq, sql, tableAlias, uploadAlias, null, null);
    }
    
    /** Create a query within information about the
     *  full executation plan.
     *  The conversion of the query to the local database
     *  may depend upon the position of this node in the execution plan.
     */
    public Query(DBQuery dbq, String sql, String tableAlias, String uploadAlias, Node plan, Node element) {
	origSQL = sql;
	currSQL = sql;
	alias   = tableAlias;
	upload  = uploadAlias;
	myDB    = dbq;
	  
	FullNode.log("Query: Initial SQL:"+sql+"  Alias:"+alias+"  Upload:"+upload);
	this.plan    = plan;
	this.element = element;
    }
    
    /** Convert the query to a query the local database can accommodate. */
    public void transform() throws Exception {
	region();
	if (alias != null) {
	    if (plan != null && element != null) {
	        xmatch();
	    }
	}
	FullNode.log("Query: after transform SQL is:"+currSQL);
    }
    
    /** Convert a region specification */
    private void region() throws Exception {
	int start     = currSQL.indexOf("REGION(");
	int end       = currSQL.indexOf(")", start);
	
	if (start < 0 || end < 0) {
	    return;
	}
	String regStr = currSQL.substring(start+7,end).trim();
	if (regStr.length() < 2) {
	    return;
	}
	if (regStr.charAt(0) == '\'' && regStr.charAt(regStr.length()-1) == '\'') {
	    regStr = regStr.substring(1, regStr.length()-1);
	}
        String[] fields = regStr.split(" ");
	String newReg = stdRegion(fields);
	if (newReg == null) {
	    newReg = "(1=1)";
	}
	currSQL = currSQL.substring(0,start)+newReg+currSQL.substring(end+1);
    }
    
    /** Handle a standard region request */
    private String stdRegion(String[] fields) throws Exception {
	
	// Check for proper number of fields.
	if (fields.length != 5  && fields.length != 6) {
	    return null;
	}
	
	
	// Only handle J2000
	if (!fields[1].equals("J2000") && !fields[1].equals("ICRS")) {
	    return null;
	}
	
	if (fields[0].equalsIgnoreCase("CIRCLE")) {
	    
	    double ra     = Double.parseDouble(fields[2]);
	    double dec    = Double.parseDouble(fields[3]);
	    double radius = Double.parseDouble(fields[4]);
	    return cone(ra, dec, radius);
	    
	} else if (fields[1].equalsIgnoreCase("CARTESIAN")) {
	    
	    double x   = Double.parseDouble(fields[2]);
	    double y   = Double.parseDouble(fields[2]);
	    double z   = Double.parseDouble(fields[2]);
	    double r   = Double.parseDouble(fields[5]);
	    double dec = Math.toDegrees(Math.acos(z));
	    double ra  = Math.toDegrees(Math.atan2(y, x));
	    return cone(ra, dec, r);
	    
	} else {
	    return null;
	}
    }
    
    /** Return the sql needed for a query in a cone of radius rad
     *  around the position ra,dec.
     */
    private String cone(double ra, double dec, double rad) {
	
	String a = alias;
	if (a == null) a = "a";
	
	String cond;
	//  Assume radius in arcminutes.
	rad = rad/60;
	double minDec = dec-rad;
	double maxDec = dec+rad;
	
	String rn = SkyNode.raName;
	String dn = SkyNode.decName;
	
	if (minDec < -90) {
	    minDec = -90;
	}
	if (maxDec > 90) {
	    maxDec = 90;
	}
	cond = "("+a+"."+dn+" between "+minDec+" and "+maxDec+")";
	
	double rar  = Math.toRadians(ra);
	double decr = Math.toRadians(dec);
	double r    = Math.toRadians(rad);
	
	// Look for RA limits?
	if (maxDec < 90 && minDec > -90) {
	
	    
	    // The maximum range in longitude occurs at the latitude:
	    //   sin(b) = sin(b0)/cos(r)
	    // Putting this in the radius function gives:
	    // 

	    double deltal = Math.sqrt(Math.cos(r)*Math.cos(r) - Math.sin(decr)*Math.sin(decr) )/ Math.cos(decr);
	    
	    if (Math.abs(deltal) < 1) {
		deltal = Math.acos(deltal);
	    } else {
		deltal = 1.e-7;  // A small number.
	    }
	    deltal = Math.toDegrees(deltal);
	    
	    double minRA = ra - deltal;
	    double maxRA = ra + deltal;
	    
	    if (minRA < 0) {
		minRA += 360;
	    }
	    
	    if (maxRA > 360) {
		maxRA -= 360;
	    }
	    
	    String not = "";
	    
	    if (minRA > maxRA) {
		double temp = minRA;
		minRA = maxRA;
		maxRA = temp;
		not = " not ";
	    }
	    
	    cond += " and ("+a+"."+rn+" " + not + " between "+minRA+" and " +maxRA+")";
	}
	
	// Now do the actual distance calculation.  The Haversine formula is:
	//   d = 2 asin( [sin( (d0-d1)/2 ) ]^2 + cos(d0)cos(d1)[ sin( (a0-a1)/2 )]^2 ) 
	// or equivalently
	//   sin(d/2) = [sin(....
	// with all quantities in radians.  We assume the database is stored
	// internally with RA and Dec in decimal degrees.
	double rd     =  Math.atan2(1,0)/90;
	
	double max    = Math.sin(r/2);
	
	String sdeld  = "sin( ("+a+"."+dn+"-("+dec+"))*"+rd+"/2)";
	double ccos0  = Math.cos(decr);
	String ccos   = "cos("+a+"."+dn+"*"+rd+")";
	String sdelr  = "sin( ("+a+"."+rn+"-("+ra+"))*"+rd+"/2)";
	
	String hvs    = sdeld+"*"+sdeld+"  +  ("+ccos0+")*"+ccos+"*"+sdelr+"*"+sdelr;
	cond += " and ( ("+hvs+") <= "+max+") ";
	return cond;
    }
	
    /** Run the query */
    public String execute() throws Exception {
	myDB.query(currSQL);
	
	ResultSet rs  = myDB.getResults();
	Encoder   enc = new Encoder(rs);
	String    vot = enc.encode();
	FullNode.log("Query: Got VOTable:"+vot);
	return vot;
    }
    
    /** Return the count from a query of the
     *  form select count(*) from ....
     */
    public int count() throws Exception {
	myDB.query(currSQL);
	ResultSet rs = myDB.getResults();
	if (rs.next()) {
	    return rs.getInt(1);
	} else {
	    return 0;
	}
    }
    
   /** Update SQL to handle an XMatch.
     * 
     *  The XMATCH is magic but here is what we do.
     * 
     *  If there is an XMATCH in the current query:
     * 
     *     We assume that we have the XMATCH columns uploaded
     *     in a downstream query.
     * 
     *  If there is a previous query
     *  
     *     We assume that we need to return the XMATCH columns
     *     in the query list reqardless of whether they
     *     are currently specified.  If there is no XMATCH
     *     in the current query we need to generate weighted
     *     XMATCH columns.
     * 
     * 
     *  The XMATCH can be a match or an anticorrelation.
     *  The first table is the only one that can be an anticorrelation.
     * 
     *  The XMATCH columns are:
     *     XMATCH_A[XYZ] - The weighted sums of the unit vectors.
     *     XMATCH_A      - The sums of the weights.
     */
    void xmatch() throws Exception {
	boolean nxc = needXMatchColumns();
	boolean hxc = true;
	if (nxc) {
	    hxc = haveXMatchColumns();
	}
	
	boolean hx   = haveXMatch();
	boolean anti = false;
	
	// Do I need to do anything?
	if (!nxc && !hx) {
	    return;
	}
	
        if (hx) {
	    anti = haveAnti();
	}
	
	if (!hxc) {
	    getXmatchColumns(hx, anti);
	}
	
	if (hx) {
	    doXmatch(anti);
	}
	
	if (currSQL.matches(".*\\bxup.xmatch_chisq\\b.*") ) {
	    addChisq();
	}
	
    }
    
    /** Does this query need to return the XMATCH columns? */
    private boolean needXMatchColumns() {
	return element.getPreviousSibling() != null;
    }
    
    /** Does this query already specify XMATCH columns? */
    private boolean haveXMatchColumns() {
	return (currSQL.matches(".* as XMATCH_A.*"));
    }
    
    /** Does this query have an XMATCH constraint? */
    private boolean haveXMatch() {
	return (currSQL.matches(".* XMATCH\\(.*"));
    }
    
    /** Does this query have an XMATCH anticorrelation? */
    private boolean haveAnti() {
	return (currSQL.matches(".* XMATCH\\(!.*"));
    }
    
    /** Add in the unit vector columns */
    private void getXmatchColumns(boolean haveXmatch, boolean anti) {
	
	String x = "";
	String y = "";
	String z = "";
	String a = "";
	String plus = "";
	
	// If anticorrelation, don't add in the current node's values
	if (!anti) {
	    
	    String rn = alias+"."+ SkyNode.raName+"*"+Math.atan2(1,0)/90;
	    String dn = alias+"."+ SkyNode.decName+"*"+Math.atan2(1,0)/90;
	    x = "cos("+rn+")*cos("+dn+")*"+SkyNode.TableWeight;
	    y = "sin("+rn+")*cos("+dn+")*"+SkyNode.TableWeight;
	    z = "sin("+dn+")*"+SkyNode.TableWeight;
	    a = ""+SkyNode.TableWeight;
	    plus = " + ";
	}
	
	// If we are doing an XMATCH there will
	// be downline values to add in.
	if (haveXmatch) {
	    x += plus+upload+".xmatch_ax";
	    y += plus+upload+".xmatch_ay";
	    z += plus+upload+".xmatch_az";
	    a += plus+upload+".xmatch_a";
	}
	
	x += " as xmatch_ax";
	y += " as xmatch_ay";
	z += " as xmatch_az";
	a += " as xmatch_a";
	
	int from = currSQL.indexOf(" FROM ");
	currSQL  = currSQL.substring(0,from)+","+x+","+y+","+z+","+a+currSQL.substring(from);
    }
    
    /** Replace the XMATCH query constraint with XML that
     *  we can do.
     */
    private void doXmatch(boolean anti) throws Exception {
	
	int begin = currSQL.indexOf("XMATCH(");
	int end   = currSQL.indexOf(")", begin);
	
	String   innards = currSQL.substring(begin+7, end);
	xmtabs    = innards.split(",");
	if (xmtabs.length != 2) {
	    throw new Exception("Number of tables in Node XMATCH is "+xmtabs.length+". Should be 2.");
	}
	
	if (xmtabs[0].startsWith("!")) {
	    xmtabs[0] = xmtabs[0].substring(1);
	}
	String chisq = getChisq();
	
    
	String lastSQL = currSQL;
        currSQL = currSQL.substring(0,begin)+ chisq + currSQL.substring(end+1);
	
	if (!anti) {
	    return;
	}
	
	int xstart = 7; // Get past the select...
	int xend   = currSQL.indexOf(" FROM ");
	String xsql = currSQL.substring(0,7) + " into "+SkyNode.TempTableName+"2 "+upload+"._unique_id" +
	              currSQL.substring(xend);
	myDB.query(xsql);
	
	ResultSet rs = myDB.getResults();
	while (rs.next()) {
	}
	
	// Now there is a temporary table with all the rows we don't want!
	// Execute the original query (with the original argument list)
	// but replace the XMATCH with a not in (select ...)
	
	String xm = "(_unique_id not in (select _unique_id from "+SkyNode.TempTableName+"2))";
	
	// The logic for parsing out the XMATCH criterion and handling
	// the < xxx needs to change for later XMATCH.
	
	xm += "  0";   // This should give '_unique_id not in ...from #temp2)) + 0<sigma ...
	currSQL = lastSQL.substring(0, begin)+xm+lastSQL.substring(end+1);
    }
    
    /** Return the Chi-square value string */
    private String getChisq() {
	String t = xmtabs[0];
	if (t.equals(upload)) {
	    t = xmtabs[1];
	}
	String u = upload;
	
	String rn = t+"."+SkyNode.raName +"*"+Math.atan2(1,0)/90;
	String dn = t+"."+SkyNode.decName+"*"+Math.atan2(1,0)/90;
	String x = "cos("+rn+")*cos("+dn+")*"+SkyNode.TableWeight + "+" +u+ ".xmatch_ax";
	String y = "sin("+rn+")*cos("+dn+")*"+SkyNode.TableWeight + "+" +u+ ".xmatch_ay";
	String z = "sin("+dn+")*"+SkyNode.TableWeight             + "+" +u+ ".xmatch_az";
	String a = ""+SkyNode.TableWeight+" + "+u+".xmatch_a";
	String xm = "( ("+a+") - "+
	            "sqrt(power("+x+",2) + power("+y+",2)+power("+z+",2)) )";
        return xm;
    }
    
    /** Add the chi-square value to the list of returned values.
     *  Generally this is done for the top node just before returning the
     *  results to the portal node.
     */
    private void addChisq() {
        String xsq = getChisq();
	currSQL = currSQL.replaceFirst("xup\\.xmatch_chisq", xsq+" as xmatch_chisq");
    }
}
