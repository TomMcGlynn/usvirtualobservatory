package edu.harvard.cfa.vo.tapclient.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class HttpExceptionTest {
    @Test public void statusCodeConstructorTest() {
	HttpException ex = new HttpException(404);
	assertNull(ex.getMessage());
	assertEquals(404, ex.getStatusCode());
	assertNull(ex.getContent());
   	assertNull(ex.getCause());
    }

    @Test public void messageTest() {
	HttpException ex = new HttpException("message");
	assertEquals("message", ex.getMessage());
	assertEquals(0, ex.getStatusCode());
 	assertNull(ex.getContent());
   	assertNull(ex.getCause());
    }

    @Test public void messageStatusCodeTest() {
	HttpException ex = new HttpException("message", 403);
	assertEquals("message", ex.getMessage());
	assertEquals(403, ex.getStatusCode());
 	assertNull(ex.getContent());
   	assertNull(ex.getCause());
    }

    @Test public void messageStatusCodeContentTest() {
	HttpException ex = new HttpException("message", 403, "content");
	assertEquals("message", ex.getMessage());
	assertEquals(403, ex.getStatusCode());
 	assertEquals("content", ex.getContent());
   	assertNull(ex.getCause());
    }

    @Test public void messageCauseTest() {
	Throwable cause = new Throwable();
	HttpException ex = new HttpException("message", cause);
	assertEquals("message", ex.getMessage());
	assertEquals(0, ex.getStatusCode());
 	assertNull(ex.getContent());
  	assertEquals(cause, ex.getCause());
    }

    @Test public void causeTest() {
	Throwable cause = new Throwable();
	HttpException ex = new HttpException(cause);
	assertEquals((cause==null ? null : cause.toString()), ex.getMessage());
	assertEquals(0, ex.getStatusCode());
 	assertNull(ex.getContent());
  	assertEquals(cause, ex.getCause());
    }
}