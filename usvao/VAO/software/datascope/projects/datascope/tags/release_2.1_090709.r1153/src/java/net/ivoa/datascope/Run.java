package net.ivoa.datascope;

/** Run a main method in another class, trap and report errors
 *  for use in a Web page.
 */
public class Run {
    
    
    public static void main(String[] args) {
	
	if (args.length == 0) {
	    error(new IllegalArgumentException("No class specified"));
	}
	
	String cls = args[0];
	
	try {
	    String[] nargs = new String[args.length-1];
	    System.arraycopy(args, 1, nargs, 0, args.length-1);
	    java.lang.reflect.Method m = Class.forName(cls).getMethod("main", new Class[]{String[].class});
	    m.invoke(null, new Object[]{nargs});
	} catch (Exception e) {
	    error(e);
	} catch (Error f) {
	    error(f);
	}
    }
    
    private static void error(Throwable e) {
	
	System.out.flush();
	if (e instanceof java.lang.reflect.InvocationTargetException) {
	    e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
	}
	
	System.out.println("Content-type: text/html\n");
	System.out.println("<head><title>DataScope exception </title>");
	System.out.println("<script language=JavaScript>");
	System.out.println("var vis='visible'");
	System.out.println("function flipVisible() {");
	System.out.println("var x = document.getElementById('traceback')");
	System.out.println("if (x != null) x.style.visibility=vis");
	System.out.println("x = document.getElementById('jobinfo')");
	System.out.println("if (x != null) x.style.visibility=vis");
	System.out.println("if (vis == 'visible') vis = 'hidden'");
	System.out.println("else vis='visible'");
	System.out.println("}");
	System.out.println("</script></head><body>");
	System.out.println("<b> Processing error:</b>"+e.getMessage()+"\n");
	System.out.println("<br><input type=button name='See details' value='See details' onclick='flipVisible()'>");
	System.out.println("<div id=traceback style='visibility:hidden'>");
	System.out.println("<pre>");
	e.printStackTrace(System.out);
	System.out.println("</pre></div>");
    }
}
