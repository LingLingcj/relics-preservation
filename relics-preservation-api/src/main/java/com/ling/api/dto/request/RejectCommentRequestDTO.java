package com.ling.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author: LingRJ
 * @Description: 评论审核拒绝
 * @DateTime: 2025/7/11 23:32
 **/
@Data
public class RejectCommentRequestDTO {
    @NotBlank(message = "拒绝理由不能为空")
    @Size(max = 500, message = "拒绝理由不能超过500字符")
    private String reason;
}
