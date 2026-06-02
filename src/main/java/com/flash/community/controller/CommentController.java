package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.dto.CommentCreateRequest;
import com.flash.community.entity.Comment;
import com.flash.community.service.CommentService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            commentService.createComment(request.getContent(), articleId,
                userDetails.getId(), request.getParentId()));
    }
}
