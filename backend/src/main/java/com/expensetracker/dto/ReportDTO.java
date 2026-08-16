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
public class ReportDTO {
    private String periodLabel;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal net;
    private Map<String, BigDecimal> byCategory;
    private List<TrendPointDTO> trend;
}
