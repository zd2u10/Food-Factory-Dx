-- MySQL dump 10.13  Distrib 8.0.25, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: food_factory_dx
-- ------------------------------------------------------
-- Server version	8.0.23

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
-- Table structure for table `batch_material_usage`
--

DROP TABLE IF EXISTS `batch_material_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch_material_usage` (
  `usage_id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL,
  `material_lot_id` bigint NOT NULL,
  `suggested_qty` decimal(10,2) NOT NULL COMMENT 'FEFO計算による理論値(プレースホルダー)',
  `used_qty` decimal(10,2) NOT NULL COMMENT '作業員が実際に入力した実測値',
  `usage_type` enum('CONSUMPTION','DISPOSAL') NOT NULL DEFAULT 'CONSUMPTION',
  `comment` varchar(255) DEFAULT NULL COMMENT '廃棄の場合の理由等',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`usage_id`),
  KEY `fk_bmu_batch` (`batch_id`),
  KEY `fk_bmu_lot` (`material_lot_id`),
  CONSTRAINT `fk_bmu_batch` FOREIGN KEY (`batch_id`) REFERENCES `manufacturing_batch` (`batch_id`),
  CONSTRAINT `fk_bmu_lot` FOREIGN KEY (`material_lot_id`) REFERENCES `material_lot` (`lot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch_material_usage`
--

LOCK TABLES `batch_material_usage` WRITE;
/*!40000 ALTER TABLE `batch_material_usage` DISABLE KEYS */;
INSERT INTO `batch_material_usage` VALUES (1,1,1,15000.00,15000.00,'CONSUMPTION',NULL,'2026-07-28 00:13:53','2026-07-28 00:13:53'),(2,2,3,15000.00,15000.00,'CONSUMPTION',NULL,'2026-07-29 00:37:43','2026-07-29 00:37:43');
/*!40000 ALTER TABLE `batch_material_usage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carrier`
--

DROP TABLE IF EXISTS `carrier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrier` (
  `carrier_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`carrier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrier`
--

LOCK TABLES `carrier` WRITE;
/*!40000 ALTER TABLE `carrier` DISABLE KEYS */;
INSERT INTO `carrier` VALUES (1,'配送会社A','2026-07-29 00:18:03','2026-07-29 00:18:03');
/*!40000 ALTER TABLE `carrier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `customer_type` enum('B2B','B2C') NOT NULL,
  `required_residual_ratio` decimal(4,3) DEFAULT NULL COMMENT '出荷時に必要な残存期限の割合(0〜1)。指定なしはNULL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'大手スーパーA','B2B',0.666,'2026-07-29 00:17:37','2026-07-29 00:17:37'),(2,'超厳格取引先','B2B',0.990,'2026-07-29 00:34:12','2026-07-29 00:34:12');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_order`
--

DROP TABLE IF EXISTS `customer_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `order_date` date NOT NULL,
  `desired_delivery_date` date DEFAULT NULL,
  `status` enum('NEW','CONFIRMED','PARTIALLY_SHIPPED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'NEW',
  `external_order_no` varchar(100) DEFAULT NULL COMMENT '先方の注文システム上の番号(あれば)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `fk_co_customer` (`customer_id`),
  CONSTRAINT `fk_co_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_order`
--

LOCK TABLES `customer_order` WRITE;
/*!40000 ALTER TABLE `customer_order` DISABLE KEYS */;
INSERT INTO `customer_order` VALUES (1,1,'2026-07-29','2026-08-02','COMPLETED',NULL,'2026-07-29 00:18:42','2026-07-29 00:31:51'),(2,2,'2026-07-29',NULL,'COMPLETED',NULL,'2026-07-29 00:35:09','2026-07-29 00:42:15'),(3,1,'2026-07-29',NULL,'NEW',NULL,'2026-07-29 00:39:42','2026-07-29 00:39:42'),(4,1,'2026-07-29',NULL,'CANCELLED',NULL,'2026-07-29 01:01:26','2026-07-29 01:02:10'),(5,1,'2026-08-01',NULL,'NEW',NULL,'2026-08-03 03:25:17','2026-08-03 03:25:17');
/*!40000 ALTER TABLE `customer_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hold_resolution`
--

DROP TABLE IF EXISTS `hold_resolution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hold_resolution` (
  `hold_id` bigint NOT NULL AUTO_INCREMENT,
  `line_id` bigint NOT NULL COMMENT '保留が発生した元の入荷明細',
  `held_qty_snapshot` decimal(10,2) NOT NULL COMMENT '保留発生時点の保留数量のスナップショット(元明細のheld_qtyが後で0に書き換わっても追跡できる)',
  `resolution_type` enum('RETURNED','EXCHANGED','ACCEPTED_LATE') DEFAULT NULL COMMENT '対応方針。返品/交換/結局受け入れ。未確定の間はNULL',
  `resolved_line_id` bigint DEFAULT NULL COMMENT '交換品として新規登録された入荷明細(EXCHANGEDの場合のみ)',
  `status` enum('ON_HOLD','RESOLVED') NOT NULL DEFAULT 'ON_HOLD',
  `comment` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`hold_id`),
  KEY `fk_hr_line` (`line_id`),
  KEY `fk_hr_resolved_line` (`resolved_line_id`),
  CONSTRAINT `fk_hr_line` FOREIGN KEY (`line_id`) REFERENCES `material_arrival_line` (`line_id`),
  CONSTRAINT `fk_hr_resolved_line` FOREIGN KEY (`resolved_line_id`) REFERENCES `material_arrival_line` (`line_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hold_resolution`
--

LOCK TABLES `hold_resolution` WRITE;
/*!40000 ALTER TABLE `hold_resolution` DISABLE KEYS */;
INSERT INTO `hold_resolution` VALUES (1,2,15000.00,'ACCEPTED_LATE',NULL,'RESOLVED','再検査の結果、異物混入は誤検知と判明。受け入れ可能と判断','2026-07-28 00:15:44','2026-07-28 00:17:09'),(2,3,15000.00,'RETURNED',NULL,'RESOLVED','箱破損のため仕入先に返品、交換なし','2026-07-28 00:27:01','2026-07-28 00:27:36'),(3,4,15000.00,'EXCHANGED',5,'RESOLVED','交換品(line_id=5)を受け入れて対応','2026-07-28 00:35:49','2026-07-28 00:38:14'),(4,6,15000.00,'RETURNED',NULL,'RESOLVED','破損のため仕入先に返品、交換なし','2026-07-28 00:43:11','2026-07-28 00:44:50');
/*!40000 ALTER TABLE `hold_resolution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '商品名',
  `safety_stock_qty` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '適正在庫',
  `target_stock_qty` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '目標在庫(将来拡張用)',
  `standard_batch_qty` decimal(10,2) NOT NULL COMMENT '1バッチあたりの標準製造数(季節変動込み平均値)',
  `shelf_life_days` int NOT NULL DEFAULT '90' COMMENT '賞味期限日数',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `items`
--

LOCK TABLES `items` WRITE;
/*!40000 ALTER TABLE `items` DISABLE KEYS */;
INSERT INTO `items` VALUES (1,'うどん',500.00,500.00,198.00,90,'2026-07-28 00:08:54','2026-07-28 00:08:54'),(2,'ラーメンウェーブ',500.00,500.00,198.00,90,'2026-07-28 00:09:04','2026-07-28 00:09:04');
/*!40000 ALTER TABLE `items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `manufacturing_batch`
--

DROP TABLE IF EXISTS `manufacturing_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manufacturing_batch` (
  `batch_id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint NOT NULL,
  `mrp_run_id` bigint DEFAULT NULL COMMENT 'フェーズ4で使用予定。現時点ではmrp_runテーブル未実装のためFK制約なし',
  `batch_date` date NOT NULL,
  `batch_seq` int NOT NULL COMMENT 'その日・その商品の何バッチ目か(1から始まる連番)',
  `status` enum('DRAFT','PLAN','MANUFACTURING','COMPLETED','REJECTED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  `origin_type` enum('MRP_AUTO','MANUAL') NOT NULL DEFAULT 'MANUAL' COMMENT 'フェーズ2時点ではMRP自動化が未実装のため、常にMANUALになる',
  `created_by` varchar(100) DEFAULT NULL COMMENT '手動追加時の担当者(任意)',
  `planned_qty` decimal(10,2) NOT NULL COMMENT '通常は items.standard_batch_qty と同値',
  `produced_qty` decimal(10,2) DEFAULT NULL COMMENT '完了時に確定する製造数(合格+不良の合計)',
  `accepted_qty` decimal(10,2) DEFAULT NULL COMMENT '完了時に確定する合格数(商品在庫に計上される数)',
  `remaining_qty` decimal(10,2) DEFAULT NULL COMMENT '出荷等で減っていく残量。完了時にaccepted_qtyと同値で初期化される(フェーズ5で使用)',
  `exceeded_plan` tinyint(1) NOT NULL DEFAULT '0' COMMENT '完了時、produced_qty(合格+不良)がplanned_qtyを超えていた場合にtrue',
  `loss_qty` decimal(10,2) DEFAULT NULL COMMENT '完了時に確定する軽微な不良数',
  `loss_comment` varchar(255) DEFAULT NULL,
  `reject_comment` varchar(255) DEFAULT NULL COMMENT 'REJECTEDになった場合の理由',
  `cancel_comment` varchar(255) DEFAULT NULL COMMENT 'CANCELLEDになった場合の理由(製造開始前の取り消し)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`batch_id`),
  UNIQUE KEY `uq_mb_item_date_seq` (`item_id`,`batch_date`,`batch_seq`),
  KEY `fk_mb_mrp_run` (`mrp_run_id`),
  CONSTRAINT `fk_mb_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `fk_mb_mrp_run` FOREIGN KEY (`mrp_run_id`) REFERENCES `mrp_run` (`run_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manufacturing_batch`
--

LOCK TABLES `manufacturing_batch` WRITE;
/*!40000 ALTER TABLE `manufacturing_batch` DISABLE KEYS */;
INSERT INTO `manufacturing_batch` VALUES (1,1,NULL,'2026-07-27',1,'COMPLETED','MANUAL','テスト太郎',198.00,198.00,195.00,15.00,0,3.00,'検品時に3個、成形不良を確認',NULL,NULL,'2026-07-28 00:12:25','2026-07-29 00:42:15'),(2,1,NULL,'2026-07-29',1,'COMPLETED','MANUAL','テスト太郎',198.00,198.00,198.00,198.00,0,0.00,NULL,NULL,NULL,'2026-07-29 00:37:15','2026-07-29 00:39:07'),(3,1,1,'2026-08-03',1,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(4,1,1,'2026-08-03',2,'CANCELLED','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,'テスト取り消し','2026-08-03 03:30:13','2026-08-03 03:30:35'),(5,1,1,'2026-08-03',3,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(6,1,1,'2026-08-03',4,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(7,1,1,'2026-08-03',5,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(8,2,1,'2026-08-03',1,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(9,2,1,'2026-08-03',2,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(10,2,1,'2026-08-03',3,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:13','2026-08-03 03:41:06'),(11,1,2,'2026-08-03',6,'PLAN','MRP_AUTO',NULL,198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:30:35','2026-08-03 03:41:06'),(12,1,NULL,'2026-08-03',7,'DRAFT','MANUAL','テスト太郎',198.00,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'2026-08-03 03:42:25','2026-08-03 03:42:25');
/*!40000 ALTER TABLE `manufacturing_batch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material`
--

DROP TABLE IF EXISTS `material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material` (
  `material_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '材料名(例:米粉、玄米粉)',
  `category` enum('RAW','ADDITIVE') NOT NULL COMMENT 'RAW=原料 / ADDITIVE=添加物',
  `base_unit` enum('WEIGHT','VOLUME') NOT NULL COMMENT 'WEIGHT=重量(g) / VOLUME=体積(ml)',
  `is_main_material` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'ベーカーズパーセント計算の基準材料か',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material`
--

LOCK TABLES `material` WRITE;
/*!40000 ALTER TABLE `material` DISABLE KEYS */;
INSERT INTO `material` VALUES (1,'米粉','RAW','WEIGHT',1,'2026-07-28 00:08:39','2026-07-28 00:08:39');
/*!40000 ALTER TABLE `material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_arrival`
--

DROP TABLE IF EXISTS `material_arrival`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_arrival` (
  `arrival_id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` varchar(100) NOT NULL,
  `arrival_date` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`arrival_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_arrival`
--

LOCK TABLES `material_arrival` WRITE;
/*!40000 ALTER TABLE `material_arrival` DISABLE KEYS */;
INSERT INTO `material_arrival` VALUES (1,'仕入先A','2026-07-27','2026-07-28 00:09:52','2026-07-28 00:09:52'),(2,'仕入先A','2026-07-27','2026-07-28 00:15:18','2026-07-28 00:15:18'),(3,'仕入先A','2026-07-28','2026-07-28 00:22:40','2026-07-28 00:22:40'),(4,'仕入先A','2026-07-28','2026-07-28 00:35:23','2026-07-28 00:35:23'),(5,'仕入先A','2026-07-30','2026-07-28 00:37:36','2026-07-28 00:37:36'),(6,'仕入先A','2026-07-28','2026-07-28 00:42:29','2026-07-28 00:42:29');
/*!40000 ALTER TABLE `material_arrival` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_arrival_line`
--

DROP TABLE IF EXISTS `material_arrival_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_arrival_line` (
  `line_id` bigint NOT NULL AUTO_INCREMENT,
  `arrival_id` bigint NOT NULL,
  `material_id` bigint NOT NULL COMMENT 'この明細で届いた材料。1回の配送内で複数材料が混在してもよいよう明細側に持つ',
  `order_id` bigint DEFAULT NULL COMMENT '対応する発注(緊急入荷等、発注に紐づかない場合はNULL)。1回の配送内で複数発注が混在してもよいよう明細側に持つ',
  `supplier_lot_no` varchar(100) NOT NULL COMMENT '仕入先が発行したロット番号',
  `origin` varchar(100) NOT NULL COMMENT '産地(原料の場合)。添加物は仕入先区分などを入れる',
  `expiry_date` date NOT NULL COMMENT '賞味期限(FEFO判定の基準になる)',
  `package_count` int NOT NULL COMMENT '入荷した箱数/袋数',
  `package_weight_snapshot` decimal(10,2) NOT NULL COMMENT '入荷時点での1箱あたり目安重量のスナップショット',
  `arrived_qty` decimal(10,2) NOT NULL COMMENT 'package_count × package_weight_snapshot で計算した総量',
  `accepted_qty` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '検品合格数量(在庫に反映される分)',
  `held_qty` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '検品保留数量(在庫には反映しない)',
  `check_damage` tinyint(1) NOT NULL DEFAULT '1' COMMENT '検品項目:破損がないか(TRUE=問題なし)',
  `check_expiry` tinyint(1) NOT NULL DEFAULT '1' COMMENT '検品項目:期限切れでないか',
  `check_contamination` tinyint(1) NOT NULL DEFAULT '1' COMMENT '検品項目:異物混入の兆候がないか',
  `exchange_source_line_id` bigint DEFAULT NULL COMMENT '交換品の場合、元の保留明細を参照する(自己参照)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`line_id`),
  KEY `fk_mal_arrival` (`arrival_id`),
  KEY `fk_mal_material` (`material_id`),
  KEY `fk_mal_order` (`order_id`),
  KEY `fk_mal_exchange_source` (`exchange_source_line_id`),
  CONSTRAINT `fk_mal_arrival` FOREIGN KEY (`arrival_id`) REFERENCES `material_arrival` (`arrival_id`),
  CONSTRAINT `fk_mal_exchange_source` FOREIGN KEY (`exchange_source_line_id`) REFERENCES `material_arrival_line` (`line_id`),
  CONSTRAINT `fk_mal_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`),
  CONSTRAINT `fk_mal_order` FOREIGN KEY (`order_id`) REFERENCES `material_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_arrival_line`
--

LOCK TABLES `material_arrival_line` WRITE;
/*!40000 ALTER TABLE `material_arrival_line` DISABLE KEYS */;
INSERT INTO `material_arrival_line` VALUES (1,1,1,1,'LOT-A','三重','2026-10-01',5,15000.00,75000.00,75000.00,0.00,1,1,1,NULL,'2026-07-28 00:10:08','2026-07-28 00:10:08'),(2,2,1,2,'LOT-B','三重','2026-10-15',3,15000.00,45000.00,45000.00,0.00,1,1,0,NULL,'2026-07-28 00:15:44','2026-07-28 00:17:09'),(3,3,1,3,'LOT-C','三重','2026-08-01',1,15000.00,15000.00,0.00,15000.00,0,1,1,NULL,'2026-07-28 00:27:01','2026-07-28 00:27:01'),(4,4,1,4,'LOT-D','三重','2026-08-05',1,15000.00,15000.00,0.00,15000.00,1,1,0,NULL,'2026-07-28 00:35:49','2026-07-28 00:35:49'),(5,5,1,NULL,'LOT-D-EXCHANGE','三重','2026-08-20',1,15000.00,15000.00,15000.00,0.00,1,1,1,4,'2026-07-28 00:38:14','2026-07-28 00:38:14'),(6,6,1,5,'LOT-E','三重','2026-08-10',1,15000.00,15000.00,0.00,15000.00,0,1,1,NULL,'2026-07-28 00:43:11','2026-07-28 00:43:11');
/*!40000 ALTER TABLE `material_arrival_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_lot`
--

DROP TABLE IF EXISTS `material_lot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_lot` (
  `lot_id` bigint NOT NULL AUTO_INCREMENT,
  `material_id` bigint NOT NULL,
  `arrival_line_id` bigint NOT NULL COMMENT '生成元となった入荷明細',
  `supplier_lot_no` varchar(100) NOT NULL COMMENT '仕入先が発行したロット番号(arrival_lineからコピー、表示の利便性のため)',
  `origin` varchar(100) NOT NULL,
  `expiry_date` date NOT NULL,
  `remaining_qty` decimal(10,2) NOT NULL COMMENT '残量。消費/廃棄のたびにService層で減算する',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`lot_id`),
  UNIQUE KEY `uq_ml_arrival_line` (`arrival_line_id`),
  KEY `fk_ml_material` (`material_id`),
  CONSTRAINT `fk_ml_arrival_line` FOREIGN KEY (`arrival_line_id`) REFERENCES `material_arrival_line` (`line_id`),
  CONSTRAINT `fk_ml_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_lot`
--

LOCK TABLES `material_lot` WRITE;
/*!40000 ALTER TABLE `material_lot` DISABLE KEYS */;
INSERT INTO `material_lot` VALUES (1,1,1,'LOT-A','三重','2026-10-01',59500.00,'2026-07-28 00:10:08','2026-07-28 00:47:58'),(2,1,2,'LOT-B','三重','2026-10-15',45000.00,'2026-07-28 00:15:44','2026-07-28 00:17:09'),(3,1,5,'LOT-D-EXCHANGE','三重','2026-08-20',0.00,'2026-07-28 00:38:14','2026-07-29 00:37:43');
/*!40000 ALTER TABLE `material_lot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_order`
--

DROP TABLE IF EXISTS `material_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `material_id` bigint NOT NULL,
  `supplier_id` varchar(100) NOT NULL COMMENT '仕入先(今回は文字列管理。将来的にsupplierマスタに分離してもよい)',
  `order_qty` decimal(10,2) NOT NULL COMMENT '発注数量(g または ml)',
  `order_date` date NOT NULL,
  `expected_date` date DEFAULT NULL COMMENT '納品予定日',
  `status` enum('NOT_ARRIVED','PARTIALLY_ARRIVED','FULLY_ARRIVED') NOT NULL DEFAULT 'NOT_ARRIVED' COMMENT '未入荷/一部入荷/入荷完了。入荷明細の合格数量を集計して判定する',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `fk_mo_material` (`material_id`),
  CONSTRAINT `fk_mo_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_order`
--

LOCK TABLES `material_order` WRITE;
/*!40000 ALTER TABLE `material_order` DISABLE KEYS */;
INSERT INTO `material_order` VALUES (1,1,'仕入先A',45000.00,'2026-07-27',NULL,'FULLY_ARRIVED','2026-07-28 00:09:37','2026-07-28 00:10:08'),(2,1,'仕入先A',45000.00,'2026-07-27',NULL,'FULLY_ARRIVED','2026-07-28 00:15:08','2026-07-28 00:17:09'),(3,1,'仕入先A',15000.00,'2026-07-28',NULL,'NOT_ARRIVED','2026-07-28 00:22:26','2026-07-28 00:22:26'),(4,1,'仕入先A',15000.00,'2026-07-28',NULL,'NOT_ARRIVED','2026-07-28 00:34:56','2026-07-28 00:34:56'),(5,1,'仕入先A',15000.00,'2026-07-28',NULL,'NOT_ARRIVED','2026-07-28 00:41:41','2026-07-28 00:41:41');
/*!40000 ALTER TABLE `material_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material_package_spec`
--

DROP TABLE IF EXISTS `material_package_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_package_spec` (
  `spec_id` bigint NOT NULL AUTO_INCREMENT,
  `material_id` bigint NOT NULL,
  `origin` varchar(100) NOT NULL COMMENT '産地・仕入先区分(例:愛知、新潟)',
  `package_weight` decimal(10,2) NOT NULL COMMENT '1箱/袋あたりの目安数量(g または ml)',
  `package_unit_label` varchar(20) NOT NULL COMMENT '表示用単位名(箱、袋、缶など)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`spec_id`),
  UNIQUE KEY `uq_mps_material_origin` (`material_id`,`origin`),
  CONSTRAINT `fk_mps_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_package_spec`
--

LOCK TABLES `material_package_spec` WRITE;
/*!40000 ALTER TABLE `material_package_spec` DISABLE KEYS */;
/*!40000 ALTER TABLE `material_package_spec` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mrp_run`
--

DROP TABLE IF EXISTS `mrp_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mrp_run` (
  `run_id` bigint NOT NULL AUTO_INCREMENT,
  `run_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `triggered_by` enum('AUTO','MANUAL','EVENT') NOT NULL COMMENT 'AUTO=1日1回の定期実行, MANUAL=人による手動実行, EVENT=CANCELLED/REJECTED発生時の即時再計算',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`run_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mrp_run`
--

LOCK TABLES `mrp_run` WRITE;
/*!40000 ALTER TABLE `mrp_run` DISABLE KEYS */;
INSERT INTO `mrp_run` VALUES (1,'2026-08-03 03:30:13','MANUAL','2026-08-03 03:30:13'),(2,'2026-08-03 03:30:35','EVENT','2026-08-03 03:30:35');
/*!40000 ALTER TABLE `mrp_run` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_line`
--

DROP TABLE IF EXISTS `order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_line` (
  `line_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `qty` decimal(10,2) NOT NULL COMMENT '注文数量(商品の個数)',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '単価(任意。記録のみ、請求書機能はスコープ外)',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '金額(unit_price × qty)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`line_id`),
  KEY `fk_ol_order` (`order_id`),
  KEY `fk_ol_item` (`item_id`),
  CONSTRAINT `fk_ol_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `fk_ol_order` FOREIGN KEY (`order_id`) REFERENCES `customer_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_line`
--

LOCK TABLES `order_line` WRITE;
/*!40000 ALTER TABLE `order_line` DISABLE KEYS */;
INSERT INTO `order_line` VALUES (1,1,1,150.00,200.00,30000.00,'2026-07-29 00:19:24','2026-07-29 00:19:24'),(2,2,1,10.00,NULL,NULL,'2026-07-29 00:35:56','2026-07-29 00:35:56'),(3,3,1,60.00,NULL,NULL,'2026-07-29 00:40:02','2026-07-29 00:40:02'),(4,5,1,500.00,NULL,NULL,'2026-08-03 03:25:50','2026-08-03 03:25:50');
/*!40000 ALTER TABLE `order_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recipe_item`
--

DROP TABLE IF EXISTS `recipe_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recipe_item` (
  `recipe_item_id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `use_qty` decimal(10,2) NOT NULL COMMENT '使用量(g または ml、material.base_unitに従う)',
  `allowed_origins` varchar(255) NOT NULL COMMENT 'カンマ区切りで使用可能な産地を列挙(例:愛知,三重)',
  `is_main_material` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'この商品における主原料か',
  `is_liquid` tinyint(1) NOT NULL DEFAULT '0' COMMENT '加水率計算に合算する液体material_id か',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`recipe_item_id`),
  UNIQUE KEY `uq_ri_item_material` (`item_id`,`material_id`),
  KEY `fk_ri_material` (`material_id`),
  CONSTRAINT `fk_ri_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`),
  CONSTRAINT `fk_ri_material` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recipe_item`
--

LOCK TABLES `recipe_item` WRITE;
/*!40000 ALTER TABLE `recipe_item` DISABLE KEYS */;
INSERT INTO `recipe_item` VALUES (1,1,1,15000.00,'愛知,三重',1,0,'2026-07-28 00:09:16','2026-07-28 00:09:16');
/*!40000 ALTER TABLE `recipe_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment`
--

DROP TABLE IF EXISTS `shipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipment` (
  `shipment_id` bigint NOT NULL AUTO_INCREMENT,
  `carrier_id` bigint NOT NULL,
  `shipped_date` date NOT NULL,
  `destination` varchar(255) DEFAULT NULL COMMENT '配送先住所等(自由記述)',
  `temperature_zone` enum('FROZEN','AMBIENT') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`shipment_id`),
  KEY `fk_sh_carrier` (`carrier_id`),
  CONSTRAINT `fk_sh_carrier` FOREIGN KEY (`carrier_id`) REFERENCES `carrier` (`carrier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment`
--

LOCK TABLES `shipment` WRITE;
/*!40000 ALTER TABLE `shipment` DISABLE KEYS */;
INSERT INTO `shipment` VALUES (1,1,'2026-07-29','大手スーパーA 配送センター','AMBIENT','2026-07-29 00:31:21','2026-07-29 00:31:21'),(2,1,'2026-07-29','テスト配送先','AMBIENT','2026-07-29 00:41:43','2026-07-29 00:41:43');
/*!40000 ALTER TABLE `shipment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shipment_line`
--

DROP TABLE IF EXISTS `shipment_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipment_line` (
  `line_id` bigint NOT NULL AUTO_INCREMENT,
  `shipment_id` bigint NOT NULL,
  `order_line_id` bigint NOT NULL,
  `batch_id` bigint NOT NULL COMMENT '出荷元の製造バッチ(=商品ロット)',
  `shipped_qty` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`line_id`),
  KEY `fk_sl_shipment` (`shipment_id`),
  KEY `fk_sl_order_line` (`order_line_id`),
  KEY `fk_sl_batch` (`batch_id`),
  CONSTRAINT `fk_sl_batch` FOREIGN KEY (`batch_id`) REFERENCES `manufacturing_batch` (`batch_id`),
  CONSTRAINT `fk_sl_order_line` FOREIGN KEY (`order_line_id`) REFERENCES `order_line` (`line_id`),
  CONSTRAINT `fk_sl_shipment` FOREIGN KEY (`shipment_id`) REFERENCES `shipment` (`shipment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shipment_line`
--

LOCK TABLES `shipment_line` WRITE;
/*!40000 ALTER TABLE `shipment_line` DISABLE KEYS */;
INSERT INTO `shipment_line` VALUES (1,1,1,1,150.00,'2026-07-29 00:31:51','2026-07-29 00:31:51'),(2,2,2,1,30.00,'2026-07-29 00:42:15','2026-07-29 00:42:15');
/*!40000 ALTER TABLE `shipment_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_adjustment`
--

DROP TABLE IF EXISTS `stock_adjustment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_adjustment` (
  `adjustment_id` bigint NOT NULL AUTO_INCREMENT,
  `lot_id` bigint NOT NULL,
  `before_qty` decimal(10,2) NOT NULL COMMENT '調整前の数量',
  `after_qty` decimal(10,2) NOT NULL COMMENT '調整後の数量',
  `adjustment_date` date NOT NULL,
  `comment` varchar(255) NOT NULL COMMENT '調整理由(必須)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`adjustment_id`),
  KEY `fk_sa_lot` (`lot_id`),
  CONSTRAINT `fk_sa_lot` FOREIGN KEY (`lot_id`) REFERENCES `material_lot` (`lot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_adjustment`
--

LOCK TABLES `stock_adjustment` WRITE;
/*!40000 ALTER TABLE `stock_adjustment` DISABLE KEYS */;
INSERT INTO `stock_adjustment` VALUES (1,2,30000.00,45000.00,'2026-07-28','hold_id=1 の保留対応(ACCEPTED_LATE)による在庫増加','2026-07-28 00:17:09','2026-07-28 00:17:09'),(2,1,60000.00,59500.00,'2026-07-28','棚卸しの結果、帳簿より500g少なかったため補正','2026-07-28 00:47:58','2026-07-28 00:47:58');
/*!40000 ALTER TABLE `stock_adjustment` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-03 14:16:44
