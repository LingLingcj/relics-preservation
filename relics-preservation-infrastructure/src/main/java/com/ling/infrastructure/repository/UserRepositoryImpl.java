package com.ling.infrastructure.repository;

import com.ling.domain.login.adapter.IUserRepository;
import com.ling.domain.login.model.entity.UserEntity;
import com.ling.domain.login.model.valobj.RegisterVO;
import com.ling.infrastructure.dao.IUserDao;
import com.ling.infrastructure.dao.po.User;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

/**
* @Author: LingRJ
* @Description: 用户仓储实现
* @DateTime: 2025/6/27 9:06
**/
@Repository
public class UserRepositoryImpl implements IUserRepository {

    @Resource
    private IUserDao userDao;

    @Override
    public void register(RegisterVO registerVO) {

    }

    @Override
    public UserEntity findByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userDao.findByUsernameOrEmail(usernameOrEmail);

        if (user == null) {
            return null;
        }

        return UserEntity.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .status(user.getStatus())
                .build();
    }
}
