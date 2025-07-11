package com.ling.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 批审核评论
 * @DateTime: 2025/7/11 23:33
 **/
@Data
public class BatchCommentReviewRequestDTO {
    @NotEmpty(message = "评论ID列表不能为空")
    private List<Long> commentIds;

    @NotBlank(message = "审核操作不能为空")
    private String action;

    @Size(max = 500, message = "审核理由不能超过500字符")
    private String reason;
}
