# 评论审核功能测试指南

## 🎯 测试目标

验证评论审核系统的核心功能是否正常工作，特别是修复后的用户名获取问题。

## 🔧 测试环境准备

### 1. 数据库准备
```sql
-- 执行数据库迁移脚本
source docs/mysql/sql/2025-07-11-interaction-tables.sql;

-- 插入测试数据
INSERT INTO user_comments (comment_id, username, relics_id, content, comment_status, create_time, update_time, status) VALUES
(1001, 'testuser1', 1, '这件文物保存得很好', 0, NOW(), NOW(), 0),
(1002, 'testuser2', 1, '工艺精湛，值得收藏', 0, NOW(), NOW(), 0),
(1003, 'testuser3', 2, '历史价值很高', 0, NOW(), NOW(), 0);

-- 插入测试用户（如果不存在）
INSERT INTO user (username, nickname, password, role, status, create_time, update_time) VALUES
('expert1', '专家一号', '$2a$10$encrypted_password', 'EXPERT', 1, NOW(), NOW()),
('admin1', '管理员一号', '$2a$10$encrypted_password', 'ADMIN', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;
```

### 2. 应用启动
```bash
# 启动应用
mvn spring-boot:run

# 或者使用IDE启动主类
# RelicsPreservationApplication.java
```

## 📋 测试用例

### 测试用例 1：获取待审核评论列表

**目标：** 验证 `CommentWithUser` 修复后能正确获取用户名

**请求：**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/comments/pending?page=1&size=10" \
  -H "Authorization: Bearer EXPERT_OR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

**预期响应：**
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "comments": [
      {
        "id": 1001,
        "relicsId": 1,
        "username": "testuser1",  // ✅ 应该正确显示用户名
        "content": "这件文物保存得很好",
        "status": "待审核",
        "createTime": "2025-07-11T10:00:00",
        "waitingDays": 0,
        "urgent": false
      }
    ],
    "total": 3,
    "page": 1,
    "size": 10,
    "hasNext": false
  }
}
```

### 测试用例 2：审核单个评论

**目标：** 验证审核功能正常工作

**请求：**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/comments/1001/review" \
  -H "Authorization: Bearer EXPERT_OR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "通过",
    "reason": "内容符合社区规范"
  }'
```

**预期响应：**
```json
{
  "code": "0000",
  "info": "审核成功",
  "data": {
    "commentId": 1001,
    "action": "通过",
    "reviewer": "expert1",
    "reason": "内容符合社区规范",
    "reviewTime": "2025-07-11T11:00:00",
    "beforeStatus": "待审核",
    "afterStatus": "已通过",
    "success": true,
    "message": "评论 1001 已被 expert1 通过，理由：内容符合社区规范"
  }
}
```

### 测试用例 3：批量审核评论

**请求：**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/comments/batch-review" \
  -H "Authorization: Bearer EXPERT_OR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "commentIds": [1002, 1003],
    "action": "通过",
    "reason": "批量通过符合规范的评论"
  }'
```

**预期响应：**
```json
{
  "code": "0000",
  "info": "批量审核完成: 成功 2/2",
  "data": {
    "totalCount": 2,
    "successCount": 2,
    "failureCount": 0,
    "successRate": 1.0,
    "allSuccess": true,
    "errors": []
  }
}
```

### 测试用例 4：权限验证

**目标：** 验证只有专家和管理员可以访问审核接口

**请求（普通用户）：**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/comments/pending" \
  -H "Authorization: Bearer USER_TOKEN" \
  -H "Content-Type: application/json"
```

**预期响应：**
```json
{
  "code": "2101",
  "info": "没有权限",
  "data": null
}
```

## 🔍 关键验证点

### 1. 用户名显示验证
- ✅ 待审核评论列表中每个评论都应该显示正确的用户名
- ✅ 用户名不应该为 null 或空字符串
- ✅ 用户名应该与数据库中的实际用户名一致

### 2. 审核功能验证
- ✅ 审核操作应该成功执行
- ✅ 评论状态应该正确更新
- ✅ 审核日志应该记录（查看应用日志）
- ✅ 领域事件应该发布（查看应用日志）

### 3. 权限控制验证
- ✅ 专家角色可以访问审核接口
- ✅ 管理员角色可以访问审核接口
- ✅ 普通用户无法访问审核接口

### 4. 数据一致性验证
```sql
-- 验证评论状态更新
SELECT comment_id, username, content, comment_status, update_time 
FROM user_comments 
WHERE comment_id IN (1001, 1002, 1003);

-- 验证审核日志记录（如果实现了）
SELECT * FROM comment_review_logs 
WHERE comment_id IN (1001, 1002, 1003) 
ORDER BY review_time DESC;
```

## 🐛 常见问题排查

### 问题 1：用户名显示为 null
**原因：** `CommentWithUser` 转换逻辑有问题
**解决：** 检查 `convertToCommentWithUser` 方法

### 问题 2：权限验证失败
**原因：** Spring Security 配置或角色设置问题
**解决：** 检查用户角色和 `@PreAuthorize` 注解

### 问题 3：审核操作失败
**原因：** 数据库连接或事务问题
**解决：** 检查数据库连接和事务配置

### 问题 4：找不到评论
**原因：** `findCommentWithUserById` 方法实现有问题
**解决：** 检查查询逻辑和数据库数据

## 📊 性能测试

### 并发审核测试
```bash
# 使用 Apache Bench 进行并发测试
ab -n 100 -c 10 -H "Authorization: Bearer EXPERT_TOKEN" \
   -H "Content-Type: application/json" \
   -p test-data.json \
   http://localhost:8080/api/v1/admin/comments/pending
```

### 大量数据测试
```sql
-- 插入大量测试评论
INSERT INTO user_comments (comment_id, username, relics_id, content, comment_status, create_time, update_time, status)
SELECT 
    2000 + ROW_NUMBER() OVER(),
    CONCAT('testuser', (ROW_NUMBER() OVER() % 100) + 1),
    (ROW_NUMBER() OVER() % 10) + 1,
    CONCAT('测试评论内容 ', ROW_NUMBER() OVER()),
    0,
    NOW(),
    NOW(),
    0
FROM information_schema.columns 
LIMIT 1000;
```

## ✅ 测试通过标准

1. **功能完整性**
   - 所有 API 接口正常响应
   - 用户名正确显示
   - 审核操作成功执行

2. **数据准确性**
   - 评论状态正确更新
   - 审核日志正确记录
   - 数据库数据一致

3. **安全性**
   - 权限控制正确
   - 只有授权用户可以审核
   - 敏感信息不泄露

4. **性能表现**
   - 响应时间在可接受范围内
   - 并发处理正常
   - 大量数据处理稳定

## 📝 测试报告模板

```markdown
# 评论审核功能测试报告

## 测试环境
- 应用版本：v1.0.0
- 数据库：MySQL 8.0
- 测试时间：2025-07-11

## 测试结果
- [ ] 用户名显示正确
- [ ] 审核功能正常
- [ ] 权限控制有效
- [ ] 数据一致性良好
- [ ] 性能表现满足要求

## 发现问题
1. 问题描述
2. 重现步骤
3. 预期结果
4. 实际结果

## 建议改进
1. 改进建议
2. 优化方向
```

通过这个测试指南，可以全面验证评论审核系统的功能是否正常，特别是我们修复的用户名获取问题。
