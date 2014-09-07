This project directory contains several projects for running a VO Resource registry like the VAO registry at the Space Telescope Science Instistute. 

Requirements:
* Visual Studio 2010 or later or any IDE capable of reading .csjproj and .sln files, with a C# compiler.
* A Windows Server 2008 or newer or Windows 7 or newer machine, with the following software running:
	* Microsoft's IIS web server, version 7 or later.
	* Microsoft SQL Server, 2008 or later.
	* .NET 4.0 or later.

Projects:
* dbsetup: This is a series of scripts for setting up an empty database for holding registry resources, and not a standard 'project' per se.
* directory: This is the main registry project, which builds into registry.dll and several .aspx and .asmx web pages.
* operationsmanagement: This is the main dev/ops project for resource curation tools. It builds into operationsmanagement.dll and several .aspx and .asmx web pages. 
* harversterservice: This project is optional for a registry installation. It includes a standalone program that can be run as scheduled task or windows service or hand-run executable, for copying the contents of other resource registries into your local registry.
* publishing: This is a standalone project for users to publish their own resources. It has its own page in the VAO closeout documentation; if you wish to support user-published resources please refer to that page as it will not be covered here.

The main project file to use without publishing included is Directory/DirectoryStandalone.sln.



Setup Instructions:

First download the trunk of the dbsetup, directory, operationsmanagement, and harvester service projects under a single project directory (vao/directory/dbsetup, vao/directory/directory, etc). 

One time only, create your database for your first build and deployment, create an empty database in Microsoft SQL Server, and create two users for it, one with read/write privileges and one with only read. The write user will be used by harvesting and publishing services for data ingest; the read-only user will be used by outward-facing web functionality for searches only. Edit the dbsetup/VOResourceCreateBaseTables.sql file to USE the name of your database. If you wish to use a schema other than dbo, search/replace that. As a user with permission to create tables, indices, and stored procedures, run the script. Then edit and run VOResourceCreateUsers.sql to create the users and give them access to the relevant stored procedures for resource tracking.

If you wish to run the harvester service and update your registry from an archived list of the IVOA Registry of Registries publishers at http://rofr.ivoa.net/cgi-bin/listPublishers.py, edit and run the dbsetup/VOResourceCreate_harvestertables.sql file. If you wish to harvest from some other list, run only the table creation sections and populate the data by hand using the existing script as a reference. If you wish to only publish your own resources using the publishing tool, skip this step.


Open the directory/DirectoryStandalone.sln file with your IDE. The projects HarvesterService, OperationsManagement, and registry should load. 

One time only, edit the <appSettings> sections of each project's *.config files, to match your local configuration. From the main web.config, this goes as follows. All other configuration files need only subsets of this data to be adjusted:

       <add key="dbAdmin" value="[a value chosen by you for dev/ops admin]" />
        <add key="log_location" value="[a directory IUSR has write access to for logging beyond IIS standard. If this is blank or unwritable the project will still run]" />
<!--default. uses internal cache, 60x speed improvement on standard search-->
        <add key="useCache" value="True" /> 
<!--default. standard harvesting interface provides 100 records at a time-->
        <add key="resumptionTokenLimit" value="100" />
        <add key="registryIdentity" value="[the ivo identifier of your registry itself. See http://rofr.ivoa.net/]"/>
        <add key="registryName" value="[the name of your registry]"/>
        <add key="registryEmail" value="[contact email address]"/>
        <add key="SqlConnection" value="Initial Catalog=[database name]; Data Source=[server name]; User Id=[read only user]; Password=[their pwd]" />
        <add key="SqlAdminConnection"  value="Initial Catalog=[database name]; Data Source=[server name]; User Id=[WRITE user]; Password=[their pwd]"  />


One time only, set up IIS to host a virtual application pointing at directory/directory, using an application pool running .NET 4.0. MSDN documentation can help you through this process. You can also choose to set up post-build steps to copy your built files elsewhere, or autodeploy with Visual Studio/IIS integration, which is not set up in this project by default.



Build instructions:

From this point, the build should be repeatable: in your IDE, rebuild all from the main .sln file. Your project directory's index.aspx should be a search page where entering a keyword in the main form and hitting 'search' will try to access your resource database and return a table of results. Directory/harvesterservice/bin/Debug should contain the executable for the harvester program and the .dlls on which it depends.

For there to be any results to search, obviously there need to be records in your registry database. This requires running the harvester or setting up the user publishing standalone project.


