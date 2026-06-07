package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Bookmark;
import com.flash.community.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{articleId}")
    public ApiResponse<Map<String, Boolean>> toggle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean bookmarked = bookmarkService.toggleBookmark(userDetails.getId(), articleId);
        return ApiResponse.success(Map.of("bookmarked", bookmarked));
    }

    @GetMapping
    public ApiResponse<Page<Bookmark>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookmarkService.getUserBookmarks(userDetails.getId(), page, size));
    }

    @GetMapping("/{articleId}/status")
    public ApiResponse<Map<String, Boolean>> status(
            @PathVariable Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean bookmarked = bookmarkService.isBookmarked(userDetails.getId(), articleId);
        return ApiResponse.success(Map.of("bookmarked", bookmarked));
    }
}
