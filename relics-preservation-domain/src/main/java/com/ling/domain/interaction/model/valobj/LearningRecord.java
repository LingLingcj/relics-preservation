package com.ling.domain.interaction.model.valobj;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习记录值对象
 * @Author: LingRJ
 * @Description: 用户学习文物的详细记录和进度跟踪
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@EqualsAndHashCode(of = {"recordId"})
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Slf4j
public class LearningRecord {
    
    /** 记录ID */
    private final String recordId;
    
    /** 文物ID */
    private final Long relicsId;
    
    /** 学习会话ID（同一次学习的多个记录共享） */
    private final String sessionId;
    
    /** 学习开始时间 */
    private final LocalDateTime startTime;
    
    /** 学习结束时间 */
    private final LocalDateTime endTime;
    
    /** 学习时长（秒） */
    private final Long durationSeconds;
    
    /** 学习类型 */
    private final LearningType learningType;
    
    /** 学习活动列表 */
    private final List<LearningActivity> activities;
    
    /** 知识点掌握情况 */
    private final List<KnowledgePoint> knowledgePoints;
    
    /** 学习效果评估 */
    private final LearningEffectiveness effectiveness;
    
    /** 学习笔记数量 */
    private final Integer noteCount;
    
    /** 是否完成学习目标 */
    private final boolean isGoalAchieved;
    
    /** 学习评分（1-5星） */
    private final Integer learningRating;
    
    /** 创建时间 */
    private final LocalDateTime createTime;
    
    /**
     * 学习类型枚举
     */
    public enum LearningType {
        BROWSE("浏览学习", "快速浏览文物信息"),
        DETAILED_STUDY("深入学习", "详细研究文物特征"),
        COMPARATIVE_STUDY("对比学习", "与其他文物对比学习"),
        RESEARCH("专题研究", "针对特定主题的深入研究"),
        REVIEW("复习回顾", "复习已学过的文物知识");
        
        private final String name;
        private final String description;
        
        LearningType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 学习活动记录
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class LearningActivity {
        /** 活动类型 */
        private final ActivityType activityType;
        /** 活动时间 */
        private final LocalDateTime activityTime;
        /** 活动描述 */
        private final String description;
        /** 活动时长（秒） */
        private final Long durationSeconds;
        
        public enum ActivityType {
            VIEW_DETAILS("查看详情"),
            VIEW_IMAGES("查看图片"),
            READ_DESCRIPTION("阅读描述"),
            TAKE_NOTES("记录笔记"),
            ADD_FAVORITE("添加收藏"),
            SEARCH_RELATED("搜索相关"),
            SHARE("分享文物"),
            RATE("评分文物");
            
            private final String name;
            
            ActivityType(String name) {
                this.name = name;
            }
            
            public String getName() { return name; }
        }
    }
    
    /**
     * 知识点掌握情况
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class KnowledgePoint {
        /** 知识点名称 */
        private final String pointName;
        /** 知识点类型 */
        private final String pointType;
        /** 掌握程度（0-100） */
        private final Integer masteryLevel;
        /** 学习次数 */
        private final Integer studyCount;
        /** 最后学习时间 */
        private final LocalDateTime lastStudyTime;
    }
    
    /**
     * 学习效果评估
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class LearningEffectiveness {
        /** 专注度评分（1-5） */
        private final Integer focusScore;
        /** 理解度评分（1-5） */
        private final Integer comprehensionScore;
        /** 记忆度评分（1-5） */
        private final Integer retentionScore;
        /** 兴趣度评分（1-5） */
        private final Integer interestScore;
        /** 整体效果评分（1-5） */
        private final Integer overallScore;
        /** 评估备注 */
        private final String notes;
    }
    
    /**
     * 创建新的学习记录
     * @param relicsId 文物ID
     * @param learningType 学习类型
     * @return 学习记录值对象
     */
    public static LearningRecord create(Long relicsId, LearningType learningType) {
        if (relicsId == null) {
            throw new IllegalArgumentException("文物ID不能为空");
        }
        if (learningType == null) {
            throw new IllegalArgumentException("学习类型不能为空");
        }
        
        LocalDateTime now = LocalDateTime.now();
        String sessionId = generateSessionId();
        
        return LearningRecord.builder()
                .recordId(generateRecordId())
                .relicsId(relicsId)
                .sessionId(sessionId)
                .startTime(now)
                .endTime(null)
                .durationSeconds(0L)
                .learningType(learningType)
                .activities(new ArrayList<>())
                .knowledgePoints(new ArrayList<>())
                .effectiveness(null)
                .noteCount(0)
                .isGoalAchieved(false)
                .learningRating(0)
                .createTime(now)
                .build();
    }
    
    /**
     * 结束学习记录
     * @return 更新后的学习记录
     */
    public LearningRecord endLearning() {
        LocalDateTime now = LocalDateTime.now();
        long duration = Duration.between(startTime, now).getSeconds();
        
        return LearningRecord.builder()
                .recordId(this.recordId)
                .relicsId(this.relicsId)
                .sessionId(this.sessionId)
                .startTime(this.startTime)
                .endTime(now)
                .durationSeconds(duration)
                .learningType(this.learningType)
                .activities(this.activities)
                .knowledgePoints(this.knowledgePoints)
                .effectiveness(this.effectiveness)
                .noteCount(this.noteCount)
                .isGoalAchieved(this.isGoalAchieved)
                .learningRating(this.learningRating)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 添加学习活动
     * @param activityType 活动类型
     * @param description 活动描述
     * @return 更新后的学习记录
     */
    public LearningRecord addActivity(LearningActivity.ActivityType activityType, String description) {
        LearningActivity activity = LearningActivity.builder()
                .activityType(activityType)
                .activityTime(LocalDateTime.now())
                .description(description)
                .durationSeconds(0L)
                .build();
        
        List<LearningActivity> newActivities = new ArrayList<>(this.activities);
        newActivities.add(activity);
        
        return LearningRecord.builder()
                .recordId(this.recordId)
                .relicsId(this.relicsId)
                .sessionId(this.sessionId)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .durationSeconds(this.durationSeconds)
                .learningType(this.learningType)
                .activities(newActivities)
                .knowledgePoints(this.knowledgePoints)
                .effectiveness(this.effectiveness)
                .noteCount(this.noteCount)
                .isGoalAchieved(this.isGoalAchieved)
                .learningRating(this.learningRating)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 更新知识点掌握情况
     * @param knowledgePoint 知识点
     * @return 更新后的学习记录
     */
    public LearningRecord updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        List<KnowledgePoint> newKnowledgePoints = new ArrayList<>(this.knowledgePoints);
        
        // 查找是否已存在相同知识点
        boolean found = false;
        for (int i = 0; i < newKnowledgePoints.size(); i++) {
            if (newKnowledgePoints.get(i).getPointName().equals(knowledgePoint.getPointName())) {
                newKnowledgePoints.set(i, knowledgePoint);
                found = true;
                break;
            }
        }
        
        if (!found) {
            newKnowledgePoints.add(knowledgePoint);
        }
        
        return LearningRecord.builder()
                .recordId(this.recordId)
                .relicsId(this.relicsId)
                .sessionId(this.sessionId)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .durationSeconds(this.durationSeconds)
                .learningType(this.learningType)
                .activities(this.activities)
                .knowledgePoints(newKnowledgePoints)
                .effectiveness(this.effectiveness)
                .noteCount(this.noteCount)
                .isGoalAchieved(this.isGoalAchieved)
                .learningRating(this.learningRating)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 设置学习效果评估
     * @param effectiveness 学习效果评估
     * @return 更新后的学习记录
     */
    public LearningRecord setEffectiveness(LearningEffectiveness effectiveness) {
        return LearningRecord.builder()
                .recordId(this.recordId)
                .relicsId(this.relicsId)
                .sessionId(this.sessionId)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .durationSeconds(this.durationSeconds)
                .learningType(this.learningType)
                .activities(this.activities)
                .knowledgePoints(this.knowledgePoints)
                .effectiveness(effectiveness)
                .noteCount(this.noteCount)
                .isGoalAchieved(this.isGoalAchieved)
                .learningRating(this.learningRating)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 设置学习评分
     * @param rating 评分（1-5）
     * @return 更新后的学习记录
     */
    public LearningRecord setLearningRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("学习评分必须在1-5之间");
        }
        
        return LearningRecord.builder()
                .recordId(this.recordId)
                .relicsId(this.relicsId)
                .sessionId(this.sessionId)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .durationSeconds(this.durationSeconds)
                .learningType(this.learningType)
                .activities(this.activities)
                .knowledgePoints(this.knowledgePoints)
                .effectiveness(this.effectiveness)
                .noteCount(this.noteCount)
                .isGoalAchieved(this.isGoalAchieved)
                .learningRating(rating != null ? rating : 0)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 标记目标达成
     * @return 更新后的学习记录
     */
    public LearningRecord markGoalAchieved() {
        return LearningRecord.builder()
                .recordId(this.recordId)
                .relicsId(this.relicsId)
                .sessionId(this.sessionId)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .durationSeconds(this.durationSeconds)
                .learningType(this.learningType)
                .activities(this.activities)
                .knowledgePoints(this.knowledgePoints)
                .effectiveness(this.effectiveness)
                .noteCount(this.noteCount)
                .isGoalAchieved(true)
                .learningRating(this.learningRating)
                .createTime(this.createTime)
                .build();
    }
    
    /**
     * 获取学习时长（分钟）
     * @return 学习时长
     */
    public long getDurationMinutes() {
        return durationSeconds != null ? durationSeconds / 60 : 0;
    }
    
    /**
     * 获取活动数量
     * @return 活动数量
     */
    public int getActivityCount() {
        return activities != null ? activities.size() : 0;
    }
    
    /**
     * 获取知识点掌握平均分
     * @return 平均掌握度
     */
    public double getAverageMasteryLevel() {
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            return 0.0;
        }
        
        return knowledgePoints.stream()
                .mapToInt(KnowledgePoint::getMasteryLevel)
                .average()
                .orElse(0.0);
    }
    
    /**
     * 检查是否为有效的学习记录
     * @return 是否有效
     */
    public boolean isValidLearning() {
        return durationSeconds != null && durationSeconds > 30; // 至少学习30秒
    }
    
    /**
     * 生成记录ID
     * @return 记录ID
     */
    private static String generateRecordId() {
        return "lr_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 生成会话ID
     * @return 会话ID
     */
    private static String generateSessionId() {
        return "session_" + System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return String.format("LearningRecord{recordId='%s', relicsId=%d, type=%s, duration=%d分钟, activities=%d}", 
                recordId, relicsId, learningType.getName(), getDurationMinutes(), getActivityCount());
    }
}
