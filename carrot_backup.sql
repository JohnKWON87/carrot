-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: carrot
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_log`
--

DROP TABLE IF EXISTS `admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_log` (
  `admin_log_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `item_id` bigint NOT NULL,
  `moderated_at` datetime(6) NOT NULL,
  `moderation_reason` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `moderation_status` enum('BLINDED','DELETED','VISIBLE') COLLATE utf8mb4_general_ci NOT NULL,
  `moderator_email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`admin_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_log`
--

LOCK TABLES `admin_log` WRITE;
/*!40000 ALTER TABLE `admin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_menu`
--

DROP TABLE IF EXISTS `admin_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `display_order` int NOT NULL,
  `enabled` bit(1) NOT NULL,
  `menu_icon` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `menu_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `menu_url` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_menu`
--

LOCK TABLES `admin_menu` WRITE;
/*!40000 ALTER TABLE `admin_menu` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `agreements`
--

DROP TABLE IF EXISTS `agreements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agreements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `buyer` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `direct_date` date DEFAULT NULL,
  `direct_time` datetime(6) DEFAULT NULL,
  `method` enum('DIRECT','SHIPPING') COLLATE utf8mb4_general_ci NOT NULL,
  `note` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `phone` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `place` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `price` int DEFAULT NULL,
  `productid` bigint DEFAULT NULL,
  `receiver` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `userid` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `agreements`
--

LOCK TABLES `agreements` WRITE;
/*!40000 ALTER TABLE `agreements` DISABLE KEYS */;
/*!40000 ALTER TABLE `agreements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_type` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `content` text COLLATE utf8mb4_general_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` int NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5vlh90qyii65ixwsbnafd55ud` (`user_id`),
  CONSTRAINT `FK5vlh90qyii65ixwsbnafd55ud` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board`
--

LOCK TABLES `board` WRITE;
/*!40000 ALTER TABLE `board` DISABLE KEYS */;
/*!40000 ALTER TABLE `board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(2000) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `file_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message_type` enum('FILE','IMAGE','PRICE_NEGOTIATION','PURCHASE_REQUEST','SYSTEM','TEXT','TRADE_CANCELLED','TRADE_COMPLETED','TRADE_CONFIRMED','TRADE_OFFER') COLLATE utf8mb4_general_ci NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `sent_at` datetime(6) NOT NULL,
  `chat_room_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbcsxusjp1v4rd8879fhvq8ssb` (`chat_room_id`),
  KEY `FKgiqeap8ays4lf684x7m0r2729` (`sender_id`),
  CONSTRAINT `FKbcsxusjp1v4rd8879fhvq8ssb` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_rooms` (`id`),
  CONSTRAINT `FKgiqeap8ays4lf684x7m0r2729` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_rooms`
--

DROP TABLE IF EXISTS `chat_rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `buyer_last_read_at` datetime(6) DEFAULT NULL,
  `buyer_unread_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `last_message` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `last_message_at` datetime(6) DEFAULT NULL,
  `seller_last_read_at` datetime(6) DEFAULT NULL,
  `seller_unread_count` int NOT NULL,
  `status` enum('ACTIVE','BLOCKED','CLOSED','COMPLETED','INACTIVE','PAUSED') COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `buyer_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhbap39tpdxxb39tf4qmovcb18` (`buyer_id`),
  KEY `FKiq1smm2max7030q1ncsli0bwy` (`item_id`),
  KEY `FKc3j4hkkph4fy04l2t23kcl8os` (`seller_id`),
  CONSTRAINT `FKc3j4hkkph4fy04l2t23kcl8os` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhbap39tpdxxb39tf4qmovcb18` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKiq1smm2max7030q1ncsli0bwy` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_rooms`
--

LOCK TABLES `chat_rooms` WRITE;
/*!40000 ALTER TABLE `chat_rooms` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `sale_item_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs31hsgaqdbdjpojnmpek8wsya` (`sale_item_id`),
  KEY `FKa2lwa7bjrnbti5v12mga2et1y` (`user_id`),
  CONSTRAINT `FKa2lwa7bjrnbti5v12mga2et1y` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKs31hsgaqdbdjpojnmpek8wsya` FOREIGN KEY (`sale_item_id`) REFERENCES `sale_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(2000) COLLATE utf8mb4_general_ci NOT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `location` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `moderation_status` enum('BLINDED','DELETED','VISIBLE') COLLATE utf8mb4_general_ci NOT NULL,
  `price` int NOT NULL,
  `sell_status` enum('BUY','RESERVED','SELL','SOLD_OUT') COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` int NOT NULL,
  `wish_count` int NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`item_id`),
  KEY `FKsm9ro5ntn6yaav2m7ydato0fc` (`seller_id`),
  CONSTRAINT `FKsm9ro5ntn6yaav2m7ydato0fc` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES (23,'electronics','2025-09-30 12:52:21.776558','상태 좋은 아이폰 13 프로입니다. 케이스와 함께 판매합니다. 배터리 성능 90% 이상, 외관 상태 양호합니다. 직거래 선호합니다.','/images/electronics/iphone13.jpg','서울 강남구','VISIBLE',850000,'SELL','아이폰 13 Pro 128GB 판매','2025-09-30 12:52:59.425510',1,0,2),(24,'electronics','2025-09-30 12:52:21.813461','거의 새 제품, 박스 포함 모든 구성품 있습니다. 스크린 보호필름 부착되어 있고, 케이스도 같이 드립니다. 급매로 내놓습니다.','/images/electronics/galaxy-s22.jpg','서울 서초구','VISIBLE',700000,'SELL','갤럭시 S22 Ultra 256GB','2025-09-30 12:52:54.320921',1,0,1),(25,'electronics','2025-09-30 12:52:21.818447','사용감 거의 없는 맥북 에어입니다. 학업용으로 가볍게 사용했습니다. 충전 사이클 50회 미만, 완전 새것과 동일합니다.','/images/electronics/macbook-air.jpg','서울 마포구','VISIBLE',1200000,'SELL','맥북 에어 M1 2021년형','2025-09-30 12:52:57.570811',1,0,2),(26,'electronics','2025-09-30 12:52:21.822435','가벼운 17인치 노트북, 업무용으로 완벽합니다. Intel i7, 16GB RAM, 512GB SSD 탑재. 키보드 및 화면 상태 우수합니다.','/images/electronics/lg-gram.jpg','경기 성남시','VISIBLE',900000,'SELL','LG 그램 17인치 노트북','2025-09-30 12:52:21.822435',0,0,1),(27,'electronics','2025-09-30 12:52:21.827422','미개봉 새상품입니다. 선물받았는데 이미 있어서 판매합니다. 정품 인증 가능하며, 영수증도 같이 드립니다.','/images/electronics/airpods-pro.jpg','서울 홍대','VISIBLE',250000,'SELL','에어팟 프로 2세대 새상품','2025-09-30 15:00:52.217060',13,0,2),(28,'electronics','2025-09-30 12:52:21.831589','태블릿과 키보드 커버 세트로 판매합니다. 거의 사용하지 않아 상태 매우 좋습니다. 동영상 시청용, 업무용으로 좋습니다.','/images/electronics/galaxy-tab.jpg','부산 해운대구','VISIBLE',450000,'SELL','삼성 갤럭시탭 S8 11인치','2025-09-30 12:53:01.219227',1,0,1),(29,'clothes','2025-09-30 12:52:21.837008','280mm 사이즈, 몇 번 신지 않은 상태입니다. 흰색 깔끔한 디자인으로 어떤 옷과도 잘 어울립니다. 박스 포함 판매합니다.','/images/clothes/nike-airforce.jpg','서울 홍대','VISIBLE',120000,'SELL','나이키 에어포스1 280mm','2025-09-30 12:52:21.837008',0,0,2),(30,'clothes','2025-09-30 12:52:21.841986','깔끔한 화이트 스탠스미스, 260mm입니다. 한 시즌 착용했지만 관리 잘 해서 상태 좋습니다. 세탁 완료된 상태입니다.','/images/clothes/adidas-stansmith.jpg','부산 해운대구','VISIBLE',80000,'SELL','아디다스 스탠스미스 260mm','2025-09-30 12:52:21.841986',0,0,1),(31,'clothes','2025-09-30 12:52:21.846993','작년 겨울에 구매한 유니클로 다운 점퍼입니다. 따뜻하고 가벼워서 실용적입니다. 드라이클리닝 완료, 보관상태 우수합니다.','/images/clothes/uniqlo-padding.jpg','서울 강남구','VISIBLE',50000,'SELL','유니클로 다운 패딩 점퍼 L사이즈','2025-09-30 12:52:21.846993',0,0,2),(32,'clothes','2025-09-30 12:52:21.851060','조던 1 하이 브레드 정품입니다. 270mm, 상태 양호합니다. 신발 관리 도구로 깨끗하게 관리했습니다. 박스와 함께 판매.','/images/clothes/jordan1.jpg','서울 용산구','VISIBLE',180000,'SELL','조던 1 하이 브레드 270mm','2025-09-30 12:59:15.920831',2,0,1),(33,'clothes','2025-09-30 12:52:21.856067','노스페이스 정품 플리스 자켓 판매합니다. 100 사이즈(L), 보온성 좋고 활동하기 편합니다. 세탁 완료, 냄새 없음.','/images/clothes/northface-fleece.jpg','경기 수원시','VISIBLE',60000,'SELL','노스페이스 플리스 자켓 100 사이즈','2025-09-30 13:00:02.773163',6,0,2),(34,'misc','2025-09-30 12:52:21.860373','이케아에서 구매한 원목 책상입니다. 스크래치 거의 없어요. 서랍 2개 있고, 조립 상태로 직거래만 가능합니다.','/images/misc/wooden-desk.jpg','서울 용산구','VISIBLE',150000,'SELL','원목 책상 1200x600','2025-09-30 15:04:23.146136',2,0,2),(35,'misc','2025-09-30 12:52:21.864554','정품 허먼밀러 에어론 의자입니다. 재택근무용으로 구매했는데 거의 사용안했어요. 등받이 조절 가능, 상태 완벽합니다.','/images/misc/herman-miller.jpg','경기 수원시','VISIBLE',800000,'SELL','허먼밀러 에어론 의자 B사이즈','2025-09-30 12:52:21.864554',0,0,1),(36,'misc','2025-09-30 12:52:21.870037','카메라 입문용으로 좋은 캐논 R50입니다. 렌즈킷으로 판매하며, 구매한 지 3개월 정도 되었습니다. 박스, 설명서 모두 있음.','/images/misc/canon-camera.jpg','서울 마포구','VISIBLE',650000,'SELL','캐논 EOS R50 미러리스 카메라','2025-09-30 12:52:21.870037',0,0,2),(37,'misc','2025-09-30 12:52:21.874543','다이슨 V11 무선청소기 판매합니다. 흡입력 좋고 무선이라 편리합니다. 브러시 헤드 여러 개 포함, 충전 거치대도 있습니다.','/images/misc/dyson-v11.jpg','서울 강남구','VISIBLE',300000,'SELL','다이슨 V11 무선청소기','2025-09-30 12:52:21.874543',0,0,1),(38,'misc','2025-09-30 12:52:21.879557','닌텐도 스위치 OLED 화이트 모델입니다. 게임팩 몇 개와 함께 판매합니다. 보호필름, 케이스 포함. 상태 매우 좋습니다.','/images/misc/nintendo-switch.jpg','부산 부산진구','VISIBLE',280000,'SELL','닌텐도 스위치 OLED 화이트','2025-09-30 14:32:44.948758',3,0,2),(39,'misc','2025-09-30 12:52:21.884530','브레빌 바리스타 익스프레스 에스프레소 머신입니다. 집에서 카페 퀄리티의 커피를 즐길 수 있습니다. 사용법 설명해드려요.','/images/misc/breville-espresso.jpg','서울 서초구','VISIBLE',450000,'SELL','브레빌 에스프레소 머신','2025-09-30 12:52:21.884530',0,0,1);
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_request`
--

DROP TABLE IF EXISTS `purchase_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `image_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `price` int NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `requester_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiblco0tepdx0lhsxgvewdiqnb` (`requester_id`),
  CONSTRAINT `FKiblco0tepdx0lhsxgvewdiqnb` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_request`
--

LOCK TABLES `purchase_request` WRITE;
/*!40000 ALTER TABLE `purchase_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `purchase_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_requests`
--

DROP TABLE IF EXISTS `purchase_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `offered_price` int DEFAULT NULL,
  `preferred_location` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `preferred_time` datetime(6) DEFAULT NULL,
  `priority` int NOT NULL,
  `request_message` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `responded_at` datetime(6) DEFAULT NULL,
  `response_message` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` enum('APPROVED','CANCELLED','COMPLETED','EXPIRED','IN_PROGRESS','PENDING','REJECTED') COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `buyer_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK809sqf7t17nub6pklvvlu3ko6` (`buyer_id`),
  KEY `FKl6mt6l8q4gr6wi5h9dn5q4wb2` (`item_id`),
  KEY `FKdeecxdi97i219k6wtb1jnaefh` (`seller_id`),
  CONSTRAINT `FK809sqf7t17nub6pklvvlu3ko6` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKdeecxdi97i219k6wtb1jnaefh` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl6mt6l8q4gr6wi5h9dn5q4wb2` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_requests`
--

LOCK TABLES `purchase_requests` WRITE;
/*!40000 ALTER TABLE `purchase_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `purchase_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sale_item`
--

DROP TABLE IF EXISTS `sale_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sale_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `image_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `price` int NOT NULL,
  `status` enum('FOR_SALE','PURCHASED','RESERVED','SOLD','WANTED') COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `seller_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbv01aedca6n3ajgnpwm7m126v` (`seller_id`),
  CONSTRAINT `FKbv01aedca6n3ajgnpwm7m126v` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sale_item`
--

LOCK TABLES `sale_item` WRITE;
/*!40000 ALTER TABLE `sale_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `sale_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `role` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2025-09-26 09:58:27.831144','admin@carrot.com',_binary '','관리자','$2a$10$j6jEokvL5B144jNthdCTh.K9Cqog0gsFzRiajY6o/al/fSUyUzeUC','010-0000-0000','ADMIN','2025-09-26 09:58:27.831144','admin',NULL),(2,'2025-09-26 09:58:27.950095','test@carrot.com',_binary '','테스트사용자','$2a$10$Wb.6VtIDNT.kW7sjYQUHf.M3sxi52xx5/4bSwdABzLy4jY6QwQPTi','010-1111-1111','USER','2025-09-26 09:58:27.950095','testuser',NULL),(3,'2025-09-26 09:58:59.307344','kim@aa.com',_binary '','권혁민','$2a$10$Izjpd9KxL3mcDBIhYF.NQOkLfO.gy5ut3TlC2mZVBfB46TZwKL/hS','010-1234-5678','USER','2025-09-26 09:58:59.307344','johnkwon',NULL),(4,'2025-09-26 10:47:28.153688','a@a.com',_binary '','아자르','$2a$10$O2VCGdj3pPG5WQU9jnPWBON5M6wnTEthLpDHVoViEcug2FyVsEDqG','010-1234-1234','USER','2025-09-26 10:47:28.153688','aaa111',NULL),(5,'2025-09-29 10:06:38.133822','123@aa.com',_binary '','이용자','$2a$10$Oo55BDl5BBw4gOFvG7gvaenC9kFzGJl1FlG/oQu.2ODfYT8nn/60W','010-9988-7766','USER','2025-09-29 12:56:38.194326','qwerty',NULL),(6,'2025-09-29 13:00:18.743480','133323@aa.com',_binary '','이용자','$2a$10$PMqy1QY7QZmlaVcMnMdg7.ii9EwVsOUo3cuB1rgtGH4avce6nu9KC','010-5555-8888','USER','2025-09-29 13:00:18.743480','qwe123',NULL),(7,'2025-09-29 15:05:30.260217','skh51779@naver.com',_binary '','손경훈','$2a$10$7jI4XnpnERddTRjb1v1Dfu1gcSkcIXmqJW37YChLbLaC3ONB8sglm','010-2001-0270','USER','2025-09-29 15:05:30.260217','skhskh',NULL),(8,'2025-09-30 09:37:08.998953','aaaa@123.com',_binary '','당근이','$2a$10$S9FVkZJFZtTgL1uMa1F9XOtUHJedOrn5Rtu8j2hljSSbC57tHMRBS','010-1234-5698','USER','2025-09-30 09:37:08.998953','aaa123',NULL),(9,'2025-09-30 09:58:57.512844','jak@dd.coms',_binary '','박길도','$2a$10$yXDufD0o/qpslPll57lpP.6WDVK2gjklo/t7O4N4OBOVr80qK3W3i','010-1237-8765','USER','2025-09-30 09:58:57.512844','user',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wanted_items`
--

DROP TABLE IF EXISTS `wanted_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wanted_items` (
  `wanted_item_id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(2000) COLLATE utf8mb4_general_ci NOT NULL,
  `interest_count` int NOT NULL,
  `location` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `max_price` int NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` int NOT NULL,
  `wanted_status` enum('ACTIVE','CANCELLED','MATCHED') COLLATE utf8mb4_general_ci NOT NULL,
  `buyer_id` bigint NOT NULL,
  PRIMARY KEY (`wanted_item_id`),
  KEY `FKqr6q3pgdxumed8avp1n0vwsno` (`buyer_id`),
  CONSTRAINT `FKqr6q3pgdxumed8avp1n0vwsno` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wanted_items`
--

LOCK TABLES `wanted_items` WRITE;
/*!40000 ALTER TABLE `wanted_items` DISABLE KEYS */;
INSERT INTO `wanted_items` VALUES (1,'electronics','2025-09-30 12:57:35.848208','아이폰 14 Pro 256GB 이상을 찾고 있습니다.\n- 색상: 딥퍼플 또는 스페이스블랙 선호\n- 배터리 성능 85% 이상\n- 외관 상태 양호한 제품\n- 박스 및 액세서리 포함 시 우대',0,'서울 강남구',1000000,'아이폰 14 Pro 구매희망합니다','2025-09-30 14:36:54.394324',1,'ACTIVE',2),(2,'electronics','2025-09-30 12:57:35.878118','갤럭시탭 S8 11인치 WiFi 모델을 구매하고 싶습니다.\n- 128GB 또는 256GB\n- 키보드 커버 포함 시 추가 금액 지불 가능\n- 강남, 서초 지역 직거래 선호',0,'서울 서초구',400000,'갤럭시탭 S8 구매합니다','2025-09-30 12:57:35.878118',0,'ACTIVE',1),(3,'electronics','2025-09-30 12:57:35.883104','맥북 에어 M2 2022년형을 구매하려고 합니다.\n- 8GB RAM / 256GB SSD 이상\n- 실버 또는 스페이스그레이\n- 사용감 적은 제품 우대\n- AppleCare+ 남아있으면 더욱 좋습니다',0,'경기 성남시',1300000,'맥북 에어 M2 찾습니다','2025-09-30 14:36:56.368620',1,'ACTIVE',2),(4,'clothes','2025-09-30 12:57:35.887092','나이키 에어조던 1 하이 또는 로우를 찾고 있습니다.\n- 사이즈: 270mm\n- 색상: 브레드, 시카고, 로얄 선호\n- 상태: 8/10 이상\n- 정품 인증서 또는 영수증 있으면 좋겠습니다',0,'서울 홍대',200000,'나이키 조던 1 구매희망','2025-09-30 15:01:15.060173',2,'ACTIVE',2),(5,'clothes','2025-09-30 12:57:35.892079','유니클로 울트라라이트다운 재킷을 구매하고 싶습니다.\n- 사이즈: L (100)\n- 색상: 블랙, 네이비, 그레이\n- 작년 또는 올해 모델\n- 세탁 완료된 깨끗한 상태',0,'부산 해운대구',40000,'유니클로 다운패딩 구매','2025-09-30 14:36:58.456191',1,'ACTIVE',1),(6,'clothes','2025-09-30 12:57:35.896068','아디다스 스탠스미스 화이트/그린을 찾습니다.\n- 사이즈: 265mm\n- 상태: 상급 이상 (발가락 부분 변색 없는 것)\n- 박스 있으면 우대\n- 서울 전지역 직거래 가능',0,'서울 용산구',70000,'아디다스 스탠스미스 흰색','2025-09-30 12:59:31.449785',1,'ACTIVE',2),(7,'misc','2025-09-30 12:57:35.901056','이케아 책상을 구매하고 싶습니다.\n- 크기: 120x60cm 이상\n- 높이 조절 가능한 것 우대\n- 서랍 포함된 모델 선호\n- 원목 또는 화이트 색상\n- 수원, 용인 지역 직거래만 가능',0,'경기 수원시',100000,'이케아 책상 구매희망','2025-09-30 15:01:17.185111',2,'ACTIVE',2),(8,'misc','2025-09-30 12:57:35.906045','다이슨 무선청소기 V10 이상 모델을 구매하려고 합니다.\n- 배터리 수명 양호한 제품\n- 브러시 헤드 여러 개 포함\n- 충전 거치대 포함 필수\n- A/S 가능한 정품만',0,'서울 마포구',250000,'다이슨 무선청소기 찾습니다','2025-09-30 14:08:39.461084',1,'ACTIVE',1),(9,'misc','2025-09-30 12:57:35.911030','캐논 DSLR 카메라를 구매하고 싶습니다.\n- 기종: 80D, 90D 또는 6D Mark II\n- 렌즈킷 포함 (18-55mm 또는 24-105mm)\n- 셔터 수 5만 이하\n- 메모리카드, 가방 등 액세서리 포함 시 우대',0,'부산 부산진구',600000,'캐논 카메라 DSLR 구매','2025-09-30 12:57:35.911030',0,'ACTIVE',2),(10,'misc','2025-09-30 12:59:09.867975','hjhgjghjjhjhgjhgj',0,'부산 부산진구',280000,'구매희망: 닌텐도 스위치 OLED 화이트','2025-09-30 14:36:46.170559',4,'ACTIVE',3),(11,'electronics','2025-09-30 14:08:31.716907','45435435435',0,'서울 홍대',545435,'구매희망: 에어팟 프로 2세대 새상품','2025-09-30 14:12:12.434393',1,'ACTIVE',3);
/*!40000 ALTER TABLE `wanted_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist`
--

DROP TABLE IF EXISTS `wishlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist` (
  `wishlist_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `item_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`wishlist_id`),
  UNIQUE KEY `UKnauy4jq9k27h9c4de8c9t5pap` (`user_id`,`item_id`),
  KEY `FKic49brbn286b9avhgsidyn3c5` (`item_id`),
  CONSTRAINT `FKic49brbn286b9avhgsidyn3c5` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `FKtrd6335blsefl2gxpb8lr0gr7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist`
--

LOCK TABLES `wishlist` WRITE;
/*!40000 ALTER TABLE `wishlist` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-30 15:19:37
