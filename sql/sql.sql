DROP TABLE IF EXISTS `seckill_user`;
CREATE TABLE `seckill_user`
(
    `id`              BIGINT(20)   NOT NULL COMMENT '用户 ID, 设为主键, 唯一 手机号',
    `nickname`        VARCHAR(255) NOT NULL DEFAULT '',
    `password`        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'MD5(MD5(pass 明 文 + 固 定
salt)+salt)',
    `slat`            VARCHAR(10)  NOT NULL DEFAULT '',
    `head`            VARCHAR(128) NOT NULL DEFAULT '' COMMENT '头像',
    `register_date`   DATETIME              DEFAULT NULL COMMENT '注册时间',
    `last_login_date` DATETIME              DEFAULT NULL COMMENT '最后一次登录时间',
    `login_count`     INT(11)               DEFAULT '0' COMMENT '登录次数',
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4;

--   第一个用户 id = 13300000000,密码: 123456