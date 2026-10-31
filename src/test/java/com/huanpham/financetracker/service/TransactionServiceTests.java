package com.huanpham.financetracker.service;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.model.dto.ChartData;
import com.huanpham.financetracker.model.dto.FinancialSummary;
import com.huanpham.financetracker.model.dto.TransactionFilter;
import com.huanpham.financetracker.model.dto.TransactionForm;
import com.huanpham.financetracker.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TransactionServiceTests {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private TransactionRepository transactionRepository;

    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        salary = categoryService.findByType(TransactionType.INCOME).stream()
                .filter(c -> c.getName().equals("Lương")).findFirst().orElseThrow();
        food = categoryService.findByType(TransactionType.EXPENSE).stream()
                .filter(c -> c.getName().equals("Ăn uống")).findFirst().orElseThrow();
    }

    @Test
    void defaultCategoriesAreSeeded() {
        assertThat(categoryService.findByType(TransactionType.EXPENSE)).isNotEmpty();
        assertThat(categoryService.findByType(TransactionType.INCOME)).isNotEmpty();
    }

    @Test
    void createUpdateDeleteTransaction() {
        Transaction created = transactionService.save(form(null, "Ăn trưa", 50_000, TransactionType.EXPENSE, food, LocalDate.now()));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(TransactionType.EXPENSE);

        TransactionForm edit = TransactionForm.from(created);
        edit.setAmount(new BigDecimal("75000"));
        edit.setDescription("Ăn trưa văn phòng");
        transactionService.save(edit);

        Transaction updated = transactionService.findById(created.getId());
        assertThat(updated.getAmount()).isEqualByComparingTo("75000");
        assertThat(updated.getDescription()).isEqualTo("Ăn trưa văn phòng");

        transactionService.delete(created.getId());
        assertThatThrownBy(() -> transactionService.findById(created.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsCategoryOfWrongType() {
        TransactionForm form = form(null, "Sai loại", 10_000, TransactionType.INCOME, food, LocalDate.now());
        assertThatThrownBy(() -> transactionService.save(form)).isInstanceOf(BusinessException.class);
    }

    @Test
    void calculatesSummaryAndFiltersByMonthAndType() {
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);
        transactionService.save(form(null, "Lương", 10_000_000, TransactionType.INCOME, salary, thisMonth.atDay(1)));
        transactionService.save(form(null, "Ăn uống", 2_000_000, TransactionType.EXPENSE, food, thisMonth.atDay(1)));
        transactionService.save(form(null, "Lương cũ", 8_000_000, TransactionType.INCOME, salary, lastMonth.atDay(5)));

        FinancialSummary overall = transactionService.overallSummary();
        assertThat(overall.totalIncome()).isEqualByComparingTo("18000000");
        assertThat(overall.totalExpense()).isEqualByComparingTo("2000000");
        assertThat(overall.balance()).isEqualByComparingTo("16000000");

        List<Transaction> monthOnly = transactionService.search(new TransactionFilter(thisMonth, null, null, null));
        assertThat(monthOnly).hasSize(2);
        FinancialSummary monthly = TransactionService.summarize(monthOnly);
        assertThat(monthly.balance()).isEqualByComparingTo("8000000");
        assertThat(monthly.savingRate()).isEqualTo(80);

        List<Transaction> incomes = transactionService.search(new TransactionFilter(null, TransactionType.INCOME, null, null));
        assertThat(incomes).hasSize(2).allMatch(Transaction::isIncome);

        List<Transaction> keyword = transactionService.search(new TransactionFilter(null, null, null, "cũ"));
        assertThat(keyword).hasSize(1);
    }

    @Test
    void buildsChartData() {
        YearMonth thisMonth = YearMonth.now();
        transactionService.save(form(null, "Lương", 10_000_000, TransactionType.INCOME, salary, thisMonth.atDay(1)));
        transactionService.save(form(null, "Ăn uống", 2_000_000, TransactionType.EXPENSE, food, thisMonth.atDay(1)));

        List<Transaction> month = transactionService.search(new TransactionFilter(thisMonth, null, null, null));
        ChartData.CategoryBreakdown pie = statisticsService.breakdownByCategory(month, TransactionType.EXPENSE);
        assertThat(pie.labels()).containsExactly("Ăn uống");
        assertThat(pie.values().getFirst()).isEqualByComparingTo("2000000");

        ChartData.CashFlow flow = statisticsService.monthlyCashFlow(thisMonth, 12);
        assertThat(flow.labels()).hasSize(12);
        assertThat(flow.cumulativeBalance().getLast()).isEqualByComparingTo("8000000");

        ChartData.CashFlow daily = statisticsService.dailyCashFlow(thisMonth);
        assertThat(daily.labels()).hasSize(thisMonth.lengthOfMonth());
    }

    @Test
    void cannotDeleteCategoryInUse() {
        transactionService.save(form(null, "Ăn uống", 100_000, TransactionType.EXPENSE, food, LocalDate.now()));
        assertThatThrownBy(() -> categoryService.delete(food.getId())).isInstanceOf(BusinessException.class);
    }

    private static TransactionForm form(Long id, String description, long amount, TransactionType type,
                                        Category category, LocalDate date) {
        TransactionForm f = new TransactionForm();
        f.setId(id);
        f.setDescription(description);
        f.setAmount(BigDecimal.valueOf(amount));
        f.setType(type);
        f.setCategoryId(category.getId());
        f.setDate(date);
        return f;
    }
}
