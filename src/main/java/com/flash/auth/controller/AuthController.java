package com.flash.auth.controller;

import com.flash.auth.dto.*;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.auth.service.AuthService;
import com.flash.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(authService.getCurrentUser(userDetails.getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserDTO> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(authService.updateProfile(userDetails.getId(), request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.changePassword(userDetails.getId(), request);
        return ApiResponse.success();
    }
}
