package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: LingRJ
 * @Description: user接口
 * @DateTime: 2025/6/26 22:53
 **/
@Mapper
public interface IUserDao {
    /**
     * 根据用户名或邮箱查询用户
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户对象
     */
    User findByUsernameOrEmail(@Param("value")String usernameOrEmail);
    
    /**
     * 插入用户
     * @param user 用户对象
     * @return 影响行数
     */
    int insertUser(User user);
    
    /**
     * 更新用户密码
     * @param user 用户对象
     * @return 影响行数
     */
    int updatePassword(User user);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否已经存在（排除当前用户）
     * @param email 邮箱地址
     * @param currentUsername 当前用户名（排除自己）
     * @return 是否存在
     */
    boolean existsByEmailExcludeCurrentUser(@Param("email") String email, @Param("currentUsername") String currentUsername);
    
    /**
     * 检查手机号是否已经存在（排除当前用户）
     * @param phoneNumber 手机号
     * @param currentUsername 当前用户名（排除自己）
     * @return 是否存在
     */
    boolean existsByPhoneNumberExcludeCurrentUser(@Param("phoneNumber") String phoneNumber, @Param("currentUsername") String currentUsername);
    
    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响行数
     */
    int updateProfile(User user);
}
