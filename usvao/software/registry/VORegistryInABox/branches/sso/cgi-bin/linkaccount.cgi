#! /usr/local/bin/python2.5
#
import cgi, os, os.path, sys
import cgitb;  cgitb.enable()
from string import Template

# defhome = ""
defhome = "/appl/VO/regsso"
defwebpath = ""
if os.environ.has_key("VOPUB_HOME"):
    defhome = os.environ["VOPUB_HOME"];
elif not defhome:
    defhome = "@INSTALLDIR@"
if defhome.startswith('@'): 
    defhome = os.path.dirname(os.path.dirname(sys.argv[0]))
if len(defhome) == 0:
    defhome = "."

defsyspath = [ os.path.join(defhome, "lib", "python") ]
sys.path.extend(defsyspath)

from VORegInABox.form import RepositoryService, NoSuchUser, AuthenticationFailed
from VORegInABox.repository import RepositoryError
from VORegInABox.nicedom import xpath
from VORegInABox import nicedom
# import cgitb;  cgitb.enable()
from random import random

def main():
    form = cgi.FieldStorage(keep_blank_values=True)

    home = defhome
    if os.environ.has_key('VOPUB_HOME'):
        home = os.environ['VOPUB_HOME']

    reposroot = os.path.join(home, "data/webwrk")
    if os.environ.has_key('VOPUB_REPOSITORY'):
        reposroot = os.environ['VOPUB_REPOSITORY']

    tokendir = os.path.join(home, "var")
    if os.environ.has_key('VOPUB_TOKENDIR'):
        print >> sys.stderr, "tokendir:", os.environ['VOPUB_TOKENDIR']
        tokendir = os.environ['VOPUB_TOKENDIR']

    if form.has_key('tryout') or form.getfirst('uname') == 'sample':
        if os.environ.has_key('VOPUB_PLAY_REPOSITORY'):
            reposroot = os.environ['VOPUB_PLAY_REPOSITORY']
        else:
            reposroot = os.path.join(home,"data/alt")
        print >> sys.stderr, "Repos:", reposroot

    webpath = defwebpath
    if os.environ.has_key('VOPUB_WEBPATH'):
        webpath = os.environ['VOPUB_WEBPATH']
    elif not webpath:
        webpath = os.path.join(home, 'web')

    asServer = None
    if os.environ.has_key('VOPUB_HANDLE_AS_SERVER'):
        asServer = os.environ['VOPUB_HANDLE_AS_SERVER']

    service = RepositoryService(reposroot, tokendir, webpath, form, asServer)

    
    if not service.cgi.has_key('ftype'):
        sendForm(service)
        return

    ftype = service.cgi.getfirst('ftype').strip()

    server = os.environ['SERVER_NAME']
    if os.environ.get('SERVER_PORT'):
        server += ":%s" % os.environ['SERVER_PORT']
    portal = os.path.dirname(os.environ.get('SCRIPT_NAME','/x'))
    url = "http://%s%s/Resource_Form.cgi" % (server, portal)

    if ftype == 'Link to this account':
        uname = service.cgi.getfirst('uname')
        pw = service.cgi.getfirst('password')

        try:
            repos = service.openRepositoryWithPassword(uname, pw)
        except NoSuchUser, ex:
            # reshow login form
            sendForm(service, "The entered username was not recognized", uname)
            return
        except AuthenticationFailed, ex:
            # reshow login form
            sendForm(service, "Your password was not correct.", uname)
            return

        try: 
            makeOpenIDUserFor(service, os.environ['REMOTE_USER'], uname)
        except RepositoryError, e:
            raise

        sys.stdout.write("Location: %s?ftype=List\r\n\r\n" % url)

    elif ftype == "Create new site":
        makeOpenIDUserFor(service,
                          os.environ['REMOTE_USER'], os.environ['REMOTE_USER'])
        sys.stdout.write("Location: %s?ftype=NewSite\r\n\r\n" % url)
                            

configFileTmpl = "%sconfig.xml"

def makeOpenIDUserFor(service, openid, olduser):
    file = os.path.join(service.dir, configFileTmpl % openid)
    if os.path.exists(file):
        raise RepositoryError("Username in use.")

    config = nicedom.Document("Config")
    root = config.documentElement
    root.appendChild(config.createTextElement("Username", olduser))
    root.appendChild(config.createTextElement("Password", str(random())))

    f = open(file, 'w')
    config.writexml(f)
    f.close()

    

def sendForm(service, emsg='', username=''):
    formfile = os.path.join(service.webpath, "oid-welcome.html")
    # log("sending form file: " + formfile)
    # log("...from: " + service.webpath)
    bufsz = 1<<16

    env = os.environ.copy()
    env['ERROR_MESSAGE'] = '<p><font color="red">%s</font></p>' % emsg
    env['USERNAME'] = username
    
    sys.stdout.write("Content-Type: text/html\r\n\r\n")
    fd = None
    try:
        try:
            fd = open(formfile, 'rb')
            data = fd.read(bufsz)
            while data:
                tmpl = Template(data)
                sys.stdout.write(tmpl.safe_substitute(env))
                data = fd.read(bufsz)

        except Exception, ex:
            log("Problem sending file data: " + str(ex))
    finally:
        if fd:  fd.close()

def log(msg):
    sys.stderr.write(msg)
    sys.stderr.flush()


if __name__ == "__main__":
    main()
