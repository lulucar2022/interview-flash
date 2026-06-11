package com.flash.community.service;

import com.flash.community.entity.Article;
import com.flash.community.entity.Bookmark;
import com.flash.community.repository.ArticleRepository;
import com.flash.community.repository.BookmarkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    @Test
    void toggleBookmark_firstTime_returnsTrue() {
        when(bookmarkRepository.findByUserIdAndArticleId(1L, 100L)).thenReturn(Optional.empty());
        Article article = new Article();
        article.setId(100L);
        when(articleRepository.findById(100L)).thenReturn(Optional.of(article));

        boolean result = bookmarkService.toggleBookmark(1L, 100L);

        assertTrue(result);
        verify(bookmarkRepository).save(any(Bookmark.class));
        verify(bookmarkRepository, never()).delete(any());
    }

    @Test
    void toggleBookmark_alreadyBookmarked_returnsFalse() {
        Bookmark existing = new Bookmark();
        existing.setUserId(1L);
        existing.setArticle(new Article());
        when(bookmarkRepository.findByUserIdAndArticleId(1L, 100L)).thenReturn(Optional.of(existing));

        boolean result = bookmarkService.toggleBookmark(1L, 100L);

        assertFalse(result);
        verify(bookmarkRepository).delete(existing);
        verify(bookmarkRepository, never()).save(any());
    }

    @Test
    void toggleBookmark_articleNotExists_throwsException() {
        when(bookmarkRepository.findByUserIdAndArticleId(1L, 99L)).thenReturn(Optional.empty());
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookmarkService.toggleBookmark(1L, 99L));
    }

    @Test
    void isBookmarked_exists_returnsTrue() {
        when(bookmarkRepository.existsByUserIdAndArticleId(1L, 100L)).thenReturn(true);

        assertTrue(bookmarkService.isBookmarked(1L, 100L));
    }

    @Test
    void isBookmarked_notExists_returnsFalse() {
        when(bookmarkRepository.existsByUserIdAndArticleId(1L, 100L)).thenReturn(false);

        assertFalse(bookmarkService.isBookmarked(1L, 100L));
    }

    @Test
    void getUserBookmarks_returnsPage() {
        Page<Bookmark> page = new PageImpl<>(Collections.emptyList());
        when(bookmarkRepository.findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<Bookmark> result = bookmarkService.getUserBookmarks(1L, 0, 20);

        assertEquals(0, result.getTotalElements());
    }
}
