package com.ling.trigger.http;

import com.ling.domain.interaction.model.valobj.*;
import com.ling.domain.interaction.service.IPersonalGalleryService;
import com.ling.types.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 个人收藏馆控制器
 * @Author: LingRJ
 * @Description: 个人收藏馆功能增强API接口
 * @DateTime: 2025/7/13
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/personal-gallery")
@Tag(name = "个人收藏馆管理", description = "个人收藏馆功能增强API")
public class PersonalGalleryController {
    
    @Autowired
    private IPersonalGalleryService personalGalleryService;
    
    // ==================== 个人笔记管理 ====================
    
    @PostMapping("/notes")
    @Operation(summary = "添加个人笔记", description = "为收藏馆中的文物添加个人学习笔记")
    public Response<String> addPersonalNote(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "收藏馆ID", required = true) @RequestParam String galleryId,
            @Parameter(description = "文物ID", required = true) @RequestParam Long relicsId,
            @Parameter(description = "笔记标题", required = true) @RequestParam String title,
            @Parameter(description = "笔记内容", required = true) @RequestParam String content,
            @Parameter(description = "笔记类型") @RequestParam(defaultValue = "GENERAL") PersonalNote.NoteType noteType) {
        
        try {
            log.info("用户 {} 为文物 {} 添加个人笔记", username, relicsId);
            
            InteractionResult result = personalGalleryService.addPersonalNote(
                    username, galleryId, relicsId, title, content, noteType);
            
            if (result.isSuccess()) {
                return Response.<String>builder()
                        .code("0000")
                        .info("添加个人笔记成功")
                        .data(result.getData(String.class))
                        .build();
            } else {
                return Response.<String>builder()
                        .code("0001")
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("添加个人笔记失败", e);
            return Response.<String>builder()
                    .code("0001")
                    .info("添加个人笔记失败: " + e.getMessage())
                    .build();
        }
    }
    
    @PutMapping("/notes/{noteId}")
    @Operation(summary = "更新个人笔记", description = "更新已有的个人学习笔记")
    public Response<String> updatePersonalNote(
            @Parameter(description = "笔记ID", required = true) @PathVariable String noteId,
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "新标题", required = true) @RequestParam String title,
            @Parameter(description = "新内容", required = true) @RequestParam String content) {
        
        try {
            log.info("用户 {} 更新个人笔记 {}", username, noteId);
            
            InteractionResult result = personalGalleryService.updatePersonalNote(username, noteId, title, content);
            
            if (result.isSuccess()) {
                return Response.<String>builder()
                        .code("0000")
                        .info("更新个人笔记成功")
                        .build();
            } else {
                return Response.<String>builder()
                        .code("0001")
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("更新个人笔记失败", e);
            return Response.<String>builder()
                    .code("0001")
                    .info("更新个人笔记失败: " + e.getMessage())
                    .build();
        }
    }
    
    @DeleteMapping("/notes/{noteId}")
    @Operation(summary = "删除个人笔记", description = "删除指定的个人学习笔记")
    public Response<String> deletePersonalNote(
            @Parameter(description = "笔记ID", required = true) @PathVariable String noteId,
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("用户 {} 删除个人笔记 {}", username, noteId);
            
            InteractionResult result = personalGalleryService.deletePersonalNote(username, noteId);
            
            if (result.isSuccess()) {
                return Response.<String>builder()
                        .code("0000")
                        .info("删除个人笔记成功")
                        .build();
            } else {
                return Response.<String>builder()
                        .code("0001")
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("删除个人笔记失败", e);
            return Response.<String>builder()
                    .code("0001")
                    .info("删除个人笔记失败: " + e.getMessage())
                    .build();
        }
    }
    
    @GetMapping("/notes")
    @Operation(summary = "获取个人笔记列表", description = "获取用户的所有个人学习笔记")
    public Response<List<PersonalNote>> getAllPersonalNotes(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("获取用户 {} 的所有个人笔记", username);
            
            List<PersonalNote> notes = personalGalleryService.getAllPersonalNotes(username);
            
            return Response.<List<PersonalNote>>builder()
                    .code("0000")
                    .info("获取个人笔记成功")
                    .data(notes)
                    .build();
            
        } catch (Exception e) {
            log.error("获取个人笔记失败", e);
            return Response.<List<PersonalNote>>builder()
                    .code("0001")
                    .info("获取个人笔记失败: " + e.getMessage())
                    .build();
        }
    }
    
    @GetMapping("/notes/relics/{relicsId}")
    @Operation(summary = "获取文物的个人笔记", description = "获取指定文物的个人学习笔记")
    public Response<PersonalNote> getPersonalNote(
            @Parameter(description = "文物ID", required = true) @PathVariable Long relicsId,
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("获取用户 {} 对文物 {} 的个人笔记", username, relicsId);
            
            Optional<PersonalNote> noteOpt = personalGalleryService.getPersonalNote(username, relicsId);
            
            if (noteOpt.isPresent()) {
                return Response.<PersonalNote>builder()
                        .code("0000")
                        .info("获取个人笔记成功")
                        .data(noteOpt.get())
                        .build();
            } else {
                return Response.<PersonalNote>builder()
                        .code("0001")
                        .info("该文物暂无个人笔记")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("获取个人笔记失败", e);
            return Response.<PersonalNote>builder()
                    .code("0001")
                    .info("获取个人笔记失败: " + e.getMessage())
                    .build();
        }
    }
    
    // ==================== 学习记录管理 ====================
    
    @PostMapping("/learning/start")
    @Operation(summary = "开始学习记录", description = "开始记录用户的学习过程")
    public Response<String> startLearningRecord(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "文物ID", required = true) @RequestParam Long relicsId,
            @Parameter(description = "学习类型") @RequestParam(defaultValue = "DETAILED_STUDY") LearningRecord.LearningType learningType) {
        
        try {
            log.info("用户 {} 开始学习文物 {}", username, relicsId);
            
            String recordId = personalGalleryService.startLearningRecord(username, relicsId, learningType);
            
            if (recordId != null) {
                return Response.<String>builder()
                        .code("0000")
                        .info("开始学习记录成功")
                        .data(recordId)
                        .build();
            } else {
                return Response.<String>builder()
                        .code("0001")
                        .info("开始学习记录失败")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("开始学习记录失败", e);
            return Response.<String>builder()
                    .code("0001")
                    .info("开始学习记录失败: " + e.getMessage())
                    .build();
        }
    }
    
    @PostMapping("/learning/end")
    @Operation(summary = "结束学习记录", description = "结束学习记录并保存学习数据")
    public Response<String> endLearningRecord(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "学习记录ID", required = true) @RequestParam String recordId,
            @Parameter(description = "学习评分(1-5)") @RequestParam(required = false) Integer rating) {
        
        try {
            log.info("用户 {} 结束学习记录 {}", username, recordId);
            
            InteractionResult result = personalGalleryService.endLearningRecord(username, recordId, rating);
            
            if (result.isSuccess()) {
                return Response.<String>builder()
                        .code("0000")
                        .info("结束学习记录成功")
                        .build();
            } else {
                return Response.<String>builder()
                        .code("0001")
                        .info(result.getMessage())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("结束学习记录失败", e);
            return Response.<String>builder()
                    .code("0001")
                    .info("结束学习记录失败: " + e.getMessage())
                    .build();
        }
    }
    
    @GetMapping("/learning/records")
    @Operation(summary = "获取学习记录", description = "获取用户的学习记录列表")
    public Response<List<LearningRecord>> getLearningRecords(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "文物ID（可选）") @RequestParam(required = false) Long relicsId) {
        
        try {
            log.info("获取用户 {} 的学习记录", username);
            
            List<LearningRecord> records = personalGalleryService.getLearningRecords(username, relicsId);
            
            return Response.<List<LearningRecord>>builder()
                    .code("0000")
                    .info("获取学习记录成功")
                    .data(records)
                    .build();
            
        } catch (Exception e) {
            log.error("获取学习记录失败", e);
            return Response.<List<LearningRecord>>builder()
                    .code("0001")
                    .info("获取学习记录失败: " + e.getMessage())
                    .build();
        }
    }
    
    // ==================== 成就系统管理 ====================
    
    @GetMapping("/achievements")
    @Operation(summary = "获取用户成就", description = "获取用户的所有成就信息")
    public Response<List<Achievement>> getUserAchievements(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("获取用户 {} 的成就信息", username);
            
            List<Achievement> achievements = personalGalleryService.getUserAchievements(username);
            
            return Response.<List<Achievement>>builder()
                    .code("0000")
                    .info("获取用户成就成功")
                    .data(achievements)
                    .build();
            
        } catch (Exception e) {
            log.error("获取用户成就失败", e);
            return Response.<List<Achievement>>builder()
                    .code("0001")
                    .info("获取用户成就失败: " + e.getMessage())
                    .build();
        }
    }
    
    @GetMapping("/achievements/unlocked")
    @Operation(summary = "获取已解锁成就", description = "获取用户已解锁的成就列表")
    public Response<List<Achievement>> getUnlockedAchievements(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("获取用户 {} 的已解锁成就", username);
            
            List<Achievement> achievements = personalGalleryService.getUnlockedAchievements(username);
            
            return Response.<List<Achievement>>builder()
                    .code("0000")
                    .info("获取已解锁成就成功")
                    .data(achievements)
                    .build();
            
        } catch (Exception e) {
            log.error("获取已解锁成就失败", e);
            return Response.<List<Achievement>>builder()
                    .code("0001")
                    .info("获取已解锁成就失败: " + e.getMessage())
                    .build();
        }
    }
    
    @PostMapping("/achievements/check")
    @Operation(summary = "检查成就进度", description = "检查并更新用户的成就进度")
    public Response<List<Achievement>> checkAchievements(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("检查用户 {} 的成就进度", username);
            
            List<Achievement> newlyUnlocked = personalGalleryService.checkAndUpdateAchievements(username);
            
            return Response.<List<Achievement>>builder()
                    .code("0000")
                    .info("检查成就进度成功")
                    .data(newlyUnlocked)
                    .build();
            
        } catch (Exception e) {
            log.error("检查成就进度失败", e);
            return Response.<List<Achievement>>builder()
                    .code("0001")
                    .info("检查成就进度失败: " + e.getMessage())
                    .build();
        }
    }
    
    // ==================== 推荐和分析功能 ====================
    
    @GetMapping("/recommendations/relics")
    @Operation(summary = "获取文物推荐", description = "获取个性化文物推荐")
    public Response<List<Long>> getRelicsRecommendations(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            log.info("获取用户 {} 的文物推荐", username);
            
            List<Long> recommendations = personalGalleryService.getPersonalizedRelicsRecommendations(username, limit);
            
            return Response.<List<Long>>builder()
                    .code("0000")
                    .info("获取文物推荐成功")
                    .data(recommendations)
                    .build();
            
        } catch (Exception e) {
            log.error("获取文物推荐失败", e);
            return Response.<List<Long>>builder()
                    .code("0001")
                    .info("获取文物推荐失败: " + e.getMessage())
                    .build();
        }
    }
    
    @GetMapping("/analysis")
    @Operation(summary = "获取学习分析", description = "获取用户的学习分析报告")
    public Response<IPersonalGalleryService.UserLearningAnalysis> getUserLearningAnalysis(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        
        try {
            log.info("获取用户 {} 的学习分析", username);
            
            IPersonalGalleryService.UserLearningAnalysis analysis = personalGalleryService.getUserLearningAnalysis(username);
            
            return Response.<IPersonalGalleryService.UserLearningAnalysis>builder()
                    .code("0000")
                    .info("获取学习分析成功")
                    .data(analysis)
                    .build();
            
        } catch (Exception e) {
            log.error("获取学习分析失败", e);
            return Response.<IPersonalGalleryService.UserLearningAnalysis>builder()
                    .code("0001")
                    .info("获取学习分析失败: " + e.getMessage())
                    .build();
        }
    }
}
