# ************************************************************
# Sequel Pro SQL dump
# Version 3348
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: tempsdss.pha.jhu.edu (MySQL 5.0.77)
# Database: vospace_20
# Generation Time: 2011-08-18 09:24:26 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table jobs
# ------------------------------------------------------------

DROP TABLE IF EXISTS `jobs`;

CREATE TABLE `jobs` (
  `id` char(36) NOT NULL,
  `login` varchar(30) NOT NULL,
  `starttime` datetime default NULL,
  `endtime` datetime default NULL,
  `state` enum('PENDING','RUN','COMPLETED','ERROR') NOT NULL,
  `direction` enum('PULLFROMVOSPACE','PULLTOVOSPACE','PUSHFROMVOSPACE','PUSHTOVOSPACE','LOCAL') NOT NULL,
  `target` text,
  `json_notation` text NOT NULL,
  `note` text,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table metaproperties
# ------------------------------------------------------------

DROP TABLE IF EXISTS `metaproperties`;

CREATE TABLE `metaproperties` (
  `identifier` varchar(128) NOT NULL,
  `type` smallint(6) default '0',
  `readonly` smallint(6) default '0',
  PRIMARY KEY  (`identifier`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table nodes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `nodes`;

CREATE TABLE `nodes` (
  `identifier` varchar(128) NOT NULL,
  `type` enum('NODE','DATA_NODE','LINK_NODE','CONTAINER_NODE','UNSTRUCTURED_DATA_NODE','STRUCTURED_DATA_NODE') NOT NULL default 'NODE',
  `view` varchar(128) default NULL,
  `status` smallint(6) default '0',
  `owner` varchar(128) default NULL,
  `creationDate` datetime default NULL,
  `lastModificationDate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `node` text,
  PRIMARY KEY  (`identifier`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table oauth_accessors
# ------------------------------------------------------------

DROP TABLE IF EXISTS `oauth_accessors`;

CREATE TABLE `oauth_accessors` (
  `request_token` varchar(32) default NULL,
  `access_token` varchar(32) default NULL,
  `token_secret` varchar(32) NOT NULL,
  `consumer_key` varchar(32) NOT NULL,
  `login` varchar(30) default NULL,
  `authorized` bit(1) NOT NULL default b'0',
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP,
  UNIQUE KEY `request_token` (`request_token`),
  UNIQUE KEY `access_token` (`access_token`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table oauth_consumers
# ------------------------------------------------------------

DROP TABLE IF EXISTS `oauth_consumers`;

CREATE TABLE `oauth_consumers` (
  `consumer_key` varchar(32) NOT NULL,
  `consumer_secret` varchar(32) NOT NULL,
  `consumer_description` varchar(256) NOT NULL,
  `callback_url` varchar(256) default NULL,
  PRIMARY KEY  (`consumer_key`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `oauth_consumers` WRITE;
/*!40000 ALTER TABLE `oauth_consumers` DISABLE KEYS */;

INSERT INTO `oauth_consumers` (`consumer_key`, `consumer_secret`, `consumer_description`, `callback_url`)
VALUES
	('sclient','ssecret','sample client',NULL);

/*!40000 ALTER TABLE `oauth_consumers` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table properties
# ------------------------------------------------------------

DROP TABLE IF EXISTS `properties`;

CREATE TABLE `properties` (
  `identifier` varchar(128) NOT NULL,
  `property` varchar(128) NOT NULL,
  `value` varchar(256) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table storage_users_pool
# ------------------------------------------------------------

DROP TABLE IF EXISTS `storage_users_pool`;

CREATE TABLE `storage_users_pool` (
  `username` varchar(60) NOT NULL default '',
  `apikey` varchar(60) default NULL,
  `used` tinyint(1) NOT NULL default '0',
  `using_user` varchar(30) default NULL,
  PRIMARY KEY  (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table users
# ------------------------------------------------------------

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `login` varchar(30) NOT NULL,
  `password` varchar(200) default NULL,
  `storage_credentials` tinyblob,
  `certificate` blob,
  `certificate_expiration` datetime default NULL,
  `created` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `softlimit` bigint(64) unsigned NOT NULL default '104857600',
  `hardlimit` bigint(64) unsigned NOT NULL default '115343360',
  PRIMARY KEY  (`login`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
