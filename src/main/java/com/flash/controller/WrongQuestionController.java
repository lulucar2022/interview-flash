package com.flash.controller;

import com.flash.dto.ApiResponse;
import com.flash.dto.WrongQuestionDTO;
import com.flash.service.WrongQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题本Controller
 */
@RestController
@RequestMapping("/api/wrong")
@RequiredArgsConstructor
@Tag(name = "错题本", description = "错题本相关接口")
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @Operation(summary = "获取用户错题列表")
    @GetMapping
    public ApiResponse<List<WrongQuestionDTO>> getUserWrongQuestions(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        return ApiResponse.success(wrongQuestionService.getUserWrongQuestions(userId));
    }

    @Operation(summary = "获取错题数量")
    @GetMapping("/count")
    public ApiResponse<Long> getWrongQuestionCount(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        return ApiResponse.success(wrongQuestionService.getWrongQuestionCount(userId));
    }

    @Operation(summary = "移除错题")
    @DeleteMapping
    public ApiResponse<Void> removeWrongQuestion(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "题目ID", required = true) @RequestParam Long questionId) {
        wrongQuestionService.removeWrongQuestion(userId, questionId);
        return ApiResponse.success("已从错题本移除", null);
    }

    @Operation(summary = "记录答题并自动处理错题本")
    @PostMapping("/record")
    public ApiResponse<Void> recordAnswer(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "题目ID", required = true) @RequestParam Long questionId,
            @Parameter(description = "用户答案") @RequestParam(required = false) String userAnswer,
            @Parameter(description = "是否正确", required = true) @RequestParam Boolean isCorrect) {
        wrongQuestionService.recordAnswer(userId, questionId, userAnswer, isCorrect);
        if (isCorrect) {
            return ApiResponse.success("回答正确，已从错题本移除", null);
        } else {
            return ApiResponse.success("回答错误，已加入错题本", null);
        }
    }
}