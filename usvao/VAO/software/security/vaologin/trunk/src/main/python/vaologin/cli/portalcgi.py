"""
A CGI script implementation of a simple portal whose contents are protected
by VAO logins.  
"""
import os, sys, optparse, cgi, urlparse, re, pdb
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler

import vaologin.config as config
from vaologin.portal import ProtectedPortal
from vaologin.cli.utils import Log, FatalError
from vaologin import VAOLoginError

usage = "Usage: %prog [debug-options]"
description = """Ensure that user is logged in before delivering protected content"""

prog = "portal"
deferrstrm = sys.stderr
defoutstrm = sys.stdout

configfile = None

def main():
    prog = os.path.basename(sys.argv[0])
    if prog.endswith(".py"):  prog = prog[:-3]

    cla = createCliParser(prog)
    (opts, args) = cla.parse_args()
    updateEnv(opts, args)

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
        portal = ProtectedPortal(conf, None, sessionid,
                                 ostrm=outstrm, estrm=errstrm)
        portal.enter()

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

def updateEnv(opts, args):
    pass

class PortalRequestHandler(BaseHTTPRequestHandler):
    """
    a handler for running this CGI service as a stand-alone web server
    """
    def do_GET(self):
        if not self.path.startswith(self.server.script_name):
            self.send_error(300, "%s: Not found" % self.path)
            return

        pathinfo = self.path[len(self.server.script_name):]
        pathinfo = pathinfo.split('?', 1)
        os.environ['QUERY_STRING'] = ''
        if len(pathinfo) > 1:
            os.environ['QUERY_STRING'] = pathinfo[1]
        pathinfo = pathinfo[0]

        os.environ['SERVER_NAME'] = self.server.ipname
        os.environ['SERVER_PORT'] = str(self.server.port)
        os.environ['SERVER_PROTOCOL'] = self.request_version
        os.environ['REQUEST_METHOD'] = self.command
        os.environ['PATH_INFO'] = pathinfo
        os.environ['SCRIPT_NAME'] = self.server.script_name
        os.environ['HTTP_COOKIE'] = self.headers.getheader('Cookie','')
        if not os.environ['HTTP_COOKIE']:  del os.environ['HTTP_COOKIE']

        portal = ProtectedPortal(self.server.conf, asServer=True, 
                                 execScripts=False,
                                 ostrm=self.wfile, estrm=deferrstrm)

        # self.send_response(200, "OK")
        portal.enter()
        self.wfile.close()


class PortalServer(HTTPServer):
    """
    a custom web server for demonstrating this CGI service.  This server is 
    not recommended for production use as it is not validated as sufficiently 
    secure.
    """
    def __init__(self, config, scripturl, 
                 RequestHandlerClass=PortalRequestHandler):
        parts = urlparse.urlsplit(scripturl)
        if not parts.path:
            raise RuntimeError("Script URL needs to include file path: " + 
                               baseurl)
        srvr = parts.netloc.split(':')
        if len(srvr) < 2:
            srvr.append('80')
        srvr = (srvr[0], int(srvr[1]))

        HTTPServer.__init__(self, srvr, RequestHandlerClass)
        self.conf = config
        self.script_name = parts.path
        self.ipname = srvr[0]
        self.port = srvr[1]

if __name__ == "__main__":
    try:
        main()
    except FatalError, e:
        sys.exit(e.code)
#    except Exception, e:
#        print >> deferrstrm, "%s: Unexpected fatal error: %s" % (prog, str(e))
#        sys.exit(10)
