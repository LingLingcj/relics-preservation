# 文物遗产保护系统 (Relics Preservation System)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6.0+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> 重庆大学23级软件综合实训项目 - 基于领域驱动设计(DDD)的现代化文物遗产保护与学习平台

## 📖 项目简介

文物遗产保护系统是一个集文物展示、用户交互、个人学习于一体的综合性平台。系统采用领域驱动设计(DDD)架构，提供文物浏览、收藏管理、评论互动、个人收藏馆、学习记录、成就系统等丰富功能，旨在通过现代化的技术手段促进文物文化的传承与普及。

### 🛠️ 技术栈

#### 核心框架
- **Spring Boot 3.0+** - 现代化Java应用框架，提供自动配置和生产就绪特性
- **Spring Security 6.0+** - 企业级安全框架，支持OAuth2、JWT认证授权
- **Spring Data JPA** - 简化数据访问层开发，支持自动查询生成
- **MyBatis 3.5+** - 灵活的SQL映射框架，支持复杂查询和动态SQL

#### 数据存储
- **MySQL 8.0+** - 主数据库，支持JSON字段、窗口函数等现代特性
- **Redis 7.0+** - 分布式缓存，支持多种数据结构和持久化
- **Elasticsearch 8.0+** - 全文搜索引擎，提供强大的文物搜索能力

#### 消息与通信
- **RabbitMQ 3.11+** - 消息队列中间件，支持异步处理和事件驱动
- **WebSocket** - 实时通信协议，支持在线用户交互
- **RESTful API** - 标准化API设计，支持OpenAPI 3.0规范

#### 监控与运维
- **Docker + Docker Compose** - 容器化部署和编排

#### 开发工具
- **Maven 3.8+** - 项目构建和依赖管理
- **JUnit 5 + Mockito** - 单元测试和模拟框架
- **Swagger/OpenAPI 3.0** - API文档生成

### 🎯 核心特性

- **🏛️ 文物展示系统** - 高质量文物图片展示、详细信息介绍、多维度分类浏览
- **👤 用户交互系统** - 文物收藏、评论互动、社交分享功能
- **🎨 个人收藏馆** - 自定义主题收藏馆、个人笔记、标签管理
- **📚 学习记录系统** - 学习进度跟踪、知识点掌握、学习路径推荐
- **🏆 成就激励系统** - 多维度成就体系、积分奖励、等级晋升
- **🤖 智能推荐** - 基于用户行为的个性化文物推荐
- **📊 数据分析** - 学习分析报告、用户行为统计


## 🚀 快速开始

### 环境要求

- **Java**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Docker**: 20.0+ (可选)


#### 5. 访问应用
- **API文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

### Docker 部署

```bash
# 构建镜像
docker build -t relics-preservation:latest .

# 启动完整环境
docker-compose up -d

# 查看日志
docker-compose logs -f app
```

## 📁 项目结构

```
relics-preservation/
├── docs/                           # 项目文档
│   ├── api/                       # API文档
│   ├── pgvector/                  # 向量数据库脚本
│   ├── mysql/                     # 数据库脚本
|   └── plugins                    # elasticsearch插件 ik分词器
|
├── relics-preservation-app/        # 应用启动模块
│   ├── src/main/java/            # 应用配置
│   └── src/main/resources/       # 配置文件
|
├── relics-preservation-domain/     # 领域层
│   ├── src/main/java/
│   │   ├── model/                # 领域模型
│   │   ├── service/              # 领域服务
│   │   └── adapter/              # 端口接口
│   └── src/test/java/            # 领域层测试
|
├── relics-preservation-infrastructure/ # 基础设施层
│   └── src/main/java/
│       ├── repository/           # 仓储实现
│       ├── dao/                  # 数据访问对象
│       └── cache/                # 缓存服务
│   
├── relics-preservation-trigger/    # 触发器层
│   └── src/main/java/
│       ├── http/                 # HTTP控制器
│       ├── gateway/              # mqtt发送网关
│       ├── job/                  # 定时任务
│       ├── listener/             # 事件监听器
|       └── websocket/            # 前端通信
|        
├── relics-preservation-types/      # 通用类型
│   └── src/main/java/
│       ├── common/                # 通用模型
│       └── event/                # 领域事件
├── docker-compose.yml             # Docker编排文件
├── Dockerfile                     # Docker镜像构建文件
├── pom.xml                        # Maven主配置
└── README.md                      # 项目说明
```

## 🎯 核心功能模块

### 1. 文物管理系统
- **文物信息管理**: 文物基本信息、图片、历史背景
- **分类体系**: 多维度分类（朝代、材质、类型等）
- **搜索功能**: 全文搜索、筛选、排序
- **权限控制**: 管理员审核、内容管理

### 2. 用户交互系统 (DDD重构)
基于领域驱动设计重构的用户交互系统，拆分为三个独立的聚合根：

#### 2.1 用户收藏聚合根 (UserFavorites)
```java
// 核心功能
- 文物收藏/取消收藏
- 收藏列表管理
- 收藏统计分析
- 收藏历史记录

// 技术特性
- 增量保存机制
- Redis缓存优化
- 事务一致性保证
```

#### 2.2 用户评论聚合根 (UserComments)
```java
// 核心功能
- 文物评论发布
- 评论状态管理
- 评论审核机制
- 评论统计分析

// 技术特性
- 批量操作支持
- 缓存策略优化
```

#### 2.3 收藏馆管理聚合根 (GalleryManager)
```java
// 核心功能
- 个人收藏馆创建
- 主题风格定制
- 收藏馆分享
- 收藏馆统计

// 技术特性
- 13种预定义主题
- 自定义主题支持
- 分享码机制
```

### 3. 个人收藏馆增强功能 ⭐ (新增)
基于功能增强分析报告实现的个性化学习平台：

#### 3.1 个人笔记系统
```java
// 笔记类型
- 一般笔记 (GENERAL)
- 研究笔记 (RESEARCH)
- 灵感笔记 (INSPIRATION)
- 疑问笔记 (QUESTION)
- 总结笔记 (SUMMARY)

// 学习状态
- 未开始 → 学习中 → 已复习 → 已掌握 → 专家级

// 功能特性
- 个人评分系统 (1-5星)
- 标签管理
- 关键点记录
- 笔记搜索
```

#### 3.2 学习记录系统
```java
// 学习类型
- 浏览学习 (BROWSE)
- 深入学习 (DETAILED_STUDY)
- 对比学习 (COMPARATIVE_STUDY)
- 专题研究 (RESEARCH)
- 复习回顾 (REVIEW)

// 学习活动跟踪
- 查看详情、查看图片、阅读描述
- 记录笔记、添加收藏、搜索相关
- 分享文物、评分文物

// 学习效果评估
- 专注度、理解度、记忆度、兴趣度评分
- 知识点掌握情况
- 学习时长统计
```

#### 3.3 成就系统
```java
// 成就类型 (7种)
- 收藏成就、学习成就、社交成就
- 探索成就、专业成就、时间成就、特殊成就

// 成就等级 (6级)
- 青铜 → 白银 → 黄金 → 铂金 → 钻石 → 传奇

// 成就条件 (10种)
- 收藏数量、学习时长、笔记数量、连续天数
- 分类专家、分享次数、评分次数、收藏馆数量
- 知识掌握、社交互动

// 奖励机制
- 积分奖励、徽章收集、等级晋升
```

### 4. 智能推荐系统
- **个性化推荐**: 基于用户行为的文物推荐
- **学习路径**: 智能学习路径规划
- **热门推荐**: 热门文物和收藏馆推荐
- **相似推荐**: 基于文物特征的相似推荐

### 5. 数据分析系统
- **用户行为分析**: 浏览、收藏、学习行为统计
- **学习效果分析**: 学习进度、知识掌握情况
- **内容分析**: 文物热度、分类统计
- **系统监控**: 性能指标、错误监控

## 🔧 开发指南

### 代码规范

#### 1. 包结构规范
```java
com.ling.domain.{模块}
├── model/
│   ├── entity/          # 聚合根和实体
│   ├── valobj/          # 值对象
│   └── aggregate/       # 聚合
├── service/             # 领域服务
├── adapter/             # 端口接口
└── event/               # 领域事件
```

#### 2. 命名规范
- **聚合根**: 使用名词，如 `UserFavorites`, `UserComments`
- **值对象**: 使用名词，如 `PersonalNote`, `Achievement`
- **领域服务**: 使用动词+Service，如 `UserInteractionService`
- **仓储接口**: 使用I+名词+Repository，如 `IUserFavoritesRepository`

#### 3. DDD设计原则
- **聚合根**: 确保事务边界和一致性
- **值对象**: 不可变对象，包含业务逻辑
- **领域服务**: 处理跨聚合根的业务逻辑
- **仓储模式**: 封装数据访问逻辑

### 测试策略

#### 1. 单元测试
```bash
# 运行所有单元测试
mvn test

# 运行特定模块测试
mvn test -pl relics-preservation-domain

# 生成测试报告
mvn test jacoco:report
```

#### 2. 集成测试
```bash
# 运行集成测试
mvn verify -P integration-test

# 使用TestContainers进行数据库测试
mvn test -Dtest=*IntegrationTest
```

### 性能优化

#### 1. 缓存策略
```java
// 多层缓存设计
- L1: 本地缓存 (Caffeine)
- L2: 分布式缓存 (Redis)
- L3: 数据库缓存 (MySQL Query Cache)

// 缓存更新策略
- 写入时更新 (Write-Through)
- 延迟写入 (Write-Behind)
- 失效时更新 (Cache-Aside)
```

#### 2. 数据库优化
```sql
-- 索引优化
CREATE INDEX idx_user_favorites_username_status ON user_favorites(username, status);
CREATE INDEX idx_user_comments_relics_id_status ON user_comments(relics_id, status);
CREATE INDEX idx_collection_gallery_username_status ON collection_gallery(username, status);

-- 分区表设计
PARTITION BY RANGE (YEAR(create_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026)
);
```

#### 3. 异步处理
```java
// 消息队列异步处理
@RabbitListener(queues = "user.interaction.queue")
public void handleUserInteraction(UserInteractionEvent event) {
    // 异步处理用户交互事件
}

// 定时任务
@Scheduled(cron = "0 0 2 * * ?")
public void generateDailyReport() {
    // 生成每日统计报告
}
```

## 📊 API 文档

### 核心API接口

#### 1. 用户交互API
```http
# 添加收藏
POST /api/v1/user-interaction/favorite
Content-Type: application/json
{
    "username": "user123",
    "relicsId": 1001
}

# 添加评论
POST /api/v1/user-interaction/comment
Content-Type: application/json
{
    "username": "user123",
    "relicsId": 1001,
    "content": "这件文物很有历史价值"
}

# 创建收藏馆
POST /api/v1/user-interaction/gallery
Content-Type: application/json
{
    "username": "user123",
    "name": "我的青铜器收藏",
    "description": "收集各朝代青铜器",
    "theme": "BRONZE",
    "isPublic": true
}
```

#### 2. 个人收藏馆增强API ⭐
```http
# 添加个人笔记
POST /api/v1/personal-gallery/notes
Content-Type: application/json
{
    "username": "user123",
    "galleryId": "gallery_001",
    "relicsId": 1001,
    "title": "青铜鼎学习笔记",
    "content": "这是一件西周时期的青铜鼎...",
    "noteType": "RESEARCH"
}

# 开始学习记录
POST /api/v1/personal-gallery/learning/start
Content-Type: application/json
{
    "username": "user123",
    "relicsId": 1001,
    "learningType": "DETAILED_STUDY"
}

# 获取用户成就
GET /api/v1/personal-gallery/achievements?username=user123

# 获取学习分析报告
GET /api/v1/personal-gallery/analysis?username=user123
```

#### 3. 文物管理API
```http
# 获取文物列表
GET /api/v1/relics?page=1&size=20&category=bronze

# 获取文物详情
GET /api/v1/relics/{relicsId}

# 搜索文物
GET /api/v1/relics/search?keyword=青铜&dynasty=西周
```

### API文档访问
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🗄️ 数据库设计

### 核心数据表

#### 1. 用户交互相关表
```sql
-- 用户收藏表
CREATE TABLE user_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    relics_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    status TINYINT DEFAULT 0,
    INDEX idx_username_status (username, status),
    INDEX idx_relics_id (relics_id)
);

-- 用户评论表
CREATE TABLE user_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    relics_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status TINYINT DEFAULT 0,
    create_time DATETIME NOT NULL,
    INDEX idx_username_status (username, status),
    INDEX idx_relics_id_status (relics_id, status)
);

-- 收藏馆表
CREATE TABLE collection_gallery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gallery_id VARCHAR(36) UNIQUE NOT NULL,
    username VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    theme VARCHAR(32) NOT NULL,
    display_style VARCHAR(32) NOT NULL,
    relics_ids TEXT,
    is_public TINYINT DEFAULT 0,
    share_code VARCHAR(32) UNIQUE,
    create_time DATETIME NOT NULL,
    INDEX idx_username_status (username, status),
    INDEX idx_theme (theme),
    INDEX idx_share_code (share_code)
);
```

#### 2. 个人收藏馆增强表 ⭐
```sql
-- 个人笔记表 (计划中)
CREATE TABLE personal_notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id VARCHAR(36) UNIQUE NOT NULL,
    username VARCHAR(32) NOT NULL,
    relics_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    note_type VARCHAR(32) NOT NULL,
    learning_status VARCHAR(32) NOT NULL,
    rating TINYINT,
    tags JSON,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 学习记录表 (计划中)
CREATE TABLE learning_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id VARCHAR(36) UNIQUE NOT NULL,
    username VARCHAR(32) NOT NULL,
    relics_id BIGINT NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    learning_type VARCHAR(32) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    duration_seconds BIGINT,
    activities JSON,
    effectiveness JSON,
    create_time DATETIME NOT NULL
);

-- 用户成就表 (计划中)
CREATE TABLE user_achievements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    achievement_id VARCHAR(36) NOT NULL,
    current_progress INT DEFAULT 0,
    target_progress INT NOT NULL,
    is_unlocked TINYINT DEFAULT 0,
    unlocked_time DATETIME,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_achievement (username, achievement_id)
);
```


## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 👥 团队成员

- **项目开发者**: [LingRJ](https://github.com/LingLingcj)


## 🙏 致谢

感谢以下开源项目和技术社区的支持：

- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架
- [MyBatis](https://mybatis.org/) - 持久层框架
- [Redis](https://redis.io/) - 缓存数据库
- [MySQL](https://www.mysql.com/) - 关系型数据库
- [Docker](https://www.docker.com/) - 容器化平台

---

**重庆大学23级软件综合实训项目** | **文物遗产保护设计** | **2025年**
