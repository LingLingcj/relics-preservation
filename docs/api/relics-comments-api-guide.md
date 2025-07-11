# 文物评论API接口文档

## 🎯 接口概述

本文档描述了根据文物ID获取评论列表的API接口，该接口用于在文物详情页展示该文物的所有已通过审核的评论。

## 📋 接口详情

### 获取文物评论列表

**接口地址：** `GET /api/relics/{id}/comments`

**接口描述：** 获取指定文物的所有已通过审核的评论，支持分页查询

**权限要求：** 公开接口，无需认证

#### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | Long | 是 | - | 文物ID（路径参数） |
| page | Integer | 否 | 1 | 页码，从1开始 |
| size | Integer | 否 | 10 | 每页大小，最大100 |

#### 请求示例

```bash
# 获取文物ID为1的评论列表，第1页，每页10条
GET /api/relics/1/comments?page=1&size=10

# 获取文物ID为1的评论列表，第2页，每页20条
GET /api/relics/1/comments?page=2&size=20
```

#### 响应参数

**成功响应：**

```json
{
  "code": "0000",
  "info": "查询成功，共 25 条评论",
  "data": {
    "comments": [
      {
        "commentId": 1001,
        "username": "文物爱好者",
        "content": "这件文物保存得非常好，工艺精湛，历史价值很高。",
        "createTime": "2025-07-11T10:30:00",
        "likeCount": 5,
        "featured": false,
        "recent": true,
        "contentSummary": "这件文物保存得非常好，工艺精湛，历史价值很高。"
      },
      {
        "commentId": 1002,
        "username": "历史研究者",
        "content": "从这件文物可以看出当时的制作工艺已经相当成熟...",
        "createTime": "2025-07-10T15:20:00",
        "likeCount": 3,
        "featured": false,
        "recent": false,
        "contentSummary": "从这件文物可以看出当时的制作工艺已经相当成熟..."
      }
    ],
    "totalCount": 25,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false,
    "relicsId": 1,
    "paginationSummary": "第 1-10 条，共 25 条评论"
  }
}
```

**空结果响应：**

```json
{
  "code": "0000",
  "info": "暂无评论",
  "data": {
    "comments": [],
    "totalCount": 0,
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false,
    "relicsId": 1,
    "paginationSummary": "暂无评论"
  }
}
```

**错误响应：**

```json
{
  "code": "1001",
  "info": "获取评论列表失败",
  "data": null
}
```

#### 响应字段说明

**RelicsCommentListResponseDTO：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| comments | Array | 评论列表 |
| totalCount | Long | 总评论数 |
| currentPage | Integer | 当前页码 |
| pageSize | Integer | 每页大小 |
| totalPages | Integer | 总页数 |
| hasNext | Boolean | 是否有下一页 |
| hasPrevious | Boolean | 是否有上一页 |
| relicsId | Long | 文物ID |
| paginationSummary | String | 分页信息摘要 |

**RelicsCommentDTO：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| commentId | Long | 评论ID |
| username | String | 用户名 |
| content | String | 评论内容 |
| createTime | LocalDateTime | 发表时间 |
| likeCount | Integer | 点赞数（预留字段） |
| featured | Boolean | 是否为精选评论（预留字段） |
| recent | Boolean | 是否为最近评论（24小时内） |
| contentSummary | String | 评论摘要（最多100字符） |

## 🧪 测试用例

### 测试用例 1：正常查询

**请求：**
```bash
curl -X GET "http://localhost:8080/api/relics/1/comments?page=1&size=5" \
  -H "Content-Type: application/json"
```

**预期响应：**
- 状态码：200
- 返回指定文物的评论列表
- 只包含已通过审核的评论
- 按时间倒序排列

### 测试用例 2：无评论的文物

**请求：**
```bash
curl -X GET "http://localhost:8080/api/relics/999/comments" \
  -H "Content-Type: application/json"
```

**预期响应：**
- 状态码：200
- 返回空的评论列表
- totalCount 为 0

### 测试用例 3：分页测试

**请求：**
```bash
# 第一页
curl -X GET "http://localhost:8080/api/relics/1/comments?page=1&size=2"

# 第二页
curl -X GET "http://localhost:8080/api/relics/1/comments?page=2&size=2"
```

**验证点：**
- 第一页：hasPrevious=false, hasNext=true
- 第二页：hasPrevious=true, hasNext根据实际数据确定
- 评论不重复

### 测试用例 4：参数边界测试

**请求：**
```bash
# 页码为0（应自动调整为1）
curl -X GET "http://localhost:8080/api/relics/1/comments?page=0&size=10"

# 页面大小超过限制（应自动调整为100）
curl -X GET "http://localhost:8080/api/relics/1/comments?page=1&size=200"

# 负数页面大小（应自动调整为10）
curl -X GET "http://localhost:8080/api/relics/1/comments?page=1&size=-5"
```

### 测试用例 5：无效文物ID

**请求：**
```bash
curl -X GET "http://localhost:8080/api/relics/0/comments" \
  -H "Content-Type: application/json"
```

**预期响应：**
- 状态码：200
- 返回空的评论列表

## 🔍 业务规则

### 1. 评论过滤规则
- 只显示已通过审核的评论（comment_status = 1）
- 不显示被删除的评论（status = 1）
- 不显示被拒绝的评论

### 2. 排序规则
- 按评论发表时间倒序排列（最新的在前）
- 相同时间的评论按评论ID倒序排列

### 3. 分页规则
- 页码从1开始，小于1时自动调整为1
- 每页大小默认为10，最小为1，最大为100
- 超出范围时自动调整到合理值

### 4. 内容展示规则
- 评论内容完整显示，不截断
- contentSummary 字段提供100字符的摘要
- recent 字段标识24小时内的评论

## 🔗 相关接口

### 1. 发表评论
- **接口：** `POST /api/interaction/comment`
- **说明：** 用户发表评论（需要认证）

### 2. 删除评论
- **接口：** `DELETE /api/interaction/comment/{id}`
- **说明：** 用户删除自己的评论（需要认证）

### 3. 评论审核
- **接口：** `POST /api/v1/admin/comments/{id}/review`
- **说明：** 专家/管理员审核评论（需要特殊权限）

### 4. 获取用户评论
- **接口：** `GET /api/interaction/comments`
- **说明：** 获取当前用户的评论列表（需要认证）

## 📊 性能考虑

### 1. 查询优化
- 在 relics_id 和 comment_status 字段上建立复合索引
- 在 create_time 字段上建立索引用于排序
- 使用 LIMIT 和 OFFSET 进行分页

### 2. 缓存策略
- 可以考虑对热门文物的评论进行缓存
- 缓存时间建议设置为5-10分钟
- 新评论通过审核后清除相关缓存

### 3. 数据量控制
- 限制每页最大100条记录
- 对于评论数量特别多的文物，可以考虑只显示最近的评论

## 🚨 注意事项

### 1. 安全考虑
- 该接口为公开接口，不需要认证
- 不暴露用户的敏感信息
- 只显示已通过审核的评论内容

### 2. 数据一致性
- 评论状态的变更可能有延迟
- 新发表的评论需要审核后才会显示
- 删除的评论会立即从列表中消失

### 3. 错误处理
- 无效的文物ID返回空列表而不是错误
- 数据库异常时返回空列表
- 记录详细的错误日志用于排查问题

通过这个接口，用户可以方便地查看文物的所有评论，提升了用户体验和系统的完整性。
