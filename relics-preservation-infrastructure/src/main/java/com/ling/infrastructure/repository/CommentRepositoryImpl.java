package com.ling.infrastructure.repository;

import com.ling.domain.comment.adapter.ICommentRepository;
import com.ling.domain.comment.model.entity.CommentEntity;
import com.ling.infrastructure.dao.ICommentDao;
import com.ling.infrastructure.dao.po.Comment;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 评论仓储实现
 */
@Repository
public class CommentRepositoryImpl implements ICommentRepository {

    @Autowired
    private ICommentDao commentDao;
    
    @Override
    public CommentEntity saveComment(CommentEntity commentEntity) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentEntity, comment);
        
        // 保存到数据库
        commentDao.insert(comment);
        // ID回填到实体
        commentEntity.setId(comment.getId());
        
        return commentEntity;
    }

    @Override
    public List<CommentEntity> getCommentsByRelicsId(Long relicsId) {
        List<Comment> comments = commentDao.selectByRelicsId(relicsId);
        return comments.stream().map(this::convertToEntity).collect(Collectors.toList());
    }

    @Override
    public int deleteComment(Long commentId) {
        return commentDao.deleteById(commentId);
    }

    @Override
    public CommentEntity getCommentById(Long commentId) {
        Comment comment = commentDao.selectById(commentId);
        if (comment == null) {
            return null;
        }
        return convertToEntity(comment);
    }

    @Override
    public List<CommentEntity> getCommentsByPage(Long relicsId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentDao.selectByPage(relicsId, offset, size);
        return comments.stream().map(this::convertToEntity).collect(Collectors.toList());
    }

    @Override
    public int countCommentsByRelicsId(Long relicsId) {
        return commentDao.countByRelicsId(relicsId);
    }
    
    /**
     * PO转Entity
     */
    private CommentEntity convertToEntity(Comment comment) {
        CommentEntity entity = new CommentEntity();
        BeanUtils.copyProperties(comment, entity);
        return entity;
    }
} 