package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

/**
 * 收藏馆展示风格枚举
 * @Author: LingRJ
 * @Description: 定义收藏馆的展示风格，提供不同的视觉呈现方式
 * @DateTime: 2025/7/13
 */
@Getter
public enum DisplayStyle {
    
    GRID("grid", "网格布局", "以网格形式展示文物，适合图片展示"),
    LIST("list", "列表布局", "以列表形式展示文物，适合详细信息展示"),
    TIMELINE("timeline", "时间线布局", "按时间顺序展示文物，适合历史展示"),
    MASONRY("masonry", "瀑布流布局", "不规则网格布局，适合不同尺寸图片"),
    CARD("card", "卡片布局", "以卡片形式展示文物，信息丰富"),
    GALLERY("gallery", "画廊布局", "类似画廊的展示方式，突出艺术感");
    
    private final String code;
    private final String name;
    private final String description;
    
    DisplayStyle(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    /**
     * 根据代码获取展示风格
     * @param code 风格代码
     * @return 展示风格枚举
     */
    public static DisplayStyle fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return GRID; // 默认网格布局
        }
        
        for (DisplayStyle style : values()) {
            if (style.code.equals(code.toLowerCase())) {
                return style;
            }
        }
        return GRID;
    }
    
    /**
     * 根据名称获取展示风格
     * @param name 风格名称
     * @return 展示风格枚举
     */
    public static DisplayStyle fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return GRID;
        }
        
        for (DisplayStyle style : values()) {
            if (style.name.equals(name)) {
                return style;
            }
        }
        return GRID;
    }
    
    /**
     * 验证风格代码是否有效
     * @param code 风格代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        for (DisplayStyle style : values()) {
            if (style.code.equals(code.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取默认展示风格
     * @return 默认展示风格
     */
    public static DisplayStyle getDefault() {
        return GRID;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s)", name, code);
    }
}
