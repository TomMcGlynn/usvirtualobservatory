package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.DatabaseAccessException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EmailQuery {
    public static boolean emailExists(String email) throws DatabaseAccessException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getDBConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select " + DatabaseConstants.USER_COL_ID
                    + " from " + DatabaseConstants.USER_TABLE_NAME
                    +" where " + DatabaseConstants.USER_COL_EMAIL + "='" + DatabaseManager.sanitize(email.trim()) + "'");
            return rs.next();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Unable to query for email \"" + email + "\": " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    DatabaseManager.returnDBConnection(conn);
                } catch (DatabaseAccessException ignored) { }
            }
        }
    }
}
