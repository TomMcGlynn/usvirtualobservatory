create schema data_schema;


CREATE TABLE data_schema.Frame(
	fieldID bigint NOT NULL,
	zoom int NOT NULL,
	run int NOT NULL,
	rerun int NOT NULL,
	camcol int NOT NULL,
	field int NOT NULL,
	stripe int NOT NULL,
	strip varchar(32) NOT NULL,
	a float NOT NULL,
	b float NOT NULL,
	c float NOT NULL,
	d float NOT NULL,
	e float NOT NULL,
	f float NOT NULL,
	node float NOT NULL,
	incl float NOT NULL,
	raMin float NOT NULL,
	raMax float NOT NULL,
	decMin float NOT NULL,
	decMax float NOT NULL,
	mu float NOT NULL,
	nu float NOT NULL,
	ra float NOT NULL,
	decl float NOT NULL,
	cx float NOT NULL,
	cy float NOT NULL,
	cz float NOT NULL,
	
 CONSTRAINT pk_Frame_fieldID_zoom PRIMARY KEY CLUSTERED 
 (
	fieldID ASC,
	zoom ASC
 )
)
;
ALTER TABLE data_schema.Frame ALTER  COLUMN mu SET DEFAULT 0;

ALTER TABLE data_schema.Frame ALTER COLUMN nu SET DEFAULT 0;

ALTER TABLE data_schema.Frame ALTER COLUMN ra SET DEFAULT 0;

ALTER TABLE data_schema.Frame ALTER COLUMN decl SET DEFAULT 0;

ALTER TABLE data_schema.Frame ALTER COLUMN cx SET DEFAULT 0;

ALTER TABLE data_schema.Frame ALTER  COLUMN cy SET DEFAULT 0;

ALTER TABLE data_schema.Frame ALTER  COLUMN cz SET DEFAULT 0;


LOAD DATA  LOCAL INFILE 'yourfilepath/DataSample.txt' INTO TABLE data_schema.frame




