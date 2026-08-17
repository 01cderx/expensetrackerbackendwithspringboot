package com.expensetracker.service;

import com.expensetracker.dto.RecurringExpenseDTO;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.*;
import com.expensetracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    // Safety cap so a very old / long-neglected recurring item can't generate
    // thousands of rows in one request if the app hasn't run in a long time.
    private static final int MAX_CATCH_UP_ITERATIONS = 500;

    public List<RecurringExpenseDTO> getAllForUser(Long userId) {
        return recurringExpenseRepository.findByUserIdOrderByNextRunDateAsc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RecurringExpenseDTO create(Long userId, RecurringExpenseDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RecurringExpense recurring = new RecurringExpense();
        applyDto(recurring, dto, userId);
        recurring.setUser(user);
        recurring.setStartDate(dto.getStartDate());
        recurring.setNextRunDate(dto.getStartDate());
        recurring.setActive(true);

        return toDTO(recurringExpenseRepository.save(recurring));
    }

    public RecurringExpenseDTO update(Long userId, Long id, RecurringExpenseDTO dto) {
        RecurringExpense recurring = recurringExpenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        applyDto(recurring, dto, userId);
        return toDTO(recurringExpenseRepository.save(recurring));
    }

    public RecurringExpenseDTO toggleActive(Long userId, Long id) {
        RecurringExpense recurring = recurringExpenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        recurring.setActive(!recurring.isActive());
        return toDTO(recurringExpenseRepository.save(recurring));
    }

    public void delete(Long userId, Long id) {
        RecurringExpense recurring = recurringExpenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
        recurringExpenseRepository.delete(recurring);
    }

    /**
     * Materializes any due occurrences into real Expense rows, "catching up" on
     * however many periods have elapsed since the app last ran this check.
     *
     * This is called lazily from the expense/dashboard/recurring endpoints on every
     * request, rather than via a @Scheduled background job. A free-tier Render
     * instance goes to sleep after 15 minutes of inactivity, so a scheduled cron-style
     * job could simply never fire while the instance is asleep — checking on every
     * real request guarantees it always catches up, however long the gap was.
     */
    public void processDue(Long userId) {
        LocalDate today = LocalDate.now();
        List<RecurringExpense> due = recurringExpenseRepository
                .findByUserIdAndActiveTrueAndNextRunDateLessThanEqual(userId, today);

        if (due.isEmpty()) {
            return;
        }

        for (RecurringExpense recurring : due) {
            int iterations = 0;
            while (!recurring.getNextRunDate().isAfter(today) && iterations < MAX_CATCH_UP_ITERATIONS) {
                Expense expense = new Expense();
                expense.setTitle(recurring.getTitle());
                expense.setAmount(recurring.getAmount());
                expense.setDate(recurring.getNextRunDate());
                expense.setNotes(recurring.getNotes());
                expense.setType(recurring.getType());
                expense.setCategory(recurring.getCategory());
                expense.setPaymentMethod(recurring.getPaymentMethod());
                expense.setUser(recurring.getUser());
                expenseRepository.save(expense);

                recurring.setNextRunDate(advance(recurring.getNextRunDate(), recurring.getFrequency()));
                iterations++;
            }
        }

        recurringExpenseRepository.saveAll(due);
    }

    private LocalDate advance(LocalDate date, RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }

    private void applyDto(RecurringExpense recurring, RecurringExpenseDTO dto, Long userId) {
        recurring.setTitle(dto.getTitle());
        recurring.setAmount(dto.getAmount());
        recurring.setType(TransactionType.valueOf(dto.getType() == null ? "EXPENSE" : dto.getType().toUpperCase()));
        recurring.setFrequency(RecurrenceFrequency.valueOf(dto.getFrequency().toUpperCase()));
        recurring.setNotes(dto.getNotes());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            recurring.setCategory(category);
        } else {
            recurring.setCategory(null);
        }

        if (dto.getPaymentMethodId() != null) {
            PaymentMethod pm = paymentMethodRepository.findByIdAndUserId(dto.getPaymentMethodId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
            recurring.setPaymentMethod(pm);
        } else {
            recurring.setPaymentMethod(null);
        }
    }

    private RecurringExpenseDTO toDTO(RecurringExpense recurring) {
        RecurringExpenseDTO dto = new RecurringExpenseDTO();
        dto.setId(recurring.getId());
        dto.setTitle(recurring.getTitle());
        dto.setAmount(recurring.getAmount());
        dto.setType(recurring.getType().name());
        dto.setFrequency(recurring.getFrequency().name());
        dto.setStartDate(recurring.getStartDate());
        dto.setNextRunDate(recurring.getNextRunDate());
        dto.setActive(recurring.isActive());
        dto.setNotes(recurring.getNotes());
        if (recurring.getCategory() != null) {
            dto.setCategoryId(recurring.getCategory().getId());
            dto.setCategoryName(recurring.getCategory().getName());
            dto.setCategoryColor(recurring.getCategory().getColor());
        }
        if (recurring.getPaymentMethod() != null) {
            dto.setPaymentMethodId(recurring.getPaymentMethod().getId());
            dto.setPaymentMethodName(recurring.getPaymentMethod().getName());
        }
        return dto;
    }
}
