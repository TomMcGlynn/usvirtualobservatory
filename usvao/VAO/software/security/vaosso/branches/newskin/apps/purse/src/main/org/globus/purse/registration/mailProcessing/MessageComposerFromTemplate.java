/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * an interface used for assembling the body of a mail message from 
 * a template. <p>
 * 
 * This class will read in a template message and substitute in information 
 * provided to it via the compose() method in the form of a Properties 
 * object.  The template is simply text provided in a string or a file 
 * (via the constructor) with embedded tags of the form @name@ that 
 * represent substitution points.  When a tag is encountered, the name is 
 * looked up in the information Properties object.  If found, the tag is 
 * replaced by the value.  If not, tag remains intact in the output message.  
 * The tag @@ will be replaced with a single percent sign; this can be 
 * over-ridden by including an information property value with a zero-length
 * string.  <p>
 */
public class MessageComposerFromTemplate implements MessageComposer {
    Vector tokens = null;

    /**
     * Create a composer.  setTemplate() needs to be called to set 
     * the template before calling compose().
     */
    public MessageComposerFromTemplate() { }

    /**
     * create a composer with a template read from an input stream
     * @param template  the input stream to read the template from
     * @exception IOException   if an error occurs while reading the template
     */
    public MessageComposerFromTemplate(Reader template) 
         throws IOException
    {
        this();
        loadTemplate(template);
    }

    /**
     * Create a composer with a given template
     * @param template  the file containing the template
     * @exception IOException   if an error occurs while reading the template
     */
    public MessageComposerFromTemplate(File template) 
         throws IOException
    {
        this();
        loadTemplate(template);
    } 

    /**
     * Create a composer with a template taken from a string
     * @param template  a string containing the template message.  
     *                  THIS IS NOT A FILENAME.
     * @exception IOException   if an error occurs while reading the template
     */
    public MessageComposerFromTemplate(String template) 
         throws IOException
    {
        this();
        loadTemplate(template);
    }

    /**
     * load the template message from an input stream
     * @param template   an input stream containing the template message 
     * @exception IOException   if an error occurs while reading the template
     */
    public void loadTemplate(Reader template) throws IOException {
        BufferedReader in = null;
        in = (template instanceof BufferedReader) 
            ? (BufferedReader) template 
            : new BufferedReader(template);
        load(in);
    }

    /**
     * load the template message from an input stream
     * @param template   a file containing the template message 
     * @exception IOException   if an error occurs while opening or 
     *                          reading the template
     */
    public void loadTemplate(File template) throws IOException {
        load(new BufferedReader(new FileReader(template)));
    }

    /**
     * load the template message from an input stream
     * @param template   a string containing the template message 
     * @exception IOException   if an error occurs while reading the template
     */
    public void loadTemplate(String template) throws IOException {
        load(new BufferedReader(new StringReader(template)));
    }

    /**
     * parse the template, looking for substitution tags.  
     * @exception IOException   if an error occurs while reading the template
     */
    void load(BufferedReader template) throws IOException {
        tokens = new Vector();
        StringBuffer sb = new StringBuffer();
        String line = null;
        int p1,p2;
        while ((line = template.readLine()) != null) {
	    // line = removeComment(line); // don't do this -- it removes URLs too.
	    if (line == null)
		    continue;
            while ((p1 = line.indexOf('@')) >= 0) {
                p2 = line.indexOf('@',p1+1);
                if (p2 >= 0) {
                    sb.append(line.substring(0,p1));
                    tokens.addElement(sb.toString());
                    tokens.addElement(line.substring(p1,p2));
                    line = line.substring(p2+1);
                    sb = new StringBuffer();
                }
                else {
                    break;
                }
            }

            sb.append(line).append('\n');
        }
        tokens.addElement(sb.toString());
    }

    public String removeComment(String s) {
	Pattern p = Pattern.compile("//.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);

	String temp = new String(s);
	Matcher m = p.matcher(s);
	if (m.find()) {
		int start = m.start();
		if (start == 0)
			return null;
		int end = m.end();
		StringBuffer buf = new StringBuffer(temp);
		buf = buf.delete(start,end);
		temp = buf.toString();
	}
	return temp;
    }
    /**
     * write the message to the given output stream.
     * @param info    a list of properties representing information to be 
     *                   incorporated into the message.  Keys in this list 
     *                   are tag names and values provide the strings to 
     *                   substitute for the tags.
     * @param out     the output stream to write the message to.
     */
    public void compose(Properties info, Writer out) throws IOException {
        if (tokens == null) 
            throw new IllegalStateException("Message template not yet loaded");

        PrintWriter msg = (out instanceof PrintWriter) 
            ? (PrintWriter) out : new PrintWriter(out);
        for(Enumeration e = tokens.elements(); e.hasMoreElements();) {
            String token = (String) e.nextElement();
            if (token.length() > 0 && token.charAt(0) == '@') {
                String sub = info.getProperty(token.substring(1));
                if (sub == null) {
                    if (token.length() == 1) 
                        // if we have just @ (which appears as @@ in the 
                        // template), just substitute in a single percent
                        // sign.  
                        msg.print("@");
                    else 
                        // if no tag mapping is found, just spit out the tag
                        // as it was found in the template
                        msg.print(token + '@');
                }
                else {
                    msg.print(sub);
                }
            }
            else {
                msg.print(token);
            }
        }
    }

}
