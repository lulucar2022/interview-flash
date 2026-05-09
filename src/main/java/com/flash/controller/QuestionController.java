package com.flash.controller;

import com.flash.dto.ApiResponse;
import com.flash.dto.CreateQuestionDTO;
import com.flash.dto.QuestionDTO;
import com.flash.entity.Question;
import com.flash.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "题目管理", description = "题目相关接口")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "获取题目总数")
    @GetMapping("/count")
    public ApiResponse<Long> getTotalCount() {
        return ApiResponse.success(questionService.getTotalCount());
    }

    @Operation(summary = "获取热门题目")
    @GetMapping("/hot")
    public ApiResponse<List<QuestionDTO>> getHotQuestions(
            @Parameter(description = "数量") @RequestParam(defaultValue = "5") int size) {
        return ApiResponse.success(questionService.getHotQuestions(size));
    }

    @Operation(summary = "获取题目列表", description = "支持按分类和难度筛选，分页返回")
    @GetMapping
    public ApiResponse<Page<QuestionDTO>> getQuestions(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "难度") @RequestParam(required = false) String difficulty,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (categoryId != null) {
            return ApiResponse.success(questionService.getQuestionsByCategory(categoryId, pageable));
        } else if (difficulty != null) {
            return ApiResponse.success(questionService.getQuestionsByDifficulty(
                    Question.Difficulty.valueOf(difficulty), pageable));
        }
        
        return ApiResponse.success(questionService.searchQuestions("", pageable));
    }

    @Operation(summary = "随机获取题目")
    @GetMapping("/random")
    public ApiResponse<QuestionDTO> getRandomQuestion(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "难度") @RequestParam(required = false) String difficulty) {
        
        QuestionDTO question = questionService.getRandomQuestion(
                categoryId, 
                difficulty != null ? Question.Difficulty.valueOf(difficulty) : null);
        
        return ApiResponse.success(question);
    }

    @Operation(summary = "批量随机获取题目（智能排序）")
    @GetMapping("/random/batch")
    public ApiResponse<List<QuestionDTO>> getRandomQuestions(
            @Parameter(description = "用户ID", required = true) @RequestParam Integer userId,
            @Parameter(description = "数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "题型") @RequestParam(required = false) String type,
            @Parameter(description = "难度") @RequestParam(required = false) String difficulty) {
        Question.QuestionType questionType = type != null ? Question.QuestionType.valueOf(type) : null;
        Question.Difficulty diff = difficulty != null ? Question.Difficulty.valueOf(difficulty) : null;
        return ApiResponse.success(questionService.getRandomQuestions(userId, size, categoryId, questionType, diff));
    }

    @Operation(summary = "搜索题目")
    @GetMapping("/search")
    public ApiResponse<Page<QuestionDTO>> searchQuestions(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(questionService.searchQuestions(keyword, pageable));
    }

    @Operation(summary = "根据ID获取题目")
    @GetMapping("/{id}")
    public ApiResponse<QuestionDTO> getQuestionById(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        return ApiResponse.success(questionService.getQuestionById(id));
    }

    @Operation(summary = "创建题目")
    @PostMapping
    public ApiResponse<QuestionDTO> createQuestion(@Valid @RequestBody CreateQuestionDTO dto) {
        return ApiResponse.success("创建成功", questionService.createQuestion(dto));
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    public ApiResponse<QuestionDTO> updateQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id,
            @Valid @RequestBody CreateQuestionDTO dto) {
        return ApiResponse.success("更新成功", questionService.updateQuestion(id, dto));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.success("删除成功", null);
    }
}
