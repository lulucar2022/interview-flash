package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.entity.Bookmark;
import com.flash.community.service.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookmarkService bookmarkService;

    @BeforeEach
    void setUp() {
        CustomUserDetails user = new CustomUserDetails(1L, "testuser", "USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void toggle_bookmark_returnsTrue() throws Exception {
        when(bookmarkService.toggleBookmark(1L, 100L)).thenReturn(true);

        mockMvc.perform(post("/api/bookmarks/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarked").value(true));
    }

    @Test
    void toggle_unbookmark_returnsFalse() throws Exception {
        when(bookmarkService.toggleBookmark(1L, 100L)).thenReturn(false);

        mockMvc.perform(post("/api/bookmarks/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarked").value(false));
    }

    @Test
    void list_returnsPagedBookmarks() throws Exception {
        Page<Bookmark> page = new PageImpl<>(Collections.emptyList());
        when(bookmarkService.getUserBookmarks(1L, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void status_bookmarked_returnsTrue() throws Exception {
        when(bookmarkService.isBookmarked(1L, 100L)).thenReturn(true);

        mockMvc.perform(get("/api/bookmarks/100/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarked").value(true));
    }

    @Test
    void status_notBookmarked_returnsFalse() throws Exception {
        when(bookmarkService.isBookmarked(1L, 100L)).thenReturn(false);

        mockMvc.perform(get("/api/bookmarks/100/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarked").value(false));
    }
}
