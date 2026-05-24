package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Article;
import com.flash.community.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<Page<Article>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long topicId) {
        return ApiResponse.success(articleService.listArticles(page, size, topicId));
    }

    @GetMapping("/hot")
    public ApiResponse<Page<Article>> hot(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(articleService.getHotArticles(page, size));
    }

    @GetMapping("/search")
    public ApiResponse<Page<Article>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(articleService.search(q, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Article> detail(@PathVariable Long id) {
        return ApiResponse.success(articleService.getArticle(id));
    }

    @PostMapping
    public ApiResponse<Article> create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String[] tags,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            articleService.createArticle(title, content, userDetails.getId(), topicId, tags));
    }
}
