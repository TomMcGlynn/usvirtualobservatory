package cfa.vo.sedlib.io;

import java.io.*;
import org.xml.sax.SAXException;

import uk.ac.starlink.votable.*;

import cfa.vo.sedlib.common.SedException;


/**
 *  Reads documents in VOTable format from files, streams into a VOElement which is 
 *  then returned. 
 */
class VOTableReader implements IReader 
{

    /**
     * Reads data from a file and returns an VOElement object
     * @param  file
     *    {@link java.lang.String}
     * @return
     *    {@link VOElement}
     */
    public Object read(String file) throws SedException
    {
	VOElement voElement = null;

	try {
	    
	    // Create a tree of VOElements from the given XML file.
	    voElement = new VOElementFactory().makeVOElement( file );
	}
	catch(SAXException saxe) {

            throw new SedException ("Problem reading the VOTable file,"+file+".", saxe);
	}
	catch(IOException ioe) {

 	    throw new SedException ("Problem reading the VOTable file,"+file+".",ioe);
	}

	return voElement;

    }//end read()
    /**
     * Reads (or marshals) data from a file and returns an IWrapper (VOTableWrapper) object; returns null if the file cannot be read.
     * @param  file
     *    {@link java.io.InputStream}
     * @return
     *    {@link IWrapper}
     */
    public Object read(InputStream file) throws SedException
    {
	VOElement voElement = null;

	try {
	    
	    // Create a tree of VOElements from the given XML file.
	    voElement = new VOElementFactory().makeVOElement( file, null );
	}
	catch(SAXException saxe) {
            throw new SedException ("Problem reading the VOTable stream.", saxe);
	}
	catch(IOException ioe) {

            throw new SedException ("Problem reading the VOTable stream.",ioe);
 	    
	}

	return voElement;

    }//end read()

}
