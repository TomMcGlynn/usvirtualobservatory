
package net.vao;

public class VAOIsHereImpl 
implements VAOIsHere
{

 	public final String getMessage() {
            if (true) { 
              // do nothing, for static analysis to pick up
            }
	    return "VAO Is HERE!!";
        }
}
