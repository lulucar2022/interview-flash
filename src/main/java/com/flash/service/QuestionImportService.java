package com.flash.service;

import com.flash.dto.ImportResult;
import com.flash.entity.Category;
import com.flash.entity.Question;
import com.flash.repository.CategoryRepository;
import com.flash.repository.QuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionImportService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> VALID_TYPES = Set.of(
            "SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE",
            "FILL_BLANK", "SHORT_ANSWER", "CODING", "SCENARIO");
    private static final Set<String> VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");
    private static final String[] TEMPLATE_HEADERS = {
            "title", "content", "answer", "type", "difficulty", "categoryName", "options"
    };

    @Transactional
    public ImportResult importFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            ImportResult r = ImportResult.empty();
            r.getErrors().add("文件名为空");
            return r;
        }

        if (filename.endsWith(".xlsx")) {
            return importExcel(file);
        } else if (filename.endsWith(".json")) {
            return importJson(file);
        } else {
            ImportResult r = ImportResult.empty();
            r.getErrors().add("不支持的文件格式，请上传 .xlsx 或 .json 文件");
            return r;
        }
    }

    private ImportResult importExcel(MultipartFile file) {
        ImportResult result = new ImportResult();
        Map<String, Category> categoryCache = new HashMap<>();
        int rowCount = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                rowCount++;

                try {
                    String title = getCellString(row, 0);
                    String content = getCellString(row, 1);
                    String answer = getCellString(row, 2);
                    String type = getCellString(row, 3);
                    String difficulty = getCellString(row, 4);
                    String categoryName = getCellString(row, 5);
                    String options = getCellString(row, 6);

                    if (title.isEmpty() || content.isEmpty()) {
                        result.addError(rowCount, "title 或 content 不能为空");
                        continue;
                    }

                    QuestionsImportRow importRow = new QuestionsImportRow();
                    importRow.category = getCategoryByName(categoryName, categoryCache);
                    importRow.title = title;
                    importRow.content = content;
                    importRow.answer = answer;
                    importRow.type = type;
                    importRow.difficulty = difficulty;
                    importRow.options = options;

                    Question question = buildQuestion(importRow);
                    questionRepository.save(question);
                    result.addSuccess();
                } catch (Exception e) {
                    result.addError(rowCount, e.getMessage());
                }
            }

        } catch (Exception e) {
            result.getErrors().add("Excel 解析失败: " + e.getMessage());
        }

        result.setTotal(rowCount);
        return result;
    }

    private ImportResult importJson(MultipartFile file) {
        ImportResult result = new ImportResult();
        Map<String, Category> categoryCache = new HashMap<>();

        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                    file.getInputStream(), new TypeReference<>() {});
            result.setTotal(items.size());

            for (int i = 0; i < items.size(); i++) {
                try {
                    Map<String, Object> item = items.get(i);
                    QuestionsImportRow row = objectMapper.convertValue(item, QuestionsImportRow.class);
                    row.category = getCategoryByName(
                            String.valueOf(item.getOrDefault("categoryName", "")), categoryCache);

                    Question question = buildQuestion(row);
                    questionRepository.save(question);
                    result.addSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            result.getErrors().add("JSON 解析失败: " + e.getMessage());
        }
        return result;
    }

    private Question buildQuestion(QuestionsImportRow row) {
        if (row.category == null) {
            throw new RuntimeException("分类不存在: " + row.categoryName);
        }

        String type = row.type.toUpperCase().trim();
        if (!VALID_TYPES.contains(type)) {
            throw new RuntimeException("无效的题型: " + row.type + "，可选: " + String.join(",", VALID_TYPES));
        }

        String difficulty = row.difficulty.toUpperCase().trim();
        if (!VALID_DIFFICULTIES.contains(difficulty)) {
            throw new RuntimeException("无效的难度: " + row.difficulty + "，可选: EASY,MEDIUM,HARD");
        }

        Question question = new Question();
        question.setTitle(row.title);
        question.setContent(row.content);
        question.setAnswer(row.answer);
        question.setType(Question.QuestionType.valueOf(type));
        question.setDifficulty(Question.Difficulty.valueOf(difficulty));
        question.setCategory(row.category);
        if (row.options != null && !row.options.trim().isEmpty()) {
            question.setOptions(row.options.trim());
        }
        return question;
    }

    private Category getCategoryByName(String name, Map<String, Category> cache) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("分类名称为空");
        }
        return cache.computeIfAbsent(name.trim(), key ->
                categoryRepository.findByName(key)
                        .orElseThrow(() -> new RuntimeException("分类不存在: " + key)));
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    // 内部 DTO 用于 ObjectMapper 转换
    private static class QuestionsImportRow {
        public String title;
        public String content;
        public String answer;
        public String type;
        public String difficulty;
        public String categoryName;
        public String options;
        public Category category;
    }

    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("题库导入模板");
            // 顶部说明行
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
                sheet.autoSizeColumn(i);
            }

            // 示例行
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("HashMap 底层原理");
            sample.createCell(1).setCellValue("请解释 HashMap 的 put 流程");
            sample.createCell(2).setCellValue("1. 计算 hash\n2. 定位桶\n3. 插入/更新");
            sample.createCell(3).setCellValue("SHORT_ANSWER");
            sample.createCell(4).setCellValue("MEDIUM");
            sample.createCell(5).setCellValue("Java基础");
            sample.createCell(6).setCellValue("[{\"label\":\"A\",\"content\":\"选项A\"}]");

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("模板生成失败", e);
        }
    }
}
