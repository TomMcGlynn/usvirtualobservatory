package net.ivoa.query;


import java.io.IOException;
import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.ByteArrayOutputStream;

/** This class copies an output stream as it writes it.
  * The copy can be accessed using getCopy() which returns
  * the byte array as written.
  */
public class CopyingOutputStream extends FilterOutputStream {
    
    private java.io.ByteArrayOutputStream bs = new ByteArrayOutputStream();
    
    public CopyingOutputStream(OutputStream base) {
	super(base);
    }
   
    public void close() throws IOException {
	super.close();
	bs.close();
    }
    
    public void write(int b) throws IOException {
	super.write(b);
	bs.write(b);
    }
    
    public void write(byte[] b) throws IOException {
	super.write(b);
	bs.write(b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
	super.write(b,off,len);
	bs.write(b,off,len);
    }
    
    public void flush() throws IOException {
	super.flush();
	bs.flush();
    }
    
    public byte[] getCopy() {
	return bs.toByteArray();
    }
}	
