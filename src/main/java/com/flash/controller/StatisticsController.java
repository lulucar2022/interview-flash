package com.flash.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.dto.ApiResponse;
import com.flash.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/daily")
    public ApiResponse<List<Map<String, Object>>> getDailyStats(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "365") int days) {
        return ApiResponse.success(statisticsService.getDailyStats(userDetails.getId(), days));
    }

    @GetMapping("/streak")
    public ApiResponse<Map<String, Object>> getStreak(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(statisticsService.getStreak(userDetails.getId()));
    }

    @GetMapping("/category")
    public ApiResponse<List<Map<String, Object>>> getCategoryStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(statisticsService.getCategoryStats(userDetails.getId()));
    }
}
