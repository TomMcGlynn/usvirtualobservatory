package net.ivoa.skynode;

import net.ivoa.util.Settings;
import java.lang.reflect.Field;

/** SkyNode processing values. These values are not final, so if a user
 *  wishes to adjust them in some initialization phase that should work fine.
 *  E.g., a user might be providing serveral different SkyNodes using
 *  the same code base.  Each of these nodes would read in their
 *  initialization (in some fashion) and would need to adjust at least the
 *  TargetNode value to distinguish the various nodes.
 */
public class SkyNode {
    
    /** The root directory for Web documents */
    public  static String HTTPPrefix   = "/skyview/htdocs";
    
    /** The root directory for Web scripts */
    public  static String CGIPrefix    = "/skyview/htdocs/cgi-bin";
    
    /** The host name of the machine */
    public  static String HTTPHost     = "skyview.gsfc.nasa.gov";
    
    /** The directory relative to the CGI prefix where scripts are to be found.*/
    public  static String CGIBase      = "/vo/skynode/";
    
    /** The full path of the SkyNode CGI directory */
    public  static String CGIHome      = CGIPrefix + CGIBase;
    
    /** The directory relative to the http prefix where SkyNode files are to be found */
    public  static String URLBase      = "/vo/skynode/";
    
    /** The full path of the SkyNode URL directory */
    public  static String URLHome      = HTTPPrefix + URLBase;
    
    /** The WSDL file for the SkyNode. This file defines the contract
     *  which this code is to support.
     *  The URLs in the WSDL file should be updated to the location
     *  where the sn.pl CGI script is placed.
     */
    public  static String WSDLFile     = URLHome + "skynode_mysql.wsdl";
    
    /** The XSL file used to transform the XML based representation of ADQL
     *  to an SQL based representation.
     *  Note that the XSL file supplied generates ADQL with a SQL Server
     *  like dialect.  You may need to update this XSL file to accommodate
     *  other databases, or you can edit the SQL statement itself.  E.g.,
     *     select top 10 xxx from yyy
     *  should be 
     *     set rowcount 10 select xxx from yyy set rowcount 0
     *  for Sybase.
     */
    public  static String XSLFile      = URLHome + "xml2sql.xsl";
    
    /** Maximum length of string.  Should be kept
     *  small enough to fit the machine database.
     */
    public  static int MaxString       = 128;
    
    /** The URI used to define the ADQL name space.
     *  This needs to be consistent with other sites.
     */
    public  static String AdqlURI      = "http://www.ivoa.net/xml/ADQL/v0.7.4";
    
    /** TheURI used to define the VOTable name space.
     *  This needs to be consistent with other sites.
     */
    public  static String VOTableURI   = "http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd";
    
    /** The class used to initiate JDBC connections. This typically
     *  depends upon the underlying database system.
     *  The driver for some common databases:
     *     Adabas D       (de.sag.jdbc.adabasd.ADriver)
     *     FrontBase      (jdbc.FrontBase.FBJDriver)
     *     i-net Opta 7.0 (com.inet.tds.TdsDriver)
     *     i-net Oracle   (com.inet.ora.OraDriver)
     *     i-net Sytraks  (com.inet.syb.SybDriver)
     *     IBM DB2        (COM.ibm.db2.jdbc.net.DB2Driver)
     *     JSQLConnect    (com.jnetdirect.jsql.JSQLDriver)
     *     Mckoi          (com.mckoi.JDBCDriver)
     *     MS SQL Server 2000 (com.microsoft.jdbc.sqlserver.SQLServerDriver)
     *     MySQL          (org.gjt.mm.mysql.Driver)
     *     OpenBase       (com.openbase.jdbc.ObDriver)
     *     Oracle 8i      (oracle.jdbc.driver.OracleDriver)
     *     Oracle 9i      (oracle.jdbc.OracleDriver)
     *     PostgreSQL     (org.postgresql.Driver)
     *     Sybase         (com.sybase.jdbc2.jdbc.SybDriver)
     * 
     *  The appropriate JAR file containing the driver and other
     *  database dependent classes needs to be in the CLASSPATH set in the sn.pl command.
     *     
     */
    public  static String JDBCDriver   = "org.gjt.mm.mysql.Driver";
    
    /** The JDBC url used to connect to the appropriate database.  The
     *  detailed syntax of the URL depends upon the database system.
     *       Oracle:    jdbc:oracle:thin:@<SERVERNAME>:<PORT>:<DBNAME>
     *       Sybase:    jdbc:sybase:Tds:<SERVERNAME>:<PORT>/<DBNAME>
     *       SQL Server jdbc:microsoft:sqlserver://<SERVERNAME>:<PORT>
     *       MySQL 	jdbc:mysql://<SERVERNAME>:<PORT>/<DBNAME>
     *       Postgres   jdbc:postgresql://<SERVERNAME>:<PORT>/<DBNAME>
     * 
     *   Other syntaxes may also be supported.  Here the SERVERNAME is the
     *   host on which the Service is run.  PORT is the port at which the
     *   database listens for requests, and DBNAME is the name of the 'database'
     *   (i.e., a group of tables) in the installed system to be queried.
     */
    public  static String jdbcURL       = "";
    
    /** The database account name used in logging onto the system.
     *  Generally an account with read-only privileges on
     *  non-temporary tables should be used for services that
     *  support web access.
     */
    public  static String jdbcName      = "";
    
    /** The database account password used in logging on the system. */
    public  static String jdbcPwd       = "";
    
    /** The table named used when creating a temporary table.
     *  It is presumed that one can append numbers to this
     *  to get other temporary tables.
     */
    public  static String TempTableName = "#tempname";
    
    /** The target that identifies this SkyNode in a ExecPlan.
     *  If you wish to support multiple SkyNodes, they must
     *  have distinguished TargetNodes, so that you should
     *  override this default.
     */
    public static String TargetNode     = "";
    
    /** The name of the RA column used in XMATCH and REGION queries.
     */
    public static String  raName        = "ra";
    
    /** The name of the Dec column used in XMATCH and REGION queries. */
    public static String  decName       = "dec";
    
    /** The name of the table[s] that is[are] to be queried in this SkyNode. */
    public static String[] TableName    = {""};
    public static String[] TableDesc    = {""};
    public static int[]    TableSize    = {-1};
    
    
    /** The table weight is the inverse of the positional
     *  uncertainties expressed in radians.  It is used in calculating
     *  the weighted unit vector sums in XMATCH.  For the XMATCH algorithm
     *  this should be a realistic value based upon the known positional
     *  errors, or intrinsic sizes of sources in teh catalog.
     */
    public static double      PositUncert  = 1000;  // arcseconds
    public static double      TableWeight  = Math.pow(Math.toRadians(PositUncert/3600), -2);
    
    
    
    /** The following methods update the default value using the Settings file.
     *  Any of the fields specified above can be overriden using the vo.settings.file.
     *  Enter lines of the form 
     *    posituncert=1000
     *  Note that the keyword is case insensitive.  See Settings.java for further
     *  information.
     */
    public static void update() {
	
	System.err.println("Updating:");
	Field[] fields = SkyNode.class.getFields();
	for (Field f: fields) {
	    
	    String name = f.getName();
	    Class type  = f.getType();
	    
	    System.err.println("Field:"+name+" "+Settings.has(name));
	    
	    if (Settings.has(name)) {
		System.err.println("Val is supposed to be:"+Settings.get(name));
		try {
		    if (type == String.class) {
		        updateString(f, name);
		    } else if (type == int.class) {
		        updateInt(f, name);
		    } else if (type == double.class) {
		        updateDouble(f, name);
		    } else if(type == String[].class) {
		        updateStrArr(f, name);
		    } else if (type == int[].class) {
		        updateIntArr(f, name);
		    } else if (type == double[].class) {
		        updateDoubleArr(f, name);
		    } else {
		        System.err.println("Attempt to update field "+name+" not of supported update type.");
		    }
		} catch (Exception e) {
		    System.err.println("Error updating field:"+name+" Exception:"+e);
		}
	    }
	}
    }
    
    private static void updateString(Field f, String name) throws IllegalAccessException {
	f.set(null, Settings.get(name));
    }
    
    private static void updateInt(Field f, String name) throws IllegalAccessException {
	try {
	    int tst = Integer.parseInt(Settings.get(name));
	    f.set(null, new Integer(tst));
	} catch (Exception e) {
	    System.err.println("Attempt to update "+name+" with invalid integer:"+Settings.get(name));
	}
    }
    
    private static void updateDouble(Field f, String name) throws IllegalAccessException {
	try {
	    double tst = Double.parseDouble(Settings.get(name));
	    f.set(null, new Double(tst));
	} catch (Exception e) {
	    System.err.println("Attempt to update "+name+" with invalid double:"+Settings.get(name));
	}
    }
	
    private static void updateStrArr(Field f, String name) throws IllegalAccessException {
	f.set(null, Settings.getArray(name));
    }
    
    private static void updateIntArr(Field f, String name) throws IllegalAccessException {
	
	String[] str = Settings.getArray(name);
	int[]    tst = new int[str.length];
	
	try {
	    for (int i=0; i<str.length; i += 1) {
		tst[i] = Integer.parseInt(str[i]);
	    }
	    f.set(null, tst);
	} catch (Exception e) {
	    System.err.println("Error parsing update of int array for value "+name+": "+Settings.get(name));
	}
    }
	
    private static void updateDoubleArr(Field f, String name) throws IllegalAccessException {
	
	String[] str = Settings.getArray(name);
	double[] tst = new double[str.length];
	
	try {
	    for (int i=0; i<str.length; i += 1) {
		tst[i] = Double.parseDouble(str[i]);
	    }
	    f.set(null, tst);
	} catch (Exception e) {
	    System.err.println("Error parsing update of int array for value "+name+": "+Settings.get(name));
	}
    }
}
