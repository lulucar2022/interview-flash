package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.*;
import com.flash.community.repository.*;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final TopicRepository topicRepository;
    private final ArticleTagRepository articleTagRepository;
    private final UserRepository userRepository;
    private final BlacklistRepository blacklistRepository;
    private final ArticleDailyViewRepository articleDailyViewRepository;
    private final SeriesRepository seriesRepository;

    public Page<Article> listArticles(int page, int size, Long topicId, Long currentUserId) {
        log.debug("listArticles: page={}, size={}, topicId={}", page, size, topicId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Article> result;
        if (topicId != null) {
            result = articleRepository.findByTopicIdAndStatus(topicId, Article.ArticleStatus.PUBLISHED, pageable);
        } else {
            result = articleRepository.findByStatusOrderByCreatedAtDesc(Article.ArticleStatus.PUBLISHED, pageable);
        }
        result.forEach(this::populateTags);
        if (currentUserId != null) {
            List<Long> blockedIds = blacklistRepository.findBlockedUserIdsByBlockerId(currentUserId);
            if (!blockedIds.isEmpty()) {
                List<Article> filtered = result.stream()
                        .filter(a -> !blockedIds.contains(a.getAuthor().getId()))
                        .collect(Collectors.toList());
                result = new PageImpl<>(filtered, result.getPageable(), filtered.size());
            }
        }
        return result;
    }

    public Page<Article> getMyArticles(Long userId, int page, int size) {
        log.debug("getMyArticles: userId={}, page={}, size={}", userId, page, size);
        Page<Article> result = articleRepository.findByAuthorIdAndStatus(userId, Article.ArticleStatus.PUBLISHED, PageRequest.of(page, size));
        result.forEach(this::populateTags);
        return result;
    }

    public Page<Article> getMyDrafts(Long userId, int page, int size) {
        log.debug("getMyDrafts: userId={}, page={}, size={}", userId, page, size);
        Page<Article> result = articleRepository.findByAuthorIdAndStatus(userId, Article.ArticleStatus.DRAFT, PageRequest.of(page, size));
        result.forEach(this::populateTags);
        return result;
    }

    public Page<Article> getHotArticles(int page, int size, Long currentUserId) {
        log.debug("getHotArticles: page={}, size={}", page, size);
        Page<Article> result = articleRepository.findHotArticles(PageRequest.of(page, size));
        result.forEach(this::populateTags);
        if (currentUserId != null) {
            List<Long> blockedIds = blacklistRepository.findBlockedUserIdsByBlockerId(currentUserId);
            if (!blockedIds.isEmpty()) {
                List<Article> filtered = result.stream()
                        .filter(a -> !blockedIds.contains(a.getAuthor().getId()))
                        .collect(Collectors.toList());
                result = new PageImpl<>(filtered, result.getPageable(), filtered.size());
            }
        }
        return result;
    }

    @Transactional
    public Article getArticle(Long id, Long currentUserId) {
        log.debug("getArticle: id={}", id);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        if (currentUserId != null) {
            List<Long> blockedIds = blacklistRepository.findBlockedUserIdsByBlockerId(currentUserId);
            if (blockedIds.contains(article.getAuthor().getId())) {
                throw new BusinessException("文章不可见");
            }
        }
        articleRepository.incrementViewCount(id);
        article.setViewCount(article.getViewCount() + 1);
        recordDailyView(article.getAuthor().getId());
        populateTags(article);
        return article;
    }

    @Transactional
    public Article createArticle(String title, String content, Long userId, Long topicId, String[] tagNames, Article.ArticleStatus status, Long seriesId, Integer seriesOrder) {
        log.debug("createArticle: title={}, userId={}, topicId={}, status={}", title, userId, topicId, status);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setStatus(status != null ? status : Article.ArticleStatus.PUBLISHED);
        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new BusinessException("话题不存在"));
            article.setTopic(topic);
        }
        if (seriesId != null) {
            Series series = seriesRepository.findById(seriesId)
                    .orElseThrow(() -> new BusinessException("系列不存在"));
            if (!series.getUserId().equals(userId)) {
                throw new BusinessException("无权使用该系列");
            }
            article.setSeries(series);
            article.setSeriesOrder(seriesOrder);
            series.setArticleCount((int) articleRepository.countBySeriesId(seriesId) + 1);
            seriesRepository.save(series);
        }
        article = articleRepository.save(article);
        log.info("Article created: id={}, title={}", article.getId(), title);

        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setTagName(tagName);
                            return tagRepository.save(newTag);
                        });
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tag.getId());
                articleTagRepository.save(at);
                tag.setArticleCount(tag.getArticleCount() + 1);
                tagRepository.save(tag);
                log.debug("Tag attached: articleId={}, tagName={}", article.getId(), tagName);
            }
        }
        return article;
    }

    @Transactional
    public Article updateArticle(Long id, Long userId, String title, String content, Long topicId, String[] tagNames, Long seriesId, Integer seriesOrder) {
        log.debug("updateArticle: id={}, userId={}", id, userId);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        if (!article.getAuthor().getId().equals(userId)) {
            throw new BusinessException(403, "无权编辑该文章");
        }

        article.setTitle(title);
        article.setContent(content);
        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new BusinessException("话题不存在"));
            article.setTopic(topic);
        } else {
            article.setTopic(null);
        }

        Series oldSeries = article.getSeries();
        if (seriesId != null) {
            Series series = seriesRepository.findById(seriesId)
                    .orElseThrow(() -> new BusinessException("系列不存在"));
            if (!series.getUserId().equals(userId)) {
                throw new BusinessException("无权使用该系列");
            }
            article.setSeries(series);
            article.setSeriesOrder(seriesOrder);
        } else {
            article.setSeries(null);
            article.setSeriesOrder(null);
        }
        if (oldSeries != null && !oldSeries.equals(article.getSeries())) {
            oldSeries.setArticleCount((int) articleRepository.countBySeriesId(oldSeries.getId()));
            seriesRepository.save(oldSeries);
        }
        if (seriesId != null) {
            Series series = seriesRepository.findById(seriesId).get();
            series.setArticleCount((int) articleRepository.countBySeriesId(seriesId));
            seriesRepository.save(series);
        }

        List<ArticleTag> oldTags = articleTagRepository.findByArticleId(id);
        for (ArticleTag at : oldTags) {
            tagRepository.findById(at.getTagId()).ifPresent(tag -> {
                tag.setArticleCount(Math.max(0, tag.getArticleCount() - 1));
                tagRepository.save(tag);
            });
        }
        articleTagRepository.deleteByArticleId(id);

        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setTagName(tagName);
                            return tagRepository.save(newTag);
                        });
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tag.getId());
                articleTagRepository.save(at);
                tag.setArticleCount(tag.getArticleCount() + 1);
                tagRepository.save(tag);
                log.debug("Tag attached: articleId={}, tagName={}", article.getId(), tagName);
            }
        }

        article = articleRepository.save(article);
        populateTags(article);
        log.info("Article updated: id={}, title={}", id, title);
        return article;
    }

    @Transactional
    public void deleteArticle(Long id, Long userId) {
        log.debug("deleteArticle: id={}, userId={}", id, userId);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        if (!article.getAuthor().getId().equals(userId)) {
            throw new BusinessException(403, "无权删除该文章");
        }

        List<ArticleTag> oldTags = articleTagRepository.findByArticleId(id);
        for (ArticleTag at : oldTags) {
            tagRepository.findById(at.getTagId()).ifPresent(tag -> {
                tag.setArticleCount(Math.max(0, tag.getArticleCount() - 1));
                tagRepository.save(tag);
            });
        }
        articleTagRepository.deleteByArticleId(id);
        articleRepository.delete(article);
        log.info("Article deleted: id={}", id);
    }

    public Page<Article> search(String keyword, int page, int size, Long currentUserId) {
        log.debug("search: keyword={}, page={}, size={}", keyword, page, size);
        Page<Article> result = articleRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        result.forEach(this::populateTags);
        if (currentUserId != null) {
            List<Long> blockedIds = blacklistRepository.findBlockedUserIdsByBlockerId(currentUserId);
            if (!blockedIds.isEmpty()) {
                List<Article> filtered = result.stream()
                        .filter(a -> !blockedIds.contains(a.getAuthor().getId()))
                        .collect(Collectors.toList());
                result = new PageImpl<>(filtered, result.getPageable(), filtered.size());
            }
        }
        return result;
    }

    @Transactional
    public void recordDailyView(Long userId) {
        LocalDate today = LocalDate.now();
        articleDailyViewRepository.findByUserIdAndDate(userId, today)
                .ifPresentOrElse(
                        v -> v.setCount(v.getCount() + 1),
                        () -> {
                            ArticleDailyView v = new ArticleDailyView();
                            v.setUserId(userId);
                            v.setDate(today);
                            v.setCount(1);
                            articleDailyViewRepository.save(v);
                        }
                );
    }

    public List<Map<String, Object>> getArticleViewTrend(Long userId, int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        List<ArticleDailyView> records = articleDailyViewRepository
                .findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);

        Map<String, Long> recordMap = new LinkedHashMap<>();
        for (ArticleDailyView adv : records) {
            recordMap.put(adv.getDate().toString(), adv.getCount().longValue());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String date = from.plusDays(i).toString();
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", date);
            entry.put("count", recordMap.getOrDefault(date, 0L));
            result.add(entry);
        }
        return result;
    }

    public Long getTotalArticleViews(Long userId) {
        return articleRepository.sumViewCountByAuthorId(userId);
    }

    private void populateTags(Article article) {
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(article.getId());
        if (articleTags.isEmpty()) {
            article.setTags("");
            return;
        }
        List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).toList();
        List<Tag> tags = tagRepository.findAllById(tagIds);
        article.setTags(tags.stream().map(Tag::getTagName).collect(Collectors.joining(",")));
    }
}
