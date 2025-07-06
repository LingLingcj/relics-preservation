# 文物遗产保护系统 API 接口文档

## 项目概述

文物遗产保护系统是一个基于Spring Boot 3.5.3的现代化后端系统，采用DDD（领域驱动设计）架构，集成了AI智能化、全文搜索、物联网监控等先进技术，为文物数字化保护提供全面的技术支持。

### 技术特性
- **AI智能化**：集成Spring AI + OpenAI + RAG知识库
- **智能搜索**：Elasticsearch + IK中文分词器
- **物联网监控**：MQTT协议 + 实时传感器数据处理
- **多数据源**：MySQL + PostgreSQL + Redis
- **安全认证**：JWT + Spring Security

## 基础信息

- **Base URL**: `http://sj.frp.one:40098`   //内网穿透
- **API版本**: v1.0.0
- **认证方式**: JWT Bearer Token
- **数据格式**: JSON

## 通用响应格式

所有API接口都遵循统一的响应格式：

```json
{
  "code": "0000",
  "info": "成功",
  "data": {}
}
```

### 响应字段说明
- `code`: 响应状态码
- `info`: 响应信息描述
- `data`: 响应数据内容

### 常用状态码
- `0000`: 成功
- `9999`: 系统异常
- `1001`: 用户名长度应为3-50个字符
- `1002`: 用户名只能包含小写字母，数字，下划线和连字符
- `1003`: 用户名已存在
- `1004`: 密码长度应为8-128个字符
- `1005`: 密码只能包含字母、数字和特殊字符
- `1006`: 两次输入的密码不一致
- `1007`: 用户名或密码错误
- `1008`: 原密码错误
- `1009`: 用户未登录
- `1010`: 无效的用户角色
- `1011`: 用户不存在
- `1012`: 更新用户信息失败
- `1013`: 邮箱已存在
- `1014`: 手机号已存在
- `1015`: 身份错误
- `2001`: 未找到指定文物
- `2101`: 没有权限
- `3001`: 参数错误

## 认证说明

### JWT Token使用
1. 登录成功后获取JWT Token
2. 在请求头中添加：`Authorization: Bearer {token}`
3. Token有效期：24小时

### 需要认证的接口
- 用户资料管理
- 文物收藏功能
- 评论功能
- 密码修改

## API接口详细说明

## 1. 用户认证模块 (/api/auth)

### 1.1 用户注册
- **接口**: `POST /api/auth/register`
- **描述**: 创建新用户并返回JWT令牌
- **请求体**:
```json
{
  "username": "string",
  "password": "string",
  "confirmPassword": "string",
  "role": "USER"
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "注册成功",
  "data": {
    "token": "jwt_token_string",
    "message": "注册成功"
  }
}
```

### 1.2 用户登录
- **接口**: `POST /api/auth/login`
- **描述**: 验证用户凭据并返回JWT令牌
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "登录成功",
  "data": {
    "token": "jwt_token_string",
    "message": "登录成功"
  }
}
```

### 1.3 修改密码
- **接口**: `POST /api/auth/change-password`
- **描述**: 修改当前登录用户的密码
- **认证**: 需要JWT Token
- **请求体**:
```json
{
  "oldPassword": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "密码修改成功",
  "data": {
    "message": "密码修改成功"
  }
}
```

## 2. 文物管理模块 (/api/relics)

### 2.1 添加文物
- **接口**: `POST /api/relics`
- **描述**: 添加文物信息到系统
- **请求体**:
```json
{
  "name": "string",
  "description": "string",
  "preservation": 1,
  "category": "string",
  "era": "string",
  "material": "string",
  "imageUrl": "string",
  "locationId": 1
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "上传成功",
  "data": {
    "success": true,
    "message": "上传成功"
  }
}
```

### 2.2 按朝代搜索文物
- **接口**: `GET /api/relics/era?era={era}`
- **描述**: 根据朝代名称搜索文物信息
- **参数**: 
  - `era` (string): 朝代名称
- **响应**:
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "total": 10,
    "list": [
      {
        "relicsId": 1,
        "name": "string",
        "description": "string",
        "preservation": 1,
        "category": "string",
        "era": "string",
        "material": "string",
        "imageUrl": "string",
        "status": 1,
        "locationId": 1
      }
    ]
  }
}
```

### 2.3 按名称搜索文物
- **接口**: `GET /api/relics/name?name={name}`
- **描述**: 根据文物名称关键词搜索文物信息
- **参数**:
  - `name` (string): 文物名称关键词
- **响应**:
```json
{
  "code": "0000",
  "info": "搜索成功",
  "data": {
    "total": 5,
    "list": [
      {
        "relicsId": 1,
        "name": "青铜鼎",
        "description": "商代青铜器",
        "preservation": 1,
        "category": "青铜器",
        "era": "商",
        "material": "青铜",
        "imageUrl": "http://example.com/image.jpg",
        "status": 1,
        "locationId": 1
      }
    ]
  }
}
```

### 2.4 按ID查询文物
- **接口**: `GET /api/relics/id?id={id}`
- **描述**: 根据ID查询文物详细信息
- **参数**:
  - `id` (long): 文物ID
- **响应**:
```json
{
  "code": "0000",
  "info": "获取成功",
  "data": {
    "relicsId": 1,
    "name": "青铜鼎",
    "description": "商代青铜器，用于祭祀",
    "preservation": 1,
    "category": "青铜器",
    "era": "商",
    "material": "青铜",
    "imageUrl": "http://example.com/image.jpg",
    "status": 1,
    "locationId": 1
  }
}
```

### 2.5 获取文物评论
- **接口**: `GET /api/relics/{relicsId}/comments`
- **描述**: 获取指定文物的评论列表
- **路径参数**:
  - `relicsId` (long): 文物ID
- **响应**:
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": [
    {
      "commentId": 1,
      "relicsId": 1,
      "username": "user123",
      "content": "这件文物很有历史价值",
      "createTime": "2025-07-06T10:30:00",
      "isOwner": true
    }
  ]
}
```

### 2.6 获取其他朝代文物
- **接口**: `GET /api/relics/other-eras`
- **描述**: 获取除唐、宋、明之外的其他朝代文物信息
- **响应**:
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "total": 8,
    "list": [
      {
        "relicsId": 1,
        "name": "青铜鼎",
        "description": "商代青铜器",
        "preservation": 1,
        "category": "青铜器",
        "era": "商",
        "material": "青铜",
        "imageUrl": "http://example.com/image.jpg",
        "status": 1,
        "locationId": 1
      }
    ]
  }
}
```

## 3. 文物搜索模块 (/api/relics/search)

### 3.1 按名称搜索文物
- **接口**: `GET /api/relics/search/name?keyword={keyword}`
- **描述**: 根据名称关键词搜索文物信息
- **参数**:
  - `keyword` (string): 搜索关键词
- **响应**:
```json
{
  "code": "0000",
  "info": "搜索成功",
  "data": {
    "total": 3,
    "list": [
      {
        "relicsId": 1,
        "name": "青铜鼎",
        "era": "商",
        "category": "青铜器",
        "description": "商代青铜器",
        "imageUrl": "http://example.com/image.jpg",
        "locationId": 1,
        "preservation": 1,
        "material": "青铜",
        "status": 1
      }
    ]
  }
}
```

### 3.2 多字段搜索文物
- **接口**: `GET /api/relics/search/keyword?keyword={keyword}`
- **描述**: 根据关键词搜索文物名称、朝代、类别、描述、材质等字段
- **参数**:
  - `keyword` (string): 搜索关键词
- **响应**:
```json
{
  "code": "0000",
  "info": "搜索成功",
  "data": {
    "total": 5,
    "list": [
      {
        "relicsId": 1,
        "name": "青铜鼎",
        "era": "商",
        "category": "青铜器",
        "description": "商代青铜器，用于祭祀",
        "imageUrl": "http://example.com/image.jpg",
        "locationId": 1,
        "preservation": 1,
        "material": "青铜",
        "status": 1
      }
    ]
  }
}
```

### 3.3 同步文物到ES
- **接口**: `POST /api/relics/search/sync`
- **描述**: 同步所有文物数据到Elasticsearch
- **响应**:
```json
{
  "code": "0000",
  "info": "同步成功",
  "data": true
}
```

## 4. AI知识库模块 (/api/knowledge/rag)

### 4.1 上传知识库文件
- **接口**: `POST /api/knowledge/rag`
- **描述**: 上传文档到AI知识库
- **请求类型**: multipart/form-data
- **参数**:
  - `ragTag` (string): 知识库标签
  - `files` (file[]): 上传的文件列表
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": null
}
```

### 4.2 AI问答
- **接口**: `POST /api/knowledge/rag/ai`
- **描述**: 基于知识库的AI问答
- **请求体**:
```json
{
  "message": "请介绍一下青铜器的历史",
  "ragTag": "文物知识"
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": "青铜器是中国古代重要的文物类型，主要出现在商周时期..."
}
```

## 5. 传感器监控模块

### 5.1 传感器控制 (/api/sensor/value)
- **接口**: `POST /api/sensor/value`
- **描述**: 控制传感器设备（风扇、LED等）
- **请求体**:
```json
{
  "sensorType": "fan",
  "value": 1
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": null
}
```

### 5.2 传感器数据分析 (/api/sensor/analysis)
- **接口**: `GET /api/sensor/analysis/report`
- **描述**: 获取AI生成的传感器数据分析报告
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": "<html>AI生成的传感器数据分析报告HTML内容</html>"
}
```

## 6. 收藏功能模块 (/api/favorites)

### 6.1 添加收藏
- **接口**: `POST /api/favorites`
- **认证**: 需要JWT Token
- **请求体**:
```json
{
  "relicsId": 1
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "收藏成功",
  "data": null
}
```

### 6.2 取消收藏
- **接口**: `DELETE /api/favorites/{relicsId}`
- **认证**: 需要JWT Token
- **路径参数**:
  - `relicsId` (long): 文物ID
- **响应**:
```json
{
  "code": "0000",
  "info": "取消收藏成功",
  "data": null
}
```

### 6.3 检查收藏状态
- **接口**: `GET /api/favorites/check/{relicsId}`
- **认证**: 需要JWT Token
- **路径参数**:
  - `relicsId` (long): 文物ID
- **响应**:
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": true
}
```

### 6.4 获取收藏列表
- **接口**: `GET /api/favorites`
- **认证**: 需要JWT Token
- **响应**:
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "items": [
      {
        "favoriteId": 1,
        "relicsId": 1,
        "relicsName": "青铜鼎",
        "relicsImageUrl": "http://example.com/image.jpg",
        "relicsDescription": "商代青铜器",
        "createTime": "2025-07-06T10:30:00"
      }
    ],
    "total": 1
  }
}
```

## 7. 评论功能模块 (/api/comments)

### 7.1 添加评论
- **接口**: `POST /api/comments`
- **认证**: 需要JWT Token
- **请求体**:
```json
{
  "relicsId": 1,
  "content": "这件文物很有历史价值"
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "评论添加成功",
  "data": {
    "commentId": 1,
    "relicsId": 1,
    "username": "user123",
    "content": "这件文物很有历史价值",
    "createTime": "2025-07-06T10:30:00",
    "isOwner": true
  }
}
```

### 7.2 查询评论
- **接口**: `GET /api/comments?relicsId={relicsId}`
- **参数**:
  - `relicsId` (long): 文物ID
- **响应**:
```json
{
  "code": "0000",
  "info": "查询成功",
  "data": {
    "list": [
      {
        "commentId": 1,
        "relicsId": 1,
        "username": "user123",
        "content": "这件文物很有历史价值",
        "createTime": "2025-07-06T10:30:00",
        "isOwner": true
      }
    ],
    "total": 1
  }
}
```

### 7.3 删除评论
- **接口**: `DELETE /api/comments/{commentId}`
- **认证**: 需要JWT Token
- **路径参数**:
  - `commentId` (long): 评论ID
- **响应**:
```json
{
  "code": "0000",
  "info": "删除成功",
  "data": true
}
```

## 8. 用户资料管理模块 (/api/user/profile)

### 8.1 获取用户信息
- **接口**: `GET /api/user/profile`
- **认证**: 需要JWT Token
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": {
    "username": "user123",
    "nickname": "文物爱好者",
    "title": "研究员",
    "email": "user@example.com",
    "avatarUrl": "http://example.com/avatar.jpg",
    "role": "USER",
    "fullName": "张三",
    "phoneNumber": "13800138000"
  }
}
```

### 8.2 更新用户信息
- **接口**: `PUT /api/user/profile`
- **认证**: 需要JWT Token
- **请求体**:
```json
{
  "nickname": "文物专家",
  "title": "高级研究员",
  "email": "expert@example.com",
  "avatarUrl": "http://example.com/new_avatar.jpg",
  "fullName": "张三",
  "phoneNumber": "13800138001"
}
```
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": {
    "username": "user123",
    "nickname": "文物专家",
    "title": "高级研究员",
    "email": "expert@example.com",
    "avatarUrl": "http://example.com/new_avatar.jpg",
    "role": "EXPERT",
    "fullName": "张三",
    "phoneNumber": "13800138001"
  }
}
```

## 9. 告警功能模块 (/api/alert)

### 9.1 查询告警列表
- **接口**: `GET /api/alert/list`
- **参数**:
  - `sensorId` (string): 传感器ID
  - `alertType` (string): 告警类型
  - `status` (string): 状态
  - `startTime` (datetime): 开始时间
  - `endTime` (datetime): 结束时间
  - `limit` (int): 限制数量
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": [
    {
      "sensorId": "ems",
      "alertType": "ALERT_TEMP",
      "message": "温度过高告警",
      "relicsId": 1,
      "currentValue": 35.5,
      "threshold": 35.0,
      "status": "ACTIVE",
      "timestamp": "2025-07-06T10:30:00"
    }
  ]
}
```

### 9.2 更新告警状态
- **接口**: `PUT /api/alert/{alertId}/status?status={status}`
- **路径参数**:
  - `alertId` (string): 告警ID
- **参数**:
  - `status` (string): 新状态
- **响应**:
```json
{
  "code": "0000",
  "info": "成功",
  "data": true
}
```

## 10. 首页模块 (/api/index)

### 10.1 随机获取文物
- **接口**: `GET /api/index/random-relics?count={count}`
- **描述**: 为首页随机获取指定数量的文物信息
- **参数**:
  - `count` (int): 获取数量，默认6
- **响应**:
```json
{
  "code": "0000",
  "info": "获取成功",
  "data": {
    "total": 6,
    "list": [
      {
        "relicsId": 1,
        "name": "青铜鼎",
        "description": "商代青铜器",
        "preservation": 1,
        "category": "青铜器",
        "era": "商",
        "material": "青铜",
        "imageUrl": "http://example.com/image.jpg",
        "status": 1,
        "locationId": 1
      }
    ]
  }
}
```

---

## 数据模型说明

### 文物信息模型 (RelicsResponseDTO)
```json
{
  "relicsId": "long - 文物ID",
  "name": "string - 文物名称",
  "description": "string - 文物描述",
  "preservation": "int - 保护等级",
  "category": "string - 类别",
  "era": "string - 朝代",
  "material": "string - 材质",
  "imageUrl": "string - 图片URL",
  "status": "int - 状态",
  "locationId": "int - 位置ID"
}
```

### 用户信息模型 (UserInfoResponseDTO)
```json
{
  "username": "string - 用户名",
  "nickname": "string - 昵称",
  "title": "string - 头衔",
  "email": "string - 邮箱",
  "avatarUrl": "string - 头像URL",
  "role": "string - 角色",
  "fullName": "string - 全名",
  "phoneNumber": "string - 电话号码"
}
```

### 评论信息模型 (CommentResponseDTO)
```json
{
  "commentId": "long - 评论ID",
  "relicsId": "long - 文物ID",
  "username": "string - 用户名",
  "content": "string - 评论内容",
  "createTime": "datetime - 创建时间",
  "isOwner": "boolean - 是否为评论所有者"
}
```

### 收藏信息模型 (FavoriteResponseDTO)
```json
{
  "favoriteId": "long - 收藏ID",
  "relicsId": "long - 文物ID",
  "relicsName": "string - 文物名称",
  "relicsImageUrl": "string - 文物图片URL",
  "relicsDescription": "string - 文物描述",
  "createTime": "datetime - 收藏时间"
}
```

### 告警信息模型 (AlertResponseDTO)
```json
{
  "sensorId": "string - 传感器ID",
  "alertType": "string - 告警类型",
  "message": "string - 告警消息",
  "relicsId": "long - 文物ID",
  "currentValue": "double - 当前值",
  "threshold": "double - 阈值",
  "status": "string - 状态",
  "timestamp": "datetime - 时间戳"
}
```

## 错误处理

### 常见错误响应
```json
{
  "code": "9999",
  "info": "系统异常",
  "data": null
}
```

### 认证错误
```json
{
  "code": "1007",
  "info": "用户名或密码错误",
  "data": null
}
```

### 权限错误
```json
{
  "code": "2101",
  "info": "没有权限",
  "data": null
}
```

## 使用示例

### 1. 用户登录获取Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 2. 使用Token访问需要认证的接口
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer your_jwt_token_here"
```

### 3. 搜索文物
```bash
curl -X GET "http://localhost:8080/api/relics/search/keyword?keyword=青铜器"
```

### 4. 添加收藏
```bash
curl -X POST http://localhost:8080/api/favorites \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token_here" \
  -d '{
    "relicsId": 1
  }'
```

## 开发环境配置

### 数据库配置
- **MySQL**: localhost:3309/relics
- **PostgreSQL**: localhost:5432/vector_db
- **Redis**: localhost:6379

### 外部服务
- **Elasticsearch**: localhost:9200
- **MQTT Broker**: tcp://117.72.63.151:1883:1883

## 版本更新记录

### v1.0.0 (2025-07-06)
- 初始版本发布
- 完成所有核心功能模块
- 集成AI智能化功能
- 支持物联网传感器监控

---

**文档生成时间**: 2025-07-06  
**系统版本**: v1.0.0  
**维护者**: 金凌
**联系邮箱**: 3122973174@qq.com
