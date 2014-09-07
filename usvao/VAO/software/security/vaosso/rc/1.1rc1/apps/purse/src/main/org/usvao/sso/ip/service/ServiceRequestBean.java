package org.usvao.sso.ip.service;

import java.util.Properties;
import java.util.Arrays;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.SSOProviderServiceException;

import org.globus.purse.util.Comma;
import org.globus.purse.util.HtmlEncode;

/**
 * a base class for beans that provide or help provide a service.  The 
 * service inputs are represented by bean properties.  Implementations are 
 * typically used with JSP pages that provide an interface to the service
 * and may encapsulate knowledge of how that page appears and behaves.
 */
public abstract class ServiceRequestBean {

    protected ParamErrors _errors = null;
    protected Properties _params = new Properties();
    protected String[] _propNames = null;

    /**
     * initialize this been with the names of the service parameters
     */
    protected ServiceRequestBean(String[] parameters) { 
        _propNames = Arrays.copyOf(parameters, parameters.length);
        _errors = new ParamErrors(_propNames);
        for (String name : _propNames)
            _params.setProperty(name, "");
    }

    /**
     * set a service parameter value.  The value is trimmed and HTML 
     * (entity)-decoded before being saved.  
     * @param name   the name of the property chosen from those set at 
     *                 ServiceRequestBean construction time.
     */
    protected void setParameter(String name, String val) {
        val = (val == null) ? "" : clean(val);
        _params.setProperty(name, val);
    }

    /**
     * get a service parameter value.
     * @param name   the name of the property chosen from those set at 
     *                 ServiceRequestBean construction time.
     */
    public String getParameter(String name) { return _params.getProperty(name); }

    /**
     * trim and decode the string.  This used for cleaning up a string 
     * provided by the user (e.g. via an input form) which may have 
     * entity-encoded bits or superfluous spaces.
     */
    protected String clean(String s) { 
        if (s == null) return "";
        try {
            return URLDecoder.decode(s.trim(), "UTC-16"); 
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError("prog. error in clean():"+ex.getMessage());
        }
    }

    /**
     * return true if the input paramters are valid for processing by the 
     * service.  If they are not, register errors internally.  
     * <p>
     * This method should not throw any exceptions except unexpected 
     * ones resulting from unintended system failures.  
     */
    public abstract boolean validate() throws SSOProviderSystemException;

    /**
     * execute this service with the given parameters.  It should throw
     * one of the declared exceptions if the request is unsuccessful.  
     * <p>
     * If one wants the request to execute itself, then one should override
     * this function.  The default implementation throws an 
     * ExecuteUnsupportedException. 
     */
    public void execute() 
        throws SSOProviderServiceException, SSOProviderSystemException 
    {
        throw new ExecuteUnsupportedException(getClass().getName());
    }

    /**
     * an exception thrown by execute if execution by this bean is not 
     * supported.  
     */
    public class ExecuteUnsupportedException extends SSOProviderSystemException {
        public ExecuteUnsupportedException(String outerclass) { 
          super("execute() on " + outerclass + "not supported"); 
        }
    }

    /**
     * add an error message associated with the given parameter name
     */
    public void addErrorMsg(String paramName, String msg) {
        _errors.addMessage(paramName, msg);
    }

    /**
     * return the error message for a given parameter or null if there are 
     * no messages registered.
     */
    public String[] getErrorMsgsFor(String paramName) {
        return _errors.getMessagesFor(paramName);
    }

    /**
     * return true if there are error messages registered for the given 
     * parameter.
     */
    public boolean errorsFoundFor(String paramName) {
        return _errors.hasMessagesFor(paramName);
    }

    /**
     * return true if any errors were register as a result of running validate().
     */
    public boolean errorsFound() {
        return (_errors.getParamCount() > 0);
    }

    /** 
     * return the error message set for external storage
     */
    public ParamErrors exportErrors() { return _errors; }

    /**
     * load errors from external storage
     */
    public void loadErrors(ParamErrors errs) {
        if (errs != null) _errors = errs;
    }

    /**
     * create URL-GET arguments from these inputs
     */
    public String toURLArgs() {
        ParamFormatter pf = new ParamFormatter("", "=", "", "&", false);
        pf.formatParams();
        return pf.toString();
    }

    /**
     * a class for formatting the name-value pairs into a String.  This is 
     * most commonly used to convert the parameters into URL-GET name-value
     * arguments, but it can be used for other forms.  Calling formatParams()
     * creates the string concatonation.
     */
    protected class ParamFormatter {
        String _pre = "";
        String _post = "";
        String _delim = "";
        Comma _sep = new Comma("", "");
        boolean _encode = false;
        StringBuilder buf = new StringBuilder();

        /**
         * create the formatter 
         * @param pre    prepend each parameter name with this string
         * @param delim  seperate the name and value with this string
         * @param post   append each parameter value with thei string
         * @param sep    seperate each name-value pair (including pre, delim, 
         *                and post) with this string.
         */
        public ParamFormatter(String pre, String delim, String post, String sep,
                       boolean htmlEncode)
        {
            if (pre != null) _pre = pre;
            if (post != null) _post = post;
            if (delim != null) _delim = delim;
            if (sep != null) _sep = new Comma("", sep);
            _encode = htmlEncode;
        }

        public void appendParam(String name, String val) {
            if (_encode) val = HtmlEncode.encode(val);
            buf.append(_sep).append(_pre).append(name).append(_delim)
               .append(val).append(_post);
        }

        public void formatParams() {
            String val = null;
            for (String name : _propNames) {
                val = getParameter(name);
                if (val != null && val.length() > 0) 
                    appendParam(name, val);
            }
        }

        public String toString() { return buf.toString(); }
    }

}