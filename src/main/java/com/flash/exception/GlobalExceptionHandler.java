package com.flash.exception;

import com.flash.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一捕获并处理项目中的各类异常，返回统一格式的响应
 * 
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 作用于所有@RestController，拦截所有异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 如：用户不存在、参数校验失败等业务逻辑错误
     * 
     * @param e 业务异常对象
     * @return 统一格式的响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        // 返回异常携带的错误码和消息
        return ApiResponse.error(e);
    }

    /**
     * 处理参数校验异常
     * 当DTO字段添加了@NotBlank/@NotNull等注解但传值不合法时触发
     * 
     * @param e 参数校验异常对象
     * @return 校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        // 遍历所有校验失败的字段
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return ApiResponse.error(400, "参数校验失败: " + errors);
    }

    /**
     * 处理非法参数异常
     * 程序逻辑检查到的参数错误
     * 
     * @param e 非法参数异常对象
     * @return 错误信息
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 处理未知异常
     * 兜底处理未预见的异常，避免原生堆栈信息暴露给前端
     * 
     * @param e 任意异常对象
     * @return 友好的错误提示
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        // 打印完整堆栈信息到日志，方便开发调试
        log.error("系统异常", e);
        // 返回友好提示，避免泄露系统细节
        return ApiResponse.error(500, "系统内部错误，请稍后重试");
    }
}
