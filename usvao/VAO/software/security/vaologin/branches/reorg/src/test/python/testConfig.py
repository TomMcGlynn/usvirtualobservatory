#!/usr/bin/env python
"""
Tests of the config module
"""
from __future__ import with_statement

import pdb                              # we may want to say pdb.set_trace()
import os, sys, shutil, unittest, time

from vaologin import config, ConfigurationError

testdir = os.path.join(os.getcwd(), 'target', 'tmp')
scriptdir = os.path.join('src','test','python')

def septr(path):
    if os.sep != '/':
        path = path.translate('/', os.sep)
    return path

class ConfigTestCase(unittest.TestCase):

    def setUp(self):
        pass
    def tearDown(self):
        if os.environ.has_key('VAOLOGIN_CONF'):
            del os.environ['VAOLOGIN_CONF']
        if os.environ.has_key('VAOLOGIN_HOME'):
            del os.environ['VAOLOGIN_HOME']
        confdir = os.path.join(testdir, 'conf')
        if os.path.exists(confdir):
            shutil.rmtree(confdir)

    def testGetConfigLocations(self):
        # start with no env var, no args
        locs = config.getConfigLocations()
        self.assert_(locs and len(locs) > 2)
        self.assertEquals(septr("src/test/conf/vaologin.cfg"), locs[0])
        self.assertEquals("vaologin.cfg", locs[1])
        self.assertEquals(septr("/etc/httpd/conf/vaologin.cfg"), locs[2])
        self.assertEquals(3, len(locs))

        locs = config.getConfigLocations("goofy.conf")
        self.assert_(locs and len(locs) > 2)
        self.assertEquals(septr("src/test/conf/goofy.conf"), locs[0])
        self.assertEquals("goofy.conf", locs[1])
        self.assertEquals(septr("/etc/httpd/conf/goofy.conf"), locs[2])
        self.assertEquals(3, len(locs))

        os.environ['VAOLOGIN_CONF'] = \
            septr("/opt/sw/vaologin/1.0/cfg/vaologin.conf")
        os.environ['VAOLOGIN_HOME'] = septr("target")
        locs = config.getConfigLocations()
        self.assert_(locs and len(locs) > 3)
        self.assertEquals(septr("/opt/sw/vaologin/1.0/cfg/vaologin.conf"), 
                          locs[0])
        self.assertEquals(septr("target/conf/vaologin.cfg"), locs[1])
        self.assertEquals("vaologin.cfg", locs[2])
        self.assertEquals(septr("/etc/httpd/conf/vaologin.cfg"), locs[3])
        self.assertEquals(4, len(locs))
            
    def testLocateConfigFile(self):
        loc = config.locateConfigFile()
        self.assert_(loc is None)
        self.assertRaises(ConfigurationError, config.locateConfigFile, True)

        os.environ['VAOLOGIN_CONF'] = \
            os.path.join(os.getcwd(), 'examples', "vaologin.cfg")
        self.assert_(os.path.exists(os.environ['VAOLOGIN_CONF']))
        loc = config.locateConfigFile()
        self.assertEquals(loc, os.environ['VAOLOGIN_CONF'])

        confdir = os.path.join(testdir, 'conf')
        os.makedirs(confdir)
        shutil.copy(os.environ['VAOLOGIN_CONF'], confdir)
        os.environ['VAOLOGIN_HOME'] = testdir
        loc = config.locateConfigFile()
        # VAOLOGIN_CONF takes precedence
        self.assertEquals(loc, os.environ['VAOLOGIN_CONF'])

        del os.environ['VAOLOGIN_CONF']
        loc = config.locateConfigFile()
        self.assertEquals(loc, os.path.join(os.environ['VAOLOGIN_HOME'],
                                            'conf', 'vaologin.cfg'))

    def testGetConfig1(self):
        conffile = os.path.join(os.getcwd(), 'examples', "vaologin.cfg")
        self.assert_(os.path.exists(conffile))
        
        defaults = { 'foo': 'bar', 'vaologin.auth.statedir': '/var/vaologin' }
        cfg = config.getConfig(conffile, defaults)
        self._testConfig(cfg)

    def testGetConfig2(self):
        conffile = os.path.join(os.getcwd(), 'examples', "vaologin.cfg")
        self.assert_(os.path.exists(conffile))
        os.environ['VAOLOGIN_CONF'] = conffile

        defaults = { 'foo': 'bar', 'vaologin.auth.statedir': '/var/vaologin' }
        cfg = config.getConfig(defaults=defaults)
        self._testConfig(cfg)

    def testGetConfigFail(self):
        self.assertRaises(ConfigurationError, config.getConfig, 
                          None, None, True)

    def _testConfig(self, cfg):
        self.assert_(isinstance(cfg, dict))
        self.assertEquals('8', cfg['vaologin.portal.sessionlifehours'])
        self.assertEquals('./var', cfg['vaologin.auth.statedir'], )
        self.assertEquals('bar', cfg['foo'])

        self.assert_(not cfg.has_key('vaologin.portal.goob'))
        self.assertEquals('gurn', cfg.get('vaologin.portal.goob', 'gurn'))
        self.assert_(cfg.get('vaologin.portal.goob') is None)

if __name__ == "__main__":
    unittest.main()

