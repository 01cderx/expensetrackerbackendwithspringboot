package com.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long id;

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    @Pattern(regexp = "CASH|BANK|UPI|CARD", message = "Type must be CASH, BANK, UPI or CARD")
    private String type;

    @NotNull(message = "Opening balance is required")
    private BigDecimal openingBalance = BigDecimal.ZERO;

    private boolean active = true;
    private BigDecimal balance;
}
