USE [VOResourceTest]
GO

/*** Database creation script vor voresource 1.1. 04/03/2013

Creates harvester-related tables, only required if the VAO/STScI harvester service will be run against this database.

Related files:
	* VOResourceCreate Basic table generation for minimal database schema and search.
	* VOResourceCreate_populate_harvester, populates harvester data with a snapshot from the IVOA Registry of Registries (optional)
		This script must be run before populating harvester data.

***/


/****** Object:  Table [dbo].[HarvesterLog]    Script Date: 04/01/2013 16:12:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HarvesterLog](
	[ServiceURL] [nvarchar](100) NOT NULL,
	[date] [datetime] NOT NULL,
	[type] [nchar](10) NOT NULL,
	[status] [int] NULL,
	[message] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Harvester]    Script Date: 04/01/2013 16:12:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Harvester](
	[ServiceURL] [nvarchar](100) NOT NULL,
	[RegistryID] [nvarchar](100) NOT NULL,
	[DoHarvest] [bit] NOT NULL
) ON [PRIMARY]
GO



GO
/****** Object:  Table [dbo].[Harvester]    Script Date: 04/24/2013 17:59:33 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Harvester](
	[ServiceURL] [nvarchar](100) NOT NULL,
	[RegistryID] [nvarchar](100) NOT NULL,
	[DoHarvest] [bit] NOT NULL
) ON [PRIMARY]
GO
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://rofr.ivoa.net/cgi-bin/oai.pl', N'ivo://ivoa.net/rofr', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://www.cadc-ccda.hia-iha.nrc-cnrc.gc.ca/reg/OAIHandlerv1_0', N'ivo://cadc.nrc.ca/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://gavo.aip.de/oai.xml', N'ivo://aip.gavo.org/__system__/services/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://dc.zah.uni-heidelberg.de/oai.xml', N'ivo://org.gavo.dc/static/registryrecs/registry.rr', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://registry.euro-vo.org/oai.jsp', N'ivo://esavo/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://msslkz.mssl.ucl.ac.uk/mssl-registry/OAIHandlerv1_0', N'ivo://mssl.ucl.ac.uk/org.astrogrid.registry.RegistryService', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://vo.astronet.ru/cas/registry.php', N'ivo://astronet.ru/cas/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://astrogrid.ast.cam.ac.uk/CASU-registry/OAIHandlerv1_0', N'ivo://uk.ac.cam.ast/org.astrogrid.registry.RegistryService', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://jvo.nao.ac.jp/publishingRegistry1.0/oai.pl', N'ivo://jvo/publishingregistry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://nvo.ncsa.uiuc.edu/cgi-bin/reg10/oai.pl', N'ivo://nvo.ncsa/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://datanet.csiro.au:80/astrogrid-registry/OAIHandlerv1_0', N'ivo://au.csiro/org.astrogrid.registry.RegistryService', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://publishing-registry.roe.ac.uk:80/astrogrid-registry_v1_0/OAIHandlerv1_0', N'ivo://wfau.roe.ac.uk/org.astrogrid.registry.RegistryService', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://nvo.caltech.edu:8080/carnivore/cgi-bin/OAI-XML/carnivore/OAI.pl', N'ivo://nvo.caltech/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://heasarc.gsfc.nasa.gov/cgi-bin/OAI2/XMLFile/nvo/oai.pl', N'ivo://nasa.heasarc/heasarc.xml', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://cdsweb.u-strasbg.fr/reg-bin/vizier/oai.pl', N'ivo://CDS.VizieR/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://cdaftp.harvard.edu/cgi-bin/riabox/oai.pl', N' ivo://cxc.harvard.edu/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://vao.stsci.edu/directory/oai.aspx', N'ivo://archive.stsci.edu/nvoregistry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://nova.iafe.uba.ar/oai.xml', N'ivo://ar.nova/__system__/services/registry', 0)
INSERT [dbo].[Harvester] ([ServiceURL], [RegistryID], [DoHarvest]) VALUES (N'http://voparis-registry.obspm.fr/vo/oai', N'ivo://vopdc/registry', 0)
