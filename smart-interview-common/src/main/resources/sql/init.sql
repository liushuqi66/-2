-- Smart Interview Platform - DDL + Seed Data
CREATE DATABASE IF NOT EXISTS smart_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_interview;

CREATE TABLE IF NOT EXISTS `user` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`username` VARCHAR(50) NOT NULL,`password` VARCHAR(255) NOT NULL,`real_name` VARCHAR(50),`email` VARCHAR(100),`phone` VARCHAR(20),`role` VARCHAR(20) DEFAULT 'candidate',`avatar_url` VARCHAR(500),`status` TINYINT DEFAULT 1,`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,UNIQUE KEY `uk_username` (`username`),KEY `idx_email` (`email`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `resume` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`user_id` BIGINT NOT NULL,`education` VARCHAR(50),`school` VARCHAR(100),`major` VARCHAR(100),`experience_years` INT DEFAULT 0,`skills` TEXT,`summary` TEXT,`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,KEY `idx_user_id` (`user_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `question` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`title` VARCHAR(200) NOT NULL,`content` TEXT NOT NULL,`reference_answer` TEXT,`difficulty` TINYINT NOT NULL DEFAULT 2,`tags` VARCHAR(500),`knowledge_points` VARCHAR(500),`type` VARCHAR(20) DEFAULT 'open',`status` TINYINT DEFAULT 1,`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,KEY `idx_difficulty` (`difficulty`),KEY `idx_tags` (`tags`(191))) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interview_record` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`user_id` BIGINT NOT NULL,`position` VARCHAR(100) NOT NULL,`difficulty_level` TINYINT NOT NULL DEFAULT 2,`status` TINYINT NOT NULL DEFAULT 0,`score` DECIMAL(5,2),`paper_id` VARCHAR(50),`start_time` DATETIME,`end_time` DATETIME,`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,KEY `idx_user_id` (`user_id`),KEY `idx_status` (`status`),KEY `idx_user_status` (`user_id`,`status`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `interview_answer` (`id` BIGINT AUTO_INCREMENT PRIMARY KEY,`interview_id` BIGINT NOT NULL,`question_id` BIGINT NOT NULL,`answer` TEXT,`score` DECIMAL(5,2),`feedback` TEXT,`question_order` INT DEFAULT 0,`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,KEY `idx_interview_id` (`interview_id`),UNIQUE KEY `uk_interview_question` (`interview_id`,`question_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== Seed Data ==========
INSERT INTO `user` (`username`,`password`,`real_name`,`email`,`role`) VALUES
('admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','管理员','admin@si.com','admin'),
('interviewer01','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','面试官张','zh@si.com','interviewer'),
('candidate01','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','求职者李','li@si.com','candidate')
ON DUPLICATE KEY UPDATE username=VALUES(username);

INSERT INTO `question` (`title`,`content`,`reference_answer`,`difficulty`,`tags`,`knowledge_points`) VALUES
('Java面向对象三大特性','简述封装、继承和多态，并举例说明每种特性的应用场景。','封装：将数据和方法封装在类中，通过访问修饰符控制可见性。继承：子类复用父类的属性和方法，Java单继承。多态：同一方法在不同对象中有不同表现，编译时(重载)和运行时(重写)。',1,'Java,基础,OOP','封装,继承,多态'),
('Spring Boot核心注解','解释@SpringBootApplication注解的组成及其自动配置原理。','组合注解：@Configuration+@EnableAutoConfiguration+@ComponentScan。@EnableAutoConfiguration通过spring.factories加载自动配置类。',1,'Spring,注解','自动配置'),
('Redis五种数据结构','列举Redis的五种基本数据结构及其典型应用场景。','String:缓存计数器;Hash:用户信息;List:消息队列;Set:共同好友;ZSet:排行榜。',2,'Redis,缓存','数据结构,缓存策略'),
('微服务架构设计原则','在设计微服务架构时需遵循哪些核心原则？结合Spring Cloud说明实践方式。','单一职责：每个服务专注一个业务领域。服务自治：独立部署和扩展。去中心化：微服务独立决策。容错设计：断路器模式。基础设施自动化：CI/CD。',3,'微服务,架构,SpringCloud','微服务设计,服务拆分'),
('JVM内存模型与垃圾回收','描述JVM内存区域划分及常见GC算法。','堆(新生代Eden/S0/S1+老年代)、方法区(元空间)、虚拟机栈、本地方法栈、程序计数器。GC算法：标记清除、标记整理、复制(Copying)、分代收集。',3,'Java,JVM,GC','内存模型,垃圾回收'),
('MySQL索引优化策略','如何优化MySQL查询性能？从索引设计和SQL编写角度分析。','最左前缀原则、覆盖索引、避免SELECT*、使用EXPLAIN分析执行计划、定期ANALYZE TABLE、注意索引区分度（>0.1为佳）。',2,'MySQL,数据库,索引','B+树,查询优化'),
('RabbitMQ消息可靠性','如何保证消息的可靠投递和消费？','生产者确认(Publisher Confirm)、消费者手动ACK、消息+队列+交换机持久化、死信队列处理失败消息、消息补偿机制。',2,'RabbitMQ,中间件','可靠性,ACK机制'),
('CAP理论实践','简述CAP理论并举例分布式系统的取舍。','一致性/可用性/分区容错性不可兼得。场景：Nacos AP保证高可用、ZooKeeper CP保证强一致性、Eureka AP。根据业务选择。',3,'分布式,CAP','一致性,高可用'),
('Spring Cloud Gateway','描述Gateway核心组件及请求处理流程。','三大核心：Route(路由)、Predicate(断言)、Filter(过滤器)。流程：请求匹配路由→断言判断→过滤器链处理→转发后端。',2,'Gateway,SpringCloud','路由,过滤器'),
('高并发系统设计思路','面对高并发场景，从哪些方面设计系统？','多级缓存(CDN+本地+Redis)、读写分离+分库分表、异步化(MQ)、限流熔断(Sentinel)、无状态设计、连接池优化、预热机制。',3,'高并发,系统设计','架构优化,性能')
ON DUPLICATE KEY UPDATE title=VALUES(title);
