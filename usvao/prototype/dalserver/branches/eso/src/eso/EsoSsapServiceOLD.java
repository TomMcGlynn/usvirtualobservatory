
/*
 * EsoSsapServiceOLD.java
 * $ID*
 */

/**
 *
 * @author M.Sierra
 */

package eso;

import dalserver.*;
import cds.savot.model.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.ArrayList;

/**
 * <p>ForsSsapService implements the queryData operation for GOODS/FORS2 data.
 *
 * <p> This class uses DALServer package to build a SSAP service. In this service
 * we issue a query to a local database, mysql table, and  and convert the metadata
 * returned into the SSAP query response.
 *
 */
public class EsoSsapServiceOLD extends SsapService {
    /**
     * The service name, used to identify resources associated with this
     * service instance.
     */
    private String serviceName = "EsoSsap";
    
    //Base URL for dataset retrieval.
    //private static final String dataUrl = EsoSsapServlet.dataAddr+":50080"+"/cgi-bin/retrieveSpectrum?id=${filename}" ;
    //private static final String dataUrl = "
    private static final String newDataUrl = "http://vops1.hq.eso.org/cgi-bin/retrieveSpectrum?id=";
    //Base URL for spectral display
    private static final String displayUrl = "http://vops2.hq.eso.org/cgi-bin/displaySpectrum?target=${filename}";
    
    //Base URL for the mysql connetion
    private static final String mysqlUrl = "jdbc:mysql://vops1.hq.eso.org/exercise_eso";
    String filename = "/lib/propierties.txt";
    
    /**
     * Create a new local service instance.
     *
     * @param params	Service parameter set.
     */
    public EsoSsapServiceOLD(SsapParamSet params) {
        super(params);
    }
    
    
    
    // ------------- Service Operations ----------------
    
    /**
     * Process a data query and generate a list of candidate spectral
     * datasets matching the query parameters.
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
    throws DalServerException {
        
        // Form the query. Takes parameters, POS, SIZE, BAND, TIME and FORMAT
        // to make a sql query to a local database
        
        // Execute the query, read the query response (which comes back as
        // structured XML in this case) and use a SAX XML parser to convert
        // it into a simple keyword table indexed by UTYPE to permit simple
        // keyword-based data model lookups.
        //
        
        
        //read lines from propierties file
        
        
        this.computeQuery(params, response);
        
        // Build the SSAP request response from the metadata provided by
        // the service.
    }
    
    
    
// ------------- Internal Methods ----------------
    
    /**
     * Build an spectrum services query.
     *
     *
     * @param	params	The processed SSAP request parameter set.
     */
    
    
    
    private void computeQuery(SsapParamSet params, RequestResponse response)
    throws DalServerException {
        
        
        //Construct an empty VOTable and define fields to be filled in from the response table
        //Execute the query conecting to mysql table previously created with MEX
        
        SsapKeywordFactory ssap = new SsapKeywordFactory(response);
        RequestResponse r = response;
        Param p = null;
        String id, key;
        LinkSet lsa = new LinkSet();
        LinkSet lsd = new LinkSet();
        
        r.setDescription("DALServer FORS SSAP service");
        r.setType("results");
        r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
        //Echo the query parameters as INFOs in the query response.
        for (Object o : params.entrySet()) {
            Map.Entry<String,Param> keyVal = (Map.Entry<String,Param>)o;
            Param param = keyVal.getValue();
            if (!param.isSet() || param.getLevel() == ParamLevel.SERVICE)
                continue;
            r.addInfo(id=param.getName(), new TableInfo(id, param.stringValue()));
        }
        
        
        // Create the table metadata for a standard SSAP query response.
        // Any fields which have a constant value for the table may be
        // output as PARAMs.
        
        // Query Metadata
        r.addGroup(ssap.newGroup("Query"));
        r.addField(ssap.newField("Score"));
        
        
        // User-defined FIELDS:
        // To define a new field or parameter: id, name, datatype, ucd and utype are required to be able to create the VOTable;
        // the params go to TableField.java:
        // newTableField(String name, String id, String gid, String datatype,
        // String size, String unit, String utype, String ucd, String description)
        // r.addField(new TableField("filename",
        //        "filename", "Access", "char", "*" , null , "Access.Filename", "meta.name", "Filename"));
        
        r.addField(new TableField("filename",
                "filename", null, "char", "*" , null ,"Filename", "meta.name", "Filename"));
        
        //add user-defined global FIELD. We add a new field with the url to display the spectrum launching specview applet.
        //        r.addField(new TableField("DisplayRef",
        //               "DisplayRef", "Access", "char", "*" , null , null, null, "URL used to display dataset"));
        
        TableField tfd = new TableField("DisplayRef",
                "DisplayRef", null, "char", "*", null, "Access.Display", "meta.display.url", "URL used to display dataset");
        SavotLink sld = new SavotLink();
        //sld.setHref("http://vops2.hq.eso.org/cgi-bin/displaySpectrum?target=${filename}");
        sld.setHref(displayUrl);
        lsd.addItem(sld);
        tfd.setLinks(lsd);
        r.addField(tfd);
        
        
        //Access Metadata
        
        r.addGroup(ssap.newGroup("Access"));
        //r.addField(ssap.newField("AcRef"));
        
        // user-defined FIELD to access the data from a link
        r.addField(new TableField("AccessRef",
                "AccessRef", null, "char", "*", null, "UserAccess.Reference", "meta.display.url", "URL used to display dataset"));
        
//         TableField tfa = new TableField("AccessRef",
//            "AccessRef", null, "char", "*", null, "UserAccess.Reference", "meta.display.url", "URL used to display dataset");
//         SavotLink sla = new SavotLink();
//         sla.setHref(newDataUrl);
//         lsa.addItem(sla);
//         tfa.setLinks(lsa);
//         r.addField(tfa);
        
        
        //r.addField(ssap.newField("Format"));
        r.addParam(ssap.newParam("Format","application/fits"));
        // General Dataset Metadata
        r.addGroup(ssap.newGroup("Dataset"));
        //r.addField(ssap.newField("DataModel"));
        r.addField(ssap.newField("DataLength"));
        r.addParam(ssap.newParam("DataModel", "FORS2 Spectrum"));
        r.addParam(ssap.newParam("DatasetType", "Spectrum"));
        
        // Dataset Identification Metadata
        r.addGroup(ssap.newGroup("DataID"));
        r.addField(ssap.newField("Title"));
        r.addField(ssap.newField("Creator"));
        r.addField(ssap.newField("Collection"));
        r.addParam(ssap.newParam("CreatorDate","24 Dec 2005"));
        r.addParam(ssap.newParam("CreatorVersion","2.0"));
        r.addField(ssap.newField("Instrument"));
        //r.addField(ssap.newField("DataSource"));
        r.addParam(ssap.newParam("DataSource","Survey"));
        //r.addField(ssap.newField("CreationType"));
        r.addParam(ssap.newParam("CreationType","Archival"));
        
        // Curation Metadata
        r.addGroup(ssap.newGroup("Curation"));
        //r.addField(ssap.newField("Publisher"));
        r.addParam(ssap.newParam("Publisher","ESO/VOS"));
        //r.addField(ssap.newField("PublisherDate"));
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
        
        //add two new FIELDS for the coordinates, RA and DEC
//        r.addField(new TableField("Ra",
//                "Ra", "Char.SpatialAxis", "float", null ,"deg", "Char.SpatialAxis.Coverage.Ra", "pos.eq.ra", "Right Ascension"));
//        r.addField(new TableField("Dec",
//                "Dec", "Char.SpatialAxis", "float", null,"deg", "Char.SpatialAxis.Coverage.Dec", "pos.eq.dec", "Declination"));
        
        //info:aperture angular diameter, deg), width slit
        r.addField(ssap.newField("SpatialExtent"));
        //r.addField(ssap.newField("SpatialArea"));
        r.addField(ssap.newField("SpatialCalibration"));
        //r.addField(ssap.newField("SpatialResolution"));
        
        // Spectral Axis Characterization
        r.addGroup(ssap.newGroup("Char.SpectralAxis"));
        r.addField(ssap.newField("SpectralAxisUcd"));
        r.addField(ssap.newField("SpectralLocation"));
        r.addField(ssap.newField("SpectralExtent"));
        r.addField(ssap.newField("SpectralStart"));
        r.addField(ssap.newField("SpectralStop"));
        //r.addField(ssap.newField("SpectralCalibration"));
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
        r.addField(ssap.newField("FluxAxisUcd"));
        //r.addField(ssap.newField("FluxStatError"));
        //r.addField(ssap.newField("FluxSysError"));
        r.addField(ssap.newField("FluxCalibration"));
        
        
        // try to add a resource for the footprint
        //r.setUtype("dal:footprint.ssa");
        
        
        //Get the parameters from the query
        double ra=0;
        double dec=0;
        double rad=0;
        String format=null;
        double waveQueryMin=0;
        double waveQueryMax=0;
        String queryPosition = null;
        String queryTime = null;
        String queryBand = null;
        String queryFormat = null;
        
        // SPATIAL Coverage. If SIZE is omitted, we use a default value, 0.1deg.
        // to find anything which includes the specified position
        
        if ((p = params.getParam("POS")) != null && p.isSet()) {
            ra = params.getParam("POS").rangeListValue().doubleValue(0);
            dec = params.getParam("POS").rangeListValue().doubleValue(1);
            // Construct the positional query
            queryPosition = " (degrees(ACOS(SIN(RADIANS(Coverage_Location_Sky_Dec)) * SIN(RADIANS("+dec+")) + " +
                    "COS(RADIANS(Coverage_Location_Sky_Dec)) * COS(RADIANS("+dec+")) * COS(RADIANS(Coverage_Location_Sky_RA-"+ra+"))))";
            if(params.getValue("SIZE")!=null) {
                rad = params.getParam("SIZE").doubleValue();
                queryPosition += " <="+rad+") ";
                
            }else {
                rad = 0.1;
                queryPosition += " <="+rad+") ";
            }
        }
        
        // TIME Coverage. If only a single value is specified, find anything
        // which includes the specified time value.
        DateParser dp = new DateParser();
        if ((p = params.getParam("TIME")) != null && p.isSet()) {
            Range rg = p.rangeListValue().getRange(0);
            try {
                double mjdDateFrom = dp.getMJD(rg.dateValue1());
                double mjdDateTo;
                if (rg.stringValue2()==null) {
                    // There was only 1 date value for the range, create an implicit range from the first date only
                    // If the first date is YYYY, the range will be YYYY-01-01:00h00/(YYYY+1)-01-01:00h00
                    // If the first date is YYYY-MM, the range will be YYYY-MM-01:00h00/YYYY-(MM+1)-01:00h00
                    // If the first date is YYYY-MM-DD, the range will be YYYY-MM-DD:00h00/YYYY-MM-(DD+1):00h00
                    String[] ymd = rg.stringValue1().split("-");
                    Calendar cc = Calendar.getInstance();
                    cc.setTime(rg.dateValue1());
                    
                    if (ymd.length==1) {
                        cc.add(Calendar.YEAR, +1);
                    } else if (ymd.length==2) {
                        cc.add(Calendar.MONTH, +1);
                    } else if (ymd.length==3) {
                        cc.add(Calendar.DAY_OF_MONTH, +1);
                    } else {
                        throw new InvalidDateException();
                    }
                    java.util.Date dd = cc.getTime();
                    mjdDateTo = dp.getMJD(dd);
                } else {
                    mjdDateTo = dp.getMJD(rg.dateValue2());
                }
                // Construct temporal query
                queryTime = " (Coverage_Temporal_StartTime >="+mjdDateFrom+" and  Coverage_Temporal_StopTime<="+mjdDateTo+")" ;
                
            }catch (InvalidDateException ex) {
                ex.printStackTrace();
            }
        }
        
        // SPECTRAL Coverage. If only a single value is specified, find anything
        // which includes the specified spectral value. Only numerical wavelengh in
        // meters are considered
        
        if ((p = params.getParam("BAND")) != null && p.isSet()) {
            Range range = p.rangeListValue().getRange(0);
            waveQueryMin = range.doubleValue1();
            String StringwaveQueryMax = ((StringwaveQueryMax = range.stringValue2()) != null
                    ? StringwaveQueryMax : range.stringValue1());
            waveQueryMax = new Double(StringwaveQueryMax).doubleValue();
            //wavelength in DB is in Angstrom so we need to convert the input wavelength from meters to Angstrom to make the query.
            double waveQueryMinNM = (waveQueryMin * 1e10);
            double waveQueryMaxNM= (waveQueryMax* 1e10);
            // Construct spectral query
            queryBand = " not (Coverage_Region_Spectral_Max <= "+waveQueryMinNM+" or Coverage_Region_Spectral_Min >= "+waveQueryMaxNM+")" ;
            
        }
        
        
        // FORMAT Coverage
        // We only have fits files, but not spectrum-compliant data in fits format so only format=native or format=ALL should execute the quey.
        //If format is omitted format=ALL must be assumed so we only execute the sql query in one of the above cases.
        format = params.getValue("FORMAT");
        
        String query = "select * from DataFiles d JOIN Format f on f.DataFiles_id=d.id where isSpectrum=true ";
        
        if( format == null || format.equalsIgnoreCase("all") ||  format.equalsIgnoreCase("native")) {
            
            
            if(queryPosition!=null) {
                query+=" and "+queryPosition;
                if(queryTime!=null) {
                    query+=" and "+queryTime;
                }
                if(queryBand!=null) {
                    query+=" and "+queryBand;
                }
            } else {
                if(queryTime!=null) {
                    query+=" and "+queryTime;
                    if(queryBand!=null) {
                        query+=" and "+queryBand;
                    }
                    
                    
                } else if(queryBand!=null) {
                    query+=" and "+queryBand;
                    
                }
            }
            
            // Print the final query
            System.out.println(query);
            
//            to connect to the new DB
//            public Database(String host,int port,String username, String passwd) {
//                try {
//                    Class.forName("com.sybase.jdbc3.jdbc.SybDriver");
//                } catch (ClassNotFoundException e) {
//                    throw new IllegalAccessError("Can't load Sybase driver:
//                            "+e.getMessage());
//                }
//                String url = "jdbc:sybase:Tds:"+host+":"+port;
//                try {
//                    this.conn = DriverManager.getConnection(url,username,passwd);
//                } catch (SQLException e) {
//                    throw new IllegalArgumentException("can't connect to the database:
//                            "+e.getMessage());
//                }
//                try {
//                    this.stmt = conn.createStatement();
//                } catch (SQLException e) {
//                    throw new IllegalAccessError("Can't access to the database:
//                            "+e.getMessage());
//                }
//
//                // Init count Result to -1
//                this.countResult = -1;
//            }
            
            // Connect to the DB (in our case, exercise_eso) to make the query and fill in the VOTable result
            Connection mysqlConn = null;
            try {
                String userName = "root";
                String password = "";
                
                //String url = "jdbc:mysql://127.0.0.1/exercise_eso";
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                //String mysqlUrl = "jdbc:mysql://vops1.hq.eso.org/exercise_eso";
                mysqlConn = DriverManager.getConnection(mysqlUrl, userName, password);
                System.out.println("MySQL Database connection established");
                Statement queryStatement = mysqlConn.createStatement();
                System.out.println(query);
                ResultSet rs = queryStatement.executeQuery(query);
                
                //Create the table metadata for a standard SSAP query response. Fill in the fields
                while(rs.next()) {
                    
                    r.addRow();
                    
                    r.setValue("filename", rs.getString("filename"));
                    //r.setValue("AcRef",dataUrl + rs.getString("filename"));
                    //r.setValue("AcRef", dataUrl);
                    r.setValue("AccessRef",newDataUrl + rs.getString("filename"));
                    //r.setValue("AccessRef","Retrieve Data");
                    if (rs.getString("DataID_Instrument").equalsIgnoreCase("FORS2") || rs.getString("DataID_Instrument").equalsIgnoreCase("HARPS"))  {
                        r.setValue("DisplayRef","Display Data");
                    } else {
                        r.setValue("DisplayRef", "");
                    }
                    //r.setValue("DisplayRef",displayUrl + rs.getString("filename"));
                    r.setValue("DataLength", rs.getString("naxis1"));
                    r.setValue("Title", rs.getString("filename"));
                    //r.setValue("Title", rs.getString("Curation_Facility"));
                    r.setValue("Creator", rs.getString("Curation_Creator"));
                    r.setValue("Collection", rs.getString("Curation_Collection"));
                    r.setValue("Instrument", rs.getString("Instrument_Name"));
                    r.setValue("Reference", rs.getString("Curation_PaperRef"));
                    r.setValue("TargetName", rs.getString("Target_Name"));
                    r.setValue("SpaceFrameName", rs.getString("radecSys"));
                    r.setValue("SpaceFrameEquinox", rs.getString("equinox"));
                    // Use the slit center coordinates
                    r.setValue("SpatialLocation", rs.getString("Coverage_Location_Sky_RA") + " " + rs.getString("Coverage_Location_Sky_Dec"));
                    //r.setValue("Ra",rs.getString("Coverage_Location_Sky_RA"));
                    //r.setValue("Dec",rs.getString("Coverage_Location_Sky_Dec"));
                    // Use the slit width for the spatial extent, convert to meters from arcsec
                    double sw = new Double(rs.getString("Instrument_Slit_Width"));
                    r.setValue("SpatialExtent", new Double(sw/ 3600));
                    r.setValue("SpatialCalibration", rs.getString("Curation_CalibLevel_Spatial"));
                    r.setValue("SpectralAxisUcd","em.wl");
                    double w1 = new Double(rs.getString("Coverage_Region_Spectral_Min")).doubleValue()*1e-10;
                    double w2 = new Double(rs.getString("Coverage_Region_Spectral_Max")).doubleValue()*1e-10;
                    r.setValue("SpectralStart", w1);
                    r.setValue("SpectralStop", w2);
                    r.setValue("SpectralLocation",new Double((w1 + w2) / 2.0).toString());
                    r.setValue("SpectralExtent",new Double(w2 - w1).toString());
                    r.setValue("TimeExtent",rs.getString("Observation_TotalExposure"));
                    double d1 = new Double(rs.getString("Coverage_Temporal_StartTime")).doubleValue();
                    double d2 = new Double(rs.getString("Coverage_Temporal_StopTime")).doubleValue();
                    r.setValue("TimeStart",d1);
                    r.setValue("TimeStop",d2);
                    r.setValue("TimeLocation",new Double((d1 + d2) / 2.0).toString());
                    r.setValue("TimeCalibration",rs.getString("Curation_CalibLevel_Temporal"));
                    r.setValue("FluxAxisUcd","phot.flux.dens;em.wl");
                    r.setValue("FluxCalibration",rs.getString("Curation_CalibLevel_Flux"));
                    
                }
                
                
                
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            } finally {
                if (mysqlConn != null) {
                    try {
                        mysqlConn.close();
                        System.out.println("MySQL Database connection terminated");
                    } catch (Exception e) { /* ignore close errors */
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
}

