package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Article;
import com.flash.community.entity.Series;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.SeriesRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public List<Series> getAllSeries() {
        return seriesRepository.findAll();
    }

    public List<Series> getUserSeries(Long userId) {
        return seriesRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Series getSeries(Long id) {
        return seriesRepository.findById(id)
                .orElseThrow(() -> new BusinessException("系列不存在"));
    }

    @Transactional
    public Series createSeries(Long userId, String title, String description, String coverImage) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Series series = new Series();
        series.setUserId(author.getId());
        series.setTitle(title);
        series.setDescription(description);
        series.setCoverImage(coverImage);
        series.setArticleCount(0);
        series = seriesRepository.save(series);
        log.info("Series created: id={}, title={}", series.getId(), title);
        return series;
    }

    @Transactional
    public Series updateSeries(Long id, Long userId, String title, String description, String coverImage) {
        Series series = seriesRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("系列不存在或无权编辑"));
        series.setTitle(title);
        series.setDescription(description);
        series.setCoverImage(coverImage);
        series = seriesRepository.save(series);
        log.info("Series updated: id={}", id);
        return series;
    }

    @Transactional
    public void deleteSeries(Long id, Long userId) {
        Series series = seriesRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("系列不存在或无权删除"));

        List<Article> articles = articleRepository.findBySeriesId(id);
        for (Article article : articles) {
            article.setSeries(null);
            article.setSeriesOrder(null);
        }
        articleRepository.saveAll(articles);

        seriesRepository.delete(series);
        log.info("Series deleted: id={}", id);
    }

    @Transactional
    public void assignArticle(Long articleId, Long userId, Long seriesId, Integer order) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        if (!article.getAuthor().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作该文章");
        }

        if (seriesId != null) {
            Series series = seriesRepository.findById(seriesId)
                    .orElseThrow(() -> new BusinessException("系列不存在"));
            if (!series.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权将文章加入该系列");
            }

            article.setSeries(series);
            article.setSeriesOrder(order);
            series.setArticleCount((int) articleRepository.countBySeriesId(seriesId));
            seriesRepository.save(series);
        } else {
            Series oldSeries = article.getSeries();
            article.setSeries(null);
            article.setSeriesOrder(null);
            if (oldSeries != null) {
                oldSeries.setArticleCount((int) articleRepository.countBySeriesId(oldSeries.getId()) - 1);
                seriesRepository.save(oldSeries);
            }
        }
        articleRepository.save(article);
        log.info("Article {} assigned to series {} with order {}", articleId, seriesId, order);
    }

    public Page<Article> getSeriesArticles(Long seriesId, int page, int size) {
        return articleRepository.findBySeriesIdAndStatusOrderBySeriesOrderAscCreatedAtDesc(
                seriesId, Article.ArticleStatus.PUBLISHED, PageRequest.of(page, size));
    }
}
