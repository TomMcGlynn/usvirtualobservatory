import java.io.*;
import java.io.BufferedReader;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import nom.tam.fits.*;
import nom.tam.util.*;
import java.util.regex.*;
//import nom.tam.fits.BasicHDU.*;

public class RunStilts
{      

        String c_votableurl = null;   
        String c_status = null;
        String c_imageurl = null;
        public RunStilts(String votableurl)
        {
          c_votableurl  = votableurl;
       
        }
        public void test()
	{ 
           String s = null;
           try{
                URL url               = new URL(c_votableurl);
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
                                 c_imageurl= t;
                                 break;              
                             }
                     }

               }

            catch (MalformedURLException e)
            {
                System.out.println("FitsURLTester.java" + e);
            }
            catch (IOException e)
            {           
                System.out.println(e);
            }
            catch (NumberFormatException nfe)
            {
                System.out.println("NumberFormatException: " + nfe.getMessage());
            }
    
          }
          public String getImageURL()
          {
	       return c_imageurl;
          }
}

