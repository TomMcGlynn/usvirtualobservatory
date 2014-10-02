/*
 * EsoSsapService.java
 *
 */

/*
 *
 * @author M.Sierra
 */

package eso;

import java.util.*;
import java.util.GregorianCalendar;
import java.io.*;
import java.sql.*;
import dalserver.*;
import cds.savot.model.*;
import cds.savot.writer.*;
import javax.vecmath.Vector3d;
import org.eso.vos.VOsearch.SSASearch;
import org.eso.vos.intersection.BoundingGeometry;
import org.eso.vos.intersection.Point;
import org.eso.vos.intersection.Polygon;
import org.eso.vos.intersection.Sphere;

/**
 * <p>EsoSsapService implements the queryData operation for ESO spectral data.
 *
 * <p> This class uses DALServer package to build a SSAP service. In this service
 * we issue a query to the ESO database, sybase, and  and convert the metadata
 * returned into the SSAP query response.
 *
 */

public class EsoSsapService extends SsapService
{
    
    
    protected String propFile;
    protected String serviceName;
    protected String dataUrlLink;
    protected String dataUrl;
    protected String displayUrlLink;
    protected String displayUrl;
    protected String dbUrl;
    protected String userName;
    protected String password;
    
    
    
    // ------------- Constructors ----------------
    
    /**
     * Create a new local service instance.
     *
     * @param params	Service parameter set.
     */
    
    
    public EsoSsapService(SsapParamSet params)
    {
        super(params);
        try
        {
            // Read global variables from properties file
            this.propFile=params.getValue("configDir") + "properties.txt";
        }
        catch (DalServerException ex)
        {
            ex.printStackTrace();
        }
        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(new File(propFile)));
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Cannot open file " + "["+propFile+"]");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        this.serviceName = properties.getProperty("serviceName");
        this.dataUrlLink = properties.getProperty("dataUrlLink");
        this.dataUrl = properties.getProperty("dataUrl");
        this.displayUrlLink = properties.getProperty("displayUrlLink");
        this.displayUrl = properties.getProperty("displayUrl");
        this.dbUrl = properties.getProperty("dbUrl");
        this.userName = properties.getProperty("userName");
        this.password = properties.getProperty("password");
    }
    
    
    
    // ------------- Service Operations ----------------
    
    /**
     * Process a data query and generate a list of candidate spectral
     * datasets matching the query parameters,
     *
     * @param   params    The fully processed SSAP parameter set representing
     *                    the request to be processed.
     *
     * @param   response  A dalserver request response object to which the
     *                    query response should be written.  Note this is
     *                    not a file, but an object containing the metadata
     *                    to be returned to the client.
     */
    
    public void queryData(SsapParamSet params, RequestResponse response)
    throws DalServerException
    {
        
        // Form the query. Take parameters, POS, SIZE, BAND, TIME and FORMAT
        // to make a query to the database
        
        // Execute the query, read the query response (which comes back as
        // structured XML in this case) and use a SAX XML parser to convert
        // it into a simple keyword table indexed by UTYPE to permit simple
        // keyword-based data model lookups.
        // Build the SSAP request response from the metadata provided by
        // the service.
        
        this.computeQuery(params, response);
        
        
    }
    
    
    
    // ------------- Internal Methods ----------------
    
    
    /**
     * Build an spectrum services query. Constructs an empty VOTable and build the
     * the SSAP response from the metadata provided by the service.
     *
     *
     * @param	params	The processed SSAP request parameter set.
     *
     * @param   response  A dalserver request response object to which the
     *                    query response should be written.  Note this is
     *                    not a file, but an object containing the metadata
     *                    to be returned to the client.
     *
     */
    
    private void computeQuery(SsapParamSet params, RequestResponse response)
    throws DalServerException
    {
        double EPSILON = 1e-16;
        RequestResponse r = response;
        Param p = null;
        String id, key;
        
        // Create the table metadata for a standard EsoSsap query response (empty VOTable)
        // and define all the possible fields to be returned from the query.
        createVOtable(params,response);
        
        //Inicialize the variables to be used to form the query
        double radius=0.1;
        double[] convex = null;
        String format=null;
        double waveQueryFrom;
        double waveQueryTo;
        double waveQuery;
        double mjdDateQueryFrom;
        double mjdDateQueryTo;
        double mjdDateQuery;
        String queryPosition = null;
        String queryTime = null;
        String queryBand = null;
        String queryFormat = null;
        String isodate = null;
        SSASearch ssa = null;
        long nSide = 32;
        
        
        // Form the query to the database
        // SPATIAL Coverage. Use healpix to index the sky
        Vector3d vectorCone = null;
        Healpix healpix = new Healpix(nSide);
        
        // Non-standard query using VirGO input parameters (polygonal positional serch instead of cone search)
        //Region defined by a convex
        if ((p = params.getParam("convexpolygon")) !=null &&p.isSet())
        {
            
            String [] values = p.stringValue().split("\\,");
            convex = new double[values.length];
            for(int i=0;i<values.length;i++)
                convex[i] = Double.valueOf(values[i]);
            
            ArrayList listToTest = null;
            // List of healpix vectors
            ArrayList<Point> listHealpixVectors = new ArrayList <Point>(convex.length);
            for(int i=0;i<convex.length;i=i+2)
                listHealpixVectors.add(new Point(healpix.Ang2Vec(Math.toRadians(90 - (convex[i+1])), Math.toRadians(convex[i]))));
            
            boolean clockwise = Polygon.isClockwise(
                    new Point(listHealpixVectors.get(0).vector()),
                    new Point(listHealpixVectors.get(1).vector()),
                    new Point(listHealpixVectors.get(2).vector())
                    );
            // We create a Sphere embedding the shape so that
            // we compute the healpix request with disc.
            Sphere sphere = BoundingGeometry.ritterSphere(new Polygon(listHealpixVectors));
            double denom = 0;
            double radiusAngle=0.0;
            
            if(clockwise)
            {
                Polygon polygon = new Polygon(listHealpixVectors);
                Point barycenter = polygon.getBarycenter();
                Vector3d point = polygon.closestPoint(barycenter).vector();
                point.normalize();
                sphere.setCenter(barycenter);
                // We express the radius as an angle and we remove 2 resolution element
                // so that our Healpix pixel stay inside the FOV
                denom = point.length()*barycenter.vector().length();
                if(denom<EPSILON)
                {
                    denom = 0.0;
                    radiusAngle=Math.PI/2.0;
                }
                else
                {
                    radiusAngle= Math.acos(point.dot(sphere.getCenter().vector())/denom);
                }
                
                Point newCenter = new Point(sphere.getCenter().vector());
                newCenter.vector().scale(-1.0);
                sphere.setCenter(newCenter);
                sphere.setRadius(Math.PI-radiusAngle);
                listToTest = healpix.query_disc(healpix.getnSide(),sphere.getCenter().vector(),sphere.getRadius(),0,1);
                
            }
            else
            {
                denom = sphere.getCenter().vector().length()*listHealpixVectors.get(0).vector().length();
                if(denom<EPSILON)
                {
                    denom = 0.0;
                    radiusAngle=Math.PI/2.0;
                }
                else
                {
                    radiusAngle = Math.acos(sphere.getCenter().vector().dot(listHealpixVectors.get(0).vector())/denom);
                }
                sphere.setRadius(radiusAngle);
                listToTest = healpix.query_disc(healpix.getnSide(),sphere.getCenter().vector(),sphere.getRadius(),0,1);
            }
            
            ssa = new SSASearch(listHealpixVectors);
            // Construct the query
            queryPosition = null;
            //queryPosition  = healpix.clauseWhere(listToTest).toString();
        }
        
        // Standard SSAP query. If SIZE is omitted, we use a default value, 0.1deg.
        // to find anything which includes the specified position
        else if ((p = params.getParam("POS")) != null && p.isSet())
        {
            double ra = params.getParam("POS").rangeListValue().doubleValue(0);
            double dec = params.getParam("POS").rangeListValue().doubleValue(1);
            // queryPosition = " (degrees(ACOS(SIN(RADIANS(SpatialAxis_Location_Dec)) * SIN(RADIANS("+dec+")) + " +
            //         "COS(RADIANS(SpatialAxis_Location_Dec)) * COS(RADIANS("+dec+")) * COS(RADIANS(SpatialAxis_Location_Ra-"+ra+"))))";
            if(params.getValue("SIZE")!=null)
            {
                radius = params.getParam("SIZE").doubleValue();
            }
            
            // Index the sky using healpix
            //Compute 3d vector from input coordinates ra and dec
            vectorCone = healpix.ang2Vect(ra,dec);
            ssa = new SSASearch(vectorCone,Math.toRadians(radius));
            // Construct positional query
            queryPosition  = healpix.clauseWhere(healpix.intersectAgainstCone(vectorCone,radius)).toString();
            
        }
        
        // TIME Coverage. Range-list format implemented allowing both values and ranges.
        // Open and closed ranges supported. If only a single value is specified, find anything
        // which includes the specified time value.
        
        DateParser dp = new DateParser();
        if ((p = params.getParam("TIME")) != null && p.isSet())
        {
            StringBuilder queryTimeFinal = new StringBuilder();
            double l = p.rangeListValue().length();
            Calendar cc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            // Check all the values in the list, evaluate if they are single values or ranges and construct the query accordingly
            for (int j=0; j< l; j++)
            {
                Range range = p.rangeListValue().getRange(j);
                System.out.println(range);
                if (j>0)
                {
                    queryTimeFinal.append(" or");
                }
                try
                {
                    switch (range.rangeType)
                    {
                        case ONEVAL:
                            // Create an implicit range for the query:
                            // If the date is YYYY, the range will be YYYY-01-01:00h00/(YYYY+1)-01-01:00h00
                            // If the date is YYYY-MM, the range will be YYYY-MM-01:00h00/YYYY-(MM+1)-01:00h00
                            // If the date is YYYY-MM-DD, the range will be YYYY-MM-DD:00h00/YYYY-MM-(DD+1):00h00
                            mjdDateQueryFrom = dp.getMJD(dp.parse(range.stringValue1()));
                            // Transform the date to obtain the search range and get the corresponding mjd date
                            mjdDateQueryTo = transformDate(range.stringValue1());
                            queryTime = " not (TimeAxis_Bounds_Stop <= "+mjdDateQueryFrom+" or TimeAxis_Bounds_Start >= "+mjdDateQueryTo+")" ;
                            queryTimeFinal.append(queryTime);
                            break;
                        case LOVAL:
                            // Nothing to change here, the query will be from the first instant.
                            mjdDateQueryFrom = dp.getMJD(dp.parse(range.stringValue1()));
                            queryTime = " (TimeAxis_Bounds_Stop >= "+mjdDateQueryFrom+")" ;
                            queryTimeFinal.append(queryTime);
                            break;
                        case HIVAL:
                            // Transform the date to make the query to search all the values until the end of the date requested
                            // If the date is YYYY, the query will cover until the end of that year, transform to (YYYY+1)-01-01:00h00
                            // If the date is YYYY-MM, the query will cover until the end of that month, transform to YYYY-(MM+1)-01:00h00
                            // If the date is YYYY-MM-DD, the query will cover until the end of that day, transform to YYYY-MM-(DD+1):00h00
                            mjdDateQuery = dp.getMJD(dp.parse(range.stringValue1()));
                            // Trasnform the date to obtain the search range and get the corresponding mjd date
                            mjdDateQueryTo = transformDate(range.stringValue1());
                            queryTime =  " (TimeAxis_Bounds_Start <= "+mjdDateQueryTo+")" ;
                            queryTimeFinal.append(queryTime);
                            break;
                        case CLOSED:
                            // Transform the second date to construct the query for searching until the end of second date requested
                            // If the date is YYYY, the query will cover until the end of that year, transform to (YYYY+1)-01-01:00h00
                            // If the date is YYYY-MM, the query will cover until the end of that month, transform to YYYY-(MM+1)-01:00h00
                            // If the date is YYYY-MM-DD, the query will cover until the end of that day, transform to YYYY-MM-(DD+1):00h00
                            mjdDateQueryFrom = dp.getMJD(dp.parse(range.stringValue1()));
                            // Transform date to obtain the search range and get the corresponding mjd date
                            mjdDateQueryTo = transformDate(range.stringValue2());
                            queryTime = " not (TimeAxis_Bounds_Stop <= "+mjdDateQueryFrom+" or TimeAxis_Bounds_Start >= "+mjdDateQueryTo+")" ;
                            queryTimeFinal.append(queryTime);
                            break;
                        case ANY:
                            queryTime = " (TimeAxis_Bounds_Start >= 0.0)" ;
                            queryTimeFinal.append(queryTime);
                            break;
                    }
                }
                catch (InvalidDateException ex)
                {
                    System.err.println(isodate+" is invalid");
                    System.err.println(ex.getMessage());
                }
            }
            queryTime = queryTimeFinal.toString();
        }
        
        
        // SPECTRAL Coverage. Implement range-list format with numerical wavelenght values or ranges.
        // Open and closed ranges supported. If only a single value is specified, find anything
        // which includes the specified spectral value. Only numerical wavelenghs in
        // meters are considered.
        
        if ((p = params.getParam("BAND")) != null && p.isSet())
        {
            // Select all the wavelength range (same result as leaving this parameter empty)
            if (params.getValue("BAND").equalsIgnoreCase("ALL"))
            {
                queryBand = null;
            }
            else
            {
                StringBuilder queryBandFinal = new StringBuilder();
                double l = p.rangeListValue().length();
                // Check all the values in the list, evaluate if they are single values or ranges and construct the query accordingly
                for (int j=0; j< l; j++)
                {
                    Range range = p.rangeListValue().getRange(j);
                    if (j>0)
                    {
                        queryBandFinal.append(" or");
                    }
                    switch (range.rangeType)
                    {
                        case ONEVAL:
                            waveQuery = range.doubleValue1();
                            queryBand = " (SpectralAxis_Bounds_Start <= "+waveQuery+" and SpectralAxis_Bounds_Stop >= "+waveQuery+")" ;
                            queryBandFinal.append(queryBand);
                            break;
                        case LOVAL:
                            waveQueryFrom = range.doubleValue1();
                            queryBand = " (SpectralAxis_Bounds_Stop >= "+waveQueryFrom+")" ;
                            queryBandFinal.append(queryBand);
                            break;
                        case HIVAL:
                            waveQueryTo = range.doubleValue1();
                            queryBand =  " (SpectralAxis_Bounds_Start <= "+waveQueryTo+")" ;
                            queryBandFinal.append(queryBand);
                            break;
                        case CLOSED:
                            waveQueryFrom = range.doubleValue1();
                            waveQueryTo = range.doubleValue2();
                            queryBand = " not (SpectralAxis_Bounds_Stop <= "+waveQueryFrom+" or SpectralAxis_Bounds_Start >= "+waveQueryTo+")" ;
                            queryBandFinal.append(queryBand);
                            break;
                        case ANY:
                            queryBand = " (SpectralAxis_Bounds_Start >= 0.0)" ;
                            queryBandFinal.append(queryBand);
                            break;
                    }
                    System.out.println(queryBandFinal);
                }
                queryBand = queryBandFinal.toString();
                //System.out.println(queryBand);
            }
        }
        
        // FORMAT Coverage
        // We only have fits files, but not spectrum-compliant data in fits format so only format=native or format=ALL should execute the quey.
        // If format is omitted format=ALL must be assumed so we only execute the sql query in one of the above cases.
        format = params.getValue("FORMAT");
        
        String query = "select * from SSA s JOIN SSAhealpix_32 h on s.SSA_id=h.SSA_id where DataSet_Length >1  ";
        if( format == null || format.equalsIgnoreCase("all") ||  format.equalsIgnoreCase("native"))
        {
            
            if(queryPosition!=null)
            {
                query+=" and (" +queryPosition+ ")";
                if(queryTime!=null)
                {
                    query+=" and (" +queryTime+ ")";
                }
                if(queryBand!=null)
                {
                    query+=" and (" +queryBand+ ")";
                }
            }
            else
            {
                if(queryTime!=null)
                {
                    query+=" and (" +queryTime+ ")";
                    if(queryBand!=null)
                    {
                        query+=" and (" +queryBand+ ")";
                    }
                    
                }
                else if(queryBand!=null)
                {
                    query+=" and (" +queryBand+ ")";
                }
            }
            
            // Print the final query
            System.out.println(query);
            
            // Connect to the DB to make the query and fill in the fields VOTable result
            Connection conn = null;
            
            try
            {
                Class.forName("com.sybase.jdbc3.jdbc.SybDriver");
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalAccessError("Can't load Sybase driver: "
                        +e.getMessage());
            }
            try
            {
                conn = DriverManager.getConnection(dbUrl,userName,password);
                System.out.println("Connection to the Database established");
            }
            catch (SQLException e)
            {
                throw new IllegalArgumentException("Can't connect to the Database: "
                        +e.getMessage());
            }
            try
            {
                Statement queryStatement = conn.createStatement();
                ResultSet rs = queryStatement.executeQuery(query);
                
                // Generate the table metadata
                while(rs.next())
                {
                    // No positional query
                    if(vectorCone == null && convex==null)
                    {
                        fillVOtable(r,rs);
                    }
                    
                    else
                    {
                        // Test when a record is in the user request
                        if(ssa.testIntersection(new Point(healpix.ang2Vect(rs.getDouble("SpatialAxis_Location_Ra"), rs.getDouble("SpatialAxis_Location_Dec")))))
                        {
                            // display data
                            fillVOtable(r,rs);
                        }
                    }
                }
                
                // Show the number of table rows in the response header.
                r.addInfo(key="TableRows",
                        new TableInfo(key, new Integer(r.size()).toString()));
                
            }
            catch (SQLException e)
            {
                throw new IllegalAccessError("Can't access to the database: "
                        +e.getMessage());
            }
            finally
            {
                if (conn != null)
                {
                    try
                    {
                        conn.close();
                        System.out.println("Database connection terminated");
                    }
                    catch (Exception e)
                    { /* ignore close errors */
                    }
                }
            }
        }
        
        // Show the number of table rows in the response header.
        r.addInfo(key="TableRows",
                new TableInfo(key, new Integer(r.size()).toString()));
        // Compute a default normalized SCORE heuristic for each matched
        // dataset, placing the result in the named table field.
        r.score((dalserver.ParamSet)params, "Score");
        
        // Sort the result set by SCORE.
        r.sort("Score", -1);
        
    }
    
    
    /**
     * Transform the date in ISO format to mjd date to be used for the query to the database
     * If isodate is YYYY transform it to YYYY+1-01-01:00h00
     * If isodate is YYYY-MM transform it to YYYY-(MM+1)-01:00h0
     * If isodate is YYYY-MM-DD transform it to YYYY-MM-(DD+1):00h00
     *
     * @param isodate date in ISO format
     * @return mjd date in mjd
     *
     * @throws DalServerException
     */
    private double transformDate(String isodate)
    throws DalServerException
    {
        DateParser dp = new DateParser();
        
        try
        {
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            java.util.Date date = dp.parse(isodate);
            calendar.setTime(date);
            String[] ymd = isodate.split("-");
            
            if (ymd.length==1)
            {
                calendar.add(GregorianCalendar.YEAR, +1);
            }
            else if (ymd.length==2)
            {
                calendar.add(GregorianCalendar.MONTH, +1);
            }
            else if (ymd.length==3)
            {
                calendar.add(GregorianCalendar.DAY_OF_MONTH, +1);
            }
            else
            {
                throw new InvalidDateException();
            }
            
            java.util.Date dd = calendar.getTime();
            double mjd = dp.getMJD(dd);
            return mjd;
        }
        catch (InvalidDateException ex)
        {
            throw new DalServerException();
        }
    }
    
    /**
     * Create empty metadata query response, define all the possible fields to be
     *
     * @param params  The fully processed SSAP parameter set representing
     *                the request to be processed.
     *
     * @param response A request response object
     *
     */
    
    private void createVOtable(SsapParamSet params, RequestResponse response)
    throws DalServerException
    {
        
        SsapKeywordFactory ssap = new SsapKeywordFactory(response);
        RequestResponse r = response;
        Param p = null;
        String id, key;
        LinkSet lsa = new LinkSet();
        LinkSet lsd = new LinkSet();
        String queryDate = new java.util.Date().toString();
        r.setDescription("DALServer ESO SSAP service");
        r.setType("results");
        // This indicates the query executed successfully.  If an exception
        // occurs the output we generate here will never be returned
        r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
        // Add the query Date to the query response
        r.addInfo(key="QUERY_TIME",new TableInfo(key, queryDate));
        //Echo the query parameters as INFOs in the query response.
        for (Object o : params.entrySet())
        {
            Map.Entry<String,Param> keyVal = (Map.Entry<String,Param>)o;
            Param param = keyVal.getValue();
            if (!param.isSet() || param.getLevel() == ParamLevel.SERVICE)
                continue;
            r.addInfo(id=param.getName(), new TableInfo(id, param.stringValue()));
        }
        // Any fields which have a constant value for the table may be output as PARAMs.
        
        // Query Metadata
        r.addGroup(ssap.newGroup("Query"));
        r.addField(ssap.newField("Score"));
        
        // Add user-defined FIELDS
        // To define a new field or parameter: id, name, datatype, ucd and utype are required to be able to create the VOTable;
        // newTableField(String name, String id, String gid, String datatype,
        // String size, String unit, String utype, String ucd, String description)
        // If the field or parameter is not a standard field we need to use a different namespace (not ssa) this is implemented when
        // field or parameter are not part of a group, so gid should be set null, then namespace will be set as "saf".
        
        // Add two new FIELDS for the coordinates, RA and DEC
        r.addField(new TableField("Ra",
                "Ra", null, "float", null ,"deg", "Char.SpatialAxis.Coverage.Ra", "pos.eq.ra", "Right Ascension"));
        r.addField(new TableField("Dec",
                "Dec", null, "float", null,"deg", "Char.SpatialAxis.Coverage.Dec", "pos.eq.dec", "Declination"));
        
//        r.addField(new TableField("AccessRef",
//                "AccessRef", null, "char", "*", null, "AccessReference", "meta.accessref.url", "URL used to display dataset"));
        // user-defined FIELD to access the data from a link in Aladin
        TableField tfa = new TableField("AccessRef",
                "AccessRef", null, "char", "*", null, "AccessReference", "meta.accessref.url", "URL used to accesss dataset");
        SavotLink sla = new SavotLink();
        sla.setHref(dataUrlLink);
        lsa.addItem(sla);
        tfa.setLinks(lsa);
        r.addField(tfa);
        
        // Add user-defined global FIELD. We add a new field with the url to display the spectrum launching specview applet.
        TableField tfd = new TableField("DisplayRef",
                "DisplayRef", null, "char", "*", null, "DisplayReference", "meta.display.url", "URL used to display dataset");
        SavotLink sld = new SavotLink();
        sld.setHref(displayUrlLink);
        lsd.addItem(sld);
        tfd.setLinks(lsd);
        r.addField(tfd);
        
        // Add ESO specific information
        r.addField(new TableField("NGAS_Id",
                "NGAS_Id", null, "char", "*" , null, "Curation.NgasID", "meta.id;meta.dataset", "NGAS Identification"));
        r.addField(new TableField("ProgramID",
                "ProgramID", null, "char", "*", null, "Curation.ProgramID", "meta.code;obs.proposal", "ESO Program Identification"));
        r.addField(new TableField("PIName",
                "PIName", null, "char", "*", null, "Curation.Observer", "meta.id.PI", "Principal Investigator"));
        r.addField(new TableField("ProposalTitle",
                "ProposalTitle", null, "char", "*", null, "Curation.ProposalTitle", "meta.curation;obs.proposal", "Proposal Title"));
        r.addField(new TableField("TelescopeName",
                "TelescopeName", null, "char", "*", null, "Curation.Telescope", "meta.id;instr.tel", "Telescope Name"));
        r.addField(new TableField("Grism",
                "Grism", null, "char", "*", null, "Instrument.DispersiveElement", "instr.setup", "Dispersive Element"));
        r.addField(new TableField("Category",
                "Category", null, "char", "*", null, "Observation.Category", "meta.code.class;obs", "Observation Category"));
        r.addField(new TableField("Mode",
                "Mode", null, "char", "*", null, "Observation.Technique", "meta.code.class;instr.setup", "Observation Technique"));
        r.addField(new TableField("Type",
                "Type", null, "char", "*", null, "Observation.Type", "meta.code.class", "Observation/Exposure Type "));
        r.addField(new TableField("OB_Name",
                "OB_Name", null, "char", "*", null, "Observation.OB.Name", "meta.id.obs", "Observation Block Name"));
        r.addField(new TableField("OB_Id",
                "OB_Id", null, "char", "*", null, "Observation.OB.ID", "meta.id", "Observation Block Identification"));
        
        
        // Access Metadata
        r.addGroup(ssap.newGroup("Access"));
        //r.addField(ssap.newField("AcRef"));
        r.addField(ssap.newField("Format"));
        
        // General Dataset Metadata
        r.addGroup(ssap.newGroup("Dataset"));
        r.addField(ssap.newField("DataModel"));
        r.addField(ssap.newField("DataLength"));
        r.addField(ssap.newField("DatasetType"));
        
        // Dataset Identification Metadata
        r.addGroup(ssap.newGroup("DataID"));
        r.addField(ssap.newField("Title"));
        r.addField(ssap.newField("Creator"));
        r.addField(ssap.newField("Collection"));
        r.addField(ssap.newField("CreatorDate"));
        r.addField(ssap.newField("CreatorVersion"));
        r.addField(ssap.newField("Instrument"));
        r.addField(ssap.newField("DataSource"));
        r.addField(ssap.newField("CreationType"));
        
        // Curation Metadata
        r.addGroup(ssap.newGroup("Curation"));
        r.addField(ssap.newField("Publisher"));
        r.addField(ssap.newField("PublisherDate"));
        r.addField(ssap.newField("Reference"));
        
        // Target Metadata
        r.addGroup(ssap.newGroup("Target"));
        r.addField(ssap.newField("TargetName"));
        
        // Derived Metadata
        //r.addGroup(ssap.newGroup("Derived"));
        //r.addField(ssap.newField("DerivedSNR"));
        
        // Coordinate System Metadata
        r.addGroup(ssap.newGroup("CoordSys"));
        r.addField(ssap.newField("SpaceFrameName"));
        r.addField( ssap.newField("SpaceFrameEquinox"));
        
        // Spatial Axis Characterization
        r.addGroup(ssap.newGroup("Char.SpatialAxis"));
        r.addField(ssap.newField("SpatialLocation"));
        //info:aperture angular diameter, deg, width slit
        r.addField(ssap.newField("SpatialExtent"));
        //r.addField(ssap.newField("SpatialArea"));
        r.addField(ssap.newField("SpatialCalibration"));
        //r.addField(ssap.newField("SpatialResolution"));
        
        // Spectral Axis Characterization
        r.addGroup(ssap.newGroup("Char.SpectralAxis"));
        r.addField(ssap.newField("SpectralAxisUcd"));
        r.addField(ssap.newField("SpectralAxisUnit"));
        r.addField(ssap.newField("SpectralLocation"));
        r.addField(ssap.newField("SpectralExtent"));
        r.addField(ssap.newField("SpectralStart"));
        r.addField(ssap.newField("SpectralStop"));
        r.addField(ssap.newField("SpectralCalibration"));
        //r.addField(ssap.newField("SpectralResolution"));
        //r.addField(ssap.newField("SpectralResPower"));
        
        // Time Axis Characterization
        r.addGroup(ssap.newGroup("Char.TimeAxis"));
        r.addField(ssap.newField("TimeLocation"));
        r.addField(ssap.newField("TimeExtent"));
        r.addField(ssap.newField("TimeStart"));
        r.addField(ssap.newField("TimeStop"));
        r.addField(ssap.newField("TimeCalibration"));
        
        // Flux Axis Characterization
        r.addGroup(ssap.newGroup("Char.FluxAxis"));
        r.addField(ssap.newField("FluxAxisUnit"));
        r.addField(ssap.newField("FluxAxisUcd"));
        //r.addField(ssap.newField("FluxStatError"));
        //r.addField(ssap.newField("FluxSysError"));
        r.addField(ssap.newField("FluxCalibration"));
        
    }
    
    /**
     * Build the SSAP request response from the metadata provided by
     * the native service.Add a new row to the Response taken from the database entry
     *
     * @param r request response object
     * @param rs result set from the database query
     *
     * @throws dalserver.DalServerException
     * @throws java.sql.SQLException
     */
    
    private void fillVOtable(RequestResponse r, ResultSet rs)
    throws DalServerException, SQLException
    {
        
        String vot;
        r.addRow();
        
        double ra = new Double(rs.getString("SpatialAxis_Location_Ra")).doubleValue();
        double dec = new Double(rs.getString("SpatialAxis_Location_Dec")).doubleValue();
        r.setValue("Ra",ra);
        r.setValue("Dec",dec);
        
        
//        r.setValue("Ra",rs.getString("SpatialAxis_Location_Ra"));
//        r.setValue("Dec",rs.getString("SpatialAxis_Location_Dec"));
        //r.setValue("AccessRef","Retrieve Data");
        //r.setValue("AcRef", rs.getString("Access_Reference"));
        //r.setValue("AcRef", dataUrl + rs.getString("Filename"));
        r.setValue("AccessRef", dataUrl + rs.getString("NGAS_Id"));
        if (rs.getString("DataID_Instrument").equalsIgnoreCase("FORS2") || rs.getString("DataID_Instrument").equalsIgnoreCase("HARPS"))
        {
            //r.setValue("DisplayRef","Display Data");
            r.setValue("DisplayRef", displayUrl + rs.getString("NGAS_Id"));
        }
        else
        {
            r.setValue("DisplayRef","");
        }
        r.setValue("NGAS_Id", rs.getString("NGAS_Id"));
        
        String newProgId = toWellFormedText(rs.getString("Prog_Id"));
        r.setValue("ProgramID",newProgId);
        r.setValue("PIName",rs.getString("PI_Name"));
        String newProposalTitle = toWellFormedText(rs.getString("prog_title"));
        r.setValue("ProposalTitle",newProposalTitle);
        r.setValue("TelescopeName", rs.getString("telescope_name"));
        r.setValue("Grism",rs.getString("grism"));
        r.setValue("Category", rs.getString("dpr_cat"));
        r.setValue("Mode", rs.getString("dpr_tech"));
        r.setValue("Type",rs.getString("dpr_type"));
        r.setValue("OB_Name",rs.getString("ob_name"));
        r.setValue("OB_Id", rs.getString("ob_id"));
        r.setValue("Format", rs.getString("Access_Format"));
        r.setValue("DataModel", rs.getString("DataSet_DataModel"));
        r.setValue("DatasetType", rs.getString("DataSet_Type"));
        r.setValue("DataLength", rs.getString("DataSet_Length"));
        r.setValue("Title", rs.getString("DataID_Title"));
        r.setValue("Creator", rs.getString("DataID_Creator"));
        r.setValue("Collection", rs.getString("DataID_Collection"));
        r.setValue("CreatorDate",rs.getString("DataID_Date"));
        r.setValue("CreatorVersion",rs.getString("DataID_Version"));
        r.setValue("Instrument", rs.getString("DataID_Instrument"));
        r.setValue("DataSource", rs.getString("DataID_DataSource"));
        r.setValue("CreationType", rs.getString("DataID_CreationType"));
        r.setValue("Publisher",rs.getString("Curation_Publisher"));
        r.setValue("Reference", rs.getString("Curation_Reference"));
        r.setValue("TargetName", rs.getString("Target_Name"));
        r.setValue("SpaceFrameName", rs.getString("CoordSys_SpaceFrame_Name"));
        r.setValue("SpaceFrameEquinox", new Double(rs.getString("CoordSys_SpaceFrame_Equinox")).doubleValue());
        r.setValue("FluxAxisUnit", rs.getString("FluxAxis_Unit"));
        r.setValue("FluxAxisUcd",rs.getString("FluxAxis_UCD"));
        // Use the slit centre coordinates
        r.setValue("SpatialLocation", ra + " " + dec);
        r.setValue("SpatialExtent",rs.getString("SpatialAxis_Bounds_Extent"));
        r.setValue("SpatialCalibration", rs.getString("SpatialAxis_Calibration"));
        r.setValue("SpectralAxisUcd",rs.getString("SpectralAxis_UCD"));
        r.setValue("SpectralAxisUnit", rs.getString("SpectralAxis_Unit"));
        r.setValue("SpectralLocation",rs.getString("SpectralAxis_Location_Value"));
        r.setValue("SpectralExtent",rs.getString("SpectralAxis_Bounds_Extent"));
        r.setValue("SpectralStart", rs.getString("SpectralAxis_Bounds_Start"));
        r.setValue("SpectralStop",rs.getString("SpectralAxis_Bounds_Stop"));
        r.setValue("SpectralCalibration",rs.getString("SpectralAxis_Calibration"));
        r.setValue("TimeLocation", new Double(rs.getString("TimeAxis_Location_Value")).doubleValue());
        r.setValue("TimeExtent",new Double(rs.getString("TimeAxis_Bounds_Extent")).doubleValue());
        r.setValue("TimeStart",new Double(rs.getString("TimeAxis_Bounds_Start")).doubleValue());
        r.setValue("TimeStop",new Double(rs.getString("TimeAxis_Bounds_Stop")).doubleValue());
        r.setValue("TimeCalibration",rs.getString("TimeAxis_Calibration"));
        r.setValue("FluxCalibration",rs.getString("FluxAxis_Calibration"));
        
    }
    /**
     * Check if there are any unreadable characters in the fields that will cause an error when parsing the XML file (VOTable response)
     * @param text string to be checked
     *
     * @return text string corrected
     */
    private static String toWellFormedText(String text)
    {
        if(text == null)
        {
            return "";
        }
        
        boolean hasErrors = false;
        char[] chars = text.toCharArray();
        
        for (int j = 0; j < chars.length; j++)
        {
            char c = chars[j];
            if (c == 0x9 || c == 0xA || c == 0xD || c >= 0x20 && c <= 0xD7FF || c >= 0xE000 && c <= 0xFFFD)
            {
                // character is OK
            }
            else
            {
                //error!
                hasErrors = true;
                chars[j] = '?';
                text = new String(chars);
            }
        }
        
        return text;
    }
}




