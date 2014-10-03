#! /usr/bin/env python
#
import nicedom
from utils import VORegInABoxError
from nicedom import xpath
from datetime import datetime
from random import random
from xml.sax import saxutils
import os, os.path, re, sys
try:
    import hashlib
    makehash = hashlib.md5
except ImportError:
    import md5
    makehash = md5.new

configFileTmpl = "%sconfig.xml"
rischema = "http://www.ivoa.net/xml/RegistryInterface/v1.0"
stcschema = "http://www.ivoa.net/xml/STC/stc-v1.30.xsd"
xlinkschema = "http://www.w3.org/1999/xlink"
rbxschema = "http://nvo.ncsa.uiuc.edu/xml/VORegInABox/v1.0"

class RepositoryError(VORegInABoxError):
    """a general error accessing the repository"""
    def __init__(self, msg):
        VORegInABoxError.__init__(self, msg)

class AuthenticationFailed(RepositoryError):
    """an exception indicating that a provided password does not match for a 
given username"""
    def __init__(self, user):
        self.user = user
        RepositoryError.__init__(self, "authentication failed for user " + user)

class NoSuchUser(RepositoryError):
    """an exception indicating that a configuration file for a given user does not exist"""
    def __init__(self, user):
        self.user = user
        RepositoryError.__init__(self, "No such user found: %s" % user)

class IllegalArgument(RepositoryError):
    """an exception indicating that an illegal argument was passed to a 
function. """ 
    def __init__(self, msg):
        RepositoryError.__init__(self, msg)

class ResourceNotFound(RepositoryError):
    """an exception indicating that a resource file is missing from disk."""
    def __init__(self, file, msg=None):
        self.file = file
        if msg is None: msg = "%s: resource file not found" % file
        RepositoryError.__init__(self, msg)

def makeToken():
    return makehash(datetime.now().isoformat()).hexdigest()

class Repository:

    def __init__(self, dir, user):
        """
        create access to the repository of resource descriptions for a given
        user.
        """
        self.dir = dir
        if not os.path.isdir(self.dir):
            raise IllegalArgument(dir + ": not an existing directory")
        self.user = user
        self.publishers = None
        self.registry = None
        self.resfilere = re.compile(r'^%s\d+.xml(.uc_add)?$' % user)
        try:
            (self.pub, self.pubid) = self.getPublisher()
        except NoSuchUser, e:
            self.pub = None
            self.pubid = None
        self.token = makeToken()

    def tokenAuthenticates(self, token):
        return token == self.token

    def passwordAuthenticates(self, passwd):
        """
        return true if the given password is correct for the current user.
        """
        configFile = os.path.join(self.dir, configFileTmpl % self.user)
        if not os.path.exists(configFile):
            raise NoSuchUser(self.user)
        cfg = open(configFile, 'r')
        doc = nicedom.parse(cfg)
        cfg.close()
        pw = xpath.thevalue(doc, "Config/Password")

        if pw != passwd:  return None
        return makehash(datetime.now().isoformat()).hexdigest()

    def getResourceFiles(self):
        """
        return all saved files for the current user
        """
        base = self.user + '0'
        out = []
        for root, dirs, files in os.walk(self.dir):
            out.extend(map(lambda x: os.path.join(root, x), 
                           filter(lambda x: self.resfilere.match(x), files)))

        out = sorted(out, Repository._resfilecmp)
        for i in xrange(0, len(out)):
            if os.path.exists(out[i]+".uc_undel"):
                out[i] += ".uc_undel"
            elif os.path.exists(out[i]+".uc_del"):
                out[i] += ".uc_del"
            elif os.path.exists(out[i]+".uc_edit"):
                out[i] += ".uc_edit"

        return out


    @staticmethod
    def _resfilecmp(filex, filey):
        return cmp(os.path.basename(filex), os.path.basename(filey))

    def setStyleSheet(self, doc, stylesheetLoc):
        root = doc.documentElement
        ssdata = 'type="text/xsl" href="%s"' % stylesheetLoc
        proc = doc.createProcessingInstruction("xml-stylesheet", ssdata)
        doc.insertBefore(proc, root)
        return doc

    def getRegisteredAuthorityIDs(self):
        reg = self.getRegistryResource()
        return xpath.getvalues(reg.documentElement, "managedAuthority")

    def getRegistryFile(self):
        regfilere = re.compile(r'\d+.xml$')
        reg = filter(regfilere.search, os.listdir(os.path.join(self.dir,
                                                               "Registry")))
        if len(reg) > 1:
            print >> sys.stderr, \
                "Warning, more than one registry record available:", reg
        elif len(reg) == 0:
            raise ResourceNotFound(None, msg="Registry record is missing!")

        reg = reg[0]
        reg = os.path.join(self.dir, "Registry", reg)

        if os.path.exists(reg + ".uc_edit"):
            reg += ".uc_edit"

        return reg

    def getRegistryResource(self, stylesheetLoc=None, annotate=False,
                            annotateData=None):
        if self.registry is not None:
            return self.registry

        reg = self.getRegistryFile()
        self.registry = self.getResource(reg[len(self.dir)+1:])
        return self.registry

    def saveRegistryResource(self, file=None):
        if file is None or file == '':
            file = self.getRegistryFile()
        file = file.strip()

        if self.registry is not None and \
           self.registry.documentElement is not None:
            self.saveResourceAs(self.registry, file)

    def getResource(self, resfile, stylesheetLoc=None, annotate=False,
                    annotateData=None):
        file = os.path.join(self.dir, resfile)
        if not os.path.exists(file):
            raise ResourceNotFound(resfile)
        f = open(file, 'r')
        out = nicedom.parse(f)
        f.close()

        root = out.documentElement
        if stylesheetLoc is not None:
            self.setStyleSheet(out, stylesheetLoc)

        if annotate:
            self.annotateResource(root, annotateData, resfile)

        return out

    QUOTE = re.compile(r'"')

    def annotateResource(self, root, annotateData=None, resfile=None):
        if resfile is None and annotateData is not None and \
           annotateData.has_key("src"):
            resfile = annotateData["src"]
        root.setAttribute("xmlns:rbx", rbxschema)

        stat = root.getAttribute("status")
        data = { "publishers" : "#".join(self.getPublishers()),
                 "authids"    : "#".join(self.getAuthorityIDs()),
                 "resources"  : "#".join(self.getResources()),
                 "publisher"  : self.pub,
                 "pubid"      : self.pubid,
                 "user"       : self.user
                 }
        if data['publisher'] is None:  data['publisher'] = ''
        if data['pubid'] is None:  data['pubid'] = ''

        if resfile is not None and resfile != '':
            data["pub-status"] = self.getStatusFromFilename(resfile,stat)
            data["src"]        = resfile
            data["src-type"]   = os.path.dirname(resfile)
            data["form-type"]  = os.path.dirname(resfile)
        else:
            data["pub-status"] = stat

        if annotateData is not None:
            for key in annotateData.keys():
                data[key] = annotateData[key]

        for key in data.keys():
            try:
                annotation = self.QUOTE.sub("", data[key])
                root.setAttribute('rbx:'+key, annotation)
            except TypeError, e:
                raise TypeError("Unexpected annotation value for ['%s']: %s (%s)"
                                % (key, data[key], str(e)))

        return root

    def writeResource(self, strm, resfile, stylesheetLoc=None, annotate=False,
                      annotateData=None):
        out = self.getResource(resfile, stylesheetLoc, annotate, annotateData)
        out.writexml(strm)

    setstolabels = {
        "Organisation"        : "Organization or Project",
        "DataCollection"      : "Data Collection",
        "Authority"           : "Naming Authority ID",
        "BrowserBasedService" : "Service accessible from a Web Page",
        "ConeSearch"          : "Simple Cone Search Service",
        "SIAService"          : "Simple Image Access Service",
        "SSAService"          : "Simple Spectral Access Service",
        "SkyNode"             : "SkyNode Service",
        "WebService"          : "SOAP-based Web Service", 
        "CGIService"          : "CGI/URL-based Web Service",
        "Service"             : "Generic Service",
        "Resource"            : "Generic Resource",
        "Registry"            : "This Registry" 
        }
    def getUserType(self, filename):
        return self.setstolabels[os.path.dirname(filename)]

    def getPublisher(self):
        """return the title and id of the main publisher for this user"""
        configFile = os.path.join(self.dir, configFileTmpl % self.user)
        if not os.path.exists(configFile):
            raise NoSuchUser(self.user)

        cfg = open(configFile, 'r')
        doc = nicedom.parse(cfg)
        cfg.close()

        pub = xpath.thevalue(doc, "Config/Publisher")
        pubid = xpath.thevalue(doc, "Config/Publisher/@ivo-id")

        if pub is None or pub == '':
            pub = self.getPublishers()
            if len(pub) == 0: return (None, None)
            pub = pub[0].rsplit(')',1)[0]
            (pub, pubid) = pub.rsplit('(',1)

        return (pub, pubid)

    def getPublishers(self, choose=''):
        if self.publishers is not None and choose != '':
            return self.publishers

        out = []
        for res in filter(lambda x: x.find('/Organisation/') >= 0, 
                          self.getResourceFiles()):
            f = open(res, 'r')
            d = nicedom.parse(f)
            f.close()
            r = d.documentElement

            id = xpath.thevalue(r, 'identifier')
            pub = "%s (%s)" % (xpath.thevalue(r, 'title'), id)
            if id.strip() == choose.strip(): 
                pub += ' selected'
            out.append(pub)

        self.publishers = out
        return out
            
    def getAuthorityIDs(self, choose=''):
        out = []
        for res in filter(lambda x: x.find('/Authority/') >= 0, 
                          self.getResourceFiles()):
            f = open(res, 'r')
            d = nicedom.parse(f)
            f.close()
            r = d.documentElement

            id = xpath.thevalue(r, 'identifier')
            label = "%s (%s)" % (id.strip()[6:], xpath.thevalue(r, 'title'))
            if id.strip() == choose.strip(): 
                label += ' selected'
            out.append(label)

        return out
            
    def getResources(self, choose=''):
        """
        create a text listing resource titles and identifiers.  

        This function returns a list of strings where each element is a
        resource title followed by its identifier in parentheses: e.g.,

           Natural Satellites Service (ivo://rvo.ru/db/nss)

        If choose is set to an ivoa identifier, then the element whose ID 
        matches this identifier will have the word "selected" appended to it: 
        e.g.,

           Natural Satellites Service (ivo://rvo.ru/db/nss) selected

        @param choose    the identifier of the resource to have a "selected"
                            label appended to the corresponding element.
        """
        out = []
        for res in self.getResourceFiles():
            f = open(res, 'r')
            d = nicedom.parse(f)
            f.close()
            r = d.documentElement

            id = xpath.thevalue(r, 'identifier')
            reslabel = "%s (%s)" % (xpath.thevalue(r, 'title'), id)
            if id.strip() == choose.strip(): 
                reslabel += ' selected'
            out.append(reslabel)

        return out
            

    def writeAllResources(self, strm, stylesheetLoc=None, annotate=False,
                          annotateData=None):
        out = nicedom.Document("ri:VOResources")
        root = out.documentElement
        root.setAttribute("xmlns:ri", rischema)
        if annotate:
            root.setAttribute("xmlns:rbx", rbxschema)
            if self.user is not None:
                root.setAttribute("rbx:user", self.user)
            if self.pub is not None and self.pub != '':
                root.setAttribute("rbx:publisher", self.pub)
            if self.pubid is not None and self.pubid != '':
                root.setAttribute("rbx:pubid", self.pubid)
            if annotateData is not None and annotateData.has_key('tryout'):
                root.setAttribute("rbx:tryout", annotateData['tryout'])

        if stylesheetLoc is not None:
            ssdata = 'type="text/xsl" href="%s"' % stylesheetLoc
            proc = out.createProcessingInstruction("xml-stylesheet", ssdata)
            out.insertBefore(proc, root)

        for res in self.getResourceFiles():
            f = open(res, 'r')
            d = nicedom.parse(f)
            f.close()
            r = d.documentElement

            if annotate:
                resfile = res[len(self.dir)+1:]
                stat = d.documentElement.getAttribute("status")
                data = { "src"        : resfile,
                         "src-type"   : os.path.dirname(resfile),
                         "pub-status" : self.getStatusFromFilename(resfile,stat),
                         "user-type"  : self.getUserType(resfile)
                         }

                if annotateData is not None:
                    for key in annotateData.keys():
                        data[key] = annotateData[key]

                for key in data.keys():
                    r.setAttribute('rbx:'+key, data[key])

            if r is not None:
                out.importNode(r)
                root.appendChild(r)

        out.writexml(strm)

    statusextre = re.compile(".(\w+)$")
    exttostatus = {
        "xml"      : "published",
        "uc_add"   : "uncommitted add",
        "uc_edit"  : "uncommitted edit",
        "uc_del"   : "uncommitted delete",
        "uc_undel" : "uncommitted undelete",
        }
    def getStatusFromFilename(self, filename, status=None):
        m = self.statusextre.search(filename)
        if m is None:
            return "unknown"
        ext = m.group(1)
        if not self.exttostatus.has_key(ext):
            return "unknown"
        out = self.exttostatus[ext]
        if status is not None and out == "published" and status != 'active':
            out = status
        return out

    def createLookup(self):
        """
        return a mapping of IVOA identifiers to metadata, 
        including identifier, shortName, title, and the filename.
        """
        out = {}
        for res in self.getResourceFiles():
            f = open(res, 'r')
            d = nicedom.parse(f)
            f.close()
            r = d.documentElement
            if r is not None:
                md = {}
                try:
                    id = xpath.thevalue(r, 'identifier')
                    md['identifier'] = id
                    md['title'] = xpath.thevalue(r, 'title')
                    md['shortName'] = xpath.thevalue(r, 'shortName')
                    md['created'] = xpath.thevalue(r, '@created')
                    md['status'] = xpath.thevalue(r, '@status')
                    md['filename'] = res
                    md['src'] = res[len(self.dir)+1:]
                    out[id] = md
                except IndexError:
                    print >> stderr, "%s: missing identifier!" % res

        return out

    def saveNewResource(self, doc, set):

        getNumFromFile = _GetNumFromFile(self.user)
        maxn = max(map(getNumFromFile, self.getResourceFiles()))
        filename = "%s/%s%05d.xml.uc_add" % (set, self.user, maxn+1)
        filename = os.path.join(self.dir,filename)

        return self.saveResourceAs(doc, filename)

    def saveUpdatedResource(self, doc, file):

        getNumFromFile = _GetNumFromFile(self.user)
        filename = os.path.join(self.dir, file)
        if not re.search(r'.xml.uc_\w+$', filename):
            filename += ".uc_edit"

        return self.saveResourceAs(doc, filename)

    def saveResourceAs(self, doc, filename):
        print >> sys.stderr, "saving as %s" % filename
        temp = filename + str(os.getpid())
        f = open(temp, 'w')
        doc.writexml(f)
        f.close()
        os.rename(temp, filename)

    def deleteResource(self, resfile):
        if not re.search(r'/%s\d+.xml(.uc_\w+)*' % self.user, resfile):
            print >> sys.stderr, \
                "Warning: attempt by %s to remove another user's file: %s" % \
                (self.user, resfile)
            return
        resfile = os.path.join(self.dir, resfile)

        if resfile.endswith(".xml.uc_del"):
            pass

        elif re.search(r'.xml.uc_\w+$', resfile):
            if os.path.exists(resfile):
                os.remove(resfile)
            else:
                print >> sys.stderr, "Warning: requested file to delete does", \
                    "not exist: %s" % resfile

        else:
            if os.path.exists(resfile + ".uc_del"):
                return

            doc = self.getResource(resfile[len(self.dir)+1:])
            if doc.documentElement.getAttribute("status") != "deleted":
                doc.documentElement.setAttribute("status", "deleted")

                file = resfile + ".uc_del"
                self.saveResourceAs(doc, file)
            
    def undeleteResource(self, resfile):
        if not re.search(r'/%s\d+.xml(.uc_\w+)*' % self.user, resfile):
            print >> sys.stderr, \
                "Warning: attempt by %s to remove another user's file: %s" % \
                (self.user, resfile)
            return
        resfile = os.path.join(self.dir, resfile)

        if resfile.endswith(".xml.uc_del"):
            if os.path.exists(resfile):
                os.remove(resfile)
            else:
                print >> sys.stderr, "Warning: requested file to undelete does",\
                    "not exist: %s" % resfile

        elif resfile.endswith(".xml"):
            if os.path.exists(resfile+".uc_del"):
                return self.undeleteResource(resfile[len(self.dir)+1:]+".uc_del")

            doc = self.getResource(resfile[len(self.dir)+1:])
            root = doc.documentElement
            if root.getAttribute("status") == "deleted":
                root.setAttribute("status", "active")

                file = resfile + ".uc_undel"
                self.saveResourceAs(doc, file)

    ucre = re.compile(r'.xml.uc_\w+$')
    def commitResources(self):
        uncommitted = filter(lambda x: self.ucre.search(x), 
                             self.getResourceFiles())

        doupdreg = False
        for resfile in uncommitted:
            if self.commitResource(resfile, False):  doupdreg = True

        if doupdreg:
            self.saveRegistryResource()
            

    def commitResource(self, resfile, updateRegRec=True):
        doupdreg = False
        if self.ucre.search(resfile):
            file = self.ucre.sub('.xml', resfile)
            if file == resfile:
                raise RepositoryError("programmer error during commit: %s == %s"
                                      % (file, resfile))

            if resfile.startswith(self.dir):
                resfile = resfile[len(self.dir)+1:]

            doc = self.getResource(resfile)

            if doc.documentElement.getAttribute("xsi:type") == "vg:Authority":
               aid = xpath.thevalue(doc.documentElement, "identifier").strip()
               aid = aid[len("ivo://"):]
               if aid not in self.getRegisteredAuthorityIDs():
                   reg = self.getRegistryResource()
                   child = reg.createTextElement("managedAuthority", aid)
                   reg.documentElement.appendChild(child)
                   doupdreg = True

            doc.documentElement.setAttribute("updated", self.now())
            self.saveResourceAs(doc, file)
            os.remove(os.path.join(self.dir, resfile))

            if doupdreg and updateRegRec:
                self.saveRegistryResource()
                return False

        else:
            print >> sys.stderr, "attempt to commit already-committed file: " +\
                                 resfile

        return doupdreg

    def now(self):
        return datetime.utcnow().isoformat()[:-4]

class _GetNumFromFile:
    def __init__(self, username):
        self.numre = re.compile(r'/%s0(\d+).xml' %  username)

    def __call__(self, filename):
        try:
            return int(self.numre.search(filename).group(1))
        except:
            return -1

def makeRepository(dir, user, password, orgfile=None, openidauth=False):
    if not user:
        raise RepositoryError("No username provided")
    if not openidauth and not password:
        raise RepositoryError("No password provided")
    if not password:
        password = str(random())

    file = os.path.join(dir, configFileTmpl % user)
    if not openidauth and os.path.exists(file):
        raise RepositoryError("Username in use.")

    needRegRec = ensureRegistryDir(dir)

    if not os.path.exists(file):
        config = nicedom.Document("Config")
        root = config.documentElement
        root.appendChild(config.createTextElement("Username", user))
        root.appendChild(config.createTextElement("Password", str(random())))

        f = open(file, 'w')
        config.writexml(f)
        f.close()

    pub = None
    pubid = None
    if orgfile is not None:
        f = open(orgfile, 'r')
        doc = nicedom.parse(f)
        f.close()

        file = "%s/Organisation/%s00001.xml.uc_add" % (dir, user)
        if os.path.exists(file):
            raise CGIError("Organisation file already exists: " + file)

        f = open(file, 'w')
        doc.writexml(f)
        f.close()
        orgfile = file

        root = doc.documentElement
        pubid = xpath.thevalue(root, "identifier")
        authid = pubid[6:].split('/', 1)[0]
        pub = xpath.thevalue(root, "title")

        authrec = organisationToAuthority(doc, "ivo://%s" % authid, pub, pubid)
                                          
        file = "%s/Authority/%s00002.xml.uc_add" % (dir, user)
        f = open(file, 'w')
        doc.writexml(f)
        f.close()

        if needRegRec:
            file = "%s/Registry/%s00003.xml.uc_add" % (dir, user)
            print >> sys.stderr, "Creating new registry record:", file
            regrec = authorityToRegistry(doc, "ivo://%s/registry" % authid,
                                         pub, pubid)
            f = open(file, 'w')
            doc.writexml(f)
            f.close()

    config = nicedom.Document("Config")
    root = config.documentElement
    root.appendChild(config.createTextElement("Username", user))
    root.appendChild(config.createTextElement("Password", password))
    if pub is not None:
        child = config.createTextElement("Publisher", pub)
        child.setAttribute("ivo-id", pubid)
        child.setAttribute("src", orgfile)
        root.appendChild(child)

    file = os.path.join(dir, configFileTmpl % user)
    f = open(file, 'w')
    config.writexml(f)
    f.close()

    out = Repository(dir, user)

    return out

def authorityToRegistry(doc, id, pub, pubid):
    root = doc.documentElement
    root.setAttribute("xsi:type", "vg:Registry")
    if root.getAttribute("xmlns:vg") == '':
        root.setAttribute("xmlns:vg", schemas["vg"])

    oldnode = xpath.match(root, "title")[0]
    root.replaceChild(doc.createTextElement("title",
                                            "%s Publishing Registry" % pub),
                      oldnode)

    oldnode = xpath.match(root, "identifier")[0]
    root.replaceChild(doc.createTextElement("identifier", id), oldnode)

    content = xpath.match(root, "content")[0]
    oldnode = xpath.match(content, "description")[0]
    msg = "A registry for publishing resources from the %s" \
            % pub
    content.replaceChild(doc.createTextElement("description", msg), oldnode)

    return doc

def organisationToAuthority(doc, id, pub, pubid):
    root = doc.documentElement
    root.setAttribute("xsi:type", "vg:Authority")
    if root.getAttribute("xmlns:vg") == '':
        root.setAttribute("xmlns:vg", "http://www.ivoa.net/xml/VORegistry/v1.0")

    oldnode = xpath.match(root, "title")[0]
    root.replaceChild(doc.createTextElement("title","%s Naming Authority" % pub),
                      oldnode)

    oldnode = xpath.match(root, "identifier")[0]
    root.replaceChild(doc.createTextElement("identifier", id), oldnode)

    curation = xpath.match(root, "curation")[0]
    oldnode = xpath.match(curation, "publisher")[0]
    newnode = doc.createTextElement("publisher", pub)
    newnode.setAttribute("ivo-id", pubid)
    curation.replaceChild(newnode, oldnode)

    content = xpath.match(root, "content")[0]
    oldnode = xpath.match(content, "subject")
    for node in oldnode:
        content.removeChild(node)
    content.insertBefore(doc.createTextElement("subject", 
                                               "virtual observatory"),
                         content.firstChild)

    oldnode = xpath.match(content, "description")[0]
    msg = "A naming authority for identifying resources from the %s" \
            % pub
    content.replaceChild(doc.createTextElement("description", msg), oldnode)

    oldnode = xpath.match(content, "type")
    for node in oldnode:
        content.removeChild(node)
    oldnode = xpath.match(content, "contentLevel")
    for node in oldnode:
        content.removeChild(node)

    content.appendChild(doc.createTextElement("type", "Other"))
    content.appendChild(doc.createTextElement("contentLevel", "General"))

    return doc

def ensureRegistryDir(dir):
    if not os.path.exists(dir) or not os.path.isdir(dir):
        raise RepositoryError("%s: Directory not found")

    sets = Repository.setstolabels.keys()
    for set in sets:
        sdir = os.path.join(dir, set)
        if not os.path.exists(sdir):
            os.mkdir(sdir)
        if not os.path.isdir(sdir):
            raise RepositoryError("%s: not a directory")

    regfilere = re.compile("0000\d.xml")
    return len(filter(regfilere.search, 
                      os.listdir(os.path.join(dir, "Registry")))) == 0

