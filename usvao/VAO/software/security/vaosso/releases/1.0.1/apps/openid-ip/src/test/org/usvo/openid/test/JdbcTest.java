package org.usvo.openid.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class JdbcTest {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection c = DriverManager.getConnection
                ("jdbc:mysql://localhost/purseDatabase", "purse", "mystarsandcomets");
        ResultSet rs = c.createStatement().executeQuery("select * from user_table");
        while (rs.next())
            System.out.println(rs.getString("user_name"));
    }
}
