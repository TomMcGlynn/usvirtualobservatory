import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;


public interface DBTable 
{	
    public void writeRow(List types, List columns, String name) throws Exception;
    public ArrayList readRow();
   

}
