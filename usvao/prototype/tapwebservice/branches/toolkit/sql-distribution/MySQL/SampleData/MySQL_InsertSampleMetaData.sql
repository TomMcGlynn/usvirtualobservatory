insert into TAP_SCHEMA.schemas (schema_name,description,utype) values
( 'data_schema', 'schema to describe actual Data tableset', NULL);

insert into TAP_SCHEMA.tables (schema_name,table_name,table_type, description,utype) values
( 'data_schema', 'data_schema.Frame', 'table', 'Frame table contains information about the images per field', NULL);

insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'fieldID', 'ID for each field in the Frame', 64, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'zoom', 'Image zoom level per field', NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'run', 'RUN number',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'rerun', 'RERUN',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'camcol', 'Camera Column',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'field', 'SDSS field',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'stripe', 'strip',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'strip', 'strip',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'a', 'astrometry',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'b', 'photometry',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'c', 'pho',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'd', 'Camera Column',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'e', 'Camera Column',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);
insert into TAP_SCHEMA.columns (table_name,column_name,description,utype,ucd,unit,datatype,size,principal,indexed,std) values
( 'data_schema.frame', 'f', 'Camera Column',               NULL, NULL, NULL, 'adql:VARCHAR', 512, 1,0,0);



insert into TAP_SCHEMA.keys (key_id, from_table,target_table) values
( 'k1', 'TAP_SCHEMA.tables', 'data_schema.frame');

insert into TAP_SCHEMA.key_columns (key_id,from_column,target_column) values
( 'k1', 'schema_name', 'schema_name');
