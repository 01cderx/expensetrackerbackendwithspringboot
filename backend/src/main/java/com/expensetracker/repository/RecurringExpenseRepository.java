package com.expensetracker.repository;

import com.expensetracker.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    List<RecurringExpense> findByUserIdOrderByNextRunDateAsc(Long userId);
    Optional<RecurringExpense> findByIdAndUserId(Long id, Long userId);
    List<RecurringExpense> findByUserIdAndActiveTrueAndNextRunDateLessThanEqual(Long userId, LocalDate date);
}
