package com.flash.controller;

import com.flash.dto.ApiResponse;
import com.flash.dto.UpdateProgressDTO;
import com.flash.dto.UserProgressDTO;
import com.flash.service.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Tag(name = "用户进度", description = "用户学习进度相关接口")
public class UserProgressController {

    private final UserProgressService userProgressService;

    @Operation(summary = "获取用户所有进度")
    @GetMapping
    public ApiResponse<List<UserProgressDTO>> getUserProgress(
            @Parameter(description = "用户ID", required = true) @RequestParam Integer userId) {
        return ApiResponse.success(userProgressService.getUserProgress(userId));
    }

    @Operation(summary = "获取指定题目的进度")
    @GetMapping("/question")
    public ApiResponse<UserProgressDTO> getProgressByQuestion(
            @Parameter(description = "用户ID") @RequestParam Integer userId,
            @Parameter(description = "题目ID") @RequestParam Long questionId) {
        UserProgressDTO progress = userProgressService.getProgressByQuestion(userId, questionId);
        return ApiResponse.success(progress);
    }

    @Operation(summary = "获取用户的错题列表")
    @GetMapping("/wrong")
    public ApiResponse<List<UserProgressDTO>> getWrongQuestions(
            @Parameter(description = "用户ID") @RequestParam Integer userId) {
        return ApiResponse.success(userProgressService.getWrongQuestions(userId));
    }

    @Operation(summary = "获取用户收藏")
    @GetMapping("/favorites")
    public ApiResponse<List<UserProgressDTO>> getFavorites(
            @Parameter(description = "用户ID") @RequestParam Integer userId) {
        return ApiResponse.success(userProgressService.getFavorites(userId));
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @Parameter(description = "用户ID") @RequestParam Integer userId) {
        return ApiResponse.success(userProgressService.getStatistics(userId));
    }

    @Operation(summary = "更新学习进度")
    @PostMapping
    public ApiResponse<UserProgressDTO> updateProgress(
            @Parameter(description = "用户ID") @RequestParam Integer userId,
            @Valid @RequestBody UpdateProgressDTO dto) {
        return ApiResponse.success("更新成功", userProgressService.updateProgress(userId, dto));
    }

    @Operation(summary = "重置题目进度")
    @DeleteMapping("/reset")
    public ApiResponse<Void> resetProgress(
            @Parameter(description = "用户ID") @RequestParam Integer userId,
            @Parameter(description = "题目ID") @RequestParam Long questionId) {
        userProgressService.resetProgress(userId, questionId);
        return ApiResponse.success("重置成功", null);
    }
}
