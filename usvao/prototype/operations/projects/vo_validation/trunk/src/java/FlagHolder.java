import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;


public class FlagHolder
{
    
    private static String s_flag;
    private static String s_specialid;

    public static void storeflag(String flag, String specialid)
    {       	
	s_flag = flag;
	s_specialid = specialid;
    }
    public static String getflag()
    {
	return s_flag;
    }
    public static String getspecialid()
    {
	return s_specialid;
    }
}
