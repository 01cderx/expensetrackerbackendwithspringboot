package com.expensetracker.controller;

import com.expensetracker.dto.BudgetDTO;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(budgetService.getAllForUser(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> create(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody BudgetDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> update(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable Long id,
                                             @Valid @RequestBody BudgetDTO dto) {
        return ResponseEntity.ok(budgetService.update(principal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        budgetService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
