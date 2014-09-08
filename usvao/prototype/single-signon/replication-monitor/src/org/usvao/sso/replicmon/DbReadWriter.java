package org.usvao.sso.replicmon;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Simply write a value to a database or read one out. */
public class DbReadWriter {
    private static final Logger log = Logger.getLogger(DbReadWriter.class);

    private DbServerConfig config;
    private Connection conn;

    public DbReadWriter(Connection conn, DbServerConfig config) {
        this.conn = conn;
        this.config = config;
    }

    /** What is the value of the column?  Retrieves the first result if <tt>where</tt> matches several.
     *  Null if no matches.
     *  @param where the query's where clause */
    public Integer readInt(String where) throws SQLException {
        String testColumnName = config.getUberConfig().getTestColumn();
        Statement s = conn.createStatement();
        String query = "select " + testColumnName
                + " from " + config.getUberConfig().getTestTable() + " where " + where;
        try {
            log.debug(query);
            ResultSet rs = s.executeQuery(query);
            if (rs.next()) {
                int result = rs.getInt(testColumnName);
                log.trace("result: " + testColumnName + "=" + result);
                return result;
            }
            else
                return null;
        } finally {
            s.close();
        }
    }

    public void insertInt(String filterColumnName, Object filterColumnValue, int value) throws SQLException {
        execute("insert into " + config.getUberConfig().getTestTable()
                    + " (" + filterColumnName + ", " + config.getUberConfig().getTestColumn() + ")"
                    + " values (" + DbReadWriter.quoteChar(filterColumnValue) + ", " + value + ")");
        updateDate(where(filterColumnName, value));
    }

    public void updateInt(int value, String where) throws SQLException {
        execute("update " + config.getUberConfig().getTestTable()
                + " set " + config.getUberConfig().getTestColumn() + "=" + value + " where " + where);
    }

    public void updateDate(String where) throws SQLException {
        execute("update " + config.getUberConfig().getTestTable()
                + " set " + config.getUberConfig().getDateColumn() + " = now()");
    }

    public void execute(String command) throws SQLException {
        Statement s = conn.createStatement();
        try {
            s.execute(command);
        } finally {
            s.close();
        }
    }

    /** This readerWriter's connection to its database. */
    public Connection getConn() { return conn; }

    /** The readerWriter's configuration. */
    public DbServerConfig getConfig() { return config; }

    // Static helper functions
    public static String where(String columnName, Object value) { return columnName + "=" + quoteChar(value); }
    public static String quoteChar(Object value) { return getQuote(value) + value + getQuote(value); }
    public static String getQuote(Object value) {
        if (value instanceof Number) return "";
        else if (value instanceof String) return "'";
        else throw new UnsupportedOperationException
                    ("Unsupported value type: " + value.getClass() + " (" + value + ")");
    }

    public String toDebugString() {
        String connIsValid;
        try {
            connIsValid = conn.isValid(1) ? "valid" : "not valid";
        } catch (SQLException e) {
            log.trace(e);
            connIsValid = "timed out";
        }
        return "DbReadWriter{" +
                "config=" + config +
                ", connection valid=" + connIsValid +
                '}';
    }
}
