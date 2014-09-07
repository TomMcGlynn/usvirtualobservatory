"""
functions for handling vaologin configuration.  

vaologin can load various default settings from a configuration file.  This
file can be edited to tweek the defaults, but it is usually not necessary.
"""
import os, sys, re, ConfigParser
from vaologin import ConfigurationError

version = "$VERSION"

# $INSTALLDIR is expected to be replaced by the actual installation
# directory by "python setup.py build".
def_product_home = "$INSTALLDIR"
def_sys_config_dir = "$CONFDIR"

def_unix_sys_config_dir = "/etc/httpd/conf"
if def_sys_config_dir.startswith('$'):
    def_sys_config_dir = def_unix_sys_config_dir
config_filename = "vaologin-py.cfg"
vaologin_home_envvar = "VAOLOGIN_HOME"
vaologin_conf_envvar = "VAOLOGIN_CONF"
vaologin_conf_file_envvar = "VAOLOGIN_CONF_FILE"

def getConfig(conffile=None, defaults=None, fail=False, 
              deffilename=config_filename):
    """
    return configuration parameters as a dictionary.  See getConfigLocations()
    for the ordered list of default locations for the configuration file.
    @param conffile     the full path to the configuration file to use.  If 
                          None, the parameters will be loaded from a default 
                          location.
    @param defaults     a dictionary of default values to use if for any 
                          parameters not specified in the configuration file.
    @param fail         if true, a ConfigurationError will be raised if no 
                          configuration file is found.  The message will list 
                          all of the locations it looked.
    @param deffilename  the path-less filename for the config file to assume
                          when locating a default configuration file (i.e.
                          when conffile=None).
    """
    out = {}
    if defaults:  out = defaults.copy()

    if not conffile:
        conffile = locateConfigFile(fail, deffilename)
    if not conffile or not os.path.exists(conffile): 
        if fail:  
            raise ConfigurationError("configuration file not found: "+conffile)
        return out

    # this implementation assumes the .ini format 
    cfg = ConfigParser.SafeConfigParser()
    cfg.read(conffile)
    secs = cfg.sections()
    for sec in secs:
        for namevalue in cfg.items(sec):
            out[".".join((sec, namevalue[0]))] = namevalue[1]

    return out

def locateConfigFile(fail=False, filename=config_filename):
    """
    look for a configuration file on the system and return its path.  This
    will return the first path returned by getConfigLocations() that is 
    found to exist, or None if none of these exist.
    @param fail       if true, a ConfigurationError will be raised if no 
                         configuration file is found.  The message will list 
                         all of the locations it looked.
    @param filename   the path-less filename for the config file.  
    """

    # if VAOLOGIN_CONF_FILE is set, assume that it must be there
    path = os.environ.get(vaologin_conf_file_envvar);
    if path:
        if os.path.exists(path):
            return path
        elif fail:
            raise ConfigurationError("%s env var set to non-existant file: %s" 
                                     % (vaologin_conf_file_envvar, path))

    # Now look in an ordered preference of locations
    locs = getConfigLocations(filename=filename)
    for path in locs:
        if os.path.exists(path):
            return path
    if fail:
        raise ConfigurationError("Failed to find configuration file in any of these locations:\n  " + "\n  ".join(locs))

    return None
    
def getConfigLocations(filename=config_filename):
    """
    return a list of file paths where we might find a configuration file, 
    ordered from most prefered to least prefered.  
    @param filename   the path-less filename for the config file.  
    """
    out = []

    # Loading out in order of preference.
    # $VAOLOGIN_CONF
    #
    if os.environ.has_key(vaologin_conf_envvar):
        out.append(os.environ[vaologin_conf_envvar]);

    # location relative to $VAOLOGIN_HOME, script path
    #
    if os.environ.has_key(vaologin_home_envvar):
        home = os.environ[vaologin_home_envvar];
    else:
        home = def_product_home
    if home.startswith('$') or not os.path.exists(home): 
        # $ means that "setup.py install" has not been done
        # look relative to the script path assuming the script is in 
        # a "bin" or "cgi-bin" sub-directory relative to home 
        home = os.path.dirname(os.path.dirname(sys.argv[0]))
    if not home:
        home = "."
    out.append(os.path.join(home, "conf", filename))

    # directly in the current directory
    #
    out.append(filename)

    # the default system path
    #
    out.append(os.path.join(def_sys_config_dir, filename))

    return out

_delimre = re.compile(r"\s*[,\s]\s*")
def splitList(cfgval):
    """
    split a configuration parameter value assuming it is a comma-delimited
    list
    @param cfgval   the configuration parameter value to split
    @return list    the individual items as a list
    """
    if cfgval is None:
        return []
    return _delimre.split(cfgval.strip())

def mergeIntoList(cfglist, newitems):
    """
    merge one or more values into a comma-delimited list.  Unless it already
    appears in the given comma-delimited list, each new value will be appended.
    @param cfglist   a configuration parameter value assumed to be a 
                        comma-separated list
    @param newitems  the item or items to merge into cfglist.  This can be a 
                        python scalar or a python list.
    @return str   a new comma-delimited list 
    """
    if newitems is None: 
        newitems = []
    if not isinstance(newitems, list):
        newitems = [newitems]
    if not cfglist and len(newitems) > 0:
        cfglist = newitems.pop(0)

    vals = splitList(cfglist)
    for item in newitems:
        if item not in vals:
            cfglist += ", %s" % str(item)

    return cfglist
