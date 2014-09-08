package edu.harvard.cfa.vo.tapclient.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ResponseFormatExceptionTest {
    @Test public void noArgTest() {
	ResponseFormatException ex = new ResponseFormatException();
	assertNull(ex.getMessage());
	assertNull(ex.getCause());
    }

    @Test public void messageTest() {
	ResponseFormatException ex = new ResponseFormatException("message");
	assertEquals("message", ex.getMessage());
	assertNull(ex.getCause());
    }

    @Test public void messageCauseTest() {
	Throwable cause = new Throwable();
	ResponseFormatException ex = new ResponseFormatException("message", cause);
	assertEquals("message", ex.getMessage());
  	assertEquals(cause, ex.getCause());
    }

    @Test public void causeTest() {
	Throwable cause = new Throwable();
	ResponseFormatException ex = new ResponseFormatException(cause);
	assertEquals((cause == null ? null : cause.toString()), ex.getMessage());
  	assertEquals(cause, ex.getCause());
    }
}