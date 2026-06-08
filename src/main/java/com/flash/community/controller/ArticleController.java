package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.dto.ArticleCreateRequest;
import com.flash.community.dto.ArticleUpdateRequest;
import com.flash.community.entity.Article;
import com.flash.community.service.ArticleService;
import com.flash.community.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final LikeService likeService;

    @GetMapping
    public ApiResponse<Page<Article>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long topicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(articleService.listArticles(page, size, topicId, userId));
    }

    @GetMapping("/my")
    public ApiResponse<Page<Article>> my(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(articleService.getMyArticles(userDetails.getId(), page, size));
    }

    @GetMapping("/my/drafts")
    public ApiResponse<Page<Article>> myDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(articleService.getMyDrafts(userDetails.getId(), page, size));
    }

    @GetMapping("/hot")
    public ApiResponse<Page<Article>> hot(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(articleService.getHotArticles(page, size, userId));
    }

    @GetMapping("/search")
    public ApiResponse<Page<Article>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(articleService.search(q, page, size, userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Article> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(articleService.getArticle(id, userId));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> like(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = likeService.toggleLike(id, userDetails.getId());
        long count = likeService.getLikeCount(id);
        return ApiResponse.success(Map.of("liked", liked, "count", count));
    }

    @GetMapping("/{id}/like-status")
    public ApiResponse<Map<String, Boolean>> likeStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = likeService.hasLiked(id, userDetails.getId());
        return ApiResponse.success(Map.of("liked", liked));
    }

    @PutMapping("/{id}")
    public ApiResponse<Article> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            articleService.updateArticle(id, userDetails.getId(),
                request.getTitle(), request.getContent(), request.getTopicId(), request.getTags()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        articleService.deleteArticle(id, userDetails.getId());
        return ApiResponse.success();
    }

    @PostMapping
    public ApiResponse<Article> create(
            @Valid @RequestBody ArticleCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Article.ArticleStatus status = "DRAFT".equals(request.getStatus())
                ? Article.ArticleStatus.DRAFT : Article.ArticleStatus.PUBLISHED;
        return ApiResponse.success(
            articleService.createArticle(request.getTitle(), request.getContent(),
                userDetails.getId(), request.getTopicId(), request.getTags(), status));
    }
}
