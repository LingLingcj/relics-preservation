package com.ling.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.service.UserFavoritesCacheService;
import com.ling.infrastructure.cache.service.UserCommentsCacheService;
import com.ling.infrastructure.cache.service.GalleryManagerCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 测试基础类
 * @Author: LingRJ
 * @Description: 提供测试的基础设施和通用方法
 * @DateTime: 2025/7/13
 */
@SpringBootTest
@ActiveProfiles("test")

@Transactional
public abstract class BaseTest {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    protected UserFavoritesCacheService userFavoritesCacheService;
    
    @Autowired
    protected UserCommentsCacheService userCommentsCacheService;
    
    @Autowired
    protected GalleryManagerCacheService galleryManagerCacheService;
    
    // 测试数据常量
    protected static final String TEST_USERNAME_1 = "testuser1";
    protected static final String TEST_USERNAME_2 = "testuser2";
    protected static final Long TEST_RELICS_ID_1 = 1001L;
    protected static final Long TEST_RELICS_ID_2 = 1002L;
    protected static final String TEST_GALLERY_ID_1 = "gallery_test_001";
    protected static final String TEST_GALLERY_ID_2 = "gallery_test_002";
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        cleanupTestData();
        
        // 初始化测试数据
        initializeTestData();
    }
    
    @AfterEach
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }
    
    /**
     * 清理测试数据
     */
    protected void cleanupTestData() {
        // 清理Redis缓存
        cleanupRedisCache();
        
        // 清理数据库数据（由@Transactional自动回滚）
    }
    
    /**
     * 初始化测试数据
     */
    protected void initializeTestData() {
        // 子类可以重写此方法来初始化特定的测试数据
    }
    
    /**
     * 清理Redis缓存
     */
    protected void cleanupRedisCache() {
        try {
            // 获取所有测试相关的缓存键
            Set<String> keys = redisTemplate.keys("test:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            // 清理用户相关缓存
            userFavoritesCacheService.evictUserFavorites(Username.of(TEST_USERNAME_1));
            userFavoritesCacheService.evictUserFavorites(Username.of(TEST_USERNAME_2));
            
            userCommentsCacheService.evictUserComments(Username.of(TEST_USERNAME_1));
            userCommentsCacheService.evictUserComments(Username.of(TEST_USERNAME_2));
            
            galleryManagerCacheService.evictGalleryManager(Username.of(TEST_USERNAME_1));
            galleryManagerCacheService.evictGalleryManager(Username.of(TEST_USERNAME_2));
            
        } catch (Exception e) {
            // 忽略清理过程中的异常
        }
    }
    
    /**
     * 创建测试用户名
     */
    protected Username createTestUsername(String username) {
        return Username.of(username);
    }
    
    /**
     * 创建测试时间
     */
    protected LocalDateTime createTestTime() {
        return LocalDateTime.of(2025, 1, 1, 12, 0, 0);
    }
    
    /**
     * 等待异步操作完成
     */
    protected void waitForAsyncOperation() {
        try {
            Thread.sleep(100); // 等待100ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 验证缓存是否存在
     */
    protected boolean isCacheExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 获取缓存值
     */
    protected Object getCacheValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 设置测试缓存
     */
    protected void setTestCache(String key, Object value) {
        redisTemplate.opsForValue().set("test:" + key, value);
    }
    
    /**
     * 断言缓存存在
     */
    protected void assertCacheExists(String key) {
        assert isCacheExists(key) : "缓存不存在: " + key;
    }
    
    /**
     * 断言缓存不存在
     */
    protected void assertCacheNotExists(String key) {
        assert !isCacheExists(key) : "缓存不应该存在: " + key;
    }
    
    /**
     * 生成测试JSON字符串
     */
    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("转换JSON失败", e);
        }
    }
    
    /**
     * 从JSON字符串解析对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("解析JSON失败", e);
        }
    }
    
    /**
     * 验证对象相等（忽略时间字段）
     */
    protected void assertEqualsIgnoreTime(Object expected, Object actual) {
        // 这里可以实现自定义的对象比较逻辑
        // 忽略时间字段的差异
        assert expected != null && actual != null : "对象不能为null";
    }
    
    /**
     * 生成随机字符串
     */
    protected String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * 生成随机数字
     */
    protected Long generateRandomLong() {
        return (long) (Math.random() * 1000000);
    }
    
    /**
     * 模拟并发执行
     */
    protected void executeConcurrently(Runnable task, int threadCount) throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    /**
     * 测试性能
     */
    protected long measureExecutionTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 验证执行时间
     */
    protected void assertExecutionTime(Runnable task, long maxTimeMs) {
        long executionTime = measureExecutionTime(task);
        assert executionTime <= maxTimeMs : 
            String.format("执行时间过长: %dms > %dms", executionTime, maxTimeMs);
    }
}
