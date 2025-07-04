package com.ling.domain.auth.adapter;

import com.ling.domain.auth.model.entity.UserEntity;
import com.ling.domain.auth.model.valobj.RegisterVO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @Author: LingRJ
 * @Description: 用户仓储
 * @DateTime: 2025/6/26 23:17
 **/
public interface IUserRepository {
    /**
     * 注册
     * @param registerVO 所需值对象
     */
    void register(RegisterVO registerVO);

    /**
     * 根据用户名或邮件地址查找用户
     * @param usernameOrEmail 用户名或邮件地址
     * @return 用户实体
     */
    UserEntity findByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException;

    /**
     * 根据用户名查找用户是否存在
     * @param username 用户名
     * @return 查找结果
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已经存在（排除当前用户）
     * @param email 邮箱地址
     * @param currentUsername 当前用户名（排除自己）
     * @return 是否存在
     */
    boolean existsByEmailExcludeCurrentUser(String email, String currentUsername);
    
    /**
     * 检查手机号是否已经存在（排除当前用户）
     * @param phoneNumber 手机号
     * @param currentUsername 当前用户名（排除自己）
     * @return 是否存在
     */
    boolean existsByPhoneNumberExcludeCurrentUser(String phoneNumber, String currentUsername);

    /**
     * 保存用户
     * @param user 用户信息
     * @return 保存结果
     */
    boolean save(UserEntity user);

    /**
     * 根据用户名查找用户信息
     * @param username 用户名
     * @return 用户实体信息
     */
    UserEntity findByUsername(String username);

    /**
     * 更新用户密码
     * @param userEntity 用户实体
     * @return 更新结果
     */
    boolean updatePassword(UserEntity userEntity);

    /**
     * 更新用户信息
     * @param userEntity 用户实体
     * @return 更新结果
     */
    boolean updateProfile(UserEntity userEntity);
}
