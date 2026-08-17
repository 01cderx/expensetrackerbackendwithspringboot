package com.expensetracker.controller;

import com.expensetracker.dto.AccountDTO;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.getAll(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<AccountDTO> create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AccountDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id, @Valid @RequestBody AccountDTO dto) {
        return ResponseEntity.ok(service.update(principal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        service.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
