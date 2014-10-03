-- --------------------------------------------------------
-- adapted from same file in cadcTAP of the opencadc suite
-- at http://code.google.com/p/opencadc/
-- --------------------------------------------------------

-- minimal TAP_SCHEMA creation

-- assumes that the TAP_SCHEMA schema exists

-- sizes for fields are rather arbitrary and generous

-- tested with: PostgreSQL 8.x
-- tested Sybase 15.x

create table TAP_SCHEMA_schemas
(
   schema_name   varchar(64),        -- schema name, possibly qualified
   utype         varchar(512) NULL,  -- brief description of schema
   description   varchar(512) NULL,  -- UTYPE if schema corresponds to a data model
   
   primary key (schema_name)
)
;


create table TAP_SCHEMA_tables
(
   schema_name   varchar(64),        -- the schema name from TAP_SCHEMA.schemas
   table_name    varchar(128),       -- table name as it should be used in queries
   table_type    varchar(8),         -- one of: table, view
   utype         varchar(512) NULL,  -- UTYPE if table corresponds to a data model
   description   varchar(512) NULL,  -- brief description of table
   
   primary key (table_name),
   foreign key (schema_name) references TAP_SCHEMA_schemas (schema_name)
)
;

create table TAP_SCHEMA_columns
(
   table_name    varchar(128),
   column_name   varchar(64),
   utype         varchar(512) NULL,
   ucd           varchar(64)  NULL,
   unit          varchar(64)  NULL,
   description   varchar(512) NULL,
   datatype      varchar(64)  NOT NULL,
   size          integer      NULL,
   principal     integer      NOT NULL,
   indexed       integer      NOT NULL,
   std           integer      NOT NULL,
   
   primary key (table_name,column_name),
   foreign key (table_name) references TAP_SCHEMA_tables (table_name)
)
;


create table TAP_SCHEMA_keys
(
   key_id        varchar(64),
   from_table    varchar(128) NOT NULL,
   target_table  varchar(128) NOT NULL,
   utype         varchar(512) NULL,
   description   varchar(512) NULL,
      
   primary key (key_id),
   foreign key (from_table) references TAP_SCHEMA_tables (table_name),
   foreign key (target_table) references TAP_SCHEMA_tables (table_name)
)
;

create table TAP_SCHEMA_key_columns
(
   key_id varchar(64),
   from_column varchar(64)   NOT NULL,
   target_column varchar(64) NOT NULL,
   
   foreign key (key_id) references TAP_SCHEMA_keys (key_id)
)
;


