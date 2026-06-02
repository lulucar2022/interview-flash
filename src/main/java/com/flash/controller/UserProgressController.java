package com.flash.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.dto.UpdateProgressDTO;
import com.flash.dto.UserProgressDTO;
import com.flash.service.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Tag(name = "用户进度", description = "用户学习进度相关接口")
public class UserProgressController {

    private final UserProgressService userProgressService;

    @GetMapping
    public ApiResponse<List<UserProgressDTO>> getUserProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userProgressService.getUserProgress(userDetails.getId()));
    }

    @GetMapping("/question")
    public ApiResponse<UserProgressDTO> getProgressByQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long questionId) {
        return ApiResponse.success(userProgressService.getProgressByQuestion(userDetails.getId(), questionId));
    }

    @GetMapping("/wrong")
    public ApiResponse<List<UserProgressDTO>> getWrongQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userProgressService.getWrongQuestions(userDetails.getId()));
    }

    @GetMapping("/favorites")
    public ApiResponse<List<UserProgressDTO>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userProgressService.getFavorites(userDetails.getId()));
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userProgressService.getStatistics(userDetails.getId()));
    }

    @PostMapping
    public ApiResponse<UserProgressDTO> updateProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProgressDTO dto) {
        return ApiResponse.success("更新成功", userProgressService.updateProgress(userDetails.getId(), dto));
    }

    @DeleteMapping("/reset")
    public ApiResponse<Void> resetProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long questionId) {
        userProgressService.resetProgress(userDetails.getId(), questionId);
        return ApiResponse.success("重置成功", null);
    }
}
