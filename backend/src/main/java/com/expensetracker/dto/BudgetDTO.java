package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than 0")
    private BigDecimal monthlyLimit;

    // Computed server-side, read-only on responses — ignored if sent in a request
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentUsed;
    private boolean overBudget;
}
