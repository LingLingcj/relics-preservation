package com.ling.domain.interaction.service.impl;

import com.ling.domain.interaction.adapter.IGalleryManagerRepository;
import com.ling.domain.interaction.adapter.IUserCommentsRepository;
import com.ling.domain.interaction.adapter.IUserFavoritesRepository;
import com.ling.domain.interaction.adapter.IUserInteractionRepository;
import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.interaction.model.entity.UserComments;
import com.ling.domain.interaction.model.entity.UserFavorites;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.user.model.valobj.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserInteractionServiceImpl集成测试
 * @Author: LingRJ
 * @Description: 测试服务层协调三个聚合根的功能
 * @DateTime: 2025/7/13
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户交互服务集成测试")
class UserInteractionServiceImplTest {

    @Mock
    private IUserInteractionRepository userInteractionRepository;

    @Mock
    private IUserFavoritesRepository userFavoritesRepository;

    @Mock
    private IUserCommentsRepository userCommentsRepository;

    @Mock
    private IGalleryManagerRepository galleryManagerRepository;

    @InjectMocks
    private UserInteractionServiceImpl userInteractionService;

    private String testUsername;
    private Long testRelicsId;

    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        testRelicsId = 1L;
    }

    @Test
    @DisplayName("添加收藏成功 - 新用户")
    void testAddFavoriteSuccessNewUser() {
        // Given
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.empty());
        when(userFavoritesRepository.saveIncremental(any(UserFavorites.class)))
                .thenReturn(true);

        // When
        InteractionResult result = userInteractionService.addFavorite(testUsername, testRelicsId);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("收藏成功", result.getMessage());
        verify(userFavoritesRepository).findByUsername(Username.of(testUsername));
        verify(userFavoritesRepository).saveIncremental(any(UserFavorites.class));
    }

    @Test
    @DisplayName("添加收藏成功 - 现有用户")
    void testAddFavoriteSuccessExistingUser() {
        // Given
        UserFavorites existingFavorites = UserFavorites.create(Username.of(testUsername));
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.of(existingFavorites));
        when(userFavoritesRepository.saveIncremental(any(UserFavorites.class)))
                .thenReturn(true);

        // When
        InteractionResult result = userInteractionService.addFavorite(testUsername, testRelicsId);

        // Then
        assertTrue(result.isSuccess());
        verify(userFavoritesRepository).findByUsername(Username.of(testUsername));
        verify(userFavoritesRepository).saveIncremental(existingFavorites);
    }

    @Test
    @DisplayName("添加收藏失败 - 保存失败")
    void testAddFavoriteFailureSaveError() {
        // Given
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.empty());
        when(userFavoritesRepository.saveIncremental(any(UserFavorites.class)))
                .thenReturn(false);

        // When
        InteractionResult result = userInteractionService.addFavorite(testUsername, testRelicsId);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("保存失败", result.getMessage());
    }

    @Test
    @DisplayName("取消收藏成功")
    void testRemoveFavoriteSuccess() {
        // Given
        UserFavorites existingFavorites = UserFavorites.create(Username.of(testUsername));
        existingFavorites.addFavorite(testRelicsId); // 先添加收藏
        
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.of(existingFavorites));
        when(userFavoritesRepository.saveIncremental(any(UserFavorites.class)))
                .thenReturn(true);
        when(galleryManagerRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.of(GalleryManager.create(Username.of(testUsername))));
        when(galleryManagerRepository.saveIncremental(any(GalleryManager.class)))
                .thenReturn(true);

        // When
        InteractionResult result = userInteractionService.removeFavorite(testUsername, testRelicsId);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("取消收藏成功", result.getMessage());
        verify(userFavoritesRepository).saveIncremental(existingFavorites);
    }

    @Test
    @DisplayName("取消收藏失败 - 用户不存在")
    void testRemoveFavoriteFailureUserNotExists() {
        // Given
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.empty());

        // When
        InteractionResult result = userInteractionService.removeFavorite(testUsername, testRelicsId);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("用户收藏记录不存在", result.getMessage());
    }

    @Test
    @DisplayName("检查收藏状态 - 已收藏")
    void testIsFavoritedTrue() {
        // Given
        UserFavorites existingFavorites = UserFavorites.create(Username.of(testUsername));
        existingFavorites.addFavorite(testRelicsId);
        
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.of(existingFavorites));

        // When
        boolean result = userInteractionService.isFavorited(testUsername, testRelicsId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查收藏状态 - 未收藏")
    void testIsFavoritedFalse() {
        // Given
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.empty());

        // When
        boolean result = userInteractionService.isFavorited(testUsername, testRelicsId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("添加评论成功 - 新用户")
    void testAddCommentSuccessNewUser() {
        // Given
        String content = "这是一个很棒的文物！";
        when(userCommentsRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.empty());
        when(userCommentsRepository.saveIncremental(any(UserComments.class)))
                .thenReturn(true);

        // When
        InteractionResult result = userInteractionService.addComment(testUsername, testRelicsId, content);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("评论成功", result.getMessage());
        verify(userCommentsRepository).findByUsername(Username.of(testUsername));
    }

    @Test
    @DisplayName("添加评论成功 - 现有用户")
    void testAddCommentSuccessExistingUser() {
        // Given
        String content = "这是一个很棒的文物！";
        UserComments existingComments = UserComments.create(Username.of(testUsername));
        when(userCommentsRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.of(existingComments));
        when(userCommentsRepository.saveIncremental(any(UserComments.class)))
                .thenReturn(true);

        // When
        InteractionResult result = userInteractionService.addComment(testUsername, testRelicsId, content);

        // Then
        assertTrue(result.isSuccess());
        verify(userCommentsRepository).findByUsername(Username.of(testUsername));
        verify(userCommentsRepository).saveIncremental(existingComments);
    }

    @Test
    @DisplayName("删除评论成功")
    void testDeleteCommentSuccess() {
        // Given
        Long commentId = 1L;
        UserComments existingComments = UserComments.create(Username.of(testUsername));
        existingComments.addComment(testRelicsId, "测试评论");
        
        when(userCommentsRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.of(existingComments));
        when(userCommentsRepository.saveIncremental(any(UserComments.class)))
                .thenReturn(true);

        // When
        InteractionResult result = userInteractionService.deleteComment(testUsername, commentId);

        // Then
        assertTrue(result.isSuccess());
        verify(userCommentsRepository).saveIncremental(existingComments);
    }

    @Test
    @DisplayName("删除评论失败 - 用户不存在")
    void testDeleteCommentFailureUserNotExists() {
        // Given
        Long commentId = 1L;
        when(userCommentsRepository.findByUsername(any(Username.class)))
                .thenReturn(Optional.empty());

        // When
        InteractionResult result = userInteractionService.deleteComment(testUsername, commentId);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("用户评论记录不存在", result.getMessage());
    }

    @Test
    @DisplayName("服务层异常处理")
    void testServiceExceptionHandling() {
        // Given
        when(userFavoritesRepository.findByUsername(any(Username.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When
        InteractionResult result = userInteractionService.addFavorite(testUsername, testRelicsId);

        // Then
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("添加收藏失败"));
    }

}
