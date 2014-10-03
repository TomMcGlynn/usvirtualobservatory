/*****************************************************************************
 * Copyright (c) 2011, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapserver.db.oracle;

import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.util.Properties;
import java.util.Vector;
import java.util.Arrays;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.apache.log4j.Logger;

import oracle.jdbc.*;
import oracle.jdbc.pool.OracleDataSource;

/**
 * simple access to the Oracle database used for testing the JDBC connection
 */
public class TestClient {

    static Properties tapprops = null;
    static Class defaultDriver = null;
    static Logger logger = null;

    private Connection conn = null;

    /**
     * load the drivers and create a connection
     */
    public TestClient(String dbname, String host, String user, String pw) 
        throws SQLException 
    {
        this(user+"/"+pw+"@//"+host+"/"+dbname);
    }

    public TestClient(String jdbcPath) throws SQLException {
        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:thin:"+jdbcPath);
        conn = ods.getConnection();
    }

    public TestClient() throws IOException, SQLException {
        ensureLoadedDriver();
        OracleDataSource ods = new OracleDataSource();
        ods.setURL(tapprops.getProperty("database.URL"));
        conn = ods.getConnection();
    }

    static void ensureLoadedDriver() throws IOException {
        if (tapprops == null) loadDriver();
    }

    public static void loadDriver() throws IOException {
        logger = Logger.getLogger(TestClient.class);

        tapprops = new Properties();
        tapprops.load(ClassLoader.getSystemResourceAsStream("tapwebservice.properties"));
        String driver = tapprops.getProperty("database.Driver");
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                logger.error("Configured Database Driver not found: " + driver);
            }
        }
    }

    /**
     * return the version of the Oracle driver
     */
    public String getVersion() throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        return meta.getDriverVersion();
    }

    /**
     * print to standard out the version of the Oracle driver
     */
    public void printVersion() throws SQLException, IOException {
        System.out.println("JDBC Driver version: " + getVersion());
    }

    /**
     * run a test query to test the database connection, returning true if it 
     * successful.
     * @param testsql   An SQL string that returns at least one row.
     * @return boolean  true if the query was successfully executed and returned at 
     *                    least one row.  
     */
    public boolean testQuery(String testsql) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(testsql);
            return rs.next();
        }
        catch (Exception ex) {
            return false;
        }
        finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) { }
        }
    }

    public boolean testQuery() throws IOException {
        ensureLoadedDriver();
        String query = tapprops.getProperty("data.testquery");
        if (query == null) 
            query = "select schema_name from information_schema.schemata;";
        return testQuery(query);
    }

    protected void finalize() throws SQLException {
        conn.close();
    }

    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }

    /**
     * write the results from a query to an output stream.
     * @param rs        the results as a ResultSet
     * @param wr        the output stream to write to as a PrintWriter
     * @param rowcount  the max number of rows to print; if negative, 
     *                    print all available rows. 
     * @return int  the number of rows actually printed.
     */
    public int printResults(ResultSet rs, PrintWriter wr, int rowcount) 
        throws SQLException, IOException
    {
        ResultSetMetaData rmd = rs.getMetaData();
        int ncol = rmd.getColumnCount();
        int i;

        String[] labels = new String[ncol];
        for(i=0; i < labels.length; i++) 
            labels[i] = rmd.getColumnLabel(i+1);

        int[] widths = new int[ncol];
        Arrays.fill(widths, 4);
        Vector<String[]> rows = 
            new Vector<String[]>((rowcount > 0) ? rowcount : 10);

        int r=0;
        String[] cols =  null;
        while ((r++ < rowcount || rowcount <= 0) && rs.next()) {
            cols = new String[ncol];
            for(i=0; i < ncol; i++) {
                cols[i] = rs.getString(i+1);
                if (cols[i] != null && cols[i].length() > widths[i]) 
                    widths[i] = cols[i].length();
            }
            rows.add(cols);
        }
        if (r == rowcount) rs.previous();

        for(i=0; i < widths.length; i++) 
            if (widths[i] < labels[i].length()) widths[i] = labels[i].length();

        printTable(labels, rows, widths, wr);
        return rows.size();
    }

    protected void printTable(String[] labels, Vector<String[]> rows,
                              int[] widths, PrintWriter wr) 
    {
        int i;
        char[] buf = null;
        if (labels.length != widths.length)
            throw new IllegalArgumentException("printRowHeader(): lengths of " + 
                                               "label and width arrays not equal");

        // top line
        StringBuffer lb = new StringBuffer("+");
        for(i=0; i < widths.length; i++) {
            buf = new char[widths[i]+2];
            Arrays.fill(buf, '-');
            lb.append(buf).append('+');
        }
        String line = lb.toString();
        wr.println(line);

        // labels
        printRow(labels, widths, wr);

        // dividing line
        wr.println(line);
        wr.flush();

        // rows
        for(String[] row : rows) {
            printRow(row, widths, wr);
            wr.flush();
        }

        // dividing line
        wr.println(line);
        wr.printf("(%d rows)\n", rows.size());
        wr.flush();
    }

    protected void printRow(String[] items, int[] widths, PrintWriter wr) {
        wr.print('|');
        for(int i=0; i < items.length; i++) 
            wr.printf(" %-"+widths[i]+"s |", items[i]);
        wr.println();
    }

    /**
     * write the results from a query to an output stream.
     * @param rs        the results as a ResultSet
     * @param s         the output stream to write to as a Writer
     * @param rowcount  the max number of rows to print; if negative, 
     *                    print all available rows. 
     * @return int  the number of rows actually printed.
     */
    public int printResults(ResultSet rs, OutputStream s, int rowcount) 
        throws SQLException, IOException
    {
        return printResults(rs, new PrintWriter(new OutputStreamWriter(s)), 
                            rowcount);
    }

    /**
     * interactively read and execute SQL commands, printing out the results
     */
    public void runShell() throws IOException, SQLException {
        runShell(System.in, System.out, System.err);
    }

    /**
     * interactively read and execute SQL commands, printing out the results
     */
    public void runShell(InputStream in, OutputStream out, OutputStream err) 
        throws IOException, SQLException
    {
        runShell(in, new PrintStream(out), new PrintStream(err));
    }

    /**
     * interactively read and execute SQL commands, printing out the results
     */
    public void runShell(InputStream in, PrintStream out, PrintStream err) 
        throws IOException, SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;
        BufferedReader inw = new BufferedReader(new InputStreamReader(in));

        try {
            stmt = conn.createStatement();

            printHello(err);
            printPrompt(err);

            String line = null;
            while((line = inw.readLine()) != null) {
                line = line.trim();
                out.println(line);  out.flush();

                if (line.length() == 0) {
                    printPrompt(err);
                    continue;
                }
                if ("quit".startsWith(line)) 
                    break;

                if (line.charAt(line.length()-1) == ';')
                    line = line.substring(0,line.length()-1);

                try {
                    if (stmt.execute(line)) {
                        rs = stmt.getResultSet();
                        printResults(rs, out, -1);
                    }
                    else
                        printUpdateCount(stmt.getUpdateCount(), out);
                }
                catch (Exception ex) {
                    err.println("!! " + ex.getMessage());
                    ex.printStackTrace();
                }
                printPrompt(err);
            }
        } catch (IOException ex) {
            err.println("Trouble communicating with user");
            ex.printStackTrace();
            throw ex;
        } catch (SQLException ex) {
            err.println("Failed to create JDBC Statement: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) { }
        }
        
    }

    protected void printUpdateCount(int count, PrintStream out) {
        out.print("(completed ");
        out.print(count);
        out.println(" updates)");
    }

    protected void printHello(PrintStream out) {
        out.println();
        out.println("TestClient:");
        out.println("Enter complete SQL statements at the prompt.");
        out.println("Enter \"q\" or \"quit\" to exit.");
        out.println();
    }
    protected void printPrompt(PrintStream out) {
        out.print("> ");
        out.flush();
    }

    public static void printHelp(PrintStream out) {
        out.println("Usage: TestClient help|<jdbcpath> " +
                    "[help|version|query|shell] [querystr]");
    }

    /**
     * run this client from the command line
     */
    public static void main(String[] args) {
        if (args.length < 1 || "help".startsWith(args[0])) {
            printHelp(System.err);
            System.exit(0);
        }

        try {
            TestClient client = new TestClient(args[0]);

            if (args.length < 2 || "shell".startsWith(args[1])) {
                client.runShell();
                System.exit(0);
            }

            if ("version".startsWith(args[1]))
                client.printVersion();
            else if ("query".startsWith(args[1])) {
                if (args.length < 3) {
                    System.err.println("TestClient: missing query statement");
                    System.exit(1);
                }

                StringBuffer query = new StringBuffer();
                for(int i=2; i < args.length; i++) {
                    if (i > 2) query.append(' ');
                    query.append(args[i]);
                }

                Statement stmt = client.createStatement();
                ResultSet rs = stmt.executeQuery(query.toString());
                client.printResults(rs, System.out, -1);
            }
        }
        catch (Exception ex) {
            System.err.println("TestClient: Failed completing request: " + 
                               ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}