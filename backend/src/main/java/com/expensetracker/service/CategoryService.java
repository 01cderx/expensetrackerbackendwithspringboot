package com.expensetracker.service;

import com.expensetracker.dto.CategoryDTO;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<CategoryDTO> getAllForUser(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO create(Long userId, CategoryDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = new Category();
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category.setUser(user);

        return toDTO(categoryRepository.save(category));
    }

    public CategoryDTO update(Long userId, Long categoryId, CategoryDTO dto) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(dto.getName());
        category.setColor(dto.getColor());

        return toDTO(categoryRepository.save(category));
    }

    public void delete(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setColor(category.getColor());
        return dto;
    }
}
