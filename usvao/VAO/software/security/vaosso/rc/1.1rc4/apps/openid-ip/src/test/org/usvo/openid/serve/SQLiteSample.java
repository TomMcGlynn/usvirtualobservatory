import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteSample {

  public static void main(String[] args) throws ClassNotFoundException {

    // load the sqlite-JDBC driver using the current class loader
    Class.forName("org.sqlite.JDBC");

    Connection connection = null;
    try {
      // create a database connection
      connection = DriverManager.getConnection("jdbc:sqlite:etc/test/test.db");
      Statement statement = connection.createStatement();
      statement.setQueryTimeout(300);  // set timeout to 5 minutes

      ResultSet rs = statement.executeQuery("select * from user_session");
      while(rs.next()) {
        // read the result set
        System.out.print("id = " + rs.getInt("id"));
        System.out.println("\tcreate = " + rs.getTimestamp("create_time"));
      }
    }
    catch(SQLException e) {
      // if the error message is "out of memory", 
      // it probably means no database file is found
      System.err.println(e.getMessage());
    }
    finally {
      try {
        if(connection != null)
          connection.close();
      }
      catch(SQLException e) {
        // connection close failed.
        System.err.println(e);
      }
    }
  }
}
