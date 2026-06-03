package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.*;
import com.flash.community.repository.*;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Page<Comment> getArticleComments(Long articleId, int page, int size) {
        log.debug("getArticleComments: articleId={}, page={}, size={}", articleId, page, size);
        return commentRepository.findByArticleIdOrderByCreatedAtAsc(articleId, PageRequest.of(page, size));
    }

    @Transactional
    public Comment createComment(String content, Long articleId, Long userId, Long parentId) {
        log.debug("createComment: articleId={}, userId={}, parentId={}", articleId, userId, parentId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticle(article);
        comment.setAuthor(author);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException("父评论不存在"));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);
        article.setCommentCount(article.getCommentCount() + 1);
        articleRepository.save(article);
        log.info("Comment created: id={}, articleId={}, userId={}", comment.getId(), articleId, userId);

        if (!article.getAuthor().getId().equals(userId)) {
            notificationService.createNotification(
                    article.getAuthor().getId(),
                    "comment",
                    "评论了你的文章",
                    userId
            );
        }
        return comment;
    }
}
