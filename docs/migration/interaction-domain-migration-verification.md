# 交互领域迁移验证指南

## 📋 迁移概述

本次迁移将原有的 `favorite` 和 `comment` 两个独立领域合并为统一的 `interaction` 领域，实现了更好的 DDD 设计和业务内聚性。

## 🔍 验证步骤

### 1. 数据库验证

#### 1.1 检查新表是否创建成功
```sql
-- 检查新表结构
SHOW TABLES LIKE 'user_%';
DESCRIBE user_favorites;
DESCRIBE user_comments;
DESCRIBE interaction_statistics;
DESCRIBE user_activities;
```

#### 1.2 验证数据迁移完整性
```sql
-- 检查收藏数据迁移
SELECT COUNT(*) as new_favorite_count FROM user_favorites WHERE status = 0;
SELECT COUNT(*) as old_favorite_count FROM relics_favorite WHERE status = 0; -- 如果旧表还存在

-- 检查评论数据迁移
SELECT COUNT(*) as new_comment_count FROM user_comments WHERE status = 0;
SELECT COUNT(*) as old_comment_count FROM relics_comment WHERE status = 0; -- 如果旧表还存在

-- 检查统计数据
SELECT * FROM interaction_statistics ORDER BY total_interactions DESC LIMIT 10;
```

#### 1.3 验证数据一致性
```sql
-- 验证收藏统计一致性
SELECT 
    relics_id,
    COUNT(*) as actual_count,
    (SELECT favorite_count FROM interaction_statistics WHERE relics_id = uf.relics_id) as cached_count
FROM user_favorites uf 
WHERE status = 0 
GROUP BY relics_id 
HAVING actual_count != cached_count;

-- 验证评论统计一致性
SELECT 
    relics_id,
    COUNT(*) as actual_count,
    (SELECT comment_count FROM interaction_statistics WHERE relics_id = uc.relics_id) as cached_count
FROM user_comments uc 
WHERE status = 0 AND comment_status = 1
GROUP BY relics_id 
HAVING actual_count != cached_count;
```

### 2. 应用程序验证

#### 2.1 编译验证
```bash
# 在项目根目录执行
mvn clean compile

# 检查是否有编译错误
mvn test-compile
```

#### 2.2 启动验证
```bash
# 启动应用程序
mvn spring-boot:run

# 检查启动日志，确保没有Bean注入错误
```

#### 2.3 API 功能验证

**新的交互API测试：**
```bash
# 测试收藏功能
curl -X POST "http://localhost:8080/api/v1/interactions/favorites" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"relicsId": 1, "favorite": true}'

# 测试评论功能
curl -X POST "http://localhost:8080/api/v1/interactions/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"relicsId": 1, "content": "这是一个测试评论"}'

# 测试统计功能
curl -X GET "http://localhost:8080/api/v1/interactions/statistics" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**向后兼容API测试：**
```bash
# 测试旧的收藏API
curl -X POST "http://localhost:8080/api/v1/favorites" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"relicsId": 1}'

# 测试旧的评论API
curl -X POST "http://localhost:8080/api/v1/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"relicsId": 1, "content": "这是一个测试评论"}'
```

### 3. 性能验证

#### 3.1 查询性能测试
```sql
-- 测试收藏查询性能
EXPLAIN SELECT * FROM user_favorites WHERE username = 'testuser' AND status = 0;

-- 测试评论查询性能
EXPLAIN SELECT * FROM user_comments WHERE relics_id = 1 AND status = 0 AND comment_status = 1;

-- 测试统计查询性能
EXPLAIN SELECT * FROM interaction_statistics WHERE relics_id = 1;
```

#### 3.2 批量操作性能测试
```bash
# 使用JMeter或其他工具测试批量收藏状态检查
# 测试并发用户的交互操作性能
```

### 4. 业务逻辑验证

#### 4.1 领域事件验证
```bash
# 检查应用日志，确认领域事件正确发布
grep "UserFavoritedRelicsEvent" application.log
grep "UserCommentedOnRelicsEvent" application.log
grep "CommentDeletedEvent" application.log
```

#### 4.2 业务规则验证
- 验证用户不能重复收藏同一文物
- 验证评论内容长度限制
- 验证敏感词检测功能
- 验证评论审核流程

### 5. 数据完整性验证

#### 5.1 外键约束验证
```sql
-- 检查是否存在无效的文物ID引用
SELECT DISTINCT uf.relics_id 
FROM user_favorites uf 
LEFT JOIN relics r ON uf.relics_id = r.relics_id 
WHERE r.relics_id IS NULL AND uf.status = 0;

-- 检查是否存在无效的用户名引用
SELECT DISTINCT uf.username 
FROM user_favorites uf 
LEFT JOIN user u ON uf.username = u.username 
WHERE u.username IS NULL AND uf.status = 0;
```

#### 5.2 数据类型验证
```sql
-- 检查评论ID的唯一性
SELECT comment_id, COUNT(*) 
FROM user_comments 
WHERE status = 0 
GROUP BY comment_id 
HAVING COUNT(*) > 1;
```

## ✅ 验证清单

### 数据库层面
- [ ] 新表创建成功
- [ ] 数据迁移完整
- [ ] 索引创建正确
- [ ] 统计数据准确
- [ ] 视图创建成功

### 应用程序层面
- [ ] 编译无错误
- [ ] 启动无异常
- [ ] Bean注入正确
- [ ] 配置加载成功

### API层面
- [ ] 新API功能正常
- [ ] 向后兼容API正常
- [ ] 错误处理正确
- [ ] 响应格式一致

### 业务逻辑层面
- [ ] 领域事件正确发布
- [ ] 业务规则正确执行
- [ ] 数据一致性保证
- [ ] 事务边界正确

### 性能层面
- [ ] 查询性能满足要求
- [ ] 批量操作性能良好
- [ ] 并发处理正常
- [ ] 内存使用合理

## 🚨 回滚方案

如果验证过程中发现问题，可以按以下步骤回滚：

### 1. 应用程序回滚
```bash
# 恢复旧的代码版本
git checkout previous-version

# 重新部署
mvn clean package
java -jar target/relics-preservation.jar
```

### 2. 数据库回滚
```sql
-- 如果保留了旧表，可以直接切换回去
-- 如果没有保留，需要从备份恢复

-- 禁用新表
RENAME TABLE user_favorites TO user_favorites_backup;
RENAME TABLE user_comments TO user_comments_backup;

-- 恢复旧表（如果存在备份）
-- RENAME TABLE relics_favorite_backup TO relics_favorite;
-- RENAME TABLE relics_comment_backup TO relics_comment;
```

## 📊 监控指标

迁移完成后，需要持续监控以下指标：

### 业务指标
- 每日收藏数量
- 每日评论数量
- 用户活跃度
- 文物热度排名

### 技术指标
- API响应时间
- 数据库查询性能
- 错误率
- 系统资源使用率

### 数据质量指标
- 数据一致性检查
- 统计数据准确性
- 业务规则执行情况

## 📝 注意事项

1. **数据备份**：在执行迁移前，务必备份所有相关数据
2. **分步验证**：按照清单逐项验证，确保每个环节都正确
3. **性能监控**：密切关注迁移后的系统性能表现
4. **用户反馈**：收集用户使用反馈，及时发现和解决问题
5. **文档更新**：更新相关的API文档和用户手册

## 🎯 成功标准

迁移被认为成功的标准：
- 所有验证项目通过
- 系统性能不低于迁移前
- 用户功能完全正常
- 数据完整性得到保证
- 向后兼容性良好
