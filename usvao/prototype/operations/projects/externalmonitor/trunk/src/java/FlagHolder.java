import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;


public class FlagHolder
{
    
    private static String s_flag;
    private static String s_specialid;
    private static String s_retest = "S";
    
    public static void storeflag(String flag, String specialid)
    {       	
	s_flag = flag;
	s_specialid = specialid;
	
       //	System.out.println("Your flag is:" + s_flag +  s_specialid );
    }
    public static void storeflag(String flag, String specialid,String retest)
    {
	System.out.println("You are in the special retest routine");
	s_flag = flag;
	s_specialid  =  specialid;
	s_retest     =  retest; 
    }
    public static String getflag()
    {
	return s_flag;
    }
    public static String getspecialid()
    {
	return s_specialid;
    }
    public static String getretest()
    {
        return s_retest;
    }
}
