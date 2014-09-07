This project directory contains several projects for running a VO Resource registry like the VAO registry at the Space Telescope Science Instistute. 

Requirements:
* Visual Studio 2010 or later or any IDE capable of reading .csjproj and .sln files, with a C# compiler.
* A Windows Server 2008 or newer or Windows 7 or newer machine, with the following software running:
	* Microsoft's IIS web server, version 7 or later.
	* Microsoft SQL Server, 2008 or later.
	* .NET 4.0 or later.

Projects:
* publishing: This is the main project for users to publish their own resources. 

It depends on the following projects under the VAO Registry.

* dbsetup: This is a series of scripts for setting up an empty database for holding registry resources, and not a standard 'project' per se.
* directory: This is the main registry project, which builds into registry.dll and several .aspx and .asmx web pages.
* operationsmanagement: This is the main dev/ops project for resource curation tools. It builds into operationsmanagement.dll and several .aspx and .asmx web pages. 


The main project file to use for directory setup with publishing is Directory/DirectoryPublishing.sln. Since it depends on the Registry project already installed, one can also follow all the instructions for that project and add the directory/publishing/publishing.csproj file to the main project.



Setup Instructions:

This project is wholly dependent on the VAO Registry Infrastruture project. Download and set it up according to its own instructions.

Once the infrastructure is up and running, you can add the publishing project.

Download the trunk of the directory/publishing project into a directory called 'publishing' under 'directory' as you've done with the other subprojects.

In order to add user-specific information to your registry database, edit (to match your database/schema name) and run the dbsetup/VOResourceCreate_usertables.sql file. 



Open the directory/DirectoryPublishing.sln file with your IDE. The directory, operationsmanagement, and publishing projects should load.

One time only, edit the <appSettings> sections publishing's *.config files, to match your local configuration, as you've done for the other projects. The database fields should be the same:

        <add key="SqlConnection" value="Initial Catalog=[database name]; Data Source=[server name]; User Id=[read only user]; Password=[their pwd]" />
        <add key="SqlAdminConnection"  value="Initial Catalog=[database name]; Data Source=[server name]; User Id=[WRITE user]; Password=[their pwd]"  />


The following settings exist to connect your publishing interface to a VAO login endpoint for user authentication. Other VAO login mirrors exist.

        <add key="VAOLoginEndpoint" value="https://sso.usvao.org/openid/provider_id" />
        <add key="VAOLoginRegistrationEndpoint" value="https://sso.usvao.org/register/register.jsp" />
        <add key="VAOLoginRealm" value="http://*.[your domain]"/>



One time only, set up IIS to host a virtual application pointing at directory/publishing, using an application pool running .NET 4.0. MSDN documentation can help you through this process. You can also choose to set up post-build steps to copy your built files elsewhere, or autodeploy with Visual Studio/IIS integration, which is not set up in this project by default.
You may also want to set your default page to login.html



Build instructions:

From this point, the build should be repeatable: in your IDE, rebuild all from the main .sln file. Your publishing project directory's login.html file will attempt to connect to the local database as a user or create a new one via a new VAO Login.
