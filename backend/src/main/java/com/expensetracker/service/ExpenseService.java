package com.expensetracker.service;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.dto.ExpenseSummaryDTO;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<ExpenseDTO> getAllForUser(Long userId) {
        return expenseRepository.findByUserIdOrderByDateDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getById(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return toDTO(expense);
    }

    public ExpenseDTO create(Long userId, ExpenseDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setNotes(dto.getNotes());
        expense.setUser(user);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
        }

        return toDTO(expenseRepository.save(expense));
    }

    public ExpenseDTO update(Long userId, Long expenseId, ExpenseDTO dto) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
        } else {
            expense.setCategory(null);
        }

        return toDTO(expenseRepository.save(expense));
    }

    public void delete(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public List<ExpenseDTO> getByCategory(Long userId, Long categoryId) {
        return expenseRepository.findByUserIdAndCategoryId(userId, categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getByDateRange(Long userId, LocalDate start, LocalDate end) {
        return expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExpenseSummaryDTO getSummary(Long userId) {
        BigDecimal totalAllTime = expenseRepository.sumAmountByUserId(userId);

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        BigDecimal totalThisMonth = expenseRepository.sumAmountByUserIdAndDateBetween(userId, start, end);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Object[] row : expenseRepository.sumAmountGroupedByCategory(userId)) {
            String categoryName = row[0] != null ? (String) row[0] : "Uncategorized";
            BigDecimal amount = (BigDecimal) row[1];
            byCategory.put(categoryName, amount);
        }

        return new ExpenseSummaryDTO(totalAllTime, totalThisMonth, byCategory);
    }

    private ExpenseDTO toDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setDate(expense.getDate());
        dto.setNotes(expense.getNotes());
        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
            dto.setCategoryColor(expense.getCategory().getColor());
        }
        return dto;
    }
}
