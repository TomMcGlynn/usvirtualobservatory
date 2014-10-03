/****** Object:  Table [TAP_SCHEMA].[tapjobstable]    Script Date: 09/19/2011 13:27:16 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[tapjobstable](
	[jobid] [varchar](max) NULL,
	[jobstatus] [varchar](50) NULL,
	[starttime] [bigint] NULL,
	[endtime] [bigint] NULL,
	[duration] [bigint] NULL,
	[destruction] [bigint] NULL,
	[owner] [varchar](50) NULL,
	[lang] [varchar](50) NULL,
	[query] [varchar](max) NULL,
	[error] [varchar](max) NULL,
	[adql] [varchar](max) NULL,
	[resultFormat] [varchar](100) NULL,
	[request] [varchar](100) NULL,
	[maxrec] [int] NULL,
	[runid] [varchar](max) NULL,
	[uploadparam] [varchar](max) NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO


/****** Object:  Table [dbo].[tapusersdata]    Script Date: 09/19/2011 13:27:59 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[tapusersdata](
	[username] [varchar](100) NULL,
	[submittedjobs] [varchar](max) NULL,
	[tableaccess] [varchar](50) NULL,
	[tabledbname] [varchar](100) NULL,
	[tableusername] [varchar](100) NULL,
	[tableurl] [varchar](100) NULL,
	[accesstoken] [varchar](100) NULL,
	[uploadsuccess] [varchar](50) NULL,
	[tableuploadid] [varchar](150) NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO


/****** Object:  Table [TAP_SCHEMA].[tapuserauth]    Script Date: 09/19/2011 13:27:35 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [TAP_SCHEMA].[tapuserauth](
	[username] [varchar](100) NULL,
	[accesstoken] [varchar](150) NULL,
	[tokensecret] [varchar](100) NULL,
	[requesttoken] [varchar](100) NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

