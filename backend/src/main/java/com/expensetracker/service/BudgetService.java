package com.expensetracker.service;

import com.expensetracker.dto.BudgetDTO;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public List<BudgetDTO> getAllForUser(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BudgetDTO create(Long userId, BudgetDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            if (budgetRepository.findByUserIdAndCategoryId(userId, dto.getCategoryId()).isPresent()) {
                throw new BadRequestException("A budget already exists for this category");
            }
        } else if (budgetRepository.findByUserIdAndCategoryIsNull(userId).isPresent()) {
            throw new BadRequestException("An overall budget already exists — edit it instead");
        }

        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setMonthlyLimit(dto.getMonthlyLimit());
        budget.setUser(user);

        return toDTO(budgetRepository.save(budget));
    }

    public BudgetDTO update(Long userId, Long id, BudgetDTO dto) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budget.setMonthlyLimit(dto.getMonthlyLimit());
        return toDTO(budgetRepository.save(budget));
    }

    public void delete(Long userId, Long id) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    private BudgetDTO toDTO(Budget budget) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        BigDecimal spent = budget.getCategory() != null
                ? expenseRepository.sumExpenseByUserIdAndCategoryAndDateBetween(
                        budget.getUser().getId(), budget.getCategory().getId(), start, end)
                : expenseRepository.sumAmountByUserIdAndTypeAndDateBetween(
                        budget.getUser().getId(), TransactionType.EXPENSE, start, end);

        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        double percentUsed = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        if (budget.getCategory() != null) {
            dto.setCategoryId(budget.getCategory().getId());
            dto.setCategoryName(budget.getCategory().getName());
            dto.setCategoryColor(budget.getCategory().getColor());
        } else {
            dto.setCategoryName("Overall");
        }
        dto.setMonthlyLimit(budget.getMonthlyLimit());
        dto.setSpent(spent);
        dto.setRemaining(remaining);
        dto.setPercentUsed(percentUsed);
        dto.setOverBudget(spent.compareTo(budget.getMonthlyLimit()) > 0);
        return dto;
    }
}
