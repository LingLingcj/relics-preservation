package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.CollectionGalleryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收藏馆DAO接口
 * @Author: LingRJ
 * @Description: 收藏馆数据访问接口
 * @DateTime: 2025/7/13
 */
@Mapper
@Repository
public interface ICollectionGalleryDao {
    
    // ==================== 基础CRUD ====================
    
    /**
     * 插入收藏馆记录
     * @param gallery 收藏馆记录
     * @return 影响行数
     */
    int insert(CollectionGalleryPO gallery);
    
    /**
     * 根据ID查询收藏馆记录
     * @param id 主键ID
     * @return 收藏馆记录
     */
    CollectionGalleryPO selectById(Long id);
    
    /**
     * 根据收藏馆ID查询记录
     * @param galleryId 收藏馆ID
     * @return 收藏馆记录
     */
    CollectionGalleryPO selectByGalleryId(@Param("galleryId") String galleryId);
    
    /**
     * 更新收藏馆记录
     * @param gallery 收藏馆记录
     * @return 影响行数
     */
    int update(CollectionGalleryPO gallery);
    
    /**
     * 逻辑删除收藏馆记录
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 根据收藏馆ID逻辑删除
     * @param galleryId 收藏馆ID
     * @return 影响行数
     */
    int deleteByGalleryId(@Param("galleryId") String galleryId);
    
    // ==================== 用户相关查询 ====================
    
    /**
     * 根据用户名查询收藏馆列表
     * @param username 用户名
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户名查询收藏馆列表（分页）
     * @param username 用户名
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> selectByUsernameWithPage(@Param("username") String username, 
                                                       @Param("offset") int offset, 
                                                       @Param("limit") int limit);
    
    /**
     * 统计用户收藏馆数量
     * @param username 用户名
     * @return 收藏馆数量
     */
    long countByUsername(@Param("username") String username);
    
    /**
     * 检查用户是否存在指定名称的收藏馆
     * @param username 用户名
     * @param name 收藏馆名称
     * @return 收藏馆记录
     */
    CollectionGalleryPO selectByUsernameAndName(@Param("username") String username, 
                                               @Param("name") String name);
    
    // ==================== 公开收藏馆查询 ====================
    
    /**
     * 查询公开收藏馆列表
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 公开收藏馆列表
     */
    List<CollectionGalleryPO> selectPublicGalleries(@Param("offset") int offset, 
                                                    @Param("limit") int limit);
    
    /**
     * 根据主题查询公开收藏馆
     * @param theme 主题代码
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 公开收藏馆列表
     */
    List<CollectionGalleryPO> selectPublicGalleriesByTheme(@Param("theme") String theme,
                                                           @Param("offset") int offset, 
                                                           @Param("limit") int limit);
    
    /**
     * 根据分享码查询收藏馆
     * @param shareCode 分享码
     * @return 收藏馆记录
     */
    CollectionGalleryPO selectByShareCode(@Param("shareCode") String shareCode);
    
    /**
     * 统计公开收藏馆数量
     * @return 公开收藏馆数量
     */
    long countPublicGalleries();
    
    /**
     * 根据主题统计公开收藏馆数量
     * @param theme 主题代码
     * @return 收藏馆数量
     */
    long countPublicGalleriesByTheme(@Param("theme") String theme);
    
    // ==================== 文物相关查询 ====================
    
    /**
     * 查询包含指定文物的收藏馆
     * @param relicsId 文物ID
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> selectByRelicsId(@Param("relicsId") Long relicsId);
    
    /**
     * 查询用户包含指定文物的收藏馆
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> selectByUsernameAndRelicsId(@Param("username") String username, 
                                                          @Param("relicsId") Long relicsId);
    
    /**
     * 统计包含指定文物的收藏馆数量
     * @param relicsId 文物ID
     * @return 收藏馆数量
     */
    long countByRelicsId(@Param("relicsId") Long relicsId);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量插入收藏馆记录
     * @param galleries 收藏馆记录列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<CollectionGalleryPO> galleries);
    
    /**
     * 批量更新收藏馆记录
     * @param galleries 收藏馆记录列表
     * @return 影响行数
     */
    int batchUpdate(@Param("list") List<CollectionGalleryPO> galleries);
    
    /**
     * 批量查询收藏馆
     * @param galleryIds 收藏馆ID列表
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> batchSelectByGalleryIds(@Param("galleryIds") List<String> galleryIds);
    
    /**
     * 批量逻辑删除收藏馆
     * @param galleryIds 收藏馆ID列表
     * @return 影响行数
     */
    int batchDeleteByGalleryIds(@Param("galleryIds") List<String> galleryIds);
    
    // ==================== 统计查询 ====================
    
    /**
     * 获取热门收藏馆（按文物数量排序）
     * @param limit 限制数量
     * @return 热门收藏馆列表
     */
    List<CollectionGalleryPO> selectPopularGalleries(@Param("limit") int limit);
    
    /**
     * 获取最新收藏馆
     * @param limit 限制数量
     * @return 最新收藏馆列表
     */
    List<CollectionGalleryPO> selectLatestGalleries(@Param("limit") int limit);
    
    /**
     * 根据主题统计收藏馆数量
     * @return 主题统计列表
     */
    List<ThemeStatistics> selectThemeStatistics();
    
    /**
     * 获取活跃用户（按收藏馆数量排序）
     * @param limit 限制数量
     * @return 活跃用户统计
     */
    List<UserGalleryStatistics> selectActiveUsers(@Param("limit") int limit);
    
    // ==================== 搜索功能 ====================
    
    /**
     * 根据名称模糊搜索公开收藏馆
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> searchPublicGalleriesByName(@Param("keyword") String keyword,
                                                          @Param("offset") int offset, 
                                                          @Param("limit") int limit);
    
    /**
     * 根据描述模糊搜索公开收藏馆
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> searchPublicGalleriesByDescription(@Param("keyword") String keyword,
                                                                @Param("offset") int offset, 
                                                                @Param("limit") int limit);
    
    /**
     * 综合搜索公开收藏馆（名称+描述）
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏馆列表
     */
    List<CollectionGalleryPO> searchPublicGalleries(@Param("keyword") String keyword,
                                                   @Param("offset") int offset, 
                                                   @Param("limit") int limit);
    
    // ==================== 内部类：统计结果 ====================
    
    /**
     * 主题统计结果
     */
    class ThemeStatistics {
        private String theme;
        private String themeName;
        private long galleryCount;
        private long userCount;
        private long totalRelics;
        
        // getters and setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getThemeName() { return themeName; }
        public void setThemeName(String themeName) { this.themeName = themeName; }
        public long getGalleryCount() { return galleryCount; }
        public void setGalleryCount(long galleryCount) { this.galleryCount = galleryCount; }
        public long getUserCount() { return userCount; }
        public void setUserCount(long userCount) { this.userCount = userCount; }
        public long getTotalRelics() { return totalRelics; }
        public void setTotalRelics(long totalRelics) { this.totalRelics = totalRelics; }
    }
    
    /**
     * 用户收藏馆统计结果
     */
    class UserGalleryStatistics {
        private String username;
        private long totalGalleries;
        private long publicGalleries;
        private long privateGalleries;
        private long totalRelics;
        private String mostUsedTheme;
        
        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public long getTotalGalleries() { return totalGalleries; }
        public void setTotalGalleries(long totalGalleries) { this.totalGalleries = totalGalleries; }
        public long getPublicGalleries() { return publicGalleries; }
        public void setPublicGalleries(long publicGalleries) { this.publicGalleries = publicGalleries; }
        public long getPrivateGalleries() { return privateGalleries; }
        public void setPrivateGalleries(long privateGalleries) { this.privateGalleries = privateGalleries; }
        public long getTotalRelics() { return totalRelics; }
        public void setTotalRelics(long totalRelics) { this.totalRelics = totalRelics; }
        public String getMostUsedTheme() { return mostUsedTheme; }
        public void setMostUsedTheme(String mostUsedTheme) { this.mostUsedTheme = mostUsedTheme; }
    }
}
