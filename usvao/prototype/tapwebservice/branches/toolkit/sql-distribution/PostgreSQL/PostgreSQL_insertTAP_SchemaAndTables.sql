
insert into tap_schema.schemas (schema_name,description,utype) values
( 'tap_schema', 'a special schema to describe a TAP tableset', NULL);

insert into tap_schema.tables (schema_name,table_name,table_type, description,utype) values
( 'tap_schema', 'tap_schema.schemas', 'table', 'description of schemas in this tableset', NULL);
insert into tap_schema.tables (schema_name,table_name,table_type, description,utype) values
( 'tap_schema', 'tap_schema.tables', 'table', 'description of tables in this tableset', NULL);
insert into tap_schema.tables (schema_name,table_name,table_type, description,utype) values
( 'tap_schema', 'tap_schema.columns', 'table', 'description of columns in this tableset', NULL);
insert into tap_schema.tables (schema_name,table_name,table_type, description,utype) values
( 'tap_schema', 'tap_schema.keys', 'table', 'description of foreign keys in this tableset', NULL);
insert into tap_schema.tables (schema_name,table_name,table_type, description,utype) values
( 'tap_schema', 'tap_schema.key_columns', 'table', 'description of foreign key columns in this tableset', NULL);

insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.schemas', 'schema_name', 'schema name for reference to tap_schema.schemas', NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.schemas', 'utype', 'lists the utypes of schemas in the tableset',           NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.schemas', 'description', 'describes schemas in the tableset',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.tables', 'schema_name', 'the schema this table belongs to',                 NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.tables', 'table_name', 'the fully qualified table name',                    NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.tables', 'table_type',  'one of: table, view',                              NULL, NULL, NULL, 'adql:VARCHAR', 5, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.tables', 'utype', 'lists the utype of tables in the tableset',              NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.tables', 'description', 'describes tables in the tableset',                 NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'table_name', 'the table this column belongs to',                 NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'column_name', 'the column name',                                 NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'utype', 'lists the utypes of columns in the tableset',           NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'ucd', 'lists the UCDs of columns in the tableset',               NULL, NULL, NULL, 'adql:VARCHAR', 128, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'unit', 'lists the unit used for column values in the tableset',  NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'description', 'describes the columns in the tableset',           NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'datatype', 'lists the ADQL datatype of columns in the tableset', NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'size', 'lists the size of variable-length columns in the tableset', NULL, NULL, NULL, 'adql:INTEGER', NULL, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'principal', 'a principal column; 1 means 1, 0 means 0',      NULL, NULL, NULL, 'adql:INTEGER', NULL, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'indexed', 'an indexed column; 1 means 1, 0 means 0',         NULL, NULL, NULL, 'adql:INTEGER', NULL, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.columns', 'std', 'a standard column; 1 means 1, 0 means 0',             NULL, NULL, NULL, 'adql:INTEGER', NULL, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.keys', 'key_id', 'unique key to join to tap_schema.key_columns',            NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.keys', 'from_table', 'the table with the foreign key',                      NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.keys', 'target_table', 'the table with the primary key',                    NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.key_columns', 'key_id', 'key to join to tap_schema.keys',                   NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.key_columns', 'from_column', 'column in the from_table',                    NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);
insert into tap_schema.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'tap_schema.key_columns', 'target_column', 'column in the target_table',                NULL, NULL, NULL, 'adql:VARCHAR', 64, 1,0,0);

insert into tap_schema.keys (key_id, from_table,target_table) values
( 'k1', 'tap_schema.tables', 'tap_schema.schemas');
insert into tap_schema.keys (key_id, from_table,target_table) values
( 'k2', 'tap_schema.columns', 'tap_schema.tables'); 
insert into tap_schema.keys (key_id, from_table,target_table) values
( 'k3', 'tap_schema.keys', 'tap_schema.tables');
insert into tap_schema.keys (key_id, from_table,target_table) values
( 'k4', 'tap_schema.keys', 'tap_schema.tables');
insert into tap_schema.keys (key_id, from_table,target_table) values
( 'k5', 'tap_schema.key_columns', 'tap_schema.keys');

insert into tap_schema.key_columns (key_id,from_column,target_column) values
( 'k1', 'schema_name', 'schema_name');
insert into tap_schema.key_columns (key_id,from_column,target_column) values
( 'k2', 'table_name', 'table_name');
insert into tap_schema.key_columns (key_id,from_column,target_column) values
( 'k3', 'from_table', 'table_name');
insert into tap_schema.key_columns (key_id,from_column,target_column) values
( 'k4', 'target_table', 'table_name');
insert into tap_schema.key_columns (key_id,from_column,target_column) values
( 'k5', 'key_id', 'key_id');
