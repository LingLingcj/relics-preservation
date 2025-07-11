package com.ling.trigger.converter;

import com.ling.api.dto.response.RelicsCommentDTO;
import com.ling.api.dto.response.RelicsCommentListResponseDTO;
import com.ling.domain.interaction.model.valobj.RelicsComment;
import com.ling.domain.interaction.model.valobj.RelicsCommentListResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文物评论转换器
 * @Author: LingRJ
 */
@Component
public class RelicsCommentConverter {

    public RelicsCommentListResponseDTO toRelicsCommentListResponseDTO(RelicsCommentListResult result) {
        if (result == null) {
            return RelicsCommentListResponseDTO.builder()
                    .comments(List.of())
                    .totalCount(0L)
                    .build();
        }

        List<RelicsCommentDTO> commentDTOs = result.getComments().stream()
                .map(this::toRelicsCommentDTO)
                .collect(Collectors.toList());

        return RelicsCommentListResponseDTO.builder()
                .comments(commentDTOs)
                .totalCount(result.getTotalCount())
                .currentPage(result.getCurrentPage())
                .pageSize(result.getPageSize())
                .totalPages(result.getTotalPages())
                .hasNext(result.getHasNext())
                .hasPrevious(result.getHasPrevious())
                .relicsId(result.getRelicsId())
                .paginationSummary(result.getPaginationSummary())
                .build();
    }

    public RelicsCommentDTO toRelicsCommentDTO(RelicsComment comment) {
        if (comment == null) return null;

        return RelicsCommentDTO.builder()
                .commentId(comment.getCommentId())
                .username(comment.getUsername())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .likeCount(comment.getLikeCount())
                .featured(comment.getFeatured())
                .recent(comment.isRecent())
                .contentSummary(comment.getContentSummary(100))
                .build();
    }
}
