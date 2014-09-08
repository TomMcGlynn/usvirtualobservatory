username: purse2
password: l1ghtz0n

create database if not exists purse2;
use purse2;

create table if not exists entity
       (id serial, 
	created timestamp,
	name varchar(256),
	description text,
	type char(16))
       engine=innodb;

create table if not exists authentication
       (id serial,
	id_entity bigint(20) unsigned,
	foreign key (id_entity) references entity(id),
	start datetime, end datetime,
	registration_status char(16),
	password_sha1 char(40))
       engine=innodb;

create table if not exists biographical
       (id serial,
	id_entity bigint(20) unsigned,
	start datetime, end datetime,
	type char(16),
	label varchar(64),
	value varchar(1024))
       engine=innodb;

create table if not exists privacy
       (id serial,
	id_entity_protected bigint(20) unsigned,
	foreign key (id_entity_protected) references entity(id),
	id_entity_viewing bigint(20) unsigned,
	foreign key (id_entity_viewing) references entity(id),
	start datetime, end datetime,
	biographical_type char(16),
	biographical_label varchar(64))
       engine=innodb;

create table if not exists session
       (id_entity bigint(20) unsigned,
	foreign key (id_entity) references entity(id),
	id_authentication bigint(20) unsigned,
	foreign key (id_authentication) references authentication(id),
	start datetime, end datetime)
       engine=innodb;

create table if not exists role
       (id_entity_user bigint(20) unsigned,
	foreign key (id_entity_user) references entity(id),
	id_entity_group bigint(20) unsigned,
	foreign key (id_entity_group) references entity(id),
	id_entity_admin bigint(20) unsigned,
	foreign key (id_entity_admin) references entity(id),
	created timestamp,
	start datetime, end datetime,
	name char(32))		      
       engine=innodb;

create table if not exists attestation
       (id_entity_user bigint(20) unsigned,
	foreign key (id_entity_user) references entity(id),
	id_entity_ra bigint(20) unsigned,
	foreign key (id_entity_ra) references entity(id),
	id_entity_ra_admin bigint(20) unsigned,
	foreign key (id_entity_ra_admin) references entity(id),
	created timestamp,
	start datetime, end datetime,
	comment varchar(1024),
	status varchar(64))
       engine=innodb;

create table if not exists db_update
       (version varchar(64),
	description text,
	completed timestamp)
       engine=innodb;
