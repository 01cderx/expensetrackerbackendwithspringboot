package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = :type AND e.date BETWEEN :start AND :end")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(@Param("userId") Long userId, @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category.name, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.type = :type GROUP BY e.category.name")
    List<Object[]> sumAmountGroupedByCategoryAndType(@Param("userId") Long userId, @Param("type") TransactionType type);
}
