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
        log.info("No filter data for %s" % src)
        file_util.copy_file(src, dest, update=1)
        return

    log.debug('applying filter')
    with open(src) as istrm:
        with open(dest, 'w') as ostrm:
            filter_stream(istrm, ostrm, data)

class ext_build(build):

    _extra_build_options = \
        [('conf-dir=', None, 
          "default directory where the vaologin config file may be found"),
         ('home=', None, 
          "directory where vaologin will be installed"),
         ('with-support', None,
          "assume vaologin-support was installed into home; use libraries found there"),
         ('with-local-python-libs=', None,
          "colon-separated paths to directories containing shared libraries needed by python via LD_LIBRARY_PATH"),
         ('with-local-python-home=', None,
          "home directory for a local installation of python to use; DIR/bin/python will be the executable and DIR/lib will be added to LD_LIBRARY_PATH")]
    user_options = build.user_options + _extra_build_options

    def __init__(self, dist):
        build.__init__(self, dist)
        self.filter_files = filter(lambda f: f.startswith('etc/'),
                                   filter_files)
        self.filterdata = filter_data.copy()
        self.local_python_info = {}

    def initialize_options(self):
        build.initialize_options(self)
        self.home = None 
        self.conf_dir = None
        self.with_support = None
        self.with_local_python_home = None
        self.with_local_python_libs = None
        self.local_python_info = {}

    def finalize_options(self):
        build.finalize_options(self)

        # we can't use self.set_undefined_options() because the install cmd
        # tries to pull options from build (doh!) setting up a would-be 
        # infinite loop.  Instead, we'll pull the options we need by hand.  
        inst_cmd = self.distribution.get_command_obj('install')
        opts = \
          "home conf_dir with_support with_local_python_home with_local_python_libs".split()
        for att in opts:
            if getattr(self, att) is None:
                setattr(self, att, getattr(inst_cmd, att))

        if self.home is not None:
            self.filterdata['INSTALLDIR'] = self.home
        if self.conf_dir is not None:
            self.filterdata['CONFDIR'] = self.conf_dir

        if self.with_support and self.home:
            self.local_python_info['home'] = self.home
            libdir = os.path.join(self.home,'lib')
            if not self.with_local_python_home and \
               os.path.exists(os.path.join(self.home,"bin","python")):
                self.with_local_python_home = self.home
            elif self.with_local_python_libs:
                self.with_local_python_libs += ":"+libdir
            else:
                self.with_local_python_libs = libdir

        if self.with_local_python_home:
            self.local_python_info['exe'] = \
                os.path.join(self.with_local_python_home,'bin','python')
            if not self.executable:
                self.executable = self.local_python_info['exe']
            self.local_python_info['lib'] = \
                os.path.join(self.with_local_python_home,'lib')

        if self.with_local_python_libs:
            if self.local_python_info.get('lib'):
                self.local_python_info['lib'] = \
                    ":".join([self.with_local_python_libs,
                              self.local_python_info.get('lib')])
            else:
                self.local_python_info['lib'] = \
                    self.with_local_python_libs

            if not self.local_python_info.get('exe'):
                self.local_python_info['exe'] = "python"

    def run(self):
        build.run(self)
        self.filter_in_data()
        if self.local_python_info.get('lib'):
            self.wrap_python_scripts()

    def filter_in_data(self):
        for file in self.filter_files:
            if file.endswith(".in"):
                out = re.sub(r'.in$', '', file)
                if newer(file, out):
                    log.info("filtering %s -> %s", file, out)
                    filter_file(file, out, self.filterdata)

    def wrap_python_scripts(self):
        tmpl = "src/main/shell/python-wrapper.sh.in"
        for script in scripts:
            dest = os.path.join("bin", os.path.basename(script))
            if os.path.exists(dest):
                if self.is_python_script(dest):
                    log.info('wrapping %s', dest)
                    os.rename(dest, dest+".py")

            if newer(tmpl, dest):
                log.info("creating wrapper for %s", script)
                data = self.local_python_info.copy()
                data['script'] = os.path.basename(script)
                filter_file(tmpl, dest, data)

    def is_python_script(self, file):
        with open(file) as fd:
            line = fd.readline()
            if line and line.startswith('#!') and \
               line[2:].strip().split(None,1)[0].split(os.sep)[-1].startswith('python'):
                return True
        return False


class ext_install(install):

    user_options = install.user_options + \
        [('conf-dir=', None, 
          "default directory where the vaologin config file may be found"),
         ('executable=', "e", 
          "specify final destination interpreter path (build.py)"),
         ('with-support', None,
          "assume vaologin-support was installed into home; use libraries found there"),
         ('with-local-python-libs=', None,
          "colon-separated paths to directories containing shared libraries needed by python via LD_LIBRARY_PATH"),
         ('with-local-python-home=', None,
          "home directory for a local installation of python to use; DIR/bin/python will be the executable and DIR/lib will be added to LD_LIBRARY_PATH")]

    def initialize_options(self):
        install.initialize_options(self)
        self.conf_dir = None
        self.executable = None
        self.with_support = None
        self.with_local_python_home = None
        self.with_local_python_libs = None

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
            log.info("filtering %s -> %s" % (infile, outfile))
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

        # undo default setting of self.executable from build command
        # so that we can inherit it from the install command
        defexec = os.path.normpath(sys.executable)
        if self.executable == defexec:
            self.executable = None

        self.set_undefined_options('install', ('home', 'home'),
                                              ('executable', 'executable'))
        self.set_undefined_options('build',   ('home', 'home'),
                                              ('executable', 'executable'))

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
        self.mkpath(self.build_dir)

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
                "src/main/python/scripts/portal",
                "etc/vaologin-py.cfg.in"               ]    

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
      data_files=[('cacerts', filter(lambda f: os.path.isfile(f),
                                     map(lambda e: os.path.join('etc/cacerts',e),
                                         os.listdir('etc/cacerts'))) ), 
                  ('web', []), ('sys', []), ('var', []), 
                  ('conf', ['etc/vaologin-py.cfg']) ])
                  
