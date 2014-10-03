
CREATE SCHEMA tap_schema;

CREATE TABLE tap_schema.schemas(
  schema_name varchar(64) NOT NULL,
  utype varchar(512) NULL,
  description varchar(512) NULL,
  primary key (schema_name)
);

CREATE TABLE tap_schema.tables(
	schema_name varchar(64) NULL,
	table_name varchar(128) NOT NULL,
	table_type varchar(5) NOT NULL,
	utype varchar(512) NULL,
	description varchar(512) NULL,
        primary key (table_name),
        foreign key (schema_name) references tap_schema.schemas(schema_name) );


CREATE TABLE tap_schema.columns(
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
	PRIMARY KEY (	table_name ,	column_name ),
        Foreign key (table_name) references tap_schema.tables(table_name)
);

CREATE TABLE tap_schema.keys(
	key_id varchar(64) NOT NULL,
	from_table varchar(128) NOT NULL,
	target_table varchar(128) NOT NULL,
	utype varchar(512) NULL,
	description varchar(512) NULL,
  PRIMARY KEY (key_id ),
  FOREIGN KEY (from_table) references tap_schema.tables(table_name),
  FOREIGN KEY (target_table) references tap_schema.tables(table_name)
);


CREATE TABLE tap_schema.key_columns(
	key_id varchar(64) NULL,
	from_column varchar(64) NOT NULL,
	target_column varchar(64) NOT NULL,
	FOREIGN KEY (key_id) references tap_schema.keys(key_id)
);
