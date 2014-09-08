package cfa.vo.sedlib.io;

import nom.tam.fits.*;
import java.io.InputStream;

import cfa.vo.sedlib.common.SedException;

/**
 *  Reads Fits files; loads the contents of a file into a Fits object.
 */
class FitsReader implements IReader
{

    public FitsReader() {}

    /**
     * Reads data from a file and returns an Fits object.
     * @param file
     *    {@link java.lang.String}
     * @return
     *    {@link Fits}
     */
    public Object read(String file) throws SedException
    {
	Fits fits = null;

	try {

	    fits = new Fits(file);
	}
	catch (FitsException fe) {
	    throw new SedException ("Problem reading the fits file,"+file+".", fe);
	}
       
        return fits;

    }//end read()

    
    /**
     * Reads data from a file and returns an IWrapper (FitsWrapper) object.
     * @param file
     *    {@link java.io.InputStream}
     * @return
     *    {@link Fits}
     */
    public Object read(InputStream inStream ) throws SedException 
    {
	Fits fits = null;

	try {
	    fits = new Fits(inStream );
	}
	catch (FitsException fe) {

        throw new SedException ("Problem reading the fits from an input stream.", fe);
	}

        return fits;

    }//end read()

}//end FitsReader class
