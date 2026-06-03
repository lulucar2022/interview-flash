package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Article;
import com.flash.community.entity.Comment;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.CommentRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getArticleComments_returnsPage() {
        Page<Comment> page = new PageImpl<>(List.of());
        when(commentRepository.findByArticleIdOrderByCreatedAtAsc(eq(1L), any(Pageable.class))).thenReturn(page);
        Page<Comment> result = commentService.getArticleComments(1L, 0, 20);
        verify(commentRepository).findByArticleIdOrderByCreatedAtAsc(eq(1L), any(Pageable.class));
        assertEquals(0, result.getTotalElements());
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
}
