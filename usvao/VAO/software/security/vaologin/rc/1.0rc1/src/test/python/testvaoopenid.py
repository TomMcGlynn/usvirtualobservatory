#!/usr/bin/env python
"""
Tests of the authenticate module

These tests require a test server to be running and the network available.
Note that if these tests fail, one can uncomment some log-setting lines to 
see more diagnostics.
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, re, unittest, time, urlparse, cgi, re
from cStringIO import StringIO

from vaologin.cli import vaoopenid
import login  # supports navigating login pages

testdir = os.path.join(os.getcwd(), 'target', 'tmp')
statedir = os.path.join(testdir, 'vaologin')
sessionsdir = os.path.join(statedir, 'VAOsessions')
conffile = os.path.join(os.getcwd(), *'src/test/python/test.cfg'.split('/'))
server = "vaossotest.ncsa.illinois.edu"

################################
# Log message control: for more messages, adjust the commenting below
##########################################################
def silentlog(message, level=0):  pass
from openid import oidutil
oidutil.log = silentlog    # for more messages, comment out this line
stderr=None
# stderr=sys.stderr        # for more messages, uncomment this line

class VAOopenidTestCase(unittest.TestCase):

    def setUp(self):
        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        if not os.path.exists(testdir):
            os.mkdir(testdir)

    def tearDown(self):
        if os.path.exists(statedir):
            shutil.rmtree(statedir)

    def testRequest(self):
        cl = "vaoopenid -c %s request %s" % (conffile, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0]
        # print redirect
        
        self.assert_(redirect.startswith("https://%s/openid/provider" % server))

        self.assert_(re.search(r'openid.ax.type.ext\d=http%3A%2F%2Faxschema.org%2FnamePerson%2Ffriendly', redirect))

    def testRequestAtts(self):
        cl = "vaoopenid -c %s -a username,email request %s" % (conffile, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0]
        
        self.assert_(redirect.startswith ("https://%s/openid/provider" % server))

        self.assert_(re.search(r'openid.ax.type.ext\d=http%3A%2F%2Faxschema.org%2FnamePerson%2Ffriendly&', redirect))
        self.assert_(re.search(r'openid.ax.type.ext\d=http%3A%2F%2Faxschema.org%2Fcontact%2Femail&', redirect))

    def testProcess(self):
        sessionid = "testsessionid"
        cl = "vaoopenid -c %s -s %s request %s" % \
            (conffile, sessionid, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0].strip()
        self.assert_(redirect.startswith("https://%s/openid/provider" % server),
                     "unexpected id url returned: " + redirect)
        # print >> sys.stderr, redirect

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        ssosite = login.SSOSite(server, login.returnurl)
        returnedto = ssosite.login(redirect)

        cl = "vaoopenid -c %s  -s %s process %s" % \
            (conffile, sessionid, returnedto)
        sys.argv = cl.split()
        vaoopenid.main()
        # print >> sys.stderr, self.stdout.getvalue()

        lines = self.stdout.getvalue().strip().splitlines()
        atts = {}
        for line in lines:
            (name,value) = line.split('=',1)
            atts[name] = value

        self.assert_(atts.has_key('username'))
        self.assertEquals('unittest', atts['username'])
    
    def testProcessAtts(self):
        sessionid = "testsessionid"
        cl = "vaoopenid -c %s -s %s -a username,email,cert request %s" % (conffile, sessionid, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0].strip()
        self.assert_(redirect.startswith("https://%s/openid/provider" % server))
        # print >> sys.stderr, redirect

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        ssosite = login.SSOSite(server, login.returnurl)
        returnedto = ssosite.login(redirect,"username email cert".split())

        cl = "vaoopenid -c %s  -s %s process %s" % \
            (conffile, sessionid, returnedto)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        # print >> sys.stderr, self.stdout.getvalue()

        lines = self.stdout.getvalue().strip().split('\n')
        atts = {}
        for line in lines:
            (name,value) = line.split('=',1)
            atts[name] = value

        self.assert_(atts.has_key('username') and 'unittest'==atts['username'])
        self.assert_(atts.has_key('email') and atts['email'].startswith('rplante@'))
        self.assert_(atts.has_key('certificate'))
        self.assert_(atts.has_key('certfile'))
        self.assert_(os.path.exists(atts['certfile']))

    def testStatusNoSessId(self):
        cl = "vaoopenid -c %s status" % conffile
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "out")

    def testStatusOldSessId(self):
        sessionid = "goobermeister"
        cl = "vaoopenid -c %s -s %s status" % (conffile, sessionid)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "out")

    def testStatusIncompleteSessId(self):
        sessionid = "138420934575"

        cl = "vaoopenid -c %s -s %s request %s" % (conffile, sessionid, 
                                                   login.returnurl)
        sys.argv = cl.split()
        vaoopenid.main()

        cl = "vaoopenid -c %s -s %s status" % (conffile, sessionid)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "incomplete")

    def testStatusIn(self):

        # first establish a session
        sessionid = "testsessionid"
        cl = "vaoopenid -c %s -s %s request %s" % \
            (conffile, sessionid, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0].strip()
        self.assert_(redirect.startswith("https://%s/openid/provider" % server),
                     "unexpected id url returned: " + redirect)
        # print >> sys.stderr, redirect

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        ssosite = login.SSOSite(server, login.returnurl)
        returnedto = ssosite.login(redirect)

        cl = "vaoopenid -c %s -l 0.11 -s %s process %s" % \
            (conffile, sessionid, returnedto)
        sys.argv = cl.split()
        vaoopenid.main()
        # print >> sys.stderr, self.stdout.getvalue()

        lines = self.stdout.getvalue().strip().splitlines()
        atts = {}
        for line in lines:
            (name,value) = line.split('=',1)
            atts[name] = value

        self.assert_(atts.has_key('username'))
        self.assertEquals('unittest', atts['username'])

        # now test status 

        cl = "vaoopenid -c %s -s %s status" % (conffile, sessionid)
        sys.argv = cl.split()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "in")
        self.assertEquals(data['username'], "unittest")
        self.assertEquals(data['openid'], 
                          "https://%s/openid/id/unittest" % server)
        self.assert_(data.has_key('secLeft'))
        self.assert_(int(data['secLeft']) > 300)

    def testEnd(self):

        # first establish a session
        sessionid = "testsessionid"
        cl = "vaoopenid -c %s -s %s request %s" % \
            (conffile, sessionid, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0].strip()
        self.assert_(redirect.startswith("https://%s/openid/provider" % server),
                     "unexpected id url returned: " + redirect)
        # print >> sys.stderr, redirect

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        ssosite = login.SSOSite(server, login.returnurl)
        returnedto = ssosite.login(redirect)

        cl = "vaoopenid -c %s -l 0.11 -s %s process %s" % \
            (conffile, sessionid, returnedto)
        sys.argv = cl.split()
        vaoopenid.main()
        # print >> sys.stderr, self.stdout.getvalue()

        lines = self.stdout.getvalue().strip().splitlines()
        atts = {}
        for line in lines:
            (name,value) = line.split('=',1)
            atts[name] = value

        self.assert_(atts.has_key('username'))
        self.assertEquals('unittest', atts['username'])

        # now confirm status 

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        cl = "vaoopenid -c %s -s %s status" % (conffile, sessionid)
        sys.argv = cl.split()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "in")

        # now test ending the session

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        cl = "vaoopenid -c %s -s %s end" % (conffile, sessionid)
        sys.argv = cl.split()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()
        self.assertEquals(len(lines), 0)

        # test the resulting status

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        cl = "vaoopenid -c %s -s %s status" % (conffile, sessionid)
        sys.argv = cl.split()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "out")

    def testExpire(self):
        # first establish a (short) session
        sessionid = "testsessionid"
        cl = "vaoopenid -c %s -s %s request %s" % \
            (conffile, sessionid, login.returnurl)
        sys.argv = cl.split()
        # pdb.set_trace()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().split('\n')
        redirect = lines[0].strip()
        self.assert_(redirect.startswith("https://%s/openid/provider" % server),
                     "unexpected id url returned: " + redirect)
        # print >> sys.stderr, redirect

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        ssosite = login.SSOSite(server, login.returnurl)
        returnedto = ssosite.login(redirect)

        cl = "vaoopenid -c %s -l 0.0003 -s %s process %s" % \
            (conffile, sessionid, returnedto)
        sys.argv = cl.split()
        vaoopenid.main()
        # print >> sys.stderr, self.stdout.getvalue()

        lines = self.stdout.getvalue().strip().splitlines()
        atts = {}
        for line in lines:
            (name,value) = line.split('=',1)
            atts[name] = value

        self.assert_(atts.has_key('username'))
        self.assertEquals('unittest', atts['username'])

        # now let it expire
        time.sleep(2)

        self.stdout = StringIO()
        vaoopenid.defoutstrm = self.stdout
        cl = "vaoopenid -c %s -s %s status" % (conffile, sessionid)
        sys.argv = cl.split()
        vaoopenid.main()
        lines = self.stdout.getvalue().strip().splitlines()

        data = {}
        for line in lines:
            (name, value) = line.split('=', 1)
            data[name] = value

        self.assert_(data.has_key('status'))
        self.assertEquals(data['status'], "ex")
        self.assertEquals(data['username'], "unittest")
        self.assertEquals(data['openid'], 
                          "https://%s/openid/id/unittest" % server)
        



__all__ = "VAOopenidTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()
