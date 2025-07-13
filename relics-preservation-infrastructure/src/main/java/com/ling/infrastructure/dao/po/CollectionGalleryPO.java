package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏馆持久化对象
 * @Author: LingRJ
 * @Description: 收藏馆数据库映射对象
 * @DateTime: 2025/7/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionGalleryPO {
    
    /** 自增主键 */
    private Long id;
    
    /** 收藏馆唯一标识(UUID) */
    private String galleryId;
    
    /** 用户名 */
    private String username;
    
    /** 收藏馆名称 */
    private String name;
    
    /** 收藏馆描述 */
    private String description;
    
    /** 主题代码(bronze,porcelain,painting等) */
    private String theme;
    
    /** 展示风格代码(grid,list,timeline等) */
    private String displayStyle;
    
    /** 文物ID列表，逗号分隔 */
    private String relicsIds;
    
    /** 是否公开(0=私有,1=公开) */
    private Integer isPublic;
    
    /** 分享码 */
    private String shareCode;
    
    /** 自定义主题名称 */
    private String customThemeName;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 状态(0=正常,1=已删除) */
    private Integer status;
    
    /**
     * 检查是否为公开收藏馆
     * @return 是否公开
     */
    public boolean isPublicGallery() {
        return isPublic != null && isPublic == 1;
    }
    
    /**
     * 检查是否为正常状态
     * @return 是否正常
     */
    public boolean isNormalStatus() {
        return status != null && status == 0;
    }
    
    /**
     * 检查是否为已删除状态
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return status != null && status == 1;
    }
    
    /**
     * 设置为公开状态
     */
    public void setPublic() {
        this.isPublic = 1;
    }
    
    /**
     * 设置为私有状态
     */
    public void setPrivate() {
        this.isPublic = 0;
    }
    
    /**
     * 设置为正常状态
     */
    public void setNormalStatus() {
        this.status = 0;
    }
    
    /**
     * 设置为已删除状态
     */
    public void setDeletedStatus() {
        this.status = 1;
    }
    
    /**
     * 获取文物数量
     * @return 文物数量
     */
    public int getRelicsCount() {
        if (relicsIds == null || relicsIds.trim().isEmpty()) {
            return 0;
        }
        return relicsIds.split(",").length;
    }
    
    /**
     * 检查是否包含指定文物
     * @param relicsId 文物ID
     * @return 是否包含
     */
    public boolean containsRelics(Long relicsId) {
        if (relicsIds == null || relicsIds.trim().isEmpty() || relicsId == null) {
            return false;
        }
        String[] ids = relicsIds.split(",");
        String targetId = relicsId.toString();
        for (String id : ids) {
            if (targetId.equals(id.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 添加文物ID
     * @param relicsId 文物ID
     */
    public void addRelicsId(Long relicsId) {
        if (relicsId == null) {
            return;
        }
        
        if (relicsIds == null || relicsIds.trim().isEmpty()) {
            relicsIds = relicsId.toString();
        } else if (!containsRelics(relicsId)) {
            relicsIds = relicsIds + "," + relicsId;
        }
    }
    
    /**
     * 移除文物ID
     * @param relicsId 文物ID
     */
    public void removeRelicsId(Long relicsId) {
        if (relicsId == null || relicsIds == null || relicsIds.trim().isEmpty()) {
            return;
        }
        
        String[] ids = relicsIds.split(",");
        String targetId = relicsId.toString();
        StringBuilder newIds = new StringBuilder();
        
        for (String id : ids) {
            String trimmedId = id.trim();
            if (!targetId.equals(trimmedId)) {
                if (!newIds.isEmpty()) {
                    newIds.append(",");
                }
                newIds.append(trimmedId);
            }
        }
        
        relicsIds = newIds.toString();
    }
    
    /**
     * 获取显示名称（用于日志和调试）
     * @return 显示名称
     */
    public String getDisplayName() {
        return String.format("%s(%s)", name, galleryId);
    }
    
    /**
     * 获取主题显示名称
     * @return 主题显示名称
     */
    public String getThemeDisplayName() {
        if ("custom".equals(theme) && customThemeName != null && !customThemeName.trim().isEmpty()) {
            return customThemeName.trim();
        }
        return getThemeNameByCode(theme);
    }
    
    /**
     * 根据主题代码获取主题名称
     * @param themeCode 主题代码
     * @return 主题名称
     */
    private String getThemeNameByCode(String themeCode) {
        if (themeCode == null) {
            return "未知主题";
        }

        return switch (themeCode.toLowerCase()) {
            case "bronze" -> "青铜器";
            case "porcelain" -> "瓷器";
            case "painting" -> "书画";
            case "jade" -> "玉器";
            case "calligraphy" -> "书法";
            case "sculpture" -> "雕塑";
            case "furniture" -> "家具";
            case "textile" -> "织物";
            case "coin" -> "钱币";
            case "weapon" -> "兵器";
            case "ornament" -> "饰品";
            case "instrument" -> "乐器";
            case "custom" -> "自定义";
            default -> "未知主题";
        };
    }
    
    /**
     * 获取展示风格显示名称
     * @return 展示风格显示名称
     */
    public String getDisplayStyleName() {
        if (displayStyle == null) {
            return "未知风格";
        }

        return switch (displayStyle.toLowerCase()) {
            case "grid" -> "网格布局";
            case "list" -> "列表布局";
            case "timeline" -> "时间线布局";
            case "masonry" -> "瀑布流布局";
            case "card" -> "卡片布局";
            case "gallery" -> "画廊布局";
            default -> "未知风格";
        };
    }
    
    @Override
    public String toString() {
        return String.format("CollectionGalleryPO{id=%d, galleryId='%s', username='%s', name='%s', theme='%s', relicsCount=%d, isPublic=%s, status=%d}", 
                id, galleryId, username, name, theme, getRelicsCount(), isPublicGallery(), status);
    }
}
