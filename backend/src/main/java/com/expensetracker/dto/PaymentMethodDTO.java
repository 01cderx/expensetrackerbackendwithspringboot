package com.expensetracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentMethodDTO {
    private Long id;

    @NotBlank(message = "Payment method name is required")
    private String name;

    private String icon;
}
