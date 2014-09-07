#!/usr/bin/env python
"""
Tests of the authenticate module

These tests require a test server to be running and the network available.
Note that if these tests fail, one can uncomment some log-setting lines to 
see more diagnostics.
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, re, unittest, time, urlparse, cgi, pickle

from vaologin import config
from vaologin.authenticate import AuthenticationResponse, NoAvailableCredError
from vaologin.authenticate import FailedAuthenticationResponse, VAOLoginRequest
from vaologin.authenticate import Attributes, Session

import login  # supports navigating login pages

################################
# Log message control: for more messages, adjust the commenting below
##########################################################
def silentlog(message, level=0):  pass
from openid import oidutil
oidutil.log = silentlog    # for more messages, comment out this line
stderr=None
# stderr=sys.stderr        # for more messages, uncomment this line

testdir = os.path.join(os.getcwd(), 'build', 'tmp')
statedir = os.path.join(testdir, 'vaologin')
sessionsdir = os.path.join(statedir, 'VAOsessions')
conffile = os.path.join(os.getcwd(), *'src/test/python/test.cfg'.split('/'))
return_url = "http://example.org/protected/welcome.html"
provider_server = "vaossotest.ncsa.illinois.edu" 
provider_url = "https://%s/openid/provider" % provider_server
logout_url = "https://%s/openid/logout" % provider_server
provideropenid = "https://%s/openid/provider_id" % provider_server
polocalidbase = "https://%s/openid/id/" % provider_server
idselect = "http://specs.openid.net/auth/2.0/identifier_select"
cacertbundle = os.path.join(os.getcwd(), 'etc', 'cacerts', 'VAO-TestCA-cert.pem')

testusername = login.testusername
testpassword = login.testpassword

class FailedResponseTestCase(unittest.TestCase):

    def setUp(self):
        self.cfg = config.getConfig(conffile)
        session = Session('123', 'sessions/123')
        self.resp = FailedAuthenticationResponse(session, self.cfg)
    def tearDown(self):
        pass

    def testFailed(self):
        self.assertEquals('123', self.resp.getSessionId())
        self.assert_(not self.resp.isAuthenticated())
        self.assert_(self.resp.protocolFailed())
        self.assert_(not self.resp.wasCanceled())
        self.assert_(not self.resp.neededUserInput())
        self.assert_(not self.resp.credentialAvailable())
        self.assert_(self.resp.getAttributes() is None)
        self.assertRaises(NoAvailableCredError, self.resp.cacheCredential)

class AuthRequestTestCase(unittest.TestCase):

    def setUp(self):
        self.cfg = config.getConfig(conffile)
        self.cfg['vaologin.auth.statedir'] = statedir
        self.cfg['vaologin.auth.provideropenid'] = provideropenid
        self.cfg['vaologin.auth.cacertbundles'] = cacertbundle

        if not os.path.exists(statedir):
            os.makedirs(statedir)

        # pdb.set_trace()
        self.req = VAOLoginRequest(self.cfg, errlog=stderr)

    def tearDown(self):
        if os.path.exists(statedir):
            shutil.rmtree(statedir)

    def testCtor(self):
        self.assert_(self.req)
        self.assert_(self.req.session)
        self.assert_(self.req.sessionid)
        self.assert_(self.req.getSession())

    def testEndBeforeRequest(self):
        self.assert_(not os.path.exists(self.req.getSession().file))
        self.req.endSession()

    def testRequestAuth(self):
        # pdb.set_trace()
        redirect_url = self.req.requestAuthentication(return_url)
        # print redirect_url

        self.assert_(redirect_url.startswith(provider_url))
        qstr = urlparse.urlsplit(redirect_url)[3]
        self.assert_(len(qstr) > 0)

        qargs = cgi.parse_qs(qstr)

        self.assert_('openid.assoc_handle' in qargs.keys())
        self.assert_('openid.mode' in qargs.keys())
        self.assert_('openid.ns' in qargs.keys())
        self.assert_('openid.realm' in qargs.keys())
        self.assert_('openid.return_to' in qargs.keys())
        self.assert_('openid.claimed_id' in qargs.keys())
        self.assert_('openid.identity' in qargs.keys())

        self.assertEquals('checkid_setup', qargs['openid.mode'][0])
        self.assertEquals('http://specs.openid.net/auth/2.0', 
                          qargs['openid.ns'][0])
        self.assertEquals(return_url, qargs['openid.realm'][0])
        self.assert_(qargs['openid.return_to'][0].startswith(return_url+'?janrain_nonce='))

#        self.assertEquals(identity_select_url, qargs['openid.claimed_id'][0])
#        self.assertEquals(identity_select_url, qargs['openid.identity'][0])
        self.assertEquals(idselect, qargs['openid.claimed_id'][0])
        self.assertEquals(idselect, qargs['openid.identity'][0])

        self.assert_(self.req.getSession().validationNeeded())

    def testEndSessionAfterRequest(self):
        redirect_url = self.req.requestAuthentication(return_url)
        # print redirect_url

        self.assert_(redirect_url.startswith(provider_url))
        qstr = urlparse.urlsplit(redirect_url)[3]
        self.assert_(len(qstr) > 0)

        tmpdir = os.path.join(statedir,'associations')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 1)
        tmpdir = os.path.join(statedir,'nonces')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)

        # test ending session before validation
        _testEndSession(self)

    def testStatelessRequest(self):
        self.cfg['vaologin.auth.usestatelessprotocol'] = 'true'
        self.req = VAOLoginRequest(self.cfg, errlog=stderr)
        tmpdir = os.path.join(statedir,'associations')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)

        redirect_url = self.req.requestAuthentication(return_url)
        # print redirect_url

        self.assert_(redirect_url.startswith(provider_url))
        qstr = urlparse.urlsplit(redirect_url)[3]
        self.assert_(len(qstr) > 0)

        qargs = cgi.parse_qs(qstr)

        self.assert_('openid.assoc_handle' not in qargs.keys())
        self.assert_('openid.mode' in qargs.keys())

        self.assertEquals('checkid_setup', qargs['openid.mode'][0])
        self.assertEquals('http://specs.openid.net/auth/2.0', 
                          qargs['openid.ns'][0])
        self.assertEquals(return_url, qargs['openid.realm'][0])
        self.assert_(qargs['openid.return_to'][0].startswith(return_url+'?janrain_nonce='))

        self.assert_(self.req.getSession().validationNeeded())

        tmpdir = os.path.join(statedir,'associations')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)
        tmpdir = os.path.join(statedir,'nonces')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)

def _testEndSession(tc):
    # session file
    tc.assert_(os.path.exists(tc.req.getSession().file))
    tc.req.endSession()
    tc.assert_(not os.path.exists(tc.req.getSession().file))


valQstr = "openid.ext1.type.ext2=http://axschema.org/contact/email&openid.ext1.value.ext2=ysvenkat@ncsa.uiuc.edu&openid.ext1.type.ext0=http://axschema.org/namePerson/friendly&openid.op_endpoint=https://wire.ncsa.uiuc.edu/openid/provider&openid.ext1.value.ext1=http://wire.ncsa.uiuc.edu/stage/credential/W5yV7VPZPUWyt59a/ysvenkat.pem&openid.ns=http://specs.openid.net/auth/2.0&openid.identity=https://wire.ncsa.uiuc.edu/openid/id/&openid.ext1.type.ext1=http://sso.usvao.org/schema/credential/x509&openid.response_nonce=2011-07-20T20:28:30Z0&openid.mode=id_res&openid.assoc_handle=221cb498748e5567&openid.ext1.mode=fetch_response&openid.sig=i/YOH+B9COtY3uU+TTt/L6DUnBk=&openid.signed=op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle&openid.ext1.value.ext0=ysvenkat&openid.ns.ext1=http://openid.net/srv/ax/1.0&openid.claimed_id=https://wire.ncsa.uiuc.edu/openid/id/"

class AuthValidateTestCase(unittest.TestCase):

    def __init__(self, methodname='runTest'):
        unittest.TestCase.__init__(self, methodname)
        self.ssosite = login.SSOSite(provider_server, return_url)

    def setUp(self):
        self.cfg = config.getConfig(conffile)
        self.cfg['vaologin.auth.statedir'] = statedir
        self.cfg['vaologin.auth.sessionsdir'] = sessionsdir
        self.cfg['vaologin.auth.provideropenid'] = provideropenid
        self.cfg['vaologin.auth.globallogouturl'] = logout_url
        self.cfg['vaologin.auth.cacertbundles'] = cacertbundle

        if not os.path.exists(statedir):
            os.makedirs(statedir)

        self.req = VAOLoginRequest(self.cfg)
        self.sid = self.req.sessionid
        self.redirect = None

    def _requestAuth(self, attributes=None, username=None):
        oid = None
        if username:
            oid = "%s%s"% (polocalidbase, username)
        self.redirect = self.req.requestAuthentication(return_url, attributes,
                                                       openid_url=oid)

    def _authenticate(self, attributes=None, confirm=True, passwd=None):
        if not self.redirect:
            self._requestAuth(attributes)

        return self.ssosite.login(self.redirect, attributes, confirm, passwd)

    def tearDown(self):
        if os.path.exists(statedir):
            shutil.rmtree(statedir)

    def testReturn(self):
        self._requestAuth()
        self.assert_(os.path.exists(os.path.join(sessionsdir, self.sid)))
        req = VAOLoginRequest(self.cfg, self.sid)
        self.assert_(req.getSession().validationNeeded())

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
        return qargs

    def testValidate(self):
        # turn off configured attributes
        self.req.config['vaologin.auth.requiredAttributes'] = ''

        #pdb.set_trace()
        returnedto = self._authenticate()
        self.assert_(returnedto)
        self.assert_(returnedto.startswith(return_url))
        qargs = self._checkArgs(returnedto)

        self.assert_(self.req.getSession().validationNeeded())

        #pdb.set_trace()
        result = self.req.processAuthentication(qargs, return_url, 0.05)
        self.assert_(not result.protocolFailed())
        self.assert_(result.isAuthenticated())
        self.assert_(not result.wasCanceled())
        self.assert_(not result.neededUserInput())
        self.assert_(not self.req.getSession().validationNeeded())

    def testEndSessionAfertValidation(self):
        self.testValidate()

        # test ending session after validation
        _testEndSession(self)

    def testLogoutLocally(self):
        self.testValidate()
        redirect = self.req.logout(return_url, False)
        self.assert_(not os.path.exists(self.req.getSession().file),
                     "logout did not end session")
        self.assertEqual(redirect, return_url);

        self.req.config['vaologin.auth.logoutmode'] = "local"
        redirect = self.req.logout(return_url)
        self.assertEqual(redirect, return_url);

        # if we're not using sso.usvao.org and globalLogoutUrl is not 
        # set, disable global logout.  
        self.cfg['vaologin.auth.globallogouturl'] = None
        redirect = self.req.logout(return_url, True)
        self.assertEqual(redirect, return_url);

    def testLogoutGlobally(self):
        self.testValidate()
        logoutsrvc = logout_url+"?returnURL="+return_url
        redirect = self.req.logout(return_url, True)
        self.assert_(not os.path.exists(self.req.getSession().file),
                     "logout did not end session")
        self.assertEqual(redirect, logoutsrvc)

        self.assert_(self.ssosite.logged_in(), 
                     "Apparently not globally logged in")
        self.ssosite.logout(redirect)
        self.assert_(not self.ssosite.logged_in(), 
                     "Apparently not globally logged out")

        self.req.config['vaologin.auth.logoutmode'] = None
        redirect = self.req.logout(return_url, True)
        self.assertEqual(redirect, logoutsrvc)

        self.req.config['vaologin.auth.logoutmode'] = "global"
        redirect = self.req.logout(return_url, True)
        self.assertEqual(redirect, logoutsrvc)

        # unsupported logoutMode value defaults to "global"
        self.req.config['vaologin.auth.logoutmode'] = "legal"
        redirect = self.req.logout(return_url, True)
        self.assertEqual(redirect, logoutsrvc)

    def testStatelessValidate(self):
        self.cfg['vaologin.auth.usestatelessprotocol'] = 'true'
        self.cfg['vaologin.auth.requiredAttributes'] = ''
        self.req = VAOLoginRequest(self.cfg, errlog=stderr)

        #pdb.set_trace()
        returnedto = self._authenticate()
        self.assert_(returnedto)
        self.assert_(returnedto.startswith(return_url))
        qargs = self._checkArgs(returnedto)

        # make sure we went stateless on authentication
        tmpdir = os.path.join(statedir,'associations')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)
        tmpdir = os.path.join(statedir,'nonces')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)

        self.assert_(self.req.getSession().validationNeeded())

        #pdb.set_trace()
        result = self.req.processAuthentication(qargs, return_url, 0.05)

        # make sure we went stateless on authentication
        tmpdir = os.path.join(statedir,'associations')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)
        tmpdir = os.path.join(statedir,'nonces')
        self.assert_(os.path.exists(tmpdir))
        tmpfiles = os.listdir(tmpdir)
        self.assertEquals(len(tmpfiles), 0)

        # make sure verification still worked
        self.assert_(not result.protocolFailed())
        self.assert_(result.isAuthenticated())
        self.assert_(not result.wasCanceled())
        self.assert_(not result.neededUserInput())
        self.assert_(not self.req.getSession().validationNeeded())


    def testSession(self):
        # turn off configured attributes
        self.req.config['vaologin.auth.requiredAttributes'] = ''

        #pdb.set_trace()
        returnedto = self._authenticate()
        self.assert_(returnedto)
        self.assert_(returnedto.startswith(return_url))
        qargs = self._checkArgs(returnedto)

        self.assert_(self.req.getSession().validationNeeded())

        #pdb.set_trace()
        result = self.req.processAuthentication(qargs, return_url, 0.25)
        self.assert_(not result.protocolFailed())
        self.assert_(result.isAuthenticated())
        self.assert_(not result.wasCanceled())
        self.assert_(not result.neededUserInput())
        self.assert_(not self.req.getSession().validationNeeded())

        session = result.getSession()
        self.assert_(session.data.has_key('openid.identity'))
        self.assert_(session.data.has_key('openid.claimed_id'))
        self.assertEquals("%s%s"% (polocalidbase, testusername), 
                          session.data['openid.claimed_id'])
        self.assertEquals("%s%s"% (polocalidbase, testusername), 
                          session.data['openid.identity'])

        sessfile = os.path.join(sessionsdir, session.getId())
        self.assert_(os.path.exists(sessfile))
        with open(sessfile) as fd:
            sdata = pickle.load(fd)
        self.assert_(session.data.has_key('username'))
        self.assert_(session.data.has_key('validLifetime'))
        self.assert_(session.data.has_key('validSince'))
        self.assert_(session.data.has_key('openid.identity'))
        self.assert_(session.data.has_key('openid.claimed_id'))
        self.assertEquals("%s%s"% (polocalidbase, testusername), 
                          session.data['openid.claimed_id'])
        self.assertEquals("%s%s"% (polocalidbase, testusername), 
                          session.data['openid.identity'])
        self.assertEquals(3600*0.25, session.data['validLifetime'])

    def testCancel(self):
        #pdb.set_trace()
        returnedto = self._authenticate(confirm=False)
        self.assert_(returnedto)
        self.assert_(returnedto.startswith(return_url))
        qargs = self._checkArgs(returnedto, True)

        result = self.req.processAuthentication(qargs, return_url)
        self.assert_(not result.protocolFailed())
        self.assert_(not result.isAuthenticated())
        self.assert_(result.wasCanceled())
        self.assert_(not result.neededUserInput())

    def testAttributes(self):
        atts = [ Attributes.USERNAME, Attributes.EMAIL ]

        returnedto = self._authenticate(atts)
        self.assert_(returnedto)
        self.assert_(returnedto.startswith(return_url))
        qargs = self._checkArgs(returnedto)

        self.assert_(qargs.has_key('openid.ns.ext1'))
        self.assertEquals("http://openid.net/srv/ax/1.0", 
                          qargs.get('openid.ns.ext1'))
        self.assertEquals("fetch_response", qargs.get('openid.ext1.mode'))
        self.assertEquals(testusername, qargs.get('openid.ext1.value.ext0'))
        self.assert_(qargs.get('openid.ext1.value.ext1').startswith('rplante@'))

        # pdb.set_trace()
        result = self.req.processAuthentication(qargs, return_url)
        self.assert_(not result.protocolFailed())
        self.assert_(result.isAuthenticated())
        self.assert_(not result.wasCanceled())
        self.assert_(not result.neededUserInput())

        atts = result.getAttributes()
        self.assert_(atts)
        self.assert_(hasattr(atts, 'has_key'))
        self.assertEquals(testusername, atts.get(Attributes.USERNAME))
        self.assert_(atts.get(Attributes.EMAIL, '').startswith('rplante@'))

    def testAutoAttributes(self):
        """
        test that username attribute is automatically retrieved as per 
        the config file.
        """
        # pdb.set_trace()
        returnedto = self._authenticate()
        self.assert_(returnedto)
        self.assert_(returnedto.startswith(return_url))
        qargs = self._checkArgs(returnedto)

        self.assert_(qargs.has_key('openid.ns.ext1'))
        self.assertEquals("http://openid.net/srv/ax/1.0", 
                          qargs.get('openid.ns.ext1'))
        self.assertEquals("fetch_response", qargs.get('openid.ext1.mode'))
        self.assertEquals(testusername, qargs.get('openid.ext1.value.ext0'))

        # pdb.set_trace()
        result = self.req.processAuthentication(qargs, return_url)
        self.assert_(not result.protocolFailed())
        self.assert_(result.isAuthenticated())
        self.assert_(not result.wasCanceled())
        self.assert_(not result.neededUserInput())

        atts = result.getAttributes()
        self.assert_(atts)
        self.assert_(hasattr(atts, 'has_key'))
        self.assertEquals(testusername, atts.get(Attributes.USERNAME))

    def testBogusValidate(self):
        """
        this attempts a bogus call to validate without having actually 
        authenticated at the server.  Validation should fail.  
        """
        returnedto = self._authenticate()
        self.assert_(self.req.getSession().validationNeeded())

        qstr = urlparse.urlsplit(self.redirect)[3]
        self.assert_(len(qstr) > 0)
        qargs = cgi.parse_qs(qstr)
        return_url = qargs['openid.return_to'][0]
        return_url = "%s&openid.return_to=%s&" % (return_url, return_url)
        return_url += valQstr

        qstr = urlparse.urlsplit(return_url)[3]
        qargs = cgi.parse_qs(qstr)
        for k in qargs.keys():
            qargs[k] = qargs[k][0]

        # pdb.set_trace()
        result = self.req.processAuthentication(qargs, return_url)
        
        # this will not work unless we have truely authenticated
        # (signatures will not match)
        self.assert_(not result.isAuthenticated())
        


__all__ = "FailedResponseTestCase AuthRequestTestCase AuthValidateTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()
