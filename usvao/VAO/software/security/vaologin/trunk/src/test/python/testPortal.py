#!/usr/bin/env python
"""
Tests of the portal module

These tests require a test server to be running and the network available.
Note that if these tests fail, one can uncomment some log-setting lines to 
see more diagnostics.
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, re, unittest, time, urlparse, cgi, pickle
from cStringIO import StringIO

from vaologin import config
from vaologin.portal import ProtectedPortal

import login  # supports navigating login pages

################################
# Log message control: for more messages, adjust the commenting below
##########################################################
def silentlog(message, level=0):  pass
from openid import oidutil
oidutil.log = silentlog    # for more messages, comment out this line
stderr=None
# stderr=sys.stderr        # for more messages, uncomment this line

testdir = os.path.join(os.getcwd(), 'target', 'tmp')
statedir = os.path.join(testdir, 'vaologin')
sessionsdir = os.path.join(statedir, 'VAOsessions')
conffile = os.path.join(os.getcwd(), *'src/test/python/test.cfg'.split('/'))
return_url = "http://example.org/protected/welcome.html"
provider_server = "vaossotest.ncsa.illinois.edu" 
provider_url = "https://%s/openid/provider" % provider_server
provideropenid = "https://%s/openid/provider_id" % provider_server
polocalidbase = "https://%s/openid/id/" % provider_server
idselect = "http://specs.openid.net/auth/2.0/identifier_select"
cacertbundle = os.path.join(os.getcwd(), 'etc', 'cacerts', 'usvao-ca.crt')

testusername = login.testusername
testpassword = login.testpassword
oid_identity = polocalidbase + testusername

class PortalTestCase(unittest.TestCase):

    def setUp(self):
        self.cfg = config.getConfig(conffile)
        self.cfg['vaologin.auth.statedir'] = statedir
        self.cfg['vaologin.auth.provideropenid'] = provideropenid
        self.cfg['vaologin.auth.cacertbundles'] = cacertbundle

        if not os.path.exists(statedir):
            os.makedirs(statedir)

        # pdb.set_trace()
        self.ostrm = StringIO()
        self.estrm = StringIO()
        self.oldenv = os.environ.copy()
        os.environ['QUERY_STRING'] = ''
        os.environ["SERVER_NAME"] = "www.example.org"
        os.environ["SCRIPT_NAME"] = "/protected"
        os.environ['PATH_INFO'] = "/welcome.html"

        self.portal = ProtectedPortal(self.cfg, execScripts=False,
                                      ostrm=self.ostrm, estrm=self.estrm)

    def tearDown(self):
        if os.path.exists(statedir):
            shutil.rmtree(statedir)
        os.environ = self.oldenv.copy()

    def testCtor(self):
        self.assert_(self.portal)
        self.assert_(self.portal.request)
        self.assert_(not self.portal.sessionValid())
        self.assert_(not self.portal.validationNeeded())

    def testRequestAuth(self):
        self.portal.enter()
        resp = self.ostrm.getvalue()
        locpos = resp.find("Location: https://%s/openid/provider" % 
                           provider_server) 
        self.assert_(locpos >= 0)
        self.assert_(re.search(r'openid.ax.type.ext\d=http%3A%2F%2Faxschema.org%2FnamePerson%2Ffriendly', resp[locpos:]))
        self.assert_(resp.find("Set-Cookie: session=") >= 0)

    def _checkArgs(self, returnedto, cancelled=False):
        urlparts = urlparse.urlsplit(returnedto)
        qargs = cgi.parse_qs(urlparts[3])
        for k in qargs.keys(): qargs[k] = qargs[k][0]
        self.assert_(qargs.has_key('janrain_nonce'))
        self.assert_(qargs.has_key('openid.ns'))
        if cancelled:
            self.assertEquals("cancel", qargs.get('openid.mode'))
            return qargs

        self.assertEquals("id_res", qargs.get('openid.mode'))
        self.assertEquals(provider_url, qargs.get('openid.op_endpoint'))
        self.assertEquals("%s%s"% (polocalidbase, testusername), 
                          qargs.get('openid.claimed_id'))
        self.assertEquals("%s%s"% (polocalidbase, testusername), 
                          qargs.get('openid.identity'))
        self.assert_(qargs.has_key('openid.response_nonce'))
        return urlparts[3]

    def testValidated(self):
        self.portal.enter()
        resp = self.ostrm.getvalue()
        os.environ = self.oldenv.copy()
        os.environ['QUERY_STRING'] = ''
        os.environ["SERVER_NAME"] = "www.example.org"
        os.environ["SCRIPT_NAME"] = "/protected"
        os.environ['PATH_INFO'] = "/welcome.html"

        murl = re.search(r"Location: (%s\S+)" % provider_url, resp)
        self.assert_(murl)
        redirect = murl.group(1)

        args = cgi.parse_qs(urlparse.urlsplit(redirect)[3])
        return_url = args['openid.return_to'][0]
        expectedurl = "http://%s%s%s?janrain_nonce=" % \
            (os.environ['SERVER_NAME'], os.environ['SCRIPT_NAME'], 
             os.environ['PATH_INFO'])
        self.assert_(return_url.startswith(expectedurl),
                     "%s !~ %s" % (return_url, expectedurl))

        cookie = resp.split('\n')[0]
        self.assert_(cookie.startswith("Set-Cookie: "))
        cookie = cookie[len("Set-Cookie: "):].strip()
        os.environ['HTTP_COOKIE'] = cookie

        ssosite = login.SSOSite(provider_server, return_url)
        return_url = ssosite.login(redirect, ["username"])
        os.environ['QUERY_STRING'] = self._checkArgs(return_url)

        self.ostrm = StringIO()
        self.estrm = StringIO()
        self.portal = ProtectedPortal(self.cfg, execScripts=False,
                                      ostrm=self.ostrm, estrm=self.estrm)
        self.portal.enter()
        self.assert_(self.portal.sessionValid())
        self.assert_(len(self.ostrm.getvalue()) > 0)
        self.assert_(self.ostrm.getvalue().find('<h1>Welcome to the Protected Portal</h1>') >= 0)
        
    def testExecScript(self):
        os.environ['PATH_INFO'] = "/cgi-bin/showenv.csh/more/path"
        self.portal.enter()
        resp = self.ostrm.getvalue()
        os.environ = self.oldenv.copy()
        os.environ['QUERY_STRING'] = ''
        os.environ["SERVER_NAME"] = "www.example.org"
        os.environ["SCRIPT_NAME"] = "/protected"
        os.environ['PATH_INFO'] = "/cgi-bin/showenv.csh/more/path"

        murl = re.search(r"Location: (%s\S+)" % provider_url, resp)
        self.assert_(murl)
        redirect = murl.group(1)

        args = cgi.parse_qs(urlparse.urlsplit(redirect)[3])
        return_url = args['openid.return_to'][0]
        expectedurl = "http://%s%s%s?janrain_nonce=" % \
            (os.environ['SERVER_NAME'], os.environ['SCRIPT_NAME'], 
             os.environ['PATH_INFO'])
        self.assert_(return_url.startswith(expectedurl),
                     "%s !~ %s" % (return_url, expectedurl))

        cookie = resp.split('\n')[0]
        self.assert_(cookie.startswith("Set-Cookie: "))
        cookie = cookie[len("Set-Cookie: "):].strip()
        os.environ['HTTP_COOKIE'] = cookie
        m = re.search("session=(\S+)", cookie)
        self.assert_(m)
        sessionid = m.group(1)

        ssosite = login.SSOSite(provider_server, return_url)
        return_url = ssosite.login(redirect, ["username"])
        os.environ['QUERY_STRING'] = self._checkArgs(return_url)

        self.ostrm = StringIO()
        self.estrm = StringIO()
        self.portal = ProtectedPortal(self.cfg, execScripts=False,
                                      ostrm=self.ostrm, estrm=self.estrm)
        self.portal.enter()
        self.assert_(self.portal.sessionValid())
        out = self.ostrm.getvalue()
        self.assert_(len(out) > 0)

        env = {}
        header = {}
        inheader = True
        for line in self.ostrm.getvalue().split('\n'):
            if inheader:
                line = line.strip()
                if len(line) == 0:
                    inheader = False
                    continue
                nameval = map(lambda e: e.strip(), line.split(':', 1))
                if len(nameval) == 2:
                    header[nameval[0]] = nameval[1]
            else:
                nameval = map(lambda e: e.strip(), line.split('=', 1))
                if len(nameval) == 2:
                    env[nameval[0]] = nameval[1]

        self.assertEquals("text/plain", header.get("Content-Type"))

        self.assert_(env.has_key('PATH_INFO'))
        self.assertEquals('/more/path', env['PATH_INFO'])
        self.assertEquals(sessionid, env.get('VAOLOGIN_SESSION_ID'))
        self.assertEquals(oid_identity, env.get('OPENID_IDENTITY'))
        self.assertEquals(oid_identity, env.get('OPENID_CLAIMED_ID'))
        self.assertEquals(8*3600, float(env.get('VAOLOGIN_VALID_LIFETIME','-1')))
        self.assert_(env.has_key('VAOLOGIN_VALID_SINCE'))
        self.assertEquals("OpenID", env.get('AUTH_TYPE'))
        self.assertEquals(testusername, env.get('VAOLOGIN_USERNAME'))
        self.assertEquals(testusername, env.get('REMOTE_USER'))




__all__ = "PortalTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()


