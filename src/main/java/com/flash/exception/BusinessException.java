package com.flash.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑中的各种异常情况
 * 继承RuntimeException，支持事务自动回滚
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /** HTTP状态码 */
    private final int code;
    
    /**
     * 构造业务异常
     * @param message 错误消息（默认code为400）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
    
    /**
     * 构造带自定义状态码的异常
     * @param code HTTP状态码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 资源不存在异常（404）
     * @param message 错误消息
     * @return BusinessException实例
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
    
    /**
     * 未授权异常（401）
     * @param message 错误消息
     * @return BusinessException实例
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
    
    /**
     * 禁止访问异常（403）
     * @param message 错误消息
     * @return BusinessException实例
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
    
    /**
     * 请求参数错误异常（400）
     * @param message 错误消息
     * @return BusinessException实例
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
}
