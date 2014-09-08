/***********************************************************************
*
* File: io/SedSerializerFactory.java
*
* Author:  jmiller      Created: Mon Nov 29 11:57:14 2010
*
* National Virtual Observatory; contributed by Center for Astrophysics
*
***********************************************************************/

package cfa.vo.sedlib.io;

/**
    A factory for retrieving an appropriate SED library serializer.
*/
public class SedSerializerFactory
{
    public SedSerializerFactory () 
    {
    }

    /**
     * Create a serializer based on the specified format
     * @param informat
     *   {@link SedFormat}
     * @return
     *   possible serializers include
     *   {@link FitsSerializer}
     *   {@link VOTableSerilizer}
     */
    static public ISedSerializer createSerializer (SedFormat informat)
    {
        ISedSerializer serializer = null;
        switch (informat){
        case FITS:
            serializer = new FitsSerializer();
            break;
        case VOT:
            serializer = new VOTableSerializer();
            break;
        case XML:
            serializer = new XMLSerializer();
            break;
        default:
            break;
        }

        return serializer;
    }
}
