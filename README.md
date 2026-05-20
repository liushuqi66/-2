# Smart Interview Platform
## 智能面试评估平台

基于 **Spring Cloud 微服务架构** 的AI智能面试评估系统。

### 技术栈
- **架构**: Spring Cloud 2023.0.0 + Spring Cloud Alibaba 2023.0.1.0
- **核心框架**: Spring Boot 3.2.0, Java 21
- **服务治理**: Nacos 2.3.0 (注册中心/配置中心)
- **API 网关**: Spring Cloud Gateway + Knife4j 4.4.0 文档聚合
- **数据持久层**: MyBatis-Plus 3.5.5 + MySQL 8.0
- **缓存**: Redis 7 + Redisson 3.24.3 (分布式锁)
- **消息队列**: RabbitMQ 3 (异步评估)
- **安全**: BCrypt 密码加密 + JWT 无状态认证 + XSS 过滤

### 模块结构
| 模块 | 端口 | 说明 |
|------|------|------|
| smart-interview-gateway | 8080 | API网关：鉴权、限流、CORS、日志 |
| smart-interview-user | 8081 | 用户服务：注册/登录/简历管理 |
| smart-interview-interview | 8082 | 面试服务：智能组卷/答题/评估触发 |
| smart-interview-ai | 8083 | AI评估服务：自动评分/报告生成 |
| smart-interview-common | - | 公共模块：工具类/异常/DTO/枚举 |

### 核心功能
- **智能组卷算法**: 基于难度分层 + 知识点去重的贪心策略
- **分布式锁**: Redisson + Redis setIfAbsent 双保险
- **接口幂等**: 答题提交 Redis 防重复机制
- **异步评估**: RabbitMQ 消息驱动 AI 评分
- **API 文档**: Knife4j 网关层聚合多服务文档

### 快速启动
```bash
# 1. 启动基础设施
docker-compose -f docker/docker-compose.yml up -d

# 2. 初始化数据库
mysql -h localhost -P 3307 -u root -proot123 < smart-interview-common/src/main/resources/sql/init.sql

# 3. 启动服务 (需先启动 Nacos)
# 依次启动: Gateway -> User -> Interview -> AI
```

### 测试账户
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 面试官 | interviewer01 | 123456 |
| 求职者 | candidate01 | 123456 |

### 开发团队
Zhang Wei & Team - 2026
