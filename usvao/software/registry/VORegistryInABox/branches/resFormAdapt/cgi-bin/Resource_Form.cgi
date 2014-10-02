#! /usr/bin/env python
#
import cgi, os, os.path, sys

defwebpath = ""
defhome = ""
if os.environ.has_key("VOPUB_HOME"):
    defhome = os.environ["VOPUB_HOME"];
else:
    defhome = "@INSTALLDIR@"
    if defhome.startswith('@'): 
        defhome = os.path.dirname(os.path.dirname(sys.argv[0]))
if len(defhome) == 0:
    defhome = "."

defsyspath = [ os.path.join(defhome, "lib", "python") ]
sys.path.extend(defsyspath)

from VORegInABox.form import RepositoryService, SessionExpired, CGIError
# import cgitb;  cgitb.enable()

def main():
    form = cgi.FieldStorage(keep_blank_values=True)

    home = defhome
    if os.environ.has_key('VOPUB_HOME'):
        home = os.environ['VOPUB_HOME']

    reposroot = os.path.join(home, "data/repos")
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

    asServer = None
    if os.environ.has_key('VOPUB_HANDLE_AS_SERVER'):
        asServer = os.environ['VOPUB_HANDLE_AS_SERVER']

    service = RepositoryService(reposroot, tokendir, webpath, form, asServer)
    service.handle()


if __name__ == '__main__':
    main()

