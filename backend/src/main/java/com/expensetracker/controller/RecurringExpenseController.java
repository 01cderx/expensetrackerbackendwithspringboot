package com.expensetracker.controller;

import com.expensetracker.dto.RecurringExpenseDTO;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    @GetMapping
    public ResponseEntity<List<RecurringExpenseDTO>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        recurringExpenseService.processDue(principal.getId());
        return ResponseEntity.ok(recurringExpenseService.getAllForUser(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<RecurringExpenseDTO> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody RecurringExpenseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringExpenseService.create(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringExpenseDTO> update(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody RecurringExpenseDTO dto) {
        return ResponseEntity.ok(recurringExpenseService.update(principal.getId(), id, dto));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringExpenseDTO> toggle(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(recurringExpenseService.toggleActive(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        recurringExpenseService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
