package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.service.BlacklistService;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BlacklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlacklistService blacklistService;

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
    void toggle_block_returnsTrue() throws Exception {
        when(blacklistService.toggleBlock(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/block/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocked").value(true));
    }

    @Test
    void toggle_unblock_returnsFalse() throws Exception {
        when(blacklistService.toggleBlock(1L, 2L)).thenReturn(false);

        mockMvc.perform(post("/api/block/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocked").value(false));
    }

    @Test
    void status_blocked_returnsTrue() throws Exception {
        when(blacklistService.isBlocked(1L, 2L)).thenReturn(true);

        mockMvc.perform(get("/api/block/2/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocked").value(true));
    }

    @Test
    void status_notBlocked_returnsFalse() throws Exception {
        when(blacklistService.isBlocked(1L, 2L)).thenReturn(false);

        mockMvc.perform(get("/api/block/2/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocked").value(false));
    }

    @Test
    void list_returnsBlockedUsers() throws Exception {
        List<Map<String, Object>> blockedList = Collections.emptyList();
        when(blacklistService.getBlockedUsersInfo(1L)).thenReturn(blockedList);

        mockMvc.perform(get("/api/block/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
