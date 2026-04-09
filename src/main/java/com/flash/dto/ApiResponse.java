package com.flash.dto;

import com.flash.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装
 * 前端所有接口均返回此格式
 * 
 * 响应格式：{ code: number, msg: string, data: T }
 * 
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /** 状态码：200成功，4xx客户端错误，5xx服务端错误 */
    private int code;
    
    /** 提示信息 */
    private String msg;
    
    /** 响应数据 */
    private T data;
    
    /**
     * 成功响应（简化写法）
     * @param data 返回数据
     * @return 统一格式响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    
    /**
     * 成功响应（带自定义消息）
     * @param msg 自定义消息
     * @param data 返回数据
     * @return 统一格式响应
     */
    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }
    
    /**
     * 错误响应
     * @param code 错误码
     * @param msg 错误消息
     * @return 统一格式响应
     */
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
    
    /**
     * 业务异常响应
     * @param e 业务异常对象
     * @return 统一格式响应
     */
    public static <T> ApiResponse<T> error(BusinessException e) {
        return new ApiResponse<>(e.getCode(), e.getMessage(), null);
    }
}
