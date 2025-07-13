package com.ling.domain.interaction.model.entity;

import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.user.model.valobj.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserComments聚合根单元测试
 * @Author: LingRJ
 * @Description: 测试用户评论聚合根的业务逻辑
 * @DateTime: 2025/7/13
 */
@DisplayName("用户评论聚合根测试")
class UserCommentsTest {

    private UserComments userComments;
    private Username testUsername;

    @BeforeEach
    void setUp() {
        testUsername = Username.of("testuser");
        userComments = UserComments.create(testUsername);
    }

    @Test
    @DisplayName("创建用户评论聚合根")
    void testCreate() {
        // Given & When
        UserComments comments = UserComments.create(testUsername);

        // Then
        assertNotNull(comments);
        assertEquals(testUsername, comments.getUsername());
        assertNotNull(comments.getComments());
        assertTrue(comments.getComments(null).isEmpty());
        assertNotNull(comments.getCreateTime());
        assertNotNull(comments.getUpdateTime());
        assertNotNull(comments.getChangeTracker());
        assertFalse(comments.hasChanges());
    }

    @Test
    @DisplayName("从数据库重建用户评论聚合根")
    void testFromDatabase() {
        // Given
        LocalDateTime createTime = LocalDateTime.now().minusDays(1);
        LocalDateTime updateTime = LocalDateTime.now();

        // When
        UserComments comments = UserComments.fromDatabase(
                testUsername, new ArrayList<>(), createTime, updateTime);

        // Then
        assertNotNull(comments);
        assertEquals(testUsername, comments.getUsername());
        assertEquals(createTime, comments.getCreateTime());
        assertEquals(updateTime, comments.getUpdateTime());
        assertFalse(comments.hasChanges());
    }

    @Test
    @DisplayName("添加评论成功")
    void testAddCommentSuccess() {
        // Given
        Long relicsId = 1L;
        String content = "这是一个很棒的文物！";

        // When
        InteractionResult result = userComments.addComment(relicsId, content);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("评论成功", result.getMessage());
        assertNotNull(result.getData());
        assertTrue(userComments.hasChanges());
        assertEquals(1, userComments.getComments(null).size());
        assertEquals(1, userComments.getComments(relicsId).size());
    }

    @Test
    @DisplayName("添加空评论失败")
    void testAddEmptyCommentFailure() {
        // Given
        Long relicsId = 1L;
        String emptyContent = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userComments.addComment(relicsId, emptyContent);
        });
    }

    @Test
    @DisplayName("删除评论成功")
    void testDeleteCommentSuccess() {
        // Given
        Long relicsId = 1L;
        String content = "测试评论";
        InteractionResult addResult = userComments.addComment(relicsId, content);
        Long commentId = addResult.getData(Long.class);
        userComments.clearChanges(); // 清空变更记录

        // When
        InteractionResult result = userComments.deleteComment(commentId);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("删除评论成功", result.getMessage());
        assertTrue(userComments.hasChanges());
        assertEquals(0, userComments.getComments(null).size());
        assertEquals(0, userComments.getComments(relicsId).size());
    }

    @Test
    @DisplayName("删除不存在的评论失败")
    void testDeleteNonExistentComment() {
        // Given
        Long nonExistentCommentId = 999L;

        // When
        InteractionResult result = userComments.deleteComment(nonExistentCommentId);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("评论不存在", result.getMessage());
        assertFalse(userComments.hasChanges());
    }

    @Test
    @DisplayName("获取指定文物的评论")
    void testGetCommentsByRelicsId() {
        // Given
        Long relicsId1 = 1L;
        Long relicsId2 = 2L;
        userComments.addComment(relicsId1, "文物1的评论1");
        userComments.addComment(relicsId1, "文物1的评论2");
        userComments.addComment(relicsId2, "文物2的评论1");

        // When
        List<CommentAction> relics1Comments = userComments.getComments(relicsId1);
        List<CommentAction> relics2Comments = userComments.getComments(relicsId2);
        List<CommentAction> allComments = userComments.getComments(null);

        // Then
        assertEquals(2, relics1Comments.size());
        assertEquals(1, relics2Comments.size());
        assertEquals(3, allComments.size());
    }

    @Test
    @DisplayName("评论按时间倒序排列")
    void testCommentsOrderedByTimeDesc() throws InterruptedException {
        // Given
        Long relicsId = 1L;
        userComments.addComment(relicsId, "第一条评论");
        Thread.sleep(10); // 确保时间差异
        userComments.addComment(relicsId, "第二条评论");
        Thread.sleep(10);
        userComments.addComment(relicsId, "第三条评论");

        // When
        List<CommentAction> comments = userComments.getComments(relicsId);

        // Then
        assertEquals(3, comments.size());
        // 最新的评论应该在前面
        assertTrue(comments.get(0).getCreateTime().isAfter(comments.get(1).getCreateTime()));
        assertTrue(comments.get(1).getCreateTime().isAfter(comments.get(2).getCreateTime()));
    }

    @Test
    @DisplayName("获取评论统计信息")
    void testGetStatistics() {
        // Given
        userComments.addComment(1L, "评论1");
        userComments.addComment(2L, "评论2");

        // When
        UserComments.CommentStatistics stats = userComments.getStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(testUsername.getValue(), stats.getUsername());
        assertEquals(2L, stats.getTotalComments());
        assertEquals(2L, stats.getApprovedComments()); // 新评论默认通过审核
        assertEquals(0L, stats.getPendingComments());
        assertNotNull(stats.getLastCommentTime());
    }

    @Test
    @DisplayName("变更跟踪功能")
    void testChangeTracking() {
        // Given
        Long relicsId = 1L;
        String content = "测试评论";

        // When
        assertFalse(userComments.hasChanges());
        
        InteractionResult addResult = userComments.addComment(relicsId, content);
        assertTrue(userComments.hasChanges());
        assertEquals(1, userComments.getCommentChanges().size());
        
        userComments.clearChanges();
        assertFalse(userComments.hasChanges());
        assertEquals(0, userComments.getCommentChanges().size());

        // 删除评论
        Long commentId = addResult.getData(Long.class);
        userComments.deleteComment(commentId);
        assertTrue(userComments.hasChanges());
        assertEquals(1, userComments.getCommentChanges().size());
    }

    @Test
    @DisplayName("获取变更统计信息")
    void testGetChangesSummary() {
        // Given & When
        String summary1 = userComments.getChangesSummary();
        assertEquals("无变更", summary1);

        userComments.addComment(1L, "评论1");
        userComments.addComment(2L, "评论2");
        String summary2 = userComments.getChangesSummary();
        assertEquals("评论变更: 2", summary2);
    }

    @Test
    @DisplayName("获取显示名称")
    void testGetDisplayName() {
        // When & Then
        assertEquals(testUsername.getValue(), userComments.getDisplayName());
    }

    @Test
    @DisplayName("批量评论操作")
    void testBatchCommentOperations() {
        // Given
        Long relicsId = 1L;
        List<String> contents = List.of("评论1", "评论2", "评论3", "评论4", "评论5");

        // When - 批量添加评论
        for (String content : contents) {
            InteractionResult result = userComments.addComment(relicsId, content);
            assertTrue(result.isSuccess());
        }

        // Then
        assertEquals(5, userComments.getComments(relicsId).size());
        assertEquals(5, userComments.getComments(null).size());
    }

    @Test
    @DisplayName("评论内容验证")
    void testCommentContentValidation() {
        // Given
        Long relicsId = 1L;

        // When & Then - 测试各种内容
        assertDoesNotThrow(() -> {
            userComments.addComment(relicsId, "正常评论内容");
        });

        assertDoesNotThrow(() -> {
            userComments.addComment(relicsId, "包含特殊字符的评论：！@#￥%……&*（）");
        });

        assertDoesNotThrow(() -> {
            userComments.addComment(relicsId, "很长的评论内容".repeat(10));
        });
    }
}
