#! /usr/bin/env python
#
"""
This module handles web queries and posts to view and manipulate the 
repository.
"""

from repository import Repository, AuthenticationFailed, \
                       NoSuchUser, configFileTmpl, makeRepository
from repository import rischema, rbxschema, stcschema, xlinkschema
from utils import VORegInABoxError
from nicedom import xpath
from xml.sax import saxutils
import nicedom
import os, os.path, re, datetime, sys, time, urllib, cgi
import Cookie
import traceback as tb

tokenFileTmpl = "VORegSession%s.txt"
newOrgFileTmpl = "VORegNewOrg%s.txt"

schemas = { "ri":     rischema,
            "rbx":    rbxschema,
            "stc":    stcschema,
            "xlink":  xlinkschema,
            "xsi":    "http://www.w3.org/2001/XMLSchema-instance",
            "vr":     "http://www.ivoa.net/xml/VOResource/v1.0",
            "vs":     "http://www.ivoa.net/xml/VODataService/v1.0",
            "vg":     "http://www.ivoa.net/xml/VORegistry/v1.0",
            "sia":    "http://www.ivoa.net/xml/SIA/v1.0",
            "ssa":    "http://www.ivoa.net/xml/SSA/v0.4",
            "cs":     "http://www.ivoa.net/xml/ConeSearch/v1.0",
            "sn":     "http://www.ivoa.net/xml/SkyNode/v0.2"
            }

class CGIError(VORegInABoxError):
    """a general error while manipulating the repository through the web."""
    def __init__(self, msg):
        VORegInABoxError.__init__(self, msg)

class SessionExpired(CGIError):
    """an exception indicating that the service session has expired"""
    def __init__(self, user, token='', msg=None):
        self.user = user
        self.token = token
        if msg is None:  
            msg = "Session %s has apparently expired for %s" % (token, user)
        VORegInABoxError.__init__(self, msg)

class RepositoryService: 

    def __init__(self, reposdir, tokendir, webpath, cgidata, asServer=None):
        """
        create a service class for manipulating a repository
        @param reposdir   the repository directory (containing the user's 
                             config file).
        @param tokendir   the directory where cached session tokens can be 
                             written and read.  
        @param webpath    the URL base path to static documents (e.g. 
                             stylesheets) referenced by responses from this 
                             script.  
        @param cgidata    the CGI input parameters as returned by the cgi 
                             module.  
        @param asServer   the server name and version to assume.  If set, this 
                             service will assume it is responsible for sending 
                             the full HTTP header.  
        """
        self.dir = reposdir
        self.tokendir = tokendir
        self.webpath = webpath
        self.cgi = cgidata
        self.r = None
        self.handleHeaderAsServer = asServer
        self.cookie = None

    def openRepositoryWithPassword(self, user, password):
        """
        open and return a Repository object, authenticating with a password.
        @param user       the user's login name
        @param password   the user's password
        """
        r = Repository(self.dir, user)
        if not r.passwordAuthenticates(password):
            raise AuthenticationFailed("Incorrect password for %s" % user)
        return r

    def openRepositoryWithToken(self, user, token):
        """
        open and return a Repository object, authenticating with a token.
        @param user       the user's login name
        @param token      the user's security token (retrieved from the 
                             user's cookie).
        """
        r = Repository(self.dir, user)
        if not self.tokenAuthenticates(user, token):
            raise AuthenticationFailed("Incorrect token for %s's session" % user)
        return r

    def cacheToken(self, user, token):
        """
        cache a session token to disk
        @param user       the user's login name
        @param token      the user's security token (retrieved from the 
                             user's cookie).
        """
        file = os.path.join(self.tokendir, tokenFileTmpl % token)
        out = open(file, 'w')
        print >> out, user
        out.close()

    def tokenAuthenticates(self, user, token):
        """
        return True if the token is current for the given user
        @param user       the user's login name
        @param token      the user's security token (retrieved from the 
                             user's cookie).
        """
        file = os.path.join(self.tokendir, tokenFileTmpl % token)
        if not os.path.exists(file):
            raise SessionExpired(user, token)
        tokenfile = open(file, 'r')
        tokenuser = tokenfile.read().strip()
        tokenfile.close()
        return tokenuser == user

    def setCookie(self, repos):
        """
        print the header data to standard out that delivers cookie data to
        the user's browser.
        """
        cookie = Cookie.SimpleCookie()
        cookie["session"] = urllib.urlencode({"user": repos.user,
                                              "token": repos.token})
        cookie["session"]["Path"] = os.environ['SCRIPT_NAME']
        self.cookie = str(cookie)

    def getSession(self):
        """
        read a cookie sent by the user's browser and return the user's login
        name and an authenitcation token.  
        """
        if not os.environ.has_key('HTTP_COOKIE'):
            return (None, None)

        sessionCookie = Cookie.SimpleCookie(os.environ['HTTP_COOKIE'])
        if sessionCookie is None or not sessionCookie.has_key("session"):
            return (None, None)
        sessionCookie = cgi.parse_qs(sessionCookie["session"].value)
        if not sessionCookie.has_key("user") or \
           not sessionCookie.has_key("token"):
            # unexpected format; assume starting new session
            print sys.stderr, "Funny or deprecated cookie format:", \
                  str(Cookie.SimpleCookie(os.environ['HTTP_COOKIE']))
            return (None, None)
        return (sessionCookie["user"][0], sessionCookie["token"][0])

    def login(self, cachetoken=True, user=None):
        """log in the user with a username and password and display the 
        list of resources"""
        repos = None
        if user is None:  user = self.cgi.getfirst('uname')
        try:
            pw = ''
            if self.cgi.getfirst('password') is not None:
                pw = self.cgi.getfirst('password').strip()
            repos = self.openRepositoryWithPassword(user, pw)
            self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return

        if cachetoken:
            self.cacheToken(user, repos.token)
        self.redirectToView()

    def viewResources(self, user=None, session=None):
        """
        Send the list of resources to the client browser.  The list is sent
        as an XML document with a ri:VOResources root and an attached 
        stylesheet.  
        @param user       the user's login name
        @param session    the user's security token (retrieved from the 
                             user's cookie).
        """
        repos = None
        if user is None:  user = self.cgi.getfirst('uname')
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        ssheet = self.webpath + "/Resource_Form_List.xsl"

        data = {}
        if self.cgi.getfirst('tryout') is not None or user == 'sample':
            data["tryout"] = 'true'
        repos.writeAllResources(sys.stdout, ssheet, True, data)

    def editResource(self, user, session):
        """
        Send a form for editing a selected resource to the client browser.  
        The form is sent as an XML document with a ri:Resource root and 
        an attached stylesheet.  The cgi inputs indicate which resource
        to edit.  
        @param user       the user's login name
        @param session    the user's security token (retrieved from the 
                             user's cookie).
        """
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        if self.cgi.getfirst('resource') is None:
            return self.sendError("nochoice")
        resfile = self.cgi.getfirst('resource')
        data = { "user-type": repos.getUserType(resfile),
                 "target-op"     : "Submit Changes"
                 }
        if self.cgi.getfirst('tryout') is not None or user == 'sample':
            data["tryout"] = 'true'

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        ssheet = self.webpath + "/Resource_Form_Edit.xsl"
        repos.writeResource(sys.stdout, resfile, ssheet, True, data)

    def deleteResource(self, user, session):
        """
        respond to a request to delete a resource.  If the resource selected
        is an uncommitted resource, the uncommitted record file will be 
        removed from the repository.  Thus, if the record was an uncommitted add,
        the resource will be completely lost.  For all other uncommitted record
        files, the previously published version will be restored.  If the 
        selected resource is a published (i.e. already committed) record, 
        an uncommitted version marked deleted will be created.  
        @param user       the user's login name
        @param session    the user's security token (retrieved from the 
                             user's cookie).
        """
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        if self.cgi.getfirst('resource') is None:
            return self.sendError("nochoice")
        resfile = self.cgi.getfirst('resource').strip()
        repos.deleteResource(resfile)
        self.redirectToView()
        

    def undeleteResource(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        if self.cgi.getfirst('resource') is None:
            return self.sendError("nochoice")
        resfile = self.cgi.getfirst('resource').strip()
        repos.undeleteResource(resfile)
        self.redirectToView()
        
    def publishResources(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        repos.commitResources()
        self.redirectToView()



    def addParamToForm(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        # create a record from the inputs provided so far
        lu = repos.createLookup()
        doc = self.makeRecord(lu)
        root = doc.documentElement

        
        addParam = self.cgi.getfirst("addparam").strip()
        numParam = self.cgi.getfirst("numParam").strip()

        try:
            addParam = int(addParam)
        except:
            addParam = 0
        try:
            numParam = int(numParam) + addParam
        except:
            numParam = 3 + addParam
        if numParam < 1:  numParam = 1

        choice = self.cgi.getfirst('defset')
        targetop = ''
        if self.cgi.getfirst('target-ftype'):
            targetop = self.cgi.getfirst('target-ftype').strip()

        data = { "user-type"     : repos.setstolabels[choice],
                 "form-type"     : choice,
                 "target-op"     : targetop,
                 "numParam"      : str(numParam)
                 }

        if targetop == 'Add Resource':
            data["resource-type"] = "vs:DataService"
        if self.cgi.getfirst('tryout') is not None or user == 'sample':
            data["tryout"] = 'true'

        repos.annotateResource(root, resfile=self.cgi.getfirst("inputfname"),
                               annotateData=data)
        repos.setStyleSheet(doc, self.webpath + "/Resource_Form_Edit.xsl")

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        doc.writexml(sys.stdout)
        
    def addCoverageToForm(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        # create a record from the inputs provided so far
        lu = repos.createLookup()
        doc = self.makeRecord(lu)
        root = doc.documentElement

        # change the xsi:type 
        if root.getAttribute("xsi:type") == 'vr:Service':
            root.setAttribute("xsi:type", "vs:DataService")
            if root.getAttribute("xmlns:vs") == '':
                root.setAttribute("xmlns:vs", schemas["vs"])

        # now add some default coverage info
        coverage = root.getChildrenByTagName("coverage")
        if coverage.length == 0:
            resfile = self.cgi.getfirst("inputfname")
            if resfile is None:
                print >> sys.stderr, \
                    "Warning: lost track of template resource file"
            else:
                deflt = repos.getResource(resfile)
                coverage = deflt.documentElement.getChildrenByTagName("coverage")
                if coverage.length > 0:
                    coverage = coverage.item(0)
                    doc.importNode(coverage)
                    content = root.getChildrenByTagName("content")
                    if content.length > 0:
                        content = content.item(0)
                        print >> sys.stderr, "inserting default coverage"
                        root.insertBefore(coverage, content.nextSibling)
                        if root.getAttribute("xmlns:stc") == '':
                            root.setAttribute("xmlns:stc", schemas["stc"])
                            root.setAttribute("xmlns:xlink", schemas["xlink"])

        choice = self.cgi.getfirst('defset')
        targetop = ''
        if self.cgi.getfirst('target-ftype'):
            targetop = self.cgi.getfirst('target-ftype').strip()

        data = { "user-type"     : repos.setstolabels[choice],
                 "form-type"     : choice,                    
                 "target-op"     : targetop
                 }

        rtype = "vs:DataService"
        if choice == 'DataCollection': rtype = "vs:DataCollection"
        if targetop == 'Add Resource':
            data["resource-type"] = rtype                     
        if self.cgi.getfirst('tryout') is not None or user == 'sample':
            data["tryout"] = 'true'

        repos.annotateResource(root, resfile=self.cgi.getfirst("inputfname"),
                               annotateData=data)
        repos.setStyleSheet(doc, self.webpath + "/Resource_Form_Edit.xsl")

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        doc.writexml(sys.stdout)

    def makeRecord(self, lookup, id=None):
        rb = RecordBuilder(self.cgi, lookup, id)
        return rb.makeRecord()

    def validateSSA(self, root, problems):
        needvalue = { "dataSource"        : "Select at least one Type of source data",
                      "creationType"      : "Select at least one Method for creating spectra",
                      "maxSearchRadius"   : "Provide a Maximum Search Radius",
                      "maxRecords"        : "Provide a Maximum Number of Records Returned (Hard Limit)",
                      "defaultMaxRecords" : "Provide a Maximum Number of Records Returned (Soft Limit)"
                      }
        caps = xpath.match(root, "capability");
        for cap in caps:
            captype = cap.getAttribute("xsi:type")
            if captype.endswith(":SimpleSpectralAccess") or \
                    captype.endswith(":ProtoSpectralAccess"):
                for capel in needvalue.keys():
                    if (xpath.thevalue(cap, capel) == ''):
                        problems.append(needvalue[capel])
                

    def validate(self, doc, lookup):
        root = doc.documentElement
        problems = []

        needvalue = { "title"                : "Title", 
                      "shortName"            : "Short Name", 
                      "curation/contact/name": "Contact Name",
                      "content/description"  : "Description", 
                      "content/referenceURL" : "Reference URL", 
                      "content/type"         : "Type" 
                      }

        for path in needvalue.keys():
            val = filter(lambda x: x.strip()!='', xpath.getvalues(root, path))
            if len(val) == 0:
                problems.append("A value for %s is missing." % needvalue[path])

        id = xpath.thevalue(root, "identifier").strip()
        xsitype = root.getAttribute("xsi:type")
        if id == 'ivo://' or id.startswith('ivo:///'):
            problems.append("Please choose an Authority ID")
            if id == 'ivo:///':
                problems.append("A value for the Resource Key is missing")
        elif xsitype != 'vg:Authority':
            if re.match(r'^ivo://[^/]*/?$', id):
                problems.append("A value for the Resource Key is missing")

            elif not re.match(r"^ivo://[^/]+(/[\w\d][\w\d\-_\.!~\*'\(\)\+=]*)+$",id):
                problems.append("The Resource Key contains illegal characters.")

#        if not re.match(r"^ivo://[\w\d][\w\d\-_\.!~\*'\(\)\+=]{2,}$", id) and \
#               re.match(r"^ivo://[\w\d][\w\d\-_\.!~\*'\(\)\+=]{2,}/", id):
#            problems.append("The Authority ID contains illegal characters.")

        if xsitype.find('Service') > -1:
            val = xpath.thevalue(root, "capability/interface/accessURL").strip()
            if val == '':
                problems.append("The Service must include an accessURL")

            self.validateSSA(root, problems)
            
        return problems

    def createResource(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        choice = self.cgi.getfirst('defset')
        target = self.resTypeFromUserChoice(choice)
        if self.cgi.getfirst('resource') is None:
            return self.sendError("nochoice")
        resfile = self.cgi.getfirst('resource')
        print >> sys.stderr, "Chosen template:", resfile

        if resfile is None or resfile == '':
            lookup = repos.createLookup()
            pub = repos.getPublishers()
            if len(pub) > 0:
                pub = re.search(r'\(([^\)]+)\)', pub[0])
                if pub is not None:  pub = pub.group(1)

                resfile = lookup[pub]["filename"]

            if resfile is None or resfile == '' and len(lookup.keys()) > 0:
                resfile = lookup[lookup.keys()[0]]["filename"]

            if resfile.startswith(repos.dir):
                resfile = resfile[len(repos.dir)+1:]


        data = { "user-type"     : repos.setstolabels[choice],
                 "form-type"     : choice,
                 "target-op"     : "Add Resource",
                 "resource-type" : target                     }
        if self.cgi.getfirst('tryout') is not None or user == 'sample':
            data["tryout"] = 'true'

        print >> sys.stderr, "Creating from", resfile

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        ssheet = self.webpath + "/Resource_Form_Edit.xsl"
        repos.writeResource(sys.stdout, resfile, ssheet, True, data)

    def updateResource(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        file = self.cgi.getfirst("inputfname")
        lu = repos.createLookup()
        id = filter(lambda x: lu[x]['src'] == file, lu.keys())
        if len(id) > 0:
            id = id[0]
        else:
            raise CGIError(file + ": resource file not found")

        doc = self.makeRecord(lu, id)

        root = doc.documentElement

        choice = self.cgi.getfirst('defset')
        problems = self.validate(doc, lu)

        if len(problems) > 0:
            # see form again
            rtype = xpath.thevalue(doc.documentElement, "@xsi:type")
            data = { "user-type"     : repos.setstolabels[choice],
                     "form-type"     : choice,                    
                     "target-op"     : "Submit Changes",
                     "problems"      : "#".join(problems)             }
            if self.cgi.getfirst('tryout') is not None or user == 'sample':
                data["tryout"] = 'true'

            repos.annotateResource(root, resfile=file, annotateData=data)
            repos.setStyleSheet(doc, self.webpath + "/Resource_Form_Edit.xsl")

            self.sendheader()
            sys.stdout.write("Content-type: application/xml\n\n")
            doc.writexml(sys.stdout)

        else:
            # save results
            repos.saveUpdatedResource(doc, file)
            self.redirectToView()

#            sys.stdout.write("Content-type: application/xml\n\n")
#            doc.writexml(sys.stdout)

    def redirectToView(self):
        server = os.environ['SERVER_NAME']
        if os.environ.has_key('SERVER_PORT') and     \
           os.environ['SERVER_PORT'] is not None and \
           os.environ['SERVER_PORT'] != '80':
            server += ":%s" % os.environ['SERVER_PORT']
        script = os.environ['SCRIPT_NAME']

        tryout = ''
        if self.cgi.getfirst('tryout') or self.cgi.getfirst('uname') == 'sample':
            tryout = "&tryout=true"

        self.sendheader(300, "Returning to Resource List after Save")
        sys.stdout.write("Location: http://%s%s?ftype=List%s\r\n\r\n" % 
                         (server, script, tryout))
        sys.stdout.flush()

    weekdayname = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    monthname   = [None,
                   'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    def sendheader(self, code=200, message="Script output follows"):
        if self.handleHeaderAsServer:
            (year, month, day, hh, mm, ss, wd, y, z) = time.gmtime(time.time())
            d = "%s, %02d %3s %4d %02d:%02d:%02d GMT" % (self.weekdayname[wd],
                                                         day, 
                                                         self.monthname[month], 
                                                         year, hh, mm, ss)

            sys.stdout.write("HTTP/1.0 %d %s\r\n" % (code, message))
            sys.stdout.write("Server: %s\r\n" % self.handleHeaderAsServer)
            sys.stdout.write("Date: %s\r\n" % d)

        if self.cookie is not None:
            sys.stdout.write(self.cookie)
            sys.stdout.write("\r\n")

    def addResource(self, user, session):
        repos = None
        try:
            if session is not None:
                repos = self.openRepositoryWithToken(user, session)
            else:
                pw = ''
                if self.cgi.has_key('password'):
                    pw = self.cgi.getfirst('password')
                repos = self.openRepositoryWithPassword(user, pw)
                self.setCookie(repos)
        except NoSuchUser, e:
            self.sendError("no_user", (user))
            return
        except SessionExpired, e:
            self.sendError("expired", (user), e.message)
            return
        except AuthenticationFailed, e:
            self.sendError("bad_pw", (user))
            return

        lu = repos.createLookup()
        doc = self.makeRecord(lu)
        root = doc.documentElement

        choice = self.cgi.getfirst('defset')
        problems = self.validate(doc, lu)
        id = xpath.thevalue(root, "identifier")
        if lu.has_key(id):
            problems.append("You already have a resource with the select " +
                            "Authority ID and Resource Key; please choose a " +
                            "different combination")


        if len(problems) > 0:
            # see form again
            rtype = xpath.thevalue(doc.documentElement, "@xsi:type")
            data = { "user-type"     : repos.setstolabels[choice],
                     "form-type"     : choice,                    
                     "target-op"     : "Add Resource",
                     "resource-type" : rtype,
                     "problems"      : "#".join(problems)             }
            if self.cgi.getfirst('tryout') is not None or user == 'sample':
                data["tryout"] = 'true'

            repos.annotateResource(root, resfile=self.cgi.getfirst("inputfname"),
                                   annotateData=data)
            repos.setStyleSheet(doc, self.webpath + "/Resource_Form_Edit.xsl")

            self.sendheader()
            sys.stdout.write("Content-type: application/xml\n\n")
            doc.writexml(sys.stdout)

        else:
            # save results
            repos.saveNewResource(doc, choice)
            self.redirectToView()
      
        
    _usrtotype = { "Organisation"        : "vr:Organisation",
                   "DataCollection"      : "vs:DataCollection",
                   "Authority"           : "vg:Authority",
                   "Registry"            : "vg:Registry",
                   "BrowserBasedService" : "vr:Service",
                   "CGIService"          : "vr:Service",
                   "ConeSearch"          : "vs:CatalogService",
                   "SkyNode"             : "vs:CatalogService",
                   "SIAService"          : "vs:CatalogService",
                   "SSAService"          : "vs:CatalogService",
                   "WebService"          : "vr:WebService",
                   "Service"             : "vr:Service",
                   "Resource"            : "vr:Resource"
                   }
    def resTypeFromUserChoice(self, choice):
        return self._usrtotype[choice]
        

    def handle(self):
        """
        handle a query to this service.  

        All queries to this service must contain a parameter named "ftype" 
        which identifies the operation to execute.  
        """
        try: 
            if not self.cgi.has_key('ftype'):
                self.sendError("missing_ftype")
                return

            ftype = self.cgi.getfirst('ftype').strip()

            if ftype == 'Login':
                uname = self.cgi.getfirst('uname')
                pw = self.cgi.getfirst('password')
                self.login(True)
                return

            (user, token) = self.getSession()
            if ftype == 'List':
                self.viewResources(user, token)
            elif ftype == 'Cancel':
                self.redirectToView()
            elif ftype == 'NewSite' or ftype == "Create new site":
                self.createNewPublisher()
            elif ftype == 'Next':
                self.createNewUser()
            elif ftype == 'Create Repository':
                self.createNewRepository()
            elif ftype == 'Publish Resources':
                self.publishResources(user, token)
            elif ftype == 'Delete Resource':
                self.deleteResource(user, token)
            elif ftype == 'Undelete Resource':
                self.undeleteResource(user, token)
            elif ftype == 'Edit Resource':     # creates a form
                self.editResource(user, token)
            elif ftype == 'Create Resource':   # creates a form
                self.createResource(user, token)
            elif ftype == 'Submit Changes':    # takes form input to upd. record
                self.updateResource(user, token)
            elif ftype == 'Add Resource':      # takes form input to add record
                self.addResource(user, token)
            elif ftype == 'Add Coverage':      # adds coverage input to form
                self.addCoverageToForm(user, token)
            elif ftype == 'Add Param':         # adds more params input to form
                self.addParamToForm(user, token)
            else:
                self.sendError("bad_ftype", (ftype))

        except Exception, e:
            tmp = sys.exc_info()[:2]
            message = tb.format_exception(tmp[0], tmp[1], None)[0].strip()
            self.sendError("internal", logmsg=message)
            tb.print_tb(sys.exc_info()[2])

    errors = { 
        "bad_ftype" : 
        ("illegal ftype: %s",
         "The form contained unexpected data: ftype=%s"),

        "missing_ftype" :
            ("missing ftype",  "The form is missing internal data (ftype)"),

        "missing_src" :
            ("missing src while creating new user",  
             "The form is missing internal data (src)"),

        "no_user" :
            ("no such user: %s",
             """User %s could not be found.  Use your back button, check 
             the spelling of your username, and try again. Or, go back and
             create a new login if you have not done so already."""),

        "bad_pw" :
            ("incorrect password given for %s",
             """%s, your password did not match.  Please, use your back button
             and try entering your password again."""),

        "nochoice" :
            ("user did not select a resource prior to operation",
             """Please select a resource before using this operation.  Use 
             your back button and try again.  (Note: it helps to have 
             Javascript turned on to avoid this message.)"""),

        "expired" :
            ("session expired for user %s",
             """%s, your session has apparently timed out.  Please return to 
             login page to restart your session."""),

        "internal" :
            ("internal error", 
             """service experienced an internal error.  Please contact service
             administrator""")
        }
                                 

    def sendError(self, name, data=(), logmsg=None):
        msg = nicedom.Document("Error")
        root = msg.documentElement
        root.setAttribute('name', name)

        desc = "Service response failed due to unknown server error"
        log = desc
        if self.errors.has_key(name): 
            if len(self.errors[name]) > 1:
                desc = self.errors[name][1] % data
            log = self.errors[name][0] % data
        if logmsg is not None and len(logmsg) > 0:
            log = logmsg

        self.logerror(log)
        root.appendChild(msg.createTextNode(desc))

        ssdata = 'type="text/xsl" href="' + self.webpath + \
            '/Resource_Form_Error.xsl"'
        proc = msg.createProcessingInstruction("xml-stylesheet", ssdata)
        msg.insertBefore(proc, root)

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        msg.writexml(sys.stdout)

    def logerror(self, msg):
        stmp = datetime.datetime.now().isoformat(' ')
        script = "Resource_Form.cgi"
        if os.environ.has_key("SCRIPT_NAME"):
            script = os.environ["SCRIPT_NAME"]
        print >> sys.stderr, "%s: %s: %s" % (script, stmp, msg)

    def createNewPublisher(self):
        # use a dummy repository
        repos = Repository(self.dir, "")

        # create an empty resource record
        doc = nicedom.Document("ri:Resource")
        root = doc.documentElement;
        root.setAttribute("xmlns:ri", schemas["ri"])
        root.appendChild(doc.createElement("curation"))
        root.appendChild(doc.createElement("content"))

        repos.setStyleSheet(doc, self.webpath + "/Resource_Form_Edit.xsl")
        choice = "Organisation"
        data = { "pub-status":     "published" ,
                 "form-type":      "NewOrg",
                 "target-op":      "Next",
                 "user-type":      repos.setstolabels[choice],
                 "resource-type":  self.resTypeFromUserChoice(choice),
                 "publishers":     "",
                 "authids":        "",
                 "resources":      "",
                 }
        if self.cgi.getfirst('tryout') is not None:
            data["tryout"] = 'true'

        repos.annotateResource(doc.documentElement, data)

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        doc.writexml(sys.stdout)

    def createNewUser(self):
        doc = self.makeRecord({})
        root = doc.documentElement

        lu = {}
        problems = self.validate(doc, lu)

        if len(problems) > 0:
            # see form again; use a dummy repository
            repos = Repository(self.dir, "")
            choice = "Organisation"

            rtype = xpath.thevalue(doc.documentElement, "@xsi:type")
            data = { "form-type"     : "NewOrg",                    
                     "target-op"     : "Next",
                     "user-type"     : repos.setstolabels[choice],
                     "resource-type" : rtype,
                     "publishers"    : "",
                     "authids"       : "",
                     "resources"     : "",
                     "problems"      : "#".join(problems)             }
            if self.cgi.getfirst('tryout') is not None:
                data["tryout"] = 'true'

            repos.annotateResource(root, resfile=self.cgi.getfirst("inputfname"),
                                   annotateData=data)
            repos.setStyleSheet(doc, self.webpath + "/Resource_Form_Edit.xsl")

            self.sendheader()
            sys.stdout.write("Content-type: application/xml\n\n")
            doc.writexml(sys.stdout)

        else:

            repos = Repository(self.dir, "#newuser")
            file = os.path.join(self.tokendir, newOrgFileTmpl % repos.token)
            f = open(file, 'w')
            doc.writexml(f)
            f.close()

            self.askForUser(file, repository=repos)

    def askForUser(self, file, user=None, prob=None, repository=None):
        out = nicedom.Document("voreginabox")
        root = out.documentElement
        root.setAttribute("src", file)
        if user is not None:
            root.setAttribute("uname", user)
        if prob is not None:
            root.setAttribute("prob", prob)
        if self.cgi.getfirst("tryout") is not None or user == 'sample':
            root.setAttribute("tryout", self.cgi.getfirst("tryout"))

        if repository is None:  repository = Repository(self.dir, "#newuser")
        repository.setStyleSheet(out, self.webpath+"/Resource_Form_NewUser.xsl")

        self.sendheader()
        sys.stdout.write("Content-type: application/xml\n\n")
        out.writexml(sys.stdout)


    def createNewRepository(self):
        src = self.cgi.getfirst('src')
        user = self.cgi.getfirst('uname')

        passwd = self.cgi.getfirst('passwd')
        passwd2 = self.cgi.getfirst('passwd2')

        if src is None or src == '':
            return self.sendError("missing_src")
        orgfile = os.path.join(self.tokendir, src)
        if not os.path.exists(orgfile):
            return self.sendError("expired", (uname), 
                                  "session ended while creating new site")

        if user is None or user == '':
            return self.askForUser(src, prob="Please provide a user name.")

        if user == 'sample' or \
           os.path.exists(os.path.join(self.dir, configFileTmpl % user)):
            self.askForUser(src, 
                            prob="Your choice of user name is already in " +
                                 "use; please enter a different name.")
            return

        if passwd != passwd2:
            self.askForUser(src, user,
                            "You password entries do not match, please " +
                            "it again")
            return

        if passwd is None or passwd == '':
            return self.askForUser(src, user, "Please enter a password.")

        self.logerror("Creating new repository for user " + user)
        repos = makeRepository(self.dir, user, passwd, orgfile)
        os.remove(orgfile)
        self.cacheToken(user, repos.token)
        self.setCookie(repos)

        self.redirectToView()

class RecordBuilder(object):

    def __init__(self, cgi, lookup, id=None):
        """
        create the builder
        @param cgi      the CGI data from the form
        @param lookup   a dictionary created by repos object's createLookup()
                           function.  The keys are identifiers of all 
                           registered resources (both committed and 
                           uncommitted) and the values are dictionaries of 
                           important data (identifier, filename, title, 
                           shortName, status, and src).
        @param id       the IVOA identifier to use for the record.
        """
        self.cgi = cgi
        self.lookup = lookup
        self.id = id
        self.systems = {}

    def makeRecord(self, target=None, id=None):
        """
        Create the XML record for the requested type of record.
        """
        if target is None:
            target = self.cgi.getfirst('defset').strip()
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = None
        if target == 'SIAService':
            doc = self.makeSIAService()
        elif target == 'SSAService':
            doc = self.makeSSAService()
        elif target == 'ConeSearch':
            doc = self.makeCSService()
        elif target == 'SkyNode':
            doc = self.makeSkyNode()
        elif target == 'BrowserBasedService':
            doc = self.makeBrowserService()
        elif target == 'WebService':
            doc = self.makeSOAPService()
        elif target == 'CGIService':
            doc = self.makeCGIService()
        elif target == 'DataCollection':
            doc = self.makeDataCollection()
        elif target == 'Organisation':
            doc = self.makeOrganisation()
        elif target == 'Authority':
            doc = self.makeAuthority()
        else:
            doc = self.makeGenericResource()

        return doc

    def addTextElem(self, node, elem, cgivar=None, v=None, req=False):
        """
        add a simple element containing text as a child to another element.

        @param node    the parent node to add the new element to
        @param elem    the name of the element to add
        @param cgivar  the name of the CGI parameter that contains the value
                           to add.  If none, a parameter matching the element
                           name will be used.  
        @param v       the text value to encode.  If provided, this value will 
                           be used instead of the value of the parameter named 
                           by cgivar.  
        @param req     if False, the element will be added only if the value is
                           is a non-empty string.  If True, an empty value
                           will cause the an empty element to be added.  
        """
        if v is None:
            if cgivar is None: cgivar = elem
            try:
                v = self.cgi.getfirst(cgivar).strip()
            except AttributeError, e:
                raise RuntimeError("cgi variable %s not set" % cgivar)
        if not req and v == '':  return None

        child = node.ownerDocument.createTextElement(elem, v)
        node.appendChild(child)
        return child

    def makeDataCollection(self, id=None):
        """
        create a DOM document containing a resource record describing a
        data collection using inputs from the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = self.makeCoreRecord("vs:DataCollection")
        root = doc.documentElement

        self.addDataServiceExt(doc)

        return doc

    def makeOrganisation(self, id=None):
        """
        create a DOM document containing a resource record describing an
        Organization using inputs from the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = self.makeCoreRecord("vr:Organisation")
        root = doc.documentElement
        root.setAttribute("xmlns:xsi", schemas["xsi"])

        # facility, instrument
        name = self.cgi.getfirst('facility')
        if name is not None:
            id = self.cgi.getfirst('facilityId')
            if id != '' and name == '' and self.lookup.has_key(id):
                name = self.lookup[id]['title']
            child = self.addTextElem(root, "facility", v=name)
            if id != '':
                child.setAttribute('ivo-id', id)

        name = self.cgi.getfirst('instrument')
        if name is not None:
            id = self.cgi.getfirst('instrumentId')
            if id != '' and name == '' and self.lookup.has_key(id):
                name = self.lookup[id]['title']
            child = self.addTextElem(root, "instrument", v=name)
            if id != '':
                child.setAttribute('ivo-id', id)

        return doc

    def makeSkyNode(self, id=None):
        """
        create a DOM document containing a resource record having a SkyNode
        capability built from inputs submitted via the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = self.makeGenericService("vs:CatalogService")
        root = doc.documentElement
        root.setAttribute("xmlns:sn", schemas["sn"])

        cap = doc.createElement("capability")
        cap.setAttribute("xsi:type", "sn:OpenSkyNode")
        cap.setAttribute("standardID", "ivo://ivoa.net/std/SkyNode")
        root.appendChild(cap)

        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vr:WebService")
        interface.setAttribute("role", "std")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "full")

        self.addTextElem(cap, "compliance", "snCompliance", req=True)
        self.addTextElem(cap, "longitude", "loc_long", req=True)
        self.addTextElem(cap, "latitude", "loc_lat", req=True)
        self.addTextElem(cap, "maxRecords", "maxRec", req=True)
        self.addTextElem(cap, "primaryTable", req=True)
        self.addTextElem(cap, "primaryKey", req=True)

        return doc

    def makeCSService(self, id=None):
        """
        create a DOM document containing a resource record having a ConeSearch 
        capability built from inputs submitted via the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = self.makeGenericService("vs:CatalogService")
        root = doc.documentElement
        root.setAttribute("xmlns:cs", schemas["cs"])

        cap = doc.createElement("capability")
        cap.setAttribute("xsi:type", "cs:ConeSearch")
        cap.setAttribute("standardID", "ivo://ivoa.net/std/ConeSearch")
        root.appendChild(cap)

        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vs:ParamHTTP")
        interface.setAttribute("role", "std")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "base")

        self.addTextElem(cap, "maxSR", req=True)
        self.addTextElem(cap, "maxRecords", "maxRec", req=True)
        v = 'false'
        if self.cgi.getfirst("verbosity") != '': 
            v = 'true'
        self.addTextElem(cap, "verbosity", v=v, req=True)

        child = doc.createElement("testQuery")
        cap.appendChild(child)
        self.addTextElem(child, "ra", v="83.8221")
        self.addTextElem(child, "dec", v="-5.3911")
        self.addTextElem(child, "sr", v="0.5")
        
        self.addCatalogServiceExt(doc)

        return doc

    def makeSSAService(self, id=None):
        """
        create a DOM document containing a resource record having an SSA
        capability built from inputs submitted via the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = self.makeGenericService("vs:CatalogService")
        root = doc.documentElement
        root.setAttribute("xmlns:ssa", schemas["ssa"])

        ver = self.cgi.getfirst("ssaVersion")
        captype = "ssa:ProtoSpectralAccess"
        if ver == "1.0":  captype = "ssa:SimpleSpectralAccess"

        cap = doc.createElement("capability")
        cap.setAttribute("xsi:type", captype)
        cap.setAttribute("standardID", "ivo://ivoa.net/std/SSA")
        root.appendChild(cap)

        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vs:ParamHTTP")
        interface.setAttribute("role", "std")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "base")

        if ver == "1.0":
            self.addTextElem(cap, "complianceLevel", req=True)

        names = self.cgi.getlist("dataSource")
        for name in names:
            self.addTextElem(cap, "dataSource", v=name, req=True)
        
        names = self.cgi.getlist("creationType")
        for name in names:
            self.addTextElem(cap, "creationType", v=name, req=True)

        self.addTextElem(cap, "maxSearchRadius", "maxSR", req=True)
        self.addTextElem(cap, "maxRecords", "maxRec", req=True)
        self.addTextElem(cap, "defaultMaxRecords", "defMaxRec", req=True)
        self.addTextElem(cap, "maxAperture")
        self.addTextElem(cap, "supportedFrame")
        self.addTextElem(cap, "maxFileSize")

        self.addCatalogServiceExt(doc)

        return doc 

    def makeSIAService(self, id=None):
        """
        create a DOM document containing a resource record having an SIA
        capability built from inputs submitted via the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        doc = self.makeGenericService("vs:CatalogService")
        root = doc.documentElement
        root.setAttribute("xmlns:sia", schemas["sia"])

        cap = doc.createElement("capability")
        cap.setAttribute("xsi:type", "sia:SimpleImageAccess")
        cap.setAttribute("standardID", "ivo://ivoa.net/std/SIA")
        root.appendChild(cap)

        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vs:ParamHTTP")
        interface.setAttribute("role", "std")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "base")

        self.addTextElem(cap, "imageServiceType", "imServType", req=True)
        
        child = doc.createElement("maxQueryRegionSize")
        cap.appendChild(child)

        self.addTextElem(child, "long", "maxRegSize_long", req=True)
        self.addTextElem(child, "lat", "maxRegSize_lat", req=True)

        child = doc.createElement("maxImageExtent")
        cap.appendChild(child)
        self.addTextElem(child, "long", "maxImExt_long", req=True)
        self.addTextElem(child, "lat", "maxImExt_lat", req=True)

        child = doc.createElement("maxImageSize")
        cap.appendChild(child)
        self.addTextElem(child, "long", "maxImSize_long", req=True)
        self.addTextElem(child, "lat", "maxImSize_lat", req=True)

        self.addTextElem(cap, "maxFileSize", "maxFSize", req=True)
        self.addTextElem(cap, "maxRecords", "maxRec", req=True)

        self.addCatalogServiceExt(doc)

        return doc

    def _isTabular(self):
        cat = self.cgi.getfirst("catalogservice")
        if cat == "yes":  return True

        mime = self.cgi.getfirst("output")
        if mime is None or mime == '':
            return False
        if mime.find("votable") >= 0 or mime.find('/csv') >= 0 or \
           mime == "text/html":
            return True

        return False

    def makeCGIService(self, id=None):
        """
        create a DOM document containing a service resource record having a 
        generic CGI web service capability built from inputs submitted via 
        the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        xsitype = "vr:Service"
        if self._isTabular():
            xsitype = "vs:CatalogService"
        elif self.cgi.getfirst("regionFrame") is not None:
            xsitype = "vs:DataService"
        doc = self.makeGenericService(xsitype)
        root = doc.documentElement

        cap = doc.createElement("capability")
        root.appendChild(cap)
        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vs:ParamHTTP")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "base")

        mime = self.cgi.getfirst("output")
        if mime is not None:
            if mime == 'Other':
                mime = self.cgi.getfirst("outputMime")
            child = self.addTextElem(interface, "resultType", v=mime)

        names = self.cgi.getlist("param_name")
        if names is not None and len(names) > 0 and \
           any(map(lambda x: x!='', names)):
            types = self.cgi.getlist("param_dataType")
            units = self.cgi.getlist("param_unit")
            ucds  = self.cgi.getlist("param_ucd")
            descs = self.cgi.getlist("param_desc")
            reqs  = self.cgi.getlist("param_req")
            for i in xrange(0,len(names)):
                if names[i] != '' or ucds[i] != '' or descs[i] != '':
                    param = doc.createElement("param")
                    interface.appendChild(param)
                    self.addTextElem(param, "name", v=names[i], req=True)
                    self.addTextElem(param, "description", v=descs[i])
                    self.addTextElem(param, "unit", v=units[i])
                    self.addTextElem(param, "ucd", v=ucds[i])
                    self.addTextElem(param, "dataType", v=types[i])
                    if any(map(lambda x: x==str(i+1), reqs)):
                        param.setAttribute("use", "required")
                    else:
                        param.setAttribute("use", "optional")

        if xsitype == "vs:CatalogService":
            self.addCatalogServiceExt(doc)
        elif xsitype == "vs:DataService":
            self.addDataServiceExt(doc)

        return doc

    def makeBrowserService(self, id=None):
        """
        create a DOM document containing a service resource record that is 
        accessible from a browsser built from inputs submitted via 
        the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        xsitype = "vr:Service"
        if self._isTabular():
            xsitype = "vs:CatalogService"
        elif self.cgi.getfirst("regionFrame") is not None:
            xsitype = "vs:DataService"
        doc = self.makeGenericService(xsitype)
        root = doc.documentElement

        cap = doc.createElement("capability")
        root.appendChild(cap)
        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vr:WebBrowser")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "full")

        if xsitype == "vs:CatalogService":
            self.addCatalogServiceExt(doc)
        elif xsitype == "vs:DataService":
            self.addDataServiceExt(doc)

        return doc

    def makeSOAPService(self, id=None):
        """
        create a DOM document containing a service resource record having a 
        generic SOAP web service capability built from inputs submitted via 
        the form.  
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        xsitype = "vr:Service"
        if self.cgi.getfirst("regionFrame") is not None:
            xsitype = "vs:DataService"
        doc = self.makeGenericService(xsitype)
        root = doc.documentElement

        cap = doc.createElement("capability")
        root.appendChild(cap)
        interface = doc.createElement("interface")
        interface.setAttribute("xsi:type", "vr:WebService")
        cap.appendChild(interface)
        child = self.addTextElem(interface, "accessURL", "ifaceURL", req=True)
        child.setAttribute("use", "full")

        if xsitype == "vs:DataService":
            self.addDataServiceExt(doc)

        return doc

    def makeGenericService(self, restype=None, id=None):
        """
        create a DOM document containing a generic service record with no
        extension metadata
        @param restype    the value to provide as the resource's xsi:type 
                             attribute value.  The default is "vr:Service".
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        if restype is None:  restype = "vr:Service"
        doc = self.makeCoreRecord(restype)
        root = doc.documentElement
        root.setAttribute("xmlns:xsi", schemas["xsi"])

        return doc

    def makeGenericResource(self, restype=None, id=None):
        """
        create a DOM document containing a generic resource record with no
        extension metadata
        @param restype    the value to provide as the resource's xsi:type 
                             attribute value.  The default is "vr:Resource".
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        if restype is None:  restype = "vr:Resource"
        doc = self.makeCoreRecord(restype)
        root = doc.documentElement
        root.setAttribute("xmlns:xsi", schemas["xsi"])

        return doc

    def makeAuthority(self, restype=None, id=None):
        """
        create a DOM document containing an Authority resource record with no
        extension metadata
        @param restype    the value to provide as the resource's xsi:type 
                             attribute value.  The default is "vg:Authority".
        @param id    override the identifier value set at construction with 
                       this one
        """
        if id is None:
            id = self.id
        else:
            self.id = id

        if restype is None:  restype = "vg:Authority"
        doc = self.makeCoreRecord(restype)
        root = doc.documentElement
        root.setAttribute("xmlns:vg", schemas["vg"])
        root.setAttribute("xmlns:xsi", schemas["xsi"])

        pub = xpath.thevalue(root, "curation/publisher")
        pubid = xpath.thevalue(root, "curation/publisher/@ivo-id")

        if pub is None: pub = ''
        pub = pub.strip()
        child = self.addTextElem(root, "managingOrg", v=pub, req=True)
        if pubid is not None and pubid != '':
            pubid = pubid.strip()
            child.setAttribute("ivo-id", pubid)

        return doc

    def addCatalogServiceExt(self, doc):
        """
        Add to a DOM document containing a generic resource record the extension
        metadata for a CatalogService.  This calls addDataServiceExt() 
        internally.  Note that the xsi:type should already be properly set.  
        """
        doc = self.addDataServiceExt(doc)
        root = doc.documentElement

        return doc

    def addDataServiceExt(self, doc):
        """
        Add to a DOM document containing a generic resource record the extension
        metadata for a DataService.  Note that the xsi:type should already 
        be properly set.  
        """
        root = doc.documentElement
        root.setAttribute("xmlns:vs", schemas["vs"])
        root.setAttribute("xmlns:stc", stcschema)
        root.setAttribute("xmlns:xlink", xlinkschema)
        root.setAttribute("xmlns:xsi", schemas["xsi"])

        # facility, instrument
        name = self.cgi.getfirst('facility')
        if name is not None:
            id = self.cgi.getfirst('facilityId')
            if id != '' and name == '' and self.lookup.has_key(id):
                name = self.lookup[id]['title']
            child = self.addTextElem(root, "facility", v=name)
            if id != '':
                child.setAttribute('ivo-id', id)

        name = self.cgi.getfirst('instrument')
        if name is not None:
            id = self.cgi.getfirst('instrumentId')
            if id != '' and name == '' and self.lookup.has_key(id):
                name = self.lookup[id]['title']
            child = self.addTextElem(root, "instrument", v=name)
            if id != '':
                child.setAttribute('ivo-id', id)

        # rights (for DataCollection)
        names = self.cgi.getlist('rights')
        if names is not None:
            names = filter(lambda y: len(y) > 0, 
                           map(lambda x: x.strip(), names))
            for name in names:
                self.addTextElem(root, "rights", v=name)
            
        # format (for DataCollection)
        names = self.cgi.getfirst('format')
        if names is not None:
            names = filter(lambda y: len(y) > 0, 
                           map(lambda x: x.strip(), names.split("\n")))
            for name in names:
                self.addTextElem(root, "format", v=name)
            

        # Coverage
        #
        if self.cgi.getfirst('region') is not None:
            # stc
            coverage = doc.createElement('coverage')
            self.addSTC(coverage)

            # waveband
            names = self.cgi.getlist("waveband")
            for name in names:
                self.addTextElem(coverage, "waveband", v=name)

            if coverage.hasChildNodes():
                root.appendChild(coverage)

        return doc

    def formatDate(self, date, iso=False, end=False):
        if re.match(r'^\d{4}-\d\d-\d\dT\d\d:\d\d$', date):
            date += ":00.0"
        elif re.match(r'^\d{4}-\d\d-\d\d$', date):
            if iso: date += "T00:00:00.0"
        elif re.match(r'^\d{4}-\d\d$', date):
            date += "-01"
            if iso: date += "T00:00:00.0"
        elif re.match(r'^\d{4}$', date):
            if end:
                date += "-12-31"
            else:
                date += "-01-01"
            if iso: date += "T00:00:00.0"
        return date

    def makeCoreRecord(self, restype=None):
        if restype is None:  restype = "vr:Resource"
        id = self.id
        if id is None:
            id = self.cgi.getfirst("authorityId")
            if id is None:  
                id = self.cgi.getfirst("srcAuthorityID")
            if id is None:  id = ''
            id = "ivo://%s" % id.strip()
            v = self.cgi.getfirst("resourceKey").strip()
            if len(v) > 0:
                id += "/%s" % v
            self.id = id

        doc = nicedom.Document("ri:Resource")
        root = doc.documentElement

        now = self.now()
        root.setAttribute("xsi:type", restype)
        if self.lookup.has_key(id):
            root.setAttribute("status", self.lookup[id]['status'])
            root.setAttribute("created", self.lookup[id]['created'])
            root.setAttribute("updated", now)
        else:
            root.setAttribute("status", "active")
            root.setAttribute("created", now)
            root.setAttribute("updated", now)

        root.setAttribute("xmlns", "")
        root.setAttribute("xmlns:ri", schemas["ri"])
        root.setAttribute("xmlns:vr", schemas["vr"])

        self.addTextElem(root, "title", req=True)
        self.addTextElem(root, "shortName", "sname", req=True)
        self.addTextElem(root, "identifier", v=id, req=True)

        # Curation
        #
        # publisher
        curation = doc.createElement("curation")
        root.appendChild(curation)
        pid = self.cgi.getfirst("publisherId").strip()
        v = self.cgi.getfirst("pub_title").strip()
        if pid != '' and v == '' and self.lookup.has_key(id):
            v = self.lookup[id]['title']
        child = self.addTextElem(curation, "publisher", v=v, req=True)
        if pid is not None and pid != '':
            child.setAttribute("ivo-id", pid)

        # creator: there may be multiple
        names = map(lambda x: x.strip(), self.cgi.getlist("creator"))
        v = self.cgi.getfirst("logo").strip()
        if any(map(lambda x: x != '', names)) or v != '':
            creator = doc.createElement("creator")
            self.addTextElem(creator, "name", v=names[0])
            if len(v) > 0:
                self.addTextElem(creator, "logo", v=v)
            curation.appendChild(creator)
            
            for name in names[1:]:
                if len(name) > 0:
                    creator = doc.createElement("creator")
                    self.addTextElem(creator, "name", v=name)
                    curation.appendChild(creator)

        # contributor
        names = map(lambda x: x.strip(), self.cgi.getlist("contributor"))
        for name in names:
            self.addTextElem(curation, "contributor", v=name)

        # date, version
        self.addTextElem(curation, "date", 
                         v=self.formatDate(self.cgi.getfirst("date")))
        self.addTextElem(curation, "version")

        # contact
        names = [ self.cgi.getfirst('contact').strip(),
                  self.cgi.getfirst('contactAddress').strip(),
                  self.cgi.getfirst('contactEmail').strip(),
                  self.cgi.getfirst('contactTelephone').strip() ]
        if any(map(lambda x: len(x) > 0, names)):
            child = doc.createElement('contact')
            curation.appendChild(child)
            self.addTextElem(child, "name", v=names[0])
            self.addTextElem(child, "address", v=names[1])
            self.addTextElem(child, "email", v=names[2])
            self.addTextElem(child, "telephone", v=names[3])

        # Content
        #
        content = doc.createElement('content')
        root.appendChild(content)

        # subjects
        names = filter(lambda y: len(y) > 0, 
                       map(lambda x: x.strip(), 
                           self.cgi.getfirst('subject').split("\n")))
        for i in xrange(0, len(names)):
            self.addTextElem(content, "subject", v=names[i], req=True)
        if len(names) == 0:
            self.addTextElem(content, "subject", v='', req=True)

        # description, source, referenceURL
        self.addTextElem(content, "description", req=True).wrapLines = True
        self.addTextElem(content, "source")
        self.addTextElem(content, "referenceURL", "refURL", req=True)

        # type
        names = self.cgi.getlist("type")
        if names is None: names = []
        for name in names:
            self.addTextElem(content, "type", v=name)

        # contentLevel
        names = self.cgi.getlist("contentLevel")
        if names is None: names = []
        for name in names:
            self.addTextElem(content, "contentLevel", v=name)

        # relationship
        v = self.cgi.getfirst('relation')
        if v is None:  v = ''
        names = [ v.strip(),
                  self.cgi.getfirst('rel_title').strip(),
                  self.cgi.getfirst('relatedResourceId').strip() ]
        if names[1] != '' or names[2] != '':
            if names[0] == '': names[0] = 'related-to'
            if names[2] != '' and names[1] == '' and \
               self.lookup.has_key(names[2]):
                names[1] = self.lookup[names[2]]['title']
            child = doc.createElement('relationship')
            content.appendChild(child)
            self.addTextElem(child, 'relationshipType', v=names[0], req=True)
            child = self.addTextElem(child, 'relatedResource', v=names[1])
            if names[2] != '':
                child.setAttribute('ivo-id', names[2])
            
        return doc

    def addRegion(self, stc):
        doc = stc.ownerDocument
        cgi = self.cgi

        if cgi.getfirst("region")         == '' and \
           cgi.getfirst("temporal_start") == '' and \
           cgi.getfirst("temporal_end")   == '' and \
           cgi.getfirst("wave_min")       == '' and \
           cgi.getfirst("wave_min")       == '':
            return stc

        frame = 'UTC-ICRS-TOPO'
        rsel = cgi.getfirst("region")
        if rsel is None:  rsel = ''
        rsel = rsel.strip()
        if len(rsel) > 0:
            fsel = ''
            if rsel == 'CircleRegion':
                fsel = cgi.getfirst("circleRegionFrame")
            elif rsel == 'CoordRange':
                fsel = cgi.getfirst("rangeRegionFrame")
            if fsel is None:  fsel = ''
            if fsel.startswith("Galactic"): fsel = 'GALII'
            if rsel != 'AllSky' and fsel != '':
                frame = 'UTC-%s-TOPO' % fsel

        frameid = self.addFrame(stc, frame)
        area = doc.createElement("stc:AstroCoordArea")
        stc.appendChild(area)
        area.setAttribute("coord_system_id", frameid)

        v = cgi.getfirst("temporal_start") 
        w = cgi.getfirst("temporal_end")
        if v != '' or w != '':
            interval = doc.createElement("stc:TimeInterval")
            area.appendChild(interval)

            child = doc.createElement("stc:StartTime")
            interval.appendChild(child)
            if v == '':  v = "1970"
            self.addTextElem(child, "stc:ISOTime", v=self.formatDate(v, True))
            child = doc.createElement("stc:StopTime")
            interval.appendChild(child)
            if w == '':  w = str(time.gmtime().tm_year)
            self.addTextElem(child, "stc:ISOTime", 
                             v=self.formatDate(w, True, True))

        if rsel == 'AllSky':

            child = doc.createElement("stc:AllSky")
            area.appendChild(child)

        elif rsel == "CircleRegion":

            circle = doc.createElement("stc:Circle")
            circle.setAttribute("unit", "deg")
            area.appendChild(circle)
            center = doc.createElement("stc:Center")
            circle.appendChild(center)
            self.addTextElem(center, "stc:C1", "region_long", req=True)
            self.addTextElem(center, "stc:C2", "region_lat", req=True)
            self.addTextElem(circle, "stc:Radius", "region_rad", req=True)

        elif rsel == "CoordRange":

            parea = doc.createElement("stc:Position2VecInterval")
            area.appendChild(parea)
            parea.setAttribute("unit", "deg")

            child = doc.createElement("stc:LoLimit2Vec")
            parea.appendChild(child)
            self.addTextElem(child, "stc:C1", "range_long_min", req=True)
            self.addTextElem(child, "stc:C2", "range_lat_min", req=True)

            child = doc.createElement("stc:HiLimit2Vec")
            parea.appendChild(child)
            self.addTextElem(child, "stc:C1", "range_long_max", req=True)
            self.addTextElem(child, "stc:C2", "range_lat_max", req=True)

        v = cgi.getfirst("wave_min").strip()
        vu = cgi.getfirst("wave_min_units").strip()
        w = cgi.getfirst("wave_max").strip()
        wu = cgi.getfirst("wave_max_units").strip()
        if v != '' or w != '':
            child = doc.createElement("stc:SpectralInterval")
            child.setAttribute("unit", "m")
            area.appendChild(child)
            v = self.toMeters(v,vu)
            w = self.toMeters(w,wu)
            if (v < w):
                self.addTextElem(child, "stc:LoLimit", v=str(v))
                self.addTextElem(child, "stc:HiLimit", v=str(w))
            else:
                self.addTextElem(child, "stc:LoLimit", v=str(w))
                self.addTextElem(child, "stc:HiLimit", v=str(v))
        
        return stc

    def toMeters(self, val, unit, ref=None, refunit=None):
        if ref is not None: ref = float(ref)
        return self.unitConvert("m", float(val.strip()), unit, ref, refunit)

    def unitConvert(self, target, val, unit, ref=None, refunit=None):
        c = 2.9979e8
        h = 6.62608e-34
        JpereV = 1.602e-19
        mperA = 1e-10
        mxeV = h*c/JpereV
        eVperHz = h / JpereV

        if target != 'm' and target != 'Hz' and target != 'eV':
            raise CGIError("unsupported target unit: " + target)

        if unit == "meters": unit = 'm'
        (val, unit) = self.metricNorm(val, unit)
        if unit == target: return val

        if ref is not None:
            if refunit is None: refunit = unit
            ref = self.unitConvert(unit, ref, refunit)
            val = ref*ref/val

        if target == 'm':
            if unit == 'eV':
                val = mxeV/val
            elif unit == 'Hz':
                val = c/val
            else:
                raise CGIError("unsupported base unit: " + unit)
        elif target == 'Hz':
            if unit == 'm':
                val = c/val
            elif unit == 'eV':
                val = eVperHz * val
            else:
                raise CGIError("unsupported base unit: " + unit)
        elif target == 'eV':
            if unit == 'm':
                val = mxEv/val
            elif unit == 'Hz':
                val = val / eVperHz
            else:
                raise CGIError("unsupported base unit: " + unit)

        return val

    def addFrame(self, stc, ftype):
        if self.systems.has_key(ftype):
            return self.systems[ftype]

        sys = stc.ownerDocument.createElement("stc:AstroCoordSystem")
        stc.insertBefore(sys, stc.firstChild)
        sys.setAttribute("xlink:type", "simple")
        sys.setAttribute("xlink:href", 
                         'ivo://STClib/CoordSys#%s' % ftype)

        identifier = xpath.thevalue(stc, "/*/identifier")
        if identifier is None:
            raise CGIError("no identifier set for this record")
        identifier = identifier.strip()[6:]
        identifier = re.sub(r'/','_', identifier)
        frameid = "%s_%s" % (identifier, ftype)
        sys.setAttribute("id", frameid)
        self.systems[ftype] = frameid

        return frameid

    def addSTC(self, coverage):

        doc = coverage.ownerDocument
        stc = doc.createElement('stc:STCResourceProfile')
        self.addPos(stc)
        self.addRegion(stc)

        if stc.hasChildNodes():
            coverage.appendChild(stc)

    def addPos(self, stc):
        """
        add a position to an STC description:
        @param stc      the STCResourceProfile element node to add to
        """
        cgi = self.cgi
        if cgi.getfirst("spatial_res")   == '' and \
           cgi.getfirst("region_regard") == '' and \
           cgi.getfirst("spec_res")      == '' and \
           cgi.getfirst("temporal_res")  == '':
            return stc

        frame = 'UTC-ICRS-TOPO'
        rsel = cgi.getfirst("region")
        if rsel is None:  rsel = ''
        rsel = rsel.strip()
        if len(rsel) > 0:
            fsel = ''
            if rsel == 'CircleRegion':
                fsel = cgi.getfirst("circleRegionFrame")
            elif rsel == 'CoordRange':
                fsel = cgi.getfirst("rangeRegionFrame")
            if fsel is None:  fsel = ''
            if fsel.startswith("Galactic"): fsel = 'GALII'
            if rsel != 'AllSky':
                frame = 'UTC-%s-TOPO' % fsel

        frameid = self.addFrame(stc, frame)

        doc = stc.ownerDocument
        pos = doc.createElement("stc:AstroCoords")
        pos.setAttribute("coord_system_id", frameid)
        stc.appendChild(pos)

        v = cgi.getfirst("temporal_res")
        if v != '':
            child = doc.createElement("stc:Time")
            child.setAttribute("unit", "s")
            pos.appendChild(child)
            self.addTextElem(child, "stc:Resolution", v=v)

        v = cgi.getfirst("spatial_res")
        w = cgi.getfirst("region_regard")
        if v != '' or w != '':
            child = doc.createElement("stc:Position1D")
            pos.appendChild(child)
            if v != '':
                tel = self.addTextElem(child, "stc:Resolution", v=v.strip())
                tel.setAttribute("pos_unit", "deg")
            if w != '':
                tel = self.addTextElem(child, "stc:Size", v=w.strip())
                tel.setAttribute("pos_unit", "arcsec")

        v = cgi.getfirst("spec_res")
        vu = cgi.getfirst("spec_res_units")
        w = cgi.getfirst("wave_min")
        wu = cgi.getfirst("wave_min_units")
        if v != '':
            child = doc.createElement("stc:Spectral")
            pos.appendChild(child)
            child.setAttribute("unit", "m")
            self.addTextElem(child, "stc:Resolution", 
                             v=str(self.toMeters(v,vu,w,wu)))

        return stc

    def metricNorm(self, val, unit):
        cvt = { "m":   1.e-3,
                "k":   1.e3,
                "M":   1.e6,
                "G":   1.e9
                }
        if unit == 'eV' or unit == 'Hz' or unit == 'm':
            return (val, unit)
        elif unit == 'Angstrom':
            return (val * 1.0e-10, "m")
        else:
            return (val * cvt[unit[0]], unit[1:])

    def now(self):
        return datetime.datetime.utcnow().isoformat()[:-4]

def any(iterable):
    for element in iterable:
        if element: return True
    return False
