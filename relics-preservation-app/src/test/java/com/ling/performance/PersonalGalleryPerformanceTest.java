package com.ling.performance;

import com.ling.domain.interaction.model.valobj.Achievement;
import com.ling.domain.interaction.model.valobj.LearningRecord;
import com.ling.domain.interaction.model.valobj.PersonalNote;
import com.ling.domain.interaction.service.IPersonalGalleryService;
import com.ling.test.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 个人收藏馆性能测试
 * @Author: LingRJ
 * @Description: 测试个人收藏馆功能的性能指标
 * @DateTime: 2025/7/13
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "test.performance", matches = "true")
@DisplayName("个人收藏馆性能测试")
class PersonalGalleryPerformanceTest extends BaseTest {

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private IPersonalGalleryService personalGalleryService;
    
    private static final int CONCURRENT_USERS = 10;
    private static final int OPERATIONS_PER_USER = 100;
    private static final long MAX_RESPONSE_TIME_MS = 1000;
    private static final long MAX_AVERAGE_RESPONSE_TIME_MS = 500;
    
    @Test
    @DisplayName("学习记录创建性能测试")
    void testLearningRecordCreationPerformance() {
        StopWatch stopWatch = new StopWatch("学习记录创建性能测试");
        
        // 单次操作性能测试
        stopWatch.start("单次创建学习记录");
        String recordId = personalGalleryService.startLearningRecord(
            TEST_USERNAME_1, TEST_RELICS_ID_1, LearningRecord.LearningType.DETAILED_STUDY);
        stopWatch.stop();
        
        assertNotNull(recordId);
        assertTrue(stopWatch.getLastTaskTimeMillis() < MAX_RESPONSE_TIME_MS, 
            "单次创建学习记录耗时过长: " + stopWatch.getLastTaskTimeMillis() + "ms");
        
        // 批量操作性能测试
        stopWatch.start("批量创建学习记录");
        List<String> recordIds = new ArrayList<>();
        for (int i = 0; i < OPERATIONS_PER_USER; i++) {
            String id = personalGalleryService.startLearningRecord(
                TEST_USERNAME_1, TEST_RELICS_ID_1 + i, LearningRecord.LearningType.BROWSE);
            recordIds.add(id);
        }
        stopWatch.stop();
        
        assertEquals(OPERATIONS_PER_USER, recordIds.size());
        long averageTime = stopWatch.getLastTaskTimeMillis() / OPERATIONS_PER_USER;
        assertTrue(averageTime < MAX_AVERAGE_RESPONSE_TIME_MS, 
            "批量创建学习记录平均耗时过长: " + averageTime + "ms");
        
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    @DisplayName("并发学习记录性能测试")
    void testConcurrentLearningRecordPerformance() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<Future<Long>> futures = new ArrayList<>();
        
        // 提交并发任务
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            Future<Long> future = executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                
                // 每个用户执行多个操作
                for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                    String recordId = personalGalleryService.startLearningRecord(
                        TEST_USERNAME_1 + "_" + userId, 
                        TEST_RELICS_ID_1 + j, 
                        LearningRecord.LearningType.BROWSE);
                    
                    // 添加学习活动
                    personalGalleryService.addLearningActivity(
                        TEST_USERNAME_1 + "_" + userId, recordId,
                        LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS,
                        "并发测试活动");
                    
                    // 结束学习记录
                    personalGalleryService.endLearningRecord(
                        TEST_USERNAME_1 + "_" + userId, recordId, 4);
                }
                
                return System.currentTimeMillis() - startTime;
            });
            futures.add(future);
        }
        
        // 等待所有任务完成并收集结果
        List<Long> executionTimes = new ArrayList<>();
        for (Future<Long> future : futures) {
            executionTimes.add(future.get());
        }
        
        executor.shutdown();
        
        // 分析性能结果
        long totalTime = executionTimes.stream().mapToLong(Long::longValue).sum();
        long averageTime = totalTime / CONCURRENT_USERS;
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        
        System.out.println("并发性能测试结果:");
        System.out.println("并发用户数: " + CONCURRENT_USERS);
        System.out.println("每用户操作数: " + OPERATIONS_PER_USER);
        System.out.println("总操作数: " + (CONCURRENT_USERS * OPERATIONS_PER_USER));
        System.out.println("平均执行时间: " + averageTime + "ms");
        System.out.println("最大执行时间: " + maxTime + "ms");
        System.out.println("最小执行时间: " + minTime + "ms");
        
        // 性能断言
        assertTrue(averageTime < MAX_RESPONSE_TIME_MS * OPERATIONS_PER_USER, 
            "并发平均执行时间过长: " + averageTime + "ms");
        assertTrue(maxTime < MAX_RESPONSE_TIME_MS * OPERATIONS_PER_USER * 2, 
            "并发最大执行时间过长: " + maxTime + "ms");
    }
    
    @Test
    @DisplayName("个人笔记查询性能测试")
    void testPersonalNoteQueryPerformance() {
        // 先创建一些测试数据
        for (int i = 0; i < 50; i++) {
            // 模拟添加笔记到缓存
            personalGalleryService.getAllPersonalNotes(TEST_USERNAME_1 + "_" + i);
        }
        
        StopWatch stopWatch = new StopWatch("个人笔记查询性能测试");
        
        // 单次查询性能
        stopWatch.start("单次查询所有笔记");
        List<PersonalNote> notes = personalGalleryService.getAllPersonalNotes(TEST_USERNAME_1);
        stopWatch.stop();
        
        assertNotNull(notes);
        assertTrue(stopWatch.getLastTaskTimeMillis() < MAX_RESPONSE_TIME_MS,
            "单次查询笔记耗时过长: " + stopWatch.getLastTaskTimeMillis() + "ms");
        
        // 批量查询性能
        stopWatch.start("批量查询笔记");
        for (int i = 0; i < OPERATIONS_PER_USER; i++) {
            personalGalleryService.getAllPersonalNotes(TEST_USERNAME_1 + "_" + (i % 10));
        }
        stopWatch.stop();
        
        long averageTime = stopWatch.getLastTaskTimeMillis() / OPERATIONS_PER_USER;
        assertTrue(averageTime < MAX_AVERAGE_RESPONSE_TIME_MS,
            "批量查询笔记平均耗时过长: " + averageTime + "ms");
        
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    @DisplayName("成就系统性能测试")
    void testAchievementSystemPerformance() {
        StopWatch stopWatch = new StopWatch("成就系统性能测试");
        
        // 成就检查性能
        stopWatch.start("单次成就检查");
        List<Achievement> achievements = personalGalleryService.checkAndUpdateAchievements(TEST_USERNAME_1);
        stopWatch.stop();
        
        assertNotNull(achievements);
        assertTrue(stopWatch.getLastTaskTimeMillis() < MAX_RESPONSE_TIME_MS,
            "单次成就检查耗时过长: " + stopWatch.getLastTaskTimeMillis() + "ms");
        
        // 批量成就检查性能
        stopWatch.start("批量成就检查");
        for (int i = 0; i < OPERATIONS_PER_USER; i++) {
            personalGalleryService.checkAndUpdateAchievements(TEST_USERNAME_1 + "_" + i);
        }
        stopWatch.stop();
        
        long averageTime = stopWatch.getLastTaskTimeMillis() / OPERATIONS_PER_USER;
        assertTrue(averageTime < MAX_AVERAGE_RESPONSE_TIME_MS,
            "批量成就检查平均耗时过长: " + averageTime + "ms");
        
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    @DisplayName("推荐系统性能测试")
    void testRecommendationSystemPerformance() {
        StopWatch stopWatch = new StopWatch("推荐系统性能测试");
        
        // 文物推荐性能
        stopWatch.start("文物推荐");
        List<Long> recommendations = personalGalleryService.getPersonalizedRelicsRecommendations(TEST_USERNAME_1, 10);
        stopWatch.stop();
        
        assertNotNull(recommendations);
        assertEquals(10, recommendations.size());
        assertTrue(stopWatch.getLastTaskTimeMillis() < MAX_RESPONSE_TIME_MS,
            "文物推荐耗时过长: " + stopWatch.getLastTaskTimeMillis() + "ms");
        
        // 学习分析性能
        stopWatch.start("学习分析");
        IPersonalGalleryService.UserLearningAnalysis analysis = 
            personalGalleryService.getUserLearningAnalysis(TEST_USERNAME_1);
        stopWatch.stop();
        
        assertNotNull(analysis);
        assertTrue(stopWatch.getLastTaskTimeMillis() < MAX_RESPONSE_TIME_MS,
            "学习分析耗时过长: " + stopWatch.getLastTaskTimeMillis() + "ms");
        
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    @DisplayName("内存使用性能测试")
    void testMemoryUsagePerformance() {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存使用
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行大量操作
        List<String> recordIds = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String recordId = personalGalleryService.startLearningRecord(
                TEST_USERNAME_1, TEST_RELICS_ID_1 + i, LearningRecord.LearningType.BROWSE);
            recordIds.add(recordId);
            
            // 添加学习活动
            personalGalleryService.addLearningActivity(
                TEST_USERNAME_1, recordId,
                LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS,
                "内存测试活动 " + i);
        }
        
        // 记录操作后内存使用
        long afterOperationMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = afterOperationMemory - initialMemory;
        
        System.out.println("内存使用性能测试结果:");
        System.out.println("初始内存使用: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("操作后内存使用: " + (afterOperationMemory / 1024 / 1024) + " MB");
        System.out.println("内存增长: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // 内存使用不应该过度增长（这里设置为100MB的限制）
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
            "内存使用增长过多: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // 清理数据
        recordIds.clear();
        System.gc(); // 建议垃圾回收
    }
    
    @Test
    @DisplayName("缓存性能测试")
    void testCachePerformance() {
        StopWatch stopWatch = new StopWatch("缓存性能测试");
        
        // 缓存写入性能
        stopWatch.start("缓存写入");
        for (int i = 0; i < OPERATIONS_PER_USER; i++) {
            setTestCache("performance_test_" + i, "test_value_" + i);
        }
        stopWatch.stop();
        
        long writeAverageTime = stopWatch.getLastTaskTimeMillis() / OPERATIONS_PER_USER;
        assertTrue(writeAverageTime < 10, // 缓存写入应该很快，10ms内
            "缓存写入平均耗时过长: " + writeAverageTime + "ms");
        
        // 缓存读取性能
        stopWatch.start("缓存读取");
        for (int i = 0; i < OPERATIONS_PER_USER; i++) {
            Object value = getCacheValue("test:performance_test_" + i);
            assertNotNull(value);
        }
        stopWatch.stop();
        
        long readAverageTime = stopWatch.getLastTaskTimeMillis() / OPERATIONS_PER_USER;
        assertTrue(readAverageTime < 5, // 缓存读取应该更快，5ms内
            "缓存读取平均耗时过长: " + readAverageTime + "ms");
        
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    @DisplayName("压力测试")
    void testStressTest() throws InterruptedException {
        final int STRESS_USERS = 50;
        final int STRESS_OPERATIONS = 20;
        
        ExecutorService executor = Executors.newFixedThreadPool(STRESS_USERS);
        CountDownLatch latch = new CountDownLatch(STRESS_USERS);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        // 启动压力测试
        for (int i = 0; i < STRESS_USERS; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < STRESS_OPERATIONS; j++) {
                        // 混合操作
                        String recordId = personalGalleryService.startLearningRecord(
                            "stress_user_" + userId, 
                            TEST_RELICS_ID_1 + j, 
                            LearningRecord.LearningType.BROWSE);
                        
                        personalGalleryService.addLearningActivity(
                            "stress_user_" + userId, recordId,
                            LearningRecord.LearningActivity.ActivityType.VIEW_DETAILS,
                            "压力测试");
                        
                        personalGalleryService.endLearningRecord(
                            "stress_user_" + userId, recordId, 3);
                        
                        personalGalleryService.checkAndUpdateAchievements("stress_user_" + userId);
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有任务完成
        latch.await(60, TimeUnit.SECONDS); // 最多等待60秒
        executor.shutdown();
        
        long totalTime = System.currentTimeMillis() - startTime;
        int totalOperations = STRESS_USERS * STRESS_OPERATIONS * 4; // 每次循环4个操作
        
        System.out.println("压力测试结果:");
        System.out.println("并发用户数: " + STRESS_USERS);
        System.out.println("每用户操作数: " + STRESS_OPERATIONS);
        System.out.println("总操作数: " + totalOperations);
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均TPS: " + (totalOperations * 1000.0 / totalTime));
        System.out.println("异常数量: " + exceptions.size());
        
        // 压力测试断言
        assertTrue(exceptions.size() < totalOperations * 0.01, // 异常率应该小于1%
            "压力测试异常率过高: " + (exceptions.size() * 100.0 / totalOperations) + "%");
        
        assertTrue(totalTime < 60000, // 总时间应该在60秒内
            "压力测试总耗时过长: " + totalTime + "ms");
        
        // 打印异常信息（如果有）
        if (!exceptions.isEmpty()) {
            System.err.println("压力测试异常:");
            exceptions.forEach(e -> e.printStackTrace());
        }
    }
}
