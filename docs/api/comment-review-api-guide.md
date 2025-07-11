# 评论审核 API 使用指南

## 📋 概述

评论审核系统为专家（EXPERT）和管理员（ADMIN）提供了完整的评论审核功能，包括单个审核、批量审核、查询待审核评论和统计功能。

## 🔐 权限要求

所有审核 API 都需要以下权限之一：
- `ROLE_EXPERT` - 专家角色
- `ROLE_ADMIN` - 管理员角色

## 📚 API 接口

### 1. 审核单个评论

**接口地址：** `POST /api/v1/admin/comments/{commentId}/review`

**请求示例：**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/comments/123/review" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "action": "通过",
    "reason": "内容符合社区规范"
  }'
```

**请求参数：**
```json
{
  "action": "通过",  // 必填：审核操作，"通过" 或 "拒绝"
  "reason": "内容符合社区规范"  // 可选：审核理由
}
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "审核成功",
  "data": {
    "commentId": 123,
    "action": "通过",
    "reviewer": "expert_user",
    "reason": "内容符合社区规范",
    "reviewTime": "2025-07-11T10:30:00",
    "beforeStatus": "待审核",
    "afterStatus": "已通过",
    "success": true,
    "message": "评论 123 已被 expert_user 通过，理由：内容符合社区规范"
  }
}
```

### 2. 快速通过评论

**接口地址：** `POST /api/v1/admin/comments/{commentId}/approve`

**请求示例：**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/comments/123/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "reason": "内容优质"
  }'
```

### 3. 拒绝评论

**接口地址：** `POST /api/v1/admin/comments/{commentId}/reject`

**请求示例：**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/comments/123/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "reason": "包含不当内容"
  }'
```

### 4. 批量审核评论

**接口地址：** `POST /api/v1/admin/comments/batch-review`

**请求示例：**
```bash
curl -X POST "http://localhost:8080/api/v1/admin/comments/batch-review" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "commentIds": [123, 124, 125],
    "action": "通过",
    "reason": "批量通过符合规范的评论"
  }'
```

**响应示例：**
```json
{
  "code": "0000",
  "info": "批量审核完成: 成功 3/3",
  "data": {
    "totalCount": 3,
    "successCount": 3,
    "failureCount": 0,
    "successRate": 1.0,
    "allSuccess": true,
    "errors": []
  }
}
```

### 5. 获取待审核评论列表

**接口地址：** `GET /api/v1/admin/comments/pending`

**请求示例：**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/comments/pending?page=1&size=20&relicsId=456" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**查询参数：**
- `page` - 页码（默认：1）
- `size` - 每页大小（默认：20）
- `relicsId` - 文物ID（可选，筛选特定文物的评论）

**响应示例：**
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "comments": [
      {
        "id": 123,
        "relicsId": 456,
        "relicsName": "青铜鼎",
        "username": "user123",
        "content": "这件文物保存得很好",
        "status": "待审核",
        "createTime": "2025-07-11T09:00:00",
        "waitingDays": 1,
        "urgent": false
      }
    ],
    "total": 15,
    "page": 1,
    "size": 20,
    "hasNext": false
  }
}
```

### 6. 获取审核统计

**接口地址：** `GET /api/v1/admin/comments/statistics`

**请求示例：**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/comments/statistics?startTime=2025-07-01T00:00:00&endTime=2025-07-11T23:59:59" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**查询参数：**
- `startTime` - 开始时间（可选）
- `endTime` - 结束时间（可选）

**响应示例：**
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "reviewer": "expert_user",
    "startTime": "2025-07-01T00:00:00",
    "endTime": "2025-07-11T23:59:59",
    "totalReviewed": 150,
    "approvedCount": 120,
    "rejectedCount": 30,
    "approvalRate": 0.8,
    "rejectionRate": 0.2,
    "dailyStats": {
      "2025-07-11": 15,
      "2025-07-10": 12,
      "2025-07-09": 18
    },
    "avgReviewTime": 5.2,
    "efficiencyLevel": "高效"
  }
}
```

## 🔄 审核流程

### 评论状态流转

```
用户提交评论 → 待审核 (PENDING_REVIEW)
                ↓
            专家/管理员审核
                ↓
        通过 (APPROVED) / 拒绝 (REJECTED)
```

### 审核权限检查

1. **角色检查**：用户必须具有 `EXPERT` 或 `ADMIN` 角色
2. **状态检查**：只能审核状态为"待审核"的评论
3. **业务检查**：确保评论存在且有效

## 📊 审核统计说明

### 统计指标

- **总审核数量**：审核人员处理的评论总数
- **通过数量**：审核通过的评论数量
- **拒绝数量**：审核拒绝的评论数量
- **通过率**：通过数量 / 总审核数量
- **拒绝率**：拒绝数量 / 总审核数量
- **平均审核时间**：从评论提交到审核完成的平均时间
- **效率等级**：基于审核速度和质量的综合评级

### 每日统计

提供按日期分组的审核数量统计，便于分析审核工作量分布。

## ⚠️ 注意事项

### 1. 权限控制
- 所有审核接口都有严格的权限控制
- 普通用户无法访问审核功能
- 审核操作会记录审核人信息

### 2. 审核日志
- 所有审核操作都会记录详细日志
- 包括审核人、审核时间、审核理由等
- 支持审核历史查询和追溯

### 3. 批量操作
- 批量审核支持部分成功
- 会返回详细的成功/失败统计
- 失败的操作会提供具体错误信息

### 4. 业务规则
- 已审核的评论不能重复审核
- 审核理由建议填写，便于后续追溯
- 拒绝评论时必须提供拒绝理由

## 🚀 最佳实践

### 1. 审核效率
- 使用批量审核功能处理大量评论
- 定期查看待审核列表，及时处理
- 利用统计功能监控审核工作量

### 2. 审核质量
- 仔细阅读评论内容，确保审核准确性
- 提供清晰的审核理由，特别是拒绝时
- 遵循社区规范和审核标准

### 3. 系统使用
- 定期查看审核统计，了解工作情况
- 关注待审核评论的数量和时效性
- 及时处理紧急或重要的评论审核

## 🔧 错误处理

### 常见错误码

- `2101` - 没有权限：用户角色不足或无审核权限
- `2001` - 评论不存在：指定的评论ID无效
- `3001` - 参数错误：请求参数格式或内容错误
- `9998` - 未知错误：系统内部错误

### 错误处理建议

1. **权限错误**：检查用户角色和登录状态
2. **参数错误**：验证请求参数的格式和内容
3. **业务错误**：检查评论状态和业务规则
4. **系统错误**：联系技术支持或查看系统日志
