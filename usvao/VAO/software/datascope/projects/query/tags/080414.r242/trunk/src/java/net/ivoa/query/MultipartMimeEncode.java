package net.ivoa.query;

import java.util.Map;
import java.util.Date;

class MultipartMimeEncode {
   
    public static String encode (Map<String, String> data) {
	
	String separator = "SEP" + new Date().getTime() + "SEP";
	StringBuilder  output = new StringBuilder();
	
	output.append("Content-Type: multipart/form-data; boundary=");
	output.append(separator+"\r\n\r\n");
	output.append(separator+"\r\n");
	
	for (String key: data.keySet()) {
	    output.append("Content-Disposition: form-data; name=\""+key+"\"\r\n");
	    output.append("\r\n");
	    output.append(data.get(key));
	    output.append(separator+"\r\n");
	}
	
	return output.toString();
    }
}
