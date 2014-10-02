package net.ivoa.registry.harvest;

import net.ivoa.registry.std.RIStandard; 

import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.util.Properties;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

/**
 * a document iterator that will traverse across multiple source streams
 */
public abstract class MultisourceDocumentIterator extends DocumentIteratorBase {
    protected VOResourceExtractor source = null;
    Properties std = RIStandard.getDefaultDefinitions();
    IOException except = null;

    /**
     * initialize this iterator with the first source stream
     */
    protected MultisourceDocumentIterator(InputStream firstSource) {
        source = new VOResourceExtractor(firstSource, std);
    }

    /**
     * a method that returns the next XML source available.
     */
    protected abstract InputStream nextSource() throws IOException;

    public Reader nextReader() throws IOException {
        Reader nxt = source.nextReader();
        if (nxt == null) {
            InputStream nxtsource = nextSource();
            if (nxtsource == null) return null;
            source = new VOResourceExtractor(nxtsource, std);
            return this.nextReader();
        }
        return nxt;
    }
    

}
