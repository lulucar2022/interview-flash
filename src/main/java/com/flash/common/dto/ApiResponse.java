package com.flash.common.dto;

import com.flash.common.exception.BusinessException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public static <T> ApiResponse<T> error(BusinessException e) {
        return new ApiResponse<>(e.getCode(), e.getMessage(), null);
    }

    public static <T> ApiResponse<T> unauthorized(String msg) {
        return new ApiResponse<>(401, msg, null);
    }

    public static <T> ApiResponse<T> forbidden(String msg) {
        return new ApiResponse<>(403, msg, null);
    }
}
