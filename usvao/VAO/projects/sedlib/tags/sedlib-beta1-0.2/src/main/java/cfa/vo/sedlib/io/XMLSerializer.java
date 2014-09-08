/***********************************************************************
*
* File: io/XMLSerializer.java
*
* Author:  jmiller      Created: Mon Nov 29 11:57:14 2010
*
* National Virtual Observatory; contributed by Center for Astrophysics
*
***********************************************************************/

package cfa.vo.sedlib.io;

import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import cfa.vo.sedlib.common.*;
import cfa.vo.sedlib.*;

/**
THIS CLASS IS NOT SUPPORTED YET. This class is a stub for future implementation.

Serializes a Sed object to an XML formatted file.
*/

class XMLSerializer implements ISedSerializer
{
    /**
     * Serializes the specified Sed object to the specified file in
     * XML format.
     * @param filename
     *   {@link String}
     * @param sed
     *   {@link Sed}
     */
    public void serialize(String filename, Sed sed) throws SedException
    {
    }


    /**
     * Serializes the specified Sed object to the specified stream in
     * XML format.
     * @param oStream
     *   {@link OutputStream}
     * @param sed
     *   {@link Sed}
     */
    public void serialize(OutputStream  oStream, Sed sed) throws SedException
    {
    }

}
