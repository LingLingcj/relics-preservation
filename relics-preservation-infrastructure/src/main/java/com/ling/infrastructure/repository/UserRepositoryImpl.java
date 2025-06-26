package com.ling.infrastructure.repository;

import com.ling.domain.login.adapter.IUserRepository;
import com.ling.domain.login.model.valobj.RegisterVO;
import com.ling.infrastructure.dao.IUserDao;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

/**
 * @Author: LingRJ
 * @Description: 用户仓储实现
 * @DateTime: 2025/6/26 23:18
 **/
@Repository
public class UserRepositoryImpl implements IUserRepository {

    @Resource
    private IUserDao userDao;

    @Override
    public void register(RegisterVO registerVO) {

    }
}
