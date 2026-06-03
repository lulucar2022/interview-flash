package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

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
    void toggle_follow_returnsFollowing() throws Exception {
        when(followService.toggleFollow(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/follow/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));
    }

    @Test
    void toggle_unfollow_returnsNotFollowing() throws Exception {
        when(followService.toggleFollow(1L, 2L)).thenReturn(false);

        mockMvc.perform(post("/api/follow/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));
    }

    @Test
    void status_following_returnsTrue() throws Exception {
        when(followService.isFollowing(1L, 2L)).thenReturn(true);

        mockMvc.perform(get("/api/follow/2/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));
    }

    @Test
    void status_notFollowing_returnsFalse() throws Exception {
        when(followService.isFollowing(1L, 2L)).thenReturn(false);

        mockMvc.perform(get("/api/follow/2/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));
    }

    @Test
    void followers_returnsList() throws Exception {
        when(followService.getFollowers(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/follow/1/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void following_returnsList() throws Exception {
        when(followService.getFollowing(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/follow/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
