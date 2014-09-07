"""
A module for basic SAMP interactions.

SAMP (Simple Access Messaging Protocol, http://www.ivoa.net/Documents/SAMP/index.html) is an
application-level messaging structure. It allows for communication between regsitered client
applications via a SAMP Hub. Client applications can send messages to other applications to
pass data references or notifications or action invocations, e.g., load this table or this
image. Client applications can also listen for messages from other applications to which they
can respond in an appropriate manner.

This modules provides basic, low-level access to the SAMP framework. It requires a separate
SAMP Hub to be available, either as a standalone Hub or one running as part of another
SAMP-enabled application (TOPCAT, Aladin, etc. provide built-in Hubs).
"""

class Connection():
    """
    A class for sending and receiving SAMP messages to/from a SAMP Hub 
    """

    def __init__(self, multiple = False, sender = None, to = None, pattern = None, file = None, keepalive = True, proxy = None, timeout = None, session = None):
        """
        Creating a connection to a SAMP Hub running on the local machine. 

        At this time, no provision is made for connecting to a remote SAMP hub.

        :Args:
            *multiple*: handle multiple messages (default: False). The connection will
            close after a single message.

            *sender*: handle only messages from <sender>

            *to*: send to specified application (<to>); otherwise all applications will be notified

            *pattern*: message pattern - allowed values are: sync, async and notify

            *file*: send all commands in the file <file>

            *keepalive*: if set to False, disable the keep_alive feature (default: True).

            *proxy*:  the proxy IP address to use

            *timeout*: set the keep_alive timeout if keep_alive is set to True

            *session*: set a name for the session
	"""

	self.multiple = multiple
	self.sender = sender
	self.to = to
	self.pattern = pattern
	self.file = file
	self.keepalive = keepalive
	self.proxy = proxy
	self.timeout = timeout
	self.session = session
	pass

    def snoop(self):
        """
        Print all received messages
        """
        pass

    def listsession(self):
        """
        Get a list of all nodes in the current session
        
        :Returns:
            A list of all nodes in the current session
        """
        pass

    def exitsession(self):
        """
        Leave the current session

        :Returns:
            An integer indicating whether the action was successful.
        """
        pass

    def joinsession(self, name):
        """
        Join the named session

        :Args:
            *name*: the name of the session to join

        :Returns:
            An integer indicating whether the action was successful
        """
        pass

    def status(self):
        """
        Get the SAMP hub availability

        :Returns:
            A dictionary giving the boolean availability of the SAMP hub and the session manager
        """
        pass

    def list(self):
        """
        Get a list of all registered clients

        :Returns:
            A list of all registered clients

        """
        pass

    def access(self, appName):
        """
        Check the availability of the specified application.

        :Args:
            *appName*: the name of the application to be queried

        :Returns:
            A boolean indicating whether the specified application is available
        """
        pass

    def handle(self, mtype, callback = None):
        """
        Wait for a message of type mtype.

        :Args:
            *mtype*: the type of message to wait for.

            *callback*: a function to call when the appropriate message is received

        :Returns:
            A message of type mtype
        """
        pass

    def send(self, mtype, args):
        """
        Send a message of type mtype with the specified arguments

        :Args:
            *mtype*: the type of message to send
            
            *args*: a dictionary specifying the arguments to be included in the message

        :Returns:
            An integer indicating if the message was successfully sent
        """
        pass

    def sampexec(self, cmd):
        """
        Execute a client command

        This sends a SAMP message of mtype "client.cmd.exec" with the command.

        :Args:
            *cmd*: the command to execute

        :Returns:
            An integer indicating whether the command was successfully executed
        """
        pass

    def pointAt(self, ra, dec):
        """
        Point at the specified coordinates.

        This sends a SAMP message of mtype "coord.pointAt.sky" with the specified position

        :Args:
            *ra*: the Right Ascension coordinate of the location
            
            *dec*: the Declination coordinate of the location

        :Returns:
            An integer indicating whether the operation was successful
        """
        pass

    def setenv(self, name, value):
        """
        Set an environment variable to the specified value

        This sends a SAMP message of mtype "client.env.set" with the variable name and value

        :Args:
            *name*: the name of the environment variable 
            
            *value*: the value of the environment variable

        :Returns:
            An integer indicating if the environment variable was successfully set
        """
        pass

    def getenv(self, name):
        """
        Get the value of the specified environment variable

        This sends a SAMP message of mtype "client.env.get" with the variable name

        :Args:
            *name*: the name of the environment variable 

        :Returns:
            The value of the named variable. This returns None if the value is not set.
        """
        pass

    def setparam(self, name, value):
        """
        Set a parameter to the specified value

        This sends a SAMP message of mtype "client.param.set" with the parameter name and value

        :Args:
            *name*: the name of the parameter

            *value*: the value of the parameter

        :Returns:
            An integer indicating whether the parameter was successfully set
        """
        pass

    def getparam(self, name):
        """
        Get the value of the specified parameter

        This sends a SAMP message of mtype "client.param.get" with the parameter name

        :Args:
            *name*: the name of the parameter

        :Returns:
            The value of the named parameter. This returns None if the value is not set.
        """
        pass

    def load(self, url):
        """
        Load the specified image/table file

        This sends a SAMP message of mtype dependent on the type of file being loaded,
        i.e., based on its extension, with the URL

        :Args:
            *url*: the URL of the image or table to load

        :Returns:
            A URL indicating where the file can be accessed locally
        """
        pass

    def loadImage(self, url):
        """
        Load the specified image

        This sends a SAMP message of mtype "image.load.fits" with the image URL

        :Args:
            *url*: the URL of the image to load

        :Returns:
            A URL indicating where the image can be accessed locally
        """
        pass

    def loadVOTable(self, url):
        """
        Load the specified VOTable file

        This sends a SAMP message of mtype "table.load.votable" with the VOTable URL

        :Args:
            *url*: the URL of the VOTable to load

        :Returns:
            A URL indicating where the VOTable can be accessed locally
        """
        pass

    def loadFITS(self, url):
        """
        Load the specified FITS binary table

        This sends a SAMP message of mtype "table.load.fits" with the file URL

        :Args:
            *url*: the URL of the table to load

        :Returns:
            A URL indicating where the table can be accessed locally
        """
        pass

    def loadSpec(self, url):
        """
        Load the specified spectrum

        This sends a SAMP message of mtype "spectrum.load.*" with the spectrum URL

        :Args:
            *url*: the URL of the spectrum to load

        :Returns:
            A URL indicating where the spectrum can be accessed locally
        """
        pass

    def loadResource(self, url):
        """
        Load the specified VO Resource

        This sends a SAMP message of mtype "voresource.loadlist" with the resource URL

        :Args:
            *url*: the URL of the resource to load

        :Returns:
            A URL indicating where the resource can be accessed locally
        """
        pass

    def showRow(self, tblId, url, row):
        """
        Highlight the specified row in the named table at the given location

        This sends a SAMP message of mtype "table.highlight.row" with the row details

        :Args:
            *tblID*: the IVOA identifier for the table

            *url*: the URL where the table can be accessed

            *row*: the row to highlight

        :Returns:
            An integer indicating whether the operation was successful
        """
        pass

    def selectRows(self, tblId, url, row):
        """
        Select the specified rows in the named table at the given location

        This sends a SAMP message of mtype "table.select.rowList" with the row details

        :Args:
            *tblId*: the IVOA identifier for the table

            *url*: the URL where the table can be accessed

            *row*: the list of rows to highlight

        :Returns:
            An integer indicating if the selection was successful
        """
        pass

    def bibcode(self, bibcode):
        """
        Load the named bibcode

        This sends a SAMP message of mtype "bibcode.load" with the bibcode

        :Args:
            *bibcode*: the bibcode to be loaded

        :Returns:
            A URL indicating where the bibcode can be accessed locally
        """
        pass
    pass


class AccessError(Exception):
    """
    A base class for SAMP access failures
    """
    pass

class ServiceError(AccessError):
    """
    An exception indicating a failure communicating with a SAMP hub
    """
    pass


class Message():
    """
    A class representing a SAMP message.
    """

    def __init__(self, mtype, **args):
        self.mtype = mtype
        self.args = args

    def setMtype(self, mtype):
        """
        Set the mtype of this message

        :Args:
            *mtype*: the mtype of this message
        """
        self.mtype = mtype

    def getMtype(self):
        """
        Get the mtype of this message

        :Returns:
            The mtype of this message 
        """
        return self.mtype
      
    def setArgs(self, **args):
        """
        Set the arguments of this message

        :Args:
            *args*: a dictionary giving the arguments of this message
        """
        self.args = args

    def getArgs(self):
        """
        Get the arguments of this message

        :Returns:
            The arguments of this message
        """
        return self.args

