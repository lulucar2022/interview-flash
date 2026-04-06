package com.flash.controller;

import com.flash.dto.CreateQuestionDTO;
import com.flash.dto.QuestionDTO;
import com.flash.entity.Question;
import com.flash.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "题目管理", description = "题目相关接口")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "获取题目列表", description = "支持按分类和难度筛选，分页返回")
    @GetMapping
    public ResponseEntity<Page<QuestionDTO>> getQuestions(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "难度") @RequestParam(required = false) String difficulty,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (categoryId != null) {
            if (difficulty != null) {
                return ResponseEntity.ok(questionService.getQuestionsByCategory(
                        categoryId, pageable));
            }
            return ResponseEntity.ok(questionService.getQuestionsByCategory(
                    categoryId, pageable));
        } else if (difficulty != null) {
            return ResponseEntity.ok(questionService.getQuestionsByDifficulty(
                    Question.Difficulty.valueOf(difficulty), pageable));
        }
        
        return ResponseEntity.ok(questionService.searchQuestions("", pageable));
    }

    @Operation(summary = "随机获取题目")
    @GetMapping("/random")
    public ResponseEntity<QuestionDTO> getRandomQuestion(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "难度") @RequestParam(required = false) String difficulty) {
        
        QuestionDTO question = questionService.getRandomQuestion(
                categoryId, 
                difficulty != null ? Question.Difficulty.valueOf(difficulty) : null);
        
        return question != null ? ResponseEntity.ok(question) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "搜索题目")
    @GetMapping("/search")
    public ResponseEntity<Page<QuestionDTO>> searchQuestions(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(questionService.searchQuestions(keyword, pageable));
    }

    @Operation(summary = "根据ID获取题目")
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestionById(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @Operation(summary = "创建题目")
    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody CreateQuestionDTO dto) {
        return ResponseEntity.ok(questionService.createQuestion(dto));
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id,
            @Valid @RequestBody CreateQuestionDTO dto) {
        return ResponseEntity.ok(questionService.updateQuestion(id, dto));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
