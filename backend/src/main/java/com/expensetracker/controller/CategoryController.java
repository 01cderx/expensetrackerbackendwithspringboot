package com.expensetracker.controller;

import com.expensetracker.dto.CategoryDTO;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(categoryService.getAllForUser(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody CategoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(principal.getId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long id,
                                               @Valid @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.update(principal.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id) {
        categoryService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
