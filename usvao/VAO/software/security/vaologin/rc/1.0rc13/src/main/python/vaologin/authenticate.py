#!/usr/bin/env python
"""
functions for a VAO-compliant OpenID Relying Party.

Based on consumer.py in Janrain, Inc., OpenID implementation examples
(c. 2005-2008, Janrain, Inc.)
"""
from __future__ import with_statement
import pickle, os, sys, time, re, urllib
from vaologin.utils import ConfigurationError, AuthenticationError, ensureDir
import vaologin.config as conf

def quoteattr(s):
    qs = cgi.escape(s, 1)
    return '"%s"' % (qs,)

try:
    import openid
except ImportError:
    sys.stderr.write("""
Failed to import the OpenID library. In order to use this command line tool, you
must either install the python-openid library (see INSTALL in the root of the
distribution) or else add the library to python's import path (the
PYTHONPATH environment variable).

For more information, see the README in the root of the library
distribution.""")
    raise

from openid.store import filestore
from openid.consumer import consumer
from openid.cryptutil import randomString
from openid.extensions import ax

def_vao_ip_url = "https://sso.usvao.org/openid/provider_id"
test_vao_ip_url = "https://vaossotest.ncsa.illinois.edu/openid/provider_id"

def_vao_logout_url = "https://sso.usvao.org/openid/logout"
test_vao_logout_url = "https://vaossotest.ncsa.illinois.edu/openid/logout"

class VAOLoginRequest(object):
    """
    Request handler that knows how to verify an OpenID identity as 
    well as retrieve a certificate using VAO conventions.

    This class provides a simpler interface for authentication than what is
    provided by python openid.  It persists session information using a session
    file on disk.  All the caller needs to do is create a sessionId (a 
    random string) and store it in a user cookie.  

    The recipe for authenticating and managing a session is as follows:
      1.  The web app determines if the web user provided a cookie with a 
          session id in it 
      2.  If there was no cookie, the web app creates a VAOLoginRequest instance
          without a session ID.  It can then retrieve this ID via the 
          getSessionId() method and set it to be delivered in a cookie to the 
          user.  (Alternatively, the web app can create its own new session id,
          a random, unique string, and provide it to the VAOLoginRequest 
          constructor.)
      3.  Check the status of the request by first calling getSession().isValid()
          If this returns True, the web app can assume that the user is 
          authenticated and provide the protected data.
      4.  If the session is not valid, the web calls 
          getSession().validationNeeded(); if this returns False, then the 
          authentication process needs to be inititated.  The web app calls
          requestAuthentication() to start the process.  This returns a URL
          for the login page; the web app redirects the web user to this URL.
      5.  If getSession().validationNeeded() returns True, the user has already
          logged in; we just need validate the response from the openid server.
          To do this we call processAuthentication().  This returns an
          AuthenticationResponse instance whose interface can tell the web
          app if the authentication was successful.  
      6.  If the web app wishes to end the user session (e.g triggered by the
          user's request), it calls the endSession() method.  
    """
    session = None

    def __init__(self, config, sessionId=None, errlog=None, store=None, 
                 sessionfilename=None):
        """
        create the request handler
        @param config        the VAOLogin configuration dictionary
        @param sessionid     the sessionid as provided by the web framework.
                               Often this is provided by a user cookie.  If
                               None (default), a new id (for a new session)
                               will be generated.
        @param store         an openid.store instance to use for storing 
                               state.  This does not affect session files.  
                               (If provided, the 
                               vaologin.auth.useStatelessProtocol is overridden
                               to use the stateful protocol.)
        @param errlog        a file stream for sending log messages.  If None,
                               log messages will not be written.
        @param sessionfilename  the base filename to use for storing session
                               data.  If None (default), the sessionid will
                               be used.  Set this if the application wants 
                               control the filename.
        """
        self.config = config
        self.log = errlog


        # if True, the stateless OpenID protocol will be used: association and 
        # nonce files will not be created (at the cost of more network calls)
        self.stateless = \
            self.config.get("vaologin.auth.usestatelessprotocol", None);
        if self.stateless and self.stateless.lower() in ('false', '0', ''):
            self.stateless = False

        # the openid state storage
        sdir = self.config.get("vaologin.auth.statedir")
        self.store = store
        if not self.stateless and not self.store and sdir:
            ensureDir(sdir, deep=False, mode=0700);
            self.store = filestore.FileOpenIDStore(sdir)

        self.sessionsDir = self.config.get("vaologin.auth.sessionsdir")
        if not self.sessionsDir and sdir:
            self.sessionsDir = os.path.join(sdir, "VAOsessions")
            ensureDir(sdir, deep=False, mode=0700);
        if not self.sessionsDir:
            raise ConfigurationError("Neither auth.statedir nor " +
                                     "auth.sessionddir set in config")
        ensureDir(self.sessionsDir, deep=True, mode=0700);

        self.session = Session.makeSession(self.sessionsDir, sessionId, 
                                           sessionfilename)
        self.sessionid = self.session.id

        # This makes sure pycurl will find our CA cert
        self._setFetcher()

    def _setFetcher(self):
        cacertfiles = self.config.get("vaologin.auth.cacertbundles")
        if cacertfiles:
            cacertfiles = conf.splitList(cacertfiles)
        setDefaultFetcher(CustomizedCurlFetcher(cacertfiles, self.log))

    def getConsumer(self, stateless=False):
        """
        return an openid.consumer.Consumer instance 
        """
        store = None
        if not stateless:  store = self.store
        return consumer.Consumer(self.session.data, store)

    def getSession(self):
        """
        return a session state for the session.  
        """
        return self.session

    def requestAuthenticationFor(self, openid_url, return_url, attributes=None):
        """
        initiate the Authentication process and return a URL to redirect
        the user's browser to query the user for password.  This should 
        be used when the portal provides a place for the user to provide
        a VAO-local OpenID.  
        @param openid_url   the OpenId URL representing the user's identity
                              if None, we will assume that the VAO Identity
                              Provider service will be used and the user 
                              will asked to provide a login name.  
        @param return_url   the URL in the current portal that the browser 
                              should be redirected to once the user has 
                              logged in.  
        @param attributes   a list of the user attributes desired back after 
                              successful authentication
        @param str   the URL at the OpenId provider to redirect the user to
        """
        return self.requestAuthentication(return_url, attributes, openid_url)

    def requestAuthentication(self, return_url, attributes=None, 
                              openid_url=None):
        """
        initiate the Authentication process and return a URL to redirect
        the user's browser to query the user for password.  When openid_url
        is None (default), the user will also be asked for a user name.  
        @param return_url   the URL in the current portal that the browser 
                              should be redirected to once the user has 
                              logged in.  
        @param attributes   a list of the user attributes desired back after 
                              successful authentication
        @param openid_url   the OpenId URL representing the user's identity
                              if None, we will assume that the VAO Identity
                              Provider service will be used and the user 
                              will asked to provide a login name.  
        @return str   the URL at the OpenId provider to redirect the user to
        """
        anonymous = False
        if not openid_url:
            openid_url = self.config.get("vaologin.auth.provideropenid",
                                         def_vao_ip_url)

        oidconsumer = self.getConsumer(self.stateless) # this will update self.session
        request = None
        try:
            request = oidconsumer.begin(openid_url)
        except consumer.DiscoveryFailure, exc:
            raise BadOpenIdError(openid_url, 
                                 "Problem interpreting OpenID: %s: %s" %
                                 (openid_url, str(exc)))

        if not request:
            raise NoOpenIdServiceError(openid_url)

        # add the desired attributes
        self._requestAxData(request, attributes)

        # request.setAnonymous(anonymous)

        # the realm is the URL that identifies the portal (RP) as a 
        # whole to the OP.  This can be used to recognize the portal.  
        realm = self.config.get("vaologin.auth.portalurlrealm")
        if not realm:
            realm = return_url

        redirect_url = request.redirectURL(realm, return_url, False)

        # store session data
        self.session.save()

        return redirect_url

    def _requestAxData(self, request, attributes):
        ax_request = None

        # start with the configured required
        atts = conf.splitList(
            self.config.get("vaologin.auth.requiredattributes", ""))
        if atts:
            # convert to URI names
            uriname = {}
            for k in Attributes.shortname.keys():
                uriname[Attributes.shortname[k]] = k
            atts = map(lambda a: (a in uriname.keys() and uriname[a]) or a,atts)

        if atts and len(atts):
            ax_request = ax.FetchRequest()
            for att in atts:
                ax_request.add(ax.AttrInfo(att, required=True))

        # now any passed-in attributes; do not require
        if attributes:
            if not ax_request:  
                ax_request = ax.FetchRequest()
            for att in attributes:
                if att not in atts:
                    ax_request.add(ax.AttrInfo(att, required=False))
                    atts.append(att)
            
        if ax_request:
            request.addExtension(ax_request)

    def processAuthentication(self, queryargs, return_url, keepSessionFor=None):
        """
        process the response from the authentication attempt and verify
        that it was successful and valid.  If keepSessionFor is given,
        a session file will be stored that contains the "pickled" contents
        of a python dictionary with attributes about the session.  
        @param queryargs    a dictionary of the name-value pairs that appeared
                               as arguments to (i.e. after '?' in) the URL.
        @param return_url   the actual URL that was invoked and which provided
                               the query arguments.  This does not need to 
                               include the query arguments.  As part of the 
                               verification, this is checked to see if it 
                               matches the URL that authentication service (OP)
                               was told to return to; thus, this should be 
                               obtained from the web service framework and not
                               presumed by the application.
        @param certfile     a file path that an X.509 certificate should be 
                               written to, should it have been requested.  If
                               None (default), it will be written to a default
                               location based on the configuration.
        @param keepSessionFor  if provided, the session file should be kept
                               on disk to track session state.  The value 
                               is the allowed lifetime for the session in 
                               hours.
        @returns AuthenticationResponse   a class indicating the resulting 
                               status of the validation and which provides
                               access to any requested attributes.
        """
        oidconsumer = self.getConsumer(self.stateless)
        resp = None
        try:
            info = oidconsumer.complete(queryargs, return_url)
            resp = AuthenticationResponse(info, self.session, self.config)
        except Exception, ex:
            import traceback as tb
            if self.log:
                print >> self.log, "Unexpected validation failure", \
                  "(session id = %s):" % self.sessionid, str(ex)
                tb.print_exc(100,self.log)
            resp = FailedAuthenticationResponse(self.session, 
                                                self.config, str(ex))

        if resp.isAuthenticated() and keepSessionFor is not None:
            lifehours = None
            if type(keepSessionFor) is int or type(keepSessionFor) is float:  
                if keepSessionFor > 0:
                    lifehours = keepSessionFor

            self.session.clearData()
            self.session.setValid(lifehours)
            self.session.addAttributes(resp.getAttributes())
            self.session.setProperty("openid.identity", 
                                     queryargs['openid.identity'])
            self.session.setProperty("openid.claimed_id", 
                                     queryargs['openid.claimed_id'])
            self.session.save()
        else:
            self.session.end()

        return resp

    def endSession(self):
        """
        End the current request's session.  
        """
        self.cleanupStore()
        sess = self.getSession()
        if sess:
            # if self.log:
            #     print >> self.log, \
            #         "Ending session for username=%s" % \
            #         sess.data.get('username','???')
            sess.end();

    def logout(self, forwardUrl, globally=None, openid_url=None):
        """
        return a URL to forward the user to so as to log the user out of 
        their session as well as (optionally) the VAO as a whole.  This 
        will first call endSession() to end the local session.  When 
        globally=False, this method is functionally equivalent to endSession(),
        and it simply returns the forwardUrl.  If globally is True, the 
        method will return a URL on the VAO ID provider site for logging 
        out the user from all of the VAO.  Note that this assumes the use of 
        a VAO-compatible OpenID provider as the global logout function is 
        not standard OpenID behavior; if a non-VAO OpenID provider is used,
        the vaologin.auth.globalLogoutUrl configuration parameter must be 
        set.  Note also that sso.usvao.org recommends use of global logouts.
        @param forwardUrl   the URL to redirect to.  This must be an
                             absolute URL.  
        @param globally     if True log the user out of the VAO network 
                             globally (via the logout service at
                             sso.usvao.org) as well as out of this portal; 
                             if False, log the user only out of this portal.
                             If not provided (None), the behavior is controlled
                             by the configuration parameter, 
                             "vaologin.auth.logoutMode".  globally will default
                             to True if the parameter is not set.
        """
        # end the session
        self.endSession();

        # if globally is not specified, defer to the configuration.
        # logoutMode=global means globally=True; logoutMode=local means
        # globally=False
        if globally is None:
            globally = (self.config.get("vaologin.auth.logoutmode") != "local")

        lourl = None
        if globally:
            # get the URL for the logout service
            lourl = self.config.get("vaologin.auth.globallogouturl")
            if not lourl:
                if not openid_url:
                    openid_url = self.config.get("vaologin.auth.provideropenid",
                                                 def_vao_ip_url)
                if openid_url == def_vao_ip_url:
                    lourl = def_vao_logout_url
            if not lourl:
                if self.log:
                    print >> self.log, "No logout service url", \
                      "(vaologin.auth.globalLogoutUrl) configured; disabling", \
                      "global logout"
                globally = False

        if not globally:
            # just log user out of this portal; keep them logged into the 
            # VAO at large
            return forwardUrl;            

        return "%s?returnURL=%s" % (lourl, forwardUrl);

    def cleanupStore(self):
        """
        clean up the store associated with this request, purging expired files.
        """
        if self.store:
            self.store.cleanup()


class Attributes(dict):
    """
    an extension of a dictionary for holding attributes
    """
    vaoatts = [ 'http://axschema.org/namePerson/friendly',      # USERNAME
                'http://axschema.org/namePerson',               # NAME
                'http://axschema.org/namePerson/first',         # FIRSTNAME
                'http://axschema.org/namePerson/last',          # LASTNAME
                'http://axschema.org/contact/email',            # EMAIL
                'http://axschema.org/contact/phone',            # PHONE
                'http://sso.usvao.org/schema/credential/x509',  # CERT
                'http://sso.usvao.org/schema/institution',      # INST
                'http://sso.usvao.org/schema/country',          # COUNTRY
                ]
    USERNAME  = vaoatts[0]
    NAME      = vaoatts[1]
    FIRSTNAME = vaoatts[2]
    LASTNAME  = vaoatts[3]
    EMAIL     = vaoatts[4]
    PHONE     = vaoatts[5]
    CERT      = vaoatts[6]
    INST      = vaoatts[7]
    COUNTRY   = vaoatts[8]
    shortname = { USERNAME : "username",
                  NAME     : "name",
                  FIRSTNAME: "firstname",
                  LASTNAME : "lastname",
                  EMAIL    : "email",
                  PHONE    : "phone",
                  CERT     : "certificate",
                  INST     : "institution",
                  COUNTRY  : "country"   }

class AuthenticationResponse(object):
    """
    a class that encapsulates and makes sense of the OpenID response.
    It can tell one if authentication succeeded or failed (and why).  
    If successful, it can provide the attributes requested.  
    """
    
    def __init__(self, openid_info, session, config):
        """
        wrap up the response info.
        @param openid_info    the info object returned by 
                                openid.consumer.Consumer.complete()
        @param sessionid      the session identifier
        @param config         the VAOLogin configuration in use
        """
        self.cfg = config
        self.info = openid_info
        self.session = session
        self.sessionid = session.getId()
        self.attributes = None
        self._cacheAttributes()

    def _cacheAttributes(self):
        if self.isAuthenticated():
            try:
                atts = Attributes()
                ax_response = \
                    ax.FetchResponse.fromSuccessResponse(self.info, False)
                if not ax_response:
                    return
                for key in ax_response.data.keys():
                    if key in Attributes.vaoatts:
                        # these are all singles
                        atts[key] = ax_response.getSingle(key)
                    else:
                        atts[key] = ax_response.get(key)
                        if len(atts[key]) == 1: 
                            atts[keys] = atts[key][0]

                self.attributes = atts

            except ax.AXError, ex:
                if self.log:
                  print >> self.log, "Warning: Attribute Exchange failure:", ex

    def getSessionId(self):
        """return the session identifier"""
        return self.sessionid

    def getSession(self):
        """return the session object"""
        return self.session

    def isAuthenticated(self):
        """
        return True if the user successfully completed authentication.  This 
        will return False if the user fails to enter a valid password, if 
        he/she explicitly canceled the login, if the protocol was not 
        followed correctly, or additional user input was needed by not 
        received (see neededUserInput()).
        """
        return self.info and self.info.status == consumer.SUCCESS

    def protocolFailed(self):
        """
        return True if the authentication was not successful.  This can 
        can be because the user canceled the authentication, the user 
        failed to provide a valid username and password, or the response
        from the provider was invalid.  
        """
        return not self.info or self.info.status == consumer.FAILURE

    def wasCanceled(self):  # canceled
        """
        return True if the authentication failed because the user gave up
        authentication.  This may because he/she could not provide a valid
        password or because they otherwise did not want to proceed with the
        login.  
        """
        return self.info and self.info.status == consumer.CANCEL

    def neededUserInput(self):
        """
        return True if the authentication request needed user input that 
        the login service couldn't get.  This happens when the portal (RP)
        assumes the user has already logged in and there for wants to 
        skip asking the user for a password (and permission) and return 
        right away.  If this assumption is not true and user input was 
        needed but not asked, this function returns true.  
        """
        return self.info and self.info.status == consumer.SETUP_NEEDED

    def getWhy(self):
        """
        return a string explaining why this type of response occurred.  
        This is most interesting when authentication has failed (i.e.
        protocolFailed() returns true) as there are several possible 
        reasons.  This statement should explain why.
        """
        if not self.info:
            return "Unknown vaologin error"
        elif isinstance(self.info, openid.consumer.consumer.FailureResponse):
            return self.info.message
        elif self.wasCanceled():
            return "Authentication was canceled by the user or server"
        elif self.neededUserInput():
            return "Authentication did not get needed user input"
        elif self.isAuthenticated():
            return "Authentication was successful"
        else:
            return "Unknown reason"
        

    def credentialAvailable(self):
        """
        return True if an X.509 certificate representing the user is 
        available.
        """
        return self.attributes and self.attributes.has_key(Attributes.CERT)

    def getAttributes(self):
        """
        return the requested attributes as dictionary, or None if the 
        authentication and attribute retrieval failed.
        """
        return self.attributes

    def cacheCredential(self, credfile=None):
        """
        retrieve and cache the available X.509 certificate to a given file.
        Note that this retrieves a cert from the login server (OP) and that 
        it is available for a limited time (~5 minutes); thus, if one asks 
        for a cert, it should be retrieved right away.  
        @param credfile   the path to the file that the certificate should 
                            be written into.  If None, a default based on 
                            the configuation will be used.  
        @return str  the file path where the credential was written.  If 
                       the credfile parameter was specified, the parameter
                       value will be returned
        @throws NoAvailableCredError  no certficiate is available because 
             either credentialAvailable() returns False (because none was 
             asked for), certificate availability timed out, or an error 
             was encountered transfering the cert from the server.  
        """
        if not credfile:
            creddir = self.cfg.get('vaologin.auth.certdir')
            if not creddir:
                statedir = self.cfg['vaologin.auth.statedir']
                creddir = os.path.join(statedir, "certificates")
            ensureDir(creddir, deep=True, mode=0700)
            credfile = os.path.join(creddir, self.sessionid)

        try:
            urllib.urlretrieve(self.attributes[Attributes.CERT], credfile)
        except Exception, ex:
            raise NoAvailableCredError(exc=ex)

        return credfile

class FailedAuthenticationResponse(AuthenticationResponse):
    """
    an AuthenticationResponse to use when the authentication utterly fails
    (perhaps because of unexpected inputs).
    """
    def __init__(self, session, config, why=None):
        AuthenticationResponse.__init__(self, None, session, config)
        if why is None: why = "unexpected vaologin conditions"
        self.why = why

    def getWhy(self):
        return self.why

class Session(object): 
    """
    a persistable dictionary of session data which includes an ID
    """

    def __init__(self, sessionid, sessionfile):
        """
        create the session instance.  Normally, this constructor is not 
        used directly; instead, the factory function, makeSession(), is 
        called.  
        @param sessionid     the sessionid
        @param sessionfile   the path to file to use to persist the session 
                               to disk
        """
        self.id = sessionid
        self.file = sessionfile
        self.data = None
        self.clearData()

    def getId(self):
        return self.id

    def clearData(self):
        """
        forget everything we know about this session (except the id)
        """
        self.data = {}
        self.data['id'] = self.id

    def reconstitute(self):
        """
        restore all data that was last stored to disk
        """
        if os.path.exists(self.file):
            try:
                with open(self.file) as fd:
                    self.data = pickle.load(fd)
                self.data['id'] = self.id
            except:
                # session is corrupted; keep old data
                pass
        else:
            self.clearData()

    def makeSession(sessiondir, sessionid=None, sessionfilename=None):
        """
        load or create a Session.  If no sessionid is provided, this is 
        considered a new session, and a new session id will be generated.
        If a session id is provided, session data will looked for in the 
        sessiondir directory.  If none is found, it is assumed that the 
        session is new (or long expired).  
        @param sessiondir    the directory to look for session data in.  
                                It is assumed to already exist.
        @param sessionid     the sessionid as provided by the web framework.
                               Often this is provided by a user cookie.
        @param sessionfilename  the (path-less) filename to use to persist
                               session data in sessiondir.  If None (default),
                               the sessionid will be used.  
        """
        sid = sessionid
        if not sid:
            sid = randomString(16, '0123456789abcdef')

        if not sessionfilename:
            sessionfilename = sid
        spath = os.path.join(sessiondir, sessionfilename);

        out = Session(sid, spath)
        if sessionid:
            out.reconstitute()
        return out

    makeSession = staticmethod(makeSession)

    def save(self):
        """
        save contents to disk
        """
        with open(self.file, 'w') as fd:
            pickle.dump(self.data, fd)

    def end(self):
        """
        end this session by removing its image on disk
        """
        if os.path.exists(self.file):
            os.remove(self.file)
        self.clearData()

    def addAttributes(self, atts):
        if not atts:  return

        for key in atts.keys():
            self.data[key] = atts[key]
            if key in Attributes.shortname.keys():
                self.data[Attributes.shortname[key]] = atts[key]

    def setProperty(self, name, value):
        self.data[name] = value

    def setValid(self, lifehours=None, since=None):
        """
        add data that indicates the session has successfully authenticated.
        @param lifehours how long the session is good for measured in hours.
        @param since     a UTC timestamp in seconds since the Epoch.  If None
                           (default), assume now.
        """
        if not since:
            since = time.time()
        self.data['validSince'] = since

        if lifehours is None:
            self.data['validLifetime'] = None
        else:
            self.data['validLifetime'] = lifehours * 3600

    def isValid(self, lifetime=None):
        """
        return whether the session is valid based on its data.  It is invalid
        if it does not contain a "validSince" item.  If it also has a 
        'validLifetime' item, the session is valid if the addition of the 
        two values represent a time in the future.  If there is a "validSince"
        but no 'validLifetime', then the session is valid.  
        @param lifetime    a from-start lifetime to allow that overrides
                              what was imposed when the session was 
                              started.  If None (default), the original 
                              lifetime is considered.  A negative value 
                              implies means consider the session expired.
        """
        life = self.getValidTimeLeft(lifetime)
        return (life is None) or (life > 0)

    def getValidTimeLeft(self, lifetime=None):
        """
        return the number of seconds the session will still be valid for.
        None is returned if the session is valid but without an end time.  
        A number <= 0 is returned if the session is not currently valid.
        A negative number indicates that the session has expired.  
        @param lifetime    a from-start lifetime to allow that overrides
                              what was imposed when the session was 
                              started.  If None (default), the original 
                              lifetime is considered.  A negative value 
                              implies means consider the session expired.
        """
        if not self.data.has_key('validSince'): 
            return 0
        if lifetime is None and self.data.has_key('validLifetime'): 
            lifetime = self.data['validLifetime']
        if lifetime is None:
            return None
        return (self.data['validSince'] + lifetime) - time.time()

    def validationNeeded(self):
        """
        return True if authentication on this session has been initiated
        but has not yet been validated.  
        """
        return self.data.has_key('_openid_consumer_last_token')

    def isNew(self):
        """
        return True if it appears that this session is new, meaning that
        either the user has not or never logged in.  False means that 
        the user has either logged in or that the session has recently 
        expired.
        """
        return not os.path.exists(self.file);


class NoAvailableCredError(AuthenticationError):
    """
    An exception indicating that a failure occured while attempting to 
    retrieve a remote credential.  It may mean that the cert availability
    timed out.  
    """
    def __init__(self, message=None, exc=None):
        if not message:
            if exc:
                if isinstance(exc, KeyError):
                    message = "Certificate not available: none was requested" 
                else:
                    message = "Trouble retrieving certificate: %s" % str(exc)
            else:
                message = "Failed to retrieve certificate"
        AuthenticationError.__init__(self, message)
        self.exception = exc

class BadOpenIdError(AuthenticationError):
    """
    An exception indicating a problem parsing or interpreting a user's OpenID
    URL.  This typically happens when a provider cannot be successfully 
    discovered from this URL.
    """
    def __init__(self, openid_url, message=None):
        if not message:
            message = "Can't interpret OpenId: %s" % openid_url
        AuthenticationError.__init__(self, message)

class NoOpenIdServiceError(BadOpenIdError):
    """
    An exception indicating that while the the OpenId is valid in form, 
    it does not resolve to a provider.
    """
    def __init__(self, openid_url, message=None):
        if not message:
            message = "Can't resolve OpenId to a provider: %s" % openid_url
        BadOpenIdError.__init__(self, message)

import pycurl
from openid.fetchers import CurlHTTPFetcher, setDefaultFetcher, USER_AGENT, MAX_RESPONSE_KB, HTTPResponse
import cStringIO

# curlsslprob = re.compile(r"(SSL)|(CA)|(certificate)")
curlsslprob = re.compile(r"(SSL)|( CA )|(verification failed)")
class CustomizedCurlFetcher(CurlHTTPFetcher):
    def __init__(self, cacertfiles=None, log=None):
        CurlHTTPFetcher.__init__(self)
        if cacertfiles is None:
            cacertfiles = []
        if type(cacertfiles) is not list:
            cacertfiles = [cacertfiles]
        self.cacertfiles = filter(lambda f: f and os.path.exists(f), 
                                  cacertfiles)
        self.log = log

    def fetch(self, url, body=None, headers=None):
        stop = int(time.time()) + self.ALLOWED_TIME
        off = self.ALLOWED_TIME

        if headers is None:
            headers = {}

        headers.setdefault('User-Agent',
                           "%s %s" % (USER_AGENT, pycurl.version,))

        header_list = []
        if headers is not None:
            for header_name, header_value in headers.iteritems():
                header_list.append('%s: %s' % (header_name, header_value))

        cacertfiles = self.cacertfiles[:]

        c = pycurl.Curl()
        try:
            c.setopt(pycurl.NOSIGNAL, 1)

            if header_list:
                c.setopt(pycurl.HTTPHEADER, header_list)

            # Presence of a body indicates that we should do a POST
            if body is not None:
                c.setopt(pycurl.POST, 1)
                c.setopt(pycurl.POSTFIELDS, body)

            while off > 0:
                if not self._checkURL(url):
                    raise HTTPError("Fetching URL not allowed: %r" % (url,))

                data = cStringIO.StringIO()
                def write_data(chunk):
                    if data.tell() > 1024*MAX_RESPONSE_KB:
                        return 0
                    else:
                        return data.write(chunk)
                    
                response_header_data = cStringIO.StringIO()
                c.setopt(pycurl.WRITEFUNCTION, write_data)
                c.setopt(pycurl.HEADERFUNCTION, response_header_data.write)
                c.setopt(pycurl.TIMEOUT, off)
                c.setopt(pycurl.URL, openid.urinorm.urinorm(url))

                if len(cacertfiles) > 0:
                    c.setopt(pycurl.CAINFO, cacertfiles[0])

                try:
                    c.perform()
                except:
                    ex_cl, ex = sys.exc_info()[:2]
                    if ex is None:  ex = ex_cl  # string exceptions
                    if curlsslprob.search(str(ex)):
                        if len(cacertfiles) > 0:
                            cacertfiles.pop(0)
                            off = stop - int(time.time())
                            continue
                        if self.log:
                            if self.cacertfiles and len(cacertfiles) == 0:
                                why = "After trying all CA cert bundles, %s, CA verification is still failing: %s" % (self.cacertfiles, str(ex))
                            else:
                                why = "CA verification appears to be failing: a CA cert for the provider may need to be installed."
                            print >> self.log, why
                    raise

                response_headers = self._parseHeaders(response_header_data)
                code = c.getinfo(pycurl.RESPONSE_CODE)
                if code in [301, 302, 303, 307]:
                    url = response_headers.get('location')
                    if url is None:
                        raise HTTPError(
                            'Redirect (%s) returned without a location' % code)

                    # Redirects are always GETs
                    c.setopt(pycurl.POST, 0)

                    # There is no way to reset POSTFIELDS to empty and
                    # reuse the connection, but we only use it once.
                else:
                    resp = HTTPResponse()
                    resp.headers = response_headers
                    resp.status = code
                    resp.final_url = url
                    resp.body = data.getvalue()
                    return resp

                off = stop - int(time.time())

            raise HTTPError("Timed out fetching: %r" % (url,))
        finally:
            c.close()

