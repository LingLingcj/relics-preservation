package com.ling.infrastructure.repository;

import com.ling.domain.auth.adapter.IUserRepository;
import com.ling.domain.auth.model.entity.UserEntity;
import com.ling.domain.auth.model.valobj.RegisterVO;
import com.ling.infrastructure.dao.IUserDao;
import com.ling.infrastructure.dao.po.User;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

/**
* @Author: LingRJ
* @Description: 用户仓储实现
* @DateTime: 2025/6/27 9:06
**/
@Repository
public class UserRepositoryImpl implements IUserRepository {

    @Autowired
    private IUserDao userDao;

    @Override
    public void register(RegisterVO registerVO) {

    }

    @Override
    public boolean save(UserEntity userEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        return userDao.insertUser(user) > 0;
    }

    @Override
    public boolean updatePassword(UserEntity userEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        return userDao.updatePassword(user) > 0;
    }

    @Override
    public UserEntity findByUsername(String username) {
        User user = userDao.findByUsernameOrEmail(username);
        if (user == null) {
            return null;
        }
        
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(user, userEntity);
        return userEntity;
    }

    @Override
    public UserEntity findByUsernameOrEmail(String usernameOrEmail) {
        User user = userDao.findByUsernameOrEmail(usernameOrEmail);
        if (user == null) {
            return null;
        }
        
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(user, userEntity);
        return userEntity;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }
}
