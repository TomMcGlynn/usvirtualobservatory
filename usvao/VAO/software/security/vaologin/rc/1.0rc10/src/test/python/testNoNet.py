#!/usr/bin/env python
"""
Test all available tests that do not require access to the network.
"""
import sys, os, unittest
testdir = os.path.dirname(sys.argv[0])

tests = []
for t in [
    "testUtils",
    "testAuthAttributes",
    "testAuthSession",
    "testCGI",
    ]:
    tests += __import__(t).suite()
testsuite = unittest.TestSuite(tests)

def suite():
    return testsuite

if __name__ == "__main__":
    runner = None
    if len(sys.argv) > 1 and sys.argv[1]:
        # write XML results to a reports directory
        reportdir = sys.argv[1]

        try:
            import xmlrunner
            runner = xmlrunner.XMLTestRunner(output=reportdir)
        except ImportError, e:
            print >> sys.stderr, "Warning: XML reports not available"

    if not runner:
        runner = unittest.TextTestRunner()
        
    sys.exit(not runner.run(testsuite).wasSuccessful())

