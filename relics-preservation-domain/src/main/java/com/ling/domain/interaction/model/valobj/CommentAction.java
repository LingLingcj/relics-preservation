package com.ling.domain.interaction.model.valobj;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 评论行为值对象
 * @Author: LingRJ
 * @Description: 封装用户评论文物的行为
 * @DateTime: 2025/7/11
 */
@Getter
@EqualsAndHashCode(of = "id")
public class CommentAction {
    
    private final Long id;
    private final Long relicsId;
    private final CommentContent content;
    private final LocalDateTime createTime;
    private CommentStatus status;
    private boolean deleted;
    private LocalDateTime updateTime;
    
    private CommentAction(Long relicsId, CommentContent content) {
        this.id = generateId();
        this.relicsId = Objects.requireNonNull(relicsId, "文物ID不能为空");
        this.content = Objects.requireNonNull(content, "评论内容不能为空");
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
        this.status = content.needsReview() ? CommentStatus.PENDING_REVIEW : CommentStatus.APPROVED;
        this.deleted = false;
        
        if (relicsId <= 0) {
            throw new IllegalArgumentException("文物ID必须大于0");
        }
    }
    
    /**
     * 创建评论行为
     * @param relicsId 文物ID
     * @param content 评论内容
     * @return 评论行为值对象
     */
    public static CommentAction create(Long relicsId, CommentContent content) {
        return new CommentAction(relicsId, content);
    }
    
    /**
     * 生成评论ID
     * @return 评论ID
     */
    private Long generateId() {
        // 使用时间戳 + 随机数生成ID
        return System.currentTimeMillis() * 1000 + (long)(Math.random() * 1000);
    }
    
    /**
     * 审核通过
     */
    public void approve() {
        if (status == CommentStatus.PENDING_REVIEW) {
            this.status = CommentStatus.APPROVED;
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 审核拒绝
     * @param reason 拒绝原因
     */
    public void reject(String reason) {
        this.status = CommentStatus.REJECTED;
        this.updateTime = LocalDateTime.now();
        // 可以记录拒绝原因到日志或其他地方
    }
    
    /**
     * 标记为已删除
     */
    public void delete() {
        this.deleted = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 是否可见
     * @return 是否可见
     */
    public boolean isVisible() {
        return !deleted && status == CommentStatus.APPROVED;
    }
    
    /**
     * 是否需要审核
     * @return 是否需要审核
     */
    public boolean needsReview() {
        return status == CommentStatus.PENDING_REVIEW;
    }
    
    /**
     * 获取评论时长（小时）
     * @return 评论时长
     */
    public long getCommentHours() {
        return java.time.Duration.between(createTime, LocalDateTime.now()).toHours();
    }
    
    /**
     * 是否为新评论（1小时内）
     * @return 是否为新评论
     */
    public boolean isNewComment() {
        return getCommentHours() < 1;
    }
    
    /**
     * 获取内容摘要
     * @return 内容摘要
     */
    public String getContentSummary() {
        return content.getSummary();
    }
    
    /**
     * 获取完整内容
     * @return 完整内容
     */
    public String getFullContent() {
        return content.getContent();
    }

    
    @Override
    public String toString() {
        return String.format("CommentAction{id=%d, relicsId=%d, status=%s, content='%s'}", 
                id, relicsId, status, content.getSummary());
    }
}
