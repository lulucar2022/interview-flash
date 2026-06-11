package com.flash.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.dto.CommentCreateRequest;
import com.flash.community.dto.CommentDTO;
import com.flash.community.dto.CommentTreeDTO;
import com.flash.community.dto.CommentUpdateRequest;
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
    void list_returnsCommentTree() throws Exception {
        when(commentService.getArticleCommentsWithLikes(eq(1L), eq("oldest"), eq(1L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/articles/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void create_validRequest_returnsComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent("Nice article!");

        CommentDTO saved = new CommentDTO(100L, "Nice article!", 1L, "testuser", null, 1L, null, 0, null, null);
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

    @Test
    void update_validRequest_returnsUpdated() throws Exception {
        CommentUpdateRequest req = new CommentUpdateRequest();
        req.setContent("Updated content");

        CommentDTO updated = new CommentDTO(1L, "Updated content", 1L, "testuser", null, 1L, null, 0, null, null);
        when(commentService.updateComment(eq(1L), eq(1L), eq("Updated content"))).thenReturn(updated);

        mockMvc.perform(put("/api/articles/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void delete_validRequest_returnsSuccess() throws Exception {
        doNothing().when(commentService).deleteComment(1L, 1L);

        mockMvc.perform(delete("/api/articles/1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void like_toggle_returnsStatus() throws Exception {
        when(commentService.toggleLike(1L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/articles/1/comments/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
    }
}
