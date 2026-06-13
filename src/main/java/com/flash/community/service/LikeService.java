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

        var existingLike = articleLikeRepository.findByArticleIdAndUserId(articleId, userId);
        if (existingLike.isPresent()) {
            // 取消点赞：删除记录 + 原子递减计数
            articleLikeRepository.delete(existingLike.get());
            articleRepository.decrementThumbsUpCount(articleId);
            log.info("Unliked: articleId={}, userId={}", articleId, userId);
            return false;
        } else {
            // 点赞：创建记录 + 原子递增计数
            ArticleLike like = new ArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            articleLikeRepository.save(like);
            articleRepository.incrementThumbsUpCount(articleId);
            log.info("Liked: articleId={}, userId={}", articleId, userId);

            if (!article.getAuthor().getId().equals(userId)) {
                notificationService.createNotification(
                        article.getAuthor().getId(),
                        "like",
                        "赞了你的文章",
                        userId,
                        articleId
                );
            }
            return true;
        }
    }

    public boolean hasLiked(Long articleId, Long userId) {
        return articleLikeRepository.existsByArticleIdAndUserId(articleId, userId);
    }

    public long getLikeCount(Long articleId) {
        return articleLikeRepository.countByArticleId(articleId);
    }
}
