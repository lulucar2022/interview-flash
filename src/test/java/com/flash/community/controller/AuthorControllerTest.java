package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.dto.ArticleDTO;
import com.flash.community.dto.AuthorProfileResponse;
import com.flash.community.service.AuthorService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

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

    private AuthorProfileResponse createProfile(Long id, String nickname) {
        return new AuthorProfileResponse(
                id, "username", nickname, "avatar", "bio", LocalDateTime.now(),
                0L, 0L, 0L, 0L, 0L, false, new PageImpl<>(Collections.emptyList())
        );
    }

    @Test
    void profile_returnsProfile() throws Exception {
        AuthorProfileResponse profile = createProfile(1L, "TestUser");
        when(authorService.getProfile(1L, 1L)).thenReturn(profile);

        mockMvc.perform(get("/api/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nickname").value("TestUser"));
    }

    @Test
    void articles_returnsPagedArticles() throws Exception {
        Page<ArticleDTO> page = new PageImpl<>(Collections.emptyList());
        when(authorService.getArticles(1L, 0, 10, 1L)).thenReturn(page);

        mockMvc.perform(get("/api/users/1/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void profile_withoutAuth_returnsProfile() throws Exception {
        SecurityContextHolder.clearContext();
        AuthorProfileResponse profile = createProfile(2L, "OtherUser");
        when(authorService.getProfile(2L, null)).thenReturn(profile);

        mockMvc.perform(get("/api/users/2/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));
    }
}
