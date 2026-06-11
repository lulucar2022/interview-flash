package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Article;
import com.flash.community.entity.Series;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.SeriesRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeriesServiceTest {

    @Mock
    private SeriesRepository seriesRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SeriesService seriesService;

    private Series createSeries(Long id, Long userId, String title) {
        Series series = new Series();
        series.setId(id);
        series.setUserId(userId);
        series.setTitle(title);
        series.setDescription("Description");
        series.setArticleCount(0);
        return series;
    }

    @Test
    void getAllSeries_returnsAll() {
        List<Series> list = List.of(createSeries(1L, 1L, "Series 1"));
        when(seriesRepository.findAll()).thenReturn(list);

        List<Series> result = seriesService.getAllSeries();

        assertEquals(1, result.size());
    }

    @Test
    void getUserSeries_returnsUserSeries() {
        List<Series> list = List.of(createSeries(1L, 1L, "My Series"));
        when(seriesRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(list);

        List<Series> result = seriesService.getUserSeries(1L);

        assertEquals(1, result.size());
        assertEquals("My Series", result.get(0).getTitle());
    }

    @Test
    void getSeries_exists_returnsSeries() {
        Series series = createSeries(1L, 1L, "Test");
        when(seriesRepository.findById(1L)).thenReturn(Optional.of(series));

        Series result = seriesService.getSeries(1L);

        assertEquals("Test", result.getTitle());
    }

    @Test
    void getSeries_notExists_throwsException() {
        when(seriesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> seriesService.getSeries(99L));
    }

    @Test
    void createSeries_success() {
        User author = new User();
        author.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        Series saved = createSeries(1L, 1L, "New Series");
        when(seriesRepository.save(any(Series.class))).thenReturn(saved);

        Series result = seriesService.createSeries(1L, "New Series", "Desc", null);

        assertEquals("New Series", result.getTitle());
        verify(seriesRepository).save(any(Series.class));
    }

    @Test
    void createSeries_userNotExists_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> seriesService.createSeries(99L, "Title", "Desc", null));
    }

    @Test
    void updateSeries_owned_returnsUpdated() {
        Series series = createSeries(1L, 1L, "Old Title");
        when(seriesRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(series));
        Series updated = createSeries(1L, 1L, "New Title");
        when(seriesRepository.save(any(Series.class))).thenReturn(updated);

        Series result = seriesService.updateSeries(1L, 1L, "New Title", "New Desc", "img.jpg");

        assertEquals("New Title", result.getTitle());
    }

    @Test
    void updateSeries_notOwned_throwsException() {
        when(seriesRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> seriesService.updateSeries(1L, 2L, "Title", "Desc", null));
    }

    @Test
    void deleteSeries_owned_deletesAndCleansArticles() {
        Series series = createSeries(1L, 1L, "To Delete");
        when(seriesRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(series));
        Article article = new Article();
        article.setId(1L);
        article.setSeries(series);
        article.setSeriesOrder(1);
        when(articleRepository.findBySeriesId(1L)).thenReturn(List.of(article));

        seriesService.deleteSeries(1L, 1L);

        assertNull(article.getSeries());
        assertNull(article.getSeriesOrder());
        verify(articleRepository).saveAll(anyList());
        verify(seriesRepository).delete(series);
    }

    @Test
    void deleteSeries_notOwned_throwsException() {
        when(seriesRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> seriesService.deleteSeries(1L, 2L));
    }

    @Test
    void assignArticle_toSeries_success() {
        Article article = new Article();
        User author = new User();
        author.setId(1L);
        article.setId(100L);
        article.setAuthor(author);
        when(articleRepository.findById(100L)).thenReturn(Optional.of(article));
        Series series = createSeries(1L, 1L, "Target");
        when(seriesRepository.findById(1L)).thenReturn(Optional.of(series));
        when(articleRepository.countBySeriesId(1L)).thenReturn(1L);

        seriesService.assignArticle(100L, 1L, 1L, 1);

        assertEquals(series, article.getSeries());
        assertEquals(1, article.getSeriesOrder());
        verify(articleRepository).save(article);
        verify(seriesRepository).save(series);
    }

    @Test
    void assignArticle_articleNotExists_throwsException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> seriesService.assignArticle(99L, 1L, 1L, 1));
    }

    @Test
    void assignArticle_notOwned_throwsException() {
        Article article = new Article();
        User otherAuthor = new User();
        otherAuthor.setId(2L);
        article.setId(100L);
        article.setAuthor(otherAuthor);
        when(articleRepository.findById(100L)).thenReturn(Optional.of(article));

        assertThrows(BusinessException.class, () -> seriesService.assignArticle(100L, 1L, 1L, 1));
    }

    @Test
    void assignArticle_removeFromSeries_success() {
        Article article = new Article();
        User author = new User();
        author.setId(1L);
        article.setId(100L);
        article.setAuthor(author);
        Series oldSeries = createSeries(2L, 1L, "Old");
        article.setSeries(oldSeries);
        article.setSeriesOrder(1);
        when(articleRepository.findById(100L)).thenReturn(Optional.of(article));
        when(articleRepository.countBySeriesId(2L)).thenReturn(2L);

        seriesService.assignArticle(100L, 1L, null, null);

        assertNull(article.getSeries());
        assertNull(article.getSeriesOrder());
        verify(articleRepository).save(article);
        verify(seriesRepository).save(oldSeries);
    }

    @Test
    void getSeriesArticles_returnsPage() {
        Page<Article> page = new PageImpl<>(Collections.emptyList());
        when(articleRepository.findBySeriesIdAndStatusOrderBySeriesOrderAscCreatedAtDesc(
                1L, Article.ArticleStatus.PUBLISHED, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<Article> result = seriesService.getSeriesArticles(1L, 0, 20);

        assertEquals(0, result.getTotalElements());
    }
}
