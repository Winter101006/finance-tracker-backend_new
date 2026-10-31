package com.huanpham.financetracker.service;

import com.huanpham.financetracker.model.Category;
import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.model.dto.ChartData;
import com.huanpham.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tính toán dữ liệu thống kê cho biểu đồ.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy");

    private final TransactionRepository transactionRepository;

    /**
     * Phân bổ số tiền theo danh mục (cho biểu đồ tròn), sắp xếp giảm dần.
     */
    public ChartData.CategoryBreakdown breakdownByCategory(Collection<Transaction> transactions, TransactionType type) {
        Map<Category, BigDecimal> totals = new LinkedHashMap<>();
        Map<Long, Category> byId = new LinkedHashMap<>();
        Map<Long, BigDecimal> sums = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() != type) {
                continue;
            }
            Category c = t.getCategory();
            byId.putIfAbsent(c.getId(), c);
            sums.merge(c.getId(), t.getAmount(), BigDecimal::add);
        }
        sums.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> totals.put(byId.get(e.getKey()), e.getValue()));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        totals.forEach((category, amount) -> {
            labels.add(category.getName());
            values.add(amount);
            colors.add(category.getColor());
        });
        return new ChartData.CategoryBreakdown(labels, values, colors);
    }

    /**
     * Dòng tiền theo tháng trong {@code months} tháng gần nhất, kết thúc tại {@code endMonth}.
     * Số dư lũy kế bao gồm cả số dư trước giai đoạn hiển thị.
     */
    public ChartData.CashFlow monthlyCashFlow(YearMonth endMonth, int months) {
        YearMonth startMonth = endMonth.minusMonths(months - 1L);
        LocalDate from = startMonth.atDay(1);
        LocalDate to = endMonth.atEndOfMonth();

        Map<YearMonth, BigDecimal[]> buckets = new TreeMap<>();
        for (YearMonth m = startMonth; !m.isAfter(endMonth); m = m.plusMonths(1)) {
            buckets.put(m, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (Transaction t : transactionRepository.findByDateBetweenOrderByDateAsc(from, to)) {
            BigDecimal[] bucket = buckets.get(YearMonth.from(t.getDate()));
            int idx = t.getType() == TransactionType.INCOME ? 0 : 1;
            bucket[idx] = bucket[idx].add(t.getAmount());
        }

        BigDecimal running = transactionRepository.sumByTypeBefore(TransactionType.INCOME, from)
                .subtract(transactionRepository.sumByTypeBefore(TransactionType.EXPENSE, from));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> income = new ArrayList<>();
        List<BigDecimal> expense = new ArrayList<>();
        List<BigDecimal> cumulative = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal[]> e : buckets.entrySet()) {
            BigDecimal in = e.getValue()[0];
            BigDecimal out = e.getValue()[1];
            running = running.add(in).subtract(out);
            labels.add(e.getKey().format(MONTH_LABEL));
            income.add(in);
            expense.add(out);
            cumulative.add(running);
        }
        return new ChartData.CashFlow(labels, income, expense, cumulative);
    }

    /**
     * Dòng tiền theo ngày trong một tháng.
     */
    public ChartData.CashFlow dailyCashFlow(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        BigDecimal[][] days = new BigDecimal[month.lengthOfMonth()][];
        for (int i = 0; i < days.length; i++) {
            days[i] = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }
        for (Transaction t : transactionRepository.findByDateBetweenOrderByDateAsc(from, to)) {
            BigDecimal[] bucket = days[t.getDate().getDayOfMonth() - 1];
            int idx = t.getType() == TransactionType.INCOME ? 0 : 1;
            bucket[idx] = bucket[idx].add(t.getAmount());
        }

        BigDecimal running = transactionRepository.sumByTypeBefore(TransactionType.INCOME, from)
                .subtract(transactionRepository.sumByTypeBefore(TransactionType.EXPENSE, from));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> income = new ArrayList<>();
        List<BigDecimal> expense = new ArrayList<>();
        List<BigDecimal> cumulative = new ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            running = running.add(days[i][0]).subtract(days[i][1]);
            labels.add(String.format("%02d/%02d", i + 1, month.getMonthValue()));
            income.add(days[i][0]);
            expense.add(days[i][1]);
            cumulative.add(running);
        }
        return new ChartData.CashFlow(labels, income, expense, cumulative);
    }
}
