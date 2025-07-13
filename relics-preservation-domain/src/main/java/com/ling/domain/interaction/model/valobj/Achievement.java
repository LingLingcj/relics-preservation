package com.ling.domain.interaction.model.valobj;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 成就值对象
 * @Author: LingRJ
 * @Description: 用户学习和收藏成就系统
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@EqualsAndHashCode(of = {"achievementId"})
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Slf4j
public class Achievement {
    
    /** 成就ID */
    private final String achievementId;
    
    /** 成就名称 */
    private final String name;
    
    /** 成就描述 */
    private final String description;
    
    /** 成就类型 */
    private final AchievementType type;
    
    /** 成就等级 */
    private final AchievementLevel level;
    
    /** 成就图标 */
    private final String icon;
    
    /** 获得条件 */
    private final AchievementCondition condition;
    
    /** 当前进度 */
    private final Integer currentProgress;
    
    /** 目标进度 */
    private final Integer targetProgress;
    
    /** 是否已解锁 */
    private final boolean isUnlocked;
    
    /** 解锁时间 */
    private final LocalDateTime unlockedTime;
    
    /** 奖励积分 */
    private final Integer rewardPoints;
    
    /** 奖励徽章 */
    private final List<String> rewardBadges;
    
    /** 创建时间 */
    private final LocalDateTime createTime;
    
    /**
     * 成就类型枚举
     */
    public enum AchievementType {
        COLLECTION("收藏成就", "与文物收藏相关的成就"),
        LEARNING("学习成就", "与学习进度相关的成就"),
        SOCIAL("社交成就", "与社交互动相关的成就"),
        EXPLORATION("探索成就", "与文物探索相关的成就"),
        EXPERTISE("专业成就", "与专业知识相关的成就"),
        TIME("时间成就", "与使用时长相关的成就"),
        SPECIAL("特殊成就", "特殊活动或节日成就");
        
        private final String name;
        private final String description;
        
        AchievementType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 成就等级枚举
     */
    public enum AchievementLevel {
        BRONZE("青铜", "初级成就", 10, "#CD7F32"),
        SILVER("白银", "中级成就", 25, "#C0C0C0"),
        GOLD("黄金", "高级成就", 50, "#FFD700"),
        PLATINUM("铂金", "专家成就", 100, "#E5E4E2"),
        DIAMOND("钻石", "大师成就", 200, "#B9F2FF"),
        LEGENDARY("传奇", "传奇成就", 500, "#FF6347");
        
        private final String name;
        private final String description;
        private final Integer basePoints;
        private final String color;
        
        AchievementLevel(String name, String description, Integer basePoints, String color) {
            this.name = name;
            this.description = description;
            this.basePoints = basePoints;
            this.color = color;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Integer getBasePoints() { return basePoints; }
        public String getColor() { return color; }
    }
    
    /**
     * 成就条件
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class AchievementCondition {
        /** 条件类型 */
        private final ConditionType conditionType;
        /** 目标数量 */
        private final Integer targetCount;
        /** 条件参数 */
        private final String conditionParam;
        /** 时间限制（天） */
        private final Integer timeLimitDays;
        
        public enum ConditionType {
            COLLECT_COUNT("收藏数量", "收藏指定数量的文物"),
            LEARNING_TIME("学习时长", "累计学习时长达到指定小时"),
            NOTE_COUNT("笔记数量", "创建指定数量的学习笔记"),
            CONSECUTIVE_DAYS("连续天数", "连续登录指定天数"),
            CATEGORY_EXPERT("分类专家", "在某个分类下收藏指定数量文物"),
            SHARE_COUNT("分享次数", "分享文物或收藏馆指定次数"),
            RATING_COUNT("评分次数", "为文物评分指定次数"),
            GALLERY_COUNT("收藏馆数量", "创建指定数量的收藏馆"),
            KNOWLEDGE_MASTER("知识掌握", "掌握指定数量的知识点"),
            SOCIAL_INTERACTION("社交互动", "与其他用户互动指定次数");
            
            private final String name;
            private final String description;
            
            ConditionType(String name, String description) {
                this.name = name;
                this.description = description;
            }
            
            public String getName() { return name; }
            public String getDescription() { return description; }
        }
    }
    
    /**
     * 创建新成就
     * @param name 成就名称
     * @param description 成就描述
     * @param type 成就类型
     * @param level 成就等级
     * @param condition 获得条件
     * @return 成就值对象
     */
    public static Achievement create(String name, String description, AchievementType type, 
                                   AchievementLevel level, AchievementCondition condition) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("成就名称不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("成就类型不能为空");
        }
        if (level == null) {
            throw new IllegalArgumentException("成就等级不能为空");
        }
        if (condition == null) {
            throw new IllegalArgumentException("成就条件不能为空");
        }
        
        return Achievement.builder()
                .achievementId(generateAchievementId())
                .name(name.trim())
                .description(description != null ? description.trim() : "")
                .type(type)
                .level(level)
                .icon(generateDefaultIcon(type, level))
                .condition(condition)
                .currentProgress(0)
                .targetProgress(condition.getTargetCount())
                .isUnlocked(false)
                .unlockedTime(null)
                .rewardPoints(calculateRewardPoints(level))
                .rewardBadges(new ArrayList<>())
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 更新进度
     * @param newProgress 新进度
     * @return 更新后的成就
     */
    public Achievement updateProgress(Integer newProgress) {
        if (newProgress == null || newProgress < 0) {
            return this;
        }
        
        boolean shouldUnlock = !isUnlocked && newProgress >= targetProgress;
        
        return Achievement.builder()
                .achievementId(this.achievementId)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .level(this.level)
                .icon(this.icon)
                .condition(this.condition)
                .currentProgress(newProgress)
                .targetProgress(this.targetProgress)
                .isUnlocked(shouldUnlock || this.isUnlocked)
                .unlockedTime(shouldUnlock ? LocalDateTime.now() : this.unlockedTime)
                .rewardPoints(this.rewardPoints)
                .rewardBadges(this.rewardBadges)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 手动解锁成就
     * @return 更新后的成就
     */
    public Achievement unlock() {
        if (isUnlocked) {
            return this;
        }
        
        return Achievement.builder()
                .achievementId(this.achievementId)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .level(this.level)
                .icon(this.icon)
                .condition(this.condition)
                .currentProgress(this.targetProgress)
                .targetProgress(this.targetProgress)
                .isUnlocked(true)
                .unlockedTime(LocalDateTime.now())
                .rewardPoints(this.rewardPoints)
                .rewardBadges(this.rewardBadges)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 添加奖励徽章
     * @param badge 徽章名称
     * @return 更新后的成就
     */
    public Achievement addRewardBadge(String badge) {
        if (badge == null || badge.trim().isEmpty()) {
            return this;
        }
        
        List<String> newBadges = new ArrayList<>(this.rewardBadges);
        if (!newBadges.contains(badge.trim())) {
            newBadges.add(badge.trim());
        }
        
        return Achievement.builder()
                .achievementId(this.achievementId)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .level(this.level)
                .icon(this.icon)
                .condition(this.condition)
                .currentProgress(this.currentProgress)
                .targetProgress(this.targetProgress)
                .isUnlocked(this.isUnlocked)
                .unlockedTime(this.unlockedTime)
                .rewardPoints(this.rewardPoints)
                .rewardBadges(newBadges)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 获取完成百分比
     * @return 完成百分比
     */
    public double getCompletionPercentage() {
        if (targetProgress == null || targetProgress == 0) {
            return 0.0;
        }
        return Math.min(100.0, (currentProgress * 100.0) / targetProgress);
    }
    
    /**
     * 检查是否接近完成
     * @return 是否接近完成（进度超过80%）
     */
    public boolean isNearCompletion() {
        return getCompletionPercentage() >= 80.0;
    }
    
    /**
     * 获取剩余进度
     * @return 剩余进度
     */
    public int getRemainingProgress() {
        return Math.max(0, targetProgress - currentProgress);
    }
    
    /**
     * 生成成就ID
     * @return 成就ID
     */
    private static String generateAchievementId() {
        return "ach_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 生成默认图标
     * @param type 成就类型
     * @param level 成就等级
     * @return 图标名称
     */
    private static String generateDefaultIcon(AchievementType type, AchievementLevel level) {
        String typeIcon = switch (type) {
            case COLLECTION -> "collection";
            case LEARNING -> "book";
            case SOCIAL -> "users";
            case EXPLORATION -> "compass";
            case EXPERTISE -> "graduation-cap";
            case TIME -> "clock";
            case SPECIAL -> "star";
        };
        
        return typeIcon + "_" + level.name().toLowerCase();
    }
    
    /**
     * 计算奖励积分
     * @param level 成就等级
     * @return 奖励积分
     */
    private static Integer calculateRewardPoints(AchievementLevel level) {
        return level.getBasePoints();
    }
    
    /**
     * 获取显示名称（包含等级）
     * @return 显示名称
     */
    public String getDisplayName() {
        return String.format("[%s] %s", level.getName(), name);
    }
    
    /**
     * 获取进度描述
     * @return 进度描述
     */
    public String getProgressDescription() {
        if (isUnlocked) {
            return "已完成";
        }
        return String.format("%d/%d (%.1f%%)", currentProgress, targetProgress, getCompletionPercentage());
    }
    
    @Override
    public String toString() {
        return String.format("Achievement{id='%s', name='%s', type=%s, level=%s, progress=%s, unlocked=%s}", 
                achievementId, name, type.getName(), level.getName(), getProgressDescription(), isUnlocked);
    }
}
