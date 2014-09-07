

/** The publishing system requires certificate encryption for passwords until Single SignOn is implemented. 
	Instructions follow **/

/** If migrating to a server instantiation that has never had a registry database with
	publishing user management enabled, a master key must be created *once*, 
	no matter how many times testing databases and certificates then get created or destroyed **/

/** Once a server instance has a key, the rest is handled by migrating the certificate files and authenticating them with a password.
	Porting existing keys for new server instances is documented in MSDN. This page may be helpful: http://technet.microsoft.com/en-us/library/ms366281.aspx **/

/** The key used at STScI is PASS_Key_01 by certificate "PublishingPasswords" on mastsqla/b/dev and requires a password as saved in project config files **/
/** Note that if necessary user data can be migrated by opening the old key and decrypting the data into inserts in the new database with the new key, all without looking at the password data **/


--USE master;
--GO
--CREATE MASTER KEY ENCRYPTION BY PASSWORD = '******';
--GO

--CREATE CERTIFICATE PublishingPasswords 
--ENCRYPTION BY PASSWORD = '******' 
--WITH SUBJECT = 'Registry Publishing System password encryption' 
--GO

--Create Symmetric Key
--CREATE SYMMETRIC KEY PASS_Key_01 WITH
--IDENTITY_VALUE = 'Registry Publishing System password encryption',
--KEY_SOURCE = '******',
--ALGORITHM = TRIPLE_DES
--ENCRYPTION BY CERTIFICATE PublishingPasswords

--GRANT CONTROL ON CERTIFICATE::PublishingPasswords TO nvo; 
--GO
--GRANT REFERENCES ON SYMMETRIC KEY::PASS_Key_01 TO nvo; 
--GO
--GRANT CONTROL ON CERTIFICATE::PublishingPasswords TO nvowebaccess; 
--GO
--GRANT REFERENCES ON SYMMETRIC KEY::PASS_Key_01 TO nvowebaccess; 
--GO