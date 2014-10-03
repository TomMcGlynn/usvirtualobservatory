Create schema TAP_SCHEMA;
use TAP_SCHEMA;
CREATE TABLE TAP_SCHEMA.schemas(
  schema_name varchar(64) NOT NULL,
  primary key (schema_name asc),
	utype varchar(512) NULL,
	description varchar(512) NULL);


CREATE TABLE TAP_SCHEMA.tables(
	schema_name varchar(64) NULL,
	table_name varchar(128) NOT NULL,
  primary key (table_name asc),
	table_type varchar(5) NOT NULL,
	utype varchar(512) NULL,
	description varchar(512) NULL,
  foreign key (schema_name) references TAP_SCHEMA.schemas(schema_name) );


CREATE TABLE TAP_SCHEMA.columns(
	table_name varchar(128) NOT NULL,
	column_name varchar(64) NOT NULL,
	utype varchar(512) NULL,
	ucd varchar(128) NULL,
	unit varchar(64) NULL,
	description varchar(512) NULL,
	datatype varchar(64) NOT NULL,
	size int NULL,
	principal int NOT NULL,
	indexed int NOT NULL,
	std int NOT NULL,
  PRIMARY KEY (	table_name ASC,	column_name ASC),
  Foreign key (table_name) references TAP_SCHEMA.tables(table_name)
);

CREATE TABLE TAP_SCHEMA.keys(
	key_id varchar(64) NOT NULL,
	from_table varchar(128) NOT NULL,
	target_table varchar(128) NOT NULL,
	utype varchar(512) NULL,
	description varchar(512) NULL,
  PRIMARY KEY (key_id ASC),
  FOREIGN KEY (from_table) references TAP_SCHEMA.tables(table_name),
  FOREIGN KEY (target_table) references TAP_SCHEMA.tables(table_name)
);


CREATE TABLE TAP_SCHEMA.key_columns(
	key_id varchar(64) NULL,
	from_column varchar(64) NOT NULL,
	target_column varchar(64) NOT NULL,
	FOREIGN KEY (key_id) references TAP_SCHEMA.keys(key_id)
);