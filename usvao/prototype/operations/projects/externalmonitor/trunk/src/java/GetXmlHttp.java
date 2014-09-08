import java.net.*;
import java.io.*;


public final class GetXmlHttp
{
    protected String c_url;
    int c_timeout;
    boolean flag;
    private String charset   = null;
    private Object content   = null;
    private java.util.Map<String,java.util.List<String>> responseHeader = null;
    private java.net.URL responseURL = null;
    private int responseCode = -1;
    private String MIMEtype  = null;


    public GetXmlHttp(String urlin, int timeout) throws IOException, java.net.MalformedURLException
    {
	c_url = urlin;
	c_timeout=  timeout;
	
	
	
        String xml = "";
       
		final java.net.URL url = new java.net.URL(c_url);
		final java.net.URLConnection  uconn = url.openConnection();
		if ( !(uconn instanceof java.net.HttpURLConnection) )
            throw new java.lang.IllegalArgumentException(
                "URL protocol must be HTTP." );
        final java.net.HttpURLConnection conn =
            (java.net.HttpURLConnection)uconn;
 
	System.out.println("timeout is " + c_timeout);
	conn.setConnectTimeout(c_timeout);
	conn.setReadTimeout(c_timeout);
	conn.setInstanceFollowRedirects( true );
        conn.setRequestProperty( "User-agent", "spider" );

	System.out.print("True Conecting now");
	conn.connect();
	System.out.print("Connected");

	// Get the response.
        responseHeader    = conn.getHeaderFields( );
        responseCode      = conn.getResponseCode( );
        responseURL       = conn.getURL( );
	
	final int length  = conn.getContentLength( );
	final String type = conn.getContentType( );	
        if ( type != null ) {
            final String[] parts = type.split( ";" );
            MIMEtype = parts[0].trim( );
            for ( int i = 1; i < parts.length && charset == null; i++ ) {
                final String t  = parts[i].trim( );
                final int index = t.toLowerCase( ).indexOf( "charset=" );
                if ( index != -1 )
                    charset = t.substring( index+8 );
            }
	}
      
     
	// Get the content.
       final java.io.InputStream stream = conn.getErrorStream( );    
        if ( stream != null )

	    {
		
		content = readStream( length, stream );
		System.out.println("Trying to read");
	    }
        else if ( (content = conn.getContent( )) != null &&
		  content instanceof java.io.InputStream )
	    {
            content = readStream( length, (java.io.InputStream)content );
	    }
	conn.disconnect( );
    }
    private Object readStream( int length, java.io.InputStream stream )
        throws java.io.IOException {

        final int buflen = Math.max( 1024, Math.max( length, stream.available() ) );
        byte[] buf   = new byte[buflen];;
        byte[] bytes = null;
 
        for ( int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf) ) {
            if ( bytes == null ) {
                bytes = buf;
                buf   = new byte[buflen];
                continue;
            }
            final byte[] newBytes = new byte[ bytes.length + nRead ];
            System.arraycopy( bytes, 0, newBytes, 0, bytes.length );
            System.arraycopy( buf, 0, newBytes, bytes.length, nRead );
            bytes = newBytes;
        }
 
        if ( charset == null )
	    return bytes;
	    
     
        try {
            return new String( bytes, charset );
        }
        catch ( java.io.UnsupportedEncodingException e ) {System.out.println("You have an encoding exception");}
        return bytes;
    }
    /** Get the content. */
    public Object getContent( ) {
        return content;
    }
    /** Get the response code. */
    public int getResponseCode( ) {
        return responseCode;
    }
 
    /** Get the response header. */
    public java.util.Map<String,java.util.List<String>> getHeaderFields( ) {
        return responseHeader;
    }
 
    /** Get the URL of the received page. */
    public java.net.URL getURL( ) {
        return responseURL;
    }
 
    /** Get the MIME type. */
    public String getMIMEType( ) {
        return MIMEtype;
    }

}

