package com.flash.community.service;

import com.flash.community.entity.Article;
import com.flash.community.entity.Bookmark;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public boolean toggleBookmark(Long userId, Long articleId) {
        var existing = bookmarkRepository.findByUserIdAndArticleId(userId, articleId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        }
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(userId);
        bookmark.setArticle(article);
        bookmarkRepository.save(bookmark);
        return true;
    }

    public boolean isBookmarked(Long userId, Long articleId) {
        return bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    public Page<Bookmark> getUserBookmarks(Long userId, int page, int size) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }
}
