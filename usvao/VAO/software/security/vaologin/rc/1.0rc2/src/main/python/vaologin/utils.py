"""
Common utility classes and functions for vaologin.  This includes 
common error classes.
"""
import os

class VAOLoginError(Exception):
    def __init__(self, msg):
        self.message = msg
    def __str__(self):
        return repr(self.message)

class ConfigurationError(VAOLoginError):
    """
    an error indicating the there is an error in the way vaologin is 
    installed or configured.
    """
    def __init__(self, msg):
        VAOLoginError.__init__(self, msg)

class CGIError(VAOLoginError):
    """a general error while authenticating via CGI."""
    def __init__(self, msg):
        VAOLoginError.__init__(self, msg)

class SessionExpired(CGIError):
    """an exception indicating that the service session has expired"""
    def __init__(self, user, sessionid='', msg=None):
        self.user = user
        self.sessionid = sessionid
        if msg is None:  
            msg = "Session %s has apparently expired for %s" % (sessionid, user)
        CGIError.__init__(self, msg)

class AuthenticationError(VAOLoginError):
    """an exception indicating that a fatal error occured while authenticating
    the user"""
    pass

def ensureDir(path, deep=True, mode=0777):
    """
    ensure that the directory given by path exists.  If it doesn't, attempt
    to create it.
    @param path   the path to the directory to ensure exists
    @param deep   if False, the parent directory is required to exist already;
                     otherwise, an exception is raised.  
    @param mode   the permissions mode to create the directory with.
    @throws ConfigurationError  if the file does not exist
    """
    if not os.path.exists(path):
        try: 
            if deep:
                os.makedirs(path, mode)
            else:
                os.mkdir(path, mode)
        except OSError, ex:
            parent = os.path.dirname(path)
            raise ConfigurationError("Unable to create dir: %s: %s" %
                                     (path, ex.strerror))

