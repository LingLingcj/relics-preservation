package com.ling.types.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 通用响应对象
 * @DateTime: 2025/6/27 14:01
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private String code;
    private String info;
    private T data;
} 