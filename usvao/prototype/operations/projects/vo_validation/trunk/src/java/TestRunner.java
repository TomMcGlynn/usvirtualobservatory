import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;


public interface TestRunner
{
    public void test(ArrayList array, int runid, String date, String procstatus) throws IOException, WrongInputException;  
}
