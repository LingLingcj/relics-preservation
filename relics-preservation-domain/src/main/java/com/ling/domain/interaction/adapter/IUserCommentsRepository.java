package com.ling.domain.interaction.adapter;

import java.util.Optional;

import com.ling.domain.interaction.model.entity.UserComments;
import com.ling.domain.user.model.valobj.Username;

/**
 * 用户评论仓储接口
 * @Author: LingRJ
 * @Description: 用户评论聚合根的数据访问接口
 * @DateTime: 2025/7/13
 */
public interface IUserCommentsRepository {

    /**
     * 根据用户名查找用户评论聚合根
     * @param username 用户名
     * @return 用户评论聚合根
     */
    Optional<UserComments> findByUsername(Username username);

    /**
     * 保存用户评论聚合根（增量保存）
     * @param userComments 用户评论聚合根
     * @return 保存是否成功
     */
    boolean saveIncremental(UserComments userComments);


    /**
     * 保存用户评论聚合根（全量保存）
     * @param userComments 用户评论聚合根
     * @return 保存是否成功
     */
    boolean save(UserComments userComments);

    /**
     * 删除用户评论聚合根
     * @param username 用户名
     * @return 删除是否成功
     */
    boolean deleteByUsername(Username username);

    /**
     * 检查用户是否存在评论记录
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(Username username);
}
