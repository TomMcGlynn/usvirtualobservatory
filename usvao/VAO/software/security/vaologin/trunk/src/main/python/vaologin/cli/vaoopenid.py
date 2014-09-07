"""
Command line implementation of OpenID Relying Party.

Based on consumer.py in Janrain, Inc., OpenID implementation examples
(c. 2005-2008, Janrain, Inc.)
"""
import os, sys, optparse, cgi, urlparse, re

import vaologin.config as config
from vaologin.authenticate import VAOLoginRequest, Attributes
from vaologin.cli.utils import Log, FatalError
from vaologin import VAOLoginError

usage = "Usage: %prog [options] request|process|status [return_url]"
description = """Authenticate a web service user via the OpenId protocol.  When either the request or process argument is specified, the return_url argument is required."""

prog = "vaoopenid"
deferrstrm = sys.stderr
defoutstrm = sys.stdout

operations = ["request", "process", "status", "end" ]
opREQUEST = operations[0]
opPROCESS = operations[1]
opSTATUS  = operations[2]
opEND     = operations[3]

configfile = None

def main():
    prog = os.path.basename(sys.argv[0])
    if prog.endswith(".py"):  prog = prog[:-3]

    cla = createCliParser(prog)
    (opts, args) = cla.parse_args()

    if opts.verbose and opts.quiet:  opts.verbose = False
    errstrm = deferrstrm
    outstrm = defoutstrm
    log = Log(prog, errstrm, outstrm, opts.quiet)
    if opts.quiet:  errstrm = None

    if len(args) < 1:
        log.error("Missing operation argument")
        cla.print_usage(errstrm)
        log.fatal(None, 1)

    # do a minimum match on the case-insensitive operation provided
    op = args[0].lower()
    op = filter(lambda p: p.startswith(op), operations)
    if len(op) != 1:
        log.fatal("Unrecognized operation: " + args[0])
    op = op[0]
    return_url = None

    if op != opSTATUS and op != opEND: 
        if len(args) < 2:
            log.fatal("Missing return_url argument")
            log.fatal(None, 1)
        return_url = args[1]

    if len(args) > 2:
        log.error("Warning: ignoring extra arguments: " + str(args[2:]))

    # load and override the configuration
    conffile = opts.conffile or configfile
    conf = config.getConfig(conffile)
    if opts.data_path:
        conf["vaologin.cli.statedir"] = opts.data_path

    # ask for attributes
    atts = None
    if opts.attributes:
        atts = shortAttNamesToUrls(re.split(r'\s*,?\s*', opts.attributes))

    req = VAOLoginRequest(conf, opts.sessionid, errstrm)

    try:
        if op == opREQUEST:
            log.tell(req.requestAuthentication(return_url, atts, opts.userid))

        elif op == opPROCESS:
            if not req.getSession().validationNeeded():
                log.fatal("No state from request step found for sessionid=%s" %
                          req.sessionid, 3)
            qargs = parseQueryArgs(return_url, opts.querystring)

            resp = req.processAuthentication(qargs, return_url, opts.lifetime)
            if resp.protocolFailed():
                log.fatal("OpenId protocol failure", 4)
            elif resp.wasCanceled():
                log.fatal("User canceled login (session id=%s)"% req.sessionid,
                          2)
            elif resp.neededUserInput():
                log.fatal("non-interactive authentication not possible", 5)
            elif not resp.isAuthenticated():
                log.fatal("unknown authentication failure", 6)

            # cache the credential if retrieved.
            credfile = opts.cred_file
            if resp.credentialAvailable():
                credfile = resp.cacheCredential(credfile)

            # tell the attributes returned
            atts = resp.getAttributes()
            for att in atts.keys():
                value = atts[att]
                if Attributes.shortname.has_key(att):
                    print >> outstrm, "%s=%s" % \
                        (Attributes.shortname[att], value)
                else:
                    print >> outstrm, "%s=%s" % (att, value)
            if credfile:
                print >> outstrm, "certfile=%s" % credfile

        elif op == opSTATUS:
            sess = req.getSession()

            if sess is None or sess.isNew():
                print >> outstrm, "status=out"

            elif sess.validationNeeded():
                print >> outstrm, "status=incomplete"

            elif sess.isValid():
                print >> outstrm, "status=in"
                print >> outstrm, "username=%s" % sess.data.get('username')
                print >> outstrm, "openid=%s" % sess.data.get('openid.identity')
                secLeft = sess.getValidTimeLeft()
                if secLeft is not None:
                    print >> outstrm, "secLeft=%d" % secLeft

            else:
                print >> outstrm, "status=ex"
                print >> outstrm, "username=%s" % sess.data.get('username')
                print >> outstrm, "openid=%s" % sess.data.get('openid.identity')

        elif op == opEND:
            req.getSession().end()

    except VAOLoginError, ex:
        log.fatal(str(ex), 7)
            
def shortAttNamesToUrls(atts):
    if not atts:
        return atts
    # convert to URI names
    uriname = {}
    for k in Attributes.shortname.keys():
        uriname[Attributes.shortname[k]] = k
    uriname['cert'] = Attributes.CERT
    return map(lambda a: (a in uriname.keys() and uriname[a]) or a,atts)

def parseQueryArgs(url, qstr=None):
    """
    parse out the query arguements.  
    @param url   the URL that may include ?-appended query arguments
    @parm qstr   the query argument string, as encoded in the QUERY_STRING
                    environment variable set by the CGI.  If None, the 
                    url will be parsed for the arguments.
    """
    if not qstr:
        qstr = urlparse.urlsplit(url)[3]
    qargs = cgi.parse_qs(qstr)
    for arg in qargs.keys():
        qargs[arg] = qargs[arg][0]
    return qargs

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

    parser.add_option('-Q', '--query-string', dest='querystring', default=None,
                      help="The query string passed as part of the returned URL")

    parser.add_option('-d', '--data-path', dest='data_path',
                      help='Data directory for storing session state. ')

    parser.add_option('-a', '--attributes', dest='attributes', 
                      help='User attributes to request (which the user may veto), given as a comma-separated string; either the short name or URI can be provided')

#    parser.add_option('-A', '--opt-attributes', dest='optattributes', 
#                      help='Optional attributes needed about user as comma-separated string; either the short name or URI can be provided')

    parser.add_option('-f', '--cred-file', dest='cred_file',
            help='Credential file to store X.509 certificate to (if requested via -a). ')

    parser.add_option('-u', '--user-id', dest='userid',
                      help='User-supplied identifier to login in as. This can be a VAO login name or an OpenId URI' )

    parser.add_option('-l', '--session-lifetime', dest="lifetime",
                      type="float", default=None,
                      help="create a session with a lifetime in decimal hours.  Used only by the 'process' operation")

    parser.add_option('-q', '--quiet', dest="quiet", action="store_true",
                      default=False, help="suppress messages to standard error")

    parser.add_option('-v', '--verbose', dest="verbose", action="store_true",
                      default=False, help="provide extra messages to standard error")

    return parser


if __name__ == "__main__":
    try:
        main()
    except FatalError, e:
        sys.exit(e.code)
#    except Exception, e:
#        print >> deferrstrm, "%s: Unexpected fatal error: %s" % (prog, str(e))
#        sys.exit(10)
