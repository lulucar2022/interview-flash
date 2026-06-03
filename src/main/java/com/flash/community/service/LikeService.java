package com.flash.community.service;

import com.flash.community.entity.Article;
import com.flash.community.entity.ArticleLike;
import com.flash.community.repository.ArticleLikeRepository;
import com.flash.community.repository.ArticleRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final NotificationService notificationService;

    @Transactional
    public boolean toggleLike(Long articleId, Long userId) {
        log.debug("toggleLike: articleId={}, userId={}", articleId, userId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("文章不存在"));

        boolean liked = articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(like -> {
                    articleLikeRepository.delete(like);
                    article.setThumbsUpCount(article.getThumbsUpCount() - 1);
                    articleRepository.save(article);
                    log.info("Unliked: articleId={}, userId={}", articleId, userId);
                    return false;
                })
                .orElseGet(() -> {
                    ArticleLike like = new ArticleLike();
                    like.setArticleId(articleId);
                    like.setUserId(userId);
                    articleLikeRepository.save(like);
                    article.setThumbsUpCount(article.getThumbsUpCount() + 1);
                    articleRepository.save(article);
                    log.info("Liked: articleId={}, userId={}", articleId, userId);

                    if (!article.getAuthor().getId().equals(userId)) {
                        notificationService.createNotification(
                                article.getAuthor().getId(),
                                "like",
                                "赞了你的文章",
                                userId
                        );
                    }
                    return true;
                });
        return liked;
    }

    public boolean hasLiked(Long articleId, Long userId) {
        return articleLikeRepository.existsByArticleIdAndUserId(articleId, userId);
    }

    public long getLikeCount(Long articleId) {
        return articleLikeRepository.countByArticleId(articleId);
    }
}
