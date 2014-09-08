-- For use with MySQL Database
-- Schema for Purse User Management Database

-- RA Table
create table ra_table
(
        ra_id int AUTO_INCREMENT,
        ra_name varchar(100) not null,
        ra_email varchar(100) not null,
        ra_description text not null,
	constraint ra_pk_cons primary key(ra_id),
	constraint ra_uk_cons_name unique(ra_name),
	constraint ra_uk_cons_email unique(ra_email)
);

-- Status Table
create table status_table
(
	status_id int AUTO_INCREMENT,
	status_name varchar(100) not null,
	status_description text not null,
	constraint status_uk_cons unique(status_name),
	constraint status_pk_cons primary key(status_id)
);

-- Role Table
create table role_table
(
	role_id int AUTO_INCREMENT,
	role_name varchar(100) not null,
	role_description text not null,
	constraint role_uk_cons unique(role_name),
	constraint role_pk_cons primary key(role_id)
);

-- User Table
create table user_table
(
	user_id int AUTO_INCREMENT,
	token varchar(200) not null,
	first_name text not null,
	last_name text not null,
	contact_person text not null,
	stmt_of_work text not null,
	user_name varchar(200) not null,
	password_sha1 blob not null,
	institution text not null,
	project_name text not null,
	email text not null,
	phone text not null,
	country text,
	dn text,
	status_id int not null,
	creation_time date,
	number_logins int,
	last_access_time date,
	portal_confirm_url text,
        ra_id int,
	constraint status_cons foreign key(status_id) references status_table,
	constraint username_uk_cons unique(user_name),
	constraint token_uk_cons unique(token),
	constraint user_pk_cons primary key(user_id),
        constraint ra_cons foreign key(ra_id) references ra_table
);

-- User groups
create table group_table
(
	group_id int AUTO_INCREMENT,
	group_name varchar(200) not null,
	group_desc text not null,
	constraint group_uk_cons unique(group_name),
	constraint gp_pk primary key(group_id)
);

-- User/Group
create table user_group_table
(
	group_id int not null,
	user_id int not null,
	constraint foreign1 foreign key(group_id) references group_table,
	constraint foreign2 foreign key(user_id) references user_table,
	constraint user_gp_pk primary key(group_id, user_id)
);

-- User, roles association
create table user_roles_table
(
	user_id int not null,
	role_id int not null,
	constraint user_cons foreign key(user_id) references user_table,
	constraint role_cons foreign key(role_id) references role_table,
	constraint pk_cons primary key(role_id, user_id)
);
