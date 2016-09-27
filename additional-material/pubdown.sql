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
-- Table structure for table `RepositoryQuery`
--

DROP TABLE IF EXISTS `RepositoryQuery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RepositoryQuery` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `abstractPaper` bit(1) NOT NULL,
  `directory` varchar(255) DEFAULT NULL,
  `fulltextPaper` bit(1) NOT NULL,
  `groupBy` bit(1) NOT NULL,
  `keepPdf` bit(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pdfToText` bit(1) NOT NULL,
  `pubmed` bit(1) NOT NULL,
  `pubmedDownloadTo` int(11) NOT NULL,
  `query` varchar(255) DEFAULT NULL,
  `repository` varchar(255) DEFAULT NULL,
  `scopus` bit(1) NOT NULL,
  `scopusDownloadTo` int(11) NOT NULL,
  `userId` varchar(255) DEFAULT NULL,
  `daily` bit(1) NOT NULL,
  `taskId` int(11) DEFAULT NULL,
  `checked` bit(1) NOT NULL,
  `running` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7d22y06c6y9dlxi01al8de97g` (`userId`),
  KEY `FKbkd5cttod6bfi7u1djhe90er2` (`taskId`),
  CONSTRAINT `FK7d22y06c6y9dlxi01al8de97g` FOREIGN KEY (`userId`) REFERENCES `User` (`login`),
  CONSTRAINT `FKbkd5cttod6bfi7u1djhe90er2` FOREIGN KEY (`taskId`) REFERENCES `Task` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RepositoryQuery`
--

LOCK TABLES `RepositoryQuery` WRITE;
/*!40000 ALTER TABLE `RepositoryQuery` DISABLE KEYS */;
INSERT INTO `RepositoryQuery` VALUES (11,'','user/rep1/','','\0','\0','test1','\0','',2147483647,'vigo and ourense','rep1','',2147483647,'user','',1,'\0','\0'),(12,'','user/rep1/','','\0','\0','test2','\0','',3,'cancer and vigo','rep1','\0',3,'user','\0',2,'\0','\0'),(19,'\0','user/rep1/','','\0','\0','test3','\0','\0',2147483647,'vigo and ourense','rep1','\0',3,'user','',8,'\0','\0'),(22,'\0','user/rep2/','','\0','\0','test5','\0','',2147483647,'vigo and ourense','rep2','\0',2147483647,'user','\0',11,'\0','\0'),(23,'\0','user/rep2/','','\0','\0','test6','','',2147483647,'vigo and cancer','rep2','\0',2147483647,'user','\0',12,'\0','\0'),(24,'','user/rep1/','\0','\0','\0','test4','\0','',3,'vigo and ourense','rep1','\0',2147483647,'user','',13,'\0','\0');
/*!40000 ALTER TABLE `RepositoryQuery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RobotExecution`
--

DROP TABLE IF EXISTS `RobotExecution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RobotExecution` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `result` longtext,
  `robotName` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `userLogin` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RobotExecution`
--

LOCK TABLES `RobotExecution` WRITE;
/*!40000 ALTER TABLE `RobotExecution` DISABLE KEYS */;
/*!40000 ALTER TABLE `RobotExecution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Task`
--

DROP TABLE IF EXISTS `Task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hour` int(11) NOT NULL,
  `minutes` int(11) NOT NULL,
  `monday` bit(1) NOT NULL,
  `tuesday` bit(1) NOT NULL,
  `wednesday` bit(1) NOT NULL,
  `thursday` bit(1) NOT NULL,
  `friday` bit(1) NOT NULL,
  `saturday` bit(1) NOT NULL,
  `sunday` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Task`
--

LOCK TABLES `Task` WRITE;
/*!40000 ALTER TABLE `Task` DISABLE KEYS */;
INSERT INTO `Task` VALUES (1,13,23,'\0','\0','','\0','\0','\0','\0'),(2,12,13,'\0','\0','','\0','\0','\0','\0'),(8,12,43,'','','','\0','\0','\0','\0'),(10,0,0,'\0','\0','\0','\0','\0','\0','\0'),(11,21,55,'\0','','','\0','\0','\0',''),(12,1,1,'','','','','','',''),(13,12,0,'\0','\0','\0','\0','\0','\0','\0');
/*!40000 ALTER TABLE `Task` ENABLE KEYS */;
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
INSERT INTO `User` VALUES ('user','a1549163d9b16421237ec29c9bbbdf29','user@user.com','\0','$2a$10$R2IC5qcKUFfi0.95cWWL2emXgztldae/i3ExZGDAE2SazmBSTkVEO','USER'),('user2','a1549163d9b16421237ec29c9bbbdf29','user2@user2.com','\0','$2a$10$U7nJQjUJqmfnPhef48/cp.kpEUDKPHmNzbPF6Xegw0Bj1J96EKPXK','USER'),('user3','modifiedUser3','user3@user3.com','\0','$2a$10$6.dfIcragr2iIfMFtJj45ecdi4cDdeCsZr6VINxYqDKE7rlfuwbRy','USER'),('user4','lalalalllaal','user4@user4.com','\0','$2a$10$UZppivaodZp1ws7Q61zRt.FPJjQN.qwILWy09ijALGSRPY4H/6QbK','USER');
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

-- Dump completed on 2016-09-27 10:31:20
