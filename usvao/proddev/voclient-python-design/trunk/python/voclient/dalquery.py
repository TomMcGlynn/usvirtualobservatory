"""
A module for walking through the query response of VO data access layer
(DAL) queries, registry queries, and general VOTable-based datasets.

Most data queries in the VO return a table result, usually formatted as
a VOTable.  Each row of the table describes a single physical or virtual
dataset which can be retrieved.  For uniformity, datasets are described
via standard metadata defined by a data model specific to the type of
data being queried.  The fields of the data model are identified by
their fixed VOClient shortname or alias as defined in this interface, or
at a lower level by the Utype, UCD, or field name or ID of the specific
standard and version of the standard being queried.  While the data
model differs depending upon the type of data being queried, the form of
the query response is the same for all classes of data, including
registry queries, allowing a common query response interface to be used.

An exception to this occurs when querying an astronomical catalog or
other externally defined table.  In this case there is no VO defined
standard data model, rather the table defines its own data model
implicitly.  Usually the field names are used to uniquely identify table
columns.  On occasion UCDs may provide additional information on the
physical meaning of a table column.

The high level query interface described here walks through the query
response row by row, much as if we were querying a typical relational
database.  Both a high level Python interface and a Python DBI interface
(Python Database API V2.0) are provided.  Once a result set is obtained,
either the high level iterable Python interface or the generic PY-DBI
interface may be used to traverse the query response.  Direct access to
the underlying VOTable is also provided.
"""

class DALService():
    """
    An abstract base class representing a DAL service located at a particular 
    endpoint.  This is subclassed by each actual service type.
    """

    def __init__(self, baseurl, protocol=None, version=None, resmeta=None):
        """
        Instantiate the service connecting it to a base URL.

        :param baseurl:  The base URL that should be used for forming queries to
                         the service.
        :param protocol: The protocol implemented by the service, e.g., "scs", "sia",
                           "ssa", and so forth.
        :param version:  The protocol version, e.g, "1.0", "1.2", "2.0".
        :param resmeta:  A dictionary containing resource metadata describing the 
                         service.  This is usually provided a registry record.
        """
        self._baseurl = baseurl
        self._protocol = protocol
        self._version = version
        if not resmeta:
            self._desc = {}
        elif isinstance(resmeta, dict):
            self._desc = copy.deepcopy(resmeta)

    @property
    def baseurl(self):
        """
        The base URL to use for submitting queries (read-only).
        """
        return self._baseurl

    @property
    def protocol(self):
        """
        The service protocol implemented by the service (read-only).
        """
        return self._protocol

    @property
    def version(self):
        """
        The version of the service protocol implemented by the service (read-only).
        """
        return self._version

    @property
    def description(self):
        """
        an optional dictionary of resource metadata that describes the 
        service.  This is generally information stored in a VO registry.  
        """
        return self._desc;

#   def search(self):
#       """
#       Perform a discovery query using the service.  This is replaced by an
#       actual Search method in service#specific subclasses, including query
#       parameters specific to the type of service.
#       """
#       pass

    def create_query(self):
        """
        Create a query object that constraints can be added to followed by query
        execution.

        In subclasses, this will return a DALQuery subclass appropriate for the 
        service.
        """
        pass


class DALQuery():
    """
    Generic class to prepare and execute a query.  This is subclassed by
    a specific service class to define any custom query parameters.
    Query constraints are typically added via its service type-specific
    methods; however, they can be added generically (including custom
    parameters) via the setparam() function.  The various execute()
    functions will submit the query and return the results.

    The base URL for the query can be changed via the baseurl property.
    """

    def __init__(self, baseurl, protocol=None, version=None):
        """
        Initialize the query object with the baseurl and identify of the service.
        """
        self._baseurl = baseurl
        self._protocol = protocol
        self._version = version
        self._param = { }

    @property
    def baseurl(self):
        """
        The base URL that this query will be sent to when one of the 
        execute functions is called. 
        """
        return self._baseurl

    @baseurl.setter
    def baseurl(self, value):
        self._baseurl = value

    @property
    def protocol(self):
        """
        The service protocol implemented by this service.
        """
        return self._protocol

    @property
    def version(self):
        """
        The version of the service protocol implemented.
        """
        return self._version

    def setparam(self, name, val):
        """
        Add a parameter constraint to the query.

        :param name:  The name of the parameter.  This should be a name that 
                      is recognized by the service itself.  
        :param val:   The value for the constraint.  This value must meet the 
                      requirements set by the standard or by the service.  If 
                      the constraint consists of multiple values, it should be 
                      passed as a sequence.  
        """
        self._param[name] = val

    def unsetparam(self, name):
        """
        Unset the parameter constraint having the given name (if it is set).
        """
        del self._param[name]

    def getparam(self, name):
        """
        Return the current value of the parameter with the given name or None
        if it is not set.
        """
        return self._param.get(name)

    def getparams(self):
        """
        Return all currently set params as a dictionary.
        """
        pass

    def paramnames(self):
        """
        Return the names of the parameters set so far.
        """
        return self._param.keys()

    def execute(self):
        """
        Submit the query and return the results as a result set instance.

        **Raises:**
           *DALServiceError*: 
               For errors connecting to or communicating with the service.
           *DALQueryError*:   
               If the service responds with an error, including a query syntax 
               error.  
           *DALFormatError*: 
               for errors parsing the VOTable response
        
        """
        pass 

    def execute_raw(self):
        """
        Submit the query and return the raw VOTable XML as a string.

        :raises DALServiceError: For errors connecting to or         
                                 communicating with the service.     
        :raises DALQueryError:   for errors in the input query syntax
           
        """
        pass 

    def execute_votable(self):
        """
        Submit the query and return the results as an AstroPy votable instance.
        This is available only if AstroPy is loaded into the runtime environment.

        :raises DALServiceError: For errors connecting to or 
                                 communicating with the service.

        """
        pass

    def execute_table(self):
        """
        Submit the query and return the results as an AtPy astronomical table instance.
        This is available only if AtPy is loaded into the runtime environment.

        :raises DALServiceError: For errors connecting to or 
                                 communicating with the service.
        """
        pass

    def getqueryurl(self):
        """
        Return the HTTP GET URL that encodes the current query.  This is the 
        URL that the execute functions will use if called next.  
        """
        return ensure_baseurl(self.baseurl) + \
            "&".join(map(lambda p: "%s=%s" % (p, self._param[p]), 
                         self._param.keys()))


class DALResults():
    """
    The result set from a query.  It provides random access to
    records in the response.  Alternatively, it can provide results via
    a Cursor (compliant with the Python Database API) or an iterable.
    All view the same query response data.
    """

    def __init__(self, url=None, votable=None, protocol=None, version=None):
        """
        Initialize the result set.  This constructor is not typically called 
        by directly applications; rather an instance is obtained from calling 
        a DALQuery's execute().  An exception is when creating a DALResults
        instance from a saved VOTable using the "votable" parameter.
        """
        self._queryurl = url
        self._protocol = protocol
        self._version = version
        self._rowcount = 0

    def __iter__(self):
        """
        Return a python iterable for stepping through the records in this
        result set.
        """
        pass # return Iter(self)

    @property
    def queryurl(self):
        """
        The URL query that produced these results.  None is returned if unknown.
        """
        return self._queryurl

    @property
    def protocol(self):
        """
        The service protocol which generated this query response (read-only).
        """
        return self._protocol

    @property
    def version(self):
        """
        The version of the service protocol which generated this query response
        (read-only).
        """
        return self._version

    @property
    def rowcount(self):
        """
        The number of records returned in this result (read-only).
        """
        return self._rowcount

    def infos(self):
        """
        Return any INFO elements in the VOTable as a dictionary.

        :returns: A dictionary with each element corresponding to a 
                  single INFO, representing the INFO as 
                  { name: (value, content) }.
        """
        pass

    def fieldnames(self):
        """
        Return the names of the table columns.  These are the names that
        are used to access values from the dictionaries returned by
        getrecord().  They correspond to the ID of the column, if it is
        set, or otherwise to the column name.  In VOTable terms both
        PARAM and FIELD are treated as table columns here, with PARAM
        corresponding to a table column which has the same value in all
        table rows.  Herein "field" or "Field" refers to either a VOTable
        FIELD or PARAM.
        """
        pass

    def fielddesc(self):
        """
        Return the full metadata for a column as a Field instance, a
        simple object with attributes corresponding the the VOTable
        FIELD or PARAM attributes, namely: id, name, datatype,
        arraysize, width, precision, unit, ucd, utype, description.  An
        additional attribute "param" is provided, with the value True if
        the field instance is a VOTable PARAM and False if the field is
        a VOTable FIELD.
        """
        pass

    def fieldname_byucd(self, ucd):
        """
        Return the field name that has a given UCD value or None if the UCD 
        is not found.  None is also returned if the UCD is None or an empty 
        string.  The UCD string is case insensitive.
        """
        pass

    def fieldname_byutype(self, utype):
        """
        Return the field name that has a given UType value or None if the UType 
        is not found.  None is also returned if UType is None or an empty string.
        The UType string is case insensitive.
        """
        pass

    def getrecord(self, index):
        """
        Return a representation of a result record that follows dictionary
        semantics.  The keys of the dictionary are those returned by this
        instance's fieldnames() function: either the column IDs or name, if 
        the ID is not set.  The returned record may have additional accessor 
        methods for getting at standard DAL response metadata (e.g. ra, dec).

        :param index:  the integer index of the desired record where 0 returns
                       the first record

        :raises IndexError: if index is negative or equal or larger than the 
                            number of rows in the result table.
        """
        pass

    def getcolumn(self, name):
        """
        Return a table column.

        :param name:    The name of the table field or column.

        :returns:  The value returned is an array or list depending upon the 
                   datatype of the column.  Numeric columns results in an array 
                   result, otherwise a list is returned.  A NumPy array may be 
                   returned for numeric columns if NumPy is available in the 
                   runtime environment.                      
        :raises KeyError:   If name is not a recognized column name.
        """
        pass

    def getvalue(self, name, index):
        """
        Return the value of a record attribute -- a value from a given table
        column and row.

        :param name:   The name of the record attribute (table column).
        :param index:  The zero-based index of the record.

        :returns:  a scalar or array depending upon the datatype of the 
	           attribute.  A NumPy array may be returned for array 
                   valued attributes if NumPy is available in the runtime
                   environment.

        :raises IndexError: if index is negative or equal or larger than the 
                            number of rows in the result table.
        :raises KeyError:   if name is not a recognized column name.
        """
        pass

    def cursor(self):
        """
        Return a cursor that is compliant with the Python Database API's 
        Cursor interface.
        """
        return Cursor(self)


# class Iter():
#     """
#     Custom iterator class for stepping through a result set.
#     """
# 
#     def __init__(self, resultset):
#         self.resultset = resultset
#         self.pos = 0
# 
#     def __iter__(self):
#         return self
# 
#     def next(self):
#         out = self.resultset.getrecord(self.pos)
#         self.pos += 1
#         return out


class Cursor():
    """
    A class used to walk through a query response table row by row, 
    accessing the contents of each record (row) of the table.  This class
    implements the Python Database API.  VOTable PARAM and FIELD elements
    are equivalent in this view, both appearing as table columns.
    """

    def __init__(self, dalresults):
        """
        Create a cursor instance.  The constructor is not typically called 
        directly by applications; rather an instance is obtained from calling a 
        DALQuery's cursor() method.
        """
        self._dalresults = dalresults
        self._description = None
        self._rowcount = 0
        self._arraysize = 1

    @property
    def dalresults(self):
        """
        Back reference to the DALResults instance (read only).
        """
        return self._dalresults

    @property
    def description(self):
        """
        A read-only sequence of 7-item tuples as defined in the DBI
        specification.  Each tuple describes a column in the results,
        giving its name, type_code, and optionally other attributes such
        as whether null values are permitted for the field.  Attributes
        not provided have the value None.  The column name is the value
        of the VOTable NAME attribute for the column.
        """
        return self._description  # this should be a deep copy

    @property
    def rowcount(self):
        """
        The number of rows in the result set (read-only).
        """
        return self._rowcount

    @property
    def arraysize(self):
        """
        The number of rows that will be returned by returned by a call to 
        fetchmany().  This defaults to 1, but can be changed.  
        """
        return self._arraysize

    @arraysize.setter
    def arraysize(self, value):
        self._arraysize = value

    def fetchone(self):
        """
        Return the next row of the query response table.

        :returns: a tuple representing a table row, wherein each
                  element is the value of the corresponding table field.        
        """
        pass

    def fetchmany(self, size=None):
        """
        Fetch the next block of rows from the query result.

        :param size: The number of rows to return (default: cursor.arraysize).

        :returns:  A list of tuples, one per row.  An empty sequence is 
                   returned when no more rows are available.                                       
        """
        pass

    def fetchall(self):
        """
        Fetch all remaining rows from the result set.

        :returns:  A list of tuples, one per row.  An empty sequence is returned 
                   when no more rows are available.
        """
        pass

    def next(self):
        """
        Advance to the next row.  
        A StopIteration exception is raised when there are no more rows.
        """
        pass

    def scroll(self, value, mode=None):
        """
        Move the row cursor.

        :param value:  the number of rows to skip or the row number to position to.
        :param mode:   either "relative" for a relative skip, or "absolute" to position
                       to a row by its absolute index within the result set (zero indexed).
        """
        pass

    def close(self):
        """
        Close the cursor object and free all resources.  This implementation
        does nothing.  It is provided for compliance with the Python Database API.  
        """
        # this can remain implemented as "pass" 
        pass


class Field(object):

    def __init__(self, param=False, id=None, name=None, datatype=None,
            arraysize=None, width=None, precision=None, unit=None,
            ucd=None, utype=None, descr=None):
        """
        Initialize the attributes of a table Field (VOTable PARAM or FIELD).
        """
        self.param = param      # True if the field is a VOTable PARAM
        self.id = id
        self.name = name
        self.datatype = datatype
        self.arraysize = arraysize
        self.width = width
        self.precision = precision
        self.unit = unit
        self.ucd = ucd
        self.utype = utype
        self.descr = descr


class Record(dict):
    """
    A Record instance corresponds to one row of the result set table.
    For a DAL or registry query the record describes a dataset or other
    resource which can be accessed.  The column values are accessible as
    dictionary items indexed by the field name as defined by
    fieldnames().  Methods are provided for retrieving or accessing the
    dataset the record describes.  Subclasses may provide additional
    functions for access to service type-specific data.
    """

    def fielddesc(self, name):
        """
        Return a Field instance with attributes (id, name, datatype, etc.)
        that describe the record attribute with the given name.  
        """
        pass

    def getdataurl(self):
        """
        Return the URL (access reference) contained in the access URL column
        which can be used to retrieve the dataset described by this record.
        None is returned if no such column exists or has a null value.
        """
        pass

    def getdataset(self):
        """
        Get the dataset described by this record.  Valid for DAL queries where
        the access reference or data URL is defined (registry queries do not
        have an associated dataset).

        :returns:  a file-like object which may be read to retrieve the 
                   referenced dataset.

        :raises KeyError: If no datast access URL is included in the record
        :raises IOError:  If an error occurs while accessing the URL or 
                          writing out the dataset
        """
        pass

    def cachedataset(self, filename=None):
        """
        Retrieve the dataset described by this record and write it out to 
        a file with the given name.  If the file already exists, it will be
        over-written.

        :param filename:  The path to a file to write to.  If None, a default
                          name is attempted based on the record title and 
                          format.

        :raises KeyError:   If no dataset access URL is included in the record
        :raises IOError:    If an error occurs while accessing the URL or 
                            writing out the dataset.
        """
        pass

    def suggest_extension(self, default=None):
        """
        Returns a recommended filename extension for the dataset described 
        by this record.  Typically, this would look at the column describing 
        the format and choose an extension accordingly.  

        :param default:  The default value to return if the dataset type can 
                         not be determined sufficiently by the record data.  
        """
        # abstract; specialized for the different service types
        return default


class DALAccessError(Exception):
    """
    A base class for failures while accessing a DAL service.
    """
    _defreason = "Unknown service access error"

    def __init__(self, reason=None, url=None, protocol=None, version=None):
        """
        Initialize the exception with an error message.

        :param reason:    a message describing the cause of the error
        :param url:       the query URL that produced the error
        :param protocol:  the label indicating the type service that produced 
                          the error
        :param version:   version of the protocol of the service that produced 
                          the error
        """
        if not reason: reason = self._defreason
        Exception.__init__(self, reason)
        self._reason = reason
        self._url = url

    @property
    def reason(self):
        """
        A string description of what went wrong.
        """
        return self._reason
    @reason.setter
    def reason(self, val):
        if val is None: val = self._defreason
        self._reason = val
    @reason.deleter
    def reason(self):
        self._reason = self._defreason

    @property
    def url(self):
        """
        The URL that produced the error.  If None, the URL is unknown or unset.
        """
        return self._url
    @url.setter
    def url(self, val):
        self._url = val
    @url.deleter
    def url(self):
        self._url = None

    @property
    def protocol(self):
        """
        A label indicating the type service that produced the error.
        """
        return self._protocol
    @protocol.setter
    def protocol(self, protocol):
        self._protocol = protocol
    @protocol.deleter
    def protocol(self):
        self._protocol = None

    @property
    def version(self):
        """
        The version of the protocol of the service that produced the error.
        """
        return self._version
    @version.setter
    def version(self, version):
        self._version = version
    @version.deleter
    def version(self):
        self._version = None


class DALProtocolError(DALAccessError):
    """
    A base exception indicating that a DAL service responded in an
    erroneous way.  This can be either an HTTP protocol error or a
    response format error; both of these are handled by separate
    supclasses.  This base class captures an underlying exception
    clause. 
    """
    _defreason = "Unknown DAL Protocol Error"

    def __init__(self, reason=None, cause=None, url=None, 
                 protocol=None, version=None):
        """
        Initialize with a string message and an optional HTTP response code.

        :param reason:    a message describing the cause of the error
        :param cause:     an exception issued as the underlying cause.  A value
        :param url:       the query URL that produced the error
        :param protocol:  the label indicating the type service that produced 
                          the error
        :param version:   version of the protocol of the service that produced 
                          the error
        """
        DALAccessError.__init__(self, reason, url, protocol, version)
        self._cause = cause

    @property
    def cause(self):
        """
        A string description of what went wrong.
        """
        return self._cause
    @cause.setter
    def cause(self, val):
        self._cause = val
    @cause.deleter
    def cause(self):
        self._cause = None


class DALFormatError(DALProtocolError):
    """
    An exception indicating that a DAL response contains fatal format errors.
    This would include XML or VOTable format errors.  
    """
    _defreason = "Unknown VOTable Format Error"

    def __init__(self, cause=None, url=None, reason=None, 
                 protocol=None, version=None):
        """
        create the exception
        :param cause:     an exception issued as the underlying cause.  A value
                          of None indicates that no underlying exception was 
                          caught.
        :param url:       the query URL that produced the error
        :param reason:    a message describing the cause of the error
        :param protocol:  the label indicating the type service that produced 
                          the error
        :param version:   version of the protocol of the service that produced 
                          the error
        """
        DALProtocolError.__init__(self, reason, cause, url, protocol, version)


class DALServiceError(DALProtocolError):
    """
    An exception indicating a failure communicating with a DAL
    service.  Most typically, this is used to report DAL queries that result 
    in an HTTP error.  
    """
    _defreason = "Unknown service error"
    
    def __init__(self, reason=None, code=None, cause=None, url=None, 
                 protocol=None, version=None):
        """
        Initialize with a string message and an optional HTTP response code.

        :param reason:    a message describing the cause of the error
        :param code:      the HTTP error code (as an integer)
        :param cause:     an exception issued as the underlying cause.  A value
                          of None indicates that no underlying exception was 
                          caught.
        :param url:       the query URL that produced the error
        :param protocol:  the label indicating the type service that produced 
                          the error
        :param version:   version of the protocol of the service that produced 
                          the error
        """
        DALProtocolError.__init__(self, reason, cause, url, protocol, version)
        self._code = code

    @property
    def code(self):
        """
        The HTTP error code that resulted from the DAL service query,
        indicating the error.  If None, the service did not produce an HTTP 
        response.
        """
        return self._code
    @code.setter
    def code(self, val):
        self._code = val
    @code.deleter
    def code(self):
        self._code = None


class DALQueryError(DALAccessError):
    """
    An exception indicating an error by a working DAL service while processing
    a query.  Generally, this would be an error that the service successfully 
    detected and consequently was able to respond with a legal error response--
    namely, a VOTable document with an INFO element contains the description
    of the error.  Possible errors will include bad usage by the client, such
    as query-syntax errors.
    """
    _defreason = "Unknown DAL Query Error"

    def __init__(self, reason=None, label=None, url=None, 
                 protocol=None, version=None):
        """
        :param reason:    a message describing the cause of the error.  This 
                          should be set to the content of the INFO error element.
        :param label:     the identifying name of the error.  This should be the 
                          value of the INFO element's value attribute within the 
                          VOTable response that describes the error.
        :param url:       the query URL that produced the error
        :param protocol:  the label indicating the type service that produced 
        :param version:   version of the protocol of the service that produced 
                          the error
        """
        DALAccessError.__init__(self, reason, url, protocol, version)
        self._label = label
                          
    @property
    def label(self):
        """
        The identifing name for the error given in the DAL query response.
        DAL queries that produce an error which is detectable on the server
        will respond with a VOTable containing an INFO element that contains 
        the description of the error.  This property contains the value of 
        the INFO's value attribute.  
        """
        return self._label
    @label.setter
    def label(self, val):
        self._label = val
    @label.deleter
    def label(self):
        self._label = None



def ensure_baseurl(url):
    """
    Ensure a well formed DAL base URL that ends either with a '?' or a '&'.
    """
    if '?' in url:
        if url[-1] == '?' or url[-1] == '&':
            return url
        else:
            return url+'&'
    else:
        return url+'?'

