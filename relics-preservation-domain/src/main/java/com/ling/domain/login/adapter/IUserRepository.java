package com.ling.domain.login.adapter;

import com.ling.domain.login.model.entity.UserEntity;
import com.ling.domain.login.model.valobj.RegisterVO;
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
}
