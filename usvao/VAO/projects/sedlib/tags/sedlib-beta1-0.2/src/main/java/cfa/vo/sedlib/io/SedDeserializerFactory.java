/***********************************************************************
*
* File: io/SedDeserializerFactory.java
*
* Author:  jmiller      Created: Mon Nov 29 11:57:14 2010
*
* National Virtual Observatory; contributed by Center for Astrophysics
*
***********************************************************************/

package cfa.vo.sedlib.io;

/**
    A factory for retrieving an appropriate SED library deserializer.
*/
public class SedDeserializerFactory
{
    public SedDeserializerFactory () 
    {
    }

    /**
     * Create a deserializer based on the specified format 
     * @param informat
     *   {@link SedFormat}
     * @return
     *   possible deserializers include
     *   {@link FitsDeserializer}
     *   {@link VOTableDeserilizer}
     */
    static public ISedDeserializer createDeserializer (SedFormat informat)
    {
        ISedDeserializer deserializer = null;
        switch (informat){
        case FITS:
            deserializer = new FitsDeserializer();
            break;
        case VOT:
            deserializer = new VOTableDeserializer();
            break;
        case XML:
            deserializer = new XMLDeserializer();
            break;
        default:
            break;
        }

        return deserializer;
    }
}
