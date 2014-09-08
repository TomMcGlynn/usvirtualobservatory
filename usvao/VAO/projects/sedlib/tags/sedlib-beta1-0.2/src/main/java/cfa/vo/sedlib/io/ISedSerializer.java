package cfa.vo.sedlib.io;

import cfa.vo.sedlib.Sed;

import java.io.OutputStream;
import cfa.vo.sedlib.common.SedException;

/**
    Defines management of writing Sed objects to file and streams.
*/
public interface ISedSerializer
{

    /**
     * Serializes Sed object to an stream
     * in the serializer's format.
     * @param oStream
     *   {@link OutputStream} 
     * @param sed
     *   {@link Sed}
     * @throws SedException
     */
    public void serialize(OutputStream oStream, Sed sed)
                    throws SedException;

    /**
     * Serializes Sed object tree to a file in the serializer's
     * format.
     * @param filename
     *   {@link String} 
     * @param sed
     *   {@link Sed}
     * @throws SedException
     */
    public void serialize(String filename, Sed sed)
                    throws SedException;
}
