package com.ling.domain.interaction.model.valobj;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 个人笔记值对象
 * @Author: LingRJ
 * @Description: 用户对文物的个人学习笔记和心得
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@EqualsAndHashCode(of = {"noteId", "updateTime"})
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Slf4j
public class PersonalNote {
    
    /** 笔记ID */
    private final String noteId;
    
    /** 文物ID */
    private final Long relicsId;
    
    /** 笔记标题 */
    private final String title;
    
    /** 笔记内容 */
    private final String content;
    
    /** 笔记标签 */
    private final List<String> tags;
    
    /** 学习重点 */
    private final List<String> keyPoints;
    
    /** 个人评分（1-5星） */
    private final Integer rating;
    
    /** 学习状态 */
    private final LearningStatus status;
    
    /** 创建时间 */
    private final LocalDateTime createTime;
    
    /** 更新时间 */
    private final LocalDateTime updateTime;
    
    /** 是否收藏此笔记 */
    private final boolean isFavorite;
    
    /** 笔记类型 */
    private final NoteType noteType;
    
    /**
     * 学习状态枚举
     */
    public enum LearningStatus {
        NOT_STARTED("未开始", "还未开始学习此文物"),
        LEARNING("学习中", "正在学习此文物"),
        REVIEWED("已复习", "已经复习过此文物"),
        MASTERED("已掌握", "已经掌握此文物知识"),
        EXPERT("专家级", "对此文物有深入理解");
        
        private final String name;
        private final String description;
        
        LearningStatus(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 笔记类型枚举
     */
    public enum NoteType {
        GENERAL("一般笔记", "普通的学习笔记"),
        RESEARCH("研究笔记", "深入研究的学术笔记"),
        INSPIRATION("灵感笔记", "观看文物时的灵感记录"),
        QUESTION("疑问笔记", "学习过程中的疑问记录"),
        SUMMARY("总结笔记", "学习总结和心得体会");
        
        private final String name;
        private final String description;
        
        NoteType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 创建新的个人笔记
     * @param relicsId 文物ID
     * @param title 笔记标题
     * @param content 笔记内容
     * @param noteType 笔记类型
     * @return 个人笔记值对象
     */
    public static PersonalNote create(Long relicsId, String title, String content, NoteType noteType) {
        if (relicsId == null) {
            throw new IllegalArgumentException("文物ID不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记内容不能为空");
        }
        
        LocalDateTime now = LocalDateTime.now();
        return PersonalNote.builder()
                .noteId(generateNoteId())
                .relicsId(relicsId)
                .title(title.trim())
                .content(content.trim())
                .tags(new ArrayList<>())
                .keyPoints(new ArrayList<>())
                .rating(0)
                .status(LearningStatus.LEARNING)
                .createTime(now)
                .updateTime(now)
                .isFavorite(false)
                .noteType(noteType != null ? noteType : NoteType.GENERAL)
                .build();
    }
    
    /**
     * 从数据库重建个人笔记
     */
    public static PersonalNote fromDatabase(String noteId, Long relicsId, String title, String content,
                                          List<String> tags, List<String> keyPoints, Integer rating,
                                          LearningStatus status, LocalDateTime createTime, LocalDateTime updateTime,
                                          boolean isFavorite, NoteType noteType) {
        return PersonalNote.builder()
                .noteId(noteId)
                .relicsId(relicsId)
                .title(title)
                .content(content)
                .tags(tags != null ? tags : new ArrayList<>())
                .keyPoints(keyPoints != null ? keyPoints : new ArrayList<>())
                .rating(rating != null ? rating : 0)
                .status(status != null ? status : LearningStatus.NOT_STARTED)
                .createTime(createTime != null ? createTime : LocalDateTime.now())
                .updateTime(updateTime != null ? updateTime : LocalDateTime.now())
                .isFavorite(isFavorite)
                .noteType(noteType != null ? noteType : NoteType.GENERAL)
                .build();
    }
    
    /**
     * 更新笔记内容
     * @param newTitle 新标题
     * @param newContent 新内容
     * @return 更新后的笔记
     */
    public PersonalNote updateContent(String newTitle, String newContent) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("笔记内容不能为空");
        }
        
        return PersonalNote.builder()
                .noteId(this.noteId)
                .relicsId(this.relicsId)
                .title(newTitle.trim())
                .content(newContent.trim())
                .tags(this.tags)
                .keyPoints(this.keyPoints)
                .rating(this.rating)
                .status(this.status)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isFavorite(this.isFavorite)
                .noteType(this.noteType)
                .build();
    }
    
    /**
     * 添加标签
     * @param tag 标签
     * @return 更新后的笔记
     */
    public PersonalNote addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return this;
        }
        
        String trimmedTag = tag.trim();
        if (this.tags.contains(trimmedTag)) {
            return this;
        }
        
        List<String> newTags = new ArrayList<>(this.tags);
        newTags.add(trimmedTag);
        
        return PersonalNote.builder()
                .noteId(this.noteId)
                .relicsId(this.relicsId)
                .title(this.title)
                .content(this.content)
                .tags(newTags)
                .keyPoints(this.keyPoints)
                .rating(this.rating)
                .status(this.status)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isFavorite(this.isFavorite)
                .noteType(this.noteType)
                .build();
    }
    
    /**
     * 移除标签
     * @param tag 标签
     * @return 更新后的笔记
     */
    public PersonalNote removeTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return this;
        }
        
        List<String> newTags = new ArrayList<>(this.tags);
        newTags.remove(tag.trim());
        
        return PersonalNote.builder()
                .noteId(this.noteId)
                .relicsId(this.relicsId)
                .title(this.title)
                .content(this.content)
                .tags(newTags)
                .keyPoints(this.keyPoints)
                .rating(this.rating)
                .status(this.status)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isFavorite(this.isFavorite)
                .noteType(this.noteType)
                .build();
    }
    
    /**
     * 更新学习状态
     * @param newStatus 新状态
     * @return 更新后的笔记
     */
    public PersonalNote updateLearningStatus(LearningStatus newStatus) {
        if (newStatus == null) {
            return this;
        }
        
        return PersonalNote.builder()
                .noteId(this.noteId)
                .relicsId(this.relicsId)
                .title(this.title)
                .content(this.content)
                .tags(this.tags)
                .keyPoints(this.keyPoints)
                .rating(this.rating)
                .status(newStatus)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isFavorite(this.isFavorite)
                .noteType(this.noteType)
                .build();
    }
    
    /**
     * 设置个人评分
     * @param newRating 评分（1-5）
     * @return 更新后的笔记
     */
    public PersonalNote setRating(Integer newRating) {
        if (newRating != null && (newRating < 1 || newRating > 5)) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        
        return PersonalNote.builder()
                .noteId(this.noteId)
                .relicsId(this.relicsId)
                .title(this.title)
                .content(this.content)
                .tags(this.tags)
                .keyPoints(this.keyPoints)
                .rating(newRating != null ? newRating : 0)
                .status(this.status)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isFavorite(this.isFavorite)
                .noteType(this.noteType)
                .build();
    }
    
    /**
     * 切换收藏状态
     * @return 更新后的笔记
     */
    public PersonalNote toggleFavorite() {
        return PersonalNote.builder()
                .noteId(this.noteId)
                .relicsId(this.relicsId)
                .title(this.title)
                .content(this.content)
                .tags(this.tags)
                .keyPoints(this.keyPoints)
                .rating(this.rating)
                .status(this.status)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isFavorite(!this.isFavorite)
                .noteType(this.noteType)
                .build();
    }
    
    /**
     * 生成笔记ID
     * @return 笔记ID
     */
    private static String generateNoteId() {
        return "note_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 获取笔记摘要（前100个字符）
     * @return 笔记摘要
     */
    public String getSummary() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
    
    /**
     * 检查是否包含指定标签
     * @param tag 标签
     * @return 是否包含
     */
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.trim());
    }
    
    /**
     * 获取学习进度百分比（基于状态）
     * @return 进度百分比
     */
    public int getLearningProgress() {
        switch (status) {
            case NOT_STARTED: return 0;
            case LEARNING: return 25;
            case REVIEWED: return 50;
            case MASTERED: return 80;
            case EXPERT: return 100;
            default: return 0;
        }
    }
    
    @Override
    public String toString() {
        return String.format("PersonalNote{noteId='%s', relicsId=%d, title='%s', status=%s, rating=%d}", 
                noteId, relicsId, title, status.getName(), rating);
    }
}
