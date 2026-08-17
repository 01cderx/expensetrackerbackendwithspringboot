package com.expensetracker.repository;

import com.expensetracker.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByUserIdOrderByDateDesc(Long userId);
    Optional<Transfer> findByIdAndUserId(Long id, Long userId);
    boolean existsByFromAccountIdAndUserId(Long accountId, Long userId);
    boolean existsByToAccountIdAndUserId(Long accountId, Long userId);
}
