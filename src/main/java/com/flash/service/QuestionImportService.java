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
        if (row.title == null || row.title.trim().isEmpty()) {
            throw new RuntimeException("题目标题不能为空");
        }
        if (row.content == null || row.content.trim().isEmpty()) {
            throw new RuntimeException("题目内容不能为空");
        }
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

            // ── 样式 ──
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle hintStyle = workbook.createCellStyle();
            Font hintFont = workbook.createFont();
            hintFont.setItalic(true);
            hintFont.setFontHeightInPoints((short) 10);
            hintStyle.setFont(hintFont);
            hintStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            hintStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle normalStyle = workbook.createCellStyle();
            normalStyle.setWrapText(true);

            String[] headers = {"title", "content", "answer", "type", "difficulty", "categoryName", "options"};
            String[] hints = {
                "题目标签（必填，≤500字）",
                "题干内容（必填）",
                "参考答案（选填）",
                "题型枚举：SINGLE_CHOICE, MULTIPLE_CHOICE,\nTRUE_FALSE, FILL_BLANK, SHORT_ANSWER,\nCODING, SCENARIO",
                "难度枚举：EASY, MEDIUM, HARD",
                "分类名称（需与系统中已有\n分类完全匹配）",
                "选项JSON（单选/多选/判断必填）\n格式: [{\"label\":\"A\",\"content\":\"...\"}]"
            };

            // 7 种题型示例数据
            Object[][] samples = {
                {"什么是Java多态", "请解释Java多态的概念和实现方式", "多态是指同一个行为...", "SHORT_ANSWER", "MEDIUM", "Java基础", ""},
                {"下面哪个是Java关键字", "A.public  B.main  C.printf  D.scan", "A", "SINGLE_CHOICE", "EASY", "Java基础", "[{\"label\":\"A\",\"content\":\"public\"},{\"label\":\"B\",\"content\":\"main\"},{\"label\":\"C\",\"content\":\"printf\"},{\"label\":\"D\",\"content\":\"scan\"}]"},
                {"哪些是关系型数据库", "A.MySQL  B.Redis  C.PostgreSQL  D.MongoDB", "A,C", "MULTIPLE_CHOICE", "MEDIUM", "数据库", "[{\"label\":\"A\",\"content\":\"MySQL\"},{\"label\":\"B\",\"content\":\"Redis\"},{\"label\":\"C\",\"content\":\"PostgreSQL\"},{\"label\":\"D\",\"content\":\"MongoDB\"}]"},
                {"Spring Boot 自动配置默认开启", "Spring Boot 的自动配置默认是启用的", "正确", "TRUE_FALSE", "EASY", "Spring框架", "[{\"label\":\"A\",\"content\":\"正确\"},{\"label\":\"B\",\"content\":\"错误\"}]"},
                {"HashMap 默认初始容量", "HashMap 默认初始容量为____", "16", "FILL_BLANK", "MEDIUM", "Java基础", ""},
                {"反转单链表", "实现反转单链表的函数", "public ListNode ...", "CODING", "HARD", "算法与数据结构", ""},
                {"设计秒杀系统", "请设计一个高并发秒杀系统的架构", "1. 前端限流 2. ...", "SCENARIO", "HARD", "系统设计", ""}
            };

            // ── 第 0 行：标题行 ──
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── 第 1 行：备注说明行 ──
            Row hintRow = sheet.createRow(1);
            for (int i = 0; i < hints.length; i++) {
                Cell cell = hintRow.createCell(i);
                cell.setCellValue(hints[i]);
                cell.setCellStyle(hintStyle);
            }

            // ── 第 2~8 行：7 种题型示例 ──
            for (int r = 0; r < samples.length; r++) {
                Row row = sheet.createRow(r + 2);
                for (int c = 0; c < samples[r].length; c++) {
                    Cell cell = row.createCell(c);
                    Object val = samples[r][c];
                    cell.setCellValue(val != null ? val.toString() : "");
                    cell.setCellStyle(normalStyle);
                }
            }

            // ── 列宽 ──
            int[] colWidths = {30, 40, 30, 20, 14, 18, 40};
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }
            // 备注行和样例行设置行高（支持多行显示）
            hintRow.setHeight((short) (hintRow.getHeight() * 6));
            for (int r = 2; r < 2 + samples.length; r++) {
                sheet.getRow(r).setHeight((short) (sheet.getRow(r).getHeight() * 3));
            }

            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("模板生成失败", e);
        }
    }
}
