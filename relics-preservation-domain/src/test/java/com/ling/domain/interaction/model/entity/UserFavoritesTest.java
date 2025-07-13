package com.ling.domain.interaction.model.entity;

import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.user.model.valobj.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserFavorites聚合根单元测试
 * @Author: LingRJ
 * @Description: 测试用户收藏聚合根的业务逻辑
 * @DateTime: 2025/7/13
 */
@DisplayName("用户收藏聚合根测试")
class UserFavoritesTest {

    private UserFavorites userFavorites;
    private Username testUsername;

    @BeforeEach
    void setUp() {
        testUsername = Username.of("testuser");
        userFavorites = UserFavorites.create(testUsername);
    }

    @Test
    @DisplayName("创建用户收藏聚合根")
    void testCreate() {
        // Given & When
        UserFavorites favorites = UserFavorites.create(testUsername);

        // Then
        assertNotNull(favorites);
        assertEquals(testUsername, favorites.getUsername());
        assertNotNull(favorites.getFavorites());
        assertTrue(favorites.getFavorites().isEmpty());
        assertNotNull(favorites.getCreateTime());
        assertNotNull(favorites.getUpdateTime());
        assertNotNull(favorites.getChangeTracker());
        assertFalse(favorites.hasChanges());
    }

    @Test
    @DisplayName("从数据库重建用户收藏聚合根")
    void testFromDatabase() {
        // Given
        LocalDateTime createTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updateTime = LocalDateTime.now();

        // When
        UserFavorites favorites = UserFavorites.fromDatabase(
                testUsername, new HashSet<>(), createTime, updateTime);

        // Then
        assertNotNull(favorites);
        assertEquals(testUsername, favorites.getUsername());
        assertEquals(createTime, favorites.getCreateTime());
        assertEquals(updateTime, favorites.getUpdateTime());
        assertFalse(favorites.hasChanges());
    }

    @Test
    @DisplayName("添加收藏成功")
    void testAddFavoriteSuccess() {
        // Given
        Long relicsId = 1L;

        // When
        InteractionResult result = userFavorites.addFavorite(relicsId);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("收藏成功", result.getMessage());
        assertTrue(userFavorites.isFavorited(relicsId));
        assertTrue(userFavorites.hasChanges());
        assertEquals(1, userFavorites.getFavoritedRelicsIds().size());
        assertTrue(userFavorites.getFavoritedRelicsIds().contains(relicsId));
    }

    @Test
    @DisplayName("重复添加收藏失败")
    void testAddFavoriteDuplicate() {
        // Given
        Long relicsId = 1L;
        userFavorites.addFavorite(relicsId);

        // When
        InteractionResult result = userFavorites.addFavorite(relicsId);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("已经收藏过该文物", result.getMessage());
        assertEquals(1, userFavorites.getFavoritedRelicsIds().size());
    }

    @Test
    @DisplayName("取消收藏成功")
    void testRemoveFavoriteSuccess() {
        // Given
        Long relicsId = 1L;
        userFavorites.addFavorite(relicsId);
        userFavorites.clearChanges(); // 清空变更记录

        // When
        InteractionResult result = userFavorites.removeFavorite(relicsId);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("取消收藏成功", result.getMessage());
        assertFalse(userFavorites.isFavorited(relicsId));
        assertTrue(userFavorites.hasChanges());
        assertEquals(0, userFavorites.getFavoritedRelicsIds().size());
    }

    @Test
    @DisplayName("取消未收藏的文物失败")
    void testRemoveFavoriteNotExists() {
        // Given
        Long relicsId = 1L;

        // When
        InteractionResult result = userFavorites.removeFavorite(relicsId);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("未收藏该文物", result.getMessage());
        assertFalse(userFavorites.hasChanges());
    }

    @Test
    @DisplayName("检查收藏状态")
    void testIsFavorited() {
        // Given
        Long relicsId1 = 1L;
        Long relicsId2 = 2L;
        userFavorites.addFavorite(relicsId1);

        // When & Then
        assertTrue(userFavorites.isFavorited(relicsId1));
        assertFalse(userFavorites.isFavorited(relicsId2));
    }

    @Test
    @DisplayName("获取收藏文物ID列表")
    void testGetFavoritedRelicsIds() {
        // Given
        Long relicsId1 = 1L;
        Long relicsId2 = 2L;
        Long relicsId3 = 3L;
        
        userFavorites.addFavorite(relicsId3);
        userFavorites.addFavorite(relicsId1);
        userFavorites.addFavorite(relicsId2);

        // When
        List<Long> favoritedIds = userFavorites.getFavoritedRelicsIds();

        // Then
        assertEquals(3, favoritedIds.size());
        // 应该按ID排序
        assertEquals(List.of(1L, 2L, 3L), favoritedIds);
    }

    @Test
    @DisplayName("获取收藏统计信息")
    void testGetStatistics() {
        // Given
        userFavorites.addFavorite(1L);
        userFavorites.addFavorite(2L);

        // When
        UserFavorites.FavoriteStatistics stats = userFavorites.getStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(testUsername.getValue(), stats.getUsername());
        assertEquals(2L, stats.getTotalFavorites());
        assertEquals(2L, stats.getNewFavorites()); // 都是新收藏
        assertNotNull(stats.getLastFavoriteTime());
    }

    @Test
    @DisplayName("变更跟踪功能")
    void testChangeTracking() {
        // Given
        Long relicsId = 1L;

        // When
        assertFalse(userFavorites.hasChanges());
        
        userFavorites.addFavorite(relicsId);
        assertTrue(userFavorites.hasChanges());
        assertEquals(1, userFavorites.getFavoriteChanges().size());
        
        userFavorites.clearChanges();
        assertFalse(userFavorites.hasChanges());
        assertEquals(0, userFavorites.getFavoriteChanges().size());
    }

    @Test
    @DisplayName("获取变更统计信息")
    void testGetChangesSummary() {
        // Given & When
        String summary1 = userFavorites.getChangesSummary();
        assertEquals("无变更", summary1);

        userFavorites.addFavorite(1L);
        userFavorites.addFavorite(2L);
        String summary2 = userFavorites.getChangesSummary();
        assertEquals("收藏变更: 2", summary2);
    }

    @Test
    @DisplayName("获取显示名称")
    void testGetDisplayName() {
        // When & Then
        assertEquals(testUsername.getValue(), userFavorites.getDisplayName());
    }

    @Test
    @DisplayName("删除收藏后不在列表中显示")
    void testDeletedFavoriteNotInList() {
        // Given
        Long relicsId = 1L;
        userFavorites.addFavorite(relicsId);
        assertEquals(1, userFavorites.getFavoritedRelicsIds().size());

        // When
        userFavorites.removeFavorite(relicsId);

        // Then
        assertEquals(0, userFavorites.getFavoritedRelicsIds().size());
        assertFalse(userFavorites.isFavorited(relicsId));
    }

    @Test
    @DisplayName("批量收藏操作")
    void testBatchFavoriteOperations() {
        // Given
        List<Long> relicsIds = List.of(1L, 2L, 3L, 4L, 5L);

        // When - 批量添加收藏
        for (Long relicsId : relicsIds) {
            InteractionResult result = userFavorites.addFavorite(relicsId);
            assertTrue(result.isSuccess());
        }

        // Then
        assertEquals(5, userFavorites.getFavoritedRelicsIds().size());
        for (Long relicsId : relicsIds) {
            assertTrue(userFavorites.isFavorited(relicsId));
        }

        // When - 批量删除部分收藏
        userFavorites.removeFavorite(1L);
        userFavorites.removeFavorite(3L);
        userFavorites.removeFavorite(5L);

        // Then
        assertEquals(2, userFavorites.getFavoritedRelicsIds().size());
        assertFalse(userFavorites.isFavorited(1L));
        assertTrue(userFavorites.isFavorited(2L));
        assertFalse(userFavorites.isFavorited(3L));
        assertTrue(userFavorites.isFavorited(4L));
        assertFalse(userFavorites.isFavorited(5L));
    }

    @Test
    @DisplayName("并发收藏操作安全性")
    void testConcurrentFavoriteOperations() {
        // Given
        Long relicsId = 1L;

        // When - 模拟并发添加收藏
        InteractionResult result1 = userFavorites.addFavorite(relicsId);
        InteractionResult result2 = userFavorites.addFavorite(relicsId);

        // Then - 第一次成功，第二次失败
        assertTrue(result1.isSuccess());
        assertFalse(result2.isSuccess());
        assertEquals("已经收藏过该文物", result2.getMessage());
        assertEquals(1, userFavorites.getFavoritedRelicsIds().size());
    }

    @Test
    @DisplayName("收藏状态一致性验证")
    void testFavoriteStateConsistency() {
        // Given
        Long relicsId = 1L;

        // When & Then - 初始状态
        assertFalse(userFavorites.isFavorited(relicsId));
        assertEquals(0, userFavorites.getFavoritedRelicsIds().size());

        // When & Then - 添加收藏后
        userFavorites.addFavorite(relicsId);
        assertTrue(userFavorites.isFavorited(relicsId));
        assertEquals(1, userFavorites.getFavoritedRelicsIds().size());
        assertTrue(userFavorites.getFavoritedRelicsIds().contains(relicsId));

        // When & Then - 删除收藏后
        userFavorites.removeFavorite(relicsId);
        assertFalse(userFavorites.isFavorited(relicsId));
        assertEquals(0, userFavorites.getFavoritedRelicsIds().size());
        assertFalse(userFavorites.getFavoritedRelicsIds().contains(relicsId));
    }
}
