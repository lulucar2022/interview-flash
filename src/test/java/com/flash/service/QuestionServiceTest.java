package com.flash.service;

import com.flash.common.exception.BusinessException;
import com.flash.dto.CreateQuestionDTO;
import com.flash.dto.QuestionDTO;
import com.flash.entity.Category;
import com.flash.entity.Question;
import com.flash.entity.Question.Difficulty;
import com.flash.entity.Question.QuestionType;
import com.flash.repository.CategoryRepository;
import com.flash.repository.QuestionRepository;
import com.flash.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserProgressRepository userProgressRepository;

    @InjectMocks
    private QuestionService questionService;

    private Question buildQuestion(Long id, String title, QuestionType type, Difficulty diff) {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Java基础");
        Question q = new Question();
        q.setId(id);
        q.setTitle(title);
        q.setContent("Content");
        q.setAnswer("Answer");
        q.setCategory(cat);
        q.setType(type);
        q.setDifficulty(diff);
        return q;
    }

    // ── getQuestionById ──

    @Test
    void getQuestionById_exists_returnsDTO() {
        Question q = buildQuestion(1L, "HashMap", QuestionType.SHORT_ANSWER, Difficulty.MEDIUM);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));

        QuestionDTO dto = questionService.getQuestionById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("HashMap", dto.getTitle());
        assertEquals("SHORT_ANSWER", dto.getType());
        assertEquals("MEDIUM", dto.getDifficulty());
    }

    @Test
    void getQuestionById_notExists_throwsException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> questionService.getQuestionById(99L));
    }

    // ── getRandomQuestions ──

    @Test
    void getRandomQuestions_emptyPool_returnsEmptyList() {
        when(questionRepository.findAll()).thenReturn(List.of());

        List<QuestionDTO> result = questionService.getRandomQuestions(10L, 5, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRandomQuestions_withCategoryFilter_delegatesToFilter() {
        Question q = buildQuestion(1L, "Q1", QuestionType.SHORT_ANSWER, Difficulty.MEDIUM);
        when(questionRepository.filterQuestions(1L, null, null)).thenReturn(List.of(q));
        when(userProgressRepository.findByUserId(10L)).thenReturn(List.of());
        when(userProgressRepository.findWrongQuestionsByUserId(10L)).thenReturn(List.of());

        List<QuestionDTO> result = questionService.getRandomQuestions(10L, 5, 1L, null, null);

        assertEquals(1, result.size());
        assertEquals("Q1", result.get(0).getTitle());
        verify(questionRepository).filterQuestions(1L, null, null);
    }

    @Test
    void getRandomQuestions_prioritizesUnpracticed() {
        Question q1 = buildQuestion(1L, "Practiced", QuestionType.SHORT_ANSWER, Difficulty.MEDIUM);
        Question q2 = buildQuestion(2L, "Unpracticed", QuestionType.SHORT_ANSWER, Difficulty.MEDIUM);

        when(questionRepository.findAll()).thenReturn(List.of(q1, q2));

        // Mock: user has practiced q1
        com.flash.entity.UserProgress up = new com.flash.entity.UserProgress();
        up.setQuestion(q1);
        when(userProgressRepository.findByUserId(10L)).thenReturn(List.of(up));
        when(userProgressRepository.findWrongQuestionsByUserId(10L)).thenReturn(List.of());

        List<QuestionDTO> result = questionService.getRandomQuestions(10L, 1, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Unpracticed", result.get(0).getTitle());
    }

    // ── createQuestion ──

    @Test
    void createQuestion_success() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Java基础");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(10L);
            return q;
        });

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setTitle("New Question");
        dto.setContent("Content");
        dto.setAnswer("Answer");
        dto.setCategoryId(1L);
        dto.setType("SINGLE_CHOICE");
        dto.setDifficulty("EASY");

        QuestionDTO result = questionService.createQuestion(dto);

        assertEquals("New Question", result.getTitle());
        assertEquals("SINGLE_CHOICE", result.getType());
        assertEquals("EASY", result.getDifficulty());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void createQuestion_categoryNotFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setTitle("Q");
        dto.setContent("C");
        dto.setCategoryId(99L);

        assertThrows(BusinessException.class, () -> questionService.createQuestion(dto));
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestion_nullType_defaultsToSingleChoice() {
        Category cat = new Category();
        cat.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(10L);
            return q;
        });

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setTitle("Q");
        dto.setContent("C");
        dto.setCategoryId(1L);
        // type is null

        QuestionDTO result = questionService.createQuestion(dto);

        assertEquals("SINGLE_CHOICE", result.getType());
    }

    // ── updateQuestion ──

    @Test
    void updateQuestion_partialUpdate_onlyUpdatesProvidedFields() {
        Question q = buildQuestion(1L, "Old Title", QuestionType.SHORT_ANSWER, Difficulty.MEDIUM);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(questionRepository.save(any(Question.class))).thenReturn(q);

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setTitle("New Title");
        // other fields null — should not change

        QuestionDTO result = questionService.updateQuestion(1L, dto);

        assertEquals("New Title", q.getTitle());
        assertEquals(Difficulty.MEDIUM, q.getDifficulty()); // unchanged
    }

    @Test
    void updateQuestion_notFound_throwsException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setTitle("Q");
        dto.setContent("C");
        dto.setCategoryId(1L);

        assertThrows(BusinessException.class, () -> questionService.updateQuestion(99L, dto));
    }

    // ── deleteQuestion ──

    @Test
    void deleteQuestion_exists_deletes() {
        Question q = buildQuestion(1L, "Q", QuestionType.SHORT_ANSWER, Difficulty.EASY);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));

        questionService.deleteQuestion(1L);

        verify(questionRepository).delete(q);
    }

    @Test
    void deleteQuestion_notFound_throwsException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> questionService.deleteQuestion(99L));
    }

    // ── getTotalCount ──

    @Test
    void getTotalCount_delegates() {
        when(questionRepository.count()).thenReturn(42L);
        assertEquals(42L, questionService.getTotalCount());
    }

    // ── searchQuestions ──

    @Test
    void searchQuestions_delegates() {
        Page<QuestionDTO> emptyPage = new PageImpl<>(List.of());
        when(questionRepository.searchByKeyword(eq("java"), any(Pageable.class))).thenReturn(Page.empty());

        Page<QuestionDTO> result = questionService.searchQuestions("java", Pageable.unpaged());

        assertNotNull(result);
        verify(questionRepository).searchByKeyword(eq("java"), any(Pageable.class));
    }
}
