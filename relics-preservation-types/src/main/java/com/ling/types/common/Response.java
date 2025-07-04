package com.ling.types.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    @JsonProperty("code")
    private String code;

    @JsonProperty("info")
    private String info;

    @JsonProperty("data")
    private T data;

    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    public static <T> Response<T> error(T data) {
        return Response.<T>builder()
                .code(ResponseCode.SYSTEM_ERROR.getCode())
                .info(ResponseCode.SYSTEM_ERROR.getInfo())
                .data(data)
                .build();
    }
}
