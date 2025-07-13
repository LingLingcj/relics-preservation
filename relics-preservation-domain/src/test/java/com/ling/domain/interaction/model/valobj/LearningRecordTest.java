package com.ling.domain.interaction.model.valobj;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LearningRecord值对象单元测试
 * @Author: LingRJ
 * @Description: 测试学习记录值对象的所有功能
 * @DateTime: 2025/7/13
 */
@DisplayName("学习记录值对象测试")
class LearningRecordTest {
    
    private Long testRelicsId;
    private LearningRecord.LearningType testLearningType;
    
    @BeforeEach
    void setUp() {
        testRelicsId = 1001L;
        testLearningType = LearningRecord.LearningType.DETAILED_STUDY;
    }
    
    @Test
    @DisplayName("创建学习记录 - 成功")
    void testCreateLearningRecordSuccess() {
        // When
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // Then
        assertNotNull(record);
        assertNotNull(record.getRecordId());
        assertNotNull(record.getSessionId());
        assertEquals(testRelicsId, record.getRelicsId());
        assertEquals(testLearningType, record.getLearningType());
        assertNotNull(record.getStartTime());
        assertNull(record.getEndTime());
        assertEquals(0L, record.getDurationSeconds());
        assertNotNull(record.getActivities());
        assertTrue(record.getActivities().isEmpty());
        assertNotNull(record.getKnowledgePoints());
        assertTrue(record.getKnowledgePoints().isEmpty());
        assertNull(record.getEffectiveness());
        assertEquals(0, record.getNoteCount());
        assertFalse(record.isGoalAchieved());
        assertEquals(0, record.getLearningRating());
        assertNotNull(record.getCreateTime());
    }
    
    @Test
    @DisplayName("创建学习记录 - 参数验证")
    void testCreateLearningRecordValidation() {
        // 文物ID为null
        assertThrows(IllegalArgumentException.class, () -> 
            LearningRecord.create(null, testLearningType));
        
        // 学习类型为null
        assertThrows(IllegalArgumentException.class, () -> 
            LearningRecord.create(testRelicsId, null));
    }
    
    @Test
    @DisplayName("结束学习记录")
    void testEndLearning() throws InterruptedException {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        Thread.sleep(10); // 确保有时间差
        
        // When
        LearningRecord endedRecord = record.endLearning();
        
        // Then
        assertNotNull(endedRecord.getEndTime());
        assertTrue(endedRecord.getDurationSeconds() > 0);
        assertTrue(endedRecord.getEndTime().isAfter(endedRecord.getStartTime()));
        assertEquals(record.getRecordId(), endedRecord.getRecordId());
    }
    
    @Test
    @DisplayName("添加学习活动")
    void testAddActivity() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        LearningRecord.LearningActivity.ActivityType activityType = 
            LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS;
        String description = "查看文物详细信息";
        
        // When
        LearningRecord updatedRecord = record.addActivity(activityType, description);
        
        // Then
        assertEquals(1, updatedRecord.getActivities().size());
        LearningRecord.LearningActivity activity = updatedRecord.getActivities().get(0);
        assertEquals(activityType, activity.getActivityType());
        assertEquals(description, activity.getDescription());
        assertNotNull(activity.getActivityTime());
        assertEquals(0L, activity.getDurationSeconds());
    }
    
    @Test
    @DisplayName("更新知识点掌握情况 - 新增知识点")
    void testUpdateKnowledgePointNew() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        LearningRecord.KnowledgePoint knowledgePoint = LearningRecord.KnowledgePoint.builder()
                .pointName("青铜器制作工艺")
                .pointType("技术知识")
                .masteryLevel(75)
                .studyCount(1)
                .lastStudyTime(LocalDateTime.now())
                .build();
        
        // When
        LearningRecord updatedRecord = record.updateKnowledgePoint(knowledgePoint);
        
        // Then
        assertEquals(1, updatedRecord.getKnowledgePoints().size());
        assertEquals(knowledgePoint, updatedRecord.getKnowledgePoints().get(0));
    }
    
    @Test
    @DisplayName("更新知识点掌握情况 - 更新已有知识点")
    void testUpdateKnowledgePointExisting() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        LearningRecord.KnowledgePoint originalPoint = LearningRecord.KnowledgePoint.builder()
                .pointName("青铜器制作工艺")
                .pointType("技术知识")
                .masteryLevel(50)
                .studyCount(1)
                .lastStudyTime(LocalDateTime.now().minusHours(1))
                .build();
        
        LearningRecord.KnowledgePoint updatedPoint = LearningRecord.KnowledgePoint.builder()
                .pointName("青铜器制作工艺") // 相同名称
                .pointType("技术知识")
                .masteryLevel(75) // 提高掌握度
                .studyCount(2)
                .lastStudyTime(LocalDateTime.now())
                .build();
        
        // When
        LearningRecord recordWithPoint = record.updateKnowledgePoint(originalPoint);
        LearningRecord recordWithUpdatedPoint = recordWithPoint.updateKnowledgePoint(updatedPoint);
        
        // Then
        assertEquals(1, recordWithUpdatedPoint.getKnowledgePoints().size());
        LearningRecord.KnowledgePoint finalPoint = recordWithUpdatedPoint.getKnowledgePoints().get(0);
        assertEquals(75, finalPoint.getMasteryLevel());
        assertEquals(2, finalPoint.getStudyCount());
    }
    
    @Test
    @DisplayName("设置学习效果评估")
    void testSetEffectiveness() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        LearningRecord.LearningEffectiveness effectiveness = LearningRecord.LearningEffectiveness.builder()
                .focusScore(4)
                .comprehensionScore(5)
                .retentionScore(3)
                .interestScore(5)
                .overallScore(4)
                .notes("学习效果很好")
                .build();
        
        // When
        LearningRecord updatedRecord = record.setEffectiveness(effectiveness);
        
        // Then
        assertEquals(effectiveness, updatedRecord.getEffectiveness());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("设置有效学习评分")
    void testSetValidLearningRating(int rating) {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When
        LearningRecord ratedRecord = record.setLearningRating(rating);
        
        // Then
        assertEquals(rating, ratedRecord.getLearningRating());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    @DisplayName("设置无效学习评分")
    void testSetInvalidLearningRating(int rating) {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> record.setLearningRating(rating));
    }
    
    @Test
    @DisplayName("标记目标达成")
    void testMarkGoalAchieved() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        assertFalse(record.isGoalAchieved());
        
        // When
        LearningRecord achievedRecord = record.markGoalAchieved();
        
        // Then
        assertTrue(achievedRecord.isGoalAchieved());
    }
    
    @Test
    @DisplayName("获取学习时长（分钟）")
    void testGetDurationMinutes() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When - 模拟结束学习（设置时长为3600秒 = 60分钟）
        LearningRecord endedRecord = LearningRecord.builder()
                .recordId(record.getRecordId())
                .relicsId(record.getRelicsId())
                .sessionId(record.getSessionId())
                .startTime(record.getStartTime())
                .endTime(LocalDateTime.now())
                .durationSeconds(3600L)
                .learningType(record.getLearningType())
                .activities(record.getActivities())
                .knowledgePoints(record.getKnowledgePoints())
                .effectiveness(record.getEffectiveness())
                .noteCount(record.getNoteCount())
                .isGoalAchieved(record.isGoalAchieved())
                .learningRating(record.getLearningRating())
                .createTime(record.getCreateTime())
                .build();
        
        // Then
        assertEquals(60, endedRecord.getDurationMinutes());
    }
    
    @Test
    @DisplayName("获取活动数量")
    void testGetActivityCount() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When
        LearningRecord recordWithActivities = record
                .addActivity(LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS, "查看详情")
                .addActivity(LearningRecord.LearningActivity.ActivityType.TAKE_NOTES, "记录笔记")
                .addActivity(LearningRecord.LearningActivity.ActivityType.ADD_FAVORITE, "添加收藏");
        
        // Then
        assertEquals(3, recordWithActivities.getActivityCount());
    }
    
    @Test
    @DisplayName("获取知识点掌握平均分")
    void testGetAverageMasteryLevel() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When - 无知识点
        assertEquals(0.0, record.getAverageMasteryLevel());
        
        // When - 添加知识点
        LearningRecord recordWithPoints = record
                .updateKnowledgePoint(LearningRecord.KnowledgePoint.builder()
                        .pointName("知识点1").masteryLevel(80).build())
                .updateKnowledgePoint(LearningRecord.KnowledgePoint.builder()
                        .pointName("知识点2").masteryLevel(90).build())
                .updateKnowledgePoint(LearningRecord.KnowledgePoint.builder()
                        .pointName("知识点3").masteryLevel(70).build());
        
        // Then
        assertEquals(80.0, recordWithPoints.getAverageMasteryLevel(), 0.01);
    }
    
    @Test
    @DisplayName("检查是否为有效学习记录")
    void testIsValidLearning() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When - 学习时长不足
        LearningRecord shortRecord = LearningRecord.builder()
                .recordId(record.getRecordId())
                .relicsId(record.getRelicsId())
                .sessionId(record.getSessionId())
                .startTime(record.getStartTime())
                .endTime(LocalDateTime.now())
                .durationSeconds(20L) // 少于30秒
                .learningType(record.getLearningType())
                .activities(record.getActivities())
                .knowledgePoints(record.getKnowledgePoints())
                .effectiveness(record.getEffectiveness())
                .noteCount(record.getNoteCount())
                .isGoalAchieved(record.isGoalAchieved())
                .learningRating(record.getLearningRating())
                .createTime(record.getCreateTime())
                .build();
        
        // Then
        assertFalse(shortRecord.isValidLearning());
        
        // When - 学习时长充足
        LearningRecord validRecord = LearningRecord.builder()
                .recordId(record.getRecordId())
                .relicsId(record.getRelicsId())
                .sessionId(record.getSessionId())
                .startTime(record.getStartTime())
                .endTime(LocalDateTime.now())
                .durationSeconds(60L) // 超过30秒
                .learningType(record.getLearningType())
                .activities(record.getActivities())
                .knowledgePoints(record.getKnowledgePoints())
                .effectiveness(record.getEffectiveness())
                .noteCount(record.getNoteCount())
                .isGoalAchieved(record.isGoalAchieved())
                .learningRating(record.getLearningRating())
                .createTime(record.getCreateTime())
                .build();
        
        // Then
        assertTrue(validRecord.isValidLearning());
    }
    
    @Test
    @DisplayName("学习类型枚举测试")
    void testLearningTypeEnum() {
        // 验证所有学习类型都有名称和描述
        for (LearningRecord.LearningType type : LearningRecord.LearningType.values()) {
            assertNotNull(type.getName());
            assertNotNull(type.getDescription());
            assertFalse(type.getName().isEmpty());
            assertFalse(type.getDescription().isEmpty());
        }
    }
    
    @Test
    @DisplayName("活动类型枚举测试")
    void testActivityTypeEnum() {
        // 验证所有活动类型都有名称
        for (LearningRecord.LearningActivity.ActivityType type : 
             LearningRecord.LearningActivity.ActivityType.values()) {
            assertNotNull(type.getName());
            assertFalse(type.getName().isEmpty());
        }
    }
    
    @Test
    @DisplayName("toString方法测试")
    void testToString() {
        // Given
        LearningRecord record = LearningRecord.create(testRelicsId, testLearningType);
        
        // When
        String toString = record.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains(record.getRecordId()));
        assertTrue(toString.contains(testRelicsId.toString()));
        assertTrue(toString.contains(testLearningType.getName()));
    }
    
    @Test
    @DisplayName("equals和hashCode测试")
    void testEqualsAndHashCode() {
        // Given
        LearningRecord record1 = LearningRecord.create(testRelicsId, testLearningType);
        LearningRecord record2 = LearningRecord.create(testRelicsId, LearningRecord.LearningType.BROWSE);
        
        // When & Then - 不同的记录应该不相等（基于recordId）
        assertNotEquals(record1, record2);
        assertNotEquals(record1.hashCode(), record2.hashCode());
        
        // 相同的记录应该相等
        assertEquals(record1, record1);
        assertEquals(record1.hashCode(), record1.hashCode());
    }
}
