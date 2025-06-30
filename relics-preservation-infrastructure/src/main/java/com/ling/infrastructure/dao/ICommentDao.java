package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description: 文物评论DAO接口
 */
@Repository
public interface ICommentDao {
    
    /**
     * 保存评论
     * @param comment 评论对象
     * @return 影响行数
     */
    int insert(Comment comment);
    
    /**
     * 根据ID查询评论
     * @param id 评论ID
     * @return 评论对象
     */
    Comment selectById(Long id);
    
    /**
     * 根据文物ID查询评论列表
     * @param relicsId 文物ID
     * @return 评论列表
     */
    List<Comment> selectByRelicsId(Long relicsId);
    
    /**
     * 删除评论（逻辑删除）
     * @param id 评论ID
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 分页查询评论
     * @param relicsId 文物ID
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 评论列表
     */
    List<Comment> selectByPage(@Param("relicsId") Long relicsId, 
                              @Param("offset") int offset, 
                              @Param("limit") int limit);
    
    /**
     * 统计文物评论数量
     * @param relicsId 文物ID
     * @return 评论数量
     */
    int countByRelicsId(Long relicsId);
} 