package com.ling.domain.interaction.service;

import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.user.model.valobj.Username;

import java.util.List;
import java.util.Optional;

/**
 * 个人收藏馆服务接口
 * @Author: LingRJ
 * @Description: 个人收藏馆功能增强服务
 * @DateTime: 2025/7/13
 */
public interface IPersonalGalleryService {
    
    // ==================== 个人笔记管理 ====================
    
    /**
     * 为文物添加个人笔记
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @param relicsId 文物ID
     * @param title 笔记标题
     * @param content 笔记内容
     * @param noteType 笔记类型
     * @return 操作结果
     */
    InteractionResult addPersonalNote(String username, String galleryId, Long relicsId, 
                                    String title, String content, PersonalNote.NoteType noteType);
    
    /**
     * 更新个人笔记
     * @param username 用户名
     * @param noteId 笔记ID
     * @param title 新标题
     * @param content 新内容
     * @return 操作结果
     */
    InteractionResult updatePersonalNote(String username, String noteId, String title, String content);
    
    /**
     * 删除个人笔记
     * @param username 用户名
     * @param noteId 笔记ID
     * @return 操作结果
     */
    InteractionResult deletePersonalNote(String username, String noteId);
    
    /**
     * 获取文物的个人笔记
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 个人笔记
     */
    Optional<PersonalNote> getPersonalNote(String username, Long relicsId);
    
    /**
     * 获取用户的所有个人笔记
     * @param username 用户名
     * @return 个人笔记列表
     */
    List<PersonalNote> getAllPersonalNotes(String username);
    
    // ==================== 学习记录管理 ====================
    
    /**
     * 开始学习记录
     * @param username 用户名
     * @param relicsId 文物ID
     * @param learningType 学习类型
     * @return 学习记录ID
     */
    String startLearningRecord(String username, Long relicsId, LearningRecord.LearningType learningType);
    
    /**
     * 结束学习记录
     * @param username 用户名
     * @param recordId 记录ID
     * @param rating 学习评分
     * @return 操作结果
     */
    InteractionResult endLearningRecord(String username, String recordId, Integer rating);
    
    /**
     * 添加学习活动
     * @param username 用户名
     * @param recordId 记录ID
     * @param activityType 活动类型
     * @param description 活动描述
     * @return 操作结果
     */
    InteractionResult addLearningActivity(String username, String recordId, 
                                        LearningRecord.LearningActivity.ActivityType activityType, 
                                        String description);
    
    /**
     * 获取用户学习记录
     * @param username 用户名
     * @param relicsId 文物ID（可选）
     * @return 学习记录列表
     */
    List<LearningRecord> getLearningRecords(String username, Long relicsId);
    
    // ==================== 成就系统管理 ====================
    
    /**
     * 检查并更新用户成就
     * @param username 用户名
     * @return 新解锁的成就列表
     */
    List<Achievement> checkAndUpdateAchievements(String username);
    
    /**
     * 获取用户所有成就
     * @param username 用户名
     * @return 成就列表
     */
    List<Achievement> getUserAchievements(String username);
    
    /**
     * 获取用户已解锁成就
     * @param username 用户名
     * @return 已解锁成就列表
     */
    List<Achievement> getUnlockedAchievements(String username);
    
    /**
     * 手动解锁成就（管理员功能）
     * @param username 用户名
     * @param achievementId 成就ID
     * @return 操作结果
     */
    InteractionResult unlockAchievement(String username, String achievementId);
    
    // ==================== 收藏馆增强功能 ====================
    
    /**
     * 为收藏馆添加个人标签
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @param tag 标签
     * @return 操作结果
     */
    InteractionResult addGalleryTag(String username, String galleryId, String tag);
    
    /**
     * 移除收藏馆标签
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @param tag 标签
     * @return 操作结果
     */
    InteractionResult removeGalleryTag(String username, String galleryId, String tag);
    
    /**
     * 记录收藏馆浏览
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @return 操作结果
     */
    InteractionResult recordGalleryView(String username, String galleryId);
    
    /**
     * 记录收藏馆分享
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @return 操作结果
     */
    InteractionResult recordGalleryShare(String username, String galleryId);
    
    /**
     * 获取收藏馆学习统计
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @return 学习统计
     */
    Optional<GalleryLearningStats> getGalleryLearningStats(String username, String galleryId);
    
    // ==================== 推荐和分析功能 ====================
    
    /**
     * 获取个性化文物推荐
     * @param username 用户名
     * @param limit 推荐数量
     * @return 推荐文物ID列表
     */
    List<Long> getPersonalizedRelicsRecommendations(String username, int limit);
    
    /**
     * 获取学习路径推荐
     * @param username 用户名
     * @param category 文物分类（可选）
     * @return 学习路径推荐
     */
    List<LearningPathRecommendation> getLearningPathRecommendations(String username, String category);
    
    /**
     * 获取用户学习分析报告
     * @param username 用户名
     * @return 学习分析报告
     */
    UserLearningAnalysis getUserLearningAnalysis(String username);
    
    // ==================== 社交功能 ====================
    
    /**
     * 分享收藏馆
     * @param username 用户名
     * @param galleryId 收藏馆ID
     * @param shareType 分享类型
     * @return 分享链接
     */
    String shareGallery(String username, String galleryId, ShareType shareType);
    
    /**
     * 获取热门收藏馆
     * @param limit 数量限制
     * @return 热门收藏馆列表
     */
    List<CollectionGallery> getPopularGalleries(int limit);
    
    /**
     * 搜索公开收藏馆
     * @param keyword 关键词
     * @param theme 主题（可选）
     * @param limit 数量限制
     * @return 搜索结果
     */
    List<CollectionGallery> searchPublicGalleries(String keyword, GalleryTheme theme, int limit);
    
    // ==================== 内部类定义 ====================
    
    /**
     * 学习路径推荐
     */
    class LearningPathRecommendation {
        private final String pathName;
        private final String description;
        private final List<Long> relicsIds;
        private final Integer estimatedMinutes;
        private final String difficulty;
        
        public LearningPathRecommendation(String pathName, String description, List<Long> relicsIds, 
                                        Integer estimatedMinutes, String difficulty) {
            this.pathName = pathName;
            this.description = description;
            this.relicsIds = relicsIds;
            this.estimatedMinutes = estimatedMinutes;
            this.difficulty = difficulty;
        }
        
        // getters
        public String getPathName() { return pathName; }
        public String getDescription() { return description; }
        public List<Long> getRelicsIds() { return relicsIds; }
        public Integer getEstimatedMinutes() { return estimatedMinutes; }
        public String getDifficulty() { return difficulty; }
    }
    
    /**
     * 用户学习分析
     */
    class UserLearningAnalysis {
        private final String username;
        private final Long totalLearningMinutes;
        private final Integer studiedRelicsCount;
        private final Integer noteCount;
        private final Double averageRating;
        private final List<String> strongCategories;
        private final List<String> improvementAreas;
        private final Integer unlockedAchievements;
        
        public UserLearningAnalysis(String username, Long totalLearningMinutes, Integer studiedRelicsCount,
                                  Integer noteCount, Double averageRating, List<String> strongCategories,
                                  List<String> improvementAreas, Integer unlockedAchievements) {
            this.username = username;
            this.totalLearningMinutes = totalLearningMinutes;
            this.studiedRelicsCount = studiedRelicsCount;
            this.noteCount = noteCount;
            this.averageRating = averageRating;
            this.strongCategories = strongCategories;
            this.improvementAreas = improvementAreas;
            this.unlockedAchievements = unlockedAchievements;
        }
        
        // getters
        public String getUsername() { return username; }
        public Long getTotalLearningMinutes() { return totalLearningMinutes; }
        public Integer getStudiedRelicsCount() { return studiedRelicsCount; }
        public Integer getNoteCount() { return noteCount; }
        public Double getAverageRating() { return averageRating; }
        public List<String> getStrongCategories() { return strongCategories; }
        public List<String> getImprovementAreas() { return improvementAreas; }
        public Integer getUnlockedAchievements() { return unlockedAchievements; }
    }
    
    /**
     * 分享类型枚举
     */
    enum ShareType {
        LINK("链接分享"),
        QR_CODE("二维码分享"),
        SOCIAL_MEDIA("社交媒体分享"),
        EMAIL("邮件分享");
        
        private final String name;
        
        ShareType(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
    }
}
