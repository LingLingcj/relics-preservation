package com.ling.domain.interaction.service.impl;

import com.ling.domain.interaction.adapter.IGalleryManagerRepository;
import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.interaction.service.IPersonalGalleryService;
import com.ling.domain.user.model.valobj.Username;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 个人收藏馆服务实现
 * @Author: LingRJ
 * @Description: 个人收藏馆功能增强服务实现
 * @DateTime: 2025/7/13
 */
@Service
@Slf4j
public class PersonalGalleryServiceImpl implements IPersonalGalleryService {
    
    @Autowired
    private IGalleryManagerRepository galleryManagerRepository;
    
    // 内存存储（实际项目中应该使用数据库）
    private final Map<String, List<PersonalNote>> userNotesCache = new HashMap<>();
    private final Map<String, List<LearningRecord>> userLearningRecordsCache = new HashMap<>();
    private final Map<String, List<Achievement>> userAchievementsCache = new HashMap<>();
    
    // ==================== 个人笔记管理 ====================
    
    @Override
    public InteractionResult addPersonalNote(String username, String galleryId, Long relicsId, 
                                           String title, String content, PersonalNote.NoteType noteType) {
        try {
            log.info("用户 {} 为文物 {} 添加个人笔记", username, relicsId);
            
            // 验证参数
            if (username == null || galleryId == null || relicsId == null) {
                return InteractionResult.failure("参数不能为空");
            }
            
            // 创建个人笔记
            PersonalNote note = PersonalNote.create(relicsId, title, content, noteType);
            
            // 获取收藏馆管理器
            Username usernameObj = Username.of(username);
            Optional<GalleryManager> galleryManagerOpt = galleryManagerRepository.findByUsername(usernameObj);
            
            if (galleryManagerOpt.isEmpty()) {
                return InteractionResult.failure("用户收藏馆不存在");
            }
            
            GalleryManager galleryManager = galleryManagerOpt.get();
            
            // 查找指定收藏馆
            Optional<CollectionGallery> galleryOpt = galleryManager.getAllGalleries().stream()
                    .filter(g -> g.getGalleryId().getValue().equals(galleryId))
                    .findFirst();
            
            if (galleryOpt.isEmpty()) {
                return InteractionResult.failure("收藏馆不存在");
            }
            
            CollectionGallery gallery = galleryOpt.get();
            
            // 添加个人笔记到收藏馆
            CollectionGallery updatedGallery = gallery.addPersonalNote(relicsId, note);

            galleryManager.getGalleries().add(updatedGallery);

            // 保存到仓储
            boolean success = galleryManagerRepository.saveIncremental(galleryManager);
            
            if (success) {
                // 缓存个人笔记
                cachePersonalNote(username, note);
                
                // 检查成就
                checkAndUpdateAchievements(username);
                
                log.info("用户 {} 添加个人笔记成功: {}", username, note.getNoteId());
                return InteractionResult.success("添加个人笔记成功", note.getNoteId());
            } else {
                return InteractionResult.failure("保存失败");
            }
            
        } catch (Exception e) {
            log.error("添加个人笔记失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("添加个人笔记失败: " + e.getMessage());
        }
    }
    
    @Override
    public InteractionResult updatePersonalNote(String username, String noteId, String title, String content) {
        try {
            log.info("用户 {} 更新个人笔记 {}", username, noteId);
            
            // 从缓存中查找笔记
            List<PersonalNote> userNotes = userNotesCache.getOrDefault(username, new ArrayList<>());
            Optional<PersonalNote> noteOpt = userNotes.stream()
                    .filter(note -> note.getNoteId().equals(noteId))
                    .findFirst();
            
            if (noteOpt.isEmpty()) {
                return InteractionResult.failure("笔记不存在");
            }
            
            PersonalNote oldNote = noteOpt.get();
            PersonalNote updatedNote = oldNote.updateContent(title, content);
            
            // 更新缓存
            userNotes.removeIf(note -> note.getNoteId().equals(noteId));
            userNotes.add(updatedNote);
            userNotesCache.put(username, userNotes);
            
            log.info("用户 {} 更新个人笔记成功: {}", username, noteId);
            return InteractionResult.success("更新个人笔记成功");
            
        } catch (Exception e) {
            log.error("更新个人笔记失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("更新个人笔记失败: " + e.getMessage());
        }
    }
    
    @Override
    public InteractionResult deletePersonalNote(String username, String noteId) {
        try {
            log.info("用户 {} 删除个人笔记 {}", username, noteId);
            
            // 从缓存中删除笔记
            List<PersonalNote> userNotes = userNotesCache.getOrDefault(username, new ArrayList<>());
            boolean removed = userNotes.removeIf(note -> note.getNoteId().equals(noteId));
            
            if (!removed) {
                return InteractionResult.failure("笔记不存在");
            }
            
            userNotesCache.put(username, userNotes);
            
            log.info("用户 {} 删除个人笔记成功: {}", username, noteId);
            return InteractionResult.success("删除个人笔记成功");
            
        } catch (Exception e) {
            log.error("删除个人笔记失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("删除个人笔记失败: " + e.getMessage());
        }
    }
    
    @Override
    public Optional<PersonalNote> getPersonalNote(String username, Long relicsId) {
        List<PersonalNote> userNotes = userNotesCache.getOrDefault(username, new ArrayList<>());
        return userNotes.stream()
                .filter(note -> note.getRelicsId().equals(relicsId))
                .findFirst();
    }
    
    @Override
    public List<PersonalNote> getAllPersonalNotes(String username) {
        return new ArrayList<>(userNotesCache.getOrDefault(username, new ArrayList<>()));
    }
    
    // ==================== 学习记录管理 ====================
    
    @Override
    public String startLearningRecord(String username, Long relicsId, LearningRecord.LearningType learningType) {
        try {
            log.info("用户 {} 开始学习文物 {}", username, relicsId);
            
            LearningRecord record = LearningRecord.create(relicsId, learningType);
            
            // 缓存学习记录
            List<LearningRecord> userRecords = userLearningRecordsCache.getOrDefault(username, new ArrayList<>());
            userRecords.add(record);
            userLearningRecordsCache.put(username, userRecords);
            
            log.info("用户 {} 开始学习记录: {}", username, record.getRecordId());
            return record.getRecordId();
            
        } catch (Exception e) {
            log.error("开始学习记录失败: {} - {}", username, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public InteractionResult endLearningRecord(String username, String recordId, Integer rating) {
        try {
            log.info("用户 {} 结束学习记录 {}", username, recordId);
            
            List<LearningRecord> userRecords = userLearningRecordsCache.getOrDefault(username, new ArrayList<>());
            Optional<LearningRecord> recordOpt = userRecords.stream()
                    .filter(record -> record.getRecordId().equals(recordId))
                    .findFirst();
            
            if (recordOpt.isEmpty()) {
                return InteractionResult.failure("学习记录不存在");
            }
            
            LearningRecord record = recordOpt.get();
            LearningRecord endedRecord = record.endLearning();
            
            if (rating != null) {
                endedRecord = endedRecord.setLearningRating(rating);
            }
            
            // 更新缓存
            userRecords.removeIf(r -> r.getRecordId().equals(recordId));
            userRecords.add(endedRecord);
            userLearningRecordsCache.put(username, userRecords);
            
            // 检查成就
            checkAndUpdateAchievements(username);
            
            log.info("用户 {} 结束学习记录成功: {} - 时长: {}分钟", 
                    username, recordId, endedRecord.getDurationMinutes());
            return InteractionResult.success("结束学习记录成功");
            
        } catch (Exception e) {
            log.error("结束学习记录失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("结束学习记录失败: " + e.getMessage());
        }
    }
    
    @Override
    public InteractionResult addLearningActivity(String username, String recordId, 
                                               LearningRecord.LearningActivity.ActivityType activityType, 
                                               String description) {
        try {
            List<LearningRecord> userRecords = userLearningRecordsCache.getOrDefault(username, new ArrayList<>());
            Optional<LearningRecord> recordOpt = userRecords.stream()
                    .filter(record -> record.getRecordId().equals(recordId))
                    .findFirst();
            
            if (recordOpt.isEmpty()) {
                return InteractionResult.failure("学习记录不存在");
            }
            
            LearningRecord record = recordOpt.get();
            LearningRecord updatedRecord = record.addActivity(activityType, description);
            
            // 更新缓存
            userRecords.removeIf(r -> r.getRecordId().equals(recordId));
            userRecords.add(updatedRecord);
            userLearningRecordsCache.put(username, userRecords);
            
            return InteractionResult.success("添加学习活动成功");
            
        } catch (Exception e) {
            log.error("添加学习活动失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("添加学习活动失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<LearningRecord> getLearningRecords(String username, Long relicsId) {
        List<LearningRecord> userRecords = userLearningRecordsCache.getOrDefault(username, new ArrayList<>());
        
        if (relicsId != null) {
            return userRecords.stream()
                    .filter(record -> record.getRelicsId().equals(relicsId))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>(userRecords);
    }
    
    // ==================== 成就系统管理 ====================
    
    @Override
    public List<Achievement> checkAndUpdateAchievements(String username) {
        try {
            log.debug("检查用户 {} 的成就进度", username);
            
            List<Achievement> userAchievements = userAchievementsCache.getOrDefault(username, new ArrayList<>());
            List<Achievement> newlyUnlocked = new ArrayList<>();
            
            // 如果用户没有成就，初始化默认成就
            if (userAchievements.isEmpty()) {
                userAchievements = initializeDefaultAchievements();
            }
            
            // 获取用户统计数据
            int noteCount = userNotesCache.getOrDefault(username, new ArrayList<>()).size();
            long totalLearningMinutes = userLearningRecordsCache.getOrDefault(username, new ArrayList<>())
                    .stream()
                    .mapToLong(LearningRecord::getDurationMinutes)
                    .sum();
            
            // 检查各种成就条件
            for (Achievement achievement : userAchievements) {
                if (!achievement.isUnlocked()) {
                    Achievement updatedAchievement = checkAchievementCondition(achievement, noteCount, totalLearningMinutes);
                    if (updatedAchievement.isUnlocked() && !achievement.isUnlocked()) {
                        newlyUnlocked.add(updatedAchievement);
                        log.info("用户 {} 解锁新成就: {}", username, updatedAchievement.getName());
                    }
                }
            }
            
            // 更新缓存
            userAchievementsCache.put(username, userAchievements);
            
            return newlyUnlocked;
            
        } catch (Exception e) {
            log.error("检查成就失败: {} - {}", username, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Achievement> getUserAchievements(String username) {
        return new ArrayList<>(userAchievementsCache.getOrDefault(username, new ArrayList<>()));
    }
    
    @Override
    public List<Achievement> getUnlockedAchievements(String username) {
        return userAchievementsCache.getOrDefault(username, new ArrayList<>())
                .stream()
                .filter(Achievement::isUnlocked)
                .collect(Collectors.toList());
    }
    
    @Override
    public InteractionResult unlockAchievement(String username, String achievementId) {
        try {
            List<Achievement> userAchievements = userAchievementsCache.getOrDefault(username, new ArrayList<>());
            Optional<Achievement> achievementOpt = userAchievements.stream()
                    .filter(a -> a.getAchievementId().equals(achievementId))
                    .findFirst();
            
            if (achievementOpt.isEmpty()) {
                return InteractionResult.failure("成就不存在");
            }
            
            Achievement achievement = achievementOpt.get();
            if (achievement.isUnlocked()) {
                return InteractionResult.failure("成就已解锁");
            }
            
            Achievement unlockedAchievement = achievement.unlock();
            
            // 更新缓存
            userAchievements.removeIf(a -> a.getAchievementId().equals(achievementId));
            userAchievements.add(unlockedAchievement);
            userAchievementsCache.put(username, userAchievements);
            
            log.info("管理员解锁用户 {} 的成就: {}", username, achievement.getName());
            return InteractionResult.success("成就解锁成功");
            
        } catch (Exception e) {
            log.error("解锁成就失败: {} - {}", username, e.getMessage(), e);
            return InteractionResult.failure("解锁成就失败: " + e.getMessage());
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 缓存个人笔记
     */
    private void cachePersonalNote(String username, PersonalNote note) {
        List<PersonalNote> userNotes = userNotesCache.getOrDefault(username, new ArrayList<>());
        userNotes.add(note);
        userNotesCache.put(username, userNotes);
    }
    
    /**
     * 初始化默认成就
     */
    private List<Achievement> initializeDefaultAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        // 笔记相关成就
        achievements.add(Achievement.create(
                "初学者", "创建第一条学习笔记", 
                Achievement.AchievementType.LEARNING, Achievement.AchievementLevel.BRONZE,
                Achievement.AchievementCondition.builder()
                        .conditionType(Achievement.AchievementCondition.ConditionType.NOTE_COUNT)
                        .targetCount(1)
                        .build()
        ));
        
        achievements.add(Achievement.create(
                "勤奋学者", "创建10条学习笔记", 
                Achievement.AchievementType.LEARNING, Achievement.AchievementLevel.SILVER,
                Achievement.AchievementCondition.builder()
                        .conditionType(Achievement.AchievementCondition.ConditionType.NOTE_COUNT)
                        .targetCount(10)
                        .build()
        ));
        
        // 学习时长相关成就
        achievements.add(Achievement.create(
                "时间管理者", "累计学习1小时", 
                Achievement.AchievementType.TIME, Achievement.AchievementLevel.BRONZE,
                Achievement.AchievementCondition.builder()
                        .conditionType(Achievement.AchievementCondition.ConditionType.LEARNING_TIME)
                        .targetCount(60) // 60分钟
                        .build()
        ));
        
        return achievements;
    }
    
    /**
     * 检查成就条件
     */
    private Achievement checkAchievementCondition(Achievement achievement, int noteCount, long totalLearningMinutes) {
        Achievement.AchievementCondition condition = achievement.getCondition();
        int currentProgress = 0;
        
        switch (condition.getConditionType()) {
            case NOTE_COUNT:
                currentProgress = noteCount;
                break;
            case LEARNING_TIME:
                currentProgress = (int) totalLearningMinutes;
                break;
            default:
                return achievement;
        }
        
        return achievement.updateProgress(currentProgress);
    }
    
    // ==================== 其他功能的简化实现 ====================
    
    @Override
    public InteractionResult addGalleryTag(String username, String galleryId, String tag) {
        // 简化实现
        return InteractionResult.success("添加标签成功");
    }
    
    @Override
    public InteractionResult removeGalleryTag(String username, String galleryId, String tag) {
        // 简化实现
        return InteractionResult.success("移除标签成功");
    }
    
    @Override
    public InteractionResult recordGalleryView(String username, String galleryId) {
        // 简化实现
        return InteractionResult.success("记录浏览成功");
    }
    
    @Override
    public InteractionResult recordGalleryShare(String username, String galleryId) {
        // 简化实现
        return InteractionResult.success("记录分享成功");
    }
    
    @Override
    public Optional<GalleryLearningStats> getGalleryLearningStats(String username, String galleryId) {
        // 简化实现
        return Optional.of(GalleryLearningStats.empty());
    }
    
    @Override
    public List<Long> getPersonalizedRelicsRecommendations(String username, int limit) {
        // 简化实现 - 返回示例推荐
        return Arrays.asList(1L, 2L, 3L, 4L, 5L).subList(0, Math.min(limit, 5));
    }
    
    @Override
    public List<LearningPathRecommendation> getLearningPathRecommendations(String username, String category) {
        // 简化实现
        return Arrays.asList(
                new LearningPathRecommendation("青铜器入门", "了解青铜器的基础知识", 
                        Arrays.asList(1L, 2L, 3L), 30, "初级"),
                new LearningPathRecommendation("瓷器鉴赏", "学习瓷器的鉴赏技巧", 
                        Arrays.asList(4L, 5L, 6L), 45, "中级")
        );
    }
    
    @Override
    public UserLearningAnalysis getUserLearningAnalysis(String username) {
        // 简化实现
        int noteCount = userNotesCache.getOrDefault(username, new ArrayList<>()).size();
        long totalMinutes = userLearningRecordsCache.getOrDefault(username, new ArrayList<>())
                .stream().mapToLong(LearningRecord::getDurationMinutes).sum();
        int unlockedCount = getUnlockedAchievements(username).size();
        
        return new UserLearningAnalysis(username, totalMinutes, noteCount, noteCount, 4.2,
                Arrays.asList("青铜器", "瓷器"), Arrays.asList("书画", "玉器"), unlockedCount);
    }
    
    @Override
    public String shareGallery(String username, String galleryId, ShareType shareType) {
        // 简化实现
        return "https://relics.example.com/gallery/" + galleryId + "?share=" + shareType.name();
    }
    
    @Override
    public List<CollectionGallery> getPopularGalleries(int limit) {
        // 简化实现
        return new ArrayList<>();
    }
    
    @Override
    public List<CollectionGallery> searchPublicGalleries(String keyword, GalleryTheme theme, int limit) {
        // 简化实现
        return new ArrayList<>();
    }
}
