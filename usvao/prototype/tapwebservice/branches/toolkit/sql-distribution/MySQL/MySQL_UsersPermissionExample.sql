/**
There are three users on three databases with different permissions.
1. datauser: this user should have ONLY select permission on main database (data you are serving via TAP)
2. tapuser: this user should be created on TAP_SCHEMA and TAP database (which holds all TAP_SCHEMA tables)
   this tapuser should have select, insert, update, create on tap_schema, make sure it doesnt have any permissions
   on main data db other than select.
3. uploaduser : This user is created on upload_schema which in upload db (a special database which holds user uploaded data)
   make sure upload user has ONLY select, insert, create , update permission on upload_schema. 
   It should not have delete permission. It can have ONLY Select permission on the data db.

* Note that datauser should have select permission on all the databases.
     

**/


create user 'datauser'@'localhost'  identified  by  'datauser123';
grant select on data_schema.* to 'datauser'@'localhost'; 

create schema upload_schema;
create user 'uploaduser'@'localhost'  identified  by  'uploaduser123';
grant select,insert,update,create on upload_schema.* to 'uploaduser'@'localhost'; 
grant select on data_schema.* to 'uploaduser'@'localhost';
grant select on upload_schema.* to 'datauser'@'localhost';

create user 'tapuser'@'localhost'  identified  by  'tapuser123';
grant select,insert,update,create on tap_schema.* to 'tapuser'@'localhost'; 
grant select on tap_shema.* to 'datauser'@'localhost';


