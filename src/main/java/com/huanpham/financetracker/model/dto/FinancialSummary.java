package com.huanpham.financetracker.model.dto;

import java.math.BigDecimal;

/**
 * Tổng hợp tài chính: tổng thu, tổng chi, số dư.
 */
public record FinancialSummary(BigDecimal totalIncome, BigDecimal totalExpense, long transactionCount) {

    public static FinancialSummary of(BigDecimal income, BigDecimal expense, long count) {
        return new FinancialSummary(
                income == null ? BigDecimal.ZERO : income,
                expense == null ? BigDecimal.ZERO : expense,
                count);
    }

    /** Số dư = Tổng thu - Tổng chi. */
    public BigDecimal balance() {
        return totalIncome.subtract(totalExpense);
    }

    public boolean isNegative() {
        return balance().signum() < 0;
    }

    /** Tỷ lệ tiết kiệm (%) = số dư / tổng thu. */
    public int savingRate() {
        if (totalIncome.signum() == 0) {
            return 0;
        }
        return balance().multiply(BigDecimal.valueOf(100))
                .divide(totalIncome, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
