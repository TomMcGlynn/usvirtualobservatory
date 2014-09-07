#! /usr/bin/env python
#
from __future__ import with_statement
import cgi, os, os.path, sys, ConfigParser, Cookie, time, urlparse, pickle, re
import time, math, urllib
from subprocess import Popen
from vaologin.utils import CGIError, ensureDir
from vaologin.authenticate import VAOLoginRequest, Attributes
import vaologin.config as conf
# import cgitb;  cgitb.enable()

maxContentLength = 0  # 0 => unlimited

def main():
    conf = getConfig(None, {"finn": "mccool"}, True)
    for p in conf.iteritems():
        print p[0], "=", p[1]

class ProtectedPortal(object):
    """
    a class representing access to a part of a portal requiring authentication.
    It assumes that it will be used in the context of a CGI script.  It 
    will accept a request for a protected document (or other cgi script call)
    via enter().  If authentication is needed, the user's browser will 
    be redirected accordingly.  
    """

    def __init__(self, config, queryargs=None, sessionId=None, asServer=False,
                 ostrm=sys.stdout, estrm=sys.stderr, reqhandler=None, 
                 execScripts=True):
        """
        create the request handler
        @param config      the VAOLogin configuration dictionary
        @param queryargs   a dictionary containing the query data encoded as
                              name=value pairs after the ? in the URL.
                              If None, these will be extracted from the 
                              CGI environment.
        @param sessionId   a session Id as obtained by the web service 
                              framework.  If None (usually), an attempt to 
                              retrieve it via a cookie will be done.
        @param asServer    if true, do not assume that this class is operating
                              as a CGI script but within a dedicated server. 
                              HTTP responses will then include full headers.
        """
        self.config = config
        self.asServer = asServer
        self.ostrm = ostrm
        self.estrm = estrm
        self.instream = None

        # we will require username to alway be passed 
        key = "vaologin.auth.requiredattributes"
        reqatts = self.config.get(key)
        self.config[key] = conf.mergeIntoList(reqatts, "username")

        self.qargs = queryargs
        if self.qargs is None: 
            self.qargs = self._getQueryArgs()

        # update forceAuthentication
        attPaths = conf.splitList(
            self._getConfigParam("pathsneedingattributes"))
        attPaths.extend(conf.splitList(
                self._getConfigParam("pathsneedingcert")))
        if attPaths:
            key = "vaologin.portal.forceAuthentication".lower()
            forcePaths = self.config.get(key)
            self.config[key] = conf.mergeIntoList(forcePaths, attPaths)

        self.sessionid = sessionId
        self.cookie = None
        if not self.sessionid:
            self.sessionid = self._loadSessionIdFromCookie() # sets self.cookie
        if not reqhandler:
            reqhandler = VAOLoginRequest(self.config, self.sessionid, estrm)
        self.request = reqhandler
        if not self.sessionid:
            self.sessionid = self.request.sessionid

        self.docroot = self._getConfigParam("documentroot", None)
        self.docalias = self._getConfigParam("docalias", None)
        if self.docalias:
            if not self.docalias.startswith('/'):
                self.docalias = "/%s" % self.docalias
        self.scriptroot = self._getConfigParam("scriptroot", None)
        self.scriptalias = self._getConfigParam("scriptalias", None)
        if self.scriptalias:
            if not self.scriptalias.startswith('/'):
                self.scriptalias = "/%s" % self.scriptalias

        if self.docroot is None or \
                (self.docroot and not os.path.isdir(self.docroot)):
            self.docalias = None
            print >> self.estrm, \
                "VAOLogin: WARNING: vaologin.portal.documentRoot", \
                "not set to an existing directory"
        if self.scriptroot is None or \
                (self.scriptroot and not os.path.isdir(self.scriptroot)):
            self.scriptalias = None
            print >> self.estrm, \
                "VAOLogin: WARNING: vaologin.portal.scriptroot", \
                "not set to an existing directory"

        self.loginalias = self._getConfigParam("loginalias", None)
        self.loginForwardUrl = self._getConfigParam("loginforwardurl", None)
        self.logoutalias = self._getConfigParam("logoutalias", None)
        self.logoutForwardUrl = self._getConfigParam("logoutforwardurl", None)

        self.mimetypes = None
        self.asexec = execScripts
        self.handleHeaderAsServer = self._getConfigParam("httpServerId",
                                                    "VAOLogin Portal/0.0")

        self.realm = self._getConfigParam("cookierealm")
        if not self.realm:
            self.realm = self.config.get("vaologin.auth.portalurlrealm")
        if self.realm:
            self.realm = urlparse.urlsplit(self.realm)[2]
            if len(self.realm) == 0:  self.realm = "/"

    def _getQueryArgs(self):
        meth = os.environ.get('REQUEST_METHOD', 'GET')
        if meth == 'POST':
            # essentially turn this into a GET
            
            pass
        else:  # assume GET
            if os.environ.has_key('QUERY_STRING'):
                # note: an attribute value could be empty
                qargs = cgi.parse_qs(os.environ['QUERY_STRING'],
                                     keep_blank_values=True)
                for key in qargs.keys():
                    if len(qargs[key]) == 1:
                        qargs[key] = qargs[key][0]
                return qargs
        return {}

    _configPrefix = "vaologin.portal."
    def _getConfigParam(self, name, default=None):
        """
        return a configuration parameter assuming a prefix of vaologin.portal
        """
        return self.config.get(self._configPrefix + name.lower(), default)

    def _loadSessionIdFromCookie(self):
        cookie = getSessionCookie()
        if not cookie:  return None
        return cookie["session"].value

    def _selectAtts(self, urlpath):
        # select the attributes to request based on the configuration
        out = [];

        # need cert?
        pathsNeeding = self._getConfigParam("pathsneedingcert")
        if self._pathMatches(urlpath, pathsNeeding):
            out = [Attributes.CERT]

        # other requestable atts?
        atts = resolveAtts(self._getConfigParam("requestattributes"))
        pathsNeeding = self._getConfigParam("pathsneedingattributes")

        if atts and \
           (not pathsNeeding or self._pathMatches(urlpath, pathsNeeding)):
            out.extend(atts)
        if len(out) == 0:  out = None
        return out

    def _matchPath(self, urlpath, pathbases):
        if urlpath is None:  return None
        out = None
        for path in conf.splitList(pathbases):
            if urlpath.startswith(path): 
                p = ""
                # make sure we're matching whole path components
                if path.endswith("/"):
                    p = urlpath[len(path)-1:]
                else:
                    p = urlpath[len(path):]
                if len(p) == 0 or p[0] == '/':
                    out = path
                    break
        return out

    def _pathMatches(self, path, pathbases):
        print >> self.estrm, "Testing %s against %s" % (path, str(pathbases))
        return self._matchPath(path, pathbases) is not None


    def getSessionId(self):
        """
        return the session token value in use for this session
        """
        return self.sessionid

    def getSession(self):
        """
        return the session object instance representing the state of this 
        session.
        """
        return self.request.getSession()

    def enter(self, path=None):
        """
        attempt to return a protected document inside the protected portal.  
        If the session is valid (i.e. the user is already authenticated),
        then the requested path will be sent to the web client.  If the 
        session is invalid (i.e. the user has not authenticated or the 
        session has expired), he/she will be redirected to login at the 
        login server.  
        @param path   the requested path.  This may contain URL arguments
                        (i.e. trailing "?...").  If None, the path will 
                        formed from the CGI environment.  
        """
        if self.validationNeeded():
            authResp = self.validate()
            if not authResp.isAuthenticated():
                print >> self.estrm, "Validation failed: %s" % authResp.getWhy()
                exitpage = self._getConfigParam("exitpage",'/')
                self.localRedirect(exitpage, False)
                return
        else:
            if path is None:
                path = self._getInternalPath()
            if self._pathMatches(path, 
                                 self._getConfigParam("forceAuthentication")):
                print >> self.estrm, "Forcing renewal of session for %s" % path
                self.endSession();
                if self.sessionValid():
                    print >> self.estrm, "Failed to invalidate session"

        if self.sessionValid():
            self.deliver(path, self.asexec)
        elif self.logoutalias and path.startswith(self.logoutalias):
            path = self.logoutalias;
            if not path: path = "/";
            self.localRedirect(path);
        else:
            atts = self._selectAtts(path)
            self.authenticateFor(path, atts)

    def login(self, forwardUrl=None, setcookie=True):
        """
        forward the user to an arbitrary post-login page.  Note that calling
        this function assumes that the user is already authenticated.
        @param forwardUrl   the URL to redirect to.  This can either be an
                             an absolute URL or a relative one.  If it starts
                             with a '/', it is taken as a relative URL with
                             respect to the server root.  
        @param setcookie    if True (default), set the session cookie.
        """
        if not forwardUrl:
            forwardUrl = self.loginForwardUrl
        if not forwardUrl:
            forwardUrl = "/";
        if re.match(r'^\w+:/', forwardUrl):
            self.redirect(forwardUrl, setcookie)
        else:
            self.localRedirect(forwardUrl, setcookie)

    def logout(self, forwardUrl=None):
        """
        log the user out of this portal and forward the user to an 
        arbitrary post-login page.  This does not end the user's overall 
        VO session.  Note that calling this function assumes that the user 
        is already authenticated.
        @param forwardUrl   the URL to redirect to.  This can either be an
                             an absolute URL or a relative one.  If it starts
                             with a '/', it is taken as a relative URL with
                             respect to the server root.  
        """
        if not forwardUrl:
            forwardUrl = self.logoutForwardUrl
        if not forwardUrl:
            forwardUrl = "/"

        # end the session
        sess = self.getSession()
        if sess:
            print >> self.estrm, \
              "Ending session for username=%s" % sess.data.get('username','???')
            sess.end();

        if re.match(r'^\w+:/', forwardUrl):
            self.redirect(forwardUrl, False)
        else:
            self.localRedirect(forwardUrl, False)

    def _formRequestedPath(self, path=None, withQueryString=False):
        if not path:
            if os.environ.has_key('PATH_INFO'):
                path = os.environ['PATH_INFO']
            else:
                path = self._getConfigParam("defaultDocument", "/index.html");
        if not path.startswith('/'): path = "/%s" % path

        if withQueryString and \
           (not self.scriptalias or path.startswith(self.scriptalias)) and \
           os.environ.get('QUERY_STRING','') != '':
            path = "%s?%s" % (path, qstring)
        
        return path

    def validationNeeded(self):
        """
        return true if this is a redirect after authentication at the 
        OpenID server and we need to validate that the authentication 
        was successful.  
        """
        return self.request.getSession().validationNeeded() and \
               self.qargs.get('openid.mode')

    def sessionValid(self):
        """
        return true if an authenticated (validated) session has been 
        established and has not expired.  
        """
        return self.request.getSession().isValid()

    def sessionNew(self):
        """
        return True if it appears that the user session is new, meaning that
        either the user has not or never logged in.  False means that 
        the user has either logged in or that the session has recently 
        expired.
        """
        return self.request.getSession().isNew();

    def endSession(self):
        """
        destroy the session
        """
        sess = self.getSession()
        if sess:
            sess.end()

    def _formURLserver(self):
        protoc = self._getConfigParam("protocolScheme", "http")

        server = os.environ['SERVER_NAME']
        if os.environ.has_key('SERVER_PORT') and     \
           os.environ['SERVER_PORT'] is not None and \
           os.environ['SERVER_PORT'] != '80':
            server += ":%s" % os.environ['SERVER_PORT']

        return "%s://%s" % (protoc, server)

    def _formURLbase(self, basepath=None):
        server = self._formURLserver()

        if not basepath:
            if os.environ.has_key('SCRIPT_NAME'):
                basepath = os.environ.get('SCRIPT_NAME', '')
            if basepath and not basepath.startswith('/'):
                basepath = "/%s" % basepath

        return server + basepath

    def authenticateFor(self, path=None, atts=None, basepath=None):
        """
        request authentication to access the given path
        @param path   the desired document as a path relative to the 
                        document root.  
        @param atts   a list of the attributes to be returned.  The 
                        attributes must be specified by their full 
                        URIs, but one can use the (python) attributes
                        of the Attributes class (e.g. Attributes.EMAIL).
                        If None, no attributes will be requested.
        """
        if not path:
            path = self._formRequestedPath()

        # If the intended destination is a script, cache the inputs for our
        #   for keeping during our trip to the login server
        self._cacheInputs()

        return_url = self._formURLbase(basepath) + path

        redirect = self.request.requestAuthentication(return_url, atts)
        self.cookie = self.createCookie(self.sessionid, self.realm)
        print >> self.estrm, \
            "Requesting authentication for session=%s" % self.sessionid
        self.redirect(redirect, True)

    def _getInternalPath(self):
        path = "/"
        if os.environ.has_key('PATH_INFO'):
            path = os.environ['PATH_INFO']
        if not path.startswith('/'):
            path = '/' + path
        return path


    def validate(self, lifehours=None):
        """
        complete the authentication process by validating the user via 
        the CGI arguments.
        @param lifehours    the desired time limit for the session in hours.  
                              If negative, then there is no limit to the 
                              session lifetime.  If None (default), the limit 
                              will be taken from the configuration file.
        """
        return_url = self._formURLbase()
        path = self._getInternalPath()
        return_url += path

        if lifehours is None:
            lifehours = self._getConfigParam("sessionlifehours", None)
            if isinstance(lifehours, str):
                try:
                    lifehours = float(lifehours)
                except ValueError, ex:
                    if self.estrm:
                        print >> self.estrm, "Ignoring bad floating-point", \
                            "format for vaologin.portal.sessionLifehours:",    \
                            lifehours

        resp = self.request.processAuthentication(self.qargs, return_url,
                                                  lifehours)
        if resp.isAuthenticated():
            msg = "Session %s authenticated" % self.sessionid
            atts = resp.getAttributes()
            if atts and atts.has_key(Attributes.USERNAME):
                msg += "for user=%s" % atts[Attributes.USERNAME]
            print >> self.estrm, msg

            if resp.credentialAvailable():
                certfile = resp.cacheCredential()
                # update the cached session with the proper cert file path
                if lifehours:
                    sess = resp.getSession()
                    sess.setProperty("cert", certfile)
                    sess.save()

            # reconstitute the CGI environment if necessary
            inputenv = self._findCGImetadata()
            if inputenv:
                self._restoreCachedInput(inputenv)

        elif resp.wasCanceled():
            print >> self.estrm, \
                "Authentication canceled for session=%s" % self.sessionid
        else:
            print >> self.estrm, \
                "Authentication failed for session=%s" % self.sessionid
        return resp

    def deliver(self, path=None, asexec=True):
        """
        deliver the requested path, either as a static file or via a CGI 
        script.  

        Which of these the path is interpreted as depends on the values of 
        the docAlias and scriptAlias configuration parameters.  If an alias
        is set, either docAlias and scriptAlias, and the path begins with 
        the path begins with the path given as the alias's value, then the 
        path will be handled as either a static file or script, respectively.
        The alias path will be removed from the requested path, and a file
        or script will be located in the appropriate location having the 
        amended path.  

        If the one of the aliases is not set and the path does not 
        match the other, then the path will be handled as a path of the 
        type corresponding to the unset alias.  That is, if scriptAlias is 
        not set and the path does not match the docAlias, then the 
        path will be assumed to refer to a script under the script root. 

        If neither alias is set, the path will always be interpreted as 
        referring to a static file.  

        Note that this will not list directories.  Mime types for static
        files are handled by file extension only.  
        """
        urlpath = path
        if not urlpath:
            urlpath = self._getInternalPath()
            if urlpath == "/":
                urlpath = self._getConfigParam("defaultDocument","/index.html");
        path = urlpath
        if path.startswith('/'):
            path = path[1:]

        isdoc = True
        if self.loginalias and urlpath.startswith(self.loginalias):
            # special login path
            self.login()
            return
        elif self.logoutalias and urlpath.startswith(self.logoutalias):
            # special login path
            self.logout()
            return
        elif self.scriptalias and urlpath.startswith(self.scriptalias):
            # need to execute a cgi script
            path = urlpath[len(self.scriptalias):]
            isdoc = False

        elif self.docalias and urlpath.startswith(self.docalias):
            # this is a static document
            path = urlpath[len(self.docalias):]

        elif self.docalias and not self.scriptalias:
            # by default, this is a script
            isdoc = False

        if os.sep != '/':
            path = os.path.join(*path.split('/'))

        if isdoc:
            self.deliverFile(path, self.docroot)
        else:
            self.executeScript(path, self.scriptroot, asexec)

    def executeScript(self, script, scriptroot=None, asexec=True):
        """
        execute the script given by the script filename.  If the file is 
        not found relative to the script
        @param script      the path to the script relative to the 
                             directory containing protected scripts
        @param scriptroot  the directory containing protected scripts.  If 
                             None (default), the configured directory will 
                             used
        @param asexec      if True (default), "exec" the script, replacing 
                             the current process with the new script process
                             If False (usually used in a testing context),
                             run as a subprocess.  
        """
        if self.instream:
            asexec = False
        if not scriptroot:
            scriptroot = self.scriptroot
        if not scriptroot or not os.path.isdir(scriptroot):
            return self.sendNotFound(script)

        script = script.strip(os.sep)
        path = os.path.join(scriptroot, script)
        pathinfo = ''
        if os.path.isdir(path):
            print >> self.estrm, \
                "vaologin: Attempted to execute a directory: " + path
            return self.sendNotFound(script)
                
        while path and not os.path.exists(path):
            if not pathinfo:
                pathinfo = "/" + os.path.basename(path)
            else:
                pathinfo = "/" + os.path.basename(path) + pathinfo
                                          
            path = os.path.dirname(path)
        if not path or not os.path.isfile(path):
            print >> self.estrm, "vaologin: script not found: " + path
            return self.sendNotFound("/"+script)

        env = os.environ.copy()
        env['PATH_INFO'] = pathinfo
        scriptname = os.environ['SCRIPT_NAME']
        if self.scriptalias:
            scriptname += self.scriptalias.rstrip('/')
        scriptname = "%s/%s" % (scriptname, script)
        env['SCRIPT_NAME'] = scriptname

        # load the environment with what we know
        cgienv = self.getCGIEnv(self.request.getSession())
        for key in cgienv.keys():
            env[key] = cgienv[key]

        self._sendheader()
        if asexec:
            print >> self.estrm, "executing %s via exec" % path
            os.execle(path, env)
        else:
            print >> self.estrm, "executing %s via pipe" % path
            sessiondir = self.config.get('vaologin.auth.sessionsdir', '/tmp')
            instrm = self.instream or sys.stdin
            outfile = os.path.join(sessiondir, self.sessionid+'.out')
            errfile = os.path.join(sessiondir, self.sessionid+'.err')
            with open(outfile,'w') as out:
                with open(errfile,'w') as err:
                    child = Popen(path, stdout=out, stderr=err, stdin=instrm, 
                                  env=env)
                    child.wait()
            with open(outfile) as out:
                for line in out:
                    self.ostrm.write(line)
            os.remove(outfile)
            os.remove(errfile)
            if self.instream:
                self.instream.close()
                os.remove(self.instream.name)
                self.instream = None
            

    def getCGIEnv(self, sess):
        """
        return a dictionary containing modifications that should be made 
        to the CGI environment for a given authenticated session. 
        """
        env = {}
        env["VAOLOGIN_SESSION_ID"] = sess.getId()
        if sess.data.has_key('validLifetime'):
            env["OPENID_IDENTITY"] = sess.data.get('openid.identity','')
            env["OPENID_CLAIMED_ID"] = sess.data.get('openid.claimed_id','')
            env["VAOLOGIN_VALID_SINCE"] = str(sess.data.get('validSince',0))
            env["VAOLOGIN_VALID_LIFETIME"] = \
                str(sess.data.get('validLifetime',0))
            env["VAOLOGIN_USERNAME"] = sess.data.get('username','')
            if env["VAOLOGIN_USERNAME"]:
                env["AUTH_TYPE"] = "OpenID"
                env["REMOTE_USER"] = env["VAOLOGIN_USERNAME"]
            if sess.data.has_key('cert'):
                env["VAOLOGIN_USER_CERTIFICATE"] = sess.data['cert']

            # encode the attributes
            skip = "id username openid.identity openid.claimed_id validSince validLifetime cert".split()
            exp = {}
            for key in sess.data.keys():
                if key in skip:  
                    continue
                exp[key] = sess.data[key]
            env["VAOLOGIN_ATTRIBUTES"] = urllib.urlencode(exp)

        return env

    def deliverFile(self, filename=None, docroot=None):
        """
        deliver a static file to the user.  Note that this will 
        not list directories.  Mime types for static files are handled 
        by file extension only.  
        @param filename   the path relative to the document root of the 
                            desired file.  If empty or not provided, index.html
                            will be returned.  
        @param docRoot    the full path to the document root directory where 
                            static files to serve are located.  If None 
                            (default), the configured document root will be 
                            assumed.
        """
        if not docroot:
            docroot = self.docroot
        if not docroot or not os.path.isdir(docroot):
            if not filename:  filename = "index.html"
            return self.sendNotFound(filename)

        if not filename:
            filename = "index.htm"
            if not os.path.exists(os.path.join(docroot, filename)):
                filename = "index.html"
        filename = filename.strip(os.sep)

        path = os.path.join(docroot, filename)
        if not os.path.exists(path) or not os.path.isfile(path):
            print >> self.estrm, "vaologin: doc not found: " + path
            return self.sendNotFound(filename)

        defbufsz = 1024000
        try:
            bufsz = int(self._getConfigParam("bufsize", defbufsz))
        except TypeError, ex:
            bufsz = defbufsz

        ext = os.path.splitext(filename)[1]
        if ext and ext.startswith('.'):
            ext = ext[1:]
        mimetype = self.getMimeTypeFor(ext)
        self._sendheader()
        self.ostrm.write("Content-Type: %s\r\n\r\n" % mimetype)

        fd = None
        try:
            fd = open(path, 'rb')
            data = fd.read(bufsz)
            while data:
                self.ostrm.write(data)
                data = fd.read(bufsz)

        except Exception, ex:
            print >> self.estrm, "Problem sending file data: " + str(ex)
        finally:
            if fd:  fd.close()
            # self.ostrm.close()

    def getMimeTypeFor(self, exten):
        if self.mimetypes is None:
            self.mimetypes = {}

            # parse some mapping from the config file
            mapdata = self._getConfigParam("mimetypemap")
            if mapdata:
                mappings = map(lambda m: m.strip(), mapdata.split(','))
                for mapping in mappings:
                    words = mapping.split()
                    if len(words) < 2:
                        continue
                    for ext in words[1:]:
                        self.mimetypes[ext] = words[0]
            if not self.mimetypes.has_key('html'):
                self.mimetypes['html'] = "text/html"
            if not self.mimetypes.has_key('htm'):
                self.mimetypes['htm'] = "text/html"


        if self.mimetypes.has_key(exten):
            return self.mimetypes[exten]

        defmimetype = "application/x-unknown"
        mimefile = self._getConfigParam("mimetypemap")
        if mimefile:
            if not os.path.exists(mimefile):
                print >> sys.err, "Mime types file not found", \
                    "(vaologin.portal.mimeTypeMap):", mimefile
                return defmimetype

            try:
                with open(mimefile) as f:
                    for line in f:
                        line = line.trim()
                        if line.startswith('#'):
                            continue
                        words = line.split()
                        if len(words) < 2:
                            continue
                        if exten in words[1:]:
                            # found a matched extension (cache mappings)
                            for ext in words[1:]:
                                self.mimetypes[ext] = word[0]
                            return words[0]
            except:
                pass

        return defmimetype

    def sendNotFound(self, path):
        """
        redirect in a way that tells the user that the request cannot be found.
        @param path   the path to the file (or script) requested
        """
        prefix = self._getConfigParam("usenotfoundpath", "notfound").strip('/')
        if not path.startswith("/"): 
            path = "/%s" % path
        notfound = "/%s%s" % (prefix, path)
        self.localRedirect(notfound)

    def localRedirect(self, path, setcookie=False):
        """
        redirect the user's browser to a new URL on this server.  No 
        explicit authentication is done (as this path may point to a 
        public URL) apart from any that is done after the redirect. 
        @param path   the URL path to append to the base URL.  
        """
        server = os.environ['SERVER_NAME']
        if os.environ.has_key('SERVER_PORT') and     \
           os.environ['SERVER_PORT'] is not None and \
           os.environ['SERVER_PORT'] != '80':
            server += ":%s" % os.environ['SERVER_PORT']

        if not path.startswith('/'):
            path = "/%s" % path

        self.redirect("http://%s%s" % (server, path), setcookie)

    def redirect(self, url, sendcookie=False):
        """
        redirect the user's browser to an arbitrary URL anywhere on the web.  
        No explicit authentication is done.
        @param url   the URL to redirect to.  
        """
        self._sendheader(sendcookie, 302, "Found");
        self.ostrm.write("Location: %s\r\n\r\n" % url)
        self.ostrm.flush()


    weekdayname = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    monthname   = [None,
                   'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    def _sendheader(self, setcookie=False, code=200, message="OK"):

        if self.asServer:
            (year, month, day, hh, mm, ss, wd, y, z) = time.gmtime(time.time())
            d = "%s, %02d %3s %4d %02d:%02d:%02d GMT" % (self.weekdayname[wd],
                                                         day, 
                                                         self.monthname[month], 
                                                         year, hh, mm, ss)

            self.ostrm.write("HTTP/1.0 %d %s\r\n" % (code, message))
            self.ostrm.write("Server: %s\r\n" % self.handleHeaderAsServer)
            self.ostrm.write("Date: %s\r\n" % d)

        if setcookie and self.cookie:
            self.ostrm.write(self.cookie)
            self.ostrm.write("\r\n")

    def createCookie(self, sessionid, realmPath=None):
        """
        set a session cookie that marks the start of a user session
        @param sessionid  a unique session identifier
        @param user       the login name of the user.  Default is an empty string.
        @param realmPath  the URL path that this cookie should be restricted to.
                            if None (default) or empty, this realm will not 
                            be set in the cookie.
        @return str   a stirng containing the encoded cookie data 
        """
        cookie = Cookie.SimpleCookie()
        cookie["session"] = sessionid
        if realmPath:
            cookie["session"]["Path"] = realmPath
        return str(cookie)

    def _cacheInputs(self, instrm=None):
        if not instrm:
            instrm = sys.stdin
        docache = os.environ.has_key('QUERY_STRING') or \
                  os.environ.get('REQUEST_METHOD','') == 'POST'
        env = {}

        sessiondir = self.config.get('vaologin.auth.sessionsdir', '/tmp')
        inputfile = os.path.join(sessiondir, "%s.input" % self.sessionid)
        clen = 0
        try:
            clen = int(os.environ.get('CONTENT_LENGTH', '0'))
        except ValueError:
            pass
        if clen or os.environ.get('REQUEST_METHOD', 'GET') != 'GET':
            nread = 0
            out = open(inputfile, 'w+b')
            try:

                bufsz = 1<<16
                data = instrm.read(bufsz)
                nread += len(data)
                if nread == 0:
                    out.close()
                    out = None
                    os.remove(inputfile)
                    data = None
                else:
                    out.write(data)
                    docache = True
                    env['inputFile'] = inputfile

                while data:
                    data = instrm.read(bufsz)
                    if len(data) > 0:
                        nread += len(data)
                        out.write(data)

            finally:
                if out:  out.close()

        # Now cache critical data
        if docache:
            _copyItem('CONTENT_LENGTH', os.environ, env)
            _copyItem('CONTENT_TYPE', os.environ, env)
            _copyItem('REQUEST_METHOD', os.environ, env)
            _copyItem('QUERY_STRING', os.environ, env)

            datafile = os.path.join(sessiondir,"%s.env" % self.sessionid)
            with open(datafile,'w') as fd:
                pickle.dump(env, fd)

    def _findCGImetadata(self):
        sessiondir = self.config.get('vaologin.auth.sessionsdir', '/tmp')
        datafile = os.path.join(sessiondir,"%s.env" % self.sessionid)
        if not os.path.exists(datafile):
            return None
        with open(datafile) as fd:
            out = pickle.load(fd)
        os.remove(datafile)
        return out

    def _restoreCachedInput(self, env):
        cgivarre = re.compile(r'[A-Z_]+')
        for key in env.keys():
            if cgivarre.match(key):
                os.environ[key] = env[key]

        if env.has_key('inputFile'):
            self.instream = open(env['inputFile'])

    def __del__(self):
        if self.instream:
            self.instream.close()
            if os.path.exists(self.instream.name):
                os.remove(self.instream.name)

def getSessionCookie(cookiestr=None):
    """
    return a Cookie instance give a string value in the format expected 
    in the HTTP query header, HTTP_COOKIE.  If a value is not provided,
    one will be looked for in the environment.
    None will be returned if the cookie does not appear to be a session
    cookie.
    """
    if cookiestr is None:
        cookiestr = os.environ.get('HTTP_COOKIE', '')
    if not cookiestr:
        return None
    cookie = Cookie.SimpleCookie(os.environ['HTTP_COOKIE'])
    if cookie is None or not cookie.has_key("session"):
        return None
    return cookie

def cookieToSessionId(cookie):
    if cookie is None or not cookie.has_key("session"):
        return None
    return cookie["session"].value
                
class LoginStatus(object):
    """
    an object that can provide login status information in relation 
    to a particular portal.
    """

    NONE = 0
    ACTIVE = 1
    EXPIRED = 2

    stateNames = [ "out", "in", "ex" ];

    def __init__(self, session):
        """
        initialize this instance for a portal session
        @param session   a Session object for the user
        """
        self.sess = session

    @staticmethod
    def fromPortal(portal):
        """
        create an instance from a current portal session
        @param portal    the ProtectedPortal instance representing the portal
        """
        return LoginStatus(portal.getSession())

    @staticmethod
    def fromSessionId(sessId, conf):
        """
        create an instance by creating a default portal instance given 
        a sessionId
        @param sessid   the session id; if None, a new one will be created.
        @param conf     a configuration file defining the portal
        """
        if not sessId:
            sessId = cookieToSessionId(getSessionCookie())
        if sessId:
            portal = ProtectedPortal(conf, None, sessId)
            return LoginStatus.fromPortal(portal)

        return LoginStatus(None)

    def getState(self):
        """
        return the login status as one of the class enumerations, NONE,
        ACTIVE, or EXPIRED.
        """
        if self.isLoggedOut(): 
            return self.NONE
        elif self.isActive():
            return self.ACTIVE
        else:
            return self.EXPIRED

    def getUsername(self):
        """
        return the username associated with the session, or None, if it 
        is not known (because there is no session). 
        """
        if self.sess is None: return None
        return self.sess.data.get('username')

    def getOpenId(self):
        """
        return the OpenID for the logged in user or None, if there is no
        active session.
        """
        if self.sess is None: return None
        return self.sess.data.get('openid.identity')
        

    def isLoggedOut(self):
        """
        return True if the session does not exist, indicating that the user
        logged out or otherwise has never logged in.
        """
        return self.sess is None or self.sess.isNew()

    def isActive(self):
        """
        return True if the session is valid, indicating the user is still 
        logged in.
        """
        return self.sess is not None and self.sess.isValid()

    def isInactive(self):
        """
        return True if the session is not valid, indicating the user is either
        logged out or the session has expired.
        """
        return self.sess is None or not self.sess.isValid()

    def isExpired(self):
        """
        return true if the session has expired.  False will be returned if 
        the user explicitly logged out.
        """
        return self.sess is not None and not self.sess.isValid()
        
    def getTimeLeftSec(self):
        """
        return the time left in the session in whole seconds
        """
        if self.sess is None: return 0
        since = self.sess.data.get('validSince')
        if not since:  return 0

        sofar = time.time() - since
        if sofar < 0: return 0
        out = self.sess.data.get('validLifetime', 0) - sofar
        if out < 0: out = 0
        return out

    def getTimeLeftMin(self):
        """
        return the time left in the session in decimal minutes
        """
        return self.getTimeLeftSec() / 60.0;

    @staticmethod
    def formatTimeLeft(leftMin):
        """
        return the time left formatted as a string of the form, HHH:MM
        @param min   the decimal minutes left in the session as returned 
                     by getTimeLeftMin().
        """
        hours = math.floor(leftMin / 60.0)
        mins = math.floor(leftMin - hours*60.0)
        return "%i:%02d" % (hours, mins)

    def toJSON(self):
        user = "null"
        openid = "null"
        left = 0
        state = self.stateNames[self.NONE]

        if self.sess is not None:
            left = self.getTimeLeftMin()
            user = self.getUsername()
            openid = self.getOpenId()
            state = self.stateNames[self.getState()]

        out = '{ "state": "%s", "username": "%s", "openid": "%s", "minLeft": %d, "dispLeft": "%s" }'
        return out % (state, user, openid, left, 
                      LoginStatus.formatTimeLeft(left))

def resolveAtts(attlist):
    """
    convert attribute short names to URIs
    """
    if attlist is None: return None
    if not isinstance(attlist, list):
        attlist = conf.splitList(attlist)

    uriname = {}
    for k in Attributes.shortname.keys():
        uriname[Attributes.shortname[k]] = k

    out = []
    for att in attlist:
        if uriname.has_key(att):
            out.append(uriname[att])

    return out

        

def _copyItem(name, src, dest):
    if src.has_key(name):
        dest[name] = src[name]

if __name__ == "__main__":
    main()
