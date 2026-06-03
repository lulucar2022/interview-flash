package com.flash.community.dto;

import com.flash.community.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthorProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private long articleCount;
    private long totalViews;
    private long totalLikes;
    private long followerCount;
    private long followingCount;
    private boolean isFollowing;
    private Page<Article> recentArticles;
}
