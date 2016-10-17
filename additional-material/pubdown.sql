CREATE DATABASE  IF NOT EXISTS `pubdown` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `pubdown`;
-- MySQL dump 10.13  Distrib 5.7.15, for Linux (x86_64)
--
-- Host: localhost    Database: pubdown
-- ------------------------------------------------------
-- Server version	5.7.15-0ubuntu0.16.04.1

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
  `lastUpdate` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nextUpdate` varchar(255) DEFAULT NULL,
  `numberOfPapers` int(11) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `userId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkct57u0hqa9hqm13ape777xfr` (`userId`),
  CONSTRAINT `FKkct57u0hqa9hqm13ape777xfr` FOREIGN KEY (`userId`) REFERENCES `User` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Repository`
--

LOCK TABLES `Repository` WRITE;
/*!40000 ALTER TABLE `Repository` DISABLE KEYS */;
INSERT INTO `Repository` VALUES (1,'11-10-2016 11:41','repo1','',20,'repo1/example','user'),(2,'10-10-2016 11:27','repo2','',4,'repo2','user'),(3,'11-10-2016 11:37','repo3','',6,'repo3','user'),(4,'','user2repo','',0,'user2/repo','user2'),(5,'13-10-2016 10:19','repo4','',4,'repo4','user');
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
  `abstractPaper` bit(1) NOT NULL,
  `checked` bit(1) NOT NULL,
  `fulltextPaper` bit(1) NOT NULL,
  `groupBy` bit(1) NOT NULL,
  `keepPdf` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pdfToText` bit(1) NOT NULL,
  `pubmed` bit(1) NOT NULL,
  `pubmedDownloadTo` int(11) NOT NULL,
  `query` varchar(255) DEFAULT NULL,
  `running` bit(1) NOT NULL,
  `scopus` bit(1) NOT NULL,
  `scopusDownloadTo` int(11) NOT NULL,
  `repositoryId` int(11) DEFAULT NULL,
  `taskId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKem757sy1gq34ogsargs6mhyh0` (`repositoryId`),
  KEY `FKbtwsd0my7t1mwk6kjtpfw6167` (`taskId`),
  CONSTRAINT `FKbtwsd0my7t1mwk6kjtpfw6167` FOREIGN KEY (`taskId`) REFERENCES `RepositoryQueryTask` (`id`),
  CONSTRAINT `FKem757sy1gq34ogsargs6mhyh0` FOREIGN KEY (`repositoryId`) REFERENCES `Repository` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryQuery`
--

LOCK TABLES `RepositoryQuery` WRITE;
/*!40000 ALTER TABLE `RepositoryQuery` DISABLE KEYS */;
INSERT INTO `RepositoryQuery` VALUES (19,'\0','','','\0','\0','REPO2_toExecute','\0','',3,'vigo and ourense','\0','',3,2,19),(43,'','','\0','\0','\0','test1_REPO3','\0','',3,'cancer and colon','\0','',3,3,48),(46,'\0','','','\0','\0','test2','\0','',3,'cancer and colon','\0','',3,1,51),(48,'\0','','','\0','\0','REPO1_toExecute','\0','',3,'vigo and ourense','\0','',3,1,53),(49,'','','\0','\0','\0','test2','\0','',3,'vigo and ourense','\0','\0',3,2,54),(50,'\0','','','\0','\0','test3','\0','',3,'cancer','\0','',3,1,55),(51,'\0','','','\0','\0','test4','\0','\0',3,'vigo','\0','',3,1,56),(52,'\0','','','\0','\0','test5','\0','\0',3,'vigo and ourense','\0','',3,1,57),(53,'','','\0','\0','\0','test6','\0','\0',3,'vigo and ourense','\0','',3,1,58),(54,'\0','','','\0','\0','user2query','\0','',3,'vigo and ourense','\0','\0',3,4,59),(55,'\0','','','\0','\0','repo4query','\0','',3,'vigo and ourense','\0','',3,5,60);
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
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryQueryTask`
--

LOCK TABLES `RepositoryQueryTask` WRITE;
/*!40000 ALTER TABLE `RepositoryQueryTask` DISABLE KEYS */;
INSERT INTO `RepositoryQueryTask` VALUES (19,'','\0',11,26,'\0','\0','\0','\0','\0','\0'),(48,'','\0',11,36,'\0','\0','\0','\0','\0','\0'),(51,'','\0',10,8,'\0','\0','\0','\0','\0','\0'),(53,'','\0',11,26,'\0','\0','\0','\0','\0','\0'),(54,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(55,'','\0',11,41,'\0','\0','\0','\0','\0','\0'),(56,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(57,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(58,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(59,'','\0',0,0,'\0','\0','\0','\0','\0','\0'),(60,'','\0',10,19,'\0','\0','\0','\0','\0','\0');
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
  PRIMARY KEY (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES ('admin',NULL,'admin@admin.com','\0','$2a$10$zGS0bmgO5l.39ZjtI6765uNVUacuyyw4YNCKxkPoN4rvEPyNGgi6u','ADMIN'),('user','a1549163d9b16421237ec29c9bbbdf29','user@user.com','\0','$2a$10$R2IC5qcKUFfi0.95cWWL2emXgztldae/i3ExZGDAE2SazmBSTkVEO','USER'),('user2','','user2@user2.com','\0','$2a$10$U7nJQjUJqmfnPhef48/cp.kpEUDKPHmNzbPF6Xegw0Bj1J96EKPXK','USER'),('user3','modifiedUser3','user3@user3.com','\0','$2a$10$6.dfIcragr2iIfMFtJj45ecdi4cDdeCsZr6VINxYqDKE7rlfuwbRy','USER'),('user4','lalalalllaal','user4@user4.com','\0','$2a$10$UZppivaodZp1ws7Q61zRt.FPJjQN.qwILWy09ijALGSRPY4H/6QbK','USER');
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

-- Dump completed on 2016-10-17  9:21:20
