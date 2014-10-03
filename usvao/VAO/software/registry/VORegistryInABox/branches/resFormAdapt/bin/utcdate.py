#!/usr/bin/python
#
import datetime, time, re, sys, os
from optparse import OptionParser

def now(offset=0):
    """
    return a VOResource-legal timestamp (an iso8601-formated UTC)
    @param offset  offset to apply to the current time in seconds
    """
    t = time.time() + offset
    return datetime.datetime.utcfromtimestamp(t).isoformat()[:-4]

class OptionParserWithErrorHandler(OptionParser):
    """
    an extension of the standard optparse.OptionParser that adds an
    error handling function
    """
    def error(self, msg, exitval=None, ostrm=sys.stderr):
        show = ""
        if self.prog is not None:
            show += "%s: " % prog
        show += msg
        print >> ostrm, show
        if exitval is not None:
            sys.exit(exitval)

def makeCLI():
    """return a configured OptionParser instance for the updatetime script"""
    usage="usage: %prog [-h -o offset]"
    desc="""return the current UTC time plus/minus an optional offset"""

    cl = OptionParserWithErrorHandler(usage=usage, description=desc)
    cl.add_option("-o", "--offset", action="store", dest="offset", default=None,
                  help="""the offset to apply to the current time.  
If negative, the resulting time will be in the past.  The value is taken as seconds unless followed (with spaces) by a unit modifier: s (seconds), m (minutes), h (hours), d (days); e.g. "+40h" for 40 hours in the future.
""")

    return cl

offmod = { "s": 1,
           "m": 60,
           "h": 60*60,
           "d": 24*60*60 }
def offsetToSecs(offset):
    """
    convert the input offset string to a value in seconds.  It can be 
    negative or explicitly positive.  It can be followed by a unit modifier:
    s (seconds), m (minutes), h (hours), d (days); e.g. "+40h" for 40 hours 
    in the future.
    """
    fact = 1
    offset = offset.strip()
    if offset[-1].isalpha():
        try: 
            fact = offmod[offset[-1]]
        except KeyError:
            raise ValueError("%s: contains unrecognized unit modifier: %s" %
                             (offset, offset[-1]))
        offset = offset[:-1]

    return float(offset) * fact

def main():
    cli = makeCLI();
    (cli.opts, cli.args) = cli.parse_args()

    offset = 0
    if cli.opts.offset:
        try:
            offset = offsetToSecs(cli.opts.offset)
        except ValueError, e:
            cli.error(str(e), 1)

    print now(offset)
    

if __name__ == '__main__':
    main()




