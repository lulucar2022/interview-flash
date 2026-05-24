package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.*;
import com.flash.community.repository.*;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final TopicRepository topicRepository;
    private final ArticleTagRepository articleTagRepository;
    private final UserRepository userRepository;

    public Page<Article> listArticles(int page, int size, Long topicId) {
        PageRequest pageable = PageRequest.of(page, size);
        if (topicId != null) {
            return articleRepository.findByTopicIdAndStatus(topicId, Article.ArticleStatus.PUBLISHED, pageable);
        }
        return articleRepository.findByStatusOrderByCreatedAtDesc(Article.ArticleStatus.PUBLISHED, pageable);
    }

    public Page<Article> getHotArticles(int page, int size) {
        return articleRepository.findHotArticles(PageRequest.of(page, size));
    }

    @Transactional
    public Article getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        article.setViewCount(article.getViewCount() + 1);
        return articleRepository.save(article);
    }

    @Transactional
    public Article createArticle(String title, String content, Long userId, Long topicId, String[] tagNames) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setStatus(Article.ArticleStatus.PUBLISHED);
        if (topicId != null) {
            Topic topic = topicRepository.findById(topicId)
                    .orElseThrow(() -> new BusinessException("话题不存在"));
            article.setTopic(topic);
        }
        article = articleRepository.save(article);

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
            }
        }
        return article;
    }

    public Page<Article> search(String keyword, int page, int size) {
        return articleRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }
}
