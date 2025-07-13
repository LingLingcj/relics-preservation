package com.ling.domain.interaction.model.valobj;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Achievement值对象单元测试
 * @Author: LingRJ
 * @Description: 测试成就值对象的所有功能
 * @DateTime: 2025/7/13
 */
@DisplayName("成就值对象测试")
class AchievementTest {
    
    private String testName;
    private String testDescription;
    private Achievement.AchievementType testType;
    private Achievement.AchievementLevel testLevel;
    private Achievement.AchievementCondition testCondition;
    
    @BeforeEach
    void setUp() {
        testName = "初学者";
        testDescription = "创建第一条学习笔记";
        testType = Achievement.AchievementType.LEARNING;
        testLevel = Achievement.AchievementLevel.BRONZE;
        testCondition = Achievement.AchievementCondition.builder()
                .conditionType(Achievement.AchievementCondition.ConditionType.NOTE_COUNT)
                .targetCount(1)
                .conditionParam("notes")
                .timeLimitDays(null)
                .build();
    }
    
    @Test
    @DisplayName("创建成就 - 成功")
    void testCreateAchievementSuccess() {
        // When
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // Then
        assertNotNull(achievement);
        assertNotNull(achievement.getAchievementId());
        assertEquals(testName, achievement.getName());
        assertEquals(testDescription, achievement.getDescription());
        assertEquals(testType, achievement.getType());
        assertEquals(testLevel, achievement.getLevel());
        assertEquals(testCondition, achievement.getCondition());
        assertEquals(0, achievement.getCurrentProgress());
        assertEquals(testCondition.getTargetCount(), achievement.getTargetProgress());
        assertFalse(achievement.isUnlocked());
        assertNull(achievement.getUnlockedTime());
        assertEquals(testLevel.getBasePoints(), achievement.getRewardPoints());
        assertNotNull(achievement.getRewardBadges());
        assertTrue(achievement.getRewardBadges().isEmpty());
        assertNotNull(achievement.getCreateTime());
        assertNotNull(achievement.getIcon());
    }
    
    @Test
    @DisplayName("创建成就 - 参数验证")
    void testCreateAchievementValidation() {
        // 名称为null
        assertThrows(IllegalArgumentException.class, () -> 
            Achievement.create(null, testDescription, testType, testLevel, testCondition));
        
        // 名称为空字符串
        assertThrows(IllegalArgumentException.class, () -> 
            Achievement.create("", testDescription, testType, testLevel, testCondition));
        
        // 名称为空白字符串
        assertThrows(IllegalArgumentException.class, () -> 
            Achievement.create("   ", testDescription, testType, testLevel, testCondition));
        
        // 类型为null
        assertThrows(IllegalArgumentException.class, () -> 
            Achievement.create(testName, testDescription, null, testLevel, testCondition));
        
        // 等级为null
        assertThrows(IllegalArgumentException.class, () -> 
            Achievement.create(testName, testDescription, testType, null, testCondition));
        
        // 条件为null
        assertThrows(IllegalArgumentException.class, () -> 
            Achievement.create(testName, testDescription, testType, testLevel, null));
    }
    
    @Test
    @DisplayName("更新进度 - 未达到目标")
    void testUpdateProgressNotReached() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When
        Achievement updatedAchievement = achievement.updateProgress(0);
        
        // Then
        assertEquals(0, updatedAchievement.getCurrentProgress());
        assertFalse(updatedAchievement.isUnlocked());
        assertNull(updatedAchievement.getUnlockedTime());
    }
    
    @Test
    @DisplayName("更新进度 - 达到目标自动解锁")
    void testUpdateProgressReachedTarget() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When
        Achievement updatedAchievement = achievement.updateProgress(1); // 达到目标
        
        // Then
        assertEquals(1, updatedAchievement.getCurrentProgress());
        assertTrue(updatedAchievement.isUnlocked());
        assertNotNull(updatedAchievement.getUnlockedTime());
    }
    
    @Test
    @DisplayName("更新进度 - 超过目标")
    void testUpdateProgressExceedTarget() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When
        Achievement updatedAchievement = achievement.updateProgress(5); // 超过目标
        
        // Then
        assertEquals(5, updatedAchievement.getCurrentProgress());
        assertTrue(updatedAchievement.isUnlocked());
        assertNotNull(updatedAchievement.getUnlockedTime());
    }
    
    @Test
    @DisplayName("更新进度 - 无效进度值")
    void testUpdateProgressInvalidValues() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When & Then - null值
        Achievement nullResult = achievement.updateProgress(null);
        assertEquals(achievement, nullResult); // 应该返回原对象
        
        // When & Then - 负值
        Achievement negativeResult = achievement.updateProgress(-1);
        assertEquals(achievement, negativeResult); // 应该返回原对象
    }
    
    @Test
    @DisplayName("手动解锁成就")
    void testUnlock() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        assertFalse(achievement.isUnlocked());
        
        // When
        Achievement unlockedAchievement = achievement.unlock();
        
        // Then
        assertTrue(unlockedAchievement.isUnlocked());
        assertNotNull(unlockedAchievement.getUnlockedTime());
        assertEquals(unlockedAchievement.getTargetProgress(), unlockedAchievement.getCurrentProgress());
    }
    
    @Test
    @DisplayName("重复解锁成就")
    void testUnlockAlreadyUnlocked() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        Achievement unlockedAchievement = achievement.unlock();
        LocalDateTime firstUnlockTime = unlockedAchievement.getUnlockedTime();
        
        // When
        Achievement reUnlockedAchievement = unlockedAchievement.unlock();
        
        // Then
        assertEquals(unlockedAchievement, reUnlockedAchievement); // 应该返回相同对象
        assertEquals(firstUnlockTime, reUnlockedAchievement.getUnlockedTime());
    }
    
    @Test
    @DisplayName("添加奖励徽章")
    void testAddRewardBadge() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        String badge = "学习达人";
        
        // When
        Achievement achievementWithBadge = achievement.addRewardBadge(badge);
        
        // Then
        assertTrue(achievementWithBadge.getRewardBadges().contains(badge));
        assertEquals(1, achievementWithBadge.getRewardBadges().size());
    }
    
    @Test
    @DisplayName("添加重复徽章")
    void testAddDuplicateBadge() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        String badge = "学习达人";
        
        // When
        Achievement achievementWithBadge = achievement.addRewardBadge(badge);
        Achievement achievementWithDuplicateBadge = achievementWithBadge.addRewardBadge(badge);
        
        // Then
        assertEquals(1, achievementWithDuplicateBadge.getRewardBadges().size());
    }
    
    @Test
    @DisplayName("添加无效徽章")
    void testAddInvalidBadge() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When & Then - null值
        Achievement nullResult = achievement.addRewardBadge(null);
        assertEquals(achievement, nullResult);
        
        // When & Then - 空字符串
        Achievement emptyResult = achievement.addRewardBadge("");
        assertEquals(achievement, emptyResult);
        
        // When & Then - 空白字符串
        Achievement blankResult = achievement.addRewardBadge("   ");
        assertEquals(achievement, blankResult);
    }
    
    @Test
    @DisplayName("获取完成百分比")
    void testGetCompletionPercentage() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, 
            Achievement.AchievementCondition.builder()
                .conditionType(Achievement.AchievementCondition.ConditionType.NOTE_COUNT)
                .targetCount(10)
                .build());
        
        // When & Then - 0%
        assertEquals(0.0, achievement.getCompletionPercentage(), 0.01);
        
        // When & Then - 50%
        Achievement halfProgress = achievement.updateProgress(5);
        assertEquals(50.0, halfProgress.getCompletionPercentage(), 0.01);
        
        // When & Then - 100%
        Achievement fullProgress = achievement.updateProgress(10);
        assertEquals(100.0, fullProgress.getCompletionPercentage(), 0.01);
        
        // When & Then - 超过100%
        Achievement overProgress = achievement.updateProgress(15);
        assertEquals(100.0, overProgress.getCompletionPercentage(), 0.01); // 应该限制在100%
    }
    
    @Test
    @DisplayName("检查是否接近完成")
    void testIsNearCompletion() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, 
            Achievement.AchievementCondition.builder()
                .conditionType(Achievement.AchievementCondition.ConditionType.NOTE_COUNT)
                .targetCount(10)
                .build());
        
        // When & Then - 不接近完成
        assertFalse(achievement.updateProgress(5).isNearCompletion()); // 50%
        
        // When & Then - 接近完成
        assertTrue(achievement.updateProgress(8).isNearCompletion()); // 80%
        assertTrue(achievement.updateProgress(9).isNearCompletion()); // 90%
        assertTrue(achievement.updateProgress(10).isNearCompletion()); // 100%
    }
    
    @Test
    @DisplayName("获取剩余进度")
    void testGetRemainingProgress() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, 
            Achievement.AchievementCondition.builder()
                .conditionType(Achievement.AchievementCondition.ConditionType.NOTE_COUNT)
                .targetCount(10)
                .build());
        
        // When & Then
        assertEquals(10, achievement.getRemainingProgress());
        assertEquals(5, achievement.updateProgress(5).getRemainingProgress());
        assertEquals(0, achievement.updateProgress(10).getRemainingProgress());
        assertEquals(0, achievement.updateProgress(15).getRemainingProgress()); // 不能为负数
    }
    
    @Test
    @DisplayName("获取显示名称")
    void testGetDisplayName() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When
        String displayName = achievement.getDisplayName();
        
        // Then
        assertEquals("[青铜] 初学者", displayName);
    }
    
    @Test
    @DisplayName("获取进度描述")
    void testGetProgressDescription() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When & Then - 未完成
        String progressDesc = achievement.updateProgress(0).getProgressDescription();
        assertEquals("0/1 (0.0%)", progressDesc);
        
        // When & Then - 已完成
        String completedDesc = achievement.updateProgress(1).getProgressDescription();
        assertEquals("已完成", completedDesc);
    }
    
    @ParameterizedTest
    @EnumSource(Achievement.AchievementType.class)
    @DisplayName("成就类型枚举测试")
    void testAchievementTypeEnum(Achievement.AchievementType type) {
        // 验证所有成就类型都有名称和描述
        assertNotNull(type.getName());
        assertNotNull(type.getDescription());
        assertFalse(type.getName().isEmpty());
        assertFalse(type.getDescription().isEmpty());
    }
    
    @ParameterizedTest
    @EnumSource(Achievement.AchievementLevel.class)
    @DisplayName("成就等级枚举测试")
    void testAchievementLevelEnum(Achievement.AchievementLevel level) {
        // 验证所有成就等级都有完整信息
        assertNotNull(level.getName());
        assertNotNull(level.getDescription());
        assertNotNull(level.getBasePoints());
        assertNotNull(level.getColor());
        assertFalse(level.getName().isEmpty());
        assertFalse(level.getDescription().isEmpty());
        assertTrue(level.getBasePoints() > 0);
        assertFalse(level.getColor().isEmpty());
    }
    
    @ParameterizedTest
    @EnumSource(Achievement.AchievementCondition.ConditionType.class)
    @DisplayName("成就条件类型枚举测试")
    void testConditionTypeEnum(Achievement.AchievementCondition.ConditionType conditionType) {
        // 验证所有条件类型都有名称和描述
        assertNotNull(conditionType.getName());
        assertNotNull(conditionType.getDescription());
        assertFalse(conditionType.getName().isEmpty());
        assertFalse(conditionType.getDescription().isEmpty());
    }
    
    @Test
    @DisplayName("toString方法测试")
    void testToString() {
        // Given
        Achievement achievement = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        
        // When
        String toString = achievement.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains(achievement.getAchievementId()));
        assertTrue(toString.contains(testName));
        assertTrue(toString.contains(testType.getName()));
        assertTrue(toString.contains(testLevel.getName()));
    }
    
    @Test
    @DisplayName("equals和hashCode测试")
    void testEqualsAndHashCode() {
        // Given
        Achievement achievement1 = Achievement.create(testName, testDescription, testType, testLevel, testCondition);
        Achievement achievement2 = Achievement.create("不同名称", testDescription, testType, testLevel, testCondition);
        
        // When & Then - 不同的成就应该不相等（基于achievementId）
        assertNotEquals(achievement1, achievement2);
        assertNotEquals(achievement1.hashCode(), achievement2.hashCode());
        
        // 相同的成就应该相等
        assertEquals(achievement1, achievement1);
        assertEquals(achievement1.hashCode(), achievement1.hashCode());
    }
}
