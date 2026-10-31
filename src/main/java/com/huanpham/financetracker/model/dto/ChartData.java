package com.huanpham.financetracker.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dữ liệu cho các biểu đồ Chart.js trên Dashboard.
 */
public final class ChartData {

    private ChartData() {
    }

    /** Biểu đồ tròn: chi tiêu (hoặc thu nhập) theo danh mục. */
    public record CategoryBreakdown(List<String> labels, List<BigDecimal> values, List<String> colors) {
        public boolean isEmpty() {
            return values.isEmpty();
        }
    }

    /** Biểu đồ dòng tiền: thu, chi, và số dư lũy kế theo thời gian. */
    public record CashFlow(List<String> labels,
                           List<BigDecimal> income,
                           List<BigDecimal> expense,
                           List<BigDecimal> cumulativeBalance) {
    }
}
