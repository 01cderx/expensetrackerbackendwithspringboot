package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :start AND :end")
    BigDecimal sumAmountByUserIdAndDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category.name, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category.name")
    List<Object[]> sumAmountGroupedByCategory(@Param("userId") Long userId);
}
