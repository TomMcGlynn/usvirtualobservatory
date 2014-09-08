Introduction to the VAO Python API
=======================================

The VAO Python API provides a comprehensive package for access to the
Virtual Observatory.  Capabilities are provided to rapidly discover and
access data, and manipulate the resulting tables, images, spectra, and
more.  The Python routines can be used interactively from the standard
Python interpreter or can be included in scripts.

Two implementations of the VAO Python API are provided: ``voclient`` and
``vaopy``.  Both implementations support the common 
*Core DAL API*, a common API defintion for low-level access to the
VAO Registry and to core Data Access Layer (DAL) services--namely, 
Simple Cone Search (SCS), Simple Image Access (SIA), Simple Spectral
Access (SSA), and Simple Line Access (SLA or SLAP).  The ``vaopy``
package is written in pure Python and only supports this interface.
The ``voclient`` package is C-based and goes beyond this core to
support additional high-level capabilities for multiprocess tasking
and messaging.  In particular, it provides some high-level,
ready-to-use VO tasks built on top of the core API.   

As a native python implementation, ``vaopy`` is built on the 
`AstroPy <http://www.astropy.org>`_ module which provides support 
for `VOTables <http://docs.astropy.org/en/v0.1/io/vo/index.html>`_ 
and accessing columns as `NumPy <http://www.numpy.org>`_ arrays.  In
the ``voclient`` implementation, the dependency on AstroPy and NumPy
are optional; they will be supported if the modules are installed;
otherwise, the rest of the API works without them.  `VOTable
<http://www.ivoa.net/Documents/VOTable/20091130/>`_ is a table data
model and format used internally by VO services to pass table data back
to a client.  Typical uses of the VO Python API do not require direct
access to the VOTable object used internally to pass back the results
of queries, but it is available to applications that require more
detailed information that is provided by the VAO Python API.


Basic Usage
----------------------------

To use the VAO Python API first obtain and install either of these
packages, then import it into Python::

   import [vaopy|voclient] as vo

Typical usage will involve first searching the VO *resource registry*
for data collections or VO services of interest, accessing individual
services to retrieve data (often filtered or subsetted), and then
performing some local analysis of the data.  The registry search may be
skipped if one already knows what VO services are to be accessed.  An
alternative to accessing the VO registry and VO data services directly
is to use the higher level tasks provided in the full version of the
package, which provide tools for common data discovery and data
retrieval use cases (see :ref:`advanced-topics`).  In cases where an
existing task is available a page or more of Python code may be reduced
to a single function call, often with enhanced functionality, e.g., the
ability to search multiple resources simultaneously and aggregate the
results.

**Example 1**: Find X-ray images of NGC 3393.  We first search the VO
registry to find image services which have coverage in the X-ray
waveband::

    services = vo.regsearch(servicetype="sia", waveband='xray')

Next, for each image service found we query the service to find images
of NGC 3393, i.e., images with coverage at the position of NGC 3393::

    # Lookup object position.
    obj = vo.lookup('NGC 3393')     # Lookup position via name resolver
    pos = (obj.ra, obj.dec)         # Get object position as (ra,dec)

    # Search each image service found.
    for service in services:
        print "searching '%s' at url %s" %
            (service.title, service.accessurl)
        results = vo.imagesearch(service.accessurl, pos)

        # Print info on each image found.
        for image in results:
            print "ra=%f, dec=%f, title='%s' url=%s" %
                (image.ra, image.dec, image.title, image.acref)

**Example 2**: Find image previews from an archive for a region of the
sky.  In this example we already know the URL of the image service to be
accessed::

    results = vo.imagesearch(url, pos=(121.134, -3.1352), size=0.5, format='graphic')
    for image in results:
        # Download image and write to a file with a default filename.
        image.cachedataset()

In both of these examples, note that a simple query can be performed in
a single function call.  Standard properties of objects such as services
or datasets (e.g., images) are accessible as Python attributes of the
result record.

**Example 3**: Search the VO registry for a particular kind of service::

    # Perform the VO registry search.
    results = vo.regsearch(keywords='galaxy', servicetype="sia", waveband='xray')
    print "Found %d services" % results.size

    # Find a particular archive.
    for service in results:
        # Search for images from the Chandra archive.
        if "Chandra" in service.publisher:
            images = service.search(pos=(121.134, -3.1352), size=0.5)

Note that searches of the VO registry behave much like searches for data
such as images.  Once a service is discovered via a registry search it
can be queried directly.  In this example we search only for images from
the Chandra archive.

**Example 4**: Search for images over a list of positions::

    service = vo.sia.SIAService(url)
    query = service.createQuery(size=0.05, format='graphic')
    for pos in mypositions:
        query.pos = pos
        results=query.execute()
        for image in results:
            image.cachedataset()

In this case the URL of the service to be queried has already been
determined.  We create a query object so that we can repeat the query a
number of times for a list of positions, without having to set all the
query parameters each time.  Only query.pos is different for each query.
At each position, the query is executed and all images found are
downloaded with default file names.

**Example 5**: Query the GALEX catalog for sources within 30 arcmin of
NGC 3393 and plot the near UV flux for each source as a function of object
number::

    results = vo.conesearch(url, pos=(162.096, -25.162), size=0.5)
    plot(xrange(results.rowcount), results.getcolumn('nuv_flux'))

**Example 6**: Integration with AstroPy::

    service = vo.sia.SIAService(url)
    query = service.createQuery(pos=(121.134, -3.1352), size=0.05)
    votable = query.execute_votable()

In this case the query is constructed and executed, but instead of
returning a standard result set the query response is returned as an
AstroPy votable object.

**Example 7**: Integration with the Python Database API (DB-API V2).

The Python Database API V2 (`PEP-0249
<http://www.python.org/dev/peps/pep-0249/>`_) defines a standard API to
query a database and iterate through the results.  Since VO queries are
in actuality queries to remote databases at archives, this API allows
generic query code to be used to process the results of a query::

    # Perform an image search and obtain a Cursor to navigate the response.
    results = vo.imagesearch(url, pos=(121.134, -3.1352), size=0.5)
    cursor = results.cursor()

    # Fetch and print each row of the query response.
    for i in xrange(cursor.rowcount):
        row = cursor.fetchone()
        print row

The Python Database API provides a Cursor attribute ``.description``
which provides generic, standard column metadata to define the contents
of each row of the query response.  In addition the columns in a Cursor
row are identical to that in the query results object returned in the
initial query, and this object may be queried to obtain expanded VO
metadata describing the result set.


Multilevel Interface
----------------------------

The VAO Python API is multilevel, recognizing the need to support a
range of users, from end users who want a command-level interface and do
not wish to write any code, to application developers who develop their
own custom applications.  At the highest level we have the ready made VO
command-level tasks, provided as part of the C-based ``voclient``
implementation.  These can be used interactively or programatically
within a Python script.  At the API level each module provides a single
query method which can form and execute a query in a single operation.
Then we have the full API for a module, providing maximum flexibility
for application development.  Lastly, more advanced applications can go
one level deeper and process the query response VOTable directly as a
VOTable object or file, allowing external software to be used.  Which
level is best depends upon the application and the user, but entry can
be made any whatever level is most appropriate.

In what follows we will look at each module of the API in more detail,
providing an overview of the module interface.  The API to each module
is summarized here in an abbreviated, somewhat abstracted form which
makes it easy to see the entire module API at once.  The Python
reference documentation for each API provides full detail on each
interface.

Resource Discovery
----------------------------

A VO registry is a database of VO resources -- data collections and
services -- that are available for VO applications.  Typically, it is
aware of VO resources from all over the world.  A registry can find
relevent data collections and services through search queries that are
typically subject-based.  The registry responds with a list of records
describing matching resources.  With a record in hand, the application
can use the information in the record to access the resource directly.
Most often, the resource is a data service that can be queried for
individual datasets of interest.

This module provides basic, low-level access to the VAO Registry at
STScI using (proprietary) VOTable-based services.  In most cases, the
Registry task, with its higher-level features, e.g., result caching and
resource aliases, can be a more convenient interface.  The more basic
interface provided here allows developers to code their own interaction
models.

The simplest registry query can be performed all in one step with the
global function *regsearch*::

        results = regsearch (keywords=None, servicetype=None, waveband=None, sqlpred=None)

The parameters, which are common to all registry queries, are as follows:

    ============   ======================================================
    Parameter      Description
    ============   ======================================================
    keywords       A string giving a single term, or a Python list of terms to match to registry records.  
    ------------   ------------------------------------------------------
    servicetype    The service type to restrict results to; allowed values include 'catalog' (synonyms: 'scs', 'conesearch'), 'image' (synonym: 'sia'), 'spectrum' (synonym: 'ssa'), 'service' (a generic service), 'table' (synonyms: 'tap', 'database').
    ------------   ------------------------------------------------------
    waveband       The name of a desired waveband; resources returned will be restricted to those that indicate as having data in that waveband.
    ------------   ------------------------------------------------------
    sqlpred        An SQL WHERE predicate (without the leading "WHERE") that further constrains the search against supported keywords.
    ============   ======================================================

The query results in a table (instance of class *RegistryResults* which is defined
below) wherein each row describes a resource matching the query.

To pose more complex registry queries one must first create a
*RegistryService* instance to manage queries against a registry::

          class RegistryService (DALService)
                       __init__ (baseurl=None, resmeta=None, version="1.0")

               record = resolve (ivoid)
               results = search (keywords=None, servicetype=None, waveband=None, sqlpred=None)
           query = create_query (keywords=None, servicetype=None, waveband=None, sqlpred=None)

The RegistryService class provides a method *resolve* which may be used
to resolve IVO identifiers into URLs.  An IVO identifier is a fixed URI,
used to identify some VO resource, which must be resolved to an actual
Web URL to be able to access the resource.  This provides some level of
insulation against changes to the physical URLs used to access services
or other resources.

Once a RegistryService instance is created, more complex queries can be
built up in steps, adding or removing query constraints, then finally
executing the query.  This is useful for example when the same query is
repeated a number of times with minor modification in each instance.  By
default RegistryService will query the VO registry at STScI, but other
registries can be queried by explicitly setting the service *baseurl*.
A query object is then created with *create_query*, after which it may
be edited, and finally executed with *search*.

The query object is implemented by the *RegistryQuery* class, used to
compose a query against a registry::

            class RegistryQuery (DALQuery)
                       __init__ (orKeywords=True, baseurl=None, version="1.0")

             type = servicetype ()              # Type of service (SCS, SIA, etc.)
                band = waveband ()              # Restrict to only given waveband

                list = keywords ()              # Find resources with the given keywords
                    addkeywords (keywords)      # Add one or more keywords
                 removekeywords (keywords)      # Remove one or more keywords
                  clearkeywords ()              # Clear all keywords
                    or_keywords (ored)          # Set whether keywords are OR or AND
        bool = will_or_keywords ()              # Test keyword AND/OR setting

              list = predicates ()              # List of SQL predicates defined
                   addpredicate (pred)          # Add a SQL predicate
                removepredicate (pred)          # Remove predicate
                clearpredicates ()              # Clear all predicates

To compose a registry query the service type or waveband may be
constrained.  Any number of keywords may be searched, using either an
AND or OR relationship (*and*: all must be found, *or*: at least one
must be found).  SQL predicates allow more complex constraints to be
applied by passing through explicit SQL WHERE subexpressions.

The results of a registry query are returned as a table, an instance of
the class *RegistryResults*.  RegistryResults is a subclass of the
generic class *DALResults* (covered below), inheriting all the
attributes and methods of that class, e.g., *fieldnames*, *rowcount*,
*Cursor*, and so forth; both registry queries and DAL queries share the
same generic capabilities for query response processing.
RegistryResults is iterable, with each iteration returning a single row
or record.  Each row is a *SimpleResource* describing a single resource
matching the query::

          class RegistryResults (DALResults)
                       __init__ (url=None, version="1.0")

           class SimpleResource (Record)
                # Properties
                title           # The title of the resource
                shortname       # The resource's short name
                tags            # User-friendly lable for the resource
                ivoid           # The IVOA identifier for the resource
                publisher       # Name of organization providing the resource
                waveband        # List of names of wavebands with coverage
                subject         # List of subject keywords describing this resource
                type            # List of resource type that characterize this resource
                contentlevel    # List of contentlevel labels for this resource
                capability      # Name of the IVOA service capability
                standardid      # IVOA identifier of the standard implemented by capability
                accessurl       # When the resource is a service, the service's accessURL

The high level properties of each resource are provided as the
attributes of SimpleResource, as noted above.  More detailed
descriptions of the resource attributes are provided in the IVOA
standards.  The URL used to access the resource described, e.g., some VO
data access service, is given by *accessurl*.


Data Discovery and Access
----------------------------

.. note::

    These sections provide an overview of the DAL query APIs.

In the VO each major class of astronomical data has its own VO Data
Access Layer (DAL) interface for data discovery and access.  These
protocols are all similar, sharing a largely common query interface,
dataset metadata and query response.  Queries are parameter based and
usually return a tabular response formatted as a VOTable.  When querying
for datasets such as images and spectra, each row of the output table
describes a single data product which can be retrieved.  When querying a
table such as an astronomical catalog the requested portion of the
remote table is returned as the query response.

In VOClient each class of data has a separate method to query a remote
data service for available data.  All services share a common interface
to access the output query response (QR) table, with the initial query
delivering an open connection used to navigate the query response.  In
the next sections we describe the query interface for each class of
data, followed by a description of the shared interface used to process
the query response.

.. note::

    Exceptions and warnings are omitted here for now, but will need to
    be added to the final version of the API.

Cone Search (SCS)
++++++++++++++++++++++++++++++++++

The VO `Simple Cone Search (SCS)
<http://www.ivoa.net/Documents/latest/ConeSearch.html>`_ protocol is
used to query a remote table (typically an astronomical catalog),
returning all table rows within some circular region or cone on the sky.
A successful query returns a connection object which may be used to
navigate the query response table (see :ref:`query-processing`)::

    conn = vo.scs(url, ra=10.0, dec=1.0, sr=0.2)

The cone search protocol defines the following query parameters:

    =========   =============
    Parameter   Description
    =========   =============
    URL         The baseURL of the cone search service.
    ---------   -------------
    RA          Right-ascension in the ICRS coordinate system, specifing the center of the region to be searched, given in decimal degrees. [#]_
    ---------   -------------
    DEC         Declination of the search region center in the ICRS coordinate system, specified in decimal degrees.
    ---------   -------------
    SR          The radius of the search region, given in decimal degrees.
    ---------   -------------
    VERB        The "verbosity" of the output table given an a integer with one of the values 1, 2, or 3.  The higher the number the more table columns returned.  The default value of 3 returns all table columns. (optional)
    =========   =============

A special case is a query with a search radius of zero, which will
return only table metadata but no data records.  This is useful to
determine the table columns without having to query for data.

Additional, service-specific parameters may be added to access any
non-standard features provided by the particular service being queried.
For example a service might allow output to be returned in formats other
than the default VOTable, or the service might allow the table to be
accessed to be specified by name (normally there is one SCS service
instance per remote table).

The following aliases are defined to identify standard fields of the output table:

    =========    ================     ========================
    Alias        UCD                  Description
    =========    ================     ========================
    id           ID_MAIN              Unique record ID.
    ---------    ----------------     ------------------------
    ra           POS_EQ_RA_MAIN       Right-ascension of the source in the ICRS coordinate system.
    ---------    ----------------     ------------------------
    dec          POS_EQ_DEC_MAIN      Declination of the source in the ICRS coordinate system.
    =========    ================     ========================

More detailed information on the cone search protocol can be found in
the `cone search specification
<http://www.ivoa.net/Documents/latest/ConeSearch.html>`_.

Image Access (SIA)
++++++++++++++++++++++++++++++++++

The VO `Simple Image Access (SIA) <http://www.ivoa.net/Documents/SIA>`_
protocol is used to query a (normally) remote service for images
matching the specified query parameters.  A successful query returns a
connection object which may be used to navigate the query response table
(see :ref:`query-processing`)::

    conn = vo.sia(url, pos=(10.0,1.0), size=0.2, format='all')

A full list of all the SIA parameters is given in `api-sia`.  Some of
the most commonly used query parameters are the following:

    =========   =============
    Parameter   Description
    =========   =============
    URL         The baseURL of the service.
    ---------   -------------
    POS         A tuple giving the right-ascension and declination of the center of the search region in the ICRS coordinate system, specified in decimal degrees (SIA uses a POS parameter to allow more complex coordinate references to be input but we won't go into that here).  Separate RA and DEC parameters may optionally be used in place of POS.  POS may optionally be input as a string formatted as defined by the SIA protocol.
    ---------   -------------
    SIZE        The coordinate angular size of the region given in decimal degrees. The region may be specified using either one or two values. If only one value is given it applies to both coordinate axes. If two values are given the first value is the angular width in degrees of the right-ascension axis of the region, and the second value is the angular width in degrees of the declination axis.
    ---------   -------------
    FORMAT      A value or comma delimited list of values indicating the image formats the client is interested in, e.g., "fits" or "fits,graphic".  If no format is specified or FORMAT is "all" then all available images are referenced regardless of format.
    ---------   -------------
    ...         ...
    =========   =============

The SIA query response is a table describing all the available images
matching the given query, one per row.  Each record includes an access
reference URL (*acref*) which may be used to retrieve the image.  The
referenced image may be *virtual*, meaning that it will be generated on
the fly if accessed.  An SIA cutout service for example may be able to
return an image cutout or reprojection exactly covering the rectangular
region specifed by POS,SIZE.  An "archival" SIA service will find whole
images which contain or overlap with the specified region.

The following aliases are defined to identify standard fields of the
SIA query response:

    =========    ===========================     ========================
    Alias        UCD                             Description
    =========    ===========================     ========================
    title        VOX:Image_Title                 A short (one line) description of the image.
    ---------    ---------------------------     ------------------------
    instr        INST_ID                         The instrument or instruments.
    ---------    ---------------------------     ------------------------
    dateobs      VOX:Image_MJDateObs             MJD of observation mid-point.
    ---------    ---------------------------     ------------------------
    ra           POS_EQ_RA_MAIN                  RA of image center.
    ---------    ---------------------------     ------------------------
    dec          POS_EQ_DEC_MAIN                 DEC of image center.
    ---------    ---------------------------     ------------------------
    naxes        VOX:Image_Naxes                 Number of image axes.
    ---------    ---------------------------     ------------------------
    naxis        VOX:Image_Naxis                 A tuple giving the length of each axis.
    ---------    ---------------------------     ------------------------
    acref        VOX:Image_AccessReference       URL used to retrieve the image.
    ---------    ---------------------------     ------------------------
      ...          ...                             ...
    =========    ===========================     ========================


.. note::

    VOClient defines aliases for standard VO metadata both for
    convenience, to make access simpler, and to enhance information
    hiding.  SIA1 for example uses "vox:" UCDs to uniquely identify
    fields of the query response, whereas SIA2 and other second general
    DAL interfaces such as SSA use Utypes for this purpose.  Utypes can
    also change between versions of an interface.  The use of an alias
    combined with interface versioning makes it possible to shield a
    client application from such details while still rigorously defining
    interfaces at the level of a versioned protocol.  Aliases also
    provide a shorthand reference for Utypes, some of which can be quite
    long.  While aliases are not part of current VO standards there is
    no issue with using them within a client interface, which is an
    implementation and not a standard; the client has direct access to
    the underlying VOTable if finer control is required.



Spectral Access (SSA)
++++++++++++++++++++++++++++++++++

.. note::

    To be added; does not affect the interface design significantly
    since the interface pattern is the same.  On the issue of aliases,
    note below what we have for SSA aliases, compared to those for SIA1.

Sample data model (Utype) aliases for SSA:

    =========    ==========================================     ========================
    Alias        Utype                                          Description
    =========    ==========================================     ========================
    title        DataID.Title                                   A short (one line) description of the image.
    ---------    ------------------------------------------     ------------------------
    instr        DataID.Instrument                              The instrument or instruments.
    ---------    ------------------------------------------     ------------------------
    dateobs      Char.TimeAxis.Coverage.Location.Value          MJD of observation mid-point.
    ---------    ------------------------------------------     ------------------------
    ra           Char.SpatialAxis.Coverage.Location.Value       RA of image center.
    ---------    ------------------------------------------     ------------------------
    dec          Char.SpatialAxis.Coverage.Location.Value       DEC of image center.
    ---------    ------------------------------------------     ------------------------
    acref        Access.Reference                               URL used to retrieve the image.
    ---------    ------------------------------------------     ------------------------
      ...          ...                                             ...
    =========    ==========================================     ========================


Spectral Line List Access (SLAP)
++++++++++++++++++++++++++++++++++

[To be added].  Summarize the SLAP interface and capabilities, and mention
any prominent standard line list services such as Splatalog.


.. _query-processing:

VO Query Response Processing
--------------------------------

.. note::

    Provide an overview of the DALQuery API as used for both
    registry and DAL queries.

Processing via a Python Iterator
++++++++++++++++++++++++++++++++++

.. note::

    Provide a general explanation for interating through and
    processing result records.

*Editing under way: see examples above under "Basic Usage"* 


Column-based Data Access
++++++++++++++++++++++++++++++++++

.. note::

   Describe how to access a whole column of data as an array.  

*Editing under way: see dalquery.DALResults.getcolumn()* 


Python Database API Cursor Support 
++++++++++++++++++++++++++++++++++

The Core DAL API also allows users to scan query results using the 
Python Database API V2.0
(`pep-0249 <http://www.python.org/dev/peps/pep-0249/>`_).  A
DB-API-compliant ``Cursor`` instance can be obtained from the DAL
resutls object via its ``cursor()`` function::

    # Perform the query
    conn = vo.scs("http://some.survey.com", ra = 123.456, dec = 12.34, sr = 0.1)
    cursor = conn.cursor()

    # find the needed columns
    colnames = map(lambda c: c[0], cur.description)
    colidx = { "id":  colnames.index("id"),
               "ra":  colnames.index("ra"),
               "dec": colnames.index("dec") }

    # get a bunch of rows
    recs = cursor.fetchmany(100)
    for record in recs:
        print "%s: %d %d" % (rec[colidx["id"]], rec[colidx["ra"]], rec[colidx["dec"]])

    # rewind and start over
    cur.scroll(0, 'absolute')

    # iterate with the cursor one at a time
    for rec in cursor:
        print "%s: %d %d" % (rec[colidx["id"]], rec[colidx["ra"]], rec[colidx["dec"]])


VOTable Manipulation
--------------------------------

.. note::

    Summarize the facilities available for dealing with VOTable
    data, with emphasis on the multi-level architecture (OO, above
    votable, native votable APIs, direct access to votable file).

In many cases a client application never needs to deal directly with
VOTable as it is possible to navigate the query response and directly
access the fields of a table or the data model contained therein, via
the higher level DBI interface and ``getfield``.  When direct access to
a VOTable is required either the Astropy ``vo.table`` class or the
native VOClient votable class may be used (in addition ``AtPy`` provides
a higher level table abstraction, not specific to VOTable).
``vo.table`` is especially well integrated with NumPy, providing direct
support for the NumPy binary datatypes as well as NumPy record arrays,
hence might be well suited for accessing datasets formatted as VOTables.

The registry and data queries all provide an option to
directly access the VOTable which is normally processed internally.
VOTables may also be retrieved in other ways, e.g., a Spectrum dataset
may be retrieved by SSA as a VOTable.

.. _advanced-topics:

Advanced Topics
--------------------------------

.. note::

    Tasking, the VAO package, and SAMP are moved out into separate
    chapters.  These are advanced capabilities not part of the core
    DAL API.


.. _vo-standards:

VO Standards Reference
--------------------------------

List the relevant VO standards here.

.. rubric:: Footnotes

.. [#]  AstroPy contains a "vo" module which may conflict with the usage
        shown here, but let's leave it this way until we get this resolved.

.. [#]  NumPy may end up being required for the VAO tasks, but is not (yet anyway)
        required for the registry or DAL queries.

.. [#]  A useful feature might be to allow more flexibility in specifying positions;
        we can do that here, since this is a user interface even though things are
        fixed in the lower level protocol.

.. |more| image:: _images/more.png
          :align: middle
          :alt: more info

