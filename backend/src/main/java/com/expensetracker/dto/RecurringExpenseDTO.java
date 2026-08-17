package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringExpenseDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Pattern(regexp = "INCOME|EXPENSE", message = "Type must be INCOME or EXPENSE")
    private String type = "EXPENSE";

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|YEARLY", message = "Frequency must be DAILY, WEEKLY, MONTHLY, or YEARLY")
    private String frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate nextRunDate;
    private boolean active = true;
    private String notes;

    private Long categoryId;
    private String categoryName;
    private String categoryColor;

    private Long paymentMethodId;
    private String paymentMethodName;
}
