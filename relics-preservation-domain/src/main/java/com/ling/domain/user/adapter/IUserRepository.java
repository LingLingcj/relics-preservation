package com.ling.domain.user.adapter;

import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口（改进版）
 * @Author: LingRJ
 * @Description: 提供类型安全的用户数据访问接口，支持DDD值对象
 * @DateTime: 2025/7/11
 */
public interface IUserRepository {
    
    // ==================== 聚合根操作 ====================
    
    /**
     * 保存用户聚合根
     * @param user 用户聚合根
     * @return 保存结果
     */
    boolean save(User user);
    
    /**
     * 根据用户名查找用户聚合根
     * @param username 用户名值对象
     * @return 用户聚合根
     */
    Optional<User> findByUsername(Username username);
    
    /**
     * 根据邮箱查找用户聚合根
     * @param email 邮箱值对象
     * @return 用户聚合根
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * 根据手机号查找用户聚合根
     * @param phoneNumber 手机号值对象
     * @return 用户聚合根
     */
    Optional<User> findByPhoneNumber(PhoneNumber phoneNumber);
    
    // ==================== 存在性检查 ====================
    
    /**
     * 检查用户名是否存在
     * @param username 用户名值对象
     * @return 是否存在
     */
    boolean existsByUsername(Username username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱值对象
     * @return 是否存在
     */
    boolean existsByEmail(Email email);
    
    /**
     * 检查手机号是否存在
     * @param phoneNumber 手机号值对象
     * @return 是否存在
     */
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
    
    /**
     * 检查邮箱是否已存在（排除当前用户）
     * @param email 邮箱值对象
     * @param currentUsername 当前用户名
     * @return 是否存在
     */
    boolean existsByEmailExcludeCurrentUser(Email email, Username currentUsername);
    
    /**
     * 检查手机号是否已存在（排除当前用户）
     * @param phoneNumber 手机号值对象
     * @param currentUsername 当前用户名
     * @return 是否存在
     */
    boolean existsByPhoneNumberExcludeCurrentUser(PhoneNumber phoneNumber, Username currentUsername);
    
    // ==================== 查询操作 ====================
    
    /**
     * 根据角色查找用户列表
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 根据状态查找用户列表
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * 分页查询用户
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> findAll(int page, int size);
    
    /**
     * 统计用户总数
     * @return 用户总数
     */
    long count();
    
    /**
     * 根据角色统计用户数量
     * @param role 用户角色
     * @return 用户数量
     */
    long countByRole(UserRole role);
    
    /**
     * 根据状态统计用户数量
     * @param status 用户状态
     * @return 用户数量
     */
    long countByStatus(UserStatus status);
    
    // ==================== 删除操作 ====================
    
    /**
     * 软删除用户（将状态设为禁用）
     * @param user 用户聚合根
     * @return 删除结果
     */
    boolean softDelete(User user);
    
    /**
     * 物理删除用户（谨慎使用）
     * @param username 用户名值对象
     * @return 删除结果
     */
    boolean hardDelete(Username username);
}
