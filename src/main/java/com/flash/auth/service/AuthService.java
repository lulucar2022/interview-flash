package com.flash.auth.service;

import com.flash.auth.dto.*;
import com.flash.auth.entity.Role;
import com.flash.auth.entity.User;
import com.flash.auth.jwt.JwtTokenProvider;
import com.flash.auth.repository.RoleRepository;
import com.flash.auth.repository.UserRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已注册");
        }
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new BusinessException("默认角色不存在"));
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(userRole);
        user.setEnabled(true);
        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), "USER");
        return new AuthResponse(token, UserDTO.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!user.getEnabled()) {
            throw new BusinessException("账户已被禁用");
        }
        String roleCode = user.getRole() != null ? user.getRole().getCode() : "USER";
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roleCode);
        return new AuthResponse(token, UserDTO.from(user));
    }

    public UserDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return UserDTO.from(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        user = userRepository.save(user);
        return UserDTO.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Admin API ──

    public Page<AdminUserDTO> listAllForAdmin(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(AdminUserDTO::from);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }
}
