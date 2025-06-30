package com.ling.domain.comment.service.impl;

import com.ling.domain.comment.adapter.ICommentRepository;
import com.ling.domain.comment.model.entity.CommentEntity;
import com.ling.domain.comment.model.valobj.CommentVO;
import com.ling.domain.comment.service.ICommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 文物评论服务实现
 */
@Slf4j
@Service
public class CommentServiceImpl implements ICommentService {
    
    @Autowired
    private ICommentRepository commentRepository;

    @Override
    public CommentEntity addComment(CommentVO commentVO) {
        CommentEntity commentEntity = new CommentEntity();
        BeanUtils.copyProperties(commentVO, commentEntity);
        
        // 设置初始状态和时间
        // 0-正常
        commentEntity.setStatus(0);
        LocalDateTime now = LocalDateTime.now();
        commentEntity.setCreateTime(now);
        commentEntity.setUpdateTime(now);
        
        return commentRepository.saveComment(commentEntity);
    }

    @Override
    public List<CommentEntity> getCommentsByRelicsId(Long relicsId) {
        return commentRepository.getCommentsByRelicsId(relicsId);
    }

    @Override
    public boolean deleteComment(Long commentId, String username) {
        // 获取评论
        CommentEntity comment = commentRepository.getCommentById(commentId);
        
        // 检查评论是否存在及用户权限
        if (comment == null) {
            log.warn("评论不存在, commentId: {}", commentId);
            return false;
        }
        
        if (!comment.getUsername().equals(username)) {
            log.warn("用户无权限删除该评论, commentId: {}, username: {}, commentUsername: {}", 
                    commentId, username, comment.getUsername());
            return false;
        }
        
        // 执行删除
        int result = commentRepository.deleteComment(commentId);
        return result > 0;
    }

    @Override
    public Map<String, Object> getCommentsByPage(Long relicsId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 分页查询
        List<CommentEntity> comments = commentRepository.getCommentsByPage(relicsId, page, size);
        int total = commentRepository.countCommentsByRelicsId(relicsId);
        
        result.put("list", comments);
        result.put("total", total);
        
        return result;
    }
} 