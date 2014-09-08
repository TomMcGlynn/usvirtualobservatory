
-------------------------------------------------------------------------
-------------------------------------------------------------------------

				AAAREADME.txt  

Readme file miscellaneous notes for development and implementation of 
VORegistry .NET portal and including web services, VOResource, OAI, SQL Server

-------------------------------------------------------------------------

CONTENTS

1.  Replication Service
2.  web config
3.  DB nvo login and user
4.  OAI repositories to harvest
5.  xsd.exe for oai_dc
6.  cleaning up DB resources, deleting relations first

-------------------------------------------------------------------------
-------------------------------------------------------------------------

* For Installing Replicate System Service

On .NET server:

C:\Inetpub\nvo\VORegistry\Replicate\bin\Debug>installutil replicate.exe

Then set the managed service to run automatic

-------------------------------------------------------------------------

* Note web.config has customized settings for each Registry database project and
location.  NOT IN CVS!


- Style sheet location
<appSettings>
	<!-- SQL Connection Parameters -->
	<add key="SqlConnection.String" value="Initial Catalog=Voregistry10; Data Source=chart.stsci.edu; User Id=nvo; Password=nvo" />
        <add key="cstring" value="Data Source=chart.stsci.edu; User Id=nvo; Password=nvo"/>
        <add key="node_id" value="NVORegistry"/>
        <add key="primary_table" value="resource"/>
        <add key="primary_table_key" value="dbid"/>
        <add key="sigma" value="0.1"/>
        <add key="log_location" value="c:\temp"/>
        <add key="regionXsl" value="c:\Inetpub\nvo\VORegistry\region.xsl"/>

</appSettings>

-------------------------------------------------------------------------

* For DB nvo login

Note when reattaching the VOREgistry database,  attach owner as something other than
nvo, such as sa,  then run this sp.

	exec sp_change_users_login 'Update_One', 'nvo','nvo'

(this is to reattach orphaned users to their logins) Update_One is case sensitive

* Inverted Index for Keyword Search:

One thing you need to do is to set up an 'inverted index' for the registry. In SQL Server you do this...

exec sp_fulltext_database 'enable'

Then create Full-text catalog in the VORegistry database (you will see this feature in the DB tree).  You then need to run the 'populate' task (right click on the catalog once it's created).  I then make it scheduled to run every hour because when a new resource is published,  it will be visible for keyword search immediately. I am thinking about options for changing this frequency.

*** NOTE: If Database is detached and reattached you will need to rebuild the full-text catalog.  
You do not need to recreate, you can use existing one and rebuild it,  then setup schedule again.

--------------------------------------------------------------------------

* OAI Harvestable Repositories

http://hydra.star.le.ac.uk:8080/astrogrid-registry/OAIHandlerv0_10?

http://nvo.ncsa.uiuc.edu/cgi-bin/nvo/oai.pl?

http://heasarc.gsfc.nasa.gov/cgi-bin/OAI-XMLFile-2.1/XMLFile/nvo/oai.pl?

http://mercury.cacr.caltech.edu/cgi-bin/OAI-XML/carnivore/oai.pl?

http://vizier.u-strasbg.fr/cgi-bin/registry/vizier/oai_v0.10.pl?verb=ListRecords&metadataPrefix=ivo_vor

--------------------------------------------------------------------------

Running xsd.exe to generate C# classes for oai_dc

downloaded and saved the xsd for oai_dc and simpledc

within .NET tools command prompt window (not supported from default OS command prompt)

J:\Inetpub\nvodev\VOREGI~1>xsd.exe /classes oai_dc.xsd simpledc20021212.xsd
Microsoft (R) Xml Schemas/DataTypes support utility
[Microsoft (R) .NET Framework, Version 1.1.4322.573]
Copyright (C) Microsoft Corporation 1998-2002. All rights reserved.

Writing file 'J:\Inetpub\nvodev\VOREGI~1\oai_dc_simpledc20021212.cs'.

---------------------------------------------------------------------------

Cleaning Up Resources (removing relations first)

delete from resource where harvestedfrom like '%heasarc%'

delete from resourcerelations
where primaryresourcedbid in
(select dbid from resource where harvestedfrom like '%heasarc%')

