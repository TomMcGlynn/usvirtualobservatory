DALServer Package V0.5
D.Tody, R.Plante October 2012
-------------------------------

This package contains code for implementing IVOA Data Access Layer (DAL)
services.  This version supports SSAP (spectra), SIAP (images), SCS
(simple cone search), and SLAP (spectral line lists).

Full Javadoc-format documentation for the DALServer packages is given
in the "dist/docs" subdirectory.  Additional documentation is given
in the README files in each major source or runtime directory.


Installation
--------------

The DALServer package provides a Java implementation of the DAL
services.  To run the DALServer software you need to have Java 1.5 or
greater, the Ant build tool, and a standards-compliant Java servlet
container such as Apache Tomcat.  Executing the command "ant dist" will
generate a War file containing the DALServer classes and documentation
and some DALServer demonstration servlets, which one should be able
to immediately install and run within a servlet container such as
Apache Tomcat.

    % ant		# compile all packages
    % ant dist		# build the distribution files
    % ant clean		# delete generated files

After an "ant dist", load dist/ivoa-dal.war in your Java application
server to deploy the service.  By default this creates three servlets,
the echo test SSAP service "ssap", the SSAP proxy for the JHU
spectrum services, "JhuProxySsap", and a simple cone search service.
By default these are deployed at /ivoa-dal, e.g., 

    http://localhost:8080/ivoa-dal/ssap?REQUEST=queryData&POS=...

with POS and SIZE filled in, would run an echo test data query.


Demonstration/Test Servlets
---------------------------

The dalserver package includes base class implementations of all
provided services.  The base class implementations are functional and
can be executed without need to change any code.

The base SCS service includes a builtin copy of the Messier catalog
in order to provide a fully functional cone search service (the base
class can nonetheless be subclassed to implement a custom service).
The base SCS implementation also includes the ability to access a
remote catalog via JDBC, automatically providing a SCS interface.

The dalserver package also includes the echo test SSAP servlet,
which is the base class for all other SSAP servlets.  The classes
for this servlet (SsapServlet and SsapService) implement a fully
compliant SSAP service which never finds any data; queries echo back
their arguments and define all SSAP metadata, but never return any
data records.  The "servlet" class contains the servlet interface,
while the "service" class contains the actual service implementation
(which is independent of how it is called and knows nothing about
servlets, HTTP, SOAP, etc.).

The src/dataServices package contains a more realistic ready-to-use
sample SSAP service.  The JhuSsapServlet class is a functional
SSAP service which serves as a proxy for the JHU Spectrum Services.
Since the actual data service is remote, this servlet can service
realistic data queries without requiring any local data, at the cost
of having to transfer the data twice (although in pass-through mode
it will stream the data through).  It is intended to be used only
for software development purposes.


Build Your Own Service
------------------------

To build your own service, you need to add a new "*Service.java"
class to the dataServices package, or some new package of your choice.
This contains implementations of the major service operations, namely
queryData and getData.  You also need to subclass the "Servlet" class
(as in the dataServices examples), however this is just to override
the "newSsapService" method get it to call your new custom service.
In most cases, all that is needed is a subclass of the "Service" class.

What your queryData operation will most likely do is the following:

    1)  The input is an instance of SsapParamSet, containing the processed
	request parameters.

    2)  The getData method takes these input parameters, forms a query
    	to the local archive database, queries the archive, and gets
	back a bunch of archive and collection-specific metadata.

    3)  This locally defined metadata is then used in a bunch of "setValue"
    	calls to generate the metadata for each row of the query response.

    4)	Output is an instance of RequestResponse.  This is serialized and
    	returned to the client by the servlet code.  Normally it should
	not be necessary to do anything with the servlet code.

Implementing the getData operation is similar, but in this case one builds
a Spectrum object instead of a SSAP query response object.  Since the
data models are essentially the same, much the same code can be used for
both, except that in the case of a Spectrum object, most metadata values
will be PARAMs instead of FIELDs.

It is also possible to pass-through native data, either in a streaming
mode, or by returning static files.  The dalserver classes include
built-in support for passing back static files (see getData in
SsapServlet and SsapService).


DALServer Package
-------------------

The bulk of the functionality of the DALServer package is in the
"dalserver" Java package.  The dalserver package provides two
main externally-callable classes for use in writing DAL services:
RequestParams, and RequestResponse.


RequestParams
-------------

RequestParams is used to obtain the request parameters:

    RequestParams (HttpServletRequest request, ParamSet params)

For example, for an SSAP request, one would call this with

    SsapRequestParams params = new SsapParamSet();
    RequestParams (request, params);

and the fully processed request parameter set would be returned
in <params>.  One would then use simple getParam(<pname>) operations
to access individual parameters.

Normally this is handled by the "servlet" class, transparently to the
service code, which sees only a fully processed service-specific parameter
set, e.g, a SsapParamSet instance.


RequestResponse
---------------

RequestResponse is the main class used to build a response to a
service request.  It can be used both for the queryData operation,
to implement discovery queries, and for the getData operation, to
dynamically build the actual datasets to be returned.  All these
operations build a data object in memory based on a data model.
For the getData operation, the dataset thus built can subsequently
be serialized in any supported data format for return to the client.
Currently CSV and VOTable are the supported formats for dynamic data
serialization.  Future support for at least FITS is planned as well.

Given the parsed and verified input parameters to the service,
one would then query the local archive database, and compute the
metadata to be returned in the SSAP query response.  To return this
metadata to the client application as a VOTable one would use the
RequestResponse class.  For example:

    SsapKeywordFactory ssap = new SsapKeywordFactory();
    RequestResponse r = new RequestResponse();
    String id, key;

    // Set global metadata.
    r.setDescription("Builtin test for the RequestResponse class");
    r.setType("testResults");
    
    // Set some sample INFOs.
    r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
    r.addInfo(key="POS", new TableInfo(key, "180.0,1.0"));

    // TARGET component data model.
    r.addGroup(ssap.newGroup("Target"));
    r.addField(ssap.newField("TargetName"));
    r.addField(ssap.newField("TargetClass"));
    r.addField(ssap.newField("Redshift"));

    // DATAID component data model.
    r.addGroup(ssap.newGroup("DataID"));
    r.addField(ssap.newField("Title"));
    r.addParam(ssap.newParam("Creator", "Sloan Sky Survey"));
    r.addParam(ssap.newParam("Collection", "ivo://jhu/sdss/dr5");
    r.addField(ssap.newField("CreatorDID"));

    // Set the table data.
    r.addRow();
    r.setValue("TargetName", "target1");
    r.setValue("TargetClass", "target1-class");
    r.setValue("Redshift", "2.3");

    // Write the RequestResponse as a VOTable.
    OutputStream out = new FileOutputStream("_output.vot");
    r.writeVOTable(out);
    out.close();

See lib/ssap-table.txt for a full template for creating all the
SSAP keywords.  spectrum-table.txt provides the same for Spectrum
datasets.  The ssap echo test and JhuProxySsap examples provide a
complete realistic example of an actual working services.
