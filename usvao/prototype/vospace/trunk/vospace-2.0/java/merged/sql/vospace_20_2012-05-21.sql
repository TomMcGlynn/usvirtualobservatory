# ************************************************************
# Sequel Pro SQL dump
# Version 3408
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: zinc27.pha.jhu.edu (MySQL 5.1.61)
# Database: vospace_20
# Generation Time: 2012-05-21 21:36:59 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table cont_loc
# ------------------------------------------------------------

CREATE TABLE `cont_loc` (
  `container` varchar(128) NOT NULL DEFAULT '',
  `owner` varchar(128) NOT NULL DEFAULT '',
  `region` varchar(36) NOT NULL DEFAULT '',
  `syncregion` varchar(36) DEFAULT '',
  `syncurl` varchar(256) DEFAULT NULL,
  `synckey` char(32) NOT NULL DEFAULT ''
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table jobs
# ------------------------------------------------------------

CREATE TABLE `jobs` (
  `id` char(36) NOT NULL,
  `login` varchar(30) NOT NULL,
  `starttime` datetime DEFAULT NULL,
  `endtime` datetime DEFAULT NULL,
  `state` enum('PENDING','RUN','COMPLETED','ERROR') NOT NULL,
  `direction` enum('PULLFROMVOSPACE','PULLTOVOSPACE','PUSHFROMVOSPACE','PUSHTOVOSPACE','LOCAL') NOT NULL,
  `target` text,
  `json_notation` text NOT NULL,
  `note` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table metaproperties
# ------------------------------------------------------------

CREATE TABLE `metaproperties` (
  `identifier` varchar(128) NOT NULL,
  `type` smallint(6) DEFAULT '0',
  `readonly` smallint(6) DEFAULT '0',
  PRIMARY KEY (`identifier`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table nodes
# ------------------------------------------------------------

CREATE TABLE `nodes` (
  `container` varchar(128) NOT NULL DEFAULT '',
  `path` varchar(128) NOT NULL DEFAULT '',
  `type` enum('NODE','DATA_NODE','LINK_NODE','CONTAINER_NODE','UNSTRUCTURED_DATA_NODE','STRUCTURED_DATA_NODE') NOT NULL DEFAULT 'NODE',
  `current_rev` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `rev` int(32) unsigned NOT NULL DEFAULT '0',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `owner` varchar(128) NOT NULL DEFAULT '',
  `mtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `size` bigint(20) NOT NULL DEFAULT '0',
  `mimetype` varchar(30) NOT NULL DEFAULT '',
  `node` text,
  PRIMARY KEY (`container`,`path`,`owner`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table oauth_accessors
# ------------------------------------------------------------

CREATE TABLE `oauth_accessors` (
  `request_token` varchar(32) DEFAULT NULL,
  `access_token` varchar(32) DEFAULT NULL,
  `token_secret` varchar(32) NOT NULL,
  `consumer_key` varchar(32) NOT NULL,
  `login` varchar(30) DEFAULT NULL,
  `authorized` bit(1) NOT NULL DEFAULT b'0',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `request_token` (`request_token`),
  UNIQUE KEY `access_token` (`access_token`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table oauth_consumers
# ------------------------------------------------------------

CREATE TABLE `oauth_consumers` (
  `consumer_key` varchar(32) NOT NULL,
  `consumer_secret` varchar(32) NOT NULL,
  `consumer_description` varchar(256) NOT NULL,
  `callback_url` varchar(256) DEFAULT NULL,
  `container` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`consumer_key`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `oauth_consumers` WRITE;
/*!40000 ALTER TABLE `oauth_consumers` DISABLE KEYS */;

INSERT INTO `oauth_consumers` (`consumer_key`, `consumer_secret`, `consumer_description`, `callback_url`, `container`)
VALUES
	('sclient','ssecret','sample client',NULL,NULL),
	('vosync','vosync_ssecret','vosync app',NULL,'vosync');

/*!40000 ALTER TABLE `oauth_consumers` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table properties
# ------------------------------------------------------------

CREATE TABLE `properties` (
  `identifier` varchar(128) NOT NULL,
  `property` varchar(128) NOT NULL,
  `value` varchar(256) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table regions
# ------------------------------------------------------------

CREATE TABLE `regions` (
  `id` varchar(36) NOT NULL DEFAULT '',
  `url` varchar(256) NOT NULL DEFAULT '',
  `prefix` varchar(128) NOT NULL DEFAULT 'edu.jhu',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `regions` WRITE;
/*!40000 ALTER TABLE `regions` DISABLE KEYS */;

INSERT INTO `regions` (`id`, `url`, `prefix`)
VALUES
	('zinc27.pha.jhu.edu','http://zinc27.pha.jhu.edu/vospace-2.0','edu.jhu'),
	('tempsdss.pha.jhu.edu','http://tempsdss.pha.jhu.edu/vospace-2.0','edu.jhu'),
	('dimm.pha.jhu.edu','http://dimm.pha.jhu.edu:8081/vospace-2.0','edu.jhu'),
	('dimm2.pha.jhu.edu','http://dimm.pha.jhu.edu:8080/vospace-2.0','edu.jhu');

/*!40000 ALTER TABLE `regions` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table storage_users_pool
# ------------------------------------------------------------

CREATE TABLE `storage_users_pool` (
  `username` varchar(60) NOT NULL DEFAULT '',
  `apikey` varchar(60) DEFAULT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  `using_user` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table users
# ------------------------------------------------------------

CREATE TABLE `users` (
  `login` varchar(30) NOT NULL,
  `password` varchar(200) DEFAULT NULL,
  `storage_credentials` tinyblob,
  `certificate` blob,
  `certificate_expiration` datetime DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `softlimit` bigint(64) unsigned NOT NULL DEFAULT '104857600',
  `hardlimit` bigint(64) unsigned NOT NULL DEFAULT '115343360',
  PRIMARY KEY (`login`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
