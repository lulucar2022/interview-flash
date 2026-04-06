package com.flash.service;

import com.flash.entity.Category;
import com.flash.entity.Question;
import com.flash.exception.BusinessException;
import com.flash.repository.CategoryRepository;
import com.flash.repository.QuestionRepository;
import com.flash.dto.QuestionDTO;
import com.flash.dto.CreateQuestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    public Page<QuestionDTO> getQuestionsByCategory(Long categoryId, Pageable pageable) {
        return questionRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDTO);
    }

    public Page<QuestionDTO> getQuestionsByDifficulty(Question.Difficulty difficulty, Pageable pageable) {
        return questionRepository.findByDifficulty(difficulty, pageable)
                .map(this::convertToDTO);
    }

    public Page<QuestionDTO> searchQuestions(String keyword, Pageable pageable) {
        return questionRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }

    public QuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        return convertToDTO(question);
    }

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

    @Transactional
    public QuestionDTO createQuestion(CreateQuestionDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
        
        Question question = new Question();
        question.setTitle(dto.getTitle());
        question.setContent(dto.getContent());
        question.setAnswer(dto.getAnswer());
        question.setCategory(category);
        
        if (dto.getDifficulty() != null) {
            question.setDifficulty(Question.Difficulty.valueOf(dto.getDifficulty()));
        }
        
        return convertToDTO(questionRepository.save(question));
    }

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
        if (dto.getDifficulty() != null) {
            question.setDifficulty(Question.Difficulty.valueOf(dto.getDifficulty()));
        }
        
        return convertToDTO(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        questionRepository.delete(question);
    }

    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setAnswer(question.getAnswer());
        dto.setCategoryId(question.getCategory().getId());
        dto.setCategoryName(question.getCategory().getName());
        dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }
}
