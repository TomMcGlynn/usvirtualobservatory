#! /usr/bin/env python
#
import form
import CGIHTTPServer
import BaseHTTPServer
import cgi

class CGIServicesServer(BaseHTTPServer.HTTPServer):

    def __init__(self, server_address, servmap=None, docdirmap=None, 
                 basepath=None, docroot=None):
        """
        Create a server that can provide services and serve documents.
        The arguments define URLs that will resolve to a response. 
        @param server_address   a pair giving the server name and port
        @param servmap          a dictionary where the keys are relative 
                                   URL paths and values are functions.  See
                                   addService() for a description of the 
                                   assumed signature.
        @param docdirmap        a dictionary where the keys are URL paths
                                   and values are actual filesystem 
                                   directories.  
        @param basepath         a base path to prepend to all specific URL
                                   paths, including those added later via
                                   addDocDir() and addService().
        @param docroot          the base directory where static documents 
                                   can be found.  This path will be prepended
                                   to all documents registered with relative 
                                   paths.
        """
        self.basepath = basepath
        self.docroot = docroot

        if servmap is None:  servmap = {}
        self.servmap = servmap.copy()
        if docdirmap is None:  docdirmap = {}
        self.docdirmap = docdirmap.copy()

    def addDocDir(self, urlpath, filedir):
        """
        add a mapping of a URL path to a directory in the filesystem.
        @param urlpath     a relative path to look for in URL requests.  
                              If the basepath was set in the constructor,
                              this path will be taken to be relative to 
                              that base path.
        @param filedir     a path to a filesystem directory containing 
                              static files.  If the docroot was set in 
                              the constructor, a relative filedir path 
                              will be taken to be relative to the docroot
                              directory.
        """
        self.docdirmap[urlpath] = filedir

    def addService(self, urlpath, cgifunc):
        """
        add a mapping of a URL path to a function that will provide a CGI 
        service.  
        @param urlpath     a relative path to look for in URL requests.  
                              If the basepath was set in the constructor,
                              this path will be taken to be relative to 
                              that base path.
        @param cgifunc     a function that provides the CGI service.  The 
                              function should take two arguments; the 
                              first is a dictionary of environment variables, 
                              and the second is the request handler object.
        """
        self.servmap[urlpath] = cgifunc


class CGIServicesHandler(CGIHTTPServer.CGIHTTPRequestHandler):

    def __init__(self, request, client_address, server):
        CGIHTTPServer.CGIHTTPRequestHandler.__init__(self, request, 
                                                     client_address, server)

    def do_GET():
        environ = {}
        

