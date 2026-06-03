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
import java.util.List;
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

    public Page<Article> listArticles(int page, int size, Long topicId) {
        log.debug("listArticles: page={}, size={}, topicId={}", page, size, topicId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Article> result;
        if (topicId != null) {
            result = articleRepository.findByTopicIdAndStatus(topicId, Article.ArticleStatus.PUBLISHED, pageable);
        } else {
            result = articleRepository.findByStatusOrderByCreatedAtDesc(Article.ArticleStatus.PUBLISHED, pageable);
        }
        result.forEach(this::populateTags);
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

    public Page<Article> getHotArticles(int page, int size) {
        log.debug("getHotArticles: page={}, size={}", page, size);
        Page<Article> result = articleRepository.findHotArticles(PageRequest.of(page, size));
        result.forEach(this::populateTags);
        return result;
    }

    @Transactional
    public Article getArticle(Long id) {
        log.debug("getArticle: id={}", id);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        articleRepository.incrementViewCount(id);
        article.setViewCount(article.getViewCount() + 1);
        populateTags(article);
        return article;
    }

    @Transactional
    public Article createArticle(String title, String content, Long userId, Long topicId, String[] tagNames, Article.ArticleStatus status) {
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
    public Article updateArticle(Long id, Long userId, String title, String content, Long topicId, String[] tagNames) {
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

    public Page<Article> search(String keyword, int page, int size) {
        log.debug("search: keyword={}, page={}, size={}", keyword, page, size);
        Page<Article> result = articleRepository.searchByKeyword(keyword, PageRequest.of(page, size));
        result.forEach(this::populateTags);
        return result;
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
