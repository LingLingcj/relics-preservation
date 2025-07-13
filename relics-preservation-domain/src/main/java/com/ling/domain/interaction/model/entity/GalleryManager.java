package com.ling.domain.interaction.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ling.domain.interaction.model.valobj.ChangeTracker;
import com.ling.domain.interaction.model.valobj.CollectionGallery;
import com.ling.domain.interaction.model.valobj.DisplayStyle;
import com.ling.domain.interaction.model.valobj.GalleryId;
import com.ling.domain.interaction.model.valobj.GalleryTheme;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.user.model.valobj.Username;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 收藏馆管理聚合根
 * @Author: LingRJ
 * @Description: 专门管理用户收藏馆功能的聚合根，负责收藏馆创建/更新/删除、文物管理等
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Slf4j
public class GalleryManager {

    private final Username username;
    private final List<CollectionGallery> galleries;
    private final LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 变更跟踪器，用于增量保存
    private final ChangeTracker changeTracker;

    /**
     * 创建收藏馆管理聚合根
     */
    public static GalleryManager create(Username username) {
        LocalDateTime now = LocalDateTime.now();
        return GalleryManager.builder()
                .username(username)
                .galleries(new ArrayList<>())
                .createTime(now)
                .updateTime(now)
                .changeTracker(new ChangeTracker())
                .build();
    }

    /**
     * 从数据库记录重建收藏馆管理聚合根
     * @param username 用户名
     * @param galleries 收藏馆列表
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 收藏馆管理聚合根
     */
    public static GalleryManager fromDatabase(Username username,
                                            List<CollectionGallery> galleries,
                                            LocalDateTime createTime,
                                            LocalDateTime updateTime) {
        return GalleryManager.builder()
                .username(username)
                .galleries(galleries != null ? galleries : new ArrayList<>())
                .createTime(createTime != null ? createTime : LocalDateTime.now())
                .updateTime(updateTime != null ? updateTime : LocalDateTime.now())
                .changeTracker(new ChangeTracker()) // 从数据库重建时创建新的变更跟踪器
                .build();
    }

    /**
     * 创建收藏馆
     * @param name 收藏馆名称
     * @param description 收藏馆描述
     * @param theme 主题
     * @param displayStyle 展示风格
     * @param isPublic 是否公开
     * @param customThemeName 自定义主题名称
     * @return 操作结果
     */
    public InteractionResult createGallery(String name, String description,
                                         GalleryTheme theme,
                                         DisplayStyle displayStyle,
                                         boolean isPublic, String customThemeName) {
        try {
            // 检查收藏馆数量限制（最多10个）
            if (galleries.size() >= 10) {
                return InteractionResult.failure("收藏馆数量已达上限（10个）");
            }

            // 检查名称是否重复
            boolean nameExists = galleries.stream()
                    .anyMatch(gallery -> gallery.getName().equals(name.trim()));
            if (nameExists) {
                return InteractionResult.failure("收藏馆名称已存在");
            }

            // 创建新收藏馆
            CollectionGallery newGallery = CollectionGallery.create(
                    name, description, theme, displayStyle, isPublic, customThemeName);

            galleries.add(newGallery);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordAdd("GALLERY", newGallery.getGalleryId().getValue(), newGallery);

            log.info("用户 {} 创建收藏馆: {}", username.getValue(), name);
            return InteractionResult.success("收藏馆创建成功");

        } catch (Exception e) {
            log.error("创建收藏馆失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("创建收藏馆失败: " + e.getMessage());
        }
    }

    /**
     * 更新收藏馆信息
     * @param galleryId 收藏馆ID
     * @param name 新名称
     * @param description 新描述
     * @param theme 新主题
     * @param displayStyle 新展示风格
     * @param isPublic 新公开状态
     * @param customThemeName 新自定义主题名称
     * @return 操作结果
     */
    public InteractionResult updateGallery(GalleryId galleryId, String name, String description,
                                         GalleryTheme theme,
                                         DisplayStyle displayStyle,
                                         boolean isPublic, String customThemeName) {
        try {
            // 查找收藏馆
            Optional<CollectionGallery> galleryOpt = galleries.stream()
                    .filter(gallery -> gallery.getGalleryId().equals(galleryId))
                    .findFirst();

            if (galleryOpt.isEmpty()) {
                return InteractionResult.failure("收藏馆不存在");
            }

            CollectionGallery existingGallery = galleryOpt.get();

            // 检查名称是否与其他收藏馆重复
            boolean nameExists = galleries.stream()
                    .anyMatch(gallery -> !gallery.getGalleryId().equals(galleryId)
                            && gallery.getName().equals(name.trim()));
            if (nameExists) {
                return InteractionResult.failure("收藏馆名称已存在");
            }

            // 更新收藏馆
            CollectionGallery updatedGallery = existingGallery.updateInfo(
                    name, description, theme, displayStyle, isPublic, customThemeName);

            // 替换列表中的收藏馆
            int index = galleries.indexOf(existingGallery);
            galleries.set(index, updatedGallery);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordModify("GALLERY", galleryId.getValue(), updatedGallery);

            log.info("用户 {} 更新收藏馆: {}", username.getValue(), name);
            return InteractionResult.success("收藏馆更新成功");

        } catch (Exception e) {
            log.error("更新收藏馆失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("更新收藏馆失败: " + e.getMessage());
        }
    }

    /**
     * 删除收藏馆
     * @param galleryId 收藏馆ID
     * @return 操作结果
     */
    public InteractionResult deleteGallery(GalleryId galleryId) {
        try {
            // 查找收藏馆
            Optional<CollectionGallery> galleryOpt = galleries.stream()
                    .filter(gallery -> gallery.getGalleryId().equals(galleryId))
                    .findFirst();

            if (galleryOpt.isEmpty()) {
                return InteractionResult.failure("收藏馆不存在");
            }

            CollectionGallery gallery = galleryOpt.get();
            galleries.remove(gallery);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordDelete("GALLERY", galleryId.getValue(), gallery);

            log.info("用户 {} 删除收藏馆: {}", username.getValue(), gallery.getName());
            return InteractionResult.success("收藏馆删除成功");

        } catch (Exception e) {
            log.error("删除收藏馆失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("删除收藏馆失败: " + e.getMessage());
        }
    }

    /**
     * 向收藏馆添加文物
     * @param galleryId 收藏馆ID
     * @param relicsId 文物ID
     * @return 操作结果
     */
    public InteractionResult addRelicsToGallery(GalleryId galleryId, Long relicsId) {
        try {
            // 查找收藏馆
            Optional<CollectionGallery> galleryOpt = galleries.stream()
                    .filter(gallery -> gallery.getGalleryId().equals(galleryId))
                    .findFirst();

            if (galleryOpt.isEmpty()) {
                return InteractionResult.failure("收藏馆不存在");
            }

            CollectionGallery existingGallery = galleryOpt.get();

            // 添加文物到收藏馆
            CollectionGallery updatedGallery = existingGallery.addRelics(relicsId);

            // 替换列表中的收藏馆
            int index = galleries.indexOf(existingGallery);
            galleries.set(index, updatedGallery);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordModify("GALLERY", galleryId.getValue(), updatedGallery);

            log.info("用户 {} 向收藏馆 {} 添加文物 {}", username.getValue(),
                    existingGallery.getName(), relicsId);
            return InteractionResult.success("文物添加成功");

        } catch (Exception e) {
            log.error("向收藏馆添加文物失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("添加文物失败: " + e.getMessage());
        }
    }

    /**
     * 从收藏馆移除文物
     * @param galleryId 收藏馆ID
     * @param relicsId 文物ID
     * @return 操作结果
     */
    public InteractionResult removeRelicsFromGallery(GalleryId galleryId, Long relicsId) {
        try {
            // 查找收藏馆
            Optional<CollectionGallery> galleryOpt = galleries.stream()
                    .filter(gallery -> gallery.getGalleryId().equals(galleryId))
                    .findFirst();

            if (galleryOpt.isEmpty()) {
                return InteractionResult.failure("收藏馆不存在");
            }

            CollectionGallery existingGallery = galleryOpt.get();

            // 从收藏馆移除文物
            CollectionGallery updatedGallery = existingGallery.removeRelics(relicsId);

            // 替换列表中的收藏馆
            int index = galleries.indexOf(existingGallery);
            galleries.set(index, updatedGallery);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordModify("GALLERY", galleryId.getValue(), updatedGallery);

            log.info("用户 {} 从收藏馆 {} 移除文物 {}", username.getValue(),
                    existingGallery.getName(), relicsId);
            return InteractionResult.success("文物移除成功");

        } catch (Exception e) {
            log.error("从收藏馆移除文物失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("移除文物失败: " + e.getMessage());
        }
    }

    /**
     * 获取收藏馆
     * @param galleryId 收藏馆ID
     * @return 收藏馆
     */
    public Optional<CollectionGallery> getGallery(GalleryId galleryId) {
        return galleries.stream()
                .filter(gallery -> gallery.getGalleryId().equals(galleryId))
                .findFirst();
    }

    /**
     * 获取所有收藏馆
     * @return 收藏馆列表
     */
    public List<CollectionGallery> getAllGalleries() {
        return new ArrayList<>(galleries);
    }

    /**
     * 获取公开的收藏馆
     * @return 公开收藏馆列表
     */
    public List<CollectionGallery> getPublicGalleries() {
        return galleries.stream()
                .filter(CollectionGallery::isPublic)
                .toList();
    }

    /**
     * 获取收藏馆统计信息
     * @return 收藏馆统计
     */
    public GalleryStatistics getStatistics() {
        long totalGalleries = galleries.size();
        long publicGalleries = galleries.stream()
                .filter(CollectionGallery::isPublic)
                .count();
        long totalRelics = galleries.stream()
                .mapToLong(CollectionGallery::getRelicsCount)
                .sum();

        return GalleryStatistics.builder()
                .username(username.getValue())
                .totalGalleries(totalGalleries)
                .publicGalleries(publicGalleries)
                .totalRelicsInGalleries(totalRelics)
                .lastUpdateTime(updateTime)
                .build();
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return username.getValue();
    }

    // ==================== 增量保存相关方法 ====================

    /**
     * 检查是否有变更
     * @return 是否有变更
     */
    public boolean hasChanges() {
        return changeTracker.isHasChanges();
    }

    /**
     * 获取收藏馆变更记录
     * @return 收藏馆变更记录集合
     */
    public Set<ChangeTracker.ChangeRecord> getGalleryChanges() {
        return changeTracker.getChangesByType("GALLERY");
    }

    /**
     * 清空变更记录（保存成功后调用）
     */
    public void clearChanges() {
        changeTracker.clearChanges();
    }

    /**
     * 获取变更统计信息
     * @return 变更统计信息
     */
    public String getChangesSummary() {
        if (!hasChanges()) {
            return "无变更";
        }

        int galleryChanges = changeTracker.getChangeCount("GALLERY");
        return String.format("收藏馆变更: %d", galleryChanges);
    }

    /**
     * 收藏馆统计值对象
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class GalleryStatistics {
        private final String username;
        private final long totalGalleries;
        private final long publicGalleries;
        private final long totalRelicsInGalleries;
        private final LocalDateTime lastUpdateTime;
    }
}
