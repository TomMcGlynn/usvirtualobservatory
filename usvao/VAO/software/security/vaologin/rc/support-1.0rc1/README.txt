vaologin support package:  provides prerequisite packages for vaologin

This optional package is a companion to the vaologin toolkit
(http://dev.usvao.org/vaologin), providing the software package
dependencies required by vaologin.  You will find this useful on
systems where prerequisite software must be installed in user space.
You might need to do this if you do not have root permission to
install system software or when your web server operates in a
restricted environment (via chroot) which does not include vaologin's
required prerequisites.

As of this version, this support package only provides the precursors
for vaologin python module and command-line tools.  These include:

   o  pycurl and its dependecies: curl, openssl, and zlib
   o  python-openid
   o  python -- the python language interpreter

The installation process allows you to control which of these get
installed.  

Do I Need To Install This Package?
==================================

A few quick checks will tell you if you need to install this support
package.  First check that you have python by starting the
interpreter, typing "python".  If you see:

  % python                      # <-- what you typed in
  python: Command not found.

then you need to install this package in its entirety.  If you see
something like:

   Python 2.6.5 (r265:79063, Sep 26 2013, 18:48:04) 
   [GCC 4.4.3] on linux2
   Type "help", "copyright", "credits" or "license" for more information.
   >>> 

you're in good shape.  Make sure that the Python version is at least
2.6.  

Now you can check if the required python modules are available via the
python prompt.  To look for pycurl, type, "import pycurl".  If you are
missing pycurl, you'll see something like this: 

   >>> import pycurl 
   Traceback (most recent call last):
     File "<stdin>", line 1, in <module>
   ImportError: No module named pycurl

If pycurl is installed, you will just see another python prompt with
no error message.  Note that to access the VAO Login Service's HTTPS
interface, pycurl must have SSL support built into it; this is usually
the case if the curl library was installed normally via the system's
software tools.  

You can also check to see if you have the python-openid package;
similarly:

   >>> import openid
   Traceback (most recent call last):
     File "<stdin>", line 1, in <module>
   ImportError: No module named openid

If you see this error message, you must at least install python-openid.  

Installation Instructions
=========================

The intention of this package is to install the prerequisites under
the same root directory that you will install vaologin into before
actually installing vaologin.  For ease of typing, we'll save this 
location to a shell variable:

   VAOLOGIN_HOME=/path/to/vaologin/home       # for bash users 
or 
   set VAOLOGIN_HOME=/path/to/vaologin/home   # for csh/tcsh users

To do the default installation, unpack the vaologin-support tar-ball,
change into the unpacked directory, and type the following two commands:

    ./configure --prefix=$VAOLOGIN_HOME
    make install

where $VAOLOGIN_HOME is the path to where you will install vaologin.  

Now you are ready to build and install vaologin; switch over to the
directory where the vaologin package has been unpacked.  When you
install, you need to indicate that you want it to use the
prerequisites you've just installed.  To do this, use the -e option
when installing: 

   $VAOLOGIN_HOME/bin/python setup.py install \
       --home=$VAOLOGIN_HOME -e $VAOLOGIN_HOME/bin/python 

Note that we used the special version of python that we just installed
via this vaologin-support package; this is important.  

Customizing Prerequisite Package Installation
---------------------------------------------

If you have any of the prerequisite packages already installed,
re-installing them via this vaologin-support package is fine; vaologin
will still work with the new versions.  However, you may not want to
install packages that are already available (or, rather, your system
administrator may rather you didn't).  In this case, it is possible to
control what gets installed via the configure script.

If you want to see what's going to get installed before you actually
install it, type the --list (-l) option to configure:

   % ./configure -l
   Installing the following packages:

   zlib: zlib-1.2.8.tar.gz
   openssl: openssl-1.0.1f.tar.gz
   curl: curl-7.34.0.tar.gz
   python: Python-2.7.6.tgz
   pycurl: pycurl-7.19.3.1.tar.gz
   python-openid: python-openid-2.2.5.tar.gz

The most typical variation is not to install the python interpreter,
so as to use the one available via the system.  To do this, use the
configure --without-python option:

   ./configure --prefix=$VAOLOGIN_HOME --without-python
   make install

Note that even if you don't build and install python into
$VAOLOGIN_HOME, a wrapper script that calls your python will be put into 
$VAOLOGIN_HOME/bin, which you must use both at build-time and
run-time.  In other words, install the core vaologin package the same
way as described above.  

   $VAOLOGIN_HOME/bin/python setup.py install \ 
       --home=$VAOLOGIN_HOME -e $VAOLOGIN_HOME/bin/python 

(This is necessary so that python can find the various libraries
you've installed under $VAOLOGIN_HOME/lib.)

Another typical variation is when you have all of the necessary
prerequisites (including pycurl and its dependencies) except
python-openid; you can just install that:

   ./configure --prefix=$VAOLOGIN_HOME --with-openid-only
   make install

Note that if this is all you need, you don't need to use the python
wrapper (but it's okay if you do); just install vaologin normally:

   python setup.py install --home=$VAOLOGIN_HOME

For more options, see type "configure --help".  


