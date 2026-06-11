package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.dto.ArticleDTO;
import com.flash.community.entity.Article;
import com.flash.community.entity.Article.ArticleStatus;
import com.flash.community.entity.Topic;
import com.flash.community.repository.*;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private ArticleTagRepository articleTagRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BlacklistRepository blacklistRepository;
    @Mock
    private ArticleDailyViewRepository articleDailyViewRepository;
    @Mock
    private SeriesRepository seriesRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void getArticle_exists_incrementsViewCount() {
        User author = new User();
        author.setId(2L);
        Article article = new Article();
        article.setId(1L);
        article.setViewCount(10);
        article.setAuthor(author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleTagRepository.findByArticleId(1L)).thenReturn(List.of());
        when(articleDailyViewRepository.findByUserIdAndDate(anyLong(), any()))
                .thenReturn(Optional.empty());

        ArticleDTO result = articleService.getArticle(1L, null);

        assertEquals(1L, result.getId());
        verify(articleRepository).incrementViewCount(1L);
    }

    @Test
    void getArticle_notExists_throwsException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> articleService.getArticle(99L, null));
        verify(articleRepository, never()).incrementViewCount(any());
    }

    @Test
    void createArticle_basic_createsAndReturns() {
        User author = new User();
        author.setId(1L);
        author.setNickname("testuser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        Article saved = new Article();
        saved.setId(10L);
        saved.setTitle("Test Title");
        saved.setContent("Test Content");
        saved.setAuthor(author);
        saved.setStatus(Article.ArticleStatus.PUBLISHED);
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleDTO result = articleService.createArticle("Test Title", "Test Content", 1L, null, null, ArticleStatus.PUBLISHED, null, null);

        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("PUBLISHED", result.getStatus());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void createArticle_userNotExists_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> articleService.createArticle("Title", "Content", 99L, null, null, ArticleStatus.PUBLISHED, null, null));
        verify(articleRepository, never()).save(any());
    }

    @Test
    void createArticle_withTopic_setsTopic() {
        User author = new User();
        author.setId(1L);
        author.setNickname("testuser");
        Topic topic = new Topic();
        topic.setId(2L);
        topic.setTopicName("Java");
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(topicRepository.findById(2L)).thenReturn(Optional.of(topic));

        Article saved = new Article();
        saved.setId(10L);
        saved.setTitle("Title");
        saved.setContent("Content");
        saved.setAuthor(author);
        saved.setTopic(topic);
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleDTO result = articleService.createArticle("Title", "Content", 1L, 2L, null, ArticleStatus.PUBLISHED, null, null);

        assertNotNull(result.getTopic());
        assertEquals(2L, result.getTopic().getId());
    }

    @Test
    void createArticle_topicNotExists_throwsException() {
        User author = new User();
        author.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(topicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> articleService.createArticle("Title", "Content", 1L, 99L, null, ArticleStatus.PUBLISHED, null, null));
    }

    @Test
    void listArticles_withTopicId_filtersByTopic() {
        Page<Article> page = new PageImpl<>(List.of());
        when(articleRepository.findByTopicIdAndStatus(eq(1L), eq(Article.ArticleStatus.PUBLISHED), any())).thenReturn(page);
        articleService.listArticles(0, 10, 1L, null);
        verify(articleRepository).findByTopicIdAndStatus(eq(1L), eq(Article.ArticleStatus.PUBLISHED), any());
    }

    @Test
    void listArticles_withoutTopicId_returnsAll() {
        Page<Article> page = new PageImpl<>(List.of());
        when(articleRepository.findByStatusOrderByCreatedAtDesc(eq(Article.ArticleStatus.PUBLISHED), any())).thenReturn(page);
        articleService.listArticles(0, 10, null, null);
        verify(articleRepository).findByStatusOrderByCreatedAtDesc(eq(Article.ArticleStatus.PUBLISHED), any());
    }

    @Test
    void searchByKeyword_delegates() {
        Page<Article> page = new PageImpl<>(List.of());
        when(articleRepository.searchByKeyword(eq("test"), any(Pageable.class))).thenReturn(page);
        articleService.search("test", 0, 10, null);
        verify(articleRepository).searchByKeyword(eq("test"), any(Pageable.class));
    }

    @Test
    void getHotArticles_delegates() {
        Page<Article> page = new PageImpl<>(List.of());
        when(articleRepository.findHotArticles(any())).thenReturn(page);
        articleService.getHotArticles(0, 10, null);
        verify(articleRepository).findHotArticles(any());
    }

    // ── Boundary & Permission Tests ──

    @Test
    void getArticle_blockedAuthor_throwsException() {
        User author = new User();
        author.setId(2L);
        Article article = new Article();
        article.setId(1L);
        article.setViewCount(0);
        article.setAuthor(author);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(blacklistRepository.findBlockedUserIdsByBlockerId(10L)).thenReturn(List.of(2L));

        assertThrows(BusinessException.class, () -> articleService.getArticle(1L, 10L));
    }

    @Test
    void deleteArticle_nonOwner_throws403() {
        User owner = new User();
        owner.setId(2L);
        Article article = new Article();
        article.setId(1L);
        article.setAuthor(owner);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.deleteArticle(1L, 99L));
        assertEquals(403, ex.getCode());
    }

    @Test
    void updateArticle_nonOwner_throws403() {
        User owner = new User();
        owner.setId(2L);
        Article article = new Article();
        article.setId(1L);
        article.setAuthor(owner);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.updateArticle(1L, 99L, "title", "content", null, null, null, null));
        assertEquals(403, ex.getCode());
    }

    @Test
    void deleteArticle_notFound_throwsException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> articleService.deleteArticle(99L, 1L));
    }

    @Test
    void listArticles_blockedUsersFiltered() {
        User blockedAuthor = new User();
        blockedAuthor.setId(5L);
        Article blockedArticle = new Article();
        blockedArticle.setId(10L);
        blockedArticle.setAuthor(blockedAuthor);

        User okAuthor = new User();
        okAuthor.setId(6L);
        Article okArticle = new Article();
        okArticle.setId(11L);
        okArticle.setAuthor(okAuthor);

        Page<Article> page = new PageImpl<>(List.of(blockedArticle, okArticle));
        when(articleRepository.findByStatusOrderByCreatedAtDesc(eq(Article.ArticleStatus.PUBLISHED), any())).thenReturn(page);
        when(blacklistRepository.findBlockedUserIdsByBlockerId(1L)).thenReturn(List.of(5L));

        Page<ArticleDTO> result = articleService.listArticles(0, 10, null, 1L);

        assertEquals(1, result.getContent().size());
        assertEquals(11L, result.getContent().get(0).getId());
    }
}
