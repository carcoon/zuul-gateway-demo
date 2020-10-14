create table `gateway_api_define` (
  `id` varchar(50) not null,
  `path` varchar(255) not null,
  `service_id` varchar(50) default null,
  `url` varchar(255) default null,
  `retryable` tinyint(1) default null,
  `enabled` tinyint(1) not null,
  `strip_prefix` int(11) default null,
  `api_name` varchar(255) default null,
  primary key (`id`)
) engine=innodb default charset=utf8;

INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('book', '/book/**', 'server-book', NULL, 0, 1, 1, NULL);
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('pppp', '/pppp/**', NULL, 'http://localhost:8503', 0, 1, 1, NULL);
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('vehicle', '/vehicle/**', 'server-vehicle', NULL, 0, 1, 1, NULL);
INSERT INTO `gateway_api_define`(`id`, `path`, `service_id`, `url`, `retryable`, `enabled`, `strip_prefix`, `api_name`) VALUES ('xxxx', '/xxxx/**', NULL, 'http://localhost:8504', 0, 1, 1, NULL);