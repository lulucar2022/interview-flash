package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Follow;
import com.flash.community.service.FollowService;
import com.flash.community.dto.FollowUserDTO;
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
    public ApiResponse<List<FollowUserDTO>> followers(@PathVariable Long userId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(followService.getFollowersWithProfile(userId, userDetails.getId()));
    }

    @GetMapping("/{userId}/following")
    public ApiResponse<List<FollowUserDTO>> following(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(followService.getFollowingWithProfile(userId, userDetails.getId()));
    }

    @GetMapping("/{userId}/status")
    public ApiResponse<Map<String, Object>> status(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean following = followService.isFollowing(userDetails.getId(), userId);
        return ApiResponse.success(Map.of("following", following));
    }
}
