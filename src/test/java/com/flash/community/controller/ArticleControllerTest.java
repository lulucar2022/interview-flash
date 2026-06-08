package com.flash.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.dto.ArticleCreateRequest;
import com.flash.community.entity.Article;
import com.flash.community.entity.Article.ArticleStatus;
import com.flash.community.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleService articleService;

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
    void list_withoutTopicId_returnsPage() throws Exception {
        when(articleService.listArticles(0, 20, null, 1L))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void list_withTopicId_returnsFiltered() throws Exception {
        when(articleService.listArticles(0, 20, 1L, 1L))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/articles").param("topicId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void detail_exists_returnsArticle() throws Exception {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test");
        when(articleService.getArticle(1L, 1L)).thenReturn(article);

        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test"));
    }

    @Test
    void detail_notExists_returnsError() throws Exception {
        when(articleService.getArticle(99L, 1L))
                .thenThrow(new com.flash.common.exception.BusinessException("文章不存在"));

        mockMvc.perform(get("/api/articles/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_validRequest_returnsArticle() throws Exception {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("New Article");
        request.setContent("New Content");

        Article saved = new Article();
        saved.setId(10L);
        saved.setTitle("New Article");
        saved.setContent("New Content");
        when(articleService.createArticle(eq("New Article"), eq("New Content"), eq(1L), isNull(), isNull(), eq(ArticleStatus.PUBLISHED)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void create_emptyTitle_returnsBadRequest() throws Exception {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("");
        request.setContent("Content");

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_emptyContent_returnsBadRequest() throws Exception {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("Title");
        request.setContent("");

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
