package com.flash.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.dto.WrongQuestionDTO;
import com.flash.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping
    public ApiResponse<List<WrongQuestionDTO>> getUserWrongQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(wrongQuestionService.getUserWrongQuestions(userDetails.getId()));
    }

    @GetMapping("/count")
    public ApiResponse<Long> getWrongQuestionCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(wrongQuestionService.getWrongQuestionCount(userDetails.getId()));
    }

    @DeleteMapping
    public ApiResponse<Void> removeWrongQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long questionId) {
        wrongQuestionService.removeWrongQuestion(userDetails.getId(), questionId);
        return ApiResponse.success("已从错题本移除", null);
    }

    @PostMapping("/record")
    public ApiResponse<Void> recordAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long questionId,
            @RequestParam(required = false) String userAnswer,
            @RequestParam Boolean isCorrect) {
        wrongQuestionService.recordAnswer(userDetails.getId(), questionId, userAnswer, isCorrect);
        if (isCorrect) {
            return ApiResponse.success("回答正确，已从错题本移除", null);
        } else {
            return ApiResponse.success("回答错误，已加入错题本", null);
        }
    }
}