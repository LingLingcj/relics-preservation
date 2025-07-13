package com.ling.domain.interaction.model.valobj;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 收藏馆学习统计值对象
 * @Author: LingRJ
 * @Description: 收藏馆的学习统计信息
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class GalleryLearningStats {
    
    /** 总学习时长（分钟） */
    private final Long totalLearningMinutes;
    
    /** 学习文物数量 */
    private final Integer studiedRelicsCount;
    
    /** 笔记数量 */
    private final Integer noteCount;
    
    /** 平均学习评分 */
    private final Double averageRating;
    
    /** 知识点掌握数量 */
    private final Integer masteredKnowledgePoints;
    
    /** 学习会话数量 */
    private final Integer learningSessionCount;
    
    /** 最后学习时间 */
    private final LocalDateTime lastLearningTime;
    
    /** 学习完成度（百分比） */
    private final Double completionPercentage;
    
    /**
     * 创建空的学习统计
     * @return 学习统计对象
     */
    public static GalleryLearningStats empty() {
        return GalleryLearningStats.builder()
                .totalLearningMinutes(0L)
                .studiedRelicsCount(0)
                .noteCount(0)
                .averageRating(0.0)
                .masteredKnowledgePoints(0)
                .learningSessionCount(0)
                .lastLearningTime(null)
                .completionPercentage(0.0)
                .build();
    }
    
    /**
     * 更新学习统计
     * @param learningMinutes 新增学习时长
     * @param newNoteCount 新笔记数量
     * @param newRating 新评分
     * @return 更新后的统计
     */
    public GalleryLearningStats updateStats(Long learningMinutes, Integer newNoteCount, Double newRating) {
        return GalleryLearningStats.builder()
                .totalLearningMinutes(this.totalLearningMinutes + (learningMinutes != null ? learningMinutes : 0))
                .studiedRelicsCount(this.studiedRelicsCount)
                .noteCount(this.noteCount + (newNoteCount != null ? newNoteCount : 0))
                .averageRating(calculateNewAverageRating(newRating))
                .masteredKnowledgePoints(this.masteredKnowledgePoints)
                .learningSessionCount(this.learningSessionCount + 1)
                .lastLearningTime(LocalDateTime.now())
                .completionPercentage(this.completionPercentage)
                .build();
    }
    
    /**
     * 计算新的平均评分
     * @param newRating 新评分
     * @return 新的平均评分
     */
    private Double calculateNewAverageRating(Double newRating) {
        if (newRating == null) {
            return this.averageRating;
        }
        
        if (this.learningSessionCount == 0) {
            return newRating;
        }
        
        return (this.averageRating * this.learningSessionCount + newRating) / (this.learningSessionCount + 1);
    }
}
