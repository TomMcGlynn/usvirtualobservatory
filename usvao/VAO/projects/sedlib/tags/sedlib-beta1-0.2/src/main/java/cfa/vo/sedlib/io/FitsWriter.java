package cfa.vo.sedlib.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.DataOutputStream;

import nom.tam.fits.*;

import cfa.vo.sedlib.common.SedException;

/**
 *  Writes Fits objects to file.
 */
class FitsWriter implements IWriter 
{

    public FitsWriter() {}

    /**
     * Writes Fits data to a file.
     * @param filename
     *   {@link java.lang.String} 
     * @param wrapper
     *   {@link Fits}
     */
    public void write(String filename, Object data) throws SedException
    {
        Fits fits = (Fits)data;

        try
        {
            File bf = new File(filename);
            FileOutputStream fos = new FileOutputStream( bf );
            this.write( fos, fits );
            fos.flush();
            fos.close();
        }
        catch(IOException ioe)
        {
            throw new SedException ("Problem accessing, "+filename+".", ioe);
        }

    }//end write()

    /**
     * Writes data to a stream given appropriate IWrapper.  Returns status code.
     * @param outStream
     *   {@link java.io.OutputStream} 
     * @param wrapper
     *   {@link IWrapper}
     * @return 0 on success, non-zero otherwise.
     *   int
     */
    public void write( OutputStream outStream, Object data) throws SedException
    {
        //TODO  check other writers, interface
        Fits fits = (Fits)data;

        DataOutputStream dataOutStream = new DataOutputStream( outStream );

        try
        {
            fits.write( dataOutStream );
        }
        catch(FitsException fe) {

            throw new SedException ("Problem writing to a fits file.", fe);
            
        }
        
    }//end write()

}//end FitsWriter class
