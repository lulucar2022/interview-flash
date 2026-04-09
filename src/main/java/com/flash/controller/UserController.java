package com.flash.controller;

import com.flash.dto.ApiResponse;
import com.flash.dto.CreateUserDTO;
import com.flash.dto.UserDTO;
import com.flash.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取所有用户")
    @GetMapping
    public ApiResponse<List<UserDTO>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "根据用户名获取用户")
    @GetMapping("/username/{username}")
    public ApiResponse<UserDTO> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable String username) {
        return ApiResponse.success(userService.getUserByUsername(username));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody CreateUserDTO dto) {
        return ApiResponse.success("创建成功", userService.createUser(dto));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody CreateUserDTO dto) {
        return ApiResponse.success("更新成功", userService.updateUser(id, dto));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }
}
