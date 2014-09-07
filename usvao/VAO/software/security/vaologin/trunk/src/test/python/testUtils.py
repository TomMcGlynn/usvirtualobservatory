#!/usr/bin/env python
"""
Tests of the config module
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os
import sys
import unittest
import time
import cStringIO
import logging
logging.basicConfig(level=logging.ERROR)

from vaologin import utils, ConfigurationError

testdir = os.path.join(os.getcwd(), 'target', 'tmp')

class UtilsTestCase(unittest.TestCase):

    def setUp(self):
        self.dir = os.path.join(testdir,'foo')
        self.deepdir = os.path.join(self.dir,'bar')

    def tearDown(self):
        if os.path.exists(self.deepdir):
            os.rmdir(self.deepdir)
        if os.path.exists(self.dir):
            os.rmdir(self.dir)

    def testEnsureDir1(self):
        self.assert_(not os.path.exists(self.dir) and 
                     not os.path.exists(self.deepdir))

        self.assertRaises(ConfigurationError, utils.ensureDir, 
                          self.deepdir, False)
        utils.ensureDir(self.deepdir, True)
        self.assert_(os.path.exists(self.deepdir))

    def testEnsureDir2(self):
        self.assert_(not os.path.exists(self.dir) and 
                     not os.path.exists(self.deepdir))

        utils.ensureDir(self.dir, False)
        self.assert_(os.path.exists(self.dir))

__all__ = "UtilsTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()


