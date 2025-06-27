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
    User findByUsernameOrEmail(@Param("value")String usernameOrEmail);
}
