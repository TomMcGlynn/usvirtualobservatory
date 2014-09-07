package org.usvo.openid.serve;

import org.openid4java.message.AuthRequest;
import org.usvo.openid.orm.UserSession;

/** The result of an attempt to log in, whether successful or failed. */
public class AuthnAttempt {

    public static enum Source { COOKIE, INTERACTIVE, EXCEPTION }

    private AuthRequest authRequest;
    private String username;
    private boolean successful;
    private String message;
    private Source source;

    private UserSession cookieSession;

    public AuthnAttempt(String username, boolean successful, String message, Source source) {
        this.username = username;
        this.successful = successful;
        this.message = message;
        this.source = source;
    }

    public String getUsername() { return username; }
    public boolean isSuccessful() { return successful; }
    public String getMessage() { return message; }
    public Source getSource() { return source; }
    public AuthRequest getAuthRequest() { return authRequest; }

    public UserSession getCookieSession() { return cookieSession; }
    public void setCookieSession(UserSession cookieSession) { this.cookieSession = cookieSession; }

    public void initAuthRequest(AuthRequest authRequest) {
        if (authRequest == null) throw new NullPointerException("Authreq is null.");
        if (this.authRequest != null) throw new IllegalStateException("Already initialized.");
        this.authRequest = authRequest;
    }

    @Override
    public String toString() {
        return "AuthnAttempt{" +
                "username='" + username + '\'' +
                ", successful=" + successful +
                ", message='" + message + '\'' +
                ", source=" + source +
                '}';
    }
}
