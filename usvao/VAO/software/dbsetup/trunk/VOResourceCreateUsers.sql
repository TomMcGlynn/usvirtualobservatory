USE [voresourcetest]
GO
/****** Object:  User [nvowebaccess]    Script Date: 04/19/2013 16:45:01 ******/
CREATE USER [nvowebaccess] FOR LOGIN [nvowebaccess] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  User [nvo]    Script Date: 04/19/2013 16:45:01 ******/
CREATE USER [nvo] FOR LOGIN [nvo] WITH DEFAULT_SCHEMA=[dbo]
GO

GRANT EXECUTE ON OBJECT::dbo.deprecateresource
    TO [nvo];
GO

GRANT EXECUTE ON OBJECT::dbo.ensureauth
    TO [nvo];
GO

EXEC sp_addrolemember 'db_datareader', 'nvo'
EXEC sp_addrolemember 'db_datareader', 'nvowebaccess'
EXEC sp_addrolemember 'db_datawriter', 'nvo'