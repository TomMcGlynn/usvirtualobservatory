#!/usr/bin/env python
"""
Tests of the config module
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, unittest, time

from vaologin.authenticate import Session, Attributes

sessionid = "1a2b3c45d678e9"
testdir = os.path.join(os.getcwd(), 'target', 'tmp')
sessiondir = os.path.join(testdir, "vaosessions")
sessionfile = os.path.join(sessiondir, sessionid)

class SessionTestCase(unittest.TestCase):

    def setUp(self):
        self.session = Session(sessionid, sessionfile)
        if not os.path.exists(sessiondir):
            os.mkdir(sessiondir)
    def tearDown(self):
        if os.path.exists(sessiondir):
            shutil.rmtree(sessiondir)

    def testCtor(self):
        self.assertEquals(sessionid, self.session.id)
        self.assertEquals(sessionid, self.session.data['id'])
        self.assertEquals(sessionfile, self.session.file)

    def testClearData(self):
        self.session.data['goob'] = 'gurn'
        self.assert_(self.session.data.has_key('goob'))
        self.assertEquals(sessionid, self.session.data['id'])

        self.session.clearData()
        self.assert_(not self.session.data.has_key('goob'))
        self.assertEquals(sessionid, self.session.data['id'])

    def testSetValid(self):
        self.assert_(not self.session.isValid())
        self.assert_(not self.session.validationNeeded())

        self.session.setValid(2)
        self.assert_(self.session.data.has_key('validSince'))
        self.assert_(self.session.data.has_key('validLifetime'))
        self.assertEquals(7200, self.session.data['validLifetime'])
        self.assert_(self.session.getValidTimeLeft() > 7100)
        self.assert_(self.session.isValid())
        self.assert_(not self.session.validationNeeded())
        self.session.data['validSince'] = 10000
        self.assert_(not self.session.isValid())

        self.session.setValid()
        self.assert_(self.session.data.has_key('validSince'))
        self.assert_(self.session.data.has_key('validLifetime'))
        self.assert_(self.session.isValid())
        self.session.data['validSince'] = 10000
        self.assert_(self.session.isValid())
        del self.session.data['validSince']
        self.assert_(not self.session.isValid())

        self.session.setValid(-2)
        self.assert_(self.session.data.has_key('validSince'))
        self.assert_(self.session.data.has_key('validLifetime'))
        self.assert_(not self.session.isValid())

    def testSetProperty(self):
        self.session.setProperty("goob", "gurn")
        self.session.setProperty("openid.claimed_id", 
                                 "http://sso.usvao.org/openid/id/unittest")
        self.assert_(self.session.data.has_key('goob'))
        self.assert_(self.session.data.has_key('openid.claimed_id'))
        self.assertEquals("gurn", self.session.data['goob'])
        self.assertEquals("http://sso.usvao.org/openid/id/unittest", 
                          self.session.data['openid.claimed_id'])

    def testAddAttributes(self):
        atts = { Attributes.NAME: "Raymond Plante",
                 "http://goob":   "gurn" }
        self.assertEquals(1, len(self.session.data.keys()))
        self.session.addAttributes(atts)
        self.assert_(self.session.data.has_key(Attributes.NAME))
        self.assert_(self.session.data.has_key("name"))
        self.assert_(self.session.data.has_key("http://goob"))
        self.assertEquals(4, len(self.session.data.keys()))
        self.assertEquals("Raymond Plante", self.session.data[Attributes.NAME])
        self.assertEquals("Raymond Plante", self.session.data["name"])
        self.assertEquals("gurn", self.session.data["http://goob"])

    def testSave(self):
        self.session.setValid(1)
        self.session.data['goob'] = 'gurn'
        self.assert_(not os.path.exists(self.session.file))
        self.assert_(os.path.exists(sessiondir))
        self.session.save()
        self.assert_(os.path.exists(sessionfile))
        self.assert_(self.session.data.has_key('goob'))
        self.assert_(self.session.data.has_key('validSince'))
        self.session.clearData()
        self.assert_(not self.session.data.has_key('goob'))
        self.assert_(not self.session.data.has_key('validSince'))
        self.session.reconstitute()
        self.assert_(self.session.data.has_key('goob'))
        self.assert_(self.session.data.has_key('validSince'))
        self.assertEquals('gurn', self.session.data['goob'])

        self.session.end()
        self.assert_(not os.path.exists(sessionfile))
        self.session.reconstitute()
        self.assert_(not self.session.data.has_key('goob'))
        self.assert_(not self.session.data.has_key('validSince'))

    def testValidationNeeded(self):
        self.assert_(not self.session.isValid())
        self.assert_(not self.session.validationNeeded())

        self.session.data['_openid_consumer_last_token'] = {}
        self.assert_(self.session.validationNeeded())

        self.session.clearData()
        self.session.setValid(1)
        self.session.addAttributes(None)
        self.session.save()
        self.assert_(not self.session.validationNeeded())

    def testMakeSession(self):
        session = Session.makeSession(sessiondir)
        session.data['goob'] = 'gurn'
        self.assert_(session.id)
        self.assertNotEquals(sessionid, session.id)

        sid = session.id
        session.save()

        sess = Session.makeSession(sessiondir, sid)
        self.assertEquals(sid, session.id)
        self.assertEquals('gurn', sess.data.get('goob'))
        

__all__ = "SessionTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()
