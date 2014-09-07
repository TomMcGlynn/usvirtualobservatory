#!usr/bin/env python
from distutils.core import setup, Extension

import os
command = 'make'
os.system(command)
setup(name="VOClient python wrappers",
		version='1.0',
		description="VOClient python wrappers",
		packages=["voclient"],
		package_dir={'voclient': 'python/voclient' },
	        ext_modules=[Extension('voclient._libvot', ['libvotable/votParse.i'], swig_opts=['-Ilibvotable'],
			             define_macros=[('SWIG_PYTHON_SILENT_MEMLEAK', '1')],
			             library_dirs=['lib'], libraries=['VOTable', 'curl', 'expat']),
		             Extension('voclient._VOClient', ['libvoclient/VOClient.i'], swig_opts=['-Ilibvoclient' ] ,
			             define_macros=[('SWIG_PYTHON_SILENT_MEMLEAK', '1')],
				     library_dirs=['lib'], libraries=['cfitsio', 'VOClient', 'VOTable', 'curl']),
		             Extension('voclient._libsamp', ['libsamp/samp.i'], swig_opts=['-Ilibsamp' ] ,
			             define_macros=[('SWIG_PYTHON_SILENT_MEMLEAK', '1')],
				     include_dirs=['libsamp/libxrpc', 'libsamp/libxrpc/include'],
				     library_dirs=['lib'], libraries=['samp', 'curl'])],
		py_modules=['VOClient', 'libvot'] 
		)
setup(name="voclient libraries",
		version='1.0',
		packages=["voclient"],
		package_dir={'voclient': 'libvotable'},
		)
setup(name="voclient libraries",
		version='1.0',
		packages=["voclient"],
		package_dir={'voclient': 'libvoclient'},
		)
setup(name="voclient libraries",
		version='1.0',
		packages=["voclient"],
		package_dir={'voclient': 'libsamp'},
		)
