package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.dto.CommentCreateRequest;
import com.flash.community.entity.Comment;
import com.flash.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<Page<Comment>> list(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(commentService.getArticleComments(articleId, page, size));
    }

    @PostMapping
    public ApiResponse<Comment> create(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            commentService.createComment(request.getContent(), articleId,
                userDetails.getId(), request.getParentId()));
    }
}
