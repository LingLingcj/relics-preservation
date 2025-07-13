package com.ling.infrastructure.repository.converter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.interaction.model.valobj.CollectionGallery;
import com.ling.domain.interaction.model.valobj.DisplayStyle;
import com.ling.domain.interaction.model.valobj.GalleryId;
import com.ling.domain.interaction.model.valobj.GalleryTheme;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.po.CollectionGalleryPO;

import lombok.extern.slf4j.Slf4j;

/**
 * 收藏馆管理数据转换器
 * @Author: LingRJ
 * @Description: 负责收藏馆管理聚合根与数据库记录之间的转换
 * @DateTime: 2025/7/13
 */
@Slf4j
@Component
public class GalleryManagerConverter {

    /**
     * 构建收藏馆管理聚合根
     * @param username 用户名
     * @param galleryPOs 收藏馆PO列表
     * @return 收藏馆管理聚合根
     */
    public GalleryManager buildGalleryManager(Username username, List<CollectionGalleryPO> galleryPOs) {
        try {
            List<CollectionGallery> galleries = convertToCollectionGalleries(galleryPOs);
            
            // 计算创建时间和更新时间
            LocalDateTime createTime = galleryPOs.stream()
                    .map(CollectionGalleryPO::getCreateTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            LocalDateTime updateTime = galleryPOs.stream()
                    .map(CollectionGalleryPO::getUpdateTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            return GalleryManager.fromDatabase(username, galleries, createTime, updateTime);
            
        } catch (Exception e) {
            log.error("构建收藏馆管理聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            return GalleryManager.create(username);
        }
    }

    /**
     * 转换收藏馆PO列表为领域对象列表
     * @param galleryPOs 收藏馆PO列表
     * @return 收藏馆领域对象列表
     */
    public List<CollectionGallery> convertToCollectionGalleries(List<CollectionGalleryPO> galleryPOs) {
        List<CollectionGallery> galleries = new ArrayList<>();
        
        for (CollectionGalleryPO galleryPO : galleryPOs) {
            try {
                CollectionGallery gallery = convertToCollectionGallery(galleryPO);
                galleries.add(gallery);
            } catch (Exception e) {
                log.warn("转换收藏馆记录失败: {} - {}", galleryPO.getGalleryId(), e.getMessage());
            }
        }
        
        return galleries;
    }

    /**
     * 转换收藏馆PO为领域对象
     * @param galleryPO 收藏馆PO
     * @return 收藏馆领域对象
     */
    public CollectionGallery convertToCollectionGallery(CollectionGalleryPO galleryPO) {
        if (galleryPO == null) {
            throw new IllegalArgumentException("收藏馆PO不能为空");
        }

        // 转换文物ID列表
        List<Long> relicsIds = parseRelicsIds(galleryPO.getRelicsIds());
        
        // 转换主题
        GalleryTheme theme = GalleryTheme.fromCode(galleryPO.getTheme());
        
        // 转换展示风格
        DisplayStyle displayStyle = DisplayStyle.fromCode(galleryPO.getDisplayStyle());
        
        return CollectionGallery.fromDatabase(
                GalleryId.of(galleryPO.getGalleryId()),
                galleryPO.getName(),
                galleryPO.getDescription(),
                theme,
                displayStyle,
                relicsIds,
                galleryPO.getCreateTime(),
                galleryPO.getUpdateTime(),
                galleryPO.isPublicGallery(),
                galleryPO.getShareCode(),
                galleryPO.getCustomThemeName()
        );
    }

    /**
     * 转换收藏馆管理聚合根为PO列表
     * @param galleryManager 收藏馆管理聚合根
     * @return 收藏馆PO列表
     */
    public List<CollectionGalleryPO> convertToCollectionGalleries(GalleryManager galleryManager) {
        List<CollectionGalleryPO> galleryPOs = new ArrayList<>();
        
        for (CollectionGallery gallery : galleryManager.getAllGalleries()) {
            try {
                CollectionGalleryPO galleryPO = convertToCollectionGalleryPO(
                        galleryManager.getUsername(), gallery);
                galleryPOs.add(galleryPO);
            } catch (Exception e) {
                log.warn("转换收藏馆领域对象失败: {} - {}", 
                        gallery.getGalleryId().getValue(), e.getMessage());
            }
        }
        
        return galleryPOs;
    }

    /**
     * 转换收藏馆领域对象为PO
     * @param username 用户名
     * @param gallery 收藏馆领域对象
     * @return 收藏馆PO
     */
    public CollectionGalleryPO convertToCollectionGalleryPO(Username username, CollectionGallery gallery) {
        if (gallery == null) {
            throw new IllegalArgumentException("收藏馆领域对象不能为空");
        }

        // 转换文物ID列表为字符串
        String relicsIds = formatRelicsIds(gallery.getRelicsIds());
        
        return CollectionGalleryPO.builder()
                .galleryId(gallery.getGalleryId().getValue())
                .username(username.getValue())
                .name(gallery.getName())
                .description(gallery.getDescription())
                .theme(gallery.getTheme().getCode())
                .displayStyle(gallery.getDisplayStyle().getCode())
                .relicsIds(relicsIds)
                .isPublic(gallery.isPublic() ? 1 : 0)
                .shareCode(gallery.getShareCode())
                .customThemeName(gallery.getCustomThemeName())
                .createTime(gallery.getCreateTime())
                .updateTime(gallery.getUpdateTime())
                .status(0) // 新创建的收藏馆状态为正常
                .build();
    }

    /**
     * 解析文物ID字符串为列表
     * @param relicsIdsStr 文物ID字符串（逗号分隔）
     * @return 文物ID列表
     */
    private List<Long> parseRelicsIds(String relicsIdsStr) {
        if (relicsIdsStr == null || relicsIdsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return Arrays.stream(relicsIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.warn("解析文物ID列表失败: {} - {}", relicsIdsStr, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 格式化文物ID列表为字符串
     * @param relicsIds 文物ID列表
     * @return 文物ID字符串（逗号分隔）
     */
    private String formatRelicsIds(List<Long> relicsIds) {
        if (relicsIds == null || relicsIds.isEmpty()) {
            return "";
        }

        return relicsIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 构建收藏馆统计信息
     * @param username 用户名
     * @param galleryPOs 收藏馆PO列表
     * @return 收藏馆统计信息
     */
    public GalleryManager.GalleryStatistics buildGalleryStatistics(String username, 
                                                                   List<CollectionGalleryPO> galleryPOs) {
        long totalGalleries = galleryPOs.size();
        long publicGalleries = galleryPOs.stream()
                .filter(CollectionGalleryPO::isPublicGallery)
                .count();
        long privateGalleries = totalGalleries - publicGalleries;
        
        long totalRelics = galleryPOs.stream()
                .mapToLong(CollectionGalleryPO::getRelicsCount)
                .sum();
        
        // 找出最常用的主题
        String mostUsedTheme = galleryPOs.stream()
                .collect(Collectors.groupingBy(CollectionGalleryPO::getTheme, Collectors.counting()))
                .entrySet().stream()
                .max((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                .map(entry -> entry.getKey())
                .orElse("custom");
        
        // 最后创建收藏馆的时间
        LocalDateTime lastGalleryTime = galleryPOs.stream()
                .map(CollectionGalleryPO::getCreateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        return GalleryManager.GalleryStatistics.builder()
                .username(username)
                .totalGalleries(totalGalleries)
                .publicGalleries(publicGalleries)
                .build();
    }

    /**
     * 验证收藏馆PO数据的有效性
     * @param galleryPO 收藏馆PO
     * @return 是否有效
     */
    public boolean isValidGalleryPO(CollectionGalleryPO galleryPO) {
        if (galleryPO == null) {
            return false;
        }

        // 检查必要字段
        if (galleryPO.getGalleryId() == null || galleryPO.getGalleryId().trim().isEmpty()) {
            log.warn("收藏馆ID为空: {}", galleryPO);
            return false;
        }

        if (galleryPO.getUsername() == null || galleryPO.getUsername().trim().isEmpty()) {
            log.warn("用户名为空: {}", galleryPO);
            return false;
        }

        if (galleryPO.getName() == null || galleryPO.getName().trim().isEmpty()) {
            log.warn("收藏馆名称为空: {}", galleryPO);
            return false;
        }

        // 检查状态
        if (!galleryPO.isNormalStatus()) {
            log.debug("收藏馆状态异常: {}", galleryPO);
            return false;
        }

        return true;
    }

    /**
     * 过滤有效的收藏馆PO
     * @param galleryPOs 收藏馆PO列表
     * @return 有效的收藏馆PO列表
     */
    public List<CollectionGalleryPO> filterValidGalleryPOs(List<CollectionGalleryPO> galleryPOs) {
        if (galleryPOs == null) {
            return new ArrayList<>();
        }

        return galleryPOs.stream()
                .filter(this::isValidGalleryPO)
                .collect(Collectors.toList());
    }
}
