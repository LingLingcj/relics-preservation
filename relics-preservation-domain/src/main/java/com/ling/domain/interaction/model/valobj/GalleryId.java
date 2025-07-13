package com.ling.domain.interaction.model.valobj;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * 收藏馆唯一标识值对象
 * @Author: LingRJ
 * @Description: 收藏馆的唯一标识符，确保收藏馆的唯一性
 * @DateTime: 2025/7/13
 */
@Getter
@EqualsAndHashCode
public class GalleryId {
    
    private final String value;
    
    private GalleryId(String value) {
        this.value = Objects.requireNonNull(value, "收藏馆ID不能为空");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("收藏馆ID不能为空字符串");
        }
    }
    
    /**
     * 生成新的收藏馆ID
     * @return 收藏馆ID
     */
    public static GalleryId generate() {
        return new GalleryId(UUID.randomUUID().toString());
    }
    
    /**
     * 从字符串创建收藏馆ID
     * @param value ID字符串
     * @return 收藏馆ID
     */
    public static GalleryId of(String value) {
        return new GalleryId(value);
    }
    
    /**
     * 验证ID格式是否有效
     * @param value ID字符串
     * @return 是否有效
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
}
