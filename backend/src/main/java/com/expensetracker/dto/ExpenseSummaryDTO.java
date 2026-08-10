package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryDTO {
    private BigDecimal totalAllTime;
    private BigDecimal totalThisMonth;
    private Map<String, BigDecimal> byCategory;
}
