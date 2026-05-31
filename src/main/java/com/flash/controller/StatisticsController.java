package com.flash.controller;

import com.flash.dto.ApiResponse;
import com.flash.service.StatisticsService;
import lombok.RequiredArgsConstructor;
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
            @RequestParam Long userId,
            @RequestParam(defaultValue = "365") int days) {
        return ApiResponse.success(statisticsService.getDailyStats(userId, days));
    }

    @GetMapping("/streak")
    public ApiResponse<Map<String, Object>> getStreak(@RequestParam Long userId) {
        return ApiResponse.success(statisticsService.getStreak(userId));
    }

    @GetMapping("/category")
    public ApiResponse<List<Map<String, Object>>> getCategoryStats(@RequestParam Long userId) {
        return ApiResponse.success(statisticsService.getCategoryStats(userId));
    }
}
