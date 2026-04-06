package com.flash.controller;

import com.flash.dto.CreateUserDTO;
import com.flash.dto.UserDTO;
import com.flash.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "根据用户名获取用户")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody CreateUserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
