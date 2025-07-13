package com.ling.domain.interaction.model.valobj;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 评论内容值对象
 * @Author: LingRJ
 * @Description: 封装评论内容及其业务规则
 * @DateTime: 2025/7/11
 */
@Getter
@EqualsAndHashCode(of = "content")
@NoArgsConstructor(force = true)
public class CommentContent {
    
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 500;
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            ".*(?:垃圾|废物|傻逼|操你|fuck|shit).*", Pattern.CASE_INSENSITIVE);
    
    private final String content;
    private final int length;
    private final boolean containsSensitiveWords;
    
    private CommentContent(String content) {
        this.content = Objects.requireNonNull(content, "评论内容不能为空").trim();
        this.length = this.content.length();
        this.containsSensitiveWords = SENSITIVE_PATTERN.matcher(this.content).matches();
        
        validateContent();
    }
    
    /**
     * 创建评论内容值对象
     * @param content 评论内容
     * @return 评论内容值对象
     */
    public static CommentContent of(String content) {
        return new CommentContent(content);
    }
    
    /**
     * 验证评论内容
     */
    private void validateContent() {
        if (content.isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        
        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException("评论内容至少需要" + MIN_LENGTH + "个字符");
        }
        
        if (length > MAX_LENGTH) {
            throw new IllegalArgumentException("评论内容不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 检查是否只包含空白字符
        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能只包含空白字符");
        }
    }
    
    /**
     * 是否需要审核
     * @return 是否需要审核
     */
    public boolean needsReview() {
        return containsSensitiveWords || length > 200;
    }
    
    /**
     * 获取内容摘要
     * @param maxLength 最大长度
     * @return 内容摘要
     */
    public String getSummary(int maxLength) {
        if (length <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * 获取默认摘要（50字符）
     * @return 内容摘要
     */
    public String getSummary() {
        return getSummary(50);
    }
    
    /**
     * 是否为长评论
     * @return 是否为长评论
     */
    public boolean isLongComment() {
        return length > 100;
    }
    
    /**
     * 获取内容类型
     * @return 内容类型
     */
    public CommentType getType() {
        if (containsSensitiveWords) {
            return CommentType.SENSITIVE;
        } else if (length > 200) {
            return CommentType.LONG;
        } else if (length < 10) {
            return CommentType.SHORT;
        } else {
            return CommentType.NORMAL;
        }
    }
    
    /**
     * 评论类型枚举
     */
    public enum CommentType {
        SHORT("短评论"),
        NORMAL("普通评论"),
        LONG("长评论"),
        SENSITIVE("敏感评论");
        
        private final String description;
        
        CommentType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("CommentContent{length=%d, type=%s, content='%s'}", 
                length, getType(), getSummary());
    }
}
