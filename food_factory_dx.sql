-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch_material_usage`
--

LOCK TABLES `batch_material_usage` WRITE;
/*!40000 ALTER TABLE `batch_material_usage` DISABLE KEYS */;
INSERT INTO `batch_material_usage` VALUES (1,1,1,15000.00,15000.00,'CONSUMPTION',NULL,'2026-07-27 04:48:19','2026-07-27 04:48:19');
/*!40000 ALTER TABLE `batch_material_usage` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hold_resolution`
--

LOCK TABLES `hold_resolution` WRITE;
/*!40000 ALTER TABLE `hold_resolution` DISABLE KEYS */;
INSERT INTO `hold_resolution` VALUES (1,2,15000.00,NULL,NULL,'ON_HOLD',NULL,'2026-07-27 10:28:31','2026-07-27 10:28:31');
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
INSERT INTO `items` VALUES (1,'うどん',500.00,500.00,198.00,90,'2026-07-27 04:40:22','2026-07-27 04:40:22'),(2,'ラーメンウェーブ',500.00,500.00,198.00,90,'2026-07-27 04:40:29','2026-07-27 04:40:29');
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manufacturing_batch`
--

LOCK TABLES `manufacturing_batch` WRITE;
/*!40000 ALTER TABLE `manufacturing_batch` DISABLE KEYS */;
INSERT INTO `manufacturing_batch` VALUES (1,1,NULL,'2026-07-27',1,'COMPLETED','MANUAL','テスト太郎',198.00,198.00,195.00,195.00,0,3.00,'検品時に3個、成形不良を確認',NULL,NULL,'2026-07-27 04:46:37','2026-07-27 04:48:45');
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
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '有効/廃版フラグ。物理削除はせず、廃版になったらFALSEにする(論理削除)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material`
--

LOCK TABLES `material` WRITE;
/*!40000 ALTER TABLE `material` DISABLE KEYS */;
INSERT INTO `material` VALUES (1,'米粉','RAW','WEIGHT',1,1,'2026-07-27 04:40:01','2026-07-27 04:40:01'),(2,'α米','ADDITIVE','WEIGHT',0,1,'2026-08-10 01:58:28','2026-08-10 01:58:28'),(3,'アルギン酸エステル','ADDITIVE','WEIGHT',0,1,'2026-08-10 02:00:09','2026-08-10 02:00:09'),(4,'キサンタンガム','ADDITIVE','WEIGHT',0,1,'2026-08-10 02:00:16','2026-08-10 02:00:16'),(5,'FKハイパー','ADDITIVE','VOLUME',0,1,'2026-08-10 02:00:25','2026-08-10 02:00:25'),(6,'酢','ADDITIVE','VOLUME',0,1,'2026-08-10 02:00:30','2026-08-10 02:00:30'),(7,'酒精','ADDITIVE','VOLUME',0,1,'2026-08-10 02:00:34','2026-08-10 02:00:34');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_arrival`
--

LOCK TABLES `material_arrival` WRITE;
/*!40000 ALTER TABLE `material_arrival` DISABLE KEYS */;
INSERT INTO `material_arrival` VALUES (1,'仕入先A','2026-07-27','2026-07-27 04:43:15','2026-07-27 04:43:15'),(2,'仕入先A','2026-07-27','2026-07-27 10:28:16','2026-07-27 10:28:16');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_arrival_line`
--

LOCK TABLES `material_arrival_line` WRITE;
/*!40000 ALTER TABLE `material_arrival_line` DISABLE KEYS */;
INSERT INTO `material_arrival_line` VALUES (1,1,1,1,'LOT-A','三重','2026-10-01',5,15000.00,75000.00,75000.00,0.00,1,1,1,NULL,'2026-07-27 04:43:54','2026-07-27 04:43:54'),(2,2,1,2,'LOT-B','三重','2026-10-15',3,15000.00,45000.00,30000.00,15000.00,1,1,0,NULL,'2026-07-27 10:28:31','2026-07-27 10:28:31');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_lot`
--

LOCK TABLES `material_lot` WRITE;
/*!40000 ALTER TABLE `material_lot` DISABLE KEYS */;
INSERT INTO `material_lot` VALUES (1,1,1,'LOT-A','三重','2026-10-01',60000.00,'2026-07-27 04:43:54','2026-07-27 04:48:19'),(2,1,2,'LOT-B','三重','2026-10-15',30000.00,'2026-07-27 10:28:31','2026-07-27 10:28:31');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material_order`
--

LOCK TABLES `material_order` WRITE;
/*!40000 ALTER TABLE `material_order` DISABLE KEYS */;
INSERT INTO `material_order` VALUES (1,1,'仕入先A',45000.00,'2026-07-27',NULL,'FULLY_ARRIVED','2026-07-27 04:42:48','2026-07-27 04:43:54'),(2,1,'仕入先A',45000.00,'2026-07-27',NULL,'PARTIALLY_ARRIVED','2026-07-27 10:28:02','2026-07-27 10:28:31');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mrp_run`
--

LOCK TABLES `mrp_run` WRITE;
/*!40000 ALTER TABLE `mrp_run` DISABLE KEYS */;
/*!40000 ALTER TABLE `mrp_run` ENABLE KEYS */;
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
INSERT INTO `recipe_item` VALUES (1,1,1,15000.00,'愛知,三重',1,0,'2026-07-27 04:40:44','2026-07-27 04:40:44');
/*!40000 ALTER TABLE `recipe_item` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_adjustment`
--

LOCK TABLES `stock_adjustment` WRITE;
/*!40000 ALTER TABLE `stock_adjustment` DISABLE KEYS */;
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

-- Dump completed on 2026-08-10 11:17:08
