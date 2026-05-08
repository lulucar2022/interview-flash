package com.flash.service;

import com.flash.entity.Category;
import com.flash.entity.Question;
import com.flash.exception.BusinessException;
import com.flash.repository.CategoryRepository;
import com.flash.repository.QuestionRepository;
import com.flash.repository.UserProgressRepository;
import com.flash.dto.QuestionDTO;
import com.flash.dto.CreateQuestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目管理Service
 * 负责题目的增删改查等业务逻辑
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * 获取题目总数
     */
    public long getTotalCount() {
        return questionRepository.count();
    }

    /**
     * 获取热门题目
     * 根据答题次数、独立用户数、近期活跃度、错误率加权排序
     */
    public List<QuestionDTO> getHotQuestions(int size) {
        List<Object[]> stats = userProgressRepository.getHotQuestionStats();
        if (stats.isEmpty()) {
            return questionRepository.findAll(PageRequest.of(0, size))
                    .stream().map(this::convertToDTO).collect(Collectors.toList());
        }

        long maxTotal = 0, maxUnique = 0, maxRecent = 0;
        for (Object[] row : stats) {
            long total = ((Number) row[1]).longValue();
            long unique = ((Number) row[2]).longValue();
            long recent = ((Number) row[3]).longValue();
            if (total > maxTotal) maxTotal = total;
            if (unique > maxUnique) maxUnique = unique;
            if (recent > maxRecent) maxRecent = recent;
        }

        List<long[]> scored = new ArrayList<>();
        for (Object[] row : stats) {
            long questionId = ((Number) row[0]).longValue();
            long total = ((Number) row[1]).longValue();
            long unique = ((Number) row[2]).longValue();
            long recent = ((Number) row[3]).longValue();
            long errors = ((Number) row[4]).longValue();

            double totalScore = maxTotal > 0 ? (double) total / maxTotal * 0.4 : 0;
            double uniqueScore = maxUnique > 0 ? (double) unique / maxUnique * 0.3 : 0;
            double recentScore = maxRecent > 0 ? (double) recent / maxRecent * 0.2 : 0;
            double errorRate = total > 0 ? (double) errors / total : 0;
            double errorScore = errorRate * 0.1;

            long scoreBits = Double.doubleToLongBits(totalScore + uniqueScore + recentScore + errorScore);
            scored.add(new long[]{questionId, scoreBits});
        }

        scored.sort((a, b) -> Double.compare(Double.longBitsToDouble(b[1]), Double.longBitsToDouble(a[1])));

        List<Long> topIds = scored.stream()
                .limit(size)
                .map(s -> s[0])
                .collect(Collectors.toList());

        Map<Long, Question> questionMap = questionRepository.findAllById(topIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<QuestionDTO> result = new ArrayList<>();
        for (Long id : topIds) {
            Question q = questionMap.get(id);
            if (q != null) result.add(convertToDTO(q));
        }
        return result;
    }

    /**
     * 根据分类查询题目列表
     */
    public Page<QuestionDTO> getQuestionsByCategory(Long categoryId, Pageable pageable) {
        return questionRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * 根据难度查询题目列表
     */
    public Page<QuestionDTO> getQuestionsByDifficulty(Question.Difficulty difficulty, Pageable pageable) {
        return questionRepository.findByDifficulty(difficulty, pageable)
                .map(this::convertToDTO);
    }

    /**
     * 搜索题目
     */
    public Page<QuestionDTO> searchQuestions(String keyword, Pageable pageable) {
        return questionRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }

    /**
     * 根据ID查询题目
     */
    public QuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        return convertToDTO(question);
    }

    /**
     * 随机获取一道题目
     */
    public QuestionDTO getRandomQuestion(Long categoryId, Question.Difficulty difficulty) {
        List<Question> questions;
        if (categoryId != null && difficulty != null) {
            questions = questionRepository.findByCategoryIdAndDifficulty(categoryId, difficulty, Pageable.unpaged()).getContent();
        } else if (categoryId != null) {
            questions = questionRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
        } else if (difficulty != null) {
            questions = questionRepository.findByDifficulty(difficulty, Pageable.unpaged()).getContent();
        } else {
            questions = questionRepository.findAll();
        }
        
        if (questions.isEmpty()) {
            return null;
        }
        
        int randomIndex = (int) (Math.random() * questions.size());
        return convertToDTO(questions.get(randomIndex));
    }

    /**
     * 创建题目
     */
    @Transactional
    public QuestionDTO createQuestion(CreateQuestionDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
        
        Question question = new Question();
        question.setTitle(dto.getTitle());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setCategory(category);
        
        // 设置题目类型
        if (dto.getType() != null) {
            question.setType(Question.QuestionType.valueOf(dto.getType()));
        } else {
            question.setType(Question.QuestionType.SINGLE_CHOICE);
        }
        
        // 设置难度
        if (dto.getDifficulty() != null) {
            question.setDifficulty(Question.Difficulty.valueOf(dto.getDifficulty()));
        }
        
        // 设置选项
        question.setOptions(dto.getOptions());
        
        return convertToDTO(questionRepository.save(question));
    }

    /**
     * 更新题目
     */
    @Transactional
    public QuestionDTO updateQuestion(Long id, CreateQuestionDTO dto) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        
        if (dto.getTitle() != null) {
            question.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            question.setContent(dto.getContent());
        }
        if (dto.getAnswer() != null) {
            question.setAnswer(dto.getAnswer());
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> BusinessException.notFound("分类不存在"));
            question.setCategory(category);
        }
        if (dto.getType() != null) {
            question.setType(Question.QuestionType.valueOf(dto.getType()));
        }
        if (dto.getDifficulty() != null) {
            question.setDifficulty(Question.Difficulty.valueOf(dto.getDifficulty()));
        }
        if (dto.getOptions() != null) {
            question.setOptions(dto.getOptions());
        }
        
        return convertToDTO(questionRepository.save(question));
    }

    /**
     * 删除题目
     */
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        questionRepository.delete(question);
    }

    /**
     * 实体转DTO
     */
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setAnswer(question.getAnswer());
        dto.setCategoryId(question.getCategory().getId());
        dto.setCategoryName(question.getCategory().getName());
        dto.setType(question.getType() != null ? question.getType().name() : null);
        dto.setOptions(question.getOptions());
        dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }
}
