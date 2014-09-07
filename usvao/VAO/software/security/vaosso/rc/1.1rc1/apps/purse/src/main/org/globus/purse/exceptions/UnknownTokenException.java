package org.globus.purse.exceptions;

/** Thrown when the token supplied by the user is unrecognized. */
public class UnknownTokenException extends RegistrationException {
    public UnknownTokenException(String msg) { super(msg); }
    public UnknownTokenException(String msg, Throwable exp) { super(msg, exp); }
}
