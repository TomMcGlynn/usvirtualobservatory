package net.ivoa.registry.harvest;

import java.io.Reader;
import java.io.IOException;

/**
 * an interface for accessing a set of VOResource records as XML documents.
 */
public interface RecordServer {

    /**
     * return an iterator for stepping through the available VOResource
     * records.
     */
    public DocumentIterator records() throws IOException;

}