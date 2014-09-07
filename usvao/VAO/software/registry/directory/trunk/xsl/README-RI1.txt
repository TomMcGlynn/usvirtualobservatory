The standard, version 1 Registry Interface for searching (RI1) enabled
via the XSL stylesheet, ADQLx2SQLServer_Registry_v1.0.xsl, which
translates an ADQLv1 Where clause into an SQL statement ready for the
registry database.  

This stylesheet provides some error handling.  If no errors are
detected, then the result of translation will be an SQL query of the
form, 

  select xml FROM resource WHERE pkey in 
     (SELECT DISTINCT resource.pkey FROM rr.resource ....

That is, the result of the translated returns a unique list of
VOResource records.  

If an error is detected during translation, the output will consist of
one or more lines begining with "-- ERROR: " and followed by an error
message; e.g.:

  -- ERROR: resource metadatum name not supported: capability/goober

A query will not be returned.  Thus, the code that executes this
translation should look to see if the result starts with the above
label and process the error message accordingly. 

If an unanticipated error occurs, this stylesheet will likely produce
an erroneous query that will fail when executed.  Thus, the code
should also check to be sure the query was successful.  

Examples:

The adqlx-samples subdirectory contains example ADQLx1 Where clauses
that could come in as a SOAP-encoded query.  The file
adqlx-samples/correct-translation.sql provides the correct translation
for these queries.  


