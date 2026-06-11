package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.dto.ArticleDTO;
import com.flash.community.dto.AuthorProfileResponse;
import com.flash.community.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/{userId}/profile")
    public ApiResponse<AuthorProfileResponse> profile(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(authorService.getProfile(userId, currentUserId));
    }

    @GetMapping("/{userId}/articles")
    public ApiResponse<Page<ArticleDTO>> articles(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(authorService.getArticles(userId, page, size, currentUserId));
    }
}
