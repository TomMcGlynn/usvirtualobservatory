#!/usr/bin/env python
from __future__ import with_statement
import sys, os, re, urlparse, glob, stat
from string import Template
from distutils.core import setup
from distutils import file_util, log, sysconfig
from distutils.dep_util import newer
from distutils.command.build_py import build_py
from distutils.command.build_scripts import build_scripts, first_line_re
from distutils.command.build import build
from distutils.command.install import install
from distutils.util import convert_path

repos_info = r"HeadURL:trunk"
setup_py_file = __file__

def get_version():
    m = re.match(r'^HeadURL:\s*([^:/]://\S+)', repos_info)
    if m:
        # it's a (repository) URL
        path = urlparse.urlsplit(m.group(1))[2]  # the path portion
        # assume the repo directory containing this file repesents the version
        return os.path.basename(os.dirname(path))
    elif repos_info.startswith('HeadURL:'):
        # the (default) value after HeadURL: is the version name
        return repos_info.split(':',1)[1]
    else:
        return repos_info  # shouldn't happen

scripts = os.path.join(*'src/main/python/scripts'.split('/'))
scripts = glob.glob(os.path.join(scripts,'*'))

def filter_stream(istrm, ostrm, data=None):
    for line in istrm:
        if data and line.find('$') >= 0:
            try:
                line = Template(line).substitute(**data)
            except ValueError:
                pass
            except KeyError:
                pass
        ostrm.write(line)

def filter_file(src, dest, data=None):
    if not data:
        file_util.copy_file(src, dest, update=1)
        return

    with open(src) as istrm:
        with open(dest, 'w') as ostrm:
            filter_stream(istrm, ostrm, data)

class ext_build(build):

    _extra_build_options = \
        [('conf-dir=', None, 
          "default directory where the vaologin config file may be found"),
         ('home=', None, 
          "directory where vaologin will be installed")]
    user_options = build.user_options + _extra_build_options

    def initialize_options(self):
        build.initialize_options(self)
        self.home = None
        self.conf_dir = None

    def finalize_options(self):
        build.finalize_options(self)

class ext_install(install):

    user_options = install.user_options + \
        [('conf-dir=', None, 
          "default directory where the vaologin config file may be found")]

    def initialize_options(self):
        install.initialize_options(self)
        self.conf_dir = None

class ext_build_py(build_py):

    user_options = build_py.user_options + ext_build._extra_build_options

    def __init__(self, dist):
        build_py.__init__(self, dist)
        self.filter_files = map(lambda f: os.path.join(*f.split('/')), 
                                filter_files)
        self.filterdata = filter_data.copy()

    def initialize_options(self):
        build_py.initialize_options(self)
        self.home = None
        self.conf_dir = None

    def finalize_options(self):
        build_py.finalize_options(self)

        self.set_undefined_options('install', ('home', 'home'),
                                              ('conf_dir', 'conf_dir'))
        self.set_undefined_options('build',   ('home', 'home'),
                                              ('conf_dir', 'conf_dir'))

        if self.home is not None:
            self.filterdata['INSTALLDIR'] = self.home
        self.filterdata['VERSION'] = self.distribution.get_version()
        if self.conf_dir is not None:
            self.filterdata['CONFDIR'] = self.conf_dir

    def copy_file(self, infile, outfile,
                  preserve_mode=1, preserve_times=1, link=None, level=1):
        if filter(lambda f: os.path.samefile(infile, f), self.filter_files) \
                and (self.filterdata.has_key('INSTALLDIR') or 
                     newer(infile, outfile) or newer(setup_py_file, outfile)):
            # print "FILTERING:", infile
            filter_file(infile, outfile, self.filterdata)
            return (outfile, True)
        build_py.copy_file(self, infile, outfile, preserve_mode, preserve_times,
                           link, level)

class ext_build_scripts(build_scripts):

    user_options = build_scripts.user_options + \
        [('home=', None, 
          "directory where vaologin will be installed")]

    def __init__(self, dist):
        build_scripts.__init__(self, dist)
        self.filter_files = map(lambda f: os.path.join(*f.split('/')), 
                                filter_files)
        self.filterdata = filter_data.copy()

    def initialize_options(self):
        build_scripts.initialize_options(self)
        self.home = None
        self.scripts_to_filter = None

    def finalize_options(self):
        build_scripts.finalize_options(self)

        self.set_undefined_options('install', ('home', 'home'))
        self.set_undefined_options('build',   ('home', 'home'))

        self.scripts_to_filter = \
            filter(lambda s: filter(lambda f: os.path.samefile(s, f), 
                                    self.filter_files),
                   self.scripts)
        self.scripts = filter(lambda s: s not in self.scripts_to_filter, 
                              self.scripts)

        if self.home is not None:
            self.filterdata['INSTALLDIR'] = self.home
        self.filterdata['VERSION'] = self.distribution.get_version()

    def run(self):
        build_scripts.run(self)
        if self.scripts_to_filter:
            self.filter_scripts()

    def filter_scripts(self):
        # adapted from copy_scripts()

        outfiles = []
        for script in self.scripts_to_filter:
            out = self.filter_script(script)
            if out:  outfiles.append(out)

        if os.name == 'posix':
            for file in outfiles:
                self.set_mode(file);

    def filter_script(self, script):
        # adapted from copy_scripts()
        adjust = 0
        script = convert_path(script)
        outfile = os.path.join(self.build_dir, os.path.basename(script))

        if not self.force and not newer(script, outfile):
            log.debug("not copying %s (up-to-date)", script)
            return None

        # Always open the file, but ignore failures in dry-run mode --
        # that way, we'll get accurate feedback if we can read the
        # script.
        try:
            f = open(script, "r")
        except IOError:
            if not self.dry_run:
                raise
            f = None
        else:
            first_line = f.readline()
            if not first_line:
                self.warn("%s is an empty file (skipping)" % script)
                return None

            match = first_line_re.match(first_line)
            if match:
                adjust = 1
                post_interp = match.group(1) or ''

        log.info("filtering %s -> %s", script, self.build_dir)
        if not self.dry_run:
            outf = open(outfile, "w")
            if adjust:
                if not sysconfig.python_build:
                    outf.write("#!%s%s\n" %
                               (self.executable,
                                post_interp))
                else:
                    outf.write("#!%s%s\n" %
                               (os.path.join(
                        sysconfig.get_config_var("BINDIR"),
                       "python%s%s" % (sysconfig.get_config_var("VERSION"),
                                       sysconfig.get_config_var("EXE"))),
                                post_interp))

            filter_stream(f, outf, self.filterdata)
            outf.close();
        f.close()

        return outfile

    def set_mode(self, file):
        if self.dry_run:
            log.info("changing mode of %s", file)
        else:
            oldmode = os.stat(file)[stat.ST_MODE] & 07777
            newmode = (oldmode | 0555) & 07777
            if newmode != oldmode:
                log.info("changing mode of %s from %o to %o",
                         file, oldmode, newmode)
                os.chmod(file, newmode)
        

filter_files = ["src/main/python/vaologin/config.py",
                "src/main/python/scripts/vaoopenid",
                "src/main/python/scripts/loginstatus",
                "src/main/python/scripts/portal"      ]    

filter_data = { }

setup(name='vaologin',
      version=get_version(),
      description='Python modules for VAO-compliant OpenID use by portals',
      packages=['vaologin', 'vaologin/cli'],
      package_dir={'': 'src/main/python'},
      scripts=scripts,
      cmdclass={'build_py':       ext_build_py,
                'build_scripts':  ext_build_scripts,
                'build':          ext_build,
                'install':        ext_install        },
      data_files=[('cacerts', ['etc/cacerts']), ('web', []), ('sys', []), 
                  ('var', []), ('conf', ['etc/vaologin-py.cfg']) ])
                  
