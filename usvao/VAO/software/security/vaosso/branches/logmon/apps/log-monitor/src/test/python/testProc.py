#!/usr/bin/env python
"""
Tests of the scan module
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, re, unittest, time, urlparse, cgi, pickle
from cStringIO import StringIO
from datetime import datetime

import vaosso.logmon.scan as scan
import vaosso.logmon.processors as procs

testdir = os.path.join(os.getcwd(), "src", "test", "python")
if locals().has_key('__file__'):
    testdir = os.path.dirname(__file__)
catalineout = os.path.join(testdir, "catalina.out")

class CountMatchedTestCase(unittest.TestCase):

    def setUp(self):
        self.scanner = scan.LogScanner(catalineout)

    def tearDown(self):
        pass

    def testStartup(self):
        p = procs.CountMatched("Server startup")
        self.scanner.add_processor(p)
        self.scanner.scan()
        self.assertEquals(p.count, 1)

    def testMuliple(self):
        p = procs.CountMatched("WARNING: ")
        self.scanner.add_processor(p)
        self.scanner.scan()
        self.assertEquals(p.count, 4)






__all__ = "".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()


