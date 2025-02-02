CREATE TABLE IF NOT EXISTS `subscriber` (
  `id` bigint not null AUTO_INCREMENT PRIMARY KEY,
  `email` varchar(256) NOT NULL UNIQUE KEY,
  `confirmed` bit NOT NULL
);
CREATE TABLE IF NOT EXISTS `subscriberSecretPurpose` (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `purpose` varchar(128) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS `subscriberSecret` (
  `id` bigint not null AUTO_INCREMENT PRIMARY KEY,
  `subscriberId` bigint NOT NULL REFERENCES `subscriber` ( `id` ),
  `purposeId` int NOT NULL REFERENCES `subscriberSecretPurpose` ( `id` ),
  `validUntil` datetime NOT NULL,
  `secret` char(64) NOT NULL
);
 
