package com.expensetracker.controller;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.dto.ExpenseSummaryDTO;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                     @RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer size) {
        if (categoryId != null) {
            return ResponseEntity.ok(expenseService.getByCategory(principal.getId(), categoryId));
        }
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(expenseService.getByDateRange(principal.getId(), startDate, endDate));
        }
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 20;
        return ResponseEntity.ok(expenseService.getPaged(principal.getId(), resolvedPage, resolvedSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getById(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(principal.getId(), id));
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> create(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> update(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable Long id,
                                              @Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(expenseService.update(principal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
        expenseService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDTO> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(expenseService.getSummary(principal.getId()));
    }
}
