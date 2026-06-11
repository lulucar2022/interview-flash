package com.flash.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.entity.Article;
import com.flash.community.entity.Series;
import com.flash.community.service.SeriesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SeriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeriesService seriesService;

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
    void list_all_returnsSeries() throws Exception {
        List<Series> seriesList = Collections.singletonList(new Series());
        when(seriesService.getAllSeries()).thenReturn(seriesList);

        mockMvc.perform(get("/api/series"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void list_byUser_returnsUserSeries() throws Exception {
        List<Series> seriesList = Collections.emptyList();
        when(seriesService.getUserSeries(1L)).thenReturn(seriesList);

        mockMvc.perform(get("/api/series").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void detail_returnsSeriesAndArticles() throws Exception {
        Series series = new Series();
        series.setId(1L);
        series.setTitle("Test Series");
        Page<Article> articles = new PageImpl<>(Collections.emptyList());
        when(seriesService.getSeries(1L)).thenReturn(series);
        when(seriesService.getSeriesArticles(1L, 0, 20)).thenReturn(articles);

        mockMvc.perform(get("/api/series/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.series.id").value(1))
                .andExpect(jsonPath("$.data.articles.content").isArray());
    }

    @Test
    void create_returnsSeries() throws Exception {
        Series created = new Series();
        created.setId(1L);
        created.setTitle("New Series");
        when(seriesService.createSeries(1L, "New Series", "Description", null)).thenReturn(created);

        String body = objectMapper.writeValueAsString(
                Map.of("title", "New Series", "description", "Description"));

        mockMvc.perform(post("/api/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("New Series"));
    }

    @Test
    void update_returnsUpdatedSeries() throws Exception {
        Series updated = new Series();
        updated.setId(1L);
        updated.setTitle("Updated");
        when(seriesService.updateSeries(1L, 1L, "Updated", "New desc", null)).thenReturn(updated);

        String body = objectMapper.writeValueAsString(
                Map.of("title", "Updated", "description", "New desc"));

        mockMvc.perform(put("/api/series/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated"));
    }

    @Test
    void delete_returnsSuccess() throws Exception {
        doNothing().when(seriesService).deleteSeries(1L, 1L);

        mockMvc.perform(delete("/api/series/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void assignArticle_returnsSuccess() throws Exception {
        doNothing().when(seriesService).assignArticle(100L, 1L, 1L, 1);

        String body = objectMapper.writeValueAsString(
                Map.of("seriesId", 1, "order", 1));

        mockMvc.perform(put("/api/series/articles/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
