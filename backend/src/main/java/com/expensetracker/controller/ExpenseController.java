package com.expensetracker.controller;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.dto.ExpenseSummaryDTO;
import com.expensetracker.dto.PagedResponse;
import com.expensetracker.dto.ReportDTO;
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

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseDTO>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                expenseService.getPaged(principal.getId(), page, size, categoryId, type, search, startDate, endDate)
        );
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

    /**
     * Monthly report: /api/expenses/reports?year=2026&month=8
     * Yearly report:  /api/expenses/reports?year=2026
     */
    @GetMapping("/reports")
    public ResponseEntity<ReportDTO> getReport(@AuthenticationPrincipal UserPrincipal principal,
                                                @RequestParam int year,
                                                @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(expenseService.getReport(principal.getId(), year, month));
    }
}
