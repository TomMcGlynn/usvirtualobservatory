/**
 * 
 */
package org.usvao.samp.client;

/**
 * @author thomas
 *
 */
public class SampClientException 
extends Exception 
{

	private static final long serialVersionUID = 1L;

	public SampClientException() {
		super();
	}

	public SampClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public SampClientException(String message) {
		super(message);
	}

	public SampClientException(Throwable cause) {
		super(cause);
	}

}
