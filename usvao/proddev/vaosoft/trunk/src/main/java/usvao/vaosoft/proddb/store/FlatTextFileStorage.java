package usvao.vaosoft.proddb.store;

import usvao.vaosoft.proddb.ProductDataIO; 
import usvao.vaosoft.proddb.LoadProduct; 
import static usvao.vaosoft.proddb.LoadProduct.*; 

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A class that can read and write product databases stored as 
 * flat text files on disk.  
 */
public class FlatTextFileStorage implements ProductDataIO {

    File src = null;

    public final static String NULLSTR = "-";

    /**
     * attach this class to a store version of the database
     */
    public FlatTextFileStorage(File source) {
        src = source;
    }

    private BufferedReader openSource() throws IOException {
        return new BufferedReader(new FileReader(src));
    }

    /**
     * return the time of the last modification to the store's contents or 
     * -1 if this cannot be determined.  This value can be used to trigger
     * a re-read of the data.
     */
    public long lastModified() { return src.lastModified(); }    

    private int[] idx = { NAME, VERSION, ORG, HOME, PROOF, PROPS };
    String[] parse(String line) {
        String[] in = line.split(" ");
        String[] data = new String[(in.length > PROPS+1) ? in.length : PROPS+1];

        int i = 0;
        for(i=0; i < idx.length && i < in.length; i++) {
            if (!in[i].equals(NULLSTR)) data[idx[i]] = in[i];
        }
        for(; i < in.length; i++) data[i] = in[i];

        return data;
    }

    /**
     * load all available product data.  Send data for each product 
     * to a LoadProduct destination.  
     */
    public synchronized void load(LoadProduct dest) throws IOException {
        BufferedReader rdr = null;
        String line = null;
        String[] data = null;
        int i = 0;
        try {
            rdr = openSource();
        
            while ((line = rdr.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                data = parse(line);
                dest.loadProduct(data);
            }
        }
        finally {
            if (rdr != null) rdr.close();
        }
    }
    
    /**
     * count the number of product records in the database
     */
    public synchronized int countProducts() throws IOException {
        BufferedReader rdr = null;
        String line = null;
        int c = 0;
        try {
            rdr = openSource();
            while ((line = rdr.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                c++;
            }
        }
        finally {
            if (rdr != null) rdr.close();
        }
        return c;
    }

    public synchronized void export(Iterator<String[]> di) throws IOException {
        // open up a temp file
        File parent = src.getParentFile();
        File out = File.createTempFile(src.getName(),null,parent);
        Writer w = new BufferedWriter(new FileWriter(out));

        // write in data
        try {
            export(di, w);
        } finally {
            w.close();
        }

        // when done, move it to the read location.
        if (! out.renameTo(src)) 
            throw new IOException("Failed to move new filename:" + out);
    }

    public synchronized void export(Iterator<String[]> di, Writer os) 
        throws IOException 
    {
        StringBuffer sb = null;
        String[] data = null;
        while (di.hasNext()) {
            data = di.next();
            os.write((data[NAME] == null) ? NULLSTR : data[NAME]);
            os.write(' ');
            os.write((data[VERSION] == null) ? NULLSTR : data[VERSION]);
            os.write(' ');
            os.write((data[ORG] == null) ? NULLSTR : data[ORG]);
            os.write(' ');
            os.write((data[HOME] == null) ? NULLSTR : data[HOME]);
            os.write(' ');
            os.write((data[PROOF] == null) ? NULLSTR : data[PROOF]);
            if (PROPS < data.length) {
                int pi = PROPS;
                while (pi < data.length && data[PROPS] == null) pi++;
                if (pi < data.length) {
                    os.write(' ');
                    os.write(data[pi]);
                    while (++pi < data.length) {
                        if (data[pi] != null) {
                            os.write('\t');
                            os.write(data[pi]);
                        }
                    }
                }
            }
            os.write("\n");
            os.flush();
        }
    }

    /**
     * return an iterator that will return each of the available 
     * product data in the store in turn.
     */
    public Iterator<String[]> rawIterator() throws IOException {
        return new RawIterator();
    }

    class RawIterator implements Iterator<String[]> {
        String[] last = null;
        BufferedReader rdr = null;
        public RawIterator() throws IOException {
            try {
                rdr = openSource();
                cacheNext();
            }
            catch (IOException ex) {
                if (rdr != null) rdr.close();
                rdr = null;
                throw ex;
            }
        }
        protected void finalize() {
            try {
                if (rdr != null) rdr.close();
                rdr = null;
            }
            catch (IOException ex) {  }
        }

        void cacheNext() {
            String line = null;
            try {
                while ((line = rdr.readLine()) != null) 
                    if (line.charAt(0) != '#') break;
            }
            catch (IOException ex) {
                throw new StoreAccessException(ex);
            }
            if (line == null) {
                last = null;
                return;
            }
            last = parse(line);
        }

        public boolean hasNext() { return (last != null); }
        public String[] next() {
            if (last == null) 
                throw new NoSuchElementException();
            String[] out = last;
            cacheNext();

            return out;
        }

        public void remove() { 
            throw new UnsupportedOperationException("ProductDB iterators not editable");
        }
    }


}