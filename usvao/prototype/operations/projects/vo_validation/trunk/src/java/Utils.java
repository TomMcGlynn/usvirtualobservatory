import java.io.*;
import java.io.BufferedReader;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.sql.*;
import java.text.*;
import java.util.regex.*;
import java.net.*;

/* Contains methods;
public static void writeToTestsTable  
public static HashMap writeToErrorsTable  

public static HashMap getErrorCodes
public static HashMap getServiceObjects
public static Connection getConnection
public static void  validateServiceObjects 
public static void parseDocument 
public static int  getHighestRunidPlusOne
*/


public class Utils
{ 
    public static HashMap getErrorCodes(Connection con)
    {
        HashMap  errorcodes = new HashMap();
	ReadTable TR = new ReadTable(con,"ErrorCodes",0);
	ResultSet result  =  TR.readTable();
	try 
	    {
		while (result.next())
		    {                  
			String id        = result.getString("validationResCode");
			String message   = result.getString("description");			
			errorcodes.put(message,id);
		    }   
	    }
	catch (Exception e)
	    {
		System.out.println(e.getMessage());
	    }	
	return errorcodes;
    }  
    public static HashMap getTestTypes(Connection con)
    {    
	HashMap  testtypes  = new HashMap();
        ReadTable TR      = new ReadTable(con,"Test",0);
        ResultSet  result   =  TR.readTable();
        try 
	    {		
                while (result.next())
                    {                  
                        String  type     = result.getString("type");
                        String  id       = result.getString("id");
			String url       = result.getString("url");
			String processing_status = result.getString("status");		
			String method    = result.getString("classmethod");
			String concat   = method + "}" +  id + "}" + url + "}"  +  processing_status; 
                        testtypes.put(type,concat);
                    }
            }        
        catch (Exception e)
            {
                e.printStackTrace();               
            }     
        return testtypes;	
    }
    public static void printErrorCodes(HashMap errorcodes)
    {   
        Iterator it = errorcodes.entrySet().iterator();
        
        while(it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                String a = (String)entry.getKey();
                String b = (String)entry.getValue();                         
            }
    }
    public static String[] runImageTest(String votableurl,String classtype)
    {
       if (System.getProperty("debug") != null){System.out.println("running image test");}
       String[]  response   = new String[2];
       String    status =   "fail";
       String error  = null;
       try {
	   WebFile file    = new WebFile(votableurl,60000);
	   int i           = file.getResponseCode();
	   if (i !=  -1) 
	       {
		   if ( i == 200)
		       {
			   HashMap hash = Utils.loadHandlerNames(); 
			   XMLReader  r              = XMLReaderFactory.createXMLReader();
			   VOTableHandler  voHandler = new  VOTableHandler(classtype, hash);
			   r.setContentHandler(voHandler);
			   r.setErrorHandler(voHandler);   
			   r.parse(votableurl);
			   Tester tester    = voHandler.getTester();
			   if (tester != null) { 
			       tester.test();
			       status = tester.getStatus();
			         error     = voHandler.getErrorMessage();
                                 String error1 =  tester.getError();
                                 if ((error1 != null) && (error1.length() > 0)) 
                                 {  
                                      error = (error.length() >0) ?  error = error + ";" + error1 : error1;
                                 }
                           }
	                   else { error = "Could not find a tester class for this service";}
                           if (error.length() == 0) {error = "unknown error";}
                      }
	          else{
	                System.out.println("Cannot connect to web page,error code: " + i);
	              }
	       }
       }
       catch (IOException e)
	   {   
	       System.out.println("An exception may have been thrown by WebFile.java...exiting the runImage test");
               e.printStackTrace();
	   }
       catch (SAXException e)
	   {
	       System.out.println("...you have a parser exception" + e.getMessage() );
	   }
       catch (ClassCastException e)
	   {
	       System.out.println("..you have a class cast exception " + e.getMessage());
	   }
       catch(Exception e)
	   {
	       System.out.println("...you have an Exception " + e.getCause().getClass());
	   }
       
       response[0] = status;       
       response[1] = error;
       return response; 
       
    }   
    public static HashMap loadHandlerNames(){
	
        HashMap hash  = new HashMap();
        hash.put("fits","FitsURLTester");
        hash.put("application/x-votable+xml","TextURLTester");
	hash.put("fits/image","FitsURLTester");
        hash.put("spectrum","FitsURLTester");
        hash.put("image","ImageURLTester");
        hash.put("image/fits","FitsURLTester");
	hash.put("text/plain","TextURLTester");
        hash.put("image/jpeg","ImageURLTester");
        return hash;	
    }
    public static int  getHighestRunidPlusOne(Statement stmt)
    {
	int largestrunid = 0;
	try
	    {		
	        largestrunid = 1;
		String testtablequery   = "select runid  from Tests order by runid desc limit 1";                 
		ResultSet  rs       = stmt.executeQuery(testtablequery);
		boolean empty = true;
		while (rs.next())
		    {
			empty = false;
			largestrunid = rs.getInt("runid");
			largestrunid += 1;
			//System.out.println("Your highesrunid plus one is: " + largestrunid);              
		    }
		if (empty)
		    {			
			largestrunid = 1;
		    }		
	    }
	catch (Exception e)
	    {
		System.out.println(e.getMessage());
	    }
	return largestrunid;
    }
    public static ResultSet getTestableServices(ResultSet tmpresult)
    {
        HashMap serviceobjects = new HashMap();
	ResultSet result  = tmpresult;
	return tmpresult;	
    }
    public static String getValidationdbName()
    { 
        System.out.println("in get vala"); 
         String line =  null;
         try {
         BufferedReader br = new BufferedReader(new FileReader("/www/server/vo/validation/data/startup.pm"));
	 
         while ((line =  br.readLine()) != null)
	 {
	    Pattern p = Pattern.compile("(.*?)\\$::dbname(.*)\\s+\\=(.*)");
	    Matcher m = p.matcher(line);
	    if (m.matches() == true){System.out.println("Coorrru");  }
    
	 } 
          }
         catch (FileNotFoundException e)
	{
	  System.out.println(e.getMessage());
	}
        catch (Exception e) {System.out.println(e.getMessage());} 
         return line;	
    } 
    public static void processOptions(String[] args)
    {	
        MySqlConnection mycon = new MySqlConnection();
        Connection con        = mycon.getConnection(); 
        String regexp            = "ivo://.*";
        Pattern pattern       =  Pattern.compile(regexp);
        Matcher matcher;     
        if (args.length >0) 
            {
                matcher =  pattern.matcher(args[0]);
                //System.out.println("A " + args[0]);
                //System.out.println("\nEntered identifier: " + matcher.matches());
                if  (args.length == 1)
                    {
                        String id  = null;
                        //one argument...not an identifier
                        if (matcher.matches() == false)
                            {  
                                //System.out.println("You did not enter an id");
                                //the user may have entered (pass or to screen only)
                                FlagHolder.storeflag((String) args[0],id );
                            }
                        //one arg ...but it is an identifier
                        else 
                            {
                                //one argument (an identifier)
                                //System.out.println("You did enter an id");
                                FlagHolder.storeflag("default",args[0]); 
                            }           
                    }
                else if (args.length == 2)
                    {
                        //System.out.println("You entered two args");
                        FlagHolder.storeflag((String) args[0], (String) args[1]);               
                        
                    }
                else if (args.length > 2)
                    {
                        System.out.println("Error: Cannot enter more than 2 arguments");
                        System.exit(0);
                    }
            }
        else
	    {
		//no flags used on command line (default)
		FlagHolder.storeflag("default", null);
		
	    }
    }
    public static int getRowLimit()
    {
	int rowlimit = 0;
	try 
	    {
		//get the number of services to validate (from local file number_to_validate)
		BufferedReader input =  new BufferedReader(new FileReader("../data/number_to_validate"));
		String line   = null;                
		while (( line = input.readLine()) != null)
		    {
			String regexp       =  "^#.*";
			Pattern pattern       =  Pattern.compile(regexp);
			Matcher matcher       =  pattern.matcher(line);
			
			if (matcher.matches() == false)
			    {                                   
				String rowlimitstring  = line;
				rowlimit  =  Integer.valueOf(rowlimitstring).intValue(); 
			    }
		    }
	    }
	catch (IOException e)
	    {
		System.out.println("Could not read in number to validate" + e.getMessage());
	    }
	return rowlimit;
    }
    public static String fixPos(String baseurl)
    {
        String pos;
        if (! baseurl.endsWith("?") && !baseurl.endsWith("$"))
            {            
                if (baseurl.indexOf("?") >0)
		    {	
			pos =  (!baseurl.endsWith("&")) ? "&POS=" : "POS=";			   
		    }	    
		else
		    {
			pos =  "?POS=";
		    }
	    }
	else
	    {
		pos = "POS=";
	    }
	return pos;
    }
    public static String runstilts(String votableurl)
    {
	String s = null;
	try
	    {
                URL url               = new URL(votableurl);
                URLConnection connect = null;   
                connect               = url.openConnection();
                connect.setConnectTimeout( 120000 );    
                connect.setReadTimeout( 120000 );      
		java.io.InputStream d    = connect.getInputStream();
		
		Process p = Runtime.getRuntime().exec("/usr1/local/java/bin/java -jar ../javalib/stilts.jar votcopy format=tabledata  in=-"); 
		 OutputStream os = p.getOutputStream(); 
		 byte[] buf = new byte[32768]; 
		 int len;
		 while ((len=d.read(buf)) > 0) 
		     {         
			 os.write(buf, 0, len);     
		     }
		 os.close();
		 BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		 /**try
		     {
			 XMLReader  r              = XMLReaderFactory.createXMLReader(); 
			 VOTableHandler  voHandler = new  VOTableHandler();
			 r.setContentHandler(voHandler);
			 r.setErrorHandler(voHandler);
			 r.parse(stdInput);
			 //String imageurl = voHandler.getImageUrl();
		     }
		 
		 catch (SAXException e)
		     {
			 System.out.println("...There was an xml parsing exception");
		     }
		 */
		
		// read the output from the command
		 String regexp = ".*<TD>(http:|ftp:)//.*";
		 Pattern pattern  = Pattern.compile(regexp); 
		  
		 while ((s = stdInput.readLine()) != null)
		     {
			 //System.out.println(s);
			 Matcher matcher =  pattern.matcher(s);
			 if (matcher.matches() == true)
			     {
				 String t =   Utils.getRidOfWhiteSpaces(s);
				 s= t;
				 break;		     
			     }
		     }
	    }
	catch (IOException e) 
	    {
		System.out.println("exception happened - here's what I now have: ");
		e.printStackTrace();
		System.exit(-1);
	    }
	return s;
    }
    public static String getRidOfWhiteSpaces(String s) 
    {
	StringTokenizer st = new StringTokenizer(s);
	String r = "";
	while (st.hasMoreTokens()) 
	    { 
		r = r+st.nextToken(); 
	    }
	String newstring= r.replaceAll("\\<.*?\\>", "");
	return newstring;
    }
    public static String getRidOfHTML(String s)
    {
	String nohtml = s.replaceAll("\\<.*?>","");
	return nohtml;
    }
    public static String cleanup(String s)
    {
	Pattern pat = Pattern.compile("Line\\s\\d+:");
	Matcher mat = pat.matcher(s);
	String n  =  mat.replaceAll(" ");
	mat.reset();
	return n;
    }
    public static String JavaGetUrl (String inputstring)
    {
	URL u;
	InputStream is = null;
	DataInputStream dis;
	String s;
	StringBuffer response = new StringBuffer();
	
	try 
	    {
		u = new URL(inputstring);
		is = u.openStream();
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		
                String nextLineFromService = br.readLine();
                while (nextLineFromService != null) {
		    response.append(nextLineFromService);
		    nextLineFromService = br.readLine();
		}
	    }
	catch (Exception e)
	    {
		System.out.println(e.getMessage());
	    }
	String r = response.toString();
	return r;
    }  
}
