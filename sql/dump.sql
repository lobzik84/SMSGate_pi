-- MySQL dump 10.13  Distrib 5.5.44, for debian-linux-gnu (armv7l)
--
-- Host: localhost    Database: hs
-- ------------------------------------------------------
-- Server version	5.5.52-0+deb8u1

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
-- Table structure for table `actions`
--

DROP TABLE IF EXISTS `actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `actions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `alias` varchar(100) DEFAULT NULL,
  `module` varchar(50) NOT NULL,
  `notification_text` varchar(1023) DEFAULT NULL,
  `condition_id` int(11) NOT NULL,
  `severity` varchar(25) DEFAULT NULL,
  `box_mode` varchar(25) DEFAULT NULL,
  `condition_state` int(11) DEFAULT NULL,
  `event_name` varchar(25) DEFAULT NULL,
  `enabled` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actions`
--

LOCK TABLES `actions` WRITE;
/*!40000 ALTER TABLE `actions` DISABLE KEYS */;
INSERT INTO `actions` VALUES (1,'VAC_SENSOR_UNSTABLE_ARMED_SMS','ModemModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT','ARMED',1,'send_sms',1),(3,'VAC_SENSOR_UNSTABLE_WEB','WebNotificationsModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT',NULL,1,'web_notification',1),(4,'VAC_SENSOR_UNSTABLE_LOG','LogModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT',NULL,1,'log_record',1),(11,'VAC_SENSOR_POWER_LOSS_SMS','ModemModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,1,'send_sms',1),(12,'VAC_SENSOR_POWER_LOSS_DISPLAY','DisplayModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,NULL,'display_notification',1),(13,'VAC_SENSOR_POWER_LOSS_LOG','LogModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,1,'log_record',1),(16,'VAC_SENSOR_POWER_LOSS_ARMED_EMAIL','TunnelClientModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM','ARMED',1,'send_email_notification',1),(17,'VAC_SENSOR_POWER_RECOVERED_SMS','ModemModule','Восстановилось напряжение в электросети',2,'OK',NULL,0,'send_sms',1),(19,'VAC_SENSOR_POWER_RECOVERED_LOG','LogModule','Восстановилось напряжение в электросети',2,'OK',NULL,0,'log_record',1),(33,'BAT_CHARGE_LESS_30_SMS','ModemModule','Тревога! Уровень заряда аккумуляторов %VALUE% %',10,'ALARM',NULL,1,'send_sms',1),(34,'BAT_CHARGE_LESS_30_WEB','WebNotificationsModule','Уровень заряда аккумуляторов %VALUE% %',10,'ALARM',NULL,1,'web_notification',1),(35,'BAT_CHARGE_LESS_30_EMAIL','TunnelClientModule','Уровень заряда аккумуляторов упал до %VALUE% %.  ',10,'ALARM',NULL,1,'send_email_notification',1),(36,'BAT_CHARGE_LESS_30_LOG','LogModule','Уровень заряда аккумуляторов упал до %VALUE% %.',10,'ALARM',NULL,1,'log_record',1),(44,'BAT_CHARGE_BETWEEN_30_50_WEB','WebNotificationsModule','Уровень заряда аккумуляторов %VALUE% %. ',12,'ALERT',NULL,1,'web_notification',1),(45,'BAT_CHARGE_BETWEEN_30_50_LOG','LogModule','Уровень заряда аккумуляторов %VALUE% %. ',12,'ALERT',NULL,1,'log_record',1),(50,'BATT_TEMP_OVERHEAT_SMS','ModemModule','Температура аккумуляторов превысила %VALUE%’С. Устройство выключается.',16,'ALARM',NULL,1,'send_sms',1),(51,'BATT_TEMP_OVERHEAT_WEB','WebNotificationsModule','Температура аккумуляторов превысила %VALUE%’С. Устройство будет выключено. ',16,'ALARM',NULL,1,'web_notification',1),(52,'BATT_TEMP_OVERHEAT_EMAIL','TunnelClientModule','Температура аккумуляторов превысила %VALUE%’С. Устройство будет выключено. Обратитесь к производителю.',16,'ALARM',NULL,1,'send_email_notification',1),(53,'BATT_TEMP_OVERHEAT_LOG','LogModule','Температура аккумуляторов превысила %VALUE%’С. Устройство будет выключено. Обратитесь к производителю.',16,'ALARM',NULL,1,'log_record',1),(56,'INTERNAL_TEMP_SENSOR_FAILURE_ARMED_WEB','WebNotificationsModule','Отказ датчика температуры. Обратитесь к производителю',18,'ALERT',NULL,1,'web_notification',1),(57,'INTERNAL_TEMP_SENSOR_FAILURE_ARMED_LOG','LogModule','Отказ датчика температуры. Обратитесь к производителю',18,'ALERT',NULL,1,'log_record',1),(61,'INTERNAL_TEMP_FAST_FALLING_ARMED_SMS','ModemModule','Быстрое падение температуры воздуха',20,'ALERT','ARMED',1,'send_sms',1),(62,'INTERNAL_TEMP_FAST_FALLING_WEB','WebNotificationsModule','Быстрое падение температуры воздуха',20,'ALERT',NULL,1,'web_notification',1),(63,'INTERNAL_TEMP_FAST_FALLING_LOG','LogModule','Быстрое падение температуры воздуха',20,'ALERT',NULL,1,'log_record',1),(67,'INTERNAL_TEMP_FAST_RISING_ARMED_SMS','ModemModule','Быстрое повышение температуры воздуха',22,'ALERT','ARMED',1,'send_sms',1),(68,'INTERNAL_TEMP_FAST_RISING_WEB','WebNotificationsModule','Быстрое повышение температуры воздуха',22,'ALERT',NULL,1,'web_notification',1),(69,'INTERNAL_TEMP_FAST_RISING_LOG','LogModule','Быстрое повышение температуры воздуха',22,'ALERT',NULL,1,'log_record',1),(74,'INTERNAL_TEMP_OUT_OF_BOUNDS_SMS','ModemModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'send_sms',1),(75,'INTERNAL_TEMP_OUT_OF_BOUNDS_IDLE_DISPLAY','DisplayModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,NULL,'display_notification',1),(76,'INTERNAL_TEMP_OUT_OF_BOUNDS_EMAIL','TunnelClientModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'send_email_notification',1),(77,'INTERNAL_TEMP_OUT_OF_BOUNDS_LOG','LogModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'log_record',1),(80,'INTERNAL_TEMP_BACK_TO_NORMAL_SMS','ModemModule','Температура воздуха в норме',24,'OK',NULL,0,'send_sms',1),(81,'INTERNAL_TEMP_BACK_TO_NORMAL_WEB','WebNotificationsModule','Температура воздуха в норме',24,'OK',NULL,0,'web_notification',1),(82,'INTERNAL_TEMP_BACK_TO_NORMAL_LOG','LogModule','Температура воздуха в норме',24,'OK',NULL,0,'log_record',1),(86,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_WEB','WebNotificationsModule','Влажность водуха %VALUE% %',28,'INFO',NULL,1,'web_notification',1),(87,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_LOG','LogModule','Влажность водуха %VALUE% %',28,'INFO',NULL,1,'log_record',1),(95,'INTERNAL_HUMIDITY_BACK_TO_NORMAL_WEB','WebNotificationsModule','Влажность водуха  в норме',28,'OK',NULL,0,'web_notification',1),(96,'INTERNAL_HUMIDITY_BACK_TO_NORMAL_LOG','LogModule','Влажность водуха  в норме',28,'OK',NULL,0,'log_record',1),(101,'GAS_SENSOR_ALARM_SMS','ModemModule','Тревога! Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'send_sms',1),(102,'GAS_SENSOR_ALARM_WEB','WebNotificationsModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'web_notification',1),(103,'GAS_SENSOR_ALARM_EMAIL','TunnelClientModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'send_email_notification',1),(104,'GAS_SENSOR_ALARM_LOG','LogModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,1,'log_record',1),(108,'GAS_SENSOR_CLEARED_SMS','ModemModule','Концентрация газа в норме',34,'OK',NULL,0,'send_sms',1),(109,'GAS_SENSOR_CLEARED_WEB','WebNotificationsModule','Концентрация газа в норме',34,'OK',NULL,0,'web_notification',1),(110,'GAS_SENSOR_CLEARED_EMAIL','TunnelClientModule','Концентрация газа в норме',34,'OK',NULL,0,'send_email_notification',1),(111,'GAS_SENSOR_CLEARED_LOG','LogModule','Концентрация газа в норме',34,'OK',NULL,0,'log_record',1),(113,'PIR_SENSOR_ALARM_ARMED_SMS','ModemModule','Тревога! Сработал датчик движения',38,'ALARM','ARMED',1,'send_sms',1),(114,'PIR_SENSOR_ALARM_ARMED_WEB','WebNotificationsModule','Сработал датчик движения',38,'ALARM','ARMED',1,'web_notification',1),(115,'PIR_SENSOR_ALARM_ARMED_EMAIL','TunnelClientModule','Сработал датчик движения',38,'ALARM','ARMED',1,'send_email_notification',1),(116,'PIR_SENSOR_ALARM_ARMED_LOG','LogModule','Сработал датчик движения',38,'ALARM','ARMED',1,'log_record',1),(118,'PIR_SENSOR_CLEARED_ARMED_WEB','WebNotificationsModule','Датчик перестал фиксировать движения ',38,'OK','ARMED',0,'web_notification',1),(119,'PIR_SENSOR_CLEARED_ARMED_EMAIL','TunnelClientModule','Датчик перестал фиксировать движения ',38,'OK','ARMED',0,'send_email_notification',1),(120,'PIR_SENSOR_CLEARED_ARMED_LOG','LogModule','Датчик перестал фиксировать движения ',38,'OK','ARMED',0,'log_record',1),(121,'MIC_NOISE_ALARM_ARMED_WEB','WebNotificationsModule','Микрофон фиксирует шум',42,'INFO','ARMED',1,'web_notification',1),(122,'MIC_NOISE_ALARM_ARMED_LOG','LogModule','Микрофон фиксирует шум',42,'INFO','ARMED',1,'log_record',1),(123,'MIC_NOISE_CLEARED_ARMED_WEB','WebNotificationsModule','Шум прекратился',42,'OK','ARMED',0,'web_notification',1),(124,'MIC_NOISE_CLEARED_ARMED_LOG','LogModule','Шум прекратился',42,'OK','ARMED',0,'log_record',1),(133,'DOOR_SENSOR_OPEN_ARMED_SMS','ModemModule','Тревога! Сработал датчик открытия двери!',7,'INFO','ARMED',1,'send_sms',1),(134,'DOOR_SENSOR_OPEN_ARMED_WEB','WebNotificationsModule','Сработал датчик открытия двери',7,'ALARM','ARMED',1,'web_notification',1),(135,'DOOR_SENSOR_OPEN_ARMED_EMAIL','TunnelClientModule','Сработал датчик открытия двери',7,'ALARM','ARMED',1,'send_email_notification',1),(136,'DOOR_SENSOR_OPEN_ARMED_LOG','LogModule','Сработал датчик открытия двери',7,'ALARM','ARMED',1,'log_record',1),(137,'VAC_SENSOR_UNSTABLE_DISPLAY','DisplayModule','Опасный скачок напряжения электросети: %VALUE% V',1,'ALERT',NULL,NULL,'display_notification',1),(138,'GAS_SENSOR_ALARM_DISPLAY','DisplayModule','Опасная концентрация газа / дыма',34,'ALARM',NULL,NULL,'display_notification',1),(139,'GAS_SENSOR_ALARM_SOUND','SpeakerModule','alarm.wav',34,'ALARM','IDLE',1,'play_sound',1),(140,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_IDLE_DISPLAY','DisplayModule','Влажность водуха %VALUE% %',28,'INFO',NULL,NULL,'display_notification',1),(141,'INTERNAL_TEMP_OUT_OF_BOUNDS_WEB','WebNotificationsModule','Температура воздуха %VALUE%’С',24,'ALARM',NULL,1,'web_notification',1),(142,'INTERNAL_TEMP_FAST_FALLING_DISPLAY','DisplayModule','Быстрое падение температуры воздуха',20,'ALERT',NULL,NULL,'display_notification',1),(143,'INTERNAL_TEMP_FAST_RISING_IDLE_DISPLAY','DisplayModule','Быстрое повышение температуры воздуха',22,'ALERT',NULL,NULL,'display_notification',1),(144,'BAT_CHARGE_BETWEEN_30_50_DISPLAY','DisplayModule','Уровень заряда аккумуляторов %VALUE% %. ',12,'ALERT',NULL,NULL,'display_notification',1),(145,'BAT_CHARGE_LESS_30_DISPLAY','DisplayModule','Уровень заряда аккумуляторов %VALUE% % ! ',10,'ALARM',NULL,NULL,'display_notification',1),(146,'VAC_SENSOR_POWER_LOSS_WEB','WebNotificationsModule','Более 5 минут отсутствует напряжение в электросети',2,'ALARM',NULL,1,'web_notification',1),(147,'BATT_TEMP_OVERHEAT_SOUND','SpeakerModule','alarm.wav',16,'ALARM','IDLE',1,'play_sound',1),(148,'BATT_TEMP_OVERHEAT_SHUTDOWN','SystemModule','-',16,'ALARM',NULL,1,'shutdown',1),(149,'WET_SENSOR_ALARM_DISPLAY','DisplayModule','Сработал датчик протечки воды!',45,'ALARM',NULL,NULL,'display_notification',1),(150,'WET_SENSOR_ALARM_EMAIL','TunnelClientModule','Сработал датчик протечки воды!',45,'ALARM',NULL,1,'send_email_notification',1),(151,'WET_SENSOR_ALARM_LOG','LogModule','Сработал датчик протечки воды!',45,'ALARM',NULL,1,'log_record',1),(152,'WET_SENSOR_ALARM_SMS','ModemModule','Тревога! Сработал датчик протечки воды!',45,'ALARM',NULL,1,'send_sms',1),(153,'WET_SENSOR_ALARM_SOUND','SpeakerModule','alarm.wav',45,'ALARM','IDLE',1,'play_sound',1),(154,'WET_SENSOR_ALARM_WEB','WebNotificationsModule','Сработал датчик протечки воды!',45,'ALARM',NULL,1,'web_notification',1),(155,'BATT_TEMP_OVERHEAT_DISPLAY','DisplayModule','Температура аккумуляторов превысила %VALUE%’С. Устройство будет выключено. ',16,'ALARM',NULL,NULL,'display_notification',1),(156,'LUMIOSITY_DARK_LOG','LogModule','Сработал датчик освещённости: темно',46,'INFO',NULL,1,'log_record',1),(157,'LUMIOSITY_LIGHT_WEB','WebNotificationsModule','Сработал датчик освещённости: светло',46,'INFO',NULL,0,'web_notification',1),(158,'LUMIOSITY_LIGHT_LOG','LogModule','Сработал датчик освещённости: светло',46,'INFO',NULL,0,'log_record',1),(159,'LUMIOSITY_DARK_WEB','WebNotificationsModule','Сработал датчик освещённости: темно',46,'INFO',NULL,1,'web_notification',1),(161,'NIGHTTIME_IS_NIGHT_LOG','LogModule','Наступила ночь',47,'INFO',NULL,1,'log_record',1),(162,'NIGHTTIME_IS_DAY_LOG','LogModule','Наступил день',47,'INFO',NULL,0,'log_record',1),(165,'SHUTDOWN_LOG','LogModule','Устройство отключается %VALUE%',48,'ALARM',NULL,NULL,'log_record',1),(166,'LOCATION_DETECTED_LOG','LogModule','Определена геолокация %VALUE%',49,'INFO',NULL,NULL,'log_record',1),(167,'LOCATION_DETECTED_WEB','WebNotificationsModule','Определена геолокация %VALUE%',49,'INFO',NULL,NULL,'web_notification',1),(168,'TUNNEL_CONNECTED_LOG','LogModule','Установлено соединение с сервером',51,'OK',NULL,NULL,'log_record',1),(169,'TUNNEL_CONNECTION_LOST_LOG','LogModule','Потеряно соединение с сервером',53,'ALERT',NULL,NULL,'log_record',1),(170,'SHUTDOWN_SMS','ModemModule','Устройство отключается %VALUE%',48,'ALARM',NULL,NULL,'send_sms',1),(171,'SHUTDOWN_EMAIL','TunnelClientModule','Устройство отключается %VALUE%',48,'ALARM',NULL,NULL,'send_email_notification',1),(172,'FORECAST_LOADED_LOG','LogModule','Загружен прогноз погоды',50,'INFO',NULL,NULL,'log_record',1),(173,'USER_LOGGED_IN_LOG','LogModule','Пользователь авторизовался в личном кабинете %VALUE%',52,'INFO',NULL,NULL,'log_record',1),(174,'USER_REGISTERED_SMS','ModemModule','Вас приветствует Управдом! Пожалуйста, сохраните мой номер.',54,'INFO',NULL,NULL,'send_sms',1),(175,'USER_REGISTERED_WEB','WebNotificationsModule','Вас приветствует Управдом! Устройство запущено.',54,'INFO',NULL,NULL,'web_notification',1),(176,'USER_REGISTERED_LOG','LogModule','Вас приветствует Управдом! Вам отправлено SMS. Пожалуйста, сохраните мой номер в списке контактов. Серийный номер устройства <%VALUE%>. ',54,'INFO',NULL,NULL,'log_record',1),(177,'USER_REGISTERED_EMAIL','TunnelClientModule','Вас приветствует Управдом! Вам отправлено SMS. Пожалуйста, сохраните мой номер в списке контактов. Серийный номер устройства <%VALUE%>. Устройство запущено.',54,'INFO',NULL,NULL,'send_email_notification',1),(178,'BOX_MODE_CHANGED_IDLE_LOG','LogModule','Установлен режим \"Хозяин Дома\"',55,'INFO',NULL,0,'log_record',1),(179,'BOX_MODE_CHANGED_ARMED_LOG','LogModule','Установлен режим \"Охрана\"',55,'INFO',NULL,1,'log_record',1),(183,'LAMP1_PIR_SCRIPT_LOG_OFF','LogModule','По датчику движения выключена лампа 1',56,'INFO','IDLE',0,'log_record',1),(184,'LAMP1_PIR_SCRIPT_LOG_ON','LogModule','По датчику движения включена лампа 1',56,'INFO','IDLE',1,'log_record',1),(185,'LAMP1_PIR_SCRIPT_ON','ScriptsModule','-',56,'INFO','IDLE',1,'lamp1_on',1),(186,'LAMP1_PIR_SCRIPT_OFF','ScriptsModule','-',56,'INFO',NULL,0,'lamp1_off',1),(187,'LAMP2_PIR_SCRIPT_ON','ScriptsModule','-',57,'INFO','IDLE',1,'lamp2_on',1),(188,'LAMP2_PIR_SCRIPT_OFF','ScriptsModule','-',57,'INFO',NULL,0,'lamp2_off',1),(189,'LAMP2_PIR_SCRIPT_LOG_ON','LogModule','По датчику движения включена лампа 2',57,'INFO','IDLE',1,'log_record',1),(190,'LAMP2_PIR_SCRIPT_LOG_OFF','LogModule','По датчику движения выключена лампа 2',57,'INFO','IDLE',0,'log_record',1),(191,'LAMP1_NIGHT_SCRIPT_ON','ScriptsModule','-',58,'INFO',NULL,1,'lamp1_on',1),(192,'LAMP1_NIGHT_SCRIPT_OFF','ScriptsModule','-',58,'INFO',NULL,0,'lamp1_off',1),(193,'LAMP1_NIGHT_SCRIPT_LOG_ON','LogModule','По сценарию включена лампа 1',58,'INFO',NULL,1,'log_record',1),(194,'LAMP1_NIGHT_SCRIPT_LOG_OFF','LogModule','По сценарию выключена лампа 1',58,'INFO',NULL,0,'log_record',1),(195,'LAMP2_NIGHT_SCRIPT_ON','ScriptsModule','-',59,'INFO',NULL,1,'lamp2_on',1),(196,'LAMP2_NIGHT_SCRIPT_OFF','ScriptsModule','-',59,'INFO',NULL,0,'lamp2_off',1),(197,'LAMP2_NIGHT_SCRIPT_LOG_ON','LogModule','По сценарию включена лампа 2',59,'INFO',NULL,1,'log_record',1),(198,'LAMP2_NIGHT_SCRIPT_LOG_OFF','LogModule','По сценарию выключена лампа 2',59,'INFO',NULL,0,'log_record',1),(199,'BOX_MODE_CHANGED_IDLE_SMS','ModemModule','Установлен режим \"Хозяин Дома\"',55,'INFO',NULL,0,'send_sms',1),(200,'BOX_MODE_CHANGED_ARMED_SMS','ModemModule','Установлен режим \"Охрана\"',55,'INFO',NULL,1,'send_sms',1),(201,'BOX_MODE_CHANGED_IDLE_WEB','WebNotificationsModule','Установлен режим \"Хозяин Дома\"',55,'INFO',NULL,0,'web_notification',1),(202,'BOX_MODE_CHANGED_ARMED_WEB','WebNotificationsModule','Установлен режим \"Охрана\"',55,'INFO',NULL,1,'web_notification',1),(203,'INTERNAL_TEMP_FAST_RISING_EMAIL','TunnelClientModule','Быстрое повышение температуры воздуха',22,'ALERT',NULL,1,'send_email_notification',1),(204,'INTERNAL_TEMP_FAST_FALLING_EMAIL','TunnelClientModule','Быстрое падение температуры воздуха',20,'ALERT',NULL,1,'send_email_notification',1),(205,'USER_PASSWORD_UPDATED_SMS','ModemModule','Внимание! Изменён логин и/или пароль пользователя.',60,'INFO',NULL,NULL,'send_sms',1),(206,'USER_PASSWORD_UPDATED_WEB','WebNotificationsModule','Внимание! Изменён логин и/или пароль пользователя.',60,'INFO',NULL,NULL,'web_notification',1),(207,'USER_PASSWORD_UPDATED_LOG','LogModule','Внимание! Изменён логин и/или пароль пользователя.',60,'INFO',NULL,NULL,'log_record',1),(208,'BATT_TEMP_OVERHEAT_BACKUP','BackupModule','-',16,'ALARM',NULL,1,'do_backup',1),(209,'BAT_CHARGE_LESS_30_BACKUP','BackupModule','-',10,'ALARM',NULL,1,'do_backup',1),(210,'PIR_SENSOR_ALARM_ARMED_VIDEOREC','TunnelClientModuleVideoRecModule','-',38,'ALARM','ARMED',1,'upload_video_recs',1),(211,'STATISTICS_SENT_WEB','WebNotificationsModule','Отправлена статистика на электронную почту пользователя',61,'INFO',NULL,NULL,'web_notification',1),(212,'STATISTICS_SENT_LOG','LogModule','Отправлена статистика на электронную почту пользователя',61,'INFO',NULL,NULL,'log_record',1),(213,'VAC_SENSOR_UNSTABLE_SOUND','SpeakerModule','alert.wav',1,'ALERT','IDLE',1,'play_sound',1),(214,'VAC_SENSOR_POWER_LOSS_SOUND','SpeakerModule','alarm.wav',2,'ALARM','IDLE',1,'play_sound',1),(215,'BAT_CHARGE_LESS_30_SOUND','SpeakerModule','alarm.wav',10,'ALARM','IDLE',1,'play_sound',1),(216,'BAT_CHARGE_BETWEEN_30_50_SOUND','SpeakerModule','alarm.wav',12,'ALARM','IDLE',1,'play_sound',1),(217,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_IDLE_SOUND','SpeakerModule','info.wav',28,'INFO','IDLE',1,'play_sound',1),(218,'INTERNAL_TEMP_FAST_FALLING_SOUND','SpeakerModule','alert.wav',20,'ALERT','IDLE',1,'play_sound',1),(219,'INTERNAL_TEMP_FAST_RISING_IDLE_SOUND','SpeakerModule','alert.wav',22,'ALERT','IDLE',1,'play_sound',1),(220,'INTERNAL_TEMP_OUT_OF_BOUNDS_SOUND','SpeakerModule','alarm.wav',24,'ALARM','IDLE',1,'play_sound',1),(221,'GAS_SENSOR_ALARM_SOUND_OK','SpeakerModule','ok.wav',34,'OK','IDLE',0,'play_sound',1),(222,'BATT_TEMP_OVERHEAT_SOUND_OK','SpeakerModule','ok.wav',16,'OK','IDLE',0,'play_sound',1),(223,'WET_SENSOR_ALARM_SOUND_OK','SpeakerModule','ok.wav',45,'OK','IDLE',0,'play_sound',1),(224,'VAC_SENSOR_UNSTABLE_SOUND_OK','SpeakerModule','ok.wav',1,'OK','IDLE',0,'play_sound',1),(225,'VAC_SENSOR_POWER_LOSS_SOUND_OK','SpeakerModule','ok.wav',2,'OK','IDLE',0,'play_sound',1),(226,'BAT_CHARGE_LESS_30_SOUND_OK','SpeakerModule','ok.wav',10,'OK','IDLE',0,'play_sound',1),(227,'BAT_CHARGE_BETWEEN_30_50_SOUND_OK','SpeakerModule','ok.wav',12,'OK','IDLE',0,'play_sound',1),(228,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS_IDLE_SOUND_OK','SpeakerModule','ok.wav',28,'OK','IDLE',0,'play_sound',1),(229,'INTERNAL_TEMP_FAST_FALLING_SOUND_OK','SpeakerModule','ok.wav',20,'OK','IDLE',0,'play_sound',1),(230,'INTERNAL_TEMP_FAST_RISING_IDLE_SOUND_OK','SpeakerModule','ok.wav',22,'OK','IDLE',0,'play_sound',1),(231,'INTERNAL_TEMP_OUT_OF_BOUNDS_SOUND_OK','SpeakerModule','ok.wav',24,'OK','IDLE',0,'play_sound',1);
/*!40000 ALTER TABLE `actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conditions`
--

DROP TABLE IF EXISTS `conditions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `conditions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_id` int(11) DEFAULT NULL,
  `alias` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `state` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conditions`
--

LOCK TABLES `conditions` WRITE;
/*!40000 ALTER TABLE `conditions` DISABLE KEYS */;
INSERT INTO `conditions` VALUES (1,5,'VAC_SENSOR_UNSTABLE','Опасный для электроприборов скачок напряжения электросети\n(в т.ч. отключение напряжения)',0),(2,5,'VAC_SENSOR_POWER_LOSS','Более 5 минут напряжение в сети вне пределов',0),(7,21,'DOOR_SENSOR_OPEN','Сработал датчик открывания двери',0),(10,19,'BAT_CHARGE_LESS_30','Заряд аккумуляторов меньше 30% более 1 мин',0),(12,19,'BAT_CHARGE_BETWEEN_30_50','Заряд аккумуляторов < 50% и > 30% более 1 мин',0),(16,9,'BATT_TEMP_OVERHEAT','Перегрев аккумулятора в течение более 1 мин',0),(18,1,'INTERNAL_TEMP_SENSOR_FAILURE','Отказ датчика температуры (нет данных)',0),(20,1,'INTERNAL_TEMP_FAST_FALLING','Температура воздуха снижается быстрее, чем на 5 градусов в час',0),(22,1,'INTERNAL_TEMP_FAST_RISING','Температура воздуха Растет быстрее чем на 5 градусов за 10 минут',0),(24,1,'INTERNAL_TEMP_OUT_OF_BOUNDS','Вышла и более 5 минут подряд находится вне установленных пределов',0),(28,2,'INTERNAL_HUMIDITY_OUT_OF_BOUNDS','Вышла и более 5 минут подряд находится вне установленных пределов',0),(34,7,'GAS_SENSOR_ALARM','Сработал датчик газа',0),(38,6,'PIR_SENSOR_ALARM','сработал датчик движения и работает более 10 сек подряд',0),(42,12,'MIC_NOISE_ALARM','Сработал датчик шума (более 10 сек шум)',0),(45,20,'WET_SENSOR_ALARM','Сработал датчик протечки воды',0),(46,3,'LUMIOSITY_DARK','Освещённость недостаточна более 5 минут подряд',0),(47,22,'NIGHTTIME_IS_NIGHT','Наступила ночь',0),(48,NULL,'SHUTDOWN','Система отключается',0),(49,NULL,'LOCATION_DETECTED','Определено местоположение',0),(50,NULL,'FORECAST_LOADED','Загружен прогноз погоды',0),(51,NULL,'TUNNEL_CONNECTED','Установлено соединение с сервером',0),(52,NULL,'USER_LOGGED_IN','Пользователь авторизовался',0),(53,NULL,'TUNNEL_CONNECTION_LOST','Потеряно соединение с сервером',0),(54,NULL,'USER_REGISTERED','Пользователь зарегистрировался',0),(55,NULL,'BOX_MODE_CHANGED','Переключен режим устройства',0),(56,NULL,'LAMP1_PIR_SCRIPT','Сценарий лампы 1 по датчику движения и темноты',0),(57,NULL,'LAMP2_PIR_SCRIPT','Сценарий лампы 2 по датчику движения и темноты',0),(58,NULL,'LAMP1_NIGHT_SCRIPT','Сценарий лампы 1 в ночи',0),(59,NULL,'LAMP2_NIGHT_SCRIPT','Сценарий лампы 2 в ночи',0),(60,NULL,'USER_PASSWORD_UPDATED','Изменён логин и/или пароль пользователя',0),(61,NULL,'STATISTICS_SENT','Отправлена статистика на email',0);
/*!40000 ALTER TABLE `conditions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logs`
--

DROP TABLE IF EXISTS `logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `module_name` varchar(255) NOT NULL,
  `dated` datetime NOT NULL,
  `level` varchar(10) NOT NULL,
  `message` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=612993 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logs`
--

LOCK TABLES `logs` WRITE;
/*!40000 ALTER TABLE `logs` DISABLE KEYS */;
INSERT INTO `logs` VALUES (612984,'TunnelClientModule','2016-12-07 10:15:29','INFO','Disconnecting'),(612985,'InternalSensorsModule','2016-12-07 10:15:30','INFO','Stopping SerialWriter'),(612986,'DBCleanerModule','2016-12-07 10:15:30','INFO','Stopping DBCleanerModule'),(612987,'LogModule','2016-12-07 10:15:30','ERROR','Потеряно соединение с сервером'),(612988,'TunnelClientModule','2016-12-07 10:15:30','INFO','closing websocket'),(612989,'DisplayModule','2016-12-07 10:15:30','INFO','Exiting process'),(612990,'TunnelClientModule','2016-12-07 10:15:30','INFO','WS Client Disconnected.'),(612991,'MicrophoneModule','2016-12-07 10:15:30','INFO','Exiting process'),(612992,'MicrophoneModule','2016-12-07 10:15:30','INFO','Exiting process');
/*!40000 ALTER TABLE `logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mode`
--

DROP TABLE IF EXISTS `mode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mode` (
  `box_mode` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mode`
--

LOCK TABLES `mode` WRITE;
/*!40000 ALTER TABLE `mode` DISABLE KEYS */;
INSERT INTO `mode` VALUES ('IDLE');
/*!40000 ALTER TABLE `mode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parameters`
--

DROP TABLE IF EXISTS `parameters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) NOT NULL,
  `alias` varchar(60) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `format_pattern` varchar(25) DEFAULT NULL,
  `unit` varchar(5) DEFAULT NULL,
  `type` varchar(25) DEFAULT NULL,
  `calibration` double DEFAULT NULL,
  `correction` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parameters`
--

LOCK TABLES `parameters` WRITE;
/*!40000 ALTER TABLE `parameters` DISABLE KEYS */;
INSERT INTO `parameters` VALUES (1,'Internal Temperature','INTERNAL_TEMP','Температура в помещении','%2.1f','°C','DOUBLE',NULL,-2),(2,'Internal Humidity','INTERNAL_HUMIDITY','Влажность воздуха','%2.1f','%','DOUBLE',NULL,NULL),(3,'Lumiosity','LUMIOSITY','Освещённость','%2.1f','lx','DOUBLE',NULL,NULL),(4,'Battery Voltage','VBAT_SENSOR','Напряжение АКБ','%2.1f','V','DOUBLE',0.011308,NULL),(5,'AC Voltage','VAC_SENSOR','Напряжение в сети','%3.1f','V','DOUBLE',0.299727,NULL),(6,'PIR Sensor','PIR_SENSOR','Датчик движения',NULL,NULL,'BOOLEAN',NULL,NULL),(7,'Gas Sensor','GAS_SENSOR','Датчик горючих газов',NULL,NULL,'BOOLEAN',NULL,NULL),(8,'Gas Sensor Signal','GAS_SENSOR_ANALOG','Сигнал с датчика газов',NULL,'V','DOUBLE',0.001075269,NULL),(9,'Battery Temperature','BATT_TEMP','Температура АКБ',NULL,'°C','DOUBLE',NULL,NULL),(10,'Modem RSSI','MODEM_RSSI','Уровень сигнала сотовой сети','%2.1f','dBm','DOUBLE',NULL,NULL),(11,'Outside Temperature','OUTSIDE_TEMP','Температура на улице','%2.1f','°C','DOUBLE',NULL,NULL),(12,'MIC Noise','MIC_NOISE','Датчик шума',NULL,NULL,'BOOLEAN',NULL,NULL),(13,'Lamp1','LAMP_1','Лампа 1',NULL,NULL,'BOOLEAN',NULL,NULL),(14,'Lamp2','LAMP_2','Лампа 2',NULL,NULL,'BOOLEAN',NULL,NULL),(15,'Socket','SOCKET','Розетка',NULL,NULL,'BOOLEAN',NULL,NULL),(16,'Charge Enabled','CHARGE_ENABLED','Включена зарядка батареи',NULL,NULL,'BOOLEAN',NULL,NULL),(17,'Rain','RAIN','Осадки','%2.1f','mm','DOUBLE',NULL,NULL),(18,'Clouds','CLOUDS','Облачность','%2.1f','%','DOUBLE',NULL,NULL),(19,'Battery Charge','BATT_CHARGE','Заряд батареи','%2d','%','INTEGER',NULL,NULL),(20,'Wet Sensor','WET_SENSOR','Датчик затопления',NULL,NULL,'BOOLEAN',NULL,NULL),(21,'Door Sensor','DOOR_SENSOR','Датчик открытия двери',NULL,NULL,'BOOLEAN',NULL,NULL),(22,'Night Time','NIGHTTIME','Ночное время',NULL,NULL,'BOOLEAN',NULL,NULL);
/*!40000 ALTER TABLE `parameters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensors_data`
--

DROP TABLE IF EXISTS `sensors_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensors_data` (
  `rec_id` int(11) NOT NULL AUTO_INCREMENT,
  `parameter_id` int(11) NOT NULL,
  `date` datetime NOT NULL,
  `value_d` double DEFAULT NULL,
  `value_s` varchar(255) DEFAULT NULL,
  `value_b` int(11) DEFAULT NULL,
  `value_i` int(11) DEFAULT NULL,
  `value_min` double DEFAULT NULL,
  `date_min` datetime DEFAULT NULL,
  `value_max` double DEFAULT NULL,
  `date_max` datetime DEFAULT NULL,
  `transfer_count` int(11) DEFAULT NULL,
  `grouped` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`rec_id`)
) ENGINE=InnoDB AUTO_INCREMENT=147637 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensors_data`
--

LOCK TABLES `sensors_data` WRITE;
/*!40000 ALTER TABLE `sensors_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `sensors_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES (1,'BoxName','Управдом'),(2,'VACAlertMin','170'),(3,'VACAlertMax','245'),(4,'InTempAlertMin','15'),(5,'InTempAlertMax','35'),(6,'Lamp2PIRSensorScript','false'),(7,'Lamp1PIRSensorScript','true'),(8,'Lamp2DarkSensorScript','true'),(9,'Lamp1DarkSensorScript','false'),(10,'Socket1OnCommand433','10044428,200'),(11,'Socket1OffCommand433','10044420,200'),(12,'Lamp1OnCommand433','1222003,370'),(13,'Lamp1OffCommand433','1222004,370'),(14,'Lamp2OnCommand433','2159715,370'),(15,'Lamp2OffCommand433','2159716,370'),(16,'WetSensorAddress433','3526992'),(17,'DoorSensorAddress433','13088038'),(18,'SpeakerNotifications','true'),(19,'StatToEmailScript','false'),(20,'NotificationsEmail',''),(21,'ChargeAlertCritical','30'),(22,'ChargeAlertMinor','50'),(23,'VBatTempAlertMax','50'),(24,'SMSNotifications','true'),(25,'InHumAlertMax','85'),(26,'InHumAlertMin','8'),(27,'LumiosityDarkLevel','15');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_inbox`
--

DROP TABLE IF EXISTS `sms_inbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_inbox` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(140) NOT NULL,
  `sender` varchar(25) NOT NULL,
  `date` datetime NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_inbox`
--

LOCK TABLES `sms_inbox` WRITE;
/*!40000 ALTER TABLE `sms_inbox` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_inbox` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sms_outbox`
--

DROP TABLE IF EXISTS `sms_outbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sms_outbox` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` varchar(70) NOT NULL,
  `recipient` varchar(25) NOT NULL,
  `date` datetime NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sms_outbox`
--

LOCK TABLES `sms_outbox` WRITE;
/*!40000 ALTER TABLE `sms_outbox` DISABLE KEYS */;
/*!40000 ALTER TABLE `sms_outbox` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_stages`
--

DROP TABLE IF EXISTS `test_stages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `test_stages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `stage` int(11) NOT NULL,
  `message` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_stages`
--

LOCK TABLES `test_stages` WRITE;
/*!40000 ALTER TABLE `test_stages` DISABLE KEYS */;
INSERT INTO `test_stages` VALUES (1,2,''),(2,3,''),(3,4,''),(4,5,'');
/*!40000 ALTER TABLE `test_stages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timer`
--

DROP TABLE IF EXISTS `timer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `timer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `period` int(11) DEFAULT NULL,
  `enabled` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timer`
--

LOCK TABLES `timer` WRITE;
/*!40000 ALTER TABLE `timer` DISABLE KEYS */;
INSERT INTO `timer` VALUES (1,'internal_sensors_poll',NULL,10,1),(2,'write_db_data',NULL,300,1),(3,'db_clearing','2014-01-05 01:00:00',86400,1),(4,'get_forecast',NULL,10800,1),(5,'update_display','2014-01-05 01:00:01',60,1),(6,'send_statistics','2014-01-05 00:00:00',86400,1);
/*!40000 ALTER TABLE `timer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `login` varchar(50) NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `salt` varchar(32) NOT NULL,
  `verifier` varchar(66) NOT NULL,
  `status` int(2) NOT NULL,
  `public_key` varchar(256) NOT NULL,
  `keyfile` text,
  `synced` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-12-07 10:18:51
