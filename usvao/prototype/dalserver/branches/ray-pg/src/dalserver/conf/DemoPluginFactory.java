/*
 * DemoPluginFactory..java
 * $ID*
 */

package dalserver.conf;

import java.util.HashMap;

/**
 * DemoPluginFactory demonstrates how to calculate specific values for 
 * particular metadata terms
 */
public class DemoPluginFactory implements PluginFactory {

    /** Constructor to generate a new demonstration plugin factory */
    public DemoPluginFactory() {}

    /**
    * Generic method to return the value of a metadata term based on the values of other metadata
    */
    public String getValue(String param, HashMap headerVals) {
	String value = "";
	if (param.equals("RA")) {
	    value =  "123.456";
	}
	return value;
    }

}
