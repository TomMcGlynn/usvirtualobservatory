
CREATE TABLE tap_schema.tapjobstable(
	jobid varchar(10000) NULL ,
	jobstatus varchar(50),
	starttime bigint NULL,
	endtime bigint NULL,
	duration bigint NULL,
	destruction bigint NULL,
	owner varchar(50) NULL,
	lang varchar(50) NULL,
	query varchar(10000) NULL,
	error varchar(10000) NULL,
	adql varchar(10000) NULL,
	resultFormat varchar(100) NULL,
	request varchar(100) NULL,
	maxrec int NULL,
	runid varchar(10000) NULL,
	uploadparam varchar(10000) NULL
) ;


CREATE TABLE  tap_schema.tapusersdata(
	username varchar(100) NULL,
	submittedjobs varchar(10000) NULL,
	tableaccess varchar(50) NULL,
	tabledbname varchar(100) NULL,
	tableusername varchar(100) NULL,
	tableurl varchar(100) NULL,
	accesstoken varchar(100) NULL,
	uploadsuccess varchar(50) NULL,
	tableuploadid varchar(150) NULL
);


/*
/// This is an optional table used if you want to implement openid authentication package
CREATE TABLE tap_schema.tapuserauth(
	username varchar(100) NULL,
	accesstoken varchar(150) NULL,
	tokensecret varchar(100) NULL,
	requesttoken varchar(100) NULL
) 
*/
