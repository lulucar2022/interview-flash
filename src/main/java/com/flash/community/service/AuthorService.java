package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.dto.ArticleDTO;
import com.flash.community.dto.AuthorProfileResponse;
import com.flash.community.entity.Article;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.FollowRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final FollowRepository followRepository;

    public AuthorProfileResponse getProfile(Long userId, Long currentUserId) {
        log.debug("getProfile: userId={}, currentUserId={}", userId, currentUserId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        long articleCount = articleRepository.countByAuthorIdAndStatus(userId, Article.ArticleStatus.PUBLISHED);
        long totalViews = articleRepository.sumViewCountByAuthorId(userId);
        long totalLikes = articleRepository.sumThumbsUpCountByAuthorId(userId);
        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByUserId(userId);
        boolean isFollowing = currentUserId != null && followRepository.existsByUserIdAndFollowingId(currentUserId, userId);

        Page<ArticleDTO> recentArticles = articleRepository.findByAuthorIdAndStatus(userId, Article.ArticleStatus.PUBLISHED, PageRequest.of(0, 10))
                .map(ArticleDTO::from);

        return new AuthorProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCreatedAt(),
                articleCount,
                totalViews,
                totalLikes,
                followerCount,
                followingCount,
                isFollowing,
                recentArticles
        );
    }

    public Page<ArticleDTO> getArticles(Long userId, int page, int size, Long currentUserId) {
        log.debug("getArticles: userId={}, page={}, size={}", userId, page, size);
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        return articleRepository.findByAuthorIdAndStatus(userId, Article.ArticleStatus.PUBLISHED, PageRequest.of(page, size))
                .map(ArticleDTO::from);
    }
}
