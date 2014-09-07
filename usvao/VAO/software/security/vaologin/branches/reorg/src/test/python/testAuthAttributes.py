#!/usr/bin/env python
"""
Tests of the config module
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, unittest, time

from vaologin.authenticate import Attributes

class AttributesTestCase(unittest.TestCase):

    def setUp(self):
        pass
    def tearDown(self):
        pass

    def testAttsAtts(self):
        self.assert_(hasattr(Attributes, 'USERNAME'))
        self.assert_(hasattr(Attributes, 'NAME'))
        self.assert_(hasattr(Attributes, 'PHONE'))
        self.assert_(hasattr(Attributes, 'EMAIL'))
        self.assert_(hasattr(Attributes, 'CERT'))
        # self.assert_(hasattr(Attributes, 'INSTITUTION'))
        # self.assert_(hasattr(Attributes, 'COUNTRY'))

        self.assert_(hasattr(Attributes, 'shortname'))
        self.assert_(Attributes.shortname.has_key(Attributes.USERNAME))
        self.assert_(Attributes.shortname.has_key(Attributes.NAME))
        self.assert_(Attributes.shortname.has_key(Attributes.PHONE))
        self.assert_(Attributes.shortname.has_key(Attributes.EMAIL))
        self.assert_(Attributes.shortname.has_key(Attributes.CERT))



__all__ = "AttributesTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()
