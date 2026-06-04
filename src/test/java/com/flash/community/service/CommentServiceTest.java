package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.dto.CommentTreeDTO;
import com.flash.community.entity.Article;
import com.flash.community.entity.Comment;
import com.flash.community.entity.CommentLike;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.CommentLikeRepository;
import com.flash.community.repository.CommentRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getArticleComments_returnsTree() {
        Article article = new Article();
        article.setId(1L);
        User author = new User();
        author.setId(10L);
        author.setNickname("Author");

        Comment root = new Comment();
        root.setId(1L);
        root.setContent("Root comment");
        root.setArticle(article);
        root.setAuthor(author);
        root.setLikeCount(0);

        when(commentRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(1L)).thenReturn(List.of(root));
        when(commentRepository.findByArticleIdAndParentIdIsNotNull(1L)).thenReturn(List.of());

        List<CommentTreeDTO> result = commentService.getArticleComments(1L, "oldest");

        assertEquals(1, result.size());
        assertEquals("Root comment", result.get(0).getContent());
    }

    @Test
    void getArticleComments_newestSort_usesDescOrder() {
        Article article = new Article();
        article.setId(1L);
        User author = new User();
        author.setId(10L);
        author.setNickname("Author");

        Comment root = new Comment();
        root.setId(1L);
        root.setContent("Root");
        root.setArticle(article);
        root.setAuthor(author);
        root.setLikeCount(0);

        when(commentRepository.findByArticleIdAndParentIdIsNullOrderByCreatedAtDesc(1L)).thenReturn(List.of(root));
        when(commentRepository.findByArticleIdAndParentIdIsNotNull(1L)).thenReturn(List.of());

        List<CommentTreeDTO> result = commentService.getArticleComments(1L, "newest");

        assertEquals(1, result.size());
    }

    @Test
    void createComment_success_createsAndIncrementsCount() {
        Article article = new Article();
        article.setId(1L);
        article.setCommentCount(5);
        User author = new User();
        author.setId(10L);
        article.setAuthor(author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));

        Comment saved = new Comment();
        saved.setId(100L);
        saved.setContent("test comment");
        saved.setArticle(article);
        saved.setAuthor(author);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Comment result = commentService.createComment("test comment", 1L, 10L, null);

        assertEquals("test comment", result.getContent());
        assertEquals(6, article.getCommentCount());
        verify(commentRepository).save(any(Comment.class));
        verify(articleRepository).save(article);
    }

    @Test
    void createComment_articleNotExists_throwsException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class,
                () -> commentService.createComment("content", 99L, 1L, null));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_userNotExists_throwsException() {
        Article article = new Article();
        article.setId(1L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> commentService.createComment("content", 1L, 99L, null));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_withParent_setsParent() {
        Article article = new Article();
        article.setId(1L);
        User author = new User();
        author.setId(10L);
        article.setAuthor(author);
        Comment parent = new Comment();
        parent.setId(50L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));
        when(commentRepository.findById(50L)).thenReturn(Optional.of(parent));

        Comment saved = new Comment();
        saved.setId(100L);
        saved.setParent(parent);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Comment result = commentService.createComment("reply", 1L, 10L, 50L);

        assertNotNull(result.getParent());
        assertEquals(50L, result.getParent().getId());
        verify(commentRepository).findById(50L);
    }

    @Test
    void createComment_parentNotExists_throwsException() {
        Article article = new Article();
        article.setId(1L);
        User author = new User();
        author.setId(10L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> commentService.createComment("reply", 1L, 10L, 99L));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_ownComment_updatesContent() {
        User author = new User();
        author.setId(1L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Old");
        comment.setAuthor(author);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Comment updated = new Comment();
        updated.setId(1L);
        updated.setContent("New");
        updated.setAuthor(author);
        when(commentRepository.save(any(Comment.class))).thenReturn(updated);

        Comment result = commentService.updateComment(1L, 1L, "New");

        assertEquals("New", result.getContent());
    }

    @Test
    void updateComment_notOwner_throwsException() {
        User owner = new User();
        owner.setId(10L);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(owner);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(BusinessException.class,
                () -> commentService.updateComment(1L, 99L, "New"));
    }

    @Test
    void deleteComment_ownComment_deletes() {
        User author = new User();
        author.setId(1L);
        Article article = new Article();
        article.setId(1L);
        article.setCommentCount(3);
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(author);
        comment.setArticle(article);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        commentService.deleteComment(1L, 1L);

        verify(commentRepository).delete(comment);
        assertEquals(2, article.getCommentCount());
    }

    @Test
    void toggleLike_addsLike() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setLikeCount(0);
        User author = new User();
        author.setId(10L);
        comment.setAuthor(author);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.findByCommentIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        boolean result = commentService.toggleLike(1L, 1L);

        assertTrue(result);
        assertEquals(1, comment.getLikeCount());
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    void toggleLike_removesLike() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setLikeCount(1);
        CommentLike like = new CommentLike();
        like.setCommentId(1L);
        like.setUserId(1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.findByCommentIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));

        boolean result = commentService.toggleLike(1L, 1L);

        assertFalse(result);
        assertEquals(0, comment.getLikeCount());
        verify(commentLikeRepository).delete(like);
    }
}
