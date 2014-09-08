package cfa.vo.sedlib.io;

import java.io.OutputStream;
import cfa.vo.sedlib.common.SedException;

/**
 *  Interface for writing wrapped objects to files, streams.
 */
interface IWriter 
{

    /**
     * Writes data to a file given an appropriate object.
     * @param filename
     *   {@link java.lang.String} 
     * @param Object
     */
    public void write(String filename, Object data) throws SedException;

    /**
     * Writes data to a file given an appropriate object.
     * @param filename
     *   {@link java.io.OutputStream} 
     * @param Object 
     */
    public void write(OutputStream ostream, Object data) throws SedException;


}//end IWriter interface
