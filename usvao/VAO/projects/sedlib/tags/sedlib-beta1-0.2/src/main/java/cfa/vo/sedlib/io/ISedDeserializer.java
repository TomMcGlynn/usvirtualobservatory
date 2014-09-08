package cfa.vo.sedlib.io;

import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.common.SedException;

import java.io.InputStream;
import java.io.IOException;

/**
    Defines management of reading Sed objects from files and streams.
*/
public interface ISedDeserializer
{

    /**
     * Deserializes a Sed object from a stream
     * in the deserializer's format. 
     * @param iStream
     *   {@link InputStream} 
     * @return
     *   {@link Sed}
     *
     * @throws SedException
     */
    public Sed deserialize(InputStream iStream) 
    	throws SedException, IOException;

    /**
     * Deserializes Sed object from a file in deserializer's format.  
     * @param filename
     *   {@link String} 
     * @return 
     *   {@link Sed}
     *
     * @throws SedException
     */
    public Sed deserialize(String filename) 
    	throws SedException, IOException;
}
