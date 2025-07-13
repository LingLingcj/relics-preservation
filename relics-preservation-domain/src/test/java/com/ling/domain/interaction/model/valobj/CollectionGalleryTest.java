package com.ling.domain.interaction.model.valobj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * CollectionGallery值对象单元测试
 * @Author: LingRJ
 * @Description: 测试收藏馆值对象的所有功能和边界条件
 * @DateTime: 2025/7/13
 */
@DisplayName("收藏馆值对象测试")
class CollectionGalleryTest {

    @Test
    @DisplayName("创建收藏馆 - 正常情况")
    void testCreateGallery_Success() {
        // Given
        String name = "我的青铜器收藏";
        String description = "收集各朝代的青铜器";
        GalleryTheme theme = GalleryTheme.BRONZE;
        DisplayStyle displayStyle = DisplayStyle.GRID;
        boolean isPublic = true;

        // When
        CollectionGallery gallery = CollectionGallery.create(name, description, theme, displayStyle, isPublic);

        // Then
        assertNotNull(gallery);
        assertEquals(name, gallery.getName());
        assertEquals(description, gallery.getDescription());
        assertEquals(theme, gallery.getTheme());
        assertEquals(displayStyle, gallery.getDisplayStyle());
        assertTrue(gallery.isPublic());
        assertNotNull(gallery.getGalleryId());
        assertNotNull(gallery.getShareCode());
        assertEquals(0, gallery.getRelicsCount());
        assertNotNull(gallery.getCreateTime());
        assertNotNull(gallery.getUpdateTime());
    }

    @Test
    @DisplayName("创建收藏馆 - 自定义主题")
    void testCreateGallery_CustomTheme() {
        // Given
        String name = "我的特殊收藏";
        String description = "特殊主题收藏";
        GalleryTheme theme = GalleryTheme.CUSTOM;
        DisplayStyle displayStyle = DisplayStyle.LIST;
        boolean isPublic = false;
        String customThemeName = "民国文物";

        // When
        CollectionGallery gallery = CollectionGallery.create(name, description, theme, displayStyle, isPublic, customThemeName);

        // Then
        assertEquals(customThemeName, gallery.getCustomThemeName());
        assertEquals(customThemeName, gallery.getEffectiveThemeName());
        assertFalse(gallery.isPublic());
    }

    @Test
    @DisplayName("创建收藏馆 - 参数验证失败")
    void testCreateGallery_ValidationFailure() {
        // 名称为空
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create("", "描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true));
        
        // 名称为null
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create(null, "描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true));
        
        // 名称过长
        String longName = "a".repeat(51);
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create(longName, "描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true));
        
        // 描述过长
        String longDescription = "a".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create("名称", longDescription, GalleryTheme.BRONZE, DisplayStyle.GRID, true));
        
        // 主题为null
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create("名称", "描述", null, DisplayStyle.GRID, true));
        
        // 展示风格为null
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create("名称", "描述", GalleryTheme.BRONZE, null, true));
        
        // 自定义主题名称为空
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create("名称", "描述", GalleryTheme.CUSTOM, DisplayStyle.GRID, true, ""));
        
        // 自定义主题名称过长
        String longCustomTheme = "a".repeat(21);
        assertThrows(IllegalArgumentException.class, () -> 
            CollectionGallery.create("名称", "描述", GalleryTheme.CUSTOM, DisplayStyle.GRID, true, longCustomTheme));
    }

    @Test
    @DisplayName("更新收藏馆信息")
    void testUpdateGalleryInfo() {
        // Given
        CollectionGallery originalGallery = CollectionGallery.create(
            "原始名称", "原始描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);
        
        String newName = "新名称";
        String newDescription = "新描述";
        GalleryTheme newTheme = GalleryTheme.PORCELAIN;
        DisplayStyle newDisplayStyle = DisplayStyle.LIST;
        boolean newIsPublic = false;

        // When
        CollectionGallery updatedGallery = originalGallery.updateInfo(
            newName, newDescription, newTheme, newDisplayStyle, newIsPublic, null);

        // Then
        assertEquals(originalGallery.getGalleryId(), updatedGallery.getGalleryId());
        assertEquals(newName, updatedGallery.getName());
        assertEquals(newDescription, updatedGallery.getDescription());
        assertEquals(newTheme, updatedGallery.getTheme());
        assertEquals(newDisplayStyle, updatedGallery.getDisplayStyle());
        assertEquals(newIsPublic, updatedGallery.isPublic());
        assertEquals(originalGallery.getCreateTime(), updatedGallery.getCreateTime());
        assertTrue(updatedGallery.getUpdateTime().isAfter(originalGallery.getUpdateTime()));
    }

    @Test
    @DisplayName("添加文物到收藏馆")
    void testAddRelics() {
        // Given
        CollectionGallery gallery = CollectionGallery.create(
            "测试收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);
        Long relicsId = 123L;

        // When
        CollectionGallery updatedGallery = gallery.addRelics(relicsId);

        // Then
        assertEquals(1, updatedGallery.getRelicsCount());
        assertTrue(updatedGallery.containsRelics(relicsId));
        assertTrue(updatedGallery.getRelicsIds().contains(relicsId));
    }

    @Test
    @DisplayName("添加文物到收藏馆 - 重复添加")
    void testAddRelics_Duplicate() {
        // Given
        CollectionGallery gallery = CollectionGallery.create(
            "测试收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);
        Long relicsId = 123L;
        CollectionGallery galleryWithRelics = gallery.addRelics(relicsId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            galleryWithRelics.addRelics(relicsId));
    }

    @Test
    @DisplayName("添加文物到收藏馆 - 无效ID")
    void testAddRelics_InvalidId() {
        // Given
        CollectionGallery gallery = CollectionGallery.create(
            "测试收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> gallery.addRelics(null));
        assertThrows(IllegalArgumentException.class, () -> gallery.addRelics(0L));
        assertThrows(IllegalArgumentException.class, () -> gallery.addRelics(-1L));
    }

    @Test
    @DisplayName("从收藏馆移除文物")
    void testRemoveRelics() {
        // Given
        CollectionGallery gallery = CollectionGallery.create(
            "测试收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);
        Long relicsId = 123L;
        CollectionGallery galleryWithRelics = gallery.addRelics(relicsId);

        // When
        CollectionGallery updatedGallery = galleryWithRelics.removeRelics(relicsId);

        // Then
        assertEquals(0, updatedGallery.getRelicsCount());
        assertFalse(updatedGallery.containsRelics(relicsId));
        assertFalse(updatedGallery.getRelicsIds().contains(relicsId));
    }

    @Test
    @DisplayName("从收藏馆移除文物 - 文物不存在")
    void testRemoveRelics_NotExists() {
        // Given
        CollectionGallery gallery = CollectionGallery.create(
            "测试收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);
        Long relicsId = 123L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            gallery.removeRelics(relicsId));
    }

    @Test
    @DisplayName("生成分享链接")
    void testGenerateShareUrl() {
        // Given
        CollectionGallery publicGallery = CollectionGallery.create(
            "公开收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);
        String baseUrl = "https://example.com";

        // When
        String shareUrl = publicGallery.generateShareUrl(baseUrl);

        // Then
        assertNotNull(shareUrl);
        assertTrue(shareUrl.startsWith(baseUrl));
        assertTrue(shareUrl.contains(publicGallery.getShareCode()));
    }

    @Test
    @DisplayName("生成分享链接 - 私有收藏馆")
    void testGenerateShareUrl_PrivateGallery() {
        // Given
        CollectionGallery privateGallery = CollectionGallery.create(
            "私有收藏馆", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, false);
        String baseUrl = "https://example.com";

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            privateGallery.generateShareUrl(baseUrl));
    }

    @Test
    @DisplayName("获取有效主题名称")
    void testGetEffectiveThemeName() {
        // Given - 预定义主题
        CollectionGallery bronzeGallery = CollectionGallery.create(
            "青铜器收藏", "测试描述", GalleryTheme.BRONZE, DisplayStyle.GRID, true);

        // When & Then
        assertEquals(GalleryTheme.BRONZE.getName(), bronzeGallery.getEffectiveThemeName());

        // Given - 自定义主题
        CollectionGallery customGallery = CollectionGallery.create(
            "自定义收藏", "测试描述", GalleryTheme.CUSTOM, DisplayStyle.GRID, true, "民国文物");

        // When & Then
        assertEquals("民国文物", customGallery.getEffectiveThemeName());
    }

    @Test
    @DisplayName("从数据库重建收藏馆")
    void testFromDatabase() {
        // Given
        GalleryId galleryId = GalleryId.generate();
        String name = "测试收藏馆";
        String description = "测试描述";
        GalleryTheme theme = GalleryTheme.BRONZE;
        DisplayStyle displayStyle = DisplayStyle.GRID;
        java.util.List<Long> relicsIds = Arrays.asList(1L, 2L, 3L);
        LocalDateTime createTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updateTime = LocalDateTime.now();
        boolean isPublic = true;
        String shareCode = "test123";
        String customThemeName = null;

        // When
        CollectionGallery gallery = CollectionGallery.fromDatabase(
            galleryId, name, description, theme, displayStyle, relicsIds,
            createTime, updateTime, isPublic, shareCode, customThemeName);

        // Then
        assertEquals(galleryId, gallery.getGalleryId());
        assertEquals(name, gallery.getName());
        assertEquals(description, gallery.getDescription());
        assertEquals(theme, gallery.getTheme());
        assertEquals(displayStyle, gallery.getDisplayStyle());
        assertEquals(3, gallery.getRelicsCount());
        assertEquals(createTime, gallery.getCreateTime());
        assertEquals(updateTime, gallery.getUpdateTime());
        assertEquals(isPublic, gallery.isPublic());
        assertEquals(shareCode, gallery.getShareCode());
        assertEquals(customThemeName, gallery.getCustomThemeName());
    }

    @Test
    @DisplayName("收藏馆相等性测试")
    void testEquality() {
        // Given
        GalleryId galleryId = GalleryId.generate();
        CollectionGallery gallery1 = CollectionGallery.fromDatabase(
            galleryId, "名称1", "描述1", GalleryTheme.BRONZE, DisplayStyle.GRID,
            Arrays.asList(1L), LocalDateTime.now(), LocalDateTime.now(), true, "code1", null);
        
        CollectionGallery gallery2 = CollectionGallery.fromDatabase(
            galleryId, "名称2", "描述2", GalleryTheme.PORCELAIN, DisplayStyle.LIST,
            Arrays.asList(2L), LocalDateTime.now(), LocalDateTime.now(), false, "code2", null);

        // When & Then
        assertEquals(gallery1, gallery2); // 相同ID的收藏馆应该相等
        assertEquals(gallery1.hashCode(), gallery2.hashCode());
    }
}
