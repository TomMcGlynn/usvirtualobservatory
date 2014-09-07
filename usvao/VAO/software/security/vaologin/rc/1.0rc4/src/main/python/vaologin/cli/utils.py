"""
utility functions for handling cli scripts
"""

import os, sys
import traceback as tb;

class FatalError(Exception):
    """
    An exception to throw if the script should exit for a fatal error.
    The attribute, code, provides the exit code that the script should 
    exit with.
    """
    def __init__(self, msg, exitcode=1):
        """
        create the exception
        @param msg   the error message
        @param code  the exit code
        """
        if msg is None:
            msg = 'unspecified problem'
        Exception.__init__(self, "FATAL: " + msg)
        self.code = exitcode

class Log(object):
    """
    functions for sending error messages
    """
    def __init__(self, prog, estrm=sys.stderr, ostrm=sys.stdout, quiet=False):
        """
        create the log, sending the messages to an error stream
        """
        self.err = estrm
        self.out = ostrm
        self.quiet = quiet
        self.prog = prog

    def error(self, msg):
        """
        send a message to the error stream
        """
        if msg and not self.quiet:
            print >> self.err, "%s: %s" % (self.prog, msg)

    def fatal(self, msg, exitcode=1):
        """
        send a message to the error stream and raise a FatalError exception
        with a given exitcode.
        """
        self.error(msg)
        raise FatalError(msg, exitcode)

    def tell(self, msg):
        """
        send a message to the output stream
        """
        print >> self.out, msg

def run(mainfunc, errstrm=sys.stderr):
    try:
        mainfunc()
    except FatalError, e:
        sys.exit(e.code)
    except Exception, e:
        prog = os.path.basename(sys.argv[0])
        for line in tb.format_tb(sys.exc_info()[2]):
            print >> errstrm, line
        print >> errstrm, "%s: Unexpected fatal error: %s" % (prog, str(e))
        sys.exit(10)
