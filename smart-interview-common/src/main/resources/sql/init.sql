-- ============================================
-- 智能面试评估平台 - 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_interview;

-- ============================================
-- 用户服务表
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(256) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-候选人 1-面试官 2-管理员',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 简历表
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '简历ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `education` VARCHAR(64) DEFAULT NULL COMMENT '学历',
    `school` VARCHAR(128) DEFAULT NULL COMMENT '毕业院校',
    `major` VARCHAR(128) DEFAULT NULL COMMENT '专业',
    `experience_years` INT DEFAULT 0 COMMENT '工作年限',
    `skills` TEXT COMMENT '技能标签（JSON数组）',
    `summary` TEXT COMMENT '个人简介',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- ============================================
-- 面试服务表
-- ============================================

-- 题库表
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `title` VARCHAR(512) NOT NULL COMMENT '题目标题',
    `content` TEXT NOT NULL COMMENT '题目内容',
    `type` VARCHAR(32) NOT NULL COMMENT '题目类型：SINGLE_CHOICE-单选 MULTI_CHOICE-多选 ESSAY-简答 CODING-编程',
    `difficulty` TINYINT NOT NULL COMMENT '难度：1-简单 2-中等 3-困难',
    `tags` VARCHAR(512) DEFAULT NULL COMMENT '知识点标签（逗号分隔）',
    `answer` TEXT COMMENT '参考答案',
    `score` INT NOT NULL DEFAULT 5 COMMENT '默认分值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_difficulty` (`difficulty`),
    KEY `idx_tags` (`tags`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 面试记录表
DROP TABLE IF EXISTS `interview_record`;
CREATE TABLE `interview_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面试记录ID',
    `candidate_id` BIGINT NOT NULL COMMENT '候选人ID',
    `interviewer_id` BIGINT DEFAULT NULL COMMENT '面试官ID',
    `position` VARCHAR(128) DEFAULT NULL COMMENT '面试岗位',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待开始 1-进行中 2-已完成 3-已取消',
    `total_score` INT DEFAULT 0 COMMENT '总分',
    `difficulty_level` TINYINT NOT NULL DEFAULT 2 COMMENT '面试难度等级',
    `question_count` INT NOT NULL DEFAULT 10 COMMENT '题目数量',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_candidate_id` (`candidate_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';

-- 面试答题记录表
DROP TABLE IF EXISTS `interview_answer`;
CREATE TABLE `interview_answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答题记录ID',
    `interview_id` BIGINT NOT NULL COMMENT '面试记录ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `candidate_answer` TEXT COMMENT '候选人答案',
    `score` INT DEFAULT 0 COMMENT '得分',
    `ai_feedback` TEXT COMMENT 'AI评价反馈',
    `answer_time` INT DEFAULT 0 COMMENT '答题耗时（秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_interview_id` (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试答题记录表';

-- ============================================
-- 初始化测试数据
-- ============================================

-- 插入测试用户（密码均为 123456 的 BCrypt 加密）
-- 注意：BCrypt 每次生成的 salt 不同，所有 hash 都对应密码 "123456"（60字符标准 BCrypt 哈希）
-- 如果数据库初始化后重新运行此脚本，密码将变为新 hash，需通过 fix_passwords.py 脚本同步更新数据库
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `role`) VALUES
('admin', '$2b$10$l04F1M58Rl2tpo66we/pq.zVU1SpbNZNic6FcXjCXZuImmJ1ksEs.', '管理员', 'admin@smart-interview.com', 2),
('interviewer01', '$2b$10$l04F1M58Rl2tpo66we/pq.zVU1SpbNZNic6FcXjCXZuImmJ1ksEs.', '张面试官', 'zhang@smart-interview.com', 1),
('candidate01', '$2b$10$l04F1M58Rl2tpo66we/pq.zVU1SpbNZNic6FcXjCXZuImmJ1ksEs.', '刘书岐', 'candidate@smart-interview.com', 0);

-- 插入测试题目
INSERT INTO `question` (`title`, `content`, `type`, `difficulty`, `tags`, `answer`, `score`) VALUES
('Java中ArrayList和LinkedList的区别', '请详细说明ArrayList和LinkedList在底层数据结构、查询效率、增删效率方面的区别，以及各自的适用场景。', 'ESSAY', 1, 'Java,集合', 'ArrayList基于动态数组，查询O(1)，增删O(n)；LinkedList基于双向链表，查询O(n)，增删O(1)。ArrayList适合随机访问，LinkedList适合频繁增删。', 10),
('Spring Boot自动配置原理', '请解释Spring Boot的自动配置（Auto-Configuration）是如何工作的，涉及@EnableAutoConfiguration和spring.factories的作用。', 'ESSAY', 2, 'Spring Boot,自动配置', '@SpringBootApplication包含@EnableAutoConfiguration，通过@Import导入AutoConfigurationImportSelector，扫描META-INF/spring.factories中的配置类，根据@Conditional条件注解决定是否生效。', 15),
('Redis缓存穿透解决方案', '什么是缓存穿透？请列举至少3种解决方案并说明各自的优缺点。', 'ESSAY', 2, 'Redis,缓存', '缓存穿透指查询不存在的数据导致请求穿透缓存直接打DB。解决方案：1.布隆过滤器（内存高效但存在误判）；2.缓存空值（简单但有内存开销）；3.参数校验（前置拦截但范围有限）。', 15),
('微服务中分布式事务如何处理', '请描述在微服务架构中处理分布式事务的几种常见方案，包括2PC、TCC、Saga模式，以及各自的适用场景。', 'ESSAY', 3, '微服务,分布式事务', '2PC：两阶段提交，强一致性，性能差，适合单体到微服务过渡；TCC：Try-Confirm-Cancel，最终一致性，需要业务实现补偿；Saga：事务编排，长事务拆分为本地事务+补偿，适合长流程。', 20),
('JVM垃圾回收算法有哪些', '请列举JVM主要的垃圾回收算法并简述其工作原理。', 'ESSAY', 1, 'JVM,GC', '1.标记-清除：标记存活对象，清除未标记，产生碎片；2.标记-整理：标记后整理到一端，无碎片；3.复制算法：分两块区域，存活对象复制到另一块；4.分代收集：新生代用复制，老年代用标记-整理。', 10),
('MySQL索引优化原则', '请说明MySQL索引设计的基本原则和常见的索引失效场景。', 'ESSAY', 2, 'MySQL,索引', '原则：最左前缀、选择性高的列、覆盖索引、避免过多索引。失效场景：like以%开头、OR条件非索引列、类型隐式转换、函数/运算操作、!=或<>、IS NULL/IS NOT NULL、NOT IN等。', 15),
('RabbitMQ消息可靠性如何保证', '请描述RabbitMQ中保证消息不丢失的机制，包括生产者确认、持久化、消费者确认三个方面。', 'ESSAY', 2, '消息队列,RabbitMQ', '生产者确认：publisher confirm模式，消息投递到交换机后异步回调；持久化：队列持久化+消息持久化（deliveryMode=2）；消费者确认：手动ACK模式，处理成功后basicAck，失败basicNack重新入队。', 15),
('什么是CAP理论', '请简述分布式系统中的CAP理论，并说明在实际系统中如何取舍。', 'ESSAY', 1, '分布式,CAP', 'CAP：Consistency（一致性）、Availability（可用性）、Partition Tolerance（分区容错性）。P必须保证，因此实际是CP或AP的取舍。ZooKeeper选CP，Eureka选AP。', 10),
('Spring Cloud Gateway和Zuul的区别', '请比较Spring Cloud Gateway和Netflix Zuul的区别，并说明Gateway的优势。', 'ESSAY', 2, 'Spring Cloud,网关', 'Gateway基于WebFlux（响应式编程），非阻塞IO，性能更好；Zuul 1.x基于Servlet，阻塞IO。Gateway支持更灵活的路由断言工厂和过滤器链，集成Reactor模式。', 15),
('如何设计一个高并发秒杀系统', '请从系统架构层面描述高并发秒杀系统的设计要点。', 'ESSAY', 3, '高并发,系统设计', '1.前端：静态化+CDN+按钮防抖；2.网关层：限流（令牌桶/漏桶）；3.业务层：Redis预减库存+Lua脚本原子操作+RabbitMQ异步下单；4.数据库层：乐观锁防止超卖+读写分离。', 20);
