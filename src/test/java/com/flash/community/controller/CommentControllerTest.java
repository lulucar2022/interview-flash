package com.flash.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.dto.CommentCreateRequest;
import com.flash.community.entity.Comment;
import com.flash.community.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

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
    void list_returnsComments() throws Exception {
        Page<Comment> page = new PageImpl<>(List.of());
        when(commentService.getArticleComments(1L, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/articles/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void create_validRequest_returnsComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Nice article!");

        Comment saved = new Comment();
        saved.setId(100L);
        saved.setContent("Nice article!");
        when(commentService.createComment(eq("Nice article!"), eq(1L), eq(1L), isNull()))
                .thenReturn(saved);

        mockMvc.perform(post("/api/articles/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Nice article!"));
    }

    @Test
    void create_emptyContent_returnsBadRequest() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("");

        mockMvc.perform(post("/api/articles/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
