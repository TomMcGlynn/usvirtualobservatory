package net.ivoa.skynode;

import java.net.URL;
import java.net.HttpURLConnection;

import java.io.*;
import java.util.Map;

public class SoapTest {
  
    public static void main(String[] args) throws Exception {
    
        String urlStr = args[0];
        String input  = args[1];
	String op     = args[2];
        
        BufferedReader br = new BufferedReader(new FileReader(input));
        
        String inp = "";
        String line;
        while ((line = br.readLine()) != null) {
        	inp += line;
        }
        System.out.println("***Input is:\n"+inp+"\n***\n");
        
        
        URL url = new URL(urlStr);
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	conn.setRequestMethod("POST");
	conn.setRequestProperty("SOAPAction", "SkyNode.ivoa.net/"+op);
	conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
	conn.setDoOutput(true);
	
	conn.connect();
        
        OutputStream os = conn.getOutputStream();
        os.write(inp.getBytes());
        InputStream  is = conn.getInputStream();
        
	Map mp = conn.getHeaderFields();
	for(Object key:  mp.keySet()) {
	    System.out.println("Header-> "+key+": "+mp.get(key));
	}
	
        br = new BufferedReader(new InputStreamReader(is));
        
        System.out.println("***Output:");
        while ((line = br.readLine()) != null) {
        	System.out.println(line);
        }
    }
}
		       
