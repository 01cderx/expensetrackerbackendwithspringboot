package com.expensetracker.controller;

import com.expensetracker.dto.TransferDTO;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService service;

    @GetMapping
    public ResponseEntity<List<TransferDTO>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.getAll(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<TransferDTO> create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TransferDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(principal.getId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        service.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
