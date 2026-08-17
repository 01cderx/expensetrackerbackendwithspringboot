package com.expensetracker.repository;

import com.expensetracker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserIdOrderByNameAsc(Long userId);
    Optional<Account> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(CASE WHEN e.type = 'INCOME' THEN e.amount ELSE 0 END), 0) FROM Expense e WHERE e.account.id = :accountId AND e.user.id = :userId AND e.date <= :today")
    BigDecimal sumIncome(@Param("accountId") Long accountId, @Param("userId") Long userId, @Param("today") java.time.LocalDate today);

    @Query("SELECT COALESCE(SUM(CASE WHEN e.type = 'EXPENSE' THEN e.amount ELSE 0 END), 0) FROM Expense e WHERE e.account.id = :accountId AND e.user.id = :userId AND e.date <= :today")
    BigDecimal sumExpenses(@Param("accountId") Long accountId, @Param("userId") Long userId, @Param("today") java.time.LocalDate today);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.toAccount.id = :accountId AND t.user.id = :userId AND t.date <= :today")
    BigDecimal sumIncomingTransfers(@Param("accountId") Long accountId, @Param("userId") Long userId, @Param("today") java.time.LocalDate today);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.fromAccount.id = :accountId AND t.user.id = :userId AND t.date <= :today")
    BigDecimal sumOutgoingTransfers(@Param("accountId") Long accountId, @Param("userId") Long userId, @Param("today") java.time.LocalDate today);
}
