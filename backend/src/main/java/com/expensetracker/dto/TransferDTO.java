package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransferDTO {
    private Long id;

    @NotNull(message = "Source account is required")
    private Long fromAccountId;
    private String fromAccountName;

    @NotNull(message = "Destination account is required")
    private Long toAccountId;
    private String toAccountName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String notes;
}
