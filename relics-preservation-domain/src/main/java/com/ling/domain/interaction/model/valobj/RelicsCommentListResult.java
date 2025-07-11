package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * 文物评论列表结果
 * @Author: LingRJ
 * @Description: 封装文物评论查询的分页结果
 * @DateTime: 2025/7/11
 */
@Getter
public class RelicsCommentListResult {
    
    /**
     * 评论列表
     */
    private final List<RelicsComment> comments;
    
    /**
     * 总评论数
     */
    private final Long totalCount;
    
    /**
     * 当前页码
     */
    private final Integer currentPage;
    
    /**
     * 每页大小
     */
    private final Integer pageSize;
    
    /**
     * 总页数
     */
    private final Integer totalPages;
    
    /**
     * 是否有下一页
     */
    private final Boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private final Boolean hasPrevious;
    
    /**
     * 文物ID
     */
    private final Long relicsId;
    
    /**
     * 构造函数
     */
    public RelicsCommentListResult(List<RelicsComment> comments, Long totalCount, 
                                  Integer currentPage, Integer pageSize, Long relicsId) {
        this.comments = Objects.requireNonNull(comments, "评论列表不能为空");
        this.totalCount = Objects.requireNonNull(totalCount, "总数不能为空");
        this.currentPage = Objects.requireNonNull(currentPage, "当前页码不能为空");
        this.pageSize = Objects.requireNonNull(pageSize, "每页大小不能为空");
        this.relicsId = Objects.requireNonNull(relicsId, "文物ID不能为空");
        
        // 计算总页数
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        // 计算是否有下一页和上一页
        this.hasNext = currentPage < totalPages;
        this.hasPrevious = currentPage > 1;
    }
    
    /**
     * 检查是否为空结果
     * @return 是否为空
     */
    public boolean isEmpty() {
        return comments.isEmpty();
    }
    
    /**
     * 获取当前页评论数量
     * @return 当前页评论数量
     */
    public int getCurrentPageSize() {
        return comments.size();
    }
    
    /**
     * 检查是否为第一页
     * @return 是否为第一页
     */
    public boolean isFirstPage() {
        return currentPage == 1;
    }
    
    /**
     * 检查是否为最后一页
     * @return 是否为最后一页
     */
    public boolean isLastPage() {
        return currentPage.equals(totalPages);
    }
    
    /**
     * 获取下一页页码
     * @return 下一页页码，如果没有下一页则返回null
     */
    public Integer getNextPage() {
        return hasNext ? currentPage + 1 : null;
    }
    
    /**
     * 获取上一页页码
     * @return 上一页页码，如果没有上一页则返回null
     */
    public Integer getPreviousPage() {
        return hasPrevious ? currentPage - 1 : null;
    }
    
    /**
     * 获取分页信息摘要
     * @return 分页信息摘要
     */
    public String getPaginationSummary() {
        if (totalCount == 0) {
            return "暂无评论";
        }
        
        int startIndex = (currentPage - 1) * pageSize + 1;
        int endIndex = Math.min(currentPage * pageSize, totalCount.intValue());
        
        return String.format("第 %d-%d 条，共 %d 条评论", startIndex, endIndex, totalCount);
    }
    
    /**
     * 创建空结果
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 空结果
     */
    public static RelicsCommentListResult empty(Long relicsId, Integer page, Integer size) {
        return new RelicsCommentListResult(List.of(), 0L, page, size, relicsId);
    }
    
    /**
     * 创建成功结果
     * @param comments 评论列表
     * @param totalCount 总数
     * @param page 页码
     * @param size 每页大小
     * @param relicsId 文物ID
     * @return 成功结果
     */
    public static RelicsCommentListResult success(List<RelicsComment> comments, Long totalCount, 
                                                 Integer page, Integer size, Long relicsId) {
        return new RelicsCommentListResult(comments, totalCount, page, size, relicsId);
    }
    
    @Override
    public String toString() {
        return "RelicsCommentListResult{" +
                "commentsCount=" + comments.size() +
                ", totalCount=" + totalCount +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                ", relicsId=" + relicsId +
                '}';
    }
}
