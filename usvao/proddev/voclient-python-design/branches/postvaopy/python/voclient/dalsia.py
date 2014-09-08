"""
This module implements the VO Simple Image Access (SIA) protocol, used
to query and access astronomical image data.  A SIA query is issued to a
SIA service to find all images matching the query constraints; for SIA
V1 the primary query constraint is POS,SIZE defining a rectangular
region on the sky, specified in decimal degrees in the ICRS coordinate
system.  The FORMAT of the images requested may also be specified, e.g.,
"all", "image/fits", or "graphic".  The query response is a table
wherein each row describes one available image matching the query.  An
access reference URL is provided which can be used to download the image
described.

The images described in a query response may be either physical (archive
files) , or virtual images such as cutouts or reprojections which will
be generated on the fly if retrieved.  If the service being queried
delivers archival images the search region must intersect the area of a
target image for it to match.  If the service being queried is a cutout
or reprojection service then the search region specifies the ideal size
of the image to be generated.
"""

from dalquery import *

def imagesearch(url, pos, size, format='all', intersect="overlaps", verbosity=2):
    """
    Submit an simple image access (SIA) query that requests images overlapping a 
    specified region.

    :param url:        The base URL for the SIA service.
    :param pos:        A 2-element sequence giving the ICRS RA and DEC in decimal degrees.
    :param size:       A floating point number or a 2-element tuple giving the size
                       of the rectangular region around pos to search for images.  
    :param format:     The image format(s) of interest.  "all" (default) 
                       indicates all available formats; "graphic" indicates
                       graphical images (e.g. jpeg, png, gif; not FITS); 
                       "metadata" indicates that no images should be 
                       returned--only an empty table with complete metadata;
                       "image/*" indicates a particular image format where 
                       "*" can have values like "fits", "jpeg", "png", etc. 
    :param intersect:  A token indicating how the returned images should 
                       intersect with the search region.
    :param verbosity:  An integer value that indicates the volume of columns
                       to return in the result table.  0 means the minimum
                       set of columsn, 3 means as many columns as are 
                       available.  
    """
    service = SIAService(url)
    return service.search(pos, size, format, intersect, verbosity)


class SIAService(DALService):
    """
    An instance of a Simple Image Access (SIA) service.
    """

    def __init__(self, baseurl, resmeta=None, version="1.0"):
        """
        Instantiate an SIA service.

        :param baseurl:  The base URL for accessing the service.
        :param resmeta:  A dictionary of metadata that describes the service.
        :param version:  The SIA protocol version used by this service.
        """
        DALService.__init__(self, baseurl, protocol="sia", version=version, resmeta=None)

    def search(self, pos, size, format='all', intersect="overlaps", verbosity=2):
        """
        Submit a SIA query with the specified parameters.

        :param pos:  A 2-element sequence giving the region center, specified as
                     ICRS RA and DEC in decimal degrees.
        :param size: A floating point number or a 2-element tuple giving the region size
                     in decimal degrees.
        """
        q = self.createQuery()
        q.pos = pos
        q.size = size
        q.format = format
        q.intersect = intersect
        return q.execute()

    def create_query(self, pos=None, size=None, format=None, intersect=None, 
                    verbosity=None):
        """
        Create a SIA query object that can be extended or modified and then
        executed.  If any constraints are set with optional arguments, these 
        will be set in the returned query object.

        :returns: **SIAQuery** -- the query instance.
        """
        out = SIAQuery(self.baseurl, self.version)

        if not pos: out.pos = pos
        if not size: out.size = size
        if not format: out.format = format
        if not intersect: out.intersect = intersect
        if not verbosity: out.verbosity = verbosity

        return out


class SIAQuery(DALQuery):
    """
    A class for preparing an query to an SIA service.  Query constraints
    are added via its service type-specific methods.  The various execute()
    functions will submit the query and return the results.  

    The base URL for the query can be changed via the baseurl property.
    """

    def __init__(self, baseurl, version="1.0"):
        """
        Initialize the query object with a baseurl.
        """
        DALQuery.__init__(self, baseurl, protocol="sia", version=version)
        

    @property
    def pos(self):
        """
        The position (POS) constraint as a 2-element tuple denoting right
        ascension and declination in decimal degrees.  This defaults to None.
        """
        self._param.get("POS")
    @pos.setter
    def pos(self, pair):
        # do a check on the input
        self._param["POS"] = pair

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
        self.pos[0] = val

    @property
    def dec(self):
        """
        The declination part of the position constraint (default: None).
        If this is set but ra has not been set yet, ra will be set to 0.0.
        """
        if not self.pos: return None
        return self.pos[1]
    @ra.setter
    def dec(self, val):
        if not self.pos: self.pos = (0.0, 0.0)
        self.pos[1] = val

    @property
    def size(self):
        """
        The size of the region of interest specified in decimal degrees.
        """
        return self._param.get("SIZE")
    @size.setter
    def size(self, val):
        # do check on val; convert single number to a pair
        self._param["SIZE"] = val
    @size.deleter
    def size(self):
        self.unsetparam("SIZE")

    @property
    def format(self):
        """
        The desired format of the images to be returned.  This will be in the 
        form of a MIME-type or one of the following special values.  (Lower
        case are accepted on setting.)

        **Special Values:**
           * ``ALL`` -- all formats available 
           * ``GRAPHIC`` -- any graphical format (e.g. JPEG, PNG, GIF)
           * ``GRAPHIC-ALL`` -- all graphical formats available
           * ``METADATA`` -- no images reqested; only an empty table with fields 
             properly specified

        In addition, a value of :literal:`GRAPHIC-`:emphasis:`fmt[,fmt]` where 
        *fmt* is graphical format type (e.g. "jpeg", "png", "gif") that indicates 
        that a graphical format is desired with a preference for _fmt_ in the order 
        given.  Constrain the images in the query response to the indicated formats.
        """
        return self._param.get("FORMAT")
    @format.setter
    def format(self, val):
        self._param["FORMAT"] = val
    @format.deleter
    def format(self):
        self.unsetparam("FORMAT")

    @property
    def intersect(self):
        """
        For archival image discovery specify how the search region intersects
        the footprint of a target image.
        """
        return self._param.get("INTERSECT")
    @intersect.setter
    def intersect(self, val):
        self._param["INTERSECT"] = val
    @intersect.deleter
    def intersect(self):
        self.unsetparam("INTERSECT")

    @property
    def verbosity(self):
        """
        Specify how verbose the output image description should be (levels 0 - 3).
        """
        return self._param.get("VERB")
    @verbosity.setter
    def verbosity(self, val):
        # do a check on val
        self._param["VERB"] = val
    @verbosity.deleter
    def verbosity(self):
        self.unsetparam("VERB")


class SIAResults(DALResults):
    """
    Results from an SIA query.  It provides random access to records in 
    the response.  Alternatively, it can provide results via a Cursor 
    (compliant with the Python Database API) or an iterable.
    """

    def __init__(self, url=None, version="1.0"):
        """
        Initialize the result set.  This constructor is not typically called 
        by directly applications; rather an instance is obtained from calling 
        a SIAQuery's execute().
        """
        DALResults.__init__(self, url, "sia", version)

    def getrecord(self, index):
        """
        Return an SIA result record that follows dictionary
        semantics.  The keys of the dictionary are those returned by this
        instance's fieldNames() function: either the column IDs or name, if 
        the ID is not set.  The returned record has additional accessor 
        methods for getting at stardard SIA response metadata (e.g. ra, dec).
        """
        pass


class SIARecord(Record):
    """
    A dictionary-like container for data in a record from the results of an
    SIA query, describing an available physical or virtual image.
    """

    def __init__(self, ranm=None, decnm=None, titlenm=None, instrnm=None,
                 dateobsnm=None, naxesnm=None, naxisnm=None, acrefnm=None):
        # Since this constructor should not be considered public, the 
        # signature is only indicative; the actual signature will depend 
        # on the implementation
        
        self._names = { "title": titlenm, "ra": ranm, "dec": decnm,
                        "instr": instrnm, "dateobs": dateobsnm, 
                        "naxes": naxesnm, "naxis": naxisnm, "acref": acrefnm }

    @property
    def ra(self):
        """
        Return the right ascension of the center of the image, specified in decimal
        degrees in the ICRS coordinate system.
        """
        return self[self._names["ra"]]

    @property
    def dec(self):
        """
        Return the declination of the center of the image, specified in decimal
        degrees in the ICRS coordinate system.
        """
        return self[self._names["dec"]]

    @property
    def title(self):
        """
        Return the title of the image.
        """
        return self[self._names["title"]]

    @property
    def dateobs(self):
        """
        Return the modified Julien date (MJD) of the mid-point of the 
        observational data that went into the image.
        """
        return self[self._names["dateobs"]]

    @property
    def naxes(self):
        """
        Return the number of axes in this image.
        """
        return self[self._names["naxes"]]

    @property
    def naxis(self):
        """
        Return the lengths of the sides along each axis, in pixels, as 
        a sequence.
        """
        return self[self._names["naxis"]]

    @property
    def instr(self):
        """
        Return the name of the instrument (or instruments) that produced the 
        data that went into this image.
        """
        return self[self._names["instr"]]

    @property
    def acref(self):
        """
        Return the URL (access reference) that can be used to retrieve the image.
        """
        return self[self._names["acref"]]

