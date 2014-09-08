"""
The DAL Query interface specialized for Spectral Line Access (SLA) services.
"""

import dalquery

def linesearch(url, wavelength):
    """
    Submit a spectral line access (SLA) query that requests spectral lines 
    within a wavelength range.

    :param url:         The base URL for the SLA service.
    :param wavelength:  A 2-element sequence giving the wavelength spectral 
                        range to search in meters.
    """
    service = SLAService(url)
    return service.search(wavelength)


class SLAService(dalquery.DALService):
    """
    A representation of an SLA service.
    """

    def __init__(self, baseurl, resmeta=None, version="1.0"):
        """
        Instantiate an SLA service.

        :param baseurl:  The base URL for submitting search queries to the 
                         service.
        :param resmeta:  An optional dictionary of properties about the 
                         service
        """
        dalquery.DALService.__init__(self, baseurl, "sla", version, resmeta)

    def search(self, wavelength, format=None):
        """
        Submit a simple SLA query to this service with the given constraints.  

        This method is provided for a simple but typical SLA queries.  For 
        more complex queries, one should create an SLAQuery object via 
        create_query().

        :param wavelength: A 2-element sequence giving the wavelength spectral
                           range to search in meters
        :param format:     The spectral format(s) of interest. "metadata" indicates that no
                           spectra should be returned--only an empty table with complete metadata.
        """
        q = self.create_query(wavelength, format)
        return q.execute()

    def create_query(self, wavelength=None, format=None):
        """
        Create a query object that constraints can be added to and then 
        executed.  The input arguments will initialize the query with the 
        given values.

        :param wavelength: A 2-element tuple giving the wavelength spectral
                           range to search in meters
        :param format:     The spectral format(s) of interest. "metadata" indicates that no
                           spectra should be returned--only an empty table with complete metadata.

        :returns: **SLAQuery** -- the query instance
        """
        q = SLAQuery(self.baseurl, self.version)
        if wavelength is not None: q.wavelength = wavelength
        if format: q.format = format
        return q


class SLAQuery(dalquery.DALQuery):
    """
    A class for preparing an query to an SLA service.  Query constraints
    are added via its service type-specific methods.  The various execute()
    functions will submit the query and return the results.  

    The base URL for the query can be changed via the baseurl property.
    """
    
    def __init__(self, baseurl,  version="1.0", request="queryData"):
        """
        Initialize the query object with a baseurl and request type.
        """
        dalquery.DALQuery.__init__(self, baseurl, "sla", version)
        self.setparam("REQUEST", request)
        
    @property
    def wavelength(self):
        """
        The wavelength range given in a range-list format.
        """
        return self.getparam("WAVELENGTH")
    @wavelength.setter
    def wavelength(self, val):
        # Check valid value.
        self.setparam("WAVELENGTH", val)
    @wavelength.deleter
    def wavelength(self):
        self.unsetparam("WAVELENGTH")

    @property
    def format(self):
        """
        The desired format of the images to be returned.  This will be in the 
        form of a commna-separated lists of MIME-types or one of the following special
        values. 

        **Special Values:**
          * ``metadata`` -- no images requested; only an empty table with fields
            properly specified

        """
        return self.getparam("FORMAT")
    @format.setter
    def format(self, val):
        # check values
        self.setparam("FORMAT", val)
    @format.deleter
    def format(self):
        self.unsetparam("FORMAT")

    def execute(self):
        """
        Submit the query and return the results as a Results subclass instance.
        This implementation returns an SLAResults instance.

        :Raises:
           *DALServiceError*: For errors connecting to or 
                              communicating with the service.
           *DALQueryError*:   If the service responds with 
                              an error, including a query syntax error.  
        """
        return SLAResults(self.execute_votable(), self.getqueryurl())


class SLAResults(dalquery.DALResults):
    """
    Results from an SLA query.  It provides random access to records in 
    the response.  Alternatively, it can provide results via a Cursor 
    (compliant with the Python Database API) or an iterator.
    """

    def __init__(self, votable, url=None):
        """
        Initialize the cursor.  This constructor is not typically called 
        by directly applications; rather an instance is obtained from calling 
        a SLAQuery's execute().
        """
        dalquery.DALResults.__init__(self, votable, url, "sla", "1.0")
        
        
    def getrecord(self, index):
        """
        Return an SLA result record that follows dictionary
        semantics.  The keys of the dictionary are those returned by this
        instance's fieldNames() function: either the column IDs or name, if 
        the ID is not set.  The returned record has additional accessor 
        methods for getting at standard SLA response metadata (e.g. ra, dec).
        """
        return SLARecord(self, index)


class SLARecord(dalquery.Record):
    """
    A dictionary-like container for data in a record from the results of an
    SLA query, describing an individual spectral line.
    """

    def __init__(self, results, index):
        dalquery.Record.__init__(self, results, index)
        self._utypecols = results._slacols
        self._names = results._recnames

    @property
    def title(self):
        """
        Return the title of the line list.
        """
        return self.get(self._names["title"])

