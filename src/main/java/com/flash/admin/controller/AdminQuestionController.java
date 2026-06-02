package com.flash.admin.controller;

import com.flash.common.dto.ApiResponse;
import com.flash.dto.CreateQuestionDTO;
import com.flash.dto.ImportResult;
import com.flash.dto.QuestionDTO;
import com.flash.service.QuestionImportService;
import com.flash.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "题目管理（管理员）", description = "题目的增删改和导入")
public class AdminQuestionController {

    private final QuestionImportService questionImportService;
    private final QuestionService questionService;

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
    public ApiResponse<Void> deleteQuestion(@Parameter(description = "题目ID") @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/import")
    public ApiResponse<ImportResult> importQuestions(@RequestParam("file") MultipartFile file) {
        ImportResult result = questionImportService.importFile(file);
        return ApiResponse.success(result);
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] data = questionImportService.generateTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("questions-template.xlsx").build());
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
