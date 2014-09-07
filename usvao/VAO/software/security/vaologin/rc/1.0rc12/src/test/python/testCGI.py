#!/usr/bin/env python
"""
Tests of the cgi module

These tests require a test server to be running and the network available.
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, unittest, time, urlparse, cgi, re, urllib
from cStringIO import StringIO

from vaologin import config
from vaologin.authenticate import AuthenticationResponse, Session, Attributes
from vaologin.authenticate import FailedAuthenticationResponse, VAOLoginRequest
from vaologin.utils import ensureDir
from vaologin.portal import ProtectedPortal

from openid.consumer import consumer

def silentlog(message, level=0):  pass
from openid import oidutil
oidutil.log = silentlog    # for more messages, comment out this line
stderr=None
# stderr=sys.stderr        # for more messages, uncomment this line

testdir = os.path.join(os.getcwd(), 'build', 'tmp')
statedir = os.path.join(testdir, 'vaologin')
conffile = os.path.join(os.getcwd(), *'src/test/python/test.cfg'.split('/'))
return_url = "http://example.org/protected/welcome.html"
provider_server = "wire.ncsa.uiuc.edu" 
provider_url = "https://%s/openid/provider" % provider_server
signin_url = "https://%s/openid/signin" % provider_server
provideropenid = "https://%s/openid/provider_id" % provider_server
useropernidbase = "https://%s/openid/id/" % provider_server

testusername = "unittest"
testpassword = "success"

class TestLoginRequest(VAOLoginRequest):
    """
    This class allows for the testing of the cgi layer without actually 
    needing the network.
    """

    def __init__(self, config, sessionId=None, errlog=None):
        """
        create a test reqest handler
        """
        self.config = config
        self.log = errlog
        self.sessionsDir = self.config.get("vaologin.cli.sessionsdir")
        if not self.sessionsDir:
            sdir = self.config.get("vaologin.cli.statedir", statedir)
            self.sessionsDir = os.path.join(sdir, "VAOsessions")
        ensureDir(self.sessionsDir, deep=True, mode=0700);

        self.session = Session.makeSession(self.sessionsDir, sessionId, None)
        self.sessionid = self.session.getId()

        self.attributes = None
        self.status = consumer.SUCCESS

    loginpage_url = "https://wire.ncsa.uiuc.edu/openid/provider?openid.assoc_handle=8b5f9a606d61af76&openid.claimed_id=%s&openid.identity=%s&openid.mode=checkid_setup&openid.ns=http%%3A%%2F%%2Fspecs.openid.net%%2Fauth%%2F2.0&openid.realm=http%%3A%%2F%%2Fexample.org%%2Fprotected%%2Fwelcome.html&openid.return_to=%s"

    returnedto_url = "http://example.org/protected/welcome.html?janrain_nonce=2011-08-30T12%3A48%3A01ZiSCNzT&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.op_endpoint=https%3A%2F%2Fwire.ncsa.uiuc.edu%2Fopenid%2Fprovider&openid.claimed_id=https%3A%2F%2Fwire.ncsa.uiuc.edu%2Fopenid%2Fid%2Funittest&openid.response_nonce=2011-08-30T12%3A48%3A49Z0&openid.mode=id_res&openid.identity=https%3A%2F%2Fwire.ncsa.uiuc.edu%2Fopenid%2Fid%2Funittest&openid.return_to=http%3A%2F%2Fexample.org%2Fprotected%2Fwelcome.html%3Fjanrain_nonce%3D2011-08-30T12%253A48%253A01ZiSCNzT&openid.assoc_handle=8b5f9a606d61af76&openid.signed=op_endpoint%2Cclaimed_id%2Cidentity%2Creturn_to%2Cresponse_nonce%2Cassoc_handle&openid.sig=jsZ5UEjzO9M5OdW0hTNrNzSCIes%3D"

    def requestAuthentication(self, return_url, attributes=None, 
                              openid_url=None):

        if openid_url:
            openid_url = urllib.quote(openid_url)
        return_url = urllib.quote(return_url)
        out = self.loginpage_url % (openid_url, openid_url, return_url)
        if attributes:
            loginpage_url += "&openid.ns.ax=http%3A%2F%2Fopenid.net%2Fsrv%2Fax%2F1.0&openid.ax.mode=fetch_request&openid.ax.required=ext0%2Cext1"
            for a in attribute:
                loginpage_url += "&openid.ax.type.ext0=%s" % urllib.quote(a)
            self.attributes = attributes

        self.session.data['_openid_consumer_last_token'] = {}
                
        return out

    def processAuthentication(self, queryargs, return_url, keepSessionFor=None):
        class fakeinfo(object):
            def __init__(self, status, qargs):
                self.status = status
        class fakeresults(AuthenticationResponse):
            def __init__(self, session, status, config, attributes=None):
                self.cfg = config
                self.info = fakeinfo(status, cgi.FieldStorage())
                self.session = session
                self.sessionid = session.getId()
                self.attributes = attributes

        if keepSessionFor is not None:
            lifehours = None
            if type(keepSessionFor) is int or type(keepSessionFor) is float:  
                if keepSessionFor > 0:
                    lifehours = keepSessionFor

            self.session.clearData()
            self.session.setValid(lifehours)
            # self.session.addAttributes()
            self.session.save()
        else:
            self.session.end()

        return fakeresults(self.session, self.status, self.config, 
                           self.attributes)

    def setSuccess(self):
        self.status = consumer.SUCCESS
                
    def setCancelled(self):
        self.status = consumer.CANCEL

    def setFailed(self):
        self.status = consumer.FAILURE

class PortalTestCase(unittest.TestCase):

    def setUp(self):
        self.cfg = config.getConfig(conffile)
        self.cfg['vaologin.cli.statedir'] = statedir
        self.cfg['vaologin.cli.provideropenid'] = provideropenid

        self.ostrm = StringIO()
        self.estrm = StringIO()
        self.req = TestLoginRequest(self.cfg, errlog=self.estrm)
        self.oldenv = os.environ.copy()
        os.environ["SERVER_NAME"] = "www.example.org"
        os.environ['QUERY_STRING'] = ''

        self.portal = ProtectedPortal(self.cfg, 
                                      ostrm=self.ostrm, estrm=self.estrm, 
                                      reqhandler=self.req)

    def tearDown(self):
        if os.path.exists(statedir):
            shutil.rmtree(statedir)
        os.environ = self.oldenv.copy()

    def testCtor(self):
        self.assert_(not self.portal.validationNeeded())
        self.assert_(not self.portal.sessionValid())

    def testSendNotFound(self):
        # pdb.set_trace()
        self.portal.sendNotFound("gurn/goober.html");
        out = self.ostrm.getvalue()
        self.assert_(re.search(r"Location: http://[^/]+/notfound/gurn/goober.html", out), "Incorrect not-found URL: "+out)

    def testRedirect(self):
        url = "http://goober.com/gurn.html"
        self.portal.redirect(url)
        out = self.ostrm.getvalue()
        self.assert_(re.search(r"Location: %s" % url, out))
        self.assert_(out.endswith("\r\n\r\n"))

    def testLocalRedirect(self):
        url = "/gurn.html"
        self.portal.localRedirect(url)
        out = self.ostrm.getvalue()
        self.assert_(re.search(r"Location: http://%s%s" % 
                               (os.environ["SERVER_NAME"], url), out))
        self.assert_(out.endswith("\r\n\r\n"))
        url = "gurn.html"
        self.portal.localRedirect(url)
        out = self.ostrm.getvalue()
        self.assert_(re.search(r"Location: http://%s/%s" % 
                               (os.environ["SERVER_NAME"], url), out))
        self.assert_(out.endswith("\r\n\r\n"))

    def testGetMimeTypeFor(self):
        # pdb.set_trace()
        self.assertEquals("text/plain", self.portal.getMimeTypeFor("txt"))
        self.assertEquals("application/fits", 
                          self.portal.getMimeTypeFor("fits"))
        self.assertEquals("text/plain", self.portal.getMimeTypeFor("txt"))
        self.assertEquals("text/html", self.portal.getMimeTypeFor("html"))
        self.assertEquals("text/html", self.portal.getMimeTypeFor("htm"))

    def testDeliverFile(self):
        # pdb.set_trace()
        self.assert_(os.path.isdir(self.portal.docroot))
        self.assert_(os.path.isfile(os.path.join(self.portal.docroot,
                                                 'index.html')))
        self.portal.deliverFile('index.html')
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find('<h1>Welcome to the Protected Portal</h1>') >= 0)
        
    def testDeliverNotFound(self):
        # pdb.set_trace()
        self.assert_(os.path.isdir(self.portal.docroot))
        self.portal.deliverFile('gurn/goober.html')
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(re.search(r"Location: http://[^/]+/notfound/gurn/goober.html", self.ostrm.getvalue()), "Incorrect not-found URL: "+self.ostrm.getvalue())
        
    def testDeliverIndex(self):
        # pdb.set_trace()
        self.assert_(os.path.isdir(self.portal.docroot))
        self.assert_(os.path.isfile(os.path.join(self.portal.docroot,
                                                 'index.html')))
        self.portal.deliverFile('')
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find('<h1>Welcome to the Protected Portal</h1>') >= 0)

    def testExecScript(self):
        os.environ['SCRIPT_NAME'] = "/protected"
        self.assert_(os.path.isdir(self.portal.scriptroot))
        self.assert_(os.path.isfile(os.path.join(self.portal.scriptroot,
                                                 'showenv.csh')))
        # pdb.set_trace()
        self.portal.executeScript("showenv.csh/more/path", asexec=False)
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find('PATH_INFO=') >= 0)

        env = {}
        for line in self.ostrm.getvalue().split('\n'):
            nameval = map(lambda e: e.strip(), line.split('=', 1))
            if len(nameval) == 2:
                env[nameval[0]] = nameval[1]
        self.assert_(env.has_key('PATH_INFO'))
        self.assertEquals('/more/path', env['PATH_INFO'])
        self.assert_(env.has_key('VAOLOGIN_SESSION_ID'))
        
    def testExecPyScript(self):
        os.environ['SCRIPT_NAME'] = "/protected"
        self.assert_(os.path.isdir(self.portal.scriptroot))
        self.assert_(os.path.isfile(os.path.join(self.portal.scriptroot,
                                                 'showenv.py')))

        self.portal.executeScript("showenv.py/more/path", asexec=False)
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find(' PATH_INFO ') >= 0)

        env = {}
        pire = re.compile(r'<DT> (\w+) <DD> (\S.*\S)\s*$')
        for line in self.ostrm.getvalue().split('\n'):
            m = pire.search(line)
            if m:
                env[m.group(1)] = m.group(2)
        self.assert_(env.has_key('PATH_INFO'))
        self.assertEquals('/more/path', env['PATH_INFO'])
        self.assert_(env.has_key('VAOLOGIN_SESSION_ID'))
        
    def testDeliverStatic(self):
        # pdb.set_trace()
        self.assert_(os.path.isdir(self.portal.docroot))
        self.assert_(os.path.isfile(os.path.join(self.portal.docroot,
                                                 'index.html')))
        self.portal.deliver('index.html')
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find('<h1>Welcome to the Protected Portal</h1>') >= 0)
        
    def testDeliverScript(self):
        # pdb.set_trace()
        os.environ['SCRIPT_NAME'] = "/protected"
        self.assert_(os.path.isdir(self.portal.docroot))
        self.portal.deliver('/cgi-bin/showenv.py', False)
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(not re.search(r"Location: http://[^/]+/notfound/gurn/goober.html", self.ostrm.getvalue()))
        self.assert_(self.ostrm.getvalue().find(' PATH_INFO ') >= 0)

    def testAuthenticate(self):
        os.environ['SCRIPT_NAME'] = "/protected"
        os.environ['PATH_INFO'] = "/welcome.html"
        self.portal.authenticateFor()
        out = self.ostrm.getvalue()

        # test that we are being re-directed
        murl = re.search(r"Location: (%s\S+)" % provider_url, out)
        self.assert_(murl)

        # test the setting of a cookie
        self.assert_(out.find(r'Set-Cookie: session=') >= 0)

        # test the return url
        args = cgi.parse_qs(urlparse.urlsplit(murl.group(1))[3])
        expectedurl = "http://%s%s%s" % \
            (os.environ['SERVER_NAME'], os.environ['SCRIPT_NAME'], 
             os.environ['PATH_INFO'])
        self.assertEquals(expectedurl, args['openid.return_to'][0])

    def testValidate(self):
        os.environ['SCRIPT_NAME'] = "/protected"
        os.environ['PATH_INFO'] = "/welcome.html"
        self.portal.authenticateFor()
        out = self.ostrm.getvalue()
        murl = re.search(r"Location: (%s\S+)" % provider_url, out)
        self.assert_(murl)

        os.environ['QUERY_STRING'] = \
            urlparse.urlsplit(TestLoginRequest.returnedto_url)[3]
        self.portal = ProtectedPortal(self.cfg, 
                                      ostrm=self.ostrm, estrm=self.estrm, 
                                      reqhandler=self.req)

        # pdb.set_trace()
        resp = self.portal.validate()
        self.assert_(resp.isAuthenticated())
        self.assert_(resp.getSession().getValidTimeLeft() > 7.99*3600)
        self.assert_(self.portal.sessionValid())


    def testEnter(self):
        self.assert_(not self.portal.validationNeeded())
        self.assert_(not self.portal.sessionValid())

        os.environ['SCRIPT_NAME'] = "/protected"
        os.environ['PATH_INFO'] = "/welcome.html"
        self.portal.enter()

        # test that we are being re-directed
        out = self.ostrm.getvalue()
        murl = re.search(r"Location: (%s\S+)" % provider_url, out)
        self.assert_(murl)
        cookie = out.split('\n')[0]
        self.assert_(cookie.startswith("Set-Cookie: "))
        cookie = cookie[len("Set-Cookie: "):].strip()
        
        os.environ['HTTP_COOKIE'] = cookie
        os.environ['QUERY_STRING'] = \
            urlparse.urlsplit(TestLoginRequest.returnedto_url)[3]
        self.portal = ProtectedPortal(self.cfg, 
                                      ostrm=self.ostrm, estrm=self.estrm, 
                                      reqhandler=self.req)
        self.assertEquals(self.portal.sessionid, self.portal.request.sessionid)

        # pdb.set_trace()
        self.portal.enter()
        self.assert_(self.portal.sessionValid())
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find('<h1>Welcome to the Protected Portal</h1>') >= 0)

    def testCacheInputGet(self):
        os.environ['QUERY_STRING'] = qstr = 'name=me&job=bum'
        os.environ['REQUEST_METHOD'] = 'GET'

        datafile = os.path.join(statedir,'VAOsessions',
                                self.portal.sessionid+".env")
        inputfile = os.path.join(statedir,'VAOsessions',
                                 self.portal.sessionid+".input")

        self.assert_(not os.path.exists(datafile))
        self.assert_(not os.path.exists(inputfile))

        # pdb.set_trace()
        self.portal._cacheInputs()
        self.assert_(os.path.exists(datafile), "Datafile not found: "+datafile)
        self.assert_(not os.path.exists(inputfile))
        env = self.portal._findCGImetadata()
        self.assert_(env)
        self.assert_(not os.path.exists(datafile))

        self.assertEquals(qstr, env.get('QUERY_STRING'))
        self.assertEquals('GET', env.get('REQUEST_METHOD'))
        self.assert_(not env.has_key('CONTENT_LENGTH'))
        self.assert_(not env.has_key('CONTENT_TYPE'))

        os.environ['QUERY_STRING'] = 'blah'
        os.environ['REQUEST_METHOD'] = 'PUT'
        self.portal._restoreCachedInput(env)

        self.assertEquals(qstr, os.environ.get('QUERY_STRING'))
        self.assertEquals('GET', os.environ.get('REQUEST_METHOD'))
        

    def testCacheInputPost(self):
        os.environ['QUERY_STRING'] = qstr = ''
        os.environ['REQUEST_METHOD'] = 'POST'
        os.environ['CONTENT_TYPE'] = ct = 'text/plain'
        qstr = "name=me&job=bum"
        instrm = StringIO(qstr)
        os.environ['CONTENT_LENGTH'] = str(len(qstr))
        datafile = os.path.join(statedir,'VAOsessions',
                                self.portal.sessionid+".env")
        inputfile = os.path.join(statedir,'VAOsessions',
                                 self.portal.sessionid+".input")

        self.assert_(not os.path.exists(datafile))
        self.assert_(not os.path.exists(inputfile))

        # pdb.set_trace()
        self.portal._cacheInputs(instrm)
        self.assert_(os.path.exists(datafile), "Datafile not found: "+datafile)
        self.assert_(os.path.exists(inputfile))
        env = self.portal._findCGImetadata()
        self.assert_(env)
        self.assert_(not os.path.exists(datafile))
        self.assert_(os.path.exists(inputfile))

        self.assertEquals('', env.get('QUERY_STRING'))
        self.assertEquals('POST', env.get('REQUEST_METHOD'))
        self.assert_(env.has_key('CONTENT_LENGTH'))
        self.assert_(env.has_key('CONTENT_TYPE'))
        self.assertEquals('text/plain', env.get('CONTENT_TYPE'))
        self.assertEquals(str(len(qstr)), env.get('CONTENT_LENGTH'))

        self.assert_(env.has_key('inputFile'))
        self.assert_(os.path.exists(env['inputFile']))
        # self.assertEquals(inputfile, env.get('inputFile'))
        self.portal._restoreCachedInput(env)

        self.assertEquals('', os.environ.get('QUERY_STRING'))
        self.assertEquals('POST', os.environ.get('REQUEST_METHOD'))
        self.assertEquals('text/plain', os.environ.get('CONTENT_TYPE'))
        self.assertEquals(str(len(qstr)), os.environ.get('CONTENT_LENGTH'))
        data = StringIO()
        for line in self.portal.instream:
            data.write(line)
        self.assertEquals(qstr, data.getvalue())
        
        
        
__all__ = "PortalTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()
