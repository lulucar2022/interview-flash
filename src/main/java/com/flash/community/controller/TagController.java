package com.flash.community.controller;

import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Tag;
import com.flash.community.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<Tag>> list() {
        return ApiResponse.success(tagService.getAllTags());
    }
}
