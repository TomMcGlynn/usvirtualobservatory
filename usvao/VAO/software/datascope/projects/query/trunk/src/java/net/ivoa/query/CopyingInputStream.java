package net.ivoa.query;


import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.ByteArrayOutputStream;

/** This class copies an output stream as it writes it.
  * The copy can be accessed using getCopy() which returns
  * the byte array as written.
  */
public class CopyingInputStream extends FilterInputStream {
    
    private java.io.ByteArrayOutputStream bs = new ByteArrayOutputStream();
    
    public CopyingInputStream(InputStream base) {
	super(base);
    }
   
    public void close() throws IOException {
	super.close();
	bs.close();
    }
    
    public int read() throws IOException {
	int val = super.read();
	if (val >= 0) {
	    bs.write(val);
	}
	return val;
    }
    
    public int read(byte[] b) throws IOException {
	int val = super.read(b);
	if (val > 0) {
	    bs.write(b, 0, val);
	}
	return val;
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
	int val = super.read(b,off,len);
	if (val > 0) {
	    bs.write(b,off,val);
	}
	return val;
    }
    
    public byte[] getCopy() {
	return bs.toByteArray();
    }
}	
