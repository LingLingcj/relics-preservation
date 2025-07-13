# 文物保护系统 API 文档

## 目录

- [API 概览](#api-概览)
- [认证方式](#认证方式)
- [通用响应格式](#通用响应格式)
- [接口分类](#接口分类)
  - [用户认证模块](#用户认证模块)
  - [用户管理模块](#用户管理模块)
  - [文物管理模块](#文物管理模块)
  - [文物搜索模块](#文物搜索模块)
  - [用户交互模块](#用户交互模块)
  - [评论审核模块](#评论审核模块)
  - [知识问答模块](#知识问答模块)
  - [传感器监控模块](#传感器监控模块)
- [数据模型](#数据模型)
- [错误码说明](#错误码说明)
- [使用示例](#使用示例)

## API 概览

### 系统架构说明

文物保护系统采用领域驱动设计（DDD）架构，具有以下特点：

- **分层架构**：Trigger（接口层）、Domain（领域层）、Infrastructure（基础设施层）
- **模块化设计**：按业务领域划分模块，职责清晰
- **微服务友好**：支持水平扩展和服务拆分

### 技术栈

- **后端框架**：Spring Boot 3.5.3
- **安全认证**：Spring Security + JWT
- **数据存储**：MySQL + PostgreSQL + Redis + Elasticsearch
- **AI 集成**：Spring AI + OpenAI + RAG 知识库
- **物联网**：MQTT 协议 + 实时传感器数据
- **文档工具**：Swagger/OpenAPI 3

### 基础信息

- **Base URL**: `http://localhost:8080` (开发环境)
- **API 版本**: v1.0.0
- **数据格式**: JSON
- **字符编码**: UTF-8

## 认证方式

### JWT Token 认证

系统使用 JWT (JSON Web Token) 进行用户认证：

1. **获取 Token**：通过登录接口获取 JWT Token
2. **使用 Token**：在请求头中添加 `Authorization: Bearer {token}`
3. **Token 有效期**：24小时（86400秒）
4. **Token 刷新**：Token 过期后需要重新登录

### 权限级别

系统采用基于角色的访问控制（RBAC），定义了以下权限级别：

- **PUBLIC**：无需认证的公开接口
- **AUTHENTICATED**：需要用户登录认证（任何已登录用户）
- **USER**：普通用户权限（具有基础功能权限）
- **EXPERT**：专家权限（具有文物管理和评论审核权限）
- **ADMIN**：管理员权限（具有系统管理权限）

### 角色权限详情

| 角色 | 权限列表 | 说明 |
|------|----------|------|
| USER | READ_RELICS, COMMENT, FAVORITE | 查看文物、评论、收藏 |
| EXPERT | USER权限 + UPLOAD_RELICS, REVIEW_COMMENTS | 专家权限 + 上传文物、审核评论 |
| ADMIN | EXPERT权限 + MANAGE_USERS, SYSTEM_CONFIG | 管理员权限 + 用户管理、系统配置 |

### Spring Security 权限配置

根据 Spring Security 配置，各路径的权限要求如下：

#### 公开接口（无需认证）
- `/api/auth/**` - 用户认证相关
- `/api/relics/**` - 文物查询相关（除上传外）
- `/api/relics/search/**` - 文物搜索
- `/api/knowledge/rag/**` - AI 知识问答
- `/api/sensor/**` - 传感器数据
- `/api/alert/**` - 告警信息
- Swagger 文档相关路径

#### 需要认证的接口
- `/api/v1/interactions/**` - 用户交互功能
- `/api/favorite/**` - 收藏功能（兼容接口）
- `/api/comment/**` - 评论功能（兼容接口）
- `/api/profile/**` - 用户资料管理

#### 专家权限接口
- `/api/relics/upload` - 文物上传（仅专家可访问）

#### 专家或管理员权限接口
- `/api/v1/admin/comments/**` - 评论审核管理
- `/api/admin/comments/**` - 评论审核管理（兼容路径）

#### 管理员权限接口
- 系统管理相关接口（待扩展）

### 认证示例

```http
GET /api/v1/interactions/favorites
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

## 通用响应格式

所有 API 接口都遵循统一的响应格式：

```json
{
  "code": "0000",
  "info": "成功",
  "data": {}
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | String | 响应状态码，"0000" 表示成功 |
| info | String | 响应信息描述 |
| data | Object | 响应数据内容，可为 null |

### 成功响应示例

```json
{
  "code": "0000",
  "info": "成功",
  "data": {
    "id": 1,
    "name": "青铜鼎",
    "era": "商朝"
  }
}
```

### 错误响应示例

```json
{
  "code": "1007",
  "info": "用户名或密码错误",
  "data": null
}
```

## 接口分类

### 用户认证模块

基础路径：`/api/auth`

#### 用户注册

- **接口**：`POST /api/auth/register`
- **描述**：创建新用户账户
- **权限**：PUBLIC
- **请求体**：

```json
{
  "username": "testuser",
  "password": "password123",
  "confirmPassword": "password123",
  "role": "USER"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "注册成功"
  }
}
```

#### 用户登录

- **接口**：`POST /api/auth/login`
- **描述**：用户登录获取 JWT Token
- **权限**：PUBLIC
- **请求体**：

```json
{
  "username": "testuser",
  "password": "password123"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "登录成功"
  }
}
```

#### 修改密码

- **接口**：`POST /api/auth/change-password`
- **描述**：修改当前用户密码
- **权限**：AUTHENTICATED（需要登录认证）
- **请求体**：

```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "密码修改成功",
  "data": {
    "message": "密码修改成功"
  }
}
```

### 用户管理模块

基础路径：`/api/profile`

#### 获取用户信息

- **接口**：`GET /api/profile`
- **描述**：获取当前登录用户的详细信息
- **权限**：AUTHENTICATED（需要登录认证）
- **请求头**：

```http
Authorization: Bearer {token}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "username": "testuser",
    "nickname": "测试用户",
    "fullName": "张三",
    "email": "test@example.com",
    "phoneNumber": "13800138000",
    "avatarUrl": "https://example.com/avatar.jpg",
    "title": "文物研究员",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2025-01-01T10:00:00",
    "lastLoginTime": "2025-01-15T14:30:00"
  }
}
```

#### 更新用户信息

- **接口**：`PUT /api/profile`
- **描述**：更新当前用户的个人信息
- **权限**：AUTHENTICATED（需要登录认证）
- **请求体**：

```json
{
  "nickname": "新昵称",
  "fullName": "李四",
  "email": "newemail@example.com",
  "phoneNumber": "13900139000",
  "avatarUrl": "https://example.com/new-avatar.jpg",
  "title": "高级研究员"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "更新成功",
  "data": {
    "username": "testuser",
    "nickname": "新昵称",
    "fullName": "李四",
    "email": "newemail@example.com",
    "phoneNumber": "13900139000",
    "avatarUrl": "https://example.com/new-avatar.jpg",
    "title": "高级研究员",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2025-01-01T10:00:00",
    "lastLoginTime": "2025-01-15T14:30:00"
  }
}
```

### 文物管理模块

基础路径：`/api/relics`

#### 添加文物

- **接口**：`POST /api/relics`
- **描述**：添加新的文物信息
- **权限**：PUBLIC（注意：根据 Spring Security 配置，此接口当前为公开接口，建议改为 EXPERT 权限）
- **建议权限**：EXPERT（应该限制为专家权限）
- **请求体**：

```json
{
  "name": "青铜鼎",
  "description": "商朝青铜器，用于祭祀",
  "preservation": 1,
  "category": "青铜器",
  "era": "商朝",
  "material": "青铜",
  "imageUrl": "https://example.com/bronze-ding.jpg",
  "status": 1,
  "locationId": 1
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "文物添加成功",
  "data": {
    "relicsId": 123,
    "message": "文物添加成功"
  }
}
```

#### 按朝代搜索文物

- **接口**：`GET /api/relics/era`
- **描述**：根据朝代名称搜索文物
- **权限**：PUBLIC
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| era | String | 是 | 朝代名称，如"商朝"、"唐朝" |

- **请求示例**：

```http
GET /api/relics/era?era=商朝
```

- **响应**：

```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "total": 2,
    "list": [
      {
        "relicsId": 1,
        "name": "青铜鼎",
        "description": "商朝青铜器，用于祭祀",
        "preservation": 1,
        "category": "青铜器",
        "era": "商朝",
        "material": "青铜",
        "imageUrl": "https://example.com/bronze-ding.jpg",
        "status": 1,
        "locationId": 1
      }
    ]
  }
}
```

#### 按名称搜索文物

- **接口**：`GET /api/relics/name`
- **描述**：根据文物名称关键词搜索
- **权限**：PUBLIC
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 文物名称关键词 |

- **请求示例**：

```http
GET /api/relics/name?name=青铜
```

- **响应**：同按朝代搜索的响应格式

### 文物搜索模块

基础路径：`/api/relics/search`

#### 按名称搜索文物

- **接口**：`GET /api/relics/search/name`
- **描述**：使用 Elasticsearch 进行名称搜索
- **权限**：PUBLIC
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

#### 多字段搜索文物

- **接口**：`GET /api/relics/search/keyword`
- **描述**：在文物的名称、朝代、类别、描述、材质等字段中搜索
- **权限**：PUBLIC
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

#### 同步文物到 Elasticsearch

- **接口**：`POST /api/relics/search/sync`
- **描述**：将所有文物数据同步到 Elasticsearch
- **权限**：ADMIN
- **响应**：

```json
{
  "code": "0000",
  "info": "同步成功",
  "data": true
}
```

### 用户交互模块

基础路径：`/api/v1/interactions`

> **注意**：系统还提供向后兼容的收藏接口 `/api/v1/favorites`，功能相同但接口设计略有不同。

#### 收藏/取消收藏文物

- **接口**：`POST /api/v1/interactions/favorites`
- **描述**：用户收藏或取消收藏指定文物
- **权限**：AUTHENTICATED（需要登录认证）
- **请求体**：

```json
{
  "relicsId": 123,
  "favorite": true
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "操作成功",
  "data": {
    "success": true,
    "message": "收藏成功",
    "relicsId": 123,
    "username": "testuser",
    "operationTime": "2025-01-15T14:30:00",
    "data": null
  }
}
```

#### 检查收藏状态

- **接口**：`GET /api/v1/interactions/favorites/status`
- **描述**：检查用户是否已收藏指定文物
- **权限**：AUTHENTICATED（需要登录认证）
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| relicsId | Long | 是 | 文物ID |

- **响应**：

```json
{
  "code": "0000",
  "info": "查询成功",
  "data": true
}
```

#### 获取用户收藏列表

- **接口**：`GET /api/v1/interactions/favorites`
- **描述**：分页获取用户的收藏文物列表
- **权限**：AUTHENTICATED（需要登录认证）
- **查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 20 | 每页大小 |

- **响应**：

```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "favorites": [
      {
        "relicsId": 123,
        "name": "青铜鼎",
        "era": "商朝",
        "favoriteTime": "2025-01-15T14:30:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 20,
    "hasNext": false
  }
}
```

#### 添加评论

- **接口**：`POST /api/v1/interactions/comments`
- **描述**：用户对指定文物添加评论
- **权限**：AUTHENTICATED（需要登录认证）
- **请求体**：

```json
{
  "relicsId": 123,
  "content": "这件青铜鼎制作工艺精湛，具有很高的历史价值。"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "评论成功",
  "data": {
    "commentId": 456,
    "relicsId": 123,
    "username": "testuser",
    "content": "这件青铜鼎制作工艺精湛，具有很高的历史价值。",
    "status": "PENDING_REVIEW",
    "statusDescription": "待审核",
    "createTime": "2025-01-15T14:30:00",
    "needsReview": true,
    "isOwner": true
  }
}
```

#### 删除评论

- **接口**：`DELETE /api/v1/interactions/comments/{commentId}`
- **描述**：用户删除自己的评论
- **权限**：AUTHENTICATED（需要登录认证）
- **路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 评论ID |

#### 获取用户评论列表

- **接口**：`GET /api/v1/interactions/comments`
- **描述**：分页获取用户的评论列表
- **权限**：AUTHENTICATED（需要登录认证）
- **查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| relicsId | Long | 否 | - | 文物ID（可选过滤） |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 20 | 每页大小 |

### 向后兼容收藏模块

基础路径：`/api/v1/favorites`

> **说明**：此模块为向后兼容版本，推荐使用新的交互模块接口。

#### 添加收藏（兼容版本）

- **接口**：`POST /api/v1/favorites`
- **描述**：用户收藏指定文物（向后兼容版本）
- **权限**：AUTHENTICATED（需要登录认证）
- **请求体**：

```json
{
  "relicsId": 123
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "收藏成功",
  "data": {
    "favoriteId": 789,
    "relicsId": 123,
    "username": "testuser",
    "favoriteTime": "2025-01-15T14:30:00",
    "message": "收藏成功"
  }
}
```

#### 取消收藏（兼容版本）

- **接口**：`DELETE /api/v1/favorites`
- **描述**：用户取消收藏指定文物（向后兼容版本）
- **权限**：AUTHENTICATED（需要登录认证）
- **查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| relicsId | Long | 是 | 文物ID |

- **响应**：

```json
{
  "code": "0000",
  "info": "取消收藏成功",
  "data": "success"
}
```

### 评论审核模块

基础路径：`/api/v1/admin/comments`

#### 审核单个评论

- **接口**：`POST /api/v1/admin/comments/{commentId}/review`
- **描述**：专家或管理员审核指定评论
- **权限**：EXPERT 或 ADMIN
- **路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Long | 是 | 评论ID |

- **请求体**：

```json
{
  "action": "APPROVE",
  "reason": "内容符合规范"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "审核成功",
  "data": {
    "commentId": 456,
    "action": "APPROVE",
    "reviewTime": "2025-01-15T14:30:00",
    "reviewer": "expert_user",
    "reason": "内容符合规范"
  }
}
```

#### 批量审核评论

- **接口**：`POST /api/v1/admin/comments/batch-review`
- **描述**：批量审核多个评论
- **权限**：EXPERT 或 ADMIN
- **请求体**：

```json
{
  "commentIds": [456, 457, 458],
  "action": "APPROVE",
  "reason": "批量审核通过"
}
```

#### 获取待审核评论列表

- **接口**：`GET /api/v1/admin/comments/pending`
- **描述**：获取待审核的评论列表
- **权限**：EXPERT 或 ADMIN
- **查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 20 | 每页大小 |
| relicsId | Long | 否 | - | 文物ID（可选过滤） |

### 知识问答模块

基础路径：`/api/knowledge/rag`

#### 上传知识库文件

- **接口**：`POST /api/knowledge/rag`
- **描述**：上传文档到 AI 知识库
- **权限**：PUBLIC（注意：根据 Spring Security 配置，此接口当前为公开接口，建议改为 EXPERT 权限）
- **建议权限**：EXPERT（应该限制为专家权限）
- **请求类型**：multipart/form-data
- **参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ragTag | String | 是 | 知识库标签 |
| files | File[] | 是 | 上传的文件列表 |

#### AI 问答

- **接口**：`POST /api/knowledge/rag/ai`
- **描述**：基于知识库的 AI 问答
- **权限**：PUBLIC
- **请求体**：

```json
{
  "message": "请介绍一下青铜器的历史",
  "ragTag": "文物知识"
}
```

- **响应**：

```json
{
  "code": "0000",
  "info": "成功",
  "data": "青铜器是中国古代重要的文物类型，主要出现在商周时期..."
}
```

### 传感器监控模块

#### 传感器控制

- **接口**：`POST /api/sensor/value`
- **描述**：控制传感器设备（风扇、LED等）
- **权限**：PUBLIC（注意：根据 Spring Security 配置，此接口当前为公开接口，建议改为 EXPERT 权限）
- **建议权限**：EXPERT（应该限制为专家权限）
- **请求体**：

```json
{
  "sensorType": "fan",
  "value": 1
}
```

#### 获取传感器数据

- **接口**：`GET /api/sensor/data/recent`
- **描述**：获取各种传感器类型的最新数据
- **权限**：PUBLIC
- **响应**：

```json
{
  "code": "0000",
  "info": "成功",
  "data": {
    "gas": [
      {
        "sensorId": "gas_001",
        "value": 0.02,
        "timestamp": "2025-01-15T14:30:00",
        "unit": "ppm"
      }
    ],
    "temp": [
      {
        "sensorId": "temp_001",
        "value": 22.5,
        "timestamp": "2025-01-15T14:30:00",
        "unit": "°C"
      }
    ],
    "hum": [
      {
        "sensorId": "hum_001",
        "value": 45.0,
        "timestamp": "2025-01-15T14:30:00",
        "unit": "%"
      }
    ],
    "intensity": [
      {
        "sensorId": "light_001",
        "value": 800,
        "timestamp": "2025-01-15T14:30:00",
        "unit": "lux"
      }
    ]
  }
}
```

## 数据模型

### 用户信息模型

```json
{
  "username": "String - 用户名",
  "nickname": "String - 昵称",
  "fullName": "String - 真实姓名",
  "email": "String - 邮箱",
  "phoneNumber": "String - 手机号",
  "avatarUrl": "String - 头像URL",
  "title": "String - 职位/头衔",
  "role": "String - 角色(USER/EXPERT/ADMIN)",
  "status": "String - 状态(ACTIVE/INACTIVE)",
  "createTime": "LocalDateTime - 创建时间",
  "lastLoginTime": "LocalDateTime - 最后登录时间"
}
```

### 文物信息模型

```json
{
  "relicsId": "Long - 文物ID",
  "name": "String - 文物名称",
  "description": "String - 文物描述",
  "preservation": "Integer - 保护等级",
  "category": "String - 类别",
  "era": "String - 所属年代",
  "material": "String - 主要材质",
  "imageUrl": "String - 文物图片URL",
  "status": "Integer - 状态",
  "locationId": "Integer - 所在位置ID"
}
```

### 评论信息模型

```json
{
  "id": "Long - 评论ID",
  "relicsId": "Long - 文物ID",
  "username": "String - 用户名",
  "content": "String - 评论内容",
  "createTime": "LocalDateTime - 创建时间",
  "isOwner": "Boolean - 是否为当前用户的评论"
}
```

### 传感器数据模型

```json
{
  "sensorId": "String - 传感器ID",
  "value": "Double - 传感器数值",
  "timestamp": "LocalDateTime - 时间戳",
  "unit": "String - 单位"
}
```

## 错误码说明

### 成功状态码

| 状态码 | 说明 |
|--------|------|
| 0000 | 成功 |

### 用户相关错误码

| 状态码 | 说明 |
|--------|------|
| 1001 | 用户名长度应为3-50个字符 |
| 1002 | 用户名只能包含小写字母，数字，下划线和连字符 |
| 1003 | 用户名已存在 |
| 1004 | 密码长度应为8-128个字符 |
| 1005 | 密码只能包含字母、数字和特殊字符 |
| 1006 | 两次输入的密码不一致 |
| 1007 | 用户名或密码错误 |
| 1008 | 原密码错误 |
| 1009 | 用户未登录 |
| 1010 | 无效的用户角色 |
| 1011 | 用户不存在 |
| 1012 | 更新用户信息失败 |
| 1013 | 邮箱已存在 |
| 1014 | 手机号已存在 |
| 1015 | 身份错误 |

### 文物相关错误码

| 状态码 | 说明 |
|--------|------|
| 2001 | 未找到指定文物 |

### 权限相关错误码

| 状态码 | 说明 |
|--------|------|
| 2101 | 没有权限 |

### 通用错误码

| 状态码 | 说明 |
|--------|------|
| 3001 | 参数错误 |
| 9998 | 未知错误 |
| 9999 | 系统异常 |

## 使用示例

### 完整的用户注册和登录流程

```bash
# 1. 用户注册
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "confirmPassword": "password123",
    "role": "USER"
  }'

# 2. 用户登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# 3. 使用 Token 访问受保护的接口
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 文物搜索和收藏流程

```bash
# 1. 搜索文物
curl -X GET "http://localhost:8080/api/relics/search/keyword?keyword=青铜"

# 2. 收藏文物
curl -X POST http://localhost:8080/api/v1/interactions/favorites \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "relicsId": 123,
    "favorite": true
  }'

# 3. 查看收藏列表
curl -X GET "http://localhost:8080/api/v1/interactions/favorites?page=1&size=10" \
  -H "Authorization: Bearer {token}"
```

### AI 知识问答流程

```bash
# 1. 上传知识库文件（需要专家权限）
curl -X POST http://localhost:8080/api/knowledge/rag \
  -H "Authorization: Bearer {expert_token}" \
  -F "ragTag=文物知识" \
  -F "files=@knowledge.pdf"

# 2. AI 问答
curl -X POST http://localhost:8080/api/knowledge/rag/ai \
  -H "Content-Type: application/json" \
  -d '{
    "message": "请介绍一下青铜器的历史",
    "ragTag": "文物知识"
  }'
```

## 注意事项

### RESTful 设计建议

1. **URL 设计**：部分接口路径不够 RESTful，建议改进：
   - `GET /api/relics/era?era=商朝` → `GET /api/relics?era=商朝`
   - `GET /api/relics/name?name=青铜` → `GET /api/relics?name=青铜`

2. **HTTP 方法使用**：
   - 查询操作使用 GET
   - 创建操作使用 POST
   - 更新操作使用 PUT/PATCH
   - 删除操作使用 DELETE

3. **状态码使用**：建议使用标准 HTTP 状态码配合业务状态码

### 权限配置建议

根据 Spring Security 配置分析，发现以下权限配置问题：

1. **文物上传接口**：`POST /api/relics` 当前为公开接口，建议改为专家权限
2. **知识库上传接口**：`POST /api/knowledge/rag` 当前为公开接口，建议改为专家权限
3. **传感器控制接口**：`POST /api/sensor/value` 当前为公开接口，建议改为专家权限
4. **路径匹配问题**：
   - `/api/v1/interactions/**` 需要认证，但实际路径可能不匹配
   - 建议统一接口路径规范

### 建议的权限配置修改

```java
// 建议在 Spring Security 配置中添加：
authorize.requestMatchers("/api/relics").hasRole("EXPERT"); // 文物上传
authorize.requestMatchers("/api/knowledge/rag").hasRole("EXPERT"); // 知识库上传
authorize.requestMatchers("/api/sensor/value").hasRole("EXPERT"); // 传感器控制
authorize.requestMatchers("/api/v1/interactions/**").authenticated(); // 用户交互
```

### DDD 原则建议

1. **领域边界**：建议将收藏和评论功能整合到用户交互聚合根中
2. **业务逻辑**：Controller 层应该只处理 HTTP 相关逻辑，业务逻辑应在 Domain 层
3. **数据转换**：建议使用专门的转换器处理 DTO 和领域对象之间的转换

### 安全建议

1. **输入验证**：所有用户输入都应进行严格验证
2. **权限控制**：敏感操作应进行细粒度的权限控制
3. **日志记录**：重要操作应记录详细的审计日志

---

*本文档基于当前代码库生成，如有接口变更请及时更新文档。*
