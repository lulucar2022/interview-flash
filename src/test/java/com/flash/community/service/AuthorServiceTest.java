package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.dto.ArticleDTO;
import com.flash.community.dto.AuthorProfileResponse;
import com.flash.community.entity.Article;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.FollowRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private AuthorService authorService;

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setNickname("TestUser");
        user.setAvatarUrl("http://avatar.url");
        user.setBio("A bio");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Test
    void getProfile_userExists_returnsProfile() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(articleRepository.countByAuthorIdAndStatus(1L, Article.ArticleStatus.PUBLISHED)).thenReturn(5L);
        when(articleRepository.sumViewCountByAuthorId(1L)).thenReturn(100L);
        when(articleRepository.sumThumbsUpCountByAuthorId(1L)).thenReturn(20L);
        when(followRepository.countByFollowingId(1L)).thenReturn(10L);
        when(followRepository.countByUserId(1L)).thenReturn(3L);
        when(followRepository.existsByUserIdAndFollowingId(1L, 1L)).thenReturn(false);
        Page<Article> articlePage = new PageImpl<>(Collections.emptyList());
        when(articleRepository.findByAuthorIdAndStatus(1L, Article.ArticleStatus.PUBLISHED, PageRequest.of(0, 10)))
                .thenReturn(articlePage);

        AuthorProfileResponse profile = authorService.getProfile(1L, 1L);

        assertEquals(1L, profile.getId());
        assertEquals("TestUser", profile.getNickname());
        assertEquals(5, profile.getArticleCount());
        assertEquals(100, profile.getTotalViews());
        assertEquals(20, profile.getTotalLikes());
        assertEquals(10, profile.getFollowerCount());
        assertEquals(3, profile.getFollowingCount());
        assertFalse(profile.isFollowing());
    }

    @Test
    void getProfile_userNotExists_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authorService.getProfile(99L, null));
    }

    @Test
    void getProfile_withCurrentUser_checksFollow() {
        User user = createUser(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(articleRepository.countByAuthorIdAndStatus(2L, Article.ArticleStatus.PUBLISHED)).thenReturn(0L);
        when(articleRepository.sumViewCountByAuthorId(2L)).thenReturn(0L);
        when(articleRepository.sumThumbsUpCountByAuthorId(2L)).thenReturn(0L);
        when(followRepository.countByFollowingId(2L)).thenReturn(0L);
        when(followRepository.countByUserId(2L)).thenReturn(0L);
        when(followRepository.existsByUserIdAndFollowingId(1L, 2L)).thenReturn(true);
        Page<Article> articlePage = new PageImpl<>(Collections.emptyList());
        when(articleRepository.findByAuthorIdAndStatus(2L, Article.ArticleStatus.PUBLISHED, PageRequest.of(0, 10)))
                .thenReturn(articlePage);

        AuthorProfileResponse profile = authorService.getProfile(2L, 1L);

        assertTrue(profile.isFollowing());
    }

    @Test
    void getArticles_userExists_returnsPage() {
        when(userRepository.existsById(1L)).thenReturn(true);
        Page<Article> articlePage = new PageImpl<>(Collections.emptyList());
        when(articleRepository.findByAuthorIdAndStatus(1L, Article.ArticleStatus.PUBLISHED, PageRequest.of(0, 10)))
                .thenReturn(articlePage);

        Page<ArticleDTO> result = authorService.getArticles(1L, 0, 10, null);

        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getArticles_userNotExists_throwsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> authorService.getArticles(99L, 0, 10, null));
    }
}
