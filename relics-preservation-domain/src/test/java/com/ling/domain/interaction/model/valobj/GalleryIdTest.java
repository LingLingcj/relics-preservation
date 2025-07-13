package com.ling.domain.interaction.model.valobj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * GalleryId值对象单元测试
 * @Author: LingRJ
 * @Description: 测试收藏馆ID值对象的所有功能
 * @DateTime: 2025/7/13
 */
@DisplayName("收藏馆ID值对象测试")
class GalleryIdTest {

    @Test
    @DisplayName("生成新的收藏馆ID")
    void testGenerate() {
        // When
        GalleryId galleryId = GalleryId.generate();

        // Then
        assertNotNull(galleryId);
        assertNotNull(galleryId.getValue());
        assertFalse(galleryId.getValue().isEmpty());
        assertTrue(GalleryId.isValid(galleryId.getValue()));
    }

    @Test
    @DisplayName("从字符串创建收藏馆ID")
    void testOf() {
        // Given
        String validUuid = UUID.randomUUID().toString();

        // When
        GalleryId galleryId = GalleryId.of(validUuid);

        // Then
        assertNotNull(galleryId);
        assertEquals(validUuid, galleryId.getValue());
    }

    @Test
    @DisplayName("从字符串创建收藏馆ID - 参数验证")
    void testOf_Validation() {
        // When & Then
        assertThrows(NullPointerException.class, () -> GalleryId.of(null));
        assertThrows(IllegalArgumentException.class, () -> GalleryId.of(""));
        assertThrows(IllegalArgumentException.class, () -> GalleryId.of("   "));
    }

    @Test
    @DisplayName("验证ID格式")
    void testIsValid() {
        // Given
        String validUuid = UUID.randomUUID().toString();
        String invalidUuid = "invalid-uuid";

        // When & Then
        assertTrue(GalleryId.isValid(validUuid));
        assertFalse(GalleryId.isValid(invalidUuid));
        assertFalse(GalleryId.isValid(null));
        assertFalse(GalleryId.isValid(""));
        assertFalse(GalleryId.isValid("   "));
    }

    @Test
    @DisplayName("收藏馆ID相等性测试")
    void testEquality() {
        // Given
        String uuid = UUID.randomUUID().toString();
        GalleryId id1 = GalleryId.of(uuid);
        GalleryId id2 = GalleryId.of(uuid);
        GalleryId id3 = GalleryId.generate();

        // When & Then
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }

    @Test
    @DisplayName("toString方法测试")
    void testToString() {
        // Given
        String uuid = UUID.randomUUID().toString();
        GalleryId galleryId = GalleryId.of(uuid);

        // When & Then
        assertEquals(uuid, galleryId.toString());
    }

    @Test
    @DisplayName("生成的ID唯一性测试")
    void testUniqueness() {
        // When
        GalleryId id1 = GalleryId.generate();
        GalleryId id2 = GalleryId.generate();
        GalleryId id3 = GalleryId.generate();

        // Then
        assertNotEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id2, id3);
        
        assertNotEquals(id1.getValue(), id2.getValue());
        assertNotEquals(id1.getValue(), id3.getValue());
        assertNotEquals(id2.getValue(), id3.getValue());
    }
}
