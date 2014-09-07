USE [VOResourceSchemaTest]
GO

/*** Database creation script vor voresource 1.1. 04/03/2013

Creates user-related tables, only required if the VAO/STScI publishing service will be writing to this database. 
Further, instructions in VOResourceCreate_encryptionkeys must be followed before creating new user accounts in the publishing service.

Related files:
	* VOResourceCreate Basic table generation for minimal database schema and search.

	* VOResourceCreate_encryptionkeys contains instructions for setting up encryption keys from password/certificates on a new or existing server.
		Encryptio only required if the VAO/STScI publishing service will be writing to this database. 
		Further, if a new copy of the publishing service is being started and old STScI user accounts are not being migrated, 
		new certificates and passwords should be generated from scratch.
	* VOResourceCreate_userdata populates a snapshot of user data for the publishing service 
		This script must be run before populating user data.
		This is only required if the VAO/STScI publishing service will be writing to this database AND old STScI user accounts should be migrated.
***/



/****** Object:  Table [dbo].[Users]    Script Date: 04/01/2013 16:12:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Users](
	[name] [nvarchar](50) NOT NULL,
	[email] [nvarchar](320) NOT NULL,
	[username] [nvarchar](16) NOT NULL,
	[pkey] [bigint] IDENTITY(1,1) NOT NULL,
	[defaultAuthorityID] [nvarchar](50) NULL,
 CONSTRAINT [PK_Users] PRIMARY KEY CLUSTERED 
(
	[pkey] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[UserAuthorities]    Script Date: 04/01/2013 16:12:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserAuthorities](
	[pkey] [bigint] IDENTITY(1,1) NOT NULL,
	[ukey] [bigint] NOT NULL,
	[authorityID] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_UserAuthorities] PRIMARY KEY CLUSTERED 
(
	[pkey] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[PendingResources]    Script Date: 08/07/2013 17:19:14 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[PendingResources](
	[pkey] [bigint] IDENTITY(1,1) NOT NULL,
	[ukey] [bigint] NOT NULL,
	[xml] [ntext] NULL,
	[ivoid] [varchar](512) NOT NULL,
	[rstat] [int] NOT NULL,
 CONSTRAINT [PK_PendingResources] PRIMARY KEY CLUSTERED 
(
	[pkey] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [dbo].[PendingResources]  WITH CHECK ADD  CONSTRAINT [fk_UserKey] FOREIGN KEY([ukey])
REFERENCES [dbo].[Users] ([pkey])
GO

ALTER TABLE [dbo].[PendingResources] CHECK CONSTRAINT [fk_UserKey]
GO

