package com.ling.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author: LingRJ
 * @Description: 评论审核
 * @DateTime: 2025/7/11 23:31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentReviewRequestDTO {
    @NotBlank(message = "审核操作不能为空")
    private String action;

    @Size(max = 500, message = "审核理由不能超过500字符")
    private String reason;
}
