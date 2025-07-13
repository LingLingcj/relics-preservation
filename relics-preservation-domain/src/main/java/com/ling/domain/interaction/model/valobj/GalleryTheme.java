package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

/**
 * 收藏馆主题枚举
 * @Author: LingRJ
 * @Description: 定义收藏馆的主题分类，便于用户组织和展示收藏
 * @DateTime: 2025/7/13
 */
@Getter
public enum GalleryTheme {
    
    BRONZE("bronze", "青铜器", "古代青铜制品收藏"),
    PORCELAIN("porcelain", "瓷器", "各朝代瓷器收藏"),
    PAINTING("painting", "书画", "书法绘画作品收藏"),
    JADE("jade", "玉器", "玉石制品收藏"),
    CALLIGRAPHY("calligraphy", "书法", "书法作品收藏"),
    SCULPTURE("sculpture", "雕塑", "雕塑艺术品收藏"),
    FURNITURE("furniture", "家具", "古典家具收藏"),
    TEXTILE("textile", "织物", "古代织物收藏"),
    COIN("coin", "钱币", "古代钱币收藏"),
    WEAPON("weapon", "兵器", "古代兵器收藏"),
    ORNAMENT("ornament", "饰品", "古代饰品收藏"),
    INSTRUMENT("instrument", "乐器", "古代乐器收藏"),
    CUSTOM("custom", "自定义", "用户自定义主题");
    
    private final String code;
    private final String name;
    private final String description;
    
    GalleryTheme(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    /**
     * 根据代码获取主题
     * @param code 主题代码
     * @return 主题枚举
     */
    public static GalleryTheme fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return CUSTOM;
        }
        
        for (GalleryTheme theme : values()) {
            if (theme.code.equals(code.toLowerCase())) {
                return theme;
            }
        }
        return CUSTOM;
    }
    
    /**
     * 根据名称获取主题
     * @param name 主题名称
     * @return 主题枚举
     */
    public static GalleryTheme fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return CUSTOM;
        }
        
        for (GalleryTheme theme : values()) {
            if (theme.name.equals(name)) {
                return theme;
            }
        }
        return CUSTOM;
    }
    
    /**
     * 验证主题代码是否有效
     * @param code 主题代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        for (GalleryTheme theme : values()) {
            if (theme.code.equals(code.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", name, code);
    }
}
