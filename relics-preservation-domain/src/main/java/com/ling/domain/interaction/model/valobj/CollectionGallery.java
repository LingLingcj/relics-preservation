package com.ling.domain.interaction.model.valobj;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收藏馆值对象
 * @Author: LingRJ
 * @Description: 封装用户个人收藏馆的完整信息和行为
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@EqualsAndHashCode(of = {"galleryId"})
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Slf4j
public class CollectionGallery {
    
    private final GalleryId galleryId;
    private final String name;
    private final String description;
    private final GalleryTheme theme;
    private final DisplayStyle displayStyle;
    private final List<Long> relicsIds;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;
    private final boolean isPublic;
    private final String shareCode;
    private final String customThemeName; // 自定义主题名称

    // 个人收藏馆增强功能
    private final Map<Long, PersonalNote> personalNotes; // 文物个人笔记
    private final List<String> personalTags; // 个人标签
    private final GalleryLearningStats learningStats; // 学习统计
    private final Integer viewCount; // 浏览次数
    private final Integer shareCount; // 分享次数
    private final LocalDateTime lastViewTime; // 最后浏览时间
    
    /**
     * 创建新的收藏馆
     * @param name 收藏馆名称
     * @param description 收藏馆描述
     * @param theme 主题
     * @param displayStyle 展示风格
     * @param isPublic 是否公开
     * @return 收藏馆值对象
     */
    public static CollectionGallery create(String name, String description,
                                         GalleryTheme theme, DisplayStyle displayStyle,
                                         boolean isPublic) {
        return create(name, description, theme, displayStyle, isPublic, null);
    }
    
    /**
     * 创建新的收藏馆（带自定义主题名称）
     * @param name 收藏馆名称
     * @param description 收藏馆描述
     * @param theme 主题
     * @param displayStyle 展示风格
     * @param isPublic 是否公开
     * @param customThemeName 自定义主题名称
     * @return 收藏馆值对象
     */
    public static CollectionGallery create(String name, String description, 
                                         GalleryTheme theme, DisplayStyle displayStyle, 
                                         boolean isPublic, String customThemeName) {
        validateCreateParams(name, description, theme, displayStyle, customThemeName);
        
        LocalDateTime now = LocalDateTime.now();
        String shareCode = generateShareCode();
        
        CollectionGallery gallery = CollectionGallery.builder()
                .galleryId(GalleryId.generate())
                .name(name.trim())
                .description(description != null ? description.trim() : "")
                .theme(theme)
                .displayStyle(displayStyle)
                .relicsIds(new ArrayList<>())
                .createTime(now)
                .updateTime(now)
                .isPublic(isPublic)
                .shareCode(shareCode)
                .customThemeName(theme == GalleryTheme.CUSTOM ? customThemeName : null)
                // 个人收藏馆增强功能初始化
                .personalNotes(new HashMap<>())
                .personalTags(new ArrayList<>())
                .learningStats(GalleryLearningStats.empty())
                .viewCount(0)
                .shareCount(0)
                .lastViewTime(null)
                .build();
        
        log.info("创建新收藏馆: {} ({})", name, gallery.galleryId.getValue());
        return gallery;
    }
    
    /**
     * 从数据库重建收藏馆
     * @param galleryId 收藏馆ID
     * @param name 名称
     * @param description 描述
     * @param theme 主题
     * @param displayStyle 展示风格
     * @param relicsIds 文物ID列表
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param isPublic 是否公开
     * @param shareCode 分享码
     * @param customThemeName 自定义主题名称
     * @return 收藏馆值对象
     */
    public static CollectionGallery fromDatabase(GalleryId galleryId, String name, String description,
                                               GalleryTheme theme, DisplayStyle displayStyle,
                                               List<Long> relicsIds, LocalDateTime createTime,
                                               LocalDateTime updateTime, boolean isPublic,
                                               String shareCode, String customThemeName) {
        return CollectionGallery.builder()
                .galleryId(galleryId)
                .name(name)
                .description(description)
                .theme(theme)
                .displayStyle(displayStyle)
                .relicsIds(relicsIds != null ? new ArrayList<>(relicsIds) : new ArrayList<>())
                .createTime(createTime)
                .updateTime(updateTime)
                .isPublic(isPublic)
                .shareCode(shareCode)
                .customThemeName(customThemeName)
                // 个人收藏馆增强功能初始化（从数据库重建时使用默认值）
                .personalNotes(new HashMap<>())
                .personalTags(new ArrayList<>())
                .learningStats(GalleryLearningStats.empty())
                .viewCount(0)
                .shareCount(0)
                .lastViewTime(null)
                .build();
    }
    
    /**
     * 更新收藏馆基本信息
     * @param newName 新名称
     * @param newDescription 新描述
     * @param newTheme 新主题
     * @param newDisplayStyle 新展示风格
     * @param newIsPublic 新公开状态
     * @param newCustomThemeName 新自定义主题名称
     * @return 更新后的收藏馆
     */
    public CollectionGallery updateInfo(String newName, String newDescription, 
                                      GalleryTheme newTheme, DisplayStyle newDisplayStyle,
                                      boolean newIsPublic, String newCustomThemeName) {
        validateCreateParams(newName, newDescription, newTheme, newDisplayStyle, newCustomThemeName);
        
        return CollectionGallery.builder()
                .galleryId(this.galleryId)
                .name(newName.trim())
                .description(newDescription != null ? newDescription.trim() : "")
                .theme(newTheme)
                .displayStyle(newDisplayStyle)
                .relicsIds(new ArrayList<>(this.relicsIds))
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isPublic(newIsPublic)
                .shareCode(this.shareCode)
                .customThemeName(newTheme == GalleryTheme.CUSTOM ? newCustomThemeName : null)
                .build();
    }
    
    /**
     * 添加文物到收藏馆
     * @param relicsId 文物ID
     * @return 更新后的收藏馆
     */
    public CollectionGallery addRelics(Long relicsId) {
        if (relicsId == null || relicsId <= 0) {
            throw new IllegalArgumentException("文物ID无效");
        }
        
        if (relicsIds.contains(relicsId)) {
            throw new IllegalArgumentException("文物已在收藏馆中");
        }
        
        List<Long> newRelicsIds = new ArrayList<>(this.relicsIds);
        newRelicsIds.add(relicsId);
        
        return this.builder()
                .relicsIds(newRelicsIds)
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 从收藏馆移除文物
     * @param relicsId 文物ID
     * @return 更新后的收藏馆
     */
    public CollectionGallery removeRelics(Long relicsId) {
        if (relicsId == null || relicsId <= 0) {
            throw new IllegalArgumentException("文物ID无效");
        }
        
        if (!relicsIds.contains(relicsId)) {
            throw new IllegalArgumentException("文物不在收藏馆中");
        }
        
        List<Long> newRelicsIds = new ArrayList<>(this.relicsIds);
        newRelicsIds.remove(relicsId);
        
        return CollectionGallery.builder()
                .galleryId(this.galleryId)
                .name(this.name)
                .description(this.description)
                .theme(this.theme)
                .displayStyle(this.displayStyle)
                .relicsIds(newRelicsIds)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isPublic(this.isPublic)
                .shareCode(this.shareCode)
                .customThemeName(this.customThemeName)
                .build();
    }
    
    /**
     * 检查文物是否在收藏馆中
     * @param relicsId 文物ID
     * @return 是否包含
     */
    public boolean containsRelics(Long relicsId) {
        return relicsIds.contains(relicsId);
    }
    
    /**
     * 获取收藏馆中文物数量
     * @return 文物数量
     */
    public int getRelicsCount() {
        return relicsIds.size();
    }
    
    /**
     * 获取有效的主题名称
     * @return 主题名称
     */
    public String getEffectiveThemeName() {
        if (theme == GalleryTheme.CUSTOM && customThemeName != null && !customThemeName.trim().isEmpty()) {
            return customThemeName.trim();
        }
        return theme.getName();
    }
    
    /**
     * 生成分享链接
     * @param baseUrl 基础URL
     * @return 分享链接
     */
    public String generateShareUrl(String baseUrl) {
        if (!isPublic) {
            throw new IllegalStateException("私有收藏馆不能生成分享链接");
        }
        return String.format("%s/gallery/share/%s", baseUrl, shareCode);
    }
    
    /**
     * 验证创建参数
     */
    private static void validateCreateParams(String name, String description, 
                                           GalleryTheme theme, DisplayStyle displayStyle,
                                           String customThemeName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("收藏馆名称不能为空");
        }
        
        if (name.trim().length() > 50) {
            throw new IllegalArgumentException("收藏馆名称不能超过50个字符");
        }
        
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("收藏馆描述不能超过500个字符");
        }
        
        if (theme == null) {
            throw new IllegalArgumentException("收藏馆主题不能为空");
        }
        
        if (displayStyle == null) {
            throw new IllegalArgumentException("展示风格不能为空");
        }
        
        if (theme == GalleryTheme.CUSTOM) {
            if (customThemeName == null || customThemeName.trim().isEmpty()) {
                throw new IllegalArgumentException("自定义主题名称不能为空");
            }
            if (customThemeName.trim().length() > 20) {
                throw new IllegalArgumentException("自定义主题名称不能超过20个字符");
            }
        }
    }
    
    /**
     * 生成分享码
     */
    private static String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // ==================== 个人收藏馆增强功能方法 ====================

    /**
     * 添加个人笔记
     * @param relicsId 文物ID
     * @param note 个人笔记
     * @return 更新后的收藏馆
     */
    public CollectionGallery addPersonalNote(Long relicsId, PersonalNote note) {
        if (relicsId == null || note == null) {
            return this;
        }

        if (!relicsIds.contains(relicsId)) {
            throw new IllegalArgumentException("文物不在此收藏馆中");
        }

        Map<Long, PersonalNote> newNotes = new HashMap<>(this.personalNotes);
        newNotes.put(relicsId, note);

        return CollectionGallery.builder()
                .galleryId(this.galleryId)
                .name(this.name)
                .description(this.description)
                .theme(this.theme)
                .displayStyle(this.displayStyle)
                .relicsIds(this.relicsIds)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isPublic(this.isPublic)
                .shareCode(this.shareCode)
                .customThemeName(this.customThemeName)
                .personalNotes(newNotes)
                .personalTags(this.personalTags)
                .learningStats(this.learningStats.updateStats(0L, 1, null))
                .viewCount(this.viewCount)
                .shareCount(this.shareCount)
                .lastViewTime(this.lastViewTime)
                .build();
    }

    /**
     * 添加个人标签
     * @param tag 标签
     * @return 更新后的收藏馆
     */
    public CollectionGallery addPersonalTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return this;
        }

        String trimmedTag = tag.trim();
        if (personalTags.contains(trimmedTag)) {
            return this;
        }

        List<String> newTags = new ArrayList<>(this.personalTags);
        newTags.add(trimmedTag);

        return CollectionGallery.builder()
                .galleryId(this.galleryId)
                .name(this.name)
                .description(this.description)
                .theme(this.theme)
                .displayStyle(this.displayStyle)
                .relicsIds(this.relicsIds)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isPublic(this.isPublic)
                .shareCode(this.shareCode)
                .customThemeName(this.customThemeName)
                .personalNotes(this.personalNotes)
                .personalTags(newTags)
                .learningStats(this.learningStats)
                .viewCount(this.viewCount)
                .shareCount(this.shareCount)
                .lastViewTime(this.lastViewTime)
                .build();
    }

    /**
     * 记录浏览
     * @return 更新后的收藏馆
     */
    public CollectionGallery recordView() {
        return CollectionGallery.builder()
                .galleryId(this.galleryId)
                .name(this.name)
                .description(this.description)
                .theme(this.theme)
                .displayStyle(this.displayStyle)
                .relicsIds(this.relicsIds)
                .createTime(this.createTime)
                .updateTime(this.updateTime)
                .isPublic(this.isPublic)
                .shareCode(this.shareCode)
                .customThemeName(this.customThemeName)
                .personalNotes(this.personalNotes)
                .personalTags(this.personalTags)
                .learningStats(this.learningStats)
                .viewCount(this.viewCount + 1)
                .shareCount(this.shareCount)
                .lastViewTime(LocalDateTime.now())
                .build();
    }

    /**
     * 记录分享
     * @return 更新后的收藏馆
     */
    public CollectionGallery recordShare() {
        return CollectionGallery.builder()
                .galleryId(this.galleryId)
                .name(this.name)
                .description(this.description)
                .theme(this.theme)
                .displayStyle(this.displayStyle)
                .relicsIds(this.relicsIds)
                .createTime(this.createTime)
                .updateTime(LocalDateTime.now())
                .isPublic(this.isPublic)
                .shareCode(this.shareCode)
                .customThemeName(this.customThemeName)
                .personalNotes(this.personalNotes)
                .personalTags(this.personalTags)
                .learningStats(this.learningStats)
                .viewCount(this.viewCount)
                .shareCount(this.shareCount + 1)
                .lastViewTime(this.lastViewTime)
                .build();
    }

    /**
     * 获取文物的个人笔记
     * @param relicsId 文物ID
     * @return 个人笔记
     */
    public Optional<PersonalNote> getPersonalNote(Long relicsId) {
        return Optional.ofNullable(personalNotes.get(relicsId));
    }

    /**
     * 获取学习完成度
     * @return 学习完成度百分比
     */
    public double getLearningCompletionPercentage() {
        if (relicsIds.isEmpty()) {
            return 0.0;
        }
        return (double) personalNotes.size() / relicsIds.size() * 100.0;
    }

    @Override
    public String toString() {
        return String.format("CollectionGallery{id=%s, name='%s', theme=%s, relicsCount=%d, notesCount=%d}",
                galleryId.getValue(), name, getEffectiveThemeName(), getRelicsCount(), personalNotes.size());
    }
}
