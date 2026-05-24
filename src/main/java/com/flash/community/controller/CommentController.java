package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Comment;
import com.flash.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<List<Comment>> list(@PathVariable Long articleId) {
        return ApiResponse.success(commentService.getArticleComments(articleId));
    }

    @PostMapping
    public ApiResponse<Comment> create(
            @PathVariable Long articleId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            commentService.createComment(content, articleId, userDetails.getId(), parentId));
    }
}
