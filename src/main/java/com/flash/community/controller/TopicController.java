package com.flash.community.controller;

import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Topic;
import com.flash.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ApiResponse<List<Topic>> list() {
        return ApiResponse.success(topicService.getAllTopics());
    }
}
