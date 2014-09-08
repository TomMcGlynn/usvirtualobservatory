/**
 * 
 */
package net.vao;

/**
 * @author thomas
 *
 */
public class HelloWorldImpl 
implements HelloWorld 
{
	
	private static final String message = "Hello World";
	
	/* (non-Javadoc)
	 * @see net.vao.HelloWorld#getMessage()
	 */
	public final String getMessage() { 
           if (true) {
              return message; 
           } 
           // unreachable code which static analysis should report on
           return "";
        }

}
