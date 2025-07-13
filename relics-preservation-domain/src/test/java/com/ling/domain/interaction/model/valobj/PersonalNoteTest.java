package com.ling.domain.interaction.model.valobj;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PersonalNote值对象单元测试
 * @Author: LingRJ
 * @Description: 测试个人笔记值对象的所有功能
 * @DateTime: 2025/7/13
 */
@DisplayName("个人笔记值对象测试")
class PersonalNoteTest {
    
    private Long testRelicsId;
    private String testTitle;
    private String testContent;
    private PersonalNote.NoteType testNoteType;
    
    @BeforeEach
    void setUp() {
        testRelicsId = 1001L;
        testTitle = "测试笔记标题";
        testContent = "这是一个测试笔记的内容，包含了对文物的详细描述和个人理解。";
        testNoteType = PersonalNote.NoteType.GENERAL;
    }
    
    @Test
    @DisplayName("创建个人笔记 - 成功")
    void testCreatePersonalNoteSuccess() {
        // When
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        
        // Then
        assertNotNull(note);
        assertNotNull(note.getNoteId());
        assertEquals(testRelicsId, note.getRelicsId());
        assertEquals(testTitle, note.getTitle());
        assertEquals(testContent, note.getContent());
        assertEquals(testNoteType, note.getNoteType());
        assertEquals(PersonalNote.LearningStatus.LEARNING, note.getStatus());
        assertEquals(0, note.getRating());
        assertFalse(note.isFavorite());
        assertNotNull(note.getCreateTime());
        assertNotNull(note.getUpdateTime());
        assertTrue(note.getTags().isEmpty());
        assertTrue(note.getKeyPoints().isEmpty());
    }
    
    @Test
    @DisplayName("创建个人笔记 - 参数验证")
    void testCreatePersonalNoteValidation() {
        // 文物ID为null
        assertThrows(IllegalArgumentException.class, () -> 
            PersonalNote.create(null, testTitle, testContent, testNoteType));
        
        // 标题为null
        assertThrows(IllegalArgumentException.class, () -> 
            PersonalNote.create(testRelicsId, null, testContent, testNoteType));
        
        // 标题为空字符串
        assertThrows(IllegalArgumentException.class, () -> 
            PersonalNote.create(testRelicsId, "", testContent, testNoteType));
        
        // 标题为空白字符串
        assertThrows(IllegalArgumentException.class, () -> 
            PersonalNote.create(testRelicsId, "   ", testContent, testNoteType));
        
        // 内容为null
        assertThrows(IllegalArgumentException.class, () -> 
            PersonalNote.create(testRelicsId, testTitle, null, testNoteType));
        
        // 内容为空字符串
        assertThrows(IllegalArgumentException.class, () -> 
            PersonalNote.create(testRelicsId, testTitle, "", testNoteType));
    }
    
    @Test
    @DisplayName("创建个人笔记 - 默认笔记类型")
    void testCreatePersonalNoteWithDefaultType() {
        // When
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, null);
        
        // Then
        assertEquals(PersonalNote.NoteType.GENERAL, note.getNoteType());
    }
    
    @Test
    @DisplayName("从数据库重建个人笔记")
    void testFromDatabase() {
        // Given
        String noteId = "note_123456";
        List<String> tags = Arrays.asList("青铜器", "西周");
        List<String> keyPoints = Arrays.asList("制作工艺", "历史背景");
        LocalDateTime createTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updateTime = LocalDateTime.of(2025, 1, 2, 15, 30);
        
        // When
        PersonalNote note = PersonalNote.fromDatabase(
            noteId, testRelicsId, testTitle, testContent, tags, keyPoints,
            4, PersonalNote.LearningStatus.MASTERED, createTime, updateTime,
            true, PersonalNote.NoteType.RESEARCH
        );
        
        // Then
        assertEquals(noteId, note.getNoteId());
        assertEquals(testRelicsId, note.getRelicsId());
        assertEquals(testTitle, note.getTitle());
        assertEquals(testContent, note.getContent());
        assertEquals(tags, note.getTags());
        assertEquals(keyPoints, note.getKeyPoints());
        assertEquals(4, note.getRating());
        assertEquals(PersonalNote.LearningStatus.MASTERED, note.getStatus());
        assertEquals(createTime, note.getCreateTime());
        assertEquals(updateTime, note.getUpdateTime());
        assertTrue(note.isFavorite());
        assertEquals(PersonalNote.NoteType.RESEARCH, note.getNoteType());
    }
    
    @Test
    @DisplayName("更新笔记内容")
    void testUpdateContent() {
        // Given
        PersonalNote originalNote = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        String newTitle = "更新后的标题";
        String newContent = "更新后的内容";
        
        // When
        PersonalNote updatedNote = originalNote.updateContent(newTitle, newContent);
        
        // Then
        assertNotEquals(originalNote, updatedNote); // 不可变对象
        assertEquals(originalNote.getNoteId(), updatedNote.getNoteId());
        assertEquals(newTitle, updatedNote.getTitle());
        assertEquals(newContent, updatedNote.getContent());
        assertTrue(updatedNote.getUpdateTime().isAfter(originalNote.getUpdateTime()));
    }
    
    @Test
    @DisplayName("添加标签")
    void testAddTag() {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        String tag = "青铜器";
        
        // When
        PersonalNote updatedNote = note.addTag(tag);
        
        // Then
        assertTrue(updatedNote.getTags().contains(tag));
        assertEquals(1, updatedNote.getTags().size());
        assertTrue(updatedNote.hasTag(tag));
    }
    
    @Test
    @DisplayName("添加重复标签")
    void testAddDuplicateTag() {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        String tag = "青铜器";
        
        // When
        PersonalNote noteWithTag = note.addTag(tag);
        PersonalNote noteWithDuplicateTag = noteWithTag.addTag(tag);
        
        // Then
        assertEquals(1, noteWithDuplicateTag.getTags().size());
        assertEquals(noteWithTag, noteWithDuplicateTag); // 应该返回相同对象
    }
    
    @Test
    @DisplayName("移除标签")
    void testRemoveTag() {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        String tag = "青铜器";
        PersonalNote noteWithTag = note.addTag(tag);
        
        // When
        PersonalNote noteWithoutTag = noteWithTag.removeTag(tag);
        
        // Then
        assertFalse(noteWithoutTag.getTags().contains(tag));
        assertEquals(0, noteWithoutTag.getTags().size());
        assertFalse(noteWithoutTag.hasTag(tag));
    }
    
    @ParameterizedTest
    @EnumSource(PersonalNote.LearningStatus.class)
    @DisplayName("更新学习状态")
    void testUpdateLearningStatus(PersonalNote.LearningStatus status) {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        
        // When
        PersonalNote updatedNote = note.updateLearningStatus(status);
        
        // Then
        assertEquals(status, updatedNote.getStatus());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("设置有效评分")
    void testSetValidRating(int rating) {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        
        // When
        PersonalNote ratedNote = note.setRating(rating);
        
        // Then
        assertEquals(rating, ratedNote.getRating());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    @DisplayName("设置无效评分")
    void testSetInvalidRating(int rating) {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> note.setRating(rating));
    }
    
    @Test
    @DisplayName("切换收藏状态")
    void testToggleFavorite() {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        assertFalse(note.isFavorite());
        
        // When - 第一次切换
        PersonalNote favoritedNote = note.toggleFavorite();
        
        // Then
        assertTrue(favoritedNote.isFavorite());
        
        // When - 第二次切换
        PersonalNote unfavoritedNote = favoritedNote.toggleFavorite();
        
        // Then
        assertFalse(unfavoritedNote.isFavorite());
    }
    
    @Test
    @DisplayName("获取笔记摘要")
    void testGetSummary() {
        // Given - 短内容
        String shortContent = "这是一个短内容";
        PersonalNote shortNote = PersonalNote.create(testRelicsId, testTitle, shortContent, testNoteType);
        
        // When & Then
        assertEquals(shortContent, shortNote.getSummary());
        
        // Given - 长内容
        String longContent = "这是一个很长的内容".repeat(20); // 超过100个字符
        PersonalNote longNote = PersonalNote.create(testRelicsId, testTitle, longContent, testNoteType);
        
        // When & Then
        String summary = longNote.getSummary();
        assertEquals(103, summary.length()); // 100个字符 + "..."
        assertTrue(summary.endsWith("..."));
    }
    
    @Test
    @DisplayName("获取学习进度")
    void testGetLearningProgress() {
        // Given & When & Then
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        
        assertEquals(0, note.updateLearningStatus(PersonalNote.LearningStatus.NOT_STARTED).getLearningProgress());
        assertEquals(25, note.updateLearningStatus(PersonalNote.LearningStatus.LEARNING).getLearningProgress());
        assertEquals(50, note.updateLearningStatus(PersonalNote.LearningStatus.REVIEWED).getLearningProgress());
        assertEquals(80, note.updateLearningStatus(PersonalNote.LearningStatus.MASTERED).getLearningProgress());
        assertEquals(100, note.updateLearningStatus(PersonalNote.LearningStatus.EXPERT).getLearningProgress());
    }
    
    @Test
    @DisplayName("笔记类型枚举测试")
    void testNoteTypeEnum() {
        // 验证所有笔记类型都有名称和描述
        for (PersonalNote.NoteType type : PersonalNote.NoteType.values()) {
            assertNotNull(type.getName());
            assertNotNull(type.getDescription());
            assertFalse(type.getName().isEmpty());
            assertFalse(type.getDescription().isEmpty());
        }
    }
    
    @Test
    @DisplayName("学习状态枚举测试")
    void testLearningStatusEnum() {
        // 验证所有学习状态都有名称和描述
        for (PersonalNote.LearningStatus status : PersonalNote.LearningStatus.values()) {
            assertNotNull(status.getName());
            assertNotNull(status.getDescription());
            assertFalse(status.getName().isEmpty());
            assertFalse(status.getDescription().isEmpty());
        }
    }
    
    @Test
    @DisplayName("toString方法测试")
    void testToString() {
        // Given
        PersonalNote note = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        
        // When
        String toString = note.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains(note.getNoteId()));
        assertTrue(toString.contains(testRelicsId.toString()));
        assertTrue(toString.contains(testTitle));
    }
    
    @Test
    @DisplayName("equals和hashCode测试")
    void testEqualsAndHashCode() {
        // Given
        PersonalNote note1 = PersonalNote.create(testRelicsId, testTitle, testContent, testNoteType);
        PersonalNote note2 = PersonalNote.create(testRelicsId, "不同标题", "不同内容", PersonalNote.NoteType.RESEARCH);
        
        // When & Then - 不同的笔记应该不相等（基于noteId）
        assertNotEquals(note1, note2);
        assertNotEquals(note1.hashCode(), note2.hashCode());
        
        // 相同的笔记应该相等
        assertEquals(note1, note1);
        assertEquals(note1.hashCode(), note1.hashCode());
    }
}
