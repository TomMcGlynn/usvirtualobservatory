
package net.vao;

import junit.framework.TestCase;

public class TestVAOIsHere 
extends TestCase 
{

   public void test1() throws Exception {

      VAOIsHere test = new VAOIsHereImpl();

      assertEquals ("Check output is correct", "VAO Is HERE!!", test.getMessage());
   }

}
