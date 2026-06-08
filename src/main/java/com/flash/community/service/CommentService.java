package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.dto.CommentTreeDTO;
import com.flash.community.entity.*;
import com.flash.community.repository.*;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<CommentTreeDTO> getArticleComments(Long articleId, String sort) {
        log.debug("getArticleComments: articleId={}, sort={}", articleId, sort);

        List<Comment> rootComments;
        if ("newest".equals(sort)) {
            rootComments = commentRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtDesc(articleId);
        } else {
            rootComments = commentRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(articleId);
        }

        List<Comment> allChildComments = commentRepository.findByArticleIdAndParentIdIsNotNull(articleId);
        Map<Long, List<Comment>> childMap = allChildComments.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return rootComments.stream()
                .map(c -> toTreeDTO(c, childMap, null))
                .collect(Collectors.toList());
    }

    public List<CommentTreeDTO> getArticleCommentsWithLikes(Long articleId, String sort, Long currentUserId) {
        List<CommentTreeDTO> tree = getArticleComments(articleId, sort);

        Set<Long> likedCommentIds = currentUserId != null
                ? commentRepository.findByArticleIdAndParentIdIsNotNull(articleId).stream()
                    .filter(c -> commentLikeRepository.existsByCommentIdAndUserId(c.getId(), currentUserId))
                    .map(Comment::getId)
                    .collect(Collectors.toSet())
                : Set.of();

        likedCommentIds.addAll(
            commentRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(articleId).stream()
                .filter(c -> commentLikeRepository.existsByCommentIdAndUserId(c.getId(), currentUserId))
                .map(Comment::getId)
                .collect(Collectors.toSet())
        );

        applyLikes(tree, likedCommentIds);
        return tree;
    }

    private void applyLikes(List<CommentTreeDTO> comments, Set<Long> likedIds) {
        for (CommentTreeDTO dto : comments) {
            dto.setLiked(likedIds.contains(dto.getId()));
            if (dto.getChildren() != null) {
                applyLikes(dto.getChildren(), likedIds);
            }
        }
    }

    private CommentTreeDTO toTreeDTO(Comment comment, Map<Long, List<Comment>> childMap, Long currentUserId) {
        CommentTreeDTO dto = new CommentTreeDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorNickname(comment.getAuthor().getNickname());
        dto.setAuthorAvatarUrl(comment.getAuthor().getAvatarUrl());
        dto.setArticleId(comment.getArticle().getId());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);
        dto.setLiked(currentUserId != null && commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUserId));
        dto.setCreatedAt(comment.getCreatedAt());

        List<Comment> children = childMap.getOrDefault(comment.getId(), List.of());
        dto.setChildren(children.stream()
                .map(c -> toTreeDTO(c, childMap, currentUserId))
                .collect(Collectors.toList()));

        return dto;
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
                    userId,
                    articleId
            );
        }
        return comment;
    }

    @Transactional
    public Comment updateComment(Long commentId, Long userId, String content) {
        log.debug("updateComment: id={}, userId={}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BusinessException(403, "无权编辑该评论");
        }
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);
        log.info("Comment updated: id={}", commentId);
        return saved;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.debug("deleteComment: id={}, userId={}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BusinessException(403, "无权删除该评论");
        }

        List<Comment> children = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        for (Comment child : children) {
            child.setParent(null);
            commentRepository.save(child);
        }

        Article article = comment.getArticle();
        int deletedCount = 1 + children.size();
        article.setCommentCount(Math.max(0, article.getCommentCount() - deletedCount));
        articleRepository.save(article);

        commentRepository.delete(comment);
        log.info("Comment deleted: id={}", commentId);
    }

    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        log.debug("toggleLike: commentId={}, userId={}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        Optional<CommentLike> existing = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
        if (existing.isPresent()) {
            commentLikeRepository.delete(existing.get());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentRepository.save(comment);
            log.info("Comment unliked: commentId={}, userId={}", commentId, userId);
            return false;
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentRepository.save(comment);
            log.info("Comment liked: commentId={}, userId={}", commentId, userId);

            if (!comment.getAuthor().getId().equals(userId)) {
                notificationService.createNotification(
                        comment.getAuthor().getId(),
                        "like",
                        "赞了你的评论",
                        userId,
                        commentId
                );
            }
            return true;
        }
    }

    public boolean hasLiked(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }
}
