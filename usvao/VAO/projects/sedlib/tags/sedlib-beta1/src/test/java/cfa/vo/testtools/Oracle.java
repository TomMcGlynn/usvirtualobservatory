/***********************************************************************
*
* File: Oracle.java
*
* Author:  olaurino              Created: Mon Apr 4 12:26:00 EST 2011
*
* National Virtual Observatory; contributed by Center for Astrophysics
*
***********************************************************************/

package cfa.vo.testtools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Class for arbitrary property checks, for testing purposes
 * 
 */
public class Oracle extends HashMap<String, Object>{

    /**
     * This method checks that the <code>property</code> of <code>testedObject</code> equals the correct value (the @link{HashMap} value associated with the <code>property</code>)
     *
     * @param testedObject
     * @param property
     * @throws OracleFailException
     * @throws OracleException
     */
    public void testEquals(Object testedObject, String property) throws OracleFailException, OracleException {
        try {
            Object expected = this.get(property);
            Object observed = this.get(testedObject, property);
            if(!expected.equals(observed))
                throw new OracleFailException("expected "+expected+", but got "+observed);
        } catch (Exception ex) {
            throw new OracleException(ex);
        }
    }

    private Object get(Object testedObject, String key) throws Exception {
        String[] _fields = key.split("\\.");
        ArrayList<String> fields = new ArrayList(Arrays.asList(_fields));

        return get(testedObject, fields);
        
    }

    private Object get(Object testedObject, ArrayList<String> fields) throws Exception {
        String field = fields.get(0);
        String initial = field.substring(0, 1);
        String methodName= "get"+field.replaceFirst(initial, initial.toUpperCase());
        Method method = testedObject.getClass().getMethod(methodName);

        fields.remove(0);

        Object response = method.invoke(testedObject);

        if(fields.isEmpty()) {
            return response;
        }
        
        return get(response, fields);
    }

    /**
     *
     * This method tests the <code>testedObject</code> against all the properties included in the current instance of the Oracle.
     * All properties are assumed to be tested through the testEquals method
     *
     * @param segment
     * @throws Exception
     */
    public void test(Object testedObject) throws Exception {
        for(String key : keySet()) {
            testEquals(testedObject, key);
        }
    }

}
