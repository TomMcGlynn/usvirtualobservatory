import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.text.*;
import java.awt.event.*;

public class StringToBuffer
{
    String xml;
    public StringToBuffer(String xmlin)
    {
	xml = xmlin;
    }

    public java.io.InputStream parseStringToIS()
    {
	
	if(xml==null)
	    {
		return null;
	    }
	xml = xml.trim();
	java.io.InputStream in = null;
	try
	    {
		in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	    }
	catch(Exception ex)
	    {
		
	    }
	return in;
    }

}
