#! /usr/local/bin/python2.5
#
import cgitb;  cgitb.enable()
import os, sys, urlparse

vaologin_conffile = "/appl/VO/regsso/conf/vaologin.cfg"
defwebpath = ""
defhome = "/appl/VO/regsso"

if os.environ.has_key("VOPUB_HOME"):
    defhome = os.environ["VOPUB_HOME"];
elif not defhome:
    defhome = "@INSTALLDIR@"
if defhome.startswith('@'): 
    defhome = os.path.dirname(os.path.dirname(sys.argv[0]))
if len(defhome) == 0:
    defhome = "."

defsyspath = [ os.path.join(defhome, "lib", "python") ]
sys.path.extend(defsyspath)

import vaologin.portal as vaologin
import vaologin.config as config
from VORegInABox.repository import configFileTmpl
from VORegInABox.nicedom import xpath
from VORegInABox import nicedom

def main():

    home = defhome
    if os.environ.has_key('VOPUB_HOME'):
        home = os.environ['VOPUB_HOME']

    reposroot = os.path.join(home, "data/webwrk")
    if os.environ.has_key('VOPUB_REPOSITORY'):
        reposroot = os.environ['VOPUB_REPOSITORY']

    conffile = vaologin_conffile
    if not os.path.exists(conffile):
        conffile = None
    conf = config.getConfig(conffile, fail=True)

    portal = MyPortal(conf, reposroot)
    portal.enter()

class MyPortal(vaologin.ProtectedPortal):

    def __init__(self, conf, reposroot):
        vaologin.ProtectedPortal.__init__(self, conf)
        self.reposroot = reposroot
        self.cgienv = None

    def deliver(self, path=None, asexec=True):
        self.cgienv = self.getCGIEnv(self.getSession())
        userFile = os.path.join(self.reposroot, 
                                configFileTmpl % self.cgienv['REMOTE_USER'])
        log("Looking for file: " + userFile)
        if not os.path.exists(userFile):
            # this is either a new user or we need to connect the OpenID user
            # to a previous old-style user
            log("Executing linkaccount.cgi")
            self.executeScript("/linkaccount.cgi")

        else:
            print >> sys.stderr, "Going to form..."
            if not path:
                path = os.environ.get('PATH_INFO')
            if path:
                self.executeScript(path)
            else:
                os.environ['QUERY_STRING'] = "ftype=List"
                self.executeScript("/Resource_Form.cgi")
            
        
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
        if not self.cgienv:
            return vaologin.ProtectedPortal.deliverFile(self, filename, docroot)

        if not docroot:
            docroot = self.docroot
        if not docroot or not os.path.isdir(docroot):
            if not filename:  filename = "index.html"
            return self.sendNotFound(filename)

        if not filename:
            filename = "index.htm"
            if not os.path.exists(os.path.join(docroot, filename)):
                filename = "index.html"

        path = os.path.join(docroot, filename)
        if not os.path.exists(path) or not os.path.isfile(path):
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
          try:
            fd = open(path, 'rb')
            data = fd.read(bufsz)
            while data:
                tmpl = Template(data)
                self.ostrm.write(data.safe_substitute(self.cgienv))
                data = fd.read(bufsz)

          except Exception, ex:
            print >> self.estrm, "Problem sending file data:", str(ex)
        finally:
            if fd:  fd.close()
            # self.ostrm.close()


    def getCGIEnv(self, sess):
        out = vaologin.ProtectedPortal.getCGIEnv(self, sess)

        # map the OpenID user to a previous user by updating REMOTE_USER
        luser = self.findLocalUser(out)
        if luser:
            out['REMOTE_USER'] = luser

        return out

    def getOpenIDUser(self, cgienv):
        vaouser = cgienv['VAOLOGIN_USERNAME']
        if not vaouser:
            openid = cgienv['OPENID_CLAIMED']
            if not openid:
                openid = cgienv['OPENID_IDENTITY']
            schre = re.compile(r'https?://', re.IGNORECASE)
            openid = schre.sub('', openid)
            vaouser = openid.translate('/','_')

        elif vaouser.find("@") < 0:
            # qualify the name according to the OpenID provider
            authority = urlparse.urlsplit(cgienv['OPENID_IDENTITY']).netloc
            authority = authority.split(':')[0]
            flds = authority.split('.')
            if flds[0] == 'www' or flds[0] == 'sso':
                flds.pop(0)
            authority = ".".join(flds)
            vaouser = "%s@%s" % (vaouser, authority)

        return vaouser

    def getLocalName(self, vaouser):
        """
        return the local name for the user given their VAO-OpenID name
        @param vaouser    the "friendly" user name for the user
        """
        configFile = os.path.join(self.reposroot, configFileTmpl % vaouser)
        if not os.path.exists(configFile):
            return vaouser

        cfg = open(configFile, 'r')
        doc = nicedom.parse(cfg)
        cfg.close()
        luser = xpath.thevalue(doc, "Config/Username")
        if not luser: luser = vaouser

        return luser

    def findLocalUser(self, cgienv):
        return self.getLocalName(self.getOpenIDUser(cgienv))

def log(msg):
    sys.stderr.write(msg)
    sys.stderr.flush()
    
if __name__ == "__main__":
    main()
