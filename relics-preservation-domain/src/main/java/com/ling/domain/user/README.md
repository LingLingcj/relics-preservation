# User 领域重构说明

## 概述

本次重构将原有的 `auth` 领域重命名为 `user` 领域，并基于领域驱动设计（DDD）原则进行了全面的架构优化。

## 重构内容

### 1. 领域重命名
- **原名称**: `com.ling.domain.auth`
- **新名称**: `com.ling.domain.user`
- **重命名原因**: `auth` 名称过于狭窄，实际业务范围涵盖了用户管理的完整生命周期

### 2. 充血领域模型设计

#### 2.1 聚合根 (Aggregate Root)
- **User**: 用户聚合根，封装用户的完整业务逻辑
  - 用户认证 (`authenticate`)
  - 密码修改 (`changePassword`)
  - 资料更新 (`updateProfile`)
  - 状态变更 (`changeStatus`)
  - 权限检查 (`hasPermission`)

#### 2.2 值对象 (Value Objects)
- **Username**: 用户名值对象，包含格式验证和业务规则
- **Password**: 密码值对象，封装加密和验证逻辑
- **Email**: 邮箱值对象，包含格式验证和域名解析
- **PhoneNumber**: 手机号值对象，支持中国大陆手机号验证
- **UserRole**: 用户角色值对象，包含权限管理逻辑
- **UserStatus**: 用户状态值对象，包含状态转换规则

### 3. 领域事件 (Domain Events)

#### 3.1 事件基础设施
- **DomainEvent**: 领域事件基础接口
- **DomainEventPublisher**: 事件发布器，集成Spring事件机制

#### 3.2 具体事件
- **UserRegisteredEvent**: 用户注册事件
- **UserLoggedInEvent**: 用户登录事件
- **UserProfileUpdatedEvent**: 用户资料更新事件
- **PasswordChangedEvent**: 密码修改事件

#### 3.3 事件处理器
- **UserEventHandler**: 异步处理用户相关事件
  - 发送欢迎邮件
  - 记录审计日志
  - 更新统计信息
  - 同步外部系统

### 4. 改进的仓储接口

#### 4.1 类型安全的仓储接口
- **IUserRepository**: 新的类型安全仓储接口
  - 使用值对象作为参数类型
  - 支持聚合根操作
  - 提供丰富的查询方法

#### 4.2 适配器模式
- **UserRepositoryAdapter**: 适配新接口到现有实现
  - 保持向后兼容性
  - 支持渐进式迁移

### 5. 领域服务重构

#### 5.1 职责分离
- **IUserAuthenticationService**: 专门处理认证逻辑
- **IUserRegistrationService**: 专门处理注册逻辑
- **IUserManagementService**: 专门处理用户管理

#### 5.2 结果对象
- 使用专门的结果对象封装操作结果
- 提供详细的错误信息和状态码

## 向后兼容性

### 1. 现有代码保持不变
- 所有现有的 `auth` 领域代码继续工作
- 现有的API接口保持不变
- 现有的测试无需修改

### 2. 渐进式迁移
- 新功能可以选择性使用新的值对象和接口
- 事件发布是可选的，不影响主流程
- 可以逐步将现有代码迁移到新架构

### 3. 适配器支持
- 提供适配器将新的聚合根转换为现有实体
- 支持新旧接口的互操作

## 使用示例

### 1. 创建用户
```java
// 使用新的聚合根
User user = User.create("username", "password123", "USER");

// 自动发布用户注册事件
// UserRegisteredEvent 会被异步处理
```

### 2. 用户认证
```java
// 使用聚合根的业务方法
boolean authenticated = user.authenticate("password123");

// 认证成功会自动发布登录事件
// UserLoggedInEvent 会被异步处理
```

### 3. 修改密码
```java
// 业务逻辑封装在聚合根中
user.changePassword("oldPassword", "newPassword123");

// 自动发布密码修改事件
// PasswordChangedEvent 会被异步处理
```

### 4. 更新资料
```java
// 使用聚合根的业务方法
user.updateProfile("昵称", "真实姓名", "email@example.com", 
                  "13812345678", "avatar.jpg", "职位");

// 自动发布资料更新事件
// UserProfileUpdatedEvent 会被异步处理
```

### 5. 使用值对象
```java
// 类型安全的值对象
Username username = Username.of("validuser");
Email email = Email.of("user@example.com");
Password password = Password.of("securepass123");

// 值对象包含业务规则验证
boolean isValid = Username.isValid("testuser");
String domain = email.getDomain();
boolean matches = password.matches("inputPassword");
```

### 6. 仓储操作
```java
// 使用新的类型安全仓储接口
Optional<User> user = userRepository.findByUsername(Username.of("testuser"));
boolean exists = userRepository.existsByEmail(Email.of("test@example.com"));
List<User> experts = userRepository.findByRole(UserRole.EXPERT);
```

## 配置要求

### 1. 异步事件处理
```java
@Configuration
@EnableAsync
public class DomainEventConfig {
    @Bean("domainEventExecutor")
    public Executor domainEventExecutor() {
        // 配置异步线程池
    }
}
```

### 2. 事件处理器
- 确保 `UserEventHandler` 被Spring扫描到
- 配置适当的日志级别查看事件处理日志

## 测试

### 1. 单元测试
- `UserDomainTest`: 测试领域模型的核心业务逻辑
- 包含值对象、聚合根、业务规则的完整测试

### 2. 集成测试
- 测试事件发布和处理
- 测试仓储适配器
- 测试领域服务

## 注意事项

1. **事件发布失败不影响主流程**: 所有事件发布都包装在try-catch中
2. **值对象不可变**: 所有值对象都是不可变的，确保线程安全
3. **业务规则集中**: 业务逻辑集中在聚合根和值对象中
4. **类型安全**: 使用值对象提供编译时类型安全
5. **可扩展性**: 新的事件和业务规则可以轻松添加

## 后续计划

1. **完整迁移**: 逐步将现有代码迁移到新架构
2. **性能优化**: 优化事件处理和仓储查询性能
3. **监控集成**: 添加业务指标监控和告警
4. **文档完善**: 补充更详细的API文档和使用指南
