package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏DAO接口
 * @author 31229
 */
@Mapper
public interface IFavoriteDao {
    
    /**
     * 添加收藏
     * @param favorite 收藏记录
     * @return 影响行数
     */
    int insertFavorite(Favorite favorite);
    
    /**
     * 取消收藏
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 影响行数
     */
    int deleteFavorite(@Param("username") String username, @Param("relicsId") Long relicsId);
    
    /**
     * 查询收藏记录
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 收藏记录
     */
    Favorite findByUsernameAndRelicsId(@Param("username") String username, @Param("relicsId") Long relicsId);
    
    /**
     * 获取用户收藏列表
     * @param username 用户名
     * @param offset 偏移量
     * @param limit 数量
     * @return 收藏列表
     */
    List<Favorite> findByUsername(@Param("username") String username, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户收藏数量
     * @param username 用户名
     * @return 收藏数量
     */
    int countByUsername(@Param("username") String username);
} 