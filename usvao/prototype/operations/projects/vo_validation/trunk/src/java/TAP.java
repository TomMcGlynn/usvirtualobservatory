import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.regex.*;
import java.util.Random;
import java.util.logging.Logger;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.ttools.task.TapLint;
import uk.ac.starlink.ttools.task.MapEnvironment;
import java.text.*;
 

public class TAP extends Service 
{    
  
    public String getValUrl()
    {     
	String query   = "http:" + validatorurl + "heasarc" + getRandomInt() + "&endpoint=http:";
	String add     = "&RA=" + ra +  "&DEC=" + dec;
        add            += "&RASIZE=" + sr +  "&DECSIZE=" + sr;
	add            +=  "&format=xml&show=fail&op=Validate";
	try 
	    {
		String encoded = URLEncoder.encode(baseurl,"UTF-8");  
		query          = query + encoded + add;	
	        
	    }
	catch (UnsupportedEncodingException e)
	    {		
		System.out.println(e);
	    }
	
	return query;	    
    }
    
    public ArrayList test_default(ArrayList array, int runid, String date, String identifier)
    {
       	//query, serviceId,stmt,type,utc ,connection, errorcodes,validatorid,shortname;
        
        if (System.getProperty("debug")  != null){
	    System.out.println(" ");
	    System.out.println("Testing TAP: " + identifier);
	    System.out.println("baseurl is: " + baseurl);
	    System.out.println("original url is: " + originalbaseurl);
	}
	baseurl = "http:" + originalbaseurl;
	
	ArrayList array_subtestobjects  = new ArrayList();;
	ArrayList array_subtestobjects_1 = null; 

	try  {
	    //Logger.getLogger( "uk.ac.starlink" ).setLevel( Level.WARNING );
	    MapEnvironment env = new MapEnvironment();
	    env.setValue( "tapurl", baseurl);
	    env.setValue( "truncate","80");
	    env.setValue("stages","TME UWS QGE QPO QAS");
	    env.setValue("report","E");
	    // .. set other params here if you want
	    new TapLint().createExecutable( env ).execute();
	    String regexp  = "^E\\-.*";
	    Pattern p = Pattern.compile(regexp);
	    String[] olines = env.getOutputLines();
	    String[] olinesnew;
	    for ( int i = 0; i < olines.length; i++ ) 		    
		{
		    StringBuffer string  = new StringBuffer();
		    //System.out.println( olines[ i ] );
		    String[] arraynew = olines[i].split(" ");
		    for (int j = 1; j<arraynew.length; j++)
			{
			    string.append(arraynew[j] + " ");
			}
		    Matcher matcher  = p.matcher(arraynew[0]);
		    if (matcher.matches() == true)
			{
			    
			    SubTest sub = new SubTest(string.toString(),"none","fail",arraynew[0]);
			    array_subtestobjects.add(sub);
			}
		    else
			{
			    //System.out.println("PPP" + arraynew[0]);
			}
		    
		}
	}
	catch ( TaskException e){
	    System.out.println(e);	
	}
	catch (IOException e){
	    System.out.println(e);
	}
	
	/**new VO resource test, parse response
           VAOResourceTestBuilder vbuilder = new VAOResourceTestBuilder(baseurl,"MyContentHandler",ivoid);
           ValidatorResponseParser vrpnew = new ValidatorResponseParser(vbuilder.getValUrl(),"MyContentHandler");
	   array_subtestobjects_1   = vrpnew.parseDocument();
	   
           for (int i =0;i< array_subtestobjects_1.size();i++)
	   {
	   
                SubTest g = (SubTest) array_subtestobjects_1.get(i);          
                array_subtestobjects.add(g);
		}
		
	*/
	
	if (array_subtestobjects.size() == 0)
	    {				
		SubTest sub = new SubTest("empty string","none","pass","null");
		array_subtestobjects.add(sub);
		array.add(new Integer(runid));
		array.add(array_subtestobjects);
		array.add(date);             
	
	    }
	else
	    {
		SubTest subempty = (SubTest) array_subtestobjects.get(0);
		array.add(new Integer(runid));
		if ((subempty.getStatus().equals("abort")) || (subempty.getStatus().equals("fail")))
		    {			
			//System.out.println("Your service has  an abort or fail status");			
		    }
		else
		    {
			//run if test passes stand. val. 
			//System.out.println("Tap validation status:  pass");
		    }
		array.add(array_subtestobjects); 							
		array.add(date);
	    }	   
	return array;
    }
}
