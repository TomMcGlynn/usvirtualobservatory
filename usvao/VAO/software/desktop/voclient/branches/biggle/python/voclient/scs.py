"""
The DAL Query interface specialized for Simple Cone Search (SCS) services.

This module implements the VO Simple Cone Search (SCS or conesearch) 
protocol, used to select records from an astronomical source catalog
or observation list based on a position and a distance.  A Conesearch 
query is issued to an SCS service to find all sources or observations 
whose positions fall within a given distance of the given position
(set via the RA, DEC, and SR parameters).  The query response is a table
wherein each row describes one source or observation that falls within
the position-distance "cone"; arbitrary metadata about the source or 
observation can be included for each source.  
"""

import query
from voclient import VOClient as voclient
from voclient import libvot as libvot
#from odict import odict

def conesearch(url, ra, dec, sr=1.0, verbosity=2):
    """
    Submit a Simple Cone Search (SCS) query that requests objects or
    observations whose positions fall within some distance from a search
    position.

    :param url:        The base URL of the query service.
    :param ra:        The ICRS right ascension position of the center of the 
    :param dec:       The ICRS declination position of the center of the 
                      circular search region, in decimal degrees
    :param sr:        The radius of the circular search region, in decimal degrees
    :param verbosity: An integer value that indicates the volume of columns
                      to return in the result table.  0 means the minimum
                      set of columsn, 3 means as many columns as are 
                      available. 

    """
    service = SCSService(url)
    return service.search(ra, dec, sr, verbosity)


class SCSService(query.DALService):
    """
    A representation of a Cone Search service.
    """
    VOC_RAW = 0
    VOC_CSV = 1
    VOC_TSV = 2
    VOC_ASCII = 3
    VOC_VOTABLE = 4
    def __init__(self, baseurl, resmeta=None, version="1.0"):
        """
        Instantiate a Cone Search service.

        :param baseurl:  The base URL for submitting search queries to the 
                         service.
        :param resmeta:  An optional dictionary of properties about the 
                         service
        """
        query.DALService.__init__(self, baseurl, "scs", version, resmeta)

    def search(self, ra, dec, sr=1.0, verbosity=2):
        """
        Submit a simple Cone Search query that requests objects or observations
        whose positions fall within some distance from a search position.  

        :param ra:        The ICRS right ascension position of the center of the 
                          circular search region, in decimal degrees.
        :param dec:       The ICRS declination position of the center of the 
                          circular search region, in decimal degrees.
        :param sr:        The radius of the circular search region, in decimal 
                          degrees.
        :param verbosity: An integer value that indicates the volume of columns
                          to return in the result table.  0 means the minimum
                          set of columsn, 3 means as many columns as are 
                          available. 
        """
        q = self.create_query(ra, dec, sr, verbosity)
        return q.execute()

    def create_query(self, ra=None, dec=None, sr=None, verbosity=None):
        """
        Create a query object that constraints can be added to and then 
        executed.  The input arguments will initialize the query with the 
        given values.

        :param ra:        The ICRS right ascension position of the center of the 
                          circular search region, in decimal degrees
        :param dec:       The ICRS declination position of the center of the 
                          circular search region, in decimal degrees
        :param sr:        The radius of the circular search region, in decimal 
                          degrees
        :param verbosity: An integer value that indicates the volume of columns
                          to return in the result table.  0 means the minimum
                          set of columsn, 3 means as many columns as are 
                          available. 
        """
        q = SCSQuery(self._baseurl)
        if ra  is not None:  q.ra  = ra
        if dec is not None:  q.dec = dec
        if sr  is not None:  q.sr  = sr
        if verbosity is not None: q.verbosity = verbosity
	q.dal_id = self.dal_id
        return q


class SCSQuery(query.DALQuery):
    """
    A class for preparing an query to a Cone Search service.  Query constraints
    are added via its service type-specific methods.  The various execute()
    functions will submit the query and return the results.  

    The base URL for the query can be changed via the baseurl property.
    """

    def __init__(self, baseurl, version="1.0"):
        """
        Initialize the query object with a baseurl
        """
        query.DALQuery.__init__(self, baseurl, "scs", version)
        

    @property
    def ra(self):
        """
        The right ascension part of the position constraint (default: None).
        """
        return self.getparam("RA")
    @ra.setter
    def ra(self, val):
        # Do a check on the value.
        self.setparam("RA", val)
    @ra.deleter
    def ra(self):
        self.unsetparam("RA")

    @property
    def dec(self):
        """
        The declination part of the position constraint (default: None).
        """
        return self.getparam("DEC")
    @dec.setter
    def dec(self, val):
        # Do a check on the value.
        self.setparam("DEC", val)
    @dec.deleter
    def dec(self):
        self.unsetparam("DEC")

    @property
    def sr(self):
        """
        The radius of the circular (cone) search region.
        """
        return self.getparam("SR")
    @sr.setter
    def sr(self, val):
        # Do a check on the value.
        self.setparam("SR", val)
    @sr.deleter
    def sr(self):
        self.unsetparam("SR")

    @property
    def verbosity(self):
        return self.getparam("VERB")
    @verbosity.setter
    def verbosity(self, val):
        # do a check on val
        self.setparam("VERB", val)
    @verbosity.deleter
    def verbosity(self):
        self.unsetparam("VERB")

    def execute(self) :
	q = voclient.getConeQuery(self.dal_id, self.ra, self.dec, self.sr)
	ss = voclient.getQueryString(q, 2, 0)
	return SCSResults(url=ss)


class SCSResults(query.DALResults):
    """
    Results from a Cone Search query.  It provides random access to records in 
    the response.  Alternatively, it can provide results via a Cursor 
    (compliant with the Python Database API) or an iterable.
    """

    def __init__(self, votable=None, url=None, version="1.0"):
        """
        Initialize the cursor.  This constructor is not typically called 
        by directly applications; rather an instance is obtained from calling 
        a SCSQuery's execute().
        """
        query.DALResults.__init__(self, url, votable, "scs", version)

        """
    def getrecord(self, index):
        Return a Cone Search result record that follows dictionary
        semantics.  The keys of the dictionary are those returned by this
        instance's fieldNames() function: either the column IDs or name, if 
        the ID is not set.  The returned record has additional accessor 
        methods for getting at stardard Cone Search response metadata (e.g. 
        ra, dec).
        return query.DALResults.getrecord(index)
        """


class SCSRecord(query.Record):
    """
    A dictionary-like container for data in a record from the results of an
    Cone Search query, describing an available image.
    """

    def __init__(self):
        pass

    @property
    def ra(self):
        """
        Return the right ascension of the object or observation described by
        this record.
        """
        return self.get(self._names["ra"])

    @property
    def dec(self):
        """
        Return the declination of the object or observation described by
        this record.
        """
        return self.get(self._names["dec"])

    @property
    def id(self):
        """
        Return the identifying name of the object or observation described by
        this record.
        """
        return self.get(self._names["id"])

