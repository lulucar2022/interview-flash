package com.flash.admin.controller;

import com.flash.auth.dto.AdminUserDTO;
import com.flash.auth.service.AuthService;
import com.flash.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AuthService authService;

    @GetMapping
    public ApiResponse<Page<AdminUserDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(authService.listAllForAdmin(page, size));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id) {
        authService.toggleUserStatus(id);
        return ApiResponse.success();
    }
}
