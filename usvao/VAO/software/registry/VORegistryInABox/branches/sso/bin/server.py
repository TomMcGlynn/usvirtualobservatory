#! /usr/bin/env python
#
import CGIHTTPServer
import BaseHTTPServer
import sys, os, os.path, shutil
from optparse import OptionParser

def run(reposdir, docdir, docpath='', cgipath="cgi-bin", 
        port=8080, host='localhost', tokdir=None, sampledir=None, 
        server_class=BaseHTTPServer.HTTPServer, handler_class=None):
    """
    start the web server for a given VOResource record repository (i.e. a 
    publishing registry.  
    @param reposdir         the directory containing the production VOResource 
                               records and user configuration files.
    @param docdir           a httpd document directory.  The CGI directory 
                               must appear somewhere below this directory
    @param docpath          the URL path under which static documents may be 
                               accessed.  A directory with this path must 
                               exist below the directory given by the docdir
                               directory unless the value is an empty string 
                               (the default).
    @param cgipath          the URL path under which CGI scripts may be 
                               accessed.  A directory with this path must 
                               exist below the directory given by the docdir
                               directory.  The default is "cgi-bin".
    @param port             the port the server should listen on
    @param host             the host to run the server on
    @param tokdir           a writeable directory where session tokens can 
                               written.  If not specified, a directory called 
                               "vopub.$$" where "$$" is the process ID will 
                               be created under /tmp.
    @param sampledir        the directory containing sample (non-production)
                               VOResource records and user config files.
    @param server_class     the HTTPServer class to run as the server
    @param handler_class    the HTTPServer request handler class
    """
    if not os.path.exists(reposdir):
        raise IOError("%s: repository directory not found" % reposdir)

    if cgipath is not None and hasattr(handler_class,'cgi_directories'):
        if not cgipath.startswith('/'): 
            cgipath = "/" + cgipath
        handler_class.cgi_directories = []
        handler_class.cgi_directories.append(cgipath)

    if tokdir is None:
        tokdir = os.path.join("/tmp", "vopub." + os.getpid())
        os.path.makedirs(tokdir)

    if not docpath.startswith('/'):
        docpath = "/" + docpath
    os.environ['VOPUB_WEBPATH'] = docpath
    os.environ['VOPUB_REPOSITORY'] = reposdir
    os.environ['VOPUB_TOKENDIR'] = tokdir
    if sampledir is not None:
        if not os.path.exists(sampledir):
            raise IOError("%s: sample repository directory not found" % 
                          sampledir)
        os.environ['VOPUB_PLAY_REPOSITORY'] = sampledir

    if handler_class is None:
#        handler_class = CGIHTTPServer.CGIHTTPRequestHandler
        handler_class = VOPubRequestHandler
        os.environ['VOPUB_HANDLE_AS_SERVER'] = VOPubRequestHandler.server_version

    if not os.environ.has_key('VOPUB_HOME'):
        if os.environ.has_key('PWD'):
            os.environ['VOPUB_HOME'] = os.environ['PWD']
        else:
            os.environ['VOPUB_HOME'] = "."

    os.chdir(docdir)
    httpd = server_class((host, port), handler_class)
    httpd.serve_forever()

def makeworkdir(parent, docdir, cgidir, docpath='vopub', 
                cgipath="cgi-bin/vopub", basename="vopub", dirname=None):
    """
    create a temporary working directory where the server can operate.  This
    directory will be called basename.$$ where $$ is the current process ID.
    basename is "vopub" by default.  
    @param parent    the directory to make the working directory under
    @param docdir    the directory containing the login page and stylesheets
                        needed by the Resource_Form.cgi script.
    @param cgidir    the directory containing the Resource_Form.cgi script.
    @param docpath   the URL path under which static documents should be 
                       accessed.  This is the path where the login page 
                       and stylesheets will be accessed.
    @param cgipath   the URL path under the  CGI scripts should be 
                       accessed.  This path be used to access the 
                       Resource_Form.cgi script.
    @return path     the full path of the working directory that was created.
    """

    # create the working directory
    if not os.path.exists(parent):
        os.path.makedirs(parent)
    if dirname is None:
        dirname = "%s.%d" % (basename, os.getpid())
    workdir = os.path.join(parent, dirname)
    os.mkdir(workdir)

    # create the token directory
    os.mkdir(os.path.join(workdir,"var"))

    # create the root of the document directory
    os.mkdir(os.path.join(workdir,"web"))

    # create the document path
    path = os.path.dirname(docpath)
    if len(path) > 0:
        os.makedirs(os.path.join(workdir,"web",path))
    base = os.path.basename(docpath)
    os.symlink(docdir, os.path.join(workdir,"web",docpath))

    # create the cgi path
    path = os.path.dirname(cgipath)
    if len(path) > 0:
        os.makedirs(os.path.join(workdir,"web",path))
    base = os.path.basename(cgipath)
    os.symlink(cgidir, os.path.join(workdir,"web",cgipath))

    return workdir

class VOPubRequestHandler(CGIHTTPServer.CGIHTTPRequestHandler):
    """
    This specialization allows the script to do portable redirects.
    It does this by overriding the send_response() method.
    """

    def send_response(self, code, message=None):
        """Send the response header and log the response code.
        """
        if message is not None and message == "Script output follows":
            self.log_request(code)
            print >> sys.stderr, "script will send header"
        else:
            CGIHTTPServer.CGIHTTPRequestHandler.send_response(self,code,message)

    

def main():
    parser = OptionParser(usage="usage %prog [options] [docpath [cgipath]]")
    parser.add_option("-n", "--host", dest="host", default='localhost',
                      help="the name of the server host (default: localhost)")
    parser.add_option("-p", "--port", dest="port", type="int", default=8080,
                      help="the port number to listen to (default: 8080)")
    parser.add_option("-i", "--installdir", dest="installdir", 
                      default=os.environ['PWD'],
                      help="the root directory for VORegInABox (default: $PWD)")
    parser.add_option("-d", "--docdir", dest="docdir", default=None,
                    help="the directory containing the VORegInABox static docs")
    parser.add_option("-s", "--sample", dest="sampleRep", default=None,
                      help="the sample repository directory")
    parser.add_option("-r", "--repos", dest="repos", default=None,
                      help="the production repository directory")
    parser.add_option("-c", "--cgidir", dest="cgidir", default=None,
                      help="the directory containing Resource_Form.cgi")
    parser.add_option("-t", "--tmpdir", dest="tmpdir", default="/tmp",
                      help="a writable directory for temporary files")
    parser.add_option("-w", "--workdir", dest="workdir", default=None,
                      help="a working directory created from a previous run")
    parser.add_option("-x", "--expunge", action="store_true", default=None,
                      dest="removework",
                      help="remove the working directory on exit")
    parser.add_option("-X", "--noExpunge", action="store_false", default=None,
                      dest="removework",
                      help="do not remove the working directory on exit")
    

    (opts, args) = parser.parse_args()

    if opts.repos is None: 
        opts.repos = os.path.join(opts.installdir, "data", "repos")
    if opts.sampleRep is None: 
        opts.sampleRep = os.path.join(opts.installdir, "data", "sample")
    if opts.docdir is None: 
        opts.docdir = os.path.join(opts.installdir, "web")
    if opts.cgidir is None: 
        opts.cgidir = os.path.join(opts.installdir, "cgi-bin")

    docpath = "vopub"
    if len(args) > 0: docpath = args[0]
    cgipath = "cgi-bin/vopub"
    if len(args) > 1: cgipath = args[1]

    if opts.workdir is None:
        opts.workdir = makeworkdir(opts.tmpdir, opts.docdir, opts.cgidir,
                                   docpath, cgipath)
    elif not os.path.exists(opts.workdir):
        parent = os.path.dirname(opts.workdir)
        dirname = os.path.basename(opts.workdir)
        opts.workdir = makeworkdir(parent, opts.docdir, opts.cgidir,
                                   docpath, cgipath, dirname=dirname)
    
    if not os.path.isabs(opts.docdir):
        opts.docdir = os.path.abspath(opts.docdir)
    if not os.path.isabs(opts.cgidir):
        opts.cgidir = os.path.abspath(opts.cgidir)
    if not os.path.isabs(opts.workdir):
        opts.workdir = os.path.abspath(opts.workdir)

    print "Working directory:", opts.workdir

    try:
        run(opts.repos, os.path.join(opts.workdir, "web"), docpath, cgipath, 
            opts.port, opts.host, os.path.join(opts.workdir, "var"),
            opts.sampleRep)
    except KeyboardInterrupt:
        if opts.removework is None:
            sys.stdout.write("Remove working dir, %s (y/n)? " % opts.workdir)
            sys.stdout.flush()
            if sys.stdin.readline().strip() == 'y':
                opts.removework = True
            else:
                opts.removework = False
    finally:
        if opts.removework:
            print "Removing working directory,", opts.workdir
            shutil.rmtree(opts.workdir)
        else:
            print "Working directory, %s, not removed." % opts.workdir
            

if __name__ == '__main__':
    main()

