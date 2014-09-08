# Sequel Pro dump
# Version 2492
# http://code.google.com/p/sequel-pro
#
# Host: 127.0.0.1 (MySQL 5.1.49-1ubuntu8.1)
# Database: vospace_beta
# Generation Time: 2011-05-26 15:24:39 -0400
# ************************************************************

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table jobs
# ------------------------------------------------------------

DROP TABLE IF EXISTS `jobs`;

CREATE TABLE `jobs` (
  `id` char(36) NOT NULL,
  `login` varchar(11) NOT NULL,
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

DROP TABLE IF EXISTS `metaproperties`;

CREATE TABLE `metaproperties` (
  `identifier` varchar(128) NOT NULL,
  `type` smallint(6) DEFAULT '0',
  `readonly` smallint(6) DEFAULT '0',
  PRIMARY KEY (`identifier`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table nodes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `nodes`;

CREATE TABLE `nodes` (
  `identifier` varchar(128) NOT NULL,
  `type` enum('NODE','DATA_NODE','LINK_NODE','CONTAINER_NODE','UNSTRUCTURED_DATA_NODE','STRUCTURED_DATA_NODE') NOT NULL DEFAULT 'NODE',
  `view` varchar(128) DEFAULT NULL,
  `status` smallint(6) DEFAULT '0',
  `owner` varchar(128) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `lastModificationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `node` text,
  PRIMARY KEY (`identifier`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table oauth_accessors
# ------------------------------------------------------------

DROP TABLE IF EXISTS `oauth_accessors`;

CREATE TABLE `oauth_accessors` (
  `request_token` varchar(32) DEFAULT NULL,
  `access_token` varchar(32) DEFAULT NULL,
  `token_secret` varchar(32) NOT NULL,
  `consumer_key` varchar(32) NOT NULL,
  `login` varchar(11) DEFAULT NULL,
  `authorized` bit(1) NOT NULL DEFAULT b'0',
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
  `callback_url` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`consumer_key`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `oauth_consumers` WRITE;
/*!40000 ALTER TABLE `oauth_consumers` DISABLE KEYS */;
INSERT INTO `oauth_consumers` (`consumer_key`,`consumer_secret`,`consumer_description`,`callback_url`)
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
  `value` varchar(256) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table users
# ------------------------------------------------------------

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `login` varchar(11) NOT NULL,
  `password` varchar(11) DEFAULT NULL,
  `storage_credentials` tinyblob,
  PRIMARY KEY (`login`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`login`,`password`,`storage_credentials`)
VALUES
	('test','test',X'7B22757365726E616D65223A22746573743A746573746572222C226170696B6579223A2274657374696E67227D');

/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;





/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
