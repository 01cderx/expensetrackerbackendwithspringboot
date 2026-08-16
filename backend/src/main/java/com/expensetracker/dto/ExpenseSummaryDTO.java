package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryDTO {
    private BigDecimal totalIncomeAllTime;
    private BigDecimal totalExpenseAllTime;
    private BigDecimal balanceAllTime;
    private BigDecimal totalIncomeThisMonth;
    private BigDecimal totalExpenseThisMonth;
    private BigDecimal balanceThisMonth;
    /** All-time expense breakdown by category (income is not categorized in this view). */
    private Map<String, BigDecimal> byCategory;
}
