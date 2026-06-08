package com.flash.community.controller;

import com.flash.auth.entity.User;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.auth.repository.UserRepository;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Blacklist;
import com.flash.community.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;
    private final UserRepository userRepository;

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
        List<Blacklist> entries = blacklistService.getBlockedEntries(userDetails.getId());
        List<Map<String, Object>> result = entries.stream().map(e -> {
            User u = userRepository.findById(e.getBlockedId()).orElse(null);
            return Map.<String, Object>of(
                    "id", e.getId(),
                    "userId", e.getBlockedId(),
                    "nickname", u != null ? u.getNickname() : "已注销",
                    "avatarUrl", u != null ? u.getAvatarUrl() : null,
                    "createdAt", e.getCreatedAt()
            );
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }
}
