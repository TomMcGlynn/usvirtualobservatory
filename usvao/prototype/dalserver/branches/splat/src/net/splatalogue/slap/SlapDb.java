package net.splatalogue.slap;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.Range;
import dalserver.RangeType;
import dalserver.RangeList;
import dalserver.slap.SlapParamSet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * the query interface into the Splatalogue Database.  An instance of 
 * this class is created via the SlapDbFactory.connect() function.
 */
public class SlapDb {

    Connection conn = null;
    static final String selectfrom = "select catname,molformula,moltype,frequency,QNs,recommended from slapview";

    static final double cMmps = 2.99792458e2;

    boolean verbose = false;

    // no public constructor
    SlapDb(Connection c) {
        conn = c;
    }

    /**
     * Disconnect from the remote database.
     */
    public void disconnect() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException ex) { }
        }
    }

    /**
     * submit a query for data to the splatalogue database.  This is done 
     * by converting the input parameters into a database query, submitting 
     * it, and loading the results into the output results object.  
     *
     * This implementation is based on the assumption of a View that has 
     * been defined as follows:
     * 
     * <pre>
     *   create view slapview as 
     *          select l.linelist as catname,
     *                 s.s_name as molformula,
     *                 s.known_ast_molecules as moltype,
     *                 m.orderedfreq as frequency,
     *                 m.resolved_QNs as QNs,
     *                 m.Lovas_NRAO as recommended
     *          from (main m left join linelists l using (ll_id))
     *               left join species using (species_id)
     * </pre>
     *
     * @param params    The SLAP service input parameters.
     * @param response  The request response object.
     */
    public void query(SlapParamSet params, RequestResponse response)
	throws DalServerException
    {
        String sql = makeSqlQuery(params);
        if (verbose) System.err.println("Query: " + sql);

        ResultSet rs = null;
        try {

            // This statement uses ResultSet.TYPE_FORWARD_ONLY and 
            // ResultSet.CONCUR_READ_ONLY
            rs = conn.createStatement().executeQuery(sql);
        }
        catch (SQLException ex) {
            if (! verbose)
                System.err.println("Failed query: " + sql);
            throw new DalServerException("Database query failure: " + 
                                         ex.getMessage());
        }

        try {
            fillResults(rs, response);
        }
        catch (SQLException ex) {
            throw new DalServerException("Database communication failure: " + 
                                         ex.getMessage());
        }
    }

    static enum cols { CATNAME, MOLFORMULA, MOLTYPE, FREQUENCY, 
                       QNS, RECOMMENDED };

    /**
     * fill the results of the query into the SLAP response.  
     * @param rs       the result set returned as a result of the query.
     *                   It is assumed to set at the start of the result set.
     * @param response the SLAP response container to fill
     */
    protected void fillResults(ResultSet rs, RequestResponse response) 
        throws DalServerException, SQLException
    {       

        String[] names = {"catname", "molformula", "moltype", "frequency",
                          "QNs", "recommended", "title", "wavelength" };

        StringBuffer title = null;
        String val = null;
        while (rs.next()) {
            response.addRow();
            int lim = cols.values().length;
            for(int i=0; i < lim; i++) 
                response.setValue(names[i], rs.getString(i+1));

            response.setValue("wavelength", cMmps / rs.getDouble("frequency"));

            // title
            title = new StringBuffer();
            val = rs.getString("catname");
            if (val != null && val.length() > 0) 
                title.append(val).append(": ");
            title.append(rs.getString("molformula")).append(" ");
            title.append(rs.getString("QNs"));
            response.setValue("title", title.toString());
        }
    }

    /**
     * create the SQL query to be sent to the database.  
     * @param params   the SLAP input parameters
     * @throws DalServerException   if the input parameters contains illegal
     *            values.  
     */
    protected String makeSqlQuery(SlapParamSet params) 
        throws DalServerException
    {
        Param p = null;
        RangeList rl = null;
        Iterator<Range> ri = null;
        Range r = null;

        StringBuffer where = new StringBuffer("");
        p = params.getParam("WAVELENGTH");
        if (p != null && p.isSet()) {
            if (where.length() > 0) where.append(" and ");
            rl = p.rangeListValue();
            r = null;
            double freq = 0.0;
            ri=rl.iterator();
            boolean hasarange = ri.hasNext();
            if (hasarange) where.append('(');
            while (ri.hasNext()) {
                r = ri.next();
                if (r.rangeType == RangeType.LOVAL) {
                    freq = cMmps / r.doubleValue1();
                    where.append("frequency <= ");
                    where.append(Double.valueOf(freq).toString());
                }
                else if (r.rangeType == RangeType.HIVAL) {
                    freq = cMmps / r.doubleValue1();
                    where.append("frequency >= ");
                    where.append(Double.valueOf(freq).toString());
                }
                else if (r.rangeType == RangeType.ONEVAL) {
                    freq = cMmps / r.doubleValue1();
                    where.append("frequency = ");
                    where.append(Double.valueOf(freq).toString());
                }
                else {
                    // closed range
                    freq = cMmps / r.doubleValue1();
                    where.append("(frequency <= ");
                    where.append(Double.valueOf(freq).toString());
                    freq = cMmps / r.doubleValue2();
                    where.append(" and frequency >= ");
                    where.append(Double.valueOf(freq).toString()).append(')');
                }
                if (ri.hasNext()) where.append(" or ");
            }
            if (hasarange) where.append(')');
        }

        // component of chemical species 
        p = params.getParam("CHEMICAL_ELEMENT");
        if (p != null && p.isSet()) {
            if (where.length() > 0) where.append(" and ");
            rl = p.rangeListValue();

            if (rl.length() > 1) where.append(" (");
            for (int i=0; i < rl.length(); ++i) {
                if (i > 0) where.append(" and");
                where.append(" molformula like '%");
                where.append(rl.stringValue(i));
                where.append("%'");
            }
            if (rl.length() > 1) where.append(")");
        }

        // make the full SQL query string
        StringBuffer sql = new StringBuffer(selectfrom);
        if (where.length() > 0)
            sql.append(" where ").append(where).append(';');

        return sql.toString();
    }

    /**
     * ensures disconnection from the database
     */
    protected void finalize() throws Throwable {
        if (conn != null) 
            disconnect();
        super.finalize();
    }
}

