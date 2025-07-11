# 权限管理系统配置指南

## 🎯 系统概述

文物保护系统采用基于角色的访问控制（RBAC），结合 Spring Security 和自定义权限验证机制，实现细粒度的权限管理。

## 🔐 角色体系

### 1. 用户角色定义

| 角色 | 代码 | 等级 | 描述 | 权限 |
|------|------|------|------|------|
| 普通用户 | USER | 1 | 普通访客用户 | READ_RELICS, COMMENT, FAVORITE |
| 专家 | EXPERT | 2 | 文物专家 | READ_RELICS, COMMENT, FAVORITE, UPLOAD_RELICS, REVIEW_COMMENTS |
| 管理员 | ADMIN | 3 | 系统管理员 | 所有权限 + MANAGE_USERS, SYSTEM_CONFIG |

### 2. 权限详细说明

- **READ_RELICS**: 查看文物信息
- **COMMENT**: 发表评论
- **FAVORITE**: 收藏文物
- **UPLOAD_RELICS**: 上传文物信息
- **REVIEW_COMMENTS**: 审核评论
- **MANAGE_USERS**: 管理用户
- **SYSTEM_CONFIG**: 系统配置

## 🛡️ 权限验证机制

### 1. Spring Security 配置

```java
// 基础权限配置
authorize.requestMatchers("/api/v1/admin/comments/**").hasAnyRole("EXPERT", "ADMIN");
authorize.requestMatchers("/api/relics/upload").hasRole("EXPERT");
authorize.requestMatchers("/api/interaction/**").authenticated();
```

### 2. 方法级权限注解

```java
// Spring Security 注解
@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")

// 自定义权限注解
@CommentReviewPermission("评论审核权限")
```

### 3. 自定义权限验证

通过 AOP 切面实现的自定义权限验证：

```java
@CommentReviewPermission(
    value = "评论审核权限",
    allowExpert = true,
    allowAdmin = true
)
```

## 📋 接口权限配置

### 1. 公开接口（无需认证）

```
GET /api/auth/**           - 认证相关
GET /api/relics/**         - 文物信息查看
GET /api/knowledge/rag     - 知识问答
GET /api/test/public       - 公开测试接口
```

### 2. 认证接口（需要登录）

```
GET /api/interaction/**    - 用户交互
GET /api/favorite/**       - 收藏管理
GET /api/comment/**        - 评论管理
GET /api/test/protected    - 受保护测试接口
```

### 3. 专家接口（EXPERT 角色）

```
POST /api/relics/upload    - 文物上传
GET  /api/test/expert      - 专家测试接口
```

### 4. 管理员接口（ADMIN 角色）

```
GET /api/test/admin        - 管理员测试接口
```

### 5. 审核接口（EXPERT 或 ADMIN 角色）

```
GET  /api/v1/admin/comments/pending           - 获取待审核评论
POST /api/v1/admin/comments/{id}/review       - 审核单个评论
POST /api/v1/admin/comments/batch-review      - 批量审核评论
GET  /api/v1/admin/comments/history           - 审核历史
GET  /api/v1/admin/comments/statistics        - 审核统计
```

## 🧪 权限测试

### 1. 测试接口

| 接口 | 权限要求 | 用途 |
|------|----------|------|
| `/api/test/public` | 无 | 测试公开访问 |
| `/api/test/protected` | 认证用户 | 测试基础认证 |
| `/api/test/expert` | EXPERT | 测试专家权限 |
| `/api/test/admin` | ADMIN | 测试管理员权限 |
| `/api/test/comment-review` | EXPERT/ADMIN | 测试审核权限 |
| `/api/test/user-info` | 认证用户 | 获取用户信息 |

### 2. 测试用例

#### 测试用例 1：公开接口访问
```bash
curl -X GET "http://localhost:8080/api/test/public"
# 预期：200 OK，无需认证
```

#### 测试用例 2：认证接口访问
```bash
# 无 Token
curl -X GET "http://localhost:8080/api/test/protected"
# 预期：401 Unauthorized

# 有效 Token
curl -X GET "http://localhost:8080/api/test/protected" \
  -H "Authorization: Bearer YOUR_TOKEN"
# 预期：200 OK，返回用户信息
```

#### 测试用例 3：专家权限测试
```bash
# 普通用户 Token
curl -X GET "http://localhost:8080/api/test/expert" \
  -H "Authorization: Bearer USER_TOKEN"
# 预期：403 Forbidden

# 专家 Token
curl -X GET "http://localhost:8080/api/test/expert" \
  -H "Authorization: Bearer EXPERT_TOKEN"
# 预期：200 OK
```

#### 测试用例 4：评论审核权限测试
```bash
# 普通用户
curl -X GET "http://localhost:8080/api/test/comment-review" \
  -H "Authorization: Bearer USER_TOKEN"
# 预期：403 Forbidden

# 专家用户
curl -X GET "http://localhost:8080/api/test/comment-review" \
  -H "Authorization: Bearer EXPERT_TOKEN"
# 预期：200 OK

# 管理员用户
curl -X GET "http://localhost:8080/api/test/comment-review" \
  -H "Authorization: Bearer ADMIN_TOKEN"
# 预期：200 OK
```

## 🔧 配置说明

### 1. 用户角色配置

在 `UserRole` 枚举中定义：

```java
USER("USER", "普通用户", 1, Set.of("READ_RELICS", "COMMENT", "FAVORITE")),
EXPERT("EXPERT", "专家", 2, Set.of("READ_RELICS", "COMMENT", "FAVORITE", "UPLOAD_RELICS", "REVIEW_COMMENTS")),
ADMIN("ADMIN", "管理员", 3, Set.of("READ_RELICS", "COMMENT", "FAVORITE", "UPLOAD_RELICS", "REVIEW_COMMENTS", "MANAGE_USERS", "SYSTEM_CONFIG"));
```

### 2. Spring Security 配置

在 `SpringSecurityConfig` 中配置：

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests((authorize) -> {
        // 评论审核权限
        authorize.requestMatchers("/api/v1/admin/comments/**").hasAnyRole("EXPERT", "ADMIN");
        // 其他配置...
    });
}
```

### 3. 自定义权限验证

使用 `@CommentReviewPermission` 注解：

```java
@CommentReviewPermission("评论审核权限")
public Response<?> reviewComment() {
    // 方法实现
}
```

## 🚨 安全注意事项

### 1. Token 安全
- JWT Token 应设置合理的过期时间
- 敏感操作应要求重新认证
- Token 应通过 HTTPS 传输

### 2. 权限检查
- 所有敏感接口都应进行权限验证
- 权限检查应在业务逻辑执行前进行
- 权限验证失败应记录日志

### 3. 错误处理
- 权限不足时返回 403 Forbidden
- 未认证时返回 401 Unauthorized
- 不要在错误信息中泄露敏感信息

## 📊 权限监控

### 1. 日志记录
- 记录所有权限验证操作
- 记录权限验证失败的尝试
- 记录敏感操作的执行

### 2. 审计追踪
- 记录用户的关键操作
- 保留操作时间和IP地址
- 定期审查权限使用情况

## 🔄 权限升级流程

### 1. 用户申请专家权限
1. 用户提交专家认证申请
2. 管理员审核申请材料
3. 审核通过后升级用户角色
4. 通知用户权限变更

### 2. 权限回收
1. 定期审查用户权限
2. 回收不活跃用户的高级权限
3. 记录权限变更日志

通过这套完整的权限管理系统，确保文物保护系统的安全性和可控性。
