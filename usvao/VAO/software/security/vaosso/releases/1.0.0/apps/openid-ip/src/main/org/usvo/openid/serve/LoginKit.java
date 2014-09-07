package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.Conf;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.util.Compare;
import org.usvo.openid.util.ExternalProcessKit;
import org.usvo.openid.util.ParseKit;

import java.io.File;
import java.io.IOException;
import java.util.*;

/** Use an external process to check authentication via PAM or to create a credential.
 *  For timeouts, see {@link Conf#getLoginPatience()} and {@link Conf#getCredentialPatience()} */
public class LoginKit {
    private static final Log log = LogFactory.getLog(LoginKit.class);

    /** Attempt authentication with a shell command. In practice, used with a simple
     *  PAM client and PAM-MySQL to authenticate against entries in the PuRSe database. */
    public static AuthnAttempt localLogin(String username, String pwd) {
        String cmd = Conf.get().getAuthnCmd();
        int patience = Conf.get().getLoginPatience();
        return ExternalProcessKit.run(new LocalLoginProducer(username, pwd, cmd), patience);
    }

    /** Generate a credential for the user logged in by <tt>authn</tt>, using a cookie token to authenticate with MyProxy.
     *  @param authn must contain a session cookie with a valid token
     *  @return If successful, a short-lived URL for the credential, contained in an AuthnAttempt.
     *  If authentication fails, include a message in the result. */
    public static AuthnAttempt generateCredential(AuthnAttempt authn) throws IOException {
        int patience = Conf.get().getCredentialPatience();
        return ExternalProcessKit.run(new CookieCredentialProducer(authn), patience);
    }

    /** Generate an EEC (End-Entity-Credential) for the user logged in by <tt>authn</tt>, using a password to authenticate with MyProxy.
     *  @param authn
     *  @password password
     *  @return If successful, a short-lived URL for the credential, contained in an AuthnAttempt.
     *  If authentication fails, include a message in the result. */
    public static AuthnAttempt generateEndEntityCredential(AuthnAttempt authn, String pwd, int lifehours, String format, String pkcskey) throws IOException {
        int patience = Conf.get().getCredentialPatience();
        return ExternalProcessKit.run(new EndEntityCredentialProducer(authn, pwd, lifehours, format, pkcskey), patience);
    }


    /** Attempt to login based on an existing user session.
     *  @param session an existing session, which may be current or expired (probably based on a cookie)
     *  @param marginSecs the number of seconds of padding we require to avoid race conditions in
     *  completing a transaction */
    public static AuthnAttempt attemptLogin(UserSession session, int marginSecs) {
        if (session != null) {
            boolean valid = session.isValid(UserSession.SLOP_SECONDS, marginSecs);
            AuthnAttempt result = new AuthnAttempt(session.getUser().getUserName(), valid,
                    (valid ? "You are already logged in. To login to a different account, <a href=?logout=true>sign out</a>." :
                            "Your login session has expired. You can login to the below account or to a <a href=?logout=true>different account.</a>"),
                    AuthnAttempt.Source.COOKIE);
            result.setCookieSession(session);
            return result;
        }
        else
            return null;
    }

    /** Attempt to login using a username and password. */
    public static AuthnAttempt attemptLogin(String username, String password) {
        if (Compare.isBlank(username))
            return new AuthnAttempt("", false, "Please enter a login name.", AuthnAttempt.Source.INTERACTIVE);
        else if (password == null || password.length() == 0)
            return new AuthnAttempt("", false, "Please enter a password.", AuthnAttempt.Source.INTERACTIVE);
        else
            return localLogin(username, password);
    }

    // TODO handle race condition where user's session expires during the fulfillment of the request
    // -- after the OpenID request is first received, but before attributes are populated.
    public static class CookieCredentialProducer implements ExternalProcessKit.ExternalProducer<AuthnAttempt> {
        private String cmd;
        private AuthnAttempt authn;
        private File credDir, credFile;
        private String randDirName;

        public CookieCredentialProducer(AuthnAttempt authn) throws IOException {
            if (!watchingCredDir || credDirWatcher == null)
                log.warn("Credential purge thread does not appear to be running. "
                        + "Be sure to call startWatchingCredentialDir() when application is initialized.");
            this.authn = authn;
            if (authn.getCookieSession() == null) throw new IllegalArgumentException
                    ("AuthnAttempt does not contain a cookie session, which is required to create a credential.");
            File topDir = new File(Conf.get().getCredentialDir());
            randDirName = ParseKit.generateRandomBase64String(16);
            this.credDir = new File(topDir, randDirName);
            this.credFile = new File(credDir, authn.getUsername() + ".pem");
            this.cmd = Conf.get().getCredentialCommand() + " " + authn.getUsername() + " " + credFile.getPath();
        }

        private String getCredentialUrl() throws IOException {
            String result = Conf.get().getCredentialUrl();
            if (!result.endsWith("/")) result += "/";
            result += randDirName + "/" + authn.getUsername() + ".pem";
            return result;
        }

        /** If successful, return the URL where the credential can be found; if failed, return an error message.
         *  In practice, should succeed because the user is already logged in. */
        @Override
        public AuthnAttempt produce(int exitValue, List<String> output) {
            String out = ParseKit.printLines(output, " ");
            String message;
            boolean success = exitValue == 0;
            if (success) { // command completed successfully
                try {
                    message = getCredentialUrl();
                } catch (IOException e) {
                    throw new RuntimeException("Didn't expect an exception.", e);
                }
            }
            else
                message = out;
            return new AuthnAttempt(authn.getUsername(), success, message, AuthnAttempt.Source.COOKIE);
        }

        @Override
        public AuthnAttempt produce(Throwable e) {
            log.warn("Exception creating credential for " + authn.getUsername(), e);
            return new AuthnAttempt(authn.getUsername(), false, e.getMessage(), AuthnAttempt.Source.EXCEPTION);
        }

        /** cookie token is used as a password */
        @Override public String getStdIn() { return authn.getCookieSession().getToken(); }

        @Override public String[] getCommand() {
            // create credential directory as late as possible before it is needed
            if (!credDir.exists()) {
                boolean made = credDir.mkdirs();
                if (!made)
                    log.error("Unable to create credential directory \"" + credDir.getPath() + "\".");
            }
            return cmd.split(" ");
        }

        @Override
        public String getDescription() {
            return "Creating credential for " + authn.getUsername() + " from cookie token.";
        }
    }

    // TODO handle race condition where user's session expires during the fulfillment of the request
    // -- after the OpenID request is first received, but before attributes are populated.
    public static class EndEntityCredentialProducer implements ExternalProcessKit.ExternalProducer<AuthnAttempt> {
        private String cmd;
        private String password;
        private AuthnAttempt authn;
        private File credDir, credFile;
        private String randDirName;
        private int lifehours;
        private String format;
        private String pkcskey;

        public EndEntityCredentialProducer(AuthnAttempt authn, String password,
              int lifehours, String format, String pkcskey) throws IOException {
            if (!watchingCredDir || credDirWatcher == null)
                log.warn("Credential purge thread does not appear to be running. "
                        + "Be sure to call startWatchingCredentialDir() when application is initialized.");
            this.authn = authn;
            this.password = password;
            this.lifehours = lifehours;
            this.format = format;
            this.pkcskey = pkcskey;
            if (this.password == null) throw new IllegalArgumentException
                    ("password is required to create an end-entity credential.");
            if (this.lifehours == 0) throw new IllegalArgumentException
                    ("lifehours is required to be non-zero hours to create an end-entity credential.");
            if (this.format == null) throw new IllegalArgumentException
                    ("certificate format (pem or pkcs12 is required to create an end-entity credential.");
            if (this.pkcskey == null) throw new IllegalArgumentException
                    ("password to seal private key with is required to create an end-entity credential.");
            File topDir = new File(Conf.get().getCredentialDir());
            randDirName = ParseKit.generateRandomBase64String(16);
            this.credDir = new File(topDir, randDirName);
            this.credFile = new File(credDir, authn.getUsername() + ".pem");
            this.cmd = Conf.get().getEndEntityCredentialCommand() + " " + authn.getUsername() + " " + credFile.getPath() + " " + this.format + " " + this.lifehours + " " + this.pkcskey;
        }

        private String getCredentialUrl() throws IOException {
            String result = Conf.get().getCredentialUrl();
            if (!result.endsWith("/")) result += "/";
            result += randDirName + "/" + authn.getUsername() + ".pem";
            return result;
        }

        /** If successful, return the URL where the credential can be found; if failed, return an error message.
         *  In practice, should succeed because the user is already logged in. */
        @Override
        public AuthnAttempt produce(int exitValue, List<String> output) {
            String out = ParseKit.printLines(output, " ");
            String message;
            boolean success = exitValue == 0;
            if (success) { // command completed successfully
                    // message = getCredentialUrl();
                    message = credFile.getPath();
            }
            else
                message = out;
            return new AuthnAttempt(authn.getUsername(), success, message, AuthnAttempt.Source.COOKIE);
        }

        @Override
        public AuthnAttempt produce(Throwable e) {
            log.warn("Exception creating credential for " + authn.getUsername(), e);
            return new AuthnAttempt(authn.getUsername(), false, e.getMessage(), AuthnAttempt.Source.EXCEPTION);
        }

        /** cookie token is used as a password */
        @Override public String getStdIn() { return this.password; }

        @Override public String[] getCommand() {
            // create credential directory as late as possible before it is needed
            if (!credDir.exists()) {
                boolean made = credDir.mkdirs();
                if (!made)
                    log.error("Unable to create credential directory \"" + credDir.getPath() + "\".");
            }
            return cmd.split(" ");
        }

        @Override
        public String getDescription() {
            return "Creating credential for " + authn.getUsername() + " from cookie token.";
        }
    }

    public static class LocalLoginProducer implements ExternalProcessKit.ExternalProducer<AuthnAttempt> {
        private String username;
        private String pwd;
        private String cmd;

        public LocalLoginProducer(String username, String pwd, String cmd) {
            this.username = username;
            this.pwd = pwd;
            this.cmd = cmd;
        }

        private String getFullCommand() { return cmd + " " + username; }

        @Override
        public AuthnAttempt produce(int exitValue, List<String> output) {
            // newlines are not permitted in attributes, so use a space instead
            String msg = "";
            if (exitValue != 0) {
                if (output.size() > 0 && 
                    output.get(0).startsWith("usage:"))
                  // not that details are logged by ExternalProcessKit
                  msg = "Illegal username or password input";
                else 
                  msg = "Incorrect password for username";
            }
            return new AuthnAttempt(username, exitValue == 0, msg,
                                    AuthnAttempt.Source.INTERACTIVE);
        }

        @Override
        public AuthnAttempt produce(Throwable e) {
            return new AuthnAttempt(username, false, "Unable to authenticate: " + e.getMessage(),
                    AuthnAttempt.Source.EXCEPTION);
        }

        @Override public String getStdIn() { return pwd; }
        @Override public String[] getCommand() { return getFullCommand().split(" "); }

        @Override
        public String getDescription() {
            return "Authenticating via external process: \"" + getFullCommand() + "\"";
        }
    }

    private static Thread credDirWatcher;
    private static boolean watchingCredDir = false;
    /** Start a thread watching the credential storage directory. It will scan for and delete
     *  old credential subdirs every minute. Any subdirectory older than
     *  credential.delete.minutes (from config) will be deleted. */
    public static void startWatchingCredentialDir() {
        if (credDirWatcher != null)
            log.error("Already started up.");
        else {
            final File parentDir = new File(Conf.get().getCredentialDir());
            log.info("Starting credential directory purge thread, monitoring \"" + parentDir.getPath() + "\".");
            credDirWatcher = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (watchingCredDir) {
                        try {
                            long deleteMillis = Conf.get().getCredentialDeleteMinutes() * 60L * 1000L;
                            if (parentDir.exists()) {
                                if (parentDir.isFile())
                                    log.error("Credential storage dir is not a directory (\"" + parentDir.getPath() + "\".");
                                else {
                                    File[] children = parentDir.listFiles();
                                    for (File dir : children) {
                                        long age = System.currentTimeMillis() - dir.lastModified();
                                        if (age > deleteMillis)
                                            deleteRecursive(dir);
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            log.error("Exception monitoring credential dir", e);
                        }
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException ignored) { }
                    }
                    log.info("Stopping credential timed purge thread.");
                }

                private void deleteRecursive(File file) {
                    if (file.isDirectory()) {
                        File[] children = file.listFiles();
                        for (File child : children)
                            deleteRecursive(child);
                    }
                    log.debug("Deleting old credential file or dir: \"" + file.getPath() + "\"");
                    boolean deleted = file.delete();
                    if (!deleted)
                        log.error("Unable to purge out of date file \"" + file.getPath() + "\".");
                }
            });
            watchingCredDir = true;
            credDirWatcher.start();
        }
    }

    public static void stopWatchingCredentialDir() {
        watchingCredDir = false;
        if (credDirWatcher != null)
            credDirWatcher.interrupt();
    }
}
