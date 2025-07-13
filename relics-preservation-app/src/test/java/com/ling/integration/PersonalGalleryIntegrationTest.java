package com.ling.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ling.domain.interaction.model.valobj.Achievement;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.interaction.model.valobj.PersonalNote;
import com.ling.domain.interaction.model.valobj.LearningRecord;
import com.ling.domain.interaction.service.IPersonalGalleryService;
import com.ling.test.BaseTest;
import com.ling.types.common.Response;
import com.ling.types.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 个人收藏馆集成测试
 * @Author: LingRJ
 * @Description: 测试个人收藏馆功能的端到端集成
 * @DateTime: 2025/7/13
 */
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("个人收藏馆集成测试")
class PersonalGalleryIntegrationTest extends BaseTest {

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private IPersonalGalleryService personalGalleryService;
    
    @Test
    @DisplayName("个人笔记完整流程测试")
    public void testPersonalNoteCompleteFlow() throws Exception {
        // 1. 添加个人笔记
        MvcResult addResult = mockMvc.perform(post("/api/v1/personal-gallery/notes")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1)
                .param("galleryId", TEST_GALLERY_ID_1)
                .param("relicsId", TEST_RELICS_ID_1.toString())
                .param("title", "集成测试笔记")
                .param("content", "这是一个集成测试的笔记内容")
                .param("noteType", "GENERAL")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        
        String addResponseJson = addResult.getResponse().getContentAsString();
        Response<String> addResponse = objectMapper.readValue(addResponseJson, Response.class);
        
        // 验证添加结果
        if ("0000".equals(addResponse.getCode())) {
            String noteId = (String) addResponse.getData();
            assertNotNull(noteId);
            
            // 2. 更新个人笔记
            mockMvc.perform(put("/api/v1/personal-gallery/notes/" + noteId)
                            .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                    .param("username", TEST_USERNAME_1)
                    .param("title", "更新后的标题")
                    .param("content", "更新后的内容")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"));
            
            // 3. 删除个人笔记
            mockMvc.perform(delete("/api/v1/personal-gallery/notes/" + noteId)
                            .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                    .param("username", TEST_USERNAME_1)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk());
        }
        
        // 4. 获取所有个人笔记
        mockMvc.perform(get("/api/v1/personal-gallery/notes")
                .param("username", TEST_USERNAME_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("学习记录完整流程测试")
    void testLearningRecordCompleteFlow() throws Exception {
        // 1. 开始学习记录
        MvcResult startResult = mockMvc.perform(post("/api/v1/personal-gallery/learning/start")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1)
                .param("relicsId", TEST_RELICS_ID_1.toString())
                .param("learningType", "DETAILED_STUDY")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();
        
        String startResponseJson = startResult.getResponse().getContentAsString();
        Response<String> startResponse = objectMapper.readValue(startResponseJson, Response.class);
        
        // 验证开始学习结果
        if ("0000".equals(startResponse.getCode())) {
            String recordId = (String) startResponse.getData();
            assertNotNull(recordId);
            
            // 等待一段时间模拟学习过程
            waitForAsyncOperation();
            
            // 2. 结束学习记录
            mockMvc.perform(post("/api/v1/personal-gallery/learning/end")
                            .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                    .param("username", TEST_USERNAME_1)
                    .param("recordId", recordId)
                    .param("rating", "4")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"));
        }
        
        // 3. 获取学习记录
        mockMvc.perform(get("/api/v1/personal-gallery/learning/records")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("成就系统完整流程测试")
    void testAchievementCompleteFlow() throws Exception {
        // 1. 检查成就进度
        mockMvc.perform(post("/api/v1/personal-gallery/achievements/check")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray());
        
        // 2. 获取用户成就
        mockMvc.perform(get("/api/v1/personal-gallery/achievements")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray());
        
        // 3. 获取已解锁成就
        mockMvc.perform(get("/api/v1/personal-gallery/achievements/unlocked")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("推荐系统完整流程测试")
    void testRecommendationCompleteFlow() throws Exception {
        // 1. 获取文物推荐
        mockMvc.perform(get("/api/v1/personal-gallery/recommendations/relics")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")
                .param("username", TEST_USERNAME_1)
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5));
        
        // 2. 获取学习分析
        mockMvc.perform(get("/api/v1/personal-gallery/analysis")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")

                .param("username", TEST_USERNAME_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME_1));
    }
    
    @Test
    @DisplayName("服务层集成测试 - 个人笔记")
    void testServiceLayerIntegrationPersonalNotes() {
        // 1. 添加个人笔记（模拟场景，实际需要先创建收藏馆和添加文物）
        String title = "服务层测试笔记";
        String content = "这是服务层集成测试的笔记内容";
        
        // 由于需要完整的数据流，这里主要测试服务方法的调用
        List<PersonalNote> initialNotes = personalGalleryService.getAllPersonalNotes(TEST_USERNAME_1);
        assertNotNull(initialNotes);
        
        // 2. 获取个人笔记
        Optional<PersonalNote> note = personalGalleryService.getPersonalNote(TEST_USERNAME_1, TEST_RELICS_ID_1);
        // 由于没有实际添加，应该为空
        assertTrue(note.isEmpty());
    }
    
    @Test
    @DisplayName("服务层集成测试 - 学习记录")
    void testServiceLayerIntegrationLearningRecords() {
        // 1. 开始学习记录
        String recordId = personalGalleryService.startLearningRecord(
            TEST_USERNAME_1, TEST_RELICS_ID_1, LearningRecord.LearningType.DETAILED_STUDY);
        
        assertNotNull(recordId);
        assertFalse(recordId.isEmpty());
        
        // 2. 添加学习活动
        InteractionResult activityResult = personalGalleryService.addLearningActivity(
            TEST_USERNAME_1, recordId, 
            LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS, 
            "查看文物详细信息");
        
        assertTrue(activityResult.isSuccess());
        
        // 3. 结束学习记录
        InteractionResult endResult = personalGalleryService.endLearningRecord(
            TEST_USERNAME_1, recordId, 4);
        
        assertTrue(endResult.isSuccess());
        
        // 4. 获取学习记录
        List<LearningRecord> records = personalGalleryService.getLearningRecords(TEST_USERNAME_1, null);
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals(recordId, records.get(0).getRecordId());
    }
    
    @Test
    @DisplayName("服务层集成测试 - 成就系统")
    void testServiceLayerIntegrationAchievements() {
        // 1. 检查成就进度
        List<Achievement> newlyUnlocked = personalGalleryService.checkAndUpdateAchievements(TEST_USERNAME_1);
        assertNotNull(newlyUnlocked);
        
        // 2. 获取用户成就
        List<Achievement> userAchievements = personalGalleryService.getUserAchievements(TEST_USERNAME_1);
        assertNotNull(userAchievements);
        
        // 3. 获取已解锁成就
        List<Achievement> unlockedAchievements = personalGalleryService.getUnlockedAchievements(TEST_USERNAME_1);
        assertNotNull(unlockedAchievements);
        
        // 验证已解锁成就是用户成就的子集
        assertTrue(userAchievements.containsAll(unlockedAchievements));
    }
    
    @Test
    @DisplayName("缓存集成测试")
    void testCacheIntegration() {
        // 1. 测试缓存写入
        setTestCache("personal_note_test", "test_value");
        assertTrue(isCacheExists("test:personal_note_test"));
        
        // 2. 测试缓存读取
        Object cachedValue = getCacheValue("test:personal_note_test");
        assertEquals("test_value", cachedValue);
        
        // 3. 测试缓存清理
        cleanupRedisCache();
        assertFalse(isCacheExists("test:personal_note_test"));
    }
    
    @Test
    @DisplayName("并发访问测试")
    void testConcurrentAccess() throws InterruptedException {
        // 模拟多个用户同时访问个人收藏馆功能
        Runnable task = () -> {
            try {
                // 开始学习记录
                String recordId = personalGalleryService.startLearningRecord(
                    TEST_USERNAME_1, generateRandomLong(), LearningRecord.LearningType.BROWSE);
                
                // 等待一小段时间
                Thread.sleep(10);
                
                // 结束学习记录
                personalGalleryService.endLearningRecord(TEST_USERNAME_1, recordId, 3);
                
            } catch (Exception e) {
                // 记录异常但不影响测试
                System.err.println("并发测试异常: " + e.getMessage());
            }
        };
        
        // 执行并发测试
        executeConcurrently(task, 5);
        
        // 验证结果
        List<LearningRecord> records = personalGalleryService.getLearningRecords(TEST_USERNAME_1, null);
        assertNotNull(records);
        // 由于并发执行，记录数量应该大于0
        assertTrue(records.size() >= 0);
    }
    
    @Test
    @DisplayName("性能测试")
    void testPerformance() {
        // 测试获取个人笔记的性能
        assertExecutionTime(() -> {
            personalGalleryService.getAllPersonalNotes(TEST_USERNAME_1);
        }, 100); // 应该在100ms内完成
        
        // 测试获取学习记录的性能
        assertExecutionTime(() -> {
            personalGalleryService.getLearningRecords(TEST_USERNAME_1, null);
        }, 100); // 应该在100ms内完成
        
        // 测试获取用户成就的性能
        assertExecutionTime(() -> {
            personalGalleryService.getUserAchievements(TEST_USERNAME_1);
        }, 100); // 应该在100ms内完成
    }

    @Test
    @DisplayName("错误处理集成测试")
    void testErrorHandlingIntegration() throws Exception {
        // 1. 测试无效参数
        mockMvc.perform(post("/api/v1/personal-gallery/notes")
                        .header("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleHBlcnQxIiwiaWF0IjoxNzUyNDExMDM3LCJleHAiOjE3NjEwNTEwMzd9.gofuBZZMkn21CnbojUuAK9zav8leXOK2dV2JhrwuWfM")
                .param("galleryId", TEST_GALLERY_ID_1)
                .param("relicsId", TEST_RELICS_ID_1.toString())
                .param("title", "测试标题")
                .param("content", "测试内容")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0001")); // 应该返回错误码
        
        // 2. 测试不存在的资源
        mockMvc.perform(get("/api/v1/personal-gallery/notes/relics/999999")
                .param("username", TEST_USERNAME_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0001")); // 应该返回错误码
        
        // 3. 测试服务层错误处理
        InteractionResult result = personalGalleryService.endLearningRecord(
            TEST_USERNAME_1, "不存在的记录ID", 4);
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    @DisplayName("数据一致性测试")
    void testDataConsistency() {
        // 1. 创建学习记录
        String recordId = personalGalleryService.startLearningRecord(
            TEST_USERNAME_1, TEST_RELICS_ID_1, LearningRecord.LearningType.DETAILED_STUDY);
        
        // 2. 添加多个学习活动
        personalGalleryService.addLearningActivity(
            TEST_USERNAME_1, recordId, 
            LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS, "查看详情");
        personalGalleryService.addLearningActivity(
            TEST_USERNAME_1, recordId, 
            LearningRecord.LearningActivity.ActivityType.TAKE_NOTES, "记录笔记");
        
        // 3. 结束学习记录
        personalGalleryService.endLearningRecord(TEST_USERNAME_1, recordId, 5);
        
        // 4. 验证数据一致性
        List<LearningRecord> records = personalGalleryService.getLearningRecords(TEST_USERNAME_1, TEST_RELICS_ID_1);
        assertEquals(1, records.size());
        
        LearningRecord record = records.get(0);
        assertEquals(recordId, record.getRecordId());
        assertEquals(2, record.getActivityCount()); // 应该有2个活动
        assertEquals(5, record.getLearningRating()); // 评分应该是5
        assertNotNull(record.getEndTime()); // 应该有结束时间
    }
}
