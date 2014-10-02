/*
 * PluginFactory..java
 * $ID*
 */

package dalserver.conf;

import java.util.HashMap;

/**
 * PluginFactory provides methods to calculate specific values for particular metadata terms
 */
public interface PluginFactory {

    /**
     * Generic method to return the value of a metadata term based on the values of other metadata
     */
    public String getValue(String param, HashMap headerVals);

}
