CREATE DATABASE  IF NOT EXISTS `pubdown` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `pubdown`;
-- MySQL dump 10.13  Distrib 5.7.16, for Linux (x86_64)
--
-- Host: localhost    Database: pubdown
-- ------------------------------------------------------
-- Server version	5.7.16-0ubuntu0.16.10.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `GlobalConfiguration`
--

DROP TABLE IF EXISTS `GlobalConfiguration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GlobalConfiguration` (
  `configurationKey` varchar(255) NOT NULL,
  `configurationValue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`configurationKey`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `GlobalConfiguration`
--

LOCK TABLES `GlobalConfiguration` WRITE;
/*!40000 ALTER TABLE `GlobalConfiguration` DISABLE KEYS */;
INSERT INTO `GlobalConfiguration` VALUES ('repositoryPath','/home/lab33/pubdownTest');
/*!40000 ALTER TABLE `GlobalConfiguration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PasswordRecovery`
--

DROP TABLE IF EXISTS `PasswordRecovery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PasswordRecovery` (
  `login` varchar(255) NOT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PasswordRecovery`
--

LOCK TABLES `PasswordRecovery` WRITE;
/*!40000 ALTER TABLE `PasswordRecovery` DISABLE KEYS */;
/*!40000 ALTER TABLE `PasswordRecovery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Registration`
--

DROP TABLE IF EXISTS `Registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Registration` (
  `login` varchar(255) NOT NULL,
  `apiKey` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Registration`
--

LOCK TABLES `Registration` WRITE;
/*!40000 ALTER TABLE `Registration` DISABLE KEYS */;
/*!40000 ALTER TABLE `Registration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Repository`
--

DROP TABLE IF EXISTS `Repository`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Repository` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `lastUpdate` varchar(255) DEFAULT NULL,
  `userId` varchar(255) DEFAULT NULL,
  `numberOffilesInRepository` int(11) DEFAULT NULL,
  `abstractPaper` bit(1) NOT NULL,
  `fulltextPaper` bit(1) NOT NULL,
  `keepPdf` bit(1) NOT NULL,
  `pdfToText` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkct57u0hqa9hqm13ape777xfr` (`userId`),
  CONSTRAINT `FKkct57u0hqa9hqm13ape777xfr` FOREIGN KEY (`userId`) REFERENCES `User` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Repository`
--

LOCK TABLES `Repository` WRITE;
/*!40000 ALTER TABLE `Repository` DISABLE KEYS */;
INSERT INTO `Repository` VALUES (1,'repo1','repo1','09-11-2016 16:23','user',36,'','','','\0'),(15,'user3repo','user3repo','Never','user3',0,'\0','\0','','\0'),(16,'repo2','repo2','Never','user',0,'','','','\0'),(17,'repo3','repo3','Never','user',0,'','\0','','\0'),(18,'repo4','repo4','Never','user',0,'','','','\0');
/*!40000 ALTER TABLE `Repository` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RepositoryQuery`
--

DROP TABLE IF EXISTS `RepositoryQuery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RepositoryQuery` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `checked` bit(1) NOT NULL,
  `groupBy` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pubmed` bit(1) NOT NULL,
  `pubmedDownloadTo` int(11) NOT NULL,
  `query` varchar(255) DEFAULT NULL,
  `scheduled` bit(1) NOT NULL,
  `scopus` bit(1) NOT NULL,
  `scopusDownloadTo` int(11) NOT NULL,
  `repositoryId` int(11) DEFAULT NULL,
  `taskId` int(11) DEFAULT NULL,
  `downloadLimit` int(11) NOT NULL,
  `lastExecution` varchar(255) DEFAULT NULL,
  `executionState` varchar(255) NOT NULL,
  `nextExecution` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKem757sy1gq34ogsargs6mhyh0` (`repositoryId`),
  KEY `FKbtwsd0my7t1mwk6kjtpfw6167` (`taskId`),
  CONSTRAINT `FKbtwsd0my7t1mwk6kjtpfw6167` FOREIGN KEY (`taskId`) REFERENCES `RepositoryQueryTask` (`id`),
  CONSTRAINT `FKem757sy1gq34ogsargs6mhyh0` FOREIGN KEY (`repositoryId`) REFERENCES `Repository` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=197 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryQuery`
--

LOCK TABLES `RepositoryQuery` WRITE;
/*!40000 ALTER TABLE `RepositoryQuery` DISABLE KEYS */;
INSERT INTO `RepositoryQuery` VALUES (174,'','','test','',640,'vigo and ourense','\0','',1793,1,179,1,'09-11-2016 16:24','UNSCHEDULED','Unscheduled'),(175,'','','repo2query','',3,'vigo','\0','',3,16,180,3,'Never','UNSCHEDULED','Unscheduled'),(176,'','','test2','',3,'vigo and ourense','\0','\0',2147483647,1,181,3,'Never','UNSCHEDULED','Unscheduled'),(177,'','','test3','',3,'vigo','\0','',3,16,182,3,'Never','UNSCHEDULED','Unscheduled'),(178,'','','test4','',3,'vigo','\0','',3,1,183,3,'Never','UNSCHEDULED','Unscheduled'),(179,'','','sdas','',3,'vigo','\0','\0',2147483647,16,184,3,'Never','UNSCHEDULED','Unscheduled'),(180,'','','vigo','',3,'vigo','\0','\0',2147483647,16,185,3,'Never','UNSCHEDULED','Unscheduled'),(182,'','','testRepo3','',3,'vigo','\0','',3,17,187,3,'Never','UNSCHEDULED','Unscheduled'),(183,'','','aa','\0',2147483647,'vigo','\0','',3,16,188,3,'Never','UNSCHEDULED','Unscheduled'),(184,'','','aaa','\0',2147483647,'vigo','\0','',3,16,189,3,'Never','UNSCHEDULED','Unscheduled'),(185,'','','tasasdasd','\0',2147483647,'vigo','\0','',3,16,190,3,'Never','UNSCHEDULED','Unscheduled'),(186,'','','asdasdasd','\0',2147483647,'vigo','\0','',3,16,191,3,'Never','UNSCHEDULED','Unscheduled'),(187,'','','asdnasdnqa','',3,'vigo','\0','\0',2147483647,16,192,3,'Never','UNSCHEDULED','Unscheduled'),(188,'','','sasdasd','',3,'vigo','\0','\0',2147483647,16,193,3,'Never','UNSCHEDULED','Unscheduled'),(189,'','','testREPO4','',3,'vigo and ourense','\0','',3,18,194,3,'Never','UNSCHEDULED','Unscheduled'),(193,'','','newQuery','',3,'vigo and ourense','\0','',3,16,198,3,'Never','UNSCHEDULED','Unscheduled'),(194,'','','test3','\0',2147483647,'vigo and ourense','\0','',3,1,199,15,'Never','UNSCHEDULED','Unscheduled'),(195,'','','test2REPO4','\0',2147483647,'vigo','\0','',3,18,200,1,'Never','UNSCHEDULED','Unscheduled'),(196,'','','testfrom3to2','\0',2147483647,'vigo','\0','',3,16,201,1,'Never','UNSCHEDULED','Unscheduled');
/*!40000 ALTER TABLE `RepositoryQuery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RepositoryQueryTask`
--

DROP TABLE IF EXISTS `RepositoryQueryTask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RepositoryQueryTask` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `daily` bit(1) NOT NULL,
  `friday` bit(1) NOT NULL,
  `hour` int(11) NOT NULL,
  `minutes` int(11) NOT NULL,
  `monday` bit(1) NOT NULL,
  `saturday` bit(1) NOT NULL,
  `sunday` bit(1) NOT NULL,
  `thursday` bit(1) NOT NULL,
  `tuesday` bit(1) NOT NULL,
  `wednesday` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryQueryTask`
--

LOCK TABLES `RepositoryQueryTask` WRITE;
/*!40000 ALTER TABLE `RepositoryQueryTask` DISABLE KEYS */;
INSERT INTO `RepositoryQueryTask` VALUES (179,'\0','\0',16,23,'\0','\0','\0','','\0',''),(180,'','\0',10,53,'\0','\0','\0','\0','\0','\0'),(181,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(182,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(183,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(184,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(185,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(187,'','\0',10,54,'\0','\0','\0','\0','\0','\0'),(188,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(189,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(190,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(191,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(192,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(193,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(194,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(198,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(199,'\0','\0',0,0,'\0','\0','\0','\0','\0','\0'),(200,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(201,'','\0',0,0,'\0','\0','\0','\0','\0','\0');
/*!40000 ALTER TABLE `RepositoryQueryTask` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `login` varchar(255) NOT NULL,
  `apiKey` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `locked` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  `logged` bit(1) NOT NULL,
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES ('admin',NULL,'admin@admin.com','\0','$2a$10$zGS0bmgO5l.39ZjtI6765uNVUacuyyw4YNCKxkPoN4rvEPyNGgi6u','ADMIN','\0'),('admin2','admin2apikey','admin2@admin2.com','\0','$2a$10$6CPwUPA7i403TqaSBtvc/OE0SDNS36wyiPVR/NvuHrG6tWVIVuVoe','ADMIN','\0'),('user','a1549163d9b16421237ec29c9bbbdf29','user@user.com','\0','$2a$10$INDHxWHY6AjPfO/Kd9h9rOWr8d9HSTaZ2kkFDoQrjh34HoRVEf30e','USER',''),('user2','asdasdasdc232','user2@user2.com','\0','$2a$10$FYmvjPpzxJlWS42xFbUtKezzqSVE6D5Sf3MYbx4MuovRHoFvl9LM6','USER','\0'),('user3','a1549163d9b16421237ec29c9bbbdf29','user3@user3.com','\0','$2a$10$Ow/4sALOG2oATg6PCBJCF.XErYossf9/UYO0qxfyzHT0ZV1TnL/L6','USER',''),('user4','a','user4@user4.com','\0','$2a$10$z0BFyQ1pWdgIp8GEXWaZ/ennkj5h/V94KCOQCNeVy8nm48IlcYor2','USER','\0');
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-11-09 16:25:02
