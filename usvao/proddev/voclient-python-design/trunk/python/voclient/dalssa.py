"""
This module implements the VO Simple Spectral Access (SSA) protocol, used
to query and access spectral data.
"""

import dalquery

def spectrasearch(url, pos, size, format='all'):
    """
    Submit a simple spectral access (SSA) query that requests spectra with the
    given characteristics [this needs to be generalized further as SSA is not
    limited to positional queries].

    :param url:    The base URL for the SSA service.
    :param pos:    A 2-element seqence giving the ICRS RA and DEC in decimal degrees.
    :param size:   A floating point number or a 2-element tuple giving the size
                   of the rectangular region around pos to search for spectra.  
    :param format: The spectral format(s) of interest.  "all" (default) 
                   indicates all available formats; "graphic" indicates
                   graphical images (e.g. jpeg, png, gif; not FITS); 
                   "metadata" indicates that no images should be 
                   returned--only an empty table with complete metadata.
    """
    service = SSAService(url)
    return service.search(pos, size, format)


class SSAService(dalquery.DALService):
    """
    A representation of an SSA service.
    """

    def __init__(self, baseurl, version="1.0", resmeta=None):
        """
        Instantiate an SSA service.

        :param baseurl:  The base URL for submitting search queries to the 
                         service.
        :param resmeta:  An optional dictionary of properties about the 
                         service.
        """
        dalquery.DALService.__init__(self, baseurl, "ssa", version, resmeta)

    def search(self, pos, size, format='all'):
        """
        Submit a simple SSA query to this service with the given constraints.  

        This method is provided for a simple but typical SSA queries.  For 
        more complex queries, one should create an SSAQuery object via 
        create_query().

        :param pos:     A 2-element tuple giving the ICRS RA and Dec of the 
                        center of the search region in decimal degrees.
        :param size:    A 2-element tuple giving the full rectangular size of 
                        the search region along the RA and Dec directions in 
                        decimal degrees.
        :param format:  The spectral format(s) of interest.  "all" (default) 
                        indicates all available formats; "graphic" indicates
                        graphical spectra (e.g. jpeg, png, gif; not FITS); 
                        "metadata" indicates that no spectra should be 
                        returned--only an empty table with complete metadata.
        """
        q = self.create_query(pos, size, format)
        return q.execute()

    def create_query(self, pos=None, size=None, format=None):
        """
        Create a query object that constraints can be added to and then 
        executed.  The input arguments will initialize the query with the 
        given values.

        :param pos:     A 2-element tuple giving the ICRS RA and Dec of the 
                        center of the search region in decimal degrees
        :param size:    A 2-element tuple giving the full rectangular size of 
                        the search region along the RA and Dec directions in 
                        decimal degrees
        :param format:  The image format(s) of interest.  "all" indicates 
                        all available formats; "graphic" indicates
                        graphical images (e.g. jpeg, png, gif; not FITS); 
                        "metadata" indicates that no images should be 
                        returned--only an empty table with complete metadata.

        :returns: **SSAQuery** -- The query instance.
           
        """
        q = SSAQuery(self.baseurl, self.version)

        if pos is not None: q.pos = pos
        if size is not None: q.size = size
        if format: q.format = format

        return q


class SSAQuery(dalquery.DALQuery):
    """
    A class for preparing an query to an SSA service.  Query constraints
    are added via its service type-specific methods.  The various execute()
    functions will submit the query and return the results.  

    The base URL for the query can be changed via the baseurl property.
    """
    
    def __init__(self, baseurl,  version="1.0", request="queryData"):
        """
        Initialize the query object with a baseurl and request type.
        """
        dalquery.DALQuery.__init__(self, baseurl, "ssa", version)
        self.setparam("REQUEST", request)
        
    @property
    def pos(self):
        """
        The position (POS) constraint as a 2-element tuple denoting RA and 
        declination in decimal degrees.  This defaults to None.
        """
        return self.getparam("POS")
    @pos.setter
    def pos(self, pair):
        # do a check on the input
        self.setparam("POS", pair)
    @pos.deleter
    def pos(self):
        self.unsetparam('POS')

    @property
    def ra(self):
        """
        The right ascension part of the position constraint (default: None).
        If this is set but dec has not been set yet, dec will be set to 0.0.
        """
        if not self.pos: return None
        return self.pos[0]
    @ra.setter
    def ra(self, val):
        if not self.pos: self.pos = (0.0, 0.0)
        self.pos = (val, self.pos[1])

    @property
    def dec(self):
        """
        The declination part of the position constraint (default: None).
        If this is set but ra has not been set yet, ra will be set to 0.0.
        """
        if not self.pos: return None
        return self.pos[1]
    @dec.setter
    def dec(self, val):
        if not self.pos: self.pos = (0.0, 0.0)
        self.pos = (self.pos[0], val)

    @property
    def size(self):
        """
        The diameter of the search region specified in decimal degrees.
        """
        return self.getparam("SIZE")
    @size.setter
    def size(self, val):
        # doc check on val
        self.setparam("SIZE", val)
    @size.deleter
    def size(self):
        self.unsetparam("SIZE")

    @property
    def band(self):
        """
        The spectral bandpass given in a range-list format.
        """
        return self.getparam("BAND")
    @band.setter
    def band(self, val):
        self.setparam("BAND", val)
    @band.deleter
    def band(self):
        self.unsetparam("BAND")

    @property
    def time(self):
        """
        The time coverage given in a range-list format using a restricted
        subset of ISO 8601.
        """
        return self.getparam("TIME")
    @time.setter
    def time(self, val):
        # do check on format
        self.setparam("TIME", val)
    @time.deleter
    def time(self):
        self.unsetparam("TIME")

    @property
    def format(self):
        """
        The desired format of the images to be returned.  This will be in the 
        form of a commna-separated lists of MIME-types or one of the following special
        values. 

        **Special Values:**
          * ``all`` --  all formats available
          * ``compliant`` --  any SSA data model compliant format
          * ``native`` --  the native project specific format for the spectrum
          * ``graphic`` --  any of the graphics formats: JPEG, PNG, GIF
          * ``votable`` --  the SSA VOTable format
          * ``fits`` --  the SSA-compliant FITS format
          * ``xml`` --  the SSA native XML serialization
          * ``metadata`` --  no images requested; only an empty table with fields 
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
        This implementation returns an SSAResults instance.

        :raises DALServiceError: for errors connecting to or 
                                 communicating with the service
        :raises DALQueryError:   if the service responds with 
                                 an error, including a query syntax error.  
        """
        return SSAResults(self.execute_votable(), self.getqueryurl())


class SSAResults(dalquery.DALResults):
    """
    Results from an SSA query.  It provides random access to records in 
    the response.  Alternatively, it can provide results via a Cursor 
    (compliant with the Python Database API) or an iterator.
    """

    def __init__(self, votable, url=None):
        """
        Initialize the cursor.  This constructor is not typically called 
        by directly applications; rather an instance is obtained from calling 
        a SSAQuery's execute().
        """
        dalquery.DALResults.__init__(self, votable, url, "ssa", "1.0")
        
    def getrecord(self, index):
        """
        Return an SSA result record that follows dictionary
        semantics.  The keys of the dictionary are those returned by this
        instance's fieldNames() function: either the column IDs or name, if 
        the ID is not set.  The returned record has additional accessor 
        methods for getting at standard SSA response metadata (e.g. ra, dec).
        """
        return SSARecord(self, index)


class SSARecord(dalquery.Record):
    """
    A dictionary-like container for data in a record from the results of an
    SSA query, describing an available spectrum.
    """

    def __init__(self, results, index):
        dalquery.Record.__init__(self, results, index)
        self._utypecols = results._ssacols
        self._names = results._recnames

    @property
    def ra(self):
        """
        Return the right ascension of the center of the image.
        """
        return self.get(self._names["pos"])[0]

    @property
    def dec(self):
        """
        Return the declination of the center of the image.
        """
        return self.get(self._names["pos"])[1]

    @property
    def title(self):
        """
        Return the title of the image.
        """
        return self.get(self._names["title"])

    @property
    def format(self):
        """
        Return the title of the image.
        """
        return self.get(self._names["format"])

    @property
    def dateobs(self):
        """
        Return the modified Julien date (MJD) of the mid-point of the 
        observational data that went into the image.
        """
        return self.get(self._names["dateobs"])

    @property
    def instr(self):
        """
        Return the name of the instrument (or instruments) that produced the 
        data that went into this image.
        """
        return self.get(self._names["instr"])

    @property
    def acref(self):
        """
        Return the URL that can be used to retrieve the image.
        """
        return self.get(self._names["acref"])

    def getdataurl(self):
        """
        Return the URL contained in the access URL column which can be used 
        to retrieve the dataset described by this record.  None is returned
        if no such column exists.
        """
        return self.acref

    def suggestExtension(self, default=None):
        """
        Returns a recommended filename extension for the dataset described 
        by this record.  Typically, this would look at the column describing 
        the format and choose an extension accordingly.  
        """
        fmt = default
        if self.format and self.format.startswith("image/"):
            fmt = self.format[len("image/"):]
            if fmt == "jpeg":  fmt = "jpg"
        return fmt

        
