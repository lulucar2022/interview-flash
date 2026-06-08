package com.flash.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.service.ArticleService;
import com.flash.community.service.FollowService;
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
    private final ArticleService articleService;
    private final FollowService followService;

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

    @GetMapping("/article-views")
    public ApiResponse<List<Map<String, Object>>> getArticleViewTrend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(articleService.getArticleViewTrend(userDetails.getId(), days));
    }

    @GetMapping("/article-views/total")
    public ApiResponse<Long> getTotalArticleViews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(articleService.getTotalArticleViews(userDetails.getId()));
    }

    @GetMapping("/follower-trend")
    public ApiResponse<List<Map<String, Object>>> getFollowerTrend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(followService.getFollowerTrend(userDetails.getId(), days));
    }
}
