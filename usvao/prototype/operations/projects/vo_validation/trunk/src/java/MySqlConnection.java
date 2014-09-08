import java.sql.*;

/**
 *   This code is a wrapper for the java Connection object. 
 *   The MySQLConnection object sets up the appropriate connection
 *   to the MySQL database and subsequently the "validation" db.
 * 
 */


public class MySqlConnection
{
    protected Connection con;
    
    public MySqlConnection()
    {
	
	con = null;
	
        try 
            {
                String db  = System.getProperty("database");
                if (db !=  null)
		    {
			//usr/local/mysql-connector-java-5.1.1/mysql-connector-java-5.1.6-bin.jar           
			String url  = "jdbc:mysql://asddb.gsfc.nasa.gov/" + db;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(url,"webuser","Wu$Tempuser");
		    } 
            }   
        catch (Exception e)
            {
                System.out.println("MySQLConnection: Could not connect to DB " + e);
            }   
    }
    public Connection getConnection()
    {
	return con;
    }
}
