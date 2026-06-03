package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Follow;
import com.flash.community.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public ApiResponse<Map<String, Object>> toggle(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean following = followService.toggleFollow(userDetails.getId(), userId);
        return ApiResponse.success(Map.of("following", following));
    }

    @GetMapping("/{userId}/followers")
    public ApiResponse<List<Follow>> followers(@PathVariable Long userId) {
        return ApiResponse.success(followService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ApiResponse<List<Follow>> following(@PathVariable Long userId) {
        return ApiResponse.success(followService.getFollowing(userId));
    }

    @GetMapping("/{userId}/status")
    public ApiResponse<Map<String, Object>> status(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean following = followService.isFollowing(userDetails.getId(), userId);
        return ApiResponse.success(Map.of("following", following));
    }
}
