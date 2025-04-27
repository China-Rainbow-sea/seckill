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



-- ----------------------------
-- Table structure for t_goods
# 创建 t_goods - 商品表
-- ----------------------------
DROP TABLE IF EXISTS `t_goods`;
CREATE TABLE `t_goods`
(
    `id`           BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '商品 id',
    `goods_name`   VARCHAR(16) not null DEFAULT '',
    `goods_title`  VARCHAR(64) not null DEFAULT '' COMMENT '商品标题',
    `goods_img`    VARCHAR(64) not null DEFAULT '' COMMENT '商品图片',
    `goods_detail` LONGTEXT    not null COMMENT '商品详情',
    `goods_price`  DECIMAL(10, 2)       DEFAULT '0.00' COMMENT '商品价格',
    `goods_stock`  INT(11)              DEFAULT '0' COMMENT '商品库存',
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4;


- ----------------------------
-- Records of t_goods
-- ----------------------------
INSERT INTO `t_goods`
VALUES ('1', '整体厨房设计-套件', '整体厨房设计-套件', '/imgs/kitchen.jpg', '整体厨房设计-套件', '15266.00', '100');
INSERT INTO `t_goods`
VALUES ('2', '学习书桌-套件', '学习书桌-套件', '/imgs/desk.jpg', '学习书桌-套件', '5690.00', '100');

# 创建 t_seckill_goods - 秒杀商品
-- ----------------------------
-- Table structure for t_seckill_goods
-- ----------------------------
DROP TABLE IF EXISTS `t_seckill_goods`;


CREATE TABLE `t_seckill_goods`
(
    `id`            BIGINT(20) NOT NULL AUTO_INCREMENT,
    `goods_id`      BIGINT(20)     DEFAULT 0,
    `seckill_price` DECIMAL(10, 2) DEFAULT '0.00',
    `stock_count`   INT(10)        DEFAULT 0,
    `start_date`    DATETIME       DEFAULT NULL,
    `end_date`      DATETIME       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4;


-- ----------------------------
-- Records of t_seckill_goods
-- ----------------------------

INSERT INTO `t_seckill_goods`
VALUES ('1', '1', '5266.00', '0', '2022-11-18 19:36:00', '2022-11-19 09:00:00');

INSERT INTO `t_seckill_goods`
VALUES ('2', '2', '690.00', '10', '2022-11-18 08:00:00', '2022-11-19 09:00:00');



-- ----------------------------
-- Table structure for t_order 普通订单表,记录订单完整信息
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`
(
    `id`               BIGINT(20)     NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT(20)     NOT NULL DEFAULT 0,
    `goods_id`         BIGINT(20)     NOT NULL DEFAULT 0,
    `delivery_addr_id` BIGINT(20)     NOT NULL DEFAULT 0,
    `goods_name`       VARCHAR(16)    NOT NULL DEFAULT '',
    `goods_count`      INT(11)        NOT NULL DEFAULT '0',
    `goods_price`      DECIMAL(10, 2) NOT NULL DEFAULT '0.00',
    `order_channel`    TINYINT(4)     NOT NULL DEFAULT '0' COMMENT '订单渠道 1pc，2Android，
3ios',
    `status`           TINYINT(4)     NOT NULL DEFAULT '0' COMMENT '订单状态：0 新建未支付 1 已支付
2 已发货 3 已收货 4 已退款 5 已完成',
    `create_date`      DATETIME                DEFAULT NULL,
    `pay_date`         DATETIME                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 600
  DEFAULT CHARSET = utf8mb4;



-- ----------------------------
-- Table structure for t_seckill_order 秒杀订单表,记录某用户 id,秒杀的商品 id,及其订单 id
-- ----------------------------
DROP TABLE IF EXISTS `t_seckill_order`;
CREATE TABLE `t_seckill_order`
(
    `id`       BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id`  BIGINT(20) NOT NULL DEFAULT 0,
    `order_id` BIGINT(20) NOT NULL DEFAULT 0,
    `goods_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `seckill_uid_gid` (`user_id`, `goods_id`) USING BTREE COMMENT ' 用户 id，商
品 id 的唯一索引，解决同一个用户多次抢购'
) ENGINE = INNODB
  AUTO_INCREMENT = 300
  DEFAULT CHARSET = utf8mb4;