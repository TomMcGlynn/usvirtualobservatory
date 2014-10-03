
CREATE SCHEMA TAP_SCHEMA
GO

/****** Object:  Table [TAP_SCHEMA].[schemas]    Script Date: 06/24/2011 11:31:30 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[schemas](
	[schema_name] [varchar](64) NOT NULL,
	[utype] [varchar](512) NULL,
	[description] [varchar](512) NULL,
PRIMARY KEY CLUSTERED 
(
	[schema_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO



/****** Object:  Table [TAP_SCHEMA].[tables]    Script Date: 06/24/2011 11:31:49 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[tables](
	[schema_name] [varchar](64) NULL,
	[table_name] [varchar](128) NOT NULL,
	[table_type] [varchar](5) NOT NULL,
	[utype] [varchar](512) NULL,
	[description] [varchar](512) NULL,
PRIMARY KEY CLUSTERED 
(
	[table_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [TAP_SCHEMA].[tables]  WITH CHECK ADD FOREIGN KEY([schema_name])
REFERENCES [TAP_SCHEMA].[schemas] ([schema_name])
GO



/****** Object:  Table [TAP_SCHEMA].[columns]    Script Date: 06/24/2011 11:30:17 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO



CREATE TABLE [TAP_SCHEMA].[columns](
	[table_name] [varchar](128) NOT NULL,
	[column_name] [varchar](64) NOT NULL,
	[utype] [varchar](512) NULL,
	[ucd] [varchar](128) NULL,
	[unit] [varchar](64) NULL,
	[description] [varchar](512) NULL,
	[datatype] [varchar](64) NOT NULL,
	[size] [int] NULL,
	[principal] [int] NOT NULL,
	[indexed] [int] NOT NULL,
	[std] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[table_name] ASC,
	[column_name] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [TAP_SCHEMA].[columns]  WITH CHECK ADD FOREIGN KEY([table_name])
REFERENCES [TAP_SCHEMA].[tables] ([table_name])
GO


/****** Object:  Table [TAP_SCHEMA].[keys]    Script Date: 06/24/2011 11:31:06 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[keys](
	[key_id] [varchar](64) NOT NULL,
	[from_table] [varchar](128) NOT NULL,
	[target_table] [varchar](128) NOT NULL,
	[utype] [varchar](512) NULL,
	[description] [varchar](512) NULL,
PRIMARY KEY CLUSTERED 
(
	[key_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [TAP_SCHEMA].[keys]  WITH CHECK ADD FOREIGN KEY([from_table])
REFERENCES [TAP_SCHEMA].[tables] ([table_name])
GO

ALTER TABLE [TAP_SCHEMA].[keys]  WITH CHECK ADD FOREIGN KEY([target_table])
REFERENCES [TAP_SCHEMA].[tables] ([table_name])
GO


/****** Object:  Table [TAP_SCHEMA].[key_columns]    Script Date: 06/24/2011 11:30:48 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[key_columns](
	[key_id] [varchar](64) NULL,
	[from_column] [varchar](64) NOT NULL,
	[target_column] [varchar](64) NOT NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

ALTER TABLE [TAP_SCHEMA].[key_columns]  WITH CHECK ADD FOREIGN KEY([key_id])
REFERENCES [TAP_SCHEMA].[keys] ([key_id])
GO






