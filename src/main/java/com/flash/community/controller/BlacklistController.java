package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/{userId}")
    public ApiResponse<Map<String, Boolean>> toggle(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean blocked = blacklistService.toggleBlock(userDetails.getId(), userId);
        return ApiResponse.success(Map.of("blocked", blocked));
    }

    @GetMapping("/{userId}/status")
    public ApiResponse<Map<String, Boolean>> status(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean blocked = blacklistService.isBlocked(userDetails.getId(), userId);
        return ApiResponse.success(Map.of("blocked", blocked));
    }

    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(blacklistService.getBlockedUsersInfo(userDetails.getId()));
    }
}
