package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

/**
 * 交互操作结果值对象
 * @Author: LingRJ
 * @Description: 封装交互操作的结果信息
 * @DateTime: 2025/7/11
 */
@Getter
public class InteractionResult {
    
    private final boolean success;
    private final String message;
    private final Object data;
    private final String errorCode;
    
    private InteractionResult(boolean success, String message, Object data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }
    
    /**
     * 创建成功结果
     * @param message 成功消息
     * @return 成功结果
     */
    public static InteractionResult success(String message) {
        return new InteractionResult(true, message, null, null);
    }
    
    /**
     * 创建成功结果（带数据）
     * @param message 成功消息
     * @param data 返回数据
     * @return 成功结果
     */
    public static InteractionResult success(String message, Object data) {
        return new InteractionResult(true, message, data, null);
    }
    
    /**
     * 创建失败结果
     * @param message 失败消息
     * @return 失败结果
     */
    public static InteractionResult failure(String message) {
        return new InteractionResult(false, message, null, null);
    }
    
    /**
     * 创建失败结果（带错误码）
     * @param message 失败消息
     * @param errorCode 错误码
     * @return 失败结果
     */
    public static InteractionResult failure(String message, String errorCode) {
        return new InteractionResult(false, message, null, errorCode);
    }
    
    /**
     * 是否成功
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 是否失败
     * @return 是否失败
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * 获取数据（泛型）
     * @param clazz 数据类型
     * @param <T> 泛型类型
     * @return 数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> clazz) {
        if (data != null && clazz.isInstance(data)) {
            return (T) data;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("InteractionResult{success=%s, message='%s', errorCode='%s'}", 
                success, message, errorCode);
    }
}
