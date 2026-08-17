package com.expensetracker.service;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.dto.ExpenseSummaryDTO;
import com.expensetracker.dto.PagedResponse;
import com.expensetracker.dto.ReportDTO;
import com.expensetracker.dto.TrendPointDTO;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.ExpenseSpecifications;
import com.expensetracker.repository.PaymentMethodRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    public ExpenseDTO getById(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return toDTO(expense);
    }

    public ExpenseDTO create(Long userId, ExpenseDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = new Expense();
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setNotes(dto.getNotes());
        expense.setUser(user);
        expense.setType(parseType(dto.getType()));

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
        }

        if (dto.getPaymentMethodId() != null) {
            com.expensetracker.model.PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(dto.getPaymentMethodId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
            expense.setPaymentMethod(paymentMethod);
        }

        return toDTO(expenseRepository.save(expense));
    }

    public ExpenseDTO update(Long userId, Long expenseId, ExpenseDTO dto) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setDate(dto.getDate());
        expense.setNotes(dto.getNotes());
        expense.setType(parseType(dto.getType()));

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(dto.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
        } else {
            expense.setCategory(null);
        }

        if (dto.getPaymentMethodId() != null) {
            com.expensetracker.model.PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUserId(dto.getPaymentMethodId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
            expense.setPaymentMethod(paymentMethod);
        } else {
            expense.setPaymentMethod(null);
        }

        return toDTO(expenseRepository.save(expense));
    }

    public void delete(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    /**
     * Single unified listing endpoint: pagination + optional search text, category,
     * transaction type, and date range, all combinable.
     */
    public PagedResponse<ExpenseDTO> getPaged(Long userId, int page, int size, Long categoryId,
                                               String type, String search, LocalDate startDate, LocalDate endDate) {
        Specification<Expense> spec = Specification
                .where(ExpenseSpecifications.belongsToUser(userId))
                .and(ExpenseSpecifications.hasCategory(categoryId))
                .and(ExpenseSpecifications.hasType(parseTypeOrNull(type)))
                .and(ExpenseSpecifications.dateBetween(startDate, endDate))
                .and(ExpenseSpecifications.titleContains(search));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<Expense> result = expenseRepository.findAll(spec, pageable);

        List<ExpenseDTO> content = result.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public ExpenseSummaryDTO getSummary(Long userId) {
        BigDecimal totalIncomeAllTime = expenseRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
        BigDecimal totalExpenseAllTime = expenseRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
        BigDecimal balanceAllTime = totalIncomeAllTime.subtract(totalExpenseAllTime);

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        BigDecimal totalIncomeThisMonth = expenseRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.INCOME, start, end);
        BigDecimal totalExpenseThisMonth = expenseRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, start, end);
        BigDecimal balanceThisMonth = totalIncomeThisMonth.subtract(totalExpenseThisMonth);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Object[] row : expenseRepository.sumAmountGroupedByCategoryAndType(userId, TransactionType.EXPENSE)) {
            String categoryName = row[0] != null ? (String) row[0] : "Uncategorized";
            BigDecimal amount = (BigDecimal) row[1];
            byCategory.put(categoryName, amount);
        }

        return new ExpenseSummaryDTO(
                totalIncomeAllTime, totalExpenseAllTime, balanceAllTime,
                totalIncomeThisMonth, totalExpenseThisMonth, balanceThisMonth,
                byCategory
        );
    }

    /**
     * Monthly report (month provided) or yearly report (month omitted).
     * Computed in Java rather than SQL so it behaves identically across
     * H2/MySQL/Postgres and stays easy to follow.
     */
    public ReportDTO getReport(Long userId, int year, Integer month) {
        LocalDate start;
        LocalDate end;
        String periodLabel;
        boolean monthly = month != null;

        if (monthly) {
            if (month < 1 || month > 12) {
                throw new BadRequestException("Month must be between 1 and 12");
            }
            YearMonth ym = YearMonth.of(year, month);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
            periodLabel = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
        } else {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
            periodLabel = String.valueOf(year);
        }

        List<Expense> entries = expenseRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);

        BigDecimal totalIncome = entries.stream()
                .filter(e -> e.getType() == TransactionType.INCOME)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = entries.stream()
                .filter(e -> e.getType() == TransactionType.EXPENSE)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal net = totalIncome.subtract(totalExpense);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        entries.stream()
                .filter(e -> e.getType() == TransactionType.EXPENSE)
                .forEach(e -> {
                    String name = e.getCategory() != null ? e.getCategory().getName() : "Uncategorized";
                    byCategory.merge(name, e.getAmount(), BigDecimal::add);
                });

        List<TrendPointDTO> trend = monthly
                ? buildDailyTrend(entries, YearMonth.of(year, month))
                : buildMonthlyTrend(entries, year);

        return new ReportDTO(periodLabel, totalIncome, totalExpense, net, byCategory, trend);
    }

    private List<TrendPointDTO> buildDailyTrend(List<Expense> entries, YearMonth ym) {
        Map<Integer, TrendPointDTO> byDay = new LinkedHashMap<>();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            byDay.put(day, new TrendPointDTO(String.valueOf(day), BigDecimal.ZERO, BigDecimal.ZERO));
        }
        for (Expense e : entries) {
            int day = e.getDate().getDayOfMonth();
            TrendPointDTO point = byDay.get(day);
            if (e.getType() == TransactionType.INCOME) {
                point.setIncome(point.getIncome().add(e.getAmount()));
            } else {
                point.setExpense(point.getExpense().add(e.getAmount()));
            }
        }
        return List.copyOf(byDay.values());
    }

    private List<TrendPointDTO> buildMonthlyTrend(List<Expense> entries, int year) {
        Map<Integer, TrendPointDTO> byMonth = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            byMonth.put(m, new TrendPointDTO(label, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        for (Expense e : entries) {
            int m = e.getDate().getMonthValue();
            TrendPointDTO point = byMonth.get(m);
            if (e.getType() == TransactionType.INCOME) {
                point.setIncome(point.getIncome().add(e.getAmount()));
            } else {
                point.setExpense(point.getExpense().add(e.getAmount()));
            }
        }
        return List.copyOf(byMonth.values());
    }

    private TransactionType parseType(String type) {
        TransactionType parsed = parseTypeOrNull(type);
        return parsed != null ? parsed : TransactionType.EXPENSE;
    }

    private TransactionType parseTypeOrNull(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Type must be INCOME or EXPENSE");
        }
    }

    private ExpenseDTO toDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setDate(expense.getDate());
        dto.setNotes(expense.getNotes());
        dto.setType(expense.getType().name());
        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
            dto.setCategoryColor(expense.getCategory().getColor());
        }
        if (expense.getPaymentMethod() != null) {
            dto.setPaymentMethodId(expense.getPaymentMethod().getId());
            dto.setPaymentMethodName(expense.getPaymentMethod().getName());
        }
        return dto;
    }
}
