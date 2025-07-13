package com.ling.domain.interaction.model.valobj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GalleryTheme枚举单元测试
 * @Author: LingRJ
 * @Description: 测试收藏馆主题枚举的所有功能
 * @DateTime: 2025/7/13
 */
@DisplayName("收藏馆主题枚举测试")
class GalleryThemeTest {

    @Test
    @DisplayName("根据代码获取主题")
    void testFromCode() {
        // When & Then
        assertEquals(GalleryTheme.BRONZE, GalleryTheme.fromCode("bronze"));
        assertEquals(GalleryTheme.PORCELAIN, GalleryTheme.fromCode("porcelain"));
        assertEquals(GalleryTheme.PAINTING, GalleryTheme.fromCode("painting"));
        assertEquals(GalleryTheme.JADE, GalleryTheme.fromCode("jade"));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromCode("custom"));
        
        // 测试大小写不敏感
        assertEquals(GalleryTheme.BRONZE, GalleryTheme.fromCode("BRONZE"));
        assertEquals(GalleryTheme.BRONZE, GalleryTheme.fromCode("Bronze"));
        
        // 测试无效代码返回CUSTOM
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromCode("invalid"));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromCode(null));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromCode(""));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromCode("   "));
    }

    @Test
    @DisplayName("根据名称获取主题")
    void testFromName() {
        // When & Then
        assertEquals(GalleryTheme.BRONZE, GalleryTheme.fromName("青铜器"));
        assertEquals(GalleryTheme.PORCELAIN, GalleryTheme.fromName("瓷器"));
        assertEquals(GalleryTheme.PAINTING, GalleryTheme.fromName("书画"));
        assertEquals(GalleryTheme.JADE, GalleryTheme.fromName("玉器"));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromName("自定义"));
        
        // 测试无效名称返回CUSTOM
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromName("无效名称"));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromName(null));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromName(""));
        assertEquals(GalleryTheme.CUSTOM, GalleryTheme.fromName("   "));
    }

    @Test
    @DisplayName("验证主题代码是否有效")
    void testIsValidCode() {
        // When & Then
        assertTrue(GalleryTheme.isValidCode("bronze"));
        assertTrue(GalleryTheme.isValidCode("porcelain"));
        assertTrue(GalleryTheme.isValidCode("painting"));
        assertTrue(GalleryTheme.isValidCode("jade"));
        assertTrue(GalleryTheme.isValidCode("custom"));
        
        // 测试大小写不敏感
        assertTrue(GalleryTheme.isValidCode("BRONZE"));
        assertTrue(GalleryTheme.isValidCode("Bronze"));
        
        // 测试无效代码
        assertFalse(GalleryTheme.isValidCode("invalid"));
        assertFalse(GalleryTheme.isValidCode(null));
        assertFalse(GalleryTheme.isValidCode(""));
        assertFalse(GalleryTheme.isValidCode("   "));
    }

    @Test
    @DisplayName("测试所有主题的属性")
    void testThemeProperties() {
        // BRONZE
        assertEquals("bronze", GalleryTheme.BRONZE.getCode());
        assertEquals("青铜器", GalleryTheme.BRONZE.getName());
        assertEquals("古代青铜制品收藏", GalleryTheme.BRONZE.getDescription());
        
        // PORCELAIN
        assertEquals("porcelain", GalleryTheme.PORCELAIN.getCode());
        assertEquals("瓷器", GalleryTheme.PORCELAIN.getName());
        assertEquals("各朝代瓷器收藏", GalleryTheme.PORCELAIN.getDescription());
        
        // PAINTING
        assertEquals("painting", GalleryTheme.PAINTING.getCode());
        assertEquals("书画", GalleryTheme.PAINTING.getName());
        assertEquals("书法绘画作品收藏", GalleryTheme.PAINTING.getDescription());
        
        // JADE
        assertEquals("jade", GalleryTheme.JADE.getCode());
        assertEquals("玉器", GalleryTheme.JADE.getName());
        assertEquals("玉石制品收藏", GalleryTheme.JADE.getDescription());
        
        // CALLIGRAPHY
        assertEquals("calligraphy", GalleryTheme.CALLIGRAPHY.getCode());
        assertEquals("书法", GalleryTheme.CALLIGRAPHY.getName());
        assertEquals("书法作品收藏", GalleryTheme.CALLIGRAPHY.getDescription());
        
        // SCULPTURE
        assertEquals("sculpture", GalleryTheme.SCULPTURE.getCode());
        assertEquals("雕塑", GalleryTheme.SCULPTURE.getName());
        assertEquals("雕塑艺术品收藏", GalleryTheme.SCULPTURE.getDescription());
        
        // FURNITURE
        assertEquals("furniture", GalleryTheme.FURNITURE.getCode());
        assertEquals("家具", GalleryTheme.FURNITURE.getName());
        assertEquals("古典家具收藏", GalleryTheme.FURNITURE.getDescription());
        
        // TEXTILE
        assertEquals("textile", GalleryTheme.TEXTILE.getCode());
        assertEquals("织物", GalleryTheme.TEXTILE.getName());
        assertEquals("古代织物收藏", GalleryTheme.TEXTILE.getDescription());
        
        // COIN
        assertEquals("coin", GalleryTheme.COIN.getCode());
        assertEquals("钱币", GalleryTheme.COIN.getName());
        assertEquals("古代钱币收藏", GalleryTheme.COIN.getDescription());
        
        // WEAPON
        assertEquals("weapon", GalleryTheme.WEAPON.getCode());
        assertEquals("兵器", GalleryTheme.WEAPON.getName());
        assertEquals("古代兵器收藏", GalleryTheme.WEAPON.getDescription());
        
        // ORNAMENT
        assertEquals("ornament", GalleryTheme.ORNAMENT.getCode());
        assertEquals("饰品", GalleryTheme.ORNAMENT.getName());
        assertEquals("古代饰品收藏", GalleryTheme.ORNAMENT.getDescription());
        
        // INSTRUMENT
        assertEquals("instrument", GalleryTheme.INSTRUMENT.getCode());
        assertEquals("乐器", GalleryTheme.INSTRUMENT.getName());
        assertEquals("古代乐器收藏", GalleryTheme.INSTRUMENT.getDescription());
        
        // CUSTOM
        assertEquals("custom", GalleryTheme.CUSTOM.getCode());
        assertEquals("自定义", GalleryTheme.CUSTOM.getName());
        assertEquals("用户自定义主题", GalleryTheme.CUSTOM.getDescription());
    }

    @Test
    @DisplayName("toString方法测试")
    void testToString() {
        // When & Then
        assertEquals("青铜器(bronze)", GalleryTheme.BRONZE.toString());
        assertEquals("瓷器(porcelain)", GalleryTheme.PORCELAIN.toString());
        assertEquals("自定义(custom)", GalleryTheme.CUSTOM.toString());
    }

    @Test
    @DisplayName("测试所有枚举值")
    void testAllValues() {
        // When
        GalleryTheme[] themes = GalleryTheme.values();

        // Then
        assertEquals(13, themes.length);
        
        // 验证包含所有预期的主题
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.BRONZE));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.PORCELAIN));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.PAINTING));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.JADE));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.CALLIGRAPHY));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.SCULPTURE));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.FURNITURE));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.TEXTILE));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.COIN));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.WEAPON));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.ORNAMENT));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.INSTRUMENT));
        assertTrue(java.util.Arrays.asList(themes).contains(GalleryTheme.CUSTOM));
    }
}
