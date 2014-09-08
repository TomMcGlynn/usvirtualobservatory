package cfa.vo.sedlib.io;

import java.io.InputStream;

import cfa.vo.sedlib.common.SedException;

/**
 *  Declares methods for reading data files; returns them in cast Object;
 */
interface IReader 
{

    /**
     * Reads data in from a file and returns an object.
     * @param file
     *    {@link java.lang.String}
     * @return
     *    {@link IWrapper}
     */
    public Object read(String file) throws SedException;

    /**
     * Reads data in from a file and returns an object.
     * @param file
     *    {@link java.io.InputStream}
     * @return
     *    {@link IWrapper}
     */
    public Object read(InputStream file) throws SedException;


}//end IReader interface
