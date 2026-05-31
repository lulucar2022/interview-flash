package com.flash.admin.controller;

import com.flash.common.dto.ApiResponse;
import com.flash.dto.ImportResult;
import com.flash.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final QuestionImportService questionImportService;

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
