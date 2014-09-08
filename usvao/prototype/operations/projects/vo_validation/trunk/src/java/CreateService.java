import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.Random;
import java.lang.reflect.*;

public  class CreateService
{
   
    Service s;

    public CreateService(String sname,String base,String ivoident, String ra_in, String dec_in,String sr_in, 
                   String role_in,String xsi,String sid,  String vurl,String vid,String name,String procstatus)
    {
        String url = null;
        try
            {                            
                Class cls = Class.forName(name);
                String c  = cls.getName();                
                s         = (Service) cls.newInstance();
		s.init(sname,base, ivoident, ra_in,dec_in,sr_in,role_in,xsi,sid,vurl, vid,procstatus);
		url = s.getValUrl();
		s.setQuery(url);		
	    }
	catch (Throwable e)
            {
                System.err.println("Create Service Object " + e);
            }   
    }
    public Service getServiceObject()
    {
	return s;
    }
}
