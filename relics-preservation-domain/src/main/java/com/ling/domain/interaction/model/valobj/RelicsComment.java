package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 文物评论值对象
 * @Author: LingRJ
 * @Description: 用于文物详情页展示的评论信息，只包含公开可见的字段
 * @DateTime: 2025/7/11
 */
@Getter
public class RelicsComment {
    
    /**
     * 评论ID
     */
    private final Long commentId;
    
    /**
     * 文物ID
     */
    private final Long relicsId;
    
    /**
     * 评论用户名
     */
    private final String username;
    
    /**
     * 评论内容
     */
    private final String content;
    
    /**
     * 评论发表时间
     */
    private final LocalDateTime createTime;
    
    /**
     * 点赞数（预留字段）
     */
    private final Integer likeCount;
    
    /**
     * 是否为精选评论（预留字段）
     */
    private final Boolean featured;
    
    /**
     * 构造函数
     */
    public RelicsComment(Long commentId, Long relicsId, String username, 
                        String content, LocalDateTime createTime, 
                        Integer likeCount, Boolean featured) {
        this.commentId = Objects.requireNonNull(commentId, "评论ID不能为空");
        this.relicsId = Objects.requireNonNull(relicsId, "文物ID不能为空");
        this.username = Objects.requireNonNull(username, "用户名不能为空");
        this.content = Objects.requireNonNull(content, "评论内容不能为空");
        this.createTime = Objects.requireNonNull(createTime, "创建时间不能为空");
        this.likeCount = likeCount != null ? likeCount : 0;
        this.featured = featured != null ? featured : false;
    }
    
    /**
     * 简化构造函数
     */
    public RelicsComment(Long commentId, Long relicsId, String username, 
                        String content, LocalDateTime createTime) {
        this(commentId, relicsId, username, content, createTime, 0, false);
    }
    
    /**
     * 获取评论摘要（用于列表展示）
     * @param maxLength 最大长度
     * @return 评论摘要
     */
    public String getContentSummary(int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * 检查是否为最近评论（24小时内）
     * @return 是否为最近评论
     */
    public boolean isRecent() {
        return createTime.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    /**
     * 获取脱敏用户名（如果需要的话）
     * @return 脱敏用户名
     */
    public String getMaskedUsername() {
        if (username == null || username.length() <= 2) {
            return username;
        }
        // 保留首尾字符，中间用*替代
        if (username.length() <= 4) {
            return username.charAt(0) + "*" + username.charAt(username.length() - 1);
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
    
    /**
     * 检查评论内容是否过长
     * @param maxLength 最大长度
     * @return 是否过长
     */
    public boolean isContentTooLong(int maxLength) {
        return content != null && content.length() > maxLength;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelicsComment that = (RelicsComment) o;
        return Objects.equals(commentId, that.commentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(commentId);
    }
    
    @Override
    public String toString() {
        return "RelicsComment{" +
                "commentId=" + commentId +
                ", relicsId=" + relicsId +
                ", username='" + username + '\'' +
                ", content='" + getContentSummary(50) + '\'' +
                ", createTime=" + createTime +
                ", likeCount=" + likeCount +
                ", featured=" + featured +
                '}';
    }
    
    /**
     * 创建构建器
     * @return 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private Long commentId;
        private Long relicsId;
        private String username;
        private String content;
        private LocalDateTime createTime;
        private Integer likeCount = 0;
        private Boolean featured = false;
        
        public Builder commentId(Long commentId) {
            this.commentId = commentId;
            return this;
        }
        
        public Builder relicsId(Long relicsId) {
            this.relicsId = relicsId;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public Builder likeCount(Integer likeCount) {
            this.likeCount = likeCount;
            return this;
        }
        
        public Builder featured(Boolean featured) {
            this.featured = featured;
            return this;
        }
        
        public RelicsComment build() {
            return new RelicsComment(commentId, relicsId, username, content, 
                                   createTime, likeCount, featured);
        }
    }
}
