--create schema rr;

USE [VOResourceTest]
GO

/****** Object:  Table [dbo].[ResourceVOTableCache]    Script Date: 03/07/2013 11:39:42 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ResourceVOTableCache](
	[pkey] [bigint] IDENTITY(1,1) NOT NULL,
	[rkey] [bigint] NOT NULL,
	[ResourceAsRow] [varchar](max) NULL,
	[InterfaceAsRow] [varchar](max) NULL
) ON [PRIMARY]
GO
SET ANSI_PADDING OFF
GO

--------

create table dbo.authority (
   pkey bigint IDENTITY (1,1) not null,
   authority nvarchar(200) unique not null,

   primary key (pkey)
);

create table dbo.resource (
   pkey bigint IDENTITY (1,1) not null,
   ivoid varchar(512) not null,
   res_type varchar(32) not null,
   created datetime2 not null,
   updated datetime2 not null,
   [status] varchar(16) not null,
   short_name varchar(24) default null,
   res_title varchar(512) not null,
   content_level varchar(1024) default '',     -- hash list
   res_description varchar(max) not null,
   reference_url varchar(1024) not null,
   content_type varchar(1024) default '',      -- hash list
   source_format varchar(1024) default null,
   source_value varchar(1024) default null,
   [version] varchar(32) default null,
   region_of_regard real default null,
   waveband varchar(128) default null,        -- hash list
   footprint_ivoid varchar(512) default null,
   footprint_url varchar(1024) default null,
   rights varchar(1024) default null,          -- hash list
   facility varchar(2048) default null,        -- hash list
   instrument varchar(2048) default null,      -- hash list
   format varchar(1024) default null,          -- hash list
   data_url varchar(1024) default null,

   [xml] ntext default null,
   authkey bigint not null,
   rev int default 1 not null,
   rstat int default 0 not null, -- 0=deprecated, 1=active,2=inactive,3=deleted
   harvestedFromDate datetime2 default null,
   harvestedFrom varchar(1024) default null,
   harvestedFromID varchar(1024) default null,
   tag varchar(1024) default '',                -- hash list
   ukey bigint default null, -- foreign key to user table if publishing enabled, but may be unused.
   validationLevel int default 2 not null, -- resource-level validation as set by us, avoids a join looking for valid records in search interfaces

   --primary key (ivoid,rev),
   primary key (pkey),
   foreign key (authkey) references dbo.authority (pkey)
);
create index dbo.resource_ivoid_x on dbo.resource (ivoid);
create index dbo.resource_rstat_x on dbo.resource (rstat);

create table dbo.res_role (
   rkey bigint not null,
   ivoid varchar(512) not null,
   role_seq smallint not null,
   role_name varchar(1024) not null,
   role_ivoid varchar(512) default null,
   base_utype varchar(1024) not null,
   [address] varchar(1024) default null,
   email varchar(1024) default null,
   telephone varchar(128) default null,
   logo varchar(1024) default null,

   foreign key (rkey) references dbo.resource (pkey)
   --foreign key (ivoid) references dbo.resource (ivoid) -- without ivoid as a primary key, disallowed in sql server.
);
create index dbo.res_role_ivoid_x on dbo.res_role (ivoid);

create table dbo.subject (
   rkey bigint not null,
   ivoid varchar(512) not null,
   [subject] varchar(1024) not null

   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.subject_ivoid_x on dbo.subject (ivoid);

create table dbo.capability (
   pkey bigint IDENTITY (1,1) not null,
   rkey bigint not null,
   ivoid varchar(512) not null,
   cap_index smallint not null,
   cap_name varchar(1024) default null,    -- non-standard
   cap_type varchar(1024) default null,
   cap_description varchar(max) default null,
   standard_id varchar(1024) default null,

   primary key (pkey),
   -- primary key (ivoid,cap_index),
   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.capability_ivoid_x on dbo.capability (ivoid);
create index dbo.capability_cap_x on dbo.capability (cap_index);

create table dbo.res_schema (
   pkey bigint IDENTITY (1,1) not null,
   rkey bigint not null,
   ivoid varchar(512) not null,
   schema_index smallint not null,
   [schema_name] varchar(1024) default null,
   schema_title varchar(1024) default null,
   schema_description varchar(max) default null,
   schema_utype varchar(1024) default null,

   primary key (pkey),
   -- primary key (ivoid, schema_index),
   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.res_schema_ivoid_x on dbo.res_schema (ivoid);

create table dbo.res_table (
   pkey bigint IDENTITY (1,1) not null,
   rkey bigint not null,
   skey bigint not null,
   ivoid varchar(512) not null,
   schema_index smallint not null,
   table_index smallint not null,
   table_name varchar(1024) not null,
   table_title varchar(1024) default null,
   table_type varchar(1024) default null,
   table_utype varchar(1024) default null,
   table_description varchar(max) default null,

   primary key (pkey),
   -- primary key (ivoid,schema_index,table_index),
   foreign key (skey) references dbo.res_schema (pkey),
   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.res_table_ivoid_x on dbo.res_table (ivoid);

create table dbo.table_column (
   rkey bigint not null,
   skey bigint not null,
   tkey bigint not null,
   ivoid varchar(512) not null,
   schema_index smallint not null,
   table_index smallint not null,
   name varchar(200) not null,
   datatype varchar(1024) default null,
   [description] varchar(max) default null,
   ucd varchar(1024) default null,
   unit varchar(1024) default null,
   utype varchar(1024) default null,
   flag varchar(1024) default null,
   std smallint default null,
   extended_schema varchar(1024) default null,
   extended_type varchar(1024) default null,
   arraysize varchar(1024) default null,
   delim varchar(1024) default null,
   type_system varchar(1024) default null,

   -- primary key (tkey,name),
   -- primary key (ivoid,schema_index,table_index,name),
   foreign key (skey) references dbo.res_schema (pkey),
   foreign key (tkey) references dbo.res_table (pkey),
   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.table_column_ivoid_x on dbo.table_column (ivoid);

create table dbo.interface (
   pkey bigint IDENTITY (1,1) not null,
   rkey bigint not null,
   ckey bigint not null,
   ivoid varchar(512) not null,
   cap_index smallint not null,
   intf_index smallint not null,
   intf_type varchar(1024) default null,
   intf_role varchar(1024) default null,
   std_version varchar(1024) default null,
   query_type varchar(1024) default null,
   result_type varchar(1024) default null,
   wsdl_url varchar(1024) default null,
   url_use varchar(1024) not null,
   access_url varchar(1024) not null,
   sec_stdid varchar(1024) default null,

   primary key (pkey),
   -- primary key (ivoid,cap_index,intf_index),
   foreign key (rkey) references dbo.resource (pkey),
   foreign key (ckey) references dbo.capability (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.interface_ivoid_x on dbo.interface (ivoid);
create index dbo.interface_cap_x on dbo.interface (cap_index);
create index dbo.interface_intf_x on dbo.interface (intf_index);

create table dbo.intf_param (
   rkey bigint not null,
   ckey bigint not null,
   ikey bigint not null,
   ivoid varchar(512) not null,
   cap_index smallint not null,
   intf_index smallint not null,
   name varchar(128) not null,
   datatype varchar(1024) default null,
   [description] varchar(max) default null,
   ucd varchar(1024) default null,
   unit varchar(1024) default null,
   utype varchar(1024) default null,
   flag varchar(1024) default null,
   std smallint not null,
   extended_schema varchar(1024) default null,
   extended_type varchar(1024) default null,
   form varchar(1024) default null,

   -- primary key (ikey,name),
   -- primary key (ivoid,cap_index,intf_index,name),
   foreign key (rkey) references dbo.resource (pkey),
   foreign key (ckey) references dbo.capability (pkey),
   foreign key (ikey) references dbo.interface (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid),
);
create index dbo.intf_param_ivoid_x on dbo.intf_param (ivoid);
create index dbo.intf_param_cap_x on dbo.intf_param (cap_index);
create index dbo.intf_param_intf_x on dbo.intf_param (intf_index);

create table dbo.relationship (
   rkey bigint not null,
   ivoid varchar(512) not null,
   relationship_type varchar(1024) not null,
   related_id varchar(1024) default null,
   related_name varchar(1024) not null,

   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.relationship_ivoid_x on dbo.relationship (ivoid);

create table dbo.validation (
   rkey bigint not null,
   ivoid varchar(512) not null,
   validated_by varchar(1024) not null,
   [level] smallint not null,
   curated smallint default 0,  -- 0=level is auto value; 1=level is human-set
   ckey bigint default null,
   cap_index smallint default null,

   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.validation_ivoid_x on dbo.validation (ivoid);
create index dbo.validation_cap_x on dbo.validation (cap_index);

create table dbo.date (
   rkey bigint not null,
   ivoid varchar(512) not null,
   date_value date not null,
   value_role varchar(1024) default null,

   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)
);
create index dbo.date_ivoid_x on dbo.date (ivoid);

create table dbo.res_detail (
   rkey bigint not null,
   ivoid varchar(512) not null,
   ckey bigint default null,
   cap_index smallint default null,
   detail_utype varchar(256) not null,
   detail_value varchar(max) not null,

   foreign key (rkey) references dbo.resource (pkey),
   --foreign key (ivoid) references dbo.resource (ivoid)   
);
create index dbo.res_detail_ivoid_x on dbo.res_detail (ivoid);
create index dbo.res_detail_cap_x on dbo.res_detail (cap_index);
GO


GO
CREATE FULLTEXT CATALOG [xml]WITH ACCENT_SENSITIVITY = OFF
AS DEFAULT
AUTHORIZATION [dbo]
GO


/****** Object:  Index [activeandvalid]    Script Date: 07/15/2013 16:52:50 ******/
CREATE NONCLUSTERED INDEX [activeandvalid] ON [dbo].[resource] 
(
	[pkey] ASC,
	[rstat] ASC,
	[validationLevel] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO


CREATE PROCEDURE dbo.ensureauth 
  @authid varchar(128),
  @authkey bigint OUTPUT
AS
  SET @authkey = (SELECT pkey FROM dbo.authority WHERE authority=@authid); 
  IF @authkey IS NULL 
  BEGIN
    INSERT INTO dbo.authority (authority) VALUES (@authid); 
    SET @authkey = (SELECT pkey FROM dbo.authority WHERE authority=@authid); 
  END
GO

CREATE PROCEDURE dbo.deprecateresource 
   @ivorn varchar(512),  
   @existingkey bigint,
   @revision int OUTPUT
AS
  IF @existingkey IS NULL 
    SET @existingkey = (SELECT pkey FROM dbo.resource WHERE ivoid=@ivorn and rstat>0);
  IF @existingkey IS NOT NULL 
  BEGIN
    SET @revision = (SELECT MAX(rev)+1 FROM dbo.resource WHERE pkey=@existingkey);
    UPDATE dbo.resource SET rstat=0 WHERE pkey=@existingkey;
  END
GO

