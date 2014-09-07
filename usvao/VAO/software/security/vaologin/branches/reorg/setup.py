#!/usr/bin/env python
from __future__ import with_statement
import sys, os, re, urlparse, glob
from string import Template
from distutils.core import setup
from distutils import file_util
from distutils.dep_util import newer
from distutils.command.build_py import build_py

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

class ext_build_py(build_py):
    def __init__(self, dist):
        build_py.__init__(self, dist)
        self.filter_files = map(lambda f: os.path.join(*f.split('/')), 
                                filter_files)
        self.filterdata = filter_data.copy()

    def initialize_options(self):
        build_py.initialize_options(self)
        self.install_dir = None

    def finalize_options(self):
        build_py.finalize_options(self)

        self.set_undefined_options('install', ('home', 'install_dir'))

        self.filterdata['INSTALLDIR'] = self.install_dir
        if self.filterdata['INSTALLDIR'] is None:
            del self.filterdata['INSTALLDIR']
        self.filterdata['VERSION'] = self.distribution.get_version()

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

filter_files = ["src/main/python/vaologin/config.py"]    

filter_data = { }

setup(name='vaologin',
      version=get_version(),
      description='Python modules for VAO-compliant OpenID use by portals',
      packages=['vaologin', 'vaologin/cli'],
      package_dir={'': 'src/main/python'},
      scripts=scripts,
      cmdclass={'build_py': ext_build_py}
      )
