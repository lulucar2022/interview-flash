package com.flash.auth.service;

import com.flash.auth.dto.AuthResponse;
import com.flash.auth.dto.LoginRequest;
import com.flash.auth.dto.RegisterRequest;
import com.flash.auth.entity.Role;
import com.flash.auth.entity.User;
import com.flash.auth.jwt.JwtTokenProvider;
import com.flash.auth.repository.RoleRepository;
import com.flash.auth.repository.UserRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User buildUser(String username, String password, boolean enabled) {
        Role role = new Role();
        role.setCode("USER");
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword(password);
        user.setNickname(username);
        user.setEnabled(enabled);
        user.setRole(role);
        return user;
    }

    // ── Login Tests ──

    @Test
    void login_success_returnsTokenAndUserDTO() {
        User user = buildUser("alice", "encoded-pw", true);
        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-pw", "encoded-pw")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "alice", "USER")).thenReturn("jwt-token");

        LoginRequest req = new LoginRequest();
        req.setAccount("alice");
        req.setPassword("raw-pw");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("jwt-token", res.getToken());
        verify(jwtTokenProvider).generateToken(1L, "alice", "USER");
    }

    @Test
    void login_wrongPassword_throwsException() {
        User user = buildUser("alice", "encoded-pw", true);
        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-pw", "encoded-pw")).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setAccount("alice");
        req.setPassword("bad-pw");

        assertThrows(BusinessException.class, () -> authService.login(req));
        verify(jwtTokenProvider, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void login_userDisabled_throwsException() {
        User user = buildUser("alice", "encoded-pw", false);
        when(userRepository.findByUsernameOrEmail("alice", "alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-pw", "encoded-pw")).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setAccount("alice");
        req.setPassword("raw-pw");

        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByUsernameOrEmail("ghost", "ghost")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setAccount("ghost");
        req.setPassword("pw");

        assertThrows(BusinessException.class, () -> authService.login(req));
    }

    // ── Register Tests ──

    @Test
    void register_success_returnsToken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        Role role = new Role();
        role.setCode("USER");
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtTokenProvider.generateToken(anyLong(), anyString(), anyString())).thenReturn("jwt");

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@test.com");
        req.setPassword("pw");
        req.setNickname("New User");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("jwt", res.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("taken");
        req.setEmail("a@b.com");
        req.setPassword("pw");

        assertThrows(BusinessException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("taken@test.com");
        req.setPassword("pw");

        assertThrows(BusinessException.class, () -> authService.register(req));
    }

    // ── Toggle Status ──

    @Test
    void toggleUserStatus_flipsEnabled() {
        User user = buildUser("alice", "pw", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.toggleUserStatus(1L);

        assertFalse(user.getEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserStatus_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.toggleUserStatus(99L));
    }
}
