import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;


public abstract class Tester
{
    public abstract void test() throws IOException, WrongInputException;  
    public abstract String getStatus();
    public abstract String getError();

}
