package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.model.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ExpenseSpecifications {

    public static Specification<Expense> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Expense> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? null
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> hasType(TransactionType type) {
        return (root, query, cb) -> type == null
                ? null
                : cb.equal(root.get("type"), type);
    }

    public static Specification<Expense> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) return cb.between(root.get("date"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("date"), start);
            return cb.lessThanOrEqualTo(root.get("date"), end);
        };
    }

    public static Specification<Expense> titleContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
        };
    }
}
