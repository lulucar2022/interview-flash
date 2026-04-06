package com.flash.service;

import com.flash.entity.Category;
import com.flash.exception.BusinessException;
import com.flash.repository.CategoryRepository;
import com.flash.repository.QuestionRepository;
import com.flash.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
        return convertToDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw BusinessException.badRequest("分类已存在");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
        
        if (name != null && !name.isEmpty()) {
            category.setName(name);
        }
        if (description != null) {
            category.setDescription(description);
        }
        
        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));
        categoryRepository.delete(category);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setQuestionCount(questionRepository.countByCategoryId(category.getId()));
        return dto;
    }
}
