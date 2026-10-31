package com.huanpham.financetracker.controller;

import com.huanpham.financetracker.model.Transaction;
import com.huanpham.financetracker.model.TransactionType;
import com.huanpham.financetracker.model.dto.TransactionFilter;
import com.huanpham.financetracker.service.StatisticsService;
import com.huanpham.financetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.List;

/**
 * Trang tổng quan: thẻ thống kê + biểu đồ.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private static final int CASH_FLOW_MONTHS = 12;

    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                            Model model) {
        YearMonth selected = month == null ? YearMonth.now() : month;
        List<Transaction> monthTransactions = transactionService.search(
                new TransactionFilter(selected, null, null, null));

        model.addAttribute("selectedMonth", selected);
        model.addAttribute("prevMonth", selected.minusMonths(1));
        model.addAttribute("nextMonth", selected.plusMonths(1));
        model.addAttribute("overall", transactionService.overallSummary());
        model.addAttribute("monthly", TransactionService.summarize(monthTransactions));
        model.addAttribute("expenseByCategory",
                statisticsService.breakdownByCategory(monthTransactions, TransactionType.EXPENSE));
        model.addAttribute("incomeByCategory",
                statisticsService.breakdownByCategory(monthTransactions, TransactionType.INCOME));
        model.addAttribute("monthlyCashFlow", statisticsService.monthlyCashFlow(selected, CASH_FLOW_MONTHS));
        model.addAttribute("dailyCashFlow", statisticsService.dailyCashFlow(selected));
        model.addAttribute("recentTransactions", transactionService.recent());
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
