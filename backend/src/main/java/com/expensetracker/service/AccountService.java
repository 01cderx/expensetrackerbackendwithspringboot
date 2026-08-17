package com.expensetracker.service;

import com.expensetracker.dto.AccountDTO;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Account;
import com.expensetracker.model.AccountType;
import com.expensetracker.model.User;
import com.expensetracker.repository.AccountRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.TransferRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final TransferRepository transferRepository;

    public List<AccountDTO> getAll(Long userId) {
        return accountRepository.findByUserIdOrderByNameAsc(userId).stream().map(a -> toDTO(a, userId)).toList();
    }

    @Transactional
    public AccountDTO create(Long userId, AccountDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = new Account();
        account.setName(dto.getName().trim());
        account.setType(parseType(dto.getType()));
        account.setOpeningBalance(dto.getOpeningBalance() == null ? BigDecimal.ZERO : dto.getOpeningBalance());
        account.setActive(dto.isActive());
        account.setUser(user);
        return toDTO(accountRepository.save(account), userId);
    }

    @Transactional
    public AccountDTO update(Long userId, Long id, AccountDTO dto) {
        Account account = getOwned(userId, id);
        account.setName(dto.getName().trim());
        account.setType(parseType(dto.getType()));
        account.setOpeningBalance(dto.getOpeningBalance() == null ? BigDecimal.ZERO : dto.getOpeningBalance());
        account.setActive(dto.isActive());
        return toDTO(accountRepository.save(account), userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        if (expenseRepository.existsByAccountIdAndUserId(id, userId)
                || transferRepository.existsByFromAccountIdAndUserId(id, userId)
                || transferRepository.existsByToAccountIdAndUserId(id, userId)) {
            throw new BadRequestException("Cannot delete an account that has transactions or transfers. Deactivate it instead.");
        }
        accountRepository.deleteById(id);
    }

    public Account getOwned(Long userId, Long id) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public BigDecimal balance(Long userId, Long accountId) {
        Account a = getOwned(userId, accountId);
        return calculateBalance(a, userId);
    }

    private BigDecimal calculateBalance(Account a, Long userId) {
        LocalDate today = LocalDate.now();
        return a.getOpeningBalance()
                .add(accountRepository.sumIncome(a.getId(), userId, today))
                .subtract(accountRepository.sumExpenses(a.getId(), userId, today))
                .add(accountRepository.sumIncomingTransfers(a.getId(), userId, today))
                .subtract(accountRepository.sumOutgoingTransfers(a.getId(), userId, today));
    }

    private AccountDTO toDTO(Account a, Long userId) {
        AccountDTO dto = new AccountDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setType(a.getType().name());
        dto.setOpeningBalance(a.getOpeningBalance());
        dto.setActive(a.isActive());
        dto.setBalance(calculateBalance(a, userId));
        return dto;
    }

    private AccountType parseType(String value) {
        try { return AccountType.valueOf(value.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Type must be CASH, BANK, UPI or CARD"); }
    }
}
