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
from vaosso.logmon.scan import TimestampMatcher as TM

tomcat_timestamp_pat = r'[A-Z][a-z]{2} \d\d?, \d{4} \d\d?:\d\d:\d\d [AP]M'
vaosso_timestamp_pat = r'\d{4}-\d\d?-\d\d? \d\d:\d\d:\d\d'
apache_timestamp_pat = r'\d\d?/[A-Z][a-z]{2}/\d{4}:\d\d:\d\d:\d\d'

tomcat_timestamp_re = re.compile(tomcat_timestamp_pat)
vaosso_timestamp_re = re.compile(vaosso_timestamp_pat)
apache_timestamp_re = re.compile(apache_timestamp_pat)

testdir = os.path.join(os.getcwd(), "src", "test", "python")
if locals().has_key('__file__'):
    testdir = os.path.dirname(__file__)
catalineout = os.path.join(testdir, "catalina.out")

class TimestampMatcherTestCase(unittest.TestCase):

    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testConvert(self):
        self.assertEquals(TM.date_format_to_repat(scan.tomcat_timestamp_format),
                          tomcat_timestamp_pat)
        self.assertEquals(TM.date_format_to_repat(scan.vaosso_timestamp_format),
                          vaosso_timestamp_pat)
        self.assertEquals(TM.date_format_to_repat(scan.apache_timestamp_format),
                          apache_timestamp_pat)
    
    def testApachePattern(self):
        # pdb.set_trace()
        pat = TM.date_format_to_repat(scan.apache_timestamp_format)
        pat = re.compile(pat)

        for s in ["27/Mar/2013:01:24:54", "01/Sep/2005:15:24:54" ]:
            m = pat.search(s)
            self.assertTrue(m is not None, s)
            self.assertEquals(m.group(), s)

        for s in ["27/Sept/2013:01:24:54", "Oct 1, 2005 11:24:54 +0200",
                  "27/Mar/2013", "11:24:54"                   ]:
            m = pat.search(s)
            self.assertTrue(m is None, s)

    def testTomcatPattern(self):
        # pdb.set_trace()
        pat = TM.date_format_to_repat(scan.tomcat_timestamp_format)
        pat = re.compile(pat)

        for s in ["Mar 27, 2013 1:24:54 AM", "Sep 1, 2005 11:24:54 PM" ]:
            m = pat.search(s)
            self.assertTrue(m is not None,s )
            self.assertEquals(m.group(), s)

        for s in ["Sept 27, 2013 1:24:54 AM", "Oct 1, 2005 11:24:54",
                  "Mar 27, 2013", "11:24:54 PM"                   ]:
            m = pat.search(s)
            self.assertTrue(m is None, s)

    def testTomcatPattern(self):
        # pdb.set_trace()
        pat = TM.date_format_to_repat(scan.tomcat_timestamp_format)
        pat = re.compile(pat)

        for s in ["Mar 27, 2013 1:24:54 AM", "Sep 1, 2005 11:24:54 PM" ]:
            m = pat.search(s)
            self.assertTrue(m is not None, s)
            self.assertEquals(m.group(), s)

        for s in ["Sept 27, 2013 1:24:54 AM", "Oct 1, 2005 11:24:54",
                  "Mar 27, 2013", "11:24:54 PM"                   ]:
            m = pat.search(s)
            self.assertTrue(m is None, s)

    def testVaossoPattern(self):
        # pdb.set_trace()
        pat = TM.date_format_to_repat(scan.vaosso_timestamp_format)
        pat = re.compile(pat)

        for s in ["2013-10-24 04:19:24", "2005-09-01 21:24:54" ]:
            m = pat.search(s)
            self.assertTrue(m is not None)
            self.assertEquals(m.group(), s)

        for s in ["Sept 27, 2013 1:24:54 AM", "2005-09-01 3:24:54",
                  "2005-09-01", "11:24:54",   ]:
            m = pat.search(s)
            self.assertTrue(m is None, s)

    def testRepatSyntax(self):

        f2p = lambda f: '^'+TM.fmt2pat[f]+'$'

        self.assertTrue(re.match(f2p("%a"), "Wed"))
        self.assertTrue(re.match(f2p("%a"), "Sat"))
        self.assertTrue(re.match(f2p("%a"), "Fri"))
        self.assertFalse(re.match(f2p("%a"), "Friday"))
        self.assertTrue(re.match(f2p("%A"), "Wednesday"))
        self.assertTrue(re.match(f2p("%A"), "Saturday"))
        self.assertTrue(re.match(f2p("%A"), "Friday"))
        self.assertTrue(re.match(f2p("%b"), "Jan"))
        self.assertTrue(re.match(f2p("%b"), "Apr"))
        self.assertTrue(re.match(f2p("%b"), "May"))
        self.assertFalse(re.match(f2p("%b"), "Sept"))
        self.assertTrue(re.match(f2p("%B"), "January"))
        self.assertTrue(re.match(f2p("%B"), "April"))
        self.assertTrue(re.match(f2p("%B"), "May"))
        self.assertTrue(re.match(f2p("%d"), "01"))
        self.assertTrue(re.match(f2p("%d"), "15"))
        self.assertTrue(re.match(f2p("%d"), "24"))
        self.assertTrue(re.match(f2p("%d"), "6"))
        self.assertTrue(re.match(f2p("%H"), "01"))
        self.assertTrue(re.match(f2p("%H"), "15"))
        self.assertTrue(re.match(f2p("%H"), "24"))
        self.assertTrue(re.match(f2p("%I"), "01"))
        self.assertTrue(re.match(f2p("%I"), "15"))
        self.assertTrue(re.match(f2p("%I"), "24"))
        self.assertTrue(re.match(f2p("%I"), "6"))
        self.assertTrue(re.match(f2p("%j"), "01"))
        self.assertTrue(re.match(f2p("%j"), "15"))
        self.assertTrue(re.match(f2p("%j"), "24"))
        self.assertTrue(re.match(f2p("%j"), "4"))
        self.assertTrue(re.match(f2p("%j"), "215"))
        self.assertTrue(re.match(f2p("%j"), "324"))
        self.assertTrue(re.match(f2p("%j"), "024"))
        self.assertTrue(re.match(f2p("%j"), "002"))
        self.assertFalse(re.match(f2p("%j"), "0021"))
        self.assertTrue(re.match(f2p("%m"), "01"))
        self.assertTrue(re.match(f2p("%m"), "12"))
        self.assertTrue(re.match(f2p("%m"), "3"))
        self.assertTrue(re.match(f2p("%M"), "01"))
        self.assertTrue(re.match(f2p("%M"), "59"))
        self.assertFalse(re.match(f2p("%M"), "8"))
        self.assertTrue(re.match(f2p("%p"), "AM"))
        self.assertTrue(re.match(f2p("%p"), "PM"))
        self.assertFalse(re.match(f2p("%p"), "TM"))
        self.assertFalse(re.match(f2p("%p"), "pm"))
        self.assertFalse(re.match(f2p("%p"), "pm"))
        self.assertTrue(re.match(f2p("%S"), "01"))
        self.assertTrue(re.match(f2p("%S"), "61"))
        self.assertFalse(re.match(f2p("%S"), "8"))
        self.assertTrue(re.match(f2p("%S"), "00"))
        self.assertTrue(re.match(f2p("%S"), "08"))
        self.assertTrue(re.match(f2p("%S"), "53"))
        self.assertFalse(re.match(f2p("%S"), "8"))
        self.assertTrue(re.match(f2p("%y"), "00"))
        self.assertTrue(re.match(f2p("%y"), "08"))
        self.assertTrue(re.match(f2p("%y"), "99"))
        self.assertFalse(re.match(f2p("%y"), "8"))
        self.assertTrue(re.match(f2p("%Y"), "2000"))
        self.assertTrue(re.match(f2p("%Y"), "1999"))
        self.assertFalse(re.match(f2p("%Y"), "08"))
        self.assertFalse(re.match(f2p("%Y"), "8"))
        self.assertTrue(re.match(f2p("%Z"), "CDT"))
        self.assertTrue(re.match(f2p("%Z"), "MST"))
        self.assertTrue(re.match(f2p("%Z"), "EDST"))
        self.assertTrue(re.match(f2p("%z"), "+2000"))
        self.assertTrue(re.match(f2p("%z"), "-0500"))
        self.assertTrue(re.match(f2p("%z"), "-0330"))
        self.assertTrue(re.match(f2p("%%"), "%"))

    def testTMmatches1(self):
        tm = TM("%Y-%m-%d")

        self.assertTrue(tm.matches("2005-09-01: process starts"))
        self.assertTrue(tm.matches("process starts on 2005-09-01"))
        self.assertFalse(tm.matches("Sept 24, 1995: process starts"))

    def testTMmatches2(self):
        tm = TM("%Y-%m-%d", "^Log =(%D):")

        self.assertTrue(tm.matches("Log =2005-09-01: process starts"))
        self.assertFalse(tm.matches(" Log =2005-09-01: process starts"))
        self.assertFalse(tm.matches("process starts on Log =2005-09-01:"))
        self.assertFalse(tm.matches("process starts on Log =2005-09-01"))
        self.assertFalse(tm.matches("Sept 24, 1995: process starts"))

    def testGetTomcatTime(self):
        tm = scan.tomcat_timestamp_matcher
        dt = tm.get_time("Mar 27, 2013 1:24:54 AM org.apache.catalina.core.AprLifecycleListener init")

        self.assertTrue(dt is not None, "Failed to match timestamp")
        self.assertEquals(dt.year, 2013)
        self.assertEquals(dt.month, 3)
        self.assertEquals(dt.day, 27)
        self.assertEquals(dt.hour, 1)
        self.assertEquals(dt.minute, 24)
        self.assertEquals(dt.second, 54)
        self.assertEquals(dt.microsecond, 0)

    def testGetVaossoTime(self):
        tm = scan.vaosso_timestamp_matcher
        dt = tm.get_time("2013-10-24 04:19:24,669 [TP-Processor13] INFO  jsp.purse.register - Registration page referred to from sso.usvao.org")

        self.assertTrue(dt is not None, "Failed to match timestamp")
        self.assertEquals(dt.year, 2013)
        self.assertEquals(dt.month, 10)
        self.assertEquals(dt.day, 24)
        self.assertEquals(dt.hour, 4)
        self.assertEquals(dt.minute, 19)
        self.assertEquals(dt.second, 24)
        self.assertEquals(dt.microsecond, 0)

    def testGetApacheTime(self):
        tm = scan.apache_timestamp_matcher
        dt = tm.get_time('127.0.0.1 - - [16/Sep/2011:09:57:51 -0500] "GET / HTTP/1.1" 200 485 "-" "Mozilla/5.0 (X11; Linux i686 on x86_64; rv:6.0) Gecko/20100101 Firefox/6.0"')

        self.assertTrue(dt is not None, "Failed to match timestamp")
        self.assertEquals(dt.year, 2011)
        self.assertEquals(dt.month, 9)
        self.assertEquals(dt.day, 16)
        self.assertEquals(dt.hour, 9)
        self.assertEquals(dt.minute, 57)
        self.assertEquals(dt.second, 51)
        self.assertEquals(dt.microsecond, 0)
        # self.assertTrue(dt.tzinfo is not None)

class LogScannerTestCase(unittest.TestCase):

    def setUp(self):
        self.counter = scan.CountAll()
        self.scanner = scan.LogScanner(catalineout, self.counter)

    def tearDown(self):
        pass

    def testAddProcessor(self):
        procs = self.scanner.processors
        self.assertTrue(len(procs) > 0, "Empty processor list returned")
        self.assertTrue(procs[0] is self.counter)
        self.assertEquals(len(procs), 1)

        null = scan.NullProcessor()
        self.scanner.add_processor(null)
        procs = self.scanner.processors
        self.assertTrue(len(procs) > 0, "Empty processor list returned")
        self.assertTrue(procs[0] is self.counter)
        self.assertEquals(len(procs), 2)
        self.assertTrue(procs[1] is null)

    def testScan(self):
        self.scanner.scan()
        self.assertEquals(self.counter.count, 45)

    def testGetTimestamp(self):
        self.assertEquals(self.scanner._get_timestamp("Mar 27, 2013 1:24:54 AM org.apache.catalina.core.AprLifecycleListener init"),
                          datetime(2013, 3, 27, 1, 24, 54))
        self.assertEquals(self.scanner._get_timestamp("2013-10-23 17:54:39,826 [TP-Processor14] INFO  jsp.purse.process - Attempting register user goober"),
                          datetime(2013, 10, 23, 17, 54, 39))

    def testScanUntil1(self):
        self.scanner.scan(until=datetime(2013, 6, 3, 14, 0, 0));
        self.assertEquals(self.counter.count, 35)

    def testScanUntil2(self):
        self.scanner.scan(until=datetime(2014, 6, 3, 14, 0, 0));
        self.assertEquals(self.counter.count, 45)

    def testScanUntil3(self):
        self.scanner.scan(until=datetime(2012, 6, 3, 14, 0, 0));
        self.assertEquals(self.counter.count, 0)

    def testSkip(self):
        self.scanner.seek_past_date(datetime(2013, 6, 3, 14, 0, 0));
        self.scanner.scan()
        self.assertEquals(self.counter.count, 10)



__all__ = "TimestampMatcherTestCase LogScannerTestCase".split()
def suite():
    tests = []
    for t in __all__:
        tests.append(unittest.makeSuite(globals()[t]))
    return unittest.TestSuite(tests)

if __name__ == "__main__":
    unittest.main()
