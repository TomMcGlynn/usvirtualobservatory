"""
A CGI script that checks whether the remote user is logged in to the portal
(as managed by portalcgi.py).  
"""
import os, sys, optparse, cgi, urlparse, re, pdb
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler

import vaologin.config as config
from vaologin.portal import LoginStatus
from vaologin.cli.utils import Log, FatalError
from vaologin import VAOLoginError

usage = "Usage: %prog [debug-options]"
description = """Check whether the user is logged in and return user info in JSON format."""

prog = "loginstatus"
deferrstrm = sys.stderr
defoutstrm = sys.stdout

configfile = None

def main():
    prog = os.path.basename(sys.argv[0])
    if prog.endswith(".py"):  prog = prog[:-3]

    cla = createCliParser(prog)
    (opts, args) = cla.parse_args()
    # updateEnv(opts, args)

    errstrm = deferrstrm
    outstrm = defoutstrm
    log = Log(prog, errstrm, outstrm, opts.quiet)

    # load and override the configuration
    conffile = opts.conffile or configfile
    conf = config.getConfig(conffile)

    if opts.data_path:
        conf["vaologin.cli.statedir"] = opts.data_path

    if opts.scripturl:
        # run as server
        conf['vaologin.auth.portalurlrealm'] = opts.scripturl
        httpd = PortalServer(conf, opts.scripturl)
        httpd.serve_forever()
        return

    sessionid = None
    if opts.sessionid:
        sessionid = opts.sessionid

    try:
        status = LoginStatus.fromSessionId(sessionid, conf)
        msg = status.toJSON()
        print >> outstrm, "Content-type: text/plain"
        print >> outstrm, "Cache-Control: no-store, no-cache"
        print >> outstrm, "Cache-Control: post-check=0, pre-check=0" # for IE
        print >> outstrm, "Pragma: no-cache\n"  # HTTP 1.0
        print >> outstrm, msg

    except VAOLoginError, ex:
        log.fatal(str(ex), 1)


def createCliParser(prog=prog):
    """
    configure the command line options
    @param prog   the name to assume for this script
    """
    parser = optparse.OptionParser(description=description, usage=usage)

    parser.add_option('-c', '--config', dest='conffile', default=None,
                      help="Use the config file with given path")

    parser.add_option('-s', '--session-id', dest='sessionid', default=None,
                      help="A session id as provided by the web service framework.  If not provided, a new one will be created.")

    parser.add_option('-S', '--as-server', dest='scripturl', default=None,
                      help="Run as a standalone server and use given base URL as the URL to the portal CGI script")

    parser.add_option('-q', '--quiet', dest="quiet", action="store_true",
                      default=False, help="suppress messages to standard error")

    parser.add_option('-d', '--data-path', dest='data_path',
                      help='Data directory for storing session state. ')

    return parser

    
