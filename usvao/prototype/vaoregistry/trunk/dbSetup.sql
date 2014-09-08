CREATE TABLE [ServiceType] (
	[ServiceType] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	CONSTRAINT [PK_ServiceType] PRIMARY KEY  CLUSTERED 
	(
		[ServiceType]
	)  ON [PRIMARY] 
) ON [PRIMARY]
GO


insert into ServiceType values ('WebService')
insert into ServiceType values ('HTTP-GET')
insert into ServiceType values ('Documentation')
insert into ServiceType values ('SIAP')
insert into ServiceType values ('CONE')
insert into ServiceType values ('SIAP/Cutout')
insert into ServiceType values ('SIAP/Archive')


CREATE TABLE [ContentLevel] (
	[ContentLevel] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	CONSTRAINT [PK_ContentLevel] PRIMARY KEY  CLUSTERED 
	(
		[ContentLevel]
	)  ON [PRIMARY] 
) ON [PRIMARY]
GO


CREATE TABLE [CoverageSpectral] (
	[CoverageSpectral] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	CONSTRAINT [PK_CoverageSpectral] PRIMARY KEY  CLUSTERED 
	(
		[CoverageSpectral]
	)  ON [PRIMARY] 
) ON [PRIMARY]
GO

CREATE TABLE [ResourceType] (
	[ResourceType] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	CONSTRAINT [PK_ResourceType] PRIMARY KEY  CLUSTERED 
	(
		[ResourceType]
	)  ON [PRIMARY] 
) ON [PRIMARY]
GO



CREATE TABLE [Resource] (
	[Title] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Ticker] [char] (8) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Publisher] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[Creator] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Subject] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Description] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[Contributor] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Date] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Version] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Identifier] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ResourceURL] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ServiceURL] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[ContactName] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ContactEmail] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ResourceType] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[CoverageSpatial] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[CoverageSpectral] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[CoverageTemporal] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[EntrySize] [char] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[MaxSR] [float] NOT NULL ,
	[MaxRecords] [int] NOT NULL ,
	[ContentLevel] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Facility] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Instrument] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[Format] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ServiceType] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	CONSTRAINT [FK_Resource_ServiceType] FOREIGN KEY 
	(
		[ServiceType]
	) REFERENCES [ServiceType] (
		[ServiceType]
	)
) ON [PRIMARY]
GO



