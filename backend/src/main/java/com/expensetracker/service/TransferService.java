package com.expensetracker.service;

import com.expensetracker.dto.TransferDTO;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Account;
import com.expensetracker.model.Transfer;
import com.expensetracker.model.User;
import com.expensetracker.repository.TransferRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository repository;
    private final AccountService accountService;
    private final UserRepository userRepository;

    public List<TransferDTO> getAll(Long userId) {
        return repository.findByUserIdOrderByDateDesc(userId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public TransferDTO create(Long userId, TransferDTO dto) {
        if (dto.getFromAccountId().equals(dto.getToAccountId())) {
            throw new BadRequestException("Source and destination accounts must be different");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account from = accountService.getOwned(userId, dto.getFromAccountId());
        Account to = accountService.getOwned(userId, dto.getToAccountId());
        if (!from.isActive() || !to.isActive()) throw new BadRequestException("Both accounts must be active");

        Transfer transfer = new Transfer();
        transfer.setUser(user);
        transfer.setFromAccount(from);
        transfer.setToAccount(to);
        transfer.setAmount(dto.getAmount());
        transfer.setDate(dto.getDate());
        transfer.setNotes(dto.getNotes());
        return toDTO(repository.save(transfer));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Transfer transfer = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        repository.delete(transfer);
    }

    private TransferDTO toDTO(Transfer t) {
        TransferDTO dto = new TransferDTO();
        dto.setId(t.getId());
        dto.setFromAccountId(t.getFromAccount().getId());
        dto.setFromAccountName(t.getFromAccount().getName());
        dto.setToAccountId(t.getToAccount().getId());
        dto.setToAccountName(t.getToAccount().getName());
        dto.setAmount(t.getAmount());
        dto.setDate(t.getDate());
        dto.setNotes(t.getNotes());
        return dto;
    }
}
