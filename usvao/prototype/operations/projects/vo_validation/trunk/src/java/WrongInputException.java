import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class WrongInputException extends Exception
{

    String s; 
    public WrongInputException()
    {
	System.out.println("Error: You must enter the flag: 'pass' ... when entering a flag");
	System.out.println(" ");
    }
}
