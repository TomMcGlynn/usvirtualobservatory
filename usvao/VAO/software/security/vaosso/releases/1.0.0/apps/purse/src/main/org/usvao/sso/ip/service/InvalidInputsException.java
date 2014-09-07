package org.usvao.sso.ip.service;

import org.usvao.sso.ip.SSOProviderServiceException;

/**
 * an exception indicating that there are one or more problems detected in
 * the service input parameters.  An example would be that one or more 
 * required parameters have not been provided.  
 */
public class InvalidInputsException extends SSOProviderServiceException {

    ParamErrors _errors = null;

    /**
     * create the exception with a default message.
     */
    public InvalidInputsException() {  super(initMessage(null));  }

    /**
     * create the exception with a single simple message.
     */
    public InvalidInputsException(String msg) {  super(msg);  }

    /**
     * wrap a ParamErrors container that details the validation errors.
     * Note that the ParamErrors instance is not copied but rather is 
     * held by reference.  
     */
    public InvalidInputsException(String msg, ParamErrors errors) {  
        this(msg);  
        _errors = errors;
    }

    /**
     * wrap a ParamErrors container that details the validation errors.
     * Note that the ParamErrors instance is not copied but rather is 
     * held by reference.  
     */
    public InvalidInputsException(ParamErrors errors) {  
        this(initMessage(errors), errors);  
    }

    static private String initMessage(ParamErrors errors) {
        String out = null;
        if (errors != null) {
            String[] mainMsgs = errors.getMessagesFor("");
            if (mainMsgs != null && mainMsgs.length > 0) out = mainMsgs[0];
        }
        if (out == null) 
            out = "Invalid service inputs";
        return out;
    }

    /**
     * return the ParamErrors container describing the specific problems.
     */
    public ParamErrors getValidationErrors() { return _errors; }
}