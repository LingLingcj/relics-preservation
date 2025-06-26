package com.ling.domain.login.adapter;

import com.ling.domain.login.model.valobj.RegisterVO;

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
}
