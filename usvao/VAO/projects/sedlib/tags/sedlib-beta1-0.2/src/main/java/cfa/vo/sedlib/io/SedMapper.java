package cfa.vo.sedlib.io;

import cfa.vo.sedlib.Sed;
import cfa.vo.sedlib.common.SedException;
import java.io.IOException;

/**
 *  Defines management of updating Sed objects from other well defined objects.
 */
public abstract class SedMapper {

    public SedMapper () {}

    /**
     * Converts data in some format to a Sed object. 
     * @param data
     *   Object
     * @param sed
     *   {@link Sed}
     * @return
     *   The returned Sed is the same reference to the input Sed.
     * @throws  SedException
     * @throws  IOException
     */
    abstract public Sed populateSed(Object data, Sed sed) 
    	throws SedException, IOException;

    /**
     * Converts data in some format to a Sed object
     * @param data
     *   Object
     * @throws  SedException
     * @throws  IOException
     */
    public Sed populateSed(Object data) 
    	throws SedException, IOException
    {
        return this.populateSed (data, new Sed ());
    }

}

